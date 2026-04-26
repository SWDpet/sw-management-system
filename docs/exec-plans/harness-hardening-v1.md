---
tags: [dev-plan, sprint, harness, safety, observability]
sprint: "harness-hardening-v1"
status: draft-v2
created: "2026-04-26"
revised: "2026-04-26"
---

# [개발계획서] harness 보강 v1 — Trigger + Prod DB + Trace — v2

- **작성팀**: 개발팀
- **작성일**: 2026-04-26 (v1/v2 동일자)
- **근거 기획서**: [[../product-specs/harness-hardening-v1|기획서 v2]] (codex ⭕ 통과 + 사용자 최종승인)
- **상태**: 초안 v2 (codex 재검토 대기) — v1 검토 ⚠ 보완 8건 모두 반영
- **목표**: 기획서 §FR-1~3, §NFR-1~4, §R-1~5 전부 구현. 단일 PR 머지.

### v1 → v2 변경점

| # | 분류 | 항목 |
|---|------|------|
| **D1** | 스크립트 critical | `codex-trace.sh` STDIN 감지 → `if [ ! -t 0 ]` |
| **D2** | 스크립트 critical | 파이프 제거 + `< "$TMP_IN"` + `set -o pipefail` (종료코드 보존) |
| **D3** | 스크립트 minor | tags 빈값 `[""]` 방지 — `select(length>0) // []` |
| **D4** | 스크립트 minor | `date +%s%3N` 폴백 (Git Bash 미보장) |
| **D5** | 가이드 | `AGENTS.md` 에 "로컬 실행 전 `.env` 작성" 3줄 + `.env.example` 참조 |
| **D6** | 롤백 | 병합 전략별 (squash/merge-commit/rebase) revert 방법 병기 |
| **D7** | commit 분할 | `docs/AGENT_SAFETY.md` 분리 → 5 commit 으로 (정책 전용) |
| **D8** | T-6 일관성 | grep 메인/디버그 출력 모두 동일 `--include`/`--exclude` |

---

## 0. 사전 조건

- 본 스프린트는 **코드 변경 0건** (Java/프런트). 도구·문서·메모리만 변경.
- 디자인팀 자문 D 정책 (skip) — UI 키워드 없음 (`템플릿`, `css`, `폰트`, `색상` 모두 부재)
- 설치 도구 추가: 없음 (기존 git/jq/sha256sum/openssl 만 사용)
- Git Bash 환경 (현재 IUHOME) 만 지원 — Windows native PowerShell 별도 .ps1 비범위

---

## 1. 작업 순서 (Phase / Step)

### Phase 1 — Trigger hardening (FR-1)

#### Step 1-1. `feedback_trigger_confirm.md` 메모리 룰 작성 (project 메모리)
- 위치: `~/.claude/projects/C--Users-ukjin-sw-management-system/memory/feedback_trigger_confirm.md`
- 내용: 기획서 §FR-1-b/1-c/1-d/1-e 본문 압축 + 트리거 판별 룰 표 + 5단계 알고리즘 + 빠른모드 토글
- type: `feedback`
- description: "작업완료/승인 트리거 발화 시 echo + confirm 강제, 저위험 트리거는 single-shot, 빠른모드 토글"

#### Step 1-2. `feedback_dev_env_sync.md` 시크릿 검증 cancel 룰 추가
- 기존 메모리에 §FR-1-c 시크릿 마스킹 검증 fail → 자동 cancel 절차 추가 (1 섹션 신설)

#### Step 1-3. `MEMORY.md` 인덱스에 `feedback_trigger_confirm` 추가 (project 메모리 측)
- 기존 user 메모리 인덱스 (`~/.claude/projects/C--Users-ukjin/memory/MEMORY.md`) 외, project 메모리 인덱스 (`~/.claude/projects/C--Users-ukjin-sw-management-system/memory/MEMORY.md`) 도 신설/갱신

