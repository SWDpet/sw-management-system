package com.swmanager.system.lsa.controller;

import com.swmanager.system.domain.User;
import com.swmanager.system.exception.GlobalExceptionHandler;
import com.swmanager.system.lsa.dto.LsaForm;
import com.swmanager.system.lsa.service.LsaService;
import com.swmanager.system.security.CustomUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * [P2] LsaController HTTP 표면 net — standalone MockMvc (mock LsaService 주입).
 * 인증=auth_lsa(VIEW이상 또는 ROLE_ADMIN), throw → GlobalExceptionHandler 403.
 */
class LsaControllerMvcTest {

    private MockMvc mockMvc;
    private LsaService lsaService;

    @BeforeEach
    void setUp() {
        lsaService = mock(LsaService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new LsaController(lsaService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() { SecurityContextHolder.clearContext(); }

    private void login(String authLsa, String role) {
        User u = new User();
        u.setUserSeq(1L); u.setUserid("tester"); u.setUsername("테스터");
        u.setUserRole(role); u.setAuthLsa(authLsa);
        CustomUserDetails cud = new CustomUserDetails(u);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(cud, "N/A", cud.getAuthorities()));
    }

    // ── 1. 미인증 → 403 ──────────────────────────────────────────────────────
    @Test
    void list_notAuthenticated_403() throws Exception {
        mockMvc.perform(get("/lsa/list")).andExpect(status().isForbidden());
        verify(lsaService, never()).list(any());
    }

    // ── 2. authLsa NONE → 403 ────────────────────────────────────────────────
    @Test
    void list_none_403() throws Exception {
        login("NONE", "ROLE_USER");
        mockMvc.perform(get("/lsa/list")).andExpect(status().isForbidden());
    }

    // ── 3. VIEW → 200 + view + model(lsaList/keyword/canEdit=false) + keyword 바인딩 ──
    @Test
    void list_view_rendersWithModelAndKeyword() throws Exception {
        login("VIEW", "ROLE_USER");
        when(lsaService.list("강진")).thenReturn(java.util.List.of());
        mockMvc.perform(get("/lsa/list").param("keyword", "강진"))
                .andExpect(status().isOk())
                .andExpect(view().name("lsa/lsa-list"))
                .andExpect(model().attributeExists("lsaList"))
                .andExpect(model().attribute("keyword", "강진"))
                .andExpect(model().attribute("canEdit", false));   // VIEW → 편집 불가
        verify(lsaService).list("강진");
    }

    // ── 4. EDIT → canEdit=true ───────────────────────────────────────────────
    @Test
    void list_edit_canEditTrue() throws Exception {
        login("EDIT", "ROLE_USER");
        when(lsaService.list(any())).thenReturn(java.util.List.of());
        mockMvc.perform(get("/lsa/list"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("canEdit", true));
    }

    // ── 5. authLsa NONE 이지만 ROLE_ADMIN → 200 + canEdit=true (관리자 전권) ───
    @Test
    void list_adminBypassesNone_canEditTrue() throws Exception {
        login("NONE", "ROLE_ADMIN");
        when(lsaService.list(any())).thenReturn(java.util.List.of());
        mockMvc.perform(get("/lsa/list"))
                .andExpect(status().isOk())
                .andExpect(view().name("lsa/lsa-list"))
                .andExpect(model().attribute("canEdit", true));
    }

    // ── 6. /new EDIT → 200 + lsa-form + model(sidoList/isAdmin/issuer) ───────
    @Test
    void newForm_edit_rendersForm() throws Exception {
        login("EDIT", "ROLE_USER");
        when(lsaService.sidoList()).thenReturn(java.util.List.of("전라남도"));
        mockMvc.perform(get("/lsa/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("lsa/lsa-form"))
                .andExpect(model().attributeExists("sidoList"))
                .andExpect(model().attribute("isAdmin", false))
                .andExpect(model().attribute("issuer", "테스터"));   // 로그인 실명
    }

    // ── 7. /new VIEW(편집권한 부족) → checkEditAuth 403 ───────────────────────
    @Test
    void newForm_viewOnly_403() throws Exception {
        login("VIEW", "ROLE_USER");
        mockMvc.perform(get("/lsa/new")).andExpect(status().isForbidden());
        verify(lsaService, never()).sidoList();
    }

    // ── 8. POST /save EDIT → 3xx /lsa/list + 비관리자 발급자 강제(실명) ────────
    @Test
    void save_edit_nonAdmin_forcesIssuerToLoginName() throws Exception {
        login("EDIT", "ROLE_USER");
        when(lsaService.create(any(), any(), any())).thenReturn(1L);
        mockMvc.perform(post("/lsa/save")
                        .param("cityNm", "전라남도").param("distNm", "강진군")
                        .param("userNm", "홍길동").param("issuer", "위조된관리자"))   // 폼 위조 시도
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/lsa/list"));
        ArgumentCaptor<String> issuer = ArgumentCaptor.forClass(String.class);
        verify(lsaService).create(any(LsaForm.class), issuer.capture(), eq("tester"));
        // 비관리자 → 폼 issuer 무시, 로그인 실명("테스터") 강제
        org.assertj.core.api.Assertions.assertThat(issuer.getValue()).isEqualTo("테스터");
    }

    // ── 9. POST /save VIEW → checkEditAuth 403 ───────────────────────────────
    @Test
    void save_viewOnly_403() throws Exception {
        login("VIEW", "ROLE_USER");
        mockMvc.perform(post("/lsa/save").param("userNm", "x")).andExpect(status().isForbidden());
        verify(lsaService, never()).create(any(), any(), any());
    }

    // ── 10. /api/districts VIEW → JSON ───────────────────────────────────────
    @Test
    void districts_api_returnsJson() throws Exception {
        login("VIEW", "ROLE_USER");
        when(lsaService.districtList("전라남도")).thenReturn(java.util.List.of("강진군"));
        mockMvc.perform(get("/lsa/api/districts").param("sido", "전라남도"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$[0]").value("강진군"));
    }

    // ── 11. /api/persons VIEW → JSON (prefill 후보) ──────────────────────────
    @Test
    void persons_api_returnsJson() throws Exception {
        login("VIEW", "ROLE_USER");
        when(lsaService.findPersonCandidates(any(), any(), any(), any())).thenReturn(java.util.List.of());
        mockMvc.perform(get("/lsa/api/persons").param("city", "전라남도").param("dist", "강진군"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"));
    }
}
