# SW Management System - ERD (Entity Relationship Diagram)

> 실제 구현 기준 (doc-split-ops, 2026-04-29 갱신)
>
> ※ 이전 문서에 기재되어 있던 `tb_contract`, `tb_contract_target`,
>   `tb_inspect_cycle` 은 **현재 엔티티/스키마에 존재하지 않음** — 감사
>   2026-04-18 P2 3-1/3-3 조치로 ERD 에서 제거.
>
> ※ doc-split-ops (2026-04-29): 운영·유지보수 문서 5 종 (INSPECT / FAULT /
>   SUPPORT / INSTALL / PATCH) 은 `tb_ops_doc` + 자식 4 (detail / history /
>   attachment / signature) 로 분리. `tb_document` 에는 사업문서 3 종
>   (COMMENCE / INTERIM / COMPLETION) 만 잔존. `tb_inspect_checklist` /
>   `tb_inspect_issue` 는 DROP (점검 데이터는 `inspect_check_result` /
>   `inspect_visit_log` 에 통합 존재).

---

## 도메인 구조 요약

```
+------------------+     +---------------------+     +-------------------+
|   Core Domain    |     | Infrastructure Mgmt |     |  Participants &   |
|   (11 tables)    |     |    (6 tables)       |     |  Work Plan (2)    |
+------------------+     +---------------------+     +-------------------+
| users            |     | tb_infra_master     |     | tb_contract_      |
| access_logs      |     | tb_infra_server     |     |   participant     |
| ps_info          |     | tb_infra_software   |     | tb_work_plan      |
| sw_pjt           |     | tb_infra_link_upis  |     +-------------------+
| sys_mst          |     | tb_infra_link_api   |
| sigungu_code     |     | tb_infra_memo       |
| prj_types        |     +---------------------+
| cont_stat_mst    |
| cont_frm_mst     |     +---------------------+     +-------------------+
| maint_tp_mst     |     | Business Doc Mgmt   |     | Ops Doc Mgmt      |
+------------------+     |   (5 tables)        |     |   (5 tables)      |
                          +---------------------+     +-------------------+
                          | tb_document         |     | tb_ops_doc        |
                          | tb_document_detail  |     | tb_ops_doc_detail |
                          | tb_document_history |     | tb_ops_doc_history|
                          | tb_document_        |     | tb_ops_doc_       |
                          |   attachment        |     |   attachment      |
                          | tb_document_        |     | tb_ops_doc_       |
                          |   signature         |     |   signature       |
                          +---------------------+     +-------------------+
                          (COMMENCE/INTERIM/          (INSPECT/FAULT/
                           COMPLETION)                 SUPPORT/INSTALL/PATCH)

+---------------------+     +-------------------+
|  License Mgmt       |     | Quotation Mgmt    |
|   (4 tables)        |     |   (7 tables)      |
+---------------------+     +-------------------+
| license_registry    |     | qt_quotation      |
| license_upload_     |     | qt_quotation_item |
|   history           |     | qt_quotation_     |
| geonuris_license    |     |   ledger          |
| qr_license          |     | qt_quote_number_  |
+---------------------+     |   seq             |
                             | qt_product_pattern|
                             | qt_remarks_pattern|
                             | qt_wage_rate      |
                             +-------------------+
```

---

## 핵심 관계도 (Relationship Diagram)

```
                            +----------+
                            |  users   |
                            +----+-----+
                                 |
          +----------+-----------+------------+
          |          |           |            |
          v          v           v            v
    tb_work_plan  tb_contract_ tb_document  tb_performance
    (assignee)   participant   (author/     _summary
    (created_by) (user)        approver)    (user)


                       +------------------+
                       | tb_infra_master  |
                       +--------+---------+
                                |
        +-----------+-----------+-----------+-----------+
        |           |           |           |           |
        v           v           v           v           v
  tb_infra_    tb_infra_   tb_infra_   tb_infra_  tb_work_
  server       link_upis   link_api    memo       plan
    |
    v
  tb_infra_
  software

                       +------------------+
                       |     sw_pjt       |
                       +--------+---------+
                                |
                  +-------------+-------------+
                  |                           |
                  v                           v
           tb_contract_participant      tb_document (proj)
                                              |
                            +--------+--------+--------+
                            |        |        |        |
                            v        v        v        v
                       doc_detail doc_hist doc_attach doc_sign

                                       +-----------+
                                       | tb_ops_doc|
                                       +-----+-----+
                                             |
                            +--------+--------+--------+
                            |        |        |        |
                            v        v        v        v
                      ops_detail ops_hist ops_attach ops_sign
                      (INSPECT/FAULT/SUPPORT/INSTALL/PATCH)


    +---------------+
    | qt_quotation  |
    +-------+-------+
            |
            v
    qt_quotation_item
```

---

## 테이블 상세

