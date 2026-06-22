package com.swmanager.system.dto.orgunit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * StaffCreateResult 직렬화 골든 테스트 (§6-4).
 *
 * OrgUnitController.createStaff 의 컨트롤러-로컬 LinkedHashMap(success/staff_id) 200 응답조립을
 * record 로 바꾸면서 snake_case 키 무손실(success, staff_id)을 고정한다. JsonNode tree 동치.
 */
class StaffCreateResultTest {

    private final ObjectMapper om = new ObjectMapper();

    @Test
    void staffCreateResult_matchesLegacy_snakeKey() {
        // 현행: {success:true, staff_id:n}
        Map<String, Object> legacy = new LinkedHashMap<>();
        legacy.put("success", true);
        legacy.put("staff_id", 42L);

        JsonNode a = om.valueToTree(new StaffCreateResult(true, 42L));
        JsonNode b = om.valueToTree(legacy);
        assertThat(a).isEqualTo(b);
        assertThat(a.has("staff_id")).isTrue();    // snake 키 실증
        assertThat(a.has("staffId")).isFalse();
        assertThat(a.get("success").asBoolean()).isTrue();
        assertThat(a.size()).isEqualTo(2);
    }
}
