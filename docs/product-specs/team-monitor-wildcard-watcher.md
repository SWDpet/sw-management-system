---
tags: [plan, sprint, workflow, team-monitor, refactor]
sprint: "team-monitor-wildcard-watcher"
status: draft-v3
created: "2026-04-26"
revised: "2026-04-26"
---

# [기획서] team-monitor 와일드카드 watcher — v3

- **작성팀**: 기획팀
- **작성일**: 2026-04-26 (v1/v2/v3 동일자)
- **선행 스프린트**: `team-monitor-dashboard` (4팀 운영 안정화) → `designer-team-onboarding` (5번째 팀 도입)
- **상태**: 초안 v3 (codex round 2 ⚠ → 차단 1건 + 비차단 권고 14건 반영)
- **v1 → v2 변경점** (round 1 ⚠ 차단 6건 + 비차단 8건):
  - **C1**: FR-6-a vs §4-2 팀명 정규식 충돌 해소 — `^[a-z][a-z0-9_-]{0,31}$` 단일 SSoT
  - **C2**: `teamMetadataFallback=true` health/info 노출 — `TeamMonitorInfoContributor` "수정" 으로 이동
  - **C3**: FR-4-c teams.json reload 경로 명확화 (v2 단일 디렉터리 가정 → v3 에서 재정정)
  - **C4**: R-9 (캐시 일관성), R-10 (WatchService OVERFLOW) 신규
  - **C5**: 영향파일 §7 — `PollingWatcherTest.java` 신규 추가
  - **C6**: empty state CSS `.tm-empty` 추가 (team-monitor.css "수정" 으로)
  - N1: PathMatcher glob 명시 / N2: aria-atomic + aria-busy / N3: sortOrder/sort_order 컨벤션 / N4: workspaceDir property / N5: prefers-reduced-motion / N6: empty SR 안내 / N7: T-I/J/K/L 검증 / N8: T-H grep 정밀화
- **v2 → v3 변경점** (round 2 ⚠ 차단 1건 + 비차단 14건):
  - **C3-01 (차단)** — 경로 SSoT 정정. v2 "단일 디렉터리 `.team-workspace/` 감시" 가정 폐기. **실제 디렉터리 구조 명시**: `statusDir = .team-workspace/status/` (status 파일), `teamsJsonPath = .team-workspace/teams.json` (메타). WatchService **두 WatchKey** 등록 — statusDir 의 `*.status` 파일 + workspaceDir 의 `teams.json` 단일 파일 분리 감시. (§FR-2-d, §FR-2-e, §NFR-2, §4-1, §4-4 모두 정정)
  - **N9**: FR-1-b — `PathMatcher` 적용 대상을 파일명 기준으로 SSoT 명시 (`matcher.matches(p.getFileName())`)
  - **N10**: C2-01 — `/actuator/info` ADMIN 보호 정책 별도 박스 (§NFR-5 보안)
  - **N11**: FR-2-e — OVERFLOW 분기 순서 정정 (`invalidate → metadata.reload() → listTeams() → notify`)
  - **N12**: R-9 — `AtomicReference<CachedTeams>` + 불변 record (모든 필드 final) 안전 공개 표준
  - **N13**: R-12 신규 — Actuator info 노출 오구성 위험 + 보안 박스
  - **N14**: R-13 신규 — Windows 경로 매칭 편차 (단위테스트 케이스 포함)
  - **N15**: D-N5 — `prefers-reduced-motion: reduce` 적용 셀렉터 집합 명시 (CSS 예시)
  - **N16**: D-N6 — empty state SR 안내 i18n SSoT 진입점 각주 (비차단 향후 메모)
  - **N17**: T-I-02 — sort_order 변경 시 DOM 재정렬 단정 추가
  - **N18**: T-J-02 — OVERFLOW 후 중복/유실 검증 (정확히 1회 반영 단정)
  - **N19**: T-K-02 — `/actuator/info` 401/403 비인가 + 200 ADMIN 검증
  - **N20**: T-L-02 — 0팀 web UI `.tm-empty` 단일 렌더 e2e 단정
  - **N21**: T-H grep — Java `src/main/java` 한정, 멀티라인 case 옵션 주석, `set -Euo pipefail`, 센티넬 위치 코딩 규약

---

## 1. 배경 / 목표

### 배경 — 5팀 하드코딩 잔존
직전 스프린트 `designer-team-onboarding` 에서 4팀 → 5팀 전환 시 다음 6개 위치를 동시 수정 요구:

| # | 파일 | 라인 | 형태 |
|---|------|------|------|
| 1 | `TeamStatusReader.TEAMS` | 33 | `List.of("planner", "db", ...)` |
| 2 | `JavaNioWatcher.TEAM_FILES` | 25-26 | `Set.of("planner.status", ...)` |
| 3 | `team-monitor.js` `TEAMS` 상수 | 6 | `['planner', 'db', 'developer', 'codex']` (designer 누락 — 누적 잔존 dead code) |
| 4 | `team-monitor.html` 정적 카드 | 22-86 | `<article id="card-planner">` ... 5번 반복 |
| 5 | `.team-workspace/monitor.sh` | 11 | `TEAMS=(planner db developer codex designer)` + `case "$team"` 라벨 매핑 |
| 6 | `.team-workspace/set-status.sh` | 31, 35 | `case "$team" in planner|...` 검증 |

향후 6번째 팀 (예: `tester`, `security` 등) 도입 시 동일하게 6곳 동시 수정 + designer 누락 같은 부분 누락 회귀 위험. `designer-team-onboarding` codex 후속 권고 R-2 ("4팀 가정 코드 잔존 영구 차단") 이 본 스프린트의 직접 원인.

### 목표
1. **백엔드 와일드카드** — `TeamStatusReader.TEAMS` / `JavaNioWatcher.TEAM_FILES` 제거 → 디렉토리 스캔 (`*.status`) 자동 인식
2. **프런트 동적 카드** — 정적 HTML 카드 → SSE snapshot 응답 기반 JS DOM 동적 생성. dead code (`team-monitor.js` line 6 `TEAMS`) 제거.
3. **메타데이터 SSoT** — 신규 `.team-workspace/teams.json` 도입. 이모지·라벨·정렬 순서 단일 진실원. fallback (메타데이터 없음) 안전 동작.
4. **bash 도구 동적화** — `monitor.sh` / `set-status.sh` 도 teams.json 참조 (디렉토리 스캔 + 메타데이터). 검증 룰도 동적.
5. **회귀 영구 차단** — 단위 테스트 + grep 패턴으로 "5 또는 N 하드코딩" 재발 방지.

### 비목표 (이 스프린트에서 다루지 않음)
- 신규 팀 자체 도입 (예: 보안팀, 테스터팀) — 본 스프린트는 인프라만, 실제 신규 팀은 별도 스프린트
- 팀 관리 UI (관리자 화면에서 팀 추가/수정) — bash + 파일 편집 정책 유지
- 메타데이터의 추가 필드 (담당 영역, 책임 매트릭스 등) — 최소 (이모지/라벨/정렬) 만
- 카드 디자인 토큰 리뉴얼 — 기존 `--tm-*` 100% 재사용
- monitor.sh 의 ANSI 컬러 매핑 — 기존 `state_icon` 함수 그대로 (state 컬러는 팀별 X)
- 운영 환경 다국어 라벨 (i18n) — 향후 별도 (D-N6 비차단 메모만)

---

## 2. 기능 요건 (FR)

### FR-1. TeamStatusReader 와일드카드

#### 1-a. 인터페이스 변경
- 기존: `public static final List<String> TEAMS = List.of(...)` 상수
- 변경: `public List<String> listTeams()` 메서드 — 캐시 hit 시 캐시 반환, miss 시 디렉토리 스캔
- `readAll()` 은 `listTeams()` 호출 후 동작 유지

#### 1-b. 디렉토리 스캔 룰 (N1 + N9 보완)
- 매칭: `FileSystems.getDefault().getPathMatcher("glob:*.status")` 사용
- **매칭 SSoT**: `matcher.matches(p.getFileName())` — 파일명 기준 (절대경로/구분자 영향 X, R-13 보완)
- 순회: `try (Stream<Path> s = Files.list(statusDir)) { ... }` (autoclose 보장)
- 필터:
  1. `Files.isRegularFile(p)` — 디렉토리/심볼릭 제외
  2. `!p.getFileName().toString().startsWith(".")` — dot-prefix 임시파일 제외
  3. `matcher.matches(p.getFileName())` — `*.status` 매칭
