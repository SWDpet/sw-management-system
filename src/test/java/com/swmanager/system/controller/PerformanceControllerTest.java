package com.swmanager.system.controller;

import com.swmanager.system.domain.User;
import com.swmanager.system.domain.workplan.PerformanceSummary;
import com.swmanager.system.repository.UserRepository;
import com.swmanager.system.security.CustomUserDetails;
import com.swmanager.system.service.ExcelExportService;
import com.swmanager.system.service.LogService;
import com.swmanager.system.service.PerformanceService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * PerformanceController 단위 테스트 (beyond-A 커버리지 스프린트 — 컨트롤러 통합영역 7탄).
 *
 * <p>PerformanceController 는 필드 주입(@Autowired 4)이고 권한은 {@link SecurityContextHolder}
 * principal 의 authPerformance(getAuth/isAdmin)에서 읽으므로, mock 4종을 reflection 으로 필드 주입하고
 * SecurityContext 를 직접 세팅한 뒤 메서드를 직접 호출한다(DocumentController 패턴).
 *
 * <p>실 Postgres 불필요 → 기본 CI 에서 JaCoCo floor 반영. 집계 실행은 ADMIN 가드, 대시보드/리포트/엑셀은
 * authPerformance VIEW 이상 가드. 가드 위반은 redirect/403 + 서비스 미호출, 통과 경로는 mock 으로
 * 월별 차트 집계 루프·null userId 분기를 커버한다.
 */
class PerformanceControllerTest {

    private PerformanceController controller;
    private PerformanceService performanceService;
    private ExcelExportService excelExportService;
    private UserRepository userRepository;
    private LogService logService;

    @BeforeEach
    void setUp() throws Exception {
        controller = new PerformanceController();
        performanceService = mock(PerformanceService.class);
        excelExportService = mock(ExcelExportService.class);
        userRepository = mock(UserRepository.class);
        logService = mock(LogService.class);
        inject("performanceService", performanceService);
        inject("excelExportService", excelExportService);
        inject("userRepository", userRepository);
        inject("logService", logService);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() { SecurityContextHolder.clearContext(); }

    private void inject(String field, Object value) throws Exception {
        Field f = PerformanceController.class.getDeclaredField(field);
        f.setAccessible(true);
        f.set(controller, value);
    }

    private void login(String authPerformance, String role) {
        User u = new User();
        u.setUserSeq(1L); u.setUserid("tester"); u.setUsername("테스터");
        u.setUserRole(role); u.setAuthPerformance(authPerformance);
        CustomUserDetails cud = new CustomUserDetails(u);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(cud, "N/A", cud.getAuthorities()));
    }
    private void loginView()  { login("VIEW", "ROLE_USER"); }
    private void loginEdit()  { login("EDIT", "ROLE_USER"); }
    private void loginNone()  { login("NONE", "ROLE_USER"); }
    private void loginAdmin() { login("NONE", "ROLE_ADMIN"); }

    private static Model model() { return new ExtendedModelMap(); }
    private static RedirectAttributes rttr() { return new RedirectAttributesModelMap(); }

    private static PerformanceSummary summary(int month, User user) {
        PerformanceSummary ps = new PerformanceSummary();
        ps.setPeriodMonth(month);
        ps.setUser(user);
        ps.setInstallCount(1);
        ps.setPatchCount(2);
        ps.setFaultCount(3);
        ps.setSupportCount(4);
        ps.setInspectDoneCount(5);
        return ps;
    }

    // ───────────────────────── 개인 대시보드 ─────────────────────────

    @Test
    void personal_none_redirectsHome() {
        loginNone();
        assertThat(controller.personalDashboard(null, null, model(), rttr())).isEqualTo("redirect:/");
        verify(performanceService, never()).getPersonalYearlyPerformance(anyLong(), anyInt());
    }

    @Test
    void personal_view_rendersWithDefaultUserAndYear() {
        loginView();
        when(performanceService.getPersonalYearlyPerformance(eq(1L), anyInt())).thenReturn(List.of());
        when(performanceService.getAggregatedPerformance(anyLong(), anyInt(), anyInt(), anyInt(), anyInt()))
                .thenReturn(Map.of());
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        when(userRepository.findByEnabledTrue()).thenReturn(List.of());
        Model m = model();
        String view = controller.personalDashboard(null, null, m, rttr());
        assertThat(view).isEqualTo("performance/personal-dashboard");
        assertThat(m.getAttribute("userId")).isEqualTo(1L); // 로그인 사용자로 디폴트
        assertThat(((List<?>) m.getAttribute("months"))).hasSize(12);
    }

    @Test
    void personal_aggregatesMonthlyChartData() {
        loginView();
        // 3월 데이터 1건 → 차트 배열의 3월(index 2)에 반영
        when(performanceService.getPersonalYearlyPerformance(eq(7L), eq(2026)))
                .thenReturn(List.of(summary(3, null)));
        when(performanceService.getAggregatedPerformance(anyLong(), anyInt(), anyInt(), anyInt(), anyInt()))
                .thenReturn(Map.of());
        when(userRepository.findById(7L)).thenReturn(Optional.empty());
        when(userRepository.findByEnabledTrue()).thenReturn(List.of());
        Model m = model();
        controller.personalDashboard(2026, 7L, m, rttr());
        @SuppressWarnings("unchecked")
        List<Integer> installs = (List<Integer>) m.getAttribute("installs");
        assertThat(installs.get(2)).isEqualTo(1); // 3월 설치 1건
        assertThat(installs.get(0)).isEqualTo(0); // 1월은 데이터 없음
    }

