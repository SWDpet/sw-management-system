---
tags: [plan, sprint, audit, data-architecture, refactor]
sprint: "data-architecture-audit"
status: draft-v2
created: "2026-04-20"
---

# [기획서] 데이터 아키텍처 감사 — 기초 마스터 활용도 전수 조사

- **작성팀**: 기획팀
- **작성일**: 2026-04-20
- **선행 스프린트**: `refactor-01-hardcoding #1-A #1-B` (완료), `sys-type-normalization` (보류 → 본 감사에 흡수)
- **상태**: 초안 v2 (codex 재검토 대기)

### v2 주요 변경 (사용자 확정 + codex v1 피드백 반영)

**사용자 확정 (2026-04-20)**: 초기 설계 마스터 테이블 **9개** 명시
- `prj_types`, `sys_mst`, `cont_frm_mst`, `maint_tp_mst`, `cont_stat_mst`, `sigungu_code`, `sw_pjt`, `ps_info`, `users`
- 실데이터 확인: 모두 존재. (cd, nm) 스타일 5개 + sw_pjt/ps_info/users 소유 마스터 + sigungu_code(지자체)
- **중대 발견 의심**: `maint_tp_mst`(7건)는 `DocumentType`(8종)과 거의 일치 가능성, `cont_stat_mst`(2건)는 `DocumentStatus`(DRAFT/COMPLETED, 2건)와 정확히 일치 가능성. Phase 2에서 정밀 검증.

**codex v1 피드백 5건 반영**
1. §2-1을 "기초 4개 기능 + 공통 마스터 레이어" 계층화 (마스터 9개 명시)
2. §3 분류 규칙 보강 — `primary_type` 1개 필수 + `secondary_tags` 복수 + 우선순위 ②>③>①>④>⑤
3. Phase 2 타임박스 **3~5일**로 조정 + 기능별 **최소 산출 기준** 명시
4. §7-2 `#1-A` 결정 매트릭스 추가 (마스터 존재·API 영향·마이그 비용·런타임 안정성)
5. §7-5 read-only 안전통제 — 전용 계정, `SET TRANSACTION READ ONLY`, statement timeout, row limit, SQL allowlist

**추가 확인 3건 반영**
6. §2-2에서 권한/메뉴/배치/알림/코드성 설정 테이블 항목 추가
7. `org_unit`은 사용자관리 하위 명시 (중복 해소)
8. 운영 DB 개인정보 마스킹/반출 금지 기준 §7-5에 병합

---

## 1. 배경 / 목표

### 배경 — 체계적 설계 맹점 발견

사용자(프로젝트 오너)가 초기 DB 설계·검수 단계에서 **기초 데이터를 마스터 테이블 형태로 체계적으로 구성**해 두었으나, 이후 agent 기반 기능 개발 과정에서 **기존 마스터 테이블 존재를 확인하지 않고** 다음과 같은 문제가 발생:

| 증상 | 구체 사례 |
|------|----------|
| ① 하드코딩 | `"UPIS"`, `"KRAS"`, `"COMMENCE"`, `"COMPLETED"` 등 리터럴을 코드에 박음 |
| ② 불필요한 마스터 재창조 | `sys_mst`(37종)가 이미 있는데 `SystemType` Enum을 새로 만들려 함 |
| ③ 중복/부분 조합 테이블 | 기초 마스터를 FK로 참조하면 됐는데 별도 조합 스키마 가능성 |

**결정적 발견 (2026-04-20)**:
- `sys_mst` 테이블이 37개 시스템 코드를 완비하고 있음에도 `sys-type-normalization` 스프린트 기획 단계까지 인지하지 못함
- `"112"` 값이 Enum 규칙 위반이라 판단해 `E112`로 리네이밍 계획 → 실제로는 `sys_mst.cd='112'`, `nm='112시스템'`으로 정당 등록된 값. **불필요한 데이터 손상 직전**
- 사용자 지적: "기초 데이터를 내가 다 파악하고 설계·검수했는데 agent에게 맡긴 이후 기존 데이터 활용 없이 재조합만 만들고 있는 것 같다"

### 목표

1. **DB 전수 분석** — 기초 4개 기능의 마스터 성격 테이블/컬럼 완전한 인벤토리 작성
2. **감사 대상 기능 전수 검증** — 각 기능에서 코드·DB가 마스터를 올바르게 참조 중인지 확인
3. **개선 로드맵 수립** — 발견된 문제를 우선순위별 스프린트로 분해
4. **`#1-A` Enum 도입 결정 재평가** — `DocumentStatus`/`DocumentType`이 마스터와 중복이면 롤백 권고

