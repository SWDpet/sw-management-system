package com.swmanager.system.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;

/**
 * 일일 시계열 메트릭 누적 — P5 메트릭 추이 차트(30일 CPU/메모리/디스크) 데이터 소스.
 *
 * <p>기획서: {@code docs/product-specs/inspection-report-d-v6.md} §4
 * <p>마이그레이션: {@code swdept/sql/V029_inspect_metric_snapshot.sql}
 *
 * <p>적재 경로: agent-windows / agent-unix snapshot.json →
 * {@code InspectionQrBatchService.saveMetricSnapshot()} → 본 테이블 INSERT
 * (server_role IN ('AP','DB') 만, GIS 는 inspect_check_result 회차별 단일값).
 *
 * <p>UNIQUE: (pjt_id, server_role, COALESCE(host_name,''), collected_at) — 동일 시점 재업로드 시
 * INSERT ... ON CONFLICT DO NOTHING (멱등성).
 */
@Getter
@Setter
@Entity
@Table(name = "inspect_metric_snapshot")
public class InspectMetricSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "snapshot_id")
    private Long snapshotId;

    @Column(name = "pjt_id", nullable = false)
    private Long pjtId;

    /** 'AP' / 'DB' — CHECK 제약 (GIS 는 별도 저장소). */
    @Column(name = "server_role", nullable = false, length = 16)
    private String serverRole;

    /** snapshot.host.hostname — 다중 호스트(이중화/RAC) 식별. */
    @Column(name = "host_name", length = 64)
    private String hostName;

    /** snapshot.host.ip — IPv6 대비 45자. */
    @Column(name = "host_ip", length = 45)
    private String hostIp;

    /** agent 측정 시각 (snapshot.taken_at, TZ 포함). */
    @Column(name = "collected_at", nullable = false)
    private OffsetDateTime collectedAt;

    @Column(name = "cpu_pct", precision = 5, scale = 2)
    private BigDecimal cpuPct;

    @Column(name = "mem_pct", precision = 5, scale = 2)
    private BigDecimal memPct;

    /** worst drive 사용률. */
    @Column(name = "disk_pct", precision = 5, scale = 2)
    private BigDecimal diskPct;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_payload", columnDefinition = "jsonb")
    private Map<String, Object> rawPayload;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = OffsetDateTime.now();
        }
    }
}
