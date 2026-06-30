package com.swmanager.system.controller;

import com.swmanager.system.constant.enums.AccessActionType;
import com.swmanager.system.constant.enums.DocumentStatus;
import com.swmanager.system.constant.enums.DocumentType;
import com.swmanager.system.domain.Infra;
import com.swmanager.system.domain.OrgUnit;
import com.swmanager.system.domain.SwProject;
import com.swmanager.system.domain.User;
import com.swmanager.system.domain.workplan.Document;
import com.swmanager.system.domain.workplan.DocumentDetail;
import com.swmanager.system.dto.DocumentDTO;
import com.swmanager.system.dto.workplan.DocumentSaveResult;
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

    /** [owner-edit-guard] 로그인 tester(userSeq=1) 가 작성자인 문서. */
    private static User testerUser() { User u = new User(); u.setUserSeq(1L); u.setUserid("tester"); return u; }
    private static Document docOwnedByTester(Integer id) {
        Document d = new Document();
        if (id != null) d.setDocId(id);
        d.setAuthor(testerUser());
        return d;
    }

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
        existing.setAuthor(testerUser());   // [owner-edit-guard] 작성자 본인
        when(documentService.getDocumentById(7)).thenReturn(existing);
        Model m = model();
        controller.createForm("COMMENCE", null, null, 7, null, m, rttr());
        assertThat(m.getAttribute("existingDocId")).isEqualTo(7);
        assertThat(m.containsAttribute("existingDoc")).isTrue();
    }

    @Test
    void createForm_editMode_nonOwner_redirectsList() {
        loginEdit();
        stubCreateFormLookups();
        Document other = new Document(); other.setDocId(7);
        User someoneElse = new User(); someoneElse.setUserSeq(2L);   // 작성자=타인
        other.setAuthor(someoneElse);
        when(documentService.getDocumentById(7)).thenReturn(other);
        // [owner-edit-guard] 비소유 EDIT 사용자는 수정폼 진입 차단
        String view = controller.createForm("COMMENCE", null, null, 7, null, model(), rttr());
        assertThat(view).isEqualTo("redirect:/document/list");
    }

    @Test
    void createForm_editMode_adminNonOwner_loadsExistingDoc() {
        loginAdmin();
        stubCreateFormLookups();
        Document other = new Document(); other.setDocId(7); other.setTitle("타인문서");
        User someoneElse = new User(); someoneElse.setUserSeq(2L);
        other.setAuthor(someoneElse);
        when(documentService.getDocumentById(7)).thenReturn(other);
        Model m = model();
        controller.createForm("COMMENCE", null, null, 7, null, m, rttr());
        assertThat(m.containsAttribute("existingDoc")).isTrue();   // 관리자는 타인건도 진입
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
        when(documentService.getDocumentById(1)).thenReturn(docOwnedByTester(1));   // 작성자 본인
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

    // ───────────────────── 생성 폼 수정모드 rich 분기 ─────────────────────

    @Test
    void createForm_editMode_projectFilled_populatesExistingAndLegacyText() {
        loginEdit();
        stubCreateFormLookups();
        SwProject p = new SwProject();
        p.setProjId(11L); p.setYear(2025); p.setCityNm("강원도"); p.setDistNm("춘천시");
        p.setSysNm("UPIS"); p.setSysNmEn("upis"); p.setProjNm("춘천 UPIS 고도화");
        Document existing = new Document();
        existing.setDocId(7); existing.setTitle("기존문서"); existing.setSysType("UPIS");
        existing.setProject(p);
        existing.setAuthor(testerUser());   // [owner-edit-guard] 작성자 본인
        DocumentDetail det = new DocumentDetail();
        det.setSectionKey("cover"); det.setSectionData(Map.of("k", "v"));
        existing.setDetails(List.of(det));
        OrgUnit ou = new OrgUnit(); ou.setUnitId(3L);
        existing.setOrgUnit(ou);
        existing.setSupportTargetType("ORG"); existing.setEnvironment("PROD");
        when(documentService.getDocumentById(7)).thenReturn(existing);

        Model m = model();
        String view = controller.createForm("COMMENCE", null, null, 7, null, m, rttr());

        assertThat(view).isEqualTo(DocumentType.COMMENCE.templateName());
        assertThat(m.containsAttribute("existingDoc")).isTrue();
        assertThat((String) m.getAttribute("legacyTargetText")).contains("춘천시"); // 사업 포맷 분기
        assertThat(m.getAttribute("existingOrgUnitId")).isEqualTo(3L);
        assertThat(m.getAttribute("existingEnvironment")).isEqualTo("PROD");
        @SuppressWarnings("unchecked")
        Map<String, Object> existingData = (Map<String, Object>) m.getAttribute("existingDoc");
        assertThat(existingData).containsEntry("projId", 11L);
        @SuppressWarnings("unchecked")
        Map<String, Object> sections = (Map<String, Object>) existingData.get("sections");
        assertThat(sections).containsKey("cover"); // details→sectionsMap 분기
    }

    @Test
    void createForm_editMode_infraFallback_buildsInfraLegacyText() {
        loginEdit();
        stubCreateFormLookups();
        Infra inf = new Infra();
        inf.setCityNm("부산"); inf.setDistNm("해운대"); inf.setSysNm("KRAS");
        Document existing = new Document();
        existing.setDocId(8); existing.setProject(null); existing.setInfra(inf); // project=null → infra 분기
        existing.setAuthor(testerUser());   // [owner-edit-guard] 작성자 본인
        when(documentService.getDocumentById(8)).thenReturn(existing);

        Model m = model();
        String view = controller.createForm("INTERIM", null, null, 8, null, m, rttr());

        assertThat(view).isEqualTo(DocumentType.INTERIM.templateName());
        assertThat((String) m.getAttribute("legacyTargetText")).contains("해운대"); // 인프라 포맷 분기
    }

    @Test
    void createForm_editMode_loadThrows_caughtAndContinues() {
        loginEdit();
        stubCreateFormLookups();
        when(documentService.getDocumentById(9)).thenThrow(new RuntimeException("load fail"));

        Model m = model();
        String view = controller.createForm("COMMENCE", null, null, 9, null, m, rttr());
        // catch 로 삼키고 폼 정상 반환(예외 전파 없음)
        assertThat(view).isEqualTo(DocumentType.COMMENCE.templateName());
    }

    // ───────────────────────── saveDocument 성공/중복 ─────────────────────────

    @Test
    void saveDocument_create_savesSectionsAndLogs() throws Exception {
        loginEdit();
        Document created = new Document(); created.setDocId(100);
        when(documentService.findDuplicateProjDoc(any(), any(), any(), any())).thenReturn(null); // 중복 없음
        when(documentService.createDocument(any(), any(), any(), any(), any(), any(), any())).thenReturn(created);
        when(swProjectRepository.findById(10L)).thenReturn(Optional.of(new SwProject()));

        ResponseEntity<?> res = controller.saveDocument(Map.of(
                "docType", "COMMENCE", "projId", "10", "title", "착수계", "docNo", "D-1",
                "sections", List.of(Map.of("sectionKey", "cover", "sectionData", Map.of()))));

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        DocumentSaveResult body = (DocumentSaveResult) res.getBody();
        assertThat(body.success()).isTrue();
        assertThat(body.docId()).isEqualTo(100);
        verify(documentService).createDocument(any(), any(), any(), any(), any(), any(), any());
        verify(documentService).saveSection(eq(100), eq("cover"), any(), eq(0));
    }

    @Test
    void saveDocument_update_existingDoc_blankDocNoCleared() throws Exception {
        loginEdit();
        Document existing = new Document(); existing.setDocId(100); existing.setAuthor(testerUser());   // 작성자 본인
        when(documentService.getDocumentById(100)).thenReturn(existing);

        ResponseEntity<?> res = controller.saveDocument(Map.of(
                "docType", "COMMENCE", "docId", "100", "title", "수정", "docNo", "   "));

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(existing.getDocNo()).isNull(); // 공백 docNo → null
        verify(documentService).getDocumentById(100);
        verify(documentService, never()).createDocument(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void saveDocument_duplicateCommence_badRequest() {
        loginEdit();
        when(documentService.findDuplicateProjDoc(eq(10L), eq(DocumentType.COMMENCE), eq(null), eq(null)))
                .thenReturn(55);

        ResponseEntity<?> res = controller.saveDocument(Map.of(
                "docType", "COMMENCE", "projId", "10", "title", "t"));

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(documentService, never()).createDocument(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void saveDocument_duplicateInterim_parsesRound_badRequest() {
        loginEdit();
        when(documentService.findDuplicateProjDoc(eq(10L), eq(DocumentType.INTERIM), eq(2), eq(null)))
                .thenReturn(77);

        ResponseEntity<?> res = controller.saveDocument(Map.of(
                "docType", "INTERIM", "projId", "10", "title", "기성",
                "sections", List.of(Map.of("sectionKey", "inspector",
                        "sectionData", Map.of("paymentRound", "2")))));

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        // round 파싱(2) 이 findDuplicateProjDoc 에 전달됐는지 stub 매칭으로 검증됨
        verify(documentService).findDuplicateProjDoc(eq(10L), eq(DocumentType.INTERIM), eq(2), eq(null));
    }

    @Test
    void saveDocument_serviceThrows_500() {
        loginEdit();
        when(documentService.findDuplicateProjDoc(any(), any(), any(), any())).thenReturn(null); // 중복 없음
        when(documentService.createDocument(any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("boom"));

        ResponseEntity<?> res = controller.saveDocument(Map.of(
                "docType", "COMMENCE", "projId", "10", "title", "t"));

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // ───────────────────────── changeStatus 성공 ─────────────────────────

    @Test
    void changeStatus_edit_changesAndLogs() {
        loginEdit();
        when(documentService.getDocumentById(1)).thenReturn(docOwnedByTester(1));   // 작성자 본인(상태변경 가드)
        Document doc = new Document(); doc.setStatus(DocumentStatus.COMPLETED);
        when(documentService.changeStatus(eq(1), eq(DocumentStatus.COMPLETED), any(), anyString()))
                .thenReturn(doc);

        ResponseEntity<?> res = controller.changeStatus(1, DocumentStatus.COMPLETED, "완료");
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(documentService).changeStatus(eq(1), eq(DocumentStatus.COMPLETED), any(), eq("완료"));
        verify(logService).log(anyString(), any(AccessActionType.class), anyString());
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