### 비목표 (범위 외)

- 기초 4개 기능의 **테이블/코드 수정은 하지 않음** (참조 파악만)
- 감사 결과의 실제 적용은 **후속 스프린트에서 분리 진행** (본 스프린트는 **감사 + 로드맵만**)
- 외부 API 연동·프론트엔드 프레임워크 변경 등 범위 외

---

## 2. 범위 정의

### 2-1. 기초 마스터 (감사 대상 **아님**, 수정 금지) — 2계층 분리

#### A. 공통 마스터 레이어 (cross-cutting — 소유자: 프로젝트 오너 / 승인권자: 사용자)

사용자 확정 9개 마스터 중 공통 성격 + sw_pjt/ps_info/users:

| 테이블 | 컬럼 | 레코드 수 | 성격 |
|--------|------|----------|------|
| `prj_types` | (cd, nm) | 3 | 프로젝트 유형 |
| `sys_mst` | (cd, nm) | 37 | **시스템 코드** (UPIS/KRAS/...112 포함) |
| `cont_frm_mst` | (cd, nm) | 5 | 계약 형태 |
| `maint_tp_mst` | (cd, nm) | 7 | 유지보수 유형 ⚠️ `DocumentType` 후보 |
| `cont_stat_mst` | (cd, nm) | 2 | 계약 상태 ⚠️ `DocumentStatus` 후보 |
| `sigungu_code` | (instt_c, adm_sect_c, full_name, sido_name, sgg_name) | 279 | 지자체 행정구역 |

#### B. 기초 기능 소유 마스터 (기능별 소유 — 감사 제외)

| 소유 기능 | 마스터 테이블 | 비고 |
|----------|--------------|------|
| **사업관리** | `sw_pjt` (596건) | 사업 본체 (복합 속성 다수) |
| **담당자관리** | `ps_info` (107건) | 담당자 마스터 |
| **사용자관리** | `users` (10건), `org_unit` | 인증 + 조직도 (org_unit 포함) |
| **서버관리** | `tb_infra_master`, `tb_infra_server`, `tb_infra_software`, `tb_infra_link_*`, `tb_infra_memo` | 인프라 (공통 마스터 아님, 서버관리 기능 내부) |

**중요 규칙**: A/B 모두 본 스프린트에서 수정 금지. 참조 파악만 수행. 다만 감사 대상 기능이 A/B 중 어느 것과 어떤 관계로 연결되어야 하는지는 분석 대상.

### 2-2. 감사 대상 기능 (11개 대분류 — v2 확장)