- 빈 디렉토리 / 디렉토리 부재 → 빈 리스트 반환 (예외 X — `IOException` 은 호출자 전파, 단 부재는 빈 리스트)
- 팀명 추출: `name.substring(0, name.length() - ".status".length())`

#### 1-c. 정렬 정책
- **1순위**: `teams.json` 의 `sort_order` 필드 (작은 값 우선)
- **2순위**: `sort_order` 동일 또는 메타데이터 미정의 시 알파벳순
- **운영 전통 보장**: `teams.json` 기본 제공값 — `planner=10, db=20, developer=30, codex=40, designer=50`. 신규 팀은 `sort_order` 미정의 시 99 + 알파벳순.
- 정렬 결과는 `listTeams()` 반환 List 순서로 노출 → readAll/snapshot/SSE 모두 일관

#### 1-d. 캐싱 (성능, R-9 보완 — N12 안전 공개)
- 결과 보관: `private final AtomicReference<CachedTeams> cache = new AtomicReference<>()`
- `CachedTeams` 는 record (모든 필드 final, 불변): `record CachedTeams(List<String> teams, long expiresAtMs)`
  - `teams` 도 `List.copyOf(...)` 로 불변
- TTL: 1초 (디렉토리 스캔 부담 < 50개 환경 무시 수준 보장)
- invalidation: JavaNioWatcher 가 ENTRY_CREATE / ENTRY_DELETE 또는 OVERFLOW 수신 시 `cache.set(null)` 즉시 invalidate
- TTL 만료 또는 invalidate 시 다음 `listTeams()` 호출이 재계산 (race 시 동일 결과 수렴)
- 단위 테스트로 가시성/경합 케이스 회귀 차단 (§6-1)

### FR-2. JavaNioWatcher 와일드카드 + OVERFLOW + 분리 WatchKey (C3-01 정정)

#### 2-a. TEAM_FILES Set 제거 (suffix 매칭)
- 기존 `TEAM_FILES.contains(name)` 검사 제거
- 새 검증 룰: `matcher.matches(p.getFileName())` (FR-1-b 와 동일 SSoT)
- 매칭되면 팀명 추출 → notifySubscribers
- `*.status` 외 파일 (예: `monitor.sh`, `set-status.sh`) 은 무시. `teams.json` 은 §2-d 별도 WatchKey 처리.

#### 2-b. CREATE / DELETE / MODIFY 처리 (statusDir WatchKey)
- 등록 (statusDir 한정): ENTRY_CREATE + ENTRY_MODIFY + ENTRY_DELETE
- CREATE: 신규 팀 등장 → `reader.invalidateCache()` + `notifySubscribers(team)`
- MODIFY: 기존 팀 갱신 → `notifySubscribers(team)`
- DELETE: 팀 삭제 → `reader.invalidateCache()` (캐시 stale 방지). subscriber 콜백 발화 X (UI 가 다음 snapshot 에서 사라진 팀 처리 — FR-5-b)

#### 2-c. 부적절 파일 무시
- `.foo.status` (dot-prefix) → 무시
- `foo.txt` → 무시 (suffix 다름)
- `subdir/foo.status` → WatchService 는 단일 디렉토리만 감시 → 영향 없음

#### 2-d. teams.json 별도 WatchKey (C3-01 정정)
- **두 WatchKey 등록** (단일 WatchService 사용):
  - `statusDir` = `.team-workspace/status/` — `*.status` 파일 (FR-2-a/b)
  - `workspaceDir` = `.team-workspace/` — `teams.json` 단일 파일 매칭 분기
- 등록 코드 (개념):
  ```java
  this.statusKey = statusDir.register(ws, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
  this.workspaceKey = workspaceDir.register(ws, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
  ```
- 이벤트 처리 분기:
  ```java
  WatchKey key = ws.take();
  Path watchedDir = (Path) key.watchable();
  for (WatchEvent<?> ev : key.pollEvents()) {
      if (ev.kind() == OVERFLOW) { fullRescanFallback(); continue; }
      Path name = (Path) ev.context();
      String fileName = name.getFileName().toString();

      if (watchedDir.equals(workspaceDir) && fileName.equals("teams.json")) {
          teamMetadata.reload();
          reader.invalidateCache();
          notifyMetadataChange();
      } else if (watchedDir.equals(statusDir)
                 && matcher.matches(name.getFileName())
                 && !fileName.startsWith(".")) {
          // 팀 status 처리 (§2-b)
      }
      // 그 외 파일 — 무시
  }
  if (!key.reset()) break;
  ```
- 단일 WatchService 로 두 WatchKey 동시 처리 → reload 경로 명확

#### 2-e. OVERFLOW 폴백 (C4 + N11 순서 정정)
- WatchService 가 ENTRY_OVERFLOW 발화 시 (다량 변동 또는 OS 큐 오버):
  ```java
  if (ev.kind() == StandardWatchEventKinds.OVERFLOW) {
      log.warn("WatchService OVERFLOW — 풀스캔 폴백");
      reader.invalidateCache();
      teamMetadata.reload();           // 메타 먼저 (정렬 의존성 N11 보완)
      List<String> teams = reader.listTeams();
      Set<String> notifiedThisRound = new HashSet<>();
      for (String team : teams) {
          if (notifiedThisRound.add(team)) {  // 중복 방지 (T-J-02 보완)
              notifySubscribers(team);
          }
      }
      continue;
  }
  ```
- 손실 이벤트 복구 보장. 발생 빈도는 일반 환경에서 0건 가까움 (안전망).
- 풀스캔 시 중복 notify 차단 — 같은 OVERFLOW round 내에 같은 팀은 1회만 (N18, T-J-02)

### FR-3. PollingWatcher 와일드카드 호환

#### 3-a. TEAMS 참조 → listTeams() 동적 호출
- 기존: `for (String team : TeamStatusReader.TEAMS)` (line 47, 73)
- 변경: `for (String team : reader.listTeams())` 동적 호출
- start() 의 초기 mtime 기록도 동적
- tick() 의 폴링도 동적 — 매 tick 마다 listTeams() 재호출 (§FR-1-d 캐시 사용)

#### 3-b. 신규 팀 / 메타 변경 자동 picking
- 첫 tick 에서 lastMtime 에 없는 팀 발견 시 첫 등장 (CREATE 동치) 처리 → notifySubscribers
- `teams.json` 도 lastMtime 추적 — 변경 감지 시 `teamMetadata.reload()` + `reader.invalidateCache()`
- 팀 삭제 (status 파일 제거) 도 listTeams() 결과에서 자동 사라짐 — lastMtime 의 stale entry 는 다음 tick 에서 재등장 시 다시 처리됨 (소유자 cleanup 별도 필요 X)

### FR-4. 팀 메타데이터 (teams.json)

#### 4-a. 파일 위치 / 형식
- 위치: `<workspaceDir>/teams.json` (workspaceDir 기본값 `.team-workspace`, NFR-2 property 절대경로 지원)
- 형식 (예시):
```json
{
  "schema_version": 1,
  "teams": {
    "planner":   { "emoji": "🧭",  "label": "PLANNER",   "sort_order": 10 },
    "db":        { "emoji": "🗄️",  "label": "DB",        "sort_order": 20 },
    "developer": { "emoji": "🛠️",  "label": "DEVELOPER", "sort_order": 30 },
    "codex":     { "emoji": "🤖",  "label": "CODEX",     "sort_order": 40 },
    "designer":  { "emoji": "🎨",  "label": "DESIGNER",  "sort_order": 50 }
  },
  "default_emoji": "📋"
}
```

