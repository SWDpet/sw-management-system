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
        if (year == null)   { year = LocalDate.now().getYear(); queryYear = year; }
        else if (year == 0) { queryYear = null; year = null; }
        else                { queryYear = year; }

        buildDashboardModel(queryYear, year, model);
        return "main-dashboard";
    }

    // [global-sidebar-responsive Phase2] 미리보기 라우트는 메인 대시보드로 승격 → 영구 리다이렉트
    @GetMapping("/dashboard-preview")
    public String dashboardPreview() {
        return "redirect:/";
    }

    // ===== 통합 대시보드 모델 빌더 (차트행+피드행+증감 테이블, 실데이터) — `/` 와 공용 =====
    private void buildDashboardModel(Integer queryYear, Integer displayYear, Model model) {
        model.addAttribute("years", swProjectRepository.findDistinctYears());
        model.addAttribute("selectedYear", displayYear);

        List<Map<String, Object>> stats = swProjectRepository.getSystemStats(queryYear);
        if (stats == null) stats = new ArrayList<>();

        long tProj=0,tComp=0,tProg=0,tAmt=0,maxAmt=0;
        // 항목별 금액 소계 (합계행): SW/컨설팅/DB구축/패키지SW/시스템개발/HW/기타
        long tSw=0,tCnslt=0,tDb=0,tPkg=0,tDev=0,tHw=0,tEtc=0;
        List<Map<String, Object>> view = new ArrayList<>();
        for (Map<String, Object> r : stats) {
            Map<String, Object> m = new HashMap<>(r);
            long comp=lng(r.get("compCnt")), prog=lng(r.get("progCnt")), total=lng(r.get("totalCnt")), amt=lng(r.get("sumCont"));
            view.add(m);
            tProj+=total; tComp+=comp; tProg+=prog; tAmt+=amt;
            tSw+=lng(r.get("sumSw")); tCnslt+=lng(r.get("sumCnslt")); tDb+=lng(r.get("sumDb"));
            tPkg+=lng(r.get("sumPkg")); tDev+=lng(r.get("sumDev")); tHw+=lng(r.get("sumHw")); tEtc+=lng(r.get("sumEtc"));
            if (amt>maxAmt) maxAmt=amt;
        }

        model.addAttribute("stats", view);
        model.addAttribute("totalProjects", tProj);
        model.addAttribute("totalCompleted", tComp);
        model.addAttribute("totalInProgress", tProg);
        model.addAttribute("totalContAmt", tAmt);
        model.addAttribute("totalSumSw", tSw);
        model.addAttribute("totalSumCnslt", tCnslt);
        model.addAttribute("totalSumDb", tDb);
        model.addAttribute("totalSumPkg", tPkg);
        model.addAttribute("totalSumDev", tDev);
        model.addAttribute("totalSumHw", tHw);
        model.addAttribute("totalSumEtc", tEtc);
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
    }

    private static long lng(Object o)   { return (o instanceof Number) ? ((Number) o).longValue() : 0L; }
}
