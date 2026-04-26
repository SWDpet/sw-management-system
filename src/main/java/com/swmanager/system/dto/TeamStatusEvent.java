package com.swmanager.system.dto;

import java.util.List;

/**
 * SSE 페이로드 (sprint team-monitor-dashboard, 기획 §FR-5-d / 개발계획 Step 1-3).
 *
 * - Snapshot: 연결 직후 1회 (event: snapshot).
 * - Update:   단일 팀 상태 변경 시 (event: update).
 * - Heartbeat 는 별도 record 없이 컨트롤러에서 직접 직렬화.
 */
public sealed interface TeamStatusEvent {

    int schemaVersion();

    String serverTime();

    record Snapshot(
            int schemaVersion,
            String serverTime,
            List<TeamStatus> teams,
            List<TimelineEntry> timeline
    ) implements TeamStatusEvent {}

    record Update(
            int schemaVersion,
            String serverTime,
            TeamStatus team,
            TimelineEntry timelineEntry
    ) implements TeamStatusEvent {}
}
