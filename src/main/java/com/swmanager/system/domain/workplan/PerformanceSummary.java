package com.swmanager.system.domain.workplan;

import com.swmanager.system.domain.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_performance_summary",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "period_year", "period_month"}))
@Getter @Setter
public class PerformanceSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "summary_id")
    private Integer summaryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "period_year", nullable = false)
    private Integer periodYear;

    @Column(name = "period_month", nullable = false)
    private Integer periodMonth;

    @Column(name = "install_count")
    private Integer installCount = 0;

    @Column(name = "patch_count")
    private Integer patchCount = 0;

    @Column(name = "fault_count")
    private Integer faultCount = 0;

    @Column(name = "fault_avg_hours", precision = 8, scale = 2)
    private BigDecimal faultAvgHours;

    @Column(name = "support_count")
    private Integer supportCount = 0;

    @Column(name = "inspect_plan_count")
    private Integer inspectPlanCount = 0;

    @Column(name = "inspect_done_count")
    private Integer inspectDoneCount = 0;

    @Column(name = "interim_count")
    private Integer interimCount = 0;

    @Column(name = "completion_count")
    private Integer completionCount = 0;

    @Column(name = "infra_count")
    private Integer infraCount = 0;

    @Column(name = "plan_total_count")
    private Integer planTotalCount = 0;

    @Column(name = "plan_ontime_count")
    private Integer planOntimeCount = 0;

    @Column(name = "calculated_at", nullable = false)
    private LocalDateTime calculatedAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (this.calculatedAt == null) this.calculatedAt = LocalDateTime.now();
        if (this.updatedAt == null) this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
