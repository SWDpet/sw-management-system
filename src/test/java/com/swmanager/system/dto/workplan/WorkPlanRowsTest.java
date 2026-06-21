package com.swmanager.system.dto.workplan;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swmanager.system.domain.SigunguCode;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SggOptionRow / PjtOptionRow / WorkPlanStatusResult 직렬화 골든 테스트
 * (doclookup-workplan-rows-dto §6-4, C2).
 *
 * 기존 WorkPlanController 의 컨트롤러-로컬 HashMap 응답조립을 record 로 바꾸면서,
 * /api/sgg · /api/pjt-by-region · /api/status/{id}(200) 응답 JSON 무손실(키셋·값·타입)을 고정.
 * 현행이 HashMap 이라 키순서 비결정 → JsonNode tree 동치만 검증.
 */
class WorkPlanRowsTest {

    private final ObjectMapper om = new ObjectMapper();

    private void assertTreeEqual(Object record, Map<String, Object> legacy) {
        JsonNode a = om.valueToTree(record);
        JsonNode b = om.valueToTree(legacy);
        assertThat(a).isEqualTo(b);
    }

    @Test
    void sggOptionRow_unit_and_nonUnit() {
        // 일반 시군구(isUnit=false)
        SigunguCode s = new SigunguCode();
        s.setAdmSectC("51150");
        s.setSggNm("강릉시");
        s.setSidoNm("강원특별자치도");
        Map<String, Object> m = new HashMap<>();
        m.put("admSectC", s.getAdmSectC());
        m.put("sggNm", s.getSggNm());
        m.put("isUnit", s.getSggNm() != null && s.getSggNm().equals(s.getSidoNm()));
        assertTreeEqual(SggOptionRow.from(s), m);

        // self-행(본청/도청, isUnit=true)
        SigunguCode unit = new SigunguCode();
        unit.setAdmSectC("51000");
        unit.setSggNm("강원특별자치도");
        unit.setSidoNm("강원특별자치도");
        Map<String, Object> m2 = new HashMap<>();
        m2.put("admSectC", unit.getAdmSectC());
        m2.put("sggNm", unit.getSggNm());
        m2.put("isUnit", true);
        assertTreeEqual(SggOptionRow.from(unit), m2);
        assertThat(SggOptionRow.from(unit).isUnit()).isTrue();
    }

    @Test
    void pjtOptionRow_matchesLegacy() {
        Map<String, Object> m = new HashMap<>();
        m.put("projId", 100L);
        m.put("label", "2026 UPIS 유지관리");
        assertTreeEqual(new PjtOptionRow(100L, "2026 UPIS 유지관리"), m);

        JsonNode j = om.valueToTree(new PjtOptionRow(100L, "2026 UPIS 유지관리"));
        assertThat(j.size()).isEqualTo(2);
    }

    @Test
    void workPlanStatusResult_matchesLegacy() {
        Map<String, Object> m = new HashMap<>();
        m.put("success", true);
        m.put("planId", 5);
        m.put("status", "COMPLETED");
        m.put("statusLabel", "완료");
        assertTreeEqual(new WorkPlanStatusResult(true, 5, "COMPLETED", "완료"), m);

        JsonNode j = om.valueToTree(new WorkPlanStatusResult(true, 5, "COMPLETED", "완료"));
        assertThat(j.size()).isEqualTo(4);
        assertThat(j.get("success").asBoolean()).isTrue();
    }
}
