package com.swmanager.system.quotation.controller;

import com.swmanager.system.domain.User;
import com.swmanager.system.exception.InsufficientPermissionException;
import com.swmanager.system.quotation.domain.ProductPattern;
import com.swmanager.system.quotation.domain.Quotation;
import com.swmanager.system.quotation.domain.RemarksPattern;
import com.swmanager.system.quotation.domain.WageRate;
import com.swmanager.system.quotation.dto.QuotationDTO;
import com.swmanager.system.quotation.service.QuotationService;
import com.swmanager.system.repository.SwProjectRepository;
import com.swmanager.system.repository.UserRepository;
import com.swmanager.system.security.CustomUserDetails;
import com.swmanager.system.service.LogService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * QuotationController 단위 테스트 (beyond-A 커버리지 스프린트 — 컨트롤러 통합영역 2탄).
 *
 * <p>QuotationController 는 {@code @RequiredArgsConstructor}(생성자 주입)이므로
 * reflection 없이 {@code new QuotationController(mock,mock,mock,mock)} 로 생성한다(필드명 결합 제거).
 * 권한은 {@link SecurityContextHolder} principal 에서 읽으며,
 * {@code checkViewAuth()}/{@code checkEditAuth()} 는 권한 미달 시 {@link InsufficientPermissionException}
 * 를 <b>throw</b>(ResponseEntity 반환 아님)하고, 관리자 전용 엔드포인트(삭제/전체초기화)는 403 ResponseEntity 를 반환한다.
 *
 * <p>실 Postgres 불필요(운영DB 무접촉) → 기본 CI 에서 JaCoCo floor 에 반영. 쓰기경로는 가드(throw/403)와
 * 서비스 미호출(부수효과 0)을 함께 단언하고, 통과 경로는 mock 서비스로 happy-path 분기를 커버한다.
 */
class QuotationControllerTest {

    private QuotationController controller;
    private QuotationService quotationService;
    private LogService logService;
    private UserRepository userRepository;
    private SwProjectRepository swProjectRepository;

    @BeforeEach
    void setUp() {
        quotationService = mock(QuotationService.class);
        logService = mock(LogService.class);
        userRepository = mock(UserRepository.class);
        swProjectRepository = mock(SwProjectRepository.class);
        controller = new QuotationController(quotationService, logService, userRepository, swProjectRepository);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void login(String authQuotation, String role, String username) {
        User u = new User();
        u.setUserSeq(1L);
        u.setUserid("tester");
        u.setUsername(username);
        u.setUserRole(role);
        u.setAuthQuotation(authQuotation);
        CustomUserDetails cud = new CustomUserDetails(u);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(cud, "N/A", cud.getAuthorities()));
    }

    private void loginEdit()  { login("EDIT", "ROLE_USER", "작성자"); }
    private void loginView()  { login("VIEW", "ROLE_USER", "조회자"); }
    private void loginNone()  { login("NONE", "ROLE_USER", "무권한"); }
    private void loginAdmin() { login("NONE", "ROLE_ADMIN", "관리자"); }

    private static Model model() { return new ExtendedModelMap(); }

    // ───────────────────────── 견적서 작성 폼 ─────────────────────────

    @Test
    void newQuotation_anonymous_throws() {
        assertThatThrownBy(() -> controller.newQuotation(model()))
                .isInstanceOf(InsufficientPermissionException.class);
        verifyNoInteractions(quotationService);
    }

    @Test
    void newQuotation_viewOnly_throws() {
        loginView();
        assertThatThrownBy(() -> controller.newQuotation(model()))
                .isInstanceOf(InsufficientPermissionException.class);
    }

    @Test
    void newQuotation_edit_rendersForm() {
        loginEdit();
        when(quotationService.getPatterns(isNull())).thenReturn(List.of());
        String view = controller.newQuotation(model());
        assertThat(view).isEqualTo("quotation/quotation-form");
    }

    @Test
    void newQuotation_admin_loadsUserList() {
        loginAdmin();
        when(quotationService.getPatterns(isNull())).thenReturn(List.of());
        when(userRepository.findByEnabledTrue()).thenReturn(List.of());
        Model m = model();
        controller.newQuotation(m);
        assertThat(m.getAttribute("isAdmin")).isEqualTo(true);
        verify(userRepository).findByEnabledTrue();
    }

    // ───────────────────────── 목록 ─────────────────────────

    @Test
    void listQuotations_none_throws() {
        loginNone();
        assertThatThrownBy(() -> controller.listQuotations(null, null, null, null, model()))
                .isInstanceOf(InsufficientPermissionException.class);
    }

