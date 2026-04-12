package com.swmanager.system.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "pjt_equip")
@Getter @Setter
public class PjtEquip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "equip_id")
    private Long equipId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proj_id", nullable = false)
    private SwProject project;

    @Column(name = "equip_type", nullable = false, length = 10)
    private String equipType; // HW, SW

    @Column(name = "equip_name", nullable = false, length = 200)
    private String equipName;

    @Column(name = "spec", length = 500)
    private String spec;

    @Column(name = "unit_price")
    private Long unitPrice; // 도입가(부가세포함)

    @Column(name = "quantity")
    private Integer quantity = 1;

    @Column(name = "remark", length = 500)
    private String remark;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(name = "reg_dt")
    private LocalDateTime regDt;

    @PrePersist
    public void prePersist() {
        if (this.regDt == null) this.regDt = LocalDateTime.now();
        if (this.quantity == null) this.quantity = 1;
        if (this.sortOrder == null) this.sortOrder = 0;
    }
}
