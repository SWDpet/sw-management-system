# 개발계획서 — QuotationController CRUD 예외경로 커버리지 보강 (B)

- **상태**: ✅ 구현 완료(2026-06-28). +13테스트 green, QuotationController 90.3→99.3% LINE. 구현 codex PASS + dual-review(합의1 false-premise 미반영·Map instanceof 대칭만 반영). `mvnw -o clean verify` 1446 green.
- **작성일**: 2026-06-28
- **기획서**: `docs/product-specs/coverage-quotation-controller.md` (codex APPROVE-WITH-FIX)
- **안전망**: 현 green 스위트 + JaCoCo ratchet/PIT 게이트. **production 변경 0 (test-only)**.
- **원칙**: 기존 `QuotationControllerTest`(53) 확장. 4-mock 생성자주입·login 헬퍼 재사용. 커밋 1개.

---

## Step 0 — baseline 고정(완료)

- 전역 LINE 78.34%·INSTR 63.98%(floor 0.75/0.61). 대상 QuotationController LINE 90.3%(miss 29).
- **확정 사실**: 에러 본문 Map(`ExceptionMessages.safe(e)`=getMessage="boom") vs ApiResult(`failMessage(e.getMessage())`="boom"). 서비스 return: updateQuotation/savePattern/deleteAllPatterns/copyPatterns/saveRemarksPattern/saveWageRate → **thenThrow**; deleteQuotation/deletePattern/deleteRemarksPattern/deleteWageRate(void) → **doThrow**. 가드: updateQuotation/deleteQuotation/deleteAllPatterns=isAdmin(loginAdmin), 나머지=checkEditAuth(loginEdit). copyPatterns 스텁=`copyPatterns(anyList(), eq("A"))`.

**검증:** 없음(준비).

## Step 1 — Map 형태 catch (C1·C2·C3·C6·C7·C8·C11·C12·C13)

공통 헬퍼: `assertMapError(res)` → status 400 + `(Map)body` non-null·`get("success")`=false·`get("error")`="boom".
- C1 updateQuotation: loginAdmin + `when(updateQuotation(any)).thenThrow(RE("boom"))` → `controller.updateQuotation(1L, QuotationDTO.builder().build())`.
- C2 deleteQuotation: loginAdmin + `doThrow(RE).when(quotationService).deleteQuotation(1L)`.
- C3 savePattern: loginEdit + `savePattern(any)` thenThrow → `controller.savePattern(new ProductPattern())`.
- C6 deleteAllPatterns: loginAdmin + `deleteAllPatterns()` thenThrow.
- C7 copyPatterns: loginEdit + `copyPatterns(anyList(), eq("A"))` thenThrow → request `Map.of("patternIds", List.of(1), "targetCategory", "A")`.
- C8 saveRemarksPattern: loginEdit + `saveRemarksPattern(any)` thenThrow → `new RemarksPattern()`.
- C11 saveWageRate: loginEdit + `saveWageRate(any)` thenThrow → `new WageRate()`.
- C12 updateWageRate: loginEdit + `saveWageRate(any)` thenThrow → `controller.updateWageRate(1L, new WageRate())`.
- C13 deleteWageRate: loginEdit + `doThrow.when().deleteWageRate(1L)`.

## Step 2 — ApiResult 형태 catch (C4·C5·C9·C10)

공통 헬퍼: `assertApiResultError(res)` → status 400 + `(ApiResult)body` `success()`=false·`error()`="boom".
- C4 updatePattern: loginEdit + `savePattern(any)` thenThrow → `controller.updatePattern(1L, new ProductPattern())`.
- C5 deletePattern: loginEdit + `doThrow.when().deletePattern(1L)`.
- C9 updateRemarksPattern: loginEdit + `saveRemarksPattern(any)` thenThrow → `controller.updateRemarksPattern(1L, new RemarksPattern())`.
- C10 deleteRemarksPattern: loginEdit + `doThrow.when().deleteRemarksPattern(1L)`.

**검증:** `./mvnw -o -Dtest=QuotationControllerTest test` 전체 green.

## Step 3 — 전체 검증 + floor 검토

- `./mvnw -o clean verify` BUILD SUCCESS(1433 → +13).
- QuotationController LINE 90.3 → **~98%** 확인(JaCoCo html; 잔여=roundDown unit>1·getCurrentUsername 폴백 2줄).
- 전역 재측정 → 게인 작으면 floor 유지. ratchet·PIT 불변.

## Step 4 — codex 검증 + dual-review + 커밋 + 듀얼푸시

- codex 검증(분기·brittle·약한단언 — error 값 단언으로 fail()/형태 구별).
- dual-review(codex+Opus) → 수렴.
- 커밋: `test(coverage): QuotationController CRUD 예외경로 보강 (beyond-A)`.
- `git push origin master` (듀얼).

---

## 검증 매트릭스 (FR/NFR → Step)

| 항목 | 검증 Step |
|---|---|
| FR C1~C13 catch 예외경로(error 단언) | Step 1·2 |
| NFR verify·90.3→~98% | Step 3 |
| NFR floor/ratchet/PIT 불변 | Step 3 |
| NFR production 0·듀얼푸시 | Step 4 |

## 롤백

- 단일 테스트 파일 케이스 추가 → 문제 시 제거. production 무영향.
