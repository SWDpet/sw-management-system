# [기획서] DocumentController MockMvc 표면 net (beyond-A)

- **작성팀**: 기획팀 / **작성일**: 2026-06-29
- **트랙**: beyond-A (codex 컨트롤러 net 순서 3번째·마지막). standalone MockMvc 패턴 재사용.
- **상태**: ✅ 구현 완료(2026-06-29).

---

## 1. 배경
codex 순서(SwController→QuotationController→DocumentController core) 완결. 인증=`DocumentAccessSupport.getAuth()` 위임 → **mock+stub 으로 제어**(SecurityContext 불요, 가장 깔끔). NONE→redirect:/.

## 2. 범위 — `DocumentControllerMvcTest` (신규) 7케이스 (core)
- /document/list NONE → 3xx redirect / · VIEW → 200·document/document-list·model+searchDocuments 호출 검증.
- /document/detail/{id} NONE → 3xx redirect /.
- /document/api/dist-list → JSON·jsonPath $[0] body 박제.
- /document/preview/{id} → 200·document/document-preview·docId model.
- POST /document/delete/{id} EDIT → redirect /document/list + deleteDocument 호출 / 비-EDIT → redirect(삭제 안 함).

## 3. 결과 + 패턴 보강
- standalone MockMvc(@Autowired 9 deps reflection inject) + GlobalExceptionHandler + **`PageableHandlerMethodArgumentResolver`**(⚠`/list` 의 `@PageableDefault Pageable` 이 standalone 에 resolver 없으면 500 — 추가로 해소). access.getAuth() mock 으로 인증.
- 정규 verify green(7테스트+coverage). codex 순서 따름. dual-review(searchDocuments 호출 검증 추가, 나머지 standalone 한계=의도·refute). production 0. PIT 미편입.

## 4. 컨트롤러 net 트랙 완결
SwController·QuotationController·DocumentController 3종 standalone MockMvc net 완료(codex 권장 순서). 공통 패턴: standaloneSetup + setControllerAdvice(GlobalExceptionHandler)[+ Pageable resolver 필요 시] + mock deps + 인증(SecurityContext 또는 access mock).
