# [기획서] SwController MockMvc 표면 net (beyond-A)

- **작성팀**: 기획팀 / **작성일**: 2026-06-29
- **트랙**: beyond-A (codex 1순위). 컨트롤러 HTTP 표면(라우팅·status·view·JSON shape)을 MockMvc 로 박제하는 회귀 net.
- **상태**: ✅ 구현 완료(2026-06-29). `SwControllerMvcTest`(standalone MockMvc + GlobalExceptionHandler) 8케이스: status 미인증3xx·VIEW 200/project-list/model/param바인딩·page음수 바인딩·detail/{id}·new EDIT·save POST 3xx·api JSON content-type·NONE 403(핸들러). 정규 verify green(8테스트, DB불요라 CI gates 실행). codex 사전검토 APPROVE(8케이스 제시 — `.setControllerAdvice()`·SecurityContextHolder·accept 뉘앙스 반영). dual-review 분쟁9 전부 refute(standalone 한계=의도·page 클램프 실증·primitive stub 통과). production 0. PIT 미편입(컨트롤러 부적합).

---

## 1. 배경 / 목표
codex 자문: *"컨트롤러는 PIT 부적합 → MockMvc 로 URL/status/redirect/view·model/JSON shape 가 깨지지 못하게 하는 net. PIT 점수가 아니라 verify/JaCoCo 가치."* 기존 `SwControllerTest` 는 **컨트롤러 메서드 직접 호출**(view명·model·redirect 로직 커버)이라 **HTTP 배선**(실제 경로 매핑·status·@RequestParam 바인딩·@ResponseBody JSON)은 미검증.

**도구 선택**: **standalone MockMvc**(`MockMvcBuilders.standaloneSetup`) — Spring 컨텍스트·DB 불요(@WebMvcTest 보다 가벼움, CI 친화·RUN_DB_TESTS 무관). mock deps 주입 + SecurityContextHolder 로 인증 제어(기존 test 패턴 재사용).

## 2. 범위 — `SwControllerMvcTest` (신규)
standalone MockMvc 로 SwController(`/projects`) HTTP 표면 박제:
- **D1 라우팅+리다이렉트**: GET `/projects/status` 미인증 → 3xx redirect `/login`.
- **D2 라우팅+view+status+model+param바인딩**: GET `/projects/status` 인증(VIEW) → 200 · view `project-list` · model `paging` 존재 · page 기본값 0.
- **D3 상세**: GET `/projects/detail/{id}` 인증 → 200 · view `project-detail`(swService stub).
- **D4 폼**: GET `/projects/new` 인증 → 200 · view `project-form`.
- **D5 JSON API**: GET `/projects/api/districts?sido=` → 200 · @ResponseBody JSON(content-type).
- 가능 시 GET `/projects/api/dist-options` JSON.

권한-NONE 예외 경로는 기존 직접테스트가 커버 → MockMvc 는 배선 위주(중복 회피).

## 3. 요건
- **FR-1**: 위 엔드포인트의 경로매핑·status·view·redirect·JSON content-type 박제. 회귀 시 fail.
- **NFR**: DB/Spring 컨텍스트 불요(standalone) → CI gates(verify)에서 실행, RUN_DB_TESTS 무관. `mvnw -o clean verify` green. JaCoCo 컨트롤러 커버 보강(미커버 라우트 한정). codex + dual-review. PIT 미편입(컨트롤러 PIT 부적합 — codex 합의).

## 4. 영향 / 리스크
- 변경: 신규 `SwControllerMvcTest`. production 0.
- **R1 standalone 한계**: Spring Security 필터·@ControllerAdvice 미적용(기본). 컨트롤러 in-method 인증가드(getCurrentUser→redirect)는 SecurityContextHolder 세팅으로 검증 가능. 예외핸들러 필요 시 `.setControllerAdvice()` 등록.
- **R2 view 렌더 없음**: standalone 은 view name 만(렌더 안 함) — 충분(템플릿 렌더는 별건). status·view명·redirectedUrl·JSON 박제가 목적.
- **R3 mock 정합**: 기존 SwControllerTest 의 mock+login 패턴 재사용.
