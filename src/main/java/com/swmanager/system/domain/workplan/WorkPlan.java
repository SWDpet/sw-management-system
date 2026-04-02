package com.swmanager.system.domain.workplan;

import com.swmanager.system.domain.Infra;
import com.swmanager.system.domain.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tb_work_plan")
@Getter @Setter
public class WorkPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "plan_id")
    private Integer planId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "infra_id")
    private Infra infra;

    @Column(name = "plan_type", nullable = false, length = 30)
    private String planType; // CONTRACT, INSTALL, PATCH, INSPECT, PRE_CONTACT, FAULT, SUPPORT, SETTLE, COMPLETE

    @Column(name = "process_step")
    private Integer processStep; // 1~7

    @Column(name = "title", nullable = false, length = 300)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private User assignee;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "location", length = 300)
    private String location;

    @Column(name = "repeat_type", nullable = false, length = 20)
    private String repeatType = "NONE"; // NONE, MONTHLY, QUARTERLY, HALF_YEARLY

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_plan_id")
    private WorkPlan parentPlan;

    @OneToMany(mappedBy = "parentPlan")
    private List<WorkPlan> childPlans = new ArrayList<>();

    @Column(name = "status", nullable = false, length = 20)
    private String status = "PLANNED"; // PLANNED, CONTACTED, CONFIRMED, IN_PROGRESS, COMPLETED, POSTPONED, CANCELLED

    @Column(name = "status_reason", length = 500)
    private String statusReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    // 문서 관계
    @OneToMany(mappedBy = "workPlan")
    private List<Document> documents = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
        if (this.updatedAt == null) this.updatedAt = LocalDateTime.now();
        if (this.repeatType == null) this.repeatType = "NONE";
        if (this.status == null) this.status = "PLANNED";
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
