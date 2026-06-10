# 기획서 — 사업문서 목록 검색필터 핀(고정) + 작성자 검색 (doc-list-filter-pin-author)

- **상태**: v0.2 (codex 1차 반영 — 사용자 최종승인 대기)
- **작성일**: 2026-06-10 (v0.1) / v0.2 동일자 codex 반영
- **스프린트명**: `doc-list-filter-pin-author`
- **요청자**: 박욱진 (사용자) — "검색한 항목 고정 + 작성자도 검색, 둘 다 사업현황 핀 선례 방식으로. UI 변경이면 디자인팀과 상의"

---

## 0. 한 줄 요약

사업현황(project-list)에 있는 **검색 필터 핀(스티키)** 패턴을 **사업문서 목록**에 이식하고, 필터바에 **작성자 필터**를 추가한다(작성자도 검색·핀 대상).

## 1. 배경

- 사업문서 목록(`/document/list`)은 문서유형·시도·시군구·키워드 필터만 있고, 검색 후 초기화·페이지이동 시 조건이 풀린다.
- 사업현황에는 이미 **핀 기능**(필터값을 고정해 초기화/페이지이동에도 유지)이 있음(`project-list.html:231-345`, localStorage `swProjSearchPins`). 사용자가 사업문서에도 동일 패턴을 원함.
- 작성자(author)는 표시만 되고 **검색 불가**. 현재 keyword 는 `title`·`doc_no` 만 검색(`DocumentRepository:43-46`).

## 2. 범위 (In/Out)

**In**
- 사업문서 목록 필터바 각 필드에 **핀 토글**(📌) — 핀된 필드값은 초기화/페이지이동에도 유지 (project-list 패턴 이식).
- 필터바에 **작성자 필터 필드** 추가 — 작성자명으로 검색, 이 필터도 핀 대상.
- 핀 상태 저장: **localStorage**(project-list 와 동일, 브라우저 단위. 사용자별 DB 저장은 범위 외).

**Out**
- 특정 문서 행 상단 고정(즐겨찾기) — 이번 아님(핀=필터 스티키로 확정).
- 사용자별 서버 저장(신규 테이블) — 범위 외.
- 작성자 외 추가 검색필드.

## 3. 기능 요구사항 (FR)

| ID | 요구사항 |
|---|---|
| FR-1 | 필터바 각 필드(문서유형·시도·시군구·작성자·검색어)에 **핀 버튼** 노출. 토글 시 localStorage `swDocSearchPins` 에 `{field:bool}` 저장. |
| FR-2 | **초기화** 시 핀 켜진 필드값만 유지, 나머지 비움 (project-list `__resetFilters` 동일). |
| FR-3 | **페이지네이션 링크**에 현재 필터(핀 포함 모든 활성 조건) 유지 (기존 동작 보존 + 작성자 파라미터 추가). |
| FR-4 | **작성자 필터**: 필터바에 작성자명 입력. 입력 시 `users.username LIKE %값%` 로 필터. 비면 전체. |
| FR-5 | 핀 켜진 필드는 시각적 강조(project-list `.pinned` 스타일을 **토큰화**해 적용). |
| FR-6 | 핀/필터 상태는 **새 UI 추가만**, 기존 검색·엑셀다운로드·행클릭(상세이동)·날인본 컬럼 동작에 영향 없음. |
| FR-7 (codex [필수]) | **엑셀 다운로드도 작성자 필터 반영**: `/document/excel-list` 컨트롤러에 `authorName` 파라미터 추가 → 목록 검색과 동일 조건으로 내보내기. (현재 누락 시 엑셀이 작성자 필터 무시) |

## 4. 설계

### 4-1. 핀(스티키) — 프론트 (localStorage)
- project-list.html:231-345 의 JS 패턴 이식. 키만 `swDocSearchPins` / `swDocSearchValues` 로 분리(충돌 방지).
- 동작: 페이지 로드 시 핀된 필드값 복원 → 검색 폼에 주입. 초기화 버튼은 핀 안 된 필드만 비움.
- 서버 무변경(핀은 순수 클라이언트). 단 작성자 파라미터가 페이지네이션·검색에 실려야 함(FR-3).

