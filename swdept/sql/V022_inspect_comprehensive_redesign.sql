-- ============================================================
-- V022: inspect-comprehensive-redesign (S1)
-- Sprint: inspect-comprehensive-redesign (2026-04-21)
-- 근거: docs/product-specs/inspect-comprehensive-redesign.md v2.2 (사용자 최종승인)
--        docs/exec-plans/inspect-comprehensive-redesign.md v2.1 (사용자 최종승인)
-- Step 1 Precheck: docs/exec-plans/inspect-redesign-precheck-result.md (PASS)
--
-- 조치:
--   A1  inspect_check_result.result → result_code + result_text
--   A2  UPIS_SW → UPIS (3 테이블 22건)
--   A3  inspect_template.APP 28건 DELETE (opt1)
--   A4  inspect_report 점검자/확인자 FK 전환
--   A5  inspect_check_result.category TRIM
--   A6  check_section_mst 9행 + FK
--   FR-7 tb_inspect_checklist / tb_inspect_issue DROP
--   FR-8 inspect_* 4 테이블 TRUNCATE (tb_document_signature 유지)
--
-- 실행 전: :run_id 를 YYYYMMDD_HHMMSS 로 치환
--   sed "s/:run_id/$(date +%Y%m%d_%H%M%S)/g" V022_*.sql | psql ...
--
-- 멱등성: IF NOT EXISTS / ON CONFLICT DO NOTHING / IF [NOT] EXISTS COLUMN
-- ============================================================

BEGIN;

-- ═══════════════════════════════════════════════════════════
-- Phase 0. 사전 재확인 (Step 1 결과와 일치하는지)
-- ═══════════════════════════════════════════════════════════
DO $$
DECLARE upissw_cnt bigint; fk_cnt bigint;
BEGIN
  -- UPIS_SW 합계 = 22 (±5 허용)
  SELECT (SELECT COUNT(*) FROM inspect_template WHERE template_type='UPIS_SW')
       + (SELECT COUNT(*) FROM inspect_report  WHERE sys_type='UPIS_SW')
       + (SELECT COUNT(*) FROM tb_document     WHERE sys_type='UPIS_SW')
    INTO upissw_cnt;
  IF ABS(upissw_cnt - 22) > 5 THEN
    RAISE EXCEPTION 'HALT Phase 0: UPIS_SW 총 % (기준 22±5 초과)', upissw_cnt;
  END IF;

  -- 외부 FK 없음 확인 (CASCADE 미사용 전제)
  SELECT COUNT(*) INTO fk_cnt
    FROM information_schema.table_constraints tc
    JOIN information_schema.constraint_column_usage ccu
      ON tc.constraint_name = ccu.constraint_name AND tc.table_schema = ccu.table_schema
   WHERE tc.constraint_type='FOREIGN KEY' AND tc.table_schema='public'
     AND ccu.table_name IN ('inspect_report','inspect_check_result','inspect_visit_log','inspect_template',
                            'tb_inspect_checklist','tb_inspect_issue')
     AND tc.table_name NOT IN ('inspect_report','inspect_check_result','inspect_visit_log','inspect_template',
                               'tb_inspect_checklist','tb_inspect_issue');
  IF fk_cnt > 0 THEN
    RAISE EXCEPTION 'HALT Phase 0: 외부 FK % 건 발견 — Step 1 재검사 필요', fk_cnt;
  END IF;

  RAISE NOTICE 'Phase 0 PASS: UPIS_SW=%, 외부FK=0', upissw_cnt;
END $$ LANGUAGE plpgsql;

-- ═══════════════════════════════════════════════════════════
-- Phase 1. 스냅샷 백업 (롤백 자원)
-- ═══════════════════════════════════════════════════════════
DO $$
DECLARE bname text;
BEGIN
  -- 동명 백업 존재 시 HALT (S5 패턴)
  FOREACH bname IN ARRAY ARRAY[
    'inspect_report_backup_:run_id',
    'inspect_check_result_backup_:run_id',
    'inspect_visit_log_backup_:run_id',
    'inspect_template_backup_:run_id'
  ] LOOP
    IF to_regclass('public.' || bname) IS NOT NULL THEN
      RAISE EXCEPTION 'HALT Phase 1: backup table % already exists', bname;
    END IF;
  END LOOP;
