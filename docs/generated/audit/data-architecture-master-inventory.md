# 기초 마스터 인벤토리 (Phase 1 산출)

- 실행일: Mon Apr 20 13:38:14 KST 2026
- 근거: 사용자 확정 9개 마스터 (2026-04-20)

## A. 공통 마스터 레이어

### prj_types

| cd | nm |
|---|---|
| GISSW | 원천소프트웨어 |
| PKSW | 패키지소프트웨어 |
| TS | 기술지원 |

총 3행

### sys_mst

| cd | nm |
|---|---|
| 112 | 112시스템 |
| ANCISS | 공항소음대책정보화 및 지원시스템 |
| APIMS | 농업생산기반시설관리플랫폼 |
| ARPMS | 공항활주로 포장관리시스템 |
| BIS | 버스정보시스템 |
| BMIS | 전장관리정보체계 |
| BSIS | 기초조사정보시스템 v1.5 |
| BSISP | 기초조사정보시스템 v2.0 |
| CDMS | 상권관리시스템 |
| CGM | CLX GPS MAP |
| CPS | 지적포털 시스템 |
| CREIS | 종합부동산정보시스템 |
| DEIMS | 배전설비정보관리시스템 |
| DFGIS | 배전설비 지리정보시스템 |
| GCRM | GCRM |
| GMPSS | 성장관리시스템 |
| HIS | 택지정보시스템 |
| IPSS | 통합인허가관리시스템 |
| JUSO | 국가주소정보시스템 |
| KRAS | 부동산종합공부시스템 |
| LSA | 토지적성평가 업무 프로그램 |
| LSMS | 토지현황관리시스템 |
| LTCS | 위치추적관제시스템 |
| MPMIS | 국유재산관리조사시스템 |
| MPMS | 국유재산관리시스템 |
| NDTIS | 국방수송정보체계 |
| NPFMS | 국립공원 시설관리시스템 |
| NSDI | 국가공간정보체계 |
| PGMS | 스마트공원녹지플랫폼 |
| RAISE | 농어촌개발관리정보시스템 |
| SC | 스마트도시 통합플랫폼 |
| SMMS | aT센터 학교급식 모바일 시스템 |
| SUFM | 스마트도시 시설물관제 |
| UPBSS | 기초조사정보시스템 v1.0 |
| UPIS | 도시계획정보체계 |
| URTIS | 도시재생종합관리시스템 |
| VLICMS | 베트남토지정보 종합관리시스템 |

총 37행

### cont_frm_mst

| cd | nm |
|---|---|
| 1 | 도입 |
| 2 | 교체 |
| 3 | 무상 |
| 4 | 유상 |
| 5 | 종료 |

총 5행

### maint_tp_mst

| cd | nm |
|---|---|
| BASIC | 토지적성평가 베이직 |
| DHS | DB/HW/SW |
| DS | DB/SW |
| DU | 데이터 업로드 |
| HS | HW/SW |
| Pro | 토지적성평가 프로 |
| SW | SW |

총 7행

### cont_stat_mst

| cd | nm |
|---|---|
| 1 | 진행중 |
| 2 | 완료 |

총 2행

### sigungu_code_sample

| instt_c | adm_sect_c | full_name | sido_name | sgg_name |
|---|---|---|---|---|
| 6110000 | 11000 | 서울특별시 | 서울특별시 | 서울특별시 |
| 3000000 | 11110 | 서울특별시 종로구 | 서울특별시 | 종로구 |
| 3010000 | 11140 | 서울특별시 중구 | 서울특별시 | 중구 |
| 3020000 | 11170 | 서울특별시 용산구 | 서울특별시 | 용산구 |
| 3030000 | 11200 | 서울특별시 성동구 | 서울특별시 | 성동구 |
| 3040000 | 11215 | 서울특별시 광진구 | 서울특별시 | 광진구 |
| 3050000 | 11230 | 서울특별시 동대문구 | 서울특별시 | 동대문구 |
| 3060000 | 11260 | 서울특별시 중랑구 | 서울특별시 | 중랑구 |
| 3070000 | 11290 | 서울특별시 성북구 | 서울특별시 | 성북구 |
| 3080000 | 11305 | 서울특별시 강북구 | 서울특별시 | 강북구 |
| 3090000 | 11320 | 서울특별시 도봉구 | 서울특별시 | 도봉구 |
| 3100000 | 11350 | 서울특별시 노원구 | 서울특별시 | 노원구 |
| 3110000 | 11380 | 서울특별시 은평구 | 서울특별시 | 은평구 |
| 3120000 | 11410 | 서울특별시 서대문구 | 서울특별시 | 서대문구 |
| 3130000 | 11440 | 서울특별시 마포구 | 서울특별시 | 마포구 |
| 3140000 | 11470 | 서울특별시 양천구 | 서울특별시 | 양천구 |
| 3150000 | 11500 | 서울특별시 강서구 | 서울특별시 | 강서구 |
| 3160000 | 11530 | 서울특별시 구로구 | 서울특별시 | 구로구 |
| 3170000 | 11545 | 서울특별시 금천구 | 서울특별시 | 금천구 |
| 3180000 | 11560 | 서울특별시 영등포구 | 서울특별시 | 영등포구 |

총 20행

### sw_pjt_sample

