package com.swmanager.system.dto.workplan;

import java.util.List;

/**
 * 사업수행계획서 조회(/api/plan/{projId}) 응답 래퍼 dto — 7키 고정.
 *
 * 기존 DocumentController.getPlanData 의 컨트롤러-로컬 HashMap 래퍼 응답조립을 record 로 대체한다
 * (§6-4 document-plan-rows-dto). 키셋·값 동치로 무손실. sw_pjt 4필드 + 3 목록. null 포함.
 */
public record PlanData(String projPurpose, String supportType, String scopeText, String inspectMethod,
                       List<PlanTargetRow> targets, List<PlanManpowerRow> manpowerPlans,
                       List<PlanScheduleRow> schedules) {
}
