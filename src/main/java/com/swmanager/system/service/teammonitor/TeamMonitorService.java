package com.swmanager.system.service.teammonitor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swmanager.system.config.TeamMonitorProperties;
import com.swmanager.system.dto.TeamStatus;
import com.swmanager.system.dto.TeamStatusEvent;
import com.swmanager.system.dto.TimelineEntry;
import com.swmanager.system.dto.support.UlidGenerator;
import com.swmanager.system.exception.OverflowRejectedException;
import com.swmanager.system.exception.RateLimitedException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Principal;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 가상 팀 모니터 핵심 서비스 (sprint team-monitor-dashboard, 개발계획 Phase 3).
 *
 * 책임:
 *  - emitter 관리 (등록/제거/LRU 또는 reject 정책, rate limit, heartbeat).
 *  - watcher 콜백 수신 → prevState 산출 → ring buffer push → 모든 emitter 에 SSE update 전송.
 *  - HealthIndicator/InfoContributor 가 호출하는 스냅샷 read API.
 */
@Service
public class TeamMonitorService {

    private static final Logger log = LoggerFactory.getLogger(TeamMonitorService.class);
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    /** sprint team-monitor-wildcard-watcher: 1 → 2 (teamMeta 필드 추가). */
    private static final int SCHEMA_VERSION = 2;

    private final TeamMonitorProperties props;
    private final TeamStatusReader reader;
    private final TeamStatusWatcher watcher;
    private final TeamMetadata teamMetadata;
    private final ObjectMapper mapper = new ObjectMapper();

    // emitter 관리 (LRU 위해 deque + lastEventAt 메타)
    private final Deque<RegisteredEmitter> emitters = new ConcurrentLinkedDeque<>();
    // S1 보강: latest cache 는 ConcurrentHashMap. updatedAt nullable 그대로 보존.
    private final Map<String, TeamStatus> latestCache = new ConcurrentHashMap<>();
    // codex v1 보완 #3: ConcurrentLinkedDeque + AtomicInteger size 카운터
    private final ConcurrentLinkedDeque<TimelineEntry> timelineBuffer = new ConcurrentLinkedDeque<>();
    private final AtomicInteger timelineSize = new AtomicInteger(0);

    // rate limit: principal → 분당 호출 시각 큐
    private final Map<String, Deque<Instant>> recentByPrincipal = new ConcurrentHashMap<>();

    // 메트릭
    private final AtomicLong totalConnections = new AtomicLong(0);
    private final AtomicLong totalRejected503 = new AtomicLong(0);
    private final AtomicLong totalRateLimited429 = new AtomicLong(0);
    private final AtomicReference<Instant> timelineLastEntryAt = new AtomicReference<>(null);

    public TeamMonitorService(TeamMonitorProperties props,
                              TeamStatusReader reader,
                              TeamStatusWatcher watcher,
                              TeamMetadata teamMetadata) {
        this.props = props;
        this.reader = reader;
        this.watcher = watcher;
        this.teamMetadata = teamMetadata;
    }

    @PostConstruct
    void init() {
        // C3-01-A + R-11: 부팅 시 디렉토리 보장 + 절대경로 로그
        try {
            Files.createDirectories(props.getStatusPath());
            Files.createDirectories(props.getWorkspacePath());
            log.info("team-monitor workspaceDir: {}", props.getWorkspacePath().toAbsolutePath());
            log.info("team-monitor statusDir:    {}", props.getStatusPath().toAbsolutePath());
        } catch (IOException e) {
            log.warn("team-monitor 디렉토리 보장 실패: {}", e.getMessage());
        }

        // 초기 캐시 로드 (실패해도 부팅은 진행)
        try {
            Map<String, TeamStatus> all = reader.readAll();
            all.forEach((k, v) -> { if (v != null) latestCache.put(k, v); });
        } catch (Exception e) {
            log.warn("초기 status 캐시 로드 실패: {}", e.getMessage());
        }
        watcher.subscribe(this::onTeamChanged);
        watcher.subscribeMetaChange(this::onMetaChanged);            // FR-2-d
        watcher.subscribeTeamDeleted(this::onTeamDeleted);           // C3-01-B
        log.info("TeamMonitorService 초기화 완료. emitters max={}", props.getSse().getMaxEmitters());
    }

    // ------------------------------------------------------------------
    // emitter 등록
    // ------------------------------------------------------------------

