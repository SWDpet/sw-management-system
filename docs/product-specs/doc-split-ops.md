---
tags: [plan, sprint]
sprint: "doc-split-ops"
status: draft-v3
created: "2026-04-29"
revised: "2026-04-29"
---

# [기획서] 문서관리 분리 — 사업문서 / 운영·유지보수 문서 (v3)

- **작성팀**: 기획팀
- **작성일**: 2026-04-29 (v1) / **수정일**: 2026-04-29 (v2: DB팀·디자인팀 자문 흡수, v3: codex 지적 5건 반영)
- **선행**: `37f847f` (hw-interim-and-document-mgmt — 기성내역서·문서관리 UI/UX 복구)
- **상태**: v3 (사용자 최종승인 대기)
- **자가 UI 키워드 체크**: ✅ 자문 필요 (매칭: `메뉴`, `templates/`, `레이아웃`)
- **자문 종합**: 🗄️ DB팀 "수정 후 승인" / 🎨 디자인팀 "수정 후 승인" / 🤖 codex "수정 후 승인" — 모든 권고 흡수 완료

---

## 1. 배경 / 목표

### 배경
- 현재 `tb_document` 단일 테이블에 8 종 doc_type (착수/기성/준공/점검/장애/업무지원/설치/패치) 이 혼재. **사업 단위 문서**(착수·기성·준공) 와 **운영·유지보수 문서**(점검·장애·업무지원·설치·패치) 의 라이프사이클·UI 흐름·검색 키 차이를 메뉴/UX·코드 레벨에서 분리.
- 사용자 요청 (2026-04-29).
- **기존 데이터는 본 스프린트 직전 모두 클리어** (`TRUNCATE tb_document/inspect_report RESTART IDENTITY CASCADE`) — **마이그레이션 부담 없음**.

### 재검증 결과 (2026-04-29)
- `tb_document` 18→0 / `tb_document_attachment` 0 (디스크 첨부파일 없음).
- `inspect_template` 70 / `cont_frm_mst` 5 / `maint_tp_mst` 7 / `prj_types` 3 (마스터 데이터 보존).
- `DocumentType` enum 8 종 — `src/main/java/com/swmanager/system/constant/enums/DocumentType.java:20-28`.
- 점검내역서 ↔ `tb_document` 연계: `InspectReportService.linkToDocument()` 103-148줄.
- top-nav 현재 IA: `templates/fragments/top-nav.html:102-139` 의 `문서관리 ▾` 단일 드롭다운에 이미 `사업 문서 작성` / `운영·유지보수 문서` section header 골격 존재 (디자인팀 발견).

### 목표
- **사업문서**(`tb_document`) 코드/스키마/URL/테스트 **0% 변경** — 90% 완성 보존.
- **점검내역서** 본 데이터(`inspect_report` 계열)·UI·Controller·Template·Test 동작 보존, **연계 로직 1 개 메서드만 커스터마이징** (`linkToDocument` → `linkToOpsDoc`).
- **운영·유지보수 4 종 신규**(`FAULT`/`SUPPORT`/`INSTALL`/`PATCH`) 신규 패키지 (`domain/ops`) 작성.
- top-nav 현행 단일 드롭다운 유지 + section header 라벨 정비 (디자인팀 §A 권고).
- 기존 `tb_document` 의 5 종 처리 코드(라우트·템플릿·서비스 분기·Enum 값) 일괄 제거.

---

## 2. 기능 요건 (FR)

