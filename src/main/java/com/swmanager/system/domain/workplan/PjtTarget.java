package com.swmanager.system.domain.workplan;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * 사업수행계획서 - 유지관리 대상 제품 (P12)
 * 한 사업당 N개 행
 */
@Entity
@Table(name = "tb_pjt_target")
@Getter
@Setter
public class PjtTarget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "proj_id", nullable = false)
    private Long projId;

    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;

    @Column(name = "qty", nullable = false)
    private Integer qty = 1;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;
}
