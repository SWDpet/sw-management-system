#!/usr/bin/env bash
# =============================================================================
# analyze-traces.sh — codex-trace.jsonl 통계 출력.
# (sprint harness-hardening-v1 — Phase 3-2)
#
# 사용:
#   bash .team-workspace/analyze-traces.sh rounds        # 스프린트별 round 분포
#   bash .team-workspace/analyze-traces.sh verdicts      # ⭕/⚠/❌/N/A 분포
#   bash .team-workspace/analyze-traces.sh latency       # 모델별 평균 latency + 누적 비용
#   bash .team-workspace/analyze-traces.sh evolution     # 스프린트별 final verdict
#   bash .team-workspace/analyze-traces.sh all           # 위 4개 모두
# =============================================================================
set -eo pipefail

# jq 자동 탐색 (codex-trace.sh 와 동일)
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
SUB="${1:-all}"

if ! ls "$TRACE_DIR"/codex-trace-*.jsonl >/dev/null 2>&1; then
    echo "ℹ trace 파일 없음 — codex-trace.sh 로 1회 이상 호출 후 다시 시도하세요."
    echo "  ($TRACE_DIR/codex-trace-YYYY-MM.jsonl)"
    exit 0
fi

ALL_FILES=("$TRACE_DIR"/codex-trace-*.jsonl)
TOTAL_LINES=$(cat "${ALL_FILES[@]}" | wc -l)

echo "── trace 통계 (총 ${TOTAL_LINES} 호출, 파일 ${#ALL_FILES[@]}개) ─────────"

run_rounds() {
    echo
    echo "▶ 스프린트별 round 분포:"
    cat "${ALL_FILES[@]}" | jq -r '"\(.sprint) round=\(.round)"' | sort | uniq -c | sort -rn
}

run_verdicts() {
    echo
    echo "▶ verdict 분포:"
    cat "${ALL_FILES[@]}" | jq -r '.verdict' | sort | uniq -c | sort -rn
}

run_latency() {
    echo
    echo "▶ 모델별 평균 latency + 누적 비용 (USD):"
    cat "${ALL_FILES[@]}" | jq -s '
      group_by(.model) | map({
        model: .[0].model,
        count: length,
        avgMs: ((map(.durationMs) | add) / length | floor),
        totalCostUsd: (map(.costUsd // 0) | add)
      })
    '
}

run_evolution() {
    echo
    echo "▶ 스프린트별 final verdict (최종 round 기준):"
    cat "${ALL_FILES[@]}" | jq -s '
      group_by(.sprint) | map({
        sprint: .[0].sprint,
        rounds: length,
        finalVerdict: (sort_by(.round) | last | .verdict)
      })
    '
}

case "$SUB" in
    rounds)    run_rounds ;;
    verdicts)  run_verdicts ;;
    latency)   run_latency ;;
    evolution) run_evolution ;;
    all)
        run_rounds
        run_verdicts
        run_latency
        run_evolution
        ;;
    *)
        echo "❌ 알 수 없는 sub-command: $SUB"
        echo "사용 가능: rounds | verdicts | latency | evolution | all"
        exit 1
        ;;
esac
