#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
codes.json 추출기 — 폐쇄망 에이전트 setup GUI 용 코드 번들 생성.

setup GUI(폐쇄망)는 DB 에 접근할 수 없으므로, DB 연결이 되는 빌드/사무실 머신에서
본 스크립트로 sigungu_code + sys_mst 를 config/codes.json 으로 추출해 패키지에 동봉한다.
(기획서: docs/product-specs/site-setup-revamp.md §4)

사용:
    set DB_PASSWORD=...          # 또는 환경변수에 이미 설정
    python tools/export-codes.py
의존: psycopg2  (pip install psycopg2-binary)
"""
import os, sys, json, datetime
import psycopg2

DB = dict(
    host=os.environ.get("DB_HOST", "211.104.137.55"),
    port=int(os.environ.get("DB_PORT", "5881")),
    dbname=os.environ.get("DB_NAME", "SW_Dept"),
    user=os.environ.get("DB_USER", "postgres"),
    password=os.environ["DB_PASSWORD"],
)

OUT = os.path.join(os.path.dirname(__file__), "..", "config", "codes.json")

SQL_SIGUNGU = """
    SELECT sido_name, sgg_name, adm_sect_c
    FROM sigungu_code
    WHERE adm_sect_c IS NOT NULL
    ORDER BY sido_name, sgg_name
"""
SQL_SYSTEMS = "SELECT cd, nm FROM sys_mst ORDER BY nm"


def main():
    conn = psycopg2.connect(**DB)
    conn.set_client_encoding("UTF8")
    cur = conn.cursor()

    cur.execute(SQL_SIGUNGU)
    sigungu = [{"sido": s, "sgg": g, "adm": a} for (s, g, a) in cur.fetchall()]

    cur.execute(SQL_SYSTEMS)
    systems = [{"ko": nm, "en": cd} for (cd, nm) in cur.fetchall()]

    conn.close()

    data = {
        "schema": "codes/v1",
        "generated_at": datetime.date.today().isoformat(),
        "source": "SW_Dept.sigungu_code + sys_mst",
        "sigungu": sigungu,
        "systems": systems,
    }
    out = os.path.abspath(OUT)
    with open(out, "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=2)
    print("wrote %s (sigungu=%d, systems=%d)" % (out, len(sigungu), len(systems)))


if __name__ == "__main__":
    try:
        main()
    except KeyError:
        sys.exit("환경변수 DB_PASSWORD 가 필요합니다.")
