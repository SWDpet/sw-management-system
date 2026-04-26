---
tags: [dev-plan, sprint, workflow, team-monitor, refactor]
sprint: "team-monitor-wildcard-watcher"
status: draft-v5
created: "2026-04-26"
revised: "2026-04-26"
---

# [개발계획서] team-monitor 와일드카드 watcher — v5

- **작성팀**: 개발팀
- **작성일**: 2026-04-26 (v1~v5 동일자)
- **근거 기획서**: [[../product-specs/team-monitor-wildcard-watcher|기획서 v3]] (codex ⭕ + 사용자 최종승인 ✓)
- **상태**: 초안 v5 (codex round 7 ⚠ → minor cleanup 6건 정리)

### v4 → v5 변경점 (round 7 ⚠ — 모두 clarification/cleanup)

| ID | 내용 | 위치 |
|----|------|------|
| DOC-VERSION | 문서 말미 "v2" 잔존 표기 → "v5" 정정 | §7 승인 요청 |
| TEST-PATTERN | `-Dtest='*WatcherTest'` 패턴 → 클래스 명시 (`-Dtest=JavaNioWatcherTest,PollingWatcherTest`) | §6-1 commit 4 |
| T-H-LOGSKIP | check 스크립트의 `log_skip` 함수 미정의 → 단순 `echo` 정의 추가 | Phase 6 Step 6-3 |
| S4-01-b-mini | commit 5 ~ 6 구간 T-H 공백 보강 — commit 5/6 테스트 칸에 mini grep 3종 one-liner 추가 (Java/JS/CSS 핵심) | §6-1 |
| TEST-ORDER-c6 | §6-1 commit 6 테스트 칸 — manual 라이브 스모크 명령 명시 | §6-1 |
| T-G-harness | T-G-clarify e2e 실행 방법 — manual 브라우저 검증 단계 명시 (Puppeteer 자동화는 본 스프린트 비범위, 별도 후속) | §2, Phase 7 Step 7-3 |
| 권고 | T-P-strong — `OutputCaptureExtension` 로그 단정 (구현 디테일 — 비차단) | Phase 7 Step 7-3 |
| 권고 | SEC-01-curl 명령 표 구체화 (curl 헤더 grep) | Phase 7 Step 7-3 |
| 권고 | ORDER-FLIP-NOTE — commit 3/4 구간 알파벳 정렬 일시 변화 안내 | Phase 7 Step 7-4 |

### v3 → v4 변경점 (round 6 ⚠ 차단 2건 + 비차단 5건)

| ID | 내용 | 위치 |
|----|------|------|
| **S4-01-a-NPE (차단)** | `TeamStatusReader.scanAndSort()` 정렬 Comparator 를 null-safe 분기 — `teamMetadata == null` 시 알파벳 정렬 (CODE_BUILTIN 기반 정렬 X — 단순화). @Deprecated 생성자 단독 사용 시 NPE 차단. | Phase 3 Step 3-1 |
| **S4-01-b-TIMING (차단)** | T-H 첫 실행 시점 정정 — commit 7 (스크립트 신규 + 본문 작성) 로 변경 (옵션 B). §6-1 표 + §6-2 commit 7 본문 명시. | §6-1, §6-2 |
| TEST-ORDER-clarify | §6-1 표의 각 칸에 실제 실행 명령 구체화 (`./mvnw -DskipTests clean compile`, `./mvnw -q -Dtest=... test`, `bash check-no-n-team-assumptions.sh`) | §6-1 |
| T-P-strong | T-P 강화 — 기본 경로 미사용 부정 단정 + watcher 등록 디렉터리 로그 캡처 단정 | §2, Phase 7 Step 7-3 |
| T-G-clarify-strong | T-G-clarify 강화 — `window.onerror` / `console.error` 카운트 0 + 카드 수/DOM 안정 + teamMeta 미존재 시 fallback 단정 | §2, Phase 7 Step 7-3 |
| SEC-01-curl | SEC-01 강화 — `curl -i` 익명 → `HTTP/1.1 401` + `WWW-Authenticate: Basic` 헤더 단정 (302 없음 명시), USER → `403` | Phase 7 Step 7-3 |
| DOC-01-comment | §6-2 commit 7 본문에 "docs 변경은 commit 7 에 실반영 (운영 문서 동기화)" 주석 추가 | §6-2 commit 7 |
- **목표**: 기획서 §FR-1~7, §NFR-1~5, §R-1~13 전부 구현. 단일 PR 머지.

### v2 → v3 변경점 (round 5 ⚠ 차단 2건 + 비차단 8건)

| ID | 내용 | 위치 |
|----|------|------|
| **S4-01-a (차단)** | commit 3 의 `TeamStatusReader` 생성자 변경에 기존 시그니처 오버로드 `@Deprecated` 유지 — commit 3 단독 호출자 컴파일 보존 | Phase 3 Step 3-1, §6 |
| **S4-01-b (차단)** | commit 3 임시 TEAMS 블록 위에 `// ALLOW_FIVE_TEAM_FALLBACK` 센티넬 명시 + T-H 실행 시점을 commit 5 (TEAMS 최종 제거) **이후로 고정** | Phase 3 Step 3-1, Phase 7 Step 7-5, §6 |
| S2-05-a | `recentlyNotified` Set 메모리 누적 방지 — 키 처리 주기마다 `clear()` 또는 짧은 TTL Map | Phase 3 Step 3-2 |
| S3-01-a | T-H grep `-Pzo` 기능 감지에 `grep --version \| grep -q 'GNU'` 분기 추가 | Phase 6 Step 6-3 |
| S2-06 | `TeamMeta` 레코드에 `@JsonAlias({"sort_order","sortOrder"})` 적용 — 양방향 호환 | Phase 2 Step 2-1, Phase 4 Step 4-3 |
| SEC-01 | `/actuator/info` 401/403 보장 — `httpBasic(withDefaults())` 명시 + 폼 로그인 분리 (관리 엔드포인트 전용 시큐리티 체인 또는 폼 로그인 비활성) | Phase 4 Step 4-5 |
| T-P | NFR-2/R-11 — `teammonitor.workspaceDir` 오버라이드 통합 테스트 신규 | Phase 7 Step 7-3 + §2 |
| T-G-clarify | SSE v2 호환성 e2e 판정 기준 — (1) `teamMeta` 부재 구 JS 정상 렌더, (2) `schemaVersion!=1` 치명 예외 미발생 단정 | Phase 7 Step 7-3 + §2 |
| DOC-01 | components/README 갱신 — "Phase 1 결정, commit 7 반영" 문구 명시 (독자 오해 방지) | Phase 1 Step 1-4 |
| TEST-ORDER | commit 별 compile / test 구분 표기 (mvnw -DskipTests vs test) | §6 |

### v1 → v2 변경점 (round 4 ⚠ 차단 1건 + 비차단 17건)

| ID | 내용 | 위치 |
|----|------|------|
| **S4-01 (차단)** | commit 분할 재구성 — `TeamMonitorProperties` 변경 (workspaceDir property) 을 commit 2 로 끌어올림. commit 별 독립 빌드 가능성 보장. | §6 |
| S1-02 | JavaNioWatcher 의 `normStatusDir`/`normWorkspace` 를 `volatile` 인스턴스 필드 승격 | Phase 3 Step 3-2 |
| S2-01 | `TeamStatusReader.scanAndSort()` 반환을 `List.copyOf(fresh)` 로 불변화 | Phase 3 Step 3-1 |
| S2-02 / S6-01 | DTO `TeamMeta` 동명 충돌 해소 — DTO 측을 `SnapshotTeamMeta` 로 리네임 (TeamMetadata.TeamMeta 와 명시 분리) | Phase 4 Step 4-3 |
| S2-03 | TeamMetadata.reload() 정책 단일화 — "파싱 실패 → 직전 정상 스냅샷 유지 + isFallbackActive=true (degraded)" | Phase 2 Step 2-1 |
| S2-04 | emoji() 폴백 우선순위 — JSON `default_emoji` 우선 → 코드 내장 5팀 → 최종 default `📋` | Phase 2 Step 2-1 |
| S2-05 | notify 경로 idempotency 명시 — Set 중복 차단을 OVERFLOW 외 일반 경로에도 적용 가능 (테스트 포함) | Phase 3 Step 3-2 |
| S3-01 | T-H grep 의 `-Pzo` 기능 감지 보강 — `printf '' \| grep -Pzo '.*' >/dev/null 2>&1` 또는 `grep --version \| grep -q 'GNU'` | Phase 6 Step 6-3 |
| S3-02 | T-K-02 분기 키 통일 — `T-K-02a` (익명=401), `T-K-02b` (비ADMIN=403), `T-K-02c` (ADMIN=200+flag) | Phase 7 Step 7-3 + §2 |
| S3-03 / S5-01 | atomic replace (`mv` rename) 케이스 명시 — CREATE/DELETE 쌍으로 자동 커버됨을 코멘트 + 테스트로 고정. T-C-02 신규. | Phase 3 Step 3-4, §2 |
| S4-02 | FE e2e 는 commit 6 시점에서만 추가. commit 5 는 서버 단위 테스트만 (스키마 v2 단언) | §6 |
| S4-03 | Watcher/Reader/Metadata 단위 테스트는 temp 디렉터리 + 수동 인스턴스화 (Spring 컨텍스트 비의존) | Phase 2 Step 2-2, Phase 3 Step 3-4/3-5 |
| S5-02 | T-J-03 신규 — OVERFLOW 직후 메타 reload 순서 e2e 재현 | Phase 7 Step 7-3 + §2 |
| S5-03 | T-B-02 신규 — 폴링 모드 2회 연속 갱신 (>1s 간격) 안정 단정 | Phase 7 Step 7-3 + §2 |
| S6-02 | check-no-n-team-assumptions.sh 에 `--exclude-dir=.git,target,build,node_modules` 추가 | Phase 6 Step 6-3 |
| S6-03 | components/README 에 i18n SSoT 진입점 1줄 메모 (기획서 §자문-디자인 D6 N16 정합) | Phase 1 Step 1-4 (신설) |
| S6-04 | TTL 테스트 — `Clock`/`LongSupplier` 주입 점 추가로 플래키 차단 | Phase 3 Step 3-1, 3-5 |

