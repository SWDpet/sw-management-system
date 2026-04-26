---
tags: [dev-plan, sprint, monitoring, sse]
sprint: "team-monitor-dashboard"
status: draft-v4
created: "2026-04-26"
revised: "2026-04-26"
---

# [개발계획서] 가상 팀 진행율 모니터링 — 웹 대시보드 (옵션 C) — v4

- **작성팀**: 개발팀
- **작성일**: 2026-04-26 (v1~v4 모두 동일자)
- **근거 기획서**: [[../product-specs/team-monitor-dashboard|기획서 v2]] (codex ⭕ 통과 + 사용자 최종승인)
- **상태**: 초안 v4 (codex ⭕ 재검토 통과 + 비차단 권고 2건 즉시 명시 완료) — v1 보완 5건 + v2 보완 S1~S7 + T18 부하 현실화 + v3 보완 E1~E4 + v4 비차단 권고 2건 모두 반영
- **목표**: 기획서 §FR-1~6, §NFR-1~4, §R-1~7, §4-1~10 전부 구현. 단일 PR로 머지 가능한 규모.

### v2 → v3 변경점 (codex v2 검토 보완 7건 + α)

| # | 항목 | 위치 |
|---|------|------|
| **S1** | `TeamStatus.updatedAt` null 허용 + 프론트 `formatRelative` null→"미기록" 표기 (기획 R-2-c 정합) | Step 1-3, 1-4, 6-2 |
| **S2** | `TeamMonitorInfoContributor` 신규 추가 — `/actuator/info` 에 `teammonitor.timeline.size/sse.max-emitters/watcher.mode` 노출 (기획 NFR-4-a 충족) | Step 5-4 + §5 신규 파일 |
| **S3** | 세션 리스너 등록 정정 — `ServletListenerRegistrationBean<TeamMonitorSessionListener>` 등록 (이전 "WebMvcConfigurer 등록"은 부정확) | Step 3-6 + §5 신규 파일 |
| **S4** | Controller `produces="text/event-stream"` 만 (charset 은 `Content-Type` 헤더에 별도 set) | Step 4-1 |
| **S5** | T20 강화 — `ServerProperties.getCompression().getMimeTypes()` 파싱 단정 + 응답 헤더 단정 2중 방어 | §2 T20 |
| **S6** | T7b 신규 — `/admin/team-monitor/stream` 헤더 검증 | §2 T7b |
| **S7** | `@AuthenticationPrincipal Principal` 조합 → `Principal principal` 단독 (어노테이션 제거) | Step 4-1 |
| **T18 현실화** | 1000 SSE 부하 → **100건 + 모킹 기반** 으로 축소 (CI 친화) | §2 T18 |

### v3 → v4 변경점 (codex v3 검토 보완 4건)

| # | 항목 | 위치 |
|---|------|------|
| **E1** | `@EnableScheduling` 활성 명시 — heartbeat (`@Scheduled`) + watcher 재시작 동작 보장 | Step 0 + Step 3-5 |
| **E2** | 보안 헤더 필터 `setOrder()` 정정 — `Ordered.HIGHEST_PRECEDENCE+10` (보안체인 앞 위험) → `SecurityProperties.DEFAULT_FILTER_ORDER + 1` (보안체인 직후 보장) | Step 4-3 |
| **E3** | 문서 표기 정합성 — v2→v3 변경표의 S2 "Step 5-3" 오타 → "Step 5-4", T14 설명을 `session.invalidate()` 시나리오로 통일 | 본 표 + §2 T14 |
| **E4** | `@EnableConfigurationProperties(TeamMonitorProperties.class)` 명시 — Boot 자동 바인딩 보장 (현재 프로젝트에 `@ConfigurationPropertiesScan` 부재 확인) | Step 0 + Step 4-3 |

### v4 비차단 권고 (codex v4 ⭕ 후 즉시 명시)

| # | 항목 | 결정 |
|---|------|------|
| **N1** | R-6-c 폴링 fallback (P2) | **본 스프린트 비범위** — 차기 스프린트 (`team-monitor-resilience-v2`) 로 이관. 본 v4에서는 SSE 재연결(backoff) 만 구현 (Step 6-2). 별도 fallback REST 엔드포인트 미도입. |
| **N2** | JetBrains Mono 번들링 | **본 스프린트 미번들** — `font-family` 의 fallback chain (`JetBrains Mono → Fira Code → Consolas → monospace`) 만 사용. CSP `font-src 'self' data:` 기본값 그대로 통과. 추후 디자인팀이 woff2 번들 결정 시 별도 PR. |

---

## 0. 사전 조건 / 환경

- 기존 `cb4e617` 터미널판 `monitor.sh`/`set-status.sh` 동작 보존 (회귀 금지)
- 신규 의존성 1건만 추가: `spring-boot-starter-actuator` (`pom.xml`) — 헬스 indicator 용
- 패키지 명명 규칙 (기존 코드베이스 따름):
  - 컨트롤러: `com.swmanager.system.controller.TeamMonitorController`
  - 서비스: `com.swmanager.system.service.teammonitor.*`
  - DTO: `com.swmanager.system.dto.*`
  - 보안 필터: `com.swmanager.system.config.TeamMonitorSecurityHeadersFilter` (기존 `security/` 패키지 부재 → 일관성을 위해 `config/` 에 둠)
- DB 변경 없음 (기획 §4-5)

### 0-1. 메인 애플리케이션 어노테이션 추가 (codex v3 보완 E1 + E4)

`src/main/java/com/swmanager/system/SwManagerApplication.java` 에 다음 2개 어노테이션 추가:

```java
@SpringBootApplication
@EnableScheduling                                            // E1: heartbeat (@Scheduled) + watcher 재시작 주기 동작 보장
@EnableConfigurationProperties(TeamMonitorProperties.class)  // E4: TeamMonitorProperties 자동 바인딩 (프로젝트에 @ConfigurationPropertiesScan 부재 확인)
public class SwManagerApplication { ... }
```

- **확인 결과** (Grep): 현재 프로젝트는 `@EnableScheduling` 도, `@ConfigurationPropertiesScan` 도 부재. 위 2줄 추가 필수.
- **회귀 영향**: `@EnableScheduling` 활성화로 다른 `@Scheduled` 빈이 추가 트리거되는 부수효과 없음 (현재 코드베이스에 `@Scheduled` 사용처 0건 — 본 스프린트가 도입 1번째).
- **commit**: Phase 1 의 commit 1 (chore: add actuator dependency + properties) 에 포함.

---

## 1. 작업 순서 (Phase / Step)

### Phase 1 — 의존성 + 데이터 계층

