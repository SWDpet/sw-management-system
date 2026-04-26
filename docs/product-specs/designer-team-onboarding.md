---
tags: [plan, sprint, workflow, design]
sprint: "designer-team-onboarding"
status: draft-v3
created: "2026-04-26"
revised: "2026-04-26"
---

# [기획서] 가상 디자인팀(🎨 designer) 정식 도입 — v3

- **작성팀**: 기획팀
- **작성일**: 2026-04-26 (v1/v2 동일자)
- **선행 스프린트**: `team-monitor-dashboard` (4팀 운영 안정화 완료)
- **상태**: 초안 v3 (codex ⭕ 통과 + 비차단 참고 1건 즉시 반영) — v1 보완 5건 + v2 정합성 4건 + v3 grep 옵션 순서까지 모두 반영
- **v1 → v2 변경점**:
  - **B1**: PollingWatcher TEAMS 자동 반영 코드 증빙 추가 (§FR-3-a)
  - **B2**: team-monitor.js forEach + DOM lookup 메커니즘 증빙 추가 (§FR-3-b)
  - **B3**: D 정책 키워드 규칙 엄밀화 — 매칭 룰 + 동의어 + `template` 한정 (§FR-1-b, §4-3)
  - **B4**: CSS grid 표현을 "수용 기준" 으로 강등, 구현 예시는 참고안 (§FR-3-b)
  - **B5**: CI 회귀 차단 패턴 추가 — T-F 신규 (§6, §R-2)
  - **C1**: §4-3 자가 체크리스트 표 — `template`(단독) 제거 + `html template`/`Thymeleaf template`/`templates/` 한정 (FR-1-b 정합)
  - **C2**: §7 영향 파일의 CSS 변경 표현 → "수용 기준 충족 (구현 자유)" 완화 (B4 정합)
  - **C3**: T-F grep 명령에 `-E` 플래그 + ERE 패턴 (이식성 향상)
  - **C4**: §5-7 운영 롤백 절 신규 — HTML/CSS/백엔드/문서 별 리버트 가이드

---

## 1. 배경 / 목표

### 배경 — 현 4팀 체계의 한계
기존 4팀(🧭planner / 🗄️db / 🛠️developer / 🤖codex)은 백엔드·DB 중심. 디자인 결정은:
- 기획서 §NFR-1 (폰트/다크모드) 에 흩어져 있음
- 폰트 번들링·아이콘 셋·색대비·접근성 결정의 SSoT 부재
- 직전 스프린트 codex N2 ("JetBrains Mono 번들링은 디자인팀 결정 영역") 처럼 누적 미결 항목 발생
- UI 변경 PR 에서 디자인 토큰 정합성 검증 게이트 없음

### 목표
1. 가상 5번째 팀 **🎨 designer** 정식 도입 (워크플로우 + 모니터 + 산출물 위치)
2. **A+D 정책** 적용 — 기본은 기획 단계 자문(A), UI 변경 0건이면 자동 skip(D)
3. UI/디자인 결정의 SSoT를 `docs/DESIGN.md` 로 통일 + 컴포넌트별 결정은 `docs/design-docs/components/` 하위
4. 4팀 영향 최소화 — 기존 스프린트는 그대로, 신규 스프린트만 정책 적용

### 비목표 (이 스프린트에서 다루지 않음)
- 실제 디자인 토큰 리뉴얼 (별도 스프린트)
- 다크모드 전역화 (Phase 4 별도)
- 디자인 시스템 라이브러리 도입 (Tailwind/Bootstrap 등 — 비범위)
- 외부 디자이너 협업 도구 통합 (Figma/Zeplin)

---

## 2. 기능 요건 (FR)

### FR-1. 디자인팀 워크플로우 정의 (`AGENTS.md` §3)

#### 2-1-a. 팀 표 추가
| 팀 | 역할 | 산출물 |
|----|------|--------|
| 🎨 디자인팀 (designer) | UI 토큰·컴포넌트·접근성·폰트/아이콘 자문 | `docs/DESIGN.md` 갱신 + `docs/design-docs/components/*.md` |

