package com.swmanager.system.dto.workplan;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swmanager.system.dto.WorkPlanDTO;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CalendarEvent / ExtendedProps 직렬화 골든 테스트 (doclookup-workplan-rows-dto §6-4, C3).
 *
 * 기존 WorkPlanController 의 컨트롤러-로컬 HashMap(+ extendedProps=Map.of) 응답조립을 record 로
 * 바꾸면서 /api/events 응답 JSON 이 무손실(top-level 5키 + nested extendedProps 8키, ""/0 fallback)
 * 인지 고정한다. 현행이 HashMap 이라 키순서 비결정 → nested JsonNode tree 동치만 검증.
 */
class CalendarEventTest {

    private final ObjectMapper om = new ObjectMapper();

    /** 현행 컨트롤러 로직을 그대로 복제한 레거시 응답(검증 기준). */
    private Map<String, Object> legacy(WorkPlanDTO p) {
        Map<String, Object> event = new HashMap<>();
        event.put("id", p.getPlanId());
        event.put("title", p.getTitle());
        event.put("start", p.getStartDate());
        event.put("end", p.getEndDate());
        event.put("color", p.getColor());
        event.put("extendedProps", Map.of(
                "planType", p.getPlanType() != null ? p.getPlanType() : "",
                "planTypeLabel", WorkPlanDTO.getTypeLabel(p.getPlanType()),
                "status", p.getStatus() != null ? p.getStatus() : "",
                "statusLabel", WorkPlanDTO.getStatusLabel(p.getStatus()),
                "assigneeName", p.getAssigneeName() != null ? p.getAssigneeName() : "",
                "infraName", p.getTargetLabel(),
                "processStep", p.getProcessStep() != null ? p.getProcessStep() : 0,
                "stepLabel", WorkPlanDTO.getStepLabel(p.getProcessStep())
        ));
        return event;
    }

    private WorkPlanDTO fullDto() {
        WorkPlanDTO p = new WorkPlanDTO();
        p.setPlanId(5);
        p.setTitle("UPIS 정기점검");
        p.setStartDate("2026-06-22");
        p.setEndDate("2026-06-23");
        p.setColor("#ff9800");
        p.setPlanType("INSPECT");
        p.setStatus("IN_PROGRESS");
        p.setAssigneeName("박욱진");
        p.setProcessStep(5);
        p.setRegionDistNm("강릉시");
        p.setTargetSysNm("도시계획정보체계");
        return p;
    }

    @Test
    void calendarEvent_full_matchesLegacy() {
        WorkPlanDTO p = fullDto();
        JsonNode a = om.valueToTree(CalendarEvent.from(p));
        JsonNode b = om.valueToTree(legacy(p));
        assertThat(a).isEqualTo(b);
    }

    @Test
    void calendarEvent_nulls_useFallback() {
        // planType/status/assigneeName null → ""; processStep null → 0; (Map.of 가 null 거부하던 자리)
        WorkPlanDTO p = new WorkPlanDTO();
        p.setPlanId(9);
        // title/start/end/color null → top-level 키는 그대로 null 보존
        JsonNode a = om.valueToTree(CalendarEvent.from(p));
        JsonNode b = om.valueToTree(legacy(p));
        assertThat(a).isEqualTo(b);

        JsonNode ext = a.get("extendedProps");
        assertThat(ext.size()).isEqualTo(8);
        assertThat(ext.get("planType").asText()).isEmpty();
        assertThat(ext.get("status").asText()).isEmpty();
        assertThat(ext.get("assigneeName").asText()).isEmpty();
        assertThat(ext.get("processStep").asInt()).isZero();

        // top-level: title 등 null 키 보존
        assertThat(a.has("title")).isTrue();
        assertThat(a.get("title").isNull()).isTrue();
    }
}
