package com.swmanager.system.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * 문서(document) 영역 인증/권한 조회 공유 컴포넌트.
 *
 * <p>DocumentController·DocumentDownloadController 가 동일한 권한 판정을 공유하도록 분리
 * (refactor-document-controller-split S4 Phase 1). getAuth 의 admin→"EDIT" 매핑은
 * viewer-action-button-guard 정책의 단일 소스 — 변경 시 다운로드 가드 전체에 영향.
 */
@Component
public class DocumentAccessSupport {

    /** 현재 로그인 사용자(CustomUserDetails) 또는 null(미인증/비호환 principal). */
    public CustomUserDetails getCurrentUser() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) return null;
            Object principal = auth.getPrincipal();
            if (principal instanceof CustomUserDetails) return (CustomUserDetails) principal;
            return null;
        } catch (Exception e) { return null; }
    }

    /** ROLE_ADMIN 보유 여부. */
    public boolean isAdmin() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            return auth != null && auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        } catch (Exception e) { return false; }
    }

    /** 문서 권한 문자열 — admin→"EDIT", 미인증→"NONE", 그 외 authDocument(없으면 "NONE"). */
    public String getAuth() {
        if (isAdmin()) return "EDIT";
        CustomUserDetails cu = getCurrentUser();
        if (cu == null) return "NONE";
        String auth = cu.getUser().getAuthDocument();
        return (auth != null) ? auth : "NONE";
    }
}
