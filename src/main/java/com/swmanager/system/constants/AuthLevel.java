package com.swmanager.system.constants;

/**
 * 권한 레벨 상수
 * 매직 문자열을 상수로 관리하여 오타 방지 및 유지보수성 향상
 */
public class AuthLevel {
    
    // 권한 레벨
    public static final String NONE = "NONE";
    public static final String VIEW = "VIEW";
    public static final String EDIT = "EDIT";
    
    // 권한 레벨 검증
    public static boolean isValid(String authLevel) {
        return NONE.equals(authLevel) || 
               VIEW.equals(authLevel) || 
               EDIT.equals(authLevel);
    }
    
    // 편집 권한 체크
    public static boolean canEdit(String authLevel) {
        return EDIT.equals(authLevel);
    }
    
    // 조회 권한 체크 (VIEW 또는 EDIT)
    public static boolean canView(String authLevel) {
        return VIEW.equals(authLevel) || EDIT.equals(authLevel);
    }
    
    // 접근 불가 체크
    public static boolean isNone(String authLevel) {
        return NONE.equals(authLevel) || authLevel == null;
    }
    
    // private 생성자 (인스턴스화 방지)
    private AuthLevel() {
        throw new AssertionError("상수 클래스는 인스턴스화할 수 없습니다.");
    }
}
