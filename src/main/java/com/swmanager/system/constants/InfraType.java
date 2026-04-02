package com.swmanager.system.constants;

/**
 * 인프라 타입 상수
 */
public class InfraType {
    
    public static final String PROD = "PROD";  // 운영
    public static final String TEST = "TEST";  // 테스트
    
    // 운영 환경 여부 체크
    public static boolean isProd(String infraType) {
        return PROD.equals(infraType);
    }
    
    // 테스트 환경 여부 체크
    public static boolean isTest(String infraType) {
        return TEST.equals(infraType);
    }
    
    // private 생성자 (인스턴스화 방지)
    private InfraType() {
        throw new AssertionError("상수 클래스는 인스턴스화할 수 없습니다.");
    }
}
