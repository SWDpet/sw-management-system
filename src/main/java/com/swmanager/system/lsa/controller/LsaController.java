package com.swmanager.system.lsa.controller;

import com.swmanager.system.exception.InsufficientPermissionException;
import com.swmanager.system.security.CustomUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * LSA(라이선스 발급 대장) 컨트롤러.
 * License4J/GeoNURIS 와 독립된 auth_lsa 권한. 인증=QuotationController 패턴(throw → GlobalExceptionHandler 403).
 * P1: 골격(목록 placeholder). P2 검색·데이터 / P3 입력폼·ps_info / P4 상세·수정·삭제.
 */
@Slf4j
@Controller
@RequestMapping("/lsa")
public class LsaController {

    /** 현재 로그인 사용자 */
    private CustomUserDetails getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
            return (CustomUserDetails) auth.getPrincipal();
        }
        return null;
    }

    /** LSA 조회 권한 체크 (VIEW 이상 또는 관리자) */
    private void checkViewAuth() {
        CustomUserDetails cu = getCurrentUser();
        if (cu == null) throw new InsufficientPermissionException("로그인");
        String role = cu.getUser().getUserRole();
        String authLsa = cu.getUser().getAuthLsa();
        if (!"ROLE_ADMIN".equals(role) && (authLsa == null || "NONE".equals(authLsa))) {
            log.warn("LSA 접근 권한 없음 - 사용자: {}, authLsa: {}", cu.getUsername(), authLsa);
            throw new InsufficientPermissionException("LSA 조회");
        }
    }

    /** LSA 목록 (P1 placeholder — P2 에서 검색·데이터) */
    @GetMapping("/list")
    public String list(Model model) {
        checkViewAuth();
        return "lsa/lsa-list";
    }
}
