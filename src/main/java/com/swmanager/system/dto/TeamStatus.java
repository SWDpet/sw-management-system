package com.swmanager.system.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 가상 팀 상태 (sprint team-monitor-dashboard, 기획 §FR-2 / 개발계획 Step 1-3).
 *
 * updatedAt 은 nullable — 원본 status 파일의 updated 키가 누락/파싱 실패 시 null 유지.
 * (R-2-c "갱신: 미기록" UX 정합. now() 로 fallback 금지.)
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
public record TeamStatus(
        String team,
        String state,
        Integer progress,
        String task,
        String updatedAt
) {}