---

## 0. 사전 조건 / 환경

- 기존 5팀(planner/db/developer/codex/designer) 운영 안정 (직전 스프린트 `designer-team-onboarding` v3 + `harness-hardening-v1` 완료된 master)
- 신규 의존성 0건
- DB 변경 없음
- 본 스프린트는 UI 변경 (정적 카드 → 동적 생성) 포함 → A 정책 디자인팀 자문 완료 (기획서 §자문-디자인 ⭕)
- DB팀 자문 ⭕ skip

### 0-1. 기획서 §자문 추수 의무
- N15 prefers-reduced-motion: 셀렉터 집합 명시 (Phase 5 Step 5-3)
- N6 empty state SR 안내: `role="status"` + `aria-live` (Phase 5 Step 5-1)
- N16 i18n SSoT 진입점 메모: components/README + 본 개발계획 §6-3 (Phase 1 Step 1-4 신설)

---

## 1. 작업 순서 (Phase / Step)

### Phase 1 — 문서 + teams.json 데이터 (FR-4)

#### Step 1-1. `docs/product-specs/team-monitor-wildcard-watcher.md` (이미 v3 작성됨, commit 1 에 포함)

#### Step 1-2. `docs/exec-plans/team-monitor-wildcard-watcher.md` (본 문서, commit 1 에 포함)

#### Step 1-3. `.team-workspace/teams.json` 신규 (FR-4-a, commit 2)
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

#### Step 1-4. `docs/design-docs/components/README.md` 갱신 (S6-03 + N16)
- 기존 README (designer-team-onboarding 스프린트에서 신설) 끝에 1줄 추가:
  ```markdown
  ## i18n SSoT 진입점 (team-monitor-wildcard-watcher, 2026-04-26)
  team-monitor 의 안내 문자열 ("활성 팀 없음" 등) 의 현재 SSoT — `team-monitor.js` `createEmptyPlaceholder()` + template 인라인. 향후 i18n 도입 시 본 위치를 단일 진입점으로 정리.
  ```
- commit 7 (bash 도구) 또는 commit 6 (frontend) 어느 쪽에든 포함 가능. **commit 7 에 포함** — bash 도구 정리와 함께 운영 문서 업데이트.

### Phase 2 — Properties + Metadata (FR-4-c, FR-4-d, S4-01 차단 보완)

#### Step 2-0. `TeamMonitorProperties.java` (S4-01 차단 보완 — commit 2)
- `workspaceDir` property 추가 (NFR-2 + R-11):
  ```java
  private String workspaceDir = ".team-workspace";
  // getter/setter
  public Path getWorkspacePath() { return Path.of(workspaceDir); }
  public Path getStatusPath() { return getWorkspacePath().resolve("status"); }
  public Path getTeamsJsonPath() { return getWorkspacePath().resolve("teams.json"); }
  ```
- `application.properties` 에 `teammonitor.workspaceDir=.team-workspace` 추가 (commit 2)
- 후방 호환: 기존 `getStatusDir()` 메서드 유지 (deprecated 또는 위 `getStatusPath().toString()` 위임). commit 3/4 의 Reader/Watcher 변경 시 신규 getter 사용.

#### Step 2-1. `TeamMetadata.java` 신규 (FR-4-c, commit 2)
- 위치: `src/main/java/com/swmanager/system/service/teammonitor/TeamMetadata.java`
- Spring `@Component`
- 필드:
  ```java
  private final Path teamsJsonPath;
  private final ObjectMapper mapper;
  private final AtomicReference<MetaSnapshot> snapshot = new AtomicReference<>(MetaSnapshot.empty());

  // MetaSnapshot 은 record (모든 필드 final — N12 안전 공개)
  record MetaSnapshot(
      Map<String, TeamMeta> teams,        // immutable (Map.copyOf)
      String defaultEmoji,
      boolean fallbackActive,
      String degradedReason   // S2-03 — 파싱 실패 시 사유, 정상 시 null
  ) {
      static MetaSnapshot empty(String reason) {
          return new MetaSnapshot(Map.of(), null, true, reason);
      }
      static MetaSnapshot empty() { return empty("teams.json 부재"); }
  }
  // S2-06 — @JsonAlias 로 양방향 호환 (sort_order / sortOrder)
  record TeamMeta(
      String emoji,
      @JsonProperty("sort_order") @JsonAlias({"sort_order", "sortOrder"}) int sortOrder,
      String label
  ) {}
  ```
- 메서드 (기획 §FR-4-c):
  - `Optional<TeamMeta> get(String teamName)` — fallback 적용 X (raw)
  - `String emoji(String teamName)` — fallback 우선순위 (S2-04):
    1. `teams.json` 의 해당 팀 emoji
    2. `teams.json` 의 `default_emoji`
    3. 코드 내장 5팀 (CODE_BUILTIN) 의 해당 팀 emoji
    4. 최종 `📋`
  - `String label(String teamName)` — fallback (1) JSON 라벨 → (2) 코드 내장 → (3) 팀명 대문자
  - `int sortOrder(String teamName)` — fallback (1) JSON → (2) 코드 내장 → (3) 99
  - `void reload()` — JSON 재로드 + snapshot.set(...) — **S2-03 정책 단일화**:
    1. teamsJsonPath 부재 → `snapshot.set(empty("부재"))` + `isFallbackActive=true`
    2. 파싱 성공 → `snapshot.set(parsed)` + `isFallbackActive=false`
    3. 파싱 실패 → **직전 정상 스냅샷 유지** + `degradedReason` 갱신 + `isFallbackActive=true` (degraded 진단 플래그). 이전이 empty 상태였으면 empty 유지.
  - `boolean isFallbackActive()` — health/info 노출용

- **코드 내장 5팀 fallback** (FR-4-b — `// ALLOW_FIVE_TEAM_FALLBACK` 센티넬 직전 라인):
  ```java
  // ALLOW_FIVE_TEAM_FALLBACK
  private static final Map<String, TeamMeta> CODE_BUILTIN = Map.of(
      "planner",   new TeamMeta("🧭", 10, "PLANNER"),
      "db",        new TeamMeta("🗄️", 20, "DB"),
      "developer", new TeamMeta("🛠️", 30, "DEVELOPER"),
      "codex",     new TeamMeta("🤖", 40, "CODEX"),
      "designer",  new TeamMeta("🎨", 50, "DESIGNER")
  );
  ```

#### Step 2-2. `TeamMetadataTest.java` 신규 (FR-7-d, S4-03 — Spring 비의존, commit 2)
- 위치: `src/test/java/com/swmanager/system/service/teammonitor/TeamMetadataTest.java`
- 테스트 케이스:
  - `loadsFromJson_appliesSortOrder` — temp 디렉토리 + 수동 인스턴스
  - `loadsFromJson_appliesSortOrderRename` — `sort_order` ↔ `sortOrder` 매핑 검증
  - `missingFile_appliesFallbackDefaults` — 파일 부재 → empty + isFallbackActive=true
  - `missingTeam_returnsDefaultEmojiAndLabel` — 등록되지 않은 팀 (예: tester) → 📋 + TESTER
  - `corruptJson_keepsLastSnapshot_withDegradedFlag` — **S2-03 보완**: 정상 로드 후 corrupt JSON 으로 reload → 직전 스냅샷 유지 + isFallbackActive=true + degradedReason 기록
  - `corruptJson_initial_returnsEmpty_withFallbackFlag` — 첫 reload 부터 corrupt → empty + isFallbackActive=true
  - `emojiFallback_priorityOrder` — **S2-04 보완**: JSON default_emoji 우선 → 코드 내장 → 최종 📋
  - `metadata_safe_publication_concurrent` — 두 스레드 reload + get 경합, 결과 일관성 단정 (R-9, N12)

### Phase 3 — Reader / Watcher 와일드카드 (FR-1, FR-2, FR-3)

