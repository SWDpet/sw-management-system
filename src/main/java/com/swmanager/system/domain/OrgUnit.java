package com.swmanager.system.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 조직도 단위 (본부/연구소/부서/팀).
 * self-FK {@code parent_id} 로 가변 계층(1~3단) 표현.
 *
 * 스프린트 5 (2026-04-19) 신설 — docs/plans/doc-selector-org-env.md
 */
@Entity
@Table(name = "tb_org_unit")
@Getter @Setter
public class OrgUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "unit_id")
    private Long unitId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private OrgUnit parent;

    @Column(name = "unit_type", nullable = false, length = 20)
    private String unitType; // DIVISION / DEPARTMENT / TEAM

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(name = "use_yn", length = 1)
    private String useYn = "Y";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
        if (useYn == null) useYn = "Y";
        if (sortOrder == null) sortOrder = 0;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
