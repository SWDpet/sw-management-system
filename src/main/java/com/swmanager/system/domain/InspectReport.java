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

    /**
     * inspection-qr-batch sprint: 자동수집 출처 + 멱등 응답용 batch_id.
     *  - source: "manual" (UI 수동 작성) | "auto-qr" (QR batch 자동생성)
     *  - batchId: inspect_qr_batch.payload_id 와 동일 (멱등 조회 키)
     */
    @Column(name = "source", length = 20)
    private String source;

    @Column(name = "batch_id", length = 64)
    private String batchId;

    @Column(name = "insp_sign", columnDefinition = "TEXT")
    private String inspSign;

    @Column(name = "conf_sign", columnDefinition = "TEXT")
    private String confSign;

    /**
     * inspection-report-d-v5 (Phase C): 시안D v5 본문의 수동 입력 항목.
     */
    @Column(name = "key_findings", columnDefinition = "TEXT")
    private String keyFindings;

    @Column(name = "recommendation_1", columnDefinition = "TEXT")
    private String recommendation1;

    @Column(name = "recommendation_2", columnDefinition = "TEXT")
    private String recommendation2;

    @Column(name = "recommendation_3", columnDefinition = "TEXT")
    private String recommendation3;

    @Column(name = "followup_1", columnDefinition = "TEXT")
    private String followup1;

    @Column(name = "followup_2", columnDefinition = "TEXT")
    private String followup2;

    @Column(name = "followup_3", columnDefinition = "TEXT")
    private String followup3;

    @Column(name = "next_schedule_note", length = 300)
    private String nextScheduleNote;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

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
        if (this.source == null) {
            this.source = "manual";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
