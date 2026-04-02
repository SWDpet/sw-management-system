package com.swmanager.system.quotation.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * 연도별·기술등급별 노임단가
 */
@Entity
@Table(name = "qt_wage_rate", uniqueConstraints = @UniqueConstraint(columnNames = {"year", "grade_name"}))
@Getter @Setter
public class WageRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wage_id")
    private Long wageId;

    @Column(name = "year", nullable = false)
    private Integer year;

    /** 기술등급: 특급기술자, 고급기술자, 중급기술자, 초급기술자, 정보시스템운용자 등 */
    @Column(name = "grade_name", nullable = false, length = 50)
    private String gradeName;

    /** 일 노임단가 (M/D) */
    @Column(name = "daily_rate", nullable = false)
    private Long dailyRate = 0L;

    /** 월 노임단가 (M/M) */
    @Column(name = "monthly_rate")
    private Long monthlyRate = 0L;

    /** 시간 노임단가 (M/H) */
    @Column(name = "hourly_rate")
    private Long hourlyRate = 0L;

    /** 설명 */
    @Column(name = "description", length = 200)
    private String description;
}
