package com.swmanager.system.dto.orgunit;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

/**
 * 조직도 중첩 트리 노드 응답 dto — /api/org-units/tree.
 *
 * 기존 OrgUnitService.getTree 의 가변 LinkedHashMap 트리 조립(toDto + children.add)을 불변 재귀 record 로
 * 대체한다(§6-4 orgunit-tree-dto). 소비자(org-unit 관리 화면 JS)는
 * {@code unit_id/name/unit_type/parent_id/sort_order/children} 키로 접근.
 *
 * <p>{@link OrgUnitNode}(평면)와 동일 키 + {@code children}. snake_case 키는 {@link JsonProperty},
 * {@link JsonPropertyOrder} 로 현행 put 순(unit_id,name,unit_type,parent_id,sort_order,children) 고정.
 * {@code @JsonInclude} 미부착(null 포함, 특히 root 의 parent_id null). children 은 항상 배열(빈 트리=[]).
 */
@JsonPropertyOrder({"unit_id", "name", "unit_type", "parent_id", "sort_order", "children"})
public record OrgTreeNode(
        @JsonProperty("unit_id") Long unitId,
        String name,
        @JsonProperty("unit_type") String unitType,
        @JsonProperty("parent_id") Long parentId,
        @JsonProperty("sort_order") Integer sortOrder,
        List<OrgTreeNode> children) {
}
