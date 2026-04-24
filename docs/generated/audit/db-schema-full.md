# DB 전수 스키마 덤프

- 실행일: Mon Apr 20 13:38:14 KST 2026
- DB: SW_Dept @ PostgreSQL
- 감사 스프린트: `data-architecture-audit` (Phase 1)

## 테이블·컬럼 목록 (information_schema)

총 50개 테이블.

### access_logs  (rows: 3881)

| column | type | len | null | default |
|---|---|---|---|---|
| log_id | bigint | - | NO | nextval('access_logs_log_id_seq'::regclass) |
| userid | character varying | 50 | YES | - |
| username | character varying | 50 | YES | - |
| ip_addr | character varying | 45 | YES | - |
| access_time | timestamp without time zone | - | YES | now() |
| menu_nm | character varying | 100 | YES | - |
| action_type | character varying | 50 | YES | - |
| action_detail | text | - | YES | - |

### cont_frm_mst  (rows: 5)

| column | type | len | null | default |
|---|---|---|---|---|
| cd | character varying | 10 | NO | - |
| nm | character varying | 20 | NO | - |

### cont_stat_mst  (rows: 2)

| column | type | len | null | default |
|---|---|---|---|---|
| cd | character varying | 10 | NO | - |
| nm | character varying | 20 | NO | - |

### geonuris_license  (rows: 16)

| column | type | len | null | default |
|---|---|---|---|---|
| id | bigint | - | NO | nextval('geonuris_license_id_seq'::regclass) |
| license_type | character varying | 20 | NO | - |
| file_name | character varying | 100 | YES | - |
| user_name | character varying | 100 | YES | - |
| organization | character varying | 200 | YES | - |
| phone | character varying | 50 | YES | - |
| email | character varying | 150 | YES | - |
| issuer | character varying | 100 | YES | - |
| mac_address | character varying | 50 | NO | - |
| permission | character varying | 1 | YES | - |
| start_date | timestamp without time zone | - | NO | - |
| expiry_date | timestamp without time zone | - | NO | - |
| dbms_type | character varying | 20 | YES | - |
| setl_count | integer | - | YES | 0 |
| plugin_edit | boolean | - | YES | false |
| plugin_gdm | boolean | - | YES | false |
| plugin_tmbuilder | boolean | - | YES | false |
| plugin_setl | boolean | - | YES | false |
| license_data | text | - | YES | - |
| created_at | timestamp without time zone | - | YES | now() |
| created_by | character varying | 100 | YES | - |

### inspect_check_result  (rows: 493)

| column | type | len | null | default |
|---|---|---|---|---|
| id | bigint | - | NO | nextval('inspect_check_result_id_seq'::regclass) |
| report_id | bigint | - | NO | - |
| section | character varying | 20 | NO | - |
| category | character varying | 50 | YES | - |
| item_name | character varying | 200 | YES | - |
| item_method | character varying | 300 | YES | - |
| result | character varying | 500 | YES | - |
| remarks | character varying | 300 | YES | - |
| sort_order | integer | - | YES | 0 |
| created_at | timestamp without time zone | - | YES | now() |

### inspect_report  (rows: 11)

| column | type | len | null | default |
|---|---|---|---|---|
| id | bigint | - | NO | nextval('inspect_report_id_seq'::regclass) |
| pjt_id | bigint | - | NO | - |
| inspect_month | character varying | 7 | YES | - |
| sys_type | character varying | 20 | YES | - |
| doc_title | character varying | 300 | YES | - |
| insp_company | character varying | 100 | YES | - |
| insp_name | character varying | 50 | YES | - |
| conf_org | character varying | 100 | YES | - |
| conf_name | character varying | 50 | YES | - |
| insp_dbms | character varying | 200 | YES | - |
| insp_gis | character varying | 200 | YES | - |
| dbms_ip | character varying | 50 | YES | - |
| status | character varying | 20 | YES | 'DRAFT'::character varying |
| created_by | character varying | 50 | YES | - |
| updated_by | character varying | 50 | YES | - |
| created_at | timestamp without time zone | - | YES | now() |
| updated_at | timestamp without time zone | - | YES | now() |
| insp_sign | text | - | YES | - |
| conf_sign | text | - | YES | - |

### inspect_template  (rows: 104)

| column | type | len | null | default |
|---|---|---|---|---|
| id | bigint | - | NO | nextval('inspect_template_id_seq'::regclass) |
| template_type | character varying | 20 | NO | - |
| section | character varying | 20 | NO | - |
| category | character varying | 50 | YES | - |
| item_name | character varying | 200 | NO | - |
| item_method | character varying | 300 | YES | - |
| sort_order | integer | - | YES | 0 |
| use_yn | character varying | 1 | YES | 'Y'::character varying |
| created_at | timestamp without time zone | - | YES | now() |

### inspect_visit_log  (rows: 14)

| column | type | len | null | default |
|---|---|---|---|---|
| id | bigint | - | NO | nextval('inspect_visit_log_id_seq'::regclass) |
| report_id | bigint | - | NO | - |
| visit_year | character varying | 4 | YES | - |
| visit_month | character varying | 2 | YES | - |
| visit_day | character varying | 2 | YES | - |
| task | character varying | 200 | YES | - |
| symptom | character varying | 500 | YES | - |
| action | character varying | 500 | YES | - |
| sort_order | integer | - | YES | 0 |
| created_at | timestamp without time zone | - | YES | now() |

