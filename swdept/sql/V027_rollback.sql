-- ============================================================
-- V027 Rollback: UPIS 14개 메뉴 시드 제거
-- ============================================================
BEGIN;

DELETE FROM inspect_template
 WHERE template_type='UPIS' AND section='APP';

DELETE FROM check_category_mst
 WHERE section_code='APP'
   AND category_code IN ('도시계획','전자심의','지구단위계획','비정형','관리자','GIS 연동');

COMMIT;
