package com.swmanager.system.domain;

import com.swmanager.system.constant.enums.DocumentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "inspect_report")
public class InspectReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pjt_id", nullable = false)
    private Long pjtId;

    @Column(name = "inspect_month", length = 7)
    private String inspectMonth;

    @Column(name = "sys_type", length = 20)
    private String sysType;

    @Column(name = "doc_title", length = 300)
    private String docTitle;

    /**
     * S1 inspect-comprehensive-redesign (A4):
     *  - 점검자: 내부 직원 (users.user_id FK)
     *  - 확인자: 고객 담당자 (ps_info.id FK)
     *  - 기존 insp_name/insp_company/conf_name/conf_org 컬럼은 V022 에서 DROP.
     */
    @Column(name = "insp_user_id")
    private Long inspUserId;

    @Column(name = "conf_ps_info_id")
    private Long confPsInfoId;

    @Column(name = "insp_dbms", length = 200)
    private String inspDbms;

    @Column(name = "insp_gis", length = 200)
    private String inspGis;

    @Column(name = "dbms_ip", length = 50)
    private String dbmsIp;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private DocumentStatus status;

    @Column(name = "insp_sign", columnDefinition = "TEXT")
    private String inspSign;

    @Column(name = "conf_sign", columnDefinition = "TEXT")
    private String confSign;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = DocumentStatus.DRAFT;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
