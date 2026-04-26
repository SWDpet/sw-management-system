package com.swmanager.system.service.teammonitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.*;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * WatchService 기반 변경 감지 (sprint team-monitor-dashboard, 개발계획 Step 2-2).
 *
 * - 단일 백그라운드 스레드 (daemon).
 * - {planner|db|developer|codex}.status 변경/생성 시 콜백.
 * - 임시파일 (.planner.status.XXXX 등 — atomic write 의 mktemp) 은 무시.
 */
public class JavaNioWatcher implements TeamStatusWatcher {

    private static final Logger log = LoggerFactory.getLogger(JavaNioWatcher.class);
    private static final Set<String> TEAM_FILES = Set.of(
            "planner.status", "db.status", "developer.status", "codex.status");

    private final Path statusDir;
    private final List<Consumer<String>> subscribers = new CopyOnWriteArrayList<>();
    private final AtomicBoolean alive = new AtomicBoolean(false);
    private final AtomicInteger restartCount = new AtomicInteger(0);
    private volatile Instant lastEventAt;
    private volatile String lastError;
    private Thread thread;
    private WatchService ws;

    public JavaNioWatcher(Path statusDir) {
        this.statusDir = statusDir;
    }

    @Override
    public synchronized void start() {
        if (alive.get()) return;
        try {
            Files.createDirectories(statusDir);
            ws = FileSystems.getDefault().newWatchService();
            statusDir.register(ws,
                    StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.ENTRY_CREATE);
            alive.set(true);
            lastError = null;
            thread = new Thread(this::loop, "team-monitor-nio-watcher");
            thread.setDaemon(true);
            thread.start();
            log.info("JavaNioWatcher 시작: dir={}", statusDir);
        } catch (Exception e) {
            alive.set(false);
            lastError = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.warn("JavaNioWatcher 시작 실패: {}", lastError);
            throw new IllegalStateException("WatchService 시작 실패", e);
        }
    }

    private void loop() {
        try {
            while (alive.get()) {
                WatchKey key;
                try {
                    key = ws.take();
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (ClosedWatchServiceException cwse) {
                    break;
                }
                for (WatchEvent<?> ev : key.pollEvents()) {
                    if (ev.kind() == StandardWatchEventKinds.OVERFLOW) continue;
                    Object ctx = ev.context();
                    if (!(ctx instanceof Path p)) continue;
                    String name = p.getFileName().toString();
                    if (!TEAM_FILES.contains(name)) continue;
                    String team = name.substring(0, name.length() - ".status".length());
                    lastEventAt = Instant.now();
                    notifySubscribers(team);
                }
                if (!key.reset()) break;
            }
        } catch (Exception e) {
            lastError = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.warn("JavaNioWatcher loop 종료: {}", lastError);
        } finally {
            alive.set(false);
        }
    }

    private void notifySubscribers(String team) {
        for (Consumer<String> sub : subscribers) {
            try { sub.accept(team); }
            catch (Exception e) { log.warn("watcher subscriber 콜백 실패: team={}", team, e); }
        }
    }

    @Override
    public synchronized void stop() {
        alive.set(false);
        if (ws != null) {
            try { ws.close(); } catch (Exception ignored) {}
        }
        if (thread != null) {
            thread.interrupt();
        }
    }

    void incrementRestartCount() { restartCount.incrementAndGet(); }

    @Override public boolean isAlive() { return alive.get(); }
    @Override public Instant lastEventAt() { return lastEventAt; }
    @Override public void subscribe(Consumer<String> onChangedTeam) { subscribers.add(onChangedTeam); }
    @Override public String lastError() { return lastError; }
    @Override public String mode() { return "nio"; }
    @Override public int restartCount() { return restartCount.get(); }
}