    @Test
    void listQuotations_view_rendersWithKeywordFilter() {
        loginView();
        QuotationDTO match = QuotationDTO.builder().quoteNumber("Q-2026-001").projectName("강진군 사업").build();
        QuotationDTO noMatch = QuotationDTO.builder().quoteNumber("Q-2026-002").projectName("기타").build();
        when(quotationService.getQuotations(any(), any(), any())).thenReturn(List.of(match, noMatch));
        when(quotationService.getStats()).thenReturn(Map.of());
        Model m = model();
        String view = controller.listQuotations(null, 2026, null, "강진", m);
        assertThat(view).isEqualTo("quotation/quotation-list");
        @SuppressWarnings("unchecked")
        List<QuotationDTO> filtered = (List<QuotationDTO>) m.getAttribute("quotations");
        assertThat(filtered).extracting(QuotationDTO::getQuoteNumber).containsExactly("Q-2026-001");
        assertThat(m.getAttribute("canEdit")).isEqualTo(false);
    }

    @Test
    void listQuotations_defaultsYearWhenNull() {
        loginView();
        when(quotationService.getQuotations(any(), any(), any())).thenReturn(List.of());
        when(quotationService.getStats()).thenReturn(Map.of());
        Model m = model();
        controller.listQuotations(null, null, null, null, m);
        // year=null → 컨트롤러가 올해(LocalDate.now().getYear())로 디폴트.
        assertThat(m.getAttribute("selectedYear")).isEqualTo(java.time.LocalDate.now().getYear());
    }

    // ───────────────────────── 상세/미리보기 ─────────────────────────

    @Test
    void viewQuotation_view_rendersDetail() {
        loginView();
        QuotationDTO dto = QuotationDTO.builder().createdBy("작성자").build();
        when(quotationService.getQuotation(5L)).thenReturn(dto);
        Model m = model();
        String view = controller.viewQuotation(5L, m);
        assertThat(view).isEqualTo("quotation/quotation-detail");
        assertThat(m.getAttribute("isAuthor")).isEqualTo(false); // 조회자 != 작성자
    }

    @Test
    void preview_vatExcluded_basicTemplate() {
        loginView();
        QuotationDTO dto = QuotationDTO.builder()
                .totalAmount(1_000_000L).rounddownUnit(1).vatIncluded(false)
                .templateType(1).build();
        when(quotationService.getQuotation(5L)).thenReturn(dto);
        Model m = model();
        String view = controller.previewQuotation(5L, null, m);
        assertThat(view).isEqualTo("quotation/quotation-preview");
        assertThat(m.getAttribute("vatAmount")).isEqualTo(100_000L); // 10%
        assertThat(m.getAttribute("grandTotal")).isEqualTo(1_100_000L);
    }

    @Test
    void preview_vatIncluded_laborTemplate_withBidRate() {
        loginView();
        QuotationDTO dto = QuotationDTO.builder()
                .totalAmount(1_000_000L).rounddownUnit(1).vatIncluded(true)
                .bidRate(90.0).showSeal(true).templateType(2).build();
        when(quotationService.getQuotation(5L)).thenReturn(dto);
        Model m = model();
        String view = controller.previewQuotation(5L, true, m);
        assertThat(view).isEqualTo("quotation/quotation-preview2");
        // VAT포함 분기: 공급가/부가세는 0, 품목합계가 곧 합계 → 90% 낙찰 적용.
        assertThat(m.getAttribute("vatAmount")).isEqualTo(0L);
        assertThat(m.getAttribute("supplyAmount")).isEqualTo(0L);
        assertThat(m.getAttribute("grandTotal")).isEqualTo(900_000L); // 90% 낙찰
    }

    // ───────────────────────── 수정 폼 (작성자 전용) ─────────────────────────

    @Test
    void editQuotation_nonAuthorNonAdmin_throws() {
        loginEdit(); // username=작성자
        when(quotationService.getQuotation(5L))
                .thenReturn(QuotationDTO.builder().createdBy("다른사람").build());
        assertThatThrownBy(() -> controller.editQuotation(5L, model()))
                .isInstanceOf(InsufficientPermissionException.class);
    }

    @Test
    void editQuotation_author_rendersForm() {
        loginEdit(); // username=작성자
        when(quotationService.getQuotation(5L))
                .thenReturn(QuotationDTO.builder().createdBy("작성자").build());
        when(quotationService.getPatterns(isNull())).thenReturn(List.of());
        String view = controller.editQuotation(5L, model());
        assertThat(view).isEqualTo("quotation/quotation-form");
    }

