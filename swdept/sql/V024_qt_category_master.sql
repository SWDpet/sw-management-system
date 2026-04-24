-- ============================================================
-- V024: qt_quotation category 마스터 신설 (S8 옵션 B)
-- Sprint: qt-quotation-domain-normalize (2026-04-22)
-- 근거: docs/product-specs/qt-quotation-domain-normalize.md v2.1
--       docs/exec-plans/qt-quotation-domain-normalize.md v2.1
-- 멱등성: CREATE IF NOT EXISTS / ON CONFLICT / FK 존재검사
-- ============================================================

BEGIN;

-- Phase 0: 현황 재확인
DO $$
DECLARE bad bigint;
BEGIN
  SELECT COUNT(*) INTO bad FROM qt_quotation
   WHERE category NOT IN ('유지보수','용역','제품');
  IF bad > 0 THEN RAISE EXCEPTION 'HALT Phase 0: 예상 외 category %', bad; END IF;
  RAISE NOTICE 'Phase 0 PASS';
END $$ LANGUAGE plpgsql;

-- Phase 1: qt_category_mst CREATE + 3행 시드
CREATE TABLE IF NOT EXISTS qt_category_mst (
  category_code  VARCHAR(10) PRIMARY KEY,
  category_label VARCHAR(50) NOT NULL,
  display_order  INT NOT NULL DEFAULT 0
);

INSERT INTO qt_category_mst (category_code, category_label, display_order) VALUES
  ('유지보수', '유지보수', 1),
  ('용역',     '용역',     2),
  ('제품',     '제품',     3)
ON CONFLICT (category_code) DO NOTHING;

-- Phase 2: Exit Gate — qt_quotation.category 전부 마스터 존재
DO $$
DECLARE bad bigint;
BEGIN
  SELECT COUNT(*) INTO bad FROM qt_quotation
   WHERE category NOT IN (SELECT category_code FROM qt_category_mst);
  IF bad > 0 THEN RAISE EXCEPTION 'HALT Phase 2: 마스터 외 값 %', bad; END IF;
  RAISE NOTICE 'Phase 2 Exit Gate PASS';
END $$ LANGUAGE plpgsql;

-- Phase 3: FK ADD (명시적 멱등, 대상 테이블 한정)
DO $$ BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint
     WHERE conname='fk_qt_category'
       AND conrelid='qt_quotation'::regclass
  ) THEN
    ALTER TABLE qt_quotation
      ADD CONSTRAINT fk_qt_category
      FOREIGN KEY (category) REFERENCES qt_category_mst(category_code);
  END IF;
END $$ LANGUAGE plpgsql;

-- Phase 4: 최종 검증
DO $$
DECLARE mst_cnt bigint; fk_cnt bigint;
BEGIN
  SELECT COUNT(*) INTO mst_cnt FROM qt_category_mst;
  IF mst_cnt <> 3 THEN RAISE EXCEPTION 'HALT final: mst=% (expected 3)', mst_cnt; END IF;
  SELECT COUNT(*) INTO fk_cnt FROM pg_constraint
   WHERE conname='fk_qt_category' AND conrelid='qt_quotation'::regclass;
  IF fk_cnt <> 1 THEN RAISE EXCEPTION 'HALT final: fk_qt_category missing'; END IF;
  RAISE NOTICE 'PASS: S8 V024 applied (mst=3, FK x 1)';
END $$ LANGUAGE plpgsql;

COMMIT;
