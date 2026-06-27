package com.swmanager.system.controller;

import com.swmanager.system.constant.enums.DocumentStatus;
import com.swmanager.system.domain.SwProject;
import com.swmanager.system.domain.User;
import com.swmanager.system.domain.workplan.Document;
import com.swmanager.system.dto.DocumentDTO;
import com.swmanager.system.repository.InfraRepository;
import com.swmanager.system.repository.SwProjectRepository;
import com.swmanager.system.repository.UserRepository;
import com.swmanager.system.security.DocumentAccessSupport;
import com.swmanager.system.security.CustomUserDetails;
import com.swmanager.system.service.DocumentService;
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
    private SwProjectRepository swProjectRepository;
    private UserRepository userRepository;
    private LogService logService;
    private PdfExportService pdfExportService;   // [S4] previewDocument 잔존

    @BeforeEach
    void setUp() throws Exception {
        controller = new DocumentController();
        documentService = mock(DocumentService.class);
        infraRepository = mock(InfraRepository.class);
        swProjectRepository = mock(SwProjectRepository.class);
        userRepository = mock(UserRepository.class);
        logService = mock(LogService.class);
        pdfExportService = mock(PdfExportService.class);

        inject("documentService", documentService);
        inject("infraRepository", infraRepository);
        inject("swProjectRepository", swProjectRepository);
        inject("userRepository", userRepository);
        inject("logService", logService);
        inject("pdfExportService", pdfExportService);
        inject("access", new DocumentAccessSupport()); // [S4] 인증 위임 — 잔존 권한 테스트가 getAuth()→access 경로 사용

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

    // [S4 Phase 1] excelList* 다운로드 테스트는 DocumentDownloadControllerTest 로 이관.

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

    // [S4 Phase 1] download*(pdf/hwpx/excel/zip)·bulkZip* 테스트는 DocumentDownloadControllerTest 로 이관.

    // [S4 Phase 4] 전자서명/첨부/날인본 가드·동작 테스트는 DocumentFileControllerTest 로 이관.

    // [S4 Phase 5] 사용자/사업 정보 조회(getUserInfo/getUserInfoSecure/getProjectInfo) 테스트는
    // DocumentLookupControllerTest 로 이관.

    // [S4 Phase 2] 일괄 작성(batchPage/batchTargets/allSystemsForYear/batchGenerate) 테스트는
    // DocumentBatchControllerTest 로 이관.

    // ───────────────────────── 사업수행계획서 ─────────────────────────

    // [S4 Phase 3] 사업수행계획서(getPlanData/savePlanData) 테스트는 DocumentPlanControllerTest 로 이관.

    // [S4 Phase 1] zipSafe/bulkTypeLabel static 헬퍼 테스트는 DocumentDownloadControllerTest 로 이관.
}