#### Step 1-4. `AGENTS.md` §5 자동화 규칙 갱신
- 기존 "작업완료" 발화 → 즉시 commit/push → confirm echo 후 진행으로 변경
- 새 §5-1 "트리거 안전 게이트" 섹션 추가: 기획 §FR-1-b/1-d echo 산출 명령 (`git status --porcelain`, `git log --oneline origin/master..HEAD`) 그대로 인용
- `TRIGGER_FAST_MODE=1` 빠른모드 안내 1줄

### Phase 2 — Prod DB 분리 (FR-2)

#### Step 2-1. `docs/AGENT_SAFETY.md` 신규 (FR-2-b)
- 신규 파일. 다음 섹션:
  - §1 DB 작업 read-only 가드 (SELECT 만 자동, UPDATE/DELETE/DROP/ALTER 는 명시 승인)
  - §2 마이그레이션 / DDL 변경 — "마이그레이션 진행" 발화 + 2단계 confirm
  - §3 Ephemeral test DB docker-compose 가이드 (예시 yaml 만, 실제 셋업은 사용자 결정)
  - §4 시크릿 안전 채널 (1Password/Bitwarden) 권장
- AGENTS.md 에 1줄 링크 추가

#### Step 2-2. `application.properties` DB 좌표 마스킹
- 변경:
  - 기존: `spring.datasource.url=${DB_URL:jdbc:postgresql://211.104.137.55:5881/SW_Dept}`
  - 변경: `spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/swdept_local}`
- 머리 주석 갱신: "실제 prod 좌표는 환경변수 DB_URL 또는 1Password 에서 조회. localhost:5432/swdept_local 은 dummy default — prod 사고 차단"

#### Step 2-3. `AGENTS.md` / `CLAUDE.md` prod IP 마스킹 + .env 가이드 (D5)
- 기존 `211.104.137.55:5881/SW_Dept` 표기를 `${DB_HOST}:${DB_PORT}/${DB_NAME}` 으로 일괄 교체
- 운영자 주석에 "실제 좌표는 환경변수 또는 1Password" 명시
- **AGENTS.md §1 프로젝트 요약 끝에 3줄 추가**:
  ```
  ### 로컬 실행 전 환경변수
  - `.env.example` 참고하여 `.env` 작성 (DB_URL/DB_USERNAME/DB_PASSWORD/OPENAI_API_KEY)
  - 또는 환경변수 직접 export. server-restart.sh 가 Windows User env 에서 DB_PASSWORD 자동 로드
  - .env 파일은 .gitignore — 절대 commit X
  ```

#### Step 2-4. `.env.example` 신규 (T-8b)
- 위치: 리포 루트 `.env.example`
- 내용:
  ```bash
  # 본 파일은 placeholder. 실제 값은 .env 또는 환경변수로 주입 (gitignore 됨)
  DB_URL=jdbc:postgresql://YOUR_DB_HOST:5432/YOUR_DB_NAME
  DB_USERNAME=postgres
  DB_PASSWORD=YOUR_PASSWORD_HERE
  OPENAI_API_KEY=YOUR_OPENAI_KEY_HERE
  ```
- `.gitignore` 에 `.env` 추가 (이미 있는지 확인 후)

### Phase 3 — LLM trace JSONL (FR-3)

