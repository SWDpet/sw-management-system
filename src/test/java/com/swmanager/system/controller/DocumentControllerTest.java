package com.swmanager.system.controller;

import com.swmanager.system.constant.enums.DocumentStatus;
import com.swmanager.system.domain.SwProject;
import com.swmanager.system.domain.User;
import com.swmanager.system.domain.workplan.Document;
import com.swmanager.system.dto.DocumentDTO;
import com.swmanager.system.repository.InfraRepository;
import com.swmanager.system.repository.PersonInfoRepository;
import com.swmanager.system.repository.SwProjectRepository;
import com.swmanager.system.repository.UserRepository;
import com.swmanager.system.security.CustomUserDetails;
import com.swmanager.system.service.DocumentAttachmentService;
import com.swmanager.system.service.DocumentService;
import com.swmanager.system.service.DocumentSignedScanService;
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
import org.springframework.mock.web.MockMultipartFile;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * DocumentController 단위 테스트 (beyond-A 커버리지 스프린트 — 컨트롤러 통합영역).
 *
 * <p>컨벤션: DocumentController 는 권한을 메서드 파라미터가 아니라
 * {@link SecurityContextHolder} 의 principal({@link CustomUserDetails}) 에서 읽으므로
 * (getCurrentUser/getAuth/isAdmin), 풀 {@code @SpringBootTest}+MockMvc 대신
 * <b>SecurityContext 를 직접 세팅 + 의존성 mock 주입 후 컨트롤러 메서드 직접 호출</b>한다
 * ({@code OpsDocControllerAttachmentGuardTest} 의 직접호출 패턴 확장).
 *
 * <p>장점: 실 Postgres 불필요(운영DB 무접촉) → 기본 CI 에서도 실행되어 JaCoCo floor 에 반영되고,
 * 모든 권한 분기(NONE/EDIT/ADMIN)와 정적 헬퍼·조회 경로를 결정적으로 커버한다.
 * 쓰기(save/delete/status/batch-generate) 는 가드(403) 및 입력검증(400) 경로 위주로 검증하여
 * 서비스 호출(부수효과)이 발생하지 않음을 함께 단언한다.
 */
class DocumentControllerTest {

    private DocumentController controller;

    private DocumentService documentService;
    private InfraRepository infraRepository;
    private PersonInfoRepository personInfoRepository;
    private SwProjectRepository swProjectRepository;
    private UserRepository userRepository;
    private LogService logService;
    private PdfExportService pdfExportService;
    private HwpxExportService hwpxExportService;
    private ExcelExportService excelExportService;
    private DocumentAttachmentService attachmentService;
    private DocumentSignedScanService signedScanService;

