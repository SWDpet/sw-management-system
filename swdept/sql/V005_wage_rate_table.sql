-- ============================================================
-- 노임단가 테이블 및 인건비 자동계산 패턴 지원
-- 프로젝트: SW Manager
-- 작성일: 2026-03-12
-- ============================================================

-- ============================================================
-- 1. 노임단가 테이블 (연도별·직무별 평균 노임단가)
-- ITSQF 직무체계 기반 17개 직무 분류
-- ============================================================
CREATE TABLE IF NOT EXISTS qt_wage_rate (
    wage_id SERIAL PRIMARY KEY,
    year INTEGER NOT NULL,
    grade_name VARCHAR(50) NOT NULL,
    daily_rate BIGINT NOT NULL DEFAULT 0,
    monthly_rate BIGINT DEFAULT 0,
    hourly_rate BIGINT DEFAULT 0,
    description VARCHAR(200),
    UNIQUE(year, grade_name)
);

CREATE INDEX IF NOT EXISTS idx_qt_wage_rate_year ON qt_wage_rate(year);

-- ============================================================
-- 2. qt_product_pattern 테이블에 인건비 계산 관련 컬럼 추가
-- ============================================================
ALTER TABLE qt_product_pattern ADD COLUMN IF NOT EXISTS calc_type VARCHAR(10) DEFAULT 'NORMAL';
ALTER TABLE qt_product_pattern ADD COLUMN IF NOT EXISTS overhead_rate NUMERIC(5,2) DEFAULT 110.0;
ALTER TABLE qt_product_pattern ADD COLUMN IF NOT EXISTS tech_fee_rate NUMERIC(5,2) DEFAULT 20.0;

-- ============================================================
-- 3. 2026년 적용 SW기술자 평균임금 SEED DATA
-- 출처: 한국인공지능·소프트웨어산업협회
--       2025년 SW기술자 임금실태조사 (통계승인 제375001호)
-- 적용기간: 2026-01-01 ~ 2026-12-31
-- 일평균임금 = 월평균임금 ÷ 20.5일
-- 시간평균임금 = 일평균임금 ÷ 8시간
-- ============================================================
INSERT INTO qt_wage_rate (year, grade_name, daily_rate, monthly_rate, hourly_rate, description) VALUES
(2026, 'IT기획자',          578206, 11853218, 72276, '2026년 SW기술자 평균임금 (ITSQF ①)'),
(2026, 'IT컨설턴트',        522340, 10707960, 65292, '2026년 SW기술자 평균임금 (ITSQF ②)'),
(2026, '업무분석가',         475154,  9740667, 59394, '2026년 SW기술자 평균임금 (ITSQF ③)'),
(2026, '데이터분석가',       414600,  8499309, 51825, '2026년 SW기술자 평균임금 (ITSQF ④)'),
(2026, 'IT PM',             492039, 10086804, 61505, '2026년 SW기술자 평균임금 (ITSQF ⑤)'),
(2026, 'IT아키텍트',        541621, 11103230, 67703, '2026년 SW기술자 평균임금 (ITSQF ⑥)'),
(2026, 'UI/UX기획/개발자',  336666,  6901660, 42083, '2026년 SW기술자 평균임금 (ITSQF ⑦)'),
(2026, 'UI/UX디자이너',     251671,  5159246, 31459, '2026년 SW기술자 평균임금 (ITSQF ⑧)'),
(2026, '응용SW개발자',      378250,  7754124, 47281, '2026년 SW기술자 평균임금 (ITSQF ⑨)'),
(2026, '시스템SW개발자',    284888,  5840196, 35611, '2026년 SW기술자 평균임금 (ITSQF ⑩)'),
(2026, '정보시스템운용자',   519469, 10649117, 64934, '2026년 SW기술자 평균임금 (ITSQF ⑪)'),
(2026, 'IT지원기술자',      252196,  5170016, 31524, '2026년 SW기술자 평균임금 (ITSQF ⑫)'),
(2026, 'IT마케터',          575293, 11793500, 71912, '2026년 SW기술자 평균임금 (ITSQF ⑬)'),
(2026, 'IT품질관리자',      538638, 11042071, 67330, '2026년 SW기술자 평균임금 (ITSQF ⑭)'),
(2026, 'IT테스터',          197714,  4053137, 24714, '2026년 SW기술자 평균임금 (ITSQF ⑮)'),
(2026, 'IT감리',            572934, 11745154, 71617, '2026년 SW기술자 평균임금 (ITSQF ⑯)'),
(2026, '정보보안전문가',     507887, 10411680, 63486, '2026년 SW기술자 평균임금 (ITSQF ⑰)')
ON CONFLICT (year, grade_name) DO UPDATE
    SET daily_rate = EXCLUDED.daily_rate,
        monthly_rate = EXCLUDED.monthly_rate,
        hourly_rate = EXCLUDED.hourly_rate,
        description = EXCLUDED.description;

-- ============================================================
-- END OF SCRIPT
-- ============================================================