#### 4-b. fallback 룰 (메타데이터 누락 시) — 코드 내장 5팀
- `teams.json` 부재 → 5팀 기본값을 코드 내장 (Java + JS 양쪽). bash 도 fallback 매핑 제공.
- 특정 팀이 `teams` 객체에 없음 → emoji = `default_emoji` (📋), label = 팀명 대문자, sort_order = 99
- `schema_version` 미일치 시 경고 로그 + fallback 적용 (강제 실패 X)
- `teamMetadataFallback=true` health/info 노출 (§FR-7-d, C2 + N10 보완)
- 코드 내장 5팀 fallback 블록은 §6-2 T-H grep 패턴 예외 — `// ALLOW_FIVE_TEAM_FALLBACK` 센티넬 (TH-04)

#### 4-c. 메타데이터 로더 + reload 경로
- 신규 클래스: `TeamMetadata` (Spring `@Component`)
- 메서드:
  - `Optional<TeamMeta> get(String teamName)` — 단일 팀 메타
  - `String emoji(String teamName)` — fallback 포함
  - `String label(String teamName)` — fallback 포함
  - `int sortOrder(String teamName)` — fallback 포함
  - `void reload()` — JSON 재로드 + 가시성 보장 (`AtomicReference<MetaSnapshot>`, `MetaSnapshot` 불변)
  - `boolean isFallbackActive()` — health/info 노출용
- 자동 reload 경로:
  1. **JavaNioWatcher 모드** (§FR-2-d): workspaceKey 의 ENTRY_MODIFY (teams.json) 감지 시 `reload()` 호출
  2. **PollingWatcher 모드** (§FR-3-b): tick 마다 `teams.json` mtime 체크, 변경 시 `reload()` 호출
- TeamMonitorService 초기화 시 1회 `reload()` 호출 (init)

#### 4-d. JSON 직렬화 컨벤션 (N3 보완)
- 서버 Java DTO 필드: `sortOrder` (camelCase)
- JSON property: `sort_order` (snake_case) — Jackson `@JsonProperty("sort_order")` 어노테이션
- SSE snapshot 의 `teamMeta` 도 동일 컨벤션 적용

### FR-5. 프런트엔드 동적 카드 생성

#### 5-a. HTML 정적 카드 → 빈 컨테이너
- 기존: `<section id="cards">` 안에 5개 `<article>` 정적 정의
- 변경: `<section id="cards" class="tm-cards" role="region" aria-label="팀 진행율 카드"></section>` — 빈 컨테이너 + a11y 속성

#### 5-b. JS 카드 생성 함수
- 신규 함수: `createCard(teamObj, meta)` — `<article class="team-card" data-team="X" id="card-X">` DOM 생성
- snapshot 처리:
  1. 컨테이너에 `aria-busy="true"` 설정 (N2 보완)
  2. 응답에서 `teamMeta` 객체 (이모지/라벨/순서) 함께 받아 카드 생성/갱신
  3. 신규 팀 (DOM 미존재) → `createCard` 후 컨테이너에 append
  4. snapshot 에 없는 팀 (DOM 존재하지만 응답 없음) → DOM 제거 (팀 삭제 케이스)
  5. 컨테이너 `aria-busy="false"` 해제
- update 처리: 카드 DOM 이 없으면 (= 신규 팀 첫 등장) `createCard` 후 `renderCard`
- 카드 제거: snapshot 에 없는 팀은 DOM 에서 제거

#### 5-c. SSE snapshot 응답 확장
- 기존: `{ schema_version, serverTime, teams: [...], timeline: [...] }`
- 변경: `{ schema_version, serverTime, teams: [...], timeline: [...], teamMeta: { planner: {emoji, label, sort_order}, ... } }`
- `schema_version` 1 → 2 (호환성: 클라이언트는 `teamMeta` 없으면 fallback 매핑 사용)

#### 5-d. 정렬
- 카드 DOM 삽입 순서는 서버가 정렬한 `teams` 배열 순서 그대로 (FR-1-c). 클라이언트 측 정렬 X.

#### 5-e. dead code 제거
- `team-monitor.js` line 6 `const TEAMS = [...]` 제거 (사용처 없음 확인)

#### 5-f. 접근성 (디자인팀 자문 D2 + N2 보완)
- 카드 컨테이너 `role="region"` `aria-label="팀 진행율 카드"`
- 카드 자체 `role="status"` `aria-live="polite"` (라이브 영역) — 상태 변경이 스크린리더에 안내
- 카드에 `aria-atomic="true"` — 부분 변경 시에도 전체 카드 정보로 announce
- 컨테이너에 `aria-busy` 토글 — snapshot 처리 중 announce 다중화 방지
- 카드 헤더에 `aria-label="<팀라벨>"` (한글로 안내)

#### 5-g. Empty state placeholder (C6 + N6 보완)
- 0팀 (snapshot.teams 빈 배열) 시 placeholder 카드 1개 표시
- 마크업: `<div class="tm-empty" role="status" aria-live="polite" aria-label="활성 팀 없음">활성 팀이 없습니다. <code>bash .team-workspace/set-status.sh &lt;팀명&gt; ...</code> 로 첫 팀을 추가하세요.</div>`
- CSS `.tm-empty` 신규 (team-monitor.css):
  - 기존 `--tm-card-bg` / `--tm-text-muted` 토큰 재사용
  - 진행바 없음, 회색 톤
  - 카드 폭 동일 (grid auto-fit 자동 적응)
- **i18n SSoT 진입점 (N16 향후 메모, 비차단)**: 안내 문자열 ("활성 팀 없음", "팀 진행율 카드", "활성 팀이 없습니다...") 의 SSoT 위치는 본 스프린트 종료 후 별도 기획. 현재는 hardcoded 한국어, 코드 위치는 `team-monitor.js` createCard / template 인라인.

#### 5-h. prefers-reduced-motion 가드 (N5 + N15 보완)
- CSS 미디어쿼리로 fade-in / state-changed / progress transition 일괄 비활성:
  ```css
  @media (prefers-reduced-motion: reduce) {
      .team-card,
      .team-card.state-changed,
      .progress-bar > .fill {
          animation: none !important;
          transition: none !important;
      }
      @keyframes fade-in { from, to { opacity: 1; } }
  }
  ```
- 적용 대상 셀렉터 집합 명시: `.team-card`, `.team-card.state-changed`, `.progress-bar > .fill`

### FR-6. bash 도구 동적화

#### 6-a. set-status.sh 검증 (C1 보완 — 단일 SSoT 정규식)
- 기존: `case "$team" in planner|db|developer|codex|designer)`
- 변경:
  1. 형식 검증: `[[ "$team" =~ ^[a-z][a-z0-9_-]{0,31}$ ]]` — §4-2 정규식 SSoT 와 동일 (Java/JS 도 동일 정규식 적용)
  2. teams.json 의 키 목록을 jq 로 추출 — 등록되어 있으면 OK
  3. 미등록 시 경고 메시지: "⚠ '<팀명>' 메타데이터 미등록. 카드는 default 이모지 (📋) 로 표시됩니다. .team-workspace/teams.json 추가를 권장합니다."
  4. 미등록이라도 진행은 허용 (저장은 됨) — strict 모드는 향후 별도

#### 6-b. monitor.sh TEAMS 배열
- 기존: `TEAMS=(planner db developer codex designer)` 하드코딩
- 변경:
  1. 디렉토리 스캔 — `find "$STATUS_DIR" -maxdepth 1 -type f -name '*.status' ! -name '.*' -print | xargs -n1 basename | sed 's/\.status$//' | sort` (BSD/GNU 호환)
  2. 정렬: jq 사용 가능 시 `teams.json` sort_order 적용 → fallback 알파벳순
  3. 라벨/이모지: jq 우선 → fallback (`📋 <대문자팀명>`)
  4. 0팀 (status 파일 부재) 시 경고 메시지 + 빈 모니터 (loop 만 동작)
- jq 미설치 시 fallback 동작 명시 (라벨=대문자 팀명, 이모지=📋)

#### 6-c. POSIX 호환성 / 0팀 처리 (T-L 검증)
- 디렉토리 스캔 명령은 BSD/GNU find/sed/grep 양쪽 동작
- jq 미설치 환경 fallback (label = 대문자 팀명, emoji = `📋`, sort = 알파벳)
- status 디렉토리 부재 시 mkdir 후 빈 결과
- 0팀 시 monitor.sh 는 "활성 팀 없음 — set-status.sh 호출 권장" 메시지 표시 후 idle loop

