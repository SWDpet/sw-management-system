package com.swmanager.system.controller;

import com.swmanager.system.repository.SwProjectRepository;
import com.swmanager.system.repository.AccessLogRepository;
import com.swmanager.system.constants.MenuName;
import com.swmanager.system.domain.workplan.WorkPlan;
import com.swmanager.system.repository.workplan.WorkPlanRepository;
import com.swmanager.system.geonuris.domain.GeonurisLicense;
import com.swmanager.system.geonuris.repository.GeonurisLicenseRepository;
import com.swmanager.system.dto.dashboard.SystemStatRow;
import com.swmanager.system.dto.dashboard.YearCountRow;
import com.swmanager.system.dto.dashboard.DashYearBar;
import com.swmanager.system.dto.dashboard.DashLogBar;
import com.swmanager.system.dto.dashboard.DashMenuBar;
import com.swmanager.system.dto.dashboard.DashActionBar;
import com.swmanager.system.dto.dashboard.DashUpcoming;
import com.swmanager.system.dto.dashboard.DashExpiring;
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
import java.util.List;

@Slf4j
@Controller
public class MainController {

    @Autowired
    private SwProjectRepository swProjectRepository;
    @Autowired
    private WorkPlanRepository workPlanRepository;
    @Autowired
    private GeonurisLicenseRepository geonurisLicenseRepository;
    @Autowired
    private AccessLogRepository accessLogRepository;

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

        // [viewer-action-button-guard] 사업등록 버튼 노출 — 웹 /projects/new 는 authProject=="EDIT" 전용(admin 우회 없음, SwController 와 일치)
        boolean canCreateProject = (userDetails instanceof com.swmanager.system.security.CustomUserDetails cud)
                && "EDIT".equals(cud.getUser().getAuthProject());
        model.addAttribute("canCreateProject", canCreateProject);

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

        List<SystemStatRow> stats = swProjectRepository.getSystemStats(queryYear);
        if (stats == null) stats = new ArrayList<>();

        long tProj=0,tComp=0,tProg=0,tAmt=0,maxAmt=0;
        // 항목별 금액 소계 (합계행): SW/컨설팅/DB구축/패키지SW/시스템개발/HW/기타
        long tSw=0,tCnslt=0,tDb=0,tPkg=0,tDev=0,tHw=0,tEtc=0;
        for (SystemStatRow r : stats) {
            long comp=lng(r.getCompCnt()), prog=lng(r.getProgCnt()), total=lng(r.getTotalCnt()), amt=lng(r.getSumCont());
            tProj+=total; tComp+=comp; tProg+=prog; tAmt+=amt;
            tSw+=lng(r.getSumSw()); tCnslt+=lng(r.getSumCnslt()); tDb+=lng(r.getSumDb());
            tPkg+=lng(r.getSumPkg()); tDev+=lng(r.getSumDev()); tHw+=lng(r.getSumHw()); tEtc+=lng(r.getSumEtc());
            if (amt>maxAmt) maxAmt=amt;
        }

        model.addAttribute("stats", stats);
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
        List<SystemStatRow> topAmount = new ArrayList<>(stats);
        topAmount.sort((a,b) -> Long.compare(lng(b.getSumCont()), lng(a.getSumCont())));
        if (topAmount.size() > 6) topAmount = new ArrayList<>(topAmount.subList(0, 6));
        model.addAttribute("topAmount", topAmount);

        // 연도별 추이
        List<YearCountRow> yearRaw = swProjectRepository.countByYear();
        long maxYear = 0;
        if (yearRaw != null) for (YearCountRow r : yearRaw) maxYear = Math.max(maxYear, lng(r.getC()));
        // 상위 구간 확대 스케일: 최대*0.55 미만은 바닥(20%)으로 클램프, 그 위를 펼침 → 비슷한 값들 차이 가시화
        double floor = maxYear * 0.55;
        double span  = Math.max(1.0, maxYear - floor);
        List<DashYearBar> yearCounts = new ArrayList<>();
        if (yearRaw != null) for (YearCountRow r : yearRaw) {
            long c = lng(r.getC());
            double ratio = Math.max(0.0, Math.min(1.0, (c - floor) / span));
            int h = (int) Math.round(20 + ratio * 80);   // 20~100%
            yearCounts.add(new DashYearBar(r.getY(), c, h));
        }
        model.addAttribute("yearCounts", yearCounts);
        model.addAttribute("maxYearCount", maxYear);

