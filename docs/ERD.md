# SW Management System - ERD (Entity Relationship Diagram)

> 실제 구현 기준 총 38개 테이블 / 7개 도메인 모듈
>
> ※ 이전 문서에 기재되어 있던 `tb_contract`, `tb_contract_target`,
>   `tb_inspect_cycle` 은 **현재 엔티티/스키마에 존재하지 않음** — 감사
>   2026-04-18 P2 3-1/3-3 조치로 ERD 에서 제거. 필요 시 후속 스프린트에서
>   재정의 (스프린트 2a, 2026-04-19).

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
| maint_tp_mst     |     |  Document Mgmt      |     | Inspection &      |
+------------------+     |   (6 tables)        |     | Performance       |
                          +---------------------+     |   (2 tables)      |
                          | tb_document         |     +-------------------+
                          | tb_document_detail  |     | tb_inspect_       |
                          | tb_document_history |     |   checklist       |
                          | tb_document_        |     | tb_inspect_issue  |
                          |   attachment        |     | tb_performance_   |
                          | tb_document_        |     |   summary         |
                          |   signature         |     +-------------------+
                          +---------------------+

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
               +--------+--------+--------+--------+
               |        |        |        |        |
               v        v        v        v        v
          doc_detail doc_hist doc_attach doc_sign inspect_
                                                 checklist
                                                    |
                                                    v
                                               inspect_issue


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

### 5. ~~Contract - tb_contract~~ (미구현, 감사 P2 3-1 제거)

> 이 테이블은 현재 엔티티·스키마에 존재하지 않으며 실제 비즈니스 로직은
> `sw_pjt` (프로젝트) 를 중심으로 운영된다. 계약 마스터가 필요해지면 별도
> 기획서에서 재정의.

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

### 7. ~~Contract - tb_contract_target~~ (미구현, 감사 P2 3-1 제거)

> 테이블 미존재. `tb_contract` 제거와 함께 ERD 에서 제외.

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

### 9. Document - tb_document
| Column | Type | Constraint |
|--------|------|------------|
| doc_id | INT | PK, AUTO |
| doc_no | VARCHAR(50) | UNIQUE, NOT NULL |
| doc_type | VARCHAR(30) | NOT NULL |
| infra_id | BIGINT | FK -> tb_infra_master |
| plan_id | INT | FK -> tb_work_plan |
| proj_id | BIGINT | FK -> sw_pjt |
| author_id | BIGINT | FK -> users, NOT NULL |
| approver_id | BIGINT | FK -> users |
| status | VARCHAR(20) | default='DRAFT' |

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
| tb_inspect_checklist | tb_document | doc_id | N:1 |
| tb_inspect_issue | tb_document | doc_id | N:1 |
| tb_performance_summary | users | user_id | N:1 |
| qt_quotation_item | qt_quotation | quote_id | N:1 |

> ※ `tb_contract` / `tb_contract_target` / `tb_inspect_cycle` 관련 FK는
>   테이블 미구현에 따라 제거됨 (감사 P2 3-1, 3-3 조치).