### FR-7. 단위/통합 테스트

#### 7-a. TeamStatusReaderTest 갱신
- 기존: `readAll_returnsAllFiveTeams_evenIfFilesMissing` (5개 단정)
- 변경:
  - `listTeams_emptyDir_returnsEmpty` 신규
  - `listTeams_returnsTeamsFromStatusFiles` 신규 — 테스트 디렉토리에 `foo.status`, `bar.status` 만 있으면 `[bar, foo]` 반환 (알파벳)
  - `listTeams_ignoresDotPrefixedTempFiles` 신규
  - `listTeams_appliesSortOrderFromMetadata` 신규 — teams.json 적용
  - `listTeams_cacheTtl_invalidatesAfter1s` 신규 — TTL 동작 (R-9 보완)
  - `listTeams_handlesWindowsStylePath` 신규 — Windows 경로 매칭 (R-13 / N14 보완)
  - `readAll_returnsAllPresent_evenIfFilesMissing` (이름 변경) — 5팀 단정 → "기본 5팀 메타데이터 등록되어 있고 파일이 다 있을 때 5팀 모두 non-null" 단정으로 약화

#### 7-b. JavaNioWatcherTest 신규
- `wildcardWatcher_picksUpNewTeamFile` — 디렉토리에 `newteam.status` 추가 시 콜백 발화
- `wildcardWatcher_ignoresDotPrefixedTempFile`
- `wildcardWatcher_ignoresNonStatusSuffix`
- `wildcardWatcher_reloadsTeamsJsonOnModify` — workspaceKey teams.json MODIFY 시 reload (FR-2-d)
- `wildcardWatcher_handlesOverflowWithFullRescan_noDuplicate` — OVERFLOW 시 풀스캔 + 중복 없음 (R-10, N18, T-J-02)
- `wildcardWatcher_metadataReloadOrderBeforeListTeams` — OVERFLOW 시 metadata 먼저 reload 후 listTeams 호출 (N11 보완)

#### 7-c. PollingWatcherTest 신규 (C5 보완)
- `pollingWatcher_picksUpNewTeamOnFirstTick`
- `pollingWatcher_reloadsTeamsJsonOnMtimeChange`
- `pollingWatcher_emptyDir_idlesWithoutError`

#### 7-d. TeamMetadataTest 신규
- `loadsFromJson_appliesSortOrder`
- `loadsFromJson_appliesSortOrderRename` — sortOrder ↔ sort_order 매핑 (FR-4-d)
- `missingFile_appliesFallbackDefaults`
- `missingTeam_returnsDefaultEmojiAndLabel`
- `corruptJson_logsWarningAndUsesFallback`
- `isFallbackActive_returnsTrueOnMissingFile` (T-K, C2 보완)
- `metadata_visibility_safe_publication` — AtomicReference 안전 공개 단정 (R-9 / N12)

#### 7-e. 통합 테스트 (T-A ~ T-L, §6-2)
- 기존 5팀 동작 회귀 0건
- 신규 팀 등장 시나리오 end-to-end (status 파일 추가 → SSE snapshot 에 노출 → 카드 자동 렌더)
- teams.json 핫리로드 (T-I, N7) — sort_order 변경 시 DOM 재정렬 단정 추가 (N17, T-I-02)
- OVERFLOW 폴백 + 중복 없음 (T-J, R-10 + N18, T-J-02)
- info teamMetadataFallback + 접근제어 (T-K, C2 + N19, T-K-02)
- bash 0팀/jq 미설치 + UI .tm-empty 단일 렌더 (T-L, T-add-4 + N20, T-L-02)

---

## 3. 비기능 요건 (NFR)

### NFR-1. 호환성 (N3 용어 통일)
- 기존 5팀 (planner/db/developer/codex/designer) 동작 100% 유지 — teams.json 기본값으로 동일 라벨/이모지/순서
- `team-monitor-resilience-v2` 차기 firing (2026-05-11) 영향 없음 (status 파일 형식 무변경, watcher 인터페이스 무변경)
- SSE 스키마 v1 → v2: `teamMeta` 필드 추가 (optional). 구버전 클라이언트는 fallback 매핑으로 무영향.
- **JSON 컨벤션**: 서버 Java 필드 `sortOrder` (camelCase) ↔ JSON property `sort_order` (snake_case). Jackson `@JsonProperty("sort_order")` 어노테이션으로 매핑 (FR-4-d).

### NFR-2. 운영 (C3-01 정정 + N4 path property)
- **디렉토리 구조 SSoT** (C3-01 보완):
  ```
  <workspaceDir>/                  # 기본값 ".team-workspace" (절대경로 override 가능)
  ├── status/                      # statusDir = workspaceDir/status
  │   ├── planner.status
  │   ├── db.status
  │   ├── ...
  ├── teams.json                   # 메타데이터 (C3-01: 별도 watch)
  ├── monitor.sh
  ├── set-status.sh
  └── codex-trace.sh
  ```
- WatchService 등록 (FR-2-d):
  - statusDir (`workspaceDir/status`) — `*.status` 파일 (팀 상태)
  - workspaceDir — `teams.json` 단일 파일 분리 처리
- teams.json 편집은 git tracked — 변경 commit 으로 추적. 라이브 reload (§FR-4-c).
- **workspaceDir property** (R-11 보완):
  - 신규 `application.properties` 키: `teammonitor.workspaceDir=.team-workspace` (기본값)
  - 절대경로 override 지원 — 배포 환경 별 다른 경로 가능
  - 시작 시 로그에 실제 절대경로 표기 (`.toAbsolutePath()`)
  - `statusDir` 는 internal 으로 `Path.of(workspaceDir, "status")` 도출
- 신규 팀 도입 절차 비교:
  | 단계 | 기존 (designer-team-onboarding 직후) | 본 스프린트 후 |
  |------|------------------------------------|---------------|
  | 1 | TeamStatusReader.TEAMS 1줄 수정 | (불필요) |
  | 2 | JavaNioWatcher.TEAM_FILES 1줄 수정 | (불필요) |
  | 3 | team-monitor.js TEAMS 수정 | (불필요 — dead code 제거됨) |
  | 4 | team-monitor.html 카드 14줄 추가 | (불필요 — 동적 생성) |
  | 5 | monitor.sh TEAMS+case 2줄 수정 | (불필요) |
  | 6 | set-status.sh case 1줄 수정 | (불필요) |
  | 7 | teams.json 1엔트리 추가 (이모지/라벨/순서) | **유일한 필수 단계** |
  | 8 | 단위 테스트 5팀 단정 수정 | (불필요 — 동적 단정) |
- **신규 팀 도입 = teams.json 1엔트리 추가 + set-status.sh 호출** (코드 0줄 수정)

### NFR-3. 성능
- 디렉토리 스캔 부하: status 파일 < 50개 환경 (현실적 상한) 에서 1초 폴링 = 50 file `Files.isRegularFile()` 호출/초. 캐싱 (FR-1-d) 적용 시 무시 수준.
- 캐시 TTL 1초 → 신규 팀 인식 지연 최대 1초. 사용자 체감 영향 없음 (set-status.sh 호출 후 모니터 갱신 = 같은 인터벌). NIO watcher invalidation 시 즉시 반영.
- JS 카드 동적 생성 비용: 카드 50개 한 번 생성 + 매 update DOM mutate. 기존 정적 5카드 대비 무시 수준.

### NFR-4. 디자인 토큰 일관성
- 신규 카드 (`.team-card`) 도 기존 다크 페이지 스코프 변수 (`--tm-bg`, `--tm-card-bg`, `--tm-state-*`) 그대로 재사용 (별도 토큰 X)
- empty state placeholder (`.tm-empty`) 도 기존 토큰 재사용 (`--tm-card-bg` / `--tm-text-muted`)
- CSS `.tm-cards` `repeat(auto-fit, minmax(360px, 1fr))` 그대로 — 1팀~50팀 모두 자동 적응
- fallback 이모지 (📋) 와 default 라벨 (대문자) 도 디자인 일관성 검증 (디자인팀 자문 §자문-디자인 D5)

### NFR-5. 보안 (N10 + N13 보완)

