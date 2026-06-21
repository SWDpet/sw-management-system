package com.swmanager.system.dto.ops;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swmanager.system.domain.SigunguCode;
import com.swmanager.system.domain.SysMst;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * OpsDocController cascade 응답 record 2종 직렬화 골든 테스트 (opsdoc-cascade-rows-dto §6-4).
 *
 * 기존 컨트롤러-로컬 {@code HashMap} 응답조립을 record 로 바꾸면서 응답 JSON 의 키셋·값·null 포함을
 * 무손실 보존하는지 고정한다. 현행이 HashMap 이라 키순서는 비결정 → JsonNode tree 동치만 검증.
 * 특히 {@code isUnit} boolean 의 키 이름·값 보존을 확인한다.
 */
class OpsDocCascadeRowsTest {

    private final ObjectMapper om = new ObjectMapper();

    private void assertTreeEqual(Object record, Map<String, Object> legacy) {
        JsonNode a = om.valueToTree(record);
        JsonNode b = om.valueToTree(legacy);
        assertThat(a).isEqualTo(b);
    }

    private Map<String, Object> legacySgg(SigunguCode s) {
        Map<String, Object> m = new HashMap<>();
        m.put("admSectC", s.getAdmSectC());
        m.put("sggNm", s.getSggNm());
        m.put("isUnit", s.getSggNm() != null && s.getSggNm().equals(s.getSidoNm()));
        return m;
    }

    @Test
    void cascadeSggRow_matchesLegacy_isUnitTrueWhenSelfRow() {
        SigunguCode self = new SigunguCode();      // 본청/도청 self-행: sggNm == sidoNm → isUnit=true
        self.setAdmSectC("51000");
        self.setSidoNm("강원특별자치도");
        self.setSggNm("강원특별자치도");
        assertTreeEqual(CascadeSggRow.from(self), legacySgg(self));

        JsonNode j = om.valueToTree(CascadeSggRow.from(self));
        assertThat(j.has("isUnit")).isTrue();              // 키 이름 보존(unit 으로 깎이지 않음)
        assertThat(j.get("isUnit").isBoolean()).isTrue();
        assertThat(j.get("isUnit").asBoolean()).isTrue();
        assertThat(j.size()).isEqualTo(3);
    }

    @Test
    void cascadeSggRow_isUnitFalse_andSggNmNull() {
        SigunguCode sgg = new SigunguCode();        // 일반 시군구: sggNm != sidoNm → false
        sgg.setAdmSectC("51150");
        sgg.setSidoNm("강원특별자치도");
        sgg.setSggNm("강릉시");
        JsonNode j = om.valueToTree(CascadeSggRow.from(sgg));
        assertThat(j.get("isUnit").asBoolean()).isFalse();
        assertTreeEqual(CascadeSggRow.from(sgg), legacySgg(sgg));

        SigunguCode nullSgg = new SigunguCode();    // sggNm null → isUnit=false, sggNm 키 null 포함
        nullSgg.setAdmSectC("51999");
        nullSgg.setSidoNm("강원특별자치도");
        JsonNode jn = om.valueToTree(CascadeSggRow.from(nullSgg));
        assertThat(jn.get("isUnit").asBoolean()).isFalse();
        assertThat(jn.has("sggNm")).isTrue();
        assertThat(jn.get("sggNm").isNull()).isTrue();
        assertTreeEqual(CascadeSggRow.from(nullSgg), legacySgg(nullSgg));
    }

    @Test
    void cascadeSystemRow_matchesLegacy() {
        SysMst s = new SysMst();
        s.setCd("UPIS");
        s.setNm("도시계획정보체계");
        Map<String, Object> m = new HashMap<>();
        m.put("cd", s.getCd());
        m.put("nm", s.getNm());
        assertTreeEqual(CascadeSystemRow.from(s), m);

        SysMst empty = new SysMst();                // cd/nm null 포함
        JsonNode j = om.valueToTree(CascadeSystemRow.from(empty));
        assertThat(j.size()).isEqualTo(2);
        assertThat(j.has("nm")).isTrue();
        assertThat(j.get("nm").isNull()).isTrue();
    }
}
