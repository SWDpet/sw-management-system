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

    /**
     * S3 (qt-remarks-users-link, 2026-04-20):
     * 담당자 user_id (NULL 허용)
     *  - 값이 있으면 RemarksRenderer가 content의 {username}/{dept_nm}/{position_title} 치환
     *  - NULL 이면 placeholder 미치환 (content 원문 그대로 출력)
     */
    @Column(name = "user_id")
    private Long userId;
}
