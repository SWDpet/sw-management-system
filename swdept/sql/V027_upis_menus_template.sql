-- ============================================================
-- V027: UPIS 14개 메뉴 점검항목 시드 (시안D v5 8장 APPLICATION)
-- Sprint: inspection-report-d-v5 (2026-05-15)
-- 근거: docs/report-drafts/점검내역서_시안D_v5.docx 8장 표 (분류/세부분류/점검내용)
--       memory: project_inspection_report_d.md — UPIS 14개 항목 수동입력
-- 멱등성:
--   - check_category_mst : ON CONFLICT DO NOTHING (PK = section_code,category_code)
--   - inspect_template   : (template_type, section, category, item_name) 중복 검사 후 INSERT
-- 영향: section='APP' 의 비어있던 카테고리에 6개 시드. inspect_template 14행 추가.
-- ============================================================

BEGIN;

-- Phase 0: 사전 검증 ─ check_section_mst 에 APP 존재 / inspect_template 멱등 키 가용
DO $$
DECLARE app_section_cnt bigint; existing_rows bigint;
BEGIN
  SELECT COUNT(*) INTO app_section_cnt FROM check_section_mst WHERE section_code='APP';
  IF app_section_cnt <> 1 THEN
    RAISE EXCEPTION 'HALT Phase 0: check_section_mst 에 section_code=APP 가 없음 (% rows)', app_section_cnt;
  END IF;

  SELECT COUNT(*) INTO existing_rows FROM inspect_template
   WHERE template_type='UPIS' AND section='APP';
  RAISE NOTICE 'Phase 0 PASS — APP section OK, 기존 UPIS/APP rows=%', existing_rows;
END $$ LANGUAGE plpgsql;

-- ═══════════════════════════════════════════════════════════
-- Phase 1: check_category_mst — section='APP' 카테고리 6개 시드
-- ═══════════════════════════════════════════════════════════
INSERT INTO check_category_mst (section_code, category_code, category_label, display_order) VALUES
  ('APP', '도시계획',     '도시계획',     41),
  ('APP', '전자심의',     '전자심의',     42),
  ('APP', '지구단위계획', '지구단위계획', 43),
  ('APP', '비정형',       '비정형',       44),
  ('APP', '관리자',       '관리자',       45),
  ('APP', 'GIS 연동',     'GIS 연동',     46)
ON CONFLICT (section_code, category_code) DO NOTHING;

-- ═══════════════════════════════════════════════════════════
-- Phase 2: inspect_template — UPIS/APP 14항목 시드
--   item_name = 세부분류, item_method = 점검내용
--   sort_order = 카테고리 display_order × 10 + 세부순번
-- ═══════════════════════════════════════════════════════════
INSERT INTO inspect_template (template_type, section, category, item_name, item_method, sort_order, use_yn)
SELECT * FROM (VALUES
  -- 도시계획 (5)
  ('UPIS', 'APP', '도시계획',     '조회/검색',           '도시계획 정보 조회 화면 진입 → 검색 결과 정상 표출 확인',          411, 'Y'),
  ('UPIS', 'APP', '도시계획',     'KRAS 연계',           'KRAS 연계 메뉴 클릭 → 연계 응답 정상 확인',                        412, 'Y'),
  ('UPIS', 'APP', '도시계획',     '토지이용계획확인서',  '토지이용계획확인서 발급 화면 진입 → 발급 정상 확인',               413, 'Y'),
  ('UPIS', 'APP', '도시계획',     '건축물대장',          '건축물대장 조회 → 정상 표출 확인',                                 414, 'Y'),
  ('UPIS', 'APP', '도시계획',     '통계',                '통계 메뉴 진입 → 차트/표 정상 표출 확인',                          415, 'Y'),
  -- 전자심의 (1)
  ('UPIS', 'APP', '전자심의',     '전자심의',            '전자심의 게시판 진입 → 정상 표출 및 첨부 접근 확인',               421, 'Y'),
  -- 지구단위계획 (1)
  ('UPIS', 'APP', '지구단위계획', '지구단위계획',        '지구단위계획 조회 메뉴 진입 → 결과 정상 표출',                     431, 'Y'),
  -- 비정형 (1)
  ('UPIS', 'APP', '비정형',       '비정형',              '비정형 메뉴 진입 → 정상 응답 및 작성/저장 동작 확인',              441, 'Y'),
  -- 관리자 (1)
  ('UPIS', 'APP', '관리자',       '관리자',              '관리자 로그인 → 관리 메뉴 정상 표출 및 사용자 조회 확인',          451, 'Y'),
  -- GIS 연동 (5)
  ('UPIS', 'APP', 'GIS 연동',     '지도요청',            '지도 표출 정상 (Tile/WMS 응답 확인)',                              461, 'Y'),
  ('UPIS', 'APP', 'GIS 연동',     '필지이동',            '필지 검색 → 필지 위치로 지도 이동 동작 확인',                      462, 'Y'),
  ('UPIS', 'APP', 'GIS 연동',     '하일라이팅',          '필지 선택 시 하일라이트(강조 표시) 동작 확인',                     463, 'Y'),
  ('UPIS', 'APP', 'GIS 연동',     '필지정보',            '필지 클릭 시 정보 팝업 정상 표출',                                 464, 'Y'),
  ('UPIS', 'APP', 'GIS 연동',     '이력정보',            '이력 조회 메뉴 동작 및 결과 정상 표출 확인',                       465, 'Y')
) AS src(template_type, section, category, item_name, item_method, sort_order, use_yn)
WHERE NOT EXISTS (
  SELECT 1 FROM inspect_template t
   WHERE t.template_type = src.template_type
     AND t.section       = src.section
     AND t.category      = src.category
     AND t.item_name     = src.item_name
);

-- ═══════════════════════════════════════════════════════════
-- Phase 3: 최종 검증 — 14행 적재 + FK 정합
-- ═══════════════════════════════════════════════════════════
DO $$
DECLARE
  inserted_cnt   bigint;
  fk_bad_cnt     bigint;
  category_cnt   bigint;
BEGIN
  SELECT COUNT(*) INTO inserted_cnt FROM inspect_template
   WHERE template_type='UPIS' AND section='APP';
  IF inserted_cnt < 14 THEN
    RAISE EXCEPTION 'HALT Phase 3: UPIS/APP rows=% (expected ≥14)', inserted_cnt;
  END IF;

  SELECT COUNT(*) INTO fk_bad_cnt FROM inspect_template t
   WHERE t.template_type='UPIS' AND t.section='APP'
     AND (t.section, t.category) NOT IN
         (SELECT section_code, category_code FROM check_category_mst);
  IF fk_bad_cnt > 0 THEN
    RAISE EXCEPTION 'HALT Phase 3: 카테고리 FK 불일치 % rows', fk_bad_cnt;
  END IF;

  SELECT COUNT(*) INTO category_cnt FROM check_category_mst WHERE section_code='APP';
  IF category_cnt < 6 THEN
    RAISE EXCEPTION 'HALT Phase 3: APP 카테고리=% (expected ≥6)', category_cnt;
  END IF;

  RAISE NOTICE 'PASS V027: UPIS/APP template_rows=%, category_rows=%', inserted_cnt, category_cnt;
END $$ LANGUAGE plpgsql;

COMMIT;
