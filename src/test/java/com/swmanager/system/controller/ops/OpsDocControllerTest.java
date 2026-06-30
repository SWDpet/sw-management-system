package com.swmanager.system.controller.ops;

import com.swmanager.system.constant.enums.OpsDocType;
import com.swmanager.system.domain.InspectReport;
import com.swmanager.system.domain.OrgUnit;
import com.swmanager.system.domain.PersonInfo;
import com.swmanager.system.domain.SigunguCode;
import com.swmanager.system.domain.SwProject;
import com.swmanager.system.domain.SysMst;
import com.swmanager.system.domain.User;
import com.swmanager.system.domain.ops.OpsDocPartner;
import com.swmanager.system.domain.ops.OpsDocument;
import com.swmanager.system.domain.ops.Partner;
import com.swmanager.system.domain.ops.PartnerContact;
import com.swmanager.system.domain.ops.Staff;
import com.swmanager.system.dto.ops.FeedbackForm;
import com.swmanager.system.dto.ops.OpsDocForm;
import com.swmanager.system.dto.ops.RequesterForm;
import com.swmanager.system.repository.InspectReportRepository;
import com.swmanager.system.repository.OrgUnitRepository;
import com.swmanager.system.repository.PersonInfoRepository;
import com.swmanager.system.repository.SigunguCodeRepository;
import com.swmanager.system.repository.SwProjectRepository;
import com.swmanager.system.repository.SysMstRepository;
import com.swmanager.system.repository.UserRepository;
import com.swmanager.system.repository.ops.OpsDocPartnerRepository;
import com.swmanager.system.repository.ops.OpsKbFeedbackRepository;
import com.swmanager.system.repository.ops.PartnerContactRepository;
import com.swmanager.system.repository.ops.PartnerRepository;
import com.swmanager.system.repository.ops.StaffRepository;
import com.swmanager.system.security.CustomUserDetails;
import com.swmanager.system.service.inspection.InspectMaintProfile;
import com.swmanager.system.service.ops.KbMatcher;
import com.swmanager.system.service.ops.OpsDocAttachmentService;
import com.swmanager.system.service.ops.OpsDocService;
import com.swmanager.system.service.ops.OpsDocSignatureService;
import com.swmanager.system.service.ops.OpsSupportFileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * OpsDocController 단위 테스트 (beyond-A 커버리지 스프린트 — 컨트롤러 통합영역 3탄).
 *
 * <p>OpsDocController 는 {@code @RequiredArgsConstructor}(생성자 주입, 17 의존성)이고, 권한은 대부분
 * {@code @AuthenticationPrincipal CustomUserDetails} <b>메서드 파라미터</b>로 받는다(SecurityContext 불필요).
 * 따라서 모든 의존성을 mock 으로 생성자 주입하고, 권한 사용자를 파라미터로 직접 전달해 메서드를 호출한다.
 *
 * <p>첨부 4종(getAttachments/uploadAttachment/deleteAttachment/delete)은 기존
 * {@link OpsDocControllerAttachmentGuardTest} 가 이미 커버하므로 여기서는 list/폼라우팅/CRUD 가드·
 * 지원파일·검색/캐스케이드/관계자 경로를 보강한다. 실 Postgres 불필요 → 기본 CI 에서 JaCoCo floor 반영.
 */
class OpsDocControllerTest {

    private OpsDocController controller;

    private OpsDocService opsDocService;
    private OpsSupportFileService supportFileService;
    private OpsDocAttachmentService attachmentService;
    private OpsDocSignatureService signatureService;
    private SigunguCodeRepository sigunguCodeRepository;
    private InspectReportRepository inspectReportRepository;
    private SwProjectRepository swProjectRepository;
    private UserRepository userRepository;
    private PersonInfoRepository personInfoRepository;
    private OrgUnitRepository orgUnitRepository;
    private PartnerContactRepository partnerContactRepository;
    private KbMatcher kbMatcher;
    private OpsKbFeedbackRepository opsKbFeedbackRepository;
    private PartnerRepository partnerRepository;
    private OpsDocPartnerRepository opsDocPartnerRepository;
    private SysMstRepository sysMstRepository;
    private StaffRepository staffRepository;

    @BeforeEach
    void setUp() {
        opsDocService = mock(OpsDocService.class);
        supportFileService = mock(OpsSupportFileService.class);
        attachmentService = mock(OpsDocAttachmentService.class);
        signatureService = mock(OpsDocSignatureService.class);
        sigunguCodeRepository = mock(SigunguCodeRepository.class);
        inspectReportRepository = mock(InspectReportRepository.class);
        swProjectRepository = mock(SwProjectRepository.class);
        userRepository = mock(UserRepository.class);
        personInfoRepository = mock(PersonInfoRepository.class);
        orgUnitRepository = mock(OrgUnitRepository.class);
        partnerContactRepository = mock(PartnerContactRepository.class);
        kbMatcher = mock(KbMatcher.class);
        opsKbFeedbackRepository = mock(OpsKbFeedbackRepository.class);
        partnerRepository = mock(PartnerRepository.class);
        opsDocPartnerRepository = mock(OpsDocPartnerRepository.class);
        sysMstRepository = mock(SysMstRepository.class);
        staffRepository = mock(StaffRepository.class);

        controller = new OpsDocController(
                opsDocService, supportFileService, attachmentService, signatureService,
                sigunguCodeRepository, inspectReportRepository, swProjectRepository, userRepository,
                personInfoRepository, orgUnitRepository, partnerContactRepository, kbMatcher,
                opsKbFeedbackRepository, partnerRepository, opsDocPartnerRepository, sysMstRepository,
                staffRepository);

        // [owner-edit-guard] 기본: docId 조회 문서는 로그인 tester 소유(개별 테스트가 필요 시 findById override)
        OpsDocument ownedByTester = new OpsDocument();
        ownedByTester.setCreatedBy("tester");
        org.mockito.Mockito.lenient().when(opsDocService.findById(anyLong()))
                .thenReturn(Optional.of(ownedByTester));
    }