### license_registry  (rows: 1531)

| column | type | len | null | default |
|---|---|---|---|---|
| id | bigint | - | NO | nextval('license_registry_id_seq'::regclass) |
| license_id | character varying | 50 | NO | - |
| product_id | character varying | 100 | NO | - |
| license_type | character varying | 50 | YES | - |
| valid_product_edition | character varying | 100 | YES | - |
| valid_product_version | character varying | 50 | YES | - |
| validity_period | integer | - | YES | - |
| maintenance_period | integer | - | YES | - |
| generation_source | character varying | 50 | YES | - |
| generation_date_time | timestamp without time zone | - | YES | - |
| quantity | integer | - | YES | - |
| allowed_use_count | integer | - | YES | - |
| hardware_id | text | - | YES | - |
| registered_to | character varying | 200 | YES | - |
| full_name | character varying | 200 | YES | - |
| email | character varying | 200 | YES | - |
| company | character varying | 200 | YES | - |
| telephone | character varying | 50 | YES | - |
| fax | character varying | 50 | YES | - |
| street | character varying | 200 | YES | - |
| city | character varying | 100 | YES | - |
| zip_code | character varying | 20 | YES | - |
| country | character varying | 100 | YES | - |
| activation_required | boolean | - | YES | - |
| auto_activations_disabled | boolean | - | YES | - |
| manual_activations_disabled | boolean | - | YES | - |
| online_key_lease_disabled | boolean | - | YES | - |
| deactivations_disabled | boolean | - | YES | - |
| activation_period | integer | - | YES | - |
| allowed_activation_count | integer | - | YES | - |
| allowed_deactivation_count | integer | - | YES | - |
| dont_keep_deactivation_records | boolean | - | YES | - |
| ip_blocks | text | - | YES | - |
| ip_blocks_allow | boolean | - | YES | - |
| activation_return | character varying | 50 | YES | - |
| reject_modification_key_usage | boolean | - | YES | - |
| set_generation_time_to_activation_time | boolean | - | YES | - |
| generation_time_offset_from_activation_time | integer | - | YES | - |
| hardware_id_selection | character varying | 500 | YES | - |
| internal_hidden_string | text | - | YES | - |
| use_customer_name_in_validation | boolean | - | YES | - |
| use_company_name_in_validation | boolean | - | YES | - |
| floating_license_check_period | integer | - | YES | - |
| floating_license_server_connection_check_period | integer | - | YES | - |
| floating_allow_multiple_instances | boolean | - | YES | - |
| superseded_license_ids | text | - | YES | - |
| max_inactive_period | integer | - | YES | - |
| maximum_re_checks_before_drop | integer | - | YES | - |
| dont_keep_released_license_usage | boolean | - | YES | - |
| current_use_count | integer | - | YES | - |
| allowed_use_count_limit | integer | - | YES | - |
| current_use_time | integer | - | YES | - |
| allowed_use_time_limit | integer | - | YES | - |
| date_time_check | character varying | 50 | YES | - |
| ntp_server_check | boolean | - | YES | - |
| ntp_server | character varying | 200 | YES | - |
| ntp_server_timeout | integer | - | YES | - |
| web_server_check | boolean | - | YES | - |
| web_server | character varying | 500 | YES | - |
| web_server_timeout | integer | - | YES | - |
| query_local_ad_server | boolean | - | YES | - |
| unsigned_custom_features | text | - | YES | - |
| signed_custom_features | text | - | YES | - |
| license_string | text | - | NO | - |
| upload_date | timestamp without time zone | - | NO | - |
| uploaded_by | character varying | 100 | YES | - |
| created_date | timestamp without time zone | - | NO | - |

### license_upload_history  (rows: 29)

| column | type | len | null | default |
|---|---|---|---|---|
| id | bigint | - | NO | nextval('license_upload_history_id_seq'::regclass) |
| upload_date | timestamp without time zone | - | NO | - |
| file_name | character varying | 500 | YES | - |
| total_count | integer | - | NO | - |
| success_count | integer | - | NO | - |
| fail_count | integer | - | YES | 0 |
| uploaded_by | character varying | 100 | YES | - |
| created_date | timestamp without time zone | - | NO | CURRENT_TIMESTAMP |

### maint_tp_mst  (rows: 7)

| column | type | len | null | default |
|---|---|---|---|---|
| cd | character varying | 10 | NO | - |
| nm | character varying | 50 | NO | - |

### pjt_equip  (rows: 0)

| column | type | len | null | default |
|---|---|---|---|---|
| equip_id | bigint | - | NO | nextval('pjt_equip_equip_id_seq'::regclass) |
| equip_name | character varying | 200 | NO | - |
| equip_type | character varying | 10 | NO | - |
| quantity | integer | - | YES | - |
| reg_dt | timestamp without time zone | - | YES | - |
| remark | character varying | 500 | YES | - |
| sort_order | integer | - | YES | - |
| spec | character varying | 500 | YES | - |
| unit_price | bigint | - | YES | - |
| proj_id | bigint | - | NO | - |

