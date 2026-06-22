package com.swmanager.system.dto.workplan;

import com.swmanager.system.domain.workplan.PjtTarget;

/**
 * 사업수행계획서 조회(/api/plan/{projId})의 targets 행 dto.
 *
 * 기존 DocumentController.getPlanData 의 컨트롤러-로컬 HashMap 응답조립을 record 로 대체한다
 * (§6-4 document-plan-rows-dto). 키셋·값 동치로 무손실. null 포함.
 */
public record PlanTargetRow(Long id, String productName, Integer qty) {

    public static PlanTargetRow from(PjtTarget t) {
        return new PlanTargetRow(t.getId(), t.getProductName(), t.getQty());
    }
}