### 4-2. 작성자 검색 — 백엔드
- `DocumentRepository.searchDocuments`(native, line 33-72): `LEFT JOIN users u ON d.author_id = u.user_id` 추가 + 조건 `(:authorName IS NULL OR u.username ILIKE '%'||:authorName||'%')`. **`ILIKE`(대소문자 무시)** — PostgreSQL `LIKE` 는 대소문자 구분이라 이름 부분검색엔 ILIKE 적합(codex [권고]).
- `DocumentService.searchDocuments` 시그니처에 `authorName`(String) 추가(또는 오버로드). 호출처(목록·엑셀) 동시 갱신.
- `DocumentController.documentList`(109-144) **및 `/document/excel-list`(160)**: `@RequestParam(required=false) authorName` 추가 → service 전달, model 에 되돌려 폼·페이지네이션·엑셀 유지. (기존 `authorId=null` 하드코딩은 유지 — 관리자 전체조회, 작성자명 필터는 별개.)
- **페이지네이션 링크**(`document-list.html:205` th:href)에 `authorName=${authorName}` 추가(FR-3).

### 4-3. 작성자 필드 형태
- **권고: 텍스트 입력**(이름 부분일치) — keyword 와 동일 단순성, 작성자 수 많아도 무난.
- 대안: distinct 작성자 드롭다운(정밀하나 인원 많으면 길어짐). → 디자인팀 자문(§6).

## 5. DB팀 가상 자문
- **스키마 변경 없음**(핀=localStorage, 작성자=기존 users 조인). 신규 테이블·컬럼 불필요.
- native query 에 `users` LEFT JOIN 1개 추가 — `users(user_id)` PK 인덱스 사용, 성능 영향 미미.
- 주의: author_id NULL 인 문서(없어야 하나)도 LEFT JOIN 으로 안전. username 부분일치(LIKE %%)는 인덱스 미사용이나 목록 규모(수백)에서 무문제.

## 6. 디자인팀 가상 자문 (A+D 정책 — UI 변경)
- **필터바 공간**: 현재 문서유형·시도·시군구·검색 + 엑셀. **작성자 칸 추가로 빽빽**해짐. → `flex-wrap` 확인(이미 있음, `document-list.html:22`)으로 줄바꿈 허용. 라벨 짧게("작성자").
- **핀 버튼**: 각 필드 옆 작은 📌. project-list 인라인 CSS(`#0F766E`·`#F0FDFA`·`#E7E5E4` 하드코딩)를 **토큰으로 이식** — `--primary`(active 글자), `--primary-lt`(active 배경), `--border`(기본 테두리). 다크모드 자동 대응.
- **active 상태 일관성**: 핀 켜짐 = 필드 배경 살짝 강조(`--primary-lt`) + 📌 색 `--primary`. 문서유형 배지(type-*)·날인본 컬럼과 색 충돌 없게.
- **접근성**: 핀 버튼 `aria-pressed` + `aria-label`("작성자 필터 고정"), 키보드 포커스, 토글 상태 시각+보조 표시.
- **반응형**: 좁은 화면에서 필터 줄바꿈 시 핀 버튼이 필드와 붙어 보이도록.
- project-list 와 **시각 일관성** 유지(같은 핀 UX) — 사용자 학습비용 0.

## 7. 비기능/보안
- 핀=localStorage(민감정보 아님). 작성자명은 username(비민감, 목록에 이미 표시 중).
- 작성자 LIKE 파라미터는 JPA 바인딩(native query 파라미터) — SQL 인젝션 안전.
- 기존 권한 게이트(getAuth) 무변경.

## 8. 결정사항 (확정/Open)
1. 핀 의미 = **검색 필터 고정(스티키)** (사용자 확정).
2. 작성자 = **필터 필드 + 핀 대상** (사용자 확정).
3. 저장 = **localStorage**(사용자별 DB 저장 아님) (사용자 확정).
4. (Open, 디자인) 작성자 필드 = 텍스트 입력 vs 드롭다운 → 권고 텍스트 입력.

## 8-1. codex 1차 검토 (2026-06-10)
- **판정**: ⚠ 수정필요 → 방향 승인 가능. [필수] 다수는 "아직 미구현"(기획서 단계 정상, 개발계획·구현에서 충족).
- **반영(스펙)**: FR-7(엑셀 다운로드 authorName) 신설, §4-2 `ILIKE`(대소문자 무시) + 엑셀/페이지네이션 파라미터 명시.
- **개발계획 이월(권고)**: 시군구 AJAX `<option>` 을 `document.createElement` 방식으로(project-list 일관), authorName N+1 목록규모 커지면 확인.
- **확인**: `DocumentDTO.authorName` 이미 존재 → DTO 변경 불필요. 핀=localStorage 라 스키마 무변경.

## 9. 다음 단계
codex 검토 → 사용자 최종승인 → 개발계획서 → codex → 구현(백엔드 쿼리/컨트롤러 + 프론트 핀 JS + 필터바 UI) → 검증.
