#!/usr/bin/env python3
"""
extract-phase1.py — 운영DB schema dump + 마스터 데이터에서 phase1 19테이블 DDL 생성.

출력:
  src/main/resources/db_init_phase1.sql           (DDL + 마스터 시드 ~54건)
  src/main/resources/db_init_phase1_sigungu.sql   (sigungu_code 279건)

부모→자식 순서 (FR-1-B):
  1. sigungu_code → 2. sys_mst → 3. prj_types → 4. maint_tp_mst →
  5. cont_stat_mst → 6. cont_frm_mst → 7. users → 8. ps_info →
  9. sw_pjt → 10. tb_infra_master → 11. tb_infra_server →
  12. tb_infra_software → 13. tb_infra_link_upis → 14. tb_infra_link_api →
  15. tb_infra_memo → 16. access_logs → 17. tb_pjt_target →
  18. tb_pjt_manpower_plan → 19. tb_pjt_schedule
"""
import re
import sys
from pathlib import Path

DUMP = Path("docs/references/snapshots/2026-04-27-prod-schema.sql")
MASTER_DATA = Path("docs/references/snapshots/2026-04-27-master-data.sql")
SIGUNGU_DATA = Path("docs/references/snapshots/2026-04-27-sigungu-data.sql")
OUT_PHASE1 = Path("src/main/resources/db_init_phase1.sql")
OUT_SIGUNGU = Path("src/main/resources/db_init_phase1_sigungu.sql")

PHASE1_TABLES = [
    "sigungu_code", "sys_mst", "prj_types", "maint_tp_mst",
    "cont_stat_mst", "cont_frm_mst", "users", "ps_info",
    "sw_pjt", "tb_infra_master", "tb_infra_server", "tb_infra_software",
    "tb_infra_link_upis", "tb_infra_link_api", "tb_infra_memo",
    "access_logs", "tb_pjt_target", "tb_pjt_manpower_plan", "tb_pjt_schedule",
]

# 마스터 5종의 PK (ON CONFLICT 키)
MASTER_PK = {
    "sys_mst": "cd",
    "prj_types": "cd",
    "cont_stat_mst": "cd",
    "cont_frm_mst": "cd",
    "maint_tp_mst": "cd",
}

dump_lines = DUMP.read_text(encoding="utf-8").splitlines()


def find_table_block(table):
    for i, line in enumerate(dump_lines):
        if line.startswith(f"CREATE TABLE public.{table} "):
            j = i
            while j < len(dump_lines) and not dump_lines[j].rstrip().endswith(");"):
                j += 1
            if j < len(dump_lines):
                return dump_lines[i:j+1]
    return None


def find_sequence_blocks(table):
    seq_blocks = []
    seq_names = set()
    for i, line in enumerate(dump_lines):
        m = re.match(r"^CREATE SEQUENCE public\.(\w+)$", line)
        if m and (m.group(1).startswith(f"{table}_") or m.group(1) == f"{table}_seq"):
            seq_names.add(m.group(1))
            j = i
            while j < len(dump_lines) and not dump_lines[j].rstrip().endswith(";"):
                j += 1
            if j < len(dump_lines):
                seq_blocks.append("\n".join(dump_lines[i:j+1]))
    owned_blocks = []
    for line in dump_lines:
        m = re.match(rf"^ALTER SEQUENCE public\.(\w+) OWNED BY public\.{table}\.\w+;$", line)
        if m:
            owned_blocks.append(line)
            seq_names.add(m.group(1))
    default_blocks = []
    i = 0
    while i < len(dump_lines):
        if dump_lines[i].startswith(f"ALTER TABLE ONLY public.{table} ALTER COLUMN"):
            if "SET DEFAULT nextval" in dump_lines[i]:
                default_blocks.append(dump_lines[i])
            elif i+1 < len(dump_lines) and "SET DEFAULT nextval" in dump_lines[i+1]:
                default_blocks.append(dump_lines[i] + " " + dump_lines[i+1].strip())
        i += 1
    return seq_blocks, owned_blocks, default_blocks, seq_names


def find_pk_unique_blocks(table):
    blocks = []
    i = 0
    while i < len(dump_lines):
        line = dump_lines[i]
        if line.startswith(f"ALTER TABLE ONLY public.{table}"):
            if i+1 < len(dump_lines) and "ADD CONSTRAINT" in dump_lines[i+1] and \
               ("PRIMARY KEY" in dump_lines[i+1] or "UNIQUE" in dump_lines[i+1]):
                blocks.append((line.replace("public.", ""), dump_lines[i+1]))
                i += 2
                continue
        i += 1
    return blocks


