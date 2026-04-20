-- ============================================================
-- V019: access_logs.userid 오염 정제
-- Sprint: access-log-userid-fix (2026-04-20)
-- 근거 기획서: docs/plans/access-log-userid-fix.md (v2)
-- 근거 개발계획: docs/dev-plans/access-log-userid-fix.md (v2)
-- 사전검증 결과: docs/dev-plans/access-log-userid-precheck-result.md
--
-- 매핑 (사전검증 1:1 자동 매칭, 사용자 승인):
--   '관리자' (11건) → 'admin'
--   '박욱진' (16건) → 'ukjin914'
--   합계 27건
--
-- 안전장치:
--   1. 백업 테이블 자동 생성 (access_logs_cleanup_backup_<run_id>)
--   2. expected_cnt(27) == actual_cnt 검증, 불일치 시 EXCEPTION → 자동 ROLLBACK
--   3. 사후검증: 매핑 대상 잔존 0건 확인
--   4. 멱등성: 재실행 시 0건 UPDATE (WHERE userid NOT IN users)
--
-- 실행 방법:
--   sed "s/:run_id/$(date +%Y%m%d_%H%M%S)/g" V019_access_log_userid_cleanup.sql | psql ...
-- ============================================================

BEGIN;

-- (0) 동명 백업 테이블 존재 시 즉시 실패 (롤백 신뢰성 보호)
DO $$
BEGIN
  IF to_regclass('public.access_logs_cleanup_backup_:run_id') IS NOT NULL THEN
    RAISE EXCEPTION 'HALT: backup table access_logs_cleanup_backup_:run_id already exists';
  END IF;
END $$ LANGUAGE plpgsql;

-- (1) 실행 전 스냅샷 백업 (NFR-7) — UPDATE 대상과 동일 범위
CREATE TABLE access_logs_cleanup_backup_:run_id AS
  SELECT * FROM access_logs
   WHERE userid IN ('박욱진', '관리자')
     AND userid NOT IN (SELECT userid FROM users);

-- (2) 게이트 검증 + (3) UPDATE + (4) 동등 비교
DO $$
DECLARE
  expected_cnt bigint;
  actual_cnt   bigint;
  backup_cnt   bigint;
BEGIN
  -- expected: UPDATE 대상 범위와 동일 SELECT
  SELECT COUNT(*) INTO expected_cnt FROM access_logs
   WHERE userid IN ('박욱진', '관리자')
     AND userid NOT IN (SELECT userid FROM users);

  -- 백업 == expected (race condition 방지)
  SELECT COUNT(*) INTO backup_cnt FROM access_logs_cleanup_backup_:run_id;
  IF backup_cnt <> expected_cnt THEN
    RAISE EXCEPTION 'HALT: backup(%) != expected(%) — race condition', backup_cnt, expected_cnt;
  END IF;

  -- UPDATE (CTE + RETURNING 으로 actual count 정확 측정)
  WITH cleanup AS (
    UPDATE access_logs SET userid = CASE userid
      WHEN '박욱진' THEN 'ukjin914'
      WHEN '관리자' THEN 'admin'
    END
    WHERE userid IN ('박욱진', '관리자')
      AND userid NOT IN (SELECT userid FROM users)  -- 멱등성
    RETURNING 1
  )
  SELECT COUNT(*) INTO actual_cnt FROM cleanup;

  -- 동등 비교 게이트 — 불일치 시 EXCEPTION (부분 성공 커밋 금지)
  IF actual_cnt <> expected_cnt THEN
    RAISE EXCEPTION 'HALT: updated(%) != expected(%) — ROLLBACK', actual_cnt, expected_cnt;
  END IF;

  RAISE NOTICE 'PASS: cleaned % rows (exact match, backup=access_logs_cleanup_backup_:run_id)', actual_cnt;
END $$ LANGUAGE plpgsql;

-- (5) 사후 검증 — 매핑 대상 잔존 0건
DO $$
DECLARE remaining bigint;
BEGIN
  SELECT COUNT(*) INTO remaining FROM access_logs
   WHERE userid IN ('박욱진', '관리자')
     AND userid NOT IN (SELECT userid FROM users);
  IF remaining <> 0 THEN
    RAISE EXCEPTION 'HALT post: % orphan rows remain after UPDATE', remaining;
  END IF;
END $$ LANGUAGE plpgsql;

COMMIT;