#### Step 3-1. `TeamStatusReader.java` (FR-1, commit 3)
- TEAMS 상수 제거 → `listTeams()` 메서드:
  ```java
  private final Path statusDir;
  private final TeamMetadata teamMetadata;
  private final LongSupplier nowSupplier;   // S6-04 — 테스트 주입 가능
  private final AtomicReference<CachedTeams> cache = new AtomicReference<>();
  private static final long CACHE_TTL_MS = 1000L;
  private static final PathMatcher MATCHER =
      FileSystems.getDefault().getPathMatcher("glob:*.status");

  record CachedTeams(List<String> teams, long expiresAtMs) {}  // 불변 (N12, S2-01)

  public TeamStatusReader(TeamMonitorProperties props, TeamMetadata meta) {
      this(props, meta, System::currentTimeMillis);
  }
  // package-private 테스트 전용 생성자 — Clock 주입 (S6-04)
  TeamStatusReader(TeamMonitorProperties props, TeamMetadata meta, LongSupplier now) {
      this.statusDir = props.getStatusPath();    // S4-01 — 신규 getter 사용
      this.teamMetadata = meta;
      this.nowSupplier = now;
  }

  // === S4-01-a 차단 보완 ===
  // commit 3 단독 빌드 시 기존 호출부 (TeamMonitorService / Watcher 들) 호환 위해
  // 1-인자 생성자를 @Deprecated 로 잠시 유지. commit 5 (Service/Watcher listTeams 전환 완료)
  // 시점에 본 생성자 + 잠시 유지 TEAMS 상수 모두 cleanup.
  @Deprecated(forRemoval = true, since = "team-monitor-wildcard-watcher commit 3")
  public TeamStatusReader(TeamMonitorProperties props) {
      this(props, null, System::currentTimeMillis);  // null 메타 — buildComparator() 가 알파벳 폴백
  }

  // === S4-01-a-NPE 차단 보완 ===
  // teamMetadata 가 null 일 때 (1-인자 생성자 사용) sortOrder 호출 NPE 차단
  private Comparator<String> buildComparator() {
      if (teamMetadata == null) {
          return Comparator.naturalOrder();  // 알파벳 폴백
      }
      return Comparator
          .comparingInt((String t) -> teamMetadata.sortOrder(t))
          .thenComparing(Comparator.naturalOrder());
  }

  // === S4-01-b 차단 보완 ===
  // commit 3 시점 잠시 유지되는 5팀 상수 — commit 5 에서 최종 제거.
  // T-H grep 의 ALLOW_FIVE_TEAM_FALLBACK 센티넬 적용 → CI 회귀 차단 예외.
  // ALLOW_FIVE_TEAM_FALLBACK
  @Deprecated(forRemoval = true, since = "team-monitor-wildcard-watcher commit 3")
  public static final List<String> TEAMS = List.of("planner", "db", "developer", "codex", "designer");

  public List<String> listTeams() {
      CachedTeams c = cache.get();
      long now = nowSupplier.getAsLong();
      if (c != null && c.expiresAtMs() > now) return c.teams();
      List<String> fresh = scanAndSort();
      cache.set(new CachedTeams(fresh, now + CACHE_TTL_MS));
      return fresh;
  }

  public void invalidateCache() { cache.set(null); }

  private List<String> scanAndSort() {
      try {
          Files.createDirectories(statusDir);   // C3-01-A — graceful
      } catch (IOException e) {
          log.warn("statusDir 생성 실패: {}", e.getMessage());
          return List.of();
      }
      try (Stream<Path> s = Files.list(statusDir)) {
          List<String> raw = s.filter(p -> {
                  Path fn = p.getFileName();
                  return Files.isRegularFile(p)
                      && !fn.toString().startsWith(".")
                      && MATCHER.matches(fn);   // N9 SSoT — 파일명 기준
              })
              .map(p -> {
                  String n = p.getFileName().toString();
                  return n.substring(0, n.length() - ".status".length());
              })
              .sorted(buildComparator())   // S4-01-a-NPE 보완 — null-safe 분기
              .toList();
          return List.copyOf(raw);   // S2-01 — 불변화
      } catch (IOException e) {
          log.warn("listTeams 스캔 실패: {}", e.getMessage());
          return List.of();
      }
  }
  ```
- `readAll()` 변경: `for (String team : listTeams())` (TEAMS 참조 제거)

#### Step 3-2. `JavaNioWatcher.java` (FR-2 — 두 WatchKey + OVERFLOW, commit 4)
- TEAM_FILES Set 제거 (FR-2-a)
- 두 WatchKey 등록 (FR-2-d, C3-01) + S1-02 (volatile 필드 승격) + Z-01 (정규화):
  ```java
  private final Path workspaceDir;
  private final Path statusDir;
  // S1-02 — volatile 인스턴스 필드 승격 (Z-01 정규화 결과)
  private volatile Path normWorkspaceDir;
  private volatile Path normStatusDir;
  private static final PathMatcher MATCHER = ...;

  @Override
  public synchronized void start() {
      if (alive.get()) return;
      try {
          // C3-01-A: 디렉토리 보장
          Files.createDirectories(statusDir);
          Files.createDirectories(workspaceDir);

          // Z-01: 정규화 + S1-02 volatile 필드에 저장
          this.normStatusDir = statusDir.toAbsolutePath().normalize();
          this.normWorkspaceDir = workspaceDir.toAbsolutePath().normalize();

          ws = FileSystems.getDefault().newWatchService();
          this.statusKey = normStatusDir.register(ws, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
          this.workspaceKey = normWorkspaceDir.register(ws, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
          // ... thread start
      } catch (Exception e) { ... }
  }
  ```
- 이벤트 처리 분기 (FR-2-b/d/e + S2-05 idempotency):
  ```java
  private void loop() {
      Set<String> recentlyNotified = ConcurrentHashMap.newKeySet();   // S2-05 — 키 처리 단위 dedup
      while (alive.get()) {
          WatchKey key = ws.take();
          recentlyNotified.clear();   // S2-05-a — 키 처리 주기마다 초기화 (메모리 누적 방지)
          Path watchedDir = ((Path) key.watchable()).toAbsolutePath().normalize();   // Z-01 비교 정규화
          for (WatchEvent<?> ev : key.pollEvents()) {
              if (ev.kind() == OVERFLOW) { fullRescanFallback(); continue; }
              Path nameP = (Path) ev.context();
              if (nameP == null) continue;
              String fn = nameP.getFileName().toString();

              if (watchedDir.equals(normWorkspaceDir) && fn.equals("teams.json")) {
                  // Z-02 — MODIFY/CREATE/DELETE 모두 reload 경로
                  // S3-03 / S5-01 — atomic replace (mv rename) 도 CREATE+DELETE 쌍으로 자동 커버
                  teamMetadata.reload();
                  reader.invalidateCache();
                  notifyMetadataChange();
              } else if (watchedDir.equals(normStatusDir)
                         && MATCHER.matches(nameP.getFileName())
                         && !fn.startsWith(".")) {
                  String team = fn.substring(0, fn.length() - ".status".length());
                  reader.invalidateCache();
                  if (ev.kind() == ENTRY_DELETE) {
                      notifyTeamDeleted(team);   // C3-01-B — TeamMonitorService 가 snapshot 재발행
                  } else {
                      notifySubscribers(team);
                  }
              }
              // 그 외 — 무시
          }
          if (!key.reset()) break;
      }
  }
  ```
- `fullRescanFallback()` (FR-2-e + N11 순서 + N18 중복 차단):
  ```java
  private void fullRescanFallback() {
      log.warn("WatchService OVERFLOW — 풀스캔 폴백");
      reader.invalidateCache();
      teamMetadata.reload();                    // N11: 메타 먼저
      Set<String> notified = new HashSet<>();   // N18 중복 차단
      for (String team : reader.listTeams()) {
          if (notified.add(team)) notifySubscribers(team);
      }
  }
  ```

- subscribe API 확장 (`TeamStatusWatcher` 인터페이스):
  - 기존: `void subscribe(Consumer<String> onChangedTeam)` 유지
  - 신규: `void subscribeMetaChange(Runnable onMetaChange)` + `void subscribeTeamDeleted(Consumer<String> onDeleted)`

#### Step 3-3. `PollingWatcher.java` (FR-3, commit 4)
- TEAMS 참조 → listTeams() 동적 호출 (FR-3-a):
  - start() 의 초기 mtime 기록도 `for (String team : reader.listTeams())`
  - tick() 의 폴링도 동적
- `teams.json` mtime 추적 (FR-3-b):
  - 신규 필드 `private volatile long teamsJsonLastMtime`
  - 매 tick 마다 mtime 변경 감지 시 `teamMetadata.reload()` + `reader.invalidateCache()` + `notifyMetadataChange()` (Z-02)
- 신규 팀 첫 등장: 기존 로직 유지

