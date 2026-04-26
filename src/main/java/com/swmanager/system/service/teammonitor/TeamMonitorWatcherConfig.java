package com.swmanager.system.service.teammonitor;

import com.swmanager.system.config.TeamMonitorProperties;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;

/**
 * TeamStatusWatcher 빈 팩토리 (sprint team-monitor-dashboard, 개발계획 Step 2-4).
 *
 * teammonitor.watcher.mode:
 *  - nio     : 직접 JavaNioWatcher.
 *  - polling : 직접 PollingWatcher.
 *  - auto    : NIO 시도 → 실패 시 polling fallback (기본).
 */
@Configuration
public class TeamMonitorWatcherConfig {

    private static final Logger log = LoggerFactory.getLogger(TeamMonitorWatcherConfig.class);

    @Bean(destroyMethod = "stop")
    public TeamStatusWatcher teamStatusWatcher(TeamMonitorProperties props) {
        Path dir = Path.of(props.getStatusDir());
        String mode = props.getWatcher().getMode();
        long pollMs = props.getWatcher().getPollingIntervalMs();

        TeamStatusWatcher watcher;
        switch (mode == null ? "auto" : mode.toLowerCase()) {
            case "nio" -> watcher = new JavaNioWatcher(dir);
            case "polling" -> watcher = new PollingWatcher(dir, pollMs);
            default -> watcher = createAuto(dir, pollMs);
        }
        try {
            watcher.start();
        } catch (Exception e) {
            // NIO 실패 시 polling 으로 한 번 더 fallback
            if (watcher instanceof JavaNioWatcher) {
                log.warn("NIO 실패 → polling fallback: {}", e.getMessage());
                watcher = new PollingWatcher(dir, pollMs);
                watcher.start();
            } else {
                throw e;
            }
        }
        return watcher;
    }

    private TeamStatusWatcher createAuto(Path dir, long pollMs) {
        // auto 모드: NIO 가 사용 가능한지 가벼운 시도. WatchService 생성 자체가 실패하면 polling.
        try {
            JavaNioWatcher nio = new JavaNioWatcher(dir);
            // start() 가 실패하면 createAuto 호출자에서 catch 하여 polling fallback.
            return nio;
        } catch (Throwable t) {
            log.warn("NIO watcher 생성 실패 → polling fallback: {}", t.getMessage());
            return new PollingWatcher(dir, pollMs);
        }
    }

    /**
     * 컨테이너 내부 facade — 운영자가 헬스에서 watcher 정지 감지 시 별도 트리거 가능.
     * 현재는 자동 재시작 미구현 (Phase 2 비범위, restart-interval-ms 활용은 TODO).
     */
    static class WatcherLifecycle {
        @PostConstruct void init() {}
        @PreDestroy void destroy() {}
    }
}