### 1. Core - users
| Column | Type | Constraint |
|--------|------|------------|
| user_id | BIGINT | PK, AUTO |
| userid | VARCHAR(255) | UNIQUE, NOT NULL |
| username | VARCHAR(255) | |
| password | VARCHAR(255) | |
| orgNm, deptNm, teamNm | VARCHAR(255) | |
| tel, email | VARCHAR(255) | |
| userRole | VARCHAR(255) | |
| position | VARCHAR(50) | |
| techGrade | VARCHAR(20) | |
| auth* (9 columns) | VARCHAR(255) | default='NONE' |
| failedAttempts | INT | default=0 |
| lockTime | DATETIME | |
| regDt | DATETIME | auto |

### 2. Infra - tb_infra_master
| Column | Type | Constraint |
|--------|------|------------|
| infra_id | BIGINT | PK, AUTO |
| infra_type | VARCHAR(255) | |
| city_nm | VARCHAR(255) | |
| dist_nm | VARCHAR(255) | |
| sys_nm / sys_nm_en | VARCHAR(255) | |
| org_cd / dist_cd | VARCHAR(255) | |

### 3. Infra - tb_infra_server
| Column | Type | Constraint |
|--------|------|------------|
| server_id | BIGINT | PK, AUTO |
| infra_id | BIGINT | FK -> tb_infra_master |
| server_type, ip_addr | VARCHAR(255) | |
| acc_id, acc_pw | VARCHAR(255) | |
| os_nm, mac_addr | VARCHAR(255) | |
| server_model | VARCHAR(200) | |
| serial_no | VARCHAR(100) | |
| cpu/memory/disk/network/power_spec | VARCHAR | |

### 4. Infra - tb_infra_software
| Column | Type | Constraint |
|--------|------|------------|
| sw_id | BIGINT | PK, AUTO |
| server_id | BIGINT | FK -> tb_infra_server |
| sw_category, sw_nm, sw_ver | VARCHAR(255) | |
| port, sw_acc_id, sw_acc_pw | VARCHAR(255) | |
| sid, install_path | VARCHAR(255) | |

### 5. ~~Contract - tb_contract~~ (삭제 완료 2026-04-20, sprint `legacy-contract-tables-drop` 옵션 B)

> 2026-04-20 DB 스키마에서 완전 제거 (DROP TABLE). Java Entity·Repository·Service·Controller 모두 없었으며 실데이터 0건.
> `sw_pjt` (프로젝트) + `ps_info` + `sigungu_code` 로 기능 완전 대체.
> 함께 제거: `tb_document.contract_id` FK·컬럼, `tb_contract_participant.contract_id` FK·컬럼·INDEX.

### 6. Participants - tb_contract_participant
| Column | Type | Constraint |
|--------|------|------------|
| participant_id | INT | PK, AUTO |
| proj_id | BIGINT | FK -> sw_pjt |
| user_id | BIGINT | FK -> users |
| role_type | VARCHAR(20) | NOT NULL |
| tech_grade | VARCHAR(20) | |
| task_desc | VARCHAR(500) | |
| is_site_rep | BOOLEAN | default=false |
| sort_order | INT | default=0 |

### 7. ~~Contract - tb_contract_target~~ (삭제 완료 2026-04-20, sprint `legacy-contract-tables-drop` 옵션 B)

> 2026-04-20 DB 스키마에서 완전 제거 (DROP TABLE). 실데이터 0건.

### 8. Work Plan - tb_work_plan
| Column | Type | Constraint |
|--------|------|------------|
| plan_id | INT | PK, AUTO |
| infra_id | BIGINT | FK -> tb_infra_master |
| plan_type | VARCHAR(30) | NOT NULL |
| assignee_id | BIGINT | FK -> users |
| parent_plan_id | INT | FK -> tb_work_plan (self) |
| title | VARCHAR(300) | NOT NULL |
| start_date | DATE | NOT NULL |
| status | VARCHAR(20) | default='PLANNED' |
| created_by | BIGINT | FK -> users |

### 9. Business Document - tb_document
| Column | Type | Constraint |
|--------|------|------------|
| doc_id | INT | PK, AUTO |
| doc_no | VARCHAR(50) | UNIQUE, NOT NULL |
| doc_type | VARCHAR(30) | NOT NULL — COMMENCE / INTERIM / COMPLETION (doc-split-ops, 2026-04-29) |
| infra_id | BIGINT | FK -> tb_infra_master |
| plan_id | INT | FK -> tb_work_plan |
| proj_id | BIGINT | FK -> sw_pjt |
| author_id | BIGINT | FK -> users, NOT NULL |
| approver_id | BIGINT | FK -> users |
| status | VARCHAR(20) | default='DRAFT' |

