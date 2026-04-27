package com.swmanager.system.service.teammonitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * WatchService 기반 변경 감지 (sprint team-monitor-dashboard, 개발계획 Step 2-2).
 * (sprint team-monitor-wildcard-watcher: 두 WatchKey + 와일드카드 + OVERFLOW + meta reload)
 *
 * - 단일 백그라운드 스레드 (daemon).
 * - statusKey: statusDir 의 *.status 파일 (FR-2-a, dot-prefix 제외)
 * - workspaceKey: workspaceDir 의 teams.json 파일 (FR-2-d)
 * - OVERFLOW 시 풀스캔 폴백 + 메타 reload 우선 (FR-2-e, N11)
 * - 임시파일 (.team.status.XXXX 등) 무시
 */
public class JavaNioWatcher implements TeamStatusWatcher {

    private static final Logger log = LoggerFactory.getLogger(JavaNioWatcher.class);
    private static final PathMatcher MATCHER =
            FileSystems.getDefault().getPathMatcher("glob:*.status");

    private final Path statusDir;
    private final Path workspaceDir;
    private final TeamStatusReader reader;
    private final TeamMetadata teamMetadata;

    /** Z-01 + S1-02: 정규화된 절대경로를 volatile 인스턴스 필드로 승격 (이벤트 루프 비교 일관성). */
    private volatile Path normStatusDir;
    private volatile Path normWorkspaceDir;

    private final List<Consumer<String>> subscribers = new CopyOnWriteArrayList<>();
    private final List<Consumer<String>> teamDeletedSubscribers = new CopyOnWriteArrayList<>();
    private final List<Runnable> metaChangeSubscribers = new CopyOnWriteArrayList<>();
    private final AtomicBoolean alive = new AtomicBoolean(false);
    private final AtomicInteger restartCount = new AtomicInteger(0);
    private volatile Instant lastEventAt;
    private volatile String lastError;
    private Thread thread;
    private WatchService ws;

    public JavaNioWatcher(Path statusDir, Path workspaceDir,
                          TeamStatusReader reader, TeamMetadata teamMetadata) {
        this.statusDir = statusDir;
        this.workspaceDir = workspaceDir;
        this.reader = reader;
        this.teamMetadata = teamMetadata;
    }

    @Override
    public synchronized void start() {
        if (alive.get()) return;
        try {
            // C3-01-A: 부팅 시 디렉토리 보장 (graceful)
            Files.createDirectories(statusDir);
            Files.createDirectories(workspaceDir);

            // Z-01: 정규화된 절대경로로 등록 + 비교
            this.normStatusDir = statusDir.toAbsolutePath().normalize();
            this.normWorkspaceDir = workspaceDir.toAbsolutePath().normalize();

            ws = FileSystems.getDefault().newWatchService();
            normStatusDir.register(ws,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.ENTRY_DELETE);
            // teams.json 분리 감시 — workspaceDir 별도 WatchKey
            // (statusDir 와 workspaceDir 가 다를 때만; 같으면 단일 key 로 충분)
            if (!normStatusDir.equals(normWorkspaceDir)) {
                normWorkspaceDir.register(ws,
                        StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_MODIFY,
                        StandardWatchEventKinds.ENTRY_DELETE);
            }
            alive.set(true);
            lastError = null;
            thread = new Thread(this::loop, "team-monitor-nio-watcher");
            thread.setDaemon(true);
            thread.start();
            log.info("JavaNioWatcher 시작: statusDir={}, workspaceDir={}",
                    normStatusDir, normWorkspaceDir);
        } catch (Exception e) {
            alive.set(false);
            lastError = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.warn("JavaNioWatcher 시작 실패: {}", lastError);
            throw new IllegalStateException("WatchService 시작 실패", e);
        }
    }

