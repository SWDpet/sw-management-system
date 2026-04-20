-- ============================================================
-- V021: users user_id=6 (박욱진) 마스킹 회귀 데이터 정정
-- Sprint: users-masking-regression-fix (S3-B, 2026-04-20)
-- 근거 기획서: docs/plans/users-masking-regression-fix.md (v4 승인)
-- 근거 개발계획: docs/dev-plans/users-masking-regression-fix.md (v2 승인)
--
-- 정정 매핑 (사용자 확정):
--   user_id=6 (ukjin914 박욱진)
--     tel: '070-****-8093' → '070-7113-8093'
--     mobile: '01030562678' → '010-3056-2678' (hyphen 통일)
--     email: 'u***@uitgis.com' → 'ukjin914@uitgis.com'
--
-- 멱등성: 정정 후 재실행은 expected_cnt=0, actual_cnt=0 PASS
-- ============================================================

BEGIN;

-- (0) 동명 백업 테이블 존재 시 즉시 실패 (롤백 신뢰성 보호)
DO $$
BEGIN
  IF to_regclass('public.users_v021_backup_:run_id') IS NOT NULL THEN
    RAISE EXCEPTION 'HALT: backup table users_v021_backup_:run_id already exists';
  END IF;
END $$ LANGUAGE plpgsql;

-- (1) 백업 (UPDATE 대상과 동일 범위 — 멱등성: 재실행 시 0건 백업)
CREATE TABLE users_v021_backup_:run_id AS
  SELECT user_id, userid, tel, mobile, email
    FROM users
   WHERE user_id = 6
     AND (tel LIKE '%*%' OR email LIKE '%*%' OR mobile = '01030562678');

-- (2) 게이트 + UPDATE + 동등 비교 (S5/S3 패턴)
DO $$
DECLARE
  expected_cnt bigint;
  actual_cnt   bigint;
  backup_cnt   bigint;
BEGIN
  SELECT COUNT(*) INTO expected_cnt FROM users
   WHERE user_id = 6
     AND (tel LIKE '%*%' OR email LIKE '%*%' OR mobile = '01030562678');

  SELECT COUNT(*) INTO backup_cnt FROM users_v021_backup_:run_id;
  IF backup_cnt <> expected_cnt THEN
    RAISE EXCEPTION 'HALT: backup(%) != expected(%) — race condition', backup_cnt, expected_cnt;
  END IF;

  WITH applied AS (
    UPDATE users SET
      tel = '070-7113-8093',
      mobile = '010-3056-2678',
      email = 'ukjin914@uitgis.com'
    WHERE user_id = 6
      AND (tel LIKE '%*%' OR email LIKE '%*%' OR mobile = '01030562678')
    RETURNING 1
  )
  SELECT COUNT(*) INTO actual_cnt FROM applied;

  IF actual_cnt <> expected_cnt THEN
    RAISE EXCEPTION 'HALT: updated(%) != expected(%) — ROLLBACK', actual_cnt, expected_cnt;
  END IF;

  RAISE NOTICE 'PASS: fixed % users (backup=users_v021_backup_:run_id)', actual_cnt;
END $$ LANGUAGE plpgsql;

-- (3) 사후 검증 — user_id=6 의 정정값 일치 확인 (idempotent: 재실행 시 동일 PASS)
DO $$
DECLARE
  cur_tel text;
  cur_mobile text;
  cur_email text;
BEGIN
  SELECT tel, mobile, email INTO cur_tel, cur_mobile, cur_email
    FROM users WHERE user_id = 6;

  IF cur_tel IS DISTINCT FROM '070-7113-8093'
     OR cur_mobile IS DISTINCT FROM '010-3056-2678'
     OR cur_email IS DISTINCT FROM 'ukjin914@uitgis.com' THEN
    RAISE EXCEPTION 'HALT post: user_id=6 not properly fixed (tel=%, mobile=%, email=%)',
      cur_tel, cur_mobile, cur_email;
  END IF;
END $$ LANGUAGE plpgsql;

COMMIT;
