---
tags: [plan, sprint, feature]
sprint: "5"
status: v2 (사용자 피드백 반영)
created: "2026-04-19"
---

# [기획서] 문서 선택 UI 통일 + 조직도 + 운영/테스트 구분 — 스프린트 5

- **작성팀**: 기획팀
- **작성일**: 2026-04-19
- **선행**: `f12e417` (보안 스프린트 4 보류 기록)
- **상태**: v2 (구현 완료 후 사용자 피드백 반영)

### 개정 이력
| 버전 | 일자 | 변경 |
|------|------|------|
| v1 | 2026-04-19 | 초안 — 연쇄 드롭다운 5단(연도→시도→시군구→시스템→사업), 저장 키 `proj_id` |
| v2 | 2026-04-19 | 사용자 피드백 반영 — **4개 문서는 3단(시도→시군구→시스템)** 으로 간소화. 연도·사업 단계 제거. 저장 키 **`infra_id`** 로 재정의. 지원 대상 라디오 가로 배치. 조직도 관리자 메뉴 링크 추가 |

---

## 1. 배경 / 목표

### 배경
문서 4종(**장애처리, 업무지원, 설치보고서, 패치내역서**) 이 현재 `infraId` 단일 드롭다운으로 지자체·시스템을 한 번에 선택하는 방식. 타 문서(착수계·점검내역서)는 이미 **연도→시도→시군구→시스템→사업** 4(5)단계 연쇄 드롭다운 UI 로 운영 중 → **일관성 부족 + 대상 선택 시 혼동**.

또한 업무지원은 **외부(지자체) / 내부(자사 조직)** 양방향 지원이 필요하나 현재는 외부만 가능. 설치·패치는 **운영/테스트** 환경 구분이 자유입력(텍스트) 으로만 돼있어 **검색·통계 불가**.

### 목표
| # | 목표 | 기준 |
|---|------|------|
| G1 | 4개 문서의 지자체 선택 UI 를 착수계·점검내역서 패턴과 **동일 구조** 로 통일 | 연도 → 시도 → 시군구 → 시스템 → 사업 연쇄 드롭다운 |
| G2 | 업무지원에 **내부/외부** 구분 도입 + 조직도 연동 | 외부: 기존 드롭다운 / 내부: 조직 연쇄 드롭다운 |
| G3 | 조직도 **CRUD 화면** 신설 | 본부·연구소 > 부서 > 팀 3단 계층. 관리자만 수정 |
| G4 | 설치보고서·패치내역서에 **운영/테스트** 구분 필드 신설 | 단일 select, 필수 입력 |

---

## 2. 기능 요건 (FR)

### 2-1. 지자체 연쇄 드롭다운 통일 (G1)

| ID | 내용 |
|----|------|
| FR-1-A | `doc-fault.html`, `doc-support.html`, `doc-install.html`, `doc-patch.html` 의 기존 단일 `infraId` select 제거. |
| FR-1-B | **v2 개정**: 4개 문서용 **3단 연쇄 드롭다운** `selCity → selDist → selSystem` 적용. 연도·사업 단계 제거. (착수계·점검내역서는 별도 5단 체계 유지 — 계약 단위 구분 필요) |
| FR-1-C | 공통 JS 모듈 2개 배포: (a) `document-infra-selector.js` — 4개 문서용 3단(인프라 기반). (b) `document-project-selector.js` — 착수계·점검내역서용 5단(사업 기반, 미래 전환용). 둘 다 현재 제공. `doc-commence.html`/`doc-inspect.html` 마이그레이션은 **후속 스프린트로 이관**. |
| FR-1-D | 4개 문서용 신규 API (DocumentController): `/api/infra-cities`, `/api/infra-districts`, `/api/infra-systems`, `/api/infra-find`. `tb_infra_master` 의 distinct 값 기반. 기존 `/api/project-*` API 는 착수계·점검내역서에서 계속 사용. |
| FR-1-E | **v2 개정**: 4개 문서의 단일 원본은 **`tb_document.infra_id`** (지자체+시스템 식별). `proj_id` 는 4개 문서에서 사용하지 않음 (null 저장). 사용자 요구는 "계약 연도 단위 구분 불필요, 지자체·시스템 식별만 충분" — 이에 따라 v1 의 proj_id 기반 정책을 폐기하고 infra_id 기반으로 재정의. |
| FR-1-F | **기존 레코드(draft/완료 모두) 편집 재진입 시 규칙**: (a) 연쇄 드롭다운을 **빈 상태로 초기화** — 자동 추정 금지. (b) 기존 저장값(`infraId` 또는 과거 `proj_id`) 기준으로 **현재 대상을 읽기 전용 배너**로 상단 표시 ("현재 대상: 양양군 UPIS — 저장 시 재선택 필요"). (c) 저장 버튼은 **사용자가 시도→시군구→시스템명으로 `infraId` 를 명시적으로 다시 선택**하기 전까지 비활성. (d) 자동 복원 금지로 데이터 손상 위험 제거. |

