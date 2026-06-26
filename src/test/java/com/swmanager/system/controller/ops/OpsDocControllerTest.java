package com.swmanager.system.controller.ops;

import com.swmanager.system.constant.enums.OpsDocType;
import com.swmanager.system.domain.PersonInfo;
import com.swmanager.system.domain.SigunguCode;
import com.swmanager.system.domain.User;
import com.swmanager.system.domain.ops.OpsDocument;
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
import com.swmanager.system.service.ops.KbMatcher;
import com.swmanager.system.service.ops.OpsDocAttachmentService;
import com.swmanager.system.service.ops.OpsDocService;
import com.swmanager.system.service.ops.OpsDocSignatureService;
import com.swmanager.system.service.ops.OpsSupportFileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
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
        doc.setDocId(7L); doc.setDocType(OpsDocType.FAULT);
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
        doc.setDocId(7L); doc.setDocType(OpsDocType.FAULT);
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
    void downloadSupportFile_ok() {
        when(supportFileService.loadForDownload(1L)).thenReturn(new ByteArrayResource(new byte[]{1}));
        when(supportFileService.originalName(1L)).thenReturn("보고서.pdf");
        ResponseEntity<Resource> res = controller.downloadSupportFile(1L, viewUser());
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
    void downloadSupportFile_notFound_404() {
        when(supportFileService.loadForDownload(1L)).thenThrow(new RuntimeException("없음"));
        ResponseEntity<Resource> res = controller.downloadSupportFile(1L, viewUser());
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
}
