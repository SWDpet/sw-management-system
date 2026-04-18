package com.swmanager.system.controller;

import com.swmanager.system.dto.ErdGraphDTO;
import com.swmanager.system.service.ErdGraphService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 시스템 관계도
 * - 페이지: /admin/system-graph (4탭 UI)
 * - API:    /admin/system-graph/api/erd  (Phase 1 C: ERD 인터랙티브)
 */
@Controller
@RequestMapping("/admin/system-graph")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class SystemGraphController {

    private final ErdGraphService erdGraphService;

    @GetMapping
    public String view() {
        return "admin-system-graph";
    }

    /** Phase 1 C — ERD 인터랙티브 데이터 (Specs: FR-2). */
    @GetMapping("/api/erd")
    @ResponseBody
    public ErdGraphDTO getErdGraph() {
        return erdGraphService.getGraph();
    }
}
