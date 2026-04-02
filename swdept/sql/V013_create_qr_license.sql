-- V013: QR 라이선스 발급 테이블 생성
CREATE TABLE IF NOT EXISTS qr_license (
    qr_id               BIGSERIAL PRIMARY KEY,

    -- English fields
    end_user_name       VARCHAR(200) NOT NULL,
    address             VARCHAR(500),
    tel                 VARCHAR(50),
    fax                 VARCHAR(50),
    products            VARCHAR(500) NOT NULL,
    user_units          VARCHAR(100),
    version             VARCHAR(50),
    license_type        VARCHAR(100),
    serial_number       VARCHAR(200),
    application_name    VARCHAR(200),

    -- Korean fields
    end_user_name_ko    VARCHAR(200),
    address_ko          VARCHAR(500),
    tel_ko              VARCHAR(50),
    fax_ko              VARCHAR(50),
    products_ko         VARCHAR(500),
    user_units_ko       VARCHAR(100),
    version_ko          VARCHAR(50),
    license_type_ko     VARCHAR(100),
    serial_number_ko    VARCHAR(200),
    application_name_ko VARCHAR(200),

    -- System fields
    qr_image_data       TEXT,
    issued_by           VARCHAR(50),
    issued_at           TIMESTAMP DEFAULT NOW(),
    remarks             VARCHAR(1000)
);

-- 인덱스
CREATE INDEX IF NOT EXISTS idx_qr_license_issued_at ON qr_license(issued_at DESC);
CREATE INDEX IF NOT EXISTS idx_qr_license_end_user ON qr_license(end_user_name);
CREATE INDEX IF NOT EXISTS idx_qr_license_products ON qr_license(products);