#### Step 1-1. `pom.xml` actuator 의존성 + gzip 제외 설정 추가
1. `spring-boot-starter-actuator` 추가 (단독 라인). 버전은 Spring Boot BOM 위임.
2. `./mvnw -q dependency:resolve` → 의존성 해결 성공.
3. `application.properties` 에 다음 추가:
   ```properties
   # === Actuator ===
   management.endpoints.web.exposure.include=health,info
   management.endpoint.health.show-details=when_authorized

   # === Server compression — SSE 보호 (기획 §4-7) ===
   # 기본값 false 이지만 명시적 표기로 회귀 차단.
   # 향후 누군가 true 로 켜더라도 mime-types 에서 text/event-stream 은 절대 포함 금지.
   server.compression.enabled=false
   server.compression.mime-types=text/html,text/xml,text/plain,text/css,application/javascript,application/json
   # WARNING: 위 mime-types 에 text/event-stream 추가 금지.
   # 추가 시 SSE 가 압축되어 EventSource 가 first event 까지 버퍼링 → 실시간성 깨짐.
   ```
4. **프록시(nginx) 가이드 주석** — `application.properties` 머리 또는 `docs/RELIABILITY.md` (있으면) 에 다음 가이드 1블록 추가:
   ```
   # nginx reverse proxy 사용 시 /admin/team-monitor/stream 위치 블록에 다음 적용:
   #   gzip off;
   #   proxy_buffering off;
   #   proxy_cache off;
   #   proxy_read_timeout 3600s;
   #   chunked_transfer_encoding on;
   ```

#### Step 1-2. `set-status.sh` POSIX-호환 atomic write 적용 (기획 §4-10)
1. 기존 51~57 줄 heredoc → mktemp + printf + mv 패턴 교체.
2. 머리주석에 §4-10 권고 주석 그대로 포함 (mktemp 같은-디렉토리, mv rename, msys 동작 노트).
3. 회귀 검증: `bash .team-workspace/set-status.sh planner 진행중 50 "smoke"` → 기존 동일 출력 + status 파일 valid.
4. 동시 호출 검증: `seq 1 50 | xargs -I{} -P 10 bash .team-workspace/set-status.sh planner 진행중 {} "concurrent {}"` 실행 후 `cat planner.status` parseable + `team=`, `state=`, `progress=`, `task=`, `updated=` 5필드 모두 존재.

#### Step 1-3. DTO 3종 신설
1. `TeamStatus.java` — `{team, state, progress, task, updatedAt(ISO-8601 String, **nullable**)}`.
   - **codex v2 보완 S1**: `updatedAt` 은 **null 허용** record 필드. 누락 시 fallback 으로 now() 대체 금지 → R-2-c 의 "갱신: 미기록" UX 보존. Jackson 직렬화는 `@JsonInclude(JsonInclude.Include.ALWAYS)` 로 null 도 명시 전달 (프런트가 null 체크 가능하도록).
2. `TimelineEntry.java` — `{id(ULID), team, prevState, newState, prevProgress, newProgress, task, occurredAt(ISO-8601 String)}`.
   - `occurredAt` 은 **non-null** (서버 측에서 변경 감지 시점에 항상 생성). null 허용 안 함.
3. `TeamStatusEvent.java` — sealed-style sum (or 단순 record 2종):
   - `Snapshot {schemaVersion, serverTime, List<TeamStatus> teams, List<TimelineEntry> timeline}`
   - `Update {schemaVersion, serverTime, TeamStatus team, TimelineEntry timelineEntry}`
4. 모든 시각 필드는 String 타입의 `OffsetDateTime.toString()` (Asia/Seoul +09:00 기본). epoch 미포함. (`updatedAt` 만 null 허용)
5. ULID 생성: 외부 라이브러리 미도입 → `java.security.SecureRandom` + `Instant.now().toEpochMilli()` 기반 26자 자체 구현 (`com.swmanager.system.dto.support.UlidGenerator`). 또는 `java.util.UUID` 로 대체 가능 — 기획 FR-5-d 의 dedupe 목적 달성하면 OK. **결정: UUID v7 형식이 부재하므로 자체 ULID 생성기 채택** (코드 ~30줄 내).

#### Step 1-4. `TeamStatusReader` 구현
1. `@Component` Bean. 생성자 주입으로 `teammonitor.status-dir` 받음 (`@Value`).
2. `readAll()` — 4개 파일 (`planner|db|developer|codex.status`) 동기 읽고 `Map<String, TeamStatus>` 반환.
3. `readOne(String team)` — 단일 파일 read. `IOException` → 상위로 전파 (Service 가 fallback 처리).
4. 파싱: `Files.readAllLines(...)` → `key=value` split.
   - **codex v2 보완 S1**: `updated` 키가 없거나 파싱 불가(epoch 정수가 아님)면 `TeamStatus.updatedAt = null` 으로 set (now() 대체 금지). R-2-c "갱신: 미기록" UX 보존.
   - `updated` 키가 정상 epoch seconds 면 `OffsetDateTime.ofInstant(Instant.ofEpochSecond(v), ZoneId.of("Asia/Seoul")).toString()` 으로 ISO-8601 문자열 변환.
   - 다른 필드 (team/state/progress/task) 누락 시 직전 캐시 유지 (Service 처리, R-2-b).
5. 단위 테스트 (정상/`updated` 누락/`updated` 파싱 실패/잘못된 progress/한글 task) 동시 작성. `updated` 누락 케이스는 `updatedAt == null` 단정.

### Phase 2 — Watcher (NIO + Polling)

#### Step 2-1. `TeamStatusWatcher` 인터페이스 + 콜백
```java
public interface TeamStatusWatcher {
    void start();
    void stop();
    boolean isAlive();
    Instant lastEventAt();
    void subscribe(Consumer<String> onChangedTeam); // 콜백: team 이름
}
```

#### Step 2-2. `JavaNioWatcher` 구현 (기본)
1. `@Component @ConditionalOnProperty("teammonitor.watcher.mode", havingValue="nio", matchIfMissing=false)` — auto/nio 일 때 활성. 단, `auto` 처리는 §2-4의 팩토리에서 분기.
2. 단일 백그라운드 스레드 (`Executors.newSingleThreadExecutor("team-monitor-watcher")`).
3. `WatchService` 등록. `ENTRY_MODIFY` + `ENTRY_CREATE` 둘 다. 변경 파일이 `{planner|db|developer|codex}.status` 면 콜백 발화.
4. catch (`ClosedWatchServiceException`, `IOException`) → `isAlive=false` 마킹 + 헬스 indicator 가 DOWN 으로 노출.