### prj_types  (rows: 3)

| column | type | len | null | default |
|---|---|---|---|---|
| cd | character varying | 10 | NO | - |
| nm | character varying | 50 | NO | - |

### ps_info  (rows: 107)

| column | type | len | null | default |
|---|---|---|---|---|
| id | bigint | - | NO | nextval('ps_info_id_seq'::regclass) |
| sys_nm_en | character varying | 10 | YES | - |
| org_nm | character varying | 100 | YES | - |
| dept_nm | character varying | 100 | YES | - |
| team_nm | character varying | 100 | YES | - |
| pos | character varying | 50 | YES | - |
| user_nm | character varying | 50 | YES | - |
| email | character varying | 100 | YES | - |
| reg_dt | timestamp without time zone | - | YES | now() |
| tel | character varying | 20 | YES | - |
| city_nm | character varying | 50 | YES | - |
| dist_nm | character varying | 200 | YES | - |
| org_cd | character varying | 50 | YES | - |
| dist_cd | character varying | 20 | YES | - |
| sys_nm | character varying | 200 | YES | - |

### qr_license  (rows: 0)

| column | type | len | null | default |
|---|---|---|---|---|
| qr_id | bigint | - | NO | nextval('qr_license_qr_id_seq'::regclass) |
| end_user_name | character varying | 200 | NO | - |
| address | character varying | 500 | YES | - |
| tel | character varying | 50 | YES | - |
| fax | character varying | 50 | YES | - |
| products | character varying | 500 | NO | - |
| user_units | character varying | 100 | YES | - |
| version | character varying | 50 | YES | - |
| license_type | character varying | 100 | YES | - |
| serial_number | character varying | 200 | YES | - |
| application_name | character varying | 200 | YES | - |
| end_user_name_ko | character varying | 200 | YES | - |
| address_ko | character varying | 500 | YES | - |
| tel_ko | character varying | 50 | YES | - |
| fax_ko | character varying | 50 | YES | - |
| products_ko | character varying | 500 | YES | - |
| user_units_ko | character varying | 100 | YES | - |
| version_ko | character varying | 50 | YES | - |
| license_type_ko | character varying | 100 | YES | - |
| serial_number_ko | character varying | 200 | YES | - |
| application_name_ko | character varying | 200 | YES | - |
| qr_image_data | text | - | YES | - |
| issued_by | character varying | 50 | YES | - |
| issued_at | timestamp without time zone | - | YES | now() |
| remarks | character varying | 1000 | YES | - |

### qt_product_pattern  (rows: 23)

| column | type | len | null | default |
|---|---|---|---|---|
| pattern_id | bigint | - | NO | nextval('qt_product_pattern_pattern_id_seq'::regclass) |
| category | character varying | 10 | NO | - |
| pattern_group | character varying | 100 | NO | - |
| product_name | character varying | 500 | NO | - |
| default_unit | character varying | 10 | YES | '식'::character varying |
| default_unit_price | bigint | - | YES | 0 |
| description | character varying | 500 | YES | - |
| sub_items | text | - | YES | - |
| usage_count | integer | - | YES | 0 |
| calc_type | character varying | 10 | YES | 'NORMAL'::character varying |
| overhead_rate | double precision | - | YES | 110.0 |
| tech_fee_rate | double precision | - | YES | 20.0 |

### qt_quotation  (rows: 59)

| column | type | len | null | default |
|---|---|---|---|---|
| quote_id | bigint | - | NO | nextval('qt_quotation_quote_id_seq'::regclass) |
| quote_number | character varying | 30 | NO | - |
| quote_date | date | - | NO | - |
| category | character varying | 10 | NO | - |
| project_name | character varying | 500 | NO | - |
| recipient | character varying | 200 | YES | - |
| reference_to | character varying | 200 | YES | - |
| total_amount | bigint | - | YES | 0 |
| total_amount_text | character varying | 200 | YES | - |
| vat_included | boolean | - | YES | true |
| status | character varying | 10 | YES | '작성중'::character varying |
| created_by | character varying | 50 | YES | - |
| created_at | timestamp without time zone | - | YES | now() |
| updated_at | timestamp without time zone | - | YES | now() |
| rounddown_unit | integer | - | YES | 1 |
| show_seal | boolean | - | YES | true |
| template_type | integer | - | YES | 1 |
| remarks | character varying | 2000 | YES | - |
| grand_total | bigint | - | YES | - |
| bid_rate | double precision | - | YES | - |

### qt_quotation_item  (rows: 85)

| column | type | len | null | default |
|---|---|---|---|---|
| item_id | bigint | - | NO | nextval('qt_quotation_item_item_id_seq'::regclass) |
| quote_id | bigint | - | NO | - |
| item_no | integer | - | NO | - |
| product_name | character varying | 500 | NO | - |
| specification | text | - | YES | - |
| quantity | integer | - | YES | 1 |
| unit | character varying | 10 | YES | '식'::character varying |
| unit_price | bigint | - | YES | 0 |
| amount | bigint | - | YES | 0 |
| remarks | character varying | 500 | YES | - |