    private static Model model() { return new ExtendedModelMap(); }

    private static CustomUserDetails user(String authDocument, String role) {
        User u = new User();
        u.setUserSeq(1L);
        u.setUserid("tester");
        u.setUsername("테스터");
        u.setUserRole(role);
        u.setAuthDocument(authDocument);
        return new CustomUserDetails(u);
    }
    private static CustomUserDetails editUser()  { return user("EDIT", "ROLE_USER"); }
    private static CustomUserDetails viewUser()  { return user("VIEW", "ROLE_USER"); }
    private static CustomUserDetails adminUser() { return user("NONE", "ROLE_ADMIN"); }

    private static OpsDocForm minimalForm() {
        return new OpsDocForm("제목", "GIS", null, null, null, null, null, null, null, null);
    }

    private static MultipartFile file() {
        return new MockMultipartFile("file", "a.pdf", "application/pdf", new byte[]{1, 2, 3});
    }

    /** 응답 본문을 Map 으로 캐스팅(컨트롤러 성공 응답은 HashMap) — toString 의존 제거. */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> bodyMap(ResponseEntity<?> res) {
        return (Map<String, Object>) res.getBody();
    }

    // ───────────────────────── 통합 리스트 ─────────────────────────

    @Test
    void list_rendersWithRegionResolution() {
        SigunguCode sgg = new SigunguCode();
        sgg.setAdmSectC("11110");
        sgg.setSidoNm("서울특별시");
        sgg.setSggNm("종로구");
        when(sigunguCodeRepository.findAll()).thenReturn(List.of(sgg));
        when(sigunguCodeRepository.findDistinctSidoNm()).thenReturn(List.of("서울특별시"));

        OpsDocument fault = new OpsDocument();
        fault.setDocId(1L);
        fault.setDocType(OpsDocType.FAULT);
        fault.setRegionCode("11110");
        fault.setSysType("GIS");
        fault.setDocNo("FLT-2026-1");
        fault.setTitle("장애처리 건");
        OpsDocument inspect = new OpsDocument();
        inspect.setDocId(2L);
        inspect.setDocType(OpsDocType.INSPECT);
        inspect.setDocNo("INSP-2026-5");
        when(opsDocService.findAll()).thenReturn(List.of(fault, inspect));
        // INSPECT 지역해석 경로: report 미존재 → "-"
        when(inspectReportRepository.findById(5L)).thenReturn(Optional.empty());

        Model m = model();
        String view = controller.list(null, null, null, null, null, null, m);
        assertThat(view).isEqualTo("ops-doc/list");
        @SuppressWarnings("unchecked")
        List<OpsDocument> docs = (List<OpsDocument>) m.getAttribute("documents");
        assertThat(docs).hasSize(2);
        // regionCode 직접매핑 → 서울특별시
        @SuppressWarnings("unchecked")
        Map<Long, String> sidoByDoc = (Map<Long, String>) m.getAttribute("sidoByDoc");
        assertThat(sidoByDoc.get(1L)).isEqualTo("서울특별시");
    }

    @Test
    void list_filtersByDocTypeAndKeyword() {
        when(sigunguCodeRepository.findAll()).thenReturn(List.of());
        when(sigunguCodeRepository.findDistinctSidoNm()).thenReturn(List.of());
        OpsDocument fault = new OpsDocument();
        fault.setDocId(1L); fault.setDocType(OpsDocType.FAULT); fault.setTitle("네트워크 장애"); fault.setSysType("GIS");
        OpsDocument support = new OpsDocument();
        support.setDocId(2L); support.setDocType(OpsDocType.SUPPORT); support.setTitle("업무지원"); support.setSysType("GIS");
        when(opsDocService.findAll()).thenReturn(List.of(fault, support));

        Model m = model();
        controller.list("FAULT", null, "네트워크", null, null, null, m);
        @SuppressWarnings("unchecked")
        List<OpsDocument> docs = (List<OpsDocument>) m.getAttribute("documents");
        assertThat(docs).extracting(OpsDocument::getDocId).containsExactly(1L);
    }

    @Test
    void list_inspectMissingReport_resolvesToDash() {
        when(sigunguCodeRepository.findAll()).thenReturn(List.of());
        when(sigunguCodeRepository.findDistinctSidoNm()).thenReturn(List.of());
        OpsDocument inspect = new OpsDocument();
        inspect.setDocId(2L); inspect.setDocType(OpsDocType.INSPECT); inspect.setDocNo("INSP-2026-5");
        when(opsDocService.findAll()).thenReturn(List.of(inspect));
        when(inspectReportRepository.findById(5L)).thenReturn(Optional.empty());
        Model m = model();
        controller.list(null, null, null, null, null, null, m);
        @SuppressWarnings("unchecked")
        Map<Long, String> sidoByDoc = (Map<Long, String>) m.getAttribute("sidoByDoc");
        assertThat(sidoByDoc.get(2L)).isEqualTo("-"); // report 미존재 → 지역 미해석
    }

    // ───────────────────────── 폼/상세 라우팅 ─────────────────────────

    @Test
    void form_fault_returnsTemplate() {
        when(sigunguCodeRepository.findDistinctSidoNm()).thenReturn(List.of());
        // 리터럴로 단언(enum 매핑 재유도 시 동어반복 회피).
        assertThat(controller.form("FAULT", model(), editUser())).isEqualTo("ops-doc/doc-fault");
    }

    @Test
    void form_inspect_redirectsList() {
        assertThat(controller.form("INSPECT", model(), editUser())).isEqualTo("redirect:/ops-doc/list");
    }

