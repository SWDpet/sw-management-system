package com.swmanager.system.dto.orgunit;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.swmanager.system.domain.OrgUnit;

/**
 * 조직 단위 노드(평면) 응답 dto — /api/org-units/{roots,children/*}, create/update 응답.
 *
 * 기존 OrgUnitService.toDto 의 LinkedHashMap 조립을 타입 record 로 대체한다(§6-4 orgunit-node-dto).
 * 소비자(org-unit-management.html JS)는 {@code unit_id/name/unit_type/parent_id/sort_order} 키로 접근.
 *
 * <p>snake_case 키(unit_id/unit_type/parent_id/sort_order)는 {@link JsonProperty}, {@code name} 은 camelCase.
 * {@link JsonPropertyOrder} 로 현행 put 순(unit_id,name,unit_type,parent_id,sort_order)을 고정 —
 * Jackson 이 {@code @JsonProperty} 부착 컴포넌트를 앞으로 재정렬하는 기본동작을 눌러 키순서 보존.
 * {@code @JsonInclude} 미부착(null 포함, 특히 parent_id null).
 *
 * <p>※ getTree 의 가변 중첩 트리는 본 record 대상 외 — OrgUnitService 의 private {@code toDto} 헬퍼가 getTree 전용으로 잔존.
 */
@JsonPropertyOrder({"unit_id", "name", "unit_type", "parent_id", "sort_order"})
public record OrgUnitNode(
        @JsonProperty("unit_id") Long unitId,
        String name,
        @JsonProperty("unit_type") String unitType,
        @JsonProperty("parent_id") Long parentId,
        @JsonProperty("sort_order") Integer sortOrder) {

    public static OrgUnitNode from(OrgUnit u) {
        return new OrgUnitNode(
                u.getUnitId(),
                u.getName(),
                u.getUnitType(),
                u.getParent() != null ? u.getParent().getUnitId() : null,
                u.getSortOrder());
    }
}