| proj_id | year | sys_nm_en | biz_cat_en | cont_type | stat | maint_type | proj_nm_head |
|---|---|---|---|---|---|---|---|
| 622 | 2021 | 112 | GISSW | 직접 | 2 | SW | 112시스템 고도화(2단계 상용 소프트웨어(6종) 구매 |
| 621 | 2026 | IPSS | GISSW | 직접 | 1 | SW | 통합인허가 지원시스템(IPSS) 운용장비 정비용역 |
| 620 | 2026 | UPIS | GISSW | 직접 | 1 | SW | 2026년 충주 도시계획관련 정보 DB현행화 및 정비  |
| 619 | 2026 | UPIS | GISSW | 하도 | 1 | SW | ​2026년 나주시 GIS SW 유지관리 용역 |
| 618 | 2026 | UPIS | GISSW | 하도 | 1 | SW | 2026년 통영시 국토이용정보 통합플랫폼(KLIP) 운 |
| 617 | 2026 | UPIS | GISSW | 직접 | 1 | SW | 2026년 진주시 도시계획정보 유지관리 용역 |
| 616 | 2026 | UPIS | GISSW | 직접 | 1 | SW | 2026년 국토이용정보체계 시스템 GIS 엔진 유지보수 |
| 615 | 2026 | UPIS | GISSW | 하도 | 2 | SW | 남양주시 UPIS GIS 웹/편집엔진 유지관리 |
| 614 | 2026 | KRAS | GISSW | 하도 | 1 | SW | 청주시 공간정보시스템 HW통합운영 유지보수 |
| 613 | 2026 | KRAS | GISSW | 하도 | 2 | SW | 2026년 진천군 부동산종합공부시스템 통합유지 관리용역 |

총 10행

## B. 기초 기능 소유 마스터 (PII 마스킹 샘플)

### users (샘플 3건, PII 마스킹)

| user_id | userid_masked | name_masked | email_masked | user_role | enabled |
|---|---|---|---|---|---|
| 5 | ad*** | 관*** | uk*** | ROLE_ADMIN | t |
| 6 | uk*** | 박*** | u**** | ROLE_USER | t |
| 7 | ha*** | 김*** | ha*** | ROLE_USER | t |

총 3행

### ps_info (샘플 3건, PII 마스킹)

| id | sys_nm_en | org_nm_masked |
|---|---|---|
| 1 | UPIS | 단양군*** |
| 2 | KRAS | 강릉시*** |
| 3 | KRAS | 동해시*** |

총 3행

## C. 마스터 매칭 분석 (Phase 2 입력)

### tb_document_status_vs_cont_stat_mst

| value | cnt | match |
|---|---|---|
| COMPLETED | 12 | ❌ NOT IN cont_stat_mst |
| DRAFT | 3 | ❌ NOT IN cont_stat_mst |

총 2행

### tb_document_doc_type_vs_maint_tp_mst

| value | cnt | match |
|---|---|---|
| INSPECT | 8 | ❌ NOT IN maint_tp_mst |
| COMMENCE | 3 | ❌ NOT IN maint_tp_mst |
| INTERIM | 2 | ❌ NOT IN maint_tp_mst |
| COMPLETION | 2 | ❌ NOT IN maint_tp_mst |

총 4행

### tb_document_sys_type_vs_sys_mst

| value | cnt | match |
|---|---|---|
| UPIS | 9 | ✅ |
| KRAS | 5 | ✅ |
| UPIS_SW | 1 | ❌ NOT IN sys_mst |

총 3행

### inspect_report_status_vs_cont_stat_mst

| value | cnt | match |
|---|---|---|
| COMPLETED | 7 | ❌ NOT IN cont_stat_mst |
| DRAFT | 4 | ❌ NOT IN cont_stat_mst |

총 2행

### inspect_report_sys_type_vs_sys_mst

| value | cnt | match |
|---|---|---|
| UPIS | 6 | ✅ |
| IPSS | 3 | ✅ |
| KRAS | 1 | ✅ |
| UPIS_SW | 1 | ❌ NOT IN sys_mst |

총 4행

### sw_pjt_cont_type_vs_cont_frm_mst

| value | cnt | match |
|---|---|---|
| 하도 | 301 | ❌ NOT IN cont_frm_mst |
| 직접 | 290 | ❌ NOT IN cont_frm_mst |

총 2행

### sw_pjt_stat_vs_cont_stat_mst

| value | cnt | match |
|---|---|---|
| 2 | 560 | ✅ |
| 1 | 36 | ✅ |

총 2행

### sw_pjt_maint_type_vs_maint_tp_mst

| value | cnt | match |
|---|---|---|
| SW | 566 | ✅ |
| HS | 18 | ✅ |
| DHS | 8 | ✅ |
| DU | 3 | ✅ |
| DS | 1 | ✅ |

총 5행

### sw_pjt_biz_cat_en_distinct

| value | cnt |
|---|---|
| GISSW | 592 |
| TS | 4 |

총 2행

### tb_infra_master_sys_nm_en_vs_sys_mst

| value | cnt | match |
|---|---|---|
| UPIS | 115 | ✅ |
| UPBSS | 50 | ✅ |
| BSISP | 27 | ✅ |
| GMPSS | 6 | ✅ |
| APIMS | 2 | ✅ |
| SPFMS | 1 | ❌ |
| CIAMS | 1 | ❌ |
| PGMS | 1 | ✅ |
| BSIS | 1 | ✅ |

총 9행