#### XSS 방어
- teams.json 입력은 신뢰 영역 (git tracked, 사용자 직접 편집)
- JS 동적 카드 생성 시 `textContent` 사용 (innerHTML 금지) — 이모지/라벨/팀명 모두
- 팀명 형식 검증 — §4-2 SSoT 정규식 `^[a-z][a-z0-9_-]{0,31}$` (Java + bash + JS 모두 동일)
- schema_version 검증 (잘못된 버전 → fallback)

#### Actuator 노출 보안 (N10 + N13 신규 박스 — R-12 보완)

> ⚠ **info endpoint 보안 정책** (필수)
>
> `teamMetadataFallback` 필드는 운영자에게만 노출:
> - `application.properties`: `management.endpoints.web.exposure.include=health,info` (이미 적용됨)
> - **Spring Security 설정**: `/actuator/info` 는 `ROLE_ADMIN` 권한 필수. 비인가 401/403, 인가 200.
> - 회귀 차단: T-K-02 통합 테스트 — 비인가 요청 401/403 + ADMIN 인증 200 + `teamMetadataFallback` 필드 검증
>
> 미설정 시 위험: 익명 `/actuator/info` 호출로 내부 메타데이터 fallback 상태 노출.

#### 외부 의존성
- 추가 외부 의존성 0건 (CSP 영향 없음)

---

## 4. 데이터 / 설계

### 4-1. 데이터 (C3-01 정정)
- 디렉토리 구조: `<workspaceDir>/status/*.status` (팀 상태) + `<workspaceDir>/teams.json` (메타) — §NFR-2 SSoT
- 신규 파일: `.team-workspace/teams.json` (commit, git tracked, ~30줄)
- 신규 클래스: `TeamMetadata` (Spring component)
- 변경: `TeamStatusReader.TEAMS` 상수 → `listTeams()` 메서드
- 변경: `TeamStatusEvent.Snapshot` DTO — `teamMeta` 필드 추가
- 변경: `TeamMonitorInfoContributor` — `teamMetadataFallback` 필드 추가 (C2 보완)
- DB 변경 없음

### 4-2. teams.json 스키마 (단일 SSoT — C1 보완)

```json
{
  "schema_version": 1,
  "teams": {
    "<team_name>": {
      "emoji": "<emoji>",
      "label": "<label>",
      "sort_order": <int>
    }
  },
  "default_emoji": "<emoji>"
}
```

- **`team_name` 정규식 (전 영역 SSoT)**: `^[a-z][a-z0-9_-]{0,31}$`
  - 영문 소문자로 시작
  - 영문 소문자 + 숫자 + 하이픈(-) + 언더스코어(_)
  - 1-32자 (시작 1자 + 추가 0-31자)
  - 본 정규식은 Java (`TeamMetadata` validate) / bash (`set-status.sh`) / JS (클라이언트 검증) 모두 동일 적용
- `emoji`: 1 unicode emoji (호환 광범위 권장)
- `label`: 1-32자 (대문자 ASCII 권장 — 모노스페이스 폰트 정합)
- `sort_order`: int. 0 ~ 999. 작을수록 앞.
- `default_emoji`: optional. 미정의 시 코드 내장 default `📋`.

### 4-3. 영향 파일 (예상 — §7 와 일치)

#### 신규
- `docs/product-specs/team-monitor-wildcard-watcher.md` (본 문서)
- `docs/exec-plans/team-monitor-wildcard-watcher.md` (개발계획 — codex 통과 후)
- `.team-workspace/teams.json`
- `src/main/java/com/swmanager/system/service/teammonitor/TeamMetadata.java` (~120줄)
- `src/test/java/com/swmanager/system/service/teammonitor/TeamMetadataTest.java` (~120줄)
- `src/test/java/com/swmanager/system/service/teammonitor/JavaNioWatcherTest.java` (~140줄)
- `src/test/java/com/swmanager/system/service/teammonitor/PollingWatcherTest.java` (~80줄, C5 보완)
- `.team-workspace/check-no-n-team-assumptions.sh` (T-H 회귀 차단 스크립트)

#### 수정
- `src/main/java/com/swmanager/system/service/teammonitor/TeamStatusReader.java` (TEAMS → listTeams + 캐시)
- `src/main/java/com/swmanager/system/service/teammonitor/JavaNioWatcher.java` (TEAM_FILES 제거 + 두 WatchKey + OVERFLOW)
- `src/main/java/com/swmanager/system/service/teammonitor/PollingWatcher.java` (listTeams 호출 + teams.json mtime 추적)
- `src/main/java/com/swmanager/system/service/teammonitor/TeamMonitorService.java` (snapshot 빌드 — teamMeta 포함)
- `src/main/java/com/swmanager/system/health/TeamMonitorInfoContributor.java` (teamMetadataFallback 필드 추가, C2 보완 — JavaDoc 에 ADMIN 보호 전제 명시 IF-02)
- `src/main/java/com/swmanager/system/dto/TeamStatusEvent.java` (Snapshot record + teamMeta 필드)
- `src/main/java/com/swmanager/system/config/TeamMonitorProperties.java` (workspaceDir property, R-11 보완)
- `src/main/java/com/swmanager/system/config/SecurityConfig.java` 또는 등가 (Spring Security `/actuator/info` ADMIN 보호, N13)
- `src/test/java/com/swmanager/system/service/teammonitor/TeamStatusReaderTest.java`
- `src/main/resources/templates/admin/team-monitor.html` (정적 카드 5개 → 빈 컨테이너 + role/aria, ~70줄 삭제 + 5줄 추가)
- `src/main/resources/static/js/team-monitor.js` (createCard, dead code 제거, aria-busy 토글, ~70줄 변경)
- `src/main/resources/static/css/team-monitor.css` (`.tm-empty` placeholder + prefers-reduced-motion 가드, ~25줄 추가, C6 + N5 + N15 보완)
- `src/main/resources/application.properties` (teammonitor.workspaceDir 키 1줄 추가)
- `.team-workspace/monitor.sh` (~30줄 변경)
- `.team-workspace/set-status.sh` (~15줄 변경)

#### 변경 없음
- DB 스키마
- AGENTS.md / CLAUDE.md (워크플로우 무영향)
- TeamMonitorController/Advice/SecurityHeadersFilter
- TeamMonitorHealthIndicator (info 가 부담 — health 그대로)
- DTO (TeamStatus / TimelineEntry / UlidGenerator)

### 4-4. 시퀀스 (신규 팀 도입 — C3-01 반영)

```
1. 사용자: vi <workspaceDir>/teams.json
   → "tester" 엔트리 추가 (emoji=🧪, label=TESTER, sort_order=60)
   → JavaNioWatcher (workspaceKey) 가 teams.json MODIFY 감지 → teamMetadata.reload()
2. 사용자: bash <workspaceDir>/set-status.sh tester 진행중 0 "스모크 셋업"
   → 정규식 검증 통과 (^[a-z][a-z0-9_-]{0,31}$)
   → <statusDir>/tester.status 파일 생성
3. JavaNioWatcher (statusKey): ENTRY_CREATE 발화 → reader.invalidateCache() + notifySubscribers("tester")
4. TeamMonitorService: latestCache.put("tester", ...) + SSE update 송출
5. 다음 SSE snapshot: teams 배열에 tester 포함 + teamMeta.tester={🧪,TESTER,60}
6. 클라이언트 JS: snapshot 수신 → 컨테이너 aria-busy=true → 신규 팀 감지 → createCard → 컨테이너 append → aria-busy=false
7. 사용자 모니터 화면: 6번째 카드 자동 등장 (CSS state-changed fade-in, prefers-reduced-motion 시 정적)
```

---

## 5. 리스크 & 보완

### R-1. 디렉토리 스캔 부하
- **리스크**: 1초 폴링 + 매 tick 디렉토리 스캔 = 누적 부하
- **보완**:
  - M-1a 캐싱 (FR-1-d) — TTL 1초 + watcher 이벤트 기반 invalidate
  - M-1b 운영 환경 status 파일 < 50개 가정 — 그 이상이면 별도 alert
  - M-1c JavaNioWatcher 가 ENTRY_CREATE/DELETE 수신 시 캐시 invalidate → 신규 팀 즉시 인식

