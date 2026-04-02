package com.swmanager.system.config;

import com.swmanager.system.domain.User;
import com.swmanager.system.service.LoginAttemptService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final LoginAttemptService loginAttemptService;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        String userid = request.getParameter("userid");

        if (userid != null && !userid.isBlank()) {
            // User 조회 1회만 수행
            Optional<User> optUser = loginAttemptService.findUser(userid);

            if (optUser.isPresent()) {
                User user = optUser.get();

                // 이미 잠긴 계정인지 확인 (User 객체 사용, 추가 DB 조회 없음)
                if (loginAttemptService.isLocked(user)) {
                    long remaining = loginAttemptService.getRemainingLockMinutes(user);
                    log.warn("잠긴 계정 로그인 시도 - userid: {}, 남은시간: {}분", userid, remaining);
                    getRedirectStrategy().sendRedirect(request, response,
                            "/login?error=true&locked=true&minutes=" + remaining);
                    return;
                }

                // 실패 카운트 증가 + 잠금 여부 반환 (1회 save)
                boolean nowLocked = loginAttemptService.loginFailed(user);

                if (nowLocked) {
                    long remaining = loginAttemptService.getRemainingLockMinutes(user);
                    log.warn("로그인 실패 횟수 초과로 계정 잠금 - userid: {}", userid);
                    getRedirectStrategy().sendRedirect(request, response,
                            "/login?error=true&locked=true&minutes=" + remaining);
                    return;
                }
            }
        }

        getRedirectStrategy().sendRedirect(request, response, "/login?error=true");
    }
}
