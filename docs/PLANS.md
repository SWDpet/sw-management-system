# PLANS.md — 스프린트·로드맵 요약

> ✅ **2026-06-23 검증** — 현행 활성 로드맵(§1)·완료 스프린트 갱신. 본 문서는 스프린트 **로그**(이력성)이며 §2 이하 표는 시점 기록으로 보존.

---

## 1. 활성 로드맵

### 1-a. S-tier 품질 (현재 주력) — `docs/QUALITY_CHARTER.md`
등급=게이트 강제 불변식. 진행: §6-4 Map→DTO(ratchet **188 plateau**)·§6-5 거대클래스 분리(부채 **0**)·PIT 뮤테이션 게이트(95%)·문서 A 승급(2026-06-23). 차원별 등급은 `docs/QUALITY_SCORE.md`. 잔여: 코드품질/아키텍처 B→A(응답형태 변경 결정 필요), beyond-A(Testcontainers·커버리지·CI — 미착수).

### 1-b. 데이터 아키텍처 로드맵 (`docs/design-docs/data-architecture-roadmap.md`, Wave 1~4)
- Wave 1~4: ✅ 완료 (S15/S16), S8-C 후속 ✅
- S8-B(status 정규화)·S16-B(repeat_type): 보류 — 단일값 사용 중, 긴급도 낮음

### 1-c. 기능 스프린트 (최근, 모두 구현·검증·푸시 완료)
- `ops-fault-support`(장애·지원 + KB 추천, 2026-06-13) / `doc-signed-scan-upload`(날인본 스캔) / `log-management-improvement`(로그관리·대시보드 통계) / `global-sidebar-responsive`(사이드바 전환)

### 1-d. 봉인(PAUSED, 사용자 결정) — 재개 전 건드리지 않음
- `phase2-vsql-external-deps` 외 fresh-init 재현성 백로그(§2-b): V-script replay 정리 — 저우선. (단 phase2 자기완결 3-테이블 갭은 2026-06-23 해소)

---

## 2. 완료 스프린트 (최근 세션)

| Wave | 스프린트 | 요약 |
|------|---------|------|
| 1 | S6 legacy-contract-tables-drop | tb_contract DROP |
| 1 | S2 process-master-dedup | 290x 중복 정리 |
| 1 | S5 access-log-userid-fix | access_logs userid 정제 |
| 1 | S3 qt-remarks-users-link | 견적서 비고 users FK |
| 1 | S3B users-masking-regression-fix | 마스킹 회귀 fix |
| 2 | **S1 inspect-comprehensive-redesign** | 문서관리 대형 통합 (6 조치) |
| 3 | S9 access-log-action-and-menu-sync | action_type Enum + MenuName |
| 3 | S10 inspect-check-result-category-master | check_category_mst 16행 |
| 3 | S8 qt-quotation-domain-normalize | qt_category_mst 3행 |
| 4 | S15 pjt-equip-decision | pjt_equip DROP |
| 4 | S16 tb-work-plan-decision | work_plan_type/status_mst 신설 |
| 4 | S8-C qt-template-type-enum | QuoteTemplateType Enum |

### 2-a. 감사 후속 (2026-04-19, 5 스프린트, 총 20건 조치)

근거: `docs/generated/audit/2026-04-18-system-audit.md` (codex Phase 1 감사)

| ID | 스프린트 | 요약 | 커밋 |
|----|---------|------|------|
| 1 | audit-fix-p1 | P1 보안 4건 (DB 비밀번호 / 문서 API 권한 / 민감정보 평문 / inspect_report 스키마) | `2d0a9c1` |
| 2a | audit-fix-p2-schema-docs | P2 스키마·문서 정합성 6건 (init SQL DDL / ERD 정리 / plans 동기화) | `b93e8bc` |
| 2b | audit-fix-p2-deadcode | P2 Dead code 3건 (ProjectService / SwService 미호출 / orphan 템플릿) | `914fa5c` |
| 2c | audit-fix-p2-security-logging | P2 보안·로깅 4건 (MAC 응답 / show-sql / debug 로그 / Validation 로그) | `d73dd12` |
| 3 | audit-fix-p3-minor | P3 경미 3건 (LoginController / SwRepository / AdminUser keyword 로그) | `a27032c` |

