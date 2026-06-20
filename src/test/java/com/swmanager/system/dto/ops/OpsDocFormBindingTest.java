package com.swmanager.system.dto.ops;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RequesterForm / FeedbackForm 요청 바디 바인딩 골든 테스트 (opsdoc-request-dto §6-4).
 *
 * 기존 비타입 Map 수신을 타입 DTO 로 바꾸면서, 프론트(ops-doc-relations.js)가 보내던
 * JSON 키(snake_case·숫자형·명시 null)를 무손실 바인딩하는지 고정한다.
 */
class OpsDocFormBindingTest {

    private final ObjectMapper om = new ObjectMapper();

    @Test
    void requesterForm_bindsKeys_cityOmittedNull() throws Exception {
        // ops-doc-relations.js:87 본문 (city 미전송)
        String json = "{\"name\":\"홍길동\",\"org\":\"강진군청\",\"dept\":\"정보과\",\"pos\":\"주무관\",\"tel\":\"061-1\"}";

        RequesterForm f = om.readValue(json, RequesterForm.class);

        assertThat(f.name()).isEqualTo("홍길동");
        assertThat(f.org()).isEqualTo("강진군청");
        assertThat(f.dept()).isEqualTo("정보과");
        assertThat(f.pos()).isEqualTo("주무관");
        assertThat(f.tel()).isEqualTo("061-1");
        assertThat(f.city()).isNull();                  // 미전송 → null (현행 동일)
    }

    @Test
    void feedbackForm_bindsSnakeCaseNumbers() throws Exception {
        // ops-doc-relations.js:197 본문
        FeedbackForm f = om.readValue("{\"kb_id\":42,\"doc_id\":7,\"fb_action\":\"APPLIED\"}", FeedbackForm.class);

        assertThat(f.kbId()).isEqualTo(42L);            // kb_id → kbId(Long)
        assertThat(f.docId()).isEqualTo(7L);            // doc_id → docId(Long)
        assertThat(f.fbAction()).isEqualTo("APPLIED");
    }

    @Test
    void feedbackForm_docIdExplicitNull_bindsNull() throws Exception {
        // doc_id: null 명시 전송 (window.OPS_DOC_ID || null) → null → setDocId skip 보존
        FeedbackForm f = om.readValue("{\"kb_id\":42,\"doc_id\":null,\"fb_action\":\"IGNORED\"}", FeedbackForm.class);
        assertThat(f.kbId()).isEqualTo(42L);
        assertThat(f.docId()).isNull();
        assertThat(f.fbAction()).isEqualTo("IGNORED");
    }

    @Test
    void unknownKeys_ignored() throws Exception {
        RequesterForm f = om.readValue("{\"name\":\"x\",\"legacy\":1}", RequesterForm.class);
        assertThat(f.name()).isEqualTo("x");
    }
}