| ID | 내용 |
|----|------|
| FR-1 | 신규 테이블 `tb_ops_doc` (PK `doc_id` IDENTITY) + 자식 4 개 (`tb_ops_doc_detail`, `_history`, `_attachment`, `_signature`). 자식 → 부모 `ON DELETE CASCADE`, 외부 FK (`org_unit_id`/`infra_id`/`author_id`/`approver_id`/`region_code→sigungu_code`/`plan_id→tb_work_plan`) 는 **NO ACTION** (마스터 보호). |
| FR-2 | `tb_ops_doc` 컬럼 세트 (DB팀 §A + codex 권고 반영): `doc_id`, **`doc_no VARCHAR(50) NOT NULL UNIQUE`** (codex: NULL 중복 방지), `doc_type VARCHAR(30)`, `sys_type VARCHAR(20)`, `region_code VARCHAR(10)`, `org_unit_id BIGINT`, `environment VARCHAR(20)`, `support_target_type VARCHAR(20)`, `infra_id BIGINT`, **`plan_id BIGINT`**, `title VARCHAR(500)`, `status VARCHAR(20)`, `author_id BIGINT NOT NULL`, `approver_id BIGINT`, `approved_at`, `created_at`, `updated_at`, **`created_by VARCHAR(50)`**, **`updated_by VARCHAR(50)`**. `tb_ops_doc_signature.signature_image TEXT` (Base64 PNG, 점검 패턴 흡수). |
| FR-2-CHK | DB CHECK 제약 (DB팀 §B 권고): `doc_type IN ('INSPECT','FAULT','SUPPORT','INSTALL','PATCH')` / `status IN ('DRAFT','COMPLETED')` / `environment IS NULL OR IN ('PROD','TEST')` / `support_target_type IS NULL OR IN ('EXTERNAL','INTERNAL')` / **조합 제약** `(doc_type='INSPECT' AND infra_id IS NOT NULL) OR (doc_type IN ('FAULT','SUPPORT') AND region_code IS NOT NULL AND sys_type IS NOT NULL) OR (doc_type IN ('INSTALL','PATCH') AND infra_id IS NOT NULL AND environment IS NOT NULL)`. |
| FR-2-IDX | 인덱스 (DB팀 §B 권고): `idx_tb_ops_doc_type_status (doc_type, status)`, `idx_tb_ops_doc_region_sys (region_code, sys_type)`, `idx_tb_ops_doc_proj (plan_id)`, `idx_tb_ops_doc_infra (infra_id)`, `idx_tb_ops_doc_org_unit (org_unit_id)`. `doc_no` UNIQUE (자동 인덱스). |
| FR-3 | 신규 패키지 구조: `domain/ops/`, `repository/ops/`, `service/ops/`, `controller/ops/`. 엔티티: `OpsDocument`, `OpsDocumentDetail`, `OpsDocumentHistory`, `OpsDocumentAttachment`, `OpsDocumentSignature`. |
| FR-4 | 컨트롤러 `OpsDocController` (`/ops-doc/...`) — 4 종 (`FAULT`/`SUPPORT`/`INSTALL`/`PATCH`) list/detail/create/update/delete/PDF/엑셀 + `/ops-doc/list` 통합 리스트 (점검내역서 row 포함, 디자인팀 §D). |
| FR-5 | 점검내역서 URL/Controller/Template **유지** (`/document/inspect-detail/{id}`, `/document/api/inspect-*`, `templates/document/doc-inspect.html` 등). |
| FR-6 | `InspectReportService.linkToDocument()` (103-148줄) 제거 + **신규 `OpsDocLinkService` bean 분리** (codex 권고 — Spring self-invocation 회피): `InspectReportService` 가 `OpsDocLinkService` 를 `@Autowired` 로 주입받아 호출. `OpsDocLinkService.linkInspectReport(InspectReport)` public 메서드에 `@Transactional(propagation=REQUIRES_NEW)` 적용 → 별도 bean 호출 시에만 프록시 작동, 트랜잭션 격리 보장. ② COMPLETED 시 `tb_ops_doc.INSPECT` row 생성. ③ **DocumentRepository.findByDocNo fallback 제거** — 사업문서는 `INSP-` prefix 미사용으로 충돌 불가. ④ docNo prefix 컨벤션: **INSP-** (점검) / **FLT-** (장애) / **SUP-** (지원) / **INS-** (설치) / **PAT-** (패치) — 5 종간 충돌 방지 + UI 라벨에 prefix 표시 의무 (codex: INSP-/INS- 시각적 구분). |
| FR-7 | 운영문서 첨부 디렉토리 분리: `${file.ops-upload-dir:./uploads/ops-docs}` 신규 설정. `OpsDocAttachmentService` 가 사용. |
| FR-8 | top-nav 단일 드롭다운 유지 (디자인팀 §A). section header 라벨 정비: `사업 문서 작성` → **`사업문서`**, `운영·유지보수 문서` 그대로. "문서관리 목록" 단일 entry → **"📋 사업문서 목록"** + **"🛠 운영문서 목록"** 분리. **신규 메뉴 항목 인라인 hex 금지** — `var(--text2)`/`var(--surface)` 토큰 사용. **a11y 패턴은 disclosure navigation 채택** (codex 권고 — `role="menu"` 의 WAI-ARIA 전체 키보드 계약 회귀 위험 회피): `<button aria-expanded="false" aria-controls="...">문서관리 ▾</button>` + 펼침 영역 `<nav aria-label="문서관리">` + 항목 `<a aria-current="page" (현재 위치)>` + 키보드 (`Tab` 진입, `Enter/Space` 토글, `Esc` 닫기). `role="menu"` / `role="menuitem"` / `↓↑` 화살표 키는 사용 안 함. |
| FR-9 | enum 분리: `DocumentType` 사업 3 종 (`COMMENCE`/`INTERIM`/`COMPLETION`) 만 잔존, 신규 `OpsDocType` (5 종). **기존 `DocumentType.{INSPECT/FAULT/SUPPORT/INSTALL/PATCH}` 참조 코드 일괄 제거 — codex 권고로 명시 확장**: ① `DocumentDTO.getDocTypeLabel()` 의 5 종 분기, ② `PerformanceService` (성과 집계) 의 doc_type 별 분기, ③ `InspectDocNoMigrationRunner` (INSPECT 전용 docNo 포맷 마이그레이션 runner — `tb_document` row 0 + 신규로직은 `OpsDocLinkService` 가 처리하므로 **runner 자체 deprecate** 또는 ops 측으로 이전), ④ `DocumentRepository.findByDocNo` 호출부 검토 (사업측은 그대로, 점검측은 `OpsDocumentRepository.findByDocNo` 로 교체). 작업 시 `Grep "DocumentType\.(INSPECT\|FAULT\|SUPPORT\|INSTALL\|PATCH)"` 전수 + 컴파일 실패로 누락 0 검증. |
| FR-10 | DROP: `tb_inspect_checklist`, `tb_inspect_issue` (DROP IF EXISTS CASCADE — DB팀 §E 권고). |
| FR-11 | 점검내역서 페이지 시각 단서 (디자인팀 §B): `doc-inspect.html`/`inspect-detail.html`/`inspect-preview.html` 헤더에 ① 브레드크럼 `홈 › 운영·유지보수 문서 › 점검내역서` 의무 추가, ② page-header 좌측 그룹 chip `<span class="group-chip group-ops">운영문서</span>` (`var(--primary-50)` 배경, `var(--primary)` 텍스트), ③ 컨트롤러 `activeMenu="ops"` 모델 변수 → top-nav `aria-current` 자동 부착. |
| FR-12 | 신규 4 종 폼 레이아웃 가이드 (디자인팀 §C): `doc-inspect.html` 패턴 (단일 폼 + sub-section + section-title teal 좌측바), max-width 1100px, label 140px + value 그리드, 액션 버튼 page-header 우측, 첨부 영역 폼 최하단 단독 섹션. **인라인 hex 금지, `var(--primary)`/`var(--surface)`/`var(--border)`/`var(--text)` 토큰 100% 사용**. 모바일 반응형 (@media 900px) 의무. 엠프티 스테이트 (DESIGN.md §"엠프티 스테이트") 의무. |
| FR-13 | 신규 4 종 폼 필드 (디자인팀 §C 추가 권고): **장애** (`severity P1/P2/P3 chip`, `downtime_minutes`, `customer_impact_level`, 발생일/증상/원인/조치) / **업무지원** (`request_channel 전화/이메일/현장`, `man_hour`, `satisfaction_score`, 요청자/지원내용) / **설치** (`pre_check_completed 체크박스`, `rollback_window_until`, `verification_evidence` 첨부, 환경/버전/검증) / **패치** (`patch_kind 보안/기능/긴급`, `affected_users_count`, **`rollback_plan` textarea 필수 — 미입력 시 저장 차단**, 대상/버전). 컬럼은 `OpsDocumentDetail.section_data jsonb` 에 저장 (전용 컬럼 추가 후속 스프린트). |
| FR-14 | DESIGN.md 추가 시안 (디자인팀 §F): ① 운영문서 상태 뱃지 5 색 chip (DRAFT/COMPLETED + IN_PROGRESS/RESOLVED/ROLLED_BACK 후속 라이프사이클), ② ops 분류 chip 4 색 (장애=danger / 지원=info / 설치=success / 패치=warning), ③ PDF 출력 letterhead/serif 일관성 가이드 분기점 명시. |
| FR-15 | 안전망 SQL (DB팀 §F): 신규 DDL 적용 직전 `DELETE FROM tb_document WHERE doc_type IN ('INSPECT','FAULT','SUPPORT','INSTALL','PATCH');` 실행 — DocumentType enum 5 종 제거 시 enum 부재 row 부팅 실패 방지. |
| FR-16 | ERD/snapshot 동시 갱신 (DB팀 §E 권고): `docs/erd-document.mmd`, `docs/erd-descriptions.yml`, `docs/generated/erd.md`, `docs/references/snapshots/2026-04-27-prod-schema.sql` — `tb_inspect_checklist/issue` 제거 + `tb_ops_doc` 계열 추가 반영. |

