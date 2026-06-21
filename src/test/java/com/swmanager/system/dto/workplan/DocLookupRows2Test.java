package com.swmanager.system.dto.workplan;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swmanager.system.domain.Infra;
import com.swmanager.system.domain.SigunguCode;
import com.swmanager.system.domain.SwProject;
import com.swmanager.system.domain.SysMst;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RegionSigunguRow / SystemOptionRow / InfraFindResult / InfraNotFound / ProjectFilterRow
 * 직렬화 골든 테스트 (doclookup-workplan-rows-dto §6-4).
 *
 * 기존 DocumentLookupController 의 컨트롤러-로컬 Map 응답조립을 record 로 바꾸면서,
 * /api/region-sigungus · /api/systems-all · /api/infra-find · /api/projects 응답 JSON 이
 * 무손실(키셋·값·null 포함)인지 고정한다. 현행이 HashMap/LinkedHashMap 이라 키순서는 비결정
 * → JsonNode tree 동치만 검증.
 */
class DocLookupRows2Test {

    private final ObjectMapper om = new ObjectMapper();

    private void assertTreeEqual(Object record, Map<String, Object> legacy) {
        JsonNode a = om.valueToTree(record);
        JsonNode b = om.valueToTree(legacy);
        assertThat(a).isEqualTo(b);
    }

    @Test
    void regionSigunguRow_matchesLegacy() {
        SigunguCode s = new SigunguCode();
        s.setAdmSectC("51150");
        s.setSggNm("강릉시");
        s.setSidoNm("강원특별자치도");
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("admSectC", s.getAdmSectC());
        m.put("sggNm", s.getSggNm());
        m.put("sidoNm", s.getSidoNm());
        assertTreeEqual(RegionSigunguRow.from(s), m);
    }

    @Test
    void systemOptionRow_matchesLegacy() {
        SysMst s = new SysMst();
        s.setCd("UPIS");
        s.setNm("도시계획정보체계");
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("cd", s.getCd());
        m.put("nm", s.getNm());
        assertTreeEqual(SystemOptionRow.from(s), m);
    }

    @Test
    void infraFindResult_found_matchesLegacy() {
        Infra infra = new Infra();
        infra.setInfraId(42L);
        infra.setCityNm("강원특별자치도");
        infra.setDistNm("강릉시");
        infra.setSysNm("도시계획정보체계");
        infra.setSysNmEn("UPIS");
        Map<String, Object> m = new HashMap<>();
        m.put("found", true);
        m.put("infraId", infra.getInfraId());
        m.put("cityNm", infra.getCityNm());
        m.put("distNm", infra.getDistNm());
        m.put("sysNm", infra.getSysNm());
        m.put("sysNmEn", infra.getSysNmEn());
        assertTreeEqual(InfraFindResult.of(infra), m);
    }

    @Test
    void infraFindResult_found_keepsNullKeys() {
        // 현행 HashMap 은 발견 시 6키를 항상 put — sysNm 등 null 이어도 키 보존(NON_NULL 미부착).
        Infra infra = new Infra();
        infra.setInfraId(7L);   // 나머지 cityNm/distNm/sysNm/sysNmEn null
        JsonNode j = om.valueToTree(InfraFindResult.of(infra));
        assertThat(j.size()).isEqualTo(6);
        assertThat(j.has("sysNm")).isTrue();
        assertThat(j.get("sysNm").isNull()).isTrue();
        assertThat(j.get("found").asBoolean()).isTrue();
    }

    @Test
    void infraNotFound_singleKey() {
        JsonNode j = om.valueToTree(InfraNotFound.instance());
        assertThat(j.size()).isEqualTo(1);
        assertThat(j.get("found").asBoolean()).isFalse();

        Map<String, Object> m = new HashMap<>();
        m.put("found", false);
        assertTreeEqual(InfraNotFound.instance(), m);
    }

    @Test
    void projectFilterRow_matchesLegacy() {
        SwProject p = new SwProject();
        p.setProjId(100L);
        p.setYear(2026);
        p.setProjNm("UPIS 유지관리");
        p.setSysNm("도시계획정보체계");
        p.setSysNmEn("UPIS");
        p.setContAmt(50000000L);
        p.setCityNm("강원특별자치도");
        p.setDistNm("강릉시");
        Map<String, Object> m = new HashMap<>();
        m.put("projId", p.getProjId());
        m.put("year", p.getYear());
        m.put("projNm", p.getProjNm());
        m.put("sysNm", p.getSysNm());
        m.put("sysNmEn", p.getSysNmEn());
        m.put("contAmt", p.getContAmt());
        m.put("cityNm", p.getCityNm());
        m.put("distNm", p.getDistNm());
        assertTreeEqual(ProjectFilterRow.from(p), m);

        // null 값 키 보존(예: projNm null)
        SwProject empty = new SwProject();
        empty.setProjId(101L);
        JsonNode j = om.valueToTree(ProjectFilterRow.from(empty));
        assertThat(j.size()).isEqualTo(8);
        assertThat(j.has("projNm")).isTrue();
        assertThat(j.get("projNm").isNull()).isTrue();
    }
}
