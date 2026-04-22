-- ============================================================
-- V025: DROP pjt_equip (S15)
-- Sprint: pjt-equip-decision (2026-04-22)
-- 근거: docs/plans/pjt-equip-decision.md v2.2 (사용자 최종승인)
--       docs/dev-plans/pjt-equip-decision.md v2 (사용자 최종승인)
-- 실측 (2026-04-22): row=0, 외부 FK 0
-- ============================================================

BEGIN;

-- Phase 0: 3중 의존 게이트
DO $$
DECLARE fk_cnt bigint; dep_cnt bigint; row_cnt bigint;
  v_cnt bigint; mv_cnt bigint; r_cnt bigint; t_cnt bigint;
  f_cnt bigint; seq_cnt bigint;
BEGIN
  -- (a) 외부 FK
  SELECT COUNT(*) INTO fk_cnt FROM information_schema.table_constraints tc
    JOIN information_schema.constraint_column_usage ccu USING (constraint_name, table_schema)
   WHERE tc.constraint_type='FOREIGN KEY' AND tc.table_schema='public'
     AND ccu.table_name='pjt_equip' AND tc.table_name<>'pjt_equip';
  IF fk_cnt > 0 THEN RAISE EXCEPTION 'HALT Phase 0: external FK %', fk_cnt; END IF;

  -- (b) 비-FK 의존 객체 전수 (view/matview/rule/trigger/function/sequence)
  SELECT COUNT(*) INTO v_cnt  FROM pg_views    WHERE schemaname='public' AND definition ILIKE '%pjt_equip%';
  SELECT COUNT(*) INTO mv_cnt FROM pg_matviews WHERE schemaname='public' AND definition ILIKE '%pjt_equip%';
  SELECT COUNT(*) INTO r_cnt  FROM pg_rewrite rw JOIN pg_class c ON rw.ev_class=c.oid
    JOIN pg_namespace n ON c.relnamespace=n.oid
    WHERE n.nspname='public' AND rw.ev_action::text ILIKE '%pjt_equip%' AND c.relname <> 'pjt_equip';
  SELECT COUNT(*) INTO t_cnt  FROM pg_trigger tg JOIN pg_class c ON tg.tgrelid=c.oid
    WHERE c.relname='pjt_equip' AND tg.tgisinternal=FALSE;
  SELECT COUNT(*) INTO f_cnt FROM pg_proc p
    JOIN pg_namespace n ON p.pronamespace=n.oid
    WHERE n.nspname='public' AND p.prosrc ILIKE '%pjt_equip%';
  -- sequence: pjt_equip 관련 SERIAL 시퀀스는 테이블 DROP 과 함께 자동 삭제되므로 경고만
  SELECT COUNT(*) INTO seq_cnt FROM pg_class c
    JOIN pg_namespace n ON c.relnamespace=n.oid
   WHERE c.relkind='S' AND n.nspname='public' AND c.relname LIKE 'pjt_equip%'
     AND NOT EXISTS (
       SELECT 1 FROM pg_depend d WHERE d.classid='pg_class'::regclass
         AND d.objid=c.oid AND d.deptype='a'  -- 'a' = auto-dep (SERIAL) 은 자동 삭제
     );
  dep_cnt := v_cnt + mv_cnt + r_cnt + t_cnt + f_cnt + seq_cnt;
  IF dep_cnt > 0 THEN
    RAISE EXCEPTION 'HALT Phase 0: dep view=%, matview=%, rule=%, trigger=%, function=%, seq=%',
      v_cnt, mv_cnt, r_cnt, t_cnt, f_cnt, seq_cnt;
  END IF;

  -- (c) row=0 배포 직전 안전망
  SELECT COUNT(*) INTO row_cnt FROM pjt_equip;
  IF row_cnt > 0 THEN RAISE EXCEPTION 'HALT Phase 0: pjt_equip rows % (expected 0)', row_cnt; END IF;

  RAISE NOTICE 'Phase 0 PASS: external FK 0, dep 0, rows 0';
END $$ LANGUAGE plpgsql;

-- Phase 1: DROP
DROP TABLE IF EXISTS pjt_equip;

-- Phase 2: 사후 검증
DO $$
DECLARE exists_cnt bigint;
BEGIN
  SELECT COUNT(*) INTO exists_cnt FROM information_schema.tables
   WHERE table_schema='public' AND table_name='pjt_equip';
  IF exists_cnt <> 0 THEN RAISE EXCEPTION 'HALT final: pjt_equip still exists'; END IF;
  RAISE NOTICE 'PASS: S15 V025 - pjt_equip dropped';
END $$ LANGUAGE plpgsql;

COMMIT;
