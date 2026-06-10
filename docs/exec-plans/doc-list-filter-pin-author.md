# 개발계획서 — 사업문서 목록 검색필터 핀 + 작성자 검색 (doc-list-filter-pin-author)

- **상태**: DRAFT v1 (codex 검토 대기)
- **기획서**: `docs/product-specs/doc-list-filter-pin-author.md` (v0.2, 승인 2026-06-10)
- **작성일**: 2026-06-10

---

## 0. 구현 원칙
- 핀 = **순수 프론트(localStorage)**, project-list.html 패턴 이식. 단 project-list 는 select 만 핀하므로 **text input(작성자·키워드)도 처리**하도록 보강.
- 작성자 검색 = 기존 native query 에 `users` LEFT JOIN + `ILIKE` 1조건 추가(스키마 무변경).
- 기존 동작(행클릭 상세이동·날인본 컬럼·엑셀·기존 필터) 무영향.

## 1. 구현 묶음

### A. 백엔드 — 작성자 검색
1. **`DocumentRepository.searchDocuments`**(33-72): query·countQuery 양쪽에
   - `LEFT JOIN users u ON d.author_id = u.user_id` 추가
   - 조건 추가: `AND (CAST(:authorName AS VARCHAR) IS NULL OR u.username ILIKE '%' || :authorName || '%')`
   - 메서드 시그니처 `@Param("authorName") String authorName` 추가. 반환형 `Page<Document>` 유지(SELECT d.* 그대로).
2. **`DocumentService.searchDocuments`**(60-73): `authorName` 파라미터 추가 → repository 전달. **빈값 정규화**: `authorName` 이 null/`trim().isEmpty()` 면 null 로(기존 docType/keyword 등과 동일 패턴, codex [부분]2 — 불필요 join 평가·`ILIKE '%%'` 방지). 오버로드보다 시그니처 확장 + 호출처 갱신.
3. **`DocumentController`**:
   - `documentList`(109-144): `@RequestParam(required=false) String authorName` 추가 → service 전달, `model.addAttribute("authorName", authorName)`.
   - `/document/excel-list`(160): 동일하게 `authorName` 받아 service 전달(엑셀도 동일 필터, FR-7).
   - `authorId=null` 하드코딩 유지(관리자 전체조회). authorName 은 별개 이름필터.

### B. 프론트 — 핀(스티키) + 작성자 필드 (`document-list.html`)
4. **필터바 구조 변경**: 각 필터(문서유형·시도·시군구·작성자·검색어)를 `.filter-cell[data-field=...]` 로 감싸고 옆에 `.pin-btn[data-target=...]`(📌) 배치. **작성자 input** 신규(`name="authorName"`, text). **초기화 버튼** 추가(`__resetFilters`).
5. **핀 JS 이식**(project-list 231-345 패턴):
   - 키: `swDocSearchPins` / `swDocSearchValues` (충돌 방지).
   - `FIELDS = ['docType','cityNm','distNm','authorName','keyword']`.
   - **select/text 분기**: `el.tagName === 'SELECT'` 면 옵션 존재 확인 후 set, **그 외(text input: authorName·keyword) 는 바로 `.value` set** (codex [부분]3).
   - 핀 토글(active/pinned 클래스 + vals 저장), 값 변경 시 vals 갱신, 초기화 시 핀 안 된 필드만 비움.
6. **페이지네이션**(205 th:href): `authorName=${authorName}` 추가(FR-3).
7. **엑셀 JS**(`downloadDocListExcel`): 기존 FormData 방식이라 authorName input 자동 포함 — 확인만(서버 B-3 가 받으면 동작).
8. **시군구 AJAX**(onCityChange): `document.createElement('option')` 방식으로 정리(문자열결합 제거, XSS/깨짐 방지) + **fetch 실패 시 기존 옵션 보존**(project-list NFR-8 스타일: prevHtml 백업·복원, codex [충족]5).

### C. CSS / 디자인 (토큰)
9. `document-list.html` `<style>` 에 핀 스타일 추가(인라인 하드코딩 금지, 토큰만):
   - `.pin-btn { border:1px solid var(--border); ... }` / `.pin-btn.active { background:var(--primary-lt); color:var(--primary); }`
   - `.filter-cell.pinned { ... }`(살짝 강조). 다크모드 자동(토큰).
   - 접근성: `.pin-btn` `aria-pressed` 토글 + `aria-label`("OO 필터 고정").
   - project-list 와 시각 일관성. 날인본 컬럼·type 배지와 색 충돌 없음 확인.

### D. 테스트
10. **백엔드 단위/통합**: searchDocuments authorName 필터(일치/부분일치/대소문자 ILIKE/null=전체), users 조인이 기존 결과 수 불변(필터 미적용 시).
11. **수동(회사 PC)**: 핀 토글·초기화 시 핀 유지·페이지이동 시 조건유지·엑셀에 작성자 반영·다크모드·키보드 접근성.

## 2. 검증 게이트
| 게이트 | 비고 |
|---|---|
| 컴파일 `mvnw -q -DskipTests compile` | 시그니처 변경 호출처 정합 |
| 단위테스트(author 필터) | ILIKE/부분일치/null |
| 핀 JS 회귀 | localStorage 토글·초기화·복원 |
| UI 육안(디자인) | 토큰·다크모드·반응형·aria |

## 3. 리스크 / 롤백
- 시그니처 변경 → 호출처 누락 시 컴파일 에러로 즉시 발견(안전).
- 핀=localStorage 라 서버/DB 영향 0. 롤백 = 프론트 되돌림.
- authorName N+1: 목록 표시가 이미 author 읽음 → 신규 리스크 작음. 규모 커지면 fetch join 검토(이번 범위 외).

## 4. 진행 순서 (codex 권고 우선순위)
A(백엔드 authorName 체인: Repository→Service→Controller) → B(필터 UI + 페이지링크 + 핀 JS select/text 분기) → 8(시군구 createElement+실패보존) → C(CSS 토큰) → 컴파일 → D(테스트) → codex 구현검토 → 회사 PC 육안.

## 5. codex 1차 검토 (2026-06-10)
- **판정**: 수정필요 → 방향 충족. [미흡]/[부분] 다수는 "아직 미구현"(계획 단계 정상).
- **테이블/컬럼 검증**: `users.user_id`, `users.username`, `tb_document.author_id` 확인.
- **반영**: authorName 빈값→null 정규화(A-2), 핀 JS `tagName==='SELECT'` 분기(B-5), 시군구 AJAX 실패 시 옵션 보존(8).
- **확인**: 디자인 토큰(`--primary/--primary-lt/--border/--border2`, `[data-theme=dark]`) 존재 → 토큰 스타일 가능. 호출처 시그니처 변경은 컴파일 에러로 즉시 발견.
