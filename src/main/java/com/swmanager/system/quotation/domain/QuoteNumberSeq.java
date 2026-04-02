package com.swmanager.system.quotation.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * 견적번호 시퀀스 관리
 * 연도+분류별 마지막 시퀀스 번호를 관리
 */
@Entity
@Table(name = "qt_quote_number_seq",
       uniqueConstraints = @UniqueConstraint(columnNames = {"year", "category"}))
@Getter @Setter
public class QuoteNumberSeq {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seq_id")
    private Long id;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "category", nullable = false, length = 10)
    private String category;

    @Column(name = "last_seq")
    private Integer lastSeq = 0;
}
