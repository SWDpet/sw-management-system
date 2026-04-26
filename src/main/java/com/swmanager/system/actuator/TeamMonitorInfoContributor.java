package com.swmanager.system.actuator;

import com.swmanager.system.config.TeamMonitorProperties;
import com.swmanager.system.service.teammonitor.TeamMetadata;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * /actuator/info 에 team-monitor 운영 키 노출.
 * (sprint team-monitor-dashboard, 개발계획 Step 5-4 — codex v2 보완 S2)
 *
 * <p><b>⚠ 보안 (R-12 / N13)</b>: 본 endpoint 는 ADMIN 권한 필수.
 * SecurityConfig 의 {@code /actuator/info} ROLE_ADMIN 매처와 함께 동작.
 * 비인가 노출 시 내부 fallback / degraded 상태 누설 위험.
 *
 * <p>sprint team-monitor-wildcard-watcher: teamMetadataFallback 필드 추가 (C2 / N10).
 */
@Component
public class TeamMonitorInfoContributor implements InfoContributor {

    private final TeamMonitorProperties props;
    private final TeamMetadata teamMetadata;

    public TeamMonitorInfoContributor(TeamMonitorProperties props, TeamMetadata teamMetadata) {
        this.props = props;
        this.teamMetadata = teamMetadata;
    }

    @Override
    public void contribute(Info.Builder builder) {
        Map<String, Object> timeline = new LinkedHashMap<>();
        timeline.put("size", props.getTimeline().getSize());

        Map<String, Object> sse = new LinkedHashMap<>();
        sse.put("maxEmitters", props.getSse().getMaxEmitters());
        sse.put("overflowPolicy", props.getSse().getOverflowPolicy());
        sse.put("rateLimitPerMin", props.getSse().getRateLimitPerMin());

        Map<String, Object> watcher = new LinkedHashMap<>();
        watcher.put("mode", props.getWatcher().getMode());
        watcher.put("pollingIntervalMs", props.getWatcher().getPollingIntervalMs());

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("timeline", timeline);
        root.put("sse", sse);
        root.put("watcher", watcher);
        // sprint team-monitor-wildcard-watcher (C2 / N10): degraded 상태 진단
        root.put("teamMetadataFallback", teamMetadata.isFallbackActive());
        if (teamMetadata.isFallbackActive() && teamMetadata.degradedReason() != null) {
            root.put("teamMetadataDegradedReason", teamMetadata.degradedReason());
        }
        builder.withDetail("teammonitor", root);
    }
}
