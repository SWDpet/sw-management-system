package com.swmanager.system.config;

import com.swmanager.system.constant.enums.AccessActionType;
import com.swmanager.system.constants.MenuName;
import com.swmanager.system.security.CustomUserDetails;
import com.swmanager.system.service.LogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

/**
 * 로그아웃 시 접속 로그(LOGOUT) 적재.
 *
 * 핵심: {@code LogoutFilter} 가 캡처한 {@code authentication} 파라미터를 사용하므로,
 * {@code SecurityContextLogoutHandler} 가 SecurityContext 를 비운 뒤(핸들러 실행
 * 순서와 무관) 실행되더라도 사용자 식별이 유지된다. 따라서 LogService 의
 * Context 기반 추출이 아니라 명시적 식별자 오버로드를 사용한다.
 */
@Component
@RequiredArgsConstructor
public class AccessLogLogoutHandler implements LogoutHandler {

    private final LogService logService;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response,
                       Authentication authentication) {
        if (authentication != null
                && authentication.getPrincipal() instanceof CustomUserDetails cud) {
            logService.log(MenuName.ACCESS, AccessActionType.LOGOUT, "로그아웃",
                    cud.getUsername(), cud.getUser().getUsername());
        }
    }
}
