# [기획서] 문서 read API 권한 가드 보강 (S4 split 후속 하드닝 #1 · 보안)

- **작성팀**: 기획팀
- **작성일**: 2026-06-27
- **트랙**: S4 거대클래스 분리 중 dual-review 가 반복 지적한 **무가드 read API**(누적 백로그). [[feedback_all_pages_require_permission]]·[[project_swmanager_viewer_download_guard]] 연장.
- **상태**: ✅ **완료(2026-06-27)**. read API 4종(getUserInfo/getProjectInfo/getAttachments/getPlanData)에 `DocumentAccessSupport.hasDocRead()`(VIEW∥EDIT allowlist, admin→EDIT 통과, NONE/미상 fail-closed) 가드. none_forbidden 4 + admin_ok 4 + 기존 read 테스트 loginView 보강. dual-review 2회: 1차 합의15(admin오탐8=getAuth admin→EDIT, fail-open=allowlist로 해소, 중복/매직=헬퍼중앙화), 2차 합의2(getUserInfo_admin_ok 추가). 듀얼푸시.

---

## 1. 배경 / 목표

S4 분리로 드러난 사실: 문서 모듈의 일부 **조회(GET) API 4종이 권한 가드 없음**. SecurityConfig 는 `/document/**` 를 `authenticated()` 로만 막아(익명 차단), **authDocument=NONE 인 인증 사용자**(문서 권한 없는 직원)가 다음을 읽을 수 있다:

| 엔드포인트 | 컨트롤러 | 노출 데이터 |
|---|---|---|
| `GET /document/api/project/{projId}` | DocumentLookupController.getProjectInfo | 사업 전체 + **담당자 PII(이름·전화·이메일)** |
| `GET /document/api/user/{userSeq}` | DocumentLookupController.getUserInfo | 사용자 기본정보(P1-3 으로 민감필드는 제거됨) |
| `GET /document/api/attachments/{docId}` | DocumentFileController.getAttachments | 문서 첨부 메타(파일명/크기) |
| `GET /document/api/plan/{projId}` | DocumentPlanController.getPlanData | 사업수행계획서(목적/범위/인력/일정) |

문서 목록/상세(documentList/documentDetail)는 authDocument=NONE 을 redirect 로 차단하는데, 위 read API 는 그 가드를 우회한다. [[feedback_all_pages_require_permission]]("모든 페이지/API 는 권한 있어야 사용") 위반.

**목표**: 위 4 read API 에 **문서 조회 권한(VIEW 이상, admin 포함) 가드** 추가 — authDocument=NONE 은 403. 익명은 이미 차단(SecurityConfig). **최소 침습**(권한 있는 VIEW/EDIT/admin 사용자 동작 불변).

---

## 2. 변경 설계

### 2-A 가드 기준
- `DocumentAccessSupport.getAuth()`(admin→"EDIT", 미인증→"NONE", else authDocument) 재사용.
- 가드: **`if ("NONE".equals(access.getAuth())) return 403;`** → VIEW·EDIT·admin 통과, NONE 차단.
- 근거: 문서 모듈 read 접근선은 authDocument!=NONE(documentList/detail 와 동일). 다운로드(=EDIT, viewer-guard)와 달리 **조회는 VIEW 이상**이 정책 일관.

### 2-B 적용 (4 엔드포인트)
- DocumentLookupController.getUserInfo: 진입부 NONE→403.
- DocumentLookupController.getProjectInfo: 진입부 NONE→403.
- DocumentFileController.getAttachments: 진입부 NONE→403(ResponseEntity<List> → `status(403).build()`).
- DocumentPlanController.getPlanData: 진입부 NONE→403.
- 응답: 본문 없는 `ResponseEntity.status(403).build()`(프런트는 상태코드로 처리). getUserInfoSecure 의 {error:{...}} 형태는 EDIT 전용 민감 API 의 기존 계약이라 본 4종엔 미적용(단순 403).

