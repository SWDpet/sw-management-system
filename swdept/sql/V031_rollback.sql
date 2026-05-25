-- V031 rollback: inspect_report soft delete
BEGIN;
DROP INDEX IF EXISTS idx_inspect_report_active;
ALTER TABLE inspect_report DROP COLUMN IF EXISTS deleted_at;
COMMIT;
