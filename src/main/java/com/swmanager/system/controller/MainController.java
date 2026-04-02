package com.swmanager.system.controller;

import com.swmanager.system.repository.SwProjectRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
public class MainController {

    @Autowired
    private SwProjectRepository swProjectRepository;

    @GetMapping("/")
    public String mainDashboard(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(value = "year", required = false) Integer year,
            Model model) {

        if (userDetails != null) {
            log.info("[대시보드 접속] 유저: {}", userDetails.getUsername());
        }

        // year=null → 첫 로드(기본값 = 올해)
        // year=0   → "전체 연도" 선택 (전체 조회)
        // year=2026 등 → 해당 연도 조회
        Integer queryYear;
        if (year == null) {
            year = LocalDate.now().getYear();  // 첫 로드 시 현재 연도
            queryYear = year;
        } else if (year == 0) {
            queryYear = null;  // 전체 조회
            year = null;       // 화면에 "전체" 표시
        } else {
            queryYear = year;
        }

        long totalProjects   = 0;
        long totalCompleted  = 0;
        long totalInProgress = 0;
        long totalContAmt    = 0; // 모든 시스템의 금액합계(sumCont) 총합
        long totalSumSw    = 0;
        long totalSumCnslt = 0;
        long totalSumDb    = 0;
        long totalSumPkg   = 0;
        long totalSumDev   = 0;
        long totalSumHw    = 0;
        long totalSumEtc   = 0;

        try {
            List<Integer> years = swProjectRepository.findDistinctYears();
            model.addAttribute("years", years);
            model.addAttribute("selectedYear", year);        // null이면 "전체"

            List<Map<String, Object>> stats = swProjectRepository.getSystemStats(queryYear);
            model.addAttribute("stats", stats);

            if (stats != null && !stats.isEmpty()) {
                for (Map<String, Object> row : stats) {
                    Object totalCntObj = row.get("totalCnt");
                    Object compCntObj  = row.get("compCnt");
                    Object progCntObj  = row.get("progCnt");
                    Object sumContObj  = row.get("sumCont");

                    if (totalCntObj != null) totalProjects   += ((Number) totalCntObj).longValue();
                    if (compCntObj  != null) totalCompleted  += ((Number) compCntObj).longValue();
                    if (progCntObj  != null) totalInProgress += ((Number) progCntObj).longValue();
                    if (sumContObj  != null) totalContAmt    += ((Number) sumContObj).longValue();

                    Object sumSwObj    = row.get("sumSw");
                    Object sumCnsltObj = row.get("sumCnslt");
                    Object sumDbObj    = row.get("sumDb");
                    Object sumPkgObj   = row.get("sumPkg");
                    Object sumDevObj   = row.get("sumDev");
                    Object sumHwObj    = row.get("sumHw");
                    Object sumEtcObj   = row.get("sumEtc");
                    if (sumSwObj    != null) totalSumSw    += ((Number) sumSwObj).longValue();
                    if (sumCnsltObj != null) totalSumCnslt += ((Number) sumCnsltObj).longValue();
                    if (sumDbObj    != null) totalSumDb    += ((Number) sumDbObj).longValue();
                    if (sumPkgObj   != null) totalSumPkg   += ((Number) sumPkgObj).longValue();
                    if (sumDevObj   != null) totalSumDev   += ((Number) sumDevObj).longValue();
                    if (sumHwObj    != null) totalSumHw    += ((Number) sumHwObj).longValue();
                    if (sumEtcObj   != null) totalSumEtc   += ((Number) sumEtcObj).longValue();
                }
            }

        } catch (Exception e) {
            log.error("[대시보드 데이터 처리 에러] {}", e.getMessage(), e);
        }

        // 완료율 = 완료건수 / 전체건수
        double compPercent = totalProjects > 0
                ? Math.round((double) totalCompleted / totalProjects * 1000) / 10.0
                : 0.0;

        // 진행율 = 진행건수 / 전체건수
        double progPercent = totalProjects > 0
                ? Math.round((double) totalInProgress / totalProjects * 1000) / 10.0
                : 0.0;

        model.addAttribute("totalProjects",   totalProjects);
        model.addAttribute("totalCompleted",  totalCompleted);
        model.addAttribute("totalInProgress", totalInProgress);
        model.addAttribute("compPercent",     compPercent);
        model.addAttribute("progPercent",     progPercent);
        model.addAttribute("totalContAmt",    totalContAmt);
        model.addAttribute("totalSumSw",    totalSumSw);
        model.addAttribute("totalSumCnslt", totalSumCnslt);
        model.addAttribute("totalSumDb",    totalSumDb);
        model.addAttribute("totalSumPkg",   totalSumPkg);
        model.addAttribute("totalSumDev",   totalSumDev);
        model.addAttribute("totalSumHw",    totalSumHw);
        model.addAttribute("totalSumEtc",   totalSumEtc);

        return "main-dashboard";
    }
}
