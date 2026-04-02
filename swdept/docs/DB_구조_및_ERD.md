# SW Manager 데이터베이스 구조 및 ERD

> **DB**: PostgreSQL
> **스키마**: public
> **DB명**: SW_Dept
> **최종 갱신일**: 2026-03-17

---

## 1. ERD (Entity Relationship Diagram)

```
┌─────────────────┐
│    sys_mst       │◄──────────────────────────────────┐
│─────────────────│                                    │
│ PK cd (varchar)  │◄─────────────┐                    │
│    nm            │              │                    │
└─────────────────┘              │                    │
                                 │                    │
┌─────────────────┐              │                    │
│  cont_stat_mst   │◄──┐         │                    │
│─────────────────│    │         │                    │
│ PK cd (varchar)  │    │         │                    │
│    nm            │    │         │                    │
└─────────────────┘    │         │                    │
                       │         │                    │
┌─────────────────┐    │         │                    │
│  cont_frm_mst   │    │         │                    │
│─────────────────│    │         │                    │
│ PK cd (varchar)  │    │         │                    │
│    nm            │    │         │                    │
└─────────────────┘    │         │                    │
                       │         │                    │
┌─────────────────┐    │         │                    │
│   prj_types      │◄┐ │         │                    │
│─────────────────│  │ │         │                    │
│ PK cd (varchar)  │  │ │         │                    │
│    nm            │  │ │         │                    │
└─────────────────┘  │ │         │                    │
                     │ │         │                    │
┌─────────────────┐  │ │         │                    │
│  maint_tp_mst   │◄┐│ │         │                    │
│─────────────────│ ││ │         │                    │
│ PK cd (varchar)  │ ││ │         │                    │
│    nm            │ ││ │         │                    │
└─────────────────┘ ││ │         │                    │
                    ││ │         │                    │
┌─────────────────┐ ││ │         │                    │
│  sigungu_code   │◄┤│ │         │                    │
│─────────────────│ ││ │         │                    │
│ PK adm_sect_c   │ ││ │         │                    │
│    instt_c      │ ││ │         │                    │
│    full_name    │ ││ │         │                    │
│    sido_name    │ ││ │         │                    │
│    sgg_name     │ ││ │         │                    │
└─────────────────┘ ││ │         │                    │
                    ││ │         │                    │
                    ││ │         │                    │
┌───────────────────────────────────────────────┐     │
│                   sw_pjt                       │     │
│───────────────────────────────────────────────│     │
│ PK proj_id (serial)                            │     │
│    year, pms_cd, city_nm, dist_nm, org_nm      │     │
│    org_cd, org_lgh_nm                          │     │
│ FK dist_cd ──────────────────► sigungu_code    │─────┤
│ FK biz_cat_en ───────────────► prj_types       │     │
│ FK sys_nm_en ────────────────► sys_mst         │     │
│    sys_nm, proj_nm, client                     │     │
│    cont_ent, cont_dept, cont_type              │     │
│    cont_dt, start_dt, end_dt, inst_dt          │     │
│    budg_amt, sw_rt, cont_amt, cont_rt          │     │
│    sw_amt, cnslt_amt, db_impl_amt              │     │
│    pkg_sw_amt, sys_dev_amt, hw_amt, etc_amt    │     │
│    outscr_amt                                  │     │
│ FK stat ─────────────────────► cont_stat_mst   │     │
│    pre_pay, pay_prog, pay_comp                 │     │
│ FK maint_type ───────────────► maint_tp_mst    │     │
│    pay_prog_yn, pay_prog_fr                    │     │
│    person_id (→ users.user_id, 논리적 FK)       │     │
│    biz_type, biz_cat                           │     │
│    reg_dt                                      │     │
└───────────────────────────────────────────────┘     │
                                                      │
┌───────────────────────────────────────────────┐     │
│                   ps_info                      │     │
│───────────────────────────────────────────────│     │
│ PK id (serial)                                 │     │
│ FK sys_nm_en ────────────────► sys_mst         │─────┘
│    sys_nm, org_nm, dept_nm, team_nm            │
│    pos, user_nm, email, tel                    │
│    city_nm, dist_nm, org_cd, dist_cd           │
│    reg_dt                                      │
└───────────────────────────────────────────────┘


┌─────────────────────────────────────────────┐
│               qt_quotation                   │
│─────────────────────────────────────────────│
│ PK quote_id (serial)                         │
│ UQ quote_number                              │
│    quote_date, category                      │
│    project_name, recipient, reference_to     │
│    total_amount, total_amount_text           │
│    vat_included, status                      │
│    rounddown_unit, show_seal, template_type  │
│    remarks, grand_total, bid_rate            │
│    created_by, created_at, updated_at        │
└──────────────┬──────────────────────────────┘
               │ 1:N
               ▼
┌─────────────────────────────────────────────┐
│            qt_quotation_item                 │
│─────────────────────────────────────────────│
│ PK item_id (serial)                          │
│ FK quote_id ────────────► qt_quotation       │
│    item_no, product_name, specification      │
│    quantity, unit, unit_price, amount         │
│    remarks                                   │
└─────────────────────────────────────────────┘

┌─────────────────────────────────────────────┐
│           qt_quotation_ledger                │
│─────────────────────────────────────────────│
│ PK ledger_id (serial)                        │
│    quote_id (논리적 참조 → qt_quotation)       │
│    ledger_no, year, category                 │
│    quote_number, quote_date                  │
│    project_name, total_amount, grand_total   │
│    recipient, reference_to                   │
│    created_by, registered_at                 │
└─────────────────────────────────────────────┘

┌─────────────────────────────────────────────┐
│          qt_quote_number_seq                 │
│─────────────────────────────────────────────│
│ PK seq_id (serial)                           │
│ UQ (year, category)                          │
│    last_seq                                  │
└─────────────────────────────────────────────┘

┌─────────────────────────────────────────────┐
│          qt_product_pattern                  │
│─────────────────────────────────────────────│
│ PK pattern_id (serial)                       │
│    category, pattern_group                   │
│    product_name, default_unit                │
│    default_unit_price, description           │
│    sub_items (text), usage_count             │
│    calc_type, overhead_rate, tech_fee_rate   │
└─────────────────────────────────────────────┘

┌─────────────────────────────────────────────┐
│          qt_remarks_pattern                  │
│─────────────────────────────────────────────│
│ PK pattern_id (serial)                       │
│    pattern_name, content (text)              │
│    sort_order                                │
└─────────────────────────────────────────────┘

┌─────────────────────────────────────────────┐
│             qt_wage_rate                     │
│─────────────────────────────────────────────│
│ PK wage_id (serial)                          │
│ UQ (year, grade_name)                        │
│    daily_rate, monthly_rate, hourly_rate     │
│    description                               │
└─────────────────────────────────────────────┘


┌─────────────────────────────────────────────┐
│            tb_infra_master                   │
│─────────────────────────────────────────────│
│ PK infra_id (serial)                         │
│    city_nm, dist_nm, sys_nm, sys_nm_en       │
│    org_cd, dist_cd                           │
│    infra_type (기본: PROD)                    │
│    reg_dt, mod_dt                            │
└──┬────────────┬──────────┬──────────────────┘
   │ 1:N        │ 1:N      │ 1:1          1:1
   ▼            ▼          ▼              │
┌──────────┐ ┌────────┐ ┌──────────┐     ▼
│tb_infra  │ │tb_infra│ │tb_infra  │ ┌───────────┐
│_server   │ │_memo   │ │_link_upis│ │tb_infra   │
│──────────│ │────────│ │──────────│ │_link_api  │
│PK server │ │PK memo │ │PK link_id│ │───────────│
│   _id    │ │  _id   │ │FK infra  │ │PK api_id  │
│FK infra  │ │FK infra│ │  _id     │ │FK infra_id│
│  _id     │ │  _id   │ │kras_ip   │ │naver_*    │
│server    │ │memo    │ │kras_cd   │ │public_*   │
│ _type    │ │_content│ │gpki_*    │ │kras_*     │
│ip_addr   │ │_writer │ │minwon_*  │ │kgeo_*     │
│acc_id/pw │ │_date   │ │doc_*     │ │vworld_*   │
│os_nm     │ └────────┘ └──────────┘ │kakao_*    │
│mac_addr  │                          └───────────┘
└────┬─────┘
     │ 1:N
     ▼
┌─────────────────────────────────────────────┐
│           tb_infra_software                  │
│─────────────────────────────────────────────│
│ PK sw_id (serial)                            │
│ FK server_id ────────► tb_infra_server       │
│    sw_category, sw_nm, sw_ver                │
│    port, sw_acc_id, sw_acc_pw                │
│    sid, install_path                         │
└─────────────────────────────────────────────┘


┌─────────────────────────────────────────────┐
│                  users                       │
│─────────────────────────────────────────────│
│ PK user_id (serial)                          │
│ UQ userid                                    │
│    username, password                        │
│    org_nm, dept_nm, team_nm                  │
│    tel, email                                │
│    user_role (기본: ROLE_USER)                │
│    enabled (기본: false)                      │
│    auth_dashboard (기본: NONE)                │
│    auth_project (기본: NONE)                  │
│    auth_person (기본: NONE)                   │
│    auth_infra (기본: NONE)                    │
│    auth_license (기본: NONE)                  │
│    auth_quotation (기본: NONE)                │
│    failed_attempts (기본: 0)                  │
│    lock_time                                 │
│    reg_dt                                    │
└─────────────────────────────────────────────┘

┌─────────────────────────────────────────────┐
│              access_logs                     │
│─────────────────────────────────────────────│
│ PK log_id (serial)                           │
│    userid, username                          │
│    ip_addr, access_time (기본: now())         │
│    menu_nm, action_type, action_detail       │
└─────────────────────────────────────────────┘

┌─────────────────────────────────────────────┐
│           license_registry                   │
│─────────────────────────────────────────────│
│ PK id (bigserial)                            │
│ UQ (license_id, product_id)                  │
│    license_type, valid_product_edition        │
│    valid_product_version                     │
│    validity_period, maintenance_period       │
│    generation_source, generation_date_time   │
│    quantity, allowed_use_count               │
│    hardware_id, registered_to, full_name     │
│    email, company, telephone, fax            │
│    street, city, zip_code, country           │
│    activation_required, auto_activations_*   │
│    manual_activations_*, online_key_lease_*  │
│    deactivations_*, activation_period        │
│    allowed_activation/deactivation_count     │
│    ip_blocks, ip_blocks_allow                │
│    floating_*, superseded_license_ids        │
│    ntp_*, web_server_*, date_time_check      │
│    unsigned/signed_custom_features           │
│    license_string (NOT NULL)                 │
│    upload_date, uploaded_by, created_date    │
└─────────────────────────────────────────────┘

┌─────────────────────────────────────────────┐
│         license_upload_history               │
│─────────────────────────────────────────────│
│ PK id (bigserial)                            │
│    upload_date, file_name                    │
│    total_count, success_count, fail_count    │
│    uploaded_by, created_date                 │
└─────────────────────────────────────────────┘

┌─────────────────────────────────────────────┐
│           geonuris_license                   │
│─────────────────────────────────────────────│
│ PK id (serial)                               │
│    license_type, file_name                   │
│    user_name, organization, phone, email     │
│    issuer, mac_address (NOT NULL)            │
│    permission, start_date, expiry_date       │
│    dbms_type, setl_count                     │
│    plugin_edit, plugin_gdm                   │
│    plugin_tmbuilder, plugin_setl             │
│    license_data, created_at, created_by      │
└─────────────────────────────────────────────┘

┌─────────────────────────────────────────────┐
│              qr_license                      │
│─────────────────────────────────────────────│
│ PK qr_id (bigserial)                         │
│    end_user_name, address, tel, fax          │
│    products, user_units, version             │
│    license_type, serial_number               │
│    application_name                          │
│    (위 필드 모두 _ko 한글 버전 존재)            │
│    qr_image_data (text, Base64)              │
│    issued_by, issued_at, remarks             │
└─────────────────────────────────────────────┘
```

