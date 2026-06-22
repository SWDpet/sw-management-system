package com.swmanager.system.dto.workplan;

import com.swmanager.system.domain.workplan.PjtSchedule;

/**
 * 사업수행계획서 조회(/api/plan/{projId})의 schedules 행 dto (월별 m01~m12 마킹).
 *
 * 기존 DocumentController.getPlanData 의 컨트롤러-로컬 HashMap 응답조립을 record 로 대체한다
 * (§6-4 document-plan-rows-dto). 키셋·값 동치로 무손실. m01-m12 는 Boolean(엔티티 기본 false). null 포함.
 */
public record PlanScheduleRow(Long id, String processName,
                              Boolean m01, Boolean m02, Boolean m03, Boolean m04, Boolean m05, Boolean m06,
                              Boolean m07, Boolean m08, Boolean m09, Boolean m10, Boolean m11, Boolean m12,
                              String remark) {

    public static PlanScheduleRow from(PjtSchedule s) {
        return new PlanScheduleRow(s.getId(), s.getProcessName(),
                s.getM01(), s.getM02(), s.getM03(), s.getM04(), s.getM05(), s.getM06(),
                s.getM07(), s.getM08(), s.getM09(), s.getM10(), s.getM11(), s.getM12(),
                s.getRemark());
    }
}