#### 2-1-b. A+D 정책 명시
- **A (기본)**: 기획서에 UI/디자인 결정 항목이 1개 이상이면 디자인팀 자문 필수. DB팀과 **병렬** 진행 (기획→ {DB+디자인 병렬} → 개발→ codex).
- **D (자동 skip)**: 기획서 본문에 아래 키워드 0건이면 디자인팀 단계 skip.

#### 매칭 룰 (B3 보완)
- **대소문자 무시** (`UI` = `ui` = `Ui`)
- **단어 경계** 적용 — `git` 안의 `gi` 처럼 부분 매칭 금지 (정규식 `\b<keyword>\b` 또는 한글은 공백/구두점 경계)
- **코드블록 (` ``` `, ` ` `) 안의 토큰은 제외** — 클래스/변수명 오탐 방지 (예: `template` 이 코드 안에 등장해도 무시)

#### 키워드 표 (확정)
| 분류 | 키워드 |
|------|--------|
| 영어 핵심 | `UI`, `css`, `style`, `scss`, `theme`, `palette`, `typography`, `spacing`, `grid`, `column`, `contrast`, `elevation`, `shadow`, `radius` |
| 영어 인터랙션/상태 | `hover`, `focus`, `transition`, `animation` |
| 한글 핵심 | `색상`, `폰트`, `아이콘`, `다크모드`, `레이아웃`, `색대비`, `시안` |
| 한글 컴포넌트 | `배지`, `버튼`, `카드`, `메뉴`, `모달`, `드롭다운`, `툴팁` |
| 접근성 | `접근성`, `a11y`, `aria-*`, `semantic` |
| 템플릿 (한정) | `html template`, `Thymeleaf template`, `templates/` (단어 단독 `template` 은 범용어이므로 매칭 X) |

#### 책임 분리
- **1차 판정**: 기획서 작성자 (Claude/사용자) — 작성 직후 자가 체크
- **2차 판정**: codex 검토 시 누락 발견 → ⚠ 판정
- **애매하면 A** — 부담은 5분 자문, 누락 시 후속 비용 큼

#### 2-1-c. 워크플로우 도식 갱신
```
요청 → [기획팀] 기획서
     → (UI 변경 있음) [DB팀 + 디자인팀 병렬] 자문
     → [codex] 검토 → 사용자 최종승인
     → [개발팀] 개발계획 → [codex] 검토 → 사용자 최종승인
     → 구현 → [codex] 검증 → "작업완료" → 자동 commit+push
```

### FR-2. 상태 파일 + 쉘 도구 확장
- **set-status.sh** `case` 분기에 `designer` 추가 (validation 통과)
- **monitor.sh** `TEAMS` 배열에 `designer` 추가 + `case "$team"` 라벨 매핑 (`🎨 DESIGNER`)
- 기존 4팀 status 파일 호환 100% 유지 (designer.status 가 없어도 동작)

### FR-3. 백엔드 — team-monitor 5팀 대응

#### 3-a. Java (B1 증빙 추가)

| 파일 | 변경 | 자동 반영 여부 (코드 증빙) |
|------|------|---------------------------|
| `TeamStatusReader.TEAMS` | `List.of("planner", ..., "codex", "designer")` 1줄 변경 | — (변경 지점 자체) |
| `PollingWatcher` | **무수정** | ✅ 자동 반영. 코드 line 47/73: `for (String team : TeamStatusReader.TEAMS)` — TEAMS 변경 즉시 5팀 polling. |
| `JavaNioWatcher.TEAM_FILES` | `Set.of("planner.status", ..., "codex.status", "designer.status")` 1줄 변경 | ❌ 자체 Set 보유 (line 25) → **명시 수정 필수**. TEAMS 자동 반영 X. |
| `TeamMonitorService.init()` | **무수정** | ✅ 자동 반영. `reader.readAll()` 호출이므로 5팀 자동 처리. designer.status 미존재 시 null entry (기존 정책 유지). |

#### 3-b. 프런트 (B2 증빙 + B4 수용 기준화)

##### HTML (수정 필수)
- `templates/admin/team-monitor.html`: 카드 1개 추가 (`<article data-team="designer" id="card-designer">` + 🎨 이모지 + DESIGNER 라벨 + 동일한 progress-bar/state/task/updated 구조)

