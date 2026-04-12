-- 사용자 테이블 필드 추가 (2026-04-03)

-- 휴대전화
ALTER TABLE users ADD COLUMN IF NOT EXISTS mobile VARCHAR(20);

-- 직급
ALTER TABLE users ADD COLUMN IF NOT EXISTS position_title VARCHAR(50);

-- 주소
ALTER TABLE users ADD COLUMN IF NOT EXISTS address VARCHAR(300);

-- 주민번호 (예: 770914-1234567, 14자리)
ALTER TABLE users ADD COLUMN IF NOT EXISTS ssn VARCHAR(14);

-- 자격증 (복수 가능, 콤마 구분)
ALTER TABLE users ADD COLUMN IF NOT EXISTS certificate VARCHAR(500);

-- 업무 (복수 가능, 콤마 구분. 예: GIS SW 유지보수,시스템 운영)
ALTER TABLE users ADD COLUMN IF NOT EXISTS tasks VARCHAR(1000);

-- 기존 tech_grade 컬럼이 없는 경우 추가
ALTER TABLE users ADD COLUMN IF NOT EXISTS tech_grade VARCHAR(20);