---

## 2. 테이블 상세 정의

### 2.1 사용자 / 인증

#### users (사용자 계정)
| 컬럼명 | 타입 | NULL | 기본값 | 설명 |
|--------|------|------|--------|------|
| user_id | serial | NO | auto | PK |
| userid | varchar(50) | NO | - | 로그인 ID (UNIQUE) |
| username | varchar(50) | YES | - | 실명 |
| password | varchar(100) | NO | - | BCrypt 해시 |
| org_nm | varchar(100) | YES | - | 소속 기관 |
| dept_nm | varchar(100) | YES | - | 부서명 |
| team_nm | varchar(100) | YES | - | 팀명 |
| tel | varchar(20) | YES | - | 연락처 |
| email | varchar(100) | YES | - | 이메일 |
| user_role | varchar(20) | YES | ROLE_USER | ROLE_USER / ROLE_ADMIN |
| enabled | boolean | YES | false | 계정 활성화 (관리자 승인) |
| auth_dashboard | varchar(10) | YES | NONE | NONE / VIEW / EDIT |
| auth_project | varchar(10) | YES | NONE | NONE / VIEW / EDIT |
| auth_person | varchar(10) | YES | NONE | NONE / VIEW / EDIT |
| auth_infra | varchar(10) | YES | NONE | NONE / VIEW / EDIT |
| auth_license | varchar(10) | YES | NONE | NONE / VIEW / EDIT |
| auth_quotation | varchar(10) | YES | NONE | NONE / VIEW / EDIT |
| failed_attempts | integer | YES | 0 | 로그인 실패 횟수 |
| lock_time | timestamp | YES | - | 계정 잠금 시각 |
| reg_dt | timestamp | YES | now() | 등록일시 |

