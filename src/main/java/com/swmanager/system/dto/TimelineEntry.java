package com.swmanager.system.dto;

/**
 * 팀 상태 변화 1건 (sprint team-monitor-dashboard, 기획 §FR-5-d / 개발계획 Step 1-3).
 *
 * id: 클라이언트 dedupe 용 ULID.
 * occurredAt: ISO-8601 with offset (Asia/Seoul). non-null — 변경 감지 시점에 항상 생성.
 */
public record TimelineEntry(
        String id,
        String team,
        String prevState,
        String newState,
        Integer prevProgress,
        Integer newProgress,
        String task,
        String occurredAt
) {}
