-- V028 Rollback
BEGIN;
ALTER TABLE inspect_report DROP COLUMN IF EXISTS key_findings;
ALTER TABLE inspect_report DROP COLUMN IF EXISTS recommendation_1;
ALTER TABLE inspect_report DROP COLUMN IF EXISTS recommendation_2;
ALTER TABLE inspect_report DROP COLUMN IF EXISTS recommendation_3;
ALTER TABLE inspect_report DROP COLUMN IF EXISTS followup_1;
ALTER TABLE inspect_report DROP COLUMN IF EXISTS followup_2;
ALTER TABLE inspect_report DROP COLUMN IF EXISTS followup_3;
ALTER TABLE inspect_report DROP COLUMN IF EXISTS next_schedule_note;
COMMIT;
