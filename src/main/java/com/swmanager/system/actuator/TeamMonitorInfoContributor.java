package com.swmanager.system.actuator;

import com.swmanager.system.config.TeamMonitorProperties;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * /actuator/info 에 team-monitor 운영 키 노출.
 * (sprint team-monitor-dashboard, 개발계획 Step 5-4 — codex v2 보완 S2)
 */
@Component
public class TeamMonitorInfoContributor implements InfoContributor {

    private final TeamMonitorProperties props;

    public TeamMonitorInfoContributor(TeamMonitorProperties props) {
        this.props = props;
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
        builder.withDetail("teammonitor", root);
    }
}
