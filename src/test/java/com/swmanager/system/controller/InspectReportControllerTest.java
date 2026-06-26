package com.swmanager.system.controller;

import com.swmanager.system.domain.InspectMetricSnapshot;
import com.swmanager.system.domain.User;
import com.swmanager.system.dto.InspectCheckResultDTO;
import com.swmanager.system.dto.InspectReportDTO;
import com.swmanager.system.repository.InfraRepository;
import com.swmanager.system.repository.InspectMetricSnapshotRepository;
import com.swmanager.system.dto.inspection.SnapshotRow;
import com.swmanager.system.repository.SwProjectRepository;
import com.swmanager.system.response.ApiResult;
import com.swmanager.system.security.CustomUserDetails;
import com.swmanager.system.service.InspectMetricChartService;
import com.swmanager.system.service.InspectPdfService;
import com.swmanager.system.service.InspectReportService;
import com.swmanager.system.service.LogService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * InspectReportController 단위 테스트 (beyond-A 커버리지 스프린트 — 컨트롤러 통합영역 4탄).
 *
 * <p>InspectReportController 는 {@code @RequiredArgsConstructor}(7 의존성)이고 권한은
 * {@link SecurityContextHolder} principal 의 authDocument(getAuth/isAdmin)에서 읽으므로,
 * mock 7종 생성자주입 + SecurityContext 직접세팅 후 메서드 직접호출한다(DocumentController 패턴).
 *
 * <p>실 Postgres 불필요 → 기본 CI 에서 JaCoCo floor 반영. 쓰기/관리자 경로는 가드(403)와 서비스
 * 미호출(부수효과 0)을 함께 단언하고, 통과 경로는 mock 으로 happy-path/예외 분기를 커버한다.
 * 현장 스냅샷 스펙 추출(extractSnapshotSpecs)은 raw payload 를 주입해 AP/DB 키 분기까지 커버한다.
 */
class InspectReportControllerTest {

    private InspectReportController controller;

    private InspectReportService inspectReportService;
    private InspectPdfService inspectPdfService;
    private InspectMetricChartService inspectMetricChartService;
    private InspectMetricSnapshotRepository metricSnapshotRepository;
    private SwProjectRepository swProjectRepository;
    private InfraRepository infraRepository;
    private LogService logService;

    @BeforeEach
    void setUp() {
        inspectReportService = mock(InspectReportService.class);
        inspectPdfService = mock(InspectPdfService.class);
        inspectMetricChartService = mock(InspectMetricChartService.class);
        metricSnapshotRepository = mock(InspectMetricSnapshotRepository.class);
        swProjectRepository = mock(SwProjectRepository.class);
        infraRepository = mock(InfraRepository.class);
        logService = mock(LogService.class);
        controller = new InspectReportController(
                inspectReportService, inspectPdfService, inspectMetricChartService,
                metricSnapshotRepository, swProjectRepository, infraRepository, logService);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() { SecurityContextHolder.clearContext(); }

    private void login(String authDocument, String role) {
        User u = new User();
        u.setUserSeq(1L); u.setUserid("tester"); u.setUsername("테스터");
        u.setUserRole(role); u.setAuthDocument(authDocument);
        CustomUserDetails cud = new CustomUserDetails(u);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(cud, "N/A", cud.getAuthorities()));
    }
    private void loginEdit()  { login("EDIT", "ROLE_USER"); }
    private void loginView()  { login("VIEW", "ROLE_USER"); }
    private void loginAdmin() { login("NONE", "ROLE_ADMIN"); }

    private static Model model() { return new ExtendedModelMap(); }

    // ───────────────────────── 저장 (EDIT 가드) ─────────────────────────

    @Test
    void save_nonEdit_forbidden() {
        loginView();
        ResponseEntity<?> res = controller.saveInspectReport(new InspectReportDTO());
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(inspectReportService, never()).save(any());
    }

