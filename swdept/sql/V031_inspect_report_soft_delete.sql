-- ============================================================
-- V031: inspection-report-d-v6 Phase J — inspect_report soft delete
-- Sprint: inspection-report-d-v6 (2026-05-25)
-- 근거: v6 요구사항 — 관리자(admin) 전용 삭제 + 복구 가능성 확보
--
-- 변경 사유:
--   hard delete → soft delete 전환. deleted_at 컬럼으로 논리 삭제 처리.
--   기존 쿼리는 WHERE deleted_at IS NULL 조건 추가로 필터링.
--
-- 멱등성: ADD COLUMN IF NOT EXISTS
-- ============================================================
BEGIN;

ALTER TABLE inspect_report ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ DEFAULT NULL;

COMMENT ON COLUMN inspect_report.deleted_at IS '논리 삭제 시각 (NULL=활성, NOT NULL=삭제됨). 관리자만 삭제 가능.';

-- 활성 레코드 조회 성능용 partial index
CREATE INDEX IF NOT EXISTS idx_inspect_report_active
    ON inspect_report (pjt_id, created_at DESC)
    WHERE deleted_at IS NULL;

-- ═══════════════════════════════════════════════════════════
-- Validation
-- ═══════════════════════════════════════════════════════════
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'inspect_report' AND column_name = 'deleted_at'
    ) THEN
        RAISE EXCEPTION 'V031 FAILED: deleted_at column not created';
    END IF;
    RAISE NOTICE 'V031 OK — inspect_report.deleted_at added';
END $$;

COMMIT;
