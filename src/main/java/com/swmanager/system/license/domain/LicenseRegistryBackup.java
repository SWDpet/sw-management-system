package com.swmanager.system.license.domain;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/**
 * 라이선스 대장 pre-sync 스냅샷 (FR-4)
 * 매월 연동 직전 license_registry 전량을 jsonb payload 로 백업(스키마 변화 내성).
 * 스프린트: license4j-monthly-sync
 */
@Entity
@Table(name = "license_registry_backup")
@Data
public class LicenseRegistryBackup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 한 번의 연동 = 하나의 snapshot_id (license_sync_history.snapshot_id 와 동일) */
    @Column(name = "snapshot_id", nullable = false, length = 40)
    private String snapshotId;

    @Column(name = "snapshot_at", nullable = false)
    private LocalDateTime snapshotAt;

    @Column(name = "sync_history_id")
    private Long syncHistoryId;

    /** 원본 license_registry.id */
    @Column(name = "registry_id")
    private Long registryId;

    @Column(name = "license_id", length = 50)
    private String licenseId;

    @Column(name = "product_id", length = 100)
    private String productId;

    /** to_jsonb(license_registry row) 전체 */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private String payload;
}
