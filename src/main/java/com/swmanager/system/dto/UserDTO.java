package com.swmanager.system.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 사용자 정보 DTO
 * 비밀번호는 절대 포함하지 않음
 */
@Data
@Builder
@NoArgsConstructor  // ← 기본 생성자 추가!
@AllArgsConstructor // ← 모든 필드 생성자 추가!
public class UserDTO {
    
    private Long userSeq;
    private String userid;
    private String username;
    
    // 비밀번호는 DTO에서 제외 (보안)
    
    private String orgNm;
    private String deptNm;
    private String teamNm;
    private String tel;
    private String email;
    private String userRole;
    private Boolean enabled;
    
    // 권한 정보
    private String authDashboard;
    private String authProject;
    private String authPerson;
    private String authInfra;
    
    private LocalDateTime regDt;
    
    // 권한 레벨 표시명 (화면 표시용)
    private String authDashboardNm;
    private String authProjectNm;
    private String authPersonNm;
    private String authInfraNm;
    
    /**
     * Entity에서 DTO로 변환
     * 비밀번호는 제외됨
     */
    public static UserDTO fromEntity(com.swmanager.system.domain.User entity) {
        if (entity == null) {
            return null;
        }
        
        return UserDTO.builder()
                .userSeq(entity.getUserSeq())
                .userid(entity.getUserid())
                .username(entity.getUsername())
                // password는 의도적으로 제외
                .orgNm(entity.getOrgNm())
                .deptNm(entity.getDeptNm())
                .teamNm(entity.getTeamNm())
                .tel(entity.getTel())
                .email(entity.getEmail())
                .userRole(entity.getUserRole())
                .enabled(entity.getEnabled())
                .authDashboard(entity.getAuthDashboard())
                .authProject(entity.getAuthProject())
                .authPerson(entity.getAuthPerson())
                .authInfra(entity.getAuthInfra())
                .regDt(entity.getRegDt())
                // 권한명 설정
                .authDashboardNm(getAuthName(entity.getAuthDashboard()))
                .authProjectNm(getAuthName(entity.getAuthProject()))
                .authPersonNm(getAuthName(entity.getAuthPerson()))
                .authInfraNm(getAuthName(entity.getAuthInfra()))
                .build();
    }
    
    /**
     * 권한 코드를 한글명으로 변환
     */
    private static String getAuthName(String authCode) {
        if (authCode == null) return "없음";
        
        switch (authCode) {
            case "NONE": return "접근불가";
            case "VIEW": return "조회";
            case "EDIT": return "편집";
            default: return authCode;
        }
    }
    
    /**
     * 역할 한글명 반환
     */
    public String getUserRoleNm() {
        if ("ROLE_ADMIN".equals(userRole)) {
            return "관리자";
        } else if ("ROLE_USER".equals(userRole)) {
            return "일반사용자";
        }
        return userRole;
    }
    
    /**
     * 활성화 상태 한글명 반환
     */
    public String getEnabledNm() {
        return Boolean.TRUE.equals(enabled) ? "활성" : "대기중";
    }
}
