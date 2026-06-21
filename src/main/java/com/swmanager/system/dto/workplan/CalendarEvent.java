package com.swmanager.system.dto.workplan;

import com.swmanager.system.dto.WorkPlanDTO;

/**
 * /api/events (FullCalendar 이벤트) 응답 dto — top-level + nested {@link ExtendedProps}.
 *
 * 기존 WorkPlanController 의 컨트롤러-로컬 HashMap(+ extendedProps=Map.of) 응답조립을 record 로
 * 대체한다(§6-4 doclookup-workplan-rows-dto, C3). 키셋·값 동치로 무손실. camelCase 키=컴포넌트명.
 *
 * <p>현행 extendedProps 는 {@code Map.of(...)}(null 금지)라 각 키에 {@code ""}/{@code 0} fallback 을
 * 적용 중이었다 — {@link ExtendedProps#from} 에 동일 fallback 을 복제해 wire 를 보존한다.
 */
public record CalendarEvent(Integer id, String title, String start, String end,
                            String color, ExtendedProps extendedProps) {

    public static CalendarEvent from(WorkPlanDTO p) {
        return new CalendarEvent(p.getPlanId(), p.getTitle(), p.getStartDate(),
                p.getEndDate(), p.getColor(), ExtendedProps.from(p));
    }

    /** FullCalendar extendedProps (현행 Map.of 8키와 동치, null→""/0 fallback 보존). */
    public record ExtendedProps(String planType, String planTypeLabel, String status,
                                String statusLabel, String assigneeName, String infraName,
                                Integer processStep, String stepLabel) {

        public static ExtendedProps from(WorkPlanDTO p) {
            return new ExtendedProps(
                    p.getPlanType() != null ? p.getPlanType() : "",
                    WorkPlanDTO.getTypeLabel(p.getPlanType()),
                    p.getStatus() != null ? p.getStatus() : "",
                    WorkPlanDTO.getStatusLabel(p.getStatus()),
                    p.getAssigneeName() != null ? p.getAssigneeName() : "",
                    p.getTargetLabel(),
                    p.getProcessStep() != null ? p.getProcessStep() : 0,
                    WorkPlanDTO.getStepLabel(p.getProcessStep()));
        }
    }
}
