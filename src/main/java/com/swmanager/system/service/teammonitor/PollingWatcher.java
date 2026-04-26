package com.swmanager.system.service.teammonitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * lastModifiedTime 폴링 기반 변경 감지 (sprint team-monitor-dashboard, 개발계획 Step 2-3).
 * (sprint team-monitor-wildcard-watcher: listTeams 동적 호출 + teams.json mtime 추적)
 *
 * NIO WatchService 가 동작 안 하는 환경(특정 컨테이너/마운트) 의 fallback.
 */
public class PollingWatcher implements TeamStatusWatcher {

    private static final Logger log = LoggerFactory.getLogger(PollingWatcher.class);

    private final Path statusDir;
    private final Path teamsJsonPath;
    private final long intervalMs;
    private final TeamStatusReader reader;
    private final TeamMetadata teamMetadata;

    private final List<Consumer<String>> subscribers = new CopyOnWriteArrayList<>();
    private final List<Consumer<String>> teamDeletedSubscribers = new CopyOnWriteArrayList<>();
    private final List<Runnable> metaChangeSubscribers = new CopyOnWriteArrayList<>();
    private final Map<String, Long> lastMtime = new ConcurrentHashMap<>();
    private volatile long teamsJsonLastMtime = -1L;
    private final AtomicBoolean alive = new AtomicBoolean(false);
    private final AtomicInteger restartCount = new AtomicInteger(0);
    private volatile Instant lastEventAt;
    private volatile String lastError;
    private ScheduledExecutorService exec;

    public PollingWatcher(Path statusDir, Path teamsJsonPath, long intervalMs,
                          TeamStatusReader reader, TeamMetadata teamMetadata) {
        this.statusDir = statusDir;
        this.teamsJsonPath = teamsJsonPath;
        this.intervalMs = intervalMs;
        this.reader = reader;
        this.teamMetadata = teamMetadata;
    }

    @Override
    public synchronized void start() {
        if (alive.get()) return;
        try {
            Files.createDirectories(statusDir);  // C3-01-A
            // 초기 mtime 기록 (start 시점부터의 변화만 콜백)
            for (String team : reader.listTeams()) {
                try {
                    Path f = statusDir.resolve(team + ".status");
                    if (Files.exists(f)) {
                        lastMtime.put(team, Files.getLastModifiedTime(f).toMillis());
                    }
                } catch (Exception ignored) {}
            }
            // teams.json mtime 초기 기록
            if (Files.exists(teamsJsonPath)) {
                try {
                    teamsJsonLastMtime = Files.getLastModifiedTime(teamsJsonPath).toMillis();
                } catch (Exception ignored) {}
            }
            exec = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "team-monitor-polling-watcher");
                t.setDaemon(true);
                return t;
            });
            exec.scheduleAtFixedRate(this::tick, intervalMs, intervalMs, TimeUnit.MILLISECONDS);
            alive.set(true);
            lastError = null;
            log.info("PollingWatcher 시작: statusDir={}, teamsJson={}, intervalMs={}",
                    statusDir, teamsJsonPath, intervalMs);
        } catch (Exception e) {
            alive.set(false);
            lastError = e.getClass().getSimpleName() + ": " + e.getMessage();
            throw new IllegalStateException("PollingWatcher 시작 실패", e);
        }
    }

    private void tick() {
        try {
            // teams.json mtime 추적 (FR-3-b / Z-02)
            if (Files.exists(teamsJsonPath)) {
                long mt = Files.getLastModifiedTime(teamsJsonPath).toMillis();
                if (mt != teamsJsonLastMtime) {
                    teamsJsonLastMtime = mt;
                    if (mt > 0) {  // 첫 등장 (-1) 도 변화로 간주
                        teamMetadata.reload();
                        reader.invalidateCache();
                        notifyMetaChange();
                        lastEventAt = Instant.now();
                    }
                }
            } else if (teamsJsonLastMtime != -1L) {
                // teams.json 삭제됨
                teamsJsonLastMtime = -1L;
                teamMetadata.reload();
                reader.invalidateCache();
                notifyMetaChange();
                lastEventAt = Instant.now();
            }

            // status 파일 폴링
            for (String team : reader.listTeams()) {
                Path f = statusDir.resolve(team + ".status");
                if (!Files.exists(f)) continue;
                long mt = Files.getLastModifiedTime(f).toMillis();
                Long prev = lastMtime.get(team);
                if (prev == null || mt > prev) {
                    lastMtime.put(team, mt);
                    lastEventAt = Instant.now();
                    notifySubscribers(team);
                }
            }
        } catch (Exception e) {
            lastError = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.warn("PollingWatcher tick 실패: {}", lastError);
        }
    }

    private void notifySubscribers(String team) {
        for (Consumer<String> sub : subscribers) {
            try { sub.accept(team); }
            catch (Exception e) { log.warn("watcher subscriber 콜백 실패: team={}", team, e); }
        }
    }

    private void notifyTeamDeleted(String team) {
        for (Consumer<String> sub : teamDeletedSubscribers) {
            try { sub.accept(team); }
            catch (Exception e) { log.warn("teamDeleted subscriber 콜백 실패: team={}", team, e); }
        }
    }

    private void notifyMetaChange() {
        for (Runnable sub : metaChangeSubscribers) {
            try { sub.run(); }
            catch (Exception e) { log.warn("metaChange subscriber 콜백 실패", e); }
        }
    }

    @Override
    public synchronized void stop() {
        alive.set(false);
        if (exec != null) {
            exec.shutdownNow();
        }
    }

    void incrementRestartCount() { restartCount.incrementAndGet(); }

    @Override public boolean isAlive() { return alive.get(); }
    @Override public Instant lastEventAt() { return lastEventAt; }
    @Override public void subscribe(Consumer<String> onChangedTeam) { subscribers.add(onChangedTeam); }
    @Override public void subscribeMetaChange(Runnable onMetaChange) { metaChangeSubscribers.add(onMetaChange); }
    @Override public void subscribeTeamDeleted(Consumer<String> onTeamDeleted) { teamDeletedSubscribers.add(onTeamDeleted); }
    @Override public String lastError() { return lastError; }
    @Override public String mode() { return "polling"; }
    @Override public int restartCount() { return restartCount.get(); }
}
