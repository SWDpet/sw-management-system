package com.swmanager.system.qrcode.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * QR 라이선스 발급 정보
 */
@Entity
@Table(name = "qr_license")
@Getter @Setter
public class QrLicense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "qr_id")
    private Long qrId;

    // ===== English Fields =====

    /** End User Name */
    @Column(name = "end_user_name", nullable = false, length = 200)
    private String endUserName;

    /** Address */
    @Column(name = "address", length = 500)
    private String address;

    /** Telephone */
    @Column(name = "tel", length = 50)
    private String tel;

    /** Fax */
    @Column(name = "fax", length = 50)
    private String fax;

    /** Products */
    @Column(name = "products", nullable = false, length = 500)
    private String products;

    /** User/Units */
    @Column(name = "user_units", length = 100)
    private String userUnits;

    /** Version */
    @Column(name = "version", length = 50)
    private String version;

    /** License Type */
    @Column(name = "license_type", length = 100)
    private String licenseType;

    /** Serial Number */
    @Column(name = "serial_number", length = 200)
    private String serialNumber;

    /** Application Name */
    @Column(name = "application_name", length = 200)
    private String applicationName;

    // ===== Korean Fields =====

    /** 최종 사용자명 */
    @Column(name = "end_user_name_ko", length = 200)
    private String endUserNameKo;

    /** 주소 */
    @Column(name = "address_ko", length = 500)
    private String addressKo;

    /** 전화번호 */
    @Column(name = "tel_ko", length = 50)
    private String telKo;

    /** 팩스 */
    @Column(name = "fax_ko", length = 50)
    private String faxKo;

    /** 제품명 */
    @Column(name = "products_ko", length = 500)
    private String productsKo;

    /** 사용자/유닛수 */
    @Column(name = "user_units_ko", length = 100)
    private String userUnitsKo;

    /** 버전 */
    @Column(name = "version_ko", length = 50)
    private String versionKo;

    /** 라이선스 유형 */
    @Column(name = "license_type_ko", length = 100)
    private String licenseTypeKo;

    /** 시리얼 번호 */
    @Column(name = "serial_number_ko", length = 200)
    private String serialNumberKo;

    /** 애플리케이션 명 */
    @Column(name = "application_name_ko", length = 200)
    private String applicationNameKo;

    // ===== System Fields =====

    /** QR 코드 데이터 (Base64 PNG) */
    @Column(name = "qr_image_data", columnDefinition = "TEXT")
    private String qrImageData;

    /** 발급자 */
    @Column(name = "issued_by", length = 50)
    private String issuedBy;

    /** 발급일시 */
    @Column(name = "issued_at")
    private LocalDateTime issuedAt;

    /** 비고 */
    @Column(name = "remarks", length = 1000)
    private String remarks;

    @PrePersist
    public void prePersist() {
        if (issuedAt == null) issuedAt = LocalDateTime.now();
    }
}