#### Step 3-4. `JavaNioWatcherTest.java` 신규 (FR-7-b, S4-03 — Spring 비의존, commit 4)
- temp 디렉터리 + 수동 인스턴스화
- 테스트 케이스:
  - `wildcardWatcher_picksUpNewTeamFile`
  - `wildcardWatcher_ignoresDotPrefixedTempFile`
  - `wildcardWatcher_ignoresNonStatusSuffix`
  - `wildcardWatcher_reloadsTeamsJsonOnModify` (Z-02)
  - `wildcardWatcher_reloadsTeamsJsonOnCreate` (Z-02 / S5-01 atomic replace 부분 — 부재→생성)
  - `wildcardWatcher_reloadsTeamsJsonOnDelete` (Z-02 / S5-01 atomic replace 부분)
  - `wildcardWatcher_atomicReplaceTeamsJson_reloads` (S3-03 / S5-01) — `teams.json.tmp` 작성 + `mv` rename → 즉시 reload 단정. CREATE/DELETE 쌍으로 자동 커버됨을 단언.
  - `wildcardWatcher_handlesOverflowWithFullRescan_noDuplicate` (R-10, N18)
  - `wildcardWatcher_metadataReloadOrderBeforeListTeams` (N11)
  - `wildcardWatcher_normalizedPathComparison` (Z-01 + S1-02)
  - `wildcardWatcher_deleteTriggersSnapshotRefresh` (C3-01-B) — onTeamDeleted 콜백 호출 단정
  - `wildcardWatcher_notifyIdempotency_singleEventNoDuplicate` (S2-05) — 단일 이벤트로 콜백 1회 단정

#### Step 3-5. `TeamStatusReaderTest.java` 갱신 (FR-7-a + S6-04 Clock 주입, commit 3)
- 기존 테스트 갱신:
  - `readAll_returnsAllFiveTeams_evenIfFilesMissing` → `readAll_returnsAllPresent_evenIfFilesMissing`
- 신규:
  - `listTeams_emptyDir_returnsEmpty`
  - `listTeams_returnsTeamsFromStatusFiles` (알파벳)
  - `listTeams_ignoresDotPrefixedTempFiles`
  - `listTeams_appliesSortOrderFromMetadata` — TeamMetadata 모킹
  - `listTeams_cacheTtl_invalidatesAfter1s` — **S6-04**: `LongSupplier` 주입으로 가짜 시간 사용. TTL 만료 단정.
  - `listTeams_cacheInvalidate_immediateRescan` — `invalidateCache()` 호출 후 첫 tick 즉시 재계산
  - `listTeams_handlesWindowsStylePath` — Windows 경로 매칭 (R-13, N14)

#### Step 3-6. `PollingWatcherTest.java` 신규 (C5, S4-03 — Spring 비의존, commit 4)
- temp 디렉터리 + 수동 인스턴스화
- 테스트 케이스:
  - `pollingWatcher_picksUpNewTeamOnFirstTick`
  - `pollingWatcher_reloadsTeamsJsonOnMtimeChange`
  - `pollingWatcher_emptyDir_idlesWithoutError`
  - `pollingWatcher_twoConsecutiveModifies_bothPicked` (S5-03 / T-B-02) — 1초 간격 2회 갱신 모두 인지

### Phase 4 — Service / DTO / SecurityConfig / InfoContributor (commit 5)

#### Step 4-1. `TeamMonitorService.java` (FR-5-c snapshot 확장)
- `sendSnapshot()` 변경:
  ```java
  // 기존: List<String> TEAMS 직접 참조
  // 변경: reader.listTeams() 호출
  List<TeamStatus> teams = new ArrayList<>();
  Map<String, SnapshotTeamMeta> teamMeta = new LinkedHashMap<>();   // S2-02 — DTO 측 명시 분리
  for (String t : reader.listTeams()) {
      TeamStatus s = latestCache.get(t);
      if (s != null) teams.add(s);
      teamMeta.put(t, new SnapshotTeamMeta(
          teamMetadata.emoji(t),
          teamMetadata.sortOrder(t),
          teamMetadata.label(t)
      ));
  }
  TeamStatusEvent.Snapshot snap = new TeamStatusEvent.Snapshot(
      SCHEMA_VERSION_V2, nowIso(), teams, timeline, teamMeta);
  ```
- C3-01-B: watcher 의 `onTeamDeleted` 콜백 등록 → 해당 팀 latestCache.remove + 모든 emitter 에 snapshot 재발행
- watcher 의 `onMetadataChange` 콜백 등록 → 모든 emitter 에 snapshot 재발행 (메타 변경 즉시 반영)
- `SCHEMA_VERSION` 상수 1 → 2 (또는 `SCHEMA_VERSION_V2 = 2` 추가)

#### Step 4-2. `TeamMonitorService` init 시 디렉토리 보장 (C3-01-A)
- @PostConstruct (또는 init 메서드) 에서:
  ```java
  Files.createDirectories(props.getStatusPath());
  Files.createDirectories(props.getWorkspacePath());
  log.info("team-monitor workspaceDir: {}", props.getWorkspacePath().toAbsolutePath());   // R-11 운영 가시성
  teamMetadata.reload();
  ```

#### Step 4-3. `TeamStatusEvent.java` (DTO 확장 + S2-02 리네임)
- `Snapshot` record 에 `teamMeta` 필드 추가:
  ```java
  public record Snapshot(
      int schemaVersion,
      String serverTime,
      List<TeamStatus> teams,
      List<TimelineEntry> timeline,
      Map<String, SnapshotTeamMeta> teamMeta   // S2-02 — DTO 명칭 명시 분리
  ) {}
  // S2-02 — TeamMetadata.TeamMeta 와 명칭 충돌 회피
  // S2-06 — @JsonAlias 로 양방향 호환
  public record SnapshotTeamMeta(
      String emoji,
      @JsonProperty("sort_order") @JsonAlias({"sort_order", "sortOrder"}) int sortOrder,
      String label
  ) {}
  ```

#### Step 4-4. `TeamMonitorInfoContributor.java` (C2 + N13)
- `teamMetadataFallback` 필드 추가:
  ```java
  /**
   * ⚠ 보안: ADMIN 권한 필수.
   * SecurityConfig 의 /actuator/info ROLE_ADMIN 매처와 함께 동작.
   * 비인가 노출 시 내부 fallback/degraded 상태 누설 위험 (R-12 / N13).
   */
  Map<String, Object> info = new LinkedHashMap<>();
  info.put("teamMetadataFallback", teamMetadata.isFallbackActive());
  // 기존 필드 그대로
  ```

#### Step 4-5. `SecurityConfig.java` 또는 등가 (N10/N13 + R-12-A)
- `/actuator/info` ROLE_ADMIN 보호 + 401/403 보장 (SEC-01 보완):
  ```java
  // 옵션 A — 관리 엔드포인트 전용 시큐리티 체인 (권장):
  @Bean
  @Order(1)
  public SecurityFilterChain actuatorChain(HttpSecurity http) throws Exception {
      http.securityMatcher("/actuator/**")
          .authorizeHttpRequests(authz -> authz
              .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
              .requestMatchers("/actuator/info").hasRole("ADMIN")
              .anyRequest().authenticated()
          )
          .httpBasic(withDefaults())   // SEC-01 — 익명 = 401 보장
          .csrf(csrf -> csrf.disable())
          .formLogin(form -> form.disable());   // 폼 로그인 비활성 — 302 리다이렉트 차단
      return http.build();
  }

  // 옵션 B — 기존 단일 체인에 추가 (SecurityConfig 단순한 경우):
  // http.authorizeHttpRequests(...).httpBasic(withDefaults()).formLogin(form -> form.disable())
  // 단, 사이트 전체 폼 로그인 사용 중이라면 옵션 A 권장.
  ```
- **R-12-A 보장**: 익명 접근 → 401 (httpBasic + formLogin disable), 인증된 비ADMIN → 403 (Spring Security 기본 forbidden).

#### Step 4-6. `application.properties` (commit 5 — 스키마 v2 발행 시점에 enable)
- `management.endpoints.web.exposure.include=health,info` 가 이미 있는지 확인. 없으면 추가.
- `teammonitor.workspaceDir=.team-workspace` 는 commit 2 에 이미 추가됨 (Step 2-0).

### Phase 5 — 프런트엔드 (FR-5, commit 6)

#### Step 5-1. `templates/admin/team-monitor.html` (FR-5-a + FR-5-g)
- 정적 5카드 제거 → 빈 컨테이너:
  ```html
  <section id="cards" class="tm-cards" role="region" aria-label="팀 진행율 카드">
      <!-- 카드는 JS 가 snapshot 응답 받고 동적 생성 -->
  </section>
  ```
- timeline / skew-badge 등 다른 요소 변경 없음

#### Step 5-2. `team-monitor.js` (FR-5-b/d/e/f + T-I-02-A)
- dead code 제거: line 6 `const TEAMS = [...]` 삭제 (FR-5-e)
- 신규 함수 `createCard(team, meta)`:
  ```javascript
  function createCard(team, meta) {
      const card = document.createElement('article');
      card.className = 'team-card';
      card.dataset.team = team.team;
      card.id = 'card-' + team.team;
      card.setAttribute('role', 'status');
      card.setAttribute('aria-live', 'polite');
      card.setAttribute('aria-atomic', 'true');     // N2
      card.setAttribute('aria-label', meta?.label || team.team.toUpperCase());

      // 헤더 / 진행바 / 메타 / 태스크 — innerHTML 금지, textContent 만 (NFR-5)
      const header = document.createElement('header');
      header.className = 'card-head';
      const emoji = document.createElement('span');
      emoji.className = 'team-emoji';
      emoji.textContent = meta?.emoji || '📋';
      const labelEl = document.createElement('span');
      labelEl.className = 'team-label';
      labelEl.textContent = meta?.label || team.team.toUpperCase();
      const stateEl = document.createElement('span');
      stateEl.className = 'card-state';
      stateEl.dataset.field = 'state';
      stateEl.textContent = '-';
      header.append(emoji, labelEl, stateEl);
      // ... progress-bar, card-meta, card-task 동일 패턴
      card.append(header /* + 진행바 + 메타 + 태스크 */);
      return card;
  }
  ```
