# [개발계획서] QuotationService 커버리지 상향 + dead 1 정리 (beyond-A)

- **작성팀**: 개발팀
- **작성일**: 2026-06-25
- **기획서**: `docs/product-specs/coverage-quotation-service.md` (codex APPROVE-WITH-FIX, 사용자 최종승인 완료)
- **상태**: ✅ 완료 (2026-06-25). codex 기획/개발계획 APPROVE-WITH-FIX(보완 반영) + 구현검증. dead getWageRate(단수) 제거 + 신규 6테스트(기존 14→20). QuotationService LINE 69.4%→99%, 전역 48.46%→49.34%. floor 0.45→0.47/0.38→0.39.

---

## 1. 작업 개요

QuotationService 의 dead 1개(getWageRate 단수) 제거 + 미커버 메서드 mock 단위테스트 추가. 2파일(서비스·테스트).

---

## 2. Part A — dead 제거

| 파일 | 제거 | 비고 |
|---|---|---|
| `QuotationService.java` | `getWageRate(Integer year, String gradeName)` (단수, L331~335 + 직전 `@Transactional(readOnly=true)`) | 전 src 참조 0 |

- **유지**: `getWageRates`(복수, L318), `WageRateRepository.findByYearAndGradeName`(saveWageRate L338 사용). 미사용 전락 import 없음(동일 타입 계속 사용) — 실측 확인.

---

## 3. Part B — 테스트 추가 (T-n, QuotationServiceTest)

기존 14테스트 유지. 신규:

| ID | 테스트 | 검증 |
|----|--------|------|
| T-B1 | `recalculateAllGrandTotals_recomputesAndSyncsLedger` | findAll 2건 → 각 totalAmount=품목합, grandTotal=calcGrandTotal(VAT/낙찰율/절사) 재계산, quotationRepository.save 호출, ledger 존재 시 sync(setTotalAmount/setGrandTotal+save), count=2 반환 |
| T-B2 | `recalculateAllGrandTotals_noLedger_skipsSync` | ledgerRepository.findByQuoteId empty → ledger save 미호출, quotation save 는 호출 |
| T-B3 | `getStats_returnsCategoryCountsAndAmounts` | count + 카테고리별 countByCategory/sumAmountByCategory stub → Map 에 "total" + QtCategory 라벨_count/_amount 키·값 |
| T-B4 | `getRemarksPatterns_mapsToDto_withRenderedContent` | `remarksPatternRepository.findAllByOrderBySortOrderAscPatternNameAsc()` stub → `RemarksPatternDto.from(p)` + `remarksRenderer.render(content, userId)` 결과를 renderedContent 에 반영 검증 |
| T-B5 | `getQuotations_delegatesAndMaps` | category/year/status 위임 + DTO 매핑 |
| T-B6 | `delegates_simpleReadAndDelete` | 위임 대표 verify: getLedger·getPatterns·getWageRates·getWageRateYears 반환, savePattern/deletePattern/deleteAllPatterns/saveRemarksPattern/deleteRemarksPattern/deleteWageRate 호출 verify |

- 기존 setUp 의 7개 mock 재사용. 신규 import 필요 시: `QtCategory`(getStats 키 단언), 도메인/DTO 타입.
- `recalculateAllGrandTotals` 값 단언은 calcGrandTotal(private) 경유 — 절사/VAT/낙찰율 조합 1건으로 결과 고정.

---

## 4. 구현 순서 (T)

| ID | 단계 |
|----|------|
| S-1 | 가드: `getWageRate`(단수, 2인자) 전역 재스캔으로 production 호출처 0 재확인(복수형 getWageRates 제외). |
| S-2 | Part A 삭제(QuotationService getWageRate 단수). import 실측 정리. |
| S-3 | `./mvnw -q -DskipTests compile` → 성공. |
| S-4 | Part B: QuotationServiceTest 에 T-B1~T-B6 추가. |
| S-5 | `./mvnw test -Dtest=QuotationServiceTest` → 전건 green. |
| S-6 | `./mvnw verify`(전체 + JaCoCo 게이트) green. |
| S-7 | JaCoCo csv 로 QuotationService·전역 커버리지 상승 확인 → floor 상향 판단(실측−2~2.5pp, 게인 작으면 유지). |

---

## 5. 검증 기준 (NFR 매핑)

| NFR | 검증 |
|----|------|
| NFR-1 | S-3 compile |
| NFR-2 | S-5 QuotationServiceTest green |
| NFR-3 | S-6 전체 test green |
| NFR-4 | S-6 게이트 + S-7 상승 |
| NFR-5 | git history |

---

## 6. 롤백

단일 커밋. 문제 시 `git revert`. 작업 중 실패 시 Edit 되돌림.

---

## 7. 커밋 (작업완료 후)

- 메시지: `test(coverage): QuotationService 단위테스트 추가 + dead getWageRate(단수) 제거 (beyond-A)` (+ floor 상향 시 동봉)
- 듀얼푸시(SWDpet + ukjin914), author `ukjin_park@jungdouit.com`. 기획서·개발계획서 ✅ 완료 갱신 동봉.

---

## 8. 승인 요청

본 개발계획서에 대한 codex 검토 및 사용자 최종승인을 요청합니다.
