-- ============================================================
-- V020: qt_remarks_pattern.user_id FK 추가 + 7건 매핑 + 템플릿화
-- Sprint: qt-remarks-users-link (2026-04-20)
-- 근거: docs/design-docs/qt-remarks-users-link.md (v3 승인)
--       docs/exec-plans/qt-remarks-users-link.md (v2 승인)
-- 매핑:
--   1,4,5,6,7 → user_id=6 (ukjin914 박욱진)
--   2 → user_id=17 (leeds 이동수)
--   3 → user_id=16 (yeohj 여현정)
-- 멱등성: expected_cnt 동적 산정 (재실행 시 0=0 PASS)
-- ============================================================

BEGIN;

-- (0) 동명 백업 테이블 존재 시 즉시 실패
DO $$
BEGIN
  IF to_regclass('public.qt_remarks_pattern_v020_backup_:run_id') IS NOT NULL THEN
    RAISE EXCEPTION 'HALT: backup table qt_remarks_pattern_v020_backup_:run_id already exists';
  END IF;
END $$ LANGUAGE plpgsql;

-- (1) 컬럼/FK 추가 먼저 (백업 쿼리에서 user_id 참조 가능하도록 — 멱등)
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                 WHERE table_name='qt_remarks_pattern' AND column_name='user_id') THEN
    ALTER TABLE qt_remarks_pattern
      ADD COLUMN user_id BIGINT NULL,
      ADD CONSTRAINT fk_qt_remarks_pattern_user
        FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL;
  END IF;
END $$ LANGUAGE plpgsql;

-- (2) 백업 — UPDATE 대상과 동일 범위 (멱등성: 재실행 시 0건 백업)
CREATE TABLE qt_remarks_pattern_v020_backup_:run_id AS
  SELECT * FROM qt_remarks_pattern
   WHERE pattern_id IN (1,2,3,4,5,6,7)
     AND (user_id IS NULL OR content NOT LIKE '%{username}%');

-- (3) 게이트 + UPDATE + 동등 비교 (S5 패턴, 동적 expected)
DO $$
DECLARE
  expected_cnt bigint;
  actual_cnt   bigint;
  backup_cnt   bigint;
BEGIN
  SELECT COUNT(*) INTO expected_cnt FROM qt_remarks_pattern
   WHERE pattern_id IN (1,2,3,4,5,6,7)
     AND (user_id IS NULL OR content NOT LIKE '%{username}%');

  SELECT COUNT(*) INTO backup_cnt FROM qt_remarks_pattern_v020_backup_:run_id;
  IF backup_cnt <> expected_cnt THEN
    RAISE EXCEPTION 'HALT: backup(%) != expected(%) — race condition', backup_cnt, expected_cnt;
  END IF;

  WITH applied AS (
    UPDATE qt_remarks_pattern SET
      user_id = CASE pattern_id
        WHEN 1 THEN 6   WHEN 2 THEN 17  WHEN 3 THEN 16
        WHEN 4 THEN 6   WHEN 5 THEN 6   WHEN 6 THEN 6   WHEN 7 THEN 6
      END,
      content = CASE pattern_id
        WHEN 1 THEN '※ 담당자 : {dept_nm} {username} {position_title} (T.070-7113-8093  M.010-3056-2678  F.02-561-9792)'
        WHEN 2 THEN '※ 담당자 : {dept_nm} {username} {position_title} (T.070-7113-9894  M.010-9755-1316  F.053-817-9987  E-mail : leeds@uitgis.com)'
        WHEN 3 THEN '※ 담당자 : {dept_nm} {username} {position_title} (T.070-7113-8072  M.010-2815-0957  F.02-561-9792)'
        WHEN 4 THEN '1. 본 견적의 유효기간은 발급일로부터 30일 입니다.' || E'\n' ||
                    ' ※ 담당자 : {dept_nm} {username} {position_title} (T.070-7113-8093  M.010-3056-2678  F.02-561-9792)'
        WHEN 5 THEN '※ 무상하자보수 기간 : 구매일로부터 1년' || E'\n' ||
                    '※ 담당자 : {dept_nm} {username} {position_title} (T.070-7113-8093  M.010-3056-2678  F.02-561-9792)'
        WHEN 6 THEN '※ 상기 견적은 제품 공급가에 10% 요율을 적용한 금액입니다.' || E'\n' ||
                    '※ 담당자 : {dept_nm} {username} {position_title} (T.070-7113-8093  M.010-3056-2678  F.02-561-9792)'
        WHEN 7 THEN '※ 상기 제품은 조달청 디지털몰에 등록되어 있습니다.' || E'\n' ||
                    '※ 상기 견적은 제품 공급가에 16% 요율을 적용한 금액입니다.' || E'\n' ||
                    '※ 담당자 : {dept_nm} {username} {position_title} (T.070-7113-8093  M.010-3056-2678  F.02-561-9792)'
      END
    WHERE pattern_id IN (1,2,3,4,5,6,7)
      AND (user_id IS NULL OR content NOT LIKE '%{username}%')
    RETURNING 1
  )
  SELECT COUNT(*) INTO actual_cnt FROM applied;

  IF actual_cnt <> expected_cnt THEN
    RAISE EXCEPTION 'HALT: updated(%) != expected(%) — ROLLBACK', actual_cnt, expected_cnt;
  END IF;

  RAISE NOTICE 'PASS: linked % rows (backup=qt_remarks_pattern_v020_backup_:run_id)', actual_cnt;
END $$ LANGUAGE plpgsql;

-- (4) 사후 검증 — 7건 모두 user_id 매핑됨
DO $$
DECLARE missing bigint;
BEGIN
  SELECT COUNT(*) INTO missing FROM qt_remarks_pattern
   WHERE pattern_id IN (1,2,3,4,5,6,7) AND user_id IS NULL;
  IF missing <> 0 THEN
    RAISE EXCEPTION 'HALT post: % rows missing user_id', missing;
  END IF;
END $$ LANGUAGE plpgsql;

COMMIT;
