package com.swmanager.system.domain.workplan;

import com.swmanager.system.domain.Infra;
import com.swmanager.system.domain.OrgUnit;
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

    @Column(name = "doc_no", length = 50)
    private String docNo; // 수동입력: 타부서에서 문서번호 부여

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
    @JoinColumn(name = "proj_id")
    private SwProject project;

    @Column(name = "title", nullable = false, length = 500)
    private String title;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "DRAFT"; // DRAFT(작성중), COMPLETED(작성완료)

    // [스프린트 5] 업무지원 대상 구분: EXTERNAL(지자체) / INTERNAL(자사 조직)
    @Column(name = "support_target_type", length = 20)
    private String supportTargetType;

    // [스프린트 5] 내부 업무지원 시 조직 유닛
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_unit_id")
    private OrgUnit orgUnit;

    // [스프린트 5] 설치/패치 환경 구분: PROD(운영) / TEST(테스트)
    @Column(name = "environment", length = 20)
    private String environment;

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
