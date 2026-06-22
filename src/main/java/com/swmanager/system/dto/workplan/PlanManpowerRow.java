package com.swmanager.system.dto.workplan;

import com.swmanager.system.domain.workplan.PjtManpowerPlan;

/**
 * 사업수행계획서 조회(/api/plan/{projId})의 manpowerPlans 행 dto.
 *
 * 기존 DocumentController.getPlanData 의 컨트롤러-로컬 HashMap 응답조립을 record 로 대체한다
 * (§6-4 document-plan-rows-dto). 키셋·값 동치로 무손실. startDt/endDt 는 현행과 동일하게
 * LocalDate → toString()(null 보존). null 포함.
 */
public record PlanManpowerRow(Long id, String stepName, String startDt, String endDt,
                              Integer gradeSpecial, Integer gradeHigh, Integer gradeMid, Integer gradeLow,
                              Integer funcHigh, Integer funcMid, Integer funcLow, String remark) {

    public static PlanManpowerRow from(PjtManpowerPlan mp) {
        return new PlanManpowerRow(
                mp.getId(), mp.getStepName(),
                mp.getStartDt() != null ? mp.getStartDt().toString() : null,
                mp.getEndDt() != null ? mp.getEndDt().toString() : null,
                mp.getGradeSpecial(), mp.getGradeHigh(), mp.getGradeMid(), mp.getGradeLow(),
                mp.getFuncHigh(), mp.getFuncMid(), mp.getFuncLow(), mp.getRemark());
    }
}
