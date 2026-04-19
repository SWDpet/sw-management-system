# [개발계획서] 감사 후속조치 스프린트 2a — P2 스키마·문서 정합성 6건

- **작성팀**: 개발팀
- **작성일**: 2026-04-19
- **기획서**: [docs/plans/audit-fix-p2-schema-docs.md](../plans/audit-fix-p2-schema-docs.md) (승인됨)
- **상태**: v2 (codex 재검토 대기) — 1차 3건 반영

### 개정 이력
| 버전 | 변경 |
|------|------|
| v1 | 초안 |
| v2 | codex 반영: ①T3 합격 기준 명확화(노드 수 고정 대신 "제거분 감소 + 파싱 정상") ②롤백 전략에 DB DDL 비가역성 명시 + 수동 down migration 절차 추가 ③Step 8 작업 순서를 T1~T5 → T1~T7 로 확장 |

---

## 1. 영향 범위

| 계층 | 파일 | 변경 유형 |
|------|------|-----------|
| DB | `src/main/resources/db_init_phase2.sql` | 상단 주석 블록 추가 + 8 CREATE TABLE IF NOT EXISTS (tb_document 계열 7 + tb_work_plan) |
| ERD | `docs/erd-contract.mmd` | tb_contract 테이블 블록 + 관계선 제거, tb_contract_participant 의 FK contract_id → proj_id 로 교체, 제목 조정 |
| ERD | `docs/erd-core.mmd` | tb_inspect_cycle 블록 + 관계선 제거 |
| ERD | `docs/ERD.md` | tb_contract / tb_contract_target / tb_inspect_cycle 언급 "미구현" 표기 or 제거 |
| Plans | `docs/plans/system-graph-infra-perf.md` | 유형 필터 → 시스템명 필터, vis-network hierarchical → 텍스트 트리+조직도, 관련 NFR 재정립, 개정 이력 추가 |
| Docs | `docs/audit/2026-04-18-system-audit.md` | 2-2, 3-1, 3-2, 3-3, 3-4, 3-5 체크박스 ☑ 조치함 + 조치 요약 |

**수정 6 파일. 코드·Entity 변경 0.**

---

## 2. 파일별 상세

### 2-1. `db_init_phase2.sql` (2-2)

#### (A) 상단 주석 블록 추가
파일 첫 줄 `-- Phase 2: DB 테이블 생성` 앞에:
```sql
-- ============================================================
-- SW Manager — DB 초기화 스크립트 (Phase 2 증분)
--
-- 이 스크립트는 phase1 (또는 JPA DDL 생성) 이후의 **증분 DDL** 입니다.
-- 이 파일만으로는 신규 환경 초기화가 완료되지 않습니다. 다음 테이블이
-- 사전에 존재해야 합니다 (phase1 범주):
--   sw_pjt, users, tb_infra_master, tb_infra_server, tb_infra_software,
--   tb_infra_link_upis, tb_infra_link_api, tb_infra_memo, access_logs,
--   ps_info, tb_performance_summary, sys_mst, sigungu_code, prj_types,
--   maint_tp_mst, cont_stat_mst, cont_frm_mst, pjt_equip,
--   tb_pjt_target, tb_pjt_manpower_plan, tb_pjt_schedule
--
-- Phase1 DDL 정비는 별도 스프린트에서 수행 예정. (감사 P2 2-2 조치 주석)
-- 감사 보고서: docs/audit/2026-04-18-system-audit.md
-- ============================================================

```

#### (B) tb_document 계열 7 + tb_work_plan CREATE 추가
기존 `CREATE TABLE IF NOT EXISTS inspect_template (...)` 뒤에 추가.

엔티티 필드 기반 DDL (모두 IF NOT EXISTS, FK 는 엔티티 @JoinColumn 기준):

