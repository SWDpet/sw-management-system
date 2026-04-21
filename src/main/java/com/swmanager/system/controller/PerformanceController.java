package com.swmanager.system.controller;

import com.swmanager.system.config.CustomUserDetails;
import com.swmanager.system.constant.enums.AccessActionType;
import com.swmanager.system.constants.MenuName;
import com.swmanager.system.domain.User;
import com.swmanager.system.domain.workplan.PerformanceSummary;
import com.swmanager.system.repository.UserRepository;
import com.swmanager.system.service.ExcelExportService;
import com.swmanager.system.service.LogService;
import com.swmanager.system.service.PerformanceService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Controller
@RequestMapping("/performance")
public class PerformanceController {

    @Autowired private PerformanceService performanceService;
    @Autowired private ExcelExportService excelExportService;
    @Autowired private UserRepository userRepository;
    @Autowired private LogService logService;

    private CustomUserDetails getCurrentUser() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) return null;
            Object principal = auth.getPrincipal();
            if (principal instanceof CustomUserDetails) return (CustomUserDetails) principal;
            return null;
        } catch (Exception e) { return null; }
    }

    private boolean isAdmin() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            return auth != null && auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        } catch (Exception e) { return false; }
    }

    private String getAuth() {
        if (isAdmin()) return "EDIT";
        CustomUserDetails cu = getCurrentUser();
        if (cu == null) return "NONE";
        String auth = cu.getUser().getAuthPerformance();
        return (auth != null) ? auth : "NONE";
    }

    // === S-01: 개인 성과 대시보드 ===

    @GetMapping("/personal")
    public String personalDashboard(@RequestParam(name = "year", required = false) Integer year,
                                     @RequestParam(name = "userId", required = false) Long userId,
                                     Model model, RedirectAttributes rttr) {
        String auth = getAuth();
        if ("NONE".equals(auth)) {
            rttr.addFlashAttribute("errorMessage", "접근 권한이 없습니다.");
            return "redirect:/";
        }

        if (year == null) year = LocalDate.now().getYear();
        CustomUserDetails cu = getCurrentUser();
        if (userId == null && cu != null) userId = cu.getUser().getUserSeq();

        List<PerformanceSummary> monthlyData = performanceService.getPersonalYearlyPerformance(userId, year);

        // 차트 데이터 준비
        List<Integer> months = new ArrayList<>();
        List<Integer> installs = new ArrayList<>(), patches = new ArrayList<>(), faults = new ArrayList<>(),
                supports = new ArrayList<>(), inspects = new ArrayList<>();

        for (int m = 1; m <= 12; m++) {
            months.add(m);
            int mm = m;
            var data = monthlyData.stream()
                    .filter(s -> s.getPeriodMonth() == mm)
                    .findFirst();
            installs.add(data.map(s -> s.getInstallCount() != null ? s.getInstallCount() : 0).orElse(0));
            patches.add(data.map(s -> s.getPatchCount() != null ? s.getPatchCount() : 0).orElse(0));
            faults.add(data.map(s -> s.getFaultCount() != null ? s.getFaultCount() : 0).orElse(0));
            supports.add(data.map(s -> s.getSupportCount() != null ? s.getSupportCount() : 0).orElse(0));
            inspects.add(data.map(s -> s.getInspectDoneCount() != null ? s.getInspectDoneCount() : 0).orElse(0));
        }

        // 연간 합계
        Map<String, Object> totals = performanceService.getAggregatedPerformance(userId, year, 1, year, 12);

        User targetUser = userRepository.findById(userId).orElse(null);

        model.addAttribute("year", year);
        model.addAttribute("userId", userId);
        model.addAttribute("targetUser", targetUser);
        model.addAttribute("monthlyData", monthlyData);
        model.addAttribute("months", months);
        model.addAttribute("installs", installs);
        model.addAttribute("patches", patches);
        model.addAttribute("faults", faults);
        model.addAttribute("supports", supports);
        model.addAttribute("inspects", inspects);
        model.addAttribute("totals", totals);
        model.addAttribute("users", userRepository.findByEnabledTrue());
        model.addAttribute("userAuth", auth);

        logService.log(MenuName.PERFORMANCE, AccessActionType.VIEW, "개인 성과 대시보드 조회 (사용자ID: " + userId + ", " + year + "년)");
        return "performance/personal-dashboard";
    }

    // === S-02: 부서 성과 대시보드 ===

    @GetMapping("/dept")
    public String deptDashboard(@RequestParam(name = "year", required = false) Integer year,
                                 @RequestParam(name = "month", required = false) Integer month,
                                 Model model, RedirectAttributes rttr) {
        String auth = getAuth();
        if ("NONE".equals(auth)) {
            rttr.addFlashAttribute("errorMessage", "접근 권한이 없습니다.");
            return "redirect:/";
        }

        if (year == null) year = LocalDate.now().getYear();
        if (month == null) month = LocalDate.now().getMonthValue();

        List<PerformanceSummary> deptData = performanceService.getDeptMonthlyPerformance(year, month);

        // 차트용 데이터
        List<String> userNames = new ArrayList<>();
        List<Integer> totalCounts = new ArrayList<>();

        for (PerformanceSummary ps : deptData) {
            userNames.add(ps.getUser().getUsername());
            int total = safe(ps.getInstallCount()) + safe(ps.getPatchCount()) + safe(ps.getFaultCount()) +
                         safe(ps.getSupportCount()) + safe(ps.getInspectDoneCount());
            totalCounts.add(total);
        }

        model.addAttribute("year", year);
        model.addAttribute("month", month);
        model.addAttribute("deptData", deptData);
        model.addAttribute("userNames", userNames);
        model.addAttribute("totalCounts", totalCounts);
        model.addAttribute("userAuth", auth);

        logService.log(MenuName.PERFORMANCE, AccessActionType.VIEW, "부서 성과 대시보드 조회 (" + year + "년 " + month + "월)");
        return "performance/dept-dashboard";
    }

    // === S-03: 성과 리포트 / 엑셀 다운로드 ===

    @GetMapping("/report")
    public String reportPage(@RequestParam(name = "userId", required = false) Long userId,
                              @RequestParam(name = "fromYear", required = false) Integer fromYear,
                              @RequestParam(name = "fromMonth", required = false) Integer fromMonth,
                              @RequestParam(name = "toYear", required = false) Integer toYear,
                              @RequestParam(name = "toMonth", required = false) Integer toMonth,
                              Model model, RedirectAttributes rttr) {
        String auth = getAuth();
        if ("NONE".equals(auth)) {
            rttr.addFlashAttribute("errorMessage", "접근 권한이 없습니다.");
            return "redirect:/";
        }

        int currentYear = LocalDate.now().getYear();
        if (fromYear == null) fromYear = currentYear;
        if (fromMonth == null) fromMonth = 1;
        if (toYear == null) toYear = currentYear;
        if (toMonth == null) toMonth = 12;

        Map<String, Object> reportData = null;
        User targetUser = null;
        if (userId != null) {
            reportData = performanceService.getAggregatedPerformance(userId, fromYear, fromMonth, toYear, toMonth);
            targetUser = userRepository.findById(userId).orElse(null);
        }

        model.addAttribute("userId", userId);
        model.addAttribute("fromYear", fromYear);
        model.addAttribute("fromMonth", fromMonth);
        model.addAttribute("toYear", toYear);
        model.addAttribute("toMonth", toMonth);
        model.addAttribute("reportData", reportData);
        model.addAttribute("targetUser", targetUser);
        model.addAttribute("users", userRepository.findByEnabledTrue());
        model.addAttribute("userAuth", auth);

        return "performance/performance-report";
    }

    // === 성과 집계 실행 (관리자) ===

    @ResponseBody
    @PostMapping("/api/calculate")
    public ResponseEntity<Map<String, Object>> calculatePerformance(
            @RequestParam int year, @RequestParam int month) {
        if (!isAdmin()) {
            return ResponseEntity.status(403).body(Map.of("error", "관리자만 실행 가능합니다."));
        }

        int count = performanceService.calculateAllUsersMonthlyPerformance(year, month);
        logService.log(MenuName.PERFORMANCE, AccessActionType.BATCH, year + "년 " + month + "월 성과 집계 (" + count + "명)");
        return ResponseEntity.ok(Map.of("success", true, "count", count));
    }

    // === S-03: 엑셀 다운로드 ===

    @ResponseBody
    @GetMapping("/api/excel")
    public ResponseEntity<byte[]> downloadExcel(
            @RequestParam Long userId,
            @RequestParam int fromYear, @RequestParam int fromMonth,
            @RequestParam int toYear, @RequestParam int toMonth) {

        String auth = getAuth();
        if ("NONE".equals(auth)) {
            return ResponseEntity.status(403).build();
        }

        try {
            Map<String, Object> reportData = performanceService.getAggregatedPerformance(userId, fromYear, fromMonth, toYear, toMonth);
            @SuppressWarnings("unchecked")
            List<PerformanceSummary> details = (List<PerformanceSummary>) reportData.get("details");

            User targetUser = userRepository.findById(userId).orElse(null);
            String userName = targetUser != null ? targetUser.getUsername() : "Unknown";

            byte[] excelBytes = excelExportService.generatePerformanceReport(
                    userName, fromYear, fromMonth, toYear, toMonth, details, reportData);

            String fileName = "performance_" + userName + "_" + fromYear + fromMonth + "_" + toYear + toMonth + ".xlsx";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", fileName);

            logService.log(MenuName.PERFORMANCE, AccessActionType.DOWNLOAD, "엑셀 다운로드 (" + userName + ", " + fromYear + "." + fromMonth + "~" + toYear + "." + toMonth + ")");

            return ResponseEntity.ok().headers(headers).body(excelBytes);
        } catch (Exception e) {
            log.error("엑셀 다운로드 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    private int safe(Integer val) {
        return val != null ? val : 0;
    }
}