        // 최근 사업 변경이력 (등록/수정/삭제) — 기간무관 최신 6건 (access_logs 기반)
        model.addAttribute("recentProjectLogs",
                accessLogRepository.findTop6ByMenuNmAndActionTypeInOrderByAccessTimeDesc(
                        MenuName.PROJECT, List.of("등록", "수정", "삭제")));

        // 로그 통계 (최근 30일)
        List<Object[]> logRaw = accessLogRepository.findDailyTrend30d();
        long maxAct = 0;
        for (Object[] r : logRaw) maxAct = Math.max(maxAct, ((Number) r[1]).longValue());
        long maxActD = Math.max(maxAct, 1);
        List<DashLogBar> logTrend = new ArrayList<>();
        for (Object[] r : logRaw) {
            long act = ((Number) r[1]).longValue();
            long visitors = r[2] == null ? 0L : ((Number) r[2]).longValue();
            int h = (int) Math.round(10 + act * 90.0 / maxActD);
            logTrend.add(new DashLogBar(r[0], act, visitors, h));
        }
        model.addAttribute("logTrend", logTrend);

        List<Object[]> menuRaw = accessLogRepository.findMenuTop30d();
        long maxMenu = 0;
        for (Object[] r : menuRaw) maxMenu = Math.max(maxMenu, ((Number) r[1]).longValue());
        long maxMenuD = Math.max(maxMenu, 1);
        List<DashMenuBar> menuTop = new ArrayList<>();
        for (Object[] r : menuRaw) {
            long cnt = ((Number) r[1]).longValue();
            int w = (int) Math.round(cnt * 100.0 / maxMenuD);
            menuTop.add(new DashMenuBar(r[0], cnt, w));
        }
        model.addAttribute("menuTop", menuTop);

        List<Object[]> actionRaw = accessLogRepository.findActionCounts30d();
        long maxAction = 0;
        for (Object[] r : actionRaw) maxAction = Math.max(maxAction, ((Number) r[1]).longValue());
        long maxActionD = Math.max(maxAction, 1);
        List<DashActionBar> actionCounts = new ArrayList<>();
        for (Object[] r : actionRaw) {
            long cnt = ((Number) r[1]).longValue();
            int w = (int) Math.round(cnt * 100.0 / maxActionD);
            actionCounts.add(new DashActionBar(r[0], cnt, w));
        }
        model.addAttribute("actionCounts", actionCounts);

        // 임박 업무 일정 (오늘 이후, 완료/취소 제외)
        LocalDate today = LocalDate.now();
        List<DashUpcoming> upcoming = new ArrayList<>();
        for (WorkPlan wp : workPlanRepository.findTop6ByStartDateGreaterThanEqualAndStatusNotInOrderByStartDateAsc(
                today, List.of("COMPLETED", "CANCELLED"))) {
            if (wp.getStartDate() == null) continue;
            upcoming.add(new DashUpcoming(wp.getTitle(), wp.getStartDate(),
                    ChronoUnit.DAYS.between(today, wp.getStartDate())));
        }
        model.addAttribute("upcoming", upcoming);

        // 만료 임박 라이선스 (GeoNURIS expiry_date)
        List<DashExpiring> expiring = new ArrayList<>();
        for (GeonurisLicense g : geonurisLicenseRepository.findTop6ByExpiryDateGreaterThanEqualOrderByExpiryDateAsc(LocalDateTime.now())) {
            if (g.getExpiryDate() == null) continue;
            String nm = (g.getOrganization() != null && !g.getOrganization().isBlank()) ? g.getOrganization()
                      : (g.getUserName() != null ? g.getUserName() : g.getLicenseType());
            expiring.add(new DashExpiring(nm, g.getLicenseType(),
                    ChronoUnit.DAYS.between(today, g.getExpiryDate().toLocalDate())));
        }
        model.addAttribute("expiring", expiring);
    }

    private static long lng(Object o)   { return (o instanceof Number) ? ((Number) o).longValue() : 0L; }
}
