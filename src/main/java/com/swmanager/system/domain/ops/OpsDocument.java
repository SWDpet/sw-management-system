package com.swmanager.system.domain.ops;

import com.swmanager.system.constant.enums.DocumentStatus;
import com.swmanager.system.constant.enums.OpsDocType;
import com.swmanager.system.domain.Infra;
import com.swmanager.system.domain.OrgUnit;
import com.swmanager.system.domain.User;
import com.swmanager.system.domain.workplan.WorkPlan;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 운영·유지보수 문서 마스터 (tb_ops_doc) — doc-split-ops 스프린트 신규.
 *
 * 5 종 doc_type: INSPECT / FAULT / SUPPORT / INSTALL / PATCH.
 * 점검내역서(INSPECT) 본 데이터는 inspect_report 계열 그대로 유지, 본 엔티티는 운영측 row 만 보유.
 *
 * 기획서: docs/product-specs/doc-split-ops.md (v3)
 */
@Entity
@Table(name = "tb_ops_doc")
@Getter @Setter
public class OpsDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "doc_id")
    private Long docId;

    @Column(name = "doc_no", nullable = false, length = 50, unique = true)
    private String docNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "doc_type", nullable = false, length = 30)
    private OpsDocType docType;

    @Column(name = "sys_type", length = 20)
    private String sysType;

    @Column(name = "region_code", length = 10)
    private String regionCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_unit_id")
    private OrgUnit orgUnit;

    @Column(name = "environment", length = 20)
    private String environment;

    @Column(name = "support_target_type", length = 20)
    private String supportTargetType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "infra_id")
    private Infra infra;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id")
    private WorkPlan workPlan;

    @Column(name = "title", nullable = false, length = 500)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private DocumentStatus status = DocumentStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_id")
    private User approver;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OpsDocumentDetail> details = new ArrayList<>();

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OpsDocumentHistory> histories = new ArrayList<>();

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OpsDocumentAttachment> attachments = new ArrayList<>();

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OpsDocumentSignature> signatures = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
        if (this.updatedAt == null) this.updatedAt = LocalDateTime.now();
        if (this.status == null) this.status = DocumentStatus.DRAFT;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