#### access_logs (접근 로그)
| 컬럼명 | 타입 | NULL | 기본값 | 설명 |
|--------|------|------|--------|------|
| log_id | serial | NO | auto | PK |
| userid | varchar(50) | YES | - | 사용자 ID |
| username | varchar(50) | YES | - | 사용자 실명 |
| ip_addr | varchar(45) | YES | - | 접속 IP |
| access_time | timestamp | YES | now() | 접속 시각 |
| menu_nm | varchar(100) | YES | - | 메뉴명 |
| action_type | varchar(50) | YES | - | 행위 유형 (조회/등록/수정/삭제) |
| action_detail | text | YES | - | 상세 내용 |

---

### 2.2 사업관리

#### sw_pjt (사업 프로젝트)
| 컬럼명 | 타입 | NULL | 기본값 | FK | 설명 |
|--------|------|------|--------|-----|------|
| proj_id | serial | NO | auto | - | PK |
| year | integer | YES | - | - | 사업 연도 |
| pms_cd | varchar(50) | YES | - | - | PMS 코드 |
| city_nm | varchar(50) | YES | - | - | 시도명 |
| dist_nm | varchar(200) | YES | - | - | 시군구명 |
| org_nm | varchar(50) | YES | - | - | 발주기관명 |
| org_cd | varchar(50) | YES | - | - | 기관 코드 |
| org_lgh_nm | varchar(50) | YES | 기본값 | - | 기관 약칭 |
| dist_cd | varchar(20) | YES | - | sigungu_code.adm_sect_c | 시군구 코드 |
| biz_type | varchar(100) | YES | - | - | 사업 분류 |
| biz_cat | varchar(100) | YES | - | - | 사업 유형명 |
| biz_cat_en | varchar(10) | YES | - | prj_types.cd | 사업 유형 코드 |
| sys_nm | varchar(200) | YES | - | - | 시스템명 |
| sys_nm_en | varchar(10) | YES | - | sys_mst.cd | 시스템 코드 |
| proj_nm | varchar(200) | YES | - | - | 사업명 |
| client | varchar(200) | YES | - | - | 클라이언트 |
| cont_ent | varchar(200) | YES | - | - | 계약 업체 |
| cont_dept | varchar(200) | YES | - | - | 계약 부서 |
| cont_type | varchar(10) | YES | - | - | 계약 형태 코드 |
| cont_dt | date | YES | - | - | 계약일 |
| start_dt | date | YES | - | - | 착수일 |
| end_dt | date | YES | - | - | 완료일 |
| inst_dt | date | YES | - | - | 설치일 |
| budg_amt | bigint | YES | - | - | 예산액 |
| sw_rt | numeric | YES | - | - | SW 비율 |
| cont_amt | bigint | YES | - | - | 계약금액 |
| cont_rt | numeric | YES | - | - | 계약 비율 |
| sw_amt | bigint | YES | - | - | SW 금액 |
| cnslt_amt | bigint | YES | - | - | 컨설팅 금액 |
| db_impl_amt | bigint | YES | - | - | DB 구축 금액 |
| pkg_sw_amt | bigint | YES | - | - | 패키지SW 금액 |
| sys_dev_amt | bigint | YES | - | - | 시스템개발 금액 |
| hw_amt | bigint | YES | - | - | HW 금액 |
| etc_amt | bigint | YES | - | - | 기타 금액 |
| outscr_amt | bigint | YES | - | - | 외주 금액 |
| stat | varchar(10) | YES | - | cont_stat_mst.cd | 계약상태 코드 |
| pre_pay | bigint | YES | - | - | 선급금 |
| pay_prog | bigint | YES | - | - | 기성금 |
| pay_comp | bigint | YES | - | - | 준공금 |
| maint_type | varchar(10) | YES | - | maint_tp_mst.cd | 유지보수 유형 |
| pay_prog_yn | varchar(10) | YES | - | - | 기성 진행 여부 |
| pay_prog_fr | varchar(10) | YES | - | - | 기성 진행율 |
| person_id | bigint | YES | - | (논리적→users.user_id) | 담당자 |
| reg_dt | timestamp | YES | now() | - | 등록일시 |

