package com.swmanager.system.controller;

import com.swmanager.system.repository.SwProjectRepository;
import com.swmanager.system.domain.workplan.WorkPlan;
import com.swmanager.system.repository.workplan.WorkPlanRepository;
import com.swmanager.system.geonuris.domain.GeonurisLicense;
import com.swmanager.system.geonuris.repository.GeonurisLicenseRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
public class MainController {

    @Autowired
    private SwProjectRepository swProjectRepository;
    @Autowired
    private WorkPlanRepository workPlanRepository;
    @Autowired
    private GeonurisLicenseRepository geonurisLicenseRepository;

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

    // ===== [dashboard-preview] 통합 구조 + 완료/진행 전년대비 증감 (실데이터 미리보기) =====
    @GetMapping("/dashboard-preview")
    public String dashboardPreview(
            @RequestParam(value = "year", required = false) Integer year, Model model) {

        Integer queryYear;
        if (year == null)      { year = LocalDate.now().getYear(); queryYear = year; }
        else if (year == 0)    { queryYear = null; year = null; }
        else                   { queryYear = year; }

        model.addAttribute("years", swProjectRepository.findDistinctYears());
        model.addAttribute("selectedYear", year);

        List<Map<String, Object>> stats = swProjectRepository.getSystemStats(queryYear);
        if (stats == null) stats = new ArrayList<>();

        // 전년 대비 증감 맵 (sysNm → [완료, 진행])
        Map<String, long[]> prevMap = new HashMap<>();
        if (queryYear != null) {
            List<Map<String, Object>> prev = swProjectRepository.getSystemStats(queryYear - 1);
            if (prev != null) for (Map<String, Object> r : prev)
                prevMap.put(str(r.get("sysNm")), new long[]{ lng(r.get("compCnt")), lng(r.get("progCnt")) });
        }

        long tProj=0,tComp=0,tProg=0,tAmt=0,tCompD=0,tProgD=0,maxAmt=0;
        List<Map<String, Object>> view = new ArrayList<>();
        for (Map<String, Object> r : stats) {
            Map<String, Object> m = new HashMap<>(r);
            long comp=lng(r.get("compCnt")), prog=lng(r.get("progCnt")), total=lng(r.get("totalCnt")), amt=lng(r.get("sumCont"));
            long[] pv = prevMap.get(str(r.get("sysNm")));
            Long compDelta = (pv != null) ? (comp - pv[0]) : null;
            Long progDelta = (pv != null) ? (prog - pv[1]) : null;
            m.put("compDelta", compDelta);
            m.put("progDelta", progDelta);
            view.add(m);
            tProj+=total; tComp+=comp; tProg+=prog; tAmt+=amt;
            if (compDelta!=null) tCompD+=compDelta;
            if (progDelta!=null) tProgD+=progDelta;
            if (amt>maxAmt) maxAmt=amt;
        }

        model.addAttribute("stats", view);
        model.addAttribute("totalProjects", tProj);
        model.addAttribute("totalCompleted", tComp);
        model.addAttribute("totalInProgress", tProg);
        model.addAttribute("totalContAmt", tAmt);
        model.addAttribute("totalCompDelta", tCompD);
        model.addAttribute("totalProgDelta", tProgD);
        model.addAttribute("hasPrev", queryYear != null && !prevMap.isEmpty());
        model.addAttribute("maxAmt", maxAmt);
        model.addAttribute("compPercent", tProj>0 ? Math.round((double)tComp/tProj*1000)/10.0 : 0.0);

        // 시스템별 금액 Top (bars) — sumCont 내림차순 6
        List<Map<String, Object>> topAmount = new ArrayList<>(view);
        topAmount.sort((a,b) -> Long.compare(lng(b.get("sumCont")), lng(a.get("sumCont"))));
        if (topAmount.size() > 6) topAmount = new ArrayList<>(topAmount.subList(0, 6));
        model.addAttribute("topAmount", topAmount);

        // 연도별 추이
        List<Map<String, Object>> yearRaw = swProjectRepository.countByYear();
        long maxYear = 0;
        if (yearRaw != null) for (Map<String, Object> r : yearRaw) maxYear = Math.max(maxYear, lng(r.get("c")));
        // 상위 구간 확대 스케일: 최대*0.55 미만은 바닥(20%)으로 클램프, 그 위를 펼침 → 비슷한 값들 차이 가시화
        double floor = maxYear * 0.55;
        double span  = Math.max(1.0, maxYear - floor);
        List<Map<String, Object>> yearCounts = new ArrayList<>();
        if (yearRaw != null) for (Map<String, Object> r : yearRaw) {
            long c = lng(r.get("c"));
            double ratio = Math.max(0.0, Math.min(1.0, (c - floor) / span));
            Map<String, Object> m = new HashMap<>(r);
            m.put("h", (int) Math.round(20 + ratio * 80));   // 20~100%
            yearCounts.add(m);
        }
        model.addAttribute("yearCounts", yearCounts);
        model.addAttribute("maxYearCount", maxYear);

        // 최근 등록 사업
        model.addAttribute("recentProjects", swProjectRepository.findTop6ByOrderByProjIdDesc());

        // 임박 업무 일정 (오늘 이후, 완료/취소 제외)
        LocalDate today = LocalDate.now();
        List<Map<String, Object>> upcoming = new ArrayList<>();
        for (WorkPlan wp : workPlanRepository.findTop6ByStartDateGreaterThanEqualAndStatusNotInOrderByStartDateAsc(
                today, List.of("COMPLETED", "CANCELLED"))) {
            if (wp.getStartDate() == null) continue;
            Map<String, Object> m = new HashMap<>();
            m.put("title", wp.getTitle());
            m.put("date", wp.getStartDate());
            m.put("dday", ChronoUnit.DAYS.between(today, wp.getStartDate()));
            upcoming.add(m);
        }
        model.addAttribute("upcoming", upcoming);

        // 만료 임박 라이선스 (GeoNURIS expiry_date)
        List<Map<String, Object>> expiring = new ArrayList<>();
        for (GeonurisLicense g : geonurisLicenseRepository.findTop6ByExpiryDateGreaterThanEqualOrderByExpiryDateAsc(LocalDateTime.now())) {
            if (g.getExpiryDate() == null) continue;
            String nm = (g.getOrganization() != null && !g.getOrganization().isBlank()) ? g.getOrganization()
                      : (g.getUserName() != null ? g.getUserName() : g.getLicenseType());
            Map<String, Object> m = new HashMap<>();
            m.put("name", nm);
            m.put("type", g.getLicenseType());
            m.put("days", ChronoUnit.DAYS.between(today, g.getExpiryDate().toLocalDate()));
            expiring.add(m);
        }
        model.addAttribute("expiring", expiring);

        return "dashboard-preview";
    }

    private static long lng(Object o)   { return (o instanceof Number) ? ((Number) o).longValue() : 0L; }
    private static String str(Object o) { return o == null ? "" : o.toString(); }
}
