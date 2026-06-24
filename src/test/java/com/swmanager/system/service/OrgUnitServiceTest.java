package com.swmanager.system.service;

import com.swmanager.system.domain.OrgUnit;
import com.swmanager.system.domain.ops.Staff;
import com.swmanager.system.dto.orgunit.OrgMemberRow;
import com.swmanager.system.dto.orgunit.OrgTreeNode;
import com.swmanager.system.dto.orgunit.OrgUnitNode;
import com.swmanager.system.repository.OrgUnitRepository;
import com.swmanager.system.repository.ops.StaffRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * OrgUnitService 단위테스트 (커버리지 상향 beyond-A, mock 기반·운영DB 무관).
 * 필드 주입이라 ReflectionTestUtils 로 mock 주입. 트리 조립(orphan drop·재귀)·경로 추적·
 * CRUD·타입 검증·하위 존재 시 삭제 차단을 커버.
 */
class OrgUnitServiceTest {

    private final OrgUnitRepository orgUnitRepository = mock(OrgUnitRepository.class);
    private final StaffRepository staffRepository = mock(StaffRepository.class);

    private OrgUnitService service;

    @BeforeEach
    void setUp() {
        service = new OrgUnitService();
        ReflectionTestUtils.setField(service, "orgUnitRepository", orgUnitRepository);
        ReflectionTestUtils.setField(service, "staffRepository", staffRepository);
        when(orgUnitRepository.save(any(OrgUnit.class))).thenAnswer(i -> i.getArgument(0));
    }

    private OrgUnit unit(Long id, OrgUnit parent, String name, int sort) {
        OrgUnit u = new OrgUnit();
        u.setUnitId(id);
        u.setParent(parent);
        u.setName(name);
        u.setUnitType("TEAM");
        u.setSortOrder(sort);
        u.setUseYn("Y");
        return u;
    }

    // ===== getMembers =====

