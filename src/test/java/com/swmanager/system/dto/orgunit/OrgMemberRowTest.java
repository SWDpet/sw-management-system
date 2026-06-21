package com.swmanager.system.dto.orgunit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swmanager.system.domain.ops.Staff;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * OrgMemberRow 직렬화 골든 테스트 (orgunit-members-dto §6-4).
 *
 * 기존 OrgUnitService.getMembers 의 LinkedHashMap 응답조립을 record 로 바꾸면서, /api/org-units/{id}/members
 * 응답 JSON 이 무손실(키셋·값·키순서·null 포함·active boolean)인지 고정한다.
 */
class OrgMemberRowTest {

    private final ObjectMapper om = new ObjectMapper();

    private Map<String, Object> legacy(Staff s) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("staff_id", s.getStaffId());
        m.put("username", s.getName());
        m.put("position", s.getPosition());
        m.put("active", Boolean.TRUE.equals(s.getActive()));
        return m;
    }

    private Staff full() {
        Staff s = new Staff();
        s.setStaffId(7L);
        s.setName("박욱진");
        s.setPosition("대리");
        s.setActive(true);
        return s;
    }

    @Test
    void treeAndStringEqualLegacy() throws Exception {
        Staff s = full();
        JsonNode dto = om.valueToTree(OrgMemberRow.from(s));
        JsonNode legacy = om.valueToTree(legacy(s));
        assertThat(dto).isEqualTo(legacy);                                  // 키셋·값
        assertThat(om.writeValueAsString(OrgMemberRow.from(s)))
                .isEqualTo(om.writeValueAsString(legacy(s)));               // 키순서(LinkedHashMap 결정적)
        assertThat(dto.has("staff_id")).isTrue();                          // snake_case 키 보존
    }

    @Test
    void active_trueFalseNull() {
        assertThat(om.valueToTree(OrgMemberRow.from(full())).get("active").asBoolean()).isTrue();

        Staff off = full();
        off.setActive(false);
        assertThat(om.valueToTree(OrgMemberRow.from(off)).get("active").asBoolean()).isFalse();

        Staff nul = full();
        nul.setActive(null);                              // null → false(현행 Boolean.TRUE.equals)
        JsonNode j = om.valueToTree(OrgMemberRow.from(nul));
        assertThat(j.get("active").isBoolean()).isTrue();
        assertThat(j.get("active").asBoolean()).isFalse();
    }

    @Test
    void nullFields_includedAndKeysetFour() {
        Staff s = new Staff();
        s.setStaffId(8L);                                 // name/position null, active null
        JsonNode j = om.valueToTree(OrgMemberRow.from(s));
        assertThat(j.size()).isEqualTo(4);
        assertThat(j.has("position")).isTrue();
        assertThat(j.get("position").isNull()).isTrue();
        assertThat(j.get("username").isNull()).isTrue();
        JsonNode legacyJson = om.valueToTree(legacy(s));
        assertThat(j).isEqualTo(legacyJson);
    }
}
