---
tags: [plan, sprint, harness, safety, observability]
sprint: "harness-hardening-v1"
status: draft-v2
created: "2026-04-26"
revised: "2026-04-26"
---

# [기획서] harness 보강 v1 — Trigger 안전성 + Prod DB 분리 + LLM Trace — v2

- **작성팀**: 기획팀
- **작성일**: 2026-04-26 (v1/v2 동일자)
- **선행**: codex 가 본 워크플로우의 harness 성숙도를 Level 3/5 로 평가. 1순위는 자동화/관측이지만 1인 운영 컨텍스트에서 즉각적 ROI 가 큰 3가지를 우선 도입.
- **상태**: 초안 v2 (codex 재검토 대기) — v1 검토 ⚠ 보완 5건 + 권고 3건 모두 반영

### v1 → v2 변경점
| # | 항목 | 위치 |
|---|------|------|
| **C1** | trace 스키마 — `tokensIn/tokensOut/costUsd` 추가 (`TRACE_PRICE_PER_1K_INPUT/OUTPUT` 환경변수) | §FR-3-b |
| **C2** | OS 호환 — `$HOME/.claude/trace` + `mkdir -p` 내장 + Git Bash 필수 명시 (Windows native PowerShell 별도 .ps1 비범위, Git Bash 사용) | §FR-3-a |
| **C3** | 트리거 판별 룰 구체화 — 정확 일치(단독 메시지) + 의문문 제외 + 존댓말 변형 허용 | §FR-1-d 신설 |
| **C4** | echo 산출 방식 명시 — `git status --porcelain` + `git diff --name-only HEAD` + `git log --oneline origin/master..HEAD` | §FR-1-b |
| **C5** | prod IP 전역 스캔 범위 확대 — `*.md, *.yml, *.yaml, *.properties, *.conf, *.sql, *.sh, *.ps1` | §6-2 T-6 |
| **권고1** | JSONL 월별 롤링 — `codex-trace-YYYY-MM.jsonl` | §FR-3-a |
| **권고2** | 빠른확인 모드 opt-in 토글 (`TRIGGER_FAST_MODE=1` 환경변수) | §FR-1-e |
| **권고3** | verdict 자동 추출 기본 패턴 + override | §FR-3-a |

---

## 1. 배경 / 목표

### 배경 — codex Level 3/5 평가의 진짜 위험
직전 평가에서 codex 가 짚은 문제 중 **1주 안에 해결 가능 + impact 가장 큰** 3가지:

1. **NL 트리거 오발화 위험** — "작업완료" 한 마디로 자동 commit/push. 사용자가 실수로 발화하거나 Claude 가 오해하면 잘못된 코드가 master 에 들어감. 현재 차단 장치 0건.
2. **Prod DB 좌표 노출** — `211.104.137.55:5881/SW_Dept` 가 docs (CLAUDE.md, AGENTS.md, application.properties) 에 박힘. agent 가 실수로 prod DB 에 write 하거나 스모크 테스트가 prod 로 가는 사고 가능.
3. **LLM trace 부재** — codex 호출 6라운드/스프린트 진행하면서 token cost / latency / verdict 누적 데이터 0. 모델 업그레이드 시 회귀 감지 불가.

### 목표
1. **Trigger hardening**: "작업완료"/"승인" 등 NL 트리거 발화 시 자동 행동 직전에 **변경 사항 echo + 사용자 confirm** 1회 추가. 1줄 응답으로 cancel 가능.
2. **Prod DB 분리 단서**: docs 의 prod IP 마스킹 + agent 작업 시 **read-only 가드** + ephemeral test DB 패턴 안내 (실제 docker-compose 셋업은 비범위, 가이드만)
3. **LLM trace JSONL**: codex 호출 1줄 wrapper 로 model/version/prompt-hash/response/cost/verdict/timestamp 를 `~/.claude/trace/codex-trace.jsonl` 에 매번 기록. 분석은 jq 로 가능

