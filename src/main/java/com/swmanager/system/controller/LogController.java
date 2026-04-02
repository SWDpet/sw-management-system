package com.swmanager.system.controller;

import com.swmanager.system.domain.AccessLog;
import com.swmanager.system.repository.AccessLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/logs")
@PreAuthorize("hasRole('ADMIN')")
public class LogController {

    @Autowired
    private AccessLogRepository accessLogRepository;

    @GetMapping
    public String viewLogs(Model model, 
                           @RequestParam(value = "page", defaultValue = "0") int page,
                           @RequestParam(value = "kw", defaultValue = "") String kw) {
        
        // 페이지당 20개, 최신순 정렬
        Pageable pageable = PageRequest.of(page, 20, Sort.by("accessTime").descending());
        
        // 검색어가 있으면 검색, 없으면 전체 조회 (Repository 메서드 활용)
        Page<AccessLog> logs = accessLogRepository.findAllWithSearch(pageable, kw);

        model.addAttribute("logs", logs);
        model.addAttribute("kw", kw);
        
        return "admin-logs"; // admin-logs.html 파일을 찾아서 반환
    }
}