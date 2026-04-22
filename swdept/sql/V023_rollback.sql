-- ============================================================
-- V023 Rollback: inspect-category-master
-- 사용 시점: V023 적용 후 전체 롤백 필요할 때
-- ============================================================

BEGIN;

-- 1) NOT NULL 제약 해제
ALTER TABLE inspect_template ALTER COLUMN category DROP NOT NULL;

-- 2) FK 제거
ALTER TABLE inspect_template      DROP CONSTRAINT IF EXISTS fk_it_category;
ALTER TABLE inspect_check_result  DROP CONSTRAINT IF EXISTS fk_icr_category;

-- 3) 마스터 테이블 제거
DROP TABLE IF EXISTS check_category_mst;

-- 4) Phase 1 역변환 (선택, 정식형 → 변형 복원이 필요할 때만)
-- UPDATE inspect_template     SET category='GeoNURIS GeoWeb Server(GWS)'  WHERE category='GeoNURIS GeoWeb Server (GWS)';
-- UPDATE inspect_template     SET category='GeoNURIS Spatial Server(GSS)' WHERE category='GeoNURIS Spatial Server (GSS)';
-- UPDATE inspect_check_result SET category='GeoNURIS GeoWeb Server(GWS)'  WHERE category='GeoNURIS GeoWeb Server (GWS)';
-- UPDATE inspect_check_result SET category='GeoNURIS Spatial Server(GSS)' WHERE category='GeoNURIS Spatial Server (GSS)';

COMMIT;