### 2-2. 업무지원 내부/외부 구분 (G2)

| ID | 내용 |
|----|------|
| FR-2-A | `doc-support.html` 상단에 **라디오 버튼** (가로 배치): `외부(지자체)` / `내부(자사 조직)`. 기본값 `외부`. v2: `display:flex` 로 깔끔한 한 줄 레이아웃, `input[type=radio]` 기본 크기 강제. |
| FR-2-B | **외부 선택** 시: 2-1 의 3단(시도/시군구/시스템명) 드롭다운 노출. |
| FR-2-C | **내부 선택** 시: 조직 연쇄 드롭다운 노출 — `본부/연구소 → (부서) → (팀)`. 계층은 **가변** (FR-3-A) — 하위 유닛이 없으면 해당 단계 비활성. 저장 시 **가장 말단 선택 유닛의 `org_unit_id`** 를 기록. 예: "GIS사업본부 > GIS 사업4팀" 선택 시 팀 id 저장, "스마트시티본부" 선택 시 본부 id 저장. |
| FR-2-D | 문서 저장 시 `support_target_type` 값으로 `EXTERNAL` / `INTERNAL` 저장. |
| FR-2-E | **v2 개정**: 외부 저장 시 `infra_id` 채움, `org_unit_id` = null. 내부 저장 시 `org_unit_id` 채움, `infra_id`/`proj_id` = null. (v1 의 proj_id 기반 대신 infra_id 로) |
| FR-2-F | 목록/조회 화면에서 대상 표시 로직: EXTERNAL → "지자체 + 시스템명" (infra 기반), INTERNAL → "본부 > 부서 > 팀" 경로. |

### 2-3. 조직도 CRUD (G3)

| ID | 내용 |
|----|------|
| FR-3-A | 신규 테이블 `tb_org_unit` — **단일 자기참조** (self-FK `parent_id`) 로 계층 표현. 타입 필드(`unit_type`) 는 `DIVISION`(본부·연구소·부 중 최상위) / `DEPARTMENT`(부서) / `TEAM`(팀). **깊이 가변 허용** — 1단(본부만), 2단(본부→부 또는 본부→팀), 3단(본부→부→팀) 모두 유효. 실제 회사 조직은 혼재(예: GIS사업본부 → GIS 사업4팀 은 부 레벨 생략, 스마트시티본부 는 하위 없음). 관리 화면은 임의 깊이 트리 표시. |
| FR-3-B | 관리 화면 `/admin/org-units` (관리자 전용) — 트리뷰 + 추가/수정/삭제 모달. 삭제 시 하위 유닛 존재하면 차단 + 경고. **메인 대시보드 상단 네비게이션에 "🏢 조직도 관리" 링크 노출** (관리자 계정만). |
| FR-3-C | 기본 데이터 시드: 현재 회사 조직 구조 초기값으로 삽입 (본 스프린트 시작 시 사용자에게 현재 조직도 값 확인 후 seed SQL 작성). |
| FR-3-D | 조직 트리 조회 API: `GET /api/org-units/tree` (전체 트리) / `GET /api/org-units/children/{parentId}` (부분 로드). |
| FR-3-E | `users.deptNm/teamNm` 등 기존 문자열 필드와의 관계: 본 스프린트에서는 **분리 유지** — `tb_org_unit` 는 문서 선택용 정규 데이터. `users` 매핑은 별도 스프린트에서 연결. |

### 2-4. 설치보고서·패치내역서 운영/테스트 구분 (G4)

| ID | 내용 |
|----|------|
| FR-4-A | `doc-install.html`, `doc-patch.html` 에 **환경 구분 select** 추가: `운영` / `테스트`. 필수 입력. |
| FR-4-B | 문서 저장 시 `environment` 값: `PROD` / `TEST` 저장. 기존 `patch_applied_server` 자유입력 필드(텍스트) 는 유지하되, 환경 select 와는 별개. |
| FR-4-C | 목록 화면 컬럼에 환경 표시 (운영/테스트 뱃지). 필터 UI 는 본 범위 밖(Phase 2). |

---

## 3. 비기능 요건 (NFR)