#### ps_info (담당자 정보)
| 컬럼명 | 타입 | NULL | 기본값 | FK | 설명 |
|--------|------|------|--------|-----|------|
| id | serial | NO | auto | - | PK |
| sys_nm_en | varchar(10) | YES | - | sys_mst.cd | 시스템 코드 |
| sys_nm | varchar(200) | YES | - | - | 시스템명 |
| org_nm | varchar(100) | YES | - | - | 기관명 |
| dept_nm | varchar(100) | YES | - | - | 부서명 |
| team_nm | varchar(100) | YES | - | - | 팀명 |
| pos | varchar(50) | YES | - | - | 직급 |
| user_nm | varchar(50) | YES | - | - | 성명 |
| email | varchar(100) | YES | - | - | 이메일 |
| tel | varchar(20) | YES | - | - | 연락처 |
| city_nm | varchar(50) | YES | - | - | 시도명 |
| dist_nm | varchar(200) | YES | - | - | 시군구명 |
| org_cd | varchar(50) | YES | - | - | 기관코드 |
| dist_cd | varchar(20) | YES | - | - | 지역코드 |
| reg_dt | timestamp | YES | now() | - | 등록일시 |

---

### 2.3 코드 마스터 테이블

모두 동일 구조: `cd (PK, varchar) + nm (varchar)`

| 테이블명 | 설명 | 참조하는 테이블 |
|----------|------|----------------|
| sys_mst | 시스템명 코드 | sw_pjt.sys_nm_en, ps_info.sys_nm_en |
| cont_stat_mst | 계약상태 코드 | sw_pjt.stat |
| cont_frm_mst | 계약형태 코드 | (UI에서 참조) |
| prj_types | 사업유형 코드 | sw_pjt.biz_cat_en |
| maint_tp_mst | 유지보수유형 코드 | sw_pjt.maint_type |

