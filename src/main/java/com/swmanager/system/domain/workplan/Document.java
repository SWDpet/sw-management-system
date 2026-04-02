package com.swmanager.system.domain.workplan;

import com.swmanager.system.domain.Infra;
import com.swmanager.system.domain.SwProject;
import com.swmanager.system.domain.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tb_document")
@Getter @Setter
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "doc_id")
    private Integer docId;

    @Column(name = "doc_no", unique = true, length = 50)
    private String docNo; // 자동채번: (주)정도UIT {연도}-{일련번호}호

    @Column(name = "doc_type", nullable = false, length = 30)
    private String docType; // COMMENCE, INTERIM, COMPLETION, INSPECT, FAULT, SUPPORT, INSTALL, PATCH

    @Column(name = "sys_type", length = 20)
    private String sysType; // UPIS, KRAS, IPSS, GIS_SW, APIMS, ETC

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "infra_id")
    private Infra infra;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id")
    private WorkPlan workPlan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id")
    private Contract contract;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proj_id")
    private SwProject project;

    @Column(name = "title", nullable = false, length = 500)
    private String title;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "DRAFT"; // DRAFT, SUBMITTED, APPROVED, REJECTED

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

    // 관계
    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DocumentDetail> details = new ArrayList<>();

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DocumentHistory> histories = new ArrayList<>();

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DocumentAttachment> attachments = new ArrayList<>();

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DocumentSignature> signatures = new ArrayList<>();

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InspectChecklist> checklists = new ArrayList<>();

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InspectIssue> issues = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
        if (this.updatedAt == null) this.updatedAt = LocalDateTime.now();
        if (this.status == null) this.status = "DRAFT";
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
