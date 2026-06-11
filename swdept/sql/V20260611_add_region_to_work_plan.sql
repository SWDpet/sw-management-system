-- ============================================================
-- [workplan-target-infra-cascade] tb_work_plan 미계약 대상 지역+시스템 컬럼 (additive, nullable)
-- 2026-06-11 · 기획서 docs/product-specs/workplan-target-infra-cascade.md (v0.3)
-- db_init_phase2.sql 의 동일 DDL 과 1:1 일치(멱등). FK 미설정 — 앱 검증(FR-11).
-- ============================================================

ALTER TABLE tb_work_plan ADD COLUMN IF NOT EXISTS region_code    VARCHAR(10);
ALTER TABLE tb_work_plan ADD COLUMN IF NOT EXISTS region_city_nm VARCHAR(40);
ALTER TABLE tb_work_plan ADD COLUMN IF NOT EXISTS region_dist_nm VARCHAR(40);
ALTER TABLE tb_work_plan ADD COLUMN IF NOT EXISTS target_sys_nm  VARCHAR(100);

CREATE INDEX IF NOT EXISTS idx_work_plan_region_code ON tb_work_plan(region_code);

COMMENT ON COLUMN tb_work_plan.region_code   IS '대상 시군구코드(sigungu_code.adm_sect_c). 계약·미계약 모두 채움 — workplan-target-infra-cascade (2026-06-11)';
COMMENT ON COLUMN tb_work_plan.region_city_nm IS '대상 시도명(표시·통계 denormalize)';
COMMENT ON COLUMN tb_work_plan.region_dist_nm IS '대상 시군구명(표시·통계 denormalize)';
COMMENT ON COLUMN tb_work_plan.target_sys_nm IS '대상 시스템명. 계약=Infra.sys_nm 복사, 미계약=직접입력';

-- 검증: 4컬럼 + 인덱스 존재 확인
-- SELECT column_name FROM information_schema.columns WHERE table_name='tb_work_plan' AND (column_name LIKE 'region_%' OR column_name='target_sys_nm');
