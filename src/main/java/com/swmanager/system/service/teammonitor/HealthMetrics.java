package com.swmanager.system.service.teammonitor;

import java.time.Instant;

/**
 * HealthIndicator 호출 1회당 일관된 스냅샷.
 * (sprint team-monitor-dashboard, 개발계획 Step 5-1 — codex v2 보완 #3)
 *
 * Service 가 모든 필드를 atomic 하게 build 하여 반환 → indicator 가 부분 갱신으로
 * 불일치 키 노출하는 것 차단.
 */
public record HealthMetrics(
        // sse
        int currentEmitters,
        int maxEmitters,
        String overflowPolicy,
        long totalConnections,
        long totalRejected503,
        long totalRateLimited429,
        // watcher
        boolean watcherAlive,
        String watcherMode,
        Instant watcherLastEventAt,
        int restartCount,
        String lastError,
        // timeline
        int timelineSize,
        int timelineBuffered,
        Instant timelineLastEntryAt,
        // statusDir
        String statusDirPath,
        boolean statusDirReadable
) {}
