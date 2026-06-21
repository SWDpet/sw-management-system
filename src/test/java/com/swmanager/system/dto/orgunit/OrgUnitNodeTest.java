package com.swmanager.system.dto.orgunit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swmanager.system.domain.OrgUnit;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * OrgUnitNode 직렬화 골든 테스트 (orgunit-node-dto §6-4).
 *
 * 기존 OrgUnitService.toDto 의 LinkedHashMap 응답조립을 record 로 바꾸면서, 평면 노드 응답 JSON 이
 * 무손실(키셋·값·키순서·null 포함)인지 고정한다. snake_case 키 + @JsonPropertyOrder 순서 보존을 확인.
 */
class OrgUnitNodeTest {

    private final ObjectMapper om = new ObjectMapper();

    /** 치환 전 toDto() 가 만들던 LinkedHashMap 을 그대로 복제(골든 기준). */
    private Map<String, Object> legacy(OrgUnit u) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("unit_id", u.getUnitId());
        m.put("name", u.getName());
        m.put("unit_type", u.getUnitType());
        m.put("parent_id", u.getParent() != null ? u.getParent().getUnitId() : null);
        m.put("sort_order", u.getSortOrder());
        return m;
    }

    private OrgUnit withParent() {
        OrgUnit parent = new OrgUnit();
        parent.setUnitId(99L);
        OrgUnit u = new OrgUnit();
        u.setUnitId(7L);
        u.setName("SW지원팀");
        u.setUnitType("TEAM");
        u.setParent(parent);
        u.setSortOrder(10);
        return u;
    }

    @Test
    void treeAndStringEqualLegacy_withParent() throws Exception {
        OrgUnit u = withParent();
        JsonNode dto = om.valueToTree(OrgUnitNode.from(u));
        JsonNode legacy = om.valueToTree(legacy(u));
        assertThat(dto).isEqualTo(legacy);                                  // 키셋·값
        assertThat(om.writeValueAsString(OrgUnitNode.from(u)))
                .isEqualTo(om.writeValueAsString(legacy(u)));               // 키순서(@JsonPropertyOrder)
        // snake_case 키 보존
        assertThat(dto.has("unit_id")).isTrue();
        assertThat(dto.has("unit_type")).isTrue();
        assertThat(dto.has("parent_id")).isTrue();
        assertThat(dto.has("sort_order")).isTrue();
        assertThat(dto.get("parent_id").asLong()).isEqualTo(99L);
    }

    @Test
    void parentNull_parentIdNull() {
        OrgUnit u = withParent();
        u.setParent(null);
        JsonNode j = om.valueToTree(OrgUnitNode.from(u));
        assertThat(j.has("parent_id")).isTrue();
        assertThat(j.get("parent_id").isNull()).isTrue();
        JsonNode legacyJson = om.valueToTree(legacy(u));
        assertThat(j).isEqualTo(legacyJson);
    }

    @Test
    void nullFields_includedAndKeysetFive() {
        OrgUnit u = new OrgUnit();
        u.setUnitId(1L);                          // name/unitType null, parent null, sortOrder=0(필드 기본값)
        JsonNode j = om.valueToTree(OrgUnitNode.from(u));
        assertThat(j.size()).isEqualTo(5);
        assertThat(j.has("name")).isTrue();
        assertThat(j.get("name").isNull()).isTrue();
        assertThat(j.get("unit_type").isNull()).isTrue();
        JsonNode legacyJson = om.valueToTree(legacy(u));
        assertThat(j).isEqualTo(legacyJson);
    }
}