---

## 3. 비기능 요건 (NFR)

| ID | 내용 |
|----|------|
| NFR-1 | Maven `clean compile test` 성공. 기존 점검내역서 테스트 (`InspectLegacyDropArchTest`, `InspectResultCodeTest`, `InspectCategoryConstraintTest`, `DbInitRunnerBaselineTest`) 100% 통과. |
| NFR-2 | 서버 재기동 후 정상 응답: `/document/list` (사업 3종), `/document/inspect-detail/{id}`, `/document/api/inspect-*`, `/ops-doc/list` (5종 통합), `/ops-doc/{type}/{id}` (4종 신규) — 모두 200/302. |
| NFR-3 | `DbInitRunner` 멱등 실행 (CREATE/INDEX IF NOT EXISTS, DROP IF EXISTS). `baseline-non-do-hashes.txt` 갱신. |
| NFR-4 | 점검내역서 회귀 0 건: 인벤토리 §1 의 9 개 라우트 (생성/조회/수정/삭제/PDF/상세/미리보기/이전월/템플릿) 모두 동작. |
| NFR-5 | 첨부 업로드 후 `uploads/ops-docs/yyyyMM/<UUID>.<ext>` 생성 + DB row 정합 (CASCADE 동작). |
| NFR-6 | UI 회귀 0 건: top-nav 4 그룹(사업문서·운영문서·견적·업무계획) 모두 정상 진입. **신규 ops-doc 페이지 다크모드 토큰 100% 사용** (인라인 hex 0 건 — 디자인팀 §E 강화). |
| NFR-7 | 신규 4 종 폼 모바일 반응형 (@media 900px) 동작. 갤럭시탭 S8 세로 시나리오 검증. |
| NFR-8 | DB CHECK 제약 위반 시 INSERT/UPDATE 실패 — 단위 테스트로 5 가지 위반 케이스(잘못된 doc_type, status, environment, support_target_type, doc_type-별 NULL 조합) 검증. |