#### Step 2-3. `PollingWatcher` 구현 (fallback)
1. `Executors.newSingleThreadScheduledExecutor`. `teammonitor.watcher.polling-interval-ms` (기본 1000ms) 주기.
2. 각 4파일 `Files.getLastModifiedTime()` 추적, 변경 감지 시 콜백.

#### Step 2-4. Watcher 팩토리 (auto 모드)
1. `@Configuration TeamMonitorWatcherConfig`.
2. `@Bean TeamStatusWatcher` 메서드:
   - mode=`nio` → 직접 `JavaNioWatcher`.
   - mode=`polling` → `PollingWatcher`.
   - mode=`auto` (기본) → 먼저 NIO 시도 → `WatchService` 생성 실패 catch 시 polling fallback. 결정 결과 로그 1회 INFO.
3. 추가: `teammonitor.watcher.restart-interval-ms` (기본 30000) — `@Scheduled` 또는 ScheduledExecutor 로 watcher 다운 감지 시 재시작 시도.

#### Step 2-5. 단위 테스트
1. JavaNioWatcher: 임시 디렉토리에 `planner.status` 쓰기 → 1초 이내 콜백 수신 (`@Timeout(2)`).
2. PollingWatcher: 동일 시나리오, polling-interval-ms=200 으로 빠르게.
3. Auto 팩토리: WatchService 모킹 실패 케이스 → PollingWatcher 반환 검증.

### Phase 3 — 서비스 계층 (`TeamMonitorService`)

#### Step 3-1. emitter 관리
1. `@Service`. 내부 자료구조:
   - `Deque<RegisteredEmitter> emitters` (`ConcurrentLinkedDeque` + 명시적 `synchronized` 블록 — LRU 위해).
   - `RegisteredEmitter {SseEmitter emitter, String principal, Instant connectedAt, AtomicReference<Instant> lastEventAt}`.
2. `register(Principal principal, SseEmitter emitter)` 메서드:
   - 정원 검사: `emitters.size() >= teammonitor.sse.max-emitters` 면 §3-2 정책 적용.
   - 등록 성공 시 `event: snapshot` 즉시 전송 (`buildSnapshot()`).
   - emitter 의 `onCompletion`/`onTimeout`/`onError` 콜백에서 자기 자신을 `emitters` 에서 제거.

#### Step 3-2. 정원 초과 정책 (기획 §NFR-2-a/b)
1. `@Value("${teammonitor.sse.overflow-policy:reject}")` 분기.
2. `reject` (기본):
   - 컨트롤러 측에서 `OverflowRejectedException` throw → @ControllerAdvice 가 `503 Service Unavailable` + `Retry-After: 30` + JSON `{error:"sse_capacity",maxEmitters:N,retryAfterSec:30}` 반환 (NFR-2-b 표).
3. `evict-oldest`:
   - `lastEventAt` 가장 오래된 emitter 1개 선택 → `event: evicted data: {"reason":"capacity"}` 전송 → `emitter.complete()` → 신규 수락.

#### Step 3-3. Rate limit (기획 §R-3-d)
1. `@Value("${teammonitor.sse.rate-limit-per-min:10}")`.
2. `Map<String, Deque<Instant>> recentByPrincipal` — 분당 윈도우 슬라이딩.
3. 한도 초과 시 `RateLimitedException` → 컨트롤러 advice 가 `429 Too Many Requests` + `Retry-After: 5` + JSON `{error:"sse_rate_limited",retryAfterSec:5}`.

#### Step 3-4. Watcher 콜백 → 변경 dispatch (기획 §FR-5-c) — 동시성 안전