    @Test
    void form_unknown_throws() {
        // OpsDocType.fromString 은 미허용 값에 IllegalArgumentException 을 던진다(컨트롤러 null 분기는 도달 불가).
        assertThatThrownBy(() -> controller.form("BOGUS", model(), editUser()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void editForm_fault_returnsTemplate() {
        OpsDocument doc = new OpsDocument();
        doc.setDocId(7L); doc.setDocType(OpsDocType.FAULT); doc.setCreatedBy("tester");
        when(opsDocService.findById(7L)).thenReturn(Optional.of(doc));
        when(sigunguCodeRepository.findDistinctSidoNm()).thenReturn(List.of());
        assertThat(controller.editForm("FAULT", 7L, model(), editUser())).isEqualTo("ops-doc/doc-fault");
    }

    @Test
    void editForm_inspect_redirectsToReport() {
        OpsDocument doc = new OpsDocument();
        doc.setDocId(7L); doc.setDocType(OpsDocType.INSPECT); doc.setDocNo("INSP-2026-9");
        when(opsDocService.findById(7L)).thenReturn(Optional.of(doc));
        assertThat(controller.editForm("INSPECT", 7L, model(), editUser()))
                .isEqualTo("redirect:/document/inspect-detail/9");
    }

    @Test
    void editForm_notFound_throws() {
        when(opsDocService.findById(7L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> controller.editForm("FAULT", 7L, model(), editUser()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void detail_fault_returnsTemplate_editUserCanEdit() {
        OpsDocument doc = new OpsDocument();
        doc.setDocId(7L); doc.setDocType(OpsDocType.FAULT); doc.setCreatedBy("tester");
        when(opsDocService.findById(7L)).thenReturn(Optional.of(doc));
        when(sigunguCodeRepository.findDistinctSidoNm()).thenReturn(List.of());
        Model m = model();
        assertThat(controller.detail("FAULT", 7L, m, editUser())).isEqualTo("ops-doc/doc-fault");
        assertThat(m.getAttribute("mode")).isEqualTo("view");
        assertThat(m.getAttribute("canEdit")).isEqualTo(true);   // [viewer-action-button-guard] EDIT → 수정/삭제 노출
    }

    @Test
    void detail_fault_viewUser_canEditFalse() {
        OpsDocument doc = new OpsDocument();
        doc.setDocId(7L); doc.setDocType(OpsDocType.FAULT);
        when(opsDocService.findById(7L)).thenReturn(Optional.of(doc));
        when(sigunguCodeRepository.findDistinctSidoNm()).thenReturn(List.of());
        Model m = model();
        controller.detail("FAULT", 7L, m, viewUser());
        assertThat(m.getAttribute("canEdit")).isEqualTo(false);  // 조회자 → 수정/삭제 숨김
    }

    @Test
    void detail_inspect_redirectsToReport() {
        OpsDocument doc = new OpsDocument();
        doc.setDocId(7L); doc.setDocType(OpsDocType.INSPECT); doc.setDocNo("INSP-2026-12");
        when(opsDocService.findById(7L)).thenReturn(Optional.of(doc));
        assertThat(controller.detail("INSPECT", 7L, model(), editUser()))
                .isEqualTo("redirect:/document/inspect-detail/12");
    }

    // ───────────────────────── CRUD API ─────────────────────────

    @Test
    void create_inspect_badRequest() {
        ResponseEntity<?> res = controller.create("INSPECT", minimalForm(), editUser());
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(opsDocService, never()).create(any(), any(), any());
    }

    @Test
    void create_viewOnly_forbidden() {
        ResponseEntity<?> res = controller.create("FAULT", minimalForm(), viewUser());
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(opsDocService, never()).create(any(), any(), any());
    }

    @Test
    void create_edit_ok() {
        OpsDocument saved = new OpsDocument();
        saved.setDocId(50L); saved.setDocNo("FLT-2026-50");
        when(opsDocService.create(any(), any(), any())).thenReturn(saved);
        ResponseEntity<?> res = controller.create("FAULT", minimalForm(), editUser());
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(bodyMap(res)).containsEntry("docNo", "FLT-2026-50").containsEntry("docId", 50L);
    }

    @Test
    void create_admin_ok() {
        OpsDocument saved = new OpsDocument();
        saved.setDocId(51L); saved.setDocNo("FLT-2026-51");
        when(opsDocService.create(any(), any(), any())).thenReturn(saved);
        ResponseEntity<?> res = controller.create("FAULT", minimalForm(), adminUser());
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(bodyMap(res)).containsEntry("docNo", "FLT-2026-51");
        verify(opsDocService).create(any(), any(), any());
    }

    @Test
    void update_inspect_badRequest() {
        ResponseEntity<?> res = controller.update("INSPECT", 1L, minimalForm(), editUser());
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(opsDocService, never()).update(anyLong(), any(), any(), any());
    }

    @Test
    void update_viewOnly_forbidden() {
        ResponseEntity<?> res = controller.update("FAULT", 1L, minimalForm(), viewUser());
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(opsDocService, never()).update(anyLong(), any(), any(), any());
    }

    @Test
    void update_edit_ok() {
        OpsDocument updated = new OpsDocument();
        updated.setDocId(60L);
        when(opsDocService.update(eq(60L), any(), any(), any())).thenReturn(updated);
        ResponseEntity<?> res = controller.update("FAULT", 60L, minimalForm(), editUser());
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(opsDocService).update(eq(60L), any(), any(), any());
    }

    // ───────────────────────── 지원파일 업로드/다운로드/삭제 ─────────────────────────

    @Test
    void uploadSupportFile_viewOnly_forbidden() {
        ResponseEntity<?> res = controller.uploadSupportFile(1L, file(), viewUser());
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void uploadSupportFile_edit_ok() throws Exception {
        OpsDocument doc = new OpsDocument();
        doc.setSupportFileOrigName("보고서.pdf");
        doc.setSupportFileSize(123L);
        when(supportFileService.uploadOrReplace(eq(1L), any(), any())).thenReturn(doc);
        ResponseEntity<?> res = controller.uploadSupportFile(1L, file(), editUser());
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(bodyMap(res)).containsEntry("fileName", "보고서.pdf").containsEntry("fileSize", 123L);
    }

    @Test
    void uploadSupportFile_badInput_badRequest() throws Exception {
        when(supportFileService.uploadOrReplace(anyLong(), any(), any()))
                .thenThrow(new IllegalArgumentException("PDF/한글/엑셀/워드만 허용"));
        ResponseEntity<?> res = controller.uploadSupportFile(1L, file(), editUser());
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void downloadSupportFile_noView_forbidden() {
        ResponseEntity<Resource> res = controller.downloadSupportFile(1L, user("NONE", "ROLE_USER"));
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void downloadSupportFile_viewOnly_forbidden() { // [viewer-action-button-guard] 조회자 다운로드 차단(기존 requireDocView→EDIT)
        ResponseEntity<Resource> res = controller.downloadSupportFile(1L, viewUser());
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(supportFileService, never()).loadForDownload(anyLong());
    }

    @Test
    void downloadSupportFile_edit_ok() {
        when(supportFileService.loadForDownload(1L)).thenReturn(new ByteArrayResource(new byte[]{1}));
        when(supportFileService.originalName(1L)).thenReturn("보고서.pdf");
        ResponseEntity<Resource> res = controller.downloadSupportFile(1L, editUser());
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void downloadSupportFile_admin_ok() {
        when(supportFileService.loadForDownload(1L)).thenReturn(new ByteArrayResource(new byte[]{1}));
        when(supportFileService.originalName(1L)).thenReturn("보고서.pdf");
        ResponseEntity<Resource> res = controller.downloadSupportFile(1L, adminUser());
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void downloadSupportFile_edit_notFound_404() {
        when(supportFileService.loadForDownload(1L)).thenThrow(new RuntimeException("없음"));
        ResponseEntity<Resource> res = controller.downloadSupportFile(1L, editUser());
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void deleteSupportFile_viewOnly_forbidden() {
        ResponseEntity<?> res = controller.deleteSupportFile(1L, viewUser());
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void deleteSupportFile_edit_ok() {
        ResponseEntity<?> res = controller.deleteSupportFile(1L, editUser());
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(supportFileService).delete(1L);
    }

    // ───────────────────────── 관계자/검색/추천 ─────────────────────────

    @Test
    void engineers_noSwTeam_returnsEmpty() {
        when(orgUnitRepository.findFirstByNameAndUnitType("SW지원팀", "TEAM")).thenReturn(Optional.empty());
        assertThat(controller.engineers()).isEmpty();
    }

    @Test
    void requesterSearch_returnsEmpty() {
        when(personInfoRepository.findAllByKeyword(any(), any())).thenReturn(Page.empty());
        assertThat(controller.requesterSearch("kim")).isEmpty();
    }

    @Test
    void requesterCreate_viewOnly_forbidden() {
        ResponseEntity<?> res = controller.requesterCreate(
                new RequesterForm("홍길동", null, null, null, null, null), viewUser());
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(personInfoRepository, never()).save(any());
    }

    @Test
    void requesterCreate_blankName_badRequest() {
        ResponseEntity<?> res = controller.requesterCreate(
                new RequesterForm("  ", null, null, null, null, null), editUser());
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(personInfoRepository, never()).save(any());
    }

    @Test
    void requesterCreate_edit_ok() {
        PersonInfo saved = new PersonInfo();
        saved.setId(99L);
        saved.setUserNm("홍길동");
        when(personInfoRepository.save(any())).thenReturn(saved);
        ResponseEntity<?> res = controller.requesterCreate(
                new RequesterForm("홍길동", "시청", "전산과", "주무관", "010", "서울"), editUser());
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(bodyMap(res)).containsEntry("name", "홍길동").containsEntry("id", 99L);
    }

    @Test
    void partnerContactSearch_returnsEmpty() {
        when(partnerContactRepository.searchActive(any())).thenReturn(List.of());
        assertThat(controller.partnerContactSearch("a")).isEmpty();
    }

    @Test
    void kbRecommend_fault_delegatesWithGubun() {
        when(kbMatcher.recommend(eq("장애"), any(), any(), eq(5))).thenReturn(List.of());
        assertThat(controller.kbRecommend("FAULT", "GIS", "다운")).isEmpty();
        verify(kbMatcher).recommend(eq("장애"), eq("GIS"), eq("다운"), eq(5));
    }

    @Test
    void kbFeedback_viewOnly_forbidden() {
        ResponseEntity<?> res = controller.kbFeedback(new FeedbackForm(1L, null, "APPLIED"), viewUser());
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(opsKbFeedbackRepository, never()).save(any());
    }

    @Test
    void kbFeedback_nullKbId_badRequest() {
        ResponseEntity<?> res = controller.kbFeedback(new FeedbackForm(null, null, "APPLIED"), editUser());
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(opsKbFeedbackRepository, never()).save(any());
    }

    @Test
    void kbFeedback_edit_ok() {
        ResponseEntity<?> res = controller.kbFeedback(new FeedbackForm(7L, 3L, "IGNORED"), editUser());
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(opsKbFeedbackRepository).save(any());
    }

    @Test
    void kbFeedback_nonOwnerWithDocId_forbidden() {
        OpsDocument others = new OpsDocument(); others.setCreatedBy("other");   // 타인 작성 문서
        when(opsDocService.findById(3L)).thenReturn(Optional.of(others));
        ResponseEntity<?> res = controller.kbFeedback(new FeedbackForm(7L, 3L, "IGNORED"), editUser());
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(opsKbFeedbackRepository, never()).save(any());
    }

    @Test
    void kbFeedback_newDoc_nullDocId_ok() {
        ResponseEntity<?> res = controller.kbFeedback(new FeedbackForm(7L, null, "APPLIED"), editUser());
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);   // 신규작성 중(docId=null) 무제한
        verify(opsKbFeedbackRepository).save(any());
    }

    @Test
    void partnerSearch_returnsEmpty() {
        when(partnerRepository.findByUseYnAndNameContainingOrderByNameAsc(eq("Y"), any())).thenReturn(List.of());
        assertThat(controller.partnerSearch("co")).isEmpty();
    }

    @Test
    void docPartners_returnsEmpty() {
        when(opsDocPartnerRepository.findByDocId(1L)).thenReturn(List.of());
        assertThat(controller.docPartners(1L)).isEmpty();
    }

    @Test
    void staffSearch_returnsEmpty() {
        when(staffRepository.searchActive(any())).thenReturn(List.of());
        assertThat(controller.staffSearch("park")).isEmpty();
    }

    // ───────────────────────── 캐스케이드/관계 프리필 ─────────────────────────

    @Test
    void cascadeSgg_returnsEmpty() {
        when(sigunguCodeRepository.findBySidoNmOrderBySggNm("서울특별시")).thenReturn(List.of());
        assertThat(controller.cascadeSgg("서울특별시")).isEmpty();
    }

    @Test
    void cascadeSystems_returnsEmpty() {
        when(sysMstRepository.findAll(any(org.springframework.data.domain.Sort.class))).thenReturn(List.of());
        assertThat(controller.cascadeSystems()).isEmpty();
    }

    @Test
    void cascadeRegion_found_returnsMap() {
        SigunguCode s = new SigunguCode();
        s.setAdmSectC("11110"); s.setSidoNm("서울특별시"); s.setSggNm("종로구");
        when(sigunguCodeRepository.findById("11110")).thenReturn(Optional.of(s));
        Map<String, Object> m = controller.cascadeRegion("11110");
        assertThat(m).containsEntry("sido", "서울특별시").containsEntry("sgg", "종로구");
    }

    @Test
    void cascadeRegion_notFound_emptyMap() {
        when(sigunguCodeRepository.findById("00000")).thenReturn(Optional.empty());
        assertThat(controller.cascadeRegion("00000")).isEmpty();
    }

    @Test
    void relations_notFound_emptyMap() {
        when(opsDocService.findById(1L)).thenReturn(Optional.empty());
        assertThat(controller.relations(1L)).isEmpty();
    }

    @Test
    void relations_withPersonRequester_returnsMap() {
        OpsDocument doc = new OpsDocument();
        PersonInfo p = new PersonInfo();
        p.setId(5L); p.setUserNm("김담당"); p.setOrgNm("시청");
        doc.setRequesterPerson(p);
        when(opsDocService.findById(1L)).thenReturn(Optional.of(doc));
        Map<String, Object> m = controller.relations(1L);
        assertThat(m).containsEntry("requester_kind", "PERSON").containsEntry("requester_id", 5L);
    }

    // ───────── INSPECT 지역/유형 해석 (resolveRegion/resolveMaintType, nc 125-126,180-194,209) ─────────

    /** 단일 후보: report→project, bySgg[distNm] 단일 → pick=cand.get(0) + maint 배지. */
    @Test
    void list_inspectDoc_singleCandidate_resolvesRegionAndMaintBadge() {
        SigunguCode sgg = new SigunguCode();
        sgg.setAdmSectC("11110"); sgg.setSidoNm("서울특별시"); sgg.setSggNm("종로구");
        when(sigunguCodeRepository.findAll()).thenReturn(List.of(sgg));
        when(sigunguCodeRepository.findDistinctSidoNm()).thenReturn(List.of("서울특별시"));

        OpsDocument inspect = new OpsDocument();
        inspect.setDocId(7L); inspect.setDocType(OpsDocType.INSPECT); inspect.setDocNo("INSP-2026-42");
        when(opsDocService.findAll()).thenReturn(List.of(inspect));
        InspectReport report = new InspectReport(); report.setPjtId(70L);
        when(inspectReportRepository.findById(42L)).thenReturn(Optional.of(report));
        SwProject pj = new SwProject();
        pj.setCityNm("서울특별시"); pj.setDistNm("종로구"); pj.setMaintType("HS");
        when(swProjectRepository.findById(70L)).thenReturn(Optional.of(pj));

        Model m = model();
        controller.list(null, null, null, null, null, null, m);
        @SuppressWarnings("unchecked") Map<Long, String> sidoByDoc = (Map<Long, String>) m.getAttribute("sidoByDoc");
        @SuppressWarnings("unchecked") Map<Long, String> sggByDoc = (Map<Long, String>) m.getAttribute("sggByDoc");
        @SuppressWarnings("unchecked") Map<Long, String> maintLabel = (Map<Long, String>) m.getAttribute("maintLabelByDoc");
        @SuppressWarnings("unchecked") Map<Long, String> maintTone = (Map<Long, String>) m.getAttribute("maintToneByDoc");
        assertThat(sidoByDoc.get(7L)).isEqualTo("서울특별시");
        assertThat(sggByDoc.get(7L)).isEqualTo("종로구");
        assertThat(maintLabel.get(7L)).isEqualTo(InspectMaintProfile.badgeLabel("HS"));
        assertThat(maintTone.get(7L)).isEqualTo(InspectMaintProfile.badgeTone("HS"));
    }

    /** 복수 후보: cand.size()>1 → cityNm 매칭되는 2번째 후보 pick (cand.get(0) 아님). */
    @Test
    void list_inspectDoc_multiCandidate_picksBySido() {
        SigunguCode c0 = new SigunguCode();
        c0.setAdmSectC("26000"); c0.setSidoNm("부산광역시"); c0.setSggNm("중구");
        SigunguCode c1 = new SigunguCode();
        c1.setAdmSectC("11000"); c1.setSidoNm("서울특별시"); c1.setSggNm("중구");
        when(sigunguCodeRepository.findAll()).thenReturn(List.of(c0, c1)); // bySgg["중구"]=[c0,c1]
        when(sigunguCodeRepository.findDistinctSidoNm()).thenReturn(List.of("서울특별시", "부산광역시"));

        OpsDocument inspect = new OpsDocument();
        inspect.setDocId(8L); inspect.setDocType(OpsDocType.INSPECT); inspect.setDocNo("INSP-2026-43");
        when(opsDocService.findAll()).thenReturn(List.of(inspect));
        InspectReport report = new InspectReport(); report.setPjtId(80L);
        when(inspectReportRepository.findById(43L)).thenReturn(Optional.of(report));
        SwProject pj = new SwProject();
        pj.setCityNm("서울특별시"); pj.setDistNm("중구"); pj.setMaintType("DHS"); // c1 만 매칭
        when(swProjectRepository.findById(80L)).thenReturn(Optional.of(pj));

        Model m = model();
        controller.list(null, null, null, null, null, null, m);
        @SuppressWarnings("unchecked") Map<Long, String> sidoByDoc = (Map<Long, String>) m.getAttribute("sidoByDoc");
        @SuppressWarnings("unchecked") Map<Long, String> sggByDoc = (Map<Long, String>) m.getAttribute("sggByDoc");
        // cand.get(0)=부산이 아니라 cityNm 매칭된 서울(c1) 선택
        assertThat(sidoByDoc.get(8L)).isEqualTo("서울특별시");
        assertThat(sggByDoc.get(8L)).isEqualTo("중구"); // 선택된 후보의 sggNm 매핑 확인
    }

    /** 후보 없음: bySgg[distNm] 미존재 → {nz(cityNm), nz(distNm)} 폴백. */
    @Test
    void list_inspectDoc_noCandidate_fallsBackToProjectNames() {
        when(sigunguCodeRepository.findAll()).thenReturn(List.of()); // bySgg 비어있음
        when(sigunguCodeRepository.findDistinctSidoNm()).thenReturn(List.of());

        OpsDocument inspect = new OpsDocument();
        inspect.setDocId(9L); inspect.setDocType(OpsDocType.INSPECT); inspect.setDocNo("INSP-2026-44");
        when(opsDocService.findAll()).thenReturn(List.of(inspect));
        InspectReport report = new InspectReport(); report.setPjtId(90L);
        when(inspectReportRepository.findById(44L)).thenReturn(Optional.of(report));
        SwProject pj = new SwProject();
        pj.setCityNm("강원특별자치도"); pj.setDistNm("춘천시"); pj.setMaintType(null);
        when(swProjectRepository.findById(90L)).thenReturn(Optional.of(pj));

        Model m = model();
        controller.list(null, null, null, null, null, null, m);
        @SuppressWarnings("unchecked") Map<Long, String> sidoByDoc = (Map<Long, String>) m.getAttribute("sidoByDoc");
        @SuppressWarnings("unchecked") Map<Long, String> sggByDoc = (Map<Long, String>) m.getAttribute("sggByDoc");
        @SuppressWarnings("unchecked") Map<Long, String> maintLabel = (Map<Long, String>) m.getAttribute("maintLabelByDoc");
        assertThat(sidoByDoc.get(9L)).isEqualTo("강원특별자치도");
        assertThat(sggByDoc.get(9L)).isEqualTo("춘천시");
        assertThat(maintLabel).doesNotContainKey(9L); // maintType null → 배지 미생성
    }

    /** parseReportId catch: docNo 끝이 숫자 아님 → reportId null → "-". (nc 218) */
    @Test
    void list_inspectDoc_unparseableDocNo_resolvesToDash() {
        when(sigunguCodeRepository.findAll()).thenReturn(List.of());
        when(sigunguCodeRepository.findDistinctSidoNm()).thenReturn(List.of());
        OpsDocument inspect = new OpsDocument();
        inspect.setDocId(10L); inspect.setDocType(OpsDocType.INSPECT); inspect.setDocNo("INSP-2026-XX");
        when(opsDocService.findAll()).thenReturn(List.of(inspect));

        Model m = model();
        controller.list(null, null, null, null, null, null, m);
        @SuppressWarnings("unchecked") Map<Long, String> sidoByDoc = (Map<Long, String>) m.getAttribute("sidoByDoc");
        assertThat(sidoByDoc.get(10L)).isEqualTo("-");
        verify(inspectReportRepository, never()).findById(any());
    }

    // ───────── support-file 일반 예외 500 (nc 458-460, 495-497) ─────────

    @Test
    void uploadSupportFile_serverError_returns500() throws Exception {
        when(supportFileService.uploadOrReplace(anyLong(), any(), any()))
                .thenThrow(new RuntimeException("disk full"));
        ResponseEntity<?> res = controller.uploadSupportFile(1L, file(), editUser());
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void deleteSupportFile_serverError_returns500() {
        org.mockito.Mockito.doThrow(new RuntimeException("io"))
                .when(supportFileService).delete(anyLong());
        ResponseEntity<?> res = controller.deleteSupportFile(1L, editUser());
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // ───────── create 경유 applyRelations + savePartners (nc 504-517, 646-656) ─────────

    /** PERSON 요청자 + 엔지니어 + partners 중복쌍 → 캡처 검증 + savePartners dedup 2회. */
    @Test
    void create_personRequesterAndPartners_appliesRelationsAndDedupes() {
        User eng = new User(); eng.setUserSeq(5L);
        when(userRepository.findById(5L)).thenReturn(Optional.of(eng));
        PersonInfo reqP = new PersonInfo(); reqP.setId(9L);
        when(personInfoRepository.findById(9L)).thenReturn(Optional.of(reqP));
        OpsDocument saved = new OpsDocument(); saved.setDocId(100L); saved.setDocNo("FLT-100");
        when(opsDocService.create(any(), any(), any())).thenReturn(saved);

        OpsDocForm form = new OpsDocForm("제목", "GIS", null, null, null,
                5L, "PERSON", 9L, null,
                List.of(new OpsDocForm.PartnerRef(3L, "주관"),
                        new OpsDocForm.PartnerRef(3L, "주관"),   // (3,주관) 중복 → 제거
                        new OpsDocForm.PartnerRef(7L, "협력")));
        ResponseEntity<?> res = controller.create("FAULT", form, editUser());
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);

        ArgumentCaptor<OpsDocument> docCap = ArgumentCaptor.forClass(OpsDocument.class);
        verify(opsDocService).create(docCap.capture(), any(), any());
        OpsDocument captured = docCap.getValue();
        assertThat(captured.getEngineer()).isSameAs(eng);
        assertThat(captured.getRequesterPerson()).isSameAs(reqP);
        assertThat(captured.getRequesterContactId()).isNull();
        assertThat(captured.getRequesterStaffId()).isNull();

        ArgumentCaptor<OpsDocPartner> pCap = ArgumentCaptor.forClass(OpsDocPartner.class);
        verify(opsDocPartnerRepository, times(2)).save(pCap.capture());
        assertThat(pCap.getAllValues()).extracting(OpsDocPartner::getPartnerId).containsExactly(3L, 7L);
        assertThat(pCap.getAllValues()).extracting(OpsDocPartner::getRoleLabel).containsExactly("주관", "협력");
    }

    /** CONTACT 요청자 → requesterContactId 세팅(personInfo 조회 없음). */
    @Test
    void create_contactRequester_setsContactId() {
        OpsDocument saved = new OpsDocument(); saved.setDocId(101L); saved.setDocNo("FLT-101");
        when(opsDocService.create(any(), any(), any())).thenReturn(saved);
        OpsDocForm form = new OpsDocForm("제목", "GIS", null, null, null,
                null, "CONTACT", 11L, null, null);
        controller.create("FAULT", form, editUser());

        ArgumentCaptor<OpsDocument> docCap = ArgumentCaptor.forClass(OpsDocument.class);
        verify(opsDocService).create(docCap.capture(), any(), any());
        assertThat(docCap.getValue().getRequesterContactId()).isEqualTo(11L);
        assertThat(docCap.getValue().getRequesterPerson()).isNull();
        verify(personInfoRepository, never()).findById(any());
    }

    /** STAFF 요청자 → requesterStaffId 세팅. */
    @Test
    void create_staffRequester_setsStaffId() {
        OpsDocument saved = new OpsDocument(); saved.setDocId(102L); saved.setDocNo("FLT-102");
        when(opsDocService.create(any(), any(), any())).thenReturn(saved);
        OpsDocForm form = new OpsDocForm("제목", "GIS", null, null, null,
                null, "STAFF", 22L, null, null);
        controller.create("FAULT", form, editUser());

        ArgumentCaptor<OpsDocument> docCap = ArgumentCaptor.forClass(OpsDocument.class);
        verify(opsDocService).create(docCap.capture(), any(), any());
        assertThat(docCap.getValue().getRequesterStaffId()).isEqualTo(22L);
    }

    // ───────── 검색/캐스케이드 데이터 경로 (Row.from 매핑, nc 530-543,581-582,624-637,666-696) ─────────

    @Test
    void engineers_withSwTeam_mapsRows() {
        OrgUnit team = new OrgUnit(); team.setUnitId(3L);
        when(orgUnitRepository.findFirstByNameAndUnitType("SW지원팀", "TEAM")).thenReturn(Optional.of(team));
        User u = new User(); u.setUserSeq(5L); u.setUsername("홍길동"); u.setPosition("선임");
        when(userRepository.findByOrgUnitIdAndEnabledTrueOrderByUsernameAsc(3L)).thenReturn(List.of(u));

        var rows = controller.engineers();
        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).id()).isEqualTo(5L);
        assertThat(rows.get(0).name()).isEqualTo("홍길동");
        assertThat(rows.get(0).position()).isEqualTo("선임");
    }

    @Test
    void requesterSearch_withData_mapsRows() {
        PersonInfo p = new PersonInfo();
        p.setId(12L); p.setUserNm("김공무"); p.setOrgNm("강진군청"); p.setDeptNm("정보과"); p.setPos("주무관"); p.setTel("061-000");
        when(personInfoRepository.findAllByKeyword(eq("kim"), any()))
                .thenReturn(new PageImpl<>(List.of(p)));

        var rows = controller.requesterSearch("kim");
        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).id()).isEqualTo(12L);
        assertThat(rows.get(0).name()).isEqualTo("김공무");
        assertThat(rows.get(0).org()).isEqualTo("강진군청");
        assertThat(rows.get(0).dept()).isEqualTo("정보과");
        assertThat(rows.get(0).pos()).isEqualTo("주무관");
        assertThat(rows.get(0).tel()).isEqualTo("061-000");
    }

    @Test
    void partnerContactSearch_withData_mapsOrgFromPartner() {
        Partner pt = new Partner(); pt.setName("(주)지오");
        PartnerContact c = new PartnerContact();
        c.setContactId(21L); c.setName("이담당"); c.setPartner(pt); c.setPosition("과장"); c.setTel("010-1");
        when(partnerContactRepository.searchActive("a")).thenReturn(List.of(c));

        var rows = controller.partnerContactSearch("a");
        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).id()).isEqualTo(21L);
        assertThat(rows.get(0).name()).isEqualTo("이담당");
        assertThat(rows.get(0).org()).isEqualTo("(주)지오"); // partner.name
        assertThat(rows.get(0).pos()).isEqualTo("과장");
    }

    @Test
    void partnerSearch_withData_mapsRows() {
        Partner p = new Partner(); p.setPartnerId(31L); p.setName("(주)협력"); p.setPartnerType("유지보수");
        when(partnerRepository.findByUseYnAndNameContainingOrderByNameAsc("Y", "co")).thenReturn(List.of(p));

        var rows = controller.partnerSearch("co");
        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).id()).isEqualTo(31L);
        assertThat(rows.get(0).name()).isEqualTo("(주)협력");
        assertThat(rows.get(0).type()).isEqualTo("유지보수");
    }

    /** docPartners: partner found(업체명) + missing("#id") 양분기. */
    @Test
    void docPartners_withData_mapsFoundAndMissing() {
        OpsDocPartner dp1 = new OpsDocPartner(); dp1.setPartnerId(41L); dp1.setRoleLabel("주관");
        OpsDocPartner dp2 = new OpsDocPartner(); dp2.setPartnerId(42L); dp2.setRoleLabel("협력");
        when(opsDocPartnerRepository.findByDocId(1L)).thenReturn(List.of(dp1, dp2));
        Partner found = new Partner(); found.setPartnerId(41L); found.setName("(주)에이");
        when(partnerRepository.findById(41L)).thenReturn(Optional.of(found));
        when(partnerRepository.findById(42L)).thenReturn(Optional.empty());

        var rows = controller.docPartners(1L);
        assertThat(rows).hasSize(2);
        assertThat(rows.get(0).name()).isEqualTo("(주)에이");
        assertThat(rows.get(1).name()).isEqualTo("#42"); // missing 폴백
    }

    /** staffSearch: orgUnit found(라벨) + orgUnitId null(unit=null) 양분기. */
    @Test
    void staffSearch_withData_resolvesOrgUnitBranches() {
        Staff s1 = new Staff(); s1.setStaffId(51L); s1.setName("박직원"); s1.setPosition("대리"); s1.setOrgUnitId(4L);
        Staff s2 = new Staff(); s2.setStaffId(52L); s2.setName("최직원"); s2.setPosition("사원"); s2.setOrgUnitId(null);
        when(staffRepository.searchActive("park")).thenReturn(List.of(s1, s2));
        OrgUnit ou = new OrgUnit(); ou.setUnitId(4L); ou.setName("SW지원팀");
        when(orgUnitRepository.findById(4L)).thenReturn(Optional.of(ou));

        var rows = controller.staffSearch("park");
        assertThat(rows).hasSize(2);
        assertThat(rows.get(0).org()).isEqualTo("SW지원팀");
        assertThat(rows.get(1).org()).isNull();
    }

    @Test
    void cascadeSgg_withData_mapsRows() {
        SigunguCode s = new SigunguCode();
        s.setAdmSectC("11110"); s.setSidoNm("서울특별시"); s.setSggNm("종로구");
        when(sigunguCodeRepository.findBySidoNmOrderBySggNm("서울특별시")).thenReturn(List.of(s));

        var rows = controller.cascadeSgg("서울특별시");
        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).admSectC()).isEqualTo("11110");
        assertThat(rows.get(0).sggNm()).isEqualTo("종로구");
        assertThat(rows.get(0).isUnit()).isFalse(); // sggNm != sidoNm
    }

    @Test
    void cascadeSystems_withData_mapsRows() {
        SysMst sys = new SysMst(); sys.setCd("KRAS"); sys.setNm("지반정보시스템");
        when(sysMstRepository.findAll(any(Sort.class))).thenReturn(List.of(sys));

        var rows = controller.cascadeSystems();
        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).cd()).isEqualTo("KRAS");
        assertThat(rows.get(0).nm()).isEqualTo("지반정보시스템");
    }

    // ───────── relations 프리필 CONTACT/STAFF (nc 727-739) ─────────

    @Test
    void relations_contactRequester_found_mapsLabel() {
        OpsDocument doc = new OpsDocument();
        doc.setRequesterContactId(61L);
        when(opsDocService.findById(1L)).thenReturn(Optional.of(doc));
        Partner pt = new Partner(); pt.setName("(주)지오");
        PartnerContact c = new PartnerContact(); c.setContactId(61L); c.setName("이담당"); c.setPartner(pt);
        when(partnerContactRepository.findById(61L)).thenReturn(Optional.of(c));

        Map<String, Object> m = controller.relations(1L);
        assertThat(m).containsEntry("requester_kind", "CONTACT").containsEntry("requester_id", 61L);
        assertThat(m.get("requester_label")).isEqualTo("이담당 / (주)지오");
    }

    @Test
    void relations_contactRequester_missing_fallsBackToHashId() {
        OpsDocument doc = new OpsDocument();
        doc.setRequesterContactId(62L);
        when(opsDocService.findById(1L)).thenReturn(Optional.of(doc));
        when(partnerContactRepository.findById(62L)).thenReturn(Optional.empty());

        Map<String, Object> m = controller.relations(1L);
        assertThat(m.get("requester_label")).isEqualTo("업체담당자 #62");
    }

    @Test
    void relations_staffRequester_found_mapsLabel() {
        OpsDocument doc = new OpsDocument();
        doc.setRequesterStaffId(71L);
        when(opsDocService.findById(1L)).thenReturn(Optional.of(doc));
        Staff st = new Staff(); st.setStaffId(71L); st.setName("박직원"); st.setPosition("대리");
        when(staffRepository.findById(71L)).thenReturn(Optional.of(st));

        Map<String, Object> m = controller.relations(1L);
        assertThat(m).containsEntry("requester_kind", "STAFF").containsEntry("requester_id", 71L);
        assertThat(m.get("requester_label")).isEqualTo("박직원 대리");
    }
}
