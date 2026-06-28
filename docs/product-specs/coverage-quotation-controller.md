# [기획서] QuotationController CRUD 예외경로 커버리지 보강 (B)

- **작성팀**: 기획팀 / **작성일**: 2026-06-28
- **트랙**: beyond-A 커버리지 증분 B. 견적 컨트롤러. 기존 `QuotationControllerTest` 확장형.
- **상태**: ✅ 구현 완료(2026-06-28). QuotationControllerTest +13(53→66) → QuotationController **90.3→99.3% LINE**(297/299). 전역 LINE 78.34→78.57%·INSTR 63.98→64.13%, floor 유지. `mvnw -o clean verify` 1446 green. codex 기획/개발계획 APPROVE-WITH-FIX·구현 PASS(error 단언으로 위장통과 없음). dual-review(codex0/Opus5) 합의1 미반영(ApiResult는 비-제네릭 record→ApiResult<?> 컴파일에러)·분쟁4 미반영(공유 BOOM·safe()=메시지·copyPatterns 변환=전건 codex refute)·Map instanceof 대칭만 반영. 잔여 2줄=roundDown unit>1·getCurrentUsername 폴백 의도적 제외.
- **codex 보완**: C7 copyPatterns 는 컨트롤러가 `ids.stream().map(Number::longValue)` 로 List<Long> 변환 후 서비스 호출 → 스텁은 `copyPatterns(anyList(), eq("A"))`(`eq(List.of(1))` 아님). ApiResult 분기는 `e.getMessage()` 직접 → RuntimeException("boom") 유지.

---

## 1. 배경 / 목표

`QuotationController`(676줄, **LINE 90.3% / miss 29**)는 세션10(`9ab5788`)이 라우팅·CRUD happy-path·가드·미리보기를 덮었으나, **CRUD 엔드포인트의 `catch(Exception)→badRequest` 예외경로 13종**이 미커버다. 서비스 mock 을 throw 로 스텁해 박제한다(에러 핸들링 회귀 방어).

JaCoCo 미커버 라인(`QuotationController.java.html` nc): 44, 98, 399-401, 418-420, 442, 456-457, 470-471, 486-487, 506-508, 558-559, 575-576, 589-590, 623-624, 639-640, 653-654.

## 2. 대상 분기 (13 catch + 잔여 2)

각 엔드포인트 `try{ service.xxx(); log } catch(Exception e){ badRequest + error }`. 에러 본문 2형태:
- **Map**: `Map.of("success", false, "error", ExceptionMessages.safe(e))` — `safe(e)`=`e.getMessage()`(non-null) → "boom".
- **ApiResult**: `ApiResult.failMessage(e.getMessage())`.

| # | 엔드포인트(라인) | 가드 | 형태 | throw 대상 |
|---|---|---|---|---|
| C1 | updateQuotation(399-401) | admin/author | Map | updateQuotation |
| C2 | deleteQuotation(418-420) | admin | Map | deleteQuotation |
| C3 | savePattern(442) | EDIT | Map | savePattern |
| C4 | updatePattern(456-457) | EDIT | ApiResult | savePattern |
| C5 | deletePattern(470-471) | EDIT | ApiResult | deletePattern |
| C6 | deleteAllPatterns(486-487) | admin | Map | deleteAllPatterns |
| C7 | copyPatterns(506-508) | EDIT | Map | copyPatterns(유효 request) |
| C8 | saveRemarksPattern(558-559) | EDIT | Map | saveRemarksPattern |
| C9 | updateRemarksPattern(575-576) | EDIT | ApiResult | saveRemarksPattern |
| C10 | deleteRemarksPattern(589-590) | EDIT | ApiResult | deleteRemarksPattern |
| C11 | saveWageRate(623-624) | EDIT | Map | saveWageRate |
| C12 | updateWageRate(639-640) | EDIT | Map | saveWageRate |
| C13 | deleteWageRate(653-654) | EDIT | Map | deleteWageRate |

**잔여(저가치 minor)**: line 44 `roundDown` unit>1 산술 1줄·line 98 `getCurrentUsername` 폴백 — 본 스프린트 제외(정직성 명시). 필요 시 후속.

## 3. 변경 — `QuotationControllerTest` 케이스 추가 (테스트만)
- 기존 4-mock 생성자주입(`new QuotationController(...)`)·login 헬퍼(loginEdit/Admin/View/None)·model() 재사용. 신규 13 테스트.
- 각: 적절 login → `when(quotationService.<m>(...)).thenThrow(new RuntimeException("boom"))`(void 는 `doThrow`) → 엔드포인트 호출 → status 400(badRequest) + 에러값 단언.
  - **Map 형태**: `(Map)res.getBody()` `get("success")`=false·`get("error")`="boom".
  - **ApiResult 형태**: `(ApiResult)res.getBody()` `success()`=false·`error()`="boom".
- C7 copyPatterns: request=`Map.of("patternIds", List.of(1), "targetCategory", "A")`(검증 통과)+ copyPatterns throw.
- @RequestBody 객체는 non-null 생성(`new ProductPattern()`/`new RemarksPattern()`/`new WageRate()`/`QuotationDTO.builder().build()`). setPatternId/setQuoteId 등 try 내 호출은 NPE 없음.

## 4. 요건
- **FR**: C1~C13 예외경로 박제(400 + error 메시지 정확 단언, fail()/다른 형태와 구별). 가드 통과 후 예외 확인.
- **NFR**: production 변경 0, `mvnw -o clean verify` SUCCESS, QuotationController 90.3→~98% LINE, JaCoCo floor 유지, 구현 후 codex PASS → 듀얼푸시.

## 5. 영향/리스크
- `quotation/controller/QuotationControllerTest.java` 케이스 추가. production 0.
- 리스크: ⚠`logService.log` 오버로드 모호성([[InspectReportController]] 함정) — verify 시 타입 명시. update계열은 underlying save 메서드를 stub. void 메서드(delete계열)는 `doThrow`.
- 잔여 2줄(roundDown·getCurrentUsername 폴백) 의도적 제외.