##### JS (B2 증빙 — 무수정)
- `team-monitor.js` 는 카드 DOM 이 미리 정의되어 있으면 자동 렌더. 코드 증빙:
  ```js
  // line 63: snapshot.teams 배열을 forEach 로 순회
  data.teams.forEach(renderCard);
  ...
  // line 102: 팀별 카드 DOM 을 ID 로 lookup
  const card = document.getElementById('card-' + team.team);
  ```
- 즉 **HTML 카드 추가만 하면 JS 무수정으로 designer 카드 자동 갱신**. 카드 미정의 시 silent skip (에러 없음).

##### CSS (B4 — 수용 기준 + 참고 구현)

**수용 기준** (구현 자유, 개발팀이 결정):
- 1920px 가로폭에서 카드 5개 한 줄 표시 가능
- 1280px 가로폭에서 최소 3컬럼 (5팀 = 2줄)
- 풀스크린 모드(`?fullscreen=1`) 도 동일 자동 (별도 분기 불필요)
- 카드 최소 폭 보장 (라벨/진행바 깨짐 없음)
- 기존 `--tm-*` 다크 변수 100% 재사용 (NFR-4)

**참고 구현 예시** (개발계획에서 채택 여부 결정):
```css
.tm-cards { display: grid; grid-template-columns: repeat(auto-fit, minmax(360px, 1fr)); gap: 18px; }
```
다른 접근(미디어 쿼리 분기, container query 등) 도 수용 기준 충족 시 OK.

#### 3-c. 단위 테스트
- `TeamStatusReaderTest.readAll_returnsAllFourTeams_evenIfFilesMissing` → `readAll_returnsAllFiveTeams_evenIfFilesMissing` 로 갱신 (5개 단정)
- 신규 케이스: `designer.status` 만 존재할 때 readAll 결과에서 `designer` 만 non-null 단정

### FR-4. 산출물 디렉토리
- `docs/design-docs/components/` 신규 생성 (이 스프린트에서는 빈 디렉토리 + `.gitkeep` 또는 README 1개)
- 향후 컴포넌트별 결정 문서는 여기에 누적 (예: `card.md`, `badge.md`, `button.md`)
- `docs/DESIGN.md` (있다면) 머리에 "디자인팀 SSoT — 본 문서 변경은 디자인팀 단계 거침" 명시

### FR-5. 문서 갱신
- `AGENTS.md` §3 표 + 워크플로우 도식 + A+D 정책 (FR-1)
- `CLAUDE.md` 핵심 워크플로우 섹션 — designer 행 추가 (1줄)

---

## 3. 비기능 요건 (NFR)

### NFR-1. 호환성
- 기존 4팀 set-status.sh / monitor.sh / status 파일 / TeamStatusReader 모두 무결 동작 (designer 미존재 케이스 = 기존 4팀 동작과 동일)
- 진행 중인 다른 스프린트(`team-monitor-resilience-v2` 차기 firing 등) 영향 없음

### NFR-2. 운영
- 디자인팀 자문이 필요 없는 스프린트는 D 정책으로 skip — 백엔드 전용 스프린트는 부담 0
- 신규 디렉토리 `docs/design-docs/components/` 는 빈 상태 시작. 첫 사용은 다음 UI 스프린트.

### NFR-3. 성능
- 백엔드 영향: 5팀이 4팀과 비교해 read 비용 25% 증가 (status 파일 4 → 5). 무시 가능 수준.
- 프런트 영향: 카드 1개 추가 — 렌더 비용 영향 무시 가능.

### NFR-4. 디자인 토큰 일관성
- 추가되는 카드도 기존 `team-monitor.css` 의 다크 페이지 스코프 변수 (`--tm-bg`, `--tm-card-bg`, `--tm-state-*`) 그대로 재사용. 별도 토큰 신설 금지.

---

## 4. 데이터 / 설계

### 4-1. 데이터
- 신규 status 파일: `.team-workspace/status/designer.status` (set-status.sh 로 생성됨, 사전 생성 불요)
- DB 변경 없음

### 4-2. 영향 파일 (예상)

