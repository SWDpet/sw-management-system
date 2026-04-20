-- ============================================================
-- V017: 레거시 계약 테이블 DROP (옵션 B 확장)
-- Sprint: legacy-contract-tables-drop (2026-04-20)
-- 근거: docs/plans/legacy-contract-tables-drop.md v2 + 사용자 확정 옵션 B
-- 범위:
--   1) tb_document.contract_id FK 제거 + 컬럼 DROP
--   2) tb_contract_participant.contract_id FK 제거 + 컬럼 DROP
--   3) tb_contract_target DROP (FK 의존 자식)
--   4) tb_contract DROP (부모)
-- 롤백: 8d79f82:swdept/sql/V100_work_plan_performance_tables.sql:50-93,113-128
-- ============================================================

BEGIN;

-- ------------------------------------------------------------
-- Step 1: 사전 안전장치 — 레코드 수 재확인
-- ------------------------------------------------------------
DO $$
DECLARE
  cnt_contract bigint := 0;
  cnt_target   bigint := 0;
BEGIN
  IF to_regclass('public.tb_contract') IS NOT NULL THEN
    SELECT COUNT(*) INTO cnt_contract FROM tb_contract;
    IF cnt_contract > 0 THEN
      RAISE EXCEPTION 'HALT: tb_contract has % rows (expected 0)', cnt_contract;
    END IF;
  END IF;
  IF to_regclass('public.tb_contract_target') IS NOT NULL THEN
    SELECT COUNT(*) INTO cnt_target FROM tb_contract_target;
    IF cnt_target > 0 THEN
      RAISE EXCEPTION 'HALT: tb_contract_target has % rows (expected 0)', cnt_target;
    END IF;
  END IF;
END
$$ LANGUAGE plpgsql;

-- ------------------------------------------------------------
-- Step 2: tb_document.contract_id FK + 컬럼 DROP
--   - Entity(Document.java)에 contract_id 필드 없음 확인 완료 (2026-04-20)
--   - 레거시 컬럼 (스키마 드리프트 잔존)
-- ------------------------------------------------------------
DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.table_constraints
     WHERE constraint_name = 'tb_document_contract_id_fkey'
       AND table_schema = 'public'
  ) THEN
    EXECUTE 'ALTER TABLE tb_document DROP CONSTRAINT tb_document_contract_id_fkey';
  END IF;
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
     WHERE table_schema = 'public' AND table_name = 'tb_document' AND column_name = 'contract_id'
  ) THEN
    EXECUTE 'ALTER TABLE tb_document DROP COLUMN contract_id';
  END IF;
END
$$ LANGUAGE plpgsql;

-- ------------------------------------------------------------
-- Step 3: tb_contract_participant.contract_id FK + 컬럼 DROP
--   - Entity(ContractParticipant.java) 는 proj_id 사용 (contract_id 미사용)
--   - 2026-04-19 감사 C3-3-2 "조치함" ERD만 변경, DB 스키마 드리프트 잔존
-- ------------------------------------------------------------
DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.table_constraints
     WHERE constraint_name = 'tb_contract_participant_contract_id_fkey'
       AND table_schema = 'public'
  ) THEN
    EXECUTE 'ALTER TABLE tb_contract_participant DROP CONSTRAINT tb_contract_participant_contract_id_fkey';
  END IF;
  -- 관련 INDEX 도 같이 제거 (V100:111 idx_participant_contract)
  IF EXISTS (
    SELECT 1 FROM pg_indexes
     WHERE schemaname = 'public' AND indexname = 'idx_participant_contract'
  ) THEN
    EXECUTE 'DROP INDEX idx_participant_contract';
  END IF;
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
     WHERE table_schema = 'public' AND table_name = 'tb_contract_participant' AND column_name = 'contract_id'
  ) THEN
    EXECUTE 'ALTER TABLE tb_contract_participant DROP COLUMN contract_id';
  END IF;
END
$$ LANGUAGE plpgsql;

-- ------------------------------------------------------------
-- Step 4: tb_contract_target DROP (FK 자식 먼저)
-- ------------------------------------------------------------
DROP TABLE IF EXISTS tb_contract_target;

-- ------------------------------------------------------------
-- Step 5: tb_contract DROP (부모)
-- ------------------------------------------------------------
DROP TABLE IF EXISTS tb_contract;

-- ------------------------------------------------------------
-- Step 6: 사후 검증
-- ------------------------------------------------------------
DO $$
DECLARE
  c_tbl    bigint;
  c_col_d  bigint;
  c_col_p  bigint;
BEGIN
  SELECT COUNT(*) INTO c_tbl FROM information_schema.tables
   WHERE table_schema = 'public' AND table_name IN ('tb_contract', 'tb_contract_target');
  IF c_tbl <> 0 THEN
    RAISE EXCEPTION 'HALT: post-check failed. Tables still exist: %', c_tbl;
  END IF;

  SELECT COUNT(*) INTO c_col_d FROM information_schema.columns
   WHERE table_schema = 'public' AND table_name = 'tb_document' AND column_name = 'contract_id';
  IF c_col_d <> 0 THEN
    RAISE EXCEPTION 'HALT: tb_document.contract_id column still exists';
  END IF;

  SELECT COUNT(*) INTO c_col_p FROM information_schema.columns
   WHERE table_schema = 'public' AND table_name = 'tb_contract_participant' AND column_name = 'contract_id';
  IF c_col_p <> 0 THEN
    RAISE EXCEPTION 'HALT: tb_contract_participant.contract_id column still exists';
  END IF;

  RAISE NOTICE 'PASS: all legacy contract artifacts dropped';
END
$$ LANGUAGE plpgsql;

COMMIT;
