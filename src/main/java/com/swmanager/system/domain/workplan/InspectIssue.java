package com.swmanager.system.domain.workplan;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_inspect_issue")
@Getter @Setter
public class InspectIssue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "issue_id")
    private Integer issueId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doc_id", nullable = false)
    private Document document;

    @Column(name = "issue_year")
    private Integer issueYear;

    @Column(name = "issue_month")
    private Integer issueMonth;

    @Column(name = "issue_day")
    private Integer issueDay;

    @Column(name = "task_type", length = 50)
    private String taskType; // 정기점검, 장애처리, 패치 등

    @Column(name = "symptom", columnDefinition = "TEXT")
    private String symptom;

    @Column(name = "action_taken", columnDefinition = "TEXT")
    private String actionTaken;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
    }
}
