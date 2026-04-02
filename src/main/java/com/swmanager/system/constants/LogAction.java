package com.swmanager.system.constants;

/**
 * 로그 액션 타입 상수
 */
public class LogAction {
    
    public static final String VIEW = "조회";
    public static final String CREATE = "등록";
    public static final String UPDATE = "수정";
    public static final String DELETE = "삭제";
    public static final String APPROVE = "승인";
    public static final String LOGIN = "로그인";
    public static final String LOGOUT = "로그아웃";
    
    // private 생성자 (인스턴스화 방지)
    private LogAction() {
        throw new AssertionError("상수 클래스는 인스턴스화할 수 없습니다.");
    }
}
