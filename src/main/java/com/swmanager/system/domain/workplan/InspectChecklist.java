package com.swmanager.system.domain.workplan;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_inspect_checklist")
@Getter @Setter
public class InspectChecklist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "check_id")
    private Integer checkId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doc_id", nullable = false)
    private Document document;

    @Column(name = "inspect_month")
    private Integer inspectMonth;

    @Column(name = "target_sw", length = 50)
    private String targetSw; // GSS, GWS, DESKTOP_PRO, SURVEY_PROGRAM

    @Column(name = "check_item", nullable = false, length = 300)
    private String checkItem;

    @Column(name = "check_method", length = 500)
    private String checkMethod;

    @Column(name = "check_result", length = 20)
    private String checkResult = "NORMAL"; // NORMAL, INSPECT

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
        if (this.checkResult == null) this.checkResult = "NORMAL";
    }
}