END $$ LANGUAGE plpgsql;

CREATE TABLE inspect_report_backup_:run_id       AS SELECT * FROM inspect_report;
CREATE TABLE inspect_check_result_backup_:run_id AS SELECT * FROM inspect_check_result;
CREATE TABLE inspect_visit_log_backup_:run_id    AS SELECT * FROM inspect_visit_log;
CREATE TABLE inspect_template_backup_:run_id     AS SELECT * FROM inspect_template;

-- ═══════════════════════════════════════════════════════════
-- Phase 2. A6 check_section_mst 마스터 생성 (9행 C-opt1)
-- ═══════════════════════════════════════════════════════════
CREATE TABLE IF NOT EXISTS check_section_mst (
  section_code  VARCHAR(20) PRIMARY KEY,
  section_label VARCHAR(100) NOT NULL,
  section_group VARCHAR(20) NOT NULL DEFAULT 'CORE',
  display_order INT NOT NULL DEFAULT 0
);

INSERT INTO check_section_mst (section_code, section_label, section_group, display_order) VALUES
  ('DB',       'DB 점검',       'CORE',    1),
  ('AP',       'AP 점검',       'CORE',    2),
  ('DBMS',     'DBMS 운영',     'CORE',    3),
  ('GIS',      'GIS 엔진',      'CORE',    4),
  ('APP',      '응용프로그램',  'CORE',    5),
  ('DB_USAGE', 'DB 사용 현황',  'DETAIL', 11),
  ('AP_USAGE', 'AP 사용 현황',  'DETAIL', 12),
  ('DBMS_ETC', 'DBMS 기타',     'DETAIL', 13),
  ('APP_ETC',  'APP 기타',      'DETAIL', 14)
ON CONFLICT (section_code) DO NOTHING;

DO $$
DECLARE c bigint;
BEGIN
  SELECT COUNT(*) INTO c FROM check_section_mst;
  IF c <> 9 THEN RAISE EXCEPTION 'HALT Phase 2: check_section_mst count=%', c; END IF;
  RAISE NOTICE 'Phase 2 PASS: check_section_mst 9 rows';
END $$ LANGUAGE plpgsql;

-- ═══════════════════════════════════════════════════════════
-- Phase 3. A2 UPIS_SW → UPIS 통합
-- ═══════════════════════════════════════════════════════════
UPDATE inspect_template SET template_type = 'UPIS' WHERE template_type = 'UPIS_SW';
UPDATE inspect_report   SET sys_type      = 'UPIS' WHERE sys_type      = 'UPIS_SW';
UPDATE tb_document      SET sys_type      = 'UPIS' WHERE sys_type      = 'UPIS_SW';

DO $$
DECLARE c bigint;
BEGIN
  SELECT (SELECT COUNT(*) FROM inspect_template WHERE template_type='UPIS_SW')
       + (SELECT COUNT(*) FROM inspect_report  WHERE sys_type='UPIS_SW')
       + (SELECT COUNT(*) FROM tb_document     WHERE sys_type='UPIS_SW')
    INTO c;
  IF c <> 0 THEN RAISE EXCEPTION 'HALT Phase 3: UPIS_SW 잔존 %', c; END IF;
  RAISE NOTICE 'Phase 3 PASS: UPIS_SW 0건';
END $$ LANGUAGE plpgsql;

-- ═══════════════════════════════════════════════════════════
-- Phase 4. A3 APP 섹션 제거 (inspect_template 만, opt1)
-- ═══════════════════════════════════════════════════════════
DELETE FROM inspect_template WHERE section = 'APP';

DO $$
DECLARE c bigint;
BEGIN
  SELECT COUNT(*) INTO c FROM inspect_template WHERE section='APP';
  IF c <> 0 THEN RAISE EXCEPTION 'HALT Phase 4: inspect_template APP 잔존 %', c; END IF;
  RAISE NOTICE 'Phase 4 PASS: inspect_template APP 0건';
END $$ LANGUAGE plpgsql;

