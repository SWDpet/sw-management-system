-- ============================================================
-- V028: inspect_report 에 발견사항·권고사항·후속조치·차회일정 컬럼 추가
-- Sprint: inspection-report-d-v5 (2026-05-15) — Phase C
-- 근거: 시안D v5 09 NEXT ROUND 섹션 + summary.findings 본문 — 수동 입력 흡수.
-- 멱등성: ADD COLUMN IF NOT EXISTS
-- ============================================================
BEGIN;

ALTER TABLE inspect_report ADD COLUMN IF NOT EXISTS key_findings       TEXT;
ALTER TABLE inspect_report ADD COLUMN IF NOT EXISTS recommendation_1   TEXT;
ALTER TABLE inspect_report ADD COLUMN IF NOT EXISTS recommendation_2   TEXT;
ALTER TABLE inspect_report ADD COLUMN IF NOT EXISTS recommendation_3   TEXT;
ALTER TABLE inspect_report ADD COLUMN IF NOT EXISTS followup_1         TEXT;
ALTER TABLE inspect_report ADD COLUMN IF NOT EXISTS followup_2         TEXT;
ALTER TABLE inspect_report ADD COLUMN IF NOT EXISTS followup_3         TEXT;
ALTER TABLE inspect_report ADD COLUMN IF NOT EXISTS next_schedule_note VARCHAR(300);

DO $$
DECLARE col_cnt int;
BEGIN
  SELECT COUNT(*) INTO col_cnt FROM information_schema.columns
   WHERE table_schema='public' AND table_name='inspect_report'
     AND column_name IN ('key_findings','recommendation_1','recommendation_2','recommendation_3',
                          'followup_1','followup_2','followup_3','next_schedule_note');
  IF col_cnt <> 8 THEN
    RAISE EXCEPTION 'HALT V028: 신규 컬럼 누락 (cnt=%, expected 8)', col_cnt;
  END IF;
  RAISE NOTICE 'PASS V028: inspect_report +8 columns (findings/recommendations/followups/schedule)';
END $$ LANGUAGE plpgsql;

COMMIT;
