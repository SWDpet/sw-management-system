package com.swmanager.system.dto.orgunit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * /admin/api/org-units (POST 생성 / PUT 수정) 요청 바디.
 *
 * 기존 {@code @RequestBody} 비타입 Map 수신을 타입 DTO 로 대체한다(§6-4 orgunit-request-dto).
 * 프론트(org-unit-management.html: 생성 L313 parent_id/unit_type/name/sort_order, 수정 L280 name/sort_order)가
 * 보내는 JSON 키를 무손실 보존한다. snake_case 키는 {@link JsonProperty} 로 매핑하고, 숫자형(parent_id/sort_order)은
 * Long/Integer 로 직접 바인딩한다(기존 {@code ((Number)…).longValue()} 캐스팅 대체, 동일 결과).
 * 기존 Map 수신은 미지 키를 무시했으므로 {@link JsonIgnoreProperties}{@code (ignoreUnknown=true)} 로 동일 관용을 보장한다.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OrgUnitForm(
        @JsonProperty("parent_id") Long parentId,
        @JsonProperty("unit_type") String unitType,
        String name,
        @JsonProperty("sort_order") Integer sortOrder,
        @JsonProperty("use_yn") String useYn) {
}