#### Step 3-1. `.team-workspace/codex-trace.sh` 신규 wrapper
- bash 스크립트. 핵심 로직 (v2 — D1~D4 보완):
  ```bash
  #!/usr/bin/env bash
  # codex 호출 wrapper. 실행 결과를 ~/.claude/trace/codex-trace-YYYY-MM.jsonl 에 append.
  # 사용: bash codex-trace.sh -m gpt-5 "프롬프트"  또는  echo "..." | bash codex-trace.sh -m gpt-5
  set -eo pipefail   # D2: pipefail 추가
  TRACE_DIR="${HOME}/.claude/trace"
  mkdir -p "$TRACE_DIR"
  TRACE_FILE="$TRACE_DIR/codex-trace-$(date +%Y-%m).jsonl"

  # D4: %3N 미지원 fallback (밀리초 → 초로 강등 후 *1000)
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

  # D1: STDIN 감지 — `! -t 0` (TTY 가 아니면 = 파이프/리다이렉션 입력 있음)
  TMP_IN=$(mktemp)
  if [ ! -t 0 ]; then
    cat > "$TMP_IN"
  fi
  PROMPT_BYTES=$(wc -c < "$TMP_IN")
  PROMPT_HASH="sha256:$(sha256sum "$TMP_IN" | cut -d' ' -f1)"

  # D2: 파이프 제거 → 입력 리다이렉션. 종료코드 보존.
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
  if [ -n "$TRACE_PRICE_PER_1K_INPUT" ] && [ -n "$TRACE_PRICE_PER_1K_OUTPUT" ]; then
    COST=$(awk -v ti="$TOK_IN" -v to="$TOK_OUT" -v pi="$TRACE_PRICE_PER_1K_INPUT" -v po="$TRACE_PRICE_PER_1K_OUTPUT" \
      'BEGIN { printf "%.6f", (ti*pi + to*po) / 1000 }')
  fi

  # verdict 자동 추출 (마지막 50줄에서)
  VERDICT="N/A"
  VERDICT_EXTRACTED="false"
  if [ -z "$TRACE_VERDICT" ]; then
    V=$(tail -50 "$TMP_OUT" | grep -oE '최종[^\n]*[⭕⚠❌]' | tail -1 | grep -oE '[⭕⚠❌]' | tail -1 || true)
    if [ -n "$V" ]; then VERDICT="$V"; VERDICT_EXTRACTED="true"; fi
  else
    VERDICT="$TRACE_VERDICT"   # override 는 verdictExtracted=false
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
  rm -f "$TMP_IN" "$TMP_OUT"
  exit $EXIT_CODE
  ```
- 모델명은 `-m` 옵션 파싱이 복잡하므로 `TRACE_MODEL` 환경변수로 명시 주입 (사용자/Claude 가 set)
- **D1~D4 핵심 수정**:
  - STDIN 감지 `[ ! -t 0 ]` (heredoc/파이프/리다이렉션 모두 지원)
  - codex 호출에서 파이프 제거, `< "$TMP_IN"` 로 stdin 주입 → 종료코드 정확 캡처
  - `set -eo pipefail` + `set +e/-e` 로 codex 실패 캡처 (codex 가 실패해도 wrapper 는 trace 기록 + 원래 exit code 반환)
  - 시간 함수 `now_ms()` 폴백 (Git Bash 가 `%3N` 미지원 시 초 단위 *1000)

#### Step 3-2. `.team-workspace/analyze-traces.sh` 신규 분석 스크립트
- bash + jq. 기획 §FR-3-c 의 4개 jq 명령 묶어서 sub-command 로 노출:
  ```bash
  bash analyze-traces.sh rounds          # 스프린트별 round 분포
  bash analyze-traces.sh verdicts        # ⭕/⚠/❌/N/A 분포
  bash analyze-traces.sh latency         # 모델별 평균 latency + 누적 비용
  bash analyze-traces.sh evolution       # ⚠ → ⭕ 진화율 (스프린트별 final verdict)
  bash analyze-traces.sh all             # 위 4개 모두 출력
  ```
- **0건 가드** (codex 권고): trace 파일 없거나 비어있으면 친절한 메시지:
  ```bash
  TRACE_DIR="${HOME}/.claude/trace"
  if ! ls "$TRACE_DIR"/codex-trace-*.jsonl >/dev/null 2>&1; then
    echo "ℹ trace 파일 없음 — codex-trace.sh 로 1회 이상 호출 후 다시 시도하세요."
    exit 0
  fi
  ```

#### Step 3-3. README/안내 1줄 추가 (선택)
- `.team-workspace/codex-trace.sh` 머리 주석에 사용 예시 + 환경변수 표 (`TRACE_MODEL`, `TRACE_SPRINT`, `TRACE_ROUND`, `TRACE_TAGS`, `TRACE_PRICE_PER_1K_INPUT/OUTPUT`, `TRACE_VERDICT`)
- 별도 README 신설은 비범위 (스크립트 내부 주석으로 충분)

### Phase 4 — 검증 + 빌드