### 비목표
- 풀 eval harness, golden test suite (codex #1) — 차기 스프린트 후보
- LLM observability 대시보드 (codex #2) — JSONL 만 쌓고 dashboard 는 별도
- Prompt/model versioning + canary/shadow (codex #3) — 모델 업그레이드 빈도 낮아 보류
- Policy-as-code (codex #5) — 5팀 ceremony 부담 크지 않음, 보류

---

## 2. 기능 요건 (FR)

### FR-1. Trigger hardening — "작업완료" / "승인" 발화 안전 게이트

#### 1-a. 적용 대상 트리거
| 트리거 | 자동 행동 | 위험도 |
|--------|----------|--------|
| "작업완료" | git add 명시 파일 + commit + push | **높음** (master 영구 변경) |
| "승인" | 다음 단계 진입 (구현 시작 등) | 중간 (파일 변경 시작) |
| "집이야"/"사무실이야"/"출장중이야" | dev-environments push | 낮음 (private repo) |

#### 1-b. 안전 게이트 (모든 high/medium 트리거) — C4 보완

사용자 발화 → Claude 가 **자동 행동 직전 멈춤** → 다음 git 명령 3개로 echo 빌드:

```bash
# 1. 변경된 파일 (staged + unstaged + untracked)
git status --porcelain
# 2. 영향 파일 (HEAD 기준 변경)
git diff --name-only HEAD
# 3. push 될 commit 프리뷰 (작업완료 트리거에서만)
git log --oneline origin/master..HEAD
```

echo 포맷:
```
🛑 [작업완료] 트리거 감지 — 다음 행동을 진행하시겠습니까?

  push 될 commit 5건 (origin/master..HEAD):
    1. abc1234 docs(...): ...
    2. def5678 feat(...): ...
    ...
  영향 파일 12개 (HEAD 기준):
    M  AGENTS.md
    M  src/main/java/.../X.java
    A  src/main/resources/static/css/...
    ...
  미staged 변경 (commit 안 함):
    ?? Measure.java
    M  cookies.txt

  push 대상: master → origin/master (https://github.com/SWDpet/sw-management-system)

확인하려면 한 번 더 "작업완료" 또는 "yes" 입력 (cancel: 다른 응답).
```

- 사용자가 동일 트리거를 **연속 2회** 발화 시에만 진행
- "yes" / "y" / "응" / "ok" 도 confirm 으로 인정
- 기타 응답 (예: "잠깐만", "취소", 추가 질문) 시 자동 행동 cancel + 일반 대화 모드 복귀
- "미staged 변경" 영역에 무관 파일 (cookies.txt, Measure.java 등) 이 보이면 사용자가 사전에 인지 가능 → 실수 방지

#### 1-c. 예외 — 저위험 트리거
- "집이야"/"사무실이야"/"출장중이야" 는 single-shot OK (private repo, 시크릿 마스킹 검증 후 push)
- 단 시크릿 누출 검증 grep hits>0 시 자동 cancel + 사용자 확인

#### 1-d. 트리거 판별 룰 (C3 보완)

오발화 차단을 위한 명확한 판별 기준:

| 조건 | 매칭 여부 | 예시 |
|------|----------|------|
| 단독 메시지 (앞뒤 공백/구두점만) | ✅ 매칭 | "작업완료", "작업완료요", "작업 완료", "승인" |
| 존댓말 변형 | ✅ 매칭 | "작업완료요", "작업완료입니다", "승인합니다" |
| 의문문 (?, 까요, 나요 등) | ❌ 비매칭 | "작업완료 했나요?", "승인해도 될까요?" |
| 부정문 | ❌ 비매칭 | "작업완료 안 됐어", "승인하지 마" |
| 인용문 (따옴표/백틱 안) | ❌ 비매칭 | `이 시점에 "작업완료" 라고 발화하면`, "작업완료" 라는 단어 |
| 긴 문장 안의 단어 | ❌ 비매칭 | "이번 작업완료 시점은 다음 주야" |
| 영어 변형 | ✅ 매칭 | "done", "complete", "approved" (단 명령형/단독) |
| 애매한 케이스 | ⚠ 재질문 | "트리거로 보였는데 맞나요? '작업완료' 의미면 yes" |

**알고리즘** (Claude 가 적용):
1. 사용자 메시지 trim → 30 chars 이하 + `[?\.!]` 종료 부호 없음 → 매칭 후보
2. 의문 어미 (까요/나요/는지/지요) 검사 → 발견 시 비매칭
3. 인용 부호/코드 블록 안에 트리거 단어만 있으면 비매칭
4. 위 통과 시 매칭 → 1-b confirm 흐름 진입
5. 애매하면 1-shot 재질문 → 사용자 응답 기반 진행/cancel

#### 1-e. 빠른확인 모드 (권고2 opt-in)

- 환경변수 `TRIGGER_FAST_MODE=1` set 시 confirm 단계 skip (당일 세션 한정)
- 사용자가 직접 "빠른모드 on" / "fast mode on" 발화 시도 토글 가능
- 기본은 OFF — 명시 opt-in 필요. 1인 운영에서 반복 작업 시 임시 활용
- 단 push 트리거 ("작업완료") 는 빠른모드라도 echo 는 유지 (cancel 가능, confirm 만 skip)

### FR-2. Prod DB 분리 단서

#### 2-a. docs 의 prod IP 마스킹
- `AGENTS.md`, `CLAUDE.md`, `application.properties` 의 `211.104.137.55:5881/SW_Dept` 를 다음으로 교체:
  - `${DB_HOST}:${DB_PORT}/${DB_NAME}` (환경변수 참조)
  - 운영자 가이드 주석에 "실제 좌표는 환경변수 또는 1Password 에서 조회" 명시
- application.properties 는 이미 `${DB_URL:jdbc:postgresql://211.104.137.55:5881/SW_Dept}` 형태로 환경변수 우선 — **default 값을 dummy 로 교체** (`jdbc:postgresql://localhost:5432/swdept_local` 등)

#### 2-b. Agent 작업 read-only 가드
- 신규 파일 `docs/AGENT_SAFETY.md` — agent 가 따라야 할 안전 규칙:
  - DB 작업은 read-only 쿼리만 (SELECT). UPDATE/DELETE/DROP 은 사용자 명시 승인
  - 마이그레이션 / DDL 변경 시 → 별도 명시 발화 ("마이그레이션 진행" 같은) + 2단계 confirm
  - 스모크 테스트 시 ephemeral local DB 사용 권장 (가이드 §2-c)
- `AGENTS.md` 에 1줄 링크 추가

#### 2-c. Ephemeral test DB 가이드 (실제 셋업은 비범위)
- `docs/AGENT_SAFETY.md` §3 에 docker-compose 예시 포함:
  ```yaml
  services:
    test-db:
      image: postgres:16
      environment:
        POSTGRES_DB: swdept_test
        POSTGRES_PASSWORD: test
      ports: ["5433:5432"]
  ```
- 실제 docker 설치/스크립트는 사용자 결정 — 본 스프린트는 패턴만 제시

### FR-3. LLM trace JSONL

#### 3-a. wrapper 스크립트 (C2 OS 호환 + 권고1 월별 롤링 + 권고3 verdict 추출)

- 신규 파일 `.team-workspace/codex-trace.sh`:
  - `codex exec` 호출을 wrapping (`"$@"` 로 전체 인자 forward)
  - 실행 전: 시각/모델/입력 prompt-hash 기록
  - 실행 후: 응답/duration/tokens/cost/verdict 기록
  - **출력 디렉토리**: `${HOME}/.claude/trace/` (Git Bash `$HOME` = `/c/Users/ukjin`)
  - **`mkdir -p` 내장** 으로 첫 실행 시 자동 생성
  - **월별 롤링** (권고1): `codex-trace-$(date +%Y-%m).jsonl` — 1년 12개 파일, 분석은 `cat codex-trace-2026-*.jsonl | jq ...`
  - **OS 요구사항**: Git Bash 필수 (Windows native PowerShell 별도 .ps1 비범위). 본 스프린트는 Git Bash 환경 (현재 IUHOME) 만 지원
  - **verdict 자동 추출** (권고3): 응답 마지막 50줄에서 정규식 `최종[^\n]*[⭕⚠❌]` 검색. 실패 시 `N/A`. `TRACE_VERDICT` 환경변수로 override

#### 3-b. JSONL 스키마 (확정 — C1 cost 추가)
```json
{
  "ts": "2026-04-26T13:00:00+09:00",
  "model": "gpt-5",
  "promptHash": "sha256:abc...",
  "promptBytes": 12345,
  "responseBytes": 4567,
  "tokensIn": 3086,
  "tokensOut": 1141,
  "costUsd": 0.0432,
  "durationMs": 28000,
  "verdict": "⭕",
  "verdictExtracted": true,
  "sprint": "harness-hardening-v1",
  "round": 3,
  "tags": ["기획", "v1"]
}
```

- **token 추정**: `tokensIn ≈ promptBytes / 4`, `tokensOut ≈ responseBytes / 4` (영어 기준 1 token ≈ 4 bytes. 한글 2~3 bytes/token 으로 추정 오차 있음 — 정확 값은 codex API 응답에서 제공 시 채택)
- **cost 계산**:
  - `costUsd = (tokensIn × TRACE_PRICE_PER_1K_INPUT + tokensOut × TRACE_PRICE_PER_1K_OUTPUT) / 1000`
  - `TRACE_PRICE_PER_1K_INPUT` / `TRACE_PRICE_PER_1K_OUTPUT` 환경변수로 단가 주입
  - 미설정 시 `costUsd: null`
- `verdictExtracted: true` = 자동 추출 성공 / `false` = N/A 또는 override 사용
- `sprint`, `round`, `tags` 는 환경변수 (`TRACE_SPRINT`, `TRACE_ROUND`, `TRACE_TAGS`) 로 전달

#### 3-c. 분석 도구 (CLI) — `analyze-traces.sh`
```bash
# 모든 월 통합
TRACE_DIR=$HOME/.claude/trace
ALL=$(cat $TRACE_DIR/codex-trace-*.jsonl)

# 스프린트별 round 평균
echo "$ALL" | jq -r 'select(.sprint=="harness-hardening-v1") | .round' | sort -n | uniq -c

# verdict 분포 (⭕/⚠/❌/N/A)
echo "$ALL" | jq -r '.verdict' | sort | uniq -c

# 모델별 평균 latency + 누적 비용
echo "$ALL" | jq -s 'group_by(.model) | map({
  model: .[0].model,
  count: length,
  avgMs: (map(.durationMs) | add/length),
  totalCostUsd: (map(.costUsd // 0) | add)
})'

# 첫 검토 vs 재검토 — ⚠ → ⭕ 진화율
echo "$ALL" | jq -s 'group_by(.sprint) | map({
  sprint: .[0].sprint,
  rounds: length,
  finalVerdict: (sort_by(.round) | last | .verdict)
})'
```

#### 3-c. 분석 도구 (CLI 만)
- `~/.claude/trace/` 에 `analyze.sh` 1개 — jq 기반:
  ```bash
  # 스프린트별 round 평균
  jq -r 'select(.sprint=="harness-hardening-v1") | .round' codex-trace.jsonl | sort -n | uniq -c
  # ⚠ → ⭕ 진화율
  jq -r '.verdict' codex-trace.jsonl | sort | uniq -c
  # 평균 duration / 모델별 토큰 추정 (responseBytes/4 ≈ tokens)
  jq -s 'group_by(.model) | map({model: .[0].model, avgMs: (map(.durationMs) | add/length)})' codex-trace.jsonl
  ```
- 풀 dashboard 는 비범위. 매 스프린트 종료 시 사용자가 1회 분석 명령 실행

---

## 3. 비기능 요건 (NFR)

### NFR-1. 호환성
- 기존 워크플로우 미변경. 트리거/wrapper 추가만, 기존 사용자 발화 패턴 그대로 동작
- "작업완료" 단발 발화 시 → confirm echo → 그 다음 발화로 진행 (이전엔 즉시 commit, 이제는 +1 round)

### NFR-2. 운영
- trace JSONL 파일 크기: 1라운드 ≈ 1KB 미만. 100라운드/월 = 100KB. 1년 = 1.2MB. rotation 불필요
- prod DB 마스킹 후 — sw-management-system 의 빌드/실행은 환경변수 (DB_URL/DB_USERNAME/DB_PASSWORD) 100% 의존. server-restart.sh 가 이미 이 패턴

### NFR-3. 성능
- trigger confirm: 사용자 인지 약간 부하 증가 (확인 1회) 단 실수 방지 가치 큼
- trace wrapper: codex 호출당 +50ms 미만 (sha256 + JSONL append)

### NFR-4. 보안
- trace JSONL 에 실제 prompt/response 본문 저장 X (hash + bytes 만). 누출 위험 0
- prod DB IP 마스킹 후에도 git history 에 잔존 — 단 history rewrite 는 비범위 (해당 정보가 critical secret 아님 + 회사 내부 IP)

---

## 4. 데이터 / 설계

### 4-1. 영향 파일 (예상)

#### 신규
- `docs/product-specs/harness-hardening-v1.md` (본 문서)
- `docs/AGENT_SAFETY.md` (FR-2-b — agent 안전 규칙)
- `.team-workspace/codex-trace.sh` (FR-3-a wrapper)
- `~/.claude/trace/analyze.sh` (FR-3-c 분석 — 또는 `.team-workspace/` 배치)

#### 수정
- `AGENTS.md` (§5 자동화 규칙에 trigger confirm 정책 추가, AGENT_SAFETY 링크)
- `CLAUDE.md` (DB 좌표 마스킹)
- `src/main/resources/application.properties` (DB_URL default 값 변경)
- `.claude/projects/.../memory/feedback_dev_env_sync.md` (시크릿 검증 cancel 룰 추가 — FR-1-c 정합)

#### 변경 없음
- 기존 sw-management-system 코드 (Java, 프런트)
- 기존 단위/통합 테스트
- DB 스키마

### 4-2. Trigger confirm 메모리 룰 (신규 feedback)

`feedback_trigger_confirm.md`:
```
- "작업완료"/"승인" 발화 시 즉시 행동 X. 변경 사항 echo + 사용자 1회 더 확인 후 진행
- 동일 트리거 연속 2회 또는 "yes/y/응/ok" 시 진행
- 다른 응답 시 cancel
- 저위험 트리거 ("집이야"/"사무실이야"/"출장중이야") 는 single-shot OK
```

---

## 5. 리스크 & 보완

### R-1. confirm 부담 — UX 저해
- **리스크**: 매번 "작업완료" → echo → "yes" 2 round 가 번거로움
- **보완**: M-1a echo 가 명료하면 사용자 0.5초 의사결정 가능. M-1b 동일 트리거 2회 발화 도 OK 라 "작업완료 작업완료" 도 진행

### R-2. trace JSONL 인플레이션
- **리스크**: codex 호출 빈번 시 파일 커짐
- **보완**: M-2a NFR-2 분석 — 1년 1.2MB 무시 가능. M-2b 2년 후 archive 권고만

### R-3. prod DB 마스킹 후 빌드 실패
- **리스크**: 누군가 환경변수 안 set 하면 빌드 실패
- **보완**: M-3a 이미 application.properties 가 환경변수 우선 패턴. server-restart.sh 가 DB_PASSWORD 자동 로드. M-3b default 값을 명백히 가짜 (`localhost:5432/swdept_local`) 로 두면 진짜 prod 사고 차단

### R-4. wrapper 가 codex CLI 옵션 일부 차단
- **리스크**: `bash codex-trace.sh` 가 모든 codex 옵션을 forward 못 할 수 있음
- **보완**: M-4a wrapper 는 `"$@"` 로 모든 인자 forward. M-4b 단위 테스트로 옵션 (`-m`, `--config`, `-`) 모두 통과 확인

### R-5. trigger 오발화 — 정상 사용자 대화 단절
- **리스크**: "작업완료 했나요?" 같은 자연어 질문이 트리거로 오인
- **보완**: M-5a Claude 의 1차 판단 — 명령형/단독 문장만 트리거. 의문문/긴 문장은 일반 대화. M-5b 애매하면 "트리거로 보였는데 맞나요?" 한번 묻기

### 리스크 매트릭스

| ID | 영역 | 영향도 | 발생빈도 | 우선순위 |
|----|------|--------|----------|---------|
| R-1 | UX | 하 | 상 | P2 |
| R-2 | 운영 | 하 | 하 | P3 |
| R-3 | 빌드 | 중 | 중 | P1 |
| R-4 | 도구 | 중 | 하 | P2 |
| R-5 | 트리거 | 중 | 중 | P1 |

---

## 6. 검증 계획

### 6-1. Trigger hardening
- T-1: "작업완료" 단발 발화 → Claude echo + confirm 대기 (즉시 commit X)
- T-2: 연속 "작업완료 작업완료" → 즉시 진행
- T-3: "작업완료" → echo → "잠깐만 다른 거 먼저" → cancel + 일반 대화
- T-4: "집이야" → single-shot 진행 (확인 없음, 저위험)
- T-5: "집이야" + 시크릿 마스킹 검증 fail → 자동 cancel + 사용자 확인 요청

### 6-2. Prod DB 분리
- **T-6 (C5 보완)**: 레포 전체 grep `211\.104\.137\.55` 범위 확대:
  ```bash
  ! grep -Ern \
      --include='*.md' --include='*.yml' --include='*.yaml' \
      --include='*.properties' --include='*.conf' --include='*.sql' \
      --include='*.sh' --include='*.ps1' \
      '211\.104\.137\.55|SW_Dept' .
  ```
  - hits=0 단정 (모든 prod IP/DB 명 마스킹 확인)
  - 단 `docs/product-specs/harness-hardening-v1.md` 본 문서는 의도적 인용 → 제외 처리 (`--exclude='harness-hardening-*.md'` 추가) 또는 본 본문에서 `211.x.x.x` 같이 마스킹 표기로 우회
- T-7: application.properties default 값이 prod IP 미포함
- T-8: server-restart.sh 환경변수 미설정 시 빌드 실패 (현재 동작 그대로)
- T-8b: `.env.example` 파일 신규 — `DB_URL`/`DB_USERNAME`/`DB_PASSWORD` placeholder 포함 (NFR 보강)

### 6-3. LLM trace
- T-9: `bash codex-trace.sh -m gpt-5 "test"` 실행 → JSONL 1라인 추가
- T-10: JSONL 라인이 §FR-3-b 스키마 키 모두 포함
- T-11: `analyze.sh` 실행 → 스프린트별 round count 출력
- T-12: 시크릿 마스킹 — JSONL 에 실제 prompt 본문 X (hash 만)

---

## 7. 영향 파일 (확정)

### 신규
- `docs/product-specs/harness-hardening-v1.md` (본 문서)
- `docs/AGENT_SAFETY.md` (FR-2-b)
- `.team-workspace/codex-trace.sh` (FR-3-a — Git Bash 필수)
- `.team-workspace/analyze-traces.sh` (FR-3-c — `.team-workspace/` 통합)
- `.env.example` (T-8b — DB_URL/DB_USERNAME/DB_PASSWORD placeholder)
- `.claude/projects/C--Users-ukjin-sw-management-system/memory/feedback_trigger_confirm.md` (FR-1 메모리)

### 수정
- `AGENTS.md` (§5 자동화 규칙 + AGENT_SAFETY 링크 + DB 좌표 마스킹)
- `CLAUDE.md` (DB 좌표 마스킹)
- `src/main/resources/application.properties` (DB_URL default → localhost dummy)
- `~/.claude/projects/C--Users-ukjin/memory/feedback_dev_env_sync.md` (시크릿 검증 cancel 룰)

### 변경 없음
- sw-management-system Java/프런트 코드
- DB 스키마
- server-restart.sh (이미 환경변수 패턴)

---

## 8. 권한 / 보안 체크리스트

- [ ] trigger confirm 으로 NL 오발화 blast radius 차단
- [ ] prod DB IP docs 마스킹 (`AGENTS.md`, `CLAUDE.md`, `application.properties` default)
- [ ] AGENT_SAFETY.md — agent 작업 read-only 가드 명시
- [ ] LLM trace JSONL — 본문 미저장 (hash 만), 시크릿 누출 0건
- [ ] 시크릿 마스킹 검증 grep — "집이야" 트리거 직전 자동 실행, hits>0 시 cancel

---

*이 기획서가 codex 검토 통과 + 사용자 최종승인 후에만 개발계획 단계로 진입합니다.*
