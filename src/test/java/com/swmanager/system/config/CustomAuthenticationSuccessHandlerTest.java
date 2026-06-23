package com.swmanager.system.config;

import com.swmanager.system.constant.enums.AccessActionType;
import com.swmanager.system.constants.MenuName;
import com.swmanager.system.domain.User;
import com.swmanager.system.security.CustomUserDetails;
import com.swmanager.system.service.LoginAttemptService;
import com.swmanager.system.service.LogService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;

import static org.mockito.Mockito.*;

/**
 * 로그인 성공 시 접속 로그(LOGIN) 적재 검증.
 */
class CustomAuthenticationSuccessHandlerTest {

    @Test
    void onAuthenticationSuccess_logsLogin() throws Exception {
        LoginAttemptService loginAttemptService = mock(LoginAttemptService.class);
        LogService logService = mock(LogService.class);
        CustomAuthenticationSuccessHandler handler =
                new CustomAuthenticationSuccessHandler(loginAttemptService, logService);

        User user = mock(User.class);
        when(user.getUsername()).thenReturn("홍길동");
        CustomUserDetails cud = mock(CustomUserDetails.class);
        when(cud.getUsername()).thenReturn("user1");
        when(cud.getUser()).thenReturn(user);
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(cud);

        handler.onAuthenticationSuccess(new MockHttpServletRequest(),
                new MockHttpServletResponse(), auth);

        verify(loginAttemptService).loginSucceeded(user);
        verify(logService).log(MenuName.ACCESS, AccessActionType.LOGIN, "로그인",
                "user1", "홍길동");
    }
}