#### Step 4-1. T-1~T-12 (기획서 §6) 수동 실행
- 본 스프린트는 코드 변경 0건이라 단위 테스트 신규 X. 통합 검증 위주.
- T-6 (prod IP 전역 grep): **CI 명시적 실패 처리 + D8 grep 옵션 일관 + 구현 단계 정정**:
  ```bash
  # 메인 검사 + 실패 시 상세 출력 모두 동일 --include/--exclude 사용
  # 의도: "미래 회귀 차단" — 새 문서/코드에 prod IP 들어오는 것 방지.
  # 과거 실행 기록 (docs/exec-plans/ result, docs/generated/ audit) 은 역사적 사실로 보존.
  GREP_OPTS=(-Ern
      --include='*.md' --include='*.yml' --include='*.yaml'
      --include='*.properties' --include='*.conf' --include='*.sql'
      --include='*.sh' --include='*.ps1'
      --exclude='harness-hardening-v1.md'
      --exclude='AGENT_SAFETY.md'
      --exclude='application-local.properties'
      --exclude='legacy-contract-tables-drop.md'
      --exclude='security-hardening-v2-draft.md'
      --exclude-dir='archive'
      --exclude-dir='generated')
  PATTERN='211\.104\.137\.55|SW_Dept'
  # 검사 대상: active 코드/문서. exec-plans 의 *-result.md / precheck.md / audit-* 등 과거 실행 기록은
  # 역사적 사실이므로 본 검사에서 제외 — 단 product-specs/ 와 신규 exec-plans 는 검사.
  PATHS=(src/main/java src/main/resources docs/product-specs docs/design-docs .team-workspace/ AGENTS.md CLAUDE.md README.md HELP.md)

  HITS=$(grep "${GREP_OPTS[@]}" "$PATTERN" "${PATHS[@]}" 2>/dev/null | wc -l)
  if [ "$HITS" -gt 0 ]; then
    echo "❌ Prod IP/DB name 잔존: $HITS hits"
    grep "${GREP_OPTS[@]}" "$PATTERN" "${PATHS[@]}" | head -10
    exit 1
  fi
  echo "✓ T-6 PASS (hits=0)"
  ```
  - **GREP_OPTS 배열 재사용** — 메인/디버그 명령의 `--include`/`--exclude` 셋이 자동 일치 (D8)
  - 본 문서는 `--exclude='harness-hardening-v1.md'` 로 자기참조 인용 차단
  - **AGENT_SAFETY.md 제외** — 우리 안전 정책 본문에 "❌ Bad 예시" 로 의도적 인용
  - **application-local.properties 제외** — `.gitignore` 되어 있어 commit 안 됨
  - **archive/, generated/ 디렉토리 제외** — 과거 audit/precheck 실행 기록 (역사적 사실)
  - **exec-plans/ 전체는 검사 대상에서 제외** — 과거 result/precheck 문서가 prod IP 인용. 미래 신규 exec-plan 은 PR review 단계에서 검사 (별도 후속)

#### Step 4-2. 기존 테스트 회귀 검증
- `./mvnw test -Dtest='TeamStatusReaderTest,PollingWatcherTest,TeamMonitorCompressionPropertiesTest'` → 10/10 PASS 유지
- DB 좌표 변경 후 application.properties default 가 dummy 라도 환경변수 우선 패턴이라 server-restart.sh 정상 가동

#### Step 4-3. wrapper 스모크 테스트
- `TRACE_MODEL=gpt-5 TRACE_SPRINT=harness-hardening-v1 TRACE_ROUND=0 TRACE_TAGS="smoke" echo "test prompt" | bash .team-workspace/codex-trace.sh -m gpt-5 -`
- 출력: codex 정상 응답 + `~/.claude/trace/codex-trace-2026-04.jsonl` 1줄 추가
- `bash .team-workspace/analyze-traces.sh all` → 통계 출력

#### Step 4-4. 서버 재기동 회귀
- `bash server-restart.sh` → DB_PASSWORD 환경변수 자동 로드 + 부팅 정상

---

## 2. 테스트 (T#) — 기획서 §6 그대로

