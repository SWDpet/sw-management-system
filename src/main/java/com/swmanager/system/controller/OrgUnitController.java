package com.swmanager.system.controller;

import com.swmanager.system.domain.ops.Staff;
import com.swmanager.system.repository.ops.StaffRepository;
import com.swmanager.system.response.ApiResult;
import com.swmanager.system.service.OrgUnitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
    public List<Map<String, Object>> getTree() {
        return orgUnitService.getTree();
    }

    @GetMapping("/api/org-units/roots")
    @ResponseBody
    public List<Map<String, Object>> getRoots() {
        return orgUnitService.getRoots();
    }

    @GetMapping("/api/org-units/children/{parentId}")
    @ResponseBody
    public List<Map<String, Object>> getChildren(@PathVariable Long parentId) {
        return orgUnitService.getChildren(parentId);
    }

    /** [ops-fault-support M1/FR-M1-3] 조직 단위 소속 인원(재직/퇴사 포함). */
    @GetMapping("/api/org-units/{unitId}/members")
    @ResponseBody
    public List<Map<String, Object>> getMembers(@PathVariable Long unitId) {
        return orgUnitService.getMembers(unitId);
    }

    // ======== 관리 화면 (ADMIN) ========

    @GetMapping("/admin/org-units")
    public String manage(Model model) {
        return "admin/org-unit-management";
    }

    @PostMapping("/admin/api/org-units")
    @ResponseBody
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        try {
            Long parentId = body.get("parent_id") != null ? ((Number) body.get("parent_id")).longValue() : null;
            String unitType = (String) body.get("unit_type");
            String name = (String) body.get("name");
            Integer sortOrder = body.get("sort_order") != null ? ((Number) body.get("sort_order")).intValue() : 0;
            Map<String, Object> created = orgUnitService.create(parentId, unitType, name, sortOrder);
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException e) {
            return error(400, "INVALID_INPUT", e.getMessage());
        }
    }

    @PutMapping("/admin/api/org-units/{unitId}")
    @ResponseBody
    public ResponseEntity<?> update(@PathVariable Long unitId, @RequestBody Map<String, Object> body) {
        try {
            String name = (String) body.get("name");
            Integer sortOrder = body.get("sort_order") != null ? ((Number) body.get("sort_order")).intValue() : null;
            String useYn = (String) body.get("use_yn");
            return ResponseEntity.ok(orgUnitService.update(unitId, name, sortOrder, useYn));
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
    public ResponseEntity<?> createStaff(@RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        if (name == null || name.isBlank()) return error(400, "INVALID_INPUT", "이름은 필수입니다.");
        Staff s = new Staff();
        applyStaff(s, body);
        Map<String, Object> ok = new LinkedHashMap<>();
        ok.put("success", true);
        ok.put("staff_id", staffRepository.save(s).getStaffId());
        return ResponseEntity.ok(ok);
    }

    @PutMapping("/admin/api/staff/{id}")
    @ResponseBody
    public ResponseEntity<?> updateStaff(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Staff s = staffRepository.findById(id).orElse(null);
        if (s == null) return error(404, "NOT_FOUND", "직원 없음: " + id);
        applyStaff(s, body);
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

    private void applyStaff(Staff s, Map<String, Object> body) {
        if (body.get("name") != null) s.setName((String) body.get("name"));
        s.setPosition((String) body.get("position"));
        if (body.containsKey("org_unit_id")) {
            Object v = body.get("org_unit_id");
            s.setOrgUnitId(v instanceof Number ? ((Number) v).longValue() : null);
        }
        if (body.get("active") != null) s.setActive(Boolean.TRUE.equals(body.get("active")));
        s.setTel((String) body.get("tel"));
    }

    private ResponseEntity<?> error(int status, String code, String message) {
        // [dto-migration] {success:false, error:{code,message}} — ApiResult.fail 과 동일 형태.
        return ResponseEntity.status(status).body(ApiResult.fail(code, message));
    }
}
