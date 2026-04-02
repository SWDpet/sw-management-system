-- V015: 절사 단위값 수정
-- 기존: 백단위=100, 천단위=1000, 만단위=10000, 십만단위=100000
-- 수정: 백단위=1000, 천단위=10000, 만단위=100000, 십만단위=1000000
-- "백단위 절사"는 백원 자릿수를 버린다(천원 단위로 남김)는 의미

-- 절사 단위값 변환 (큰 값부터 변환하여 충돌 방지)
UPDATE qt_quotation SET rounddown_unit = 1000000 WHERE rounddown_unit = 100000;
UPDATE qt_quotation SET rounddown_unit = 100000 WHERE rounddown_unit = 10000;
UPDATE qt_quotation SET rounddown_unit = 10000 WHERE rounddown_unit = 1000;
UPDATE qt_quotation SET rounddown_unit = 1000 WHERE rounddown_unit = 100;

-- grand_total 재계산은 앱에서 "금액 재계산" 버튼을 통해 수행
