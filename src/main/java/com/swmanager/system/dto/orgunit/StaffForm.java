package com.swmanager.system.dto.orgunit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * /admin/api/staff (POST 생성 / PUT 수정) 요청 바디 — 조직도 직원.
 *
 * 기존 {@code @RequestBody} 비타입 Map 수신을 타입 DTO 로 대체한다(§6-4 orgunit-request-dto).
 * 프론트(org-unit-management.html: 생성 L250 name/position/org_unit_id/active, 수정 L256 name/position/active)가
 * 보내는 JSON 키를 무손실 보존한다.
 *
 * <p>수정 시 프론트는 {@code org_unit_id}·{@code tel} 을 전송하지 않는다. {@code applyStaff} 가
 * {@code orgUnitId != null} 일 때만 set 하므로(생략→null→skip), 기존 {@code containsKey} 기반 "유닛 보존" 동작과 등가다
 * (프론트는 명시적 null 을 보내지 않음).
 * 기존 Map 수신은 미지 키를 무시했으므로 {@link JsonIgnoreProperties}{@code (ignoreUnknown=true)} 로 동일 관용을 보장한다.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record StaffForm(
        String name,
        String position,
        @JsonProperty("org_unit_id") Long orgUnitId,
        Boolean active,
        String tel) {
}