- 신규 함수 `createEmptyPlaceholder()` (FR-5-g):
  ```javascript
  function createEmptyPlaceholder() {
      const div = document.createElement('div');
      div.className = 'tm-empty';
      div.setAttribute('role', 'status');
      div.setAttribute('aria-live', 'polite');
      div.setAttribute('aria-label', '활성 팀 없음');
      const t1 = document.createTextNode('활성 팀이 없습니다. ');
      const code = document.createElement('code');
      code.textContent = 'bash .team-workspace/set-status.sh <팀명> ...';
      const t2 = document.createTextNode(' 로 첫 팀을 추가하세요.');
      div.append(t1, code, t2);
      return div;
  }
  ```
- `onSnapshot()` 변경 (FR-5-b + T-I-02-A 노드 identity):
  ```javascript
  function onSnapshot(e) {
      const data = JSON.parse(e.data);
      // ... serverTime/skew 처리
      const container = document.getElementById('cards');
      container.setAttribute('aria-busy', 'true');
      const teams = Array.isArray(data.teams) ? data.teams : [];
      const meta = data.teamMeta || {};
      renderTeamsAndEmpty(container, teams, meta);
      container.setAttribute('aria-busy', 'false');
      // ... timeline 처리
  }

  function renderTeamsAndEmpty(container, teams, meta) {
      // 1) empty placeholder 정리
      const oldEmpty = container.querySelector('.tm-empty');
      if (oldEmpty && teams.length > 0) oldEmpty.remove();

      // 2) 0팀 → placeholder 1개
      if (teams.length === 0) {
          // 기존 카드 모두 제거
          container.querySelectorAll('.team-card').forEach(n => n.remove());
          if (!container.querySelector('.tm-empty')) {
              container.append(createEmptyPlaceholder());
          }
          return;
      }

      // 3) 신규 / 갱신 / 삭제 — 노드 identity 유지 (T-I-02-A)
      const presentTeams = new Set(teams.map(t => t.team));
      const existing = new Map();
      container.querySelectorAll('.team-card').forEach(card => {
          existing.set(card.dataset.team, card);
      });
      // 삭제 — 응답에 없는 카드 제거
      existing.forEach((card, name) => {
          if (!presentTeams.has(name)) card.remove();
      });
      // 신규 / 재배치 (서버 정렬 순서대로 append)
      const fragment = document.createDocumentFragment();
      teams.forEach(team => {
          let card = existing.get(team.team);
          if (!card) card = createCard(team, meta[team.team] || {});
          fragment.append(card);   // 동일 노드 append → 이동 (identity 유지)
          renderCard(team, false); // state 갱신 (애니 X)
      });
      container.append(fragment);
  }
  ```
- `onUpdate()`: 카드 DOM 미존재 시 `createCard` + append + `renderCard(team, true)` (애니 적용)
- `renderCard()` (기존 로직 보존): 카드 DOM 가져와 state/progress/task/updated 갱신 — `team-monitor.js` 기존 동작과 동일

#### Step 5-3. `team-monitor.css` (FR-5-h + C6 + N15)
- `.tm-empty` 신규:
  ```css
  .tm-empty {
      grid-column: 1 / -1;
      background: var(--tm-card-bg);
      border: 1px dashed var(--tm-border);
      border-radius: 10px;
      padding: 28px 24px;
      color: var(--tm-text-muted);
      text-align: center;
      font-size: 0.9rem;
  }
  .tm-empty code {
      background: var(--tm-bg);
      padding: 2px 6px;
      border-radius: 3px;
      font-family: "JetBrains Mono", "Fira Code", Consolas, monospace;
  }
  ```
- prefers-reduced-motion 가드 (N15 셀렉터 집합):
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
- 기존 그리드 (`repeat(auto-fit, minmax(360px, 1fr))`) 변경 없음

### Phase 6 — bash 도구 (FR-6 + T-H, commit 7)

#### Step 6-1. `set-status.sh` 와일드카드 검증 (FR-6-a)
- 기존 case 검증 제거 → 정규식 + teams.json 권장 경고:
  ```bash
  # 형식 검증 (§4-2 SSoT 정규식)
  if ! [[ "$team" =~ ^[a-z][a-z0-9_-]{0,31}$ ]]; then
    echo "❌ team 형식 오류 — ^[a-z][a-z0-9_-]{0,31}$ 부합 필요"
    exit 1
  fi

  # teams.json 등록 권장 경고 (jq 사용 가능 시)
  TEAMS_JSON="$SCRIPT_DIR/teams.json"
  if [ -f "$TEAMS_JSON" ] && command -v jq >/dev/null 2>&1; then
    if ! jq -e ".teams.\"$team\"" "$TEAMS_JSON" >/dev/null 2>&1; then
      echo "⚠ '$team' 메타데이터 미등록. 카드는 default 이모지 (📋) 로 표시됩니다." >&2
      echo "  $TEAMS_JSON 에 추가를 권장합니다." >&2
    fi
  fi
  ```
- usage 주석 갱신 (정규식 가이드)

#### Step 6-2. `monitor.sh` 디렉토리 스캔 + jq 메타 (FR-6-b/c)
- TEAMS 배열 동적화 (기획서 §FR-6-b 참조)
- 라벨/이모지 lookup 함수 (`team_label "$team"`)
- main loop 동적화 + 0팀 안내 메시지

#### Step 6-3. `check-no-n-team-assumptions.sh` 신규 (T-H + N21 + S3-01 + S6-02)
- 위치: `.team-workspace/check-no-n-team-assumptions.sh`
- 기획서 §6-2 T-H + 본 개발계획 보완:
  ```bash
  #!/usr/bin/env bash
  # T-H: N팀 가정 회귀 차단
  set -Euo pipefail   # N21 TH-03

  HITS=0
  EXCLUDE_DIRS="--exclude-dir=docs --exclude-dir=.git --exclude-dir=target --exclude-dir=build --exclude-dir=node_modules --exclude-dir=.idea"   # S6-02

  # 1) 셸 case 고정 열거
  HITS_SH=$(grep -ErHn $EXCLUDE_DIRS --include='*.sh' \
      'case[^|]*planner.*db.*developer.*codex.*designer' \
      .team-workspace/ src/ 2>/dev/null | grep -v 'ALLOW_FIVE_TEAM_FALLBACK' || true)

  # 2) Java/JS/HTML 5팀 명시 열거 (Java 는 src/main/java 로 한정 — N21 TH-01)
  HITS_CODE_JAVA=$(grep -ErHn $EXCLUDE_DIRS --include='*.java' \
      'planner.*db.*developer.*codex.*designer' \
      src/main/java 2>/dev/null | grep -v 'ALLOW_FIVE_TEAM_FALLBACK' || true)
  HITS_CODE_FRONT=$(grep -ErHn $EXCLUDE_DIRS --include='*.js' --include='*.html' \
      'planner.*db.*developer.*codex.*designer' \
      src/main/resources 2>/dev/null | grep -v 'ALLOW_FIVE_TEAM_FALLBACK' || true)

  # 3) Java size/length == 4 또는 5
  HITS_JAVA=$(grep -ErHn $EXCLUDE_DIRS --include='*.java' \
      '\b(size|length)\s*\(\)\s*==\s*[45]\b' \
      src/main/java 2>/dev/null | grep -v 'ALLOW_FIVE_TEAM_FALLBACK' || true)

  # 4) JS .length === 4 또는 5
  HITS_JS=$(grep -ErHn $EXCLUDE_DIRS --include='*.js' \
      '\.length\s*===?\s*[45]\b' \
      src/main/resources/static 2>/dev/null | grep -v 'ALLOW_FIVE_TEAM_FALLBACK' || true)

  # 5) CSS — 5팀 명시 grid
  HITS_CSS=$(grep -ErHn $EXCLUDE_DIRS --include='*.css' \
      'grid-template-columns:\s*repeat\(\s*5\s*,\s*1fr\)' \
      src/main/resources/static 2>/dev/null | grep -v 'ALLOW_FIVE_TEAM_FALLBACK' || true)

  # 6) 명시 상수
  HITS_CONST=$(grep -ErHn $EXCLUDE_DIRS --include='*.java' --include='*.js' --include='*.sh' \
      '\b(FourTeams|FiveTeams|TEAMS_4|TEAMS_5)\b' \
      src/ .team-workspace/ 2>/dev/null | grep -v 'ALLOW_FIVE_TEAM_FALLBACK' || true)

  # 7) 멀티라인 case (S3-01 + S3-01-a — 기능 감지 보강)
  HITS_SH_ML=""
  # T-H-LOGSKIP — log_skip 헬퍼 정의 (set -Euo pipefail 상태에서 미정의 호출 차단)
  log_skip() { echo "ℹ️  SKIP: $*" >&2; }
  if command -v pcregrep >/dev/null 2>&1; then
      HITS_SH_ML=$(pcregrep -M -rn --include='*.sh' \
          'case[\s\S]*?planner[\s\S]*?db[\s\S]*?developer[\s\S]*?codex[\s\S]*?designer' \
          .team-workspace/ src/ 2>/dev/null | grep -v 'ALLOW_FIVE_TEAM_FALLBACK' || true)
  elif (printf '' | grep -Pzo '.*' >/dev/null 2>&1) || (grep --version 2>/dev/null | grep -q 'GNU'); then
      # S3-01-a: -Pzo 기능 감지 + GNU grep 명시 분기 (busybox 등 비호환 환경 차단)
      HITS_SH_ML=$(grep -Pzo --include='*.sh' \
          'case[\s\S]*?planner[\s\S]*?db[\s\S]*?developer[\s\S]*?codex[\s\S]*?designer' \
          -rn .team-workspace/ src/ 2>/dev/null | tr '\0' '\n' | grep -v 'ALLOW_FIVE_TEAM_FALLBACK' || true)
  else
      log_skip "멀티라인 case 검사 — pcregrep / GNU grep 부재로 SKIP (단일라인 검사만 수행)"
  fi

  # 8) 센티넬 직전 라인 자동 점검 (N21 TH-04 / TH-04-S)
  awk_check_sentinel() {
      local rc=0
      while IFS= read -r f; do
          awk '
            /\/\/ ALLOW_FIVE_TEAM_FALLBACK|# ALLOW_FIVE_TEAM_FALLBACK/ {
              sentinel_line = NR
              getline next_content
              if (next_content !~ /planner|TEAMS|TEAM_FILES|"planner"|new TeamMeta/) {
                  printf "%s:%d: ALLOW_FIVE_TEAM_FALLBACK 다음 라인이 fallback 코드 아님\n", FILENAME, sentinel_line
                  exit 1
              }
            }
          ' "$f" || rc=1
      done < <(grep -lr --include='*.java' --include='*.js' --include='*.sh' \
                  'ALLOW_FIVE_TEAM_FALLBACK' src/ .team-workspace/ 2>/dev/null)
      return $rc
  }

  # 결과 집계
  for h in "$HITS_SH" "$HITS_CODE_JAVA" "$HITS_CODE_FRONT" "$HITS_JAVA" "$HITS_JS" "$HITS_CSS" "$HITS_CONST" "$HITS_SH_ML"; do
      if [ -n "$h" ]; then
          echo "❌ N팀 가정 패턴 발견:"
          echo "$h"
          HITS=1
      fi
  done

  if ! awk_check_sentinel; then
      echo "❌ ALLOW_FIVE_TEAM_FALLBACK 센티넬 규약 위반"
      HITS=1
  fi

  [ $HITS -eq 0 ] || exit 1
  echo "✅ T-H 통과 — N팀 가정 패턴 0건 + 센티넬 규약 준수"
  ```

