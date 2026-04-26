package com.swmanager.system.service.teammonitor;

import com.swmanager.system.config.TeamMonitorProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PollingWatcher 단위 테스트 — Spring 비의존 (S4-03).
 * (sprint team-monitor-wildcard-watcher §FR-7-c)
 */
class PollingWatcherTest {

    @TempDir Path workspaceDir;

    private PollingWatcher watcher;

    @AfterEach
    void cleanup() {
        if (watcher != null) {
            watcher.stop();
        }
    }

    private boolean waitFor(BooleanSupplier cond, long maxMs) {
        long deadline = System.currentTimeMillis() + maxMs;
        while (System.currentTimeMillis() < deadline) {
            try {
                if (cond.getAsBoolean()) return true;
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return cond.getAsBoolean();
    }

    private PollingWatcher startWatcher(long intervalMs) throws IOException {
        Path statusDir = workspaceDir.resolve("status");
        Files.createDirectories(statusDir);
        TeamMonitorProperties props = new TeamMonitorProperties();
        props.setStatusDir(statusDir.toString());
        props.setWorkspaceDir(workspaceDir.toString());
        TeamMetadata meta = new TeamMetadata(workspaceDir.resolve("teams.json"));
        meta.reload();
        TeamStatusReader reader = new TeamStatusReader(props, meta);
        watcher = new PollingWatcher(statusDir, workspaceDir.resolve("teams.json"), intervalMs, reader, meta);
        watcher.start();
        return watcher;
    }

    @Test
    void pollingWatcher_picksUpNewTeamFromTick() throws IOException {
        startWatcher(200);
        List<String> notified = new CopyOnWriteArrayList<>();
        watcher.subscribe(notified::add);

        Files.writeString(workspaceDir.resolve("status/alpha.status"), "team=alpha\n");

        assertThat(waitFor(() -> notified.contains("alpha"), 5000)).isTrue();
    }

    @Test
    void pollingWatcher_reloadsTeamsJsonOnMtimeChange() throws IOException, InterruptedException {
        Path teamsJson = workspaceDir.resolve("teams.json");
        Files.writeString(teamsJson, "{\"schema_version\":1,\"teams\":{}}");

        startWatcher(200);
        AtomicInteger metaChangeCount = new AtomicInteger(0);
        watcher.subscribeMetaChange(metaChangeCount::incrementAndGet);

        Thread.sleep(1100);
        Files.writeString(teamsJson,
                "{\"schema_version\":1,\"teams\":{\"x\":{\"emoji\":\"X\",\"sort_order\":1,\"label\":\"X\"}}}");

        assertThat(waitFor(() -> metaChangeCount.get() >= 1, 5000)).isTrue();
    }

    @Test
    void pollingWatcher_emptyDir_idlesWithoutError() throws IOException, InterruptedException {
        startWatcher(100);
        AtomicInteger count = new AtomicInteger(0);
        watcher.subscribe(team -> count.incrementAndGet());

        Thread.sleep(800);
        assertThat(count.get()).isZero();
        assertThat(watcher.isAlive()).isTrue();
        assertThat(watcher.lastError()).isNull();
        assertThat(watcher.mode()).isEqualTo("polling");
    }

    @Test
    void pollingWatcher_deleteTriggersTeamDeletedCallback() throws IOException, InterruptedException {
        // FR-3-DEL: 삭제 시 onTeamDeleted 콜백 발화 + lastMtime stale 정리
        Path statusDir = workspaceDir.resolve("status");
        Files.createDirectories(statusDir);
        Files.writeString(statusDir.resolve("foo.status"), "team=foo\n");

        startWatcher(200);

        List<String> deletedCalls = new CopyOnWriteArrayList<>();
        watcher.subscribeTeamDeleted(deletedCalls::add);

        // foo 가 인식될 때까지 첫 tick 대기
        Thread.sleep(300);
        Files.delete(statusDir.resolve("foo.status"));

        assertThat(waitFor(() -> deletedCalls.contains("foo"), 3000)).isTrue();
    }

    @Test
    void pollingWatcher_twoConsecutiveModifies_bothPicked() throws IOException, InterruptedException {
        // S5-03 / T-B-02: 1초 이상 간격으로 2회 modify → 모두 인지
        Path statusDir = workspaceDir.resolve("status");
        Files.createDirectories(statusDir);
        Files.writeString(statusDir.resolve("alpha.status"), "team=alpha\nstate=대기\n");

        startWatcher(200);
        AtomicInteger count = new AtomicInteger(0);
        watcher.subscribe(team -> { if ("alpha".equals(team)) count.incrementAndGet(); });

        Thread.sleep(1100);
        Files.writeString(statusDir.resolve("alpha.status"), "team=alpha\nstate=진행중\n");
        assertThat(waitFor(() -> count.get() >= 1, 3000)).isTrue();
        int afterFirst = count.get();

        Thread.sleep(1100);
        Files.writeString(statusDir.resolve("alpha.status"), "team=alpha\nstate=완료\n");
        assertThat(waitFor(() -> count.get() > afterFirst, 3000)).isTrue();
    }
}