    @BeforeEach
    void setUp() throws Exception {
        controller = new DocumentController();
        documentService = mock(DocumentService.class);
        infraRepository = mock(InfraRepository.class);
        personInfoRepository = mock(PersonInfoRepository.class);
        swProjectRepository = mock(SwProjectRepository.class);
        userRepository = mock(UserRepository.class);
        logService = mock(LogService.class);
        pdfExportService = mock(PdfExportService.class);
        hwpxExportService = mock(HwpxExportService.class);
        excelExportService = mock(ExcelExportService.class);
        attachmentService = mock(DocumentAttachmentService.class);
        signedScanService = mock(DocumentSignedScanService.class);

        inject("documentService", documentService);
        inject("infraRepository", infraRepository);
        inject("personInfoRepository", personInfoRepository);
        inject("swProjectRepository", swProjectRepository);
        inject("userRepository", userRepository);
        inject("logService", logService);
        inject("pdfExportService", pdfExportService);
        inject("hwpxExportService", hwpxExportService);
        inject("excelExportService", excelExportService);
        inject("attachmentService", attachmentService);
        inject("signedScanService", signedScanService);

        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void inject(String field, Object value) throws Exception {
        Field f = DocumentController.class.getDeclaredField(field);
        f.setAccessible(true);
        f.set(controller, value);
    }

    /** authDocument 권한 + role 로 principal 을 SecurityContext 에 세팅한다. */
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

    private static Model model() { return new ExtendedModelMap(); }
    private static RedirectAttributes rttr() { return new RedirectAttributesModelMap(); }

    // ───────────────────────── D-01 문서 목록 ─────────────────────────

    @Test
    void documentList_none_redirectsHome() {
        loginNone();
        String view = controller.documentList(null, null, null, null, null,
                PageRequest.of(0, 15), model(), rttr());
        assertThat(view).isEqualTo("redirect:/");
        verifyNoInteractions(documentService);
    }

    @Test
    void documentList_edit_rendersList() {
        loginEdit();
        when(documentService.searchDocuments(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Page.empty());
        when(documentService.getCityNames()).thenReturn(List.of("강원도"));
        Model m = model();
        String view = controller.documentList(null, null, null, null, null,
                PageRequest.of(0, 15), m, rttr());
        assertThat(view).isEqualTo("document/document-list");
        assertThat(m.getAttribute("userAuth")).isEqualTo("EDIT");
        assertThat(m.containsAttribute("documents")).isTrue();
    }

    @Test
    void documentList_withCity_loadsDistList() {
        loginView();
        when(documentService.searchDocuments(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Page.empty());
        when(documentService.getCityNames()).thenReturn(List.of("강원도"));
        when(documentService.getDistNamesByCity("강원도")).thenReturn(List.of("춘천시"));
        Model m = model();
        // VIEW 권한도 목록 접근 가능함을 명시(뷰 단언) → 이후 cityNm 분기 검증의 전제.
        String view = controller.documentList(null, "강원도", null, null, null,
                PageRequest.of(0, 15), m, rttr());
        assertThat(view).isEqualTo("document/document-list");
        verify(documentService).getDistNamesByCity("강원도");
        assertThat(m.getAttribute("distList")).isEqualTo(List.of("춘천시"));
    }

    @Test
    void getDistList_delegatesToService() {
        when(documentService.getDistNamesByCity("강원도")).thenReturn(List.of("원주시"));
        assertThat(controller.getDistList("강원도")).containsExactly("원주시");
    }

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

    // ───────────────────────── D-10 상세 ─────────────────────────

    @Test
    void documentDetail_none_redirectsHome() {
        loginNone();
        String view = controller.documentDetail(1, model(), rttr());
        assertThat(view).isEqualTo("redirect:/");
        verifyNoInteractions(documentService);
    }

    @Test
    void documentDetail_edit_rendersDetail() {
        loginEdit();
        when(documentService.getDocumentById(1)).thenReturn(new Document());
        when(documentService.getDocumentHistory(1)).thenReturn(List.of());
        when(userRepository.findByEnabledTrue()).thenReturn(List.of());
        Model m = model();
        String view = controller.documentDetail(1, m, rttr());
        assertThat(view).isEqualTo("document/document-detail");
        assertThat(m.containsAttribute("doc")).isTrue();
        assertThat(m.getAttribute("userAuth")).isEqualTo("EDIT");
    }

    // ───────────────────────── 생성 폼 ─────────────────────────

    @Test
    void createForm_nonEdit_redirectsList() {
        loginView();
        String view = controller.createForm("COMMENCE", null, null, null, null, model(), rttr());
        assertThat(view).isEqualTo("redirect:/document/list");
    }

    @Test
    void createForm_blankDocType_redirectsList() {
        loginEdit();
        String view = controller.createForm("", null, null, null, null, model(), rttr());
        assertThat(view).isEqualTo("redirect:/document/list");
    }

    @Test
    void createForm_inspect_routesToInspectTemplate() {
        loginEdit();
        stubCreateFormLookups();
        String view = controller.createForm("INSPECT", null, null, null, null, model(), rttr());
        assertThat(view).isEqualTo("document/doc-inspect");
    }

    @Test
    void createForm_commence_returnsTemplate() {
        loginEdit();
        stubCreateFormLookups();
        String view = controller.createForm("COMMENCE", null, null, null, null, model(), rttr());
        assertThat(view).isEqualTo(com.swmanager.system.constant.enums.DocumentType.COMMENCE.templateName());
    }

    @Test
    void createForm_unknownDocType_redirectsList() {
        loginEdit();
        stubCreateFormLookups();
        String view = controller.createForm("XXX", null, null, null, null, model(), rttr());
        assertThat(view).isEqualTo("redirect:/document/list");
    }

    @Test
    void createForm_editMode_loadsExistingDoc() {
        loginEdit();
        stubCreateFormLookups();
        Document existing = new Document();
        existing.setDocId(7);
        existing.setTitle("기존문서");
        when(documentService.getDocumentById(7)).thenReturn(existing);
        Model m = model();
        controller.createForm("COMMENCE", null, null, 7, null, m, rttr());
        assertThat(m.getAttribute("existingDocId")).isEqualTo(7);
        assertThat(m.containsAttribute("existingDoc")).isTrue();
    }

    private void stubCreateFormLookups() {
        when(infraRepository.findAll(any(org.springframework.data.domain.Sort.class))).thenReturn(List.of());
        when(swProjectRepository.findAll(any(org.springframework.data.domain.Sort.class))).thenReturn(List.of());
        when(userRepository.findByEnabledTrue()).thenReturn(List.of());
    }

    // ───────────────────────── 저장/상태/삭제 가드 ─────────────────────────

    @Test
    void saveDocument_nonEdit_forbidden() {
        loginView();
        ResponseEntity<?> res = controller.saveDocument(Map.of("docType", "COMMENCE"));
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verifyNoInteractions(documentService);
    }

    @Test
    void saveDocument_invalidDocType_badRequest() {
        loginEdit();
        ResponseEntity<?> res = controller.saveDocument(Map.of("docType", "BOGUS"));
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(documentService, never()).createDocument(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void changeStatus_nonEdit_forbidden() {
        loginView();
        ResponseEntity<?> res = controller.changeStatus(1, DocumentStatus.COMPLETED, null);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verifyNoInteractions(documentService);
    }

    @Test
    void deleteDocument_nonEdit_redirectsList() {
        loginView();
        String view = controller.deleteDocument(1, rttr());
        assertThat(view).isEqualTo("redirect:/document/list");
        verifyNoInteractions(documentService);
    }

    @Test
    void deleteDocument_edit_deletesAndRedirects() {
        loginEdit();
        String view = controller.deleteDocument(1, rttr());
        assertThat(view).isEqualTo("redirect:/document/list");
        verify(documentService).deleteDocument(1);
    }

    // ───────────────────────── 미리보기/출력 ─────────────────────────

    @Test
    void preview_rendersHtml() {
        when(pdfExportService.renderDocumentToHtml(5)).thenReturn("<html/>");
        Model m = model();
        String view = controller.previewDocument(5, m);
        assertThat(view).isEqualTo("document/document-preview");
        assertThat(m.getAttribute("docId")).isEqualTo(5);
    }

    @Test
    void downloadPdf_ok() {
        when(pdfExportService.generatePdf(5)).thenReturn(new byte[]{1});
        when(documentService.getDocumentById(5)).thenReturn(new Document());
        ResponseEntity<byte[]> res = controller.downloadPdf(5);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void downloadPdf_throws_500() {
        when(pdfExportService.generatePdf(5)).thenThrow(new RuntimeException("x"));
        ResponseEntity<byte[]> res = controller.downloadPdf(5);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void downloadHwpx_letter_ok() {
        when(hwpxExportService.generateHwpx(eq(5), anyString())).thenReturn(new byte[]{1});
        when(documentService.getDocumentById(5)).thenReturn(new Document());
        ResponseEntity<byte[]> res = controller.downloadHwpx(5, "letter");
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void downloadHwpx_throws_500() {
        when(hwpxExportService.generateHwpx(anyInt(), anyString())).thenThrow(new RuntimeException("x"));
        ResponseEntity<byte[]> res = controller.downloadHwpx(5, "completion_body");
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void downloadExcel_design_ok() throws Exception {
        Document doc = new Document(); // docType null → 설계내역서 분기
        when(documentService.getDocumentById(5)).thenReturn(doc);
        when(excelExportService.generateDesignEstimate(5)).thenReturn(new byte[]{1});
        ResponseEntity<byte[]> res = controller.downloadExcel(5);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(excelExportService).generateDesignEstimate(5);
    }

    @Test
    void downloadExcel_throws_500() {
        when(documentService.getDocumentById(5)).thenThrow(new RuntimeException("x"));
        ResponseEntity<byte[]> res = controller.downloadExcel(5);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void downloadZip_throws_500() {
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

    // ───────────────────────── 전자서명/첨부 가드 ─────────────────────────

    @Test
    void saveSignature_nonEdit_forbidden() {
        loginView();
        ResponseEntity<?> res = controller.saveSignature(Map.of("docId", "1"));
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verifyNoInteractions(documentService);
    }

    @Test
    void uploadAttachment_nonEdit_forbidden() {
        loginView();
        ResponseEntity<?> res = controller.uploadAttachment(1, pdf());
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verifyNoInteractions(attachmentService);
    }

    @Test
    void getAttachments_returnsRows() {
        when(attachmentService.getAttachments(1)).thenReturn(List.of());
        ResponseEntity<?> res = controller.getAttachments(1);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
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
        // ADMIN 분기: authDocument=NONE 이어도 isAdmin()→getAuth()="EDIT" 우회 (docstring ADMIN 커버 근거).
        loginAdmin();
        ResponseEntity<?> res = controller.deleteAttachment(1);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(attachmentService).deleteAttachment(1);
    }

    // ───────────────────────── 날인본 스캔 가드 ─────────────────────────

    @Test
    void uploadSignedScan_nonEdit_forbidden() {
        loginView();
        ResponseEntity<?> res = controller.uploadSignedScan(1, pdf());
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verifyNoInteractions(signedScanService);
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
        // 로그인 안함 → getCurrentUser null → 403
        ResponseEntity<?> res = controller.downloadSignedScan(1);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verifyNoInteractions(signedScanService);
    }

    @Test
    void downloadSignedScan_notFound_404() {
        loginView();
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

    // ───────────────────────── 사용자/사업 정보 ─────────────────────────

    @Test
    void getUserInfo_found() {
        User u = new User();
        u.setUserSeq(2L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(u));
        ResponseEntity<?> res = controller.getUserInfo(2L);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getUserInfo_notFound() {
        when(userRepository.findById(9L)).thenReturn(Optional.empty());
        ResponseEntity<?> res = controller.getUserInfo(9L);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getUserInfoSecure_nonEdit_forbidden() {
        loginView();
        ResponseEntity<?> res = controller.getUserInfoSecure(2L);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verifyNoInteractions(userRepository);
    }

    @Test
    void getUserInfoSecure_edit_found() {
        loginEdit();
        User u = new User();
        u.setUserSeq(2L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(u));
        ResponseEntity<?> res = controller.getUserInfoSecure(2L);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getProjectInfo_notFound() {
        when(swProjectRepository.findById(9L)).thenReturn(Optional.empty());
        ResponseEntity<?> res = controller.getProjectInfo(9L);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getProjectInfo_found() {
        SwProject p = new SwProject();
        p.setProjId(3L);
        p.setProjNm("사업A");
        when(swProjectRepository.findById(3L)).thenReturn(Optional.of(p));
        ResponseEntity<Map<String, Object>> res = controller.getProjectInfo(3L);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).containsEntry("projNm", "사업A");
    }

    // ───────────────────────── 일괄 작성 ─────────────────────────

    @Test
    void batchPage_nonEdit_redirectsList() {
        loginView();
        String view = controller.batchPage(model());
        assertThat(view).isEqualTo("redirect:/document/list");
    }

    @Test
    void batchPage_edit_rendersPage() {
        loginEdit();
        when(userRepository.findByEnabledTrue()).thenReturn(List.of());
        String view = controller.batchPage(model());
        assertThat(view).isEqualTo("document/doc-batch");
    }

    @Test
    void batchTargets_invalidDocType_badRequest() {
        ResponseEntity<?> res = controller.getBatchTargets(2026, "BOGUS", null);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void batchTargets_commence_badRequest() {
        // COMMENCE 는 일괄 대상 아님 → 400
        ResponseEntity<?> res = controller.getBatchTargets(2026, "COMMENCE", null);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void batchTargets_interim_ok() {
        when(swProjectRepository.findByYearAndInterimYnOrderByCityNmAscDistNmAsc(2026, "Y"))
                .thenReturn(List.of());
        ResponseEntity<?> res = controller.getBatchTargets(2026, "INTERIM", null);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void allSystemsForYear_filtersByYear() {
        when(swProjectRepository.findAll()).thenReturn(List.of());
        ResponseEntity<?> res = controller.getAllSystemsForYear(2026);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void batchGenerate_nonEdit_forbidden() {
        loginView();
        ResponseEntity<?> res = controller.batchGenerate(Map.of("docType", "INTERIM"));
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verifyNoInteractions(documentService);
    }

    @Test
    void batchGenerate_invalidDocType_badRequest() {
        loginEdit();
        ResponseEntity<?> res = controller.batchGenerate(Map.of("docType", "BOGUS"));
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void batchGenerate_missingProject_countsAsFail() {
        loginEdit();
        when(swProjectRepository.findById(99L)).thenReturn(Optional.empty());
        Map<String, Object> req = Map.of("docType", "INTERIM", "projIds", List.of(99));
        ResponseEntity<Map<String, Object>> res = controller.batchGenerate(req);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).containsEntry("failCount", 1);
    }

    // ───────────────────────── 사업수행계획서 ─────────────────────────

    @Test
    void getPlanData_notFound() {
        when(swProjectRepository.findById(9L)).thenReturn(Optional.empty());
        ResponseEntity<?> res = controller.getPlanData(9L);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getPlanData_found() throws Exception {
        SwProject p = new SwProject();
        p.setProjId(3L);
        when(swProjectRepository.findById(3L)).thenReturn(Optional.of(p));
        // pjt* repository 는 미주입(null) → NPE 방지 위해 stub 주입
        injectPjtRepos();
        ResponseEntity<?> res = controller.getPlanData(3L);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void savePlanData_nonEdit_forbidden() {
        loginView();
        ResponseEntity<?> res = controller.savePlanData(3L, Map.of());
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verifyNoInteractions(swProjectRepository);
    }

    private void injectPjtRepos() throws Exception {
        var pjtTargetRepo = mock(com.swmanager.system.repository.PjtTargetRepository.class);
        var pjtManpowerRepo = mock(com.swmanager.system.repository.PjtManpowerPlanRepository.class);
        var pjtScheduleRepo = mock(com.swmanager.system.repository.PjtScheduleRepository.class);
        when(pjtTargetRepo.findByProjIdOrderBySortOrderAsc(any())).thenReturn(List.of());
        when(pjtManpowerRepo.findByProjIdOrderBySortOrderAsc(any())).thenReturn(List.of());
        when(pjtScheduleRepo.findByProjIdOrderBySortOrderAsc(any())).thenReturn(List.of());
        inject("pjtTargetRepository", pjtTargetRepo);
        inject("pjtManpowerPlanRepository", pjtManpowerRepo);
        inject("pjtScheduleRepository", pjtScheduleRepo);
    }

    // ───────────────────────── 정적 헬퍼 ─────────────────────────

    @Test
    void zipSafe_sanitizesForbiddenChars() {
        assertThat(DocumentController.zipSafe("a/b:c*?\"<>|d")).isEqualTo("abcd");
        assertThat(DocumentController.zipSafe(null)).isEqualTo("미상");
        assertThat(DocumentController.zipSafe("   ")).isEqualTo("미상");
        assertThat(DocumentController.zipSafe("..traversal")).isEqualTo("traversal");
    }

    @Test
    void zipSafe_truncatesLongName() {
        String s = DocumentController.zipSafe("가".repeat(100));
        assertThat(s.length()).isLessThanOrEqualTo(60);
    }

    @Test
    void bulkTypeLabel_mapsKnownTypes() {
        assertThat(DocumentController.bulkTypeLabel("letter")).isEqualTo("공문");
        assertThat(DocumentController.bulkTypeLabel("all")).isEqualTo("전체");
        assertThat(DocumentController.bulkTypeLabel("design")).isEqualTo("설계내역서");
        assertThat(DocumentController.bulkTypeLabel("unknown")).isEqualTo("산출물");
    }

    private static MockMultipartFile pdf() {
        return new MockMultipartFile("file", "a.pdf", "application/pdf", new byte[]{1, 2, 3});
    }
}
