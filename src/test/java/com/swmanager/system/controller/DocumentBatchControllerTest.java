package com.swmanager.system.controller;

import com.swmanager.system.domain.SwProject;
import com.swmanager.system.domain.User;
import com.swmanager.system.domain.workplan.Document;
import com.swmanager.system.security.CustomUserDetails;
import com.swmanager.system.security.DocumentAccessSupport;
import com.swmanager.system.service.DocumentService;
import com.swmanager.system.service.LogService;
import com.swmanager.system.repository.SwProjectRepository;
import com.swmanager.system.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * DocumentBatchController 단위 테스트 (S4 Phase 2 — DocumentControllerTest 에서 이관 + 성공경로 신규).
 *
 * <p>착수계/기성/준공 일괄 작성 분리(refactor-document-controller-split-phase2). 권한은 실제
 * DocumentAccessSupport(SecurityContextHolder principal) 주입. 성공경로는 createDocument +
 * saveSection("letter", ...) ArgumentCaptor 로 공문 데이터(commonData/title/body) 검증.
 */
class DocumentBatchControllerTest {

    private DocumentBatchController controller;
    private DocumentService documentService;
    private SwProjectRepository swProjectRepository;
    private UserRepository userRepository;
    private LogService logService;

    @BeforeEach
    void setUp() throws Exception {
        controller = new DocumentBatchController();
        documentService = mock(DocumentService.class);
        swProjectRepository = mock(SwProjectRepository.class);
        userRepository = mock(UserRepository.class);
        logService = mock(LogService.class);

        inject("documentService", documentService);
        inject("swProjectRepository", swProjectRepository);
        inject("userRepository", userRepository);
        inject("logService", logService);
        inject("access", new DocumentAccessSupport());

        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() { SecurityContextHolder.clearContext(); }

    private void inject(String field, Object value) throws Exception {
        Field f = DocumentBatchController.class.getDeclaredField(field);
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

    private void loginEdit() { login("EDIT", "ROLE_USER"); }
    private void loginView() { login("VIEW", "ROLE_USER"); }

    private static org.springframework.ui.Model model() { return new org.springframework.ui.ExtendedModelMap(); }

    // ───────────────────────── 페이지/대상 조회 ─────────────────────────

    @Test
    void batchPage_nonEdit_redirectsList() {
        loginView();
        assertThat(controller.batchPage(model())).isEqualTo("redirect:/document/list");
    }

    @Test
    void batchPage_edit_rendersPage() {
        loginEdit();
        when(userRepository.findByEnabledTrue()).thenReturn(List.of());
        assertThat(controller.batchPage(model())).isEqualTo("document/doc-batch");
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

    // ───────────────────────── 일괄 생성 ─────────────────────────

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

    @Test
    @SuppressWarnings("unchecked")
    void batchGenerate_completion_success_savesLetterSection() { // [codex] 성공 경로 + letter 섹션 검증
        loginEdit();
        SwProject p = new SwProject();
        p.setProjId(7L);
        p.setProjNm("강진군 KRAS 유지보수");
        p.setOrgNm("강진군청");
        p.setSysNmEn("KRAS");
        p.setCityNm("전라남도");   // results Map.of 는 non-null 요구(실데이터는 채워짐)
        p.setDistNm("강진군");
        when(swProjectRepository.findById(7L)).thenReturn(Optional.of(p));
        Document doc = new Document();
        doc.setDocId(100);
        when(documentService.createDocument(any(), any(), any(), any(), any(), any(), any())).thenReturn(doc);

        Map<String, Object> common = Map.of("manager", "박욱진", "tel", "010-0000-0000", "date", "2026-06-27");
        Map<String, Object> req = Map.of("docType", "COMPLETION", "projIds", List.of(7), "commonData", common);

        ResponseEntity<Map<String, Object>> res = controller.batchGenerate(req);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> body = res.getBody();
        assertThat(body).containsEntry("successCount", 1)
                .containsEntry("failCount", 0)
                .containsEntry("totalCount", 1);
        List<Map<String, Object>> results = (List<Map<String, Object>>) body.get("results");
        assertThat(results).hasSize(1);
        assertThat(results.get(0)).containsEntry("success", true).containsEntry("docId", 100);

        // letter 섹션 ArgumentCaptor — commonData 반영 + title/body 존재
        ArgumentCaptor<Map<String, Object>> letterCap = ArgumentCaptor.forClass(Map.class);
        verify(documentService).saveSection(eq(100), eq("letter"), letterCap.capture(), eq(0));
        Map<String, Object> letter = letterCap.getValue();
        assertThat(letter).containsEntry("to", "강진군청")
                .containsEntry("manager", "박욱진")
                .containsEntry("tel", "010-0000-0000")
                .containsEntry("date", "2026-06-27");
        assertThat(letter.get("title").toString()).contains("준공계 제출 건");
        assertThat(letter).containsKey("body");
        // 준공계 본문 섹션(completion)도 저장
        verify(documentService).saveSection(eq(100), eq("completion"), any(), eq(1));
    }
}
