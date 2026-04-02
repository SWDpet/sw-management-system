-- V007: 절사 단위 체계 변경 (천단위 절사 = 10000으로 변환)
-- 기존: value=1000이면 Math.floor(v/1000)*1000 (변화 없음, 잘못된 로직)
-- 변경: "천단위 절사" = 천의 자리를 절사 = value=10000 → Math.floor(v/10000)*10000
-- 예: 571,000 → 570,000

-- 기존 데이터를 새 체계로 변환 (각 단위를 10배로)
UPDATE qt_quotation SET rounddown_unit = rounddown_unit * 10
WHERE rounddown_unit IN (10, 100, 1000, 10000);

-- 기본값 변경 (1000 → 10000)
ALTER TABLE qt_quotation ALTER COLUMN rounddown_unit SET DEFAULT 10000;
