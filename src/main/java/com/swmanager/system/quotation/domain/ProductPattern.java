package com.swmanager.system.quotation.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * 품명 패턴 마스터
 * 기존 견적서에서 분석된 품명 패턴을 저장
 */
@Entity
@Table(name = "qt_product_pattern")
@Getter @Setter
public class ProductPattern {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pattern_id")
    private Long patternId;

    /** 견적 분류: 용역, 제품, 유지보수 */
    @Column(name = "category", nullable = false, length = 10)
    private String category;

    /** 패턴 그룹: GIS SW 제품, UPIS 유지보수, 인건비 등 */
    @Column(name = "pattern_group", nullable = false, length = 100)
    private String patternGroup;

    /** 품명 */
    @Column(name = "product_name", nullable = false, length = 500)
    private String productName;

    /** 기본 단위 */
    @Column(name = "default_unit", length = 10)
    private String defaultUnit = "식";

    /** 기본 단가 */
    @Column(name = "default_unit_price")
    private Long defaultUnitPrice = 0L;

    /** 설명 */
    @Column(name = "description", length = 500)
    private String description;

    /** 하위 항목 (JSON 문자열) */
    @Column(name = "sub_items", columnDefinition = "TEXT")
    private String subItems;

    /** 사용 횟수 */
    @Column(name = "usage_count")
    private Integer usageCount = 0;

    /** 계산 유형: NORMAL(일반), LABOR(인건비 자동계산) */
    @Column(name = "calc_type", length = 10)
    private String calcType = "NORMAL";

    /** 재경비율 (%) - 직접인건비 대비 */
    @Column(name = "overhead_rate")
    private Double overheadRate = 110.0;

    /** 기술료율 (%) - (직접인건비+재경비) 대비 */
    @Column(name = "tech_fee_rate")
    private Double techFeeRate = 20.0;
}
