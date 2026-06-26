package com.swmanager.system.controller;

import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * LoginController 단위 테스트 (beyond-A — coverage-misc-controllers).
 * 의존성 없음. loginPage 의 error/locked/logout/expired 분기 model 메시지 검증.
 */
class LoginControllerTest {

    private final LoginController controller = new LoginController();

    @Test
    void locked_withMinutes_showsLockMessage() { // T-L1
        Model m = new ExtendedModelMap();
        assertThat(controller.loginPage("1", null, "true", "10", null, m)).isEqualTo("login");
        assertThat((String) m.getAttribute("loginError")).contains("10").contains("잠겼");
    }

    @Test
    void locked_nullMinutes_defaults15() { // T-L2
        Model m = new ExtendedModelMap();
        controller.loginPage("1", null, "true", null, null, m);
        assertThat((String) m.getAttribute("loginError")).contains("15분");
    }

    @Test
    void error_notLocked_showsCredentialMessage() { // T-L3
        Model m = new ExtendedModelMap();
        controller.loginPage("1", null, null, null, null, m);
        assertThat((String) m.getAttribute("loginError")).contains("아이디 또는 비밀번호");
    }

    @Test
    void logout_showsLogoutMsg() { // T-L4
        Model m = new ExtendedModelMap();
        controller.loginPage(null, "1", null, null, null, m);
        assertThat(m.getAttribute("logoutMsg")).isEqualTo("로그아웃 되었습니다.");
    }

    @Test
    void expired_showsSessionExpired() { // T-L5
        Model m = new ExtendedModelMap();
        controller.loginPage(null, null, null, null, "true", m);
        assertThat((String) m.getAttribute("loginError")).contains("세션이 만료");
    }

    @Test
    void noParams_rendersPlainLogin() { // T-L6
        Model m = new ExtendedModelMap();
        assertThat(controller.loginPage(null, null, null, null, null, m)).isEqualTo("login");
        assertThat(m.getAttribute("loginError")).isNull();
        assertThat(m.getAttribute("logoutMsg")).isNull();
    }
}
