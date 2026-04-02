-- V012: 비고 패턴 테이블 생성
CREATE TABLE IF NOT EXISTS qt_remarks_pattern (
    pattern_id    BIGSERIAL PRIMARY KEY,
    pattern_name  VARCHAR(100) NOT NULL,
    content       TEXT NOT NULL,
    sort_order    INTEGER DEFAULT 0
);

-- 기본 패턴 데이터 삽입
INSERT INTO qt_remarks_pattern (pattern_name, content, sort_order) VALUES
('기본 비고', '1. 상기 견적금액은 제안 내용 기준이며, 세부 사항은 협의에 따라 변경될 수 있습니다.
2. 본 견적서의 유효기간은 발행일로부터 30일입니다.', 1),
('유지보수 비고', '1. 상기 견적금액은 제안 내용 기준이며, 세부 사항은 협의에 따라 변경될 수 있습니다.
2. 본 견적서의 유효기간은 발행일로부터 30일입니다.
3. 유지보수 기간은 계약일로부터 1년입니다.
4. 유지보수 범위는 소프트웨어 결함 수정 및 기술 지원에 한합니다.', 2);
