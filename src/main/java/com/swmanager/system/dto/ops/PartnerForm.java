package com.swmanager.system.dto.ops;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * /partner/api (POST 생성 / PUT 수정) 요청 바디 — 외부업체(협력사).
 *
 * 기존 {@code @RequestBody} 비타입 Map 수신을 타입 DTO 로 대체한다(§6-4 partner-request-dto).
 * 프론트(partner-management.html:85)가 보내는 name/partner_type/main_tel/biz_no/note 를 무손실 보존한다.
 * 기존 Map 수신은 미지 키를 무시했으므로 {@link JsonIgnoreProperties}{@code (ignoreUnknown=true)} 로 동일 관용을 보장한다.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PartnerForm(
        String name,
        @JsonProperty("partner_type") String partnerType,
        @JsonProperty("biz_no") String bizNo,
        @JsonProperty("main_tel") String mainTel,
        String note) {
}
