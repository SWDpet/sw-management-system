package com.swmanager.system.quotation.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * 비고 패턴 마스터
 * 견적서 비고란에 사용할 수 있는 템플릿을 저장
 */
@Entity
@Table(name = "qt_remarks_pattern")
@Getter @Setter
public class RemarksPattern {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pattern_id")
    private Long patternId;

    /** 패턴 이름 (예: 기본 비고, 유지보수 비고) */
    @Column(name = "pattern_name", nullable = false, length = 100)
    private String patternName;

    /** 비고 내용 */
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    /** 정렬 순서 */
    @Column(name = "sort_order")
    private Integer sortOrder = 0;
}