    public SseEmitter register(Principal principal, SseEmitter emitter) {
        String name = principal == null ? "anonymous" : principal.getName();
        // rate limit 검사 먼저 (capacity 보다 우선)
        checkRateLimit(name);
        // capacity 검사
        if (emitters.size() >= props.getSse().getMaxEmitters()) {
            handleOverflow();
        }
        RegisteredEmitter re = new RegisteredEmitter(emitter, name, Instant.now());
        emitters.addLast(re);
        totalConnections.incrementAndGet();
        emitter.onCompletion(() -> emitters.remove(re));
        emitter.onTimeout(() -> { emitter.complete(); emitters.remove(re); });
        emitter.onError(t -> { emitter.complete(); emitters.remove(re); });

        // 즉시 snapshot 송신
        sendSnapshot(re);
        return emitter;
    }

    private void checkRateLimit(String principal) {
        int limit = props.getSse().getRateLimitPerMin();
        Instant now = Instant.now();
        Deque<Instant> q = recentByPrincipal.computeIfAbsent(principal, k -> new ArrayDeque<>());
        synchronized (q) {
            // 1분 이전 항목 제거
            while (!q.isEmpty() && q.peekFirst().isBefore(now.minus(Duration.ofMinutes(1)))) {
                q.pollFirst();
            }
            if (q.size() >= limit) {
                totalRateLimited429.incrementAndGet();
                throw new RateLimitedException(5);
            }
            q.addLast(now);
        }
    }

    private void handleOverflow() {
        String policy = props.getSse().getOverflowPolicy();
        if ("evict-oldest".equalsIgnoreCase(policy)) {
            // 진짜 LRU: lastEventAt 이 가장 오래된 emitter 선택 (codex 후속 개선 1).
            // 삽입 순서(peekFirst)는 FIFO 라 활동량을 반영 못 함.
            RegisteredEmitter oldest = null;
            Instant oldestAt = null;
            for (RegisteredEmitter re : emitters) {
                Instant t = re.lastEventAt.get();
                if (t == null) {           // 한 번도 송신 안 된 emitter — 가장 후보
                    oldest = re;
                    oldestAt = Instant.MIN;
                    continue;
                }
                if (oldestAt == null || t.isBefore(oldestAt)) {
                    oldest = re;
                    oldestAt = t;
                }
            }
            if (oldest != null) {
                try {
                    oldest.emitter.send(SseEmitter.event().name("evicted")
                            .data("{\"reason\":\"capacity\"}"));
                } catch (IOException ignored) {}
                oldest.emitter.complete();
                emitters.remove(oldest);
            }
        } else {
            // reject (기본)
            totalRejected503.incrementAndGet();
            throw new OverflowRejectedException(props.getSse().getMaxEmitters());
        }
    }

    private void sendSnapshot(RegisteredEmitter re) {
        try {
            // sprint team-monitor-wildcard-watcher: TEAMS 상수 → reader.listTeams() (FR-1)
            List<String> teamNames = reader.listTeams();
            List<TeamStatus> teams = new ArrayList<>();
            Map<String, TeamStatusEvent.SnapshotTeamMeta> teamMeta = new LinkedHashMap<>();
            for (String t : teamNames) {
                TeamStatus s = latestCache.get(t);
                if (s != null) teams.add(s);
                teamMeta.put(t, new TeamStatusEvent.SnapshotTeamMeta(
                        teamMetadata.emoji(t),
                        teamMetadata.sortOrder(t),
                        teamMetadata.label(t)));
            }
            List<TimelineEntry> timeline = snapshotTimeline();
            TeamStatusEvent.Snapshot snap = new TeamStatusEvent.Snapshot(
                    SCHEMA_VERSION, nowIso(), teams, timeline, teamMeta);
            re.emitter.send(SseEmitter.event().name("snapshot").data(mapper.writeValueAsString(snap)));
            re.lastEventAt.set(Instant.now());
        } catch (IOException e) {
            log.warn("snapshot 전송 실패 — emitter 제거: {}", e.getMessage());
            re.emitter.complete();
            emitters.remove(re);
        }
    }

    /**
     * FR-2-d: teams.json 변경 시 모든 emitter 에 snapshot 재발행
     * (라벨/이모지/정렬 즉시 반영).
     */
    void onMetaChanged() {
        log.info("teams.json 변경 감지 — snapshot 재발행 ({} emitters)", emitters.size());
        for (RegisteredEmitter re : emitters) {
            sendSnapshot(re);
        }
    }

    /**
     * C3-01-B: status DELETE 시 latestCache 정리 + snapshot 재발행
     * (UI 카드 즉시 제거).
     */
    void onTeamDeleted(String team) {
        log.info("status 파일 삭제 감지 — latestCache 정리 + snapshot 재발행: team={}", team);
        latestCache.remove(team);
        for (RegisteredEmitter re : emitters) {
            sendSnapshot(re);
        }
    }

    // ------------------------------------------------------------------
    // watcher 콜백 → 변경 dispatch
    // ------------------------------------------------------------------

