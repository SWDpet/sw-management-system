package com.swmanager.system.dto.ops;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * /ops-kb/api/{id}/reject 요청 바디 — 반려 사유.
 *
 * 기존 {@code @RequestBody} 비타입 Map 수신을 타입 DTO 로 대체한다(§6-4 opskb-request-dto).
 * 프론트(kb-list.html:124)는 {@code {"reason":"..."}} 를 전송한다.
 * 기존 Map 수신은 미지 키를 무시했으므로 {@link JsonIgnoreProperties}{@code (ignoreUnknown=true)} 로 동일 관용을 보장한다.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record RejectForm(String reason) {
}
