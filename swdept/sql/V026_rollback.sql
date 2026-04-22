-- V026 Rollback
BEGIN;
ALTER TABLE tb_work_plan DROP CONSTRAINT IF EXISTS fk_twp_type;
ALTER TABLE tb_work_plan DROP CONSTRAINT IF EXISTS fk_twp_status;
DROP TABLE IF EXISTS work_plan_type_mst;
DROP TABLE IF EXISTS work_plan_status_mst;
COMMIT;
