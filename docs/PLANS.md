# PLANS.md — 스프린트·로드맵 요약

> ⚠ **자동 생성 초안 — 검증 필요**
> 근거: `docs/design-docs/data-architecture-roadmap.md` + `docs/product-specs/*.md` 집계

---

## 1. 활성 로드맵

**[데이터 아키텍처 로드맵](./design-docs/data-architecture-roadmap.md)** (Wave 1~4)
- Wave 1~3: ✅ 완료
- Wave 4: ✅ 완료 (S15/S16)
- S8-C 후속: ✅ 완료
- S8-B (status 정규화), S16-B (repeat_type): 보류 — 단일값 사용 중, 긴급도 낮음

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
- `phase2-V018-init-ordering` — `db_init_phase2.sql` ON CONFLICT INSERT 와 V018 UNIQUE 의존성 정리
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

*Last updated: 2026-04-27 · phase1-ddl-formalization 스프린트 완료*
