# inspect-comprehensive-redesign Step 1 — 사전검증 결과

- 실행시각: Tue Apr 21 23:42:22 KST 2026
- DB: jdbc:postgresql://211.104.137.55:5881/SW_Dept
- 안전통제: READ ONLY, statement_timeout=10s

## (a) section 분포

### inspect_check_result.section
| section   | cnt |
|-----------|----:|
| AP        |  84 |
| AP_USAGE  |   4 |
| APP       |  98 |
| APP_ETC   |   1 |
| DB        | 144 |
| DB_USAGE  |   7 |
| DBMS      | 102 |
| DBMS_ETC  |   2 |
| GIS       |  51 |

**합계**: 493 rows / 9종 섹션 전부 등장

### inspect_template.section
| section | cnt |
|---------|----:|
| AP      |  14 |
| APP     |  28 |
| DB      |  24 |
| DBMS    |  17 |
| GIS     |  21 |

**합계**: 104 rows / 5종 (기본 CORE 섹션만, *_USAGE / *_ETC 없음)

## (b) UPIS_SW 건수

| 테이블 | count |
|--------|------:|
| inspect_template.template_type='UPIS_SW' | 20 |
| inspect_report.sys_type='UPIS_SW'        |  1 |
| tb_document.sys_type='UPIS_SW'           |  1 |

- **합계: 22** (로드맵 기록 22건과 **정확 일치**)

## (c) APP 섹션 건수

| 테이블 | count |
|--------|------:|
| inspect_template.section='APP'     | 28 |
| inspect_check_result.section='APP' | 98 |

## (d) 대상 테이블 row 수 (TRUNCATE 전 스냅샷)

| 테이블 | rows |
|--------|-----:|
| inspect_report        |  11 |
| inspect_check_result  | 493 |
| inspect_visit_log     |  14 |
| inspect_template      | 104 |
| tb_inspect_checklist  |   0 |
| tb_inspect_issue      |   0 |
| tb_document_signature |   0 |

## (e) 외부 FK 의존성 (DROP + TRUNCATE CASCADE 위험)

### e-1. Checklist/Issue DROP 영향
_(없음)_ — DROP 안전

### e-2. inspect_* TRUNCATE CASCADE 위험
_(없음)_ — CASCADE 불필요, 단순 TRUNCATE 안전

## (f) section NULL/공백/9종외 (Phase 5 FK ADD 전 필수)

### inspect_check_result 이상 section
_(없음)_

### inspect_template 이상 section
_(없음)_

---

## Exit Gate 1 판정

✅ **PASS** — Step 2 V022 마이그 착수 가능

### 핵심 확인 사항
- ✅ UPIS_SW 22건 (로드맵 일치)
- ✅ section 9종 + 5종 (마스터 시드 C-opt1 9행 충분)
- ✅ 외부 FK 0건 (DROP / TRUNCATE CASCADE 불필요)
- ✅ NULL/공백/변형 section 0건 (Phase 5 FK ADD 안전)
- ✅ tb_inspect_checklist/issue/document_signature 전부 0건 (DROP 안전)

### 작업 데이터 규모
- 정제 대상 rows: inspect_check_result 493 + inspect_template 104 + inspect_report 11 + inspect_visit_log 14 = **622 rows**
- TRUNCATE 대상: 4 테이블
- DROP 대상: 2 테이블 (checklist/issue), Signature 유지
