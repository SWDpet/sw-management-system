-- ============================================================
-- V029 rollback: inspect-report-d-v6 Phase A
-- inspect_metric_snapshot 테이블 + 인덱스 + FK 제거
--
-- 사용 시점: v6 시계열 메트릭 적재 중단 필요할 때.
-- 데이터: ON DELETE CASCADE 로 자동 제거 (FK 참조 없음, 단방향 의존).
-- 영향: InspectionQrBatchService.saveMetricSnapshot() 호출 시 테이블 부재로 실패 가능 —
--       서비스 코드 변경(Phase C)도 함께 revert 필요.
-- ============================================================
BEGIN;

DROP TABLE IF EXISTS inspect_metric_snapshot;

-- 검증
DO $$
DECLARE tbl_cnt int;
BEGIN
  SELECT COUNT(*) INTO tbl_cnt FROM information_schema.tables
   WHERE table_schema='public' AND table_name='inspect_metric_snapshot';
  IF tbl_cnt <> 0 THEN RAISE EXCEPTION 'HALT V029_rollback: table still exists'; END IF;
  RAISE NOTICE 'PASS V029_rollback: inspect_metric_snapshot dropped';
END $$ LANGUAGE plpgsql;

COMMIT;
