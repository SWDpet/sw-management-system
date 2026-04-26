#!/usr/bin/env bash
# =============================================================================
# codex-trace.sh — codex CLI 호출 wrapper. 각 호출을 JSONL 로 기록.
# (sprint harness-hardening-v1 — Phase 3-1)
#
# 사용:
#   bash .team-workspace/codex-trace.sh -m gpt-5 "프롬프트 한 줄"
#   echo "긴 프롬프트" | bash .team-workspace/codex-trace.sh -m gpt-5
#   cat docs/spec.md | TRACE_SPRINT=spec-v1 TRACE_ROUND=1 \
#       bash .team-workspace/codex-trace.sh -m gpt-5 "검토 요청"
#
# 환경변수 (선택):
#   TRACE_MODEL                 — 모델명 (jsonl 기록용. -m 인자 자동파싱 안 함)
#   TRACE_SPRINT                — 스프린트 명 (예: harness-hardening-v1)
#   TRACE_ROUND                 — 검토 round 번호 (정수)
#   TRACE_TAGS                  — 콤마 구분 태그 (예: "기획,v1")
#   TRACE_PRICE_PER_1K_INPUT    — 입력 1K 토큰 가격 (USD)
#   TRACE_PRICE_PER_1K_OUTPUT   — 출력 1K 토큰 가격 (USD)
#   TRACE_VERDICT               — verdict 수동 override (자동 추출 대신)
#
# 출력: ${HOME}/.claude/trace/codex-trace-YYYY-MM.jsonl (월별 롤링)
# =============================================================================
set -eo pipefail   # D2: pipefail

# jq 자동 탐색 — Git Bash PATH 가 winget 설치 jq 를 못 잡는 경우 fallback
if ! command -v jq >/dev/null 2>&1; then
    WINGET_JQ=$(find "$HOME/AppData/Local/Microsoft/WinGet/Packages" -name "jq.exe" 2>/dev/null | head -1)
    if [ -n "$WINGET_JQ" ]; then
        export PATH="$(dirname "$WINGET_JQ"):$PATH"
    else
        echo "❌ jq 미설치 — Windows: winget install jqlang.jq, macOS: brew install jq" >&2
        exit 127
    fi
fi

TRACE_DIR="${HOME}/.claude/trace"
mkdir -p "$TRACE_DIR"
TRACE_FILE="$TRACE_DIR/codex-trace-$(date +%Y-%m).jsonl"

# D4: %3N 미지원 fallback
now_ms() {
    local v
    v=$(date +%s%3N 2>/dev/null) || true
    if [[ "$v" =~ ^[0-9]+$ ]] && [ ${#v} -ge 13 ]; then
        echo "$v"
    else
        echo $(($(date +%s) * 1000))
    fi
}
TS_START=$(now_ms)
TS_ISO=$(date -u +%Y-%m-%dT%H:%M:%SZ)

# D1: STDIN 감지 — TTY 가 아니면 입력 있음 (파이프/리다이렉션/heredoc 모두 처리)
TMP_IN=$(mktemp)
trap 'rm -f "$TMP_IN" "${TMP_OUT:-}"' EXIT
if [ ! -t 0 ]; then
    cat > "$TMP_IN"
fi
PROMPT_BYTES=$(wc -c < "$TMP_IN")
PROMPT_HASH="sha256:$(sha256sum "$TMP_IN" | cut -d' ' -f1)"

# D2: 파이프 제거 → 입력 리다이렉션. set +e/-e 로 codex 종료코드 보존.
TMP_OUT=$(mktemp)
set +e
codex exec "$@" < "$TMP_IN" > "$TMP_OUT" 2>&1
EXIT_CODE=$?
set -e

TS_END=$(now_ms)
DUR_MS=$((TS_END - TS_START))
RESP_BYTES=$(wc -c < "$TMP_OUT")

# token / cost 추정
TOK_IN=$((PROMPT_BYTES / 4))
TOK_OUT=$((RESP_BYTES / 4))
COST="null"
if [ -n "${TRACE_PRICE_PER_1K_INPUT:-}" ] && [ -n "${TRACE_PRICE_PER_1K_OUTPUT:-}" ]; then
    COST=$(awk -v ti="$TOK_IN" -v to="$TOK_OUT" \
        -v pi="$TRACE_PRICE_PER_1K_INPUT" -v po="$TRACE_PRICE_PER_1K_OUTPUT" \
        'BEGIN { printf "%.6f", (ti*pi + to*po) / 1000 }')
fi

# verdict 자동 추출 (마지막 50줄)
VERDICT="N/A"
VERDICT_EXTRACTED="false"
if [ -z "${TRACE_VERDICT:-}" ]; then
    V=$(tail -50 "$TMP_OUT" | grep -oE '최종[^\n]*[⭕⚠❌]' | tail -1 | grep -oE '[⭕⚠❌]' | tail -1 || true)
    if [ -n "$V" ]; then
        VERDICT="$V"
        VERDICT_EXTRACTED="true"
    fi
else
    VERDICT="$TRACE_VERDICT"   # override → verdictExtracted=false
fi

# JSONL append (D3: tags 빈값 처리)
jq -nc \
    --arg ts "$TS_ISO" \
    --arg model "${TRACE_MODEL:-unknown}" \
    --arg ph "$PROMPT_HASH" \
    --argjson pb "$PROMPT_BYTES" \
    --argjson rb "$RESP_BYTES" \
    --argjson ti "$TOK_IN" \
    --argjson to "$TOK_OUT" \
    --argjson cost "$COST" \
    --argjson dms "$DUR_MS" \
    --arg verdict "$VERDICT" \
    --argjson vex "$VERDICT_EXTRACTED" \
    --arg sprint "${TRACE_SPRINT:-unknown}" \
    --argjson round "${TRACE_ROUND:-0}" \
    --arg tags "${TRACE_TAGS:-}" \
    '{ts:$ts, model:$model, promptHash:$ph, promptBytes:$pb, responseBytes:$rb,
      tokensIn:$ti, tokensOut:$to, costUsd:$cost, durationMs:$dms,
      verdict:$verdict, verdictExtracted:$vex,
      sprint:$sprint, round:$round,
      tags:($tags|select(length>0)|split(",") // [])}' >> "$TRACE_FILE"

cat "$TMP_OUT"
exit $EXIT_CODE
