package com.swmanager.system.config;

import com.swmanager.system.domain.User;
import com.swmanager.system.service.LoginAttemptService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 로그인 실패 시 계정 잠금 정책에 따른 리다이렉트 분기 검증 (beyond-A 커버리지).
 *
 * 기획서 docs/product-specs/coverage-auth-failure-handler.md
 * 개발계획 docs/exec-plans/coverage-auth-failure-handler.md
 *
 * production 무변경(test-only). 상속한 DefaultRedirectStrategy 가 response.sendRedirect 호출 →
 * MockHttpServletResponse.getRedirectedUrl() 로 박제. contextPath="" 기본이라 "/login..." 그대로.
 */
class CustomAuthenticationFailureHandlerTest {

    private LoginAttemptService loginAttemptService;
    private CustomAuthenticationFailureHandler handler;
    private final AuthenticationException ex = new BadCredentialsException("bad credentials");

    @BeforeEach
    void setUp() {
        loginAttemptService = mock(LoginAttemptService.class);
        handler = new CustomAuthenticationFailureHandler(loginAttemptService);
    }

    /** 주어진 userid 파라미터(null 이면 미설정)로 핸들러 실행 후 리다이렉트 URL 반환. */
    private String invoke(String userid) throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        if (userid != null) {
            req.setParameter("userid", userid);
        }
        MockHttpServletResponse res = new MockHttpServletResponse();
        handler.onAuthenticationFailure(req, res, ex);
        return res.getRedirectedUrl();
    }

    // ===== B1~B3: 서비스 미진입 / 사용자 미존재 → 일반 오류 리다이렉트 =====

    @Test
    void useridNull_redirectsPlainError() throws Exception {
        assertThat(invoke(null)).isEqualTo("/login?error=true");
        verify(loginAttemptService, never()).findUser(any());
        verifyNoMoreInteractions(loginAttemptService);
    }

    @Test
    void useridBlank_redirectsPlainError() throws Exception {
        assertThat(invoke("   ")).isEqualTo("/login?error=true");
        verify(loginAttemptService, never()).findUser(any());
        verifyNoMoreInteractions(loginAttemptService);
    }

    @Test
    void userNotFound_redirectsPlainError() throws Exception {
        when(loginAttemptService.findUser("alice")).thenReturn(Optional.empty());

        assertThat(invoke("alice")).isEqualTo("/login?error=true");
        verify(loginAttemptService).findUser("alice");
        verify(loginAttemptService, never()).isLocked(any());
        verify(loginAttemptService, never()).loginFailed(any());
        verifyNoMoreInteractions(loginAttemptService);
    }

    // ===== B4~B6: 사용자 존재 → 잠금/실패 분기 =====

    @Test
    void alreadyLocked_redirectsLockedWithMinutes() throws Exception {
        User user = mock(User.class);
        when(loginAttemptService.findUser("alice")).thenReturn(Optional.of(user));
        when(loginAttemptService.isLocked(user)).thenReturn(true);
        when(loginAttemptService.getRemainingLockMinutes(user)).thenReturn(15L);

        assertThat(invoke("alice")).isEqualTo("/login?error=true&locked=true&minutes=15");
        verify(loginAttemptService).findUser("alice");
        verify(loginAttemptService).isLocked(user);
        verify(loginAttemptService).getRemainingLockMinutes(user);
        // 이미 잠긴 계정은 실패 카운트 증가 없이 조기 리다이렉트
        verify(loginAttemptService, never()).loginFailed(any());
        verifyNoMoreInteractions(loginAttemptService);
    }

    @Test
    void failureCausesLock_redirectsLocked() throws Exception {
        User user = mock(User.class);
        when(loginAttemptService.findUser("alice")).thenReturn(Optional.of(user));
        when(loginAttemptService.isLocked(user)).thenReturn(false);
        when(loginAttemptService.loginFailed(user)).thenReturn(true);
        when(loginAttemptService.getRemainingLockMinutes(user)).thenReturn(15L);

        assertThat(invoke("alice")).isEqualTo("/login?error=true&locked=true&minutes=15");
        // 사전잠금(isLocked) → 실패처리(loginFailed) → 신규잠금 분 계산 경로 전부 거침
        verify(loginAttemptService).findUser("alice");
        verify(loginAttemptService).isLocked(user);
        verify(loginAttemptService).loginFailed(user);
        verify(loginAttemptService).getRemainingLockMinutes(user);
        verifyNoMoreInteractions(loginAttemptService);
    }

    @Test
    void failureNoLock_redirectsPlainError() throws Exception {
        User user = mock(User.class);
        when(loginAttemptService.findUser("alice")).thenReturn(Optional.of(user));
        when(loginAttemptService.isLocked(user)).thenReturn(false);
        when(loginAttemptService.loginFailed(user)).thenReturn(false);

        assertThat(invoke("alice")).isEqualTo("/login?error=true");
        verify(loginAttemptService).findUser("alice");
        verify(loginAttemptService).isLocked(user);
        verify(loginAttemptService).loginFailed(user);
        // 잠금 미발생 → 남은 분 계산 미수행
        verify(loginAttemptService, never()).getRemainingLockMinutes(any());
        verifyNoMoreInteractions(loginAttemptService);
    }
}
