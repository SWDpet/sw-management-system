package com.swmanager.system.config;

import java.nio.file.Path;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * team-monitor 설정 키 (sprint team-monitor-dashboard, 개발계획 §NFR-4-b / Step 5-3).
 *
 * 키 추가/변경 시 application.properties + 기획서 §NFR-4-b 표 동시 갱신.
 * 메인 앱 SwManagerApplication 의 @EnableConfigurationProperties 에 등록되어야 바인딩됨.
 *
 * sprint team-monitor-wildcard-watcher (R-11 / S4-01): workspaceDir property 추가.
 * statusDir 는 기존 기본값 유지 (후방 호환). getStatusPath()/getTeamsJsonPath() 는
 * workspaceDir 기반 신규 헬퍼.
 */
@ConfigurationProperties(prefix = "teammonitor")
public class TeamMonitorProperties {

    private boolean enabled = true;
    private String statusDir = ".team-workspace/status";
    private String workspaceDir = ".team-workspace";
    private final Watcher watcher = new Watcher();
    private final Sse sse = new Sse();
    private final Timeline timeline = new Timeline();
    private final Security security = new Security();

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getStatusDir() { return statusDir; }
    public void setStatusDir(String statusDir) { this.statusDir = statusDir; }

    public String getWorkspaceDir() { return workspaceDir; }
    public void setWorkspaceDir(String workspaceDir) { this.workspaceDir = workspaceDir; }

    /** workspaceDir 절대경로. R-11 보완 — 시작 로그에 절대경로 표기 권장. */
    public Path getWorkspacePath() { return Path.of(workspaceDir); }
    /** statusDir 절대경로 — 기존 statusDir property 우선 (후방 호환), 미지정 시 workspaceDir/status. */
    public Path getStatusPath() {
        return (statusDir != null && !statusDir.isBlank())
                ? Path.of(statusDir)
                : getWorkspacePath().resolve("status");
    }
    /** teams.json 경로. */
    public Path getTeamsJsonPath() { return getWorkspacePath().resolve("teams.json"); }

    public Watcher getWatcher() { return watcher; }
    public Sse getSse() { return sse; }
    public Timeline getTimeline() { return timeline; }
    public Security getSecurity() { return security; }

    public static class Watcher {
        private String mode = "auto";              // auto|nio|polling
        private long pollingIntervalMs = 1000L;
        private long restartIntervalMs = 30_000L;

        public String getMode() { return mode; }
        public void setMode(String mode) { this.mode = mode; }
        public long getPollingIntervalMs() { return pollingIntervalMs; }
        public void setPollingIntervalMs(long v) { this.pollingIntervalMs = v; }
        public long getRestartIntervalMs() { return restartIntervalMs; }
        public void setRestartIntervalMs(long v) { this.restartIntervalMs = v; }
    }

    public static class Sse {
        private int maxEmitters = 20;
        private String overflowPolicy = "reject";   // reject|evict-oldest
        private long heartbeatIntervalMs = 30_000L;
        private long timeoutMs = 0L;                // 0=무제한
        private int rateLimitPerMin = 10;

        public int getMaxEmitters() { return maxEmitters; }
        public void setMaxEmitters(int v) { this.maxEmitters = v; }
        public String getOverflowPolicy() { return overflowPolicy; }
        public void setOverflowPolicy(String v) { this.overflowPolicy = v; }
        public long getHeartbeatIntervalMs() { return heartbeatIntervalMs; }
        public void setHeartbeatIntervalMs(long v) { this.heartbeatIntervalMs = v; }
        public long getTimeoutMs() { return timeoutMs; }
        public void setTimeoutMs(long v) { this.timeoutMs = v; }
        public int getRateLimitPerMin() { return rateLimitPerMin; }
        public void setRateLimitPerMin(int v) { this.rateLimitPerMin = v; }
    }

    public static class Timeline {
        private int size = 20;

        public int getSize() { return size; }
        public void setSize(int size) { this.size = size; }
    }

    public static class Security {
        private String csp = "";                    // 빈값=기본 CSP 사용
        private String allowedOrigins = "";         // csv. 빈값=동일출처만

        public String getCsp() { return csp; }
        public void setCsp(String csp) { this.csp = csp; }
        public String getAllowedOrigins() { return allowedOrigins; }
        public void setAllowedOrigins(String v) { this.allowedOrigins = v; }
    }
}
