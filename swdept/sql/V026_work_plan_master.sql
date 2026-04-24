-- ============================================================
-- V026: tb_work_plan 마스터 2개 + FK (S16 옵션 B)
-- Sprint: tb-work-plan-decision (2026-04-22)
-- 근거: docs/design-docs/tb-work-plan-decision.md v2.1
--       docs/exec-plans/tb-work-plan-decision.md v2
-- 멱등성: CREATE IF NOT EXISTS / ON CONFLICT / FK 존재검사
-- drift 방어: Phase 4 seed label+color VALUES 비교
-- ============================================================

BEGIN;

-- Phase 0: DB 값이 Enum 집합 내
DO $$
DECLARE bad_t bigint; bad_s bigint;
BEGIN
  SELECT COUNT(*) INTO bad_t FROM tb_work_plan
   WHERE plan_type NOT IN ('CONTRACT','INSTALL','PATCH','INSPECT','PRE_CONTACT',
                           'FAULT','SUPPORT','SETTLE','COMPLETE','ETC');
  IF bad_t > 0 THEN RAISE EXCEPTION 'HALT Phase 0: 예상 외 plan_type %', bad_t; END IF;

  SELECT COUNT(*) INTO bad_s FROM tb_work_plan
   WHERE status NOT IN ('PLANNED','CONTACTED','CONFIRMED','IN_PROGRESS',
                        'COMPLETED','POSTPONED','CANCELLED');
  IF bad_s > 0 THEN RAISE EXCEPTION 'HALT Phase 0: 예상 외 status %', bad_s; END IF;
  RAISE NOTICE 'Phase 0 PASS';
END $$ LANGUAGE plpgsql;

-- Phase 1: 마스터 CREATE + 시드
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

INSERT INTO work_plan_type_mst (type_code, type_label, color, display_order) VALUES
  ('CONTRACT',   '계약',     '#1565c0', 1),
  ('INSTALL',    '설치',     '#2e7d32', 2),
  ('PATCH',      '패치',     '#00897b', 3),
  ('INSPECT',    '점검',     '#ff9800', 4),
  ('PRE_CONTACT','사전연락', '#9e9e9e', 5),
  ('FAULT',      '장애처리', '#e74a3b', 6),
  ('SUPPORT',    '업무지원', '#6a1b9a', 7),
  ('SETTLE',     '기성/준공','#5c6bc0', 8),
  ('COMPLETE',   '준공',     '#37474f', 9),
  ('ETC',        '기타',     '#795548',10)
ON CONFLICT (type_code) DO NOTHING;

INSERT INTO work_plan_status_mst (status_code, status_label, display_order) VALUES
  ('PLANNED',     '예정',     1),
  ('CONTACTED',   '연락완료', 2),
  ('CONFIRMED',   '확정',     3),
  ('IN_PROGRESS', '진행중',   4),
  ('COMPLETED',   '완료',     5),
  ('POSTPONED',   '연기',     6),
  ('CANCELLED',   '취소',     7)
ON CONFLICT (status_code) DO NOTHING;

-- Phase 2: Exit Gate — tb_work_plan 값 정합성
DO $$
DECLARE bad bigint;
BEGIN
  SELECT COUNT(*) INTO bad FROM tb_work_plan
   WHERE plan_type NOT IN (SELECT type_code FROM work_plan_type_mst)
      OR status    NOT IN (SELECT status_code FROM work_plan_status_mst);
  IF bad > 0 THEN RAISE EXCEPTION 'HALT Phase 2: 마스터 외 값 %', bad; END IF;
  RAISE NOTICE 'Phase 2 Exit Gate PASS';
END $$ LANGUAGE plpgsql;

