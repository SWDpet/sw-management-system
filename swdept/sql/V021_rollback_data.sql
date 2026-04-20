-- ============================================================
-- V021 ROLLBACK DATA — users 마스킹 회귀 정정 데이터 복원
-- Sprint: users-masking-regression-fix (S3-B)
-- 사용 시점: V021 적용 후 데이터만 롤백 필요할 때 (L2 절차)
--
-- 동작:
--  1. users_v021_backup_<run_id> 중 가장 최근 백업을 자동 탐색
--  2. 백업의 tel/mobile/email 값을 users 본 테이블로 RESTORE
--
-- 트랜잭션 보장: 단일 BEGIN/COMMIT, EXCEPTION 시 자동 ROLLBACK
-- ============================================================

BEGIN;

DO $$
DECLARE
  bk text;
  restored_cnt bigint;
BEGIN
  -- 가장 최근 백업 테이블 자동 탐색 (run_id 타임스탬프 기준 desc)
  SELECT 'users_v021_backup_' || (regexp_match(table_name, '_(\d{8}_\d{6})$'))[1]
    INTO bk
    FROM information_schema.tables
   WHERE table_name LIKE 'users_v021_backup_%'
   ORDER BY table_name DESC
   LIMIT 1;

  IF bk IS NULL THEN
    RAISE EXCEPTION 'HALT: no V021 backup table found — cannot rollback';
  END IF;

  RAISE NOTICE 'Using backup table: %', bk;

  EXECUTE format(
    'WITH applied AS (
       UPDATE users u SET tel = b.tel, mobile = b.mobile, email = b.email
         FROM %I b WHERE u.user_id = b.user_id
         RETURNING 1
     ) SELECT COUNT(*) FROM applied', bk
  ) INTO restored_cnt;

  RAISE NOTICE 'PASS: restored % rows from %', restored_cnt, bk;
END $$ LANGUAGE plpgsql;

COMMIT;
