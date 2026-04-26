---
tags: [dev-plan, sprint, workflow, design]
sprint: "designer-team-onboarding"
status: draft-v2
created: "2026-04-26"
revised: "2026-04-26"
---

# [개발계획서] 가상 디자인팀(🎨 designer) 정식 도입 — v2

- **작성팀**: 개발팀
- **작성일**: 2026-04-26 (v1/v2 동일자)
- **근거 기획서**: [[../product-specs/designer-team-onboarding|기획서 v3]] (codex ⭕ 통과 + 사용자 최종승인)
- **상태**: 초안 v2 (codex ⭕ 통과 + 비차단 권고 1건 즉시 반영) — v1 검토 ⚠ 보완 4건 + 권고 + v2 경로 표기 일관화 모두 반영
- **목표**: 기획서 §FR-1~5, §NFR-1~4, §R-1~7 전부 구현. 단일 PR 머지.

### v1 → v2 변경점

| # | 항목 | 위치 |
|---|------|------|
| **D1** | T-F grep 에서 `--include='*.md'` 제거 — docs 내 예시 문자열 false positive 차단 (코드/쉘만 스캔) | Step 5-3, §2 T-F |
| **D2** | 롤백 §3 — `teammonitor.enabled=false` 핫픽스가 실제로는 동작 안 함 (가드 없음) → "(모듈 즉시 off 미지원 — `git revert` 사용)" 솔직 표기 | §3 롤백 표 |
| **D3** | commit 분할 — docs 1개 → `docs(spec)` + `docs(plan)` 2개로 분리, 총 5 commit | §6 |
| **D4** | 부트스트랩 명시 — 본 스프린트는 디자인팀 자체 도입이라 자문 트리거 부재. §0 에 "codex 가 디자인 게이트 1회 대행" 명시 | §0 |
| 권고 | Step 4-2 CSS 스코프 — `body.team-monitor-page` 최상위 셀렉터 재확인 (다른 admin 페이지 영향 차단) | Step 4-2 |

---

## 0. 사전 조건 / 환경

- 기존 4팀(planner/db/developer/codex) 운영 안정 (직전 스프린트 `team-monitor-dashboard` v4 + 후속 LRU/gzip 개선까지 완료된 master)
- 신규 의존성 0건 (외부 라이브러리 추가 없음)
- DB 변경 없음
- 본 작업 자체는 D 정책 적용 대상이지만 **UI 키워드 (`레이아웃`, `카드`, `css`, `그리드`) 다수 → A 정책 (디자인팀 자문 필수)** 인 셈. 다만 디자인팀 자체 도입이 본 스프린트라 자문 트리거는 아직 없음 (역설적 부트스트랩).

### 0-1. 부트스트랩 정책 (D4 보완)

본 스프린트는 디자인팀 신설 자체가 목표라 "기획→디자인팀 자문" 트리거가 형식상 공백입니다. 다음 원칙으로 처리:

- **이번 스프린트 한정** codex 가 디자인 게이트 역할을 1회 대행 (이미 기획서 v1→v3 검토 + 본 개발계획 검토에서 codex 가 UI 영향·CSS 스코프·접근성 관점도 평가).
- **다음 UI 변경 스프린트부터** 정식 디자인팀 자문 절차 적용 (기획 단계에서 `docs/design-docs/components/<component>.md` 신규 + commit).
- 본 스프린트 산출물 `docs/design-docs/components/README.md` 에 위 부트스트랩 메모를 1줄 포함 (다음 작업자 혼선 방지).

---

## 1. 작업 순서 (Phase / Step)

### Phase 1 — 문서 + 디렉토리 (FR-4 / FR-5)

