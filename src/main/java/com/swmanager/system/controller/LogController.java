package com.swmanager.system.controller;

import com.swmanager.system.domain.AccessLog;
import com.swmanager.system.repository.AccessLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/admin/logs")
@PreAuthorize("hasRole('ADMIN')")
public class LogController {

    @Autowired
    private AccessLogRepository accessLogRepository;

    /**
     * 로그관리 — 탭 분리(접속자 로그 / 메뉴·행위 로그) + 기간·키워드 필터.
     * tab=access → menu_nm='접속'(로그인/로그아웃), tab=menu(기본) → 그 외.
     */
    @GetMapping
    public String viewLogs(Model model,
                           @RequestParam(value = "tab", defaultValue = "menu") String tab,
                           @RequestParam(value = "page", defaultValue = "0") int page,
                           @RequestParam(value = "kw", defaultValue = "") String kw,
                           @RequestParam(value = "from", required = false)
                           @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                           @RequestParam(value = "to", required = false)
                           @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        String kwNorm = (kw == null || kw.isBlank()) ? null : kw.trim();
        LocalDateTime fromStart = (from != null) ? from.atStartOfDay() : null;
        LocalDateTime toExclusive = (to != null) ? to.plusDays(1).atStartOfDay() : null;
        Pageable pageable = PageRequest.of(page, 20);  // 정렬은 쿼리 ORDER BY 담당

        Page<AccessLog> logs;
        if ("access".equals(tab)) {
            logs = accessLogRepository.findAccessTab(pageable, kwNorm, fromStart, toExclusive);
        } else {
            tab = "menu";
            logs = accessLogRepository.findMenuTab(pageable, kwNorm, fromStart, toExclusive);
        }

        model.addAttribute("logs", logs);
        model.addAttribute("tab", tab);
        model.addAttribute("kw", kw);
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        return "admin-logs";
    }
}