-- Phase 3: FK ADD (명시적 멱등, conrelid 한정)
DO $$ BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint
     WHERE conname='fk_twp_type' AND conrelid='tb_work_plan'::regclass
  ) THEN
    ALTER TABLE tb_work_plan
      ADD CONSTRAINT fk_twp_type
      FOREIGN KEY (plan_type) REFERENCES work_plan_type_mst(type_code);
  END IF;
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint
     WHERE conname='fk_twp_status' AND conrelid='tb_work_plan'::regclass
  ) THEN
    ALTER TABLE tb_work_plan
      ADD CONSTRAINT fk_twp_status
      FOREIGN KEY (status) REFERENCES work_plan_status_mst(status_code);
  END IF;
END $$ LANGUAGE plpgsql;

-- Phase 4: 최종 검증 (건수 + FK + seed label/color drift)
DO $$
DECLARE
  t bigint; s bigint; fk1 bigint; fk2 bigint;
  bad_label bigint; bad_color bigint;
BEGIN
  SELECT COUNT(*) INTO t FROM work_plan_type_mst;
  SELECT COUNT(*) INTO s FROM work_plan_status_mst;
  IF t <> 10 THEN RAISE EXCEPTION 'HALT final: type_mst=% (expected 10)', t; END IF;
  IF s <> 7  THEN RAISE EXCEPTION 'HALT final: status_mst=% (expected 7)', s; END IF;

  SELECT COUNT(*) INTO fk1 FROM pg_constraint
   WHERE conname='fk_twp_type' AND conrelid='tb_work_plan'::regclass;
  SELECT COUNT(*) INTO fk2 FROM pg_constraint
   WHERE conname='fk_twp_status' AND conrelid='tb_work_plan'::regclass;
  IF fk1 <> 1 OR fk2 <> 1 THEN
    RAISE EXCEPTION 'HALT final: FK missing (type=%, status=%)', fk1, fk2;
  END IF;

  -- v2: seed label drift 검증 (type)
  SELECT COUNT(*) INTO bad_label FROM (VALUES
      ('CONTRACT','계약'),('INSTALL','설치'),('PATCH','패치'),('INSPECT','점검'),
      ('PRE_CONTACT','사전연락'),('FAULT','장애처리'),('SUPPORT','업무지원'),
      ('SETTLE','기성/준공'),('COMPLETE','준공'),('ETC','기타')
    ) AS expected(code, lbl)
    LEFT JOIN work_plan_type_mst m ON m.type_code = expected.code
   WHERE m.type_label IS DISTINCT FROM expected.lbl;
  IF bad_label > 0 THEN RAISE EXCEPTION 'HALT final: type_mst label drift %', bad_label; END IF;

  -- seed color drift 검증
  SELECT COUNT(*) INTO bad_color FROM (VALUES
      ('CONTRACT','#1565c0'),('INSTALL','#2e7d32'),('PATCH','#00897b'),('INSPECT','#ff9800'),
      ('PRE_CONTACT','#9e9e9e'),('FAULT','#e74a3b'),('SUPPORT','#6a1b9a'),
      ('SETTLE','#5c6bc0'),('COMPLETE','#37474f'),('ETC','#795548')
    ) AS expected(code, clr)
    LEFT JOIN work_plan_type_mst m ON m.type_code = expected.code
   WHERE m.color IS DISTINCT FROM expected.clr;
  IF bad_color > 0 THEN RAISE EXCEPTION 'HALT final: type_mst color drift %', bad_color; END IF;

  -- seed status label drift 검증
  SELECT COUNT(*) INTO bad_label FROM (VALUES
      ('PLANNED','예정'),('CONTACTED','연락완료'),('CONFIRMED','확정'),
      ('IN_PROGRESS','진행중'),('COMPLETED','완료'),('POSTPONED','연기'),('CANCELLED','취소')
    ) AS expected(code, lbl)
    LEFT JOIN work_plan_status_mst m ON m.status_code = expected.code
   WHERE m.status_label IS DISTINCT FROM expected.lbl;
  IF bad_label > 0 THEN RAISE EXCEPTION 'HALT final: status_mst label drift %', bad_label; END IF;

  RAISE NOTICE 'PASS: S16 V026 applied (type=10, status=7, FK x 2, seed drift 0)';
END $$ LANGUAGE plpgsql;

COMMIT;