    private void loop() {
        try {
            // S2-05 + S2-05-a: 키 처리 단위 dedup (메모리 누적 차단)
            Set<String> recentlyNotified = new HashSet<>();
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
                recentlyNotified.clear();  // S2-05-a — 매 키 처리 시 초기화

                Path watchedDir = ((Path) key.watchable()).toAbsolutePath().normalize();

                for (WatchEvent<?> ev : key.pollEvents()) {
                    if (ev.kind() == StandardWatchEventKinds.OVERFLOW) {
                        fullRescanFallback(recentlyNotified);
                        continue;
                    }
                    Object ctx = ev.context();
                    if (!(ctx instanceof Path p)) continue;
                    String name = p.getFileName().toString();

                    if (watchedDir.equals(normWorkspaceDir) && name.equals("teams.json")) {
                        // Z-02: MODIFY/CREATE/DELETE 모두 reload 경로 (atomic replace 자동 커버)
                        teamMetadata.reload();
                        reader.invalidateCache();
                        notifyMetaChange();
                        lastEventAt = Instant.now();
                    } else if (watchedDir.equals(normStatusDir)
                               && MATCHER.matches(p.getFileName())
                               && !name.startsWith(".")) {
                        String team = name.substring(0, name.length() - ".status".length());
                        reader.invalidateCache();
                        if (ev.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
                            // R-14: Windows + JDK17 NIO 에서 atomic mv (REPLACE_EXISTING) 가
                            // ENTRY_DELETE 만 발생시키는 케이스 보정. 짧은 backoff 동안
                            // 파일 재출현 시 MODIFY 합성 — 그렇지 않으면 진짜 삭제.
                            Path target = watchedDir.resolve(p.getFileName());
                            if (appearsReplacedQuickly(target)) {
                                if (recentlyNotified.add(team)) {
                                    log.debug("R-14 보정: DELETE-only → MODIFY 합성: team={}", team);
                                    notifySubscribers(team);
                                }
                            } else {
                                // C3-01-B: 팀 삭제 → Service 가 snapshot 재발행
                                notifyTeamDeleted(team);
                            }
                        } else {
                            // CREATE/MODIFY — dedup 적용 (S2-05)
                            if (recentlyNotified.add(team)) {
                                notifySubscribers(team);
                            }
                        }
                        lastEventAt = Instant.now();
                    }
                    // 그 외 — 무시
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

    /**
     * FR-2-e + N11: OVERFLOW 시 메타 reload 먼저 → listTeams → notify (중복 차단).
     * package-private — 단위 테스트가 OVERFLOW 인위 재현 어려움 → 직접 호출하여 동작 검증 (T-OVERFLOW-NIO).
     */
    void fullRescanFallback(Set<String> roundDedup) {
        log.warn("WatchService OVERFLOW — 풀스캔 폴백");
        reader.invalidateCache();
        teamMetadata.reload();   // N11: 메타 먼저
        for (String team : reader.listTeams()) {
            if (roundDedup.add(team)) {
                notifySubscribers(team);
            }
        }
        lastEventAt = Instant.now();
    }

    /**
     * R-14: Windows + JDK17 NIO atomic replace 가 ENTRY_DELETE 만 발생시키는
     * 케이스 보정. 짧은 backoff 동안 파일 재출현 여부 확인.
     *
     * @return 파일이 backoff 내에 (재)존재 확인되면 true (= replace 로 간주)
     */
    private boolean appearsReplacedQuickly(Path target) {
        if (Files.exists(target)) return true;
        final long deadlineNanos = System.nanoTime()
                + java.util.concurrent.TimeUnit.MILLISECONDS.toNanos(120);
        while (System.nanoTime() < deadlineNanos) {
            try { Thread.sleep(10); }
            catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                return false;
            }
            if (Files.exists(target)) return true;
        }
        return false;
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
        if (ws != null) {
            try { ws.close(); } catch (Exception ignored) {}
        }
        if (thread != null) {
            thread.interrupt();
            // 동기적 종료 대기 — 핸들 해제 보장 (Windows + @TempDir cleanup race 회피).
            // R-14 보정의 backoff sleep (최대 120ms) + 여유 마진 확보.
            try { thread.join(500); }
            catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
        }
    }

    void incrementRestartCount() { restartCount.incrementAndGet(); }

    @Override public boolean isAlive() { return alive.get(); }
    @Override public Instant lastEventAt() { return lastEventAt; }
    @Override public void subscribe(Consumer<String> onChangedTeam) { subscribers.add(onChangedTeam); }
    @Override public void subscribeMetaChange(Runnable onMetaChange) { metaChangeSubscribers.add(onMetaChange); }
    @Override public void subscribeTeamDeleted(Consumer<String> onTeamDeleted) { teamDeletedSubscribers.add(onTeamDeleted); }
    @Override public String lastError() { return lastError; }
    @Override public String mode() { return "nio"; }
    @Override public int restartCount() { return restartCount.get(); }
}
