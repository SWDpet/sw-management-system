-- ============================================================
-- V023: inspect-check-result-category-master (S10)
-- Sprint: inspect-check-result-category-master (2026-04-22)
-- 근거: docs/plans/inspect-check-result-category-master.md v2.1 (사용자 최종승인)
--       docs/dev-plans/inspect-check-result-category-master.md v2.1 (사용자 최종승인)
-- Step 1 Precheck: docs/dev-plans/inspect-category-precheck-result.md (PASS, 16종 확정)
--
-- 6 Phase 구조 + Exit Gate 2 + 멱등성 (IF NOT EXISTS / ON CONFLICT / IF [NOT] EXISTS)
-- ============================================================

BEGIN;

-- ═══════════════════════════════════════════════════════════
-- Phase 0: NULL 금지 확인 (NOT NULL 전환 전제)
-- ═══════════════════════════════════════════════════════════
DO $$
DECLARE null_it bigint;
BEGIN
  SELECT COUNT(*) INTO null_it FROM inspect_template WHERE category IS NULL;
  IF null_it > 0 THEN
    RAISE EXCEPTION 'HALT Phase 0: inspect_template.category NULL %', null_it;
  END IF;
  RAISE NOTICE 'Phase 0 PASS: inspect_template.category NULL=0';
END $$ LANGUAGE plpgsql;

-- ═══════════════════════════════════════════════════════════
-- Phase 1: 공백 변형 → 공백 O 통일 (양 테이블, 멱등)
-- ═══════════════════════════════════════════════════════════
UPDATE inspect_template
   SET category = 'GeoNURIS GeoWeb Server (GWS)'
 WHERE category = 'GeoNURIS GeoWeb Server(GWS)';
UPDATE inspect_template
   SET category = 'GeoNURIS Spatial Server (GSS)'
 WHERE category = 'GeoNURIS Spatial Server(GSS)';

UPDATE inspect_check_result
   SET category = 'GeoNURIS GeoWeb Server (GWS)'
 WHERE category = 'GeoNURIS GeoWeb Server(GWS)';
UPDATE inspect_check_result
   SET category = 'GeoNURIS Spatial Server (GSS)'
 WHERE category = 'GeoNURIS Spatial Server(GSS)';

-- Phase 1 사후
DO $$
DECLARE c bigint;
BEGIN
  SELECT (SELECT COUNT(*) FROM inspect_template
           WHERE category IN ('GeoNURIS GeoWeb Server(GWS)','GeoNURIS Spatial Server(GSS)'))
       + (SELECT COUNT(*) FROM inspect_check_result
           WHERE category IN ('GeoNURIS GeoWeb Server(GWS)','GeoNURIS Spatial Server(GSS)'))
    INTO c;
  IF c <> 0 THEN RAISE EXCEPTION 'HALT Phase 1: 공백 변형 잔존 %', c; END IF;
  RAISE NOTICE 'Phase 1 PASS: 공백 변형 0';
END $$ LANGUAGE plpgsql;

-- ═══════════════════════════════════════════════════════════
-- Phase 2: check_category_mst 생성 + 16행 시드
-- ═══════════════════════════════════════════════════════════
CREATE TABLE IF NOT EXISTS check_category_mst (
  section_code   VARCHAR(20)  NOT NULL REFERENCES check_section_mst(section_code),
  category_code  VARCHAR(50)  NOT NULL,
  category_label VARCHAR(100) NOT NULL,
  display_order  INT          NOT NULL DEFAULT 0,
  PRIMARY KEY (section_code, category_code)
);

INSERT INTO check_category_mst (section_code, category_code, category_label, display_order) VALUES
  -- AP (4종)
  ('AP', 'H/W',                           'H/W',                           1),
  ('AP', 'OS',                            'OS',                            2),
  ('AP', '보안',                          '보안',                          3),
  ('AP', '성능',                          '성능',                          4),
  -- DB (7종)
  ('DB', 'DATA 점검',                     'DATA 점검',                     11),
  ('DB', '구성 점검',                     '구성 점검',                     12),
  ('DB', '네트워크',                      '네트워크',                      13),
  ('DB', '로그',                          '로그',                          14),
  ('DB', '성능 점검',                     '성능 점검',                     15),
  ('DB', '육안점검',                      '육안점검',                      16),
  ('DB', '프로세서',                      '프로세서',                      17),
  -- DBMS (1종)
  ('DBMS', '오라클',                      '오라클',                        21),
  -- GIS (4종, 공백 O 버전 고정)
  ('GIS', 'GeoNURIS Desktop Pro',          'GeoNURIS Desktop Pro',          31),
  ('GIS', 'GeoNURIS GeoWeb Server (GWS)',  'GeoNURIS GeoWeb Server (GWS)',  32),
  ('GIS', 'GeoNURIS Spatial Server (GSS)', 'GeoNURIS Spatial Server (GSS)', 33),
  ('GIS', '측량성과 프로그램',            '측량성과 프로그램',             34)