---

## 4. 의사결정 / 우려사항

### 4-1. 점검내역서 이관 범위 — ✅ 선택적 커스터마이징
- 점검 본 데이터·Service·Repository·DTO·Template·Test 모두 유지. 변경은 `linkToDocument` → `linkToOpsDoc` 1 개 메서드.

### 4-2. URL 이전 여부 — ✅ 미이전
- `/document/inspect-*` 그대로. 메뉴는 운영 그룹에 표시하지만 라우팅은 기존 컨트롤러로. 시각 단서는 FR-11 (브레드크럼 + chip + activeMenu).

### 4-3. 사업문서 enum 정리 — ✅ 5 종 제거
- `DocumentType` enum 사업 3 종 잔존. FR-15 안전망 SQL 로 row 충돌 방지.

### 4-4. docNo prefix 컨벤션 — ✅ 5 종 분리
- 점검 `INSP-` / 장애 `FLT-` / 지원 `SUP-` / 설치 `INS-` / 패치 `PAT-` — `tb_ops_doc.doc_no UNIQUE` + prefix 로 5 종간 충돌 방지.

### 4-5. linkToOpsDoc 트랜잭션 — ✅ REQUIRES_NEW
- 점검 본 보고서 저장 트랜잭션과 분리 — ops 연계 실패가 본 보고서 롤백 안 함. 동시성은 `doc_no UNIQUE` 가 보호.

