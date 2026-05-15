package com.swmanager.system.dto.inspection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * /api/inspection/qr-batch 응답.
 * 기획서: docs/product-specs/inspection-qr-batch.md §3 FR-3
 */
@Getter
@Builder
@AllArgsConstructor
public class InspectionQrBatchResponse {

    private Long reportId;
    private String batchId;
    private boolean idempotent;
    private Long pjtId;
    private int tierCount;
    private int itemCount;
    private int manualItems;
    private int warnItems;

    /** "ok" | "warn" | "skip" — header.hash 보조검증 결과 (NFR-3). */
    private String hashCheck;

    /** UI 링크 (예: "/document/inspect/123"). */
    private String reportUrl;
}