#### Step 6-4. `docs/design-docs/components/README.md` 갱신 (S6-03 / N16 — Phase 1 Step 1-4 의 실제 변경)
- commit 7 에 포함 (bash 도구 + 운영 문서 동시 commit)

### Phase 7 — 빌드 + 검증 + 라이브 스모크

#### Step 7-1. 빌드
- `./mvnw -DskipTests clean compile` → BUILD SUCCESS
- 컴파일 에러 0건 (특히 `TeamStatusReader.TEAMS` 참조 잔존 시 컴파일 fail 로 회귀 차단)

#### Step 7-2. 단위 테스트
- `./mvnw test`
- 신규 테스트:
  - `TeamMetadataTest` (8 케이스 — S2-03/S2-04/S6-04 보완 포함)
  - `JavaNioWatcherTest` (12 케이스 — Z-01/02 + S2-05 + atomic replace 포함)
  - `PollingWatcherTest` (4 케이스 — T-B-02 포함)
  - `TeamStatusReaderTest` (7 케이스 — S6-04 Clock 포함)

#### Step 7-3. 통합/회귀 테스트 — 기획서 §6-2 매핑 + S5 추가 시나리오

| ID | 시나리오 | 검증 방법 | Phase |
|----|---------|----------|-------|
| T-A | 5팀 그대로 회귀 | 서버 기동 → `/admin/team-monitor` 5카드 정상 | 7-3 |
| T-B-01 | 신규 팀 자동 인식 | `set-status.sh tester ...` → 1초 내 6번째 카드 등장 | 7-3 |
| **T-B-02** | 폴링 모드 2회 연속 갱신 (S5-03) | NIO 비활성 후 PollingWatcher — 1초 간격 modify 2회 모두 인지 | 7-2 (단위) |
| T-C-01 | 메타 핫리로드 (라벨) | teams.json 라벨 변경 → 다음 snapshot 반영 | 7-3 |
| **T-C-02** | atomic replace (S3-03 / S5-01) | `teams.json.tmp` 작성 → `mv` rename → 즉시 메타 reload | 7-2 (단위) |
| T-D | 팀 삭제 자동 인식 | `rm tester.status` → C3-01-B onTeamDeleted → snapshot 재발행 → 카드 제거 | 7-3 |
| T-E | 임시파일 무시 | `.tester.status.XXX` → 무시 | 7-3 |
| T-F | teams.json 손상 → degraded | 잘못된 JSON → 직전 스냅샷 유지 + isFallbackActive=true (S2-03) | 7-2 + 7-3 |
| T-G | SSE schema_version 1 호환 | 구버전 클라이언트 시뮬레이션 | 7-3 |
| T-H | CI 회귀 grep | `bash check-no-n-team-assumptions.sh` → ✅ | 7-5 |
| T-I-01 | 라벨 핫리로드 | (T-C-01 과 동일) | 7-3 |
| **T-I-02** | sort_order 재정렬 (T-I-02-A 노드 identity) | teams.json sort_order 변경 → DOM 순서 재배치 + 카드 노드 identity 유지 (재생성 X) | 7-3 (e2e) |
| T-J-01 | OVERFLOW 풀스캔 | 다량 동시 modify → OVERFLOW → 모두 정상 | 7-2 + 7-3 |
| **T-J-02** | OVERFLOW 중복 차단 | notifySubscribers 호출 횟수 = 팀 수, 중복 0 | 7-2 (단위) |
| **T-J-03** (S5-02) | OVERFLOW + 메타 동시 변경 e2e | 다량 status modify + teams.json 동시 modify → 메타 reload 먼저 → 정렬 정상 | 7-3 |
| **T-K-02a** (S3-02) | /actuator/info 익명 = 401 | curl 익명 | 7-3 |
| **T-K-02b** (S3-02) | /actuator/info 비ADMIN = 403 | curl USER 권한 | 7-3 |
| **T-K-02c** (S3-02) | /actuator/info ADMIN = 200 + flag | curl ADMIN → `teamMetadataFallback` 노출 | 7-3 |
| T-L-01 | bash 0팀 / jq 미설치 | status 비우고 monitor.sh → "활성 팀 없음" + idle | 7-4 |
| **T-L-02** | UI 0팀 placeholder | 빈 snapshot → `.tm-empty` 정확히 1개 | 7-3 (e2e) |
| **T-P (R-11) — T-P-strong** | workspaceDir override | `teammonitor.workspaceDir=/tmp/custom-ws` 시: (1) status/teams.json 경로가 `/tmp/custom-ws/status` / `/tmp/custom-ws/teams.json` 으로 반영. (2) status/메타 파일이 정상 인지. **(3) 부정 단정** — 기본 경로 `.team-workspace/status/` 에 status 파일 두어도 카드 미등장. **(4) 로그 단정** — 시작 로그에 `team-monitor workspaceDir: /tmp/custom-ws` 출력 확인. | 7-3 |
| **T-G-clarify-strong (T-G-harness)** | SSE v2 호환성 e2e | **실행 방법 (manual — Puppeteer 자동화는 본 스프린트 비범위, 별도 후속 권고)**:<br>(1) 브라우저 DevTools 콘솔 열기 → `/admin/team-monitor` 접속 → 5팀 카드 렌더 확인 + Console 탭 error 0건 단정<br>(2) DevTools 콘솔에서 `window.addEventListener('error', e => console.warn('caught:', e))` 후 SSE 재수신 (페이지 reload) — `caught:` 로그 0건 단정<br>(3) Network 탭 → `/admin/team-monitor/stream` SSE 응답 → `schemaVersion: 2`, `teamMeta` 객체 확인<br>(4) `teamMeta` 미존재 시뮬레이션 (서버 측 임시 reset 또는 corrupt teams.json) → 카드 라벨 = 대문자 팀명, 이모지 = `📋` 단정<br>| 7-3 (manual) |
| **T-K-02-curl (SEC-01-curl)** | /actuator/info HTTP 헤더 | 검증 명령 (구체화):<br>- 익명: `curl -sS -i http://localhost:9090/actuator/info \| head -1` → `HTTP/1.1 401`<br>- 익명 헤더: `curl -sS -i http://localhost:9090/actuator/info \| grep -i '^WWW-Authenticate: Basic'` → 매칭<br>- USER: `curl -sS -i -u user:pw http://localhost:9090/actuator/info \| head -1` → `HTTP/1.1 403`<br>- ADMIN: `curl -sS -u admin:pw http://localhost:9090/actuator/info \| jq '.teamMetadataFallback'` → `false` (정상) 또는 `true` (degraded) | 7-3 |