1. 생성자에서 watcher 에 `subscribe(this::onTeamChanged)` 등록.
2. **자료구조 (스레드 안전)**:
   - `latestCache`: `ConcurrentHashMap<String, TeamStatus>` (4팀 키 고정).
   - **`timelineBuffer`: `ConcurrentLinkedDeque<TimelineEntry>`** (이전 `ArrayDeque` 에서 교체 — codex v1 보완 #3).
   - `timelineSize`: `AtomicInteger` (deque 의 size 호출은 O(N)이므로 별도 카운터 유지. push/poll 에 맞춰 inc/dec).
3. `onTeamChanged(String team)`:
   - `reader.readOne(team)` 시도 — 파싱 실패 시 200ms sleep + 1회 재시도 (R-2-b). 두 번째도 실패면 직전 캐시 유지 + WARN 로그.
   - `latestCache` 의 prev 와 비교하여 `prevState/prevProgress` 산출 후 `latestCache.put(team, current)` (atomic replace).
   - `TimelineEntry` 생성 (ULID + ISO occurredAt) → `timelineBuffer.offerFirst(entry)` + `timelineSize.incrementAndGet()`.
   - **상한 초과 시 dequeue**: `while (timelineSize.get() > maxSize) { timelineBuffer.pollLast(); timelineSize.decrementAndGet(); }` — 단일 스레드(watcher 콜백) 진입 보장이지만 명시적 atomic 연산.
   - 모든 emitter 에 `event: update` + 페이로드 (기획 §FR-5-d) 전송. 전송 성공 시 `lastEventAt` 갱신. 실패 시 emitter 제거.
4. **HealthIndicator·snapshot 빌드용 read API** (Step 3-1, 5-1 에서 호출):
   - `snapshotTimeline()`: `List<TimelineEntry> copy = new ArrayList<>(timelineBuffer);` 로 **스냅샷 복사** 후 반환. ConcurrentLinkedDeque 의 iterator 는 weakly consistent 이므로 CME 미발생, 다만 호출 시점 이후 변경분은 미반영 (의도된 동작).
   - `snapshotLatestCache()`: `Map.copyOf(latestCache)` (immutable copy).
   - 두 메서드 모두 lock-free.

#### Step 3-5. Heartbeat
1. `@Scheduled(fixedRateString="${teammonitor.sse.heartbeat-interval-ms:30000}")` — 모든 emitter 에 `event: ping data: {"serverTime":"...ISO..."}` 전송.
2. 실패한 emitter 자동 제거.
3. **선행 조건 (codex v3 보완 E1)**: Step 0-1 의 `@EnableScheduling` 이 활성화되어 있어야 `@Scheduled` 콜백이 실행됨. Step 0 미완료 시 heartbeat 침묵 → SSE 가 30초 idle 후 프록시에서 끊길 수 있음.

#### Step 3-6. 세션 만료 → emitter 종료 (기획 §R-5/M-5b) — 등록 방식 정정 (codex v2 보완 S3)

1. **클래스**: `TeamMonitorSessionListener implements HttpSessionListener`.
   - `sessionDestroyed(HttpSessionEvent se)` 콜백:
     - `Principal` 추출 — `se.getSession().getAttribute("SPRING_SECURITY_CONTEXT")` 의 `SecurityContext.getAuthentication().getName()` 으로 username 획득.
     - `TeamMonitorService.removeEmittersByPrincipal(username)` 호출 → 해당 사용자의 모든 emitter `complete()`.
2. **등록 방식 — `ServletListenerRegistrationBean` 단일** (이전 "WebMvcConfigurer 등록"은 부정확하므로 삭제):
   ```java
   @Bean
   ServletListenerRegistrationBean<TeamMonitorSessionListener> teamMonitorSessionListener(
           TeamMonitorService service) {
       return new ServletListenerRegistrationBean<>(new TeamMonitorSessionListener(service));
   }
   ```
   - 위치: `TeamMonitorFilterConfig.java` 내 추가 (`@Bean` 1개 더) 또는 별도 `TeamMonitorListenerConfig.java` — **결정: `TeamMonitorListenerConfig.java` 신규 (역할 분리)**.
3. **대안 고려**: `ApplicationListener<SessionDestroyedEvent>` (Spring Security 의 `SessionDestroyedEvent`) 도 사용 가능. 다만 본 프로젝트는 표준 Servlet 세션이 충분하므로 위 방식 채택.
4. T14 통합 테스트:
   - `MockHttpSession` 생성 → SSE 연결 → `session.invalidate()` 호출 → 100ms 대기 → emitter `isComplete()` true 단정.

#### Step 3-7. 단위 테스트
1. `register()` — 상한 도달 시 reject 모드 503 raise / evict 모드 가장 오래된 emitter complete 확인.
2. `onTeamChanged()` — prevState 계산 정확 (`대기→진행중`, `진행중→완료` 등).
3. Ring buffer — 21번째 push 시 첫 항목 제거.
4. Rate limit — 11회 분당 호출 시 429 raise.

### Phase 4 — 컨트롤러 + 보안 헤더 + 응답 헤더

#### Step 4-1. `TeamMonitorController` (codex v2 보완 S4 produces / S7 Principal)

```java
@Controller
@RequestMapping("/admin/team-monitor")
@PreAuthorize("hasRole('ADMIN')")
public class TeamMonitorController {

    @GetMapping
    public String page(@RequestParam(required=false) String fullscreen, Model model) {
        model.addAttribute("fullscreen", "1".equals(fullscreen));
        return "admin/team-monitor";
    }

    // S4: produces 는 charset 없는 base 만 (Accept 협상 안정성 확보).
    // 실제 charset 은 응답 헤더에서 별도 set (§4-7).
    @GetMapping(path="/stream", produces="text/event-stream")
    public SseEmitter stream(HttpServletResponse resp,
                              HttpServletRequest req,
                              Principal principal) {  // S7: @AuthenticationPrincipal 제거, Principal 단독 (Spring Security 가 표준 Principal 주입 보장)
        validateOrigin(req); // R-5/§4-7 Vary: Origin
        // §4-7 응답 헤더 고정 — charset 은 여기서 명시
        resp.setHeader("Content-Type", "text/event-stream; charset=UTF-8");
        resp.setHeader("Cache-Control", "no-cache, no-transform");
        resp.setHeader("Pragma", "no-cache");
        resp.setHeader("Connection", "keep-alive");
        resp.setHeader("X-Accel-Buffering", "no");
        resp.setHeader("Vary", "Accept, Origin");
        long timeout = serviceProps.getSseTimeoutMs();
        SseEmitter emitter = new SseEmitter(timeout == 0 ? 0L : timeout);
        teamMonitorService.register(principal, emitter); // 상한/rate limit 검사 내부
        return emitter;
    }
}
```

- **S4 보완 의의**: `produces="text/event-stream;charset=UTF-8"` 은 일부 브라우저/클라이언트의 `Accept: text/event-stream` 헤더와 미디어 타입 매칭이 실패할 수 있음 (charset 파라미터 미스매치). base 만 두면 매칭 안정 + charset 은 응답 헤더로 자유 지정.
- **S7 보완 의의**: `@AuthenticationPrincipal Principal` 조합은 어노테이션이 `UserDetails`/실제 principal 객체를 기대하는데 타입이 `Principal` (인터페이스) 이라 주입 실패하거나 null 가능. Spring Security 는 표준 `Principal` 파라미터를 자동 주입 보장하므로 어노테이션 제거가 안전.

#### Step 4-2. `@RestControllerAdvice` 예외 변환 (NFR-2-b 응답 표)
1. `OverflowRejectedException` → 503 + `Retry-After: 30` + JSON 본문.
2. `RateLimitedException` → 429 + `Retry-After: 5` + JSON 본문.
3. `OriginNotAllowedException` → 403 + JSON 본문.
4. 기존 인증 실패 401 / 권한 부족 403 은 Spring Security 기본 처리 그대로.

#### Step 4-3. `TeamMonitorSecurityHeadersFilter` (기획 §4-6) — 등록 방식 단일화

1. **클래스 정의** — `extends OncePerRequestFilter`. URL 매칭은 필터 자체 로직이 아닌 `FilterRegistrationBean` 패턴으로 외부 위임 (회귀 영향 최소화 + 등록 위치 단일).
2. **등록 방식 — `FilterRegistrationBean` 단일** (SecurityFilterChain 직접 삽입 금지):
   ```java
   import org.springframework.boot.autoconfigure.security.SecurityProperties;

   @Configuration
   public class TeamMonitorFilterConfig {

       @Bean
       FilterRegistrationBean<TeamMonitorSecurityHeadersFilter> teamMonitorSecurityHeadersFilter(
               TeamMonitorProperties props) {
           FilterRegistrationBean<TeamMonitorSecurityHeadersFilter> reg = new FilterRegistrationBean<>();
           reg.setFilter(new TeamMonitorSecurityHeadersFilter(props));
           reg.addUrlPatterns(
               "/admin/team-monitor",
               "/admin/team-monitor/stream"
           );
           // codex v3 보완 E2: 보안체인 직후 보장 (이전 HIGHEST_PRECEDENCE+10 은 보안체인보다 앞설 위험)
           reg.setOrder(SecurityProperties.DEFAULT_FILTER_ORDER + 1);
           reg.setName("teamMonitorSecurityHeadersFilter");
           return reg;
       }
   }
   ```
3. **헤더 set** (필터 본문):
   - `Content-Security-Policy` ← `teammonitor.security.csp` (빈값이면 §4-6 기본).
   - `X-Content-Type-Options: nosniff`
   - `X-Frame-Options: DENY`
   - `Referrer-Policy: same-origin`
   - `Permissions-Policy: microphone=(), camera=(), geolocation=()`
   - `Cross-Origin-Opener-Policy: same-origin`
   - `Cross-Origin-Resource-Policy: same-origin`
   - `Strict-Transport-Security: max-age=31536000; includeSubDomains` — **`@Profile("prod")` 가드**: 필터 내부에서 `Environment.acceptsProfiles(Profiles.of("prod"))` 검사 후 set (개발/로컬 비적용, 기획 §4-6 단서).
4. **SecurityConfig 영향 없음** — 본 필터는 Spring Security 체인 외부의 `FilterRegistrationBean` 으로 등록되므로 `SecurityConfig.java` 는 무수정 (회귀 차단). `/admin/**` 권한 가드는 기존 SecurityFilterChain 이 그대로 처리.
5. **URL 패턴 한정** — `addUrlPatterns` 가 정확히 2개 경로만 매칭. 다른 admin 페이지(`/admin/users`, `/admin/dashboard` 등) 에 헤더가 새로 붙지 않음을 T7 통합 테스트로 검증 (대조 케이스 추가).

#### Step 4-4. SecurityConfig 기존 라우팅 영향
1. `/admin/**` 이미 `hasRole('ADMIN')` 으로 보호됨 → 자동 적용. 추가 라우팅 불필요.
2. `/admin/team-monitor/stream` 은 GET only → CSRF 영향 없음 (state 비변경). 기존 `/admin/api/**` CSRF 면제 패턴은 그대로 유지.
3. `application.properties` 에 `teammonitor.security.allowed-origins=` 추가 (기본 빈값=동일출처만).

#### Step 4-5. gzip 제외 — Spring 측 강제 + 통합 검증
1. **Spring 측**: Step 1-1 에서 `server.compression.enabled=false` + `server.compression.mime-types` 에 `text/event-stream` 명시적 제외 + WARNING 주석 추가 완료.
2. **컨트롤러 보강**: `TeamMonitorController.stream()` 진입 시 응답 헤더에 `Content-Encoding` 키 자체를 set 하지 않음 (확인). 일부 외부 미들웨어가 후속 Encoding 을 끼워 넣지 못하게 응답 속성으로 `request.setAttribute("org.springframework.web.server.handler.DispatcherHandler.NOT_HANDLED_NOTIFY", true)` 류 우회는 사용하지 않음 — 표준 SSE 만 사용.
3. **프록시 측 가이드** (Step 1-1-4 참조): nginx `gzip off;` + `proxy_buffering off;`.
4. **통합 테스트 T6** — `curl -H "Accept-Encoding: gzip" -N /admin/team-monitor/stream` 으로 응답 헤더 검증:
   - `Content-Encoding` 헤더 부재 확인
   - `Content-Type: text/event-stream; charset=UTF-8` 확인
   - `Cache-Control: no-cache, no-transform` 확인
5. T6 가 fail 하면 즉시 빌드 차단 (CI fail).

### Phase 5 — Actuator 헬스 indicator (기획 §4-9)

#### Step 5-1. `TeamMonitorHealthIndicator` — 동시성 안전 read

1. `extends AbstractHealthIndicator`.
2. **read 전략 (codex v1 보완 #3)**:
   - 모든 read 는 `TeamMonitorService` 가 노출하는 **스냅샷 메서드** (`snapshotTimeline()`, `snapshotLatestCache()`, `snapshotMetrics()`) 를 통해 수행. 내부 자료구조 직접 노출 금지.
   - `snapshotMetrics()` 는 `record HealthMetrics(int currentEmitters, int maxEmitters, String overflowPolicy, long totalConnections, long totalRejected503, long totalRateLimited429, Instant watcherLastEventAt, boolean watcherAlive, String watcherMode, int restartCount, String lastError, int timelineSize, int timelineBuffered, Instant timelineLastEntryAt)` 를 1회 호출에 즉시 빌드 (필드별 AtomicLong/AtomicReference get).
   - HealthIndicator 는 `HealthMetrics` 1개만 받아 빌드 → 호출 도중 부분 갱신으로 불일치 키 발생 차단.
3. `doHealthCheck(Builder)` 구현 — §4-9 JSON 샘플 그대로 구성:
   - watcher 그룹: `mode/alive/lastEventAt(ISO)/restartCount/lastError`
   - sse 그룹: `currentEmitters/maxEmitters/overflowPolicy/totalConnections/totalRejected503/totalRateLimited429`
   - timeline 그룹: `size/buffered/lastEntryAt(ISO)`
   - statusDir 그룹: `path(상대경로)/readable/files`
4. UP/DOWN 판정:
   - watcher.alive=false → DOWN
   - statusDir.readable=false → DOWN
   - 그 외 UP (정원 도달은 정상)
5. **동시성 검증** (T18 신규): 1000회 SSE 연결/해제 + heartbeat + watcher 콜백 부하 동안 `/actuator/health/teamMonitor` 100회 호출 → CME/NPE/ConcurrentModification 0건.

#### Step 5-2. 메트릭 카운터
1. Service 내 `AtomicLong totalConnections, totalRejected503, totalRateLimited429`.
2. register/reject/rateLimit 발생 지점에서 increment.
3. 헬스 indicator 가 read.

#### Step 5-3. `application.properties` (기획 §4-4 1:1)
- 기획서 §4-4 코드블록 그대로 복사 + 머리 주석에 §NFR-4-b 표 그대로 복제 (운영자 1차 조회 지점).

#### Step 5-4. `TeamMonitorInfoContributor` (codex v2 보완 S2 — 기획 NFR-4-a)

1. **신규 파일**: `src/main/java/com/swmanager/system/actuator/TeamMonitorInfoContributor.java`.
2. `implements org.springframework.boot.actuate.info.InfoContributor`.
3. `contribute(Info.Builder)` 구현 — `TeamMonitorProperties` 주입 후:
   ```java
   builder.withDetail("teammonitor", Map.of(
       "timeline", Map.of("size", props.getTimeline().getSize()),
       "sse", Map.of(
           "maxEmitters", props.getSse().getMaxEmitters(),
           "overflowPolicy", props.getSse().getOverflowPolicy(),
           "rateLimitPerMin", props.getSse().getRateLimitPerMin()
       ),
       "watcher", Map.of(
           "mode", props.getWatcher().getMode(),
           "pollingIntervalMs", props.getWatcher().getPollingIntervalMs()
       )
   ));
   ```
4. `application.properties` 에 `management.info.env.enabled=false` (기본값) 유지 — 본 InfoContributor 가 단일 진실원.
5. T8 통합 테스트에 `/actuator/info` 호출 + `teammonitor.timeline.size` 키 단정 추가 (T8 본문 수정).

### Phase 6 — 프런트엔드 (Thymeleaf + JS + CSS)

#### Step 6-1. `templates/admin/team-monitor.html` — 다크모드 적용 방식 명시

1. Thymeleaf 레이아웃: 기존 admin layout 사용 (네비게이션 포함). `fullscreen=true` 면 `th:if` 로 헤더/사이드바 숨김.
2. **`<body>` 클래스 (codex v1 보완 #5)**:
   ```html
   <body class="theme-dark team-monitor-page" th:classappend="${fullscreen} ? ' fullscreen' : ''">
   ```
   - `theme-dark` — 본 페이지 스코프 다크모드 활성화 클래스 (Phase 4 전역 다크모드 도입 전이므로 페이지 단위 적용).
   - `team-monitor-page` — CSS 변수 override 의 selector 앵커 (Step 6-3 에서 변수 재정의).
   - `fullscreen` — `?fullscreen=1` 시만 부착되어 헤더/사이드바 숨김 트리거.
3. 4팀 카드 그리드 + 하단 `<section id="timeline">` (기획 §FR-5-b).
4. **모든 동적 텍스트 `th:text` (auto-escape) — `th:utext` 절대 금지** (R-7-a).
5. JS 진입점: `<script src="/js/team-monitor.js" defer></script>` (인라인 금지, CSP 호환).
6. CSS: `<link rel="stylesheet" href="/css/team-monitor.css">`.
7. 초기 데이터는 빈 상태 (snapshot 수신 후 채움). `data-*` 속성으로 백엔드 초기 토큰 (CSRF 없음, ADMIN 세션 가정) 전달 불필요.

#### Step 6-2. `static/js/team-monitor.js`
1. `EventSource('/admin/team-monitor/stream')`.
2. 핸들러:
   - `snapshot` → 4팀 카드 + 타임라인 일괄 렌더 (textContent 만 사용, R-7-b).
   - `update` → 해당 팀 카드 갱신 + 타임라인에 prepend (size 초과 시 마지막 제거).
   - `ping` → `serverTime` 으로 skew 보정 (기획 §4-8 EMA α=0.3).
   - `evicted` → UI 배지 "다른 세션이 자리를 차지하여..." 표시 후 자동 재연결 중단.
3. 재연결: `onerror` → 응답 코드 503/429 면 `Retry-After` 우선 (fetch 헤더 확인 보조 호출), 미존재 시 exponential backoff 3→10→30→60s.
4. 시각 표기: `formatRelative(occurredAt, serverNow)` 헬퍼.
   - **codex v2 보완 S1**: 입력이 `null` 또는 빈 문자열이면 `"갱신: 미기록"` 반환 (now 로 fallback 금지). 카드의 `updatedAt` 가 null 인 경우 적용. 단위 테스트 케이스 추가.
5. 모든 DOM 삽입은 `el.textContent = …` 또는 `el.setAttribute(...)` 만. `innerHTML` 금지.

#### Step 6-3. `static/css/team-monitor.css` — 전환 수치 + 다크모드 변수 명시

1. 토큰 재사용: `docs/DESIGN.md` 디자인 토큰.
2. **다크모드 페이지 스코프 변수 (codex v1 보완 #5)**:
   ```css
   body.team-monitor-page.theme-dark {
       --tm-bg: #0f1115;
       --tm-card-bg: #1a1d24;
       --tm-text: #e6e6e6;
       --tm-text-muted: #9aa0a6;
       --tm-border: #2a2e36;
       --tm-state-wait: #6b7280;     /* 회색 — 대기 */
       --tm-state-progress: #facc15; /* 노랑 — 진행중 */
       --tm-state-done: #22c55e;     /* 초록 — 완료 */
       --tm-state-error: #ef4444;    /* 빨강 — 오류 */
       background: var(--tm-bg);
       color: var(--tm-text);
   }
   ```
   - 라이트모드 fallback 변수 (`body.team-monitor-page` 단독) 도 같은 키로 정의해 두면 향후 토글이 1줄 클래스 변경으로 가능.
3. 카드: 그리드 (데스크탑 2x2, 풀스크린 자동 확대). `background: var(--tm-card-bg)`, `border: 1px solid var(--tm-border)`.
4. 진행바: CSS 그라데이션 + state 별 변수 매핑 (`.bar.state-진행중 { background: var(--tm-state-progress); }`).
5. **전환 수치 명시 (codex v1 보완 #4 — 기획 FR-4 정합)**:
   ```css
   /* 카드 진입 + 상태 변경 시 */
   .team-card { transition: opacity 0.3s ease, background-color 0.3s ease; }
   .team-card.state-changed { animation: fade-in 0.3s ease; }
   @keyframes fade-in { from { opacity: 0.4; } to { opacity: 1; } }

   /* 진행률 바 폭 변경 */
   .progress-bar > .fill { transition: width 0.5s cubic-bezier(0.2, 0.8, 0.2, 1); }
   ```
6. 풀스크린 모드 전용: `body.team-monitor-page.fullscreen > header, body.team-monitor-page.fullscreen > aside { display: none; }` + 카드 풀화면 (그리드 자동 확대).
7. 타임라인 sticky (풀스크린): `body.team-monitor-page.fullscreen #timeline { position: sticky; bottom: 0; max-height: 4rem; overflow: hidden; }` + 클릭 시 expand는 JS 가 `.expanded` 클래스 토글, `transition: max-height 0.3s ease`.
8. 한글 라벨: `font-family: "Pretendard", system-ui, sans-serif;` / 진행률 숫자: `font-family: "JetBrains Mono", "Fira Code", Consolas, monospace;`.

#### Step 6-4. 관리자 메뉴 링크 추가
1. 기존 admin 네비게이션 템플릿 (Glob 으로 정확 위치 식별 후 결정) 에 `<a href="/admin/team-monitor">팀 모니터</a>` 1줄 추가.
2. 풀스크린 모드 별도 표기는 페이지 내 토글 버튼으로.

### Phase 7 — 테스트 + 빌드 + 스모크

#### Step 7-1. 통합 테스트 (`@SpringBootTest`, `@AutoConfigureMockMvc` 또는 `@LocalServerPort`)
1. T2/T3/T4 (아래 표) 자동화.
2. SSE 통합: `WebTestClient.get("/admin/team-monitor/stream", Accept=text/event-stream)` 으로 첫 `event: snapshot` 수신 확인.

#### Step 7-2. 수동 검증 항목 (개발자 1회 수행)
1. 브라우저 `/admin/team-monitor` 접속 → 4카드 표시.
2. 다른 터미널에서 `bash .team-workspace/set-status.sh planner 진행중 70 "수동 테스트"` 실행 → 1초 이내 카드 + 타임라인 갱신.
3. 풀스크린 (`?fullscreen=1`) 듀얼 모니터 렌더 확인.
4. XSS 페이로드: `bash .team-workspace/set-status.sh planner 오류 0 "<script>alert(1)</script>"` → 텍스트 그대로 표기 (alert 미발생) + DevTools Console CSP 차단 메시지.
5. `curl -I http://localhost:9090/admin/team-monitor` (인증 후) → §4-6 보안 헤더 전부 존재.
6. `curl -H "Accept-Encoding: gzip" -N /admin/team-monitor/stream` → `Content-Encoding` 부재 + `Cache-Control: no-cache, no-transform` 존재.

#### Step 7-3. 빌드 / 재기동 / 스모크
1. `./mvnw -q compile` → BUILD SUCCESS.
2. `./mvnw -q test -Dtest='Team*'` → 신규 테스트 전부 PASS.
3. `bash server-restart.sh` → `Started SwmanagerApplication` + ERROR 0 로그.
4. `curl -fsS http://localhost:9090/actuator/health/teamMonitor -u admin:...` → §4-9 JSON 스키마 일치 + status=UP.

#### Step 7-4. 회귀 영향 확인
1. `monitor.sh` 실행 → 기존 ANSI 렌더 정상 (위에서 적용한 atomic write 와 호환).
2. 기존 admin 메뉴 페이지 (사용자/대시보드/회원관리 등) 정상 라우팅.
3. `/actuator/info` 에 `teammonitor.*` 노출 확인.

---

## 2. 테스트 (T#)

| ID | 내용 | 검증 방법 | Pass 기준 |
|----|------|----------|-----------|
| T1 | atomic write 동시성 | `seq 1 50 | xargs -P 10 -I{} bash set-status.sh planner 진행중 {} "c{}"` 후 `cat planner.status` | 항상 valid 5필드 |
| T2 | SSE snapshot 1회 + 1초 내 update 수신 | 통합 테스트 (`WebTestClient`) | event:snapshot 1건 + update 1건 ≤1000ms |
| T3 | 정원 초과 503 (reject) | max-emitters=2 설정, 3번째 연결 시도 | HTTP 503 + Retry-After:30 + JSON `{error:"sse_capacity"}` |
| T4 | rate limit 429 | 동일 Principal 11회/분 연결 | 11번째 HTTP 429 + Retry-After:5 |
| T5 | XSS 차단 | 수동 (Step 7-2-4) | alert 미발생 + 텍스트 escape + CSP 차단 콘솔 |
| T6 | gzip 제외 | `curl -H "Accept-Encoding: gzip" -N /admin/team-monitor/stream` | `Content-Encoding` 헤더 부재 + `Cache-Control: no-cache, no-transform` 존재 |
| T7 | CSP/보안 헤더 (page) | `curl -I /admin/team-monitor` | CSP / X-Frame-Options / Referrer-Policy / Permissions-Policy / COOP / CORP 6종 모두 존재 |
| T7b | CSP/보안 헤더 (stream, **codex v2 S6**) | `curl -I /admin/team-monitor/stream` | T7 동일 6종 + `Vary: Accept, Origin` 존재 (FilterRegistrationBean 2경로 매칭 검증) |
| T8 | 헬스 indicator | `GET /actuator/health/teamMonitor` | status=UP + §4-9 JSON 스키마 키 모두 존재 |
| T9 | watcher 다운 → DOWN | watcher 강제 close 시뮬레이션 | status=DOWN + lastError 노출 |
| T10 | prevState 정확성 | 단위 테스트 (`TeamMonitorService`) | `대기→진행중`, `진행중→완료` 등 모든 전이 케이스 |
| T11 | timeline ring buffer | 단위 테스트 (`teammonitor.timeline.size=3`) 4번째 push | 첫 항목 제거 |
| T12 | skew 보정 | 수동 (브라우저 시각 5분 변경) | 우측 하단 skew 배지 노출 + 상대시각 서버 기준 |
| T13 | 풀스크린 모드 | 수동 (`?fullscreen=1`) | 헤더/사이드바 숨김 + 카드 자동 확대 + 타임라인 sticky |
| T14 | session 만료 → emitter 종료 | 통합 테스트 — `MockHttpSession` 으로 SSE 연결 후 `session.invalidate()` 호출 (HttpSessionListener 시나리오) | 100ms 내 해당 Principal emitter `isComplete()=true` 단정 |
| T15 | Maven compile | `./mvnw -q compile` | BUILD SUCCESS |
| T16 | 서버 재기동 | `bash server-restart.sh` | Started + ERROR 0 |
| T17 | 회귀 — 터미널 monitor.sh | 다른 터미널에서 `bash .team-workspace/monitor.sh` | 기존 ANSI 렌더 정상 |
| T18 | HealthIndicator 동시성 (**codex v2 현실화**) | 모킹 기반 부하: 100 SSE 연결/해제 + watcher 콜백 50건 동안 `/actuator/health/teamMonitor` 50회 호출 (`@SpringBootTest` + 풀 스레드 16개) | CME/NPE/ConcurrentModification 0건, 매 응답 §4-9 스키마 일치 |
| T19 | 보안 헤더 스코프 | `/admin/users` 등 다른 admin 페이지에 `curl -I` | CSP/COOP/CORP 등 team-monitor 전용 헤더 부재 (스코프 격리 검증) |
| T20 | gzip mime-types 회귀 (**codex v2 강화 S5**) | (a) `@Autowired ServerProperties` → `getCompression().getMimeTypes()` 가 `text/event-stream` 미포함 단정. (b) `WebTestClient.get("/admin/team-monitor/stream").headers` 에 `Content-Encoding` 헤더 부재 단정 | (a)+(b) 모두 PASS — 2중 방어 |
| T21 | InfoContributor (**codex v2 신규 S2**) | `GET /actuator/info` | `teammonitor.timeline.size`, `teammonitor.sse.maxEmitters`, `teammonitor.watcher.mode` 키 모두 존재 |

---

## 3. 롤백 전략

| 상황 | 조치 |
|------|------|
| compile 실패 | Edit 되돌림 후 import/의존 재확인. pom.xml actuator 추가가 conflict 면 별도 commit 분리. |
| 런타임 watcher 무한루프 | `teammonitor.watcher.mode=polling` 으로 강제 → 재시작. 코드 수정은 hotfix. |
| SSE OOM (emitter 누수) | `teammonitor.sse.max-emitters=5` 로 임시 축소 + 서버 재기동. 누수 원인 디버깅 후 수정. |
| 보안 헤더로 정상 페이지 깨짐 | `teammonitor.security.csp=` (빈값=기본 CSP) 또는 임시로 더 느슨한 CSP override. **단 CSP 완전 제거는 금지** (기획 §R-7-c). |
| 배포 후 회귀 | `git revert <commit>` → 재배포. 단일 PR 머지이므로 revert 1회로 깔끔히 원복. |
| status 파일 atomic write 회귀 | `set-status.sh` 만 별도 revert (기존 heredoc 복원) — 신규 web 대시보드는 영향 없음. |

---

## 4. 리스크·완화 재확인 (기획 §5 매핑)

| 리스크 | 수준 | 완화 (구현 단계 매핑) |
|--------|------|----------------------|
| R-1 watcher 환경 의존성 | P1 | Step 2-2/2-3/2-4 (auto fallback) |
| R-2 status race | P0 | Step 1-2 (atomic write) + Step 3-4 (200ms 재시도) |
| R-3 emitter 누수 | P0 | Step 3-1 (콜백 자동 제거) + 3-2 (정원) + 3-5 (heartbeat) + 3-3 (rate limit) |
| R-4 프록시 timeout | P1 | Step 4-1 (X-Accel-Buffering: no) + Step 3-5 (heartbeat) |
| R-5 인증/권한 long-lived | P0 | Step 4-1 (Origin 검증) + Step 3-6 (세션 만료 종료) + SecurityConfig `/admin/**` |
| R-6 운영/장애 | P2 | Step 5-1 (헬스 indicator) + Step 6-2 (재연결 UI) |
| R-7 XSS | P0 | Step 6-1 (`th:text`) + 6-2 (`textContent`) + 4-3 (CSP) |

---

## 5. 영향 파일 (확정)

### 신규 (Java)
- `src/main/java/com/swmanager/system/controller/TeamMonitorController.java`
- `src/main/java/com/swmanager/system/service/teammonitor/TeamStatusReader.java`
- `src/main/java/com/swmanager/system/service/teammonitor/TeamStatusWatcher.java` (interface)
- `src/main/java/com/swmanager/system/service/teammonitor/JavaNioWatcher.java`
- `src/main/java/com/swmanager/system/service/teammonitor/PollingWatcher.java`
- `src/main/java/com/swmanager/system/service/teammonitor/TeamMonitorWatcherConfig.java`
- `src/main/java/com/swmanager/system/service/teammonitor/TeamMonitorService.java`
- `src/main/java/com/swmanager/system/service/teammonitor/TeamMonitorSessionListener.java`
- `src/main/java/com/swmanager/system/service/teammonitor/TeamMonitorHealthIndicator.java`
- `src/main/java/com/swmanager/system/config/TeamMonitorSecurityHeadersFilter.java`
- `src/main/java/com/swmanager/system/config/TeamMonitorFilterConfig.java` (`FilterRegistrationBean` 등록 — Step 4-3)
- `src/main/java/com/swmanager/system/config/TeamMonitorListenerConfig.java` (**codex v2 S3** — `ServletListenerRegistrationBean<HttpSessionListener>` 등록, Step 3-6)
- `src/main/java/com/swmanager/system/config/TeamMonitorProperties.java` (`@ConfigurationProperties("teammonitor")`)
- `src/main/java/com/swmanager/system/actuator/TeamMonitorInfoContributor.java` (**codex v2 S2** — `/actuator/info` 노출, Step 5-4)
- `src/main/java/com/swmanager/system/service/teammonitor/HealthMetrics.java` (record — Step 5-1 스냅샷 DTO)
- `src/main/java/com/swmanager/system/dto/TeamStatus.java`
- `src/main/java/com/swmanager/system/dto/TimelineEntry.java`
- `src/main/java/com/swmanager/system/dto/TeamStatusEvent.java`
- `src/main/java/com/swmanager/system/dto/support/UlidGenerator.java`
- `src/main/java/com/swmanager/system/exception/OverflowRejectedException.java`
- `src/main/java/com/swmanager/system/exception/RateLimitedException.java`
- `src/main/java/com/swmanager/system/exception/OriginNotAllowedException.java`
- `src/main/java/com/swmanager/system/controller/TeamMonitorAdvice.java`
- `src/test/java/com/swmanager/system/service/teammonitor/*Test.java` (Reader/Watcher/Service/HealthIndicator)
- `src/test/java/com/swmanager/system/controller/TeamMonitorControllerIT.java`

### 신규 (정적 자원)
- `src/main/resources/templates/admin/team-monitor.html`
- `src/main/resources/static/css/team-monitor.css`
- `src/main/resources/static/js/team-monitor.js`

### 수정
- `pom.xml` — `spring-boot-starter-actuator` 1줄 추가
- `src/main/resources/application.properties` — `teammonitor.*` 13줄 + `management.*` 2줄 추가
- `.team-workspace/set-status.sh` — heredoc → mktemp+printf+mv (기획 §4-10)
- `src/main/java/com/swmanager/system/config/SecurityConfig.java` — **무수정** (codex v1 보완 #2 — `FilterRegistrationBean` 으로 외부 등록되어 SecurityFilterChain 영향 없음)
- `src/main/java/com/swmanager/system/SwManagerApplication.java` — **codex v3 보완 E1+E4** — `@EnableScheduling` + `@EnableConfigurationProperties(TeamMonitorProperties.class)` 2줄 추가 (Step 0-1)
- 관리자 네비게이션 템플릿 1개 — 메뉴 링크 1줄

### 변경 없음
- `.team-workspace/monitor.sh` (터미널판 유지)
- DB 스키마

---

## 6. 단일 PR 구성 / commit 분할

본 스프린트는 **단일 PR** 으로 머지. 단, commit 은 다음 5개로 분할 (revert 단위로 명확):

1. `chore(team-monitor): add actuator dependency + properties` (Step 1-1)
2. `feat(team-monitor): atomic write for set-status.sh` (Step 1-2)
3. `feat(team-monitor): backend (DTO/Reader/Watcher/Service/Controller/Health/SecurityHeaders)` (Step 1-3 ~ 5-2)
4. `feat(team-monitor): frontend template + js + css + admin menu link` (Step 6-1 ~ 6-4)
5. `test(team-monitor): unit + integration` (Step 7-1)

각 commit 메시지 끝에 `Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>` (AGENTS.md §5).

---

## 7. 승인 요청

본 개발계획서 v3 에 대한 **codex 재검토** 및 **사용자 최종승인** 을 요청합니다.

승인 후 진행 순서:
1. Phase 1 → 2 → ... → 7 순차 구현
2. 각 Phase 완료 시점에서 `set-status.sh developer 진행중 N` 으로 진척 갱신
3. Phase 7 완료 후 codex 검증 (구현물 기준) → 사용자 "작업완료" 발화 → 자동 commit+push (AGENTS.md §5)