### 4-6. top-nav 정보 구조 — ✅ 단일 드롭다운 + section header 정비
- 기존 9 항목 GNB 폭(1700px 한계)에 새 최상위 메뉴 추가 시 overflow → 디자인팀 §A 권고대로 단일 드롭다운 유지.

### 4-7. document-list vs ops-doc/list — ✅ 분리 (점검은 ops-doc/list 에 표시)
- 디자인팀 §D 권고. 사업 3 종은 `document-list.html` 그대로, 운영 5 종(점검 포함) 은 `ops-doc/list` 신규.

### 4-8. 신규 ops-doc 다크모드 — ✅ Day 1 부터 토큰 100%
- AGENTS.md §7 "Phase 4 대기 중" 은 *기존* 인라인 hex 일괄 리팩터를 미루는 것. *신규* 코드는 토큰 의무 (디자인팀 §E).

### 4-9. linkToOpsDoc 구현 패턴 — ✅ 별도 bean 분리 (codex 권고)
- `InspectReportService` 의 private 메서드 `@Transactional(REQUIRES_NEW)` 는 Spring self-invocation 으로 프록시 미경유 → 무효.
- 신규 bean `OpsDocLinkService.linkInspectReport(InspectReport)` public + REQUIRES_NEW. `InspectReportService` 가 주입받아 호출 → 프록시 경유 → 트랜잭션 격리 보장.

### 4-10. top-nav a11y 패턴 — ✅ disclosure navigation (codex 권고)
- `role="menu"` / `role="menuitem"` 패턴은 WAI-ARIA 전체 키보드 계약 (↓↑/Home/End/문자 검색) 부담 → 회귀 위험.
- 단순 disclosure navigation (`<button aria-expanded>` + `<nav>`) + `Tab`/`Enter/Space`/`Esc` 만으로 충분.

---

## 5. 영향 범위