#### 신규
- `docs/product-specs/designer-team-onboarding.md` (본 문서)
- `docs/design-docs/components/README.md` (1줄 안내)

#### 수정
- `AGENTS.md` (§3 팀 표 + 워크플로우 도식 + A+D 정책)
- `CLAUDE.md` (워크플로우 섹션 1줄)
- `.team-workspace/set-status.sh` (case 분기 1줄)
- `.team-workspace/monitor.sh` (TEAMS 배열 1줄 + case 라벨 1줄)
- `src/main/java/com/swmanager/system/service/teammonitor/TeamStatusReader.java` (TEAMS 1줄)
- `src/main/java/com/swmanager/system/service/teammonitor/JavaNioWatcher.java` (TEAM_FILES Set 1줄)
- `src/main/resources/templates/admin/team-monitor.html` (카드 1개 추가, ~14줄)
- `src/main/resources/static/css/team-monitor.css` (그리드 — §FR-3-b 수용 기준 충족, 구현 방식은 자유. 예: `auto-fit/minmax`, 미디어 쿼리 분기 등)
- `src/test/java/com/swmanager/system/service/teammonitor/TeamStatusReaderTest.java` (5팀 단정으로 갱신)

#### 변경 없음
- 기존 4팀 status 파일
- application.properties (teammonitor.* 키 무영향)
- TeamMonitorService (코드는 무수정 — TEAMS 참조만 자동 반영)
- TeamMonitorController, TeamMonitorAdvice, TeamMonitorSecurityHeadersFilter
- DB 스키마

### 4-3. 워크플로우 키워드 트리거 (D 정책 운영 가이드)

기획서 작성 시 "디자인팀 자문 필요한가?" 자가 체크리스트 (정확한 키워드/룰 정의는 §FR-1-b 참조):

| 키워드 발견 시 | A 정책 (자문 필수) |
|--------------|-------------------|
| `UI`, `css` | 확실 |
| `html template`, `Thymeleaf template`, `templates/` (단독 `template` 은 매칭 X — 범용어) | 확실 |
| `색상`, `폰트`, `아이콘`, `다크모드`, `색대비` | 확실 |
| `레이아웃`, `배지`, `버튼`, `카드`, `애니메이션`, `transition` | 확실 |
| `접근성`, `a11y`, `aria-*` | 확실 |
| 위 키워드 0건 | D 정책 (skip) |

---

## 5. 리스크 & 보완

### R-1. 모니터 그리드 레이아웃 깨짐
- **리스크**: 기존 2x2 grid 가 5팀이면 마지막 카드가 짝 안 맞아 어색.
- **보완**: M-1a `repeat(auto-fit, minmax(360px, 1fr))` 로 화면 폭 자동 적응. 데스크탑(1280px)=3컬럼, 와이드(1920px)=5컬럼. 풀스크린도 동일.

### R-2. 4팀 가정 코드 잔존
- **리스크**: 어딘가 hardcoded 4 가 남아 있을 수 있음 (예: 카드 wrapper 의 `repeat(2, 1fr)`, JavaNioWatcher 의 자체 TEAM_FILES Set 등).
- **보완**:
  - M-2a Grep 으로 `repeat(2`, `4팀`, `4 teams`, `TEAMS.length == 4`, `FourTeams` 등 잔존 점검
  - M-2b `TeamStatusReaderTest.readAll_*` 5개 단정으로 회귀 차단
  - M-2c **T-F (§6-2) — CI 금지 패턴 grep** 으로 향후 회귀 영구 차단

### R-3. 워크플로우 모호 — A vs D 판정 분쟁
- **리스크**: "이게 UI 변경인가?" 판단이 사람마다 다름 (예: 라벨 텍스트만 바꾸는 건?).
- **보완**:
  - M-3a 키워드 표(§4-3) 명시 → 객관적 판단 근거
  - M-3b 애매하면 A 정책 (디자인팀 자문) 기본값 — 부담은 5분 추가 자문, 누락 시 후속 비용 큼
  - M-3c codex 검토 단계에서 누락 발견 시 ⚠ 판정 (게이트)

