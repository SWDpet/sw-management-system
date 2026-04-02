-- V010: 견적서 비고 필드 추가
ALTER TABLE qt_quotation ADD COLUMN IF NOT EXISTS remarks VARCHAR(2000);
