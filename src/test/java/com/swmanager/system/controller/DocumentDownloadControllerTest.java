package com.swmanager.system.controller;

import com.swmanager.system.constant.enums.DocumentType;
import com.swmanager.system.security.DocumentAccessSupport;
import com.swmanager.system.domain.SwProject;
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

import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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

    // ───────────────────────── 구성 헬퍼 ─────────────────────────

    private static Document doc(DocumentType type, String projNm) {
        Document d = new Document();
        d.setDocType(type);
        if (projNm != null) {
            SwProject p = new SwProject();
            p.setProjNm(projNm);
            d.setProject(p);
        }
        return d;
    }

    private static DocumentDTO dto(Integer id, DocumentType type, String city, String dist, String proj) {
        DocumentDTO d = new DocumentDTO();
        d.setDocId(id);
        d.setDocType(type);
        d.setCityNm(city);
        d.setDistNm(dist);
        d.setProjNm(proj);
        return d;
    }

    /** ZIP body → 엔트리 이름 목록(바이트 내용은 비단언, 구조만 검증). */
    private static List<String> zipEntryNames(byte[] zip) throws Exception {
        List<String> names = new ArrayList<>();
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zip))) {
            ZipEntry e;
            while ((e = zis.getNextEntry()) != null) {
                names.add(e.getName());
                zis.closeEntry();
            }
        }
        return names;
    }

    // ───────────────────────── downloadZip 성공 3종 ─────────────────────────

    @Test
    void downloadZip_commence_buildsThreeEntries() throws Exception {
        loginEdit();
        when(documentService.getDocumentById(5)).thenReturn(doc(DocumentType.COMMENCE, "사업A"));
        when(hwpxExportService.generateHwpx(eq(5), anyString())).thenReturn(new byte[]{1});
        when(excelExportService.generateDesignEstimate(5)).thenReturn(new byte[]{2});

        ResponseEntity<byte[]> res = controller.downloadZip(5);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getHeaders().getFirst("Content-Type")).isEqualTo("application/zip");
        List<String> names = zipEntryNames(res.getBody());
        assertThat(names).hasSize(3)
                .anyMatch(n -> n.contains("공문"))
                .anyMatch(n -> n.endsWith("_착수계.hwpx"))
                .anyMatch(n -> n.endsWith("_설계내역서.xlsx"));
    }

    @Test
    void downloadZip_interim_buildsThreeEntries() throws Exception {
        loginEdit();
        when(documentService.getDocumentById(5)).thenReturn(doc(DocumentType.INTERIM, "사업B"));
        when(hwpxExportService.generateHwpx(eq(5), anyString())).thenReturn(new byte[]{1});
        when(excelExportService.generateInterimReport(5)).thenReturn(new byte[]{2});

        ResponseEntity<byte[]> res = controller.downloadZip(5);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<String> names = zipEntryNames(res.getBody());
        assertThat(names).hasSize(3)
                .anyMatch(n -> n.endsWith("_기성검사원.hwpx"))
                .anyMatch(n -> n.endsWith("_기성내역서.xlsx"));
    }

    @Test
    void downloadZip_completion_krasThrows_continuesWithUpis() throws Exception {
        loginEdit();
        when(documentService.getDocumentById(5)).thenReturn(doc(DocumentType.COMPLETION, "사업C"));
        when(hwpxExportService.generateHwpx(eq(5), eq("letter"))).thenReturn(new byte[]{1});
        when(hwpxExportService.generateHwpx(eq(5), eq("completion_body"))).thenThrow(new RuntimeException("kras"));
        when(hwpxExportService.generateHwpx(eq(5), eq("completion_body_upis"))).thenReturn(new byte[]{2});

        ResponseEntity<byte[]> res = controller.downloadZip(5);
        // KRAS 는 try/ignore 로 스킵, 공문 + UPIS 만 → 200
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<String> names = zipEntryNames(res.getBody());
        assertThat(names).anyMatch(n -> n.contains("UPIS"))
                .noneMatch(n -> n.contains("KRAS"));
    }

    @Test
    void downloadZip_projNull_usesFallbackName() throws Exception {
        loginEdit();
        when(documentService.getDocumentById(5)).thenReturn(doc(DocumentType.COMMENCE, null)); // project=null → "문서"
        when(hwpxExportService.generateHwpx(eq(5), anyString())).thenReturn(new byte[]{1});
        when(excelExportService.generateDesignEstimate(5)).thenReturn(new byte[]{2});

        ResponseEntity<byte[]> res = controller.downloadZip(5);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(zipEntryNames(res.getBody())).anyMatch(n -> n.startsWith("문서_"));
    }

    // ───────────────────────── downloadBulkZip 성공 ─────────────────────────

    private void stubBulkExports() throws Exception {
        when(hwpxExportService.generateHwpx(anyInt(), anyString())).thenReturn(new byte[]{1});
        when(excelExportService.generateDesignEstimate(anyInt())).thenReturn(new byte[]{2});
        when(excelExportService.generateInterimReport(anyInt())).thenReturn(new byte[]{3});
    }

    @Test
    void bulkZip_all_mixedDocTypes_ok() throws Exception {
        loginEdit();
        stubBulkExports();
        Page<DocumentDTO> page = new PageImpl<>(List.of(
                dto(1, DocumentType.COMMENCE, "서울", "강남", "사업1"),
                dto(2, DocumentType.INTERIM, "부산", "해운대", "사업2")),
                PageRequest.of(0, 201), 2);
        when(documentService.searchDocuments(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(page);

        ResponseEntity<byte[]> res = controller.downloadBulkZip(null, null, null, null, null, "all");
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<String> names = zipEntryNames(res.getBody());
        // COMMENCE: 공문+착수계+설계내역서, INTERIM: 공문+기성검사원+기성내역서
        assertThat(names).anyMatch(n -> n.contains("착수계"))
                .anyMatch(n -> n.contains("기성검사원"));
    }

    @Test
    void bulkZip_singleInspector_mismatchWritesFailList() throws Exception {
        loginEdit();
        stubBulkExports();
        Page<DocumentDTO> page = new PageImpl<>(List.of(
                dto(1, DocumentType.INTERIM, "서울", "강남", "사업1"),     // 매칭 → 기성검사원
                dto(2, DocumentType.COMMENCE, "부산", "해운대", "사업2")),  // 비매칭 → fails
                PageRequest.of(0, 201), 2);
        when(documentService.searchDocuments(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(page);

        ResponseEntity<byte[]> res = controller.downloadBulkZip(null, null, null, null, null, "inspector");
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<String> names = zipEntryNames(res.getBody());
        assertThat(names).anyMatch(n -> n.contains("기성검사원"))
                .anyMatch(n -> n.contains("_실패목록.txt")); // 비매칭 fails 기록
    }

    @Test
    void bulkZip_dedup_appendsSuffix() throws Exception {
        loginEdit();
        stubBulkExports();
        // 동일 city/dist/proj 2건 + type=letter → base 충돌 → _2 suffix
        Page<DocumentDTO> page = new PageImpl<>(List.of(
                dto(1, DocumentType.COMMENCE, "서울", "강남", "사업동일"),
                dto(2, DocumentType.COMMENCE, "서울", "강남", "사업동일")),
                PageRequest.of(0, 201), 2);
        when(documentService.searchDocuments(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(page);

        ResponseEntity<byte[]> res = controller.downloadBulkZip(null, null, null, null, null, "letter");
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(zipEntryNames(res.getBody())).anyMatch(n -> n.contains("_2"));
    }

    @Test
    void bulkZip_completion_krasUpis_ok() throws Exception {
        loginEdit();
        stubBulkExports();
        Page<DocumentDTO> page = new PageImpl<>(List.of(
                dto(1, DocumentType.COMPLETION, "서울", "강남", "사업준공")),
                PageRequest.of(0, 201), 1);
        when(documentService.searchDocuments(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(page);

        ResponseEntity<byte[]> res = controller.downloadBulkZip(null, null, null, null, null, "completion");
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<String> names = zipEntryNames(res.getBody());
        assertThat(names).anyMatch(n -> n.contains("KRAS"))
                .anyMatch(n -> n.contains("UPIS"));
    }

    @Test
    void bulkZip_design_commence_ok() throws Exception {
        loginEdit();
        stubBulkExports();
        Page<DocumentDTO> page = new PageImpl<>(List.of(
                dto(1, DocumentType.COMMENCE, "서울", "강남", "사업설계")),
                PageRequest.of(0, 201), 1);
        when(documentService.searchDocuments(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(page);

        ResponseEntity<byte[]> res = controller.downloadBulkZip(null, null, null, null, null, "design");
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(zipEntryNames(res.getBody())).anyMatch(n -> n.contains("설계내역서"));
    }

    // ───────────────────────── downloadHwpx typeLabel switch ─────────────────────────

    @Test
    void downloadHwpx_allTypeLabels_ok() {
        loginEdit();
        when(hwpxExportService.generateHwpx(eq(5), anyString())).thenReturn(new byte[]{1});
        when(documentService.getDocumentById(5)).thenReturn(doc(DocumentType.COMPLETION, "사업H"));

        for (String type : List.of("inspector", "commence_body", "completion_body",
                "completion_body_upis", "completion_full", "custom")) {
            ResponseEntity<byte[]> res = controller.downloadHwpx(5, type);
            assertThat(res.getStatusCode()).as("type=%s", type).isEqualTo(HttpStatus.OK);
        }
        // typeLabel switch 의 ASCII suffix 는 Content-Disposition(URL 인코딩) 으로 동작 검증
        // (한글 prefix 는 %xx 인코딩되므로 ASCII 부분만 단언)
        assertThat(cd(controller.downloadHwpx(5, "completion_body"))).contains("_KRAS");
        assertThat(cd(controller.downloadHwpx(5, "completion_body_upis"))).contains("_UPIS");
        assertThat(cd(controller.downloadHwpx(5, "custom"))).contains("_custom"); // default → "_" + type
    }

    private static String cd(ResponseEntity<byte[]> res) {
        return res.getHeaders().getFirst("Content-Disposition");
    }

    @Test
    void downloadHwpx_nullProjectAndDocType_fallback() {
        loginEdit();
        when(hwpxExportService.generateHwpx(eq(5), anyString())).thenReturn(new byte[]{1});
        Document d = new Document(); // docType null, project null
        when(documentService.getDocumentById(5)).thenReturn(d);

        ResponseEntity<byte[]> res = controller.downloadHwpx(5, "letter");
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // ───────────────────────── downloadExcel INTERIM ─────────────────────────

    @Test
    void downloadExcel_interim_usesInterimReport() throws Exception {
        loginEdit();
        when(documentService.getDocumentById(5)).thenReturn(doc(DocumentType.INTERIM, "사업I"));
        when(excelExportService.generateInterimReport(5)).thenReturn(new byte[]{1});

        ResponseEntity<byte[]> res = controller.downloadExcel(5);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(excelExportService).generateInterimReport(5);
    }

    // ───────────────────────── excel-list prefix 분기 ─────────────────────────

    @Test
    void excelList_invalidDocType_ignoresLabel_ok() throws Exception {
        loginEdit();
        when(documentService.searchDocuments(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Page.empty());
        when(excelExportService.generateDocumentList(any())).thenReturn(new byte[]{1});
        // "ZZZ" → DocumentType.fromString IllegalArgumentException ignore → prefix 기본
        ResponseEntity<byte[]> res = controller.downloadDocumentListExcel("ZZZ", null, null, null, null);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void excelList_blankDocType_ok() throws Exception {
        loginEdit();
        when(documentService.searchDocuments(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Page.empty());
        when(excelExportService.generateDocumentList(any())).thenReturn(new byte[]{1});
        ResponseEntity<byte[]> res = controller.downloadDocumentListExcel("   ", null, null, null, null);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
