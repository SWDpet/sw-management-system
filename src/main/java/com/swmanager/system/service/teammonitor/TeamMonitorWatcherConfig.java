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
 * (sprint team-monitor-wildcard-watcher: 두 디렉토리 + TeamMetadata 주입)
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
    public TeamStatusWatcher teamStatusWatcher(TeamMonitorProperties props,
                                               TeamStatusReader reader,
                                               TeamMetadata teamMetadata) {
        Path statusDir = props.getStatusPath();
        Path workspaceDir = props.getWorkspacePath();
        Path teamsJsonPath = props.getTeamsJsonPath();
        String mode = props.getWatcher().getMode();
        long pollMs = props.getWatcher().getPollingIntervalMs();

        TeamStatusWatcher watcher;
        switch (mode == null ? "auto" : mode.toLowerCase()) {
            case "nio" -> watcher = new JavaNioWatcher(statusDir, workspaceDir, reader, teamMetadata);
            case "polling" -> watcher = new PollingWatcher(statusDir, teamsJsonPath, pollMs, reader, teamMetadata);
            default -> watcher = new JavaNioWatcher(statusDir, workspaceDir, reader, teamMetadata);
        }
        try {
            watcher.start();
        } catch (Exception e) {
            // NIO 실패 시 polling 으로 한 번 더 fallback
            if (watcher instanceof JavaNioWatcher) {
                log.warn("NIO 실패 → polling fallback: {}", e.getMessage());
                watcher = new PollingWatcher(statusDir, teamsJsonPath, pollMs, reader, teamMetadata);
                watcher.start();
            } else {
                throw e;
            }
        }
        return watcher;
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
