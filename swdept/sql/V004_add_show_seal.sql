-- ============================================================
-- V004: qt_quotation 테이블에 도장 출력 여부 컬럼 추가
-- 프로젝트: SW Manager
-- 작성일: 2026-03-13
-- ============================================================

ALTER TABLE qt_quotation ADD COLUMN IF NOT EXISTS show_seal BOOLEAN DEFAULT true;

COMMENT ON COLUMN qt_quotation.show_seal IS '도장 출력 여부 (true=출력, false=미출력)';
