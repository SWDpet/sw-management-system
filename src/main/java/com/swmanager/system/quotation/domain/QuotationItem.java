package com.swmanager.system.quotation.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "qt_quotation_item")
@Getter @Setter
public class QuotationItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long itemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quote_id", nullable = false)
    private Quotation quotation;

    /** 품목 번호 (1, 2, 3...) */
    @Column(name = "item_no", nullable = false)
    private Integer itemNo;

    /** 품명 */
    @Column(name = "product_name", nullable = false, length = 500)
    private String productName;

    /** 규격 */
    @Column(name = "specification", columnDefinition = "TEXT")
    private String specification;

    /** 수량 */
    @Column(name = "quantity")
    private Integer quantity = 1;

    /** 단위 */
    @Column(name = "unit", length = 10)
    private String unit = "식";

    /** 단가 */
    @Column(name = "unit_price")
    private Long unitPrice = 0L;

    /** 금액 */
    @Column(name = "amount")
    private Long amount = 0L;

    /** 비고 */
    @Column(name = "remarks", length = 500)
    private String remarks;
}
