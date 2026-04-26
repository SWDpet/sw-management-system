package com.swmanager.system.service.teammonitor;

import com.swmanager.system.config.TeamMonitorProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JavaNioWatcher 단위 테스트 — Spring 비의존 (S4-03).
 * (sprint team-monitor-wildcard-watcher §FR-7-b)
 *
 * NOTE: WatchService 는 OS 이벤트 큐 의존이라 약간의 지연 발생. waitFor 헬퍼로 polling.
 */
class JavaNioWatcherTest {

    @TempDir Path workspaceDir;

    private JavaNioWatcher watcher;

    @AfterEach
    void cleanup() {
        if (watcher != null) {
            watcher.stop();
        }
    }

    private JavaNioWatcher startWatcher() throws IOException {
        Path statusDir = workspaceDir.resolve("status");
        Files.createDirectories(statusDir);
        TeamMonitorProperties props = new TeamMonitorProperties();
        props.setStatusDir(statusDir.toString());
        props.setWorkspaceDir(workspaceDir.toString());
        TeamMetadata meta = new TeamMetadata(workspaceDir.resolve("teams.json"));
        meta.reload();
        TeamStatusReader reader = new TeamStatusReader(props, meta);
        watcher = new JavaNioWatcher(statusDir, workspaceDir, reader, meta);
        watcher.start();
        return watcher;
    }

    /** 조건 충족할 때까지 폴링 (최대 maxMs). */
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

    @Test
    void wildcardWatcher_picksUpNewTeamFile() throws IOException {
        startWatcher();
        List<String> notified = new CopyOnWriteArrayList<>();
        watcher.subscribe(notified::add);

        Files.writeString(workspaceDir.resolve("status/newteam.status"), "team=newteam\n");

        assertThat(waitFor(() -> notified.contains("newteam"), 5000)).isTrue();
    }

    @Test
    void wildcardWatcher_ignoresDotPrefixedTempFile() throws IOException, InterruptedException {
        startWatcher();
        List<String> notified = new CopyOnWriteArrayList<>();
        watcher.subscribe(notified::add);

        Files.writeString(workspaceDir.resolve("status/.tmp.status.XXXX"), "temp");
        Thread.sleep(800); // 변경 미발화 확인용 대기
        assertThat(notified).isEmpty();
    }

    @Test
    void wildcardWatcher_ignoresNonStatusSuffix() throws IOException, InterruptedException {
        startWatcher();
        List<String> notified = new CopyOnWriteArrayList<>();
        watcher.subscribe(notified::add);

        Files.writeString(workspaceDir.resolve("status/readme.txt"), "doc");
        Thread.sleep(800);
        assertThat(notified).isEmpty();
    }

    @Test
    void wildcardWatcher_reloadsTeamsJsonOnModify() throws IOException, InterruptedException {
        Path teamsJson = workspaceDir.resolve("teams.json");
        Files.writeString(teamsJson, "{\"schema_version\":1,\"teams\":{}}");

        startWatcher();
        AtomicInteger metaChangeCount = new AtomicInteger(0);
        watcher.subscribeMetaChange(metaChangeCount::incrementAndGet);

        Thread.sleep(50);
        Files.writeString(teamsJson,
                "{\"schema_version\":1,\"teams\":{\"a\":{\"emoji\":\"A\",\"sort_order\":1,\"label\":\"A\"}}}");

        assertThat(waitFor(() -> metaChangeCount.get() >= 1, 5000)).isTrue();
    }

    @Test
    void wildcardWatcher_reloadsTeamsJsonOnCreate() throws IOException {
        startWatcher();
        AtomicInteger metaChangeCount = new AtomicInteger(0);
        watcher.subscribeMetaChange(metaChangeCount::incrementAndGet);

        Files.writeString(workspaceDir.resolve("teams.json"), "{\"schema_version\":1,\"teams\":{}}");

        assertThat(waitFor(() -> metaChangeCount.get() >= 1, 5000)).isTrue();
    }

    @Test
    void wildcardWatcher_reloadsTeamsJsonOnDelete() throws IOException {
        Path teamsJson = workspaceDir.resolve("teams.json");
        Files.writeString(teamsJson, "{\"schema_version\":1,\"teams\":{}}");

        startWatcher();
        AtomicInteger metaChangeCount = new AtomicInteger(0);
        watcher.subscribeMetaChange(metaChangeCount::incrementAndGet);

        Files.delete(teamsJson);

        assertThat(waitFor(() -> metaChangeCount.get() >= 1, 5000)).isTrue();
    }

    @Test
    void wildcardWatcher_atomicReplaceTeamsJson_reloads() throws IOException {
        // S3-03 / S5-01: atomic replace (tmp 작성 → mv) → CREATE/DELETE 쌍으로 자동 커버
        Path teamsJson = workspaceDir.resolve("teams.json");
        Files.writeString(teamsJson, "{\"schema_version\":1,\"teams\":{}}");

        startWatcher();
        AtomicInteger metaChangeCount = new AtomicInteger(0);
        watcher.subscribeMetaChange(metaChangeCount::incrementAndGet);

        Path tmp = workspaceDir.resolve("teams.json.tmp");
        Files.writeString(tmp, "{\"schema_version\":1,\"teams\":{\"x\":{\"emoji\":\"X\",\"sort_order\":1,\"label\":\"X\"}}}");
        Files.move(tmp, teamsJson, StandardCopyOption.REPLACE_EXISTING);

        assertThat(waitFor(() -> metaChangeCount.get() >= 1, 5000)).isTrue();
    }

    @Test
    void wildcardWatcher_deleteTriggersTeamDeletedCallback() throws IOException {
        // C3-01-B: ENTRY_DELETE → onTeamDeleted 콜백
        Path statusDir = workspaceDir.resolve("status");
        Files.createDirectories(statusDir);
        Files.writeString(statusDir.resolve("foo.status"), "team=foo\n");

        startWatcher();
        List<String> deletedCalls = new CopyOnWriteArrayList<>();
        watcher.subscribeTeamDeleted(deletedCalls::add);

        Files.delete(statusDir.resolve("foo.status"));

        assertThat(waitFor(() -> deletedCalls.contains("foo"), 5000)).isTrue();
    }

    @Test
    void wildcardWatcher_normalizedPathComparison_works() throws IOException {
        // Z-01 + S1-02: 정규화 경로 비교
        startWatcher();
        List<String> notified = new CopyOnWriteArrayList<>();
        watcher.subscribe(notified::add);

        Files.writeString(workspaceDir.resolve("status/alpha.status"), "team=alpha\n");

        assertThat(waitFor(() -> notified.contains("alpha"), 5000)).isTrue();
        assertThat(watcher.isAlive()).isTrue();
    }

    @Test
    void wildcardWatcher_singleEvent_atLeastOneNotify() throws IOException {
        startWatcher();
        AtomicInteger count = new AtomicInteger(0);
        watcher.subscribe(team -> count.incrementAndGet());

        Files.writeString(workspaceDir.resolve("status/single.status"), "team=single\n");

        assertThat(waitFor(() -> count.get() >= 1, 5000)).isTrue();
        assertThat(watcher.isAlive()).isTrue();
    }
}