### qt_quotation_ledger  (rows: 59)

| column | type | len | null | default |
|---|---|---|---|---|
| ledger_id | bigint | - | NO | nextval('qt_quotation_ledger_ledger_id_seq'::regclass) |
| quote_id | bigint | - | NO | - |
| ledger_no | integer | - | YES | - |
| year | integer | - | NO | - |
| category | character varying | 10 | NO | - |
| quote_number | character varying | 30 | NO | - |
| quote_date | date | - | NO | - |
| project_name | character varying | 500 | NO | - |
| total_amount | bigint | - | YES | 0 |
| recipient | character varying | 200 | YES | - |
| reference_to | character varying | 200 | YES | - |
| created_by | character varying | 50 | YES | - |
| registered_at | timestamp without time zone | - | YES | now() |
| grand_total | bigint | - | YES | - |

### qt_quote_number_seq  (rows: 3)

| column | type | len | null | default |
|---|---|---|---|---|
| seq_id | bigint | - | NO | nextval('qt_quote_number_seq_seq_id_seq'::regclass) |
| year | integer | - | NO | - |
| category | character varying | 10 | NO | - |
| last_seq | integer | - | YES | 0 |

### qt_remarks_pattern  (rows: 7)

| column | type | len | null | default |
|---|---|---|---|---|
| pattern_id | bigint | - | NO | nextval('qt_remarks_pattern_pattern_id_seq'::regclass) |
| pattern_name | character varying | 100 | NO | - |
| content | text | - | NO | - |
| sort_order | integer | - | YES | 0 |

### qt_wage_rate  (rows: 78)

| column | type | len | null | default |
|---|---|---|---|---|
| wage_id | bigint | - | NO | nextval('qt_wage_rate_wage_id_seq'::regclass) |
| year | integer | - | NO | - |
| grade_name | character varying | 50 | NO | - |
| daily_rate | bigint | - | NO | 0 |
| monthly_rate | bigint | - | YES | 0 |
| hourly_rate | bigint | - | YES | 0 |
| description | character varying | 200 | YES | - |

### sigungu_code  (rows: 279)

| column | type | len | null | default |
|---|---|---|---|---|
| instt_c | character varying | 20 | YES | - |
| adm_sect_c | character varying | 20 | NO | - |
| full_name | character varying | 100 | YES | - |
| sido_name | character varying | 50 | YES | - |
| sgg_name | character varying | 50 | YES | - |

### sw_pjt  (rows: 596)

| column | type | len | null | default |
|---|---|---|---|---|
| proj_id | bigint | - | NO | nextval('sw_pjt_proj_id_seq'::regclass) |
| year | integer | - | YES | - |
| pms_cd | character varying | 50 | YES | - |
| city_nm | character varying | 50 | YES | - |
| dist_nm | character varying | 200 | YES | - |
| org_nm | character varying | 50 | YES | - |
| org_cd | character varying | 50 | YES | - |
| dist_cd | character varying | 20 | YES | - |
| biz_type | character varying | 100 | YES | - |
| biz_cat | character varying | 100 | YES | - |
| biz_cat_en | character varying | 10 | YES | - |
| sys_nm | character varying | 200 | YES | - |
| sys_nm_en | character varying | 10 | YES | - |
| proj_nm | character varying | 200 | YES | - |
| client | character varying | 200 | YES | - |
| cont_ent | character varying | 200 | YES | - |
| cont_dept | character varying | 200 | YES | - |
| cont_type | character varying | 10 | YES | - |
| cont_dt | date | - | YES | - |
| start_dt | date | - | YES | - |
| end_dt | date | - | YES | - |
| inst_dt | date | - | YES | - |
| budg_amt | bigint | - | YES | - |
| sw_rt | double precision | - | YES | - |
| cont_amt | bigint | - | YES | - |
| cont_rt | double precision | - | YES | - |
| sw_amt | bigint | - | YES | - |
| cnslt_amt | bigint | - | YES | - |
| db_impl_amt | bigint | - | YES | - |
| pkg_sw_amt | bigint | - | YES | - |
| sys_dev_amt | bigint | - | YES | - |
| hw_amt | bigint | - | YES | - |
| etc_amt | bigint | - | YES | - |
| outscr_amt | bigint | - | YES | - |
| stat | character varying | 10 | YES | - |
| pre_pay | bigint | - | YES | - |
| pay_prog | bigint | - | YES | - |
| pay_comp | bigint | - | YES | - |
| maint_type | character varying | 10 | YES | - |
| reg_dt | timestamp without time zone | - | YES | now() |
| pay_prog_yn | character varying | 10 | YES | - |
| pay_prog_fr | character varying | 10 | YES | - |
| person_id | bigint | - | YES | - |
| org_lgh_nm | character varying | 50 | YES | '기본값'::character varying |
| comp_yn | character varying | 1 | YES | 'N'::character varying |
| interim_yn | character varying | 1 | YES | 'N'::character varying |
| completion_yn | character varying | 1 | YES | 'N'::character varying |
| proj_purpose | text | - | YES | - |
| support_type | character varying | 50 | YES | - |
| scope_text | text | - | YES | - |
| inspect_method | character varying | 200 | YES | - |

