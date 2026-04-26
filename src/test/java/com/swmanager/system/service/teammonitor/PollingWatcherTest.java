package com.swmanager.system.service.teammonitor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class PollingWatcherTest {

    @TempDir Path tmp;

    @Test
    void detectsFileChange_within2seconds() throws Exception {
        PollingWatcher w = new PollingWatcher(tmp, 100L);
        try {
            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<String> changedTeam = new AtomicReference<>();
            w.subscribe(team -> { changedTeam.set(team); latch.countDown(); });
            w.start();

            // 신규 파일 생성 → 콜백 발화
            Files.writeString(tmp.resolve("planner.status"),
                    "team=planner\nstate=진행중\nprogress=10\ntask=t\nupdated=1\n",
                    StandardCharsets.UTF_8);

            assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
            assertThat(changedTeam.get()).isEqualTo("planner");
            assertThat(w.isAlive()).isTrue();
            assertThat(w.lastEventAt()).isNotNull();
            assertThat(w.mode()).isEqualTo("polling");
        } finally {
            w.stop();
        }
    }
}