#### Step 1-1. `docs/design-docs/components/README.md` 신규
1. 신규 파일 1개 — 안내 + 예시 파일명 + 부트스트랩 메모 (D4 보완).
2. 내용:
   ```markdown
   # 컴포넌트 디자인 결정 (디자인팀 SSoT)

   본 디렉토리는 가상 디자인팀(🎨 designer) 의 컴포넌트별 결정 누적 위치입니다.

   - **파일명 규칙**: `<component>.md` (예: `card.md`, `badge.md`, `button.md`)
   - **디자인 토큰 SSoT**: `../DESIGN.md` (있으면)
   - **결정 시점**: 기획 단계에서 디자인팀 자문 발생 시 즉시 작성 + commit (memory 가 아닌 git history 가 진실원)

   ## 부트스트랩 메모 (2026-04-26 designer-team-onboarding)
   본 디렉토리는 designer-team-onboarding 스프린트로 신설됨. 그 시점에는 자체 디자인팀 자문 트리거가 없었으므로
   codex 가 1회 디자인 게이트 대행. 이후 UI 변경 스프린트부터는 정식 자문 산출물이 본 디렉토리에 누적될 예정.
   ```

#### Step 1-2. `AGENTS.md` §3 갱신 (FR-1 + FR-5)
1. 팀 표에 `🎨 디자인팀 (designer)` 행 추가:
   - 역할: UI 토큰·컴포넌트·접근성·폰트/아이콘 자문
   - 산출물: `docs/DESIGN.md` 갱신 + `docs/design-docs/components/*.md`
2. 워크플로우 도식 갱신:
   ```
   요청 → [기획팀] 기획서
        → (UI 변경 있음) [DB팀 + 디자인팀 병렬] 자문
        → [codex] 검토 → 사용자 최종승인
        → [개발팀] 개발계획 → [codex] 검토 → 사용자 최종승인
        → 구현 → [codex] 검증 → "작업완료" → 자동 commit+push
   ```
3. 신규 §3-1 "A+D 정책" 섹션 추가 — 기획서 §FR-1-b 본문을 그대로 인용 (매칭 룰 + 키워드 표 + 책임 분리). 약 25줄.

#### Step 1-3. `CLAUDE.md` 갱신 (FR-5)
1. "핵심 워크플로우" 섹션의 가상 팀 체계 라인을 4팀 → 5팀 으로 업데이트:
   - 기존: `**가상 팀 체계** (기획/DB/개발/테스트 + codex 검증)`
   - 변경: `**가상 팀 체계** (기획/DB/디자인/개발/테스트 + codex 검증)`
2. 워크플로우 본문에 디자인팀 한 줄 추가 (또는 `@AGENTS.md` 참조만 강조).

### Phase 2 — 쉘 도구 (FR-2)

#### Step 2-1. `set-status.sh` case 분기 확장
1. team validation case 에 `designer` 추가:
   ```bash
   case "$team" in
     planner|db|developer|codex|designer) ;;
     *) echo "❌ team 은 planner|db|developer|codex|designer 중 하나여야 합니다."; exit 1 ;;
   esac
   ```
2. usage 주석 (`team : planner | db | developer | codex`) 도 `designer` 추가.
3. 회귀 검증: `bash .team-workspace/set-status.sh designer 진행중 50 "test"` exit 0 + designer.status valid 5필드 (T-A).

#### Step 2-2. `monitor.sh` 5팀 렌더
1. `TEAMS=(planner db developer codex)` → `TEAMS=(planner db developer codex designer)`.
2. `render_team()` 의 `case "$team"` 라벨 매핑에 한 줄 추가:
   ```bash
   designer)  label="🎨 DESIGNER " ;;
   ```
3. 회귀 검증 (T-E): 기존 4팀 표시 정상 + designer 카드 추가 표시.

### Phase 3 — 백엔드 (FR-3-a + FR-3-c)

#### Step 3-1. `TeamStatusReader.TEAMS` 5팀 확장
- `List.of("planner", "db", "developer", "codex", "designer")` 1줄 변경.
- 영향: `PollingWatcher` 자동 반영 (line 47/73 — TEAMS 참조). `TeamMonitorService.init()` 자동 반영 (`reader.readAll()` 호출).

#### Step 3-2. `JavaNioWatcher.TEAM_FILES` Set 갱신
- `Set.of("planner.status", "db.status", "developer.status", "codex.status", "designer.status")` — 자체 Set 보유이므로 명시 수정 필요.

