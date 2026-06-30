# 기획서 — LSA 버전 프리셋 + 작성자 본인 편집 가드 (전 도메인 확대)

- 문서: `docs/product-specs/lsa-version-preset-and-owner-edit-guard.md`
- 작성: 2026-06-30
- 상태: **v0.2 (디자인 자문 + codex 검토 반영 — 사용자 최종승인 대기)**
- 워크플로: 기획서 → (UI 변경) 디자인 자문 ✅ → codex 검토 ✅ → 사용자 최종승인 → 개발계획

---

## 1. 배경 / 현황

LSA(라이선스 발급 대장, `system/lsa`)은 최근 4단계로 신설 완료(commit `347ed10`). 운영 중 사용자가 3건의 개선을 요청:

1. **버전 입력 개선** — 현재 LSA 작성/수정 폼의 버전 필드는 자유 입력 `<input>`(기본값 `"3.0"`). 사용자는 `Basic 3.0`, `Pro 3.0` 같은 **에디션+버전 프리셋을 선택**하면서도 **직접 수정**도 가능하길 원함.
2. **작성자 본인 편집 가드** — LSA 발급 건은 현재 `authLsa=EDIT`(또는 관리자)면 **누가 작성했든** 수정/삭제 가능. 본인이 작성한 게 아니면 편집 불가하도록 소유권 제한 추가.
3. **동일 정책 타 도메인 확대** — 같은 소유권 가드를 다른 기능에도 적용. **담당자 관리(ps_info)는 제외**.

### 현 코드 사실관계 (조사 완료)
- `Lsa` 엔티티에 `created_by`(작성자 로그인 ID), `issuer`(발급자 실명, 관리자는 타인 지정 가능) 별도 존재. → **소유권 판정은 `createdBy`(실제 작성자) 기준**, `issuer`(표시용 발급자)와 무관.
- `LsaDTO`가 `createdBy` 이미 노출 → 컨트롤러/템플릿 소유권 판정 가능.
- 본보기 패턴 2종이 코드에 **이미 존재**: `QuotationController.isAuthorOf()`, `OpsKbController.ownsOrAdmin()`. 본 작업은 신규 발명이 아니라 **기존 관용구 이식**.
- 후보 테이블 작성자 컬럼 **null 행 0건**(tb_ops_doc 23/0, tb_document 42/0, tb_work_plan 1/0) → 소유권 제한 적용해도 기존 데이터로 EDIT 사용자가 대량 잠기는 사고 없음.

---

## 2. 확정된 정책 결정 (2026-06-30 사용자)

| # | 결정 사항 | 확정값 |
|---|---|---|
| D1 | task1 버전 프리셋 목록 | **`Basic 3.0`, `Pro 3.0` 2종** + 직접입력/수정 허용 |
| D2 | task2/3 관리자(ROLE_ADMIN) 예외 | **관리자는 전체 편집 가능**, EDIT 권한자는 본인 작성건만 (기존 admin 우회 패턴 일관) |
| D3 | task2/3 소유권 제한 범위 | **수정·삭제 모두 제한** |
| D4 | task3 적용 도메인 | **업무지원 문서(ops-doc) · 문서/계약(document) · 업무계획(workplan)** |
| D5 | T1 신규폼 버전 기본값 | **공란(미선택)** — required 라 사용자가 datalist에서 `Basic 3.0`/`Pro 3.0` 선택 또는 직접입력 강제. 기존 `'3.0'` 하드기본값 제거(프리셋 불일치 해소, 디자인 자문 E). 수정모드는 기존값 유지. |

### task3 범위에서 제외된 도메인 (사유)
- **QR 라이선스** — `qr_license` 0행(빈 테이블), 발급자(`issued_by`) 의미라 차후 별건 검토.
- **점검보고서(inspect)** — 사용자 미선택.
- **지오누리스 라이선스** — update/delete 이미 admin 전용이라 실익 없음.
- **담당자(person/ps_info) · 파트너(partner)** — 사용자 명시 제외.
- **견적서 · ops-kb** — `isAuthorOf`/`ownsOrAdmin` 본인 체크 **이미 구현됨**(레퍼런스).

---

## 3. 작업 범위 (Scope)

