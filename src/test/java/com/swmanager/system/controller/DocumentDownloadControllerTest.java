package com.swmanager.system.controller;

import com.swmanager.system.security.DocumentAccessSupport;
import com.swmanager.system.domain.User;
import com.swmanager.system.domain.workplan.Document;
import com.swmanager.system.dto.DocumentDTO;
import com.swmanager.system.security.CustomUserDetails;
import com.swmanager.system.service.DocumentService;
import com.swmanager.system.service.ExcelExportService;
import com.swmanager.system.service.HwpxExportService;
import com.swmanager.system.service.LogService;
import com.swmanager.system.service.PdfExportService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * DocumentDownloadController 단위 테스트 (S4 Phase 1 — DocumentControllerTest 에서 이관).
 *
 * <p>다운로드/내보내기 분리(refactor-document-controller-split) 에 따라 download·bulk·excelList
 * 및 static 헬퍼(zipSafe/bulkTypeLabel) 테스트를 이전 단언 그대로 옮김. 권한은 실제
 * {@link DocumentAccessSupport}(SecurityContextHolder principal) 를 주입해 getAuth() admin→EDIT
 * 매핑까지 검증(viewer-action-button-guard: 다운로드=EDIT, VIEW 403·admin 200).
 */
class DocumentDownloadControllerTest {

    private DocumentDownloadController controller;
    private DocumentService documentService;
    private PdfExportService pdfExportService;
    private HwpxExportService hwpxExportService;
    private ExcelExportService excelExportService;
    private LogService logService;

    @BeforeEach
    void setUp() throws Exception {
        controller = new DocumentDownloadController();
        documentService = mock(DocumentService.class);
        pdfExportService = mock(PdfExportService.class);
        hwpxExportService = mock(HwpxExportService.class);
        excelExportService = mock(ExcelExportService.class);
        logService = mock(LogService.class);

        inject("documentService", documentService);
        inject("pdfExportService", pdfExportService);
        inject("hwpxExportService", hwpxExportService);
        inject("excelExportService", excelExportService);
        inject("logService", logService);
        inject("access", new DocumentAccessSupport()); // 실제 권한 판정(SecurityContextHolder 기반)

        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() { SecurityContextHolder.clearContext(); }

    private void inject(String field, Object value) throws Exception {
        Field f = DocumentDownloadController.class.getDeclaredField(field);
        f.setAccessible(true);
        f.set(controller, value);
    }

    private void login(String authDocument, String role) {
        User u = new User();
        u.setUserSeq(1L);
        u.setUserid("tester");
        u.setUsername("테스터");
        u.setUserRole(role);
        u.setAuthDocument(authDocument);
        CustomUserDetails cud = new CustomUserDetails(u);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(cud, "N/A", cud.getAuthorities()));
    }

    private void loginEdit()  { login("EDIT", "ROLE_USER"); }
    private void loginView()  { login("VIEW", "ROLE_USER"); }
    private void loginNone()  { login("NONE", "ROLE_USER"); }
    private void loginAdmin() { login("NONE", "ROLE_ADMIN"); }

    // ───────────────────────── Excel 목록 다운로드 ─────────────────────────

    @Test
    void excelList_none_forbidden() {
        loginNone();
        ResponseEntity<byte[]> res = controller.downloadDocumentListExcel(null, null, null, null, null);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verifyNoInteractions(excelExportService);
    }

    @Test
    void excelList_edit_ok() throws Exception {
        loginEdit();
        when(documentService.searchDocuments(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Page.empty());
        when(excelExportService.generateDocumentList(any())).thenReturn(new byte[]{1, 2});
        ResponseEntity<byte[]> res = controller.downloadDocumentListExcel("COMMENCE", null, null, null, null);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).hasSize(2);
    }

