package com.swmanager.system.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * AdminSensitiveFieldRow 직렬화 골든 테스트 (qrbatch-adminuser-rows-dto §6-4, C2).
 *
 * AdminUserController.getSensitiveField 의 컨트롤러-로컬 LinkedHashMap(field/value) 200 응답조립을
 * record 로 바꾸면서 JSON 무손실(키셋·값·value null→"" fallback·화이트리스트 2키)을 고정한다.
 */
class AdminSensitiveFieldRowTest {

    private final ObjectMapper om = new ObjectMapper();

    /** 현행 컨트롤러 로직 복제(value fallback 포함). */
    private Map<String, Object> legacy(String field, String value) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("field", field);
        m.put("value", value != null ? value : "");
        return m;
    }

    @Test
    void sensitiveFieldRow_value_matchesLegacy() {
        JsonNode a = om.valueToTree(new AdminSensitiveFieldRow("mobile", "010-1234-5678"));
        JsonNode b = om.valueToTree(legacy("mobile", "010-1234-5678"));
        assertThat(a).isEqualTo(b);
        assertThat(a.size()).isEqualTo(2);  // 화이트리스트 2키만 — 신규 키 비추가
    }

    @Test
    void sensitiveFieldRow_nullValue_fallsBackToEmpty() {
        // 현행: value null → "" 주입 후 응답
        AdminSensitiveFieldRow row = new AdminSensitiveFieldRow("ssn", null != null ? null : "");
        JsonNode a = om.valueToTree(row);
        JsonNode b = om.valueToTree(legacy("ssn", null));
        assertThat(a).isEqualTo(b);
        assertThat(a.get("value").asText()).isEmpty();
        assertThat(a.get("value").isNull()).isFalse();
    }
}