### IN
**T1 — LSA 버전 프리셋 (UI)**
- `lsa-form.html` 버전 필드: `<input>` → `<input list="...">` + `<datalist>`(옵션 `Basic 3.0`, `Pro 3.0`). 선택 가능 + 자유 입력/수정 유지.
- 서버 검증: 기존 `validateRequired`의 버전 필수만 유지(프리셋 화이트리스트 강제 **안 함** — 직접입력 허용이 요구사항).

**T2 — LSA 작성자 본인 편집 가드 (보안)**
- 소유권 헬퍼 `isOwnerOf(id)` 추가: `createdBy == 현재 로그인 ID`.
- 수정 폼 진입(`GET /lsa/{id}/edit`), 저장(`POST /lsa/save` 의 update 분기), 삭제(`POST /lsa/{id}/delete`)에 `isAdmin || isOwner` 게이트 추가. 위반 시 `InsufficientPermissionException`(기존 403 핸들러).
- 목록/상세 템플릿의 `canEdit`(수정·삭제 버튼 노출)을 **행 단위 소유권**으로 변경: `admin || (EDIT && 본인작성)`. (UI에서 못 누르게 + 서버에서 재검증, 이중 방어.)
- (디자인 자문 D) **상세 페이지**에 비소유·비관리자일 때만 보이는 muted 1줄 힌트("작성자 본인 또는 관리자만 수정·삭제할 수 있습니다.", 기존 `.hint`/`--muted` 토큰 재사용). **목록**은 상세 아이콘이 항상 남아 안내 불필요(현행 유지).

**T3 — 동일 가드 3개 도메인 확대 (보안)**
- **ops-doc** (`OpsDocController`): `PUT /api/{type}/{docId}`, `DELETE /api/{docId}` 에 소유권 게이트 추가. 소유권 = `OpsDocument.createdBy == 로그인 ID`(ops-kb `ownsOrAdmin` 동형).
- **document** (`DocumentController`): 저장(수정 분기)·삭제에 소유권 게이트. 소유권 = `Document.author.id == 현재 사용자 id`(author는 User FK, NOT NULL).
- **workplan** (`WorkPlanController`): `POST /save`(수정 분기)·`POST /delete/{id}` 에 소유권 게이트. 소유권 = `WorkPlan.createdBy.id == 현재 사용자 id`(createdBy는 User FK).
- 각 도메인의 목록/상세 화면 수정·삭제 버튼 노출도 행 단위 소유권 반영(서버 가드와 일치).

### OUT (이번 범위 아님)
- 버전 프리셋의 마스터 테이블화/관리화면(상수/datalist 고정으로 충분).
- QR·inspect·geonuris·person·partner 소유권 가드.
- 기존 데이터 마이그레이션(작성자 컬럼 null 0건 → 불필요).
- 신규 컬럼/DDL(모든 작성자 컬럼 기존 존재).

---

## 4. 도메인별 소유권 판정 규약 (중요 — 컨벤션 상이)

작성자 컬럼의 의미·타입이 도메인마다 달라 **도메인별로 일관 비교**해야 함(혼용 금지).

| 도메인 | 작성자 컬럼 | 타입 | 현재 사용자 비교 대상 | 비고 |
|---|---|---|---|---|
| LSA | `created_by` | String(로그인 ID) | `cu.getUsername()`(로그인 ID) | issuer(실명)와 구분 |
| ops-doc | `created_by` | String(로그인 ID) | `cu.getUsername()` | ⚠ `author_id`(User FK)도 있으나 **둘 다 생성 시 동일 로그인ID에서 파생**(`OpsDocService.create` L74-76·L84)·update 시 불변 → **등가**. `created_by` 채택(ops-kb 동형) |
| document | `author_id` | User FK(NOT NULL) | `cu.getUser().getId()` | User.id 비교 |
| workplan | `created_by` | User FK | `cu.getUser().getId()` | User.id 비교, null 방어 |

> ⚠ 견적서는 `createdBy`가 **실명**(`getCurrentUserName()`) 기준이라 도메인마다 다름. 본 작업은 각 도메인의 **기존 작성자 기록 소스와 동일 소스로 비교**한다(신규 비교 규칙 발명 금지).

---

