-- V009: 절사 기본값을 '절사 없음(1)'으로 변경
-- 기존 기본값(10000=천단위 절사)를 1(절사 없음)로 변경
ALTER TABLE qt_quotation ALTER COLUMN rounddown_unit SET DEFAULT 1;