#### Step 7-4. 서버 재기동 + 라이브 스모크

> 📌 **ORDER-FLIP-NOTE**: commit 3 시점 (TeamStatusReader 와일드카드 + 1-인자 생성자 fallback) 까지 일시적으로 정렬이 알파벳순 (codex/db/designer/developer/planner) 으로 표시될 수 있음. commit 5 (TeamMetadata 주입 wiring 완료) 후 정식 sort_order (planner/db/developer/codex/designer) 복원. 본 과도기는 의도된 동작 — 실서비스 영향은 commit 5 직후 사라짐.
- `bash server-restart.sh` → `Started SwManagerApplication` + 에러 0건
- `bash .team-workspace/set-status.sh planner 진행중 50 "smoke"`
- 브라우저 `/admin/team-monitor` → 5팀 동적 카드 + 진행바 50% 노랑
- `bash .team-workspace/monitor.sh` → 5팀 ANSI 정상
- `bash .team-workspace/set-status.sh tester 진행중 30 "신규팀 스모크"` (teams.json 미등록) → 6번째 카드 default 이모지 (📋) + 경고 메시지 stderr
- teams.json 에 tester 엔트리 추가 → 다음 snapshot 갱신
- `rm .team-workspace/status/tester.status` → 카드 즉시 제거 (C3-01-B 검증)
- `/actuator/info` 익명 → 401, ADMIN → 200 + `teamMetadataFallback: false`

#### Step 7-5. T-H grep 1회 수동 실행
- `bash .team-workspace/check-no-n-team-assumptions.sh` → ✅

---

## 2. 테스트 (T#) — 기획서 §6-2 매핑 + S5 추가

| ID | 내용 | Phase / Step |
|----|------|--------------|
| T-A | 5팀 회귀 | 7-3, 7-4 |
| T-B-01 | 신규 팀 자동 인식 | 7-3, 7-4 |
| T-B-02 | 폴링 2회 연속 갱신 (S5-03) | 7-2 |
| T-C-01 | 라벨 핫리로드 | 7-3 |
| T-C-02 | atomic replace (S3-03/S5-01) | 7-2 |
| T-D | 팀 삭제 자동 인식 | 7-3 |
| T-E | 임시파일 무시 | 7-3 |
| T-F | degraded fallback | 7-2, 7-3 |
| T-G | SSE 스키마 호환 | 7-3 |
| T-H | CI 회귀 grep | 7-5 |
| T-I-01 / T-I-02 | 메타 핫리로드 + sort 재정렬 (노드 identity) | 7-3 |
| T-J-01/02/03 | OVERFLOW 풀스캔 + 중복 차단 + 메타 동시 변경 | 7-2, 7-3 |
| T-K-02a/b/c | info 401/403/200+flag (S3-02 키 통일) | 7-3 |
| T-L-01/02 | bash 0팀 + UI placeholder | 7-3, 7-4 |
| T-U (단위) | 단위 테스트 합산 | 7-2 |

---

## 3. 롤백 전략 — 기획서 §R-8 매핑

단일 PR 머지이므로 `git revert <merge-sha>` 1회로 전체 원복.

| 상황 | 핫픽스 | 정식 롤백 |
|------|--------|-----------|
| listTeams 깨짐 | (없음 — 즉시 revert) | 본 스프린트 commit revert |
| teams.json 손상 | git checkout teams.json | (불필요 — degraded 자동) |
| 프런트 카드 미렌더 | 브라우저 새로고침 | template + JS commit revert |
| SSE 스키마 v2 미수신 | (자동 fallback) | (불필요) |
| watcher 신규 팀 미인식 | server-restart.sh | watcher commit revert |
| OVERFLOW 풀스캔 누락 | server-restart.sh | JavaNioWatcher OVERFLOW 분기 점검 |
| /actuator/info 노출 | Spring Security 1줄 추가 | SecurityConfig commit revert |

---

## 4. 리스크·완화 재확인 (기획 §5 매핑)

| ID | 영역 | 우선순위 | 구현 단계 |
|----|------|----------|-----------|
| R-1 | 성능 | P1 | Phase 3 Step 3-1 (TTL 1초 캐시) |
| R-2 | 안정성 | **P0** | Phase 2 Step 2-1 (degraded fallback + isFallbackActive) |
| R-3 | 회귀 | P1 | Phase 3 Step 3-1 (PathMatcher + dot-prefix) |
| R-4 | 회귀 | P2 | Phase 5 Step 5-2 (TEAMS dead code 제거 + 단위) |
| R-5 | UI | P1 | Phase 5 Step 5-2 (DocumentFragment + 노드 identity, T-I-02-A) |
| R-6 | 운영 | P2 | Phase 1 Step 1-3 (sort_order 기본값) |
| R-7 | UI | P2 | (별도) |
| R-9 | 안정성 | P1 | Phase 2/3 (AtomicReference + 불변, S2-01) |
| R-10 | 안정성 | P1 | Phase 3 Step 3-2 (OVERFLOW + 중복 차단) |
| R-11 | 운영 | P1 | Phase 2 Step 2-0 (workspaceDir property) |
| R-12 | 보안 | **P0** | Phase 4 Step 4-5 (Spring Security ADMIN, 401/403 분리) |
| R-13 | 회귀 | P1 | Phase 3 Step 3-1 (PathMatcher 파일명 SSoT + Windows 테스트) |

---

## 5. 영향 파일 (확정)

### 신규 (8개)
- `docs/product-specs/team-monitor-wildcard-watcher.md` (commit 1)
- `docs/exec-plans/team-monitor-wildcard-watcher.md` (commit 1)
- `.team-workspace/teams.json` (commit 2)
- `src/main/java/com/swmanager/system/service/teammonitor/TeamMetadata.java` (commit 2)
- `src/test/java/com/swmanager/system/service/teammonitor/TeamMetadataTest.java` (commit 2)
- `src/test/java/com/swmanager/system/service/teammonitor/JavaNioWatcherTest.java` (commit 4)
- `src/test/java/com/swmanager/system/service/teammonitor/PollingWatcherTest.java` (commit 4)
- `.team-workspace/check-no-n-team-assumptions.sh` (commit 7)

### 수정 (15개)
- `src/main/java/com/swmanager/system/config/TeamMonitorProperties.java` (commit 2 — S4-01 차단 보완)
- `src/main/resources/application.properties` (commit 2 — workspaceDir 키)
- `src/main/java/com/swmanager/system/service/teammonitor/TeamStatusReader.java` (commit 3)
- `src/test/java/com/swmanager/system/service/teammonitor/TeamStatusReaderTest.java` (commit 3)
- `src/main/java/com/swmanager/system/service/teammonitor/JavaNioWatcher.java` (commit 4)
- `src/main/java/com/swmanager/system/service/teammonitor/PollingWatcher.java` (commit 4)
- `src/main/java/com/swmanager/system/service/teammonitor/TeamStatusWatcher.java` (commit 4 — 인터페이스 확장: subscribeMetaChange + subscribeTeamDeleted)
- `src/main/java/com/swmanager/system/service/teammonitor/TeamMonitorService.java` (commit 5)
- `src/main/java/com/swmanager/system/dto/TeamStatusEvent.java` (commit 5 — S2-02 SnapshotTeamMeta)
- `src/main/java/com/swmanager/system/health/TeamMonitorInfoContributor.java` (commit 5)
- `src/main/java/com/swmanager/system/config/SecurityConfig.java` 또는 등가 (commit 5)
- `src/main/resources/templates/admin/team-monitor.html` (commit 6)
- `src/main/resources/static/js/team-monitor.js` (commit 6)
- `src/main/resources/static/css/team-monitor.css` (commit 6)
- `.team-workspace/monitor.sh`, `.team-workspace/set-status.sh` (commit 7)
- `docs/design-docs/components/README.md` (commit 7 — S6-03 i18n 메모)

### 변경 없음
- DB 스키마
- AGENTS.md / CLAUDE.md
- TeamMonitorController/Advice/SecurityHeadersFilter
- TeamMonitorHealthIndicator (info 만 부담)
- DTO (TeamStatus / TimelineEntry / UlidGenerator)

---

## 6. 단일 PR 구성 / commit 분할 (8 commits — S4-01 차단 + S4-01-a/b 보완 재구성)

본 스프린트는 단일 PR 머지. commit 8개로 분할 — 각 commit 후 **독립 빌드 가능** + 각 영역 독립 revert 가능.

### 6-1. commit 별 검증 명령 (TEST-ORDER + TEST-ORDER-clarify 보완)

