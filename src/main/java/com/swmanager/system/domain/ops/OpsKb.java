package com.swmanager.system.domain.ops;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * 장애/지원 지식베이스 노드 (tb_ops_kb) — ops-fault-support M3.
 * 업무일지 백서(증상→원인→조치) 정규화 시드. 추천(KbMatcher) 소스.
 */
@Entity
@Table(name = "tb_ops_kb")
@Getter @Setter
public class OpsKb {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "kb_id")
    private Long kbId;

    @Column(name = "kb_code", length = 20)
    private String kbCode;

    private String gubun;          // 장애 / 지원

    @Column(name = "sys_type", length = 20)
    private String sysType;

    private String category;       // 분류(시스템)
    private String symptom;        // 형태(증상 표제)
    private String cause;          // 원인

    @Column(columnDefinition = "text") private String summary;
    @Column(name = "symptom_desc", columnDefinition = "text") private String symptomDesc;
    @Column(name = "cause_desc",   columnDefinition = "text") private String causeDesc;
    @Column(columnDefinition = "text") private String action;
    @Column(columnDefinition = "text") private String prevention;
    @Column(columnDefinition = "text") private String keywords;

    @Column(name = "case_count") private Integer caseCount;
    private Boolean rewritten;

    // [ops-kb-workbench] 직접등록 확장
    @Column(length = 10) private String source = "SEED";    // SEED / MANUAL
    @Column(name = "created_by", length = 50) private String createdBy;
    @Column(length = 10) private String status = "ACTIVE";  // ACTIVE / PENDING / REJECTED / DELETED

    // [ops-kb-approval] 등록 승인 워크플로
    @Column(name = "reviewed_by", length = 50) private String reviewedBy;     // 승인/반려 처리자
    @Column(name = "reviewed_at") private java.time.LocalDateTime reviewedAt; // 처리 일시
    @Column(name = "reject_reason", columnDefinition = "text") private String rejectReason;

    @Column(name = "created_at", updatable = false) private java.time.LocalDateTime createdAt;
    @Column(name = "updated_at") private java.time.LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        updatedAt = now;
        if (source == null) source = "SEED";
        if (status == null) status = "ACTIVE";
    }

    @PreUpdate
    public void preUpdate() { updatedAt = java.time.LocalDateTime.now(); }
}