def find_fk_blocks(table):
    blocks = []
    i = 0
    while i < len(dump_lines):
        line = dump_lines[i]
        if line.startswith(f"ALTER TABLE ONLY public.{table}"):
            if i+1 < len(dump_lines) and "FOREIGN KEY" in dump_lines[i+1]:
                blocks.append((line.replace("public.", ""), dump_lines[i+1]))
                i += 2
                continue
        i += 1
    return blocks


def find_index_blocks(table):
    blocks = []
    for line in dump_lines:
        if re.search(rf"^CREATE (UNIQUE )?INDEX \w+ ON public\.{table} ", line):
            blocks.append(line)
    return blocks


def wrap_constraint_do(alter_line, constraint_line):
    """ALTER TABLE ONLY x; ADD CONSTRAINT y ...; → DO 블록으로 감싸 멱등 보장."""
    # constraint_line 예: "    ADD CONSTRAINT sigungu_code_pkey PRIMARY KEY (adm_sect_c);"
    full = f"{alter_line}\n{constraint_line}"
    return f"""DO $$ BEGIN
    {alter_line.strip()}
    {constraint_line.strip()}
EXCEPTION
    WHEN duplicate_object THEN NULL;
    WHEN duplicate_table THEN NULL;
END $$;"""


def parse_master_data():
    """master-data.sql 의 INSERT 문을 테이블별로 분류 + ON CONFLICT 추가.
    --column-inserts 형식: INSERT INTO public.<tbl> (col1, col2) VALUES (...);"""
    if not MASTER_DATA.exists():
        return {}
    text = MASTER_DATA.read_text(encoding="utf-8")
    out = {}
    for tbl, pk in MASTER_PK.items():
        out[tbl] = []
        pattern = re.compile(
            rf"^INSERT INTO public\.{tbl}\s*(\([^)]+\))?\s*VALUES\s*\((.*?)\);",
            re.MULTILINE)
        for m in pattern.finditer(text):
            cols = m.group(1) or ""
            values = m.group(2)
            cols_part = f" {cols}" if cols else ""
            out[tbl].append(f"INSERT INTO {tbl}{cols_part} VALUES ({values}) ON CONFLICT ({pk}) DO NOTHING;")
    return out


def parse_sigungu_data():
    """sigungu-data.sql 의 INSERT 문을 ON CONFLICT 추가."""
    if not SIGUNGU_DATA.exists():
        return []
    text = SIGUNGU_DATA.read_text(encoding="utf-8")
    out = []
    pattern = re.compile(
        r"^INSERT INTO public\.sigungu_code\s*(\([^)]+\))?\s*VALUES\s*\((.*?)\);",
        re.MULTILINE)
    for m in pattern.finditer(text):
        cols = m.group(1) or ""
        values = m.group(2)
        cols_part = f" {cols}" if cols else ""
        out.append(f"INSERT INTO sigungu_code{cols_part} VALUES ({values}) ON CONFLICT (adm_sect_c) DO NOTHING;")
    return out


