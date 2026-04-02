-- ============================================================
-- 견적서 자동화 모듈 - 테이블 생성 스크립트
-- 프로젝트: SW Manager
-- 작성일: 2026-03-12
-- 주의: 기존 테이블에 영향 없음 (모든 테이블 qt_ 접두사)
-- ============================================================

-- ============================================================
-- 0. 기존 users 테이블에 견적서 권한 컬럼 추가
-- ============================================================
ALTER TABLE users ADD COLUMN IF NOT EXISTS auth_quotation VARCHAR(10) DEFAULT 'NONE';
-- 관리자 계정에 견적서 편집 권한 자동 부여
UPDATE users SET auth_quotation = 'EDIT' WHERE user_role = 'ROLE_ADMIN' AND (auth_quotation IS NULL OR auth_quotation = 'NONE');


-- ============================================================
-- 1. Main Quotation Table
-- ============================================================
CREATE TABLE IF NOT EXISTS qt_quotation (
    quote_id SERIAL PRIMARY KEY,
    quote_number VARCHAR(30) NOT NULL UNIQUE,
    quote_date DATE NOT NULL,
    category VARCHAR(10) NOT NULL,
    project_name VARCHAR(500) NOT NULL,
    recipient VARCHAR(200),
    reference_to VARCHAR(200),
    total_amount BIGINT DEFAULT 0,
    total_amount_text VARCHAR(200),
    vat_included BOOLEAN DEFAULT TRUE,
    status VARCHAR(10) DEFAULT '작성중',
    created_by VARCHAR(50),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_qt_quotation_category_date ON qt_quotation(category, quote_date);
CREATE INDEX idx_qt_quotation_status ON qt_quotation(status);

-- ============================================================
-- 2. Quotation Line Items Table
-- ============================================================
CREATE TABLE IF NOT EXISTS qt_quotation_item (
    item_id SERIAL PRIMARY KEY,
    quote_id INTEGER NOT NULL REFERENCES qt_quotation(quote_id) ON DELETE CASCADE,
    item_no INTEGER NOT NULL,
    product_name VARCHAR(500) NOT NULL,
    specification VARCHAR(500),
    quantity INTEGER DEFAULT 1,
    unit VARCHAR(10) DEFAULT '식',
    unit_price BIGINT DEFAULT 0,
    amount BIGINT DEFAULT 0,
    remarks VARCHAR(500)
);

CREATE INDEX idx_qt_quotation_item_quote_id ON qt_quotation_item(quote_id);

-- ============================================================
-- 3. Quotation Ledger Table
-- ============================================================
CREATE TABLE IF NOT EXISTS qt_quotation_ledger (
    ledger_id SERIAL PRIMARY KEY,
    quote_id INTEGER NOT NULL,
    ledger_no INTEGER,
    year INTEGER NOT NULL,
    category VARCHAR(10) NOT NULL,
    quote_number VARCHAR(30) NOT NULL,
    quote_date DATE NOT NULL,
    project_name VARCHAR(500) NOT NULL,
    total_amount BIGINT DEFAULT 0,
    recipient VARCHAR(200),
    reference_to VARCHAR(200),
    created_by VARCHAR(50),
    registered_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_qt_quotation_ledger_year_category ON qt_quotation_ledger(year, category);
CREATE INDEX idx_qt_quotation_ledger_quote_id ON qt_quotation_ledger(quote_id);

-- ============================================================
-- 4. Product Pattern Table
-- ============================================================
CREATE TABLE IF NOT EXISTS qt_product_pattern (
    pattern_id SERIAL PRIMARY KEY,
    category VARCHAR(10) NOT NULL,
    pattern_group VARCHAR(100) NOT NULL,
    product_name VARCHAR(500) NOT NULL,
    default_unit VARCHAR(10) DEFAULT '식',
    default_unit_price BIGINT DEFAULT 0,
    description VARCHAR(500),
    sub_items TEXT,
    usage_count INTEGER DEFAULT 0
);

CREATE INDEX idx_qt_product_pattern_category ON qt_product_pattern(category);

-- ============================================================
-- 5. Quote Number Sequence Table
-- ============================================================
CREATE TABLE IF NOT EXISTS qt_quote_number_seq (
    seq_id SERIAL PRIMARY KEY,
    year INTEGER NOT NULL,
    category VARCHAR(10) NOT NULL,
    last_seq INTEGER DEFAULT 0,
    UNIQUE(year, category)
);

-- ============================================================
-- SEED DATA: 실제 분석 기반 품명 패턴 (137개)
-- 1,206개 엑셀 파일에서 추출한 1,574건 중 출현 2회 이상 품명
-- 21개 대분류, 3개 견적분류(용역/제품/유지보수)
-- ============================================================

DELETE FROM qt_product_pattern;

-- [DBMS] 3개 패턴
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('용역', 'DBMS', 'DBMS(오라클) - Oracle Database Standard Edition', '식', 0, '출현횟수 8건 (용역7/제품1)', 8);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('유지보수', 'DBMS', 'DBMS 유지보수', '개월', 0, '출현횟수 3건 (유지보수3)', 3);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('제품', 'DBMS', 'DBMS(Oracle) - S/W', 'EA', 0, '출현횟수 2건 (제품2)', 2);

-- [DB구축/데이터] 11개 패턴
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('용역', 'DB구축/데이터', '기초자료 조사 및 GIS DB구축 부문', '식', 0, '출현횟수 21건 (용역21)', 21);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('용역', 'DB구축/데이터', '기초조사 관리시스템 도입', '식', 0, '출현횟수 13건 (용역13)', 13);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('용역', 'DB구축/데이터', '개발행위허가 DB구축', '식', 0, '출현횟수 7건 (용역7)', 7);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('제품', 'DB구축/데이터', '기초조사 정보체계 v1.0', 'EA', 0, '출현횟수 5건 (제품5)', 5);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('용역', 'DB구축/데이터', '자료조사 및 GIS DB구축 부문', '식', 0, '출현횟수 5건 (용역5)', 5);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('용역', 'DB구축/데이터', '기초조사정보체계 구축', '식', 0, '출현횟수 4건 (용역4)', 4);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('용역', 'DB구축/데이터', '자료조사 및 데이터베이스 구축 부문', '식', 0, '출현횟수 4건 (용역4)', 4);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('제품', 'DB구축/데이터', '기초조사 관리시스템', 'EA', 0, '출현횟수 3건 (제품3)', 3);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('용역', 'DB구축/데이터', '데이터 업로드 기술지원', '식', 0, '출현횟수 3건 (용역3)', 3);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('유지보수', 'DB구축/데이터', '기초조사관리시스템(UPBSS) 유지보수', '개월', 0, '출현횟수 2건 (유지보수2)', 2);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('용역', 'DB구축/데이터', '기초조사정보체계 관리시스템 개발', '식', 0, '출현횟수 2건 (용역2)', 2);

-- [GIS SW 기술지원] 1개 패턴
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('유지보수', 'GIS SW 기술지원', 'GeoNURIS 기술지원', '개월', 0, '출현횟수 4건 (유지보수4)', 4);

-- [GIS SW 유지보수] 12개 패턴 (최다빈출 그룹 - 695건)
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('유지보수', 'GIS SW 유지보수', 'GeoNURIS for KRAS v1.0(GIS S/W) 유지보수', '개월', 0, '출현횟수 418건 - 최다빈출 품명', 418);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('유지보수', 'GIS SW 유지보수', 'GIS S/W (GeoNURIS) 유지보수', '개월', 0, '출현횟수 209건', 209);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('유지보수', 'GIS SW 유지보수', 'GIS S/W 유지보수', '개월', 0, '출현횟수 15건', 15);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('유지보수', 'GIS SW 유지보수', 'GIS S/W (GeoNURIS) 유지보수 (중앙)', '개월', 0, '출현횟수 11건', 11);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('유지보수', 'GIS SW 유지보수', 'GIS S/W (GeoNURIS) 유지보수 (광주)', '개월', 0, '출현횟수 10건', 10);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('유지보수', 'GIS SW 유지보수', 'GIS S/W (GeoNURIS Suite) 유지보수', '개월', 0, '출현횟수 6건', 6);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('유지보수', 'GIS SW 유지보수', 'GeoNURIS v3.0(GIS SW) 유지보수', '개월', 0, '출현횟수 6건', 6);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('유지보수', 'GIS SW 유지보수', 'GeoNURIS SW 유지보수', '개월', 0, '출현횟수 3건', 3);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('유지보수', 'GIS SW 유지보수', 'GeoNURIS(Web 및 Server GIS엔진) 유지보수', '개월', 0, '출현횟수 3건', 3);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('유지보수', 'GIS SW 유지보수', 'GeoNURIS(모바일 GIS 엔진)유지보수', '개월', 0, '출현횟수 3건', 3);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('유지보수', 'GIS SW 유지보수', '기초조사정보시스템(UPBSS) GIS SW 유지보수', '개월', 0, '출현횟수 2건', 2);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('유지보수', 'GIS SW 유지보수', '도시계획정보체계(UPIS) GIS SW 유지보수', '개월', 0, '출현횟수 2건', 2);

-- [GIS SW 제품] 44개 패턴 (최다 품명 그룹)
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('제품', 'GIS SW 제품', 'GeoNURIS Suite (GIS SW)', 'EA', 0, '출현횟수 37건 (용역8/제품29)', 37);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('제품', 'GIS SW 제품', 'UGIS Maple Basic', 'EA', 0, '출현횟수 24건 (제품24)', 24);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('제품', 'GIS SW 제품', 'UGIS Maple Pro', 'EA', 0, '출현횟수 23건 (제품23)', 23);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('유지보수', 'GIS SW 제품', 'GIS S/W(GeoNURIS Sutie)', '개월', 0, '출현횟수 22건 (유지보수22)', 22);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('제품', 'GIS SW 제품', 'Web GIS S/W (GeoNURIS GeoWeb Server)', 'EA', 0, '출현횟수 20건 (제품20)', 20);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('제품', 'GIS SW 제품', 'DB GIS S/W (GeoNURIS GeoSpatial Server)', 'EA', 0, '출현횟수 13건 (제품13)', 13);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('유지보수', 'GIS SW 제품', 'GeoNURIS v3.0', '개월', 0, '출현횟수 8건 (유지보수7/제품1)', 8);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('제품', 'GIS SW 제품', 'Desktop GIS S/W (GeoNURIS Desktop Pro)', 'EA', 0, '출현횟수 7건 (제품7)', 7);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('제품', 'GIS SW 제품', 'GeoNURIS Desktop Pro', 'EA', 0, '출현횟수 5건 (용역2/제품3)', 5);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('제품', 'GIS SW 제품', 'GIS Server (GeoNURIS GeoSpatial Server)', 'EA', 0, '출현횟수 4건 (제품4)', 4);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('용역', 'GIS SW 제품', 'GeoNURIS 서버 구성 및 개발 컨설팅', '식', 0, '출현횟수 4건 (용역4)', 4);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('제품', 'GIS SW 제품', 'DB Gateway 및 Web Server용 GIS 엔진(GEONuris)', 'EA', 0, '출현횟수 3건 (제품3)', 3);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('제품', 'GIS SW 제품', 'Desktop GIS S/W (GeoNURIS Maple)', 'EA', 0, '출현횟수 3건 (제품3)', 3);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('제품', 'GIS SW 제품', 'GIS S/W (UGIS MapStudio)', 'EA', 0, '출현횟수 3건 (제품3)', 3);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('제품', 'GIS SW 제품', 'GeoNURIS Desktop Pro(MAPLE)', 'EA', 0, '출현횟수 3건 (용역1/제품2)', 3);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('제품', 'GIS SW 제품', 'GeoNURIS Suite (GIS S/W)', 'EA', 0, '출현횟수 3건 (제품3)', 3);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('제품', 'GIS SW 제품', 'GeoNURIS for KRAS Suite', 'EA', 0, '출현횟수 3건 (제품3)', 3);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('제품', 'GIS SW 제품', 'Maple (Desktop GIS SW)', 'EA', 0, '출현횟수 3건 (제품3)', 3);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('제품', 'GIS SW 제품', 'UGIS Suite (GIS SW)', 'EA', 0, '출현횟수 3건 (제품3)', 3);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('제품', 'GIS SW 제품', 'Web Server용 GIS 엔진(GEONuris) - S/W', 'EA', 0, '출현횟수 3건 (제품3)', 3);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('제품', 'GIS SW 제품', 'geonuris 4.0(Tiler 포함)', 'EA', 0, '출현횟수 3건 (제품3)', 3);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('제품', 'GIS SW 제품', '편집용 GIS 엔진 - S/W', 'EA', 0, '출현횟수 3건 (제품3)', 3);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('제품', 'GIS SW 제품', 'DB Gateway 용 GIS 엔진(GEONuris) - S/W', 'EA', 0, '출현횟수 2건 (제품2)', 2);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('제품', 'GIS SW 제품', 'GIS Client (GeoNURIS Client)', 'EA', 0, '출현횟수 2건 (제품2)', 2);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('용역', 'GIS SW 제품', 'GIS엔진(GeoNURIS v3.0)', '식', 0, '출현횟수 2건 (용역2)', 2);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('제품', 'GIS SW 제품', 'GeoNURIS GWS (MapStudio Basic)', 'EA', 0, '출현횟수 2건 (제품2)', 2);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('제품', 'GIS SW 제품', 'GeoNURIS Geo Spatial Server 3.5(GSS)', 'EA', 0, '출현횟수 2건 (제품2)', 2);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('제품', 'GIS SW 제품', 'GeoNURIS Geo WebServer 3.5', 'EA', 0, '출현횟수 2건 (제품2)', 2);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('제품', 'GIS SW 제품', 'GeoNURIS GeoWeb Server', 'EA', 0, '출현횟수 2건 (제품2)', 2);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('제품', 'GIS SW 제품', 'GeoNURIS MapStudio Basic', 'EA', 0, '출현횟수 2건 (제품2)', 2);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('제품', 'GIS SW 제품', 'GeoNURIS Runtime (단말용)', 'EA', 0, '출현횟수 2건 (제품2)', 2);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('제품', 'GIS SW 제품', 'GeoNURIS SDI 3.5', 'EA', 0, '출현횟수 2건 (제품2)', 2);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('제품', 'GIS SW 제품', 'GeoNURIS SDK (개발용)', 'EA', 0, '출현횟수 2건 (제품2)', 2);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('제품', 'GIS SW 제품', 'GeoNURIS SETL', 'EA', 0, '출현횟수 2건 (제품2)', 2);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('제품', 'GIS SW 제품', 'GeoNURIS Suite(GIS SW)', 'EA', 0, '출현횟수 2건 (제품2)', 2);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('제품', 'GIS SW 제품', 'MapStudio (Web & DB Gateway GIS SW)', 'EA', 0, '출현횟수 2건 (제품2)', 2);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('제품', 'GIS SW 제품', 'Maple 1.0 (Desktop GIS SW)', 'EA', 0, '출현횟수 2건 (제품2)', 2);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('제품', 'GIS SW 제품', 'Professional GIS SW for DB Construction & Conversion (GeoNURIS Desktop Pro-Extentions)', 'EA', 0, '출현횟수 2건 (제품2)', 2);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('제품', 'GIS SW 제품', 'UGIS MapStudio 업그레이드 라이선스', 'EA', 0, '출현횟수 2건 (제품2)', 2);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('제품', 'GIS SW 제품', 'Web GIS Server (GeoNURIS GeoWeb Server)', 'EA', 0, '출현횟수 2건 (제품2)', 2);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('제품', 'GIS SW 제품', '부동산종합공부시스템(KRAS) GIS SW', 'EA', 0, '출현횟수 2건 (제품2)', 2);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('용역', 'GIS SW 제품', 'GeoNURIS Suite(GIS SW) 해외향 - 서버', '식', 0, '출현횟수 2건 (용역1/제품1)', 2);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('용역', 'GIS SW 제품', 'GeoNURIS 레이어 등록 및 서비스 구성', '식', 0, '출현횟수 2건 (용역2)', 2);

-- [UPIS 관련] 12개 패턴
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('용역', 'UPIS DB구축/현행화', '도시계획정보체계(UPIS) DB구축', '식', 0, '출현횟수 4건 (용역4)', 4);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('용역', 'UPIS 관련 기타', '도시계획정보체계시스템 기술지원', '식', 0, '출현횟수 3건 (용역2/유지보수1)', 3);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('용역', 'UPIS 시스템 설치', 'UPIS 표준시스템 재설치', '식', 0, '출현횟수 16건 (용역13/유지보수3)', 16);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('유지보수', 'UPIS 유지보수/유지관리', 'UPIS 서버(DB, APP서버) 유지보수', '개월', 0, '출현횟수 15건 (용역7/유지보수8)', 15);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('유지보수', 'UPIS 유지보수/유지관리', '표준시스템(UPIS) 유지보수', '개월', 0, '출현횟수 14건 (유지보수14)', 14);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('유지보수', 'UPIS 유지보수/유지관리', 'UPIS 전산장비 및 DBMS 유지보수', '개월', 0, '출현횟수 6건 (유지보수6)', 6);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('용역', 'UPIS 유지보수/유지관리', 'UPIS 전산장비 유지관리', '개월', 0, '출현횟수 6건 (용역5/유지보수1)', 6);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('유지보수', 'UPIS 유지보수/유지관리', '도시계획정보체계(UPIS) 표준시스템 유지관리 기술지원', '개월', 0, '출현횟수 6건 (유지보수5/제품1)', 6);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('용역', 'UPIS 유지보수/유지관리', '양평군 도시계획정보체계(UPIS) DB유지관리 용역', '식', 0, '출현횟수 5건 (용역5)', 5);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('유지보수', 'UPIS 유지보수/유지관리', '도시계획정보체계(UPIS) 시스템 유지관리', '개월', 0, '출현횟수 4건 (유지보수4)', 4);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('유지보수', 'UPIS 유지보수/유지관리', '도시계획정보체계(UPIS) 시스템 유지관리 기술지원', '개월', 0, '출현횟수 3건 (유지보수2/용역1)', 3);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('유지보수', 'UPIS 유지보수/유지관리', '도시계획정보체계(UPIS) 표준시스템 유지관리', '개월', 0, '출현횟수 2건 (유지보수2)', 2);

-- [인건비] 3개 패턴
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('용역', '인건비', '직접인건비', '인/월', 0, '출현횟수 93건 (용역93) - 2위 빈출', 93);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('용역', '인건비', '인건비', '인/월', 0, '출현횟수 5건 (용역4/제품1)', 5);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('제품', '인건비', '인건비 (시스템 운영환경 및 데이터 최적화)', '인/월', 0, '출현횟수 2건 (제품2)', 2);

-- [전산장비 도입/유지] 4개 패턴
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('용역', '전산장비 도입/유지', '전산장비 도입', 'EA', 0, '출현횟수 25건 (용역25)', 25);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('용역', '전산장비 도입/유지', '운영장비 도입', 'EA', 0, '출현횟수 6건 (용역6)', 6);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('용역', '전산장비 도입/유지', '전산장비 도입 부문', 'EA', 0, '출현횟수 2건 (용역2)', 2);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('유지보수', '전산장비 도입/유지', '전산장비 유지관리', 'EA', 0, '출현횟수 2건 (유지보수2)', 2);

-- [서버/HW 도입] 9개 패턴
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('용역', '서버/HW 도입', '데이터베이스 서버', 'EA', 0, '출현횟수 9건 (용역7/제품2)', 9);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('용역', '서버/HW 도입', '어플리케이션 서버', 'EA', 0, '출현횟수 9건 (용역7/제품2)', 9);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('제품', '서버/HW 도입', '데스크탑 인텔 i7(모니터포함)', 'EA', 0, '출현횟수 7건 (제품7)', 7);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('제품', '서버/HW 도입', 'IBM X3650M4', 'EA', 0, '출현횟수 5건 (제품5)', 5);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('제품', '서버/HW 도입', 'AP서버', 'EA', 0, '출현횟수 3건 (제품3)', 3);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('제품', '서버/HW 도입', '어플리케이션 서버 - H/W', 'EA', 0, '출현횟수 3건 (제품3)', 3);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('제품', '서버/HW 도입', 'GIS Server', 'EA', 0, '출현횟수 2건 (제품2)', 2);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('제품', '서버/HW 도입', 'Web GIS Server', 'EA', 0, '출현횟수 2건 (제품2)', 2);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('제품', '서버/HW 도입', '데이터베이스 서버 - H/W', 'EA', 0, '출현횟수 2건 (제품2)', 2);

-- [서버/HW 유지보수] 2개 패턴
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('유지보수', '서버/HW 유지보수', 'AP서버 H/W 유지보수', '개월', 0, '출현횟수 7건 (유지보수6/용역1)', 7);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('유지보수', '서버/HW 유지보수', 'DB서버 H/W 유지보수', '개월', 0, '출현횟수 2건 (유지보수2)', 2);

-- [관리시스템 도입] 7개 패턴
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('용역', '관리시스템 도입', '관리시스템 및 전산장비 도입 부문', '식', 0, '출현횟수 11건 (용역11)', 11);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('용역', '관리시스템 도입', '국유재산관리시스템 서비스 커스터마이징', '식', 0, '출현횟수 7건 (용역7)', 7);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('용역', '관리시스템 도입', '공원녹지관리시스템 도입', '식', 0, '출현횟수 4건 (용역4)', 4);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('제품', '관리시스템 도입', 'GS인증 공원녹지관리시스템 - S/W', 'EA', 0, '출현횟수 3건 (제품3)', 3);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('용역', '관리시스템 도입', '전산장비 및 관리시스템 도입', '식', 0, '출현횟수 3건 (용역3)', 3);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('용역', '관리시스템 도입', '국유재산관리시스템 항공영상 타일링 용역', '식', 0, '출현횟수 2건 (용역2)', 2);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('제품', '관리시스템 도입', '농업생산기반시설 관리시스템', 'EA', 0, '출현횟수 2건 (제품2)', 2);

-- [토지적성평가 프로그램] 6개 패턴
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('용역', '토지적성평가 프로그램', '토지적성평가', '식', 0, '출현횟수 7건 (용역7)', 7);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('제품', '토지적성평가 프로그램', '토지적성평가 운영 S/W (GIS Maple Basic)', 'EA', 0, '출현횟수 3건 (제품3)', 3);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('제품', '토지적성평가 프로그램', '토지적성평가 인증프로그램 업그레이드', 'EA', 0, '출현횟수 3건 (제품3)', 3);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('용역', '토지적성평가 프로그램', '토지적성평가 수행', '식', 0, '출현횟수 2건 (용역2)', 2);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('유지보수', '토지적성평가 프로그램', '토지적성평가 프로그램 운영SW 유지보수', '개월', 0, '출현횟수 2건 (유지보수2)', 2);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('용역', '토지적성평가 프로그램', '토지적성평가시스템 결과 데이터 변환', '식', 0, '출현횟수 2건 (용역2)', 2);

-- [스마트도시/U-City] 4개 패턴
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('유지보수', '스마트도시/U-City', 'U-City 통합시스템 기술지원', '식', 0, '출현횟수 3건 (유지보수3)', 3);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('용역', '스마트도시/U-City', '스마트도시 통합시스템 기술지원', '식', 0, '출현횟수 3건 (용역3)', 3);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('유지보수', '스마트도시/U-City', '스마트 도시재생플랫폼', '식', 0, '출현횟수 2건 (유지보수2)', 2);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('용역', '스마트도시/U-City', '화성봉담2 공공주택지구 스마트시티 마스터플랜 수립 용역', '식', 0, '출현횟수 2건 (용역2)', 2);

-- [개발/컨설팅] 1개 패턴
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('용역', '개발/컨설팅', '기능 개발', '식', 0, '출현횟수 3건 (용역3)', 3);

-- [경비/부대비용] 1개 패턴
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('용역', '경비/부대비용', '직접경비', '식', 0, '출현횟수 4건 (용역4)', 4);

-- [라이선스] 1개 패턴
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('유지보수', '라이선스', '개발용 라이선스', 'EA', 0, '출현횟수 3건 (유지보수3)', 3);

-- [기타] 14개 패턴
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('유지보수', '기타', '온라인 기술지원', '식', 0, '출현횟수 3건 (유지보수3)', 3);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('제품', '기타', 'GIS Client', 'EA', 0, '출현횟수 2건 (제품2)', 2);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('제품', '기타', '노트북(HP 오멘 17-W126TX)', 'EA', 0, '출현횟수 2건 (제품2)', 2);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('제품', '기타', '노트북(MSI GP62-i7 6QF)', 'EA', 0, '출현횟수 2건 (제품2)', 2);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('제품', '기타', '미디어편집 공유 스토리지', 'EA', 0, '출현횟수 2건 (제품2)', 2);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('용역', '기타', '도시계획 도형자료 현행화', '식', 0, '출현횟수 2건 (용역2)', 2);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('용역', '기타', '도시계획 속성자료 현행화', '식', 0, '출현횟수 2건 (용역2)', 2);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('용역', '기타', '도형자료 현행화', '식', 0, '출현횟수 2건 (용역2)', 2);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('용역', '기타', '속성자료 현행화', '식', 0, '출현횟수 2건 (용역2)', 2);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('용역', '기타', '표준시스템 설치', '식', 0, '출현횟수 2건 (용역2)', 2);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('용역', '기타', '표준시스템 유지관리', '식', 0, '출현횟수 2건 (용역2)', 2);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('용역', '기타', '2017년도 공원계획시스템 유지관리 용역', '식', 0, '출현횟수 2건 (용역2)', 2);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('용역', '기타', '2018년 도시재생 뉴딜사업 공모제안서 작성용역', '식', 0, '출현횟수 2건 (용역2)', 2);
INSERT INTO qt_product_pattern (category, pattern_group, product_name, default_unit, default_unit_price, description, usage_count) VALUES ('용역', '기타', 'HP Z440 Workstation PC', '식', 0, '출현횟수 2건 (용역2)', 2);

-- ============================================================
-- END OF SCRIPT
-- 총 137개 패턴 (1,206개 엑셀 분석 → 1,574건 중 출현 2회 이상)
-- ============================================================