| commit | 빌드 명령 | 테스트 명령 | T-H grep |
|--------|----------|-------------|----------|
| 1 (docs) | N/A | N/A | N/A |
| 2 (Properties + TeamMetadata) | `./mvnw -DskipTests clean compile` | `./mvnw -q -Dtest=TeamMetadataTest test` | (skip — 스크립트 미존재) |
| 3 (TeamStatusReader) | `./mvnw -DskipTests clean compile` | `./mvnw -q -Dtest=TeamStatusReaderTest test` | (skip — 스크립트 미존재) |
| 4 (Watcher 들 — 인터페이스 + 구현체 동시 수정 in same commit) | `./mvnw -DskipTests clean compile` | `./mvnw -q -Dtest=JavaNioWatcherTest,PollingWatcherTest test` | (skip) |
| 5 (Service/DTO/Security/Info — TEAMS 상수 최종 제거) | `./mvnw -DskipTests clean compile` | `./mvnw -q test` (전체) | **mini grep 3종 (S4-01-b-mini)** ↓ |
| 6 (frontend — Java 영향 없음) | `./mvnw -DskipTests clean compile` | manual 브라우저 스모크 — `bash server-restart.sh && curl -fsSL http://localhost:9090/admin/team-monitor > /dev/null` | **mini grep 3종 (S4-01-b-mini)** ↓ |
| 7 (bash 도구 + check 스크립트 신규) | N/A | (bash 스모크) | **첫 실행** `bash .team-workspace/check-no-n-team-assumptions.sh` ✅ |

- **S4-01-b-TIMING 차단 보완 (옵션 B)**: T-H 스크립트 (`check-no-n-team-assumptions.sh`) 가 commit 7 에서 신규 추가 → **T-H 첫 실행 = commit 7** 로 확정.
- **S4-01-b-mini 보완** (round 7 권고): commit 5/6 구간 (TEAMS 최종 제거 ~ 프런트 변경) T-H 공백 차단 — 본 구간 commit 후 다음 mini grep 3종 one-liner 1회 수동 실행:
  ```bash
  # mini T-H — 핵심 grep 3종 (commit 5/6 임시)
  ! grep -ErHn --include='*.java' '\b(size|length)\s*\(\)\s*==\s*[45]\b' src/main/java | grep -v 'ALLOW_FIVE_TEAM_FALLBACK'
  ! grep -ErHn --include='*.js' '\.length\s*===?\s*[45]\b' src/main/resources/static | grep -v 'ALLOW_FIVE_TEAM_FALLBACK'
  ! grep -ErHn --include='*.css' 'grid-template-columns:\s*repeat\(\s*5\s*,\s*1fr\)' src/main/resources/static | grep -v 'ALLOW_FIVE_TEAM_FALLBACK'
  ```
- commit 4 의 인터페이스 확장 (`TeamStatusWatcher` 신규 메서드 + JavaNioWatcher/PollingWatcher 구현체 변경) 은 **동일 commit 내 동시 수정** — 단계별 컴파일 단절 없음.

### 6-2. commit 본문

1. **`docs(team-monitor-wildcard-watcher): 기획서 v3 + 개발계획 v2`**
   - `docs/product-specs/team-monitor-wildcard-watcher.md`
   - `docs/exec-plans/team-monitor-wildcard-watcher.md`
   - 빌드 가능성: docs 만 — N/A (코드 변경 없음)

2. **`feat(team-monitor-wildcard-watcher): teams.json 메타 + Properties + TeamMetadata`**
   - `.team-workspace/teams.json` (신규)
   - `src/main/java/com/swmanager/system/config/TeamMonitorProperties.java` (workspaceDir property — **S4-01 차단 보완**)
   - `src/main/resources/application.properties` (1줄 추가)
   - `src/main/java/com/swmanager/system/service/teammonitor/TeamMetadata.java` (신규)
   - `src/test/java/com/swmanager/system/service/teammonitor/TeamMetadataTest.java` (신규)
   - 빌드 가능성: TeamMetadata 가 신규 컴포넌트로 추가, 기존 코드 영향 0건. Properties 신규 메서드는 기존 호출자 무영향. ✅
   - 단위 테스트: TeamMetadataTest 8 케이스 PASS

3. **`feat(team-monitor-wildcard-watcher): TeamStatusReader 와일드카드 + 캐시`**
   - `TeamStatusReader.java` (TEAMS 상수 → listTeams + 캐시)
   - `TeamStatusReaderTest.java` (갱신)
   - 빌드 가능성: TEAMS 상수 제거됨. 호출자 (PollingWatcher / TeamMonitorService) 는 본 commit 단계에선 **컴파일 fail** 우려 — 보완책:
     - **(보완)** TEAMS 를 `@Deprecated` 로 잠시 유지하면서 `listTeams()` 추가 → commit 4 (Watcher) / commit 5 (Service) 에서 listTeams 사용 전환 → 본 스프린트 종료 시점에 TEAMS 상수 최종 제거. 또는,
     - **(권장)** commit 3 에서 TEAMS 상수 + listTeams 양쪽 보유 (TEAMS = 코드 내장 5팀 — `// ALLOW_FIVE_TEAM_FALLBACK` 센티넬). commit 4/5 에서 listTeams 사용으로 전환. commit 7 직전 또는 별도 commit 에서 TEAMS 최종 제거.
     - 본 개발계획에서는 **권장안 채택**: commit 3 에 listTeams 추가 + TEAMS 잠시 유지 (deprecated 주석). commit 5 에서 모든 사용처 전환 후 TEAMS 제거.

4. **`feat(team-monitor-wildcard-watcher): Watcher 두 WatchKey + OVERFLOW + 메타 reload`**
   - `JavaNioWatcher.java` (TEAM_FILES 제거 + 두 WatchKey + OVERFLOW + S1-02 volatile)
   - `PollingWatcher.java` (listTeams 호출 + teams.json mtime + Z-02)
   - `TeamStatusWatcher.java` (인터페이스 확장)
   - `JavaNioWatcherTest.java` + `PollingWatcherTest.java` (신규)
   - 빌드 가능성: 인터페이스 확장은 default 메서드 또는 `subscribeMetaChange/subscribeTeamDeleted` 추가 시 기존 구현체 (Watcher 들) 모두 본 commit 에서 동시 수정 → 컴파일 ✅. TeamMonitorService 는 listTeams 호출 안 해도 무관 (commit 5 까지 TEAMS 잠시 유지).

5. **`feat(team-monitor-wildcard-watcher): Service/DTO/Security/Info — 스키마 v2 발행`**
   - `TeamMonitorService.java` (listTeams 사용 + onMetaChange/onTeamDeleted 콜백)
   - `TeamStatusEvent.java` (Snapshot + SnapshotTeamMeta — S2-02)
   - `TeamMonitorInfoContributor.java` (teamMetadataFallback)
   - `SecurityConfig.java` 또는 등가 (R-12-A 401/403)
   - `TeamStatusReader.java` (TEAMS 상수 최종 제거 — 본 commit 에서 모든 사용처 전환 후 cleanup)
   - 빌드 가능성: ✅ — 본 commit 까지 누적 변경 후 `./mvnw compile` PASS
   - 단위 테스트 + S4-02: 본 commit 의 단위 테스트는 v2 스키마만 단언 (FE 변경 X)

6. **`feat(team-monitor-wildcard-watcher): frontend 동적 카드 + .tm-empty + reduced-motion`**
   - `team-monitor.html` (정적 카드 5개 → 빈 컨테이너)
   - `team-monitor.js` (createCard + createEmptyPlaceholder + dead code 제거 + T-I-02-A)
   - `team-monitor.css` (.tm-empty + prefers-reduced-motion)
   - 빌드 가능성: ✅ (백엔드 v2 스키마와 정합)
   - S4-02: FE e2e 테스트 (T-I-02 / T-L-02 등) 본 commit 시점에 라이브 스모크

7. **`feat(team-monitor-wildcard-watcher): bash 도구 동적화 + T-H 회귀 차단 + i18n 메모`**
   - `set-status.sh` (와일드카드 검증)
   - `monitor.sh` (디렉토리 스캔 + jq 메타)
   - `check-no-n-team-assumptions.sh` (신규 — S4-01-b-TIMING: 본 commit 에서 처음 도입)
   - `docs/design-docs/components/README.md` (i18n 메모 1줄, S6-03)
   - **DOC-01-comment**: 본 commit 의 docs 변경 (`components/README.md`) 은 의사결정 시점은 Phase 1 (사전 결정), 실제 운영 문서 동기화는 본 단계. 본 분리는 운영 문서 + bash 도구의 commit 그룹 일치를 위함.
   - 빌드 가능성: shell only — N/A
   - **T-H 첫 실행** — `bash .team-workspace/check-no-n-team-assumptions.sh` ✅ 통과 단정. 본 commit 까지 누적된 모든 코드 (commit 5 의 TEAMS 최종 제거 포함) 에 N팀 가정 잔존 0건 검증.

8. (필요 시) **`fix(team-monitor-wildcard-watcher): codex 검증 단계 후속 수정`** — 없으면 생략

각 commit 메시지 끝에 `Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>`.

---

## 7. 승인 요청

본 개발계획서 v5 에 대한 codex 재검토 및 사용자 최종승인을 요청합니다.

승인 후 진행 순서:
1. Phase 1 → 2 → 3 → 4 → 5 → 6 → 7 순차 구현
2. 각 commit 후 `./mvnw compile` 통과 확인 (S4-01 보완 효과 검증)
3. Phase 7 (빌드 + 라이브 스모크) 완료 후 codex 검증 (구현물 기준)
4. codex ⭕ + 사용자 "작업완료" 발화 → 자동 commit+push (AGENTS.md §5)
