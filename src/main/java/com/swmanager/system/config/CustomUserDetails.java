package com.swmanager.system.config;

import com.swmanager.system.domain.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails, Serializable {

    // [에러 해결 핵심] 시리얼 버전 UID 추가
    private static final long serialVersionUID = 1L;

    private final User user; // 우리 DB의 User 엔티티

    public CustomUserDetails(User user) {
        this.user = user;
    }

    // HTML(Thymeleaf)에서 ${#authentication.principal.user.authProject} 식으로 접근하기 위한 메소드
    public User getUser() {
        return user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // DB에 저장된 ROLE_USER, ROLE_ADMIN 등을 권한으로 설정
        return Collections.singleton(new SimpleGrantedAuthority(user.getUserRole()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    /**
     * ✅ 수정: 로그인 ID(userid) 반환
     * Spring Security의 username은 로그인 ID를 의미함
     */
    @Override
    public String getUsername() {
        return user.getUserid();  // ✅ 로그인 ID 반환
    }

    /**
     * ✨ 추가: 사람 이름 반환 (화면 표시용)
     */
    public String getDisplayName() {
        return user.getUsername();  // 사람 이름 (예: 박욱진)
    }

    // 계정 활성화 여부 (우리 DB의 enabled 필드와 연동)
    @Override
    public boolean isEnabled() {
        return user.isEnabled();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
}