def emit_phase1():
    out = []
    out.append("""-- ============================================================
-- SW Manager — DB 초기화 Phase 1 (기초 DDL)
-- ============================================================
-- 본 스크립트는 신규 환경에서 가장 먼저 실행되는 기초 DDL 입니다.
-- 실행 순서: phase1.sql → [phase1_sigungu.sql] → phase2.sql → V*.sql
--
-- 대상 PostgreSQL: 14+ (운영DB 실측 16.11)
-- 멱등성: CREATE TABLE/INDEX IF NOT EXISTS + ADD CONSTRAINT DO 블록 + ON CONFLICT
--
-- 라이선스 모듈은 영구 보류 — license_registry, license_upload_history,
-- qr_license, geonuris_license 는 본 파일에 정의되지 않음 (재개 시 별도 스프린트)
--
-- 19 테이블 (phase1):
--   1. sigungu_code  2. sys_mst        3. prj_types      4. maint_tp_mst
--   5. cont_stat_mst 6. cont_frm_mst   7. users          8. ps_info
--   9. sw_pjt        10. tb_infra_master  11. tb_infra_server  12. tb_infra_software
--   13. tb_infra_link_upis  14. tb_infra_link_api  15. tb_infra_memo
--   16. access_logs  17. tb_pjt_target  18. tb_pjt_manpower_plan  19. tb_pjt_schedule
--
-- 출처: 운영DB schema dump (docs/references/snapshots/2026-04-27-prod-schema.sql, SHA256 f3b2b51a...)
-- ============================================================

BEGIN;

-- =========================================================
-- Section 1: 테이블 + 시퀀스 + PK/UNIQUE 정의 (부모→자식 순서)
-- =========================================================

""")

    fk_all = []
    seq_seen = set()

    for t in PHASE1_TABLES:
        block = find_table_block(t)
        if not block:
            print(f"WARNING: {t} CREATE TABLE 블록 미발견", file=sys.stderr)
            continue
        out.append(f"-- ----- {t} -----\n")
        ct = "\n".join(block)
        ct = ct.replace(f"CREATE TABLE public.{t}", f"CREATE TABLE IF NOT EXISTS {t}", 1)
        out.append(ct + "\n\n")

        # SEQUENCE
        seqs, owned, defaults, seq_names = find_sequence_blocks(t)
        for sn in seq_names - seq_seen:
            seq_seen.add(sn)
        for s in seqs:
            s2 = re.sub(r"^CREATE SEQUENCE public\.(\w+)", r"CREATE SEQUENCE IF NOT EXISTS \1", s, flags=re.MULTILINE)
            out.append(s2 + "\n\n")
        for o in owned:
            o2 = o.replace("public.", "")
            # ALTER SEQUENCE ... OWNED BY 도 DO 블록 (이미 OWNED BY 면 에러 안 나지만 안전)
            out.append(o2 + "\n")
        if owned:
            out.append("\n")
        for d in defaults:
            d2 = d.replace("public.", "")
            out.append(d2 + "\n")
        if defaults:
            out.append("\n")

        # PK + UNIQUE (DO 블록으로 멱등화)
        pks = find_pk_unique_blocks(t)
        for alter_line, constraint_line in pks:
            out.append(wrap_constraint_do(alter_line, constraint_line) + "\n\n")

        # FK 는 후행 일괄 적용
        fks = find_fk_blocks(t)
        for alter_line, constraint_line in fks:
            fk_all.append((alter_line, constraint_line))

        # INDEX (CREATE INDEX IF NOT EXISTS)
        idxs = find_index_blocks(t)
        for idx in idxs:
            idx2 = idx.replace("public.", "")
            idx2 = idx2.replace("CREATE INDEX", "CREATE INDEX IF NOT EXISTS")
            idx2 = idx2.replace("CREATE UNIQUE INDEX IF NOT EXISTS", "CREATE UNIQUE INDEX IF NOT EXISTS")  # idempotent
            idx2 = idx2.replace("CREATE UNIQUE INDEX", "CREATE UNIQUE INDEX IF NOT EXISTS")
            out.append(idx2 + "\n")
        if idxs:
            out.append("\n")

    # === Section 2: FK 일괄 적용 (DO 블록) ===
    out.append("""-- =========================================================
-- Section 2: FK 제약 일괄 적용 (모든 부모 테이블 생성 후, 멱등화)
-- =========================================================

""")
    for alter_line, constraint_line in fk_all:
        out.append(wrap_constraint_do(alter_line, constraint_line) + "\n\n")

    # === Section 3: 마스터 시드 (운영DB 실데이터 기반) ===
    out.append("""-- =========================================================
-- Section 3: 마스터 시드 (운영DB 실데이터 기반, ~54건, ON CONFLICT DO NOTHING)
-- =========================================================
-- 운영DB 데이터와 충돌 없이 신규 환경에서만 적재됨.
-- 출처: 운영DB pg_dump --data-only (2026-04-27)

""")
    masters = parse_master_data()
    for tbl in ("sys_mst", "prj_types", "cont_stat_mst", "cont_frm_mst", "maint_tp_mst"):
        rows = masters.get(tbl, [])
        out.append(f"-- {tbl} ({len(rows)}건)\n")
        for ins in rows:
            out.append(ins + "\n")
        out.append("\n")

    out.append("COMMIT;\n")

    OUT_PHASE1.parent.mkdir(parents=True, exist_ok=True)
    OUT_PHASE1.write_text("".join(out), encoding="utf-8")
    print(f"생성 phase1.sql: {OUT_PHASE1} ({sum(len(s) for s in out)} bytes, {''.join(out).count(chr(10))} 라인)")


def emit_sigungu():
    out = []
    out.append("""-- ============================================================
-- SW Manager — sigungu_code 시드 (Phase 1 분리)
-- ============================================================
-- 충돌 정책: ON CONFLICT (adm_sect_c) DO NOTHING (멱등성 보장)
-- 로드 순서: phase1.sql 다음 (sigungu_code 테이블 정의 전제)
-- 데이터 출처: 운영DB pg_dump --data-only -t sigungu_code (2026-04-27, 279행)
-- ============================================================

BEGIN;

""")
    rows = parse_sigungu_data()
    out.append(f"-- sigungu_code ({len(rows)}건)\n")
    for ins in rows:
        out.append(ins + "\n")

    out.append("\nCOMMIT;\n")

    OUT_SIGUNGU.write_text("".join(out), encoding="utf-8")
    print(f"생성 sigungu.sql: {OUT_SIGUNGU} ({sum(len(s) for s in out)} bytes, {len(rows)}행)")


if __name__ == "__main__":
    emit_phase1()
    emit_sigungu()