### R-2. teams.json 깨짐 / 손상
- **리스크**: JSON 파싱 실패 시 모니터 전체 동작 불능 우려
- **보완**:
  - M-2a 코드 내장 5팀 fallback — teams.json 부재 시에도 정상 동작 (`// ALLOW_FIVE_TEAM_FALLBACK` 센티넬, T-H 예외)
  - M-2b corrupt JSON → WARN 로그 + fallback. info endpoint 에 `teamMetadataFallback=true` 노출 (운영자 인지 가능, C2 보완 — ADMIN 보호 N13)
  - M-2c teams.json 에 schema_version 명시 — 미래 호환

### R-3. 임시 파일 / 부적절 파일 인식
- **리스크**: `.team.status.XXXXX` (atomic write 임시) 또는 `foo.status.bak` 등 인식
- **보완**:
  - M-3a dot-prefix (`.`) 시작 파일 무시 (FR-1-b)
  - M-3b 정확히 `.status` suffix 만 (다른 suffix 무시) — `PathMatcher("glob:*.status")` + 파일명 기준 매칭 (N9)
  - M-3c 단위 테스트 T-D 회귀 차단

### R-4. dead code 제거 영향 (team-monitor.js TEAMS)
- **리스크**: 누군가 본 상수를 참조하는데 grep 으로 못 찾을 수도
- **보완**:
  - M-4a 본 스프린트에서 정리 전 grep 으로 use site 0건 확인 (이미 본 기획서 작성 시 확인됨 — line 6 만 declaration, 다른 사용처 없음)
  - M-4b 제거 후 회귀 테스트 — 모든 SSE 이벤트 핸들러 정상 동작 (T-D)

### R-5. 카드 동적 생성 시 깜박임 / 레이아웃 시프트
- **리스크**: snapshot 수신 → DOM 새로 그리면 첫 로드 시 빈 화면 깜박
- **보완**:
  - M-5a 초기 HTML 에 placeholder ("팀 데이터 로딩 중...") 1개 포함, snapshot 수신 직후 제거
  - M-5b 카드 생성은 `DocumentFragment` 사용 — reflow 1회로 모음
  - M-5c CSS transition 으로 카드 등장 fade-in (기존 `.state-changed` 재사용, prefers-reduced-motion 시 OFF — N5 + N15)

### R-6. 정렬 순서 깨짐 (운영 전통 보존)
- **리스크**: 사용자가 teams.json 잘못 편집 → planner 가 마지막에 등장 (어색)
- **보완**:
  - M-6a teams.json 기본값에 sort_order 명시
  - M-6b 미정의 시 99 + 알파벳순
  - M-6c 검증 시 FR-1-c 정책 명시

### R-7. 한국어 라벨 / 이모지 환경별 렌더링
- **리스크**: 일부 단말 / 폰트 환경에서 이모지 깨짐
- **보완**:
  - M-7a fallback emoji `📋` 도 일반 unicode (호환 광범위)
  - M-7b CSS `.team-emoji` 폰트는 시스템 emoji font 자동 사용
  - M-7c 모니터 텍스트 (라벨) 은 ASCII 권장 — 한글 라벨 사용 시 모노스페이스 폰트 영향 가능

### R-8. 운영 롤백 가이드

배포 후 회귀 발견 시 영역별 최소 비용 롤백:

| 영역 | 증상 | 핫픽스 (코드 미수정) | 정식 롤백 |
|------|------|---------------------|-----------|
| 백엔드 — listTeams 깨짐 | snapshot 빈 teams 배열 | (없음) | 본 스프린트 commit revert |
| teams.json 손상 | info 에 teamMetadataFallback=true | teams.json 백업 복구 | git checkout teams.json |
| 프런트 — 카드 미렌더 | 카드 영역 빈 화면 | 브라우저 새로고침 (snapshot 재수신) | template revert (정적 카드 복구) |
| SSE 스키마 v2 미수신 | 구버전 클라이언트 fallback 동작 | (영향 없음) | (불필요) |
| watcher 신규 팀 미인식 | 카드 등장 지연 1초 초과 | server-restart.sh | watcher commit revert |
| OVERFLOW 풀스캔 누락 | 다량 변동 후 일부 팀 미반영 | server-restart.sh | JavaNioWatcher OVERFLOW 분기 점검 |
| /actuator/info ADMIN 보호 누락 | 익명 노출 | Spring Security 설정 1줄 추가 | SecurityConfig revert |

### R-9. 캐시 일관성 / 가시성 (N12 보완)
- **리스크**: 비안전 공개 → 다른 스레드에서 stale 관찰
- **보완**:
  - M-9a `AtomicReference<CachedTeams>` + `CachedTeams` 불변 record (모든 필드 final, FR-1-d)
  - M-9b watcher invalidate 와 TTL 만료 양쪽 단위 테스트 회귀 차단
  - M-9c TTL miss 시 즉시 재계산 — race 시 동일 결과 수렴

### R-10. WatchService OVERFLOW (C4 + N11 + N18)
- **리스크**: OS 큐 오버플로 → 일부 ENTRY_* 이벤트 손실
- **보완**:
  - M-10a OVERFLOW 이벤트 수신 시 풀스캔 폴백 (FR-2-e)
  - M-10b 분기 순서: invalidate → metadata.reload() → listTeams() → notify (메타 의존성)
  - M-10c notifySubscribers 중복 차단 (HashSet)
  - M-10d 단위 테스트 `wildcardWatcher_handlesOverflowWithFullRescan_noDuplicate`
  - M-10e info endpoint 의 `lastError` + 자체 카운터로 발생 빈도 모니터링

### R-11. 상대경로 의존성 (N4 보완)
- **리스크**: `.team-workspace` 상대경로 → 실행 디렉토리 의존
- **보완**:
  - M-11a `teammonitor.workspaceDir` property — 절대경로 override (NFR-2)
  - M-11b 시작 시 절대경로 로그 표기
  - M-11c 기본값 `.team-workspace` 후방 호환

### R-12. Actuator info 노출 오구성 (N13 신규)
- **리스크**: Spring Security 미설정 시 익명 `/actuator/info` 호출로 `teamMetadataFallback` 등 내부 상태 노출
- **보완**:
  - M-12a §NFR-5 보안 박스로 정책 고정 (Spring Security ADMIN 보호 명시)
  - M-12b `SecurityConfig.java` (또는 등가) 에 `/actuator/info` ROLE_ADMIN 매처 추가
  - M-12c 통합 테스트 T-K-02 — 비인가 401/403, ADMIN 200 검증

