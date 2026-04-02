-- ============================================================
-- V005: 측량업체 노임단가 데이터 추가
-- 프로젝트: SW Manager
-- 작성일: 2026-03-13
-- 기준: 측량업체 노임단가표 (단위: 원, 시간, 일, 개월 기준)
-- ============================================================

-- 기술계
INSERT INTO qt_wage_rate (year, grade_name, daily_rate, monthly_rate, hourly_rate, description)
VALUES (2026, '기술사', 442679, 0, 0, '측량업체-기술계')
ON CONFLICT (year, grade_name) DO UPDATE SET daily_rate = EXCLUDED.daily_rate, description = EXCLUDED.description;

INSERT INTO qt_wage_rate (year, grade_name, daily_rate, monthly_rate, hourly_rate, description)
VALUES (2026, '특급기술자', 326774, 0, 0, '측량업체-기술계')
ON CONFLICT (year, grade_name) DO UPDATE SET daily_rate = EXCLUDED.daily_rate, description = EXCLUDED.description;

INSERT INTO qt_wage_rate (year, grade_name, daily_rate, monthly_rate, hourly_rate, description)
VALUES (2026, '고급기술자', 289265, 0, 0, '측량업체-기술계')
ON CONFLICT (year, grade_name) DO UPDATE SET daily_rate = EXCLUDED.daily_rate, description = EXCLUDED.description;

INSERT INTO qt_wage_rate (year, grade_name, daily_rate, monthly_rate, hourly_rate, description)
VALUES (2026, '중급기술자', 251596, 0, 0, '측량업체-기술계')
ON CONFLICT (year, grade_name) DO UPDATE SET daily_rate = EXCLUDED.daily_rate, description = EXCLUDED.description;

INSERT INTO qt_wage_rate (year, grade_name, daily_rate, monthly_rate, hourly_rate, description)
VALUES (2026, '초급기술자', 215609, 0, 0, '측량업체-기술계')
ON CONFLICT (year, grade_name) DO UPDATE SET daily_rate = EXCLUDED.daily_rate, description = EXCLUDED.description;

-- 기능계 - 측량
INSERT INTO qt_wage_rate (year, grade_name, daily_rate, monthly_rate, hourly_rate, description)
VALUES (2026, '측량고급기술자', 259647, 0, 0, '측량업체-기능계/측량')
ON CONFLICT (year, grade_name) DO UPDATE SET daily_rate = EXCLUDED.daily_rate, description = EXCLUDED.description;

INSERT INTO qt_wage_rate (year, grade_name, daily_rate, monthly_rate, hourly_rate, description)
VALUES (2026, '측량중급기술자', 235899, 0, 0, '측량업체-기능계/측량')
ON CONFLICT (year, grade_name) DO UPDATE SET daily_rate = EXCLUDED.daily_rate, description = EXCLUDED.description;

INSERT INTO qt_wage_rate (year, grade_name, daily_rate, monthly_rate, hourly_rate, description)
VALUES (2026, '측량초급기술자', 200786, 0, 0, '측량업체-기능계/측량')
ON CONFLICT (year, grade_name) DO UPDATE SET daily_rate = EXCLUDED.daily_rate, description = EXCLUDED.description;

-- 기능계 - 지도제작
INSERT INTO qt_wage_rate (year, grade_name, daily_rate, monthly_rate, hourly_rate, description)
VALUES (2026, '지도제작고급기술자', 268435, 0, 0, '측량업체-기능계/지도제작')
ON CONFLICT (year, grade_name) DO UPDATE SET daily_rate = EXCLUDED.daily_rate, description = EXCLUDED.description;

INSERT INTO qt_wage_rate (year, grade_name, daily_rate, monthly_rate, hourly_rate, description)
VALUES (2026, '지도제작중급기술자', 242957, 0, 0, '측량업체-기능계/지도제작')
ON CONFLICT (year, grade_name) DO UPDATE SET daily_rate = EXCLUDED.daily_rate, description = EXCLUDED.description;

INSERT INTO qt_wage_rate (year, grade_name, daily_rate, monthly_rate, hourly_rate, description)
VALUES (2026, '지도제작초급기술자', 198089, 0, 0, '측량업체-기능계/지도제작')
ON CONFLICT (year, grade_name) DO UPDATE SET daily_rate = EXCLUDED.daily_rate, description = EXCLUDED.description;

-- 기능계 - 도화
INSERT INTO qt_wage_rate (year, grade_name, daily_rate, monthly_rate, hourly_rate, description)
VALUES (2026, '도화고급기술자', 300967, 0, 0, '측량업체-기능계/도화')
ON CONFLICT (year, grade_name) DO UPDATE SET daily_rate = EXCLUDED.daily_rate, description = EXCLUDED.description;

INSERT INTO qt_wage_rate (year, grade_name, daily_rate, monthly_rate, hourly_rate, description)
VALUES (2026, '도화중급기술자', 236885, 0, 0, '측량업체-기능계/도화')
ON CONFLICT (year, grade_name) DO UPDATE SET daily_rate = EXCLUDED.daily_rate, description = EXCLUDED.description;

INSERT INTO qt_wage_rate (year, grade_name, daily_rate, monthly_rate, hourly_rate, description)
VALUES (2026, '도화초급기술자', 229483, 0, 0, '측량업체-기능계/도화')
ON CONFLICT (year, grade_name) DO UPDATE SET daily_rate = EXCLUDED.daily_rate, description = EXCLUDED.description;

-- 기능계 - 항공사진
INSERT INTO qt_wage_rate (year, grade_name, daily_rate, monthly_rate, hourly_rate, description)
VALUES (2026, '항공사진고급기술자', 299990, 0, 0, '측량업체-기능계/항공사진')
ON CONFLICT (year, grade_name) DO UPDATE SET daily_rate = EXCLUDED.daily_rate, description = EXCLUDED.description;

INSERT INTO qt_wage_rate (year, grade_name, daily_rate, monthly_rate, hourly_rate, description)
VALUES (2026, '항공사진중급기술자', 270554, 0, 0, '측량업체-기능계/항공사진')
ON CONFLICT (year, grade_name) DO UPDATE SET daily_rate = EXCLUDED.daily_rate, description = EXCLUDED.description;

INSERT INTO qt_wage_rate (year, grade_name, daily_rate, monthly_rate, hourly_rate, description)
VALUES (2026, '항공사진초급기술자', 237664, 0, 0, '측량업체-기능계/항공사진')
ON CONFLICT (year, grade_name) DO UPDATE SET daily_rate = EXCLUDED.daily_rate, description = EXCLUDED.description;

-- 기타
INSERT INTO qt_wage_rate (year, grade_name, daily_rate, monthly_rate, hourly_rate, description)
VALUES (2026, '사업용조종사', 313877, 0, 0, '측량업체-기타')
ON CONFLICT (year, grade_name) DO UPDATE SET daily_rate = EXCLUDED.daily_rate, description = EXCLUDED.description;

INSERT INTO qt_wage_rate (year, grade_name, daily_rate, monthly_rate, hourly_rate, description)
VALUES (2026, '항법사', 303861, 0, 0, '측량업체-기타')
ON CONFLICT (year, grade_name) DO UPDATE SET daily_rate = EXCLUDED.daily_rate, description = EXCLUDED.description;

INSERT INTO qt_wage_rate (year, grade_name, daily_rate, monthly_rate, hourly_rate, description)
VALUES (2026, '항공정비사', 288757, 0, 0, '측량업체-기타')
ON CONFLICT (year, grade_name) DO UPDATE SET daily_rate = EXCLUDED.daily_rate, description = EXCLUDED.description;