| ID | 내용 |
|----|------|
| NFR-1 | 기존 레코드 회귀 규칙 (codex 지적 반영): **조회(read)** 는 draft/완료 모두 정상 — 저장된 `proj_id`/`infra_id` 기반으로 기존 대상 표시. **편집(save)** 은 FR-1-F 에 따라 사용자가 연쇄 드롭다운으로 `proj_id` 를 재선택한 후에만 허용. 재선택 없이 저장 시도하면 400 에러 + 안내. |
| NFR-2 | Maven compile 성공, 서버 재기동 후 4개 문서 작성·저장·목록 전부 정상. |
| NFR-3 | 조직도 CRUD 는 **관리자(ADMIN) 권한만** 접근 가능 (SecurityConfig `/admin/**`). 문서 작성 시 조직 드롭다운 조회(GET)는 인증된 모든 사용자. |
| NFR-4 | 연쇄 드롭다운 응답 시간 ≤ 500ms (`/api/project-*` 기존 API 재사용, 이미 검증됨). 조직 트리 API 는 전체 조직 수 100개 내외 기준 ≤ 200ms. |
| NFR-5 | 민감 정보 노출 없음 — 조직 API 응답은 `unit_id`, `name`, `unit_type`, `parent_id` 만. 사용자 이메일·연락처 포함 금지. |

---

## 4. 의사결정 / 우려사항

### 4-1. 조직도 모델 — ✅ 단일 self-FK 테이블로 확정
- 대안: `tb_division`, `tb_department`, `tb_team` 3개 분리
- 선택: **단일 `tb_org_unit` + self-FK + type enum** — 계층 깊이 변경에 유연, 조회 단순
- 위 구조로 CRUD 화면 로직도 단순(한 엔티티·리포)

### 4-2. `infraId` 대 `projId` 저장 전환 — ⚠ 스프린트 초기 재확인 필요
- 기존 4개 문서가 `infraId` 를 `Document` 엔티티의 어떤 컬럼에 저장 중인지 (`tb_document.infra_id` 추정) 개발계획서 단계에서 코드 라인 기준 재확인
- `projId` 로 전환하면서 기존 레코드 편집 회귀 없도록 FR-1-F 처리

### 4-3. 업무지원의 "내부 + 팀 미소속/부 미소속" 케이스 — ✅ 가변 계층으로 해결
- 실제 회사 조직 기준(2026-04-19 사용자 스크린샷 확인):
  - 1단: 스마트시티본부 (하위 없음)
  - 2단: GIS사업본부 → GIS 사업4팀 (부 생략)
  - 3단: GIS사업본부 → GIS사업1부 → GIS1부 사업1팀
- `tb_document.org_unit_id` 는 팀/부서/본부 어느 레벨도 가리킬 수 있음. 사용자가 선택 가능한 가장 말단 유닛 저장.
- 조직도 seed 데이터는 개발계획서 단계에서 사용자에게 확정본 받아 작성.

### 4-4. 환경 select 에 "개발" 포함? — ✅ 본 스프린트는 2종만
- 사용자 요청: 운영/테스트 2종
- 개발(DEV) 은 필요 시 후속 스프린트에서 enum 에 추가 (스키마 호환 유지)

### 4-5. 조직도 CRUD 권한 — ✅ ADMIN 만
- 본부/부서/팀 구조 변경은 관리자 업무
- 일반 사용자는 드롭다운 선택만 가능

### 4-6. 공통 연쇄 드롭다운 JS 모듈화 — ✅ 본 스프린트 포함
- 4개 문서에서 동일 로직 복붙하지 않음. 공통 JS 파일(`/js/document-project-selector.js`) 로 분리
- `doc-commence.html`, `doc-inspect.html` 의 기존 인라인 로직도 같이 모듈화 → 중복 제거 (본 스프린트 범위)

---

## 5. 영향 범위