| 계층 | 파일 | 유형 |
|------|------|------|
| DB / DDL | `src/main/resources/db_init_phase2.sql` (`tb_ops_doc` + 자식 4 + DROP `tb_inspect_checklist/issue` + 안전망 DELETE + 인덱스 + CHECK) | 수정 |
| Domain | `src/main/java/com/swmanager/system/domain/ops/{OpsDocument, OpsDocumentDetail, OpsDocumentHistory, OpsDocumentAttachment, OpsDocumentSignature}.java` | 신규 (5) |
| Repository | `src/main/java/com/swmanager/system/repository/ops/Ops*Repository.java` | 신규 (5) |
| Service | `src/main/java/com/swmanager/system/service/ops/{OpsDocService, OpsDocAttachmentService, OpsDocSignatureService}.java` | 신규 (3) |
| Controller | `src/main/java/com/swmanager/system/controller/ops/OpsDocController.java` | 신규 |
| Service (신규 bean) | `src/main/java/com/swmanager/system/service/ops/OpsDocLinkService.java` (`linkInspectReport` public + `@Transactional(propagation=REQUIRES_NEW)` — Spring self-invocation 회피) | 신규 |
| Service (커스터마이징) | `src/main/java/com/swmanager/system/service/InspectReportService.java` (`linkToDocument` 제거 + `OpsDocLinkService` 주입·호출) | 수정 |
| Service (정리) | `src/main/java/com/swmanager/system/service/PerformanceService.java`, `src/main/java/com/swmanager/system/dto/DocumentDTO.java` (5 종 doc_type 분기 제거) | 수정 |
| Runner (정리) | `src/main/java/com/swmanager/system/config/InspectDocNoMigrationRunner.java` — deprecate 또는 ops 측으로 이전 | 수정 |
| Enum | `src/main/java/com/swmanager/system/constant/enums/OpsDocType.java` | 신규 |
| Enum (5 종 제거) | `src/main/java/com/swmanager/system/constant/enums/DocumentType.java` | 수정 |
| Controller (정리) | `src/main/java/com/swmanager/system/controller/DocumentController.java` (5 종 라우트 제거) | 수정 |
| DTO/Service (정리) | `DocumentDTO`, `DocumentService` (5 종 라벨/분기 제거) | 수정 |
| Template (제거) | `templates/document/{doc-fault, doc-support, doc-install, doc-patch}.html` 및 PDF 짝 | 삭제 (8) |
| Template (신규) | `templates/ops-doc/{list, doc-fault, doc-support, doc-install, doc-patch}.html` + PDF 짝 (인라인 hex 0 건) | 신규 (~10) |
| Template (수정 — 시각 단서) | `templates/document/{doc-inspect, inspect-detail, inspect-preview}.html` (브레드크럼 + 그룹 chip 추가) | 수정 (3) |
| Template (메뉴) | `templates/fragments/top-nav.html` (라벨 정비 + a11y 키보드 핸들러) | 수정 |
| Config | `application.properties` (`file.ops-upload-dir` 추가) | 수정 |
| ERD/Snapshot | `docs/erd-document.mmd`, `docs/erd-descriptions.yml`, `docs/generated/erd.md`, `docs/references/snapshots/2026-04-27-prod-schema.sql` | 수정 (4) |
| DESIGN.md | 운영문서 상태 뱃지 / 분류 chip 시안 | 수정 |
| Docs | `docs/exec-plans/doc-split-ops.md` | 신규 (다음 단계) |
| Tests | `src/test/.../OpsDoc*Test.java` (CHECK 제약 5 위반 케이스, 멱등성, OpsDocAttachmentService) | 신규 |

**수정 ~12 파일 / 신규 ~25 파일 / 삭제 ~8 파일. DB/API 계약 변경: 신규 `/ops-doc/*` API + 기존 `/document/api/{fault,support,install,patch}*` 제거.**

---

## 6. 리스크 평가

