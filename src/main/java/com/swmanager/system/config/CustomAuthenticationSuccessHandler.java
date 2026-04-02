package com.swmanager.system.config;

import com.swmanager.system.domain.User;
import com.swmanager.system.service.LoginAttemptService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final LoginAttemptService loginAttemptService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        // Authentication에서 이미 로드된 User 객체 활용 (추가 DB 조회 없음)
        if (authentication.getPrincipal() instanceof CustomUserDetails cud) {
            User user = cud.getUser();
            loginAttemptService.loginSucceeded(user);
            log.info("로그인 성공 - userid: {}", user.getUserid());
        }

        setDefaultTargetUrl("/");
        setAlwaysUseDefaultTargetUrl(true);
        super.onAuthenticationSuccess(request, response, authentication);
    }
}