    @Test
    void getMembers_mapsStaffToRows() {
        Staff s = new Staff();
        s.setStaffId(11L); s.setName("홍길동"); s.setPosition("대리"); s.setActive(false);
        when(staffRepository.findByOrgUnitIdOrderBySortOrderAscNameAsc(5L)).thenReturn(List.of(s));

        List<OrgMemberRow> rows = service.getMembers(5L);

        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).staffId()).isEqualTo(11L);
        assertThat(rows.get(0).username()).isEqualTo("홍길동");
        assertThat(rows.get(0).position()).isEqualTo("대리");
        assertThat(rows.get(0).active()).isFalse();   // 퇴사
    }

    // ===== getTree =====

    @Test
    void getTree_buildsNestedTree_dropsOrphan() {
        OrgUnit root = unit(1L, null, "본부", 0);
        OrgUnit dept = unit(2L, root, "부서", 0);
        OrgUnit team = unit(3L, dept, "팀", 0);
        OrgUnit dept2 = unit(4L, root, "부서2", 1);
        OrgUnit orphan = unit(5L, unit(99L, null, "없는부모", 0), "고아", 0);  // 부모 99 가 활성집합에 없음
        when(orgUnitRepository.findAllByUseYnOrderBySortOrderAsc("Y"))
                .thenReturn(List.of(root, dept, team, dept2, orphan));

        List<OrgTreeNode> roots = service.getTree();

        assertThat(roots).hasSize(1);
        OrgTreeNode r = roots.get(0);
        assertThat(r.unitId()).isEqualTo(1L);
        assertThat(r.children()).extracting(OrgTreeNode::unitId).containsExactly(2L, 4L);  // sortOrder 보존
        OrgTreeNode deptNode = r.children().get(0);
        assertThat(deptNode.children()).extracting(OrgTreeNode::unitId).containsExactly(3L);
        assertThat(deptNode.parentId()).isEqualTo(1L);
        // orphan(5) 은 어느 root 의 자손도 아니므로 트리 어느 깊이에도 없음(전체 재귀 수집으로 검증)
        List<Long> allIds = new ArrayList<>();
        collectIds(roots, allIds);
        assertThat(allIds).containsExactlyInAnyOrder(1L, 2L, 3L, 4L);
    }

    /** 트리 전체(모든 깊이) unitId 재귀 수집. */
    private static void collectIds(List<OrgTreeNode> nodes, List<Long> out) {
        for (OrgTreeNode n : nodes) {
            out.add(n.unitId());
            collectIds(n.children(), out);
        }
    }

    // ===== getChildren / getRoots =====

    @Test
    void getChildren_nullParent_usesRootQuery() {
        when(orgUnitRepository.findByParentIsNullAndUseYnOrderBySortOrderAsc("Y"))
                .thenReturn(List.of(unit(1L, null, "본부", 0)));
        List<OrgUnitNode> out = service.getChildren(null);
        assertThat(out).extracting(OrgUnitNode::unitId).containsExactly(1L);
        verify(orgUnitRepository).findByParentIsNullAndUseYnOrderBySortOrderAsc("Y");
    }

    @Test
    void getChildren_withParent_usesParentQuery() {
        OrgUnit root = unit(1L, null, "본부", 0);
        when(orgUnitRepository.findByParent_UnitIdAndUseYnOrderBySortOrderAsc(1L, "Y"))
                .thenReturn(List.of(unit(2L, root, "부서", 0)));
        List<OrgUnitNode> out = service.getChildren(1L);
        assertThat(out).extracting(OrgUnitNode::unitId).containsExactly(2L);
        assertThat(out.get(0).parentId()).isEqualTo(1L);
    }

    @Test
    void getRoots_delegatesToNullParent() {
        when(orgUnitRepository.findByParentIsNullAndUseYnOrderBySortOrderAsc("Y")).thenReturn(List.of());
        service.getRoots();
        verify(orgUnitRepository).findByParentIsNullAndUseYnOrderBySortOrderAsc("Y");
    }

    // ===== getPath =====

    @Test
    void getPath_walksParentChain_rootToTarget() {
        OrgUnit root = unit(1L, null, "본부", 0);
        OrgUnit dept = unit(2L, root, "부서", 0);
        OrgUnit team = unit(3L, dept, "팀", 0);
        when(orgUnitRepository.findById(3L)).thenReturn(Optional.of(team));
        assertThat(service.getPath(3L)).containsExactly("본부", "부서", "팀");
    }

    @Test
    void getPath_unknownId_returnsEmpty() {
        when(orgUnitRepository.findById(9L)).thenReturn(Optional.empty());
        assertThat(service.getPath(9L)).isEmpty();
    }

    // ===== create =====

    @Test
    void create_validRoot_savesWithDefaults() {
        OrgUnitNode node = service.create(null, "DIVISION", "  본부  ", null);
        assertThat(node.name()).isEqualTo("본부");          // trim
        assertThat(node.unitType()).isEqualTo("DIVISION");
        assertThat(node.sortOrder()).isZero();             // null → 0
        assertThat(node.parentId()).isNull();
    }

    @Test
    void create_withParentAndSortOrder() {
        OrgUnit parent = unit(1L, null, "본부", 0);
        when(orgUnitRepository.findById(1L)).thenReturn(Optional.of(parent));
        OrgUnitNode node = service.create(1L, "TEAM", "팀A", 5);
        assertThat(node.parentId()).isEqualTo(1L);
        assertThat(node.sortOrder()).isEqualTo(5);
    }

    @Test
    void create_invalidType_throws() {
        assertThatThrownBy(() -> service.create(null, "COMPANY", "x", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("unit_type");
    }

    @Test
    void create_blankName_throws() {
        assertThatThrownBy(() -> service.create(null, "TEAM", "   ", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("name");
    }

    @Test
    void create_parentNotFound_throws() {
        when(orgUnitRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.create(99L, "TEAM", "팀", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("상위 조직 없음");
    }

    // ===== update =====

    @Test
    void update_notFound_throws() {
        when(orgUnitRepository.findById(9L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.update(9L, "x", 1, "Y"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("조직 없음");
    }

    @Test
    void update_appliesValidFields() {
        OrgUnit u = unit(1L, null, "old", 0);
        when(orgUnitRepository.findById(1L)).thenReturn(Optional.of(u));
        service.update(1L, "  new  ", 9, "N");
        assertThat(u.getName()).isEqualTo("new");   // trim
        assertThat(u.getSortOrder()).isEqualTo(9);
        assertThat(u.getUseYn()).isEqualTo("N");
    }

    @Test
    void update_invalidUseYn_andBlankName_ignored() {
        OrgUnit u = unit(1L, null, "keep", 3);
        u.setUseYn("Y");
        when(orgUnitRepository.findById(1L)).thenReturn(Optional.of(u));
        service.update(1L, "   ", null, "X");   // name blank·sortOrder null·useYn 부적합
        assertThat(u.getName()).isEqualTo("keep");   // 보존
        assertThat(u.getSortOrder()).isEqualTo(3);
        assertThat(u.getUseYn()).isEqualTo("Y");     // X 무시
    }

    // ===== delete =====

    @Test
    void delete_notFound_throws() {
        when(orgUnitRepository.findById(9L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.delete(9L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("조직 없음");
    }

    @Test
    void delete_hasChildren_throws() {
        OrgUnit u = unit(1L, null, "본부", 0);
        when(orgUnitRepository.findById(1L)).thenReturn(Optional.of(u));
        when(orgUnitRepository.existsByParent_UnitIdAndUseYn(1L, "Y")).thenReturn(true);
        assertThatThrownBy(() -> service.delete(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("하위 유닛");
        verify(orgUnitRepository, never()).delete(any());
    }

    @Test
    void delete_leaf_deletes() {
        OrgUnit u = unit(2L, null, "팀", 0);
        when(orgUnitRepository.findById(2L)).thenReturn(Optional.of(u));
        when(orgUnitRepository.existsByParent_UnitIdAndUseYn(2L, "Y")).thenReturn(false);
        service.delete(2L);
        verify(orgUnitRepository).delete(u);
    }
}