#### Step 3-3. 단위 테스트 갱신
- `TeamStatusReaderTest.readAll_returnsAllFourTeams_evenIfFilesMissing`:
  - 메서드명 → `readAll_returnsAllFiveTeams_evenIfFilesMissing`
  - `containsOnlyKeys("planner", "db", "developer", "codex", "designer")` 5개 단정
  - `assertThat(all.get("designer")).isNull()` 추가
- 신규 케이스 (FR-3-c 두 번째 항목):
  ```java
  @Test
  void readAll_designerOnlyPresent_otherFour_null() throws IOException {
      write("designer.status", "team=designer\nstate=대기\nprogress=0\ntask=onboarding\nupdated=1777170000\n");
      Map<String, TeamStatus> all = reader.readAll();
      assertThat(all.get("designer")).isNotNull();
      assertThat(all.get("planner")).isNull();
      assertThat(all.get("db")).isNull();
      assertThat(all.get("developer")).isNull();
      assertThat(all.get("codex")).isNull();
  }
  ```

### Phase 4 — 프런트엔드 (FR-3-b)

#### Step 4-1. `templates/admin/team-monitor.html` — 5번째 카드 추가
- 기존 4카드와 동일한 `<article>` 구조로 designer 카드 추가:
  ```html
  <article class="team-card" data-team="designer" id="card-designer">
      <header class="card-head">
          <span class="team-emoji">🎨</span>
          <span class="team-label">DESIGNER</span>
          <span class="card-state" data-field="state">-</span>
      </header>
      <div class="progress-bar"><div class="fill" data-field="progress-fill" style="width:0%"></div></div>
      <div class="card-meta">
          <span class="progress-text" data-field="progress-text">-</span>
          <span class="updated-text" data-field="updated">-</span>
      </div>
      <div class="card-task" data-field="task">-</div>
  </article>
  ```
- JS 무수정 (FR-3-b 증빙: forEach + getElementById lookup → 카드 DOM 만 있으면 자동 갱신)

#### Step 4-2. `team-monitor.css` — 그리드 수용 기준 충족
- 기존: `.tm-cards { grid-template-columns: repeat(2, 1fr); }`
- 변경: `.tm-cards { grid-template-columns: repeat(auto-fit, minmax(360px, 1fr)); }`
- 풀스크린 모드 그리드 라인도 동일하게 수정 (현재 `body.team-monitor-page.fullscreen .tm-cards { grid-template-columns: repeat(2, 1fr); }`).
- 수용 기준 (기획 §FR-3-b):
  - 1920px 가로폭 = 5컬 한 줄 (1920 / 360 ≈ 5.3 → 5컬)
  - 1280px 가로폭 = 3컬 (1280 / 360 ≈ 3.5 → 3컬, 5팀 = 2줄)
  - 풀스크린(가로 풀폭) = 동일 자동
- 기존 `--tm-*` 변수 100% 재사용. 새 토큰 추가 0건 (NFR-4).
- **CSS 스코프 재확인 (codex v1 권고)**: `.tm-cards` 선택자가 다른 admin 페이지로 누수되지 않도록 `body.team-monitor-page .tm-cards { ... }` 와 같이 최상위 스코프 셀렉터 유지. 기존 css 가 이미 `body.team-monitor-page` 클래스 내부에서만 변수 선언하고 있어 영향 차단 효과 큼. Step 4-2 변경 시 `.tm-cards` 단독 선언 금지.

### Phase 5 — 빌드 + 검증

#### Step 5-1. 빌드
- `./mvnw -DskipTests clean compile` → BUILD SUCCESS

#### Step 5-2. 단위 테스트
- `./mvnw test -Dtest='TeamStatusReaderTest,PollingWatcherTest,TeamMonitorCompressionPropertiesTest'`
- 9/9 → 10/10 PASS (designer-only 케이스 +1 추가).

#### Step 5-3. T-F CI 회귀 차단 패턴 1회 수동 실행 (스모크) — D1 + 구현 발견 정정

team-monitor 영역 코드/쉘만 스캔. docs 와 다른 admin 페이지(`main-dashboard.html` 등)의 정상 2컬럼 grid false positive 차단:

