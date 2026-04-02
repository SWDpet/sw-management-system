package com.swmanager.system.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 로그아웃 Controller
 * GET 방식 로그아웃 지원
 */
@Controller
public class LogoutController {

    /**
     * GET 방식 로그아웃 처리
     * 
     * 사용 예:
     * - <a href="/logout">로그아웃</a>
     * - 네비게이션 링크에서 클릭
     */
    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null) {
            // Spring Security 로그아웃 핸들러 사용
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        
        // 로그아웃 성공 후 로그인 페이지로 리다이렉트
        return "redirect:/login?logout=true";
    }
}