ON CONFLICT (section_code, category_code) DO NOTHING;

-- ═══════════════════════════════════════════════════════════
-- Phase 3: Exit Gate 2 — 양 테이블 (section, category) 마스터 정합성
-- ═══════════════════════════════════════════════════════════
DO $$
DECLARE bad_it bigint; bad_icr bigint;
BEGIN
  SELECT COUNT(*) INTO bad_it FROM inspect_template
   WHERE (section, category) NOT IN (SELECT section_code, category_code FROM check_category_mst);
  IF bad_it > 0 THEN
    RAISE EXCEPTION 'HALT Phase 3: inspect_template 불일치 % — 시드 보강 필요', bad_it;
  END IF;

  SELECT COUNT(*) INTO bad_icr FROM inspect_check_result
   WHERE category IS NOT NULL
     AND (section, category) NOT IN (SELECT section_code, category_code FROM check_category_mst);
  IF bad_icr > 0 THEN
    RAISE EXCEPTION 'HALT Phase 3: inspect_check_result 불일치 %', bad_icr;
  END IF;

  RAISE NOTICE 'Phase 3 Exit Gate 2 PASS: 양 테이블 마스터 정합';
END $$ LANGUAGE plpgsql;

-- ═══════════════════════════════════════════════════════════
-- Phase 4: FK ADD (복합키, 멱등)
-- ═══════════════════════════════════════════════════════════
DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='fk_it_category') THEN
    ALTER TABLE inspect_template
      ADD CONSTRAINT fk_it_category
      FOREIGN KEY (section, category)
      REFERENCES check_category_mst(section_code, category_code);
  END IF;
  IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='fk_icr_category') THEN
    ALTER TABLE inspect_check_result
      ADD CONSTRAINT fk_icr_category
      FOREIGN KEY (section, category)
      REFERENCES check_category_mst(section_code, category_code);
  END IF;
END $$ LANGUAGE plpgsql;

-- ═══════════════════════════════════════════════════════════
-- Phase 5: inspect_template.category NOT NULL 전환
-- ═══════════════════════════════════════════════════════════
DO $$
DECLARE null_it bigint;
BEGIN
  SELECT COUNT(*) INTO null_it FROM inspect_template WHERE category IS NULL;
  IF null_it > 0 THEN RAISE EXCEPTION 'HALT Phase 5: NOT NULL 불가 — NULL %', null_it; END IF;
END $$ LANGUAGE plpgsql;

ALTER TABLE inspect_template ALTER COLUMN category SET NOT NULL;

-- ═══════════════════════════════════════════════════════════
-- Phase 6: 최종 검증 (NFR-1: 정확히 16행)
-- ═══════════════════════════════════════════════════════════
DO $$
DECLARE mst_cnt bigint; fk_it bigint; fk_icr bigint; still_nullable bigint;
BEGIN
  SELECT COUNT(*) INTO mst_cnt FROM check_category_mst;
  IF mst_cnt <> 16 THEN RAISE EXCEPTION 'HALT final: mst=% (expected 16)', mst_cnt; END IF;

  SELECT COUNT(*) INTO fk_it  FROM pg_constraint WHERE conname='fk_it_category';
  SELECT COUNT(*) INTO fk_icr FROM pg_constraint WHERE conname='fk_icr_category';
  IF fk_it <> 1 OR fk_icr <> 1 THEN RAISE EXCEPTION 'HALT final: FK missing (it=%, icr=%)', fk_it, fk_icr; END IF;

  SELECT COUNT(*) INTO still_nullable FROM information_schema.columns
   WHERE table_schema='public' AND table_name='inspect_template'
     AND column_name='category' AND is_nullable='YES';
  IF still_nullable <> 0 THEN RAISE EXCEPTION 'HALT final: inspect_template.category still nullable'; END IF;

  RAISE NOTICE 'PASS: inspect-category-master V023 applied (mst=16, FK×2, NOT NULL)';
END $$ LANGUAGE plpgsql;

COMMIT;
