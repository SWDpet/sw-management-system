package com.swmanager.system.constants;

/**
 * 사용자 역할 상수
 */
public class UserRole {
    
    public static final String ADMIN = "ROLE_ADMIN";
    public static final String USER = "ROLE_USER";
    
    // 관리자 여부 체크
    public static boolean isAdmin(String role) {
        return ADMIN.equals(role);
    }
    
    // 일반 사용자 여부 체크
    public static boolean isUser(String role) {
        return USER.equals(role);
    }
    
    // private 생성자 (인스턴스화 방지)
    private UserRole() {
        throw new AssertionError("상수 클래스는 인스턴스화할 수 없습니다.");
    }
}
