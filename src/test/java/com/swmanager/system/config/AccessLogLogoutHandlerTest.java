package com.swmanager.system.config;

import com.swmanager.system.constant.enums.AccessActionType;
import com.swmanager.system.constants.MenuName;
import com.swmanager.system.domain.User;
import com.swmanager.system.security.CustomUserDetails;
import com.swmanager.system.service.LogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.mockito.Mockito.*;

/**
 * 로그아웃 핸들러 — SecurityContext 가 비워진 상태(clearContext)에서도
 * authentication 파라미터만으로 정확한 userid 의 LOGOUT 로그를 적재하는지 검증.
 */
class AccessLogLogoutHandlerTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void logout_logsLogoutWithUserid_evenWhenContextCleared() {
        LogService logService = mock(LogService.class);
        AccessLogLogoutHandler handler = new AccessLogLogoutHandler(logService);

        User user = mock(User.class);
        when(user.getUsername()).thenReturn("홍길동");
        CustomUserDetails cud = mock(CustomUserDetails.class);
        when(cud.getUsername()).thenReturn("user1");
        when(cud.getUser()).thenReturn(user);
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(cud);

        // 로그아웃 시점 가정: SecurityContext 는 이미 비워짐
        SecurityContextHolder.clearContext();

        handler.logout(mock(HttpServletRequest.class), mock(HttpServletResponse.class), auth);

        verify(logService).log(MenuName.ACCESS, AccessActionType.LOGOUT, "로그아웃",
                "user1", "홍길동");
    }

    @Test
    void logout_doesNothing_whenAuthenticationNull() {
        LogService logService = mock(LogService.class);
        AccessLogLogoutHandler handler = new AccessLogLogoutHandler(logService);

        handler.logout(mock(HttpServletRequest.class), mock(HttpServletResponse.class), null);

        verifyNoInteractions(logService);
    }
}