    @Test
    void excelList_serviceThrows_500() {
        loginEdit();
        when(documentService.searchDocuments(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("boom"));
        ResponseEntity<byte[]> res = controller.downloadDocumentListExcel(null, null, null, null, null);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // ───────────────────────── 단건 다운로드 ─────────────────────────

    @Test
    void downloadPdf_view_forbidden() { // [viewer-action-button-guard] 조회자 다운로드 차단
        loginView();
        ResponseEntity<byte[]> res = controller.downloadPdf(5);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verifyNoInteractions(pdfExportService);
    }

    @Test
    void downloadPdf_edit_ok() {
        loginEdit();
        when(pdfExportService.generatePdf(5)).thenReturn(new byte[]{1});
        when(documentService.getDocumentById(5)).thenReturn(new Document());
        ResponseEntity<byte[]> res = controller.downloadPdf(5);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void downloadPdf_admin_ok() { // getAuth() 가 admin→"EDIT" 매핑 → 관리자 다운로드 허용(차단 아님)
        loginAdmin();
        when(pdfExportService.generatePdf(5)).thenReturn(new byte[]{1});
        when(documentService.getDocumentById(5)).thenReturn(new Document());
        assertThat(controller.downloadPdf(5).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void downloadPdf_throws_500() {
        loginEdit(); // 권한 선행이므로 500 경로 검증엔 EDIT 필요
        when(pdfExportService.generatePdf(5)).thenThrow(new RuntimeException("x"));
        ResponseEntity<byte[]> res = controller.downloadPdf(5);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void downloadHwpx_letter_edit_ok() {
        loginEdit();
        when(hwpxExportService.generateHwpx(eq(5), anyString())).thenReturn(new byte[]{1});
        when(documentService.getDocumentById(5)).thenReturn(new Document());
        ResponseEntity<byte[]> res = controller.downloadHwpx(5, "letter");
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void downloadHwpx_throws_500() {
        loginEdit();
        when(hwpxExportService.generateHwpx(anyInt(), anyString())).thenThrow(new RuntimeException("x"));
        ResponseEntity<byte[]> res = controller.downloadHwpx(5, "completion_body");
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void downloadExcel_design_edit_ok() throws Exception {
        loginEdit();
        Document doc = new Document(); // docType null → 설계내역서 분기
        when(documentService.getDocumentById(5)).thenReturn(doc);
        when(excelExportService.generateDesignEstimate(5)).thenReturn(new byte[]{1});
        ResponseEntity<byte[]> res = controller.downloadExcel(5);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(excelExportService).generateDesignEstimate(5);
    }

    @Test
    void downloadExcel_throws_500() {
        loginEdit();
        when(documentService.getDocumentById(5)).thenThrow(new RuntimeException("x"));
        ResponseEntity<byte[]> res = controller.downloadExcel(5);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void downloadZip_throws_500() {
        loginEdit();
        when(documentService.getDocumentById(5)).thenThrow(new RuntimeException("x"));
        ResponseEntity<byte[]> res = controller.downloadZip(5);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // ───────────────────────── 일괄 ZIP ─────────────────────────

    @Test
    void bulkZip_none_forbidden() {
        loginNone();
        ResponseEntity<byte[]> res = controller.downloadBulkZip(null, null, null, null, null, "letter");
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verifyNoInteractions(documentService);
    }

    @Test
    void bulkZip_invalidType_badRequest() {
        loginEdit();
        ResponseEntity<byte[]> res = controller.downloadBulkZip(null, null, null, null, null, "bogus");
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void bulkZip_noTargets_badRequest() {
        loginEdit();
        when(documentService.searchDocuments(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Page.empty());
        ResponseEntity<byte[]> res = controller.downloadBulkZip(null, null, null, null, null, "letter");
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void bulkZip_tooMany_payloadTooLarge() {
        loginEdit();
        // total > LIMIT(200) → 413. PageImpl(content, pageable, total) 로 totalElements 만 크게.
        Page<DocumentDTO> page = new PageImpl<>(List.of(), PageRequest.of(0, 201), 500);
        when(documentService.searchDocuments(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(page);
        ResponseEntity<byte[]> res = controller.downloadBulkZip(null, null, null, null, null, "letter");
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.PAYLOAD_TOO_LARGE);
    }

    // ───────────────────────── 정적 헬퍼 ─────────────────────────

    @Test
    void zipSafe_sanitizesForbiddenChars() {
        assertThat(DocumentDownloadController.zipSafe("a/b:c*?\"<>|d")).isEqualTo("abcd");
        assertThat(DocumentDownloadController.zipSafe(null)).isEqualTo("미상");
        assertThat(DocumentDownloadController.zipSafe("   ")).isEqualTo("미상");
        assertThat(DocumentDownloadController.zipSafe("..traversal")).isEqualTo("traversal");
    }

    @Test
    void zipSafe_truncatesLongName() {
        String s = DocumentDownloadController.zipSafe("가".repeat(100));
        assertThat(s.length()).isLessThanOrEqualTo(60);
    }

    @Test
    void bulkTypeLabel_mapsKnownTypes() {
        assertThat(DocumentDownloadController.bulkTypeLabel("letter")).isEqualTo("공문");
        assertThat(DocumentDownloadController.bulkTypeLabel("all")).isEqualTo("전체");
        assertThat(DocumentDownloadController.bulkTypeLabel("design")).isEqualTo("설계내역서");
        assertThat(DocumentDownloadController.bulkTypeLabel("unknown")).isEqualTo("산출물");
    }
}
