package com.swmanager.system.service.inspection;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * QR 배치 페이로드 schema adapter.
 *
 * agent-windows 가 진화하면서 페이로드 스키마가 둘로 갈렸다:
 *
 *   <b>multi-tier (legacy, PoC v1)</b> — 한 페이로드에 모든 tier 동봉:
 *   <pre>{
 *     "id":"dyg-2026-05", "site":"dyg", "round":"2026-05",
 *     "tiers": { "ap":{"h":"UPIS-AP","os":"...","i":[...]} , "db":{...}, "gis":{...} }
 *   }</pre>
 *
 *   <b>per-tier (current agent-windows)</b> — tier 마다 1 페이로드(따라서 1 round 에 다중 QR):
 *   <pre>{
 *     "id":"dyg-2026-05-ap", "site":"dyg", "round":"2026-05",
 *     "tier":"ap", "host":"IUHOME", "i":[ ... ]
 *   }</pre>
 *
 * 본 adapter 는 per-tier shape 가 들어오면 multi-tier shape 로 정규화한다 — 백엔드/DTO/Service
 * 는 multi-tier 만 알면 되도록. 같은 round 의 다른 tier 가 별도 QR 로 들어오면
 * Service 의 merge 로직 (기존 report 발견 시 batch_id 보존 + 자동수집 section 만 재적재) 으로 누적된다.
 */
public final class QrBatchPayloadAdapter {

    private QrBatchPayloadAdapter() {}

    /**
     * 요청 본문 전체를 받아 payload 노드를 정규화한다. 입력은 보존하지 않고 in-place 수정.
     *
     * @param root  {"payload":..., "header":...} 구조의 JsonNode (반드시 ObjectNode 여야 함)
     * @return root (chain 용)
     */
    public static JsonNode normalize(JsonNode root) {
        if (root == null || !root.isObject()) return root;
        JsonNode payload = root.get("payload");
        if (payload == null || !payload.isObject()) return root;
        ObjectNode payloadObj = (ObjectNode) payload;

        boolean hasMultiTier = payloadObj.has("tiers") && payloadObj.get("tiers").isObject();
        if (hasMultiTier) return root; // 이미 multi-tier — 손대지 않음

        JsonNode tierNode = payloadObj.get("tier");
        JsonNode itemsNode = payloadObj.get("i");
        if (tierNode == null || !tierNode.isTextual()) return root; // 인식 못하는 shape — 그대로 두고 검증 실패 위임
        if (itemsNode == null || !itemsNode.isArray()) return root;

        String tierName = tierNode.asText();
        ObjectNode tierObj = payloadObj.objectNode();
        JsonNode hostNode = payloadObj.get("host");
        if (hostNode != null && hostNode.isTextual()) tierObj.set("h", hostNode);
        tierObj.set("i", itemsNode);

        ObjectNode tiers = payloadObj.objectNode();
        tiers.set(tierName, tierObj);
        payloadObj.set("tiers", tiers);

        // top-level 의 per-tier 전용 키 제거 (DTO 에 없는 필드라 무해하지만 일관성 위해)
        payloadObj.remove("tier");
        payloadObj.remove("host");
        payloadObj.remove("i");

        return root;
    }
}