#### sigungu_code (시군구 코드)
| 컬럼명 | 타입 | NULL | 설명 |
|--------|------|------|------|
| adm_sect_c | varchar(20) | NO | PK, 행정구역코드 |
| instt_c | varchar(20) | YES | 기관코드 |
| full_name | varchar(100) | YES | 전체 지역명 |
| sido_name | varchar(50) | YES | 시도명 |
| sgg_name | varchar(50) | YES | 시군구명 |

---

### 2.4 견적서

#### qt_quotation (견적서)
| 컬럼명 | 타입 | NULL | 기본값 | 설명 |
|--------|------|------|--------|------|
| quote_id | serial | NO | auto | PK |
| quote_number | varchar(30) | NO | - | 견적번호 (UNIQUE) |
| quote_date | date | NO | - | 견적일자 |
| category | varchar(10) | NO | - | 분류 (SW/HW/용역 등) |
| project_name | varchar(500) | NO | - | 건명 |
| recipient | varchar(200) | YES | - | 수신 |
| reference_to | varchar(200) | YES | - | 참조 |
| total_amount | bigint | YES | 0 | 합계금액 |
| total_amount_text | varchar(200) | YES | - | 금액 한글 표기 |
| vat_included | boolean | YES | true | 부가세 포함 여부 |
| status | varchar(10) | YES | 작성중 | 상태 (작성중/발행) |
| rounddown_unit | integer | YES | 1 | 절사 단위 |
| show_seal | boolean | YES | true | 도장 표시 여부 |
| template_type | integer | YES | 1 | 양식 (1/2) |
| remarks | varchar(2000) | YES | - | 비고 |
| grand_total | bigint | YES | - | 투찰금액 |
| bid_rate | double | YES | - | 투찰율 |
| created_by | varchar(50) | YES | - | 작성자 |
| created_at | timestamp | YES | now() | 작성일시 |
| updated_at | timestamp | YES | now() | 수정일시 |

#### qt_quotation_item (견적 품목)
| 컬럼명 | 타입 | NULL | 기본값 | FK | 설명 |
|--------|------|------|--------|-----|------|
| item_id | serial | NO | auto | - | PK |
| quote_id | integer | NO | - | qt_quotation.quote_id | FK (CASCADE) |
| item_no | integer | NO | - | - | 품목 순번 |
| product_name | varchar(500) | NO | - | - | 품명 |
| specification | text | YES | - | - | 규격 ([SW_AMT_TABLE] 포함 가능) |
| quantity | integer | YES | 1 | - | 수량 |
| unit | varchar(10) | YES | 식 | - | 단위 |
| unit_price | bigint | YES | 0 | - | 단가 |
| amount | bigint | YES | 0 | - | 금액 |
| remarks | varchar(500) | YES | - | - | 비고 |

#### qt_quotation_ledger (견적 대장)
| 컬럼명 | 타입 | NULL | 기본값 | 설명 |
|--------|------|------|--------|------|
| ledger_id | serial | NO | auto | PK |
| quote_id | integer | NO | - | 견적서 ID (논리적 참조) |
| ledger_no | integer | YES | - | 대장 번호 |
| year | integer | NO | - | 연도 |
| category | varchar(10) | NO | - | 분류 |
| quote_number | varchar(30) | NO | - | 견적번호 |
| quote_date | date | NO | - | 견적일자 |
| project_name | varchar(500) | NO | - | 건명 |
| total_amount | bigint | YES | 0 | 합계금액 |
| grand_total | bigint | YES | - | 투찰금액 |
| recipient | varchar(200) | YES | - | 수신 |
| reference_to | varchar(200) | YES | - | 참조 |
| created_by | varchar(50) | YES | - | 작성자 |
| registered_at | timestamp | YES | now() | 등록일시 |

#### qt_quote_number_seq (견적번호 채번)
| 컬럼명 | 타입 | NULL | 기본값 | 설명 |
|--------|------|------|--------|------|
| seq_id | serial | NO | auto | PK |
| year | integer | NO | - | 연도 (UNIQUE with category) |
| category | varchar(10) | NO | - | 분류 (UNIQUE with year) |
| last_seq | integer | YES | 0 | 마지막 채번 번호 |

#### qt_product_pattern (품명 패턴)
| 컬럼명 | 타입 | NULL | 기본값 | 설명 |
|--------|------|------|--------|------|
| pattern_id | serial | NO | auto | PK |
| category | varchar(10) | NO | - | 분류 |
| pattern_group | varchar(100) | NO | - | 그룹명 |
| product_name | varchar(500) | NO | - | 품명 |
| default_unit | varchar(10) | YES | 식 | 기본 단위 |
| default_unit_price | bigint | YES | 0 | 기본 단가 |
| description | varchar(500) | YES | - | 설명 |
| sub_items | text | YES | - | 하위 품목 (JSON) |
| usage_count | integer | YES | 0 | 사용 횟수 |
| calc_type | varchar(10) | YES | NORMAL | 계산 유형 (NORMAL/MANDAY) |
| overhead_rate | numeric | YES | 110.0 | 제경비율 |
| tech_fee_rate | numeric | YES | 20.0 | 기술료율 |