    void onTeamChanged(String team) {
        TeamStatus current = readWithRetry(team);
        if (current == null) {
            log.warn("status 재시도 후에도 read 실패 — 캐시 유지: team={}", team);
            return;
        }
        TeamStatus prev = latestCache.put(team, current);

        TimelineEntry entry = new TimelineEntry(
                UlidGenerator.next(),
                team,
                prev == null ? null : prev.state(),
                current.state(),
                prev == null ? null : prev.progress(),
                current.progress(),
                current.task(),
                nowIso()
        );

        timelineBuffer.offerFirst(entry);
        timelineSize.incrementAndGet();
        int max = props.getTimeline().getSize();
        while (timelineSize.get() > max) {
            if (timelineBuffer.pollLast() != null) timelineSize.decrementAndGet();
            else break;
        }
        timelineLastEntryAt.set(Instant.now());

        // 모든 emitter 에 update 송신
        TeamStatusEvent.Update upd = new TeamStatusEvent.Update(
                SCHEMA_VERSION, nowIso(), current, entry);
        String payload;
        try {
            payload = mapper.writeValueAsString(upd);
        } catch (JsonProcessingException e) {
            log.error("update 직렬화 실패", e);
            return;
        }
        for (RegisteredEmitter re : emitters) {
            try {
                re.emitter.send(SseEmitter.event().name("update").data(payload));
                re.lastEventAt.set(Instant.now());
            } catch (Exception e) {
                log.debug("emitter 전송 실패 — 제거: {}", e.getMessage());
                try { re.emitter.complete(); } catch (Exception ignored) {}
                emitters.remove(re);
            }
        }
    }

    private TeamStatus readWithRetry(String team) {
        try {
            return reader.readOne(team).orElse(null);
        } catch (IOException ioe) {
            // 200ms 후 1회 재시도 (R-2-b)
            try { Thread.sleep(200); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
            try {
                return reader.readOne(team).orElse(null);
            } catch (IOException ignored) {
                return null;
            }
        }
    }

    // ------------------------------------------------------------------
    // Heartbeat (E1: @EnableScheduling 활성 필요)
    // ------------------------------------------------------------------

    @Scheduled(fixedRateString = "${teammonitor.sse.heartbeat-interval-ms:30000}")
    public void heartbeat() {
        if (emitters.isEmpty()) return;
        String payload;
        try {
            payload = "{\"serverTime\":\"" + nowIso() + "\"}";
        } catch (Exception e) { return; }
        for (RegisteredEmitter re : emitters) {
            try {
                re.emitter.send(SseEmitter.event().name("ping").data(payload));
                re.lastEventAt.set(Instant.now());
            } catch (Exception e) {
                try { re.emitter.complete(); } catch (Exception ignored) {}
                emitters.remove(re);
            }
        }
    }

    // ------------------------------------------------------------------
    // 세션 만료 → emitter 종료
    // ------------------------------------------------------------------

    public void removeEmittersByPrincipal(String principalName) {
        if (principalName == null) return;
        Iterator<RegisteredEmitter> it = emitters.iterator();
        while (it.hasNext()) {
            RegisteredEmitter re = it.next();
            if (principalName.equals(re.principal)) {
                try { re.emitter.complete(); } catch (Exception ignored) {}
                it.remove();
            }
        }
    }

    // ------------------------------------------------------------------
    // HealthIndicator / InfoContributor 호출용 스냅샷 API
    // ------------------------------------------------------------------

    public List<TimelineEntry> snapshotTimeline() {
        return new ArrayList<>(timelineBuffer);
    }

    public Map<String, TeamStatus> snapshotLatestCache() {
        return Map.copyOf(latestCache);
    }

    public HealthMetrics snapshotMetrics() {
        Path dir = Path.of(props.getStatusDir());
        boolean readable = Files.isReadable(dir);
        return new HealthMetrics(
                emitters.size(),
                props.getSse().getMaxEmitters(),
                props.getSse().getOverflowPolicy(),
                totalConnections.get(),
                totalRejected503.get(),
                totalRateLimited429.get(),
                watcher.isAlive(),
                watcher.mode(),
                watcher.lastEventAt(),
                watcher.restartCount(),
                watcher.lastError(),
                props.getTimeline().getSize(),
                timelineSize.get(),
                timelineLastEntryAt.get(),
                dir.toString(),
                readable
        );
    }

    // ------------------------------------------------------------------

    private static String nowIso() {
        return OffsetDateTime.now(KST).toString();
    }

    private static class RegisteredEmitter {
        final SseEmitter emitter;
        final String principal;
        final Instant connectedAt;
        final AtomicReference<Instant> lastEventAt;

        RegisteredEmitter(SseEmitter e, String p, Instant t) {
            this.emitter = e;
            this.principal = p;
            this.connectedAt = t;
            this.lastEventAt = new AtomicReference<>(t);
        }
    }
}