    @Test
    void editQuotation_admin_rendersForm() {
        loginAdmin();
        when(quotationService.getQuotation(5L))
                .thenReturn(QuotationDTO.builder().createdBy("아무개").build());
        when(quotationService.getPatterns(isNull())).thenReturn(List.of());
        when(userRepository.findByEnabledTrue()).thenReturn(List.of());
        String view = controller.editQuotation(5L, model());
        assertThat(view).isEqualTo("quotation/quotation-form");
    }

    // ───────────────────────── 단순 GET 페이지 ─────────────────────────

    @Test
    void ledger_view_renders() {
        loginView();
        when(quotationService.getLedger(any(), any())).thenReturn(List.of());
        assertThat(controller.ledger(2026, null, model())).isEqualTo("quotation/quotation-ledger");
    }

    @Test
    void patterns_view_renders() {
        loginView();
        when(quotationService.getPatterns(any())).thenReturn(List.of());
        assertThat(controller.patterns(null, model())).isEqualTo("quotation/pattern-list");
    }

    @Test
    void remarksPatterns_view_renders() {
        loginView();
        assertThat(controller.remarksPatterns(model())).isEqualTo("quotation/remarks-pattern-list");
    }

    @Test
    void wageRates_view_renders() {
        loginView();
        when(quotationService.getWageRates(any())).thenReturn(List.of());
        when(quotationService.getWageRateYears()).thenReturn(List.of());
        assertThat(controller.wageRates(2026, model())).isEqualTo("quotation/wage-rate-list");
    }

    // ───────────────────────── REST: 견적 CRUD ─────────────────────────

    @Test
    void recalculate_viewOnly_throws() {
        loginView();
        assertThatThrownBy(controller::recalculateAmounts)
                .isInstanceOf(InsufficientPermissionException.class);
        verify(quotationService, never()).recalculateAllGrandTotals();
    }