| ID | 내용 | 검증 방법 | Pass 기준 |
|----|------|----------|-----------|
| T-1 | "작업완료" 단발 → echo + confirm | 사용자 발화 → Claude 응답 검사 | echo 출력 + 즉시 commit X |
| T-2 | "작업완료" 연속 2회 → 진행 | 동일 트리거 2회 | commit + push 진행 |
| T-3 | echo 후 "잠깐만" → cancel | 응답 변화 검사 | cancel + 일반 대화 |
| T-4 | "집이야" single-shot | dev-env 발화 | 즉시 push (확인 X) |
| T-5 | 시크릿 누출 시 cancel | dev-env 에 dummy secret 삽입 후 발화 | 자동 cancel + 사용자 확인 요청 |
| T-6 | prod IP 전역 grep (active 영역, archive/exec-plans 제외) | Step 4-1 명령 (GREP_OPTS 배열) | hits=0 |
| T-7 | properties default dummy | `grep "211\.104" application.properties` | 0 hits |
| T-8 | env 미설정 빌드 실패 | DB_PASSWORD unset 후 server-restart.sh | 의도된 실패 (현재 동작 그대로) |
| T-8b | .env.example 존재 | `ls .env.example` | exist |
| T-9 | wrapper 1라인 JSONL | Step 4-3 실행 후 trace 파일 검사 | JSONL 라인 +1 |
| T-10 | JSONL 스키마 키 | `jq 'keys' < codex-trace-2026-04.jsonl \| tail -1` | ts/model/promptHash/tokensIn/tokensOut/costUsd/durationMs/verdict/sprint/round/tags 키 모두 존재 |
| T-11 | analyze-traces.sh 동작 | `bash analyze-traces.sh all` | 4개 통계 모두 출력 |
| T-12 | trace 시크릿 미저장 | `grep "sk-proj-\|test prompt" codex-trace-*.jsonl` | hits=0 (hash 만 있음) |

---

## 3. 롤백 전략

| 상황 | 핫픽스 | 정식 롤백 |
|------|--------|-----------|
| trigger confirm 너무 번거로움 | `TRIGGER_FAST_MODE=1` 환경변수 set | `feedback_trigger_confirm.md` 비활성 메모 추가 |
| prod DB 마스킹 후 빌드 실패 | `DB_URL` 환경변수 명시 set | application.properties commit revert |
| codex-trace.sh 오작동 | wrapper 호출 안 함 (직접 `codex exec` 으로 복귀) | `.team-workspace/codex-trace.sh` 파일 삭제 |
| 메모리 룰 충돌 | 메모리 파일 임시 이동 | 메모리 파일 commit revert |
| AGENT_SAFETY 정책 분쟁 | `docs/AGENT_SAFETY.md` 일시 비활성 (rename) | 본 commit 전체 revert |

### 병합 전략별 revert (D6)

PR 머지 방식에 따라 원복 명령이 다름:

| 병합 전략 | 원복 명령 | 비고 |
|----------|-----------|------|
| **merge-commit** (`git merge --no-ff`) | `git revert -m 1 <merge-sha>` | 머지 commit 의 부모 1번 (= main) 기준 |
| **squash** (GitHub default) | `git revert <squash-sha>` | 단일 commit 으로 squash 됐으므로 1회 |
| **rebase** (linear history) | `git revert <commit-1-sha>..<commit-5-sha>` | 5 commit 모두 순차 revert |

본 PR 은 GitHub 기본 (squash) 가정 — 단일 commit revert 로 전체 원복.

---

## 4. 리스크·완화 재확인 (기획 §5 매핑)

| 리스크 | 수준 | 완화 (구현 매핑) |
|--------|------|------------------|
| R-1 confirm UX 부담 | P2 | 빠른모드 (Step 3-1 의 TRIGGER_FAST_MODE) + 동일 트리거 2회 단축 |
| R-2 trace 인플레 | P3 | 월별 롤링 (Step 3-1) + 1년 1.2MB 무시 가능 |
| R-3 마스킹 후 빌드 실패 | P1 | `.env.example` (Step 2-4) + dummy default 명백 (`localhost:5432/swdept_local`) |
| R-4 wrapper 옵션 차단 | P2 | `"$@"` forward (Step 3-1) + Step 4-3 스모크 |
| R-5 트리거 오발화 | P1 | 판별 룰 5단계 알고리즘 (메모리 룰, Step 1-1) + echo 로 사전 가시화 |