```bash
# 1. team-monitor 매직 상수 (전체 src 범위 — 매우 specific 한 토큰만)
! grep -Ern \
    --include='*.java' --include='*.css' --include='*.html' --include='*.js' --include='*.sh' \
    'TEAMS\.length[[:space:]]*==[[:space:]]*4|FourTeams|TEAMS_4' \
    src/main/java src/main/resources .team-workspace/

# 2. team-monitor 그리드 4팀 가정 (team-monitor.css 만 한정 — 다른 페이지의 정상 2컬럼 grid 영향 없음)
! grep -En 'repeat\(2,[[:space:]]*1fr\)' src/main/resources/static/css/team-monitor.css
```
- 두 grep 모두 hits=0 단정.
- **분리 의도**: `repeat(2, 1fr)` 는 일반적인 CSS 패턴이라 다른 페이지에서 정상 사용. team-monitor 영역만 범위 제한.
- **docs/ 는 의도적으로 제외** — 본 개발계획서나 기획서 등에 회귀 패턴이 예시로 등장하기 때문

#### Step 5-4. 서버 재기동 + 라이브 스모크
- `bash server-restart.sh` → `Started SwManagerApplication` + 에러 0건
- 다른 터미널에서 `bash .team-workspace/set-status.sh designer 진행중 50 "smoke"` 실행
- 브라우저 `/admin/team-monitor` → 5번째 카드(designer) 정상 표시 + 진행바 노랑 50%
- `bash .team-workspace/monitor.sh` → 5팀 모두 ANSI 렌더 정상

---

## 2. 테스트 (T#)

기획서 §6-2 의 T-A~T-F 를 그대로 매핑.

| ID | 내용 | 검증 방법 | Pass 기준 |
|----|------|----------|-----------|
| T-A | designer set-status | `bash .team-workspace/set-status.sh designer 진행중 50 "test"` | exit 0 + designer.status 5필드 valid |
| T-B | 기존 4팀 회귀 | `bash .team-workspace/set-status.sh planner ...` | exit 0 + 동일 출력 (atomic write 도 회귀 없음) |
| T-C | 서버 재기동 | `bash server-restart.sh` | Started + 에러 0 |
| T-D | 페이지 5팀 렌더 | 브라우저 `/admin/team-monitor` | 5카드 모두 표시, 데스크탑 기본 그리드 깨짐 없음 |
| T-E | monitor.sh 5팀 | `bash .team-workspace/monitor.sh` | 5팀 ANSI 렌더 |
| T-F | CI 회귀 차단 grep (코드/쉘만, docs 제외) | Step 5-3 명령 (D1 보완) | hits=0 |
| T-U1 | TeamStatusReaderTest 5팀 | `./mvnw test -Dtest=TeamStatusReaderTest` | 8/8 PASS (기존 7 + designer-only 1) |
| T-U2 | 빌드 통과 | `./mvnw -DskipTests clean compile` | BUILD SUCCESS |

---

## 3. 롤백 전략

기획서 §R-7 운영 롤백 가이드와 1:1 매핑. 단일 PR 머지이므로 `git revert <merge-sha>` 1회로 전체 원복.

| 상황 | 핫픽스 | 정식 롤백 |
|------|--------|-----------|
| HTML 5번째 카드 깨짐 | (UI 만 영향) | `card-designer` 추가 라인 revert |
| CSS 그리드 회귀 (4팀도 깨짐) | `team-monitor.css` 그리드만 `repeat(2, 1fr)` 임시 환원 | Phase 4-2 commit revert |
| 백엔드 — TEAMS 5팀 부담 | (D2: 모듈 즉시 off 미지원 — 현재 코드에 `@ConditionalOnProperty` 가드 없음. `teammonitor.enabled=false` set 해도 Watcher/Service 모두 로드됨) | TEAMS + TEAM_FILES revert (`git revert <sha>` 1회) |
| 단위 테스트 fail | (없음) | TEAMS 변경 commit revert |
| 워크플로우 분쟁 | 임시: 모든 스프린트 A 적용 | AGENTS.md / CLAUDE.md revert |

---

## 4. 리스크·완화 재확인 (기획 §5 매핑)