    @Test
    void recalculate_edit_ok() {
        loginEdit();
        when(quotationService.recalculateAllGrandTotals()).thenReturn(3);
        ResponseEntity<Map<String, Object>> res = controller.recalculateAmounts();
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).containsEntry("count", 3);
    }

    @Test
    void searchQuotations_view_filtersByKeyword() {
        loginView();
        QuotationDTO a = QuotationDTO.builder().projectName("강진군 GIS").quoteNumber("Q1").build();
        QuotationDTO b = QuotationDTO.builder().projectName("기타").quoteNumber("Q2").build();
        when(quotationService.getQuotations(any(), any(), isNull())).thenReturn(List.of(a, b));
        var rows = controller.searchQuotations("강진", null, 2026);
        assertThat(rows).hasSize(1);
    }

    @Test
    void getQuotationJson_view_returnsDto() {
        loginView();
        QuotationDTO dto = QuotationDTO.builder().quoteNumber("Q-1").build();
        when(quotationService.getQuotation(5L)).thenReturn(dto);
        assertThat(controller.getQuotationJson(5L)).isSameAs(dto);
    }

    @Test
    void nextNumber_view_returnsPreview() {
        loginView();
        when(quotationService.previewNextNumber("GIS", 2026)).thenReturn("Q-2026-010");
        assertThat(controller.nextNumber("GIS", 2026)).containsEntry("nextNumber", "Q-2026-010");
    }

    @Test
    void createQuotation_viewOnly_throws() {
        loginView();
        assertThatThrownBy(() -> controller.createQuotation(new QuotationDTO()))
                .isInstanceOf(InsufficientPermissionException.class);
        verify(quotationService, never()).createQuotation(any());
    }

    @Test
    void createQuotation_edit_ok_setsCreatedBy() {
        loginEdit();
        Quotation saved = new Quotation();
        saved.setQuoteId(7L);
        saved.setQuoteNumber("Q-2026-007");
        saved.setProjectName("사업");
        when(quotationService.createQuotation(any())).thenReturn(saved);
        ResponseEntity<Map<String, Object>> res = controller.createQuotation(new QuotationDTO());
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).containsEntry("id", 7L);
    }

    @Test
    void createQuotation_serviceThrows_badRequest() {
        loginEdit();
        when(quotationService.createQuotation(any())).thenThrow(new RuntimeException("boom"));
        ResponseEntity<Map<String, Object>> res = controller.createQuotation(new QuotationDTO());
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(res.getBody()).containsEntry("success", false);
    }

    @Test
    void updateQuotation_nonAuthorNonAdmin_forbidden() {
        loginEdit(); // 작성자
        when(quotationService.getQuotation(5L))
                .thenReturn(QuotationDTO.builder().createdBy("다른사람").build());
        ResponseEntity<Map<String, Object>> res = controller.updateQuotation(5L, new QuotationDTO());
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(quotationService, never()).updateQuotation(any());
    }

    @Test
    void updateQuotation_admin_ok() {
        loginAdmin();
        ResponseEntity<Map<String, Object>> res = controller.updateQuotation(5L, new QuotationDTO());
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(quotationService).updateQuotation(any());
    }

    @Test
    void deleteQuotation_nonAdmin_forbidden() {
        loginEdit();
        ResponseEntity<Map<String, Object>> res = controller.deleteQuotation(5L);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(quotationService, never()).deleteQuotation(anyLong());
    }

    @Test
    void deleteQuotation_admin_ok() {
        loginAdmin();
        ResponseEntity<Map<String, Object>> res = controller.deleteQuotation(5L);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(quotationService).deleteQuotation(5L);
    }

    // ───────────────────────── REST: 품명 패턴 ─────────────────────────

    @Test
    void getPatterns_view_ok() {
        loginView();
        when(quotationService.getPatterns("GIS")).thenReturn(List.of());
        assertThat(controller.getPatterns("GIS")).isEmpty();
    }

    @Test
    void savePattern_edit_ok() {
        loginEdit();
        ProductPattern saved = new ProductPattern();
        saved.setPatternId(11L);
        when(quotationService.savePattern(any())).thenReturn(saved);
        ResponseEntity<Map<String, Object>> res = controller.savePattern(new ProductPattern());
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).containsEntry("patternId", 11L);
    }

    @Test
    void savePattern_serviceThrows_badRequest() {
        loginEdit();
        when(quotationService.savePattern(any())).thenThrow(new RuntimeException("x"));
        ResponseEntity<Map<String, Object>> res = controller.savePattern(new ProductPattern());
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void updatePattern_edit_ok_propagatesId() {
        loginEdit();
        when(quotationService.savePattern(any())).thenReturn(new ProductPattern());
        ResponseEntity<?> res = controller.updatePattern(11L, new ProductPattern());
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        ArgumentCaptor<ProductPattern> cap = ArgumentCaptor.forClass(ProductPattern.class);
        verify(quotationService).savePattern(cap.capture());
        assertThat(cap.getValue().getPatternId()).isEqualTo(11L); // path id 가 본문에 주입됨
    }

    @Test
    void deletePattern_edit_ok() {
        loginEdit();
        ResponseEntity<?> res = controller.deletePattern(11L);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(quotationService).deletePattern(11L);
    }

    @Test
    void deleteAllPatterns_nonAdmin_forbidden() {
        loginEdit();
        ResponseEntity<Map<String, Object>> res = controller.deleteAllPatterns();
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(quotationService, never()).deleteAllPatterns();
    }

    @Test
    void deleteAllPatterns_admin_ok() {
        loginAdmin();
        when(quotationService.deleteAllPatterns()).thenReturn(5L);
        ResponseEntity<Map<String, Object>> res = controller.deleteAllPatterns();
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).containsEntry("deleted", 5L);
    }

    @Test
    void copyPatterns_emptyIds_badRequest() {
        loginEdit();
        ResponseEntity<Map<String, Object>> res =
                controller.copyPatterns(Map.of("targetCategory", "GIS"));
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(quotationService, never()).copyPatterns(any(), any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void copyPatterns_edit_ok_convertsIdsToLong() {
        loginEdit();
        when(quotationService.copyPatterns(any(), any())).thenReturn(2);
        // JSON 정수(Integer)로 들어온 patternIds 가 Long 리스트로 변환되어 서비스에 전달됨을 단언.
        ResponseEntity<Map<String, Object>> res = controller.copyPatterns(
                Map.of("patternIds", List.of(1, 2), "targetCategory", "GIS"));
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).containsEntry("copied", 2);
        ArgumentCaptor<List<Long>> cap = ArgumentCaptor.forClass(List.class);
        verify(quotationService).copyPatterns(cap.capture(), org.mockito.ArgumentMatchers.eq("GIS"));
        assertThat(cap.getValue()).containsExactly(1L, 2L);
    }

    // ───────────────────────── REST: SW 프로젝트 조회 ─────────────────────────

    @Test
    void getSwSystemNames_view_ok() {
        loginView();
        when(swProjectRepository.findAll()).thenReturn(List.of());
        assertThat(controller.getSwSystemNames()).isEmpty();
    }

    @Test
    void getSwClients_view_ok() {
        loginView();
        when(swProjectRepository.findAll()).thenReturn(List.of());
        assertThat(controller.getSwClients("GIS")).isEmpty();
    }

    // ───────────────────────── REST: 비고 패턴 ─────────────────────────

    @Test
    void getRemarksPatterns_view_ok() {
        loginView();
        when(quotationService.getRemarksPatterns()).thenReturn(List.of());
        assertThat(controller.getRemarksPatterns()).isEmpty();
    }

    @Test
    void saveRemarksPattern_edit_ok() {
        loginEdit();
        RemarksPattern saved = new RemarksPattern();
        saved.setPatternId(21L);
        when(quotationService.saveRemarksPattern(any())).thenReturn(saved);
        ResponseEntity<Map<String, Object>> res = controller.saveRemarksPattern(new RemarksPattern());
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).containsEntry("patternId", 21L);
    }

    @Test
    void updateRemarksPattern_edit_ok_propagatesId() {
        loginEdit();
        when(quotationService.saveRemarksPattern(any())).thenReturn(new RemarksPattern());
        ResponseEntity<?> res = controller.updateRemarksPattern(21L, new RemarksPattern());
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        ArgumentCaptor<RemarksPattern> cap = ArgumentCaptor.forClass(RemarksPattern.class);
        verify(quotationService).saveRemarksPattern(cap.capture());
        assertThat(cap.getValue().getPatternId()).isEqualTo(21L);
    }

    @Test
    void deleteRemarksPattern_edit_ok() {
        loginEdit();
        ResponseEntity<?> res = controller.deleteRemarksPattern(21L);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(quotationService).deleteRemarksPattern(21L);
    }

    @Test
    void deleteRemarksPattern_viewOnly_throws() {
        loginView();
        assertThatThrownBy(() -> controller.deleteRemarksPattern(21L))
                .isInstanceOf(InsufficientPermissionException.class);
        verify(quotationService, never()).deleteRemarksPattern(anyLong());
    }

    // ───────────────────────── REST: 노임단가 ─────────────────────────

    @Test
    void getWageRatesApi_view_ok() {
        loginView();
        when(quotationService.getWageRates(2026)).thenReturn(List.of());
        assertThat(controller.getWageRates(2026)).isEmpty();
    }

    @Test
    void getWageRateYears_view_ok() {
        loginView();
        when(quotationService.getWageRateYears()).thenReturn(List.of(2025, 2026));
        assertThat(controller.getWageRateYears()).containsExactly(2025, 2026);
    }

    @Test
    void saveWageRate_edit_ok() {
        loginEdit();
        WageRate saved = new WageRate();
        saved.setWageId(31L);
        when(quotationService.saveWageRate(any())).thenReturn(saved);
        ResponseEntity<Map<String, Object>> res = controller.saveWageRate(new WageRate());
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).containsEntry("wageId", 31L);
    }

    @Test
    void updateWageRate_edit_ok_propagatesId() {
        loginEdit();
        when(quotationService.saveWageRate(any())).thenReturn(new WageRate());
        ResponseEntity<Map<String, Object>> res = controller.updateWageRate(31L, new WageRate());
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        ArgumentCaptor<WageRate> cap = ArgumentCaptor.forClass(WageRate.class);
        verify(quotationService).saveWageRate(cap.capture());
        assertThat(cap.getValue().getWageId()).isEqualTo(31L);
    }

    @Test
    void deleteWageRate_edit_ok() {
        loginEdit();
        ResponseEntity<Map<String, Object>> res = controller.deleteWageRate(31L);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(quotationService).deleteWageRate(31L);
    }

    // ───────────────────────── REST: 대장/통계 ─────────────────────────

    @Test
    void getLedgerApi_view_ok() {
        loginView();
        when(quotationService.getLedger(any(), any())).thenReturn(List.of());
        assertThat(controller.getLedgerApi(null, null)).isEmpty();
    }

    @Test
    void getStats_view_ok() {
        loginView();
        when(quotationService.getStats()).thenReturn(Map.of("total", 1));
        assertThat(controller.getStats()).containsEntry("total", 1);
    }

    @Test
    void getStats_anonymous_throws() {
        assertThatThrownBy(controller::getStats)
                .isInstanceOf(InsufficientPermissionException.class);
        verifyNoInteractions(quotationService);
    }
}
