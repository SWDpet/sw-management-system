package com.swmanager.system.dto.workplan;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * DocumentSaveResult 직렬화 골든 테스트 (document-save-result-dto §6-4).
 *
 * DocumentController.saveDocument 의 컨트롤러-로컬 HashMap 성공 응답(success/docId/docNo)을 record 로
 * 바꾸면서 JSON 무손실(키셋·값·docNo "" fallback·docId null)을 고정한다. JsonNode tree 동치.
 */
class DocumentSaveResultTest {

    private final ObjectMapper om = new ObjectMapper();

    @Test
    void saveResult_matchesLegacy() {
        Map<String, Object> legacy = new HashMap<>();
        legacy.put("success", true);
        legacy.put("docId", 42);
        legacy.put("docNo", "2026-착수-001");
        JsonNode a = om.valueToTree(new DocumentSaveResult(true, 42, "2026-착수-001"));
        JsonNode b = om.valueToTree(legacy);
        assertThat(a).isEqualTo(b);
        assertThat(a.size()).isEqualTo(3);
    }

    @Test
    void saveResult_docNoEmptyFallback_andDocIdNull() {
        // 현행: docNo null → "" 주입. docId 는 doc.getDocId()(저장 후 non-null 이나 record 직렬화 검증)
        Map<String, Object> legacy = new HashMap<>();
        legacy.put("success", true);
        legacy.put("docId", null);
        legacy.put("docNo", "");
        JsonNode a = om.valueToTree(new DocumentSaveResult(true, null, ""));
        JsonNode b = om.valueToTree(legacy);
        assertThat(a).isEqualTo(b);
        assertThat(a.get("docNo").asText()).isEmpty();
        assertThat(a.get("docId").isNull()).isTrue();   // null 키 보존
        assertThat(a.get("success").asBoolean()).isTrue();
    }
}
