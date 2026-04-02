package com.swmanager.system.domain.workplan;

import com.swmanager.system.domain.Infra;
import com.swmanager.system.domain.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_inspect_cycle")
@Getter @Setter
public class InspectCycle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cycle_id")
    private Integer cycleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "infra_id", nullable = false)
    private Infra infra;

    @Column(name = "cycle_type", nullable = false, length = 20)
    private String cycleType; // MONTHLY, QUARTERLY, HALF_YEARLY

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private User assignee;

    @Column(name = "contact_name", length = 50)
    private String contactName;

    @Column(name = "contact_phone", length = 50)
    private String contactPhone;

    @Column(name = "contact_email", length = 100)
    private String contactEmail;

    @Column(name = "pre_contact_days", nullable = false)
    private Integer preContactDays = 7;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
        if (this.updatedAt == null) this.updatedAt = LocalDateTime.now();
        if (this.cycleType == null) this.cycleType = "QUARTERLY";
        if (this.preContactDays == null) this.preContactDays = 7;
        if (this.isActive == null) this.isActive = true;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