| 리스크 | 수준 | 완화책 |
|--------|------|--------|
| 점검내역서 `linkToOpsDoc` 변경으로 docNo 룩업 회귀 | 중간 | 기존 3 포맷 룩업 그대로, fallback 제거(사업측 `INSP-` 미사용으로 충돌 불가). REQUIRES_NEW 로 본 트랜잭션 격리. |
| Spring self-invocation 으로 REQUIRES_NEW 트랜잭션 무효화 | 중간 | `OpsDocLinkService` 별도 bean 분리 → `InspectReportService` 가 주입받아 호출 (codex 지적). 단위 테스트로 트랜잭션 격리 검증 (ops 연계 강제 실패 시 점검 본 보고서 정상 저장 확인). |
| top-nav `role="menu"` WAI-ARIA 키보드 회귀 | 낮음 | disclosure navigation 패턴 채택 (codex 지적) — `Tab/Enter/Space/Esc` 만 지원. ↓↑ 키는 의도적 미지원. |
| `DocumentType` 제거 시 외부 참조 누락 (PerformanceService, InspectDocNoMigrationRunner, DocumentDTO) | 중간 | FR-9 grep 전수 + 컴파일 실패 검증 + 4 개 명시 정리 대상 영향 범위에 추가. |
| 첨부/서명 호출부 누락 | 중간 | 인벤토리 §9 기반 grep 후 일괄 변경. 회귀 테스트로 첨부 업로드/조회/삭제 검증. |
| `DocumentType` 5 종 제거 시 외부 참조 누락 | 중간 | `Grep "DocumentType\.(FAULT|SUPPORT|INSTALL|PATCH)"` 전수 + 컴파일 실패 확인. |
| top-nav a11y 회귀 (기존 사용자 키보드 동선 변화) | 낮음 | 기존 마우스 동선 그대로 + 키보드 핸들러 *추가*. ARIA 속성도 *추가*. |
| 신규 4 종 폼 컬럼 부족 | 중간 | `OpsDocumentDetail.section_data jsonb` 패턴 사용. FR-13 의 권고 필드는 jsonb 에 저장. 전용 컬럼은 후속 스프린트. |
| `tb_inspect_checklist/issue` DROP 시 외부 참조 | 낮음 | DB팀 자문 결과: src/main/java/ 잔존 0 건 (Document.java:104-106 주석만). docs/exec-plan 27 파일은 역사 기록. |
| 신규 ops-doc 다크모드 부채 추가 | 낮음 | NFR-6 강화: 인라인 hex 0 건 강제. CI 단계에서 grep 검증 추가 검토. |
| docNo prefix 충돌 (5 종 간) | 낮음 | UNIQUE + prefix 컨벤션 (FR-6) — INSP-/FLT-/SUP-/INS-/PAT-. |
| DB CHECK 조합 제약으로 기존 점검 row 저장 실패 | 낮음 | row 0 건 상태 + INSPECT 만 infra_id NOT NULL 강제 — 점검 코드는 이미 infra_id 항상 set. |
| ERD/snapshot 갱신 누락 | 낮음 | FR-16 으로 명시적 task. 개발계획서에 단계 분리. |

---

## 7. 승인 요청

본 v2 기획서에 대한:
- 🤖 codex 검토 (다음 단계, 자동 호출)
- 사용자 최종승인

요청합니다. 승인 시 `docs/exec-plans/doc-split-ops.md` 개발계획서 작성으로 이동.

### v1 → v3 변경 요약 (자문·검토 흡수 16 항목)

| # | 출처 | 반영 위치 |
|---|---|---|
| 1 | DB §A — `plan_id`/`created_by`/`updated_by` 추가 | FR-2 |
| 2 | DB §A — signature_image TEXT 확장 | FR-2 |
| 3 | DB §A — 조합 CHECK 제약 | FR-2-CHK |
| 4 | DB §B — 화이트리스트 CHECK + 외부 FK NO ACTION + 인덱스 | FR-1, FR-2-CHK, FR-2-IDX |
| 5 | DB §C — REQUIRES_NEW 트랜잭션 + fallback 제거 | FR-6, §4-5 |
| 6 | DB §F — docNo prefix 컨벤션 5 종 | FR-6, §4-4 |
| 7 | DB §F — DocumentType 안전망 SQL | FR-15 |
| 8 | DB §E — ERD/snapshot 동시 갱신 | FR-16, §5 |
| 9 | 디자인 §A — top-nav 단일 드롭다운 + section header + 토큰 | FR-8, §4-6 |
| 10 | 디자인 §B/§C — 점검 페이지 브레드크럼·chip / 신규 4 종 레이아웃·필드 / 다크모드 토큰 | FR-11, FR-12, FR-13, NFR-6 |
| 11 | 디자인 §F — DESIGN.md 상태 뱃지·분류 chip / 모바일 반응형 / 엠프티 스테이트 | FR-14, NFR-7 |
| **12** | **codex — `doc_no NOT NULL`** | FR-2 |
| **13** | **codex — Spring self-invocation 회피, `OpsDocLinkService` 별도 bean** | FR-6, §4-9, §5, §6 |
| **14** | **codex — `role="menu"` 대신 disclosure navigation** | FR-8, §4-10, §6 |
| **15** | **codex — `DocumentType` 제거 영향 명시 확장** (`PerformanceService`/`InspectDocNoMigrationRunner`/`DocumentDTO`) | FR-9, §5, §6 |
| **16** | **codex — 개발계획서 5 필수 작업** (Playwright 키보드 테스트, grep 체크리스트, section_data 검증표 등) | 개발계획서로 이월 |
