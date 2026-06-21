package com.swmanager.system.dto.ops;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swmanager.system.domain.ops.OpsKb;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * OpsKbDto 직렬화 골든 테스트 (opskb-todto-record §6-4).
 *
 * 기존 {@code OpsKbController.toDto()} 의 {@code Map<String,Object>}(LinkedHashMap) 조립을 record 로 바꾸면서,
 * /ops-kb/api/list · /ops-kb/api/{id} 응답 JSON 이 무손실(키셋·값·키순서·null 포함·action 폴백)인지 고정한다.
 * 소비자(kb-list.html JS · doc-fault/support/patch/install 추천)는 snake_case 키로 접근한다.
 */
class OpsKbDtoSerializationTest {

    private final ObjectMapper om = new ObjectMapper();

    /** 치환 전 toDto() 가 만들던 LinkedHashMap 을 그대로 복제(골든 기준). */
    private Map<String, Object> legacyMap(OpsKb k) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("kb_id", k.getKbId());
        m.put("kb_code", k.getKbCode());
        m.put("gubun", k.getGubun());
        m.put("sys_type", k.getSysType());
        m.put("category", k.getCategory());
        m.put("symptom", k.getSymptom());
        m.put("cause", k.getCause());
        m.put("summary", k.getSummary());
        m.put("action", (k.getAction() != null && !k.getAction().isBlank()) ? k.getAction() : k.getCauseDesc());
        m.put("prevention", k.getPrevention());
        m.put("keywords", k.getKeywords());
        m.put("case_count", k.getCaseCount());
        m.put("source", k.getSource());
        m.put("status", k.getStatus());
        m.put("reject_reason", k.getRejectReason());
        m.put("reviewed_by", k.getReviewedBy());
        m.put("created_by", k.getCreatedBy());
        return m;
    }

    private OpsKb fullyPopulated() {
        OpsKb k = new OpsKb();
        k.setKbId(101L);
        k.setKbCode("KB-2026-00007");
        k.setGubun("장애");
        k.setSysType("UPIS");
        k.setCategory("네트워크");
        k.setSymptom("접속 불가");
        k.setCause("세션 만료");
        k.setSummary("요약문");
        k.setAction("재기동 조치");
        k.setPrevention("모니터링");
        k.setKeywords("접속,세션");
        k.setCaseCount(3);
        k.setSource("MANUAL");
        k.setStatus("ACTIVE");
        k.setRejectReason("사유");
        k.setReviewedBy("admin");
        k.setCreatedBy("ukjin");
        return k;
    }

    @Test
    void fullyPopulated_treeEqualsLegacyMap() throws Exception {
        OpsKb k = fullyPopulated();

        JsonNode dtoJson = om.valueToTree(OpsKbDto.from(k));
        JsonNode legacyJson = om.valueToTree(legacyMap(k));

        assertThat(dtoJson).isEqualTo(legacyJson);   // 키셋·값 동치
    }

    @Test
    void fullyPopulated_keyOrderPreserved() throws Exception {
        // JS 는 키로 접근(순서 무관)하나, 현행 put 순과 시각 동치까지 고정한다.
        String dto = om.writeValueAsString(OpsKbDto.from(fullyPopulated()));
        String legacy = om.writeValueAsString(legacyMap(fullyPopulated()));
        assertThat(dto).isEqualTo(legacy);
    }

    @Test
    void actionBlank_fallsBackToCauseDesc() throws Exception {
        OpsKb k = fullyPopulated();
        k.setAction("   ");          // 공백 → 폴백
        k.setCauseDesc("원인설명 폴백");

        JsonNode json = om.valueToTree(OpsKbDto.from(k));
        assertThat(json.get("action").asText()).isEqualTo("원인설명 폴백");
        // 골든 기준과도 동치
        JsonNode legacyJson = om.valueToTree(legacyMap(k));
        assertThat(json).isEqualTo(legacyJson);
    }

    @Test
    void nullFields_includedAsNull() throws Exception {
        // 현행 LinkedHashMap 은 17키를 null 값이어도 항상 담았다 → record(@JsonInclude 미부착)도 동일 보존.
        OpsKb k = new OpsKb();        // 대부분 null (source=SEED·status=ACTIVE 만 필드 기본값)
        k.setKbId(1L);

        JsonNode json = om.valueToTree(OpsKbDto.from(k));
        // null 키가 직렬화에서 누락되지 않는지(전역 jackson non_null 미설정 가정의 회귀 가드)
        assertThat(json.has("reject_reason")).isTrue();
        assertThat(json.get("reject_reason").isNull()).isTrue();
        assertThat(json.has("reviewed_by")).isTrue();
        assertThat(json.get("reviewed_by").isNull()).isTrue();
        assertThat(json.has("category")).isTrue();
        assertThat(json.get("category").isNull()).isTrue();
        // 17키 전부 존재
        assertThat(json.size()).isEqualTo(17);
        JsonNode legacyJson = om.valueToTree(legacyMap(k));
        assertThat(json).isEqualTo(legacyJson);
    }
}