후속 백로그 (별도 스프린트 또는 운영): `docs/generated/audit/dashboard.md` 참조 — security-hardening-v2 (보류), 운영 작업 (DB password rotation, Shodan 점검 등).

### 2-b. phase1 DDL 정식화 (2026-04-27, 단일 스프린트)

근거: `docs/product-specs/phase1-ddl-formalization.md` (v3) + `docs/exec-plans/phase1-ddl-formalization.md` (v1).

| 항목 | 내용 |
|------|------|
| 스프린트 | `phase1-ddl-formalization` |
| 산출물 | `db_init_phase1.sql` (19 테이블 + 마스터 54건) / `db_init_phase1_sigungu.sql` (279행) / `db_init_phase2.sql` 헤더 정정 / `docs/references/snapshots/2026-04-27-prod-schema.{sql,meta.md}` / `docs/references/setup-guide.md` §2-2 |
| 검증 | Step 8 ephemeral schema diff = **0건** (phase1 19 테이블 한정, `\restrict` 보안 토큰 외) |
| 발견 #1 (in-scope, 패치) | `phase1.sql` DO 블록 EXCEPTION 결손 — `WHEN invalid_table_definition THEN NULL;` 35건 추가 (3회 멱등성 통과) |
| 발견 #2 (out-of-scope) | `phase2.sql:60` ON CONFLICT 가 `V018_process_master_dedup.sql` UNIQUE 에 의존 — fresh init 깨짐. 후속 `phase2-V018-init-ordering` 스프린트로 분리 |
| 차단 해소 | Docker 미설치 → 동일 PG 16 binary 별도 클러스터(`localhost:25880`, data `C:\Users\PUJ\pg16-verify\data`) 로 우회. exec-plan 본문 25432→25880 정정 (9건) |
| Step 9-A | 운영DB `ro_phase1_audit` NOLOGIN + VALID UNTIL 'yesterday' (사용자 작업) |
| 커밋 | (본 작업완료 커밋에 채움) |

후속 백로그 (별도 스프린트):
- `phase2-V018-init-ordering` — **완료 (2026-05-11, 기획서 v3.3 + 개발계획서 v1.1 + commit `<TBD>`)**:
  - 산출물: `db_init_phase2.sql` (UNIQUE 제약·INDEX 선이동 + NFR-3-x 검증 게이트 + 헤더/INSERT 주석 보강)
  - 검증 (ephemeral 25880, v3.3 scope 한정): T1 (phase2 멱등 3회 — NFR-3-x PASS NOTICE + count=5/distinct=5/UNIQUE 1) / T2 (psql fresh-init line 60/70 stop 회귀 0건) / T3 (V018 재진입 rc=0 + DELETE 0) / T4 (V018 SHA256 무변경)
  - V018 무수정 (Flyway checksum 보존). 운영DB 영향 0.
  - Discovery: phase2.sql:464 `tb_org_unit` forward-reference 표면화 → 후속 `phase2-tb_ops_doc-forward-ref` 분리
- `phase2-tb_ops_doc-forward-ref` — **완료 (2026-05-11, commit `<TBD>`)**:
  - 산출물: `db_init_phase2.sql` (`tb_work_plan` + `tb_org_unit` 블록 + 39 seed INSERT 를 `tb_ops_doc` 직전으로 통째 선이동)
  - 검증 (ephemeral 25880, 본 sprint surface scope): T1 phase2 멱등 3회 — surface ERROR 0 / T2 fresh-init line 60/70/464 stop 회귀 0건 / T3 git diff 의미 불변 (`<` 0건, hunk 3개 H1+H2+H3·4 병합) / T4 V100 SHA256 무변경
  - V100 무수정 (Flyway checksum 보존). 운영DB 영향 0 (`CREATE TABLE IF NOT EXISTS` 멱등성).
  - **선행 sprint phase2-V018-init-ordering §10 Discovery closure 완료** — line 464 forward-reference 해소.
  - **신규 Discovery**: `qt_category_mst` (phase2.sql:682 INSERT) 가 외부 V024 (`swdept/sql/V024_qt_category_master.sql`) 에 의존. fresh-init 시 phase2 가 V*.sql 보다 먼저 실행돼 line 686 stop 가능성. 별도 후속 sprint 영역.
