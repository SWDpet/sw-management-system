package com.swmanager.system.license.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * License4J 매월 자동 연동 이력 (D1)
 * 기존 license_upload_history(수동 업로드)와 분리된 연동 전용 이력.
 * 스프린트: license4j-monthly-sync
 */
@Entity
@Table(name = "license_sync_history")
@Data
public class LicenseSyncHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** AUTO(스케줄러) / MANUAL(ADMIN 트리거) */
    @Column(name = "run_type", nullable = false, length = 10)
    private String runType;

    /** RUNNING / SUCCESS / PARTIAL / FAIL */
    @Column(nullable = false, length = 10)
    private String status;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Column(name = "total_count")
    private Integer totalCount = 0;

    @Column(name = "new_count")
    private Integer newCount = 0;

    @Column(name = "updated_count")
    private Integer updatedCount = 0;

    @Column(name = "duplicate_count")
    private Integer duplicateCount = 0;

    @Column(name = "fail_count")
    private Integer failCount = 0;

    /** pre-sync 스냅샷 묶음 키 (license_registry_backup.snapshot_id) */
    @Column(name = "snapshot_id", length = 40)
    private String snapshotId;

    /** 그 달 Derby 폴더 백업본 경로 */
    @Column(name = "source_snapshot_path", length = 500)
    private String sourceSnapshotPath;

    /** 요약/에러 메시지 (민감값 마스킹) */
    @Column(columnDefinition = "TEXT")
    private String message;

    /** 수동=사용자ID, 자동=SYSTEM */
    @Column(name = "triggered_by", length = 100)
    private String triggeredBy;
}
