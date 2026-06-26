package com.swmanager.system.controller;

import com.swmanager.system.domain.workplan.WorkPlan;
import com.swmanager.system.dto.dashboard.DashMenuBar;
import com.swmanager.system.dto.dashboard.DashUpcoming;
import com.swmanager.system.dto.dashboard.DashYearBar;
import com.swmanager.system.dto.dashboard.SystemStatRow;
import com.swmanager.system.dto.dashboard.YearCountRow;
import com.swmanager.system.geonuris.domain.GeonurisLicense;
import com.swmanager.system.geonuris.repository.GeonurisLicenseRepository;
import com.swmanager.system.repository.AccessLogRepository;
import com.swmanager.system.repository.SwProjectRepository;
import com.swmanager.system.repository.workplan.WorkPlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * MainController(메인 대시보드) 단위 테스트 (beyond-A 커버리지 스프린트 — 컨트롤러 통합영역 8탄).
 *
 * <p>MainController 는 필드 주입(@Autowired 4)이고 페이지 보안은 SecurityConfig 레벨이라 컨트롤러 자체
 * 가드는 없다. mock 4종을 reflection 으로 필드 주입하고 메서드를 직접 호출한다(DocumentController 패턴).
 * 통합 대시보드 모델 빌더(buildDashboardModel)의 집계 루프(시스템 통계·연도추이·로그 트렌드/메뉴/액션·
 * 임박업무·만료 라이선스)를 mock 행 데이터로 커버한다. 실 Postgres 불필요 → 기본 CI 에서 floor 반영.
 */
class MainControllerTest {

    private MainController controller;
    private SwProjectRepository swProjectRepository;
    private WorkPlanRepository workPlanRepository;
    private GeonurisLicenseRepository geonurisLicenseRepository;
    private AccessLogRepository accessLogRepository;

    @BeforeEach
    void setUp() throws Exception {
        controller = new MainController();
        swProjectRepository = mock(SwProjectRepository.class);
        workPlanRepository = mock(WorkPlanRepository.class);
        geonurisLicenseRepository = mock(GeonurisLicenseRepository.class);
        accessLogRepository = mock(AccessLogRepository.class);
        inject("swProjectRepository", swProjectRepository);
        inject("workPlanRepository", workPlanRepository);
        inject("geonurisLicenseRepository", geonurisLicenseRepository);
        inject("accessLogRepository", accessLogRepository);
        stubDashboard();
    }

    private void inject(String field, Object value) throws Exception {
        var f = MainController.class.getDeclaredField(field);
        f.setAccessible(true);
        f.set(controller, value);
    }

    /** buildDashboardModel 이 호출하는 전 리포지토리에 행 1건씩 stub — 집계 루프 전체 통과. */
    private void stubDashboard() {
        lenient().when(swProjectRepository.findDistinctYears()).thenReturn(List.of(2025, 2026));

        SystemStatRow stat = mock(SystemStatRow.class);
        lenient().when(stat.getTotalCnt()).thenReturn(10L);
        lenient().when(stat.getCompCnt()).thenReturn(4L);
        lenient().when(stat.getProgCnt()).thenReturn(6L);
        lenient().when(stat.getSumCont()).thenReturn(1000L);
        lenient().when(swProjectRepository.getSystemStats(any())).thenReturn(List.of(stat));

        YearCountRow yc = mock(YearCountRow.class);
        lenient().when(yc.getY()).thenReturn(2026);
        lenient().when(yc.getC()).thenReturn(5L);
        lenient().when(swProjectRepository.countByYear()).thenReturn(List.of(yc));

        lenient().when(accessLogRepository.findTop6ByMenuNmAndActionTypeInOrderByAccessTimeDesc(any(), any()))
                .thenReturn(List.of());
        lenient().when(accessLogRepository.findDailyTrend30d())
                .thenReturn(List.<Object[]>of(new Object[]{"2026-06-01", 5L, 3L}));
        lenient().when(accessLogRepository.findMenuTop30d())
                .thenReturn(List.<Object[]>of(new Object[]{"사업관리", 10L}));
        lenient().when(accessLogRepository.findActionCounts30d())
                .thenReturn(List.<Object[]>of(new Object[]{"등록", 7L}));

        WorkPlan wp = new WorkPlan();
        wp.setTitle("현장방문");
        wp.setStartDate(LocalDate.now().plusDays(3));
        lenient().when(workPlanRepository.findTop6ByStartDateGreaterThanEqualAndStatusNotInOrderByStartDateAsc(
                any(), any())).thenReturn(List.of(wp));

        GeonurisLicense lic = mock(GeonurisLicense.class);
        lenient().when(lic.getExpiryDate()).thenReturn(LocalDateTime.now().plusDays(10));
        lenient().when(lic.getOrganization()).thenReturn("강진군청");
        lenient().when(lic.getLicenseType()).thenReturn("KRAS");
        lenient().when(geonurisLicenseRepository.findTop6ByExpiryDateGreaterThanEqualOrderByExpiryDateAsc(any()))
                .thenReturn(List.of(lic));
    }

