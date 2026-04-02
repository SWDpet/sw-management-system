package com.swmanager.system.license.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 라이선스 대장 (License Registry)
 * CSV 완벽 매핑: 63개 컬럼 + 시스템 필드 3개 = 66개
 */
@Entity
@Table(name = "license_registry")
@Data
public class LicenseRegistry {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // ========== CSV 컬럼 (63개) ==========
    
    @Column(nullable = false, length = 50)
    private String licenseId;                     // License ID
    
    @Column(nullable = false, length = 100)
    private String productId;                     // Product ID
    
    @Column(length = 50)
    private String licenseType;                   // License Type
    
    @Column(length = 100)
    private String validProductEdition;           // Valid Product Edition
    
    @Column(length = 50)
    private String validProductVersion;           // Valid Product Version
    
    @Column
    private Integer validityPeriod;               // Validity Period
    
    @Column
    private Integer maintenancePeriod;            // Maintenance Period
    
    @Column(length = 50)
    private String generationSource;              // Generation Source
    
    @Column
    private LocalDateTime generationDateTime;     // Generation Date Time
    
    @Column
    private Integer quantity = 1;                 // Quantity
    
    @Column
    private Integer allowedUseCount;              // Allowed Use Count
    
    @Column(columnDefinition = "TEXT")
    private String hardwareId;                    // Hardware ID
    
    @Column(length = 200)
    private String registeredTo;                  // Registered To (한글 포함)
    
    @Column(length = 200)
    private String fullName;                      // Full Name
    
    @Column(length = 200)
    private String email;                         // E-Mail
    
    @Column(length = 200)
    private String company;                       // Company (한글 포함)
    
    @Column(length = 50)
    private String telephone;                     // Telephone
    
    @Column(length = 50)
    private String fax;                           // Fax
    
    @Column(length = 200)
    private String street;                        // Street
    
    @Column(length = 100)
    private String city;                          // City
    
    @Column(length = 20)
    private String zipCode;                       // Zip Code
    
    @Column(length = 100)
    private String country;                       // Country (발급자)
    
    @Column
    private Boolean activationRequired;           // Activation Required
    
    @Column
    private Boolean autoActivationsDisabled;      // Auto Activations Disabled
    
    @Column
    private Boolean manualActivationsDisabled;    // Manual Activations Disabled
    
    @Column
    private Boolean onlineKeyLeaseDisabled;       // Online Key Lease Disabled
    
    @Column
    private Boolean deactivationsDisabled;        // Deactivations Disabled
    
    @Column
    private Integer activationPeriod;             // Activation Period
    
    @Column
    private Integer allowedActivationCount;       // Allowed Activation Count
    
    @Column
    private Integer allowedDeactivationCount;     // Allowed Deactivation Count
    
    @Column
    private Boolean dontKeepDeactivationRecords;  // Dont Keep Deactivation Records
    
    @Column(columnDefinition = "TEXT")
    private String ipBlocks;                      // IP Blocks
    
    @Column
    private Boolean ipBlocksAllow;                // IP Blocks Allow
    
    @Column(length = 50)
    private String activationReturn;              // Activation Return
    
    @Column
    private Boolean rejectModificationKeyUsage;   // Reject Modification Key Usage
    
    @Column
    private Boolean setGenerationTimeToActivationTime;  // Set Generation Time to Activation Time
    
    @Column
    private Integer generationTimeOffsetFromActivationTime;  // Generation Time Offset From Activation Time
    
    @Column(length = 500)
    private String hardwareIdSelection;           // Hardware ID Selection
    
    @Column(columnDefinition = "TEXT")
    private String internalHiddenString;          // Internal Hidden String
    
    @Column
    private Boolean useCustomerNameInValidation;  // Use Customer Name in Validation
    
    @Column
    private Boolean useCompanyNameInValidation;   // Use Company Name in Validation
    
    @Column
    private Integer floatingLicenseCheckPeriod;   // Floating License Check Period
    
    @Column
    private Integer floatingLicenseServerConnectionCheckPeriod;  // Floating License Server Connection Check Period
    
    @Column
    private Boolean floatingAllowMultipleInstances;  // Floating Allow Multiple Instances
    
    @Column(columnDefinition = "TEXT")
    private String supersededLicenseIds;          // Superseded License IDs
    
    @Column
    private Integer maxInactivePeriod;            // Max Inactive Period
    
    @Column
    private Integer maximumReChecksBeforeDrop;    // Maximum Re-Checks Before Drop
    
    @Column
    private Boolean dontKeepReleasedLicenseUsage; // Don't Keep Released License Usage
    
    @Column
    private Integer currentUseCount;              // Current Use Count
    
    @Column
    private Integer allowedUseCountLimit;         // Allowed Use Count Limit
    
    @Column
    private Integer currentUseTime;               // Current Use Time
    
    @Column
    private Integer allowedUseTimeLimit;          // Allowed Use Time Limit
    
    @Column(length = 50)
    private String dateTimeCheck;                 // Date Time Check
    
    @Column
    private Boolean ntpServerCheck;               // NTP Server Check
    
    @Column(length = 200)
    private String ntpServer;                     // NTP Server
    
    @Column
    private Integer ntpServerTimeout;             // NTP Server Timeout
    
    @Column
    private Boolean webServerCheck;               // Web Server Check
    
    @Column(length = 500)
    private String webServer;                     // Web Server
    
    @Column
    private Integer webServerTimeout;             // Web Server Timeout
    
    @Column
    private Boolean queryLocalAdServer;           // Query Local AD Server
    
    @Column(columnDefinition = "TEXT")
    private String unsignedCustomFeatures;        // Unsigned Custom Features
    
    @Column(columnDefinition = "TEXT")
    private String signedCustomFeatures;          // Signed Custom Features
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String licenseString;                 // License String
    
    // ========== 시스템 필드 (3개) ==========
    
    @Column(nullable = false)
    private LocalDateTime uploadDate;             // CSV 업로드 날짜
    
    @Column(length = 100)
    private String uploadedBy;                    // 업로드한 사용자
    
    @Column(nullable = false)
    private LocalDateTime createdDate;            // 생성일
    
    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        if (uploadDate == null) {
            uploadDate = LocalDateTime.now();
        }
    }
    
    /**
     * 만료 여부 계산
     */
    public boolean isExpired() {
        if (generationDateTime == null || validityPeriod == null || validityPeriod == 0) {
            return false;  // 무제한
        }
        LocalDateTime expiryDate = generationDateTime.plusDays(validityPeriod);
        return LocalDateTime.now().isAfter(expiryDate);
    }
    
    /**
     * 남은 일수 계산
     */
    public long getDaysRemaining() {
        if (generationDateTime == null || validityPeriod == null || validityPeriod == 0) {
            return 999999;  // 무제한
        }
        LocalDateTime expiryDate = generationDateTime.plusDays(validityPeriod);
        if (isExpired()) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDateTime.now(), expiryDate);
    }
    
    /**
     * 만료일 계산 (표시용)
     */
    public LocalDateTime getExpiryDate() {
        if (generationDateTime == null || validityPeriod == null || validityPeriod == 0) {
            return null;  // 무제한
        }
        return generationDateTime.plusDays(validityPeriod);
    }
}
