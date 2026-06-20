package com.swmanager.system.dto.ops;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * /ops-doc/api/{type} (POST 생성 / PUT 수정) 요청 바디 — 운영문서 4종(FAULT/SUPPORT/INSTALL/PATCH).
 *
 * 기존 {@code @RequestBody} 비타입 Map 수신을 타입 DTO 로 대체한다(§6-4 opsdoc-createupdate-dto).
 * 프론트(doc-fault.html:187 등 + ops-doc-relations.js)가 보내는 키를 무손실 보존한다.
 * id 류(engineer_id/requester_id/partner_id)는 프론트가 {@code Number} 로 전송하므로 {@code Long} 직접 바인딩이
 * 기존 {@code instanceof Number} 판정과 동치다.
 *
 * <p>{@code sectionData} 는 문서 유형별 동적 jsonb 섹션이라 타입화하지 않고 {@code Map} 으로 유지한다(타입화 대상 아님).
 * {@code environment}/{@code supportTargetType} 은 FAULT 본문엔 없으나 SUPPORT/INSTALL/PATCH 가 전송한다.
 * 기존 Map 수신은 미지 키를 무시했으므로 {@link JsonIgnoreProperties}{@code (ignoreUnknown=true)} 로 동일 관용을 보장한다.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OpsDocForm(
        String title,
        @JsonProperty("sys_type") String sysType,
        @JsonProperty("region_code") String regionCode,
        String environment,
        @JsonProperty("support_target_type") String supportTargetType,
        @JsonProperty("engineer_id") Long engineerId,
        @JsonProperty("requester_kind") String requesterKind,
        @JsonProperty("requester_id") Long requesterId,
        @JsonProperty("section_data") Map<String, Object> sectionData,
        List<PartnerRef> partners) {

    /** 문서 협력업체 1건 — body.partners[{partner_id, role_label}]. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PartnerRef(
            @JsonProperty("partner_id") Long partnerId,
            @JsonProperty("role_label") String roleLabel) {
    }
}