-- ═══════════════════════════════════════════════════════════
-- Phase 5. A5 category 공백 정규화
-- ═══════════════════════════════════════════════════════════
UPDATE inspect_check_result
   SET category = TRIM(category)
 WHERE category IS NOT NULL AND category <> TRIM(category);

-- Phase 5 개별 게이트 생략 (Phase 6 TRUNCATE 로 어차피 초기화)

-- ═══════════════════════════════════════════════════════════
-- Phase 6. Exit Gate 2 — FK ADD 사전검증 + FK 추가
-- ═══════════════════════════════════════════════════════════
DO $$
DECLARE bad_icr bigint; bad_it bigint;
BEGIN
  SELECT COUNT(*) INTO bad_icr FROM inspect_check_result
   WHERE section IS NULL OR section = '' OR section <> TRIM(section)
      OR section NOT IN (SELECT section_code FROM check_section_mst);
  IF bad_icr > 0 THEN
    RAISE EXCEPTION 'HALT Phase 6: inspect_check_result.section 불일치 % — FK ADD 불가', bad_icr;
  END IF;

  SELECT COUNT(*) INTO bad_it FROM inspect_template
   WHERE section IS NULL OR section = '' OR section <> TRIM(section)
      OR section NOT IN (SELECT section_code FROM check_section_mst);
  IF bad_it > 0 THEN
    RAISE EXCEPTION 'HALT Phase 6: inspect_template.section 불일치 % — FK ADD 불가', bad_it;
  END IF;

  RAISE NOTICE 'Phase 6 Exit Gate 2 PASS: section 정합성 OK';
END $$ LANGUAGE plpgsql;

-- FK 추가 (멱등: 이미 존재하면 skip)
DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='fk_icr_section') THEN
    ALTER TABLE inspect_check_result
      ADD CONSTRAINT fk_icr_section FOREIGN KEY (section) REFERENCES check_section_mst(section_code);
  END IF;
  IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='fk_it_section') THEN
    ALTER TABLE inspect_template
      ADD CONSTRAINT fk_it_section FOREIGN KEY (section) REFERENCES check_section_mst(section_code);
  END IF;
END $$ LANGUAGE plpgsql;

-- ═══════════════════════════════════════════════════════════
-- Phase 7. FR-8 TRUNCATE (inspect_* 4 테이블, CASCADE 미사용)
-- ═══════════════════════════════════════════════════════════
-- 내부 FK (visit_log→report, check_result→report) 때문에 한 구문에 함께 지정
-- (CASCADE는 외부 관계 안전을 위해 사용 안 함 — Phase 0 게이트로 외부 FK 0 확인됨)
TRUNCATE inspect_check_result, inspect_visit_log, inspect_report, inspect_template RESTART IDENTITY;

-- ═══════════════════════════════════════════════════════════
-- Phase 8. A1 result 분리
-- ═══════════════════════════════════════════════════════════
ALTER TABLE inspect_check_result ADD COLUMN IF NOT EXISTS result_code VARCHAR(20);
ALTER TABLE inspect_check_result ADD COLUMN IF NOT EXISTS result_text VARCHAR(500);
ALTER TABLE inspect_check_result DROP COLUMN IF EXISTS result;

-- ═══════════════════════════════════════════════════════════
-- Phase 9. A4 점검자/확인자 FK 전환
-- ═══════════════════════════════════════════════════════════
ALTER TABLE inspect_report ADD COLUMN IF NOT EXISTS insp_user_id    BIGINT;
ALTER TABLE inspect_report ADD COLUMN IF NOT EXISTS conf_ps_info_id BIGINT;

-- FK 제약 (멱등)
DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='fk_ir_insp_user') THEN
    ALTER TABLE inspect_report
      ADD CONSTRAINT fk_ir_insp_user FOREIGN KEY (insp_user_id) REFERENCES users(user_id);
  END IF;
  IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='fk_ir_conf_ps_info') THEN
    ALTER TABLE inspect_report
      ADD CONSTRAINT fk_ir_conf_ps_info FOREIGN KEY (conf_ps_info_id) REFERENCES ps_info(id);
  END IF;
END $$ LANGUAGE plpgsql;