| # | 기능 | 주요 테이블 (추정) |
|---|------|-------------------|
| 1 | 문서관리 전반 | `tb_document`, `tb_document_detail`, `tb_document_attachment` |
| 2 | 점검내역서 | `inspect_report`, `inspect_check_result`, `inspect_visit_log`, `inspect_template` |
| 3 | 업무계획 | `tb_work_plan`, 관련 프로세스 마스터 |
| 4 | 성과관리 | `tb_performance_summary` |
| 5 | 견적서 | `tb_quotation`, `tb_quotation_item`, `pattern_*`, `wage_rate_*`, `remarks_pattern` |
| 6 | 라이선스 | `geonuris_*`, `license_*`, `tb_license_registry` |
| 7 | QR 코드 | `tb_qr_*` |
| 8 | 시스템 그래프 / ERD | (주로 뷰·리포트 기능, 별도 테이블 없을 수 있음) |
| 9 | 권한 / 메뉴 / 배치 / 알림 / 코드성 설정 (v2 추가 — codex 권장 #1) | `access_logs`, 공통 코드 테이블, 배치 상태 등 |
| 10 | 기타 공통 (메시지·설정) | `messages.properties`, `application.properties` 외부화 값 |
| 11 | 부가 (문서 버전, 댓글 등 — Phase 1에서 재발견 시) | Phase 1 결과 기반 확장 |

**주의 (codex 권장 #2 반영)**: `org_unit`은 §2-1 B(사용자관리 하위)에 포함 — **감사 대상 아님**. 혼동 방지.

---

## 3. 발견 유형 분류 (v2 — codex 권장 #2 반영)

각 감사 대상 기능 내 발견 사례에 아래 5가지 유형 태깅.

### 3-1. 유형 정의

| 유형 | 정의 | 조치 방향 | 스프린트 규모 |
|------|------|----------|---------------|
| **① 코드 하드코딩** | 마스터 테이블에 값이 존재하는데 Java/JS 코드에 리터럴로 박힘 | 코드 리팩터 — `Repository.findBy*()` 또는 캐시 조회로 대체 | 소~중 |
| **② 불필요한 중복 테이블/컬럼** | 기초 마스터를 FK로 참조하면 충분한데 별도 VARCHAR 컬럼 또는 중복 테이블로 조합 | **DB 스키마 재구성** — 컬럼 타입 변경, FK 추가, 중복 테이블 제거 | 중~대 |
| **③ 부분 중복 (정규화 미흡)** | 일부 컬럼은 마스터 참조 + 일부는 자체 조합 | 정규화 — FK 재설계, 데이터 마이그레이션 | 중 |
| **④ 마스터 미존재 (신설 필요)** | 코드에만 있고 DB 마스터 없음, 현재 실사용 중 | 마스터 테이블 신설 후보 (사용자 최종 판단) | 중 |
| **⑤ 레거시 미사용** | 테이블/컬럼 존재하지만 실사용 코드 없음 | 제거 대상 (P3) | 소 |

### 3-2. 분류 규칙

- **`primary_type`: 1개 필수** — 사례 하나당 가장 지배적인 유형 1개
- **`secondary_tags`: 복수 허용** — 부차적으로 해당되는 유형 태깅 (예: primary ②, secondary [①, ③])
- **우선순위 (동시 성립 시 선택 규칙)**: **② > ③ > ① > ④ > ⑤**
  - 구조적 문제(②③)가 코드 문제(①)보다 우선
  - 신설 제안(④)보다 기존 활용(①③)이 우선
  - 제거(⑤)는 항상 마지막

### 3-3. `#1-A` Enum 재평가 원칙

- `DocumentStatus` (DRAFT/COMPLETED) → `cont_stat_mst`(2건)와 정확히 일치 **강력 의심** → primary_type **②** 후보
- `DocumentType` (COMMENCE/INTERIM/COMPLETION/INSPECT/FAULT/SUPPORT/INSTALL/PATCH, 8종) → `maint_tp_mst`(7건)와 거의 일치 **의심** → Phase 2에서 정밀 검증
- 등록되어 있으면 → 유형 ② 또는 ③ 로 분류, 롤백 후보 스프린트로 로드맵 등록
- 등록되어 있지 않으면 → Enum 유지 or 마스터 신설 (④) 판단
- 결정은 **§7-2 결정 매트릭스**로 정량 판정

---

## 4. 작업 구조 (Phase)

### Phase 1: 기초 마스터 데이터 인벤토리 (0.5~1일)

**1-1. DB 전수 스캔**
```sql
SELECT table_schema, table_name, column_name, data_type, character_maximum_length
  FROM information_schema.columns
 WHERE table_schema = current_schema()
 ORDER BY table_name, ordinal_position;
```
→ `docs/generated/audit/db-schema-full.md`

**1-2. 기초 4개 기능 소유 테이블 확정**
§2-1 목록을 실제 엔티티/Repository와 대조. 추가 테이블 발견 시 기획서 보완.

**1-3. 각 마스터 테이블 실데이터 덤프**
- `sys_mst` (이미 확인: 37건)
- `sigungu_code` (지자체)
- `maint_tp_mst`, `prj_types`, `cont_stat_mst`, `cont_frm_mst` (사업 관련)
- `users`, `org_unit`
- 기타 발견 시

→ `docs/generated/audit/data-architecture-master-inventory.md` (v1 산출물)

### Phase 2: 감사 대상 기능 검증 (3~5일, v2 codex 권장 #3 반영)

각 감사 대상 기능 (11개 대분류)마다 다음 절차:

1. **사용 테이블 파악** — Entity 클래스 + Repository + JPQL `@Query` 조사
2. **컬럼별 마스터 매칭** — 각 VARCHAR/코드성 컬럼이 기초 마스터에 존재하는지
3. **코드 하드코딩 스캔** — `rg` 기반 리터럴 전수 + switch/equals 패턴
4. **실데이터 품질 확인** — 고아 값 존재 여부 (FK 없으므로 런타임 유입 가능)
5. **유형 분류** (§3) — primary_type + secondary_tags

**기능별 최소 산출 기준 (타임박스 3일 유지 시 필수)**:
- 각 기능당 최소 **2개 컬럼 이상** 마스터 매칭 분석
- 각 기능당 최소 **1개 구체 SQL/grep 근거** 제시
- 발견 유형 미확정 사례는 "추가 조사 필요"로 표시 후 Phase 3 로드맵에 이관

**일정 운영**:
- 하한 3일 (기능별 최소 산출 기준 충족)
- 상한 5일 (기능별 완전 심층 조사)
- 3일 초과 시점에 중간 리뷰 → 사용자 판단으로 상한 조정

**산출물**: `docs/generated/audit/data-architecture-utilization-audit.md`

구조:
```markdown
## 기능 1: 문서관리
### 테이블
- tb_document (...)
### 마스터 매칭
| 컬럼 | 값 종류 | 마스터? | 유형 |
| status | DRAFT/COMPLETED | ❌ 마스터 없음 | ④ 마스터 신설 후보 |
| sys_type | UPIS/KRAS/... | ✅ sys_mst | **① 리팩터 + #1-A Enum 롤백** |
...
```

### Phase 3: 개선 로드맵 (0.5일)

Phase 2 결과를 우선순위별 스프린트로 분해.

**우선순위 기준**:
- **P1**: 마스터 존재하는데 코드·스키마에 중복 → 스키마 재구성 (유형 ②③)
- **P2**: 코드 하드코딩 — 마스터 조회로 대체 (유형 ①)
- **P3**: 마스터 신설 필요 (유형 ④), 레거시 제거 (유형 ⑤)

**산출물**: `docs/design-docs/data-architecture-roadmap.md` — 후속 스프린트 목록 + 각 스프린트 우선순위·영향도·예상 범위

### Phase 4: 기존 `sys-type-normalization` 처리

본 감사 결과에 흡수. Phase 3 로드맵의 일부 스프린트로 재편성:
- 실제 고아 데이터 4건 (`UPIS_SW`×2, `SPFMS`, `CIAMS`)은 별도 **데이터 정제 스프린트**로 분리
- FK 제약 추가는 P1 스프린트로 편성

---

## 5. 기능 요건 (FR)

| ID | 내용 |
|----|------|
| FR-1 | `docs/generated/audit/db-schema-full.md` — DB 전 테이블 스키마 덤프 (자동 생성 허용) |
| FR-2 | `docs/generated/audit/data-architecture-master-inventory.md` — 기초 4개 기능의 마스터 테이블 목록 + 각 테이블 스키마 + 실데이터 값 분포 |
| FR-3 | `docs/generated/audit/data-architecture-utilization-audit.md` — 감사 대상 11개 기능별 섹션, 컬럼별 마스터 매칭 표, 유형 분류, 구체 사례 |
| FR-4 | `docs/design-docs/data-architecture-roadmap.md` — 우선순위별 후속 스프린트 목록, 각 스프린트 기획 개요 |
| FR-5 | `#1-A` Enum 도입 결정 재평가 결과 — 유지/롤백/부분수정 중 1 권고안 + 근거 (유틸리제이션 감사 섹션 내) |
| FR-6 | **수정 금지 준수** — 본 스프린트에서 코드·DB 변경은 **없음**. 감사 문서만 산출. |

---

## 6. 비기능 요건 (NFR)

| ID | 내용 |
|----|------|
| NFR-1 | 본 스프린트 종료 시 `./mvnw clean compile` / 서버 동작에 영향 없음 (문서 산출만) |
| NFR-2 | 감사 문서는 모두 마크다운 형식, 실데이터 근거 포함 (파일:라인 또는 SQL 결과) |
| NFR-3 | 감사 대상 11개 기능 모두 조사 완료 — Phase 2 섹션 11개 이상 생성 |
| NFR-4 | 기초 4개 기능 수정 0건 (git diff로 확인) |
| NFR-5 | 로드맵은 최소 3~5개 이상의 후속 스프린트로 분해 (P1~P3 각 1건 이상) |
| NFR-6 | `#1-A` Enum 재평가 결론이 문서에 명확히 기록 |

---

## 7. 의사결정 / 우려사항

### 7-1. `sys-type-normalization` 보류 — ✅ 본 감사에 흡수
- v2 기획서(`docs/product-specs/sys-type-normalization.md`) 상태를 `superseded-by-data-architecture-audit`로 변경
- 단, Pre-flight 결과(`hardcoding-preflight-result.md`)는 귀중한 입력 데이터로 재사용

### 7-2. `#1-A` Enum 재평가 — 결정 매트릭스 (v2 codex 권장 #4 반영)

Phase 2 결과를 아래 매트릭스로 평가하여 **유지 / 선별 롤백 / 후속 패치 / 전체 롤백** 중 선택.

| 평가 기준 | 측정 방법 | 유지 | 선별 롤백 | 후속 패치 | 전체 롤백 |
|-----------|-----------|------|-----------|-----------|-----------|
| **마스터 존재** | Phase 2 매칭 결과 | 마스터 없음 | 일부 일치 | 일치하나 매핑 상이 | 정확히 일치 |
| **외부 API 계약 영향** | 응답 스키마 변화 여부 | 변화 없음 | 소폭 변화 | 내부만 변화 | 변화 없음 |
| **데이터 마이그레이션 비용** | 건수·복잡도 | 0건 | 중간 | 낮음 | 낮음 (Enum→VARCHAR) |
| **런타임 안정성** | 테스트 커버리지·스모크 통과 | 안정 | 부분 영향 | 안정 | 전면 영향 |

**실행안 (codex 권장 #4 — `git revert` 단일안 금지)**:
- **유지**: 조치 없음. 감사 문서에만 "검증 완료 — 마스터 없음 정당" 기록
- **선별 롤백**: 해당 Enum만 revert 커밋 작성. 다른 `#1-A` 변경은 유지. 고위험.
- **후속 패치**: Enum은 유지하되 **내부 값을 `Repository` 조회로 동적 로드** (예: `DocumentType.values()` → `maintTpRepository.findAll()` 매핑)
- **전체 롤백**: `#1-A` 커밋을 revert — 신규 테스트(22건)와 `EnumErrorResponseFactory` 포함 전부 원복. 최후 수단.

**기본 권고 실행안**: **후속 패치**. 이유:
- `#1-A`의 `EnumConversionConfig`, `EnumErrorResponseFactory`는 **범용 인프라**로 유지 가치↑
- Enum을 제거하는 대신 내부 값 공급원만 마스터로 전환하면 API 호환 + 유지보수성 둘 다 확보
- 전체 롤백은 MessageResolver(#1-B) 의존성까지 검토 필요하므로 비용 큼

실제 결정은 Phase 2 매트릭스 점수 집계 후 로드맵에 기록.

### 7-3. 스키마 재구성 허용 — ✅ 사용자 승인
- 중대한 중복 발견 시 DB 테이블 삭제·FK 재설계 허용
- 단 실행은 **후속 스프린트에서 개별 기획·승인** 거쳐서 진행 (본 감사에서 직접 실행 금지)

### 7-4. 감사 문서 형식 — 마크다운 + Obsidian Dataview 호환
- 기존 `docs/generated/audit/` 패턴 따름
- 표 기반 정량 보고 + 링크로 기초 근거 파일 연결

### 7-5. 감사 SQL 안전통제 (v2 codex 권장 #5 + 추가확인 #3 반영)

#### 7-5-1. 권한 통제
- **DB 계정 권한**: `SELECT` 전용 권한 확인. 마이그레이션용 계정이 아닌 read-only 계정 사용 권장.
- 현재 개발 DB 계정(`postgres`)은 superuser. 감사 실행 중 **UPDATE/DELETE/DDL 절대 금지** — 코드 리뷰로 확인.

#### 7-5-2. 런타임 통제
감사 SQL 러너 시작 시 다음 명령을 첫 번째로 실행:
```sql
SET TRANSACTION READ ONLY;           -- 이 세션 내 쓰기 차단
SET LOCAL statement_timeout = '30s'; -- 장기 쿼리 차단
```
- 대용량 스캔 부하 방지: 개별 쿼리 `LIMIT 1000` 기본, 필요 시 명시적으로 상향.
- 락 대기 방지: `SET LOCAL lock_timeout = '5s'`.

#### 7-5-3. SQL Allowlist
- 모든 감사 SQL은 `docs/generated/audit/data-architecture-scan.java` 내부 배열로 고정.
- 애드혹 쿼리 금지. 필요 시 파일에 추가 후 재컴파일.
- allowlist 파일은 Git 추적.

#### 7-5-4. 개인정보 보호 (codex 추가확인 #3 반영)
- 기초 마스터 인벤토리 수집 시 `users`, `ps_info` 전체 레코드 덤프 금지.
- 스키마(컬럼 목록) + 레코드 수(COUNT) + 샘플 1~3건 + **PII 컬럼은 마스킹** (예: `LEFT(email, 2) || '***'`).
- 감사 산출물 파일에는 실제 PII 값 포함 금지. 발견 시 `***` 마스킹 후 기록.
- 운영 DB 접속 결과는 외부 반출·공유 금지.

#### 7-5-5. 실행 환경
- 로컬 Java JDBC (기존 `preflight-runner.java` 패턴 재사용)
- read replica가 있으면 우선 사용. 현재 환경은 단일 DB — 운영 영향 최소화 위해 **업무시간 외 실행** 권장.

---

## 8. 영향 범위

| 계층 | 파일 | 유형 |
|------|------|------|
| 감사 문서 | `docs/generated/audit/db-schema-full.md` | 신규 |
| 감사 문서 | `docs/generated/audit/data-architecture-master-inventory.md` | 신규 |
| 감사 문서 | `docs/generated/audit/data-architecture-utilization-audit.md` | 신규 |
| 로드맵 | `docs/design-docs/data-architecture-roadmap.md` | 신규 |
| 상위 감사 | `docs/generated/audit/2026-04-18-system-audit.md` | 수정 (참조 추가) |
| 기존 기획 | `docs/product-specs/sys-type-normalization.md` | 수정 (superseded 표기) |
| SQL 러너 | `docs/generated/audit/data-architecture-scan.java` (1회용) | 신규 |

**합계**: 신규 4~5, 수정 2. **코드·DB 스키마 변경 0**. 라인 수 감사 대상 아님.

---

## 9. 리스크 평가

| ID | 리스크 | 수준 | 완화 |
|----|--------|------|------|
| R-1 | 감사 대상 11개 기능 중 누락 발생 | 중간 | Phase 1에서 전 테이블 스캔으로 기능 목록 역추적. 누락 발견 시 Phase 2 범위 확장. |
| R-2 | 기초 4개 기능 판단 오류 (마스터/감사 대상 경계) | 중간 | 사용자 승인받은 §2-1 목록 준수. Phase 1 결과로 이견 있으면 사용자 재확인. |
| R-3 | Phase 2 분석이 피상적·오분류 | 중간 | 각 기능마다 최소 1개 이상 구체 사례(파일:라인 + SQL 결과) 포함 의무. |
| R-4 | `#1-A` 롤백 권고가 운영 안정성 훼손 | 중간 | 롤백 실행은 **후속 스프린트**에서 별도 검토. 본 감사는 권고만. |
| R-5 | 감사 자체로 작업 시간 과다 소모 | 중간 | Phase별 타임박스 설정 (Ph1 1일, Ph2 3일, Ph3 0.5일 = 총 4.5일 상한). 넘어가면 범위 축소. |
| R-6 | 감사 SQL로 운영 DB 부하 | 매우 낮음 | read-only `SELECT`만. 트랜잭션 롤백 대상 아님. |

---

## 10. 산출물 요약

| # | 파일 | Phase | 목적 |
|---|------|-------|------|
| 1 | `docs/generated/audit/db-schema-full.md` | 1 | 전 테이블 컬럼 목록 |
| 2 | `docs/generated/audit/data-architecture-master-inventory.md` | 1 | 기초 4개 기능 마스터 테이블 완전한 카탈로그 |
| 3 | `docs/generated/audit/data-architecture-utilization-audit.md` | 2 | 감사 대상 11개 기능 분석 리포트 |
| 4 | `docs/design-docs/data-architecture-roadmap.md` | 3 | 후속 스프린트 우선순위 로드맵 |
| 5 | `docs/generated/audit/data-architecture-scan.java` | 1 | 감사용 SQL 러너 (1회용) |

---

## 11. 승인 요청

### 승인 전 확인 사항

- [x] 사용자 Q1 확정: 기초 4개 기능 = 사업관리 / 담당자관리 / 사용자관리 / 서버관리. `sys_mst`는 서버관리(공통 마스터).
- [x] 사용자 Q2 확정: `#1-A` Enum 롤백 권고 허용 (마스터 중복일 경우).
- [x] 사용자 Q3 확정: 스키마 재구성 허용 (단, 후속 스프린트로 분리 실행).

### 다음 절차
1. 사용자 "반영" → 기획서 v2 개정
2. 사용자 "최종승인" → **[개발팀]** 개발계획서 작성 (`docs/exec-plans/data-architecture-audit.md`). 본 스프린트는 문서 산출만이라 개발계획서는 "작업 절차 · 검증 방법" 중심으로 간결히.
3. 개발계획서 codex 검토 → 사용자 최종승인 → Phase 1~3 순차 실행.
