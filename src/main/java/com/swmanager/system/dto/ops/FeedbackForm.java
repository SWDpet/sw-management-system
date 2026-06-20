package com.swmanager.system.dto.ops;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * /ops-doc/api/kb/feedback (POST) 요청 바디 — KB 추천 채택 피드백(APPLIED/IGNORED).
 *
 * 기존 {@code @RequestBody} 비타입 Map 수신을 타입 DTO 로 대체한다(§6-4 opsdoc-request-dto).
 * 프론트(ops-doc-relations.js:197)는 kb_id/doc_id/fb_action 을 전송한다(doc_id 는 명시 null 가능).
 * snake_case 키는 {@link JsonProperty} 로 매핑하고, 숫자형(kb_id/doc_id)은 Long 으로 직접 바인딩한다.
 * 기존 Map 수신은 미지 키를 무시했으므로 {@link JsonIgnoreProperties}{@code (ignoreUnknown=true)} 로 동일 관용을 보장한다.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record FeedbackForm(
        @JsonProperty("kb_id") Long kbId,
        @JsonProperty("doc_id") Long docId,
        @JsonProperty("fb_action") String fbAction) {
}
