package com.swmanager.system.controller;

import com.swmanager.system.domain.User;
import com.swmanager.system.domain.workplan.Document;
import com.swmanager.system.domain.workplan.DocumentAttachment;
import com.swmanager.system.security.CustomUserDetails;
import com.swmanager.system.security.DocumentAccessSupport;
import com.swmanager.system.service.DocumentAttachmentService;
import com.swmanager.system.service.DocumentService;
import com.swmanager.system.service.DocumentSignedScanService;
import com.swmanager.system.service.LogService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * DocumentFileController 단위 테스트 (S4 Phase 4 — DocumentControllerTest 에서 이관 + 성공경로 신규).
 *
 * <p>전자서명/첨부/날인본 분리(refactor-document-controller-split-phase4). 권한은 실제
 * DocumentAccessSupport(admin→EDIT). 다운로드 성공은 200 + Content-Disposition + application/pdf 단언.
 */
class DocumentFileControllerTest {

    private DocumentFileController controller;
    private DocumentService documentService;
    private DocumentAttachmentService attachmentService;
    private DocumentSignedScanService signedScanService;
    private LogService logService;

    @BeforeEach
    void setUp() throws Exception {
        controller = new DocumentFileController();
        documentService = mock(DocumentService.class);
        attachmentService = mock(DocumentAttachmentService.class);
        signedScanService = mock(DocumentSignedScanService.class);
        logService = mock(LogService.class);

        inject("documentService", documentService);
        inject("attachmentService", attachmentService);
        inject("signedScanService", signedScanService);
        inject("logService", logService);
        inject("access", new DocumentAccessSupport());

        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() { SecurityContextHolder.clearContext(); }

    private void inject(String field, Object value) throws Exception {
        Field f = DocumentFileController.class.getDeclaredField(field);
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

    private static MockMultipartFile pdf() {
        return new MockMultipartFile("file", "a.pdf", "application/pdf", new byte[]{1, 2, 3});
    }

    // ───────────────────────── 전자서명 ─────────────────────────

    @Test
    void saveSignature_nonEdit_forbidden() {
        loginView();
        ResponseEntity<?> res = controller.saveSignature(Map.of("docId", "1"));
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verifyNoInteractions(documentService);
    }

    @Test
    void saveSignature_edit_ok() {
        loginEdit();
        ResponseEntity<?> res = controller.saveSignature(Map.of(
                "docId", "1", "signerType", "author", "signerName", "박욱진", "signerOrg", "정도UIT",
                "signatureImage", "data:image/png;base64,AAAA"));
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(documentService).saveSignature(1, "author", "박욱진", "정도UIT", "data:image/png;base64,AAAA");
    }

    // ───────────────────────── 첨부파일 ─────────────────────────

    @Test
    void uploadAttachment_nonEdit_forbidden() {
        loginView();
        ResponseEntity<?> res = controller.uploadAttachment(1, pdf());
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verifyNoInteractions(attachmentService);
    }

    @Test
    void uploadAttachment_edit_ok() throws Exception {
        loginEdit();
        DocumentAttachment att = mock(DocumentAttachment.class);
        when(att.getAttachId()).thenReturn(7);
        when(att.getFileName()).thenReturn("a.pdf");
        when(att.getFileSize()).thenReturn(3L);
        when(attachmentService.saveAttachment(eq(1), any())).thenReturn(att);
        ResponseEntity<?> res = controller.uploadAttachment(1, pdf());
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(attachmentService).saveAttachment(eq(1), any());
    }

    @Test
    void getAttachments_none_forbidden() { // [harden-read-api] 무권한 첨부 메타 차단
        loginNone();
        ResponseEntity<?> res = controller.getAttachments(1);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verifyNoInteractions(attachmentService);
    }

    @Test
    void getAttachments_returnsRows() {
        loginView();
        when(attachmentService.getAttachments(1)).thenReturn(List.of());
        ResponseEntity<?> res = controller.getAttachments(1);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getAttachments_admin_ok() { // hasDocRead: admin(authDocument=NONE+ROLE_ADMIN)→getAuth EDIT→통과(차단 아님)
        loginAdmin();
        when(attachmentService.getAttachments(1)).thenReturn(List.of());
        assertThat(controller.getAttachments(1).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void deleteAttachment_nonEdit_forbidden() {
        loginView();
        ResponseEntity<?> res = controller.deleteAttachment(1);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verifyNoInteractions(attachmentService);
    }

    @Test
    void deleteAttachment_edit_ok() {
        loginEdit();
        ResponseEntity<?> res = controller.deleteAttachment(1);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(attachmentService).deleteAttachment(1);
    }

    @Test
    void deleteAttachment_admin_ok_evenWhenAuthNone() {
        // ADMIN 분기: authDocument=NONE 이어도 isAdmin()→getAuth()="EDIT" 우회.
        loginAdmin();
        ResponseEntity<?> res = controller.deleteAttachment(1);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(attachmentService).deleteAttachment(1);
    }

    // ───────────────────────── 날인본 스캔 ─────────────────────────

    @Test
    void uploadSignedScan_nonEdit_forbidden() {
        loginView();
        ResponseEntity<?> res = controller.uploadSignedScan(1, pdf());
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verifyNoInteractions(signedScanService);
    }

    @Test
    void uploadSignedScan_edit_ok() throws Exception {
        loginEdit();
        Document doc = mock(Document.class);
        when(doc.getSignedScanOrigName()).thenReturn("날인본.pdf");
        when(doc.getSignedScanSize()).thenReturn(123L);
        when(signedScanService.uploadOrReplace(eq(1), any(), any())).thenReturn(doc);
        ResponseEntity<?> res = controller.uploadSignedScan(1, pdf());
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(signedScanService).uploadOrReplace(eq(1), any(), any());
    }

    @Test
    void uploadSignedScan_nullOrigName_ok() throws Exception { // [harden-nullsafe] origName null 이어도 200(500 아님)
        loginEdit();
        Document doc = mock(Document.class);
        when(doc.getSignedScanOrigName()).thenReturn(null);
        when(doc.getSignedScanSize()).thenReturn(null);
        when(signedScanService.uploadOrReplace(eq(1), any(), any())).thenReturn(doc);
        assertThat(controller.uploadSignedScan(1, pdf()).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void uploadSignedScan_badInput_badRequest() throws Exception {
        loginEdit();
        when(signedScanService.uploadOrReplace(eq(1), any(), any()))
                .thenThrow(new IllegalArgumentException("PDF 만 허용"));
        ResponseEntity<?> res = controller.uploadSignedScan(1, pdf());
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void downloadSignedScan_anonymous_forbidden() {
        // 미로그인 → getAuth NONE → 403
        ResponseEntity<?> res = controller.downloadSignedScan(1);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verifyNoInteractions(signedScanService);
    }

    @Test
    void downloadSignedScan_view_forbidden() { // [viewer-action-button-guard] 조회자 다운로드 차단
        loginView();
        ResponseEntity<?> res = controller.downloadSignedScan(1);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verifyNoInteractions(signedScanService);
    }

    @Test
    void downloadSignedScan_edit_ok() { // [codex] 성공 — 200 + Content-Disposition + application/pdf
        loginEdit();
        Resource resource = new ByteArrayResource(new byte[]{1, 2, 3});
        when(signedScanService.loadForDownload(1)).thenReturn(resource);
        when(signedScanService.originalName(1)).thenReturn("강진_날인본.pdf");
        ResponseEntity<Resource> res = controller.downloadSignedScan(1);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION)).contains("filename*=UTF-8''");
        assertThat(res.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PDF);
        assertThat(res.getBody()).isSameAs(resource);
    }

    @Test
    void downloadSignedScan_edit_notFound_404() {
        loginEdit(); // 권한 통과 후 미존재 → 404
        when(signedScanService.loadForDownload(1)).thenThrow(new RuntimeException("없음"));
        ResponseEntity<?> res = controller.downloadSignedScan(1);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void deleteSignedScan_nonEdit_forbidden() {
        loginView();
        ResponseEntity<?> res = controller.deleteSignedScan(1);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verifyNoInteractions(signedScanService);
    }

    @Test
    void deleteSignedScan_edit_ok() {
        loginEdit();
        ResponseEntity<?> res = controller.deleteSignedScan(1);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(signedScanService).delete(1);
    }

    @Test
    void deleteSignedScan_badInput_400() {
        loginEdit();
        org.mockito.Mockito.doThrow(new IllegalArgumentException("없음")).when(signedScanService).delete(1);
        ResponseEntity<?> res = controller.deleteSignedScan(1);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
