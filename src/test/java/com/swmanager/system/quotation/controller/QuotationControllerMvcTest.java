package com.swmanager.system.quotation.controller;

import com.swmanager.system.domain.User;
import com.swmanager.system.exception.GlobalExceptionHandler;
import com.swmanager.system.quotation.dto.QuotationDTO;
import com.swmanager.system.quotation.service.QuotationService;
import com.swmanager.system.repository.SwProjectRepository;
import com.swmanager.system.repository.UserRepository;
import com.swmanager.system.security.CustomUserDetails;
import com.swmanager.system.service.LogService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * [beyond-A] QuotationController HTTP 표면 net — standalone MockMvc (SwControllerMvcTest 패턴 재사용).
 * 라우팅(/quotation/*, /api/quotation/*)·status·view명·@ResponseBody JSON·권한예외→403 박제.
 * 인증=checkViewAuth/checkEditAuth 가 InsufficientPermissionException throw → GlobalExceptionHandler 403.
 * DB/컨텍스트 불요 → CI gates(verify) 실행. PIT 미편입(컨트롤러 부적합).
 */
class QuotationControllerMvcTest {

    private MockMvc mockMvc;
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
        QuotationController controller = new QuotationController(
                quotationService, logService, userRepository, swProjectRepository);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() { SecurityContextHolder.clearContext(); }

    private void login(String authQuotation, String role) {
        User u = new User();
        u.setUserSeq(1L); u.setUserid("tester"); u.setUsername("테스터");
        u.setUserRole(role); u.setAuthQuotation(authQuotation);
        CustomUserDetails cud = new CustomUserDetails(u);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(cud, "N/A", cud.getAuthorities()));
    }
    private void loginView() { login("VIEW", "ROLE_USER"); }
    private void loginEdit() { login("EDIT", "ROLE_USER"); }

    // ── 1. 미인증 → checkViewAuth throw → 403 ────────────────────────────────
    @Test
    void list_notAuthenticated_403() throws Exception {
        mockMvc.perform(get("/quotation/list"))
                .andExpect(status().isForbidden());
        verify(quotationService, never()).getQuotations(any(), any(), any());
    }

    // ── 2. VIEW → 200 + view quotation-list + model ─────────────────────────
    @Test
    void list_view_rendersList() throws Exception {
        loginView();
        when(quotationService.getQuotations(any(), any(), any())).thenReturn(java.util.List.of());
        when(quotationService.getStats()).thenReturn(Map.of("total", 0L));
        mockMvc.perform(get("/quotation/list"))
                .andExpect(status().isOk())
                .andExpect(view().name("quotation/quotation-list"))
                .andExpect(model().attributeExists("quotations", "stats", "canEdit"));
    }

    // ── 3. /quotation/{id} VIEW → 200 + view quotation-detail ────────────────
    @Test
    void view_byId_rendersDetail() throws Exception {
        loginView();
        QuotationDTO dto = QuotationDTO.builder().quoteId(1L).createdBy("다른작성자").build();
        when(quotationService.getQuotation(1L)).thenReturn(dto);
        mockMvc.perform(get("/quotation/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("quotation/quotation-detail"))
                .andExpect(model().attributeExists("quotation", "canEdit", "isAdmin"))
                .andExpect(model().attribute("isAuthor", false));   // createdBy 다른작성자 ≠ 로그인유저 → false
    }

    // ── 4. /quotation/new EDIT → 200 + view quotation-form ───────────────────
    @Test
    void newQuotation_edit_rendersForm() throws Exception {
        loginEdit();
        when(quotationService.getPatterns(null)).thenReturn(java.util.List.of());
        mockMvc.perform(get("/quotation/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("quotation/quotation-form"))
                .andExpect(model().attributeExists("patterns", "isAdmin"));
    }

    // ── 5. /quotation/new VIEW(편집권한 부족) → checkEditAuth throw → 403 ──────
    @Test
    void newQuotation_viewOnly_403() throws Exception {
        loginView();   // EDIT 아님
        mockMvc.perform(get("/quotation/new"))
                .andExpect(status().isForbidden());
        verify(quotationService, never()).getPatterns(any());
    }

    // ── 6. /api/quotation/{id} VIEW → 200 + @ResponseBody JSON ───────────────
    @Test
    void apiGetById_view_returnsJson() throws Exception {
        loginView();
        when(quotationService.getQuotation(1L))
                .thenReturn(QuotationDTO.builder().quoteId(1L).build());
        mockMvc.perform(get("/api/quotation/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.quoteId").value(1));   // 실제 payload shape 박제
    }

    // ── 7. /api/quotation/next-number VIEW → 200 + JSON ─────────────────────
    @Test
    void apiNextNumber_view_returnsJson() throws Exception {
        loginView();
        when(quotationService.previewNextNumber(eq("용역"), anyInt())).thenReturn("UIT - SQ - 2026 - 001");
        mockMvc.perform(get("/api/quotation/next-number").param("category", "용역"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.nextNumber").value("UIT - SQ - 2026 - 001"));   // payload 박제
    }
}
