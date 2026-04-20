-- ============================================================
-- V018: tb_process_master / tb_service_purpose 중복 제거
-- Sprint: process-master-dedup (2026-04-20)
-- 근거: docs/plans/process-master-dedup.md v2 (사용자 최종승인)
-- 사전검증: FR-0 전체 PASS (process-master-precheck)
-- 절차:
--   (1) 트랜잭션 내 사전검증 DO 블록
--   (2) DELETE with MIN(PK) 보존 (purpose_text 원문 기준)
--   (3) NOT NULL 전환 (키 5 컬럼)
--   (4) UNIQUE 제약 / 표현식 UNIQUE INDEX 추가
--   (5) 사후검증 DO 블록 (COUNT + UNIQUE + NOT NULL 5컬럼)
-- 롤백: docs/dev-plans/process-master-dedup.md §3 R-SQL
-- ============================================================

BEGIN;

-- (1) 사전검증 재확인
DO $$
DECLARE
  dist_proc bigint; dist_purp bigint;
  null_cnt bigint; fk_cnt bigint;
BEGIN
  SELECT COUNT(DISTINCT (sys_nm_en || '|' || process_name)) INTO dist_proc FROM tb_process_master;
  SELECT COUNT(DISTINCT (sys_nm_en || '|' || purpose_type || '|' || md5(purpose_text))) INTO dist_purp FROM tb_service_purpose;
  IF dist_proc <> 5 THEN RAISE EXCEPTION 'HALT: tb_process_master distinct=% (expected 5)', dist_proc; END IF;
  IF dist_purp <> 5 THEN RAISE EXCEPTION 'HALT: tb_service_purpose distinct=% (expected 5)', dist_purp; END IF;

  SELECT (
    (SELECT COUNT(*) FROM tb_process_master WHERE sys_nm_en IS NULL OR process_name IS NULL) +
    (SELECT COUNT(*) FROM tb_service_purpose WHERE sys_nm_en IS NULL OR purpose_type IS NULL OR purpose_text IS NULL)
  ) INTO null_cnt;
  IF null_cnt <> 0 THEN RAISE EXCEPTION 'HALT: NULL in key columns: %', null_cnt; END IF;

  SELECT COUNT(*) INTO fk_cnt
    FROM information_schema.table_constraints tc
    JOIN information_schema.constraint_column_usage ccu
      ON tc.constraint_name = ccu.constraint_name AND tc.table_schema = ccu.table_schema
   WHERE tc.constraint_type = 'FOREIGN KEY'
     AND tc.table_schema = 'public'
     AND ccu.table_name IN ('tb_process_master', 'tb_service_purpose')
     AND tc.table_name NOT IN ('tb_process_master', 'tb_service_purpose');
  IF fk_cnt > 0 THEN RAISE EXCEPTION 'HALT: external FK count=%', fk_cnt; END IF;
END $$ LANGUAGE plpgsql;

-- (2) 중복 제거 (MIN(PK) 보존, purpose_text 원문 기준)
DELETE FROM tb_process_master
 WHERE process_id NOT IN (
   SELECT MIN(process_id) FROM tb_process_master GROUP BY sys_nm_en, process_name
 );

DELETE FROM tb_service_purpose
 WHERE purpose_id NOT IN (
   SELECT MIN(purpose_id) FROM tb_service_purpose GROUP BY sys_nm_en, purpose_type, purpose_text
 );

-- (3) NOT NULL 전환 (UNIQUE 전)
ALTER TABLE tb_process_master
  ALTER COLUMN sys_nm_en SET NOT NULL,
  ALTER COLUMN process_name SET NOT NULL;

ALTER TABLE tb_service_purpose
  ALTER COLUMN sys_nm_en SET NOT NULL,
  ALTER COLUMN purpose_type SET NOT NULL,
  ALTER COLUMN purpose_text SET NOT NULL;

-- (4) UNIQUE 제약 (tb_process_master) + 표현식 UNIQUE INDEX (tb_service_purpose)
DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uq_process_master_sys_name') THEN
    ALTER TABLE tb_process_master
      ADD CONSTRAINT uq_process_master_sys_name
      UNIQUE (sys_nm_en, process_name);
  END IF;
END $$ LANGUAGE plpgsql;

CREATE UNIQUE INDEX IF NOT EXISTS uq_service_purpose_sys_type_md5
  ON tb_service_purpose (sys_nm_en, purpose_type, md5(purpose_text));

-- (5) 사후검증 (COUNT + UNIQUE + NOT NULL 5컬럼)
DO $$
DECLARE
  c_proc bigint; c_purp bigint;
  uq_proc bigint; uq_purp bigint;
  nn_bad bigint;
BEGIN
  SELECT COUNT(*) INTO c_proc FROM tb_process_master;
  SELECT COUNT(*) INTO c_purp FROM tb_service_purpose;
  IF c_proc <> 5 THEN RAISE EXCEPTION 'HALT post: tb_process_master count=%', c_proc; END IF;
  IF c_purp <> 5 THEN RAISE EXCEPTION 'HALT post: tb_service_purpose count=%', c_purp; END IF;

  SELECT COUNT(*) INTO uq_proc FROM pg_constraint WHERE conname = 'uq_process_master_sys_name';
  SELECT COUNT(*) INTO uq_purp FROM pg_indexes WHERE indexname = 'uq_service_purpose_sys_type_md5';
  IF uq_proc <> 1 THEN RAISE EXCEPTION 'HALT post: UNIQUE constraint missing'; END IF;
  IF uq_purp <> 1 THEN RAISE EXCEPTION 'HALT post: UNIQUE INDEX missing'; END IF;

  SELECT COUNT(*) INTO nn_bad
    FROM information_schema.columns
   WHERE table_schema = 'public'
     AND ((table_name = 'tb_process_master' AND column_name IN ('sys_nm_en', 'process_name'))
       OR (table_name = 'tb_service_purpose' AND column_name IN ('sys_nm_en', 'purpose_type', 'purpose_text')))
     AND is_nullable <> 'NO';
  IF nn_bad <> 0 THEN RAISE EXCEPTION 'HALT post: NOT NULL missing on % key columns', nn_bad; END IF;

  RAISE NOTICE 'PASS: dedup + constraints + NOT NULL applied';
END $$ LANGUAGE plpgsql;

COMMIT;
