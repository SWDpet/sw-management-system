# [기획서] CSRF 정식화 — JSON API 면제 축소 + 전역 fetch 토큰 인터셉터 (beyond-A)

- **작성팀**: 기획팀 / **작성일**: 2026-06-29
- **트랙**: beyond-A track 3 (codex 자문 보안 순위). CSRF 면제경로를 닫아 상태변경 JSON API 도 토큰 검증.
- **상태**: ✅ 구현+QA 완료(2026-06-29). 면제 6경로 제거 + 공통 fragment `fragments/csrf.html`(전역 fetch 인터셉터) → top-nav + standalone document-preview 포함. 브라우저 QA(회사PC, 운영DB): **5개 API 프리픽스 전부 토큰없으면 403·토큰주입시 통과**, 실제 페이지(quotation/new·ops-doc/list·admin/org-units·document/batch·document-preview id3~5) 인터셉터 확인, GET 무회귀. `mvnw -o clean verify` green. codex 사전검토(미구현글리치+실질 일치)→구현. dual-review 2R: **실제 갭 1건 검출**(document-preview standalone top-nav 미포함→403 위험)→fragment 추출로 해소 + 하드닝(중복가드·robust URL·Request처리). 잔여 합의는 무효(XHR 0개)/엣지(string URL만)/컨벤션(full-doc fragment) 수용. 비-fetch·외부 API 클라이언트 0 검증(QR=브라우저 admin, agent=다운로드만, mobile QR=GET). production 영향: 전역 fetch 래핑.

---

## 1. 배경 — 현재 CSRF 상태(정밀조사 결과)
**CSRF 는 이미 대부분 활성.** SecurityConfig 2체인:
- Chain1 `/actuator/**`: STATELESS + httpBasic → `csrf.disable()`(정상, admin 전용 API).
- Chain2 메인앱: **CSRF ON**(Thymeleaf 폼에 `_csrf` 자동주입 — 로그인 폼 작동이 활성 증거) + **6개 JSON API 경로만 `ignoringRequestMatchers` 면제**: `/api/**`, `/admin/api/**`, `/document/api/**`, `/admin/api/org-units/**`, `/ops-kb/api/**`, `/ops-doc/api/**`.

**틈**: 면제경로의 상태변경 fetch(POST/PUT/DELETE ~35개)는 CSRF 미검증 → 세션쿠키 기반 인증이라 CSRF 공격 표면. (SameSite 는 방어층이나 "CSRF 해결"은 아님 — codex.)

## 2. 정밀조사 (완료)
- 면제경로 fetch 다수는 **GET(읽기)** — CSRF 무관. **상태변경 POST 25·DELETE 9·PUT 1 ≈ 35개**가 실대상.
- **비-fetch AJAX 0건**(XHR/jQuery/axios 없음) → `window.fetch` 전역 래핑으로 전수 커버.
- 위험 페이지(doc-batch/document-detail/qr-issue/quotation-form/org-unit-management/doc-fault/support…) **전부 `fragments/top-nav :: nav` 포함**(54화면). → top-nav 1곳에 인터셉터 = 전역 적용.
- 10개 템플릿은 이미 수동 토큰 전송(doc-*·qr-*·project-*) → 인터셉터는 `!headers.has()` 로 무충돌.
- 폼(method=post) 17개 + 견적폼(action=/api/quotation): Thymeleaf 자동주입 → 면제 제거 시 정상.

## 3. 범위
### D1 — 전역 fetch CSRF 인터셉터 (`fragments/top-nav.html`)
- top-nav 은 body fragment(`th:fragment="nav"`, `<head>` 아님) → **data-attribute + `<script>`** 방식(메타 in body 회피, Thymeleaf `[[` 인라인 백지함정 회피).
- 동일출처 + 상태변경(POST/PUT/DELETE/PATCH) 요청에 `X-CSRF-TOKEN: ${_csrf.token}` 자동 주입. 이미 헤더 있으면 미덮어씀. 외부URL 제외.
- 멀티파트 업로드도 헤더방식이라 MultipartFilter 순서 이슈 없음.

### D2 — SecurityConfig 면제 축소
- Chain2 `ignoringRequestMatchers` 6경로 제거(전역 인터셉터가 모든 fetch 커버하므로 일괄 제거 안전). Chain1(actuator)은 불변.

### D3 — 브라우저 QA (필수)
- 주요 상태변경 흐름 실검증: ① 문서 저장(doc-save) ② 문서 batch 생성 ③ 첨부/날인본 업로드·삭제 ④ ops-doc 지원문서 업로드·삭제+KB피드백 ⑤ QR 발급 ⑥ 견적 저장 ⑦ admin 조직단위 CRUD ⑧ 점검 reset-all. 각 200/정상 + 토큰 누락 시 403 회귀.

## 4. 요건
- **FR-1**: 면제경로 6개 제거 후 모든 상태변경 fetch/폼이 토큰 검증 통과(403 없음).
- **FR-2**: 토큰 없는 위조요청은 403(CSRF 방어 실효).
- **NFR**: 기존 폼·AJAX 무회귀(브라우저 QA), `mvnw -o clean verify` green, codex PASS + dual-review. UI 시각변화 0(메타/스크립트만 — 디자인 자문 N/A).

## 5. 영향 / 리스크
- 변경: `fragments/top-nav.html`(스크립트 추가) + `SecurityConfig.java`(6라인 제거). production JS 동작 전역 영향.
- **R1 (高) 인터셉터 버그 = 전 fetch 영향**: 토큰 미주입/오류 시 모든 상태변경 403. → 브라우저 QA 필수, 동일출처·method 가드 견고히.
- **R2 사각지대**: top-nav 미포함 페이지의 상태변경 fetch. → 조사상 위험 페이지 전부 포함 확인. login/signup 은 폼(자동주입).
- **R3 multipart**: 헤더방식이라 OK(폼필드 토큰 아님).
- **R4 actuator**: Chain1 별도(csrf.disable 유지) — 영향 없음.
- 롤백: 면제 6경로 복구(1커밋 revert)로 즉시 원복.
