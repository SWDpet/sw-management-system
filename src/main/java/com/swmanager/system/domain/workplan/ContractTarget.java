package com.swmanager.system.domain.workplan;

import com.swmanager.system.domain.InfraSoftware;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tb_contract_target")
@Getter @Setter
public class ContractTarget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "target_id")
    private Integer targetId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    @Column(name = "target_category", length = 30)
    private String targetCategory; // S/W, H/W 등

    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;

    @Column(name = "product_detail", length = 500)
    private String productDetail;

    @Column(name = "quantity")
    private Integer quantity = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sw_id")
    private InfraSoftware software;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;
}
