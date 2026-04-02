-- V008: 견적서 출력 양식 선택 필드 추가
-- 1=기본양식, 2=인건비통합양식
ALTER TABLE qt_quotation ADD COLUMN IF NOT EXISTS template_type INTEGER DEFAULT 1;
