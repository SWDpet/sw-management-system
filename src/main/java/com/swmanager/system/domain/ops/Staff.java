package com.swmanager.system.domain.ops;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 직원 디렉터리 (tb_staff) — ops-fault-support staff.
 * 조직도 소속 인원 + 직원 요청자 소스. 로그인 계정(users)과 분리된 회사 인원 명부.
 */
@Entity
@Table(name = "tb_staff")
@Getter @Setter
public class Staff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "staff_id")
    private Long staffId;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 50)
    private String position;     // 직급

    @Column(name = "org_unit_id")
    private Long orgUnitId;

    private Boolean active = true;   // 재직 true / 퇴사 false

    @Column(length = 40)
    private String tel;

    @Column(length = 100)
    private String email;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        updatedAt = now;
        if (active == null) active = true;
        if (sortOrder == null) sortOrder = 0;
    }

    @PreUpdate
    public void preUpdate() { updatedAt = LocalDateTime.now(); }
}