## 5. 비기능 요구 (NFR)

- **NFR-1 (이중 방어)**: 서버 가드가 정본. 템플릿 버튼 숨김은 UX 보조일 뿐, 서버에서 반드시 재검증(직접 POST/PUT 위조 차단).
- **NFR-2 (회귀 0)**: 관리자 동작·기존 EDIT 권한자의 **본인 작성건** 동작은 변경 없음. VIEW 권한자 영향 없음.
- **NFR-3 (레거시 안전)**: null 작성자 행은 0건이므로 추가 마이그레이션 불필요. 단 가드 구현은 null 작성자 → 비관리자 매칭 실패(=admin만 편집)로 안전 동작(ops-kb 선례와 동일).
- **NFR-4 (메시지 일관)**: 권한 위반 메시지는 기존 도메인 관용구 재사용(403 + 안내).

---

## 6. 테스트 계획 (개요)

- **T1**: 폼 렌더에 datalist 옵션 2종 존재 / 직접입력값 저장 통과(화이트리스트 강제 안 됨).
- **T2**: (a) 작성자 본인 EDIT → 수정·삭제 OK, (b) 타인 EDIT → 403, (c) 관리자 → 타인건도 OK, (d) 목록/상세 버튼: 타인건 비노출. MockMvc+Security.
- **T3**: ops-doc/document/workplan 각각 (b)(c) 핵심 케이스. 기존 테스트 회귀 없음.
- 실행: `RUN_DB_TESTS=true` + DB 좌표(컨트롤러 가드 테스트는 슬라이스/모킹 우선).

---

## 7. UI 변경 → 디자인 자문 대상

T1(버전 datalist) + 수정·삭제 버튼 노출 로직 변경은 UI 변경 → **디자인팀 가상 자문 필수**(토큰·다크모드·일관성). 본 기획서 승인 전 자문 결과 반영.

---

## 8. 산출물 / 변경 파일 (예상)

- `lsa-form.html`(버전 datalist), `lsa-list.html`·`lsa-detail.html`(버튼 소유권), `LsaController`(소유권 헬퍼+게이트), `LsaService`(영향 없음 예상).
- `OpsDocController`, `DocumentController`, `WorkPlanController`(+ 관련 목록/상세 템플릿) 소유권 게이트.
- 테스트: `LsaControllerMvcTest` 확장 + 도메인별 가드 테스트.
- DDL/마이그레이션: **없음**.

---

## 9. 미해결/확인 필요
- (없음 — D1~D5로 핵심 결정 확정). 개발계획 단계에서 도메인별 정확한 라인·헬퍼 시그니처 확정.

---

## 10. 검토 결과 (2026-06-30)

### 🎨 디자인 자문 — 차단 사항 없음
| 관점 | 판정 | 반영 |
|---|---|---|
| A. 버전 UI 패턴 | ✅ `<input list>+<datalist>` (userNm 선례 일치) | §3 T1 그대로 |
| B. 토큰 준수 | ✅ 신규 색/마크업 없음 | — |
| C. 다크모드 | ✅ 비대상·네이티브 한계는 기존 동률 | — |
| D. 비노출 UX | ⚠ 상세에 muted 1줄 힌트 | §3 T2 반영(D5 위) |
| E. 기타 | ⚠ 기본값 `'3.0'` 프리셋 불일치·중복 id 비확산 | §2 D5 반영 |

### 🤖 codex 검토 — 종합 "반려"는 **기계적 false-FAIL**(구현 diff 부재 사유, 메모리 기록 패턴). 실질 지적 1건만 유효:
- **(유효·해소)** ops-doc 작성자 필드 이원화(`author_id` User FK + `created_by` String) → 어느 쪽이 권위인지 확정 요구. **검증 결과 둘 다 생성 시 동일 로그인ID 파생·등가**, `created_by` 채택으로 §4 명시 보강.
- 나머지(T1·T2·T3 "미구현", NFR-2·테스트 "미충족")는 전부 **"아직 구현 안 됨"** = 기획 단계 정상. 본 기획서가 모두 구현 대상으로 명시하고 있으므로 설계 결함 아님.
- **결론**: 설계상 반려 사유 없음. 사용자 승인 시 개발계획 단계로 진행.
