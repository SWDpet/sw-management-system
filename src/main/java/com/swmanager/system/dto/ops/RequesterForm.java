package com.swmanager.system.dto.ops;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * /ops-doc/api/requester (POST) 요청 바디 — 요청자(공무원) 인라인 신규 등록.
 *
 * 기존 {@code @RequestBody} 비타입 Map 수신을 타입 DTO 로 대체한다(§6-4 opsdoc-request-dto).
 * 프론트(ops-doc-relations.js:87)는 name/org/dept/pos/tel 을 전송한다. {@code city} 는 미전송이나
 * 컨트롤러가 ps_info.cityNm 으로 반영하므로 외부 호환을 위해 필드를 유지한다(미전송→null, 현행 동일).
 * 기존 Map 수신은 미지 키를 무시했으므로 {@link JsonIgnoreProperties}{@code (ignoreUnknown=true)} 로 동일 관용을 보장한다.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record RequesterForm(String name, String org, String dept, String pos, String tel, String city) {
}
