# 운영DB Schema 변경 노트 — 2026-04-29 (`doc-split-ops` 스프린트)

본 문서는 `2026-04-27-prod-schema.sql` 스냅샷 이후 `doc-split-ops` 스프린트로
적용된 스키마 변경분 요약입니다. 운영DB 재추출 시점까지의 임시 베이스라인.

> 운영 적용 일자: 2026-04-29 (db-split-ops 스프린트 Step 2/9 — 사용자 최종승인 후)

## 1. DROP 테이블 (CASCADE)

| 테이블 | DROP 사유 |
|--------|----------|
| `tb_inspect_checklist` | S1 통합 후 사실상 dead — 점검 데이터는 `inspect_check_result` 사용 |
| `tb_inspect_issue` | 동상 — 점검 이슈는 `inspect_visit_log` 통합 |

## 2. 데이터 안전망 (Step 2 사전 정리)

```
DELETE FROM tb_document
 WHERE doc_type IN ('INSPECT', 'FAULT', 'SUPPORT', 'INSTALL', 'PATCH');
```

이후 `tb_document` 에는 `COMMENCE / INTERIM / COMPLETION` 만 잔존.

## 3. 신규 테이블 (5)

### 3-1. `tb_ops_doc` (마스터)

| 컬럼 | 타입 | 비고 |
|------|------|------|
| doc_id | BIGSERIAL PK | |
| doc_no | VARCHAR(50) UNIQUE NOT NULL | INSP-/FLT-/SUP-/INS-/PAT- prefix |
| doc_type | VARCHAR(30) NOT NULL | CHECK ∈ (INSPECT/FAULT/SUPPORT/INSTALL/PATCH) |
| sys_type | VARCHAR(20) | sys_mst.cd 참조 (FK 없음 — 텍스트 코드) |
| region_code | VARCHAR(10) | sigungu_code.region_code 참조 (FK 없음) |
| org_unit_id | BIGINT FK → tb_org_unit | SUPPORT(INTERNAL) |
| environment | VARCHAR(20) | CHECK ∈ (PROD/TEST) — INSTALL/PATCH 필수 |
| support_target_type | VARCHAR(20) | CHECK ∈ (EXTERNAL/INTERNAL) — SUPPORT 만 |
| infra_id | BIGINT FK → tb_infra_master | INSTALL/PATCH 필수 |
| plan_id | INT FK → tb_work_plan | |
| title | VARCHAR(500) NOT NULL | |
| status | VARCHAR(20) | NOT NULL default='DRAFT' (CHECK ∈ DRAFT/IN_REVIEW/COMPLETED) |
| author_id | BIGINT FK → users NOT NULL | |
| approver_id | BIGINT FK → users | |
| approved_at | TIMESTAMP | COMPLETED 전이 시 자동 세팅 — PerformanceService 집계용 |
| created_at / updated_at | TIMESTAMP NOT NULL | |
| created_by / updated_by | VARCHAR(50) | |

**조합 CHECK** (`ck_tb_ops_doc_combo`):
```
doc_type = 'INSPECT'
OR (doc_type IN ('FAULT','SUPPORT') AND region_code IS NOT NULL AND sys_type IS NOT NULL)
OR (doc_type IN ('INSTALL','PATCH')  AND infra_id    IS NOT NULL AND environment IS NOT NULL)
```

### 3-2. 자식 4 테이블

| 테이블 | 핵심 컬럼 |
|--------|----------|
| `tb_ops_doc_detail` | detail_id PK, doc_id FK CASCADE, section_key, section_data jsonb, sort_order |
| `tb_ops_doc_history` | history_id PK, doc_id FK CASCADE, action, changed_field, old_value, new_value, actor_id FK users, comment |
| `tb_ops_doc_attachment` | attach_id PK, doc_id FK CASCADE, file_name, file_path, file_size, mime_type, uploaded_at |
| `tb_ops_doc_signature` | sign_id PK, doc_id FK CASCADE, signer_type, signer_name, signer_org, signature_image TEXT (Base64 PNG), signed_at |

## 4. 인덱스 (9)

```
idx_tb_ops_doc_type             (doc_type)
idx_tb_ops_doc_status           (status)
idx_tb_ops_doc_author           (author_id)
idx_tb_ops_doc_created          (created_at DESC)
idx_tb_ops_doc_doc_no           (doc_no)            -- UNIQUE 와 별개 검색용
idx_tb_ops_doc_detail_doc       (doc_id)
idx_tb_ops_doc_history_doc      (doc_id)
idx_tb_ops_doc_attachment_doc   (doc_id)
idx_tb_ops_doc_signature_doc    (doc_id)
```

## 5. 운영DB 재추출 시 후속 작업

다음 prod schema 재추출 시 본 변경분이 반영된 새 스냅샷 (`YYYY-MM-DD-prod-schema.sql`)
을 생성하고 본 변경 노트는 archive 폴더로 이동.