---

## 5. 영향 파일 (확정)

### 신규
- `docs/exec-plans/harness-hardening-v1.md` (본 문서)
- `docs/AGENT_SAFETY.md` (Step 2-1)
- `.team-workspace/codex-trace.sh` (Step 3-1, Git Bash 필수)
- `.team-workspace/analyze-traces.sh` (Step 3-2)
- `.env.example` (Step 2-4)
- `~/.claude/projects/C--Users-ukjin-sw-management-system/memory/feedback_trigger_confirm.md` (Step 1-1)
- `~/.claude/projects/C--Users-ukjin-sw-management-system/memory/MEMORY.md` (Step 1-3 — 신설 또는 업데이트)

### 수정
- `AGENTS.md` (§5 트리거 안전 게이트 + AGENT_SAFETY 링크 + DB 좌표 마스킹)
- `CLAUDE.md` (DB 좌표 마스킹)
- `src/main/resources/application.properties` (DB_URL default → localhost dummy + 머리 주석)
- `.gitignore` (`.env` 추가 — 없으면)
- `~/.claude/projects/C--Users-ukjin/memory/feedback_dev_env_sync.md` (Step 1-2 — 시크릿 검증 cancel 룰)

### 변경 없음
- sw-management-system Java 코드 (TeamMonitor 영역 포함)
- 단위 테스트 코드 (회귀 검증만)
- DB 스키마
- server-restart.sh (이미 환경변수 패턴 — 무수정)

---

## 6. 단일 PR 구성 / commit 분할 (D7 — 5개로 세분화)

1. **`docs(harness-hardening-v1): 기획서 v2 + 개발계획 v2`** — `docs/product-specs/harness-hardening-v1.md` + `docs/exec-plans/harness-hardening-v1.md`
2. **`docs(harness-hardening-v1): AGENT_SAFETY 정책 문서`** — `docs/AGENT_SAFETY.md` 신규 + `AGENTS.md` 의 링크 1줄 추가 (Step 2-1)
3. **`feat(harness-hardening-v1): trigger confirm 메모리 룰 + AGENTS §5 갱신`** — `~/.claude/.../feedback_trigger_confirm.md` + `feedback_dev_env_sync.md` 갱신 + `AGENTS.md §5` 갱신 + `MEMORY.md` (Step 1-1 ~ 1-4)
4. **`feat(harness-hardening-v1): prod DB 마스킹 + .env.example + .env 가이드`** — `application.properties` + `AGENTS.md` (DB 좌표 + .env 가이드 §1 끝) + `CLAUDE.md` + `.env.example` + `.gitignore` (Step 2-2 ~ 2-4)
5. **`feat(harness-hardening-v1): codex-trace.sh wrapper + analyze-traces.sh`** — `.team-workspace/` 2개 파일 (Step 3-1 ~ 3-3)

각 commit `Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>`.

분리 의도 (D7 보완):
- commit 1 (docs) — 기획서/개발계획 보존만
- commit 2 (정책 문서) — AGENT_SAFETY 별도 분리 (정책 변경 추적/리뷰/롤백 단위 명확)
- commit 3 (트리거) — 메모리 룰 + AGENTS 워크플로우만
- commit 4 (prod 분리) — DB 좌표 + .env (보안 영향)
- commit 5 (trace 도구) — wrapper 도구 (선택적 사용)

→ 영역별 독립 revert 가능. 예: trace 만 비활성화하려면 commit 5 만 revert.

---

## 7. 승인 요청

본 개발계획 v1 에 대한 codex 검토 + 사용자 최종승인을 요청합니다.

승인 후:
1. Phase 1 → 2 → 3 → 4 순차 구현
2. Phase 4 (검증) 후 codex 구현물 검증 → 사용자 "작업완료" 발화 (이때부터 새 confirm 흐름 적용)
3. 자동 commit + push (4 commit, AGENTS.md §5)