| 계층 | 파일 | 유형 |
|------|------|------|
| DB (신규) | `src/main/resources/db_init_phase2.sql` | `tb_org_unit` 테이블 + 초기 seed |
| DB (변경) | 동 SQL | **`tb_document` 에 컬럼 신규 추가로 확정** (codex 지적 반영): `support_target_type VARCHAR(20)`(EXTERNAL/INTERNAL, nullable — 업무지원 문서만 사용), `org_unit_id BIGINT FK`(nullable), `environment VARCHAR(20)`(PROD/TEST, nullable — 설치·패치 문서만 사용). jsonb(sectionData) 사용 안 함 — 인덱싱·검색·통계 유리. |
| Entity (신규) | `domain/OrgUnit.java` | 신규 |
| Repository (신규) | `repository/OrgUnitRepository.java` | 신규 |
| Service (신규) | `service/OrgUnitService.java` | 트리 조회·CRUD |
| Controller (신규) | `controller/OrgUnitController.java` | `/admin/org-units`, `/api/org-units/tree` |
| Controller (수정) | `DocumentController.java` | 업무지원 저장 로직에 internal/external 분기, 설치·패치 저장에 environment 반영 |
| Template (수정) | `doc-fault.html`, `doc-support.html`, `doc-install.html`, `doc-patch.html` | 지자체 드롭다운을 연쇄로 교체. 업무지원에 라디오+조직 드롭다운. 설치/패치에 환경 select |
| Template (수정) | `doc-commence.html`, `doc-inspect.html` | 공통 JS 모듈 사용으로 전환 (중복 제거) |
| Template (신규) | `admin/org-unit-management.html` | 조직도 트리 관리 화면 |
| Static (신규) | `static/js/document-infra-selector.js` | **4개 문서용 3단(시도/시군구/시스템명) 인프라 선택 모듈** (v2 핵심) |
| Static (신규) | `static/js/document-project-selector.js` | 착수계·점검내역서용 5단 사업 선택 모듈 (미래 전환 대비) |
| Static (신규) | `static/js/org-unit-selector.js` | 조직 연쇄 드롭다운 모듈 |
| Template (수정) | `main-dashboard.html` | 관리자 네비게이션에 "조직도 관리" 링크 |
| Docs (수정) | `docs/ERD.md`, `docs/erd-*.mmd` | `tb_org_unit` 추가 반영 |
| Docs (수정) | `docs/audit/dashboard.md` (해당 시) | 이번 변경을 향후 개선 TODO 와 상충 없음 확인 |

**대략 신규 7 + 수정 9 = 16 파일.** DB 스키마 변경 1 (신규 테이블). API 신규 2개.

---

## 6. 리스크 평가

| 리스크 | 수준 | 완화책 |
|--------|------|--------|
| 기존 작성된 4개 문서 편집 재진입 시 드롭다운 초기값 복원 실패 | 중간 | FR-1-F 의 best-effort 역조회 + 토스트 안내. 개발계획서 T# 에 기존 레코드 편집 시나리오 포함 |
| `tb_document` 의 `infra_id` vs `proj_id` 운영 상 혼재 | 중간 | 본 스프린트에서 4개 문서는 `proj_id` 단일 소스 사용. infra_id 는 `proj → infra` 파생 가능 |
| 조직도 초기 seed 누락 → 업무지원 내부 선택 불가 | 낮음 | 개발계획서 단계에서 사용자에게 현재 조직 구조 확인 후 seed SQL 작성 |
| 공통 JS 모듈화 과정에서 기존 착수계·점검내역서 회귀 | 중간 | 모듈 추출 후 기존 2개 문서의 전체 작성 시나리오 수동 검증(T# 포함) |
| 환경 enum 확장성 (DEV 추가 요구) | 낮음 | 문자열 enum 사용 — 스키마 변경 없이 코드만 확장 가능 |

---

## 7. 범위 제외 (명시)

- ❌ `users` 테이블과 `tb_org_unit` 매핑 (users 의 deptNm/teamNm 문자열과 연결)
- ❌ 설치/패치 목록의 환경 기반 필터·검색 UI
- ❌ 업무지원 외 타 문서(장애·설치·패치) 에도 내부/외부 구분 도입
- ❌ 기존 완료된 문서의 일괄 마이그레이션 (작성 중 Draft 만 호환)
- ❌ 조직도에 사용자 매핑 (누가 어느 팀인지) — 별도 스프린트
- ❌ `doc-commence.html`, `doc-inspect.html` 의 공통 JS 모듈 마이그레이션 — 후속 리팩터 스프린트 (FR-1-C 수정 반영)

---

## 8. 개발계획서 단계에서 재확인 필요 항목

1. `tb_document` 의 현재 실제 컬럼 구성 (`infra_id` vs `proj_id` 사용 패턴)
2. ~~업무지원 내부 대상 저장 방식 결정~~ → **확정됨**: `tb_document` 컬럼 신규 추가 (FR-2-D/E, 영향 범위 표 참조)
3. 현재 회사 조직도 실제 구조 → seed SQL 작성 (사용자에게 요청)
4. 공통 JS 모듈의 API 계약 (param 이름, 이벤트 버스)
5. 업무지원의 내부/외부 전환 시 기존 입력값 유지·초기화 UX

---

## 9. 승인 요청

본 기획서에 대한 codex 검토 및 사용자 최종승인을 요청합니다.