### R-13. 플랫폼별 경로 매칭 편차 (N14 신규)
- **리스크**: Windows 경로 구분자 (`\`) vs Unix (`/`) → glob 매칭 오동작
- **보완**:
  - M-13a 매칭 SSoT — `matcher.matches(p.getFileName())` 파일명 기준 (N9)
  - M-13b 단위 테스트 `listTeams_handlesWindowsStylePath` — Windows-스타일 경로 케이스 포함
  - M-13c PollingWatcher 도 동일 SSoT 적용 (FR-3)

### 리스크 매트릭스 요약

| ID | 영역 | 영향도 | 발생빈도 | 우선순위 |
|----|------|--------|----------|----------|
| R-1 | 성능 | 중 | 중 | P1 |
| R-2 | 안정성 | 상 | 하 | **P0** |
| R-3 | 회귀 | 중 | 중 | P1 |
| R-4 | 회귀 | 하 | 하 | P2 |
| R-5 | UI | 중 | 중 | P1 |
| R-6 | 운영 | 하 | 중 | P2 |
| R-7 | UI | 하 | 하 | P2 |
| R-9 | 안정성 | 중 | 하 | P1 |
| R-10 | 안정성 | 중 | 하 | P1 |
| R-11 | 운영 | 중 | 하 | P1 |
| R-12 | 보안 | **상** | 하 | **P0** |
| R-13 | 회귀 | 중 | 하 | P1 |

---

## 6. 검증 계획

### 6-1. 단위 테스트
- `TeamStatusReaderTest` — listTeams 룰, 정렬, 캐싱 TTL/invalidate, Windows 경로 (FR-7-a)
- `JavaNioWatcherTest` — 와일드카드 매칭, teams.json reload (workspaceKey), OVERFLOW + 중복 차단 + 순서 (FR-7-b)
- `PollingWatcherTest` — 신규 팀 첫 등장, teams.json mtime 추적 (FR-7-c, C5 보완)
- `TeamMetadataTest` — JSON 로드, fallback, isFallbackActive, 안전 공개 (FR-7-d)

### 6-2. 통합/회귀

- **T-A**: 5팀 status 파일 그대로 — `/admin/team-monitor` 5팀 카드 정상. 기존 정적 5팀 시나리오 회귀 0건.
- **T-B**: `set-status.sh tester ...` → tester.status 생성 → 모니터 1초 내 6번째 카드 자동 등장. teams.json 미등록 → default emoji `📋` + 라벨 `TESTER`.
- **T-C**: teams.json 에 tester 메타데이터 추가 → 카드 emoji/label 갱신 (다음 snapshot reload).
- **T-D**: tester.status 파일 삭제 → 모니터에서 카드 자동 제거.
- **T-E**: `.tester.status.XXX` (dot-prefix 임시파일) → 무시 (모니터에 등장 X).
- **T-F**: teams.json 손상 (잘못된 JSON) → WARN 로그 + fallback. 5팀 default 동작.
- **T-G**: SSE schema_version 1 클라이언트 (legacy) — teamMeta 미수신 → fallback 매핑 (5팀 hardcoded). 무에러.

#### T-H (회귀 차단 grep — N8 + N21 보완)

향후 N팀 가정 코드 재발 차단. CI 통합 권장. 패턴 발견 시 빌드 실패:

```bash
#!/usr/bin/env bash
# T-H: N팀 가정 회귀 차단 grep
# 코드 내장 fallback 블록은 ALLOW_FIVE_TEAM_FALLBACK 센티넬 주석으로 예외
# 코딩 규약: 센티넬은 fallback 블록 바로 위 줄에 // (Java/JS) 또는 # (sh) 로

set -Euo pipefail   # N21: 안전 가드 — 실패 즉시 전파
HITS=0

# 1) 셸 case 고정 열거 (planner|db|developer|codex|designer 5팀 case)
HITS_SH=$(grep -ErHn --include='*.sh' \
    'case[^|]*planner.*db.*developer.*codex.*designer' \
    .team-workspace/ src/ 2>/dev/null | grep -v 'ALLOW_FIVE_TEAM_FALLBACK' || true)
# 멀티라인 case 패턴 보강 (선택 — pcregrep -M 또는 grep -Pzo 옵션 가능):
#   pcregrep -M 'case[\s\S]*?planner[\s\S]*?db[\s\S]*?developer[\s\S]*?codex[\s\S]*?designer'

# 2) Java/JS/HTML 5팀 명시 열거 (한 줄에 5팀 모두) — Java 는 main 만 (테스트 제외, N21 TH-01)
HITS_CODE_JAVA=$(grep -ErHn --include='*.java' \
    'planner.*db.*developer.*codex.*designer' \
    src/main/java 2>/dev/null | grep -v 'ALLOW_FIVE_TEAM_FALLBACK' || true)
HITS_CODE_FRONT=$(grep -ErHn --include='*.js' --include='*.html' \
    'planner.*db.*developer.*codex.*designer' \
    src/main/resources 2>/dev/null | grep -v 'ALLOW_FIVE_TEAM_FALLBACK' || true)

# 3) Java size/length == 4 또는 == 5 (main 만)
HITS_JAVA=$(grep -ErHn --include='*.java' \
    '\b(size|length)\s*\(\)\s*==\s*[45]\b' \
    src/main/java 2>/dev/null | grep -v 'ALLOW_FIVE_TEAM_FALLBACK' || true)

# 4) JS .length === 4 또는 === 5
HITS_JS=$(grep -ErHn --include='*.js' \
    '\.length\s*===?\s*[45]\b' \
    src/main/resources/static 2>/dev/null | grep -v 'ALLOW_FIVE_TEAM_FALLBACK' || true)

# 5) CSS — 5팀 명시 grid (4팀 가정 제거 — 오탐 회피)
HITS_CSS=$(grep -ErHn --include='*.css' \
    'grid-template-columns:\s*repeat\(\s*5\s*,\s*1fr\)' \
    src/main/resources/static 2>/dev/null | grep -v 'ALLOW_FIVE_TEAM_FALLBACK' || true)

# 6) 명시 상수
HITS_CONST=$(grep -ErHn --include='*.java' --include='*.js' --include='*.sh' \
    '\b(FourTeams|FiveTeams|TEAMS_4|TEAMS_5)\b' \
    src/ .team-workspace/ 2>/dev/null | grep -v 'ALLOW_FIVE_TEAM_FALLBACK' || true)

for h in "$HITS_SH" "$HITS_CODE_JAVA" "$HITS_CODE_FRONT" "$HITS_JAVA" "$HITS_JS" "$HITS_CSS" "$HITS_CONST"; do
    if [ -n "$h" ]; then
        echo "❌ N팀 가정 패턴 발견:"
        echo "$h"
        HITS=1
    fi
done

# 제외:
#   docs/ — 의도적 (예시 코드 / 역사 스냅샷)
#   .team-workspace/teams.json — 정의 파일 (5팀 기본값)
#   src/test/java — 테스트 유틸 (정렬 단정 등 정상 사용)

[ $HITS -eq 0 ] || exit 1
echo "✅ T-H 통과 — N팀 가정 패턴 0건"
```

- **예외 메커니즘 (N21 TH-04)**: 코드 내장 fallback 블록 **바로 위 줄에** 센티넬 주석 (`// ALLOW_FIVE_TEAM_FALLBACK` Java/JS, `# ALLOW_FIVE_TEAM_FALLBACK` sh). grep -v 로 통과
- **제외 디렉토리 (N21 TH-01)**: `docs/` (예시 / 역사), `.team-workspace/teams.json` (정의), `src/test/java` (테스트 유틸)
- **안전 가드 (N21 TH-03)**: `set -Euo pipefail` 으로 파이프라인 오류 전파
- **멀티라인 case (N21 TH-02)**: `pcregrep -M` 또는 `grep -Pzo` 활용 가능 (주석으로 명시)

#### 신규 검증 (N7 + N17~20 보완)
- **T-I (teams.json 핫리로드 + sort_order 재정렬, N17)**:
  - T-I-01: 라벨/이모지 변경 → 다음 snapshot 에 새 라벨 반영 (서버 재기동 X)
  - **T-I-02**: 같은 팀 집합에서 sort_order 변경 (예: planner=10 → 99) → DOM 카드 순서가 새 정렬 그대로 반영 단정 (UI 재정렬 검증)
- **T-J (OVERFLOW 폴백 + 중복 차단, N18)**:
  - T-J-01: 다량 status 파일 동시 생성 → OVERFLOW 발화 → 풀스캔으로 모든 팀 notifySubscribers
  - **T-J-02**: 풀스캔 후 스냅샷의 팀 집합이 정확히 1회 반영 (notifySubscribers 호출 횟수 = 팀 수, 중복 0건) 단정
- **T-K (info teamMetadataFallback + 접근제어, N19)**:
  - T-K-01: teams.json 손상 후 ADMIN 인증 `/actuator/info` 호출 → `teamMetadataFallback: true` 노출
  - **T-K-02**: 비인가 (인증 X 또는 비ADMIN) `/actuator/info` 호출 → 401/403. ADMIN 인증 → 200 + `teamMetadataFallback` 필드 정상.
- **T-L (bash 0팀 / jq 미설치 + UI .tm-empty, N20)**:
  - T-L-01: 0팀 (status 디렉토리 빔) → monitor.sh "활성 팀 없음" + idle loop. set-status.sh tester → 1팀 등장.
  - T-L-01b: jq 미설치 → fallback 매핑 (라벨=대문자, 이모지=📋)
  - **T-L-02**: 0팀 web UI — snapshot.teams 빈 배열 수신 시 `.tm-empty` placeholder 정확히 1개 렌더 단정 (e2e)

### 6-3. 수동 확인
- 화면 폭 변화: 1280/1600/1920 에서 카드 1~10개 그리드 자연스러움
- 풀스크린 모드 (`?fullscreen=1`) 도 동일 자동 적응
- 카드 등장/사라짐 fade-in/out 부드러움 (prefers-reduced-motion: reduce 시 정적, N15 검증)
- 접근성: 스크린리더 (NVDA/VoiceOver) 로 카드 상태 변경 announce 확인 (디자인팀 자문 §자문-디자인 D2 검증) — 컨테이너 aria-busy 토글로 다중 announce 방지 확인
- empty state placeholder SR 안내 — "활성 팀 없음" 발화 확인 (D6 검증)

### 6-4. 운영 시나리오 dry-run
- 가상 6번째 팀 (예: tester) 도입 — teams.json 1엔트리 + set-status.sh 1회 → 코드 변경 0건으로 완전 통합 확인
- 즉시 teams.json 만 revert → 5팀 default 동작 복구 확인 (롤백 dry-run)

---

## 7. 영향 파일 (확정 — §4-3 와 일치)

### 신규
- `docs/product-specs/team-monitor-wildcard-watcher.md` (본 문서)
- `docs/exec-plans/team-monitor-wildcard-watcher.md` (개발계획 — codex 통과 후)
- `.team-workspace/teams.json`
- `src/main/java/com/swmanager/system/service/teammonitor/TeamMetadata.java`
- `src/test/java/com/swmanager/system/service/teammonitor/TeamMetadataTest.java`
- `src/test/java/com/swmanager/system/service/teammonitor/JavaNioWatcherTest.java`
- `src/test/java/com/swmanager/system/service/teammonitor/PollingWatcherTest.java` (C5)
- `.team-workspace/check-no-n-team-assumptions.sh` (T-H)

### 수정
- `src/main/java/com/swmanager/system/service/teammonitor/TeamStatusReader.java`
- `src/main/java/com/swmanager/system/service/teammonitor/JavaNioWatcher.java`
- `src/main/java/com/swmanager/system/service/teammonitor/PollingWatcher.java`
- `src/main/java/com/swmanager/system/service/teammonitor/TeamMonitorService.java`
- `src/main/java/com/swmanager/system/health/TeamMonitorInfoContributor.java` (C2 + JavaDoc IF-02)
- `src/main/java/com/swmanager/system/dto/TeamStatusEvent.java`
- `src/main/java/com/swmanager/system/config/TeamMonitorProperties.java` (R-11)
- `src/main/java/com/swmanager/system/config/SecurityConfig.java` 또는 등가 (N13 — `/actuator/info` ADMIN)
- `src/test/java/com/swmanager/system/service/teammonitor/TeamStatusReaderTest.java`
- `src/main/resources/templates/admin/team-monitor.html`
- `src/main/resources/static/js/team-monitor.js`
- `src/main/resources/static/css/team-monitor.css` (`.tm-empty` + prefers-reduced-motion)
- `src/main/resources/application.properties` (workspaceDir 키)
- `.team-workspace/monitor.sh`
- `.team-workspace/set-status.sh`

### 변경 없음
- DB 스키마
- AGENTS.md / CLAUDE.md
- TeamMonitorController/Advice/SecurityHeadersFilter
- TeamMonitorHealthIndicator
- DTO (TeamStatus / TimelineEntry / UlidGenerator)

---

## 8. 권한 / 보안 체크리스트

- [ ] 기존 `/admin/team-monitor` ADMIN 권한 그대로
- [ ] 보안 헤더 필터 영향 없음 (URL 패턴 동일)
- [ ] teams.json 권한 — git tracked, 사용자 직접 편집 (관리자 영역)
- [ ] JS 동적 카드 생성 시 `textContent` 사용 (innerHTML 금지)
- [ ] 팀명 형식 검증 — §4-2 SSoT 정규식 `^[a-z][a-z0-9_-]{0,31}$` (Java + bash + JS 모두 동일, C1)
- [ ] schema_version 검증 (잘못된 버전 → fallback)
- [ ] 추가 외부 의존성 0건 (CSP 영향 없음)
- [ ] **`/actuator/info` ROLE_ADMIN 보호** (N10 + N13, R-12) — Spring Security 매처 + T-K-02 회귀 검증
- [ ] info `teamMetadataFallback` 필드는 ADMIN 인가 후에만 노출

---

## §자문-디자인 (A 정책 — 디자인팀 자문)

본 스프린트는 UI 변경 (정적 카드 → 동적 생성) 포함이므로 A 정책. 디자인팀 자문 결과:

### D1. 카드 동적 생성 패턴 — ⭕
- DocumentFragment 사용으로 reflow 최소화 (R-5 보완)
- 카드 등장 시 기존 `.state-changed` fade-in 재사용 — 새 토큰 불필요

### D2. 접근성 (a11y) 강화 — ⭕ (FR-5-f / N2 / N5 / N15 반영)
- 카드 동적 추가/제거는 스크린리더가 인지해야 함
- 보완:
  - 컨테이너: `role="region"` `aria-label="팀 진행율 카드"`
  - 개별 카드: `role="status"` `aria-live="polite"` `aria-atomic="true"`
  - 컨테이너 `aria-busy` 토글 (snapshot 처리 중 다중 announce 방지)
  - **N15 — `prefers-reduced-motion: reduce` 시 fade-in / state-changed / progress transition 일괄 비활성** (FR-5-h CSS 셀렉터 집합 명시)
- 카드 제거 시 announce 불필요 (DOM 제거만)

### D3. 정렬 순서 — ⭕
- 운영 전통 (planner=10, ...) sort_order 기본값 명시 — 시각적 일관성 보존
- 신규 팀은 sort_order 미정의 시 99 + 알파벳 — 예측 가능

### D4. fallback emoji 선택 — ⭕
- `📋` 클립보드 이모지 — "팀 = 작업 목록" 메타포 적합
- 다른 후보 (`👥`, `🏷️`, `❓`) 보다 중립적
- Unicode 호환 광범위 (R-7 보완)

### D5. fallback 라벨 디자인 — ⭕ (FR-5-f)
- 대문자 변환 만으로는 모노스페이스 정렬 깨질 수 있음 (`tester` → `TESTER` 6글자, `qa` → `QA` 2글자)
- 보완: CSS `.team-label` 의 `letter-spacing: 0.05em` 그대로 + `min-width` 제거 — 카드 폭은 grid auto-fit 으로 균일 유지
- 라벨 길이 제한 (1-32자) 으로 극단적 케이스 차단

### D6. Empty state — ⭕ (N6 / FR-5-g)
- 0팀 시 `.tm-empty` placeholder 카드 1개 표시
- placeholder CSS: 기존 카드 토큰 (`--tm-card-bg` / `--tm-text-muted`) 재사용, 진행바 숨김, 회색 톤
- placeholder SR 안내: `role="status"` + `aria-live="polite"` + `aria-label="활성 팀 없음"`
- **i18n SSoT 진입점 (N16, 비차단 향후 메모)**: 안내 문자열 SSoT 위치는 본 스프린트 종료 후 별도 기획. 현재 hardcoded 한국어, 코드 위치 — `team-monitor.js` `createEmptyPlaceholder()` + template 인라인.

### D7. 카드 폭 / 그리드 — ⭕
- 기존 `repeat(auto-fit, minmax(360px, 1fr))` 변경 없음 — 1팀~50팀 자동
- 1팀일 때 카드 1개가 row 전체 차지 → 어색함 가능 → max-width 검토 권장 (개발계획에서 결정)

### D8. 팀 색상 토큰 — ⭕ (비범위)
- 본 스프린트는 팀별 컬러 X (state 컬러만)
- 향후 팀별 ID 컬러 (예: planner=teal, db=blue) 도입 시 별도 스프린트

### 디자인팀 자문 종합 판정
- **⭕ 통과** (D2/D5/D6 보완 사항 본 기획서 §FR-5-f / §FR-5-g / §FR-5-h / §자문-디자인 D2/D5/D6 모두 반영 완료)

---

## §자문-DB (DB팀 자문)

- DB 스키마 무변경
- 신규 테이블/인덱스/마이그레이션 없음
- DB 연결/쿼리 영향 없음
- **판정**: ⭕ skip (자문 불필요)

---

*이 기획서가 codex 검토 통과 + 사용자 최종승인 후에만 개발계획 단계로 진입합니다.*
