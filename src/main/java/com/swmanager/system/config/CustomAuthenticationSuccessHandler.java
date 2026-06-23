package com.swmanager.system.config;

import com.swmanager.system.constant.enums.AccessActionType;
import com.swmanager.system.constants.MenuName;
import com.swmanager.system.domain.User;
import com.swmanager.system.security.CustomUserDetails;
import com.swmanager.system.service.LoginAttemptService;
import com.swmanager.system.service.LogService;
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
    private final LogService logService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        // Authentication에서 이미 로드된 User 객체 활용 (추가 DB 조회 없음)
        if (authentication.getPrincipal() instanceof CustomUserDetails cud) {
            User user = cud.getUser();
            loginAttemptService.loginSucceeded(user);
            log.info("로그인 성공 - userid: {}", user.getUserid());
            // 접속 로그 적재 — principal 에서 직접 식별(Context 의존 X). 실패는 LogService 가 격리.
            logService.log(MenuName.ACCESS, AccessActionType.LOGIN, "로그인",
                    cud.getUsername(), user.getUsername());
        }

        setDefaultTargetUrl("/");
        setAlwaysUseDefaultTargetUrl(true);
        super.onAuthenticationSuccess(request, response, authentication);
    }
}
