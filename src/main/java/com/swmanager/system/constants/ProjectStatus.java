package com.swmanager.system.constants;

/**
 * 프로젝트 상태 상수
 */
public class ProjectStatus {
    
    public static final String IN_PROGRESS = "1";  // 진행중
    public static final String COMPLETED = "2";    // 완료
    
    // 진행중 여부 체크
    public static boolean isInProgress(String status) {
        return IN_PROGRESS.equals(status);
    }
    
    // 완료 여부 체크
    public static boolean isCompleted(String status) {
        return COMPLETED.equals(status);
    }
    
    // private 생성자 (인스턴스화 방지)
    private ProjectStatus() {
        throw new AssertionError("상수 클래스는 인스턴스화할 수 없습니다.");
    }
}
