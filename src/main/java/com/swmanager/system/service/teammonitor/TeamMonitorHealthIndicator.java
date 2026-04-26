package com.swmanager.system.service.teammonitor;

import com.swmanager.system.dto.TeamStatus;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * /actuator/health/teamMonitor 응답 빌더.
 * (sprint team-monitor-dashboard, 개발계획 Step 5-1 — 기획 §4-9)
 *
 * 동시성: TeamMonitorService.snapshotMetrics() 가 record 1개로 atomic build.
 * 본 indicator 는 read-only 조회만 수행.
 */
@Component("teamMonitor")
public class TeamMonitorHealthIndicator extends AbstractHealthIndicator {

    private final TeamMonitorService service;

    public TeamMonitorHealthIndicator(TeamMonitorService service) {
        this.service = service;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) {
        HealthMetrics m = service.snapshotMetrics();

        Map<String, Object> watcher = new LinkedHashMap<>();
        watcher.put("mode", m.watcherMode());
        watcher.put("alive", m.watcherAlive());
        watcher.put("lastEventAt", iso(m.watcherLastEventAt()));
        watcher.put("restartCount", m.restartCount());
        if (m.lastError() != null) watcher.put("lastError", m.lastError());

        Map<String, Object> sse = new LinkedHashMap<>();
        sse.put("currentEmitters", m.currentEmitters());
        sse.put("maxEmitters", m.maxEmitters());
        sse.put("overflowPolicy", m.overflowPolicy());
        sse.put("totalConnections", m.totalConnections());
        sse.put("totalRejected503", m.totalRejected503());
        sse.put("totalRateLimited429", m.totalRateLimited429());

        Map<String, Object> timeline = new LinkedHashMap<>();
        timeline.put("size", m.timelineSize());
        timeline.put("buffered", m.timelineBuffered());
        timeline.put("lastEntryAt", iso(m.timelineLastEntryAt()));

        Map<String, Object> statusDir = new LinkedHashMap<>();
        statusDir.put("path", m.statusDirPath());
        statusDir.put("readable", m.statusDirReadable());
        statusDir.put("files", listFiles(m.statusDirPath()));

        builder.withDetail("watcher", watcher);
        builder.withDetail("sse", sse);
        builder.withDetail("timeline", timeline);
        builder.withDetail("statusDir", statusDir);

        if (!m.watcherAlive() || !m.statusDirReadable()) {
            builder.down();
        } else {
            builder.up();
        }
    }

    private static String iso(Instant i) {
        return i == null ? null : i.toString();
    }

    private static List<String> listFiles(String dirPath) {
        try {
            Path dir = Path.of(dirPath);
            if (!Files.isDirectory(dir)) return List.of();
            return Files.list(dir)
                    .filter(p -> p.getFileName().toString().endsWith(".status"))
                    .map(p -> p.getFileName().toString())
                    .sorted()
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return List.of();
        }
    }
}
