package com.swmanager.system.controller;

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
            Map<String, Object> ok = new LinkedHashMap<>();
            ok.put("success", true);
            return ResponseEntity.ok(ok);
        } catch (IllegalArgumentException e) {
            return error(400, "NOT_FOUND", e.getMessage());
        } catch (IllegalStateException e) {
            return error(400, "HAS_CHILDREN", e.getMessage());
        }
    }

    private ResponseEntity<?> error(int status, String code, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("error", Map.of("code", code, "message", message));
        return ResponseEntity.status(status).body(body);
    }
}
