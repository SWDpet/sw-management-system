# S10 inspect-category-master Step 1 — 사전검증 결과

- 실행시각: 2026-04-22
- DB: jdbc:postgresql://211.104.137.55:5881/SW_Dept
- 안전통제: READ ONLY, statement_timeout=10s

## (a) inspect_template.(section, category) 분포 (18 distinct, 70 rows)

| section | category | cnt |
|---------|----------|----:|
| AP   | H/W                              |  6 |
| AP   | OS                               |  5 |
| AP   | 보안                             |  1 |
| AP   | 성능                             |  2 |
| DB   | DATA 점검                        |  3 |
| DB   | 구성 점검                        |  5 |
| DB   | 네트워크                         |  3 |
| DB   | 로그                             |  2 |
| DB   | 성능 점검                        |  5 |
| DB   | 육안점검                         |  5 |
| DB   | 프로세서                         |  1 |
| DBMS | 오라클                           | 17 |
| GIS  | GeoNURIS Desktop Pro             |  2 |
| GIS  | GeoNURIS GeoWeb Server (GWS)     |  3 |
| GIS  | GeoNURIS GeoWeb Server(GWS)      |  3 |
| GIS  | GeoNURIS Spatial Server (GSS)    |  3 |
| GIS  | GeoNURIS Spatial Server(GSS)     |  3 |
| GIS  | 측량성과 프로그램                |  1 |

## (b) 공백 변형 2쌍 검출

| table | category | cnt |
|-------|----------|----:|
| inspect_template | GeoNURIS GeoWeb Server(GWS) | 3 |
| inspect_template | GeoNURIS Spatial Server(GSS) | 3 |
| inspect_check_result | _(없음)_ | 0 |

## (c) NULL 카운트

| table | null_cnt |
|-------|---------:|
| inspect_template      | 0 |
| inspect_check_result  | 0 |

## (d) inspect_check_result.(section, category) 분포

_(없음)_ — S1 TRUNCATE 후 0 rows

---

## Exit Gate 1 판정

✅ **PASS** — Step 2 V023 착수 가능

### 확정 정보
- inspect_template.category NULL = 0 ✅
- 공백 변형 총 6 rows (template만) → Phase 1 UPDATE 대상
- 공백 통일 후 distinct 16종 (NFR-1 정확 일치)

### check_category_mst 시드 확정값 (16행)

| # | section | category_code | category_label | display_order |
|---|---------|---------------|----------------|---------------|
| 1 | AP | H/W | H/W | 1 |
| 2 | AP | OS  | OS  | 2 |
| 3 | AP | 보안 | 보안 | 3 |
| 4 | AP | 성능 | 성능 | 4 |
| 5 | DB | DATA 점검 | DATA 점검 | 11 |
| 6 | DB | 구성 점검 | 구성 점검 | 12 |
| 7 | DB | 네트워크 | 네트워크 | 13 |
| 8 | DB | 로그 | 로그 | 14 |
| 9 | DB | 성능 점검 | 성능 점검 | 15 |
| 10 | DB | 육안점검 | 육안점검 | 16 |
| 11 | DB | 프로세서 | 프로세서 | 17 |
| 12 | DBMS | 오라클 | 오라클 | 21 |
| 13 | GIS | GeoNURIS Desktop Pro | GeoNURIS Desktop Pro | 31 |
| 14 | GIS | GeoNURIS GeoWeb Server (GWS) | GeoNURIS GeoWeb Server (GWS) | 32 |
| 15 | GIS | GeoNURIS Spatial Server (GSS) | GeoNURIS Spatial Server (GSS) | 33 |
| 16 | GIS | 측량성과 프로그램 | 측량성과 프로그램 | 34 |