    @Test
    void save_edit_ok() {
        loginEdit();
        when(inspectReportService.save(any())).thenReturn(new InspectReportDTO());
        ResponseEntity<?> res = controller.saveInspectReport(new InspectReportDTO());
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(inspectReportService).save(any());
    }

    @Test
    void save_edit_serviceThrows_okWithFailBody() {
        loginEdit();
        when(inspectReportService.save(any())).thenThrow(new RuntimeException("boom"));
        ResponseEntity<?> res = controller.saveInspectReport(new InspectReportDTO());
        // 현행 계약: 예외도 HTTP 200 + {success:false,error:msg} (failMessage)
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        ApiResult body = (ApiResult) res.getBody();
        assertThat(body.success()).isFalse();
        assertThat(String.valueOf(body.error())).contains("boom");
    }

    // ───────────────────────── 조회 (가드 없음) ─────────────────────────

    @Test
    void getInspectReport_ok() {
        when(inspectReportService.findById(5L)).thenReturn(new InspectReportDTO());
        assertThat(controller.getInspectReport(5L).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getInspectReport_throws_okWithFailBody() {
        when(inspectReportService.findById(5L)).thenThrow(new RuntimeException("없음"));
        ResponseEntity<?> res = controller.getInspectReport(5L);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        ApiResult body = (ApiResult) res.getBody();
        assertThat(body.success()).isFalse();
        assertThat(String.valueOf(body.error())).contains("없음");
    }

    @Test
    void listInspectReports_ok() {
        when(inspectReportService.findByProject(1L)).thenReturn(List.of());
        assertThat(controller.listInspectReports(1L).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void findInspectReport_ok() {
        when(inspectReportService.findByProjectAndMonth(1L, "2026-03")).thenReturn(null);
        assertThat(controller.findInspectReport(1L, "2026-03").getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getPreviousVisits_ok() {
        when(inspectReportService.getPreviousVisits(1L, "2026-03")).thenReturn(List.of());
        assertThat(controller.getPreviousVisits(1L, "2026-03").getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getInspectTemplate_ok() {
        when(inspectReportService.getTemplateItems("KRAS")).thenReturn(List.of());
        assertThat(controller.getInspectTemplate("KRAS").getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // ───────────────────────── 삭제/초기화 (ADMIN 가드) ─────────────────────────

    @Test
    void delete_nonAdmin_forbidden() {
        loginEdit(); // EDIT 이지만 admin 아님
        ResponseEntity<?> res = controller.deleteInspectReport(5L);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(inspectReportService, never()).delete(anyLong());
    }

    @Test
    void delete_admin_ok() {
        loginAdmin();
        ResponseEntity<?> res = controller.deleteInspectReport(5L);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(inspectReportService).delete(5L);
    }

    @Test
    void resetAll_nonAdmin_forbidden() {
        loginEdit();
        ResponseEntity<?> res = controller.resetAllInspect();
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(inspectReportService, never()).resetAllInspectData();
    }

    @Test
    void resetAll_admin_ok() {
        loginAdmin();
        when(inspectReportService.resetAllInspectData()).thenReturn(Map.of("reports", 3L));
        ResponseEntity<?> res = controller.resetAllInspect();
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) res.getBody();
        assertThat(body).containsEntry("success", true).containsKey("deleted");
    }

    // ───────────────────────── 인프라 서버 / 스냅샷 (VIEW 가드) ─────────────────────────

    @Test
    void getInfraServers_none_forbidden() {
        login("NONE", "ROLE_USER");
        ResponseEntity<?> res = controller.getInfraServers("양양군", "UPIS");
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(infraRepository, never()).findByDistNmAndSysNmEn(any(), any());
    }

    @Test
    void getInfraServers_empty_returnsEmptyList() {
        loginView();
        when(infraRepository.findByDistNmAndSysNmEn("양양군", "UPIS")).thenReturn(List.of());
        ResponseEntity<?> res = controller.getInfraServers("양양군", "UPIS");
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat((List<?>) res.getBody()).isEmpty();
    }

    @Test
    void getInspectSnapshots_none_forbidden() {
        login("NONE", "ROLE_USER");
        ResponseEntity<?> res = controller.getInspectSnapshots(1L);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(metricSnapshotRepository, never()).findLatestPerRoleHost(anyLong());
    }

    @Test
    void getInspectSnapshots_empty_ok() {
        loginView();
        when(metricSnapshotRepository.findLatestPerRoleHost(1L)).thenReturn(List.of());
        ResponseEntity<?> res = controller.getInspectSnapshots(1L);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat((List<?>) res.getBody()).isEmpty();
    }

    @Test
    void getInspectSnapshots_extractsApAndDbSpecs() {
        loginView();
        // AP 키 스냅샷
        InspectMetricSnapshot ap = new InspectMetricSnapshot();
        ap.setServerRole("AP"); ap.setHostName("ap-01");
        ap.setRawPayload(Map.of("i", List.of(
                List.of("ap.hw.cpu", "OK", Map.of("name", "Xeon", "cores", 8)),
                List.of("ap.hw.memory", "OK", Map.of("installed_gb", 32)),
                List.of("ap.os.disk_summary", "OK", Map.of("summary", "500GB")))));
        // DB 키 스냅샷
        InspectMetricSnapshot db = new InspectMetricSnapshot();
        db.setServerRole("DB"); db.setHostName("db-01");
        db.setRawPayload(Map.of("i", List.of(
                List.of("db.os.cpu_info", "OK", Map.of("cores", 16, "clock_ghz", 2.5)),
                List.of("db.os.mem_info", "OK", Map.of("total_gb", 64)),
                List.of("db.os.disk", "OK", Map.of("count", 4)))));
        when(metricSnapshotRepository.findLatestPerRoleHost(1L)).thenReturn(List.of(ap, db));

        ResponseEntity<?> res = controller.getInspectSnapshots(1L);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        @SuppressWarnings("unchecked")
        List<SnapshotRow> rows = (List<SnapshotRow>) res.getBody();
        assertThat(rows).hasSize(2);
        // 추출 필드를 직접 단언(toString 포맷 비의존). AP: name+cores, DB: total_gb.
        SnapshotRow apRow = rows.get(0);
        assertThat(apRow.cpu()).isEqualTo("Xeon 8코어");
        assertThat(apRow.memory()).isEqualTo("32GB");
        assertThat(apRow.disk()).isEqualTo("500GB");
        SnapshotRow dbRow = rows.get(1);
        assertThat(dbRow.cpu()).isEqualTo("16코어 2.5GHz");
        assertThat(dbRow.memory()).isEqualTo("64GB");
        assertThat(dbRow.disk()).isEqualTo("4개");
    }

    // ───────────────────────── PDF 다운로드 ─────────────────────────

    @Test
    void downloadPdf_inspectMonthSuffix_ok() {
        InspectReportDTO report = new InspectReportDTO();
        report.setInspectMonth("2026-03");
        report.setDocTitle("UPIS 점검내역서");
        when(inspectPdfService.generatePdf(5L)).thenReturn(new byte[]{1});
        when(inspectReportService.findById(5L)).thenReturn(report);
        ResponseEntity<byte[]> res = controller.downloadInspectPdf(5L);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).hasSize(1);
        // 2026-03 → "_3월" suffix (월 = UTF-8 %EC%9B%94)
        assertThat(res.getHeaders().getFirst("Content-Disposition")).contains("_3%EC%9B%94");
    }

    @Test
    void downloadPdf_noMonth_emptySuffix_ok() {
        InspectReportDTO report = new InspectReportDTO(); // visits/month 모두 null → suffix 없음
        when(inspectPdfService.generatePdf(5L)).thenReturn(new byte[]{1});
        when(inspectReportService.findById(5L)).thenReturn(report);
        ResponseEntity<byte[]> res = controller.downloadInspectPdf(5L);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        // month/visits 모두 null → 월 suffix 없음 (%EC%9B%94 미포함)
        assertThat(res.getHeaders().getFirst("Content-Disposition")).doesNotContain("%EC%9B%94");
    }

    @Test
    void downloadPdf_throws_500() {
        when(inspectPdfService.generatePdf(5L)).thenThrow(new RuntimeException("x"));
        assertThat(controller.downloadInspectPdf(5L).getStatusCode())
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // ───────────────────────── 차트 미리보기 ─────────────────────────

    @Test
    void chartPreview_withMonth_ok() {
        when(inspectMetricChartService.renderChart(anyLong(), any(), any())).thenReturn(new byte[]{1, 2});
        ResponseEntity<byte[]> res = controller.inspectChartPreview(1L, "2026-03");
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void chartPreview_noMonth_ok() {
        when(inspectMetricChartService.renderChart(anyLong(), any(), any())).thenReturn(new byte[]{1});
        ResponseEntity<byte[]> res = controller.inspectChartPreview(1L, null);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void chartPreview_emptyPng_noContent() {
        when(inspectMetricChartService.renderChart(anyLong(), any(), any())).thenReturn(new byte[0]);
        assertThat(controller.inspectChartPreview(1L, null).getStatusCode())
                .isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void chartPreview_throws_noContent() {
        when(inspectMetricChartService.renderChart(anyLong(), any(), any()))
                .thenThrow(new RuntimeException("x"));
        assertThat(controller.inspectChartPreview(1L, null).getStatusCode())
                .isEqualTo(HttpStatus.NO_CONTENT);
    }

    // ───────────────────────── 상세 / 미리보기 (뷰) ─────────────────────────

    @Test
    void inspectDetail_ok_aggregatesCounts() {
        InspectReportDTO report = new InspectReportDTO();
        report.setPjtId(1L);
        report.setCheckResults(List.of(
                checkResult("DB", "NORMAL"),
                checkResult("AP", "PARTIAL"),
                checkResult("DB", "ABNORMAL"),
                checkResult("GIS", "MANUAL")));
        when(inspectReportService.findById(5L)).thenReturn(report);
        when(swProjectRepository.findById(1L)).thenReturn(java.util.Optional.empty());
        Model m = model();
        String view = controller.inspectDetail(5L, m);
        assertThat(view).isEqualTo("document/inspect-detail");
        assertThat(m.getAttribute("cntTotal")).isEqualTo(4);
        assertThat(m.getAttribute("cntNormal")).isEqualTo(1);
        assertThat(m.getAttribute("cntViolation")).isEqualTo(1); // ABNORMAL
    }

    @Test
    void inspectDetail_throws_redirectsList() {
        when(inspectReportService.findById(5L)).thenThrow(new RuntimeException("없음"));
        assertThat(controller.inspectDetail(5L, model())).isEqualTo("redirect:/ops-doc/list");
    }

    @Test
    void inspectPreview_ok_injectsToolbar() {
        when(inspectPdfService.renderToHtmlV2(5L))
                .thenReturn("<html><body><h1>보고서</h1></body></html>");
        String html = controller.inspectPreview(5L);
        assertThat(html).contains("preview-bar")          // 툴바 주입됨
                .contains("/document/api/inspect-pdf/5");  // PDF 링크
    }

    @Test
    void inspectPreview_throws_errorHtml() {
        when(inspectPdfService.renderToHtmlV2(5L)).thenThrow(new RuntimeException("x"));
        String html = controller.inspectPreview(5L);
        assertThat(html).contains("점검내역서를 찾을 수 없습니다");
    }

    private static InspectCheckResultDTO checkResult(String section, String resultCode) {
        InspectCheckResultDTO r = new InspectCheckResultDTO();
        r.setSection(section);
        r.setResultCode(resultCode);
        return r;
    }
}
