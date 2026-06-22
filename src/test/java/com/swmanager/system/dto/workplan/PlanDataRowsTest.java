package com.swmanager.system.dto.workplan;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swmanager.system.domain.workplan.PjtManpowerPlan;
import com.swmanager.system.domain.workplan.PjtSchedule;
import com.swmanager.system.domain.workplan.PjtTarget;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PlanData / PlanTargetRow / PlanManpowerRow / PlanScheduleRow 직렬화 골든 테스트
 * (document-plan-rows-dto §6-4).
 *
 * DocumentController.getPlanData 의 컨트롤러-로컬 HashMap 응답조립을 record 로 바꾸면서
 * 응답 JSON 무손실(7키 래퍼 + 중첩 행 키셋·값·null·LocalDate toString·Boolean)을 고정한다.
 * 현행 HashMap 이라 키순서 비결정 → JsonNode tree 동치.
 */
class PlanDataRowsTest {

    private final ObjectMapper om = new ObjectMapper();

    private void assertTreeEqual(Object record, Object legacy) {
        JsonNode a = om.valueToTree(record);
        JsonNode b = om.valueToTree(legacy);
        assertThat(a).isEqualTo(b);
    }

    @Test
    void planTargetRow_matchesLegacy() {
        PjtTarget t = new PjtTarget();
        t.setId(1L);
        t.setProductName("UPIS 라이선스");
        t.setQty(3);
        Map<String, Object> m = new HashMap<>();
        m.put("id", t.getId());
        m.put("productName", t.getProductName());
        m.put("qty", t.getQty());
        assertTreeEqual(PlanTargetRow.from(t), m);
    }

    @Test
    void planManpowerRow_matchesLegacy_dateToString_andNull() {
        PjtManpowerPlan mp = new PjtManpowerPlan();
        mp.setId(2L);
        mp.setStepName("설계");
        mp.setStartDt(LocalDate.of(2026, 6, 1));
        // endDt null
        mp.setGradeSpecial(1);
        mp.setGradeHigh(2);
        mp.setRemark("비고");
        Map<String, Object> m = new HashMap<>();
        m.put("id", mp.getId());
        m.put("stepName", mp.getStepName());
        m.put("startDt", mp.getStartDt() != null ? mp.getStartDt().toString() : null);
        m.put("endDt", mp.getEndDt() != null ? mp.getEndDt().toString() : null);
        m.put("gradeSpecial", mp.getGradeSpecial());
        m.put("gradeHigh", mp.getGradeHigh());
        m.put("gradeMid", mp.getGradeMid());
        m.put("gradeLow", mp.getGradeLow());
        m.put("funcHigh", mp.getFuncHigh());
        m.put("funcMid", mp.getFuncMid());
        m.put("funcLow", mp.getFuncLow());
        m.put("remark", mp.getRemark());
        assertTreeEqual(PlanManpowerRow.from(mp), m);

        JsonNode j = om.valueToTree(PlanManpowerRow.from(mp));
        assertThat(j.get("startDt").asText()).isEqualTo("2026-06-01");
        assertThat(j.get("endDt").isNull()).isTrue();
        assertThat(j.size()).isEqualTo(12);
    }

    @Test
    void planScheduleRow_matchesLegacy_booleans() {
        PjtSchedule s = new PjtSchedule();
        s.setId(3L);
        s.setProcessName("착수");
        s.setM01(true);
        s.setM06(true);
        s.setRemark("r");
        Map<String, Object> m = new HashMap<>();
        m.put("id", s.getId());
        m.put("processName", s.getProcessName());
        m.put("m01", s.getM01()); m.put("m02", s.getM02()); m.put("m03", s.getM03());
        m.put("m04", s.getM04()); m.put("m05", s.getM05()); m.put("m06", s.getM06());
        m.put("m07", s.getM07()); m.put("m08", s.getM08()); m.put("m09", s.getM09());
        m.put("m10", s.getM10()); m.put("m11", s.getM11()); m.put("m12", s.getM12());
        m.put("remark", s.getRemark());
        assertTreeEqual(PlanScheduleRow.from(s), m);

        JsonNode j = om.valueToTree(PlanScheduleRow.from(s));
        assertThat(j.size()).isEqualTo(15);
        assertThat(j.get("m01").asBoolean()).isTrue();
        assertThat(j.get("m02").asBoolean()).isFalse();
    }

    @Test
    void planData_wrapper_7keys_emptyLists() {
        PlanData pd = new PlanData("목적", "상주", "범위", "점검", List.of(), List.of(), List.of());
        Map<String, Object> m = new HashMap<>();
        m.put("projPurpose", "목적");
        m.put("supportType", "상주");
        m.put("scopeText", "범위");
        m.put("inspectMethod", "점검");
        m.put("targets", List.of());
        m.put("manpowerPlans", List.of());
        m.put("schedules", List.of());
        assertTreeEqual(pd, m);

        JsonNode j = om.valueToTree(pd);
        assertThat(j.size()).isEqualTo(7);
        assertThat(j.get("targets").isArray()).isTrue();

        // null 래퍼 필드 보존
        PlanData empty = new PlanData(null, null, null, null, List.of(), List.of(), List.of());
        JsonNode je = om.valueToTree(empty);
        assertThat(je.has("projPurpose")).isTrue();
        assertThat(je.get("projPurpose").isNull()).isTrue();
    }
}
