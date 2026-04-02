-- ============================================================
-- V003: 품명 패턴 하위항목(sub_items) 데이터 추가
-- 실행 대상: PostgreSQL (192.168.10.194:5880 / SW_Dept)
-- 설명: 주요 패턴에 하위항목 세트를 추가하여
--       견적서 작성 시 자동 채움 기능 지원
-- ============================================================

-- GeoNURIS for KRAS v1.0 유지보수 (최다빈출 418건)
UPDATE qt_product_pattern
SET sub_items = '- 제품명 : GeoNURIS for KRAS v1.0
- GeoNURIS Desktop 1식
- GeoNURIS Server 1식
- 유지보수 기간 : 2026년 월 ~ 월
- 지원형태 : 연간유지보수
- 점검방법 : 원격/방문
- 긴급장애 : 24시간 이내 처리'
WHERE product_name = 'GeoNURIS for KRAS v1.0(GIS S/W) 유지보수';

-- GIS S/W (GeoNURIS) 유지보수 (209건)
UPDATE qt_product_pattern
SET sub_items = '- 제품명 : GIS S/W (GeoNURIS)
- GeoNURIS Desktop 1식
- GeoNURIS Server 1식
- 유지보수 기간 : 2026년 월 ~ 월
- 지원형태 : 연간유지보수
- 점검방법 : 원격/방문
- 긴급장애 : 24시간 이내 처리'
WHERE product_name = 'GIS S/W (GeoNURIS) 유지보수' AND category = '유지보수';

-- GIS S/W(GeoNURIS Suite) 제품 (22건)
UPDATE qt_product_pattern
SET sub_items = '- 제품명 : GeoNURIS Suite
- GeoNURIS Desktop License
- GeoNURIS Server License
- 설치 및 기술지원 포함'
WHERE product_name = 'GIS S/W(GeoNURIS Sutie)' AND category = '유지보수';

-- GIS S/W 유지보수 (15건)
UPDATE qt_product_pattern
SET sub_items = '- 유지보수 기간 : 2026년 월 ~ 월
- 지원형태 : 연간유지보수
- 점검방법 : 원격/방문
- 긴급장애 : 24시간 이내 처리'
WHERE product_name = 'GIS S/W 유지보수' AND category = '유지보수';

-- GIS S/W (GeoNURIS) 유지보수 (중앙) (11건)
UPDATE qt_product_pattern
SET sub_items = '- 제품명 : GIS S/W (GeoNURIS) - 중앙
- GeoNURIS Desktop 1식
- GeoNURIS Server 1식
- 유지보수 기간 : 2026년 월 ~ 월
- 지원형태 : 연간유지보수
- 점검방법 : 원격/방문'
WHERE product_name = 'GIS S/W (GeoNURIS) 유지보수 (중앙)' AND category = '유지보수';

-- GIS S/W (GeoNURIS) 유지보수 (광주) (10건)
UPDATE qt_product_pattern
SET sub_items = '- 제품명 : GIS S/W (GeoNURIS) - 광주
- GeoNURIS Desktop 1식
- GeoNURIS Server 1식
- 유지보수 기간 : 2026년 월 ~ 월
- 지원형태 : 연간유지보수
- 점검방법 : 원격/방문'
WHERE product_name = 'GIS S/W (GeoNURIS) 유지보수 (광주)' AND category = '유지보수';

-- GeoNURIS v3.0(GIS SW) 유지보수 (6건)
UPDATE qt_product_pattern
SET sub_items = '- 제품명 : GeoNURIS v3.0
- GeoNURIS Desktop 1식
- GeoNURIS Server 1식
- 유지보수 기간 : 2026년 월 ~ 월
- 지원형태 : 연간유지보수
- 점검방법 : 원격/방문'
WHERE product_name = 'GeoNURIS v3.0(GIS SW) 유지보수' AND category = '유지보수';

-- GIS S/W (GeoNURIS Suite) 유지보수 (6건)
UPDATE qt_product_pattern
SET sub_items = '- 제품명 : GeoNURIS Suite
- GeoNURIS Desktop 1식
- GeoNURIS Server 1식
- 유지보수 기간 : 2026년 월 ~ 월
- 지원형태 : 연간유지보수
- 점검방법 : 원격/방문'
WHERE product_name = 'GIS S/W (GeoNURIS Suite) 유지보수' AND category = '유지보수';

-- UPIS 서버 유지보수 (15건)
UPDATE qt_product_pattern
SET sub_items = '- 대상 서버 : DB서버, APP서버
- 유지보수 기간 : 2026년 월 ~ 월
- 지원형태 : 연간유지보수
- 점검방법 : 원격/방문
- 긴급장애 : 24시간 이내 처리'
WHERE product_name = 'UPIS 서버(DB, APP서버) 유지보수' AND category = '유지보수';

-- UPIS 표준시스템 재설치 (16건)
UPDATE qt_product_pattern
SET sub_items = '- UPIS 표준시스템 설치
- DB 마이그레이션
- 기능 테스트 및 검증
- 사용자 교육'
WHERE product_name = 'UPIS 표준시스템 재설치' AND category = '용역';

-- GeoNURIS 기술지원 (4건)
UPDATE qt_product_pattern
SET sub_items = '- 기술지원 기간 : 2026년 월 ~ 월
- 지원범위 : GeoNURIS 전 제품
- 지원형태 : 원격/방문
- 긴급장애 : 24시간 이내 처리'
WHERE product_name = 'GeoNURIS 기술지원' AND category = '유지보수';

-- DBMS 유지보수 (3건)
UPDATE qt_product_pattern
SET sub_items = '- DBMS 유지보수 기간 : 2026년 월 ~ 월
- 지원형태 : 연간유지보수
- 점검방법 : 원격/방문'
WHERE product_name = 'DBMS 유지보수' AND category = '유지보수';

-- 기초조사관리시스템(UPBSS) 유지보수 (2건)
UPDATE qt_product_pattern
SET sub_items = '- 제품명 : 기초조사관리시스템(UPBSS)
- 유지보수 기간 : 2026년 월 ~ 월
- 지원형태 : 연간유지보수
- 점검방법 : 원격/방문'
WHERE product_name = '기초조사관리시스템(UPBSS) 유지보수' AND category = '유지보수';

-- ============================================================
-- 확인 쿼리
-- ============================================================
-- SELECT product_name, sub_items FROM qt_product_pattern WHERE sub_items IS NOT NULL ORDER BY usage_count DESC;
