package com.swmanager.system.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @Column(name = "mobile", length = 20)
    private String mobile;

    private String email;

    @Column(name = "position_title", length = 50)
    private String positionTitle;

    @Column(name = "address", length = 300)
    private String address;

    @Column(name = "ssn", length = 14)
    private String ssn;

    @Column(name = "certificate", length = 500)
    private String certificate;

    @Column(name = "tasks", length = 1000)
    private String tasks;

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

    @Column(name = "field_role", length = 50)
    private String fieldRole;

    @Column(name = "career_years", length = 20)
    private String careerYears;

    // 로그인 시도 제한 필드
    @Column(name = "failed_attempts")
    private Integer failedAttempts = 0;

    @Column(name = "lock_time")
    private LocalDateTime lockTime;

    @Column(name = "reg_dt", updatable = false)
    private LocalDateTime regDt = LocalDateTime.now();

    public boolean isEnabled() { return Boolean.TRUE.equals(this.enabled); }

    /**
     * 주민번호 마스킹 표시 (예: 770914-1******)
     */
    public String getSsnMasked() {
        if (ssn == null || ssn.length() < 8) return ssn;
        return ssn.substring(0, 8) + "******";
    }

    /**
     * 업무 목록 (콤마 구분 -> List)
     */
    public List<String> getTaskList() {
        if (tasks == null || tasks.isBlank()) return new ArrayList<>();
        List<String> list = new ArrayList<>();
        for (String t : tasks.split(",")) {
            if (!t.trim().isEmpty()) list.add(t.trim());
        }
        return list;
    }

    /**
     * 업무 목록 설정 (List -> 콤마 구분 문자열)
     */
    public void setTaskList(List<String> taskList) {
        if (taskList == null || taskList.isEmpty()) {
            this.tasks = null;
        } else {
            this.tasks = String.join(",", taskList);
        }
    }

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
