-- ephemeral 전용 shim — phase2.sql 의 ON CONFLICT 가 전제하는 sprint migration
-- (V018 / V024 / V026) 의 CREATE TABLE 및 UNIQUE 만 추출. 본 sprint 들의 사전·사후 DO 블록,
-- BEGIN/COMMIT, DELETE, NOT NULL 전환, FK 등은 모두 제외 — testbed 가 phase2.sql 의 ON CONFLICT
-- 를 PG 가 받아주는 최소 상태만 만들어주는 목적.
--
-- 후속 sprint phase2-V018-init-ordering 이 phase2.sql 의 init-ordering 정정 완료 시 본 파일 제거 (exec plan v4).
-- ⚠ 본 파일은 src/test/resources/ephemeral/ 한정 — 제품 마이그레이션 / 운영 DB 적용 금지.
--
-- sprint: dbinitrunner-dollar-quote-aware (exec plan v3.2, 2026-04-29)
-- codex 3차 자문 옵션 D + 4차 검토 권고 (search_path) + 5차 자문 D-1+D-3 + 시행 단계 V024/V026 의존 발견 반영.

SET search_path = public;

-- V018_process_master_dedup.sql 의 (4) 절 UNIQUE
CREATE UNIQUE INDEX IF NOT EXISTS uq_tb_process_master_sysnm_process
  ON public.tb_process_master (sys_nm_en, process_name);

CREATE UNIQUE INDEX IF NOT EXISTS uq_service_purpose_sys_type_md5
  ON public.tb_service_purpose (sys_nm_en, purpose_type, md5(purpose_text));

-- V024_qt_category_master.sql 의 Phase 1 CREATE (FK 제외)
CREATE TABLE IF NOT EXISTS qt_category_mst (
  category_code  VARCHAR(10) PRIMARY KEY,
  category_label VARCHAR(50) NOT NULL,
  display_order  INT NOT NULL DEFAULT 0
);

-- V026_work_plan_master.sql 의 Phase 1 CREATE (FK 제외)
CREATE TABLE IF NOT EXISTS work_plan_type_mst (
  type_code     VARCHAR(20) PRIMARY KEY,
  type_label    VARCHAR(50) NOT NULL,
  color         VARCHAR(10) NOT NULL,
  display_order INT NOT NULL DEFAULT 0
);
CREATE TABLE IF NOT EXISTS work_plan_status_mst (
  status_code   VARCHAR(20) PRIMARY KEY,
  status_label  VARCHAR(50) NOT NULL,
  display_order INT NOT NULL DEFAULT 0
);
