# FR-0 사전검증 결과 (process-master-dedup)

- 실행일: Mon Apr 20 16:08:31 KST 2026

## 1. 중복 분포

| 테이블 | total | distinct |
|---|---|---|
| tb_process_master | 1455 | 5 |
| tb_service_purpose | 1455 | 5 |

✅ DISTINCT = 5 확인

## 2. 중복 그룹 MIN PK (보존 대상)

### tb_process_master

| sys_nm_en | process_name | keep_id | dup_cnt |
|---|---|---|---|
| UPIS | 도시계획정보체계용 GIS SW 유지관리 | 1 | 291 |
| KRAS | 부동산종합공부시스템용 GIS SW 유지관리 | 2 | 291 |
| IPSS | 지하시설물관리시스템용 GIS SW 유지관리 | 3 | 291 |
| GIS_SW | GIS SW 유지관리 | 4 | 291 |
| APIMS | 도로관리시스템용 GIS SW 유지관리 | 5 | 291 |

총 5행


### tb_service_purpose

| sys_nm_en | purpose_type | keep_id | dup_cnt |
|---|---|---|---|
| UPIS | PURPOSE | 1 | 291 |
| KRAS | PURPOSE | 2 | 291 |
| IPSS | PURPOSE | 3 | 291 |
| GIS_SW | PURPOSE | 4 | 291 |
| APIMS | PURPOSE | 5 | 291 |

총 5행

## 3. 키 컬럼 NULL 존재

| col | count |
|---|---|
| tb_process_master.sys_nm_en | 0 |
| tb_process_master.process_name | 0 |
| tb_service_purpose.sys_nm_en | 0 |
| tb_service_purpose.purpose_type | 0 |
| tb_service_purpose.purpose_text | 0 |

총 5행

✅ NULL 없음

## 4. 외부 FK

| referencing | constraint_name | column_name | target |
|---|---|---|---|

총 0행

✅ 외부 FK 없음

## 5. purpose_text 최대 길이

| max_len | max_bytes |
|---|---|
| 39 | 93 |

총 1행

---

## 최종 판정

✅ **PASS**
