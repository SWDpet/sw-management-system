package com.swmanager.system.quotation.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 견적번호 관리대장
 * 견적서 발행 시 자동으로 등록됨
 */
@Entity
@Table(name = "qt_quotation_ledger")
@Getter @Setter
public class QuotationLedger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ledger_id")
    private Long ledgerId;

    @Column(name = "quote_id", nullable = false)
    private Long quoteId;

    /** 대장 순번 (연도+분류별 순번) */
    @Column(name = "ledger_no")
    private Integer ledgerNo;

    @Column(name = "year", nullable = false)
    private Integer year;

    /** 분류: 용역, 제품, 유지보수 */
    @Column(name = "category", nullable = false, length = 10)
    private String category;

    @Column(name = "quote_number", nullable = false, length = 30)
    private String quoteNumber;

    @Column(name = "quote_date", nullable = false)
    private LocalDate quoteDate;

    @Column(name = "project_name", nullable = false, length = 500)
    private String projectName;

    @Column(name = "total_amount")
    private Long totalAmount;

    /** 최종 합계금액 (견적서 출력물 합계) */
    @Column(name = "grand_total")
    private Long grandTotal;

    @Column(name = "recipient", length = 200)
    private String recipient;

    @Column(name = "reference_to", length = 200)
    private String referenceTo;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "registered_at")
    private LocalDateTime registeredAt;

    @PrePersist
    public void prePersist() {
        if (this.registeredAt == null) this.registeredAt = LocalDateTime.now();
    }
}
