package com.swmanager.system.dto.ops;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * /partner/api/{partnerId}/contact (POST) 요청 바디 — 협력사 담당자.
 *
 * 기존 {@code @RequestBody} 비타입 Map 수신을 타입 DTO 로 대체한다(§6-4 partner-request-dto).
 * 프론트(partner-management.html:94)는 name/position/tel 을 전송한다. {@code email} 은 미전송이나
 * 컨트롤러가 수신 시 반영하므로 외부 호환을 위해 필드를 유지한다(미전송→null, 현행 동일).
 * 기존 Map 수신은 미지 키를 무시했으므로 {@link JsonIgnoreProperties}{@code (ignoreUnknown=true)} 로 동일 관용을 보장한다.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ContactForm(String name, String position, String tel, String email) {
}
