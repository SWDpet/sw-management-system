package com.swmanager.system.dto.workplan;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swmanager.system.domain.SwProject;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SystemAllRow / BatchTargetRow 직렬화 골든 테스트 (document-batch-rows-dto §6-4).
 *
 * DocumentController.getAllSystemsForYear / getBatchTargets 의 컨트롤러-로컬 HashMap 응답조립을 record 로
 * 바꾸면서 응답 JSON 무손실(키셋·값·null·LocalDate toString)을 고정한다. JsonNode tree 동치.
 */
class BatchRowsTest {

    private final ObjectMapper om = new ObjectMapper();

    private void assertTreeEqual(Object record, Object legacy) {
        JsonNode a = om.valueToTree(record);
        JsonNode b = om.valueToTree(legacy);
        assertThat(a).isEqualTo(b);
    }

    @Test
    void systemAllRow_matchesLegacy() {
        Map<String, Object> m = new HashMap<>();
        m.put("sysNmEn", "UPIS");
        m.put("sysNm", "도시계획정보체계");
        assertTreeEqual(new SystemAllRow("UPIS", "도시계획정보체계"), m);
        assertThat(om.valueToTree(new SystemAllRow("UPIS", "도시계획정보체계")).size()).isEqualTo(2);
    }

    @Test
    void batchTargetRow_matchesLegacy_dates_andNull() {
        SwProject p = new SwProject();
        p.setProjId(100L);
        p.setProjNm("UPIS 유지관리");
        p.setSysNmEn("UPIS");
        p.setCityNm("강원특별자치도");
        p.setDistNm("강릉시");
        p.setOrgNm("강릉시청");
        p.setContAmt(50000000L);
        p.setContDt(LocalDate.of(2026, 3, 1));
        p.setStartDt(LocalDate.of(2026, 4, 1));
        // endDt null
        p.setClient("강릉시");

        Map<String, Object> m = new HashMap<>();
        m.put("projId", p.getProjId());
        m.put("projNm", p.getProjNm());
        m.put("sysNmEn", p.getSysNmEn());
        m.put("cityNm", p.getCityNm());
        m.put("distNm", p.getDistNm());
        m.put("orgNm", p.getOrgNm());
        m.put("contAmt", p.getContAmt());
        m.put("contDt", p.getContDt() != null ? p.getContDt().toString() : null);
        m.put("startDt", p.getStartDt() != null ? p.getStartDt().toString() : null);
        m.put("endDt", p.getEndDt() != null ? p.getEndDt().toString() : null);
        m.put("client", p.getClient());
        assertTreeEqual(BatchTargetRow.from(p), m);

        JsonNode j = om.valueToTree(BatchTargetRow.from(p));
        assertThat(j.size()).isEqualTo(11);
        assertThat(j.get("contDt").asText()).isEqualTo("2026-03-01");
        assertThat(j.get("endDt").isNull()).isTrue();
    }

    @Test
    void batchTargetRow_nullScalars_preserved() {
        SwProject p = new SwProject();
        p.setProjId(101L);   // 나머지 null
        JsonNode j = om.valueToTree(BatchTargetRow.from(p));
        assertThat(j.size()).isEqualTo(11);
        assertThat(j.has("projNm")).isTrue();
        assertThat(j.get("projNm").isNull()).isTrue();
        assertThat(j.get("contAmt").isNull()).isTrue();
    }
}
