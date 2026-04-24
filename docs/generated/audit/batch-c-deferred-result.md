# Batch C 추가 조사 — 미확인 테이블

## tb_contract 컬럼

| column_name | data_type |
|---|---|
| contract_id | integer |
| infra_id | bigint |
| contract_name | character varying |
| contract_no | character varying |
| contract_type | character varying |
| contract_method | character varying |
| contract_law | character varying |
| contract_clause | character varying |
| contract_amount | bigint |
| guarantee_amount | bigint |
| guarantee_rate | numeric |
| contract_date | date |
| start_date | date |
| end_date | date |
| actual_end_date | date |
| progress_status | character varying |
| contract_year | integer |
| client_org | character varying |
| client_addr | character varying |
| client_phone | character varying |
| client_fax | character varying |
| client_dept | character varying |
| client_contact | character varying |
| client_contact_phone | character varying |
| prime_contractor | character varying |
| subcontract_amount | bigint |
| subcontract_type | character varying |
| purchase_order_no | character varying |
| created_at | timestamp without time zone |
| updated_at | timestamp without time zone |

총 30행

## tb_contract 레코드 수

| total |
|---|
| 0 |

총 1행

## tb_contract 샘플

| contract_id | infra_id | contract_name | contract_no | contract_type | contract_method | contract_law | contract_clause | contract_amount | guarantee_amount | guarantee_rate | contract_date | start_date | end_date | actual_end_date | progress_status | contract_year | client_org | client_addr | client_phone | client_fax | client_dept | client_contact | client_contact_phone | prime_contractor | subcontract_amount | subcontract_type | purchase_order_no | created_at | updated_at |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|

총 0행

## tb_contract_target 컬럼

| column_name | data_type |
|---|---|
| target_id | integer |
| contract_id | integer |
| target_category | character varying |
| product_name | character varying |
| product_detail | character varying |
| quantity | integer |
| sw_id | bigint |
| sort_order | integer |

총 8행

## tb_contract_target 레코드 수

| total |
|---|
| 0 |

총 1행

## tb_inspect_checklist 컬럼

| column_name | data_type |
|---|---|
| check_id | integer |
| doc_id | integer |
| inspect_month | integer |
| target_sw | character varying |
| check_item | character varying |
| check_method | character varying |
| check_result | character varying |
| sort_order | integer |
| created_at | timestamp without time zone |

총 9행

## tb_inspect_checklist 레코드 수

| total |
|---|
| 0 |

총 1행

## tb_inspect_issue 컬럼

| column_name | data_type |
|---|---|
| issue_id | integer |
| doc_id | integer |
| issue_year | integer |
| issue_month | integer |
| issue_day | integer |
| task_type | character varying |
| symptom | text |
| action_taken | text |
| created_at | timestamp without time zone |

총 9행

## tb_inspect_issue 레코드 수

| total |
|---|
| 0 |

총 1행

## pjt_equip 컬럼

| column_name | data_type |
|---|---|
| equip_id | bigint |
| equip_name | character varying |
| equip_type | character varying |
| quantity | integer |
| reg_dt | timestamp without time zone |
| remark | character varying |
| sort_order | integer |
| spec | character varying |
| unit_price | bigint |
| proj_id | bigint |

총 10행

## pjt_equip 레코드 수

| total |
|---|
| 0 |

총 1행

## pjt_equip 샘플

| equip_id | equip_name | equip_type | quantity | reg_dt | remark | sort_order | spec | unit_price | proj_id |
|---|---|---|---|---|---|---|---|---|---|

총 0행

## tb_document_history 레코드 수

| total |
|---|
| 13 |

총 1행

## tb_document_history.action_type distinct

❌ 오류: "action_type" 이름의 칼럼은 없습니다
  Position: 8

## tb_document_signature 레코드 수

| total |
|---|
| 0 |

총 1행

## tb_performance_summary 컬럼

| column_name | data_type |
|---|---|
| summary_id | integer |
| user_id | bigint |
| period_year | integer |
| period_month | integer |
| install_count | integer |
| patch_count | integer |
| fault_count | integer |
| fault_avg_hours | numeric |
| support_count | integer |
| inspect_plan_count | integer |
| inspect_done_count | integer |
| interim_count | integer |
| completion_count | integer |
| infra_count | integer |
| plan_total_count | integer |
| plan_ontime_count | integer |
| calculated_at | timestamp without time zone |
| updated_at | timestamp without time zone |

총 18행

## tb_performance_summary 레코드 수

| total |
|---|
| 0 |

총 1행

## tb_org_unit 컬럼

| column_name | data_type |
|---|---|
| unit_id | bigint |
| parent_id | bigint |
| unit_type | character varying |
| name | character varying |
| sort_order | integer |
| use_yn | character varying |
| created_at | timestamp without time zone |
| updated_at | timestamp without time zone |

총 8행

## tb_org_unit 레코드 수

| total |
|---|
| 39 |

총 1행