#### qt_remarks_pattern (비고 패턴)
| 컬럼명 | 타입 | NULL | 기본값 | 설명 |
|--------|------|------|--------|------|
| pattern_id | bigserial | NO | auto | PK |
| pattern_name | varchar(100) | NO | - | 패턴명 |
| content | text | NO | - | 비고 내용 |
| sort_order | integer | YES | 0 | 정렬 순서 |

#### qt_wage_rate (노임단가)
| 컬럼명 | 타입 | NULL | 기본값 | 설명 |
|--------|------|------|--------|------|
| wage_id | serial | NO | auto | PK |
| year | integer | NO | - | 연도 (UNIQUE with grade_name) |
| grade_name | varchar(50) | NO | - | 등급명 (UNIQUE with year) |
| daily_rate | bigint | NO | 0 | 일 단가 |
| monthly_rate | bigint | YES | 0 | 월 단가 |
| hourly_rate | bigint | YES | 0 | 시간 단가 |
| description | varchar(200) | YES | - | 비고 |

---

### 2.5 라이선스 관리

#### license_registry (라이선스 대장)
| 컬럼명 | 타입 | NULL | 설명 |
|--------|------|------|------|
| id | bigserial | NO | PK |
| license_id | varchar(50) | NO | 라이선스 ID (UNIQUE) |
| product_id | varchar(100) | NO | 제품 ID (UNIQUE) |
| license_type | varchar(50) | YES | 라이선스 유형 |
| valid_product_edition | varchar(100) | YES | 제품 에디션 |
| valid_product_version | varchar(50) | YES | 제품 버전 |
| validity_period | integer | YES | 유효 기간 |
| maintenance_period | integer | YES | 유지보수 기간 |
| generation_source | varchar(50) | YES | 생성 소스 |
| generation_date_time | timestamp | YES | 생성 일시 |
| quantity | integer | YES | 수량 |
| registered_to | varchar(200) | YES | 등록 대상 |
| full_name | varchar(200) | YES | 전체 이름 |
| email | varchar(200) | YES | 이메일 |
| company | varchar(200) | YES | 회사명 |
| license_string | text | NO | 라이선스 문자열 |
| upload_date | timestamp | NO | 업로드 일시 |
| uploaded_by | varchar(100) | YES | 업로드 사용자 |
| created_date | timestamp | NO | 생성 일시 |
| *(이외 활성화/비활성화/floating/NTP/WebServer 관련 30여개 컬럼)* |

#### license_upload_history (업로드 이력)
| 컬럼명 | 타입 | NULL | 기본값 | 설명 |
|--------|------|------|--------|------|
| id | bigserial | NO | auto | PK |
| upload_date | timestamp | NO | - | 업로드 일시 |
| file_name | varchar(500) | YES | - | 파일명 |
| total_count | integer | NO | - | 전체 건수 |
| success_count | integer | NO | - | 성공 건수 |
| fail_count | integer | YES | 0 | 실패 건수 |
| uploaded_by | varchar(100) | YES | - | 업로드 사용자 |
| created_date | timestamp | NO | CURRENT_TIMESTAMP | 생성일시 |

#### geonuris_license (GeoNURIS 라이선스)
| 컬럼명 | 타입 | NULL | 설명 |
|--------|------|------|------|
| id | serial | NO | PK |
| license_type | varchar(20) | NO | GSS30/GSS35/DESKTOP/SETL_AGENT/SETL_PROXY |
| file_name | varchar(100) | YES | 라이선스 파일명 |
| user_name | varchar(100) | YES | 사용자명 |
| organization | varchar(200) | YES | 기관명 |
| phone | varchar(50) | YES | 연락처 |
| email | varchar(150) | YES | 이메일 |
| issuer | varchar(100) | YES | 발급자 |
| mac_address | varchar(50) | NO | MAC 주소 |
| permission | varchar(1) | YES | 권한 (P: 영구, T: 임시) |
| start_date | timestamp | NO | 시작일 |
| expiry_date | timestamp | NO | 만료일 |
| dbms_type | varchar(20) | YES | DBMS 유형 |
| setl_count | integer | YES | SETL 수량 (기본: 0) |
| plugin_edit | boolean | YES | 편집 플러그인 (기본: false) |
| plugin_gdm | boolean | YES | GDM 플러그인 (기본: false) |
| plugin_tmbuilder | boolean | YES | TMBuilder 플러그인 (기본: false) |
| plugin_setl | boolean | YES | SETL 플러그인 (기본: false) |
| license_data | text | YES | 라이선스 데이터 (Base64) |
| created_at | timestamp | YES | 생성일시 (기본: now()) |
| created_by | varchar(100) | YES | 생성자 |

