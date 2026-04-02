package com.swmanager.system.quotation.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "qt_quotation")
@Getter @Setter
public class Quotation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quote_id")
    private Long quoteId;

    /** 견적번호: UIT - {SQ|PQ|MQ} - {년도} - {순번} */
    @Column(name = "quote_number", unique = true, nullable = false, length = 30)
    private String quoteNumber;

    /** 견적일자 */
    @Column(name = "quote_date", nullable = false)
    private LocalDate quoteDate;

    /** 분류: 용역, 제품, 유지보수 */
    @Column(name = "category", nullable = false, length = 10)
    private String category;

    /** 건명 (프로젝트명) */
    @Column(name = "project_name", nullable = false, length = 500)
    private String projectName;

    /** 수신 기관 */
    @Column(name = "recipient", length = 200)
    private String recipient;

    /** 참조 */
    @Column(name = "reference_to", length = 200)
    private String referenceTo;

    /** 견적 총금액 */
    @Column(name = "total_amount")
    private Long totalAmount;

    /** 견적금액 한글 */
    @Column(name = "total_amount_text", length = 200)
    private String totalAmountText;

    /** 최종 합계금액 (공급가액+VAT 절사+낙찰율 적용, 견적서 출력물 합계) */
    @Column(name = "grand_total")
    private Long grandTotal;

    /** 낙찰율 (%, null이면 미적용) */
    @Column(name = "bid_rate")
    private Double bidRate;

    /** VAT 포함 여부 */
    @Column(name = "vat_included")
    private Boolean vatIncluded = true;

    /** ROUNDDOWN 절사 단위 (1=없음, 100=백단위, 1000=천단위, 10000=만단위, 100000=십만단위) */
    @Column(name = "rounddown_unit")
    private Integer rounddownUnit = 1;

    /** 도장 출력 여부 */
    @Column(name = "show_seal")
    private Boolean showSeal = true;

    /** 출력 양식 (1=기본양식, 2=인건비통합양식) */
    @Column(name = "template_type")
    private Integer templateType = 1;

    /** 비고 */
    @Column(name = "remarks", length = 2000)
    private String remarks;

    /** 상태: 작성중, 발행완료, 취소 */
    @Column(name = "status", length = 10)
    private String status = "작성중";

    /** 작성자 */
    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** 품목 목록 */
    @OneToMany(mappedBy = "quotation", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("itemNo ASC")
    private List<QuotationItem> items = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /** 품목 추가 헬퍼 */
    public void addItem(QuotationItem item) {
        items.add(item);
        item.setQuotation(this);
    }

    public void clearItems() {
        items.forEach(i -> i.setQuotation(null));
        items.clear();
    }
}
