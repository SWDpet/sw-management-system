-- ============================================================
-- V029: inspect-report-d-v6 — 일일 시계열 메트릭 누적 저장소
-- Sprint: inspection-report-d-v6 (2026-05-17) — Phase A
-- 근거: docs/product-specs/inspection-report-d-v6.md §4 (신규 인프라)
--       docs/exec-plans/inspection-report-d-v6.md Phase A
--       DB팀 자문 반영 — FK + TIMESTAMPTZ + host_name/host_ip + CHECK + BRIN + 1년 TTL
--
-- 적재 경로: agent-windows / agent-unix snapshot.json
--   → InspectionQrBatchService.upload() 의 신규 단계 saveMetricSnapshot()
--   → inspect_metric_snapshot INSERT (server_role IN 'AP','DB' 만)
--
-- 조회: 30일 차트 (P5 메트릭 추이) — InspectMetricChartService 가 PNG 렌더
-- 멱등성: CREATE * IF NOT EXISTS
-- ============================================================
BEGIN;

CREATE TABLE IF NOT EXISTS inspect_metric_snapshot (
    snapshot_id     BIGSERIAL    PRIMARY KEY,
    pjt_id          BIGINT       NOT NULL
                                 REFERENCES sw_pjt(proj_id) ON DELETE CASCADE,
    server_role     VARCHAR(16)  NOT NULL,
    host_name       VARCHAR(64),
    host_ip         VARCHAR(45),
    collected_at    TIMESTAMPTZ  NOT NULL,
    cpu_pct         NUMERIC(5,2),
    mem_pct         NUMERIC(5,2),
    disk_pct        NUMERIC(5,2),
    raw_payload     JSONB,
    created_at      TIMESTAMPTZ  DEFAULT now() NOT NULL,
    CONSTRAINT ck_metric_server_role
        CHECK (server_role IN ('AP','DB')),
    CONSTRAINT uk_metric_pjt_role_host_time
        UNIQUE (pjt_id, server_role, COALESCE(host_name, ''), collected_at)
);

-- 시계열 조회 메인 인덱스 (30일 차트 쿼리)
CREATE INDEX IF NOT EXISTS idx_metric_pjt_role_time
    ON inspect_metric_snapshot (pjt_id, server_role, collected_at DESC);

-- BRIN 보조 인덱스 — 1년+ 누적 풀스캔 집계 비용 절감 (시계열 단조 증가에 최적)
CREATE INDEX IF NOT EXISTS idx_metric_collected_at_brin
    ON inspect_metric_snapshot USING BRIN (collected_at)
    WITH (pages_per_range = 32);

-- 코멘트
COMMENT ON TABLE  inspect_metric_snapshot               IS '일일 시계열 메트릭 누적 (agent snapshot.json, P5 30일 차트용)';
COMMENT ON COLUMN inspect_metric_snapshot.server_role   IS 'AP / DB (GIS 는 inspect_check_result 회차별 단일값)';
COMMENT ON COLUMN inspect_metric_snapshot.host_name     IS 'snapshot.host.hostname — 다중 호스트(이중화/RAC) 식별';
COMMENT ON COLUMN inspect_metric_snapshot.host_ip       IS 'snapshot.host.ip — IPv6 대비 45자';
COMMENT ON COLUMN inspect_metric_snapshot.collected_at  IS 'agent 측정 시각 (snapshot.taken_at, TZ 포함 ISO-8601)';
COMMENT ON COLUMN inspect_metric_snapshot.cpu_pct       IS 'CPU 사용률 % (NULL = 수집 실패)';
COMMENT ON COLUMN inspect_metric_snapshot.mem_pct       IS 'Memory 사용률 %';
COMMENT ON COLUMN inspect_metric_snapshot.disk_pct      IS 'Disk 사용률 % (worst drive)';
COMMENT ON COLUMN inspect_metric_snapshot.raw_payload   IS 'snapshot.json 원본 — 보조 진단용 (인덱스 없음)';

-- 검증 게이트 (V022/V028 패턴)
DO $$
DECLARE col_cnt int; idx_cnt int; fk_cnt int; chk_cnt int;
BEGIN
  SELECT COUNT(*) INTO col_cnt FROM information_schema.columns
   WHERE table_schema='public' AND table_name='inspect_metric_snapshot';
  IF col_cnt < 11 THEN RAISE EXCEPTION 'HALT V029: column count=% (need >=11)', col_cnt; END IF;

  SELECT COUNT(*) INTO idx_cnt FROM pg_indexes
   WHERE schemaname='public' AND tablename='inspect_metric_snapshot';
  IF idx_cnt < 2 THEN RAISE EXCEPTION 'HALT V029: index count=% (need >=2: PK + brin)', idx_cnt; END IF;

  SELECT COUNT(*) INTO fk_cnt FROM pg_constraint
   WHERE conrelid='public.inspect_metric_snapshot'::regclass AND contype='f';
  IF fk_cnt < 1 THEN RAISE EXCEPTION 'HALT V029: FK missing (sw_pjt)'; END IF;

  SELECT COUNT(*) INTO chk_cnt FROM pg_constraint
   WHERE conrelid='public.inspect_metric_snapshot'::regclass AND contype='c';
  IF chk_cnt < 1 THEN RAISE EXCEPTION 'HALT V029: CHECK constraint missing (server_role)'; END IF;

  RAISE NOTICE 'PASS V029: inspect_metric_snapshot (cols=%, idx=%, fk=%, chk=%)',
                col_cnt, idx_cnt, fk_cnt, chk_cnt;
END $$ LANGUAGE plpgsql;

COMMIT;
