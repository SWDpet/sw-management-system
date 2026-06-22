package com.swmanager.system.dto.workplan;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ParticipantSaveResult / secure-403 Map.of 직렬화 골든 테스트 (participant-save-dto §6-4).
 *
 * DocumentParticipantController 의 컨트롤러-로컬 Map 응답조립을 record/ApiResult/Map.of 로 바꾸면서
 * 응답 JSON 무손실(성공 {success,count}, secure403 {error:{code,message}})을 고정한다. JsonNode tree 동치.
 * (요청바디는 lenient Map 파싱 보존 — 본 sprint 비대상)
 */
class ParticipantSaveResultTest {

    private final ObjectMapper om = new ObjectMapper();

    @Test
    void saveResult_success_matchesLegacy() {
        // 현행 성공: {success:true, count:n}
        Map<String, Object> legacy = new LinkedHashMap<>();
        legacy.put("success", true);
        legacy.put("count", 3);
        JsonNode a = om.valueToTree(new ParticipantSaveResult(true, 3));
        JsonNode b = om.valueToTree(legacy);
        assertThat(a).isEqualTo(b);
        assertThat(a.size()).isEqualTo(2);
    }

    @Test
    void saveResult_zeroCount_serializesBoth() {
        // count=0·success 항상 직렬화(primitive)
        JsonNode j = om.valueToTree(new ParticipantSaveResult(true, 0));
        assertThat(j.has("success")).isTrue();
        assertThat(j.has("count")).isTrue();
        assertThat(j.get("count").asInt()).isZero();
    }

    @Test
    void secure403_mapOf_matchesLegacy() {
        // 현행 secure 403: {error:{code,message}} (success 키 없음)
        Map<String, Object> legacy = new LinkedHashMap<>();
        legacy.put("error", Map.of("code", "FORBIDDEN", "message", "민감 정보 조회 권한이 없습니다"));

        Object current = Map.of("error", Map.of("code", "FORBIDDEN", "message", "민감 정보 조회 권한이 없습니다"));
        JsonNode a = om.valueToTree(current);
        JsonNode b = om.valueToTree(legacy);
        assertThat(a).isEqualTo(b);
        assertThat(a.has("success")).isFalse();   // success 키 미추가 보존
        assertThat(a.get("error").get("code").asText()).isEqualTo("FORBIDDEN");
    }
}
