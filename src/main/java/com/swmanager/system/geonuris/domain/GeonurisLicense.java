package com.swmanager.system.geonuris.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * GeoNURIS 라이선스 발급 대장 Entity
 * 기존 license_registry 와 완전히 별도 테이블(geonuris_license) 사용
 */
@Entity
@Table(name = "geonuris_license")
@Data
public class GeonurisLicense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── 라이선스 종류 ──────────────────────────────────────────
    @Column(name = "license_type", nullable = false, length = 20)
    private String licenseType;          // GSS30 / GSS35 / DESKTOP / SETL_AGENT / SETL_PROXY

    @Column(name = "file_name", length = 100)
    private String fileName;             // GSS.lic / GeoNURISDesktop.lic / SETLAgent.lic / SETLProxy.lic

    // ── 수혜자 정보 ────────────────────────────────────────────
    @Column(name = "user_name", length = 100)
    private String userName;

    @Column(name = "organization", length = 200)
    private String organization;

    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "issuer", length = 100)
    private String issuer;

    // ── 라이선스 기술 정보 ─────────────────────────────────────
    @Column(name = "mac_address", nullable = false, length = 50)
    private String macAddress;

    @Column(name = "permission", length = 1)
    private String permission;           // A~Z

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    @Column(name = "dbms_type", length = 20)
    private String dbmsType;             // oracle / tibero / mssql / altibase

    @Column(name = "setl_count")
    private Integer setlCount = 0;

    // ── Desktop 플러그인 ───────────────────────────────────────
    @Column(name = "plugin_edit")
    private Boolean pluginEdit = false;

    @Column(name = "plugin_gdm")
    private Boolean pluginGdm = false;

    @Column(name = "plugin_tmbuilder")
    private Boolean pluginTmbuilder = false;

    @Column(name = "plugin_setl")
    private Boolean pluginSetl = false;

    // ── 파일 ───────────────────────────────────────────────────
    @Column(name = "license_data", columnDefinition = "TEXT")
    private String licenseData;          // Base64 인코딩된 .lic 파일

    // ── 시스템 ─────────────────────────────────────────────────
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
        if (this.pluginEdit == null)      this.pluginEdit = false;
        if (this.pluginGdm == null)       this.pluginGdm = false;
        if (this.pluginTmbuilder == null) this.pluginTmbuilder = false;
        if (this.pluginSetl == null)      this.pluginSetl = false;
        if (this.setlCount == null)       this.setlCount = 0;
    }

    // ── 편의 메서드 ───────────────────────────────────────────
    public String getLicenseTypeLabel() {
        if (licenseType == null) return "-";
        return switch (licenseType) {
            case "GSS30"      -> "GeoNURIS GSS 3.0";
            case "GSS35"      -> "GeoNURIS GSS 3.5";
            case "DESKTOP"    -> "GeoNURIS Desktop";
            case "SETL_AGENT" -> "SETL Agent";
            case "SETL_PROXY" -> "SETL Proxy";
            default           -> licenseType;
        };
    }

    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDateTime.now());
    }

    public long getDaysRemaining() {
        if (expiryDate == null) return Long.MAX_VALUE;
        return java.time.Duration.between(LocalDateTime.now(), expiryDate).toDays();
    }
}
