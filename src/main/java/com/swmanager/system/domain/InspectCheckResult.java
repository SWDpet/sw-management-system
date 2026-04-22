package com.swmanager.system.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "inspect_check_result")
public class InspectCheckResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_id", nullable = false)
    private Long reportId;

    @Column(name = "section", nullable = false, length = 20)
    private String section;

    // S10 inspect-check-result-category-master (2026-04-22):
    //  - NULL 허용 (작성 중 임시 저장), 비-NULL 값은 check_category_mst FK 제약으로 검증
    @Column(name = "category", length = 50)
    private String category;

    @Column(name = "item_name", length = 200)
    private String itemName;

    @Column(name = "item_method", length = 300)
    private String itemMethod;

    /**
     * S1 inspect-comprehensive-redesign: result → result_code + result_text 분리
     *  - result_code: InspectResultCode name (NORMAL/PARTIAL/ABNORMAL/NOT_APPLICABLE)
     *  - result_text: 자유 텍스트 (사유/비고)
     */
    @Column(name = "result_code", length = 20)
    private String resultCode;

    @Column(name = "result_text", length = 500)
    private String resultText;

    @Column(name = "remarks", length = 300)
    private String remarks;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.sortOrder == null) {
            this.sortOrder = 0;
        }
    }
}
