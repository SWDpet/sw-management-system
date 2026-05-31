-- ============================================================
-- 점검 site_code 신규 규칙 전환 마이그레이션 (site-setup-revamp, 2026-05-31)
-- ------------------------------------------------------------
-- 신 규칙: site_code = {adm_sect_c}_{sys_nm_en}  (예: 46810_UPIS)
-- 구 코드(gangjin 등)는 site_code_alias 로 보존 → findBySiteCode 미스 시 폴백 매핑.
-- 모두 멱등(idempotent): 이미 신코드면 UPDATE 0 rows.
-- 기획서: docs/product-specs/site-setup-revamp.md  (D4/D5)
-- ============================================================

-- 1) 별칭 컬럼 (db_init_phase2.sql 에도 동일 ALTER 가 있어 서버 기동 시 자동 적용)
ALTER TABLE sw_pjt ADD COLUMN IF NOT EXISTS site_code_alias VARCHAR(64);

-- 2) 강진군 UPIS (proj 108): gangjin -> 46810_UPIS, 구 코드는 별칭 보존
--    (전라남도 강진군 adm_sect_c=46810, sys_nm_en=UPIS)
UPDATE sw_pjt
   SET site_code_alias = COALESCE(site_code_alias, site_code),
       site_code       = '46810_UPIS'
 WHERE proj_id = 108
   AND site_code = 'gangjin';

-- 3) (참고) 향후 신규/기존 사업도 동일 규칙으로 site_code 설정:
--    UPDATE sw_pjt SET site_code = adm || '_' || sys_nm_en ...  (사업관리 화면 자동생성 버튼 후속)

-- 확인:
-- SELECT proj_id, city_nm, dist_nm, sys_nm_en, site_code, site_code_alias
--   FROM sw_pjt WHERE site_code IS NOT NULL;
