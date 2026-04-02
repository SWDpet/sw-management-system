-- ============================================================
-- V003: qt_quotation 테이블에 ROUNDDOWN 절사 단위 컬럼 추가
-- 프로젝트: SW Manager
-- 작성일: 2026-03-13
-- ============================================================

ALTER TABLE qt_quotation ADD COLUMN IF NOT EXISTS rounddown_unit INTEGER DEFAULT 1000;

COMMENT ON COLUMN qt_quotation.rounddown_unit IS 'ROUNDDOWN 절사 단위 (1=없음, 10, 100, 1000, 10000)';