### 9b. Ops Document - tb_ops_doc *(doc-split-ops, 2026-04-29 신규)*
| Column | Type | Constraint |
|--------|------|------------|
| doc_id | BIGSERIAL | PK |
| doc_no | VARCHAR(50) | UNIQUE, NOT NULL — INSP-/FLT-/SUP-/INS-/PAT- prefix |
| doc_type | VARCHAR(30) | NOT NULL — INSPECT / FAULT / SUPPORT / INSTALL / PATCH (CHECK 제약) |
| sys_type | VARCHAR(20) | sys_mst.cd 참조 |
| region_code | VARCHAR(10) | FAULT/SUPPORT(EXTERNAL) 필수 (조합 CHECK) |
| org_unit_id | BIGINT | FK -> tb_org_unit (SUPPORT(INTERNAL) 시) |
| environment | VARCHAR(20) | PROD / TEST — INSTALL/PATCH 필수 (조합 CHECK) |
| support_target_type | VARCHAR(20) | EXTERNAL / INTERNAL — SUPPORT 만 |
| infra_id | BIGINT | FK -> tb_infra_master (INSTALL/PATCH 필수) |
| plan_id | INT | FK -> tb_work_plan |
| title | VARCHAR(500) | NOT NULL |
| status | VARCHAR(20) | NOT NULL, default='DRAFT' (CHECK ∈ DRAFT/IN_REVIEW/COMPLETED) |
| author_id | BIGINT | FK -> users, NOT NULL |
| approver_id | BIGINT | FK -> users |
| approved_at | TIMESTAMP | COMPLETED 전이 시 자동 세팅 (PerformanceService 집계용) |
| created_at / updated_at | TIMESTAMP | NOT NULL |
| created_by / updated_by | VARCHAR(50) | |

자식 테이블 4: `tb_ops_doc_detail` (jsonb section_data) / `tb_ops_doc_history` / `tb_ops_doc_attachment` / `tb_ops_doc_signature` (Base64 PNG signature_image).

### 10. Quotation - qt_quotation
| Column | Type | Constraint |
|--------|------|------------|
| quote_id | BIGINT | PK, AUTO |
| quote_number | VARCHAR(30) | UNIQUE, NOT NULL |
| quote_date | DATE | NOT NULL |
| category | VARCHAR(10) | NOT NULL |
| project_name | VARCHAR(500) | NOT NULL |
| total_amount | BIGINT | |
| grand_total | BIGINT | |
| bid_rate | DOUBLE | |
| vat_included | BOOLEAN | default=true |

### 11. License - license_registry
| Column | Type | Constraint |
|--------|------|------------|
| id | BIGINT | PK, AUTO |
| license_id | VARCHAR(50) | NOT NULL |
| product_id | VARCHAR(100) | NOT NULL |
| license_string | TEXT | NOT NULL |
| (+ 63 more columns for license details) | | |

---

## FK 관계 요약

| From | To | FK Column | 관계 |
|------|----|-----------|------|
| tb_infra_server | tb_infra_master | infra_id | N:1 |
| tb_infra_software | tb_infra_server | server_id | N:1 |
| tb_infra_link_upis | tb_infra_master | infra_id | N:1 |
| tb_infra_link_api | tb_infra_master | infra_id | N:1 |
| tb_infra_memo | tb_infra_master | infra_id | N:1 |
| tb_work_plan | tb_infra_master | infra_id | N:1 |
| tb_work_plan | users | assignee_id | N:1 |
| tb_work_plan | users | created_by | N:1 |
| tb_work_plan | tb_work_plan | parent_plan_id | N:1 (self) |
| tb_contract_participant | sw_pjt | proj_id | N:1 |
| tb_contract_participant | users | user_id | N:1 |
| tb_org_unit | tb_org_unit | parent_id | N:1 (self) |
| tb_document | tb_org_unit | org_unit_id | N:1 (업무지원 내부 대상, 스프린트 5) |
| tb_document | tb_infra_master | infra_id | N:1 |
| tb_document | tb_work_plan | plan_id | N:1 |
| tb_document | sw_pjt | proj_id | N:1 |
| tb_document | users | author_id | N:1 |
| tb_document | users | approver_id | N:1 |
| tb_document_detail | tb_document | doc_id | N:1 |
| tb_document_history | tb_document | doc_id | N:1 |
| tb_document_history | users | actor_id | N:1 |
| tb_document_attachment | tb_document | doc_id | N:1 |
| tb_document_signature | tb_document | doc_id | N:1 |
| tb_ops_doc | tb_org_unit | org_unit_id | N:1 (SUPPORT INTERNAL) |
| tb_ops_doc | tb_infra_master | infra_id | N:1 (INSTALL/PATCH) |
| tb_ops_doc | tb_work_plan | plan_id | N:1 |
| tb_ops_doc | users | author_id | N:1 |
| tb_ops_doc | users | approver_id | N:1 |
| tb_ops_doc_detail | tb_ops_doc | doc_id | N:1 |
| tb_ops_doc_history | tb_ops_doc | doc_id | N:1 |
| tb_ops_doc_attachment | tb_ops_doc | doc_id | N:1 |
| tb_ops_doc_signature | tb_ops_doc | doc_id | N:1 |
| tb_performance_summary | users | user_id | N:1 |
| qt_quotation_item | qt_quotation | quote_id | N:1 |

> ※ `tb_contract` / `tb_contract_target` / `tb_inspect_cycle` 관련 FK는
>   테이블 미구현에 따라 제거됨 (감사 P2 3-1, 3-3 조치).