| 리스크 | 수준 | 완화 (구현 단계 매핑) |
|--------|------|----------------------|
| R-1 그리드 깨짐 | P1 | Step 4-2 (auto-fit minmax + 수용 기준 검증) |
| R-2 4팀 잔존 코드 | P0 | Step 3-1/3-2 (TEAMS+TEAM_FILES 동시 변경) + Step 5-3 (T-F grep) + 단위 테스트 5팀 단정 |
| R-3 A vs D 분쟁 | P1 | Step 1-2 (AGENTS.md 키워드 표 + 매칭 룰 명시) |
| R-4 디자인 일관성 | P2 | Step 1-1 (components/README — git history SSoT) |
| R-5 빈 디렉토리 | P2 | Step 1-1 (README 1줄 안내) |
| R-7 운영 롤백 | — | §3 표 |

---

## 5. 영향 파일 (확정)

### 신규
- `docs/exec-plans/designer-team-onboarding.md` (본 문서)
- `docs/design-docs/components/README.md` (Step 1-1)

### 수정
- `AGENTS.md` (§3 표 + 워크플로우 도식 + §3-1 A+D 정책 — 약 30줄)
- `CLAUDE.md` (가상 팀 체계 라인 4팀→5팀, ~2줄)
- `.team-workspace/set-status.sh` (case 분기 1줄 + usage 주석 1줄)
- `.team-workspace/monitor.sh` (TEAMS 배열 1줄 + case 라벨 1줄)
- `src/main/java/com/swmanager/system/service/teammonitor/TeamStatusReader.java` (TEAMS List 1줄)
- `src/main/java/com/swmanager/system/service/teammonitor/JavaNioWatcher.java` (TEAM_FILES Set 1줄)
- `src/main/resources/templates/admin/team-monitor.html` (designer 카드 14줄 추가)
- `src/main/resources/static/css/team-monitor.css` (그리드 2줄 변경 — 일반 + 풀스크린)
- `src/test/java/com/swmanager/system/service/teammonitor/TeamStatusReaderTest.java` (5팀 단정 갱신 + designer-only 케이스 추가, ~25줄)

### 변경 없음
- `application.properties` (teammonitor.* 무영향)
- TeamMonitorService.java, Controller, Advice, Filter, HealthIndicator, InfoContributor, Properties
- DTO (TeamStatus, TimelineEntry, TeamStatusEvent), UlidGenerator
- JS (`team-monitor.js`) — forEach + getElementById lookup 으로 자동 처리
- DB 스키마

---

## 6. 단일 PR 구성 / commit 분할 (D3 — 5개로 세분화)

본 스프린트는 단일 PR 머지. commit 5개로 분할 — docs / 정책 / 쉘 / 백엔드 / 프런트 각각 독립 revert 가능:

1. **`docs(designer-team): 기획서 v3 + 개발계획 v2`** — `docs/product-specs/` + `docs/exec-plans/` + `docs/design-docs/components/README.md` (Step 1-1)
2. **`docs(designer-team): AGENTS/CLAUDE — 5팀 워크플로우 + A+D 정책`** (Step 1-2, 1-3)
3. **`feat(designer-team): set-status.sh + monitor.sh — designer 추가`** (Step 2-1, 2-2)
4. **`feat(designer-team): backend — TEAMS + TEAM_FILES + 단위 테스트 5팀`** (Step 3-1 ~ 3-3)
5. **`feat(designer-team): frontend — 5번째 카드 + 그리드 auto-fit`** (Step 4-1, 4-2)

각 commit 메시지 끝에 `Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>`.

- **분리 의도**: docs 와 정책(AGENTS/CLAUDE) 을 분리하면 "정책만 임시 revert" 또는 "코드만 revert + 정책 유지" 같은 시나리오 가능.
- **단일 PR + 5 commit** — `git revert <merge-sha>` 1회로 전체 원복 가능 (단일 PR 보장).

---

## 7. 승인 요청

본 개발계획서 v2 에 대한 codex 재검토 및 사용자 최종승인을 요청합니다.

승인 후 진행 순서:
1. Phase 1 → 2 → 3 → 4 → 5 순차 구현
2. Phase 5 완료 후 codex 검증 (구현물 기준) → 사용자 "작업완료" 발화 → 자동 commit+push (AGENTS.md §5)