> ⚠**비목표(이번 제외)**: getProjectInfo 담당자 PII 를 EDIT 전용으로 추가 축소(P1-3 getUserInfoSecure 식)하는 것은 VIEW 사용자 문서작업 UX 영향 가능 → 별도 결정. 본 스프린트는 **NONE 차단**까지만(미인가 접근 제거가 핵심).

---

## 3. 기능 요건 (FR)

| ID | 내용 |
|----|------|
| FR-1 | 4 read API 모두 authDocument=NONE(문서 권한 없음) 호출 시 403, 서비스/레포 미호출. |
| FR-2 | VIEW·EDIT·admin 사용자는 기존과 동일하게 200 + 데이터(동작 불변). |
| FR-3 | 응답 형태: 403 = body 없음(status only). 성공 응답은 기존 그대로. |
| FR-4 | 다른 엔드포인트·UI·DB 변경 0. |

---

## 4. 비기능 요건 (NFR)

| ID | 내용 |
|----|------|
| NFR-1 | compile + `./mvnw -o clean verify` green. |
| NFR-2 | 각 엔드포인트 *_none_forbidden(403, 서비스 미호출 verify) 신규 + **해당 4 엔드포인트를 호출하는 기존 테스트 전체에 loginView() 추가**(codex: *_found/returnsRows/personMapping **및 *_notFound** 도 현재 무로그인→가드 후 403 → 의미 보존 위해 loginView 필요). getUserInfoSecure 테스트는 이미 로그인(불변). |
| NFR-3 | 커버리지 비감소·floor 유지. ratchet 불변(서비스/레포 접근 변화 없음, 가드만 추가). |
| NFR-4 | 구현 후 dual-review → 듀얼푸시. |

---

## 5. 의사결정

- **5-1 VIEW 이상 가드(EDIT 아님)** ✅: 조회는 VIEW 권한 정책(문서목록/상세와 동일). 다운로드(EDIT)와 구분. NONE 만 차단.
- **5-2 PII EDIT 축소는 제외** ✅: getProjectInfo 담당자 연락처를 VIEW 에게서 추가로 가리는 건 문서작업 UX 변경 → 별도 스프린트/사용자 결정. 본 건은 미인가(NONE) 차단에 집중.
- **5-3 403 body 없음** ✅: read API 는 상태코드로 충분. 기존 getUserInfoSecure 의 코드형 error 본문은 그 API 고유 계약으로 유지(본 4종 미변경 형태).

---

## 6. 영향 범위 / 리스크

| 계층 | 파일 | 유형 |
|------|------|------|
| Controller | DocumentLookupController·DocumentFileController·DocumentPlanController | 가드 추가(각 1~2줄) |
| Test | 위 3 컨트롤러 Test | none_forbidden 신규 + 기존 read 테스트 loginView 보강 |

UI/DB 변경 0. **동작 변경 = NONE 사용자 403(의도된 보안 강화)**.

| 리스크 | 수준 | 완화 |
|---|---|---|
| VIEW 사용자 오차단 | 낮음 | getAuth VIEW→통과(테스트로 200 확인). NONE 만 403 |
| 프런트가 NONE 으로 호출 후 200 가정 | 낮음 | NONE 은 애초 문서 페이지 접근 불가(documentList redirect). 정상 사용자 영향 없음 |
| 기존 무로그인 테스트 회귀 | 중 | NFR-2 로 기존 read 테스트에 loginView 추가 |

---

## 6-b 후속 백로그 (codex)

- DocumentLookupController 의 다른 `/document/api/*` cascade 조회(project-years/cities/systems·region·infra 등)도 `authenticated()` 만 — 드롭다운 옵션이라 민감도 낮으나 일관성 차원 별도 점검 대상.
- getProjectInfo 담당자 PII(personTel/personEmail)를 VIEW 에게 계속 노출 — EDIT 전용 축소(P1-3 식)는 UX 영향 가능, 별도 보안/UX 결정 백로그.

## 7. 승인 요청

본 기획서(read API 권한 가드 보강)에 대한 codex 검토 및 사용자 최종승인을 요청합니다.