### R-4. 디자인팀이 Claude 인격이라 의견 일관성 부족
- **리스크**: 동일 스프린트의 시안이 다음 세션에서 뒤집힐 수 있음.
- **보완**:
  - M-4a 결정은 즉시 `docs/design-docs/components/*.md` 에 commit (memory 가 아닌 git history 가 SSoT)
  - M-4b 본 메모리 시스템 (`feedback_*`) 으로 사용자 디자인 선호 누적

### R-5. 신규 디렉토리 빈 상태 — 자기참조 안내 무한루프
- **리스크**: `docs/design-docs/components/` 가 비어 있으면 "어디에 결정 적나?" 모름.
- **보완**: M-5a `components/README.md` 에 1줄 안내 + 예시 파일명 (`card.md`, `badge.md` 등)

### 리스크 매트릭스 요약

| 식별 ID | 영역 | 영향도 | 발생빈도 | 보완 우선순위 |
|---------|------|--------|----------|---------------|
| R-1 | UI | 하 | 중 | P1 |
| R-2 | 회귀 | 중 | 중 | **P0** |
| R-3 | 운영 | 중 | 중 | P1 |
| R-4 | 일관성 | 중 | 하 | P2 |
| R-5 | 문서 | 하 | 하 | P2 |

### R-7. 운영 롤백 가이드 (C4 신규)

배포 후 회귀 발견 시 영역별 최소 비용 롤백 절차 (단일 PR 가정):

| 영역 | 증상 | 핫픽스 (코드 미수정) | 정식 롤백 (commit revert) |
|------|------|---------------------|--------------------------|
| HTML — 5번째 카드 깨짐 | 데스크탑에서 designer 카드 어색/깨짐 | (없음 — UI 만 영향, 서비스 정상) | `card-designer` 추가 commit revert |
| CSS — 그리드 layout 회귀 | 4팀 카드도 깨짐 | `team-monitor.css` 의 그리드만 기존 `repeat(2, 1fr)` 로 임시 환원 | 그리드 변경 commit revert |
| 백엔드 — TEAMS 5팀 부담 | 헬스/info 응답 이상 | `teammonitor.enabled=false` 로 모듈 즉시 off + 서버 재시작 | `TeamStatusReader.TEAMS` + `JavaNioWatcher.TEAM_FILES` revert |
| 문서 — 워크플로우 혼란 | A+D 정책 적용 분쟁 | 임시: 모든 스프린트에 A 적용 (자문 강제) | `AGENTS.md` §3 / `CLAUDE.md` revert |
| 단위 테스트 fail | `TeamStatusReaderTest` 5팀 단정 실패 | (없음) | TEAMS 변경 commit revert (테스트와 함께 묶여 있음) |

- **단일 PR 머지** 원칙으로 전체 revert 도 1회 commit 으로 가능 (`git revert <merge-sha>`)
- designer.status 미존재 시 기존 동작과 동일 (null 처리) → 데이터 누수 없음

---

## 6. 검증 계획

### 6-1. 단위 테스트
- `TeamStatusReaderTest.readAll_*` → 5팀 단정 (기존 4팀 단정에서 1줄 변경)
- 신규 케이스 추가: `designer` status 파일만 있을 때 readAll 결과에 4 null + 1 entry