### sys_mst  (rows: 37)

| column | type | len | null | default |
|---|---|---|---|---|
| cd | character varying | 10 | NO | - |
| nm | character varying | 100 | NO | - |

### tb_contract  (rows: 0)

| column | type | len | null | default |
|---|---|---|---|---|
| contract_id | integer | - | NO | nextval('tb_contract_contract_id_seq'::regclass) |
| infra_id | bigint | - | YES | - |
| contract_name | character varying | 300 | NO | - |
| contract_no | character varying | 100 | YES | - |
| contract_type | character varying | 30 | NO | 'UNIT_PRICE'::character varying |
| contract_method | character varying | 100 | YES | - |
| contract_law | character varying | 100 | YES | - |
| contract_clause | character varying | 100 | YES | - |
| contract_amount | bigint | - | YES | 0 |
| guarantee_amount | bigint | - | YES | 0 |
| guarantee_rate | numeric | - | YES | - |
| contract_date | date | - | YES | - |
| start_date | date | - | YES | - |
| end_date | date | - | YES | - |
| actual_end_date | date | - | YES | - |
| progress_status | character varying | 30 | NO | 'BUDGET'::character varying |
| contract_year | integer | - | YES | - |
| client_org | character varying | 200 | YES | - |
| client_addr | character varying | 500 | YES | - |
| client_phone | character varying | 50 | YES | - |
| client_fax | character varying | 50 | YES | - |
| client_dept | character varying | 100 | YES | - |
| client_contact | character varying | 50 | YES | - |
| client_contact_phone | character varying | 50 | YES | - |
| prime_contractor | character varying | 200 | YES | - |
| subcontract_amount | bigint | - | YES | - |
| subcontract_type | character varying | 100 | YES | - |
| purchase_order_no | character varying | 100 | YES | - |
| created_at | timestamp without time zone | - | NO | now() |
| updated_at | timestamp without time zone | - | NO | now() |

### tb_contract_participant  (rows: 0)

| column | type | len | null | default |
|---|---|---|---|---|
| participant_id | integer | - | NO | nextval('tb_contract_participant_participant_id_seq'::regclass) |
| contract_id | integer | - | NO | - |
| user_id | bigint | - | YES | - |
| role_type | character varying | 20 | NO | 'PARTICIPANT'::character varying |
| tech_grade | character varying | 20 | YES | - |
| task_desc | character varying | 500 | YES | - |
| is_site_rep | boolean | - | NO | false |
| sort_order | integer | - | YES | 0 |
| proj_id | bigint | - | YES | - |

### tb_contract_target  (rows: 0)

| column | type | len | null | default |
|---|---|---|---|---|
| target_id | integer | - | NO | nextval('tb_contract_target_target_id_seq'::regclass) |
| contract_id | integer | - | NO | - |
| target_category | character varying | 30 | YES | - |
| product_name | character varying | 200 | NO | - |
| product_detail | character varying | 500 | YES | - |
| quantity | integer | - | YES | 1 |
| sw_id | bigint | - | YES | - |
| sort_order | integer | - | YES | 0 |

### tb_document  (rows: 15)

| column | type | len | null | default |
|---|---|---|---|---|
| doc_id | integer | - | NO | nextval('tb_document_doc_id_seq'::regclass) |
| doc_no | character varying | 50 | YES | - |
| doc_type | character varying | 30 | NO | - |
| sys_type | character varying | 20 | YES | - |
| infra_id | bigint | - | YES | - |
| plan_id | integer | - | YES | - |
| contract_id | integer | - | YES | - |
| title | character varying | 500 | NO | - |
| status | character varying | 20 | NO | 'DRAFT'::character varying |
| author_id | bigint | - | NO | - |
| approver_id | bigint | - | YES | - |
| approved_at | timestamp without time zone | - | YES | - |
| created_at | timestamp without time zone | - | NO | now() |
| updated_at | timestamp without time zone | - | NO | now() |
| proj_id | bigint | - | YES | - |
| support_target_type | character varying | 20 | YES | - |
| org_unit_id | bigint | - | YES | - |
| environment | character varying | 20 | YES | - |
| region_code | character varying | 10 | YES | - |

### tb_document_attachment  (rows: 0)

| column | type | len | null | default |
|---|---|---|---|---|
| attach_id | integer | - | NO | nextval('tb_document_attachment_attach_id_seq'::regclass) |
| doc_id | integer | - | NO | - |
| file_name | character varying | 300 | NO | - |
| file_path | character varying | 500 | NO | - |
| file_size | bigint | - | YES | - |
| mime_type | character varying | 100 | YES | - |
| uploaded_at | timestamp without time zone | - | NO | now() |

### tb_document_detail  (rows: 40)

| column | type | len | null | default |
|---|---|---|---|---|
| detail_id | integer | - | NO | nextval('tb_document_detail_detail_id_seq'::regclass) |
| doc_id | integer | - | NO | - |
| section_key | character varying | 50 | NO | - |
| section_data | jsonb | - | NO | '{}'::jsonb |
| sort_order | integer | - | YES | 0 |

