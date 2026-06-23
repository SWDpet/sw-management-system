package com.swmanager.system.controller;

import com.swmanager.system.security.CustomUserDetails;
import com.swmanager.system.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * 통합검증: 대시보드 렌더 smoke + 로그관리(/admin/logs) ADMIN 가드 (실 Postgres + Spring Security 필터체인).
 *
 * log-management-improvement P3/P4 잔여(회사 PC 통합검증):
 *   - 대시보드 렌더 smoke: ADMIN 으로 GET / → 200 + main-dashboard 뷰 + 통계 모델(logTrend/menuTop/actionCounts/recentProjectLogs).
 *     Thymeleaf 실제 렌더까지 수행되므로 백지렌더([[ 인라인 함정 등) 회귀도 포착.
 *   - ADMIN 가드: SecurityConfig `/admin/**` hasRole(ADMIN) — 익명 redirect / 비ADMIN 403 / ADMIN 200.
 *
 * 모두 GET(읽기전용) → 운영DB에 안전. RUN_DB_TESTS=true 환경에서만 실행.
 */
@SpringBootTest
@AutoConfigureMockMvc
@EnabledIfEnvironmentVariable(named = "RUN_DB_TESTS", matches = "true",
        disabledReason = "Live DB required; set RUN_DB_TESTS=true to run. 기본 CI에서는 스킵.")
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=none",
        "logging.level.org.hibernate.SQL=OFF"
})
class DashboardAndAdminLogsSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * 실제 {@link CustomUserDetails}(principal.user = User 엔티티) 기반 ADMIN principal.
     * 템플릿 top-nav 가 {@code #authentication.principal.user.authProject} 를 참조하므로
     * Spring 기본 User(@WithMockUser)로는 렌더 불가 → 앱 principal 로 인증한다.
     * auth* 권한필드는 null 이어도 템플릿이 {@code ?.} 안전탐색 → 사이드바 정상.
     */
    private RequestPostProcessor adminPrincipal() {
        User u = new User();
        u.setUserid("it-admin");
        u.setUsername("통합테스트관리자");
        u.setUserRole("ROLE_ADMIN");
        CustomUserDetails cud = new CustomUserDetails(u);
        return authentication(
                new UsernamePasswordAuthenticationToken(cud, "N/A", cud.getAuthorities()));
    }

    // ── 대시보드 ──────────────────────────────────────────────

    @Test
    void dashboard_anonymous_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void dashboard_admin_rendersWithStatsModel() throws Exception {
        mockMvc.perform(get("/").with(adminPrincipal()))
                .andExpect(status().isOk())
                .andExpect(view().name("main-dashboard"))
                .andExpect(model().attributeExists(
                        "recentProjectLogs", "logTrend", "menuTop", "actionCounts"));
    }

    // ── 로그관리 ADMIN 가드 ───────────────────────────────────

    @Test
    void adminLogs_anonymous_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/admin/logs"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void adminLogs_nonAdmin_forbidden() throws Exception {
        mockMvc.perform(get("/admin/logs"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminLogs_admin_rendersWithTabModel() throws Exception {
        mockMvc.perform(get("/admin/logs").with(adminPrincipal()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("logs", "tab"));
    }
}