### 6-2. 통합/회귀
- T-A: `bash .team-workspace/set-status.sh designer 진행중 50 "test"` → exit 0 + designer.status valid 5필드
- T-B: 기존 4팀 명령 (`set-status.sh planner ...`) 회귀 없음 — exit 0 + 동일 출력
- T-C: 서버 재기동 → 로그에 "JavaNioWatcher 시작" + "TeamMonitorService 초기화 완료" + 에러 0건
- T-D: `/admin/team-monitor` 페이지 → 5팀 카드 (designer 카드 포함) 정상 렌더
- T-E: `bash .team-workspace/monitor.sh` 5팀 모두 표시 (기존 모니터 회귀 없음)
- **T-F: CI 회귀 차단 패턴 (B5 신규, C3 보완)** — Grep ERE 단정 (build/CI 스크립트에서 hits=0 확인). 패턴 발견 시 빌드 실패:
  ```bash
  # -E (ERE) 사용. 공백은 POSIX 문자 클래스 [[:space:]] (BSD/GNU grep 양쪽 호환).
  # \s 는 ERE 표준 외 — BSD grep 미지원이라 사용 금지.
  ! grep -Ern \
      --include='*.java' --include='*.css' --include='*.html' --include='*.js' --include='*.sh' --include='*.md' \
      'TEAMS\.length[[:space:]]*==[[:space:]]*4|FourTeams|repeat\(2,[[:space:]]*1fr\)|TEAMS_4' \
      src/main/java src/main/resources docs/ .team-workspace/

  # --include 호환 안 되는 BSD grep 대안 (선택):
  # find src/main/java src/main/resources docs/ .team-workspace/ -type f \
  #     \( -name '*.java' -o -name '*.css' -o -name '*.html' -o -name '*.js' -o -name '*.sh' -o -name '*.md' \) \
  #     -print0 | xargs -0 grep -En '...같은 패턴...'
  ```
  - 의도: 향후 누군가 4팀 가정으로 코드/문서를 추가하면 PR 머지 차단 → 5팀 정합 영구 보존
  - 본 스프린트는 패턴 추가만 — 실제 CI 통합 자동화는 별도 (단순 grep 명령 문서화로도 충분)

### 6-3. 수동 확인
- 화면 폭 변화: 1280/1600/1920 에서 그리드 칸 수 자연스럽게 변하는지
- 풀스크린 모드 (`?fullscreen=1`) 도 5팀 정상

### 6-4. 워크플로우 검증 (다음 스프린트 dry-run)
- 다음 임의 백엔드 스프린트에서 D 정책 적용 — 기획서에 UI 키워드 0건 → designer 단계 skip 확인
- 다음 임의 UI 스프린트에서 A 정책 적용 — 기획서 §NFR 또는 §UI 항목에서 디자인팀 자문 흔적 (commit 또는 components/*.md 신규) 확인

---

## 7. 영향 파일 (확정)

### 신규
- `docs/product-specs/designer-team-onboarding.md` (본 문서)
- `docs/design-docs/components/README.md` (1줄 안내)

### 수정
- `AGENTS.md` (§3 표 + 워크플로우 도식 + A+D 정책 — 약 15줄)
- `CLAUDE.md` (워크플로우 섹션 — 1줄)
- `.team-workspace/set-status.sh` (case 분기 1줄)
- `.team-workspace/monitor.sh` (TEAMS 배열 1줄 + case 라벨 1줄)
- `src/main/java/com/swmanager/system/service/teammonitor/TeamStatusReader.java` (TEAMS 1줄)
- `src/main/java/com/swmanager/system/service/teammonitor/JavaNioWatcher.java` (TEAM_FILES Set 1줄)
- `src/main/resources/templates/admin/team-monitor.html` (카드 ~14줄 추가)
- `src/main/resources/static/css/team-monitor.css` (그리드 layout 변경 — auto-fit)
- `src/test/java/com/swmanager/system/service/teammonitor/TeamStatusReaderTest.java` (5팀 단정 갱신)

### 변경 없음
- application.properties (teammonitor.* 무영향)
- TeamMonitorService.java (코드 무수정, TEAMS 참조 자동 반영)
- TeamMonitorController.java, TeamMonitorAdvice.java, TeamMonitorSecurityHeadersFilter.java
- TeamMonitorHealthIndicator.java, TeamMonitorInfoContributor.java
- DTO (TeamStatus/TimelineEntry/TeamStatusEvent), UlidGenerator
- DB 스키마

---

## 8. 권한 / 보안 체크리스트

- [ ] 기존 `/admin/team-monitor` ADMIN 권한 그대로 — 변경 없음
- [ ] 보안 헤더 필터 영향 없음 (URL 패턴 동일)
- [ ] 신규 designer.status 파일 권한 (set-status.sh atomic write 패턴 그대로)
- [ ] 추가 외부 의존성 0건 (CSP 영향 없음)
- [ ] 민감정보 노출 없음 (status 파일 내용 동일 형식)

---

*이 기획서가 codex 검토 통과 + 사용자 최종승인 후에만 개발계획 단계로 진입합니다.*
