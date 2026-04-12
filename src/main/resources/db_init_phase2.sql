-- Phase 2: DB 테이블 생성
-- 1. 사업별 과업참여자 배정
CREATE TABLE IF NOT EXISTS tb_contract_participant (
    participant_id SERIAL PRIMARY KEY,
    proj_id BIGINT REFERENCES sw_pjt(proj_id),
    user_id BIGINT REFERENCES users(user_id),
    role_type VARCHAR(30),
    tech_grade VARCHAR(30),
    task_desc VARCHAR(500),
    is_site_rep BOOLEAN DEFAULT FALSE,
    sort_order INTEGER DEFAULT 0
);

-- 2. 시스템별 공정명 마스터
CREATE TABLE IF NOT EXISTS tb_process_master (
    process_id SERIAL PRIMARY KEY,
    sys_nm_en VARCHAR(30),
    process_name VARCHAR(200),
    sort_order INTEGER DEFAULT 0,
    use_yn VARCHAR(1) DEFAULT 'Y'
);

-- 3. 시스템별 용역 목적/과업 내용 마스터
CREATE TABLE IF NOT EXISTS tb_service_purpose (
    purpose_id SERIAL PRIMARY KEY,
    sys_nm_en VARCHAR(30),
    purpose_type VARCHAR(20),
    purpose_text TEXT,
    sort_order INTEGER DEFAULT 0,
    use_yn VARCHAR(1) DEFAULT 'Y'
);

-- 기본 공정명 데이터
INSERT INTO tb_process_master (sys_nm_en, process_name, sort_order) VALUES
('UPIS', '도시계획정보체계용 GIS SW 유지관리', 1),
('KRAS', '부동산종합공부시스템용 GIS SW 유지관리', 1),
('IPSS', '지하시설물관리시스템용 GIS SW 유지관리', 1),
('GIS_SW', 'GIS SW 유지관리', 1),
('APIMS', '도로관리시스템용 GIS SW 유지관리', 1)
ON CONFLICT DO NOTHING;

-- 기본 용역 목적 데이터
INSERT INTO tb_service_purpose (sys_nm_en, purpose_type, purpose_text, sort_order) VALUES
('UPIS', 'PURPOSE', '도시계획정보체계(UPIS)의 최신 버전 유지와 원활한 서비스를 제공', 1),
('KRAS', 'PURPOSE', '부동산종합공부시스템(KRAS)의 최신 버전 유지와 원활한 서비스를 제공', 1),
('IPSS', 'PURPOSE', '지하시설물관리시스템(IPSS)의 최신 버전 유지와 원활한 서비스를 제공', 1),
('GIS_SW', 'PURPOSE', 'GIS SW의 최신 버전 유지와 원활한 서비스를 제공', 1),
('APIMS', 'PURPOSE', '도로관리시스템(APIMS)의 최신 버전 유지와 원활한 서비스를 제공', 1)
ON CONFLICT DO NOTHING;

-- users.field_role 컬럼 추가 (분야별: 유지보수책임기술자/유지보수참여기술자)
ALTER TABLE users ADD COLUMN IF NOT EXISTS field_role VARCHAR(50);
UPDATE users SET field_role = '유지보수책임기술자' WHERE username = '박욱진' AND (field_role IS NULL OR field_role = '');
UPDATE users SET field_role = '유지보수참여기술자' WHERE username IN ('김한준','서현규') AND (field_role IS NULL OR field_role = '');

-- users.career_years 컬럼 추가 (경력 연수, 예: "22년")
ALTER TABLE users ADD COLUMN IF NOT EXISTS career_years VARCHAR(20);
UPDATE users SET career_years = '22년' WHERE username = '박욱진' AND (career_years IS NULL OR career_years = '');
UPDATE users SET career_years = '13년' WHERE username = '김한준' AND (career_years IS NULL OR career_years = '');
UPDATE users SET career_years = '8년'  WHERE username = '서현규' AND (career_years IS NULL OR career_years = '');
