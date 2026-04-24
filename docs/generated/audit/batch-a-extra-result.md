# 배치 A 추가 검증

## tb_process_master 총 레코드/DISTINCT

| total | distinct_cnt |
|---|---|
| 1450 | 5 |

총 1행

## tb_process_master 시스템별 중복 정도

| sys_nm_en | total | uniq_names |
|---|---|---|
| APIMS | 290 | 1 |
| GIS_SW | 290 | 1 |
| IPSS | 290 | 1 |
| KRAS | 290 | 1 |
| UPIS | 290 | 1 |

총 5행

## tb_service_purpose 총 레코드/DISTINCT

| total | distinct_cnt |
|---|---|
| 1450 | 5 |

총 1행

## inspect_check_result.category 공백 정규화 후

| cat_normalized | cnt | raw_variants |
|---|---|---|
| 오라클 | 102 | 1 |
| H/W | 36 | 1 |
| GIS엔진 | 35 | 1 |
| 성능 점검 | 30 | 1 |
| 육안점검 | 30 | 1 |
| OS | 30 | 1 |
| 구성 점검 | 30 | 1 |
| 도시계획 | 28 | 1 |
| GeoNURIS GeoWeb Server (GWS) | 21 | 1 |
| GeoNURIS Spatial Server (GSS) | 21 | 1 |
| 네트워크 | 18 | 1 |
| DATA 점검 | 18 | 1 |
| 성능 | 12 | 1 |
| 로그 | 12 | 1 |
| 관리자 | 7 | 1 |
| 전자심의 | 7 | 1 |
| 지구단위계획 | 7 | 1 |
| 통계조회 | 7 | 1 |
| 비정형자료실 | 7 | 1 |
| 프로세서 | 6 | 1 |
| 보안 | 6 | 1 |
| GeoNURIS Spatial Server(GSS) | 3 | 1 |
| GeoNURIS GeoWeb Server(GWS) | 3 | 1 |
| 기타 | 3 | 1 |
| CPU | 2 | 1 |
| GeoNURIS Desktop Pro | 2 | 1 |
| Memory | 2 | 1 |
| 측량성과 프로그램 | 1 | 1 |
| /backup | 1 | 1 |
| Disk D: | 1 | 1 |
| oracle | 1 | 1 |
| / | 1 | 1 |
| Oradata | 1 | 1 |
| Disk C: | 1 | 1 |
| archive | 1 | 1 |

총 35행

## inspect_check_result.item_name 샘플

| item_head | cnt |
|---|---|
| KRAS 연계 | 21 |
| 메뉴활성화 | 14 |
|  | 14 |
| 조회/검색 | 14 |
| Network 점검 | 12 |
| Memory | 12 |
| CPU | 12 |
| GSS 로그파일 삭제 | 7 |
| GWS 서비스 확인 | 7 |
| 이력정보 | 7 |
| 사용자관리 | 7 |
| 시스템통계 | 7 |
| 필지정보 | 7 |
| Desktop Pro 데이터저장소 구동확인 | 7 |
| GSS 구동확인 | 7 |
| 지도 요청 | 7 |
| GWS 로그파일 삭제 | 7 |
| 필지 이동 | 7 |
| 하일라이팅 | 7 |
| GWS 구동확인 | 7 |

총 20행

## inspect_template vs sys_mst 비교

| value | cnt | match |
|---|---|---|
| UPIS | 75 | ✅ |
| UPIS_SW | 20 | ❌ NOT IN sys_mst |
| KRAS | 9 | ✅ |

총 3행

## tb_work_plan 전체 레코드 상세

| plan_id | plan_type | status | process_step | repeat_type | title_head | infra_id |
|---|---|---|---|---|---|---|
| 1 | PATCH | CONFIRMED | (null) | NONE | 영월군 기초조사정보시스템 v2.4 패치 | 18 |

총 1행

## inspect_check_result.result 패턴 분류

| pattern | cnt |
|---|---|
| NORMAL_KOR | 313 |
| EMPTY | 165 |
| PERCENT | 9 |
| OTHER | 3 |
| DISK_USAGE | 2 |
| INSPECT_KOR | 1 |

총 6행

