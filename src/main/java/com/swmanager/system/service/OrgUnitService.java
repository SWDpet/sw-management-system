package com.swmanager.system.service;

import com.swmanager.system.domain.OrgUnit;
import com.swmanager.system.domain.ops.Staff;
import com.swmanager.system.dto.orgunit.OrgMemberRow;
import com.swmanager.system.dto.orgunit.OrgUnitNode;
import com.swmanager.system.repository.OrgUnitRepository;
import com.swmanager.system.repository.ops.StaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 조직도 CRUD + 트리 조회.
 * 응답 DTO 는 NFR-5 준수 — unit_id/name/unit_type/parent_id/sort_order/children 만 노출.
 * (use_yn/created_at/updated_at 는 내부 관리용)
 */
@Service
@Transactional
public class OrgUnitService {

    @Autowired
    private OrgUnitRepository orgUnitRepository;

    @Autowired
    private StaffRepository staffRepository;

    // ======== 조회 ========

    /**
     * [ops-fault-support staff] 조직 단위 소속 직원(퇴사 포함, 재직/퇴사 배지용).
     * 소스 = tb_staff(직원 디렉터리). 노출: staff_id/username(이름)/position/active.
     */
    @Transactional(readOnly = true)
    public List<OrgMemberRow> getMembers(Long unitId) {
        List<OrgMemberRow> result = new ArrayList<>();
        for (Staff s : staffRepository.findByOrgUnitIdOrderBySortOrderAscNameAsc(unitId)) {
            result.add(OrgMemberRow.from(s));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getTree() {
        List<OrgUnit> all = orgUnitRepository.findAllByUseYnOrderBySortOrderAsc("Y");
        // id → node
        Map<Long, Map<String, Object>> nodeMap = new LinkedHashMap<>();
        for (OrgUnit u : all) {
            Map<String, Object> node = toDto(u);
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

    @Transactional(readOnly = true)
    public List<OrgUnitNode> getChildren(Long parentId) {
        List<OrgUnit> units = (parentId == null)
                ? orgUnitRepository.findByParentIsNullAndUseYnOrderBySortOrderAsc("Y")
                : orgUnitRepository.findByParent_UnitIdAndUseYnOrderBySortOrderAsc(parentId, "Y");
        List<OrgUnitNode> result = new ArrayList<>();
        for (OrgUnit u : units) result.add(OrgUnitNode.from(u));
        return result;
    }

    @Transactional(readOnly = true)
    public List<OrgUnitNode> getRoots() {
        return getChildren(null);
    }

    /** 경로(루트→대상) 이름 배열. FR-2-F 표시용. */
    @Transactional(readOnly = true)
    public List<String> getPath(Long unitId) {
        List<String> path = new ArrayList<>();
        OrgUnit cur = orgUnitRepository.findById(unitId).orElse(null);
        while (cur != null) {
            path.add(0, cur.getName());
            cur = cur.getParent();
        }
        return path;
    }

    // ======== CRUD ========

    public OrgUnitNode create(Long parentId, String unitType, String name, Integer sortOrder) {
        validateType(unitType);
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("name 은 필수입니다.");
        }
        OrgUnit u = new OrgUnit();
        if (parentId != null) {
            OrgUnit parent = orgUnitRepository.findById(parentId)
                    .orElseThrow(() -> new IllegalArgumentException("상위 조직 없음: " + parentId));
            u.setParent(parent);
        }
        u.setUnitType(unitType);
        u.setName(name.trim());
        u.setSortOrder(sortOrder != null ? sortOrder : 0);
        u.setUseYn("Y");
        return OrgUnitNode.from(orgUnitRepository.save(u));
    }

    public OrgUnitNode update(Long unitId, String name, Integer sortOrder, String useYn) {
        OrgUnit u = orgUnitRepository.findById(unitId)
                .orElseThrow(() -> new IllegalArgumentException("조직 없음: " + unitId));
        if (name != null && !name.trim().isEmpty()) u.setName(name.trim());
        if (sortOrder != null) u.setSortOrder(sortOrder);
        if (useYn != null && (useYn.equals("Y") || useYn.equals("N"))) u.setUseYn(useYn);
        return OrgUnitNode.from(orgUnitRepository.save(u));
    }

    public void delete(Long unitId) {
        OrgUnit u = orgUnitRepository.findById(unitId)
                .orElseThrow(() -> new IllegalArgumentException("조직 없음: " + unitId));
        // 하위 존재 시 차단
        if (orgUnitRepository.existsByParent_UnitIdAndUseYn(unitId, "Y")) {
            throw new IllegalStateException("하위 유닛이 존재하므로 삭제 불가");
        }
        orgUnitRepository.delete(u);
    }

    // ======== 헬퍼 ========

    private Map<String, Object> toDto(OrgUnit u) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("unit_id", u.getUnitId());
        m.put("name", u.getName());
        m.put("unit_type", u.getUnitType());
        m.put("parent_id", u.getParent() != null ? u.getParent().getUnitId() : null);
        m.put("sort_order", u.getSortOrder());
        return m;
    }

    private void validateType(String type) {
        if (!Arrays.asList("DIVISION", "DEPARTMENT", "TEAM").contains(type)) {
            throw new IllegalArgumentException("unit_type 은 DIVISION/DEPARTMENT/TEAM 만 허용: " + type);
        }
    }
}