- `phase2-vsql-external-deps` — ⏸ **PAUSED v2 (2026-05-11)**:
  - 기획서 `docs/product-specs/phase2-vsql-external-deps.md` v2 + 개발계획 `docs/exec-plans/phase2-vsql-external-deps.md` v1.2 모두 codex ⭕ + 사용자 최종승인 → 구현 Step 1~2 완료 (3 INSERT 삭제 + compile PASS) → Step 3 T2 측정 시 phase2.sql rc=0 ✅ (본 sprint 핵심) 그러나 **V*.sql 9건 본 sprint 외 결함 표면화** → 사용자 결정 (option 4) 으로 phase2.sql revert + sprint 일시 중단
  - 9건 분석 보고서: `docs/references/vsql-fresh-init-defects-analysis.md` v3 (codex 1차 ⚠ 7건 + 2차 ⚠ 차단 2건 반영)
  - **재진입 조건**: 후속 sprint `vsql-fresh-init-procedure-fix` 처리 → V*.sql 7건 해소 → 본 기획서 v3 개정 (skip-list 명시) → 재개. 잔여 V005/V100 은 별도 sprint 위임
- `vsql-fresh-init-procedure-fix` (신규 권고, 2026-05-11) — fresh-init 절차 보강 sprint. setup-guide §2-2 의 V*.sql for-loop 에 (1) skip-list 추가 (V021_rollback_data, V022, V023, V025 — 영구 skip + V005/V100 — 임시 skip) + (2) `-v run_id=...` 추가로 V019/V020/V021_users_masking 통과. V*.sql 무수정 → Flyway 영향 0. 본 sprint 가 `phase2-vsql-external-deps` 재진입의 직전 sprint
- `vsql-V005-ordering` (신규 권고, 2026-05-11) — V005 두 파일 ordering 결함 (V005_add_surveying_wage_rates 가 V005_wage_rate_table 보다 먼저 실행) 정리. **옵션 E (manifest 기반)** 우선 → 장기적으로 옵션 B (단일 파일 통합). 운영DB flyway_schema_history 확인 선행. 분량 소~중
- `vsql-V100-legacy-contract-cleanup` (신규 권고, 2026-05-11) — V100 의 legacy contract block (헤더 주석에 예고된 후속 정리 미완) 전체 정리. `tb_document.contract_id`, `idx_doc_contract`, line 388-390 trigger loop 의 `tb_contract`, `tb_contract_participant/target`, `CREATE INDEX IF NOT EXISTS` 일괄 보강 포함. 운영DB history + 기획 검토 선행. 분량 중~대
- `vsql-V025-idempotent` (선택 권고, 2026-05-11) — V025 의 멱등성 결함 (이미 DROP 된 pjt_equip 재참조). `vsql-fresh-init-procedure-fix` 의 skip-list 처리가 충분하므로 본 sprint 는 선택. skip 부적합 시 V025 자체에 `IF EXISTS` 가드 추가 (Flyway 위배 위험)
- `ro_phase1_audit` 롤 DROP (Step 9-B, 별도 시점에 사용자 결정)

---

## 3. 영구 패스 (추천 금지)

- S4 license-country-cleanup
- S11 license-registry-type-enum
- S12 geonuris-license-type-enum
- S13 (S4 병합)
- S14 qr-license-decision

근거: 수동 한글화 정책 (`AGENTS.md §6`).

---

## 4. 문서 구조

본 프로젝트 문서 레이아웃:
- `docs/product-specs/` — 기획서 (설계 스펙)
- `docs/exec-plans/` — 개발계획서 (실행 계획)
- `docs/design-docs/` — 결정 완료 (로드맵·-decision)
- `docs/generated/audit/` — 감사 결과
- `docs/generated/erd.md` — ERD

자세한 맵: `AGENTS.md §2`.

---

*Last updated: 2026-05-11 · phase2-vsql-external-deps PAUSED + V*.sql 9건 분석 보고서 v3 + 후속 sprint 4개 (vsql-fresh-init-procedure-fix / V005-ordering / V100-legacy-contract-cleanup / V025-idempotent) 등록*