### tb_document_history  (rows: 13)

| column | type | len | null | default |
|---|---|---|---|---|
| history_id | integer | - | NO | nextval('tb_document_history_history_id_seq'::regclass) |
| doc_id | integer | - | NO | - |
| action | character varying | 30 | NO | - |
| changed_field | character varying | 100 | YES | - |
| old_value | text | - | YES | - |
| new_value | text | - | YES | - |
| actor_id | bigint | - | NO | - |
| comment | text | - | YES | - |
| created_at | timestamp without time zone | - | NO | now() |

### tb_document_signature  (rows: 0)

| column | type | len | null | default |
|---|---|---|---|---|
| sign_id | integer | - | NO | nextval('tb_document_signature_sign_id_seq'::regclass) |
| doc_id | integer | - | NO | - |
| signer_type | character varying | 30 | NO | - |
| signer_name | character varying | 50 | NO | - |
| signer_org | character varying | 200 | YES | - |
| signature_image | character varying | 500 | YES | - |
| signed_at | timestamp without time zone | - | YES | - |
| created_at | timestamp without time zone | - | NO | now() |

### tb_infra_link_api  (rows: 43)

| column | type | len | null | default |
|---|---|---|---|---|
| api_id | bigint | - | NO | nextval('tb_infra_link_api_api_id_seq'::regclass) |
| infra_id | bigint | - | NO | - |
| naver_news_key | character varying | 300 | YES | - |
| naver_news_req_dt | date | - | YES | - |
| naver_news_exp_dt | date | - | YES | - |
| naver_news_user | character varying | 50 | YES | - |
| naver_secret_key | character varying | 300 | YES | - |
| naver_secret_req_dt | date | - | YES | - |
| naver_secret_exp_dt | date | - | YES | - |
| naver_secret_user | character varying | 50 | YES | - |
| public_data_key | character varying | 200 | YES | - |
| public_data_req_dt | date | - | YES | - |
| public_data_exp_dt | date | - | YES | - |
| public_data_user | character varying | 50 | YES | - |
| kras_key | character varying | 200 | YES | - |
| kras_req_dt | date | - | YES | - |
| kras_exp_dt | date | - | YES | - |
| kras_user | character varying | 50 | YES | - |
| kgeo_key | character varying | 300 | YES | - |
| kgeo_req_dt | date | - | YES | - |
| kgeo_exp_dt | date | - | YES | - |
| kgeo_user | character varying | 50 | YES | - |
| vworld_key | character varying | 200 | YES | - |
| vworld_req_dt | date | - | YES | - |
| vworld_exp_dt | date | - | YES | - |
| vworld_user | character varying | 50 | YES | - |
| kakao_key | character varying | 200 | YES | - |
| kakao_req_dt | date | - | YES | - |
| kakao_exp_dt | date | - | YES | - |
| kakao_user | character varying | 50 | YES | - |

### tb_infra_link_upis  (rows: 92)

| column | type | len | null | default |
|---|---|---|---|---|
| link_id | bigint | - | NO | nextval('tb_infra_link_upis_link_id_seq'::regclass) |
| infra_id | bigint | - | NO | - |
| kras_ip | character varying | 50 | YES | - |
| kras_cd | character varying | 50 | YES | - |
| gpki_id | character varying | 50 | YES | - |
| gpki_pw | character varying | 200 | YES | - |
| minwon_ip | character varying | 200 | YES | - |
| minwon_link_cd | character varying | 50 | YES | - |
| minwon_key | character varying | 200 | YES | - |
| doc_ip | character varying | 200 | YES | - |
| doc_adm_id | character varying | 50 | YES | - |
| doc_id | character varying | 200 | YES | - |

### tb_infra_master  (rows: 204)

| column | type | len | null | default |
|---|---|---|---|---|
| infra_id | bigint | - | NO | nextval('tb_infra_master_infra_id_seq'::regclass) |
| city_nm | character varying | 50 | YES | - |
| dist_nm | character varying | 50 | YES | - |
| sys_nm | character varying | 100 | YES | - |
| sys_nm_en | character varying | 100 | YES | - |
| org_cd | character varying | 20 | YES | - |
| dist_cd | character varying | 20 | YES | - |
| reg_dt | timestamp without time zone | - | YES | now() |
| mod_dt | timestamp without time zone | - | YES | - |
| infra_type | character varying | 20 | YES | 'PROD'::character varying |

### tb_infra_memo  (rows: 2)

| column | type | len | null | default |
|---|---|---|---|---|
| memo_id | bigint | - | NO | nextval('tb_infra_memo_memo_id_seq'::regclass) |
| infra_id | bigint | - | NO | - |
| memo_content | text | - | YES | - |
| memo_writer | character varying | 50 | YES | - |
| memo_date | date | - | YES | - |

### tb_infra_server  (rows: 331)

