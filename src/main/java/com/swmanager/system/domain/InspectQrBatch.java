package com.swmanager.system.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 점검 QR 배치 — PoC 자동수집 원본 + inspect_report 연결.
 *
 * 기획서: docs/product-specs/inspection-qr-batch.md v1
 * 개발계획서: docs/exec-plans/inspection-qr-batch.md v1
 *
 * 멱등 키 = payload_id (UNIQUE). 동일 payload 재업로드 시 신규 row 생성 X, 기존 row + report_id 반환.
 */
@Getter
@Setter
@Entity
@Table(name = "inspect_qr_batch")
public class InspectQrBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payload_id", nullable = false, length = 64, unique = true)
    private String payloadId;

    @Column(name = "report_id")
    private Long reportId;

    @Column(name = "site_code", nullable = false, length = 32)
    private String siteCode;

    @Column(name = "inspect_round", length = 7)
    private String inspectRound;

    @Column(name = "payload_ts")
    private Long payloadTs;

    @Column(name = "source_inspector", length = 50)
    private String sourceInspector;

    @Column(name = "header_hash", length = 16)
    private String headerHash;

    @Column(name = "raw_bytes")
    private Integer rawBytes;

    @Column(name = "gz_bytes")
    private Integer gzBytes;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload_json", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> payloadJson;

    @Column(name = "hash_check", length = 10)
    private String hashCheck;

    @Column(name = "uploaded_by")
    private Long uploadedBy;

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;

    @PrePersist
    protected void onCreate() {
        if (this.uploadedAt == null) {
            this.uploadedAt = LocalDateTime.now();
        }
        if (this.hashCheck == null) {
            this.hashCheck = "skip";
        }
    }
}