- `tb_document`: doc_id(PK, SERIAL), doc_no(VARCHAR 50 UK), doc_type(VARCHAR 30), sys_type(VARCHAR 20), infra_id(BIGINT FK→tb_infra_master), plan_id(INT), proj_id(BIGINT FK→sw_pjt), title(VARCHAR 500), status(VARCHAR 20), author_id(BIGINT FK→users, NOT NULL), approver_id(BIGINT FK→users), approved_at(TIMESTAMP), created_at, updated_at
- `tb_document_detail`: detail_id(PK), doc_id(FK→tb_document), section_key(VARCHAR), section_data(JSONB)
- `tb_document_history`: history_id(PK), doc_id(FK), action, actor_id(FK→users), created_at
- `tb_document_attachment`: attach_id(PK), doc_id(FK), file_name, file_size(BIGINT), file_path
- `tb_document_signature`: sign_id(PK), doc_id(FK), signer_type, signer_name, signed_at
- `tb_inspect_checklist`: check_id(PK), doc_id(FK), check_item, check_result
- `tb_inspect_issue`: issue_id(PK), doc_id(FK), task_type, symptom(TEXT), action(TEXT)
- `tb_work_plan`: plan_id(PK), infra_id(FK→tb_infra_master), plan_type, process_step(INT), title, description(TEXT), assignee_id(FK→users), start_date, end_date, location, repeat_type, status, status_reason, parent_plan_id(FK→tb_work_plan self), created_by(FK→users), created_at, updated_at

**원칙**: 엔티티의 `@Column`/`@JoinColumn` 에 정의된 필드·FK 만 사용. 엔티티에 없는 필드는 추가하지 않음.

### 2-2. `erd-contract.mmd` (3-1, 3-2)

#### (A) tb_contract 블록 제거
라인 14~24 (title 헤더 이후 첫 tb_contract 블록) 삭제.

#### (B) tb_contract_target 블록 제거 (기획서 3-1 연관 — 미구현 테이블)
`tb_contract_target {...}` 블록 삭제.

#### (C) tb_contract_participant 필드 교체
- `contract_id FK` → `proj_id FK`
- 의미: 프로젝트 기반 참여자 (실제 코드와 일치)

#### (D) 관계선 정리
제거:
```
tb_infra_master ||--o{ tb_contract : "has"
tb_contract ||--o{ tb_contract_participant : "has"
tb_contract ||--o{ tb_contract_target : "has"
```

추가:
```
sw_pjt ||--o{ tb_contract_participant : "has participants"
```

유지:
```
users ||--o{ tb_contract_participant : "joins"
tb_infra_master ||--o{ tb_work_plan : "has"
users ||--o{ tb_work_plan : "assigned"
```

#### (E) title 수정
`title: "2. Contract & Work Plan"` → `title: "2. Project Participants & Work Plan"` (계약 개념 제거 반영)

#### (F) sw_pjt 참조 stub 추가
`tb_contract_participant.proj_id` 가 sw_pjt 를 참조하려면 sw_pjt 테이블 stub 이 이 파일에도 필요:
```
sw_pjt {
    bigint proj_id PK
    varchar proj_nm
    varchar sys_nm
}
```
(스텁 — FR-1-MERGE 에 의해 erd-core 의 정본이 채택되므로 문제 없음)

### 2-3. `erd-core.mmd` (3-3)

#### (A) tb_inspect_cycle 블록 제거 (라인 41 부근)
블록과 관련 관계선 삭제.

#### (B) 관계선 제거
`users ||--o{ tb_inspect_cycle : "assigned"` (라인 66) 삭제.

### 2-4. `docs/ERD.md` (3-1, 3-3)

여러 언급 위치가 있음:
- 다이어그램 헤더·블록도 속 `tb_contract`, `tb_inspect_cycle` 표기 → "미구현" 주석 추가 or 삭제
- 라인 153 `### 5. Contract - tb_contract` 섹션 → 전체 섹션 제거 or "미구현 (ERD 예정)" 표기
- 라인 167 `### 6. Contract - tb_contract_participant` FK 컬럼 설명 → `contract_id → tb_contract` 를 `proj_id → sw_pjt` 로 수정

**선택**: 섹션 완전 삭제보다 "미구현" 주석을 달아 역사적 맥락 보존.

### 2-5. `system-graph-infra-perf.md` (3-4, 3-5)

#### (A) 개정 이력 테이블에 한 줄 추가
```
| v3 | 2026-04-19 (감사 P2 3-4/3-5 반영): ①유형 필터(UPIS/KRAS/기타) → 시스템명(sys_nm_en) 필터로 변경 ②vis-network hierarchical 렌더러 → 텍스트 트리(CLI 스타일) + 조직도 토글(≤80 노드) 로 전환 ③DataSet 배치 업데이트 요구 삭제 (innerHTML replace 방식) |
```