| column | type | len | null | default |
|---|---|---|---|---|
| server_id | bigint | - | NO | nextval('tb_infra_server_server_id_seq'::regclass) |
| infra_id | bigint | - | NO | - |
| server_type | character varying | 10 | NO | - |
| ip_addr | character varying | 200 | YES | - |
| acc_id | character varying | 200 | YES | - |
| acc_pw | character varying | 200 | YES | - |
| os_nm | character varying | 200 | YES | - |
| mac_addr | character varying | 200 | YES | - |
| server_model | character varying | 200 | YES | - |
| serial_no | character varying | 100 | YES | - |
| cpu_spec | character varying | 200 | YES | - |
| memory_spec | character varying | 100 | YES | - |
| disk_spec | character varying | 300 | YES | - |
| network_spec | character varying | 200 | YES | - |
| power_spec | character varying | 100 | YES | - |
| os_detail | character varying | 300 | YES | - |
| rack_location | character varying | 100 | YES | - |
| note | text | - | YES | - |

### tb_infra_software  (rows: 983)

| column | type | len | null | default |
|---|---|---|---|---|
| sw_id | bigint | - | NO | nextval('tb_infra_software_sw_id_seq'::regclass) |
| server_id | bigint | - | NO | - |
| sw_category | character varying | 20 | YES | - |
| sw_nm | character varying | 100 | YES | - |
| sw_ver | character varying | 50 | YES | - |
| port | character varying | 200 | YES | - |
| sw_acc_id | character varying | 100 | YES | - |
| sw_acc_pw | character varying | 200 | YES | - |
| sid | character varying | 100 | YES | - |
| install_path | character varying | 500 | YES | - |

### tb_inspect_checklist  (rows: 0)

| column | type | len | null | default |
|---|---|---|---|---|
| check_id | integer | - | NO | nextval('tb_inspect_checklist_check_id_seq'::regclass) |
| doc_id | integer | - | NO | - |
| inspect_month | integer | - | YES | - |
| target_sw | character varying | 50 | YES | - |
| check_item | character varying | 300 | NO | - |
| check_method | character varying | 500 | YES | - |
| check_result | character varying | 20 | YES | 'NORMAL'::character varying |
| sort_order | integer | - | YES | 0 |
| created_at | timestamp without time zone | - | NO | now() |

### tb_inspect_issue  (rows: 0)

| column | type | len | null | default |
|---|---|---|---|---|
| issue_id | integer | - | NO | nextval('tb_inspect_issue_issue_id_seq'::regclass) |
| doc_id | integer | - | NO | - |
| issue_year | integer | - | YES | - |
| issue_month | integer | - | YES | - |
| issue_day | integer | - | YES | - |
| task_type | character varying | 50 | YES | - |
| symptom | text | - | YES | - |
| action_taken | text | - | YES | - |
| created_at | timestamp without time zone | - | NO | now() |

### tb_org_unit  (rows: 39)

| column | type | len | null | default |
|---|---|---|---|---|
| unit_id | bigint | - | NO | nextval('tb_org_unit_unit_id_seq'::regclass) |
| parent_id | bigint | - | YES | - |
| unit_type | character varying | 20 | NO | - |
| name | character varying | 100 | NO | - |
| sort_order | integer | - | YES | 0 |
| use_yn | character varying | 1 | YES | 'Y'::character varying |
| created_at | timestamp without time zone | - | NO | now() |
| updated_at | timestamp without time zone | - | NO | now() |

### tb_performance_summary  (rows: 0)

| column | type | len | null | default |
|---|---|---|---|---|
| summary_id | integer | - | NO | nextval('tb_performance_summary_summary_id_seq'::regclass) |
| user_id | bigint | - | NO | - |
| period_year | integer | - | NO | - |
| period_month | integer | - | NO | - |
| install_count | integer | - | YES | 0 |
| patch_count | integer | - | YES | 0 |
| fault_count | integer | - | YES | 0 |
| fault_avg_hours | numeric | - | YES | - |
| support_count | integer | - | YES | 0 |
| inspect_plan_count | integer | - | YES | 0 |
| inspect_done_count | integer | - | YES | 0 |
| interim_count | integer | - | YES | 0 |
| completion_count | integer | - | YES | 0 |
| infra_count | integer | - | YES | 0 |
| plan_total_count | integer | - | YES | 0 |
| plan_ontime_count | integer | - | YES | 0 |
| calculated_at | timestamp without time zone | - | NO | now() |
| updated_at | timestamp without time zone | - | NO | now() |

### tb_pjt_manpower_plan  (rows: 6)

| column | type | len | null | default |
|---|---|---|---|---|
| id | bigint | - | NO | nextval('tb_pjt_manpower_plan_id_seq'::regclass) |
| proj_id | bigint | - | NO | - |
| step_name | character varying | 200 | NO | - |
| start_dt | date | - | YES | - |
| end_dt | date | - | YES | - |
| grade_special | integer | - | YES | 0 |
| grade_high | integer | - | YES | 0 |
| grade_mid | integer | - | YES | 0 |
| grade_low | integer | - | YES | 0 |
| func_high | integer | - | YES | 0 |
| func_mid | integer | - | YES | 0 |
| func_low | integer | - | YES | 0 |
| remark | character varying | 300 | YES | - |
| sort_order | integer | - | YES | 0 |

### tb_pjt_schedule  (rows: 6)

