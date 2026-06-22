package com.swmanager.system.dto.orgunit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swmanager.system.domain.OrgUnit;
import com.swmanager.system.repository.OrgUnitRepository;
import com.swmanager.system.service.OrgUnitService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * OrgTreeNode 재귀 트리 골든 테스트 (orgunit-tree-dto §6-4).
 *
 * OrgUnitService.getTree 의 가변 Map 트리 빌더를 불변 재귀 record 빌더로 재작성(알고리즘 변경)한 뒤,
 * <b>실제 서비스 getTree()</b>(Mockito repo 주입) 결과가 현행 Map 알고리즘 복제본과 JsonNode tree 동치인지
 * 고정한다. 핵심: orphan-drop(부모가 활성집합에 없는 노드 드롭)·children/root 순서·snake 키.
 */
class OrgTreeNodeTest {

    private final ObjectMapper om = new ObjectMapper();

    private OrgUnit unit(Long id, String name, String type, OrgUnit parent, Integer sort) {
        OrgUnit u = new OrgUnit();
        u.setUnitId(id);
        u.setName(name);
        u.setUnitType(type);
        u.setParent(parent);
        u.setSortOrder(sort);
        u.setUseYn("Y");
        return u;
    }

    /** 현행 getTree 알고리즘 복제(검증 기준): nodeMap + children.add, orphan(부모 부재) 드롭. */
    private List<Map<String, Object>> legacyTree(List<OrgUnit> all) {
        Map<Long, Map<String, Object>> nodeMap = new LinkedHashMap<>();
        for (OrgUnit u : all) {
            Map<String, Object> node = new LinkedHashMap<>();
            node.put("unit_id", u.getUnitId());
            node.put("name", u.getName());
            node.put("unit_type", u.getUnitType());
            node.put("parent_id", u.getParent() != null ? u.getParent().getUnitId() : null);
            node.put("sort_order", u.getSortOrder());
            node.put("children", new ArrayList<Map<String, Object>>());
            nodeMap.put(u.getUnitId(), node);
        }
        List<Map<String, Object>> roots = new ArrayList<>();
        for (OrgUnit u : all) {
            Map<String, Object> node = nodeMap.get(u.getUnitId());
            Long parentId = (u.getParent() != null) ? u.getParent().getUnitId() : null;
            if (parentId == null) {
                roots.add(node);
            } else {
                Map<String, Object> parentNode = nodeMap.get(parentId);
                if (parentNode != null) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> children = (List<Map<String, Object>>) parentNode.get("children");
                    children.add(node);
                }
            }
        }
        return roots;
    }

    private List<OrgTreeNode> serviceTree(List<OrgUnit> all) {
        OrgUnitRepository repo = mock(OrgUnitRepository.class);
        when(repo.findAllByUseYnOrderBySortOrderAsc("Y")).thenReturn(all);
        OrgUnitService service = new OrgUnitService();
        ReflectionTestUtils.setField(service, "orgUnitRepository", repo);
        return service.getTree();
    }

    @Test
    void getTree_matchesLegacy_multiLevel_order_orphanDrop() {
        OrgUnit u1 = unit(1L, "본부", "DIVISION", null, 1);
        OrgUnit u2 = unit(2L, "부서A", "DEPARTMENT", u1, 2);
        OrgUnit u3 = unit(3L, "팀A1", "TEAM", u2, 3);
        OrgUnit u4 = unit(4L, "본부2", "DIVISION", null, 4);
        OrgUnit u5 = unit(5L, "부서B", "DEPARTMENT", u1, 5);    // u1 의 둘째 자식(u2 다음 순서)
        OrgUnit ghost = unit(999L, "비활성본부", "DIVISION", null, 0); // 활성집합에 미포함
        OrgUnit orphan = unit(6L, "고아부서", "DEPARTMENT", ghost, 6); // 부모(999) 비활성 → 드롭

        // 활성 조회 결과(sortOrder 순) — ghost 제외
        List<OrgUnit> all = List.of(u1, u2, u3, u4, u5, orphan);

        JsonNode actual = om.valueToTree(serviceTree(all));
        JsonNode legacy = om.valueToTree(legacyTree(all));
        assertThat(actual).isEqualTo(legacy);
    }

    @Test
    void getTree_snakeKeys_childrenAlways_andRootParentNull() {
        OrgUnit u1 = unit(1L, "본부", "DIVISION", null, 1);
        OrgUnit u2 = unit(2L, "부서A", "DEPARTMENT", u1, 2);
        JsonNode tree = om.valueToTree(serviceTree(List.of(u1, u2)));

        JsonNode root = tree.get(0);
        assertThat(root.has("unit_id")).isTrue();
        assertThat(root.has("unit_type")).isTrue();
        assertThat(root.has("parent_id")).isTrue();
        assertThat(root.has("sort_order")).isTrue();
        assertThat(root.has("unitId")).isFalse();          // snake 강제
        assertThat(root.get("parent_id").isNull()).isTrue(); // root parent null 보존
        assertThat(root.get("children").isArray()).isTrue();
        assertThat(root.get("children").get(0).get("unit_id").asLong()).isEqualTo(2L);
        // 잎 노드 children 빈 배열
        assertThat(root.get("children").get(0).get("children").size()).isZero();
    }

    @Test
    void getTree_empty() {
        assertThat(om.valueToTree(serviceTree(List.of())).size()).isZero();
    }
}
