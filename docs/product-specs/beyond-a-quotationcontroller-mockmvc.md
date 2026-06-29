# [기획서] QuotationController MockMvc 표면 net (beyond-A)

- **작성팀**: 기획팀 / **작성일**: 2026-06-29
- **트랙**: beyond-A (codex 컨트롤러 net 순서 2번째). SwControllerMvcTest 패턴 재사용.
- **상태**: ✅ 구현 완료(2026-06-29).

---

## 1. 배경
codex 자문 순서(SwController→QuotationController→DocumentController). [[beyond-a-swcontroller-mockmvc]] 의 standalone MockMvc 패턴 재사용. QuotationController 인증=`checkViewAuth`/`checkEditAuth` 가 `InsufficientPermissionException` throw(SwController 의 redirect 와 달리) → GlobalExceptionHandler 403.

## 2. 범위 — `QuotationControllerMvcTest` (신규) 7케이스
- /quotation/list 미인증 → 403(checkViewAuth throw).
- /quotation/list VIEW → 200·view quotation/quotation-list·model quotations/stats/canEdit.
- /quotation/{id} VIEW → 200·view quotation/quotation-detail·isAuthor 값 단언(createdBy≠로그인유저→false).
- /quotation/new EDIT → 200·view quotation/quotation-form.
- /quotation/new VIEW(편집권한 부족) → 403(checkEditAuth throw).
- /api/quotation/{id} VIEW → 200·JSON·**jsonPath $.quoteId** body 박제.
- /api/quotation/next-number VIEW → 200·JSON·**jsonPath $.nextNumber** body 박제.

## 3. 결과
- standalone MockMvc(생성자 주입, 4 deps) + GlobalExceptionHandler + SecurityContextHolder 인증(authQuotation). DB/컨텍스트 불요 → CI gates 실행.
- 정규 verify green(7테스트+coverage). codex 사전검토 APPROVE. dual-review(합의 5: API body 미검증→jsonPath 추가, isAuthor 값 단언 추가 반영). production 0. PIT 미편입.
