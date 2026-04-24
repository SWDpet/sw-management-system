# Batch C 실데이터 분포

## qr_license 레코드 수

| total |
|---|
| 0 |

총 1행

## qr_license.license_type distinct

| license_type | cnt |
|---|---|

총 0행

## qr_license.license_type_ko distinct

| license_type_ko | cnt |
|---|---|

총 0행

## qr_license.issued_by distinct (내부직원 users 참조?)

| issued_by | cnt |
|---|---|

총 0행

## access_logs 레코드 수

| total |
|---|
| 3885 |

총 1행

## access_logs.menu_nm distinct (MenuName 상수 참조?)

| menu_nm | cnt |
|---|---|
| 서버관리 | 1007 |
| 문서관리 | 989 |
| 라이선스대장 | 650 |
| 견적서 | 576 |
| GeoNURIS라이선스 | 233 |
| 사업관리 | 229 |
| 담당자관리 | 59 |
| 회원관리 | 53 |
| 업무계획 | 35 |
| 사업현황 | 27 |
| 성과통계 | 19 |
| 마이페이지 | 4 |
| 회원가입 | 4 |

총 13행

## access_logs.action_type distinct

| action_type | cnt |
|---|---|
| 조회 | 2121 |
| 수정 | 835 |
| 미리보기 | 191 |
| 접근 | 187 |
| 등록 | 120 |
| 목록조회 | 92 |
| 생성 | 62 |
| 발급폼접근 | 46 |
| 상세조회 | 44 |
| 다운로드 | 31 |
| 패턴수정 | 29 |
| 삭제 | 28 |
| 패턴등록 | 22 |
| 발급 | 18 |
| 일괄생성 | 8 |
| 상태변경 | 8 |
| 승인 | 8 |
| 수정폼접근 | 8 |
| 민감정보조회 | 7 |
| 비고패턴등록 | 7 |

총 20행

## access_logs.userid vs users.userid 매칭

| userid | match | cnt |
|---|---|---|
| admin | ✅ | 1418 |
| hanjun | ✅ | 992 |
| seohg0801 | ✅ | 739 |
| ukjin914 | ✅ | 640 |
| test02 | ✅ | 36 |
| 박욱진 | ❌ NOT IN users | 16 |
| parksh | ✅ | 13 |
| 관리자 | ❌ NOT IN users | 11 |
| yeohj | ✅ | 8 |
| anonymousUser | ❌ NOT IN users | 4 |
| ybkang | ✅ | 4 |
| jeongsj | ✅ | 4 |

총 12행

## 테이블명 패턴 system/graph/menu

| table_name |
|---|
| cont_frm_mst |
| cont_stat_mst |
| maint_tp_mst |
| sigungu_code |
| sys_mst |

총 5행

## 감사 대상 후보 테이블 — 전수 목록 (마스터/기초 4개 기능 제외 후보)

| table_name |
|---|
| access_logs |
| geonuris_license |
| inspect_check_result |
| inspect_report |
| inspect_template |
| inspect_visit_log |
| license_registry |
| license_upload_history |
| pjt_equip |
| qr_license |
| qt_product_pattern |
| qt_quotation |
| qt_quotation_item |
| qt_quotation_ledger |
| qt_quote_number_seq |
| qt_remarks_pattern |
| qt_wage_rate |
| tb_contract |
| tb_contract_target |
| tb_document |
| tb_document_attachment |
| tb_document_detail |
| tb_document_history |
| tb_document_signature |
| tb_inspect_checklist |
| tb_inspect_issue |
| tb_org_unit |
| tb_performance_summary |
| tb_pjt_manpower_plan |
| tb_pjt_schedule |
| tb_pjt_target |
| tb_process_master |
| tb_service_purpose |
| tb_work_plan |

총 34행

## tb_document 상세 속성 (region_code, status 등 Batch A에서 건드리지 않은 컬럼)

| column_name | data_type |
|---|---|
| doc_id | integer |
| doc_no | character varying |
| doc_type | character varying |
| sys_type | character varying |
| infra_id | bigint |
| plan_id | integer |
| contract_id | integer |
| title | character varying |
| status | character varying |
| author_id | bigint |
| approver_id | bigint |
| approved_at | timestamp without time zone |
| created_at | timestamp without time zone |
| updated_at | timestamp without time zone |
| proj_id | bigint |
| support_target_type | character varying |
| org_unit_id | bigint |
| environment | character varying |
| region_code | character varying |

총 19행

