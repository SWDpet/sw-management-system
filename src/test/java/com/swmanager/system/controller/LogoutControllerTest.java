package com.swmanager.system.controller;

import com.swmanager.system.domain.User;
import com.swmanager.system.security.CustomUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * LogoutController 단위 테스트 (beyond-A — coverage-misc-controllers).
 * 의존성 없음. GET 로그아웃: auth 존재(핸들러 호출 후 컨텍스트 클리어) / null 모두 redirect.
 */
class LogoutControllerTest {

    private final LogoutController controller = new LogoutController();

    @AfterEach
    void tearDown() { SecurityContextHolder.clearContext(); }

    @Test
    void logout_authPresent_clearsAndRedirects() { // T-G1
        User u = new User();
        u.setUserid("tester");
        u.setUserRole("ROLE_USER");
        CustomUserDetails cud = new CustomUserDetails(u);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(cud, "N/A", cud.getAuthorities()));

        String view = controller.logout(new MockHttpServletRequest(), new MockHttpServletResponse());
        assertThat(view).isEqualTo("redirect:/login?logout=true");
        // SecurityContextLogoutHandler 가 컨텍스트를 클리어
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void logout_authNull_redirects() { // T-G2
        SecurityContextHolder.clearContext();
        String view = controller.logout(new MockHttpServletRequest(), new MockHttpServletResponse());
        assertThat(view).isEqualTo("redirect:/login?logout=true");
    }
}
