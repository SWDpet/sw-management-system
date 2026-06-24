package com.swmanager.system.service.inspection;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * QrBatchPayloadAdapter.normalize 단위테스트 (커버리지 상향 beyond-A, 순수 static·의존 없음).
 *
 * <p>per-tier shape → multi-tier shape 정규화와, 손대지 않아야 하는 입력(이미 multi-tier·
 * 인식 못하는 shape·비ObjectNode)을 검증한다.</p>
 */
class QrBatchPayloadAdapterTest {

    private final ObjectMapper om = new ObjectMapper();

    private JsonNode parse(String json) {
        try {
            return om.readTree(json);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ── 손대지 않는 입력 (그대로 반환) ──────────────────────────────

    @Test
    void normalize_null_returnsNull() {
        assertThat(QrBatchPayloadAdapter.normalize(null)).isNull();
    }

    @Test
    void normalize_nonObjectRoot_returnedAsIs() {
        JsonNode arr = parse("[1,2,3]");
        assertThat(QrBatchPayloadAdapter.normalize(arr)).isSameAs(arr);
    }

    @Test
    void normalize_missingPayload_unchanged() {
        JsonNode root = parse("{\"header\":{\"k\":1}}");
        JsonNode out = QrBatchPayloadAdapter.normalize(root);
        assertThat(out).isSameAs(root);
        assertThat(out.has("payload")).isFalse();
    }

    @Test
    void normalize_payloadNotObject_unchanged() {
        JsonNode root = parse("{\"payload\":\"oops\"}");
        JsonNode out = QrBatchPayloadAdapter.normalize(root);
        assertThat(out).isSameAs(root);                          // 형제 테스트처럼 root 그대로 반환
        assertThat(out.get("payload").asText()).isEqualTo("oops");
    }

    @Test
    void normalize_alreadyMultiTier_untouched() {
        JsonNode root = parse("{\"payload\":{\"id\":\"dyg-2026-05\",\"tiers\":{\"ap\":{\"h\":\"UPIS-AP\",\"i\":[1]}}}}");
        JsonNode out = QrBatchPayloadAdapter.normalize(root);
        ObjectNode payload = (ObjectNode) out.get("payload");
        assertThat(payload.has("tiers")).isTrue();
        assertThat(payload.get("tiers").get("ap").get("h").asText()).isEqualTo("UPIS-AP");
        // per-tier 키는 애초에 없었으므로 그대로
        assertThat(payload.has("tier")).isFalse();
    }

    @Test
    void normalize_tierNotTextual_unchanged() {
        JsonNode root = parse("{\"payload\":{\"tier\":123,\"i\":[1]}}");
        JsonNode out = QrBatchPayloadAdapter.normalize(root);
        assertThat(out.get("payload").has("tiers")).isFalse();
        assertThat(out.get("payload").get("tier").asInt()).isEqualTo(123);
    }

    @Test
    void normalize_itemsNotArray_unchanged() {
        JsonNode root = parse("{\"payload\":{\"tier\":\"ap\",\"i\":\"nope\"}}");
        JsonNode out = QrBatchPayloadAdapter.normalize(root);
        assertThat(out.get("payload").has("tiers")).isFalse();
    }

    @Test
    void normalize_missingItems_unchanged() {
        JsonNode root = parse("{\"payload\":{\"tier\":\"ap\"}}");
        JsonNode out = QrBatchPayloadAdapter.normalize(root);
        assertThat(out.get("payload").has("tiers")).isFalse();
    }

    // ── per-tier → multi-tier 정규화 ───────────────────────────────

    @Test
    void normalize_perTierWithHost_convertsToMultiTierAndStripsPerTierKeys() {
        JsonNode root = parse("{\"payload\":{"
                + "\"id\":\"dyg-2026-05-ap\",\"site\":\"dyg\",\"round\":\"2026-05\","
                + "\"tier\":\"ap\",\"host\":\"IUHOME\",\"i\":[{\"k\":\"cpu\"},{\"k\":\"mem\"}]}}");
        JsonNode out = QrBatchPayloadAdapter.normalize(root);
        ObjectNode payload = (ObjectNode) out.get("payload");

        // tiers.ap 생성, host→h, i 이동
        JsonNode ap = payload.get("tiers").get("ap");
        assertThat(ap.get("h").asText()).isEqualTo("IUHOME");
        assertThat(ap.get("i")).hasSize(2);
        assertThat(ap.get("i").get(0).get("k").asText()).isEqualTo("cpu");

        // per-tier 전용 키 제거
        assertThat(payload.has("tier")).isFalse();
        assertThat(payload.has("host")).isFalse();
        assertThat(payload.has("i")).isFalse();
        // 공통 식별 키는 보존
        assertThat(payload.get("id").asText()).isEqualTo("dyg-2026-05-ap");
        assertThat(payload.get("round").asText()).isEqualTo("2026-05");
    }

    @Test
    void normalize_perTierWithoutHost_omitsHKey() {
        JsonNode root = parse("{\"payload\":{\"tier\":\"db\",\"i\":[{\"k\":\"tablespace\"}]}}");
        JsonNode out = QrBatchPayloadAdapter.normalize(root);
        JsonNode db = out.get("payload").get("tiers").get("db");
        assertThat(db.has("h")).isFalse();          // host 없으면 h 미설정
        assertThat(db.get("i")).hasSize(1);
        assertThat(out.get("payload").has("tier")).isFalse();
    }

    @Test
    void normalize_perTierHostNotTextual_omitsHKey() {
        JsonNode root = parse("{\"payload\":{\"tier\":\"gis\",\"host\":42,\"i\":[]}}");
        JsonNode out = QrBatchPayloadAdapter.normalize(root);
        JsonNode gis = out.get("payload").get("tiers").get("gis");
        assertThat(gis.has("h")).isFalse();         // host 가 텍스트 아니면 h 미설정
        assertThat(gis.get("i")).isEmpty();
        assertThat(out.get("payload").has("host")).isFalse();
    }
}
