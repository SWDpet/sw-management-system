# 점검자/확인자 저장 방식 확인

- 점검자 = 내부 직원(users)
- 확인자 = 고객 담당자(ps_info)


## inspect_report 점검자/확인자 컬럼 현황

| column_name | data_type | character_maximum_length |
|---|---|---|
| inspect_month | character varying | 7 |
| insp_company | character varying | 100 |
| insp_name | character varying | 50 |
| conf_org | character varying | 100 |
| conf_name | character varying | 50 |
| insp_dbms | character varying | 200 |
| insp_gis | character varying | 200 |
| insp_sign | text | (null) |
| conf_sign | text | (null) |

총 9행

## inspect_report 점검자(insp_*) 샘플 — 내부직원 users 참조 필요

| id | insp_company | insp_name |
|---|---|---|
| 34 | (주)정도UIT | 박욱진 |
| 35 | (주)정도UIT | 박욱진 |
| 36 | (주)정도UIT | 서현규 |
| 37 | (주)정도UIT | 서현규 |
| 38 | (주)정도UIT | 김한준 부장 |
| 39 | (주)정도UIT | 김한준 부장 |
| 40 | (주)정도UIT | 김한준 차장 |
| 41 | (주)정도UIT | 김한준 부장 |
| 42 | (주)정도UIT | 김한준 차장 |
| 43 | (주)정도UIT | 김현탁 주무관 |
| 44 | (주)정도UIT |  |

총 11행

## inspect_report 확인자(conf_*) 샘플 — 고객담당자 ps_info 참조 필요

| id | conf_org | conf_name |
|---|---|---|
| 34 | 양양군청 | 김학범 |
| 35 | 양양군청 | 김학범 |
| 36 | 울산광역시청 | 임춘근 |
| 37 | 성주군청 | 서봉군 |
| 38 | 이천시청 | 함정우 주무관 |
| 39 | 이천시청 | 함정우 주무관 |
| 40 | 김포시청 | 임예빈 주무관 |
| 41 | 김포시청 | 임예빈 주무관 |
| 42 | 김포시청 | 임예빈 주무관 |
| 43 | 가평군청 | 김한준 부장 |
| 44 | 이천시청 |  |

총 11행

## inspect_report.insp_name vs users.username 매칭 (PII 마스킹)

| insp_name | match |
|---|---|
|  | ❌ NOT IN users |
| 김한준 부장 | ❌ NOT IN users |
| 김한준 차장 | ❌ NOT IN users |
| 김현탁 주무관 | ❌ NOT IN users |
| 박욱진 | ✅ matched user_id=6 |
| 서현규 | ✅ matched user_id=10 |

총 6행

## inspect_report.conf_name vs ps_info.user_nm 매칭

| conf_name | conf_org | match |
|---|---|---|
|  | 이천시청 | ❌ NOT IN ps_info |
| 김학범 | 양양군청 | ✅ matched ps_info.id=101 |
| 김한준 부장 | 가평군청 | ❌ NOT IN ps_info |
| 서봉군 | 성주군청 | ✅ matched ps_info.id=71 |
| 임예빈 주무관 | 김포시청 | ❌ NOT IN ps_info |
| 임춘근 | 울산광역시청 | ✅ matched ps_info.id=92 |
| 함정우 주무관 | 이천시청 | ❌ NOT IN ps_info |

총 7행