| column | type | len | null | default |
|---|---|---|---|---|
| id | bigint | - | NO | nextval('tb_pjt_schedule_id_seq'::regclass) |
| proj_id | bigint | - | NO | - |
| process_name | character varying | 200 | NO | - |
| m01 | boolean | - | YES | false |
| m02 | boolean | - | YES | false |
| m03 | boolean | - | YES | false |
| m04 | boolean | - | YES | false |
| m05 | boolean | - | YES | false |
| m06 | boolean | - | YES | false |
| m07 | boolean | - | YES | false |
| m08 | boolean | - | YES | false |
| m09 | boolean | - | YES | false |
| m10 | boolean | - | YES | false |
| m11 | boolean | - | YES | false |
| m12 | boolean | - | YES | false |
| remark | character varying | 300 | YES | - |
| sort_order | integer | - | YES | 0 |

### tb_pjt_target  (rows: 18)

| column | type | len | null | default |
|---|---|---|---|---|
| id | bigint | - | NO | nextval('tb_pjt_target_id_seq'::regclass) |
| proj_id | bigint | - | NO | - |
| product_name | character varying | 200 | NO | - |
| qty | integer | - | NO | 1 |
| sort_order | integer | - | YES | 0 |

### tb_process_master  (rows: 1450)

| column | type | len | null | default |
|---|---|---|---|---|
| process_id | integer | - | NO | nextval('tb_process_master_process_id_seq'::regclass) |
| sys_nm_en | character varying | 30 | YES | - |
| process_name | character varying | 200 | YES | - |
| sort_order | integer | - | YES | 0 |
| use_yn | character varying | 1 | YES | 'Y'::character varying |

### tb_service_purpose  (rows: 1450)

| column | type | len | null | default |
|---|---|---|---|---|
| purpose_id | integer | - | NO | nextval('tb_service_purpose_purpose_id_seq'::regclass) |
| sys_nm_en | character varying | 30 | YES | - |
| purpose_type | character varying | 20 | YES | - |
| purpose_text | text | - | YES | - |
| sort_order | integer | - | YES | 0 |
| use_yn | character varying | 1 | YES | 'Y'::character varying |

### tb_work_plan  (rows: 1)

| column | type | len | null | default |
|---|---|---|---|---|
| plan_id | integer | - | NO | nextval('tb_work_plan_plan_id_seq'::regclass) |
| infra_id | bigint | - | YES | - |
| plan_type | character varying | 30 | NO | - |
| process_step | integer | - | YES | - |
| title | character varying | 300 | NO | - |
| description | text | - | YES | - |
| assignee_id | bigint | - | YES | - |
| start_date | date | - | NO | - |
| end_date | date | - | YES | - |
| location | character varying | 300 | YES | - |
| repeat_type | character varying | 20 | NO | 'NONE'::character varying |
| parent_plan_id | integer | - | YES | - |
| status | character varying | 20 | NO | 'PLANNED'::character varying |
| status_reason | character varying | 500 | YES | - |
| created_at | timestamp without time zone | - | NO | now() |
| updated_at | timestamp without time zone | - | NO | now() |
| created_by | bigint | - | YES | - |

### users  (rows: 10)

| column | type | len | null | default |
|---|---|---|---|---|
| user_id | bigint | - | NO | nextval('users_user_id_seq'::regclass) |
| userid | character varying | 50 | NO | - |
| password | character varying | 100 | NO | - |
| org_nm | character varying | 100 | YES | - |
| dept_nm | character varying | 100 | YES | - |
| team_nm | character varying | 100 | YES | - |
| tel | character varying | 20 | YES | - |
| email | character varying | 100 | YES | - |
| user_role | character varying | 20 | YES | 'ROLE_USER'::character varying |
| enabled | boolean | - | YES | false |
| auth_dashboard | character varying | 10 | YES | 'NONE'::character varying |
| auth_project | character varying | 10 | YES | 'NONE'::character varying |
| auth_person | character varying | 10 | YES | 'NONE'::character varying |
| reg_dt | timestamp without time zone | - | YES | now() |
| username | character varying | 50 | YES | - |
| auth_infra | character varying | 10 | YES | 'NONE'::character varying |
| auth_license | character varying | 10 | YES | 'NONE'::character varying |
| auth_quotation | character varying | 10 | YES | 'NONE'::character varying |
| failed_attempts | integer | - | YES | 0 |
| lock_time | timestamp without time zone | - | YES | - |
| auth_work_plan | character varying | 20 | YES | 'NONE'::character varying |
| auth_document | character varying | 20 | YES | 'NONE'::character varying |
| auth_contract | character varying | 20 | YES | 'NONE'::character varying |
| auth_performance | character varying | 20 | YES | 'NONE'::character varying |
| position | character varying | 50 | YES | - |
| tech_grade | character varying | 20 | YES | - |
| address | character varying | 300 | YES | - |
| certificate | character varying | 500 | YES | - |
| mobile | character varying | 20 | YES | - |
| position_title | character varying | 50 | YES | - |
| ssn | character varying | 14 | YES | - |
| tasks | character varying | 1000 | YES | - |
| field_role | character varying | 50 | YES | - |
| career_years | character varying | 20 | YES | - |