#### (B) 본문 FR-2 (유형 필터)
> "인프라 유형 필터를 **단일 select 드롭다운** 으로 변경. 옵션: 전체/UPIS/KRAS/기타"
→
> "인프라 필터를 **시스템명(sys_nm_en) 단일 select 드롭다운** 으로 운영 (사용자 피드백 반영, infra_type 값이 정합성 부족). API 응답의 distinct sys_nm_en 값으로 동적 생성. 기본값: 전체."

#### (C) 본문 FR-1 (레이아웃)
> "vis-network 옵션에 layout.hierarchical 을 설정하여 계층 트리 레이아웃 적용"
→
> "순수 HTML `<pre>` 기반 텍스트 트리 (CLI tree 스타일, `├── │ └──`) 를 기본 렌더러로 사용. 이와 별도로 조직도 박스 뷰 토글(≤80 노드 한도) 제공. vis-network 은 C 탭(ERD) 에서만 사용."

#### (D) FR-4 (batch update)
> "onInfraFilter() 의 DataSet 업데이트를 배치 호출..."
→
> "필터 변경 시 DOM innerHTML 을 한 번의 replace 로 전체 재생성. 1,600+ 노드 규모에서도 수백 ms 이내 반응."

#### (E) 시나리오·리스크 섹션의 vis-network 관련 언급 정리
"hierarchical / dragNodes" 등 더 이상 유효하지 않은 문구 삭제 or 표시.

### 2-6. `docs/audit/2026-04-18-system-audit.md`

6 개 항목 (2-2, 3-1, 3-2, 3-3, 3-4, 3-5) 의 체크박스 `☐ 조치함` → `☑ 조치함` + 각 항목 끝에 조치 요약 한 줄 추가.

---

## 3. 작업 순서

| Step | 작업 | 검증 |
|------|------|------|
| 1 | `db_init_phase2.sql` 상단 주석 + 8 CREATE 추가 | `grep "^CREATE TABLE" db_init_phase2.sql | wc -l` → 15 |
| 2 | `erd-contract.mmd` 수정 (2-2) | 파일 diff + C 탭 테스트에서 33 노드 유지 |
| 3 | `erd-core.mmd` 수정 (2-3) | 동일 |
| 4 | `docs/ERD.md` 수정 (2-4) | diff |
| 5 | `system-graph-infra-perf.md` 수정 (2-5) | diff + 개정 이력 확인 |
| 6 | `2026-04-18-system-audit.md` 체크박스 업데이트 (2-6) | 6건 모두 ☑ |
| 7 | 서버 재기동 | `Started SwManagerApplication`, C 탭 ERD 파싱 오류 없음 |
| 8 | 회귀 테스트 **T1~T7 전체** | 모두 PASS (특히 T6 체크박스, T7 SQL 오류 무여부) |
| 9 | codex Specs 중심 검증 | ✅ |
| 10 | 사용자 "작업완료" 후 commit + push | master 반영 |

---

## 4. 회귀 테스트

| # | 시나리오 | 기대 결과 | Specs |
|---|---------|-----------|-------|
| T1 | `grep "^CREATE TABLE IF NOT EXISTS" db_init_phase2.sql \| wc -l` | 15 (기존 7 + 신규 8) | FR-2-2-B, C |
| T2 | 서버 재기동 로그 | `ERD descriptions loaded: N tables`, 에러 없음 | NFR-3 |
| T3 | C 탭(ERD 인터랙티브) 진입 → 노드 수 확인 | **제거된 테이블 수(tb_contract·tb_contract_target·tb_inspect_cycle = 최대 3개) 만큼 감소, 파싱 오류 0**. 정확한 기대값은 변경 전 실측 기반으로 계산. 단일 허용 조건: 변경 전 노드 수 − (실제 제거된 테이블 중 기존 그래프에 있었던 수) = 변경 후 노드 수 | NFR-3 |
| T4 | ERD.md 수동 읽기 검토 | tb_contract/inspect_cycle 섹션에 "미구현" 주석 있음 | 2-4 |
| T5 | `system-graph-infra-perf.md` 의 개정 이력 테이블 | v3 행 추가됨, 본문도 시스템명/텍스트트리 언급 | 2-5 |
| T6 | 감사 보고서 2-2/3-1/3-2/3-3/3-4/3-5 체크박스 | 6개 모두 `☑ 조치함` | 2-6 |
| T7 | 서버 로그에 SQL 오류 없음 | 없음 | NFR-1 |

