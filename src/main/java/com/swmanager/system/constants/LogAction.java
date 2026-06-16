package com.swmanager.system.constants;

/**
 * 로그 액션 타입 상수.
 *
 * @deprecated 단일 SSoT 인 {@link com.swmanager.system.constant.enums.AccessActionType}
 * (한글 label 보유)로 대체. 신규 코드는 enum 을 사용할 것. 미사용 잔존 상수이며
 * 호환을 위해 보존(2026-06-16 log-management-improvement).
 */
@Deprecated
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
