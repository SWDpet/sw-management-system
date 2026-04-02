package com.swmanager.system.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.io.Serializable; // [1] 필수: 직렬화 인터페이스 임포트
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
public class User implements Serializable { // [2] 필수: Serializable 구현 추가

    // [3] 필수: 직렬화 버전 ID 추가 (세션 유지에 필수적임)
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id") 
    private Long userSeq;

    @Column(name = "userid", nullable = false, unique = true)
    private String userid;

    @Column(name = "username")
    private String username;

    private String password;
    private String orgNm;
    private String deptNm;
    private String teamNm;
    private String tel;
    private String email;
    private String userRole;
    private Boolean enabled;

    private String authDashboard;
    private String authProject;
    private String authPerson;

    // 서버관리 권한 필드
    @Column(name = "auth_infra")
    private String authInfra;

    // ✨ 라이선스 대장 권한 필드 추가
    @Column(name = "auth_license")
    private String authLicense;

    // ✨ 견적서 권한 필드 추가
    @Column(name = "auth_quotation")
    private String authQuotation;

    // === 업무계획 및 성과 전산화 권한 필드 ===
    @Column(name = "auth_work_plan")
    private String authWorkPlan;

    @Column(name = "auth_document")
    private String authDocument;

    @Column(name = "auth_contract")
    private String authContract;

    @Column(name = "auth_performance")
    private String authPerformance;

    @Column(name = "position", length = 50)
    private String position;

    @Column(name = "tech_grade", length = 20)
    private String techGrade;

    // 로그인 시도 제한 필드
    @Column(name = "failed_attempts")
    private Integer failedAttempts = 0;

    @Column(name = "lock_time")
    private LocalDateTime lockTime;

    @Column(name = "reg_dt", updatable = false)
    private LocalDateTime regDt = LocalDateTime.now();

    public boolean isEnabled() { return Boolean.TRUE.equals(this.enabled); }

    @PrePersist
    public void prePersist() {
        if (this.regDt == null) this.regDt = LocalDateTime.now();
        if (this.authDashboard == null) this.authDashboard = "NONE";
        if (this.authProject == null) this.authProject = "NONE";
        if (this.authPerson == null) this.authPerson = "NONE";
        
        if (this.authInfra == null) this.authInfra = "NONE";
        
        // ✨ 라이선스 권한 기본값 설정
        if (this.authLicense == null) this.authLicense = "NONE";
        // ✨ 견적서 권한 기본값 설정
        if (this.authQuotation == null) this.authQuotation = "NONE";
        // 업무계획 및 성과 전산화 권한 기본값 설정
        if (this.authWorkPlan == null) this.authWorkPlan = "NONE";
        if (this.authDocument == null) this.authDocument = "NONE";
        if (this.authContract == null) this.authContract = "NONE";
        if (this.authPerformance == null) this.authPerformance = "NONE";
    }
}