---

## 5. 롤백 전략

### ⚠️ 비가역 변경 있음
`db_init_phase2.sql` 에 추가된 **CREATE TABLE IF NOT EXISTS 는 이미 실행된 DB 에 실제 테이블을 생성**하므로, `git revert` 만으로는 DB 상 추가된 테이블이 제거되지 않음. 즉 **이번 스프린트는 "git-만 롤백" 불가능**.

### 코드·문서 롤백
```bash
git revert <sha>
git push
bash server-restart.sh
```
→ 파일(DDL·ERD·plans·감사보고서)은 이전 상태 복귀.

### DB down migration (필요 시 수동)
롤백 후에도 DB 에 남은 신규 테이블이 문제가 되면 운영자가 수동 실행:
```sql
-- ⚠️ 다음 테이블에 데이터가 없거나 참조가 없음을 먼저 확인
-- 신규 테이블 역순으로 DROP (FK 제약 고려)
DROP TABLE IF EXISTS tb_work_plan;
DROP TABLE IF EXISTS tb_inspect_issue;
DROP TABLE IF EXISTS tb_inspect_checklist;
DROP TABLE IF EXISTS tb_document_signature;
DROP TABLE IF EXISTS tb_document_attachment;
DROP TABLE IF EXISTS tb_document_history;
DROP TABLE IF EXISTS tb_document_detail;
DROP TABLE IF EXISTS tb_document;
```
⚠️ 이 테이블들은 실제로 운영에 사용 중일 수 있으므로(엔티티는 이미 있음), 수동 DROP 전 데이터 백업 및 사용처 확인 필수.

### 롤백 검증 체크포인트
1. 코드·ERD·plans 문서 원래대로
2. C 탭(ERD 인터랙티브) 기존 테이블 수 복귀 (tb_contract·tb_inspect_cycle 재출현)
3. DB down migration 실행한 경우: 해당 테이블 미존재 상태
4. 서버 재기동 후 애플리케이션 정상 동작

---

## 6. 빌드 · 재시작

- Java 변경 **없음** → 빌드 불필요
- 템플릿 변경 없음, 하지만 ERD 변경 반영 위해 **서버 재기동 필요** (ErdGraphService 는 @PostConstruct 에서 .mmd 파일 파싱)

---

## 7. 예상 소요

| 작업 | 시간 |
|------|------|
| db_init_phase2.sql 주석 + 8 DDL 작성 | 15 분 |
| erd-contract.mmd 수정 | 5 분 |
| erd-core.mmd 수정 | 3 분 |
| ERD.md 정리 | 10 분 |
| system-graph-infra-perf.md 개정 | 10 분 |
| 감사 보고서 체크박스 | 3 분 |
| 서버 재기동 + C 탭 확인 | 5 분 |
| codex 검증 | 5 분 |
| **합계** | **~56 분** |

---

## 8. 체크리스트

- [ ] `db_init_phase2.sql` 상단 주석 블록
- [ ] `db_init_phase2.sql` tb_document 계열 7 CREATE 추가
- [ ] `db_init_phase2.sql` tb_work_plan CREATE 추가
- [ ] `erd-contract.mmd` tb_contract/tb_contract_target 제거
- [ ] `erd-contract.mmd` tb_contract_participant FK: contract_id → proj_id
- [ ] `erd-contract.mmd` title, 관계선 정리
- [ ] `erd-core.mmd` tb_inspect_cycle 제거
- [ ] `ERD.md` tb_contract / inspect_cycle 섹션 "미구현" 표기
- [ ] `system-graph-infra-perf.md` v3 개정 이력 + 본문 갱신
- [ ] `2026-04-18-system-audit.md` 6개 체크박스 ☑
- [ ] 서버 재기동 + C 탭 파싱 오류 없음
- [ ] 회귀 테스트 T1~T7 통과
- [ ] codex Specs 중심 검증 ✅

---

## 9. 승인 요청

본 개발계획서에 대한 codex 검토 및 사용자 최종승인을 요청합니다.
