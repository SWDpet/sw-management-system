package com.swmanager.system.constants;

/**
 * 서버 타입 상수
 */
public class ServerType {
    
    public static final String WEB = "WEB";
    public static final String DB = "DB";
    
    // WEB 서버 여부 체크
    public static boolean isWeb(String serverType) {
        return WEB.equals(serverType);
    }
    
    // DB 서버 여부 체크
    public static boolean isDb(String serverType) {
        return DB.equals(serverType);
    }
    
    // private 생성자 (인스턴스화 방지)
    private ServerType() {
        throw new AssertionError("상수 클래스는 인스턴스화할 수 없습니다.");
    }
}