-- 기존 문자열 컬럼 삭제 (테스트 단계 전제 + Phase 7 TRUNCATE 이후)
ALTER TABLE inspect_report DROP COLUMN IF EXISTS insp_name;
ALTER TABLE inspect_report DROP COLUMN IF EXISTS insp_company;
ALTER TABLE inspect_report DROP COLUMN IF EXISTS conf_name;
ALTER TABLE inspect_report DROP COLUMN IF EXISTS conf_org;

-- ═══════════════════════════════════════════════════════════
-- Phase 10. FR-7 DROP (Exit Gate 3)
-- ═══════════════════════════════════════════════════════════
DROP TABLE IF EXISTS tb_inspect_checklist;
DROP TABLE IF EXISTS tb_inspect_issue;
-- tb_document_signature 는 §3-E E-opt1 권장으로 유지

DO $$
DECLARE c bigint;
BEGIN
  SELECT COUNT(*) INTO c FROM information_schema.tables
   WHERE table_schema='public' AND table_name IN ('tb_inspect_checklist','tb_inspect_issue');
  IF c <> 0 THEN RAISE EXCEPTION 'HALT Phase 10: legacy 테이블 잔존 %', c; END IF;

  -- Signature 유지 확인
  SELECT COUNT(*) INTO c FROM information_schema.tables
   WHERE table_schema='public' AND table_name='tb_document_signature';
  IF c <> 1 THEN RAISE EXCEPTION 'HALT Phase 10: tb_document_signature 예상치 못한 상태 %', c; END IF;

  RAISE NOTICE 'Phase 10 Exit Gate 3 PASS: Checklist/Issue DROP, Signature 유지';
END $$ LANGUAGE plpgsql;

-- ═══════════════════════════════════════════════════════════
-- Phase 11. 최종 검증
-- ═══════════════════════════════════════════════════════════
DO $$
DECLARE
  mst_cnt bigint; fk_icr bigint; fk_it bigint;
  ir_new_cols bigint; ir_old_cols bigint;
  icr_new_cols bigint; icr_old_cols bigint;
BEGIN
  SELECT COUNT(*) INTO mst_cnt FROM check_section_mst;
  IF mst_cnt <> 9 THEN RAISE EXCEPTION 'HALT final: check_section_mst %', mst_cnt; END IF;

  SELECT COUNT(*) INTO fk_icr FROM pg_constraint WHERE conname='fk_icr_section';
  SELECT COUNT(*) INTO fk_it  FROM pg_constraint WHERE conname='fk_it_section';
  IF fk_icr <> 1 OR fk_it <> 1 THEN RAISE EXCEPTION 'HALT final: section FK missing'; END IF;

  SELECT COUNT(*) INTO ir_new_cols FROM information_schema.columns
   WHERE table_schema='public' AND table_name='inspect_report'
     AND column_name IN ('insp_user_id','conf_ps_info_id');
  SELECT COUNT(*) INTO ir_old_cols FROM information_schema.columns
   WHERE table_schema='public' AND table_name='inspect_report'
     AND column_name IN ('insp_name','insp_company','conf_name','conf_org');
  IF ir_new_cols <> 2 OR ir_old_cols <> 0 THEN
    RAISE EXCEPTION 'HALT final: inspect_report columns new=%/old=%', ir_new_cols, ir_old_cols;
  END IF;

  SELECT COUNT(*) INTO icr_new_cols FROM information_schema.columns
   WHERE table_schema='public' AND table_name='inspect_check_result'
     AND column_name IN ('result_code','result_text');
  SELECT COUNT(*) INTO icr_old_cols FROM information_schema.columns
   WHERE table_schema='public' AND table_name='inspect_check_result'
     AND column_name='result';
  IF icr_new_cols <> 2 OR icr_old_cols <> 0 THEN
    RAISE EXCEPTION 'HALT final: inspect_check_result columns new=%/old=%', icr_new_cols, icr_old_cols;
  END IF;

  RAISE NOTICE 'PASS: inspect-comprehensive-redesign V022 applied (backup=inspect_*_backup_:run_id)';
END $$ LANGUAGE plpgsql;

COMMIT;