#### qr_license (QR 라이선스)
| 컬럼명 | 타입 | NULL | 설명 |
|--------|------|------|------|
| qr_id | bigserial | NO | PK |
| end_user_name | varchar(200) | NO | 사용자명 (영문) |
| address | varchar(500) | YES | 주소 (영문) |
| tel | varchar(50) | YES | 전화 (영문) |
| fax | varchar(50) | YES | 팩스 (영문) |
| products | varchar(500) | NO | 제품명 (영문) |
| user_units | varchar(100) | YES | 사용 단위 |
| version | varchar(50) | YES | 버전 |
| license_type | varchar(100) | YES | 라이선스 유형 |
| serial_number | varchar(200) | YES | 시리얼 번호 |
| application_name | varchar(200) | YES | 애플리케이션명 |
| *(위 10개 필드 모두 _ko 한글 버전 존재)* |
| qr_image_data | text | YES | QR 이미지 (Base64) |
| issued_by | varchar(50) | YES | 발급자 |
| issued_at | timestamp | YES | 발급일시 (기본: now()) |
| remarks | varchar(1000) | YES | 비고 |

---

### 2.6 서버 인프라

#### tb_infra_master (인프라 마스터)
| 컬럼명 | 타입 | NULL | 기본값 | 설명 |
|--------|------|------|--------|------|
| infra_id | bigserial | NO | auto | PK |
| city_nm | varchar(50) | YES | - | 시도명 |
| dist_nm | varchar(50) | YES | - | 시군구명 |
| sys_nm | varchar(100) | YES | - | 시스템명 |
| sys_nm_en | varchar(100) | YES | - | 시스템명(영문) |
| org_cd | varchar(20) | YES | - | 기관코드 |
| dist_cd | varchar(20) | YES | - | 지역코드 |
| infra_type | varchar(20) | YES | PROD | 인프라 유형 (PROD/DEV/TEST) |
| reg_dt | timestamp | YES | now() | 등록일시 |
| mod_dt | timestamp | YES | - | 수정일시 |

#### tb_infra_server (서버 정보)
| 컬럼명 | 타입 | NULL | FK | 설명 |
|--------|------|------|-----|------|
| server_id | bigserial | NO | - | PK |
| infra_id | bigint | NO | tb_infra_master | FK |
| server_type | varchar(10) | NO | - | WAS/WEB/DB 등 |
| ip_addr | varchar(50) | YES | - | IP 주소 |
| acc_id | varchar(100) | YES | - | 접속 ID |
| acc_pw | varchar(200) | YES | - | 접속 비밀번호 |
| os_nm | varchar(100) | YES | - | OS명 |
| mac_addr | varchar(50) | YES | - | MAC 주소 |

#### tb_infra_software (소프트웨어 정보)
| 컬럼명 | 타입 | NULL | FK | 설명 |
|--------|------|------|-----|------|
| sw_id | bigserial | NO | - | PK |
| server_id | bigint | NO | tb_infra_server | FK |
| sw_category | varchar(20) | YES | - | SW 분류 (WAS/DBMS/GIS 등) |
| sw_nm | varchar(100) | YES | - | SW명 |
| sw_ver | varchar(50) | YES | - | 버전 |
| port | varchar(20) | YES | - | 포트 |
| sw_acc_id | varchar(100) | YES | - | 접속 ID |
| sw_acc_pw | varchar(200) | YES | - | 접속 비밀번호 |
| sid | varchar(100) | YES | - | DB SID |
| install_path | varchar(300) | YES | - | 설치 경로 |

#### tb_infra_link_api (연계 API 키 관리)
| 컬럼명 | 타입 | NULL | FK | 설명 |
|--------|------|------|-----|------|
| api_id | bigserial | NO | - | PK |
| infra_id | bigint | NO | tb_infra_master | FK |
| naver_news_key | varchar(300) | YES | - | 네이버 뉴스 API 키 |
| naver_news_req_dt | date | YES | - | 신청일 |
| naver_news_exp_dt | date | YES | - | 만료일 |
| naver_news_user | varchar(50) | YES | - | 신청자 |
| *(secret, public_data, kras, kgeo, vworld, kakao 동일 구조)* |

#### tb_infra_link_upis (UPIS 연계 정보)
| 컬럼명 | 타입 | NULL | FK | 설명 |
|--------|------|------|-----|------|
| link_id | bigserial | NO | - | PK |
| infra_id | bigint | NO | tb_infra_master | FK |
| kras_ip | varchar(50) | YES | - | KRAS IP |
| kras_cd | varchar(50) | YES | - | KRAS 코드 |
| gpki_id | varchar(50) | YES | - | GPKI ID |
| gpki_pw | varchar(200) | YES | - | GPKI 비밀번호 |
| minwon_ip | varchar(50) | YES | - | 민원 IP |
| minwon_link_cd | varchar(50) | YES | - | 민원 연계코드 |
| minwon_key | varchar(200) | YES | - | 민원 키 |
| doc_ip | varchar(50) | YES | - | 문서 IP |
| doc_adm_id | varchar(50) | YES | - | 문서 관리자 ID |
| doc_id | varchar(50) | YES | - | 문서 ID |

