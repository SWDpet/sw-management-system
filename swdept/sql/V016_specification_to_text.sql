-- V016: specification 컬럼을 TEXT 타입으로 변경 (500자 제한 해제)
ALTER TABLE qt_quotation_item ALTER COLUMN specification TYPE TEXT;
