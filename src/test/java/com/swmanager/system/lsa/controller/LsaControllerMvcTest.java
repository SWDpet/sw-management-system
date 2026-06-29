package com.swmanager.system.lsa.controller;

import com.swmanager.system.domain.User;
import com.swmanager.system.exception.GlobalExceptionHandler;
import com.swmanager.system.security.CustomUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * [P1] LsaController HTTP 표면 net — standalone MockMvc (확립 패턴).
 * 인증=auth_lsa(VIEW이상 또는 ROLE_ADMIN), throw → GlobalExceptionHandler 403.
 * DB/컨텍스트 불요 → CI gates 실행.
 */
class LsaControllerMvcTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new LsaController())
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

    // ── 1. 미인증 → checkViewAuth throw → 403 ────────────────────────────────
    @Test
    void list_notAuthenticated_403() throws Exception {
        mockMvc.perform(get("/lsa/list")).andExpect(status().isForbidden());
    }

    // ── 2. authLsa NONE → 403 ────────────────────────────────────────────────
    @Test
    void list_none_403() throws Exception {
        login("NONE", "ROLE_USER");
        mockMvc.perform(get("/lsa/list")).andExpect(status().isForbidden());
    }

    // ── 3. authLsa VIEW → 200 + view lsa/lsa-list ────────────────────────────
    @Test
    void list_view_rendersList() throws Exception {
        login("VIEW", "ROLE_USER");
        mockMvc.perform(get("/lsa/list"))
                .andExpect(status().isOk())
                .andExpect(view().name("lsa/lsa-list"));
    }

    // ── 4. authLsa NONE 이지만 ROLE_ADMIN → 200 (관리자 전권) ──────────────────
    @Test
    void list_adminBypassesNone_200() throws Exception {
        login("NONE", "ROLE_ADMIN");
        mockMvc.perform(get("/lsa/list"))
                .andExpect(status().isOk())
                .andExpect(view().name("lsa/lsa-list"));
    }
}
