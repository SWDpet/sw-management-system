-- ============================================================
-- V030 rollback: inspect-report-d-v6 Phase 0
-- DBMS 17 (modern) 제거 + GIS sort 7~12 제거 → v5 시점 시드(db_init_phase2.sql) 로 복귀
--
-- 사용 시점: V030 적용 후 v6 작업 중단 필요할 때.
-- db_init_phase2.sql 의 v5 시드(DBMS 17 OS 수준 + GIS 6) 가 그대로 남아있다면 앱 재시작으로 복귀 가능.
-- 본 스크립트는 명시적 롤백용 — DBMS / GIS 둘 다 DELETE 만 수행, 재시드는 db_init_phase2.sql 또는 수동.
-- ============================================================
BEGIN;

-- DBMS 17 (v6 modern) 제거
DELETE FROM inspect_template WHERE template_type='UPIS' AND section='DBMS';

-- GIS sort 7~12 제거 (v6 신규 6 항목)
DELETE FROM inspect_template
 WHERE template_type='UPIS' AND section='GIS' AND sort_order BETWEEN 7 AND 12;

-- 검증
DO $$
DECLARE dbms_cnt int; gis_cnt int;
BEGIN
  SELECT COUNT(*) INTO dbms_cnt FROM inspect_template WHERE template_type='UPIS' AND section='DBMS';
  SELECT COUNT(*) INTO gis_cnt  FROM inspect_template WHERE template_type='UPIS' AND section='GIS';
  IF dbms_cnt <> 0 THEN RAISE EXCEPTION 'HALT V030_rollback: DBMS count=% (need 0)', dbms_cnt; END IF;
  IF gis_cnt > 6  THEN RAISE EXCEPTION 'HALT V030_rollback: GIS count=% (need <=6)', gis_cnt; END IF;
  RAISE NOTICE 'PASS V030_rollback: DBMS=0 (v5 db_init_phase2 재시드 대기), GIS=%', gis_cnt;
END $$ LANGUAGE plpgsql;

COMMIT;