    private static Model model() { return new ExtendedModelMap(); }

    @Test
    void dashboard_defaultYear_rendersWithAggregates() {
        int expectedYear = LocalDate.now().getYear(); // 컨트롤러 호출 전 캡처(연 경계 flakiness 회피)
        Model m = model();
        String view = controller.mainDashboard(null, null, m);
        assertThat(view).isEqualTo("main-dashboard");
        // year=null → 올해로 디폴트
        assertThat(m.getAttribute("selectedYear")).isEqualTo(expectedYear);
        // 시스템 통계 집계
        assertThat(m.getAttribute("totalProjects")).isEqualTo(10L);
        assertThat(m.getAttribute("totalCompleted")).isEqualTo(4L);
        assertThat(m.getAttribute("totalContAmt")).isEqualTo(1000L);
        assertThat(m.getAttribute("maxYearCount")).isEqualTo(5L); // yc.getC() 매핑
        // 각 차트 행 빌드 — 매핑 필드까지 단언(사이즈만 보지 않음)
        DashYearBar yb = (DashYearBar) ((List<?>) m.getAttribute("yearCounts")).get(0);
        assertThat(yb.getY()).isEqualTo(2026);
        assertThat(yb.getC()).isEqualTo(5L);
        DashMenuBar mb = (DashMenuBar) ((List<?>) m.getAttribute("menuTop")).get(0);
        assertThat(mb.getMenu()).isEqualTo("사업관리");
        assertThat(mb.getCnt()).isEqualTo(10L);
        DashUpcoming up = (DashUpcoming) ((List<?>) m.getAttribute("upcoming")).get(0);
        assertThat(up.getTitle()).isEqualTo("현장방문");
        assertThat((List<?>) m.getAttribute("logTrend")).hasSize(1);
        assertThat((List<?>) m.getAttribute("actionCounts")).hasSize(1);
        assertThat((List<?>) m.getAttribute("expiring")).hasSize(1);
    }

    @Test
    void dashboard_allYears_year0_clearsSelectedYear() {
        Model m = model();
        String view = controller.mainDashboard(null, 0, m);
        assertThat(view).isEqualTo("main-dashboard");
        assertThat(m.getAttribute("selectedYear")).isNull(); // year=0 → 전체 연도
    }

    @Test
    void dashboard_specificYear_keepsSelectedYear() {
        Model m = model();
        // 올해(2026)와 다른 연도로 → 명시 연도 경로가 디폴트 경로와 구별됨을 증명
        controller.mainDashboard(null, 2024, m);
        assertThat(m.getAttribute("selectedYear")).isEqualTo(2024);
    }

    @Test
    void dashboard_anonymous_stillRenders() {
        // userDetails=null 분기(로그 미기록)도 정상 렌더
        assertThat(controller.mainDashboard(null, null, model())).isEqualTo("main-dashboard");
    }

    @Test
    void dashboard_emptyStats_safeAggregates() {
        when(swProjectRepository.getSystemStats(any())).thenReturn(List.of());
        Model m = model();
        controller.mainDashboard(null, 2026, m);
        assertThat(m.getAttribute("totalProjects")).isEqualTo(0L);
        assertThat(m.getAttribute("compPercent")).isEqualTo(0.0); // tProj=0 분기
    }

    @Test
    void dashboardPreview_redirectsHome() {
        assertThat(controller.dashboardPreview()).isEqualTo("redirect:/");
    }
}
