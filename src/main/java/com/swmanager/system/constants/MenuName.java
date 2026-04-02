package com.swmanager.system.constants;

/**
 * 메뉴 이름 상수
 */
public class MenuName {
    
    public static final String DASHBOARD = "대시보드";
    public static final String PROJECT = "사업관리";
    public static final String PERSON = "담당자관리";
    public static final String INFRA = "서버관리";
    public static final String USER = "회원관리";
    public static final String LOG = "로그관리";
    public static final String MYPAGE = "마이페이지";

    // 업무계획 및 성과 전산화 모듈
    public static final String CONTRACT = "사업현황";
    public static final String WORK_PLAN = "업무계획";
    public static final String DOCUMENT = "문서관리";
    public static final String PERFORMANCE = "성과통계";
    
    // private 생성자 (인스턴스화 방지)
    private MenuName() {
        throw new AssertionError("상수 클래스는 인스턴스화할 수 없습니다.");
    }
}