#### tb_infra_memo (인프라 메모)
| 컬럼명 | 타입 | NULL | FK | 설명 |
|--------|------|------|-----|------|
| memo_id | bigserial | NO | - | PK |
| infra_id | bigint | NO | tb_infra_master | FK |
| memo_content | text | YES | - | 메모 내용 |
| memo_writer | varchar(50) | YES | - | 작성자 |
| memo_date | date | YES | - | 작성일 |

---

## 3. FK 관계 요약

```
sw_pjt.dist_cd          → sigungu_code.adm_sect_c
sw_pjt.biz_cat_en       → prj_types.cd
sw_pjt.sys_nm_en         → sys_mst.cd
sw_pjt.stat              → cont_stat_mst.cd
sw_pjt.maint_type        → maint_tp_mst.cd
sw_pjt.person_id         → users.user_id (논리적 FK, 물리적 미설정)

ps_info.sys_nm_en         → sys_mst.cd

qt_quotation_item.quote_id → qt_quotation.quote_id

tb_infra_server.infra_id  → tb_infra_master.infra_id
tb_infra_software.server_id → tb_infra_server.server_id
tb_infra_link_api.infra_id → tb_infra_master.infra_id
tb_infra_link_upis.infra_id → tb_infra_master.infra_id
tb_infra_memo.infra_id    → tb_infra_master.infra_id
```

---

## 4. 권한 체계

users 테이블의 `auth_*` 컬럼 값 체계:

| 값 | 의미 | 허용 범위 |
|----|------|----------|
| NONE | 권한 없음 | 접근 불가 |
| VIEW | 조회 권한 | 목록 조회, 상세 보기, 출력 |
| EDIT | 편집 권한 | VIEW + 등록, 수정, 삭제 |

- `ROLE_ADMIN`은 모든 권한을 자동으로 보유
- 모듈별 독립 권한: dashboard, project, person, infra, license, quotation

---

## 5. 주요 시퀀스

| 시퀀스명 | 테이블 | 컬럼 |
|----------|--------|------|
| access_logs_log_id_seq | access_logs | log_id |
| sw_pjt_proj_id_seq | sw_pjt | proj_id |
| ps_info_id_seq | ps_info | id |
| users_user_id_seq | users | user_id |
| qt_quotation_quote_id_seq | qt_quotation | quote_id |
| qt_quotation_item_item_id_seq | qt_quotation_item | item_id |
| qt_quotation_ledger_ledger_id_seq | qt_quotation_ledger | ledger_id |
| qt_quote_number_seq_seq_id_seq | qt_quote_number_seq | seq_id |
| qt_product_pattern_pattern_id_seq | qt_product_pattern | pattern_id |
| qt_remarks_pattern_pattern_id_seq | qt_remarks_pattern | pattern_id |
| qt_wage_rate_wage_id_seq | qt_wage_rate | wage_id |
| geonuris_license_id_seq | geonuris_license | id |
| license_registry_id_seq | license_registry | id |
| license_upload_history_id_seq | license_upload_history | id |
| qr_license_qr_id_seq | qr_license | qr_id |
| tb_infra_master_infra_id_seq | tb_infra_master | infra_id |
| tb_infra_server_server_id_seq | tb_infra_server | server_id |
| tb_infra_software_sw_id_seq | tb_infra_software | sw_id |
| tb_infra_link_api_api_id_seq | tb_infra_link_api | api_id |
| tb_infra_link_upis_link_id_seq | tb_infra_link_upis | link_id |
| tb_infra_memo_memo_id_seq | tb_infra_memo | memo_id |

---

## 6. 개발 시 주의사항

1. **ddl-auto=none** — JPA가 테이블을 자동 생성/수정하지 않음. 스키마 변경 시 반드시 SQL 마이그레이션 파일 작성 후 수동 실행
2. **코드 마스터 테이블 참조** — sw_pjt의 stat, maint_type 등은 코드값(cd)을 저장하므로, 화면에 표시할 때는 마스터 테이블의 nm으로 변환 필요
3. **논리적 FK (person_id)** — sw_pjt.person_id는 users.user_id를 참조하지만 물리적 FK 제약조건은 없음
4. **금액 필드** — 모두 bigint 타입 사용 (원 단위, 소수점 없음)
5. **비율 필드** — numeric 타입 (sw_rt, cont_rt, overhead_rate 등)
6. **타임스탬프** — 대부분 `timestamp without time zone` 사용, 기본값 `now()`
7. **권한 변경** — users 테이블의 auth_* 컬럼은 관리자 페이지에서만 변경 가능 (MyPageController에서 사용자 직접 변경 차단됨)
8. **라이선스 대장** — license_registry의 UNIQUE 제약은 (license_id, product_id) 복합이 아닌 각각 개별 UNIQUE