    // ───────────────────────── 부서 대시보드 ─────────────────────────

    @Test
    void dept_none_redirectsHome() {
        loginNone();
        assertThat(controller.deptDashboard(null, null, model(), rttr())).isEqualTo("redirect:/");
    }

    @Test
    void dept_view_rendersWithTotals() {
        loginView();
        User u = new User();
        u.setUsername("박성과");
        when(performanceService.getDeptMonthlyPerformance(anyInt(), anyInt()))
                .thenReturn(List.of(summary(6, u)));
        Model m = model();
        String view = controller.deptDashboard(2026, 6, m, rttr());
        assertThat(view).isEqualTo("performance/dept-dashboard");
        @SuppressWarnings("unchecked")
        List<String> names = (List<String>) m.getAttribute("userNames");
        assertThat(names).containsExactly("박성과");
        @SuppressWarnings("unchecked")
        List<Integer> totals = (List<Integer>) m.getAttribute("totalCounts");
        assertThat(totals).containsExactly(15); // 1+2+3+4+5
    }

    // ───────────────────────── 리포트 페이지 ─────────────────────────

    @Test
    void report_none_redirectsHome() {
        loginNone();
        assertThat(controller.reportPage(null, null, null, null, null, model(), rttr()))
                .isEqualTo("redirect:/");
    }

    @Test
    void report_noUserId_nullReportData() {
        loginView();
        when(userRepository.findByEnabledTrue()).thenReturn(List.of());
        Model m = model();
        String view = controller.reportPage(null, null, null, null, null, m, rttr());
        assertThat(view).isEqualTo("performance/performance-report");
        assertThat(m.getAttribute("reportData")).isNull();
        verify(performanceService, never()).getAggregatedPerformance(anyLong(), anyInt(), anyInt(), anyInt(), anyInt());
    }

    @Test
    void report_withUserId_loadsReportData() {
        loginView();
        when(performanceService.getAggregatedPerformance(eq(3L), anyInt(), anyInt(), anyInt(), anyInt()))
                .thenReturn(Map.of("total", 10));
        when(userRepository.findById(3L)).thenReturn(Optional.empty());
        when(userRepository.findByEnabledTrue()).thenReturn(List.of());
        Model m = model();
        controller.reportPage(3L, null, null, null, null, m, rttr());
        assertThat(m.getAttribute("reportData")).isNotNull();
    }

    // ───────────────────────── 집계 실행 (ADMIN) ─────────────────────────

    @Test
    void calculate_nonAdmin_forbidden() {
        loginView();
        ResponseEntity<?> res = controller.calculatePerformance(2026, 6);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(performanceService, never()).calculateAllUsersMonthlyPerformance(anyInt(), anyInt());
    }

    @Test
    void calculate_admin_ok() {
        loginAdmin();
        when(performanceService.calculateAllUsersMonthlyPerformance(2026, 6)).thenReturn(12);
        ResponseEntity<Map<String, Object>> res = controller.calculatePerformance(2026, 6);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).containsEntry("count", 12);
    }

    // ───────────────────────── 엑셀 다운로드 ─────────────────────────

    @Test
    void excel_none_forbidden() {
        loginNone();
        ResponseEntity<byte[]> res = controller.downloadExcel(1L, 2026, 1, 2026, 12);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(performanceService, never()).getAggregatedPerformance(anyLong(), anyInt(), anyInt(), anyInt(), anyInt());
    }

    @Test
    void excel_view_forbidden() { // [viewer-action-button-guard] 조회자 다운로드 차단
        loginView();
        ResponseEntity<byte[]> res = controller.downloadExcel(1L, 2026, 1, 2026, 12);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(performanceService, never()).getAggregatedPerformance(anyLong(), anyInt(), anyInt(), anyInt(), anyInt());
    }

    @Test
    void excel_admin_ok() throws Exception { // getAuth() admin→"EDIT" → 관리자 다운로드 허용
        loginAdmin();
        when(performanceService.getAggregatedPerformance(eq(1L), anyInt(), anyInt(), anyInt(), anyInt()))
                .thenReturn(Map.of("details", List.of()));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        when(excelExportService.generatePerformanceReport(any(), anyInt(), anyInt(), anyInt(), anyInt(), any(), any()))
                .thenReturn(new byte[]{1});
        assertThat(controller.downloadExcel(1L, 2026, 1, 2026, 12).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void excel_edit_ok() throws Exception {
        loginEdit();
        when(performanceService.getAggregatedPerformance(eq(1L), anyInt(), anyInt(), anyInt(), anyInt()))
                .thenReturn(Map.of("details", List.of()));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        when(excelExportService.generatePerformanceReport(any(), anyInt(), anyInt(), anyInt(), anyInt(), any(), any()))
                .thenReturn(new byte[]{1, 2});
        ResponseEntity<byte[]> res = controller.downloadExcel(1L, 2026, 1, 2026, 12);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).hasSize(2);
    }

    @Test
    void excel_serviceThrows_500() {
        loginEdit(); // 권한 선행 → 500 경로 검증엔 EDIT 필요
        when(performanceService.getAggregatedPerformance(anyLong(), anyInt(), anyInt(), anyInt(), anyInt()))
                .thenThrow(new RuntimeException("boom"));
        ResponseEntity<byte[]> res = controller.downloadExcel(1L, 2026, 1, 2026, 12);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
