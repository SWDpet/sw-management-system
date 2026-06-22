package com.swmanager.system.controller;

import com.swmanager.system.domain.ops.Staff;
import com.swmanager.system.dto.orgunit.OrgMemberRow;
import com.swmanager.system.dto.orgunit.OrgTreeNode;
import com.swmanager.system.dto.orgunit.OrgUnitForm;
import com.swmanager.system.dto.orgunit.OrgUnitNode;
import com.swmanager.system.dto.orgunit.StaffCreateResult;
import com.swmanager.system.dto.orgunit.StaffForm;
import com.swmanager.system.repository.ops.StaffRepository;
import com.swmanager.system.response.ApiResult;
import com.swmanager.system.service.OrgUnitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 조직도 조회(/api) + 관리 화면(/admin).
 *
 * 스프린트 5 (2026-04-19) — docs/product-specs/doc-selector-org-env.md
 *
 * 접근 통제: SecurityConfig 에 의해 /admin/** 은 ADMIN, /api/org-units/** 는 인증 사용자.
 */
@Controller
public class OrgUnitController {

    @Autowired
    private OrgUnitService orgUnitService;

    @Autowired
    private StaffRepository staffRepository;   // [staff] 직원 관리

    // ======== 조회 API (인증 사용자) ========

    @GetMapping("/api/org-units/tree")
    @ResponseBody
    public List<OrgTreeNode> getTree() {
        return orgUnitService.getTree();
    }

    @GetMapping("/api/org-units/roots")
    @ResponseBody
    public List<OrgUnitNode> getRoots() {
        return orgUnitService.getRoots();
    }

    @GetMapping("/api/org-units/children/{parentId}")
    @ResponseBody
    public List<OrgUnitNode> getChildren(@PathVariable Long parentId) {
        return orgUnitService.getChildren(parentId);
    }

    /** [ops-fault-support M1/FR-M1-3] 조직 단위 소속 인원(재직/퇴사 포함). */
    @GetMapping("/api/org-units/{unitId}/members")
    @ResponseBody
    public List<OrgMemberRow> getMembers(@PathVariable Long unitId) {
        return orgUnitService.getMembers(unitId);
    }

    // ======== 관리 화면 (ADMIN) ========

    @GetMapping("/admin/org-units")
    public String manage(Model model) {
        return "admin/org-unit-management";
    }

    @PostMapping("/admin/api/org-units")
    @ResponseBody
    public ResponseEntity<?> create(@RequestBody OrgUnitForm form) {
        try {
            Integer sortOrder = form.sortOrder() != null ? form.sortOrder() : 0;
            OrgUnitNode created = orgUnitService.create(form.parentId(), form.unitType(), form.name(), sortOrder);
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException e) {
            return error(400, "INVALID_INPUT", e.getMessage());
        }
    }

    @PutMapping("/admin/api/org-units/{unitId}")
    @ResponseBody
    public ResponseEntity<?> update(@PathVariable Long unitId, @RequestBody OrgUnitForm form) {
        try {
            return ResponseEntity.ok(orgUnitService.update(unitId, form.name(), form.sortOrder(), form.useYn()));
        } catch (IllegalArgumentException e) {
            return error(400, "INVALID_INPUT", e.getMessage());
        }
    }

    @DeleteMapping("/admin/api/org-units/{unitId}")
    @ResponseBody
    public ResponseEntity<?> delete(@PathVariable Long unitId) {
        try {
            orgUnitService.delete(unitId);
            return ResponseEntity.ok(ApiResult.ok());
        } catch (IllegalArgumentException e) {
            return error(400, "NOT_FOUND", e.getMessage());
        } catch (IllegalStateException e) {
            return error(400, "HAS_CHILDREN", e.getMessage());
        }
    }

    // ======== [staff] 직원 관리 (ADMIN) — 조직도 인원 추가/수정/삭제 ========

    @PostMapping("/admin/api/staff")
    @ResponseBody
    public ResponseEntity<?> createStaff(@RequestBody StaffForm form) {
        String name = form.name();
        if (name == null || name.isBlank()) return error(400, "INVALID_INPUT", "이름은 필수입니다.");
        Staff s = new Staff();
        applyStaff(s, form);
        return ResponseEntity.ok(new StaffCreateResult(true, staffRepository.save(s).getStaffId()));
    }

    @PutMapping("/admin/api/staff/{id}")
    @ResponseBody
    public ResponseEntity<?> updateStaff(@PathVariable Long id, @RequestBody StaffForm form) {
        Staff s = staffRepository.findById(id).orElse(null);
        if (s == null) return error(404, "NOT_FOUND", "직원 없음: " + id);
        applyStaff(s, form);
        staffRepository.save(s);
        return ResponseEntity.ok(ApiResult.ok());
    }

    @DeleteMapping("/admin/api/staff/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteStaff(@PathVariable Long id) {
        try {
            staffRepository.deleteById(id);
            return ResponseEntity.ok(ApiResult.ok());
        } catch (Exception e) {
            return error(400, "IN_USE", "문서 요청자로 사용 중인 직원은 삭제할 수 없습니다.");
        }
    }

    private void applyStaff(Staff s, StaffForm form) {
        if (form.name() != null) s.setName(form.name());
        s.setPosition(form.position());
        // 수정 시 프론트가 org_unit_id 를 생략(명시 null 미전송) → null → skip → 기존 유닛 보존(현행 containsKey 등가).
        if (form.orgUnitId() != null) s.setOrgUnitId(form.orgUnitId());
        if (form.active() != null) s.setActive(form.active());
        s.setTel(form.tel());
    }

    private ResponseEntity<?> error(int status, String code, String message) {
        // [dto-migration] {success:false, error:{code,message}} — ApiResult.fail 과 동일 형태.
        return ResponseEntity.status(status).body(ApiResult.fail(code, message));
    }
}
