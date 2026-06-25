# [기획서] QuotationService 커버리지 상향 + dead 1 정리 (beyond-A)

- **작성팀**: 기획팀
- **작성일**: 2026-06-25
- **트랙**: beyond-A 커버리지 스프린트(coverage-core-services). 선행 `coverage-inspect-report-service`(`62ca2e4`) 후속.
- **상태**: ✅ 완료 (2026-06-25). codex APPROVE-WITH-FIX(보완 반영) + 구현검증. dead getWageRate(단수) 제거 + 신규 6테스트. QuotationService LINE 69.4%→99%, 전역 48.46%→49.34%.

---

## 1. 배경 / 목표

`QuotationService`(403줄, public 23)는 현재 LINE **69.4%**(미커버 약 63줄). 기존 `QuotationServiceTest`(14테스트)는 견적번호·createQuotation(VAT/낙찰율/절사)·update/delete·copyPatterns·saveWageRate 를 커버. 미커버는 (a) 호출처 0 public 1개 + (b) 미테스트 메서드(통계·재계산·조회/삭제 위임 다수).

**목표**: dead 1개 제거(무손실) + 미커버 메서드 mock 단위테스트 추가 → 커버리지 상향.

---

## 2. Part A — dead 제거

| 메서드 | 라인 | 비고 |
|---|---|---|
| `getWageRate(Integer year, String gradeName)` (단수) | 331~335 | 전 src(java/html) 참조 0. ⚠ 내부 호출 `wageRateRepository.findByYearAndGradeName` 는 **saveWageRate(L338) 가 사용 → repo 메서드 유지**(cascade 없음). 복수형 `getWageRates` 와 혼동 주의(그건 live). |

---

## 3. Part B — 테스트 추가 대상

| 대상 | 커버 내용 |
|---|---|
| `recalculateAllGrandTotals` | findAll 순회 → rawTotal 합산, totalAmount/Text 복원, grandTotal 재계산(calcGrandTotal 경유: VAT/낙찰율/절사), quotation.save + ledger 동기화, count 반환 |
| `getStats` | count + 카테고리별 countByCategory/sumAmountByCategory → Map key/value(QtCategory 라벨 기반) |
| `getRemarksPatterns` | `findAllByOrderBySortOrderAscPatternNameAsc` → RemarksPatternDto.from + remarksRenderer.render → renderedContent |
| `getQuotations` | category/year/status 위임 + DTO 매핑 |
| 위임 그룹 | `getLedger`/`getPatterns`/`savePattern`/`deletePattern`/`deleteAllPatterns`/`saveRemarksPattern`/`deleteRemarksPattern`/`getWageRates`/`getWageRateYears`/`deleteWageRate` 중 대표 위임 verify |

- 기존 setUp 의 7개 mock(quotation/ledger/pattern/remarksPattern/wageRate/seq repo + remarksRenderer) 재사용. 추가 mock 불요.

---

## 4. 기능 요건 (FR)

| ID | 내용 |
|----|------|
| FR-1 | Part A: `getWageRate`(단수) 삭제. `findByYearAndGradeName` repo 메서드·`getWageRates`(복수) 는 유지. |
| FR-2 | 삭제 전 `getWageRate` 단수 전역 참조 0 재확인(복수형 제외). |
| FR-3 | Part B: §3 대상에 mock 단위테스트 추가(운영DB 무관). `recalculateAllGrandTotals`·`getStats` 는 값 단언, 위임은 호출 verify. |
| FR-4 | live 동작·시그니처 불변(getWageRate 외). |

---

## 5. 비기능 요건 (NFR)

| ID | 내용 |
|----|------|
| NFR-1 | compile 성공. |
| NFR-2 | `QuotationServiceTest` 전건 green(기존 14 + 신규). |
| NFR-3 | 전체 `./mvnw test` green. |
| NFR-4 | JaCoCo 게이트 통과 + QuotationService·전역 커버리지 상승. floor 상향은 구현 후 실측 판단(실측−2~2.5pp). |
| NFR-5 | dead 삭제 비가역 — git history. |

---

## 6. 의사결정

- **6-1 getWageRate 단수만 삭제** ✅: 복수형 getWageRates 는 live(컨트롤러 사용), repo findByYearAndGradeName 은 saveWageRate 사용 → 둘 다 유지. 혼동 방지로 단수만 정밀 제거.
- **6-2 위임 메서드 테스트 깊이** ✅: 1~3줄 위임은 호출 verify + 반환 매핑 정도(과한 단언 불요). 로직성(recalc/getStats) 만 값 단언.

---

## 7. 영향 범위 / 리스크

| 계층 | 파일 | 유형 |
|------|------|------|
| Service(수정) | `quotation/service/QuotationService.java` | 메서드 1 제거 |
| Test(수정) | `quotation/service/QuotationServiceTest.java` | 신규 테스트 추가 |

DB/API/UI 변경 0. UI 키워드 0 → 디자인팀 skip.

| 리스크 | 수준 | 완화 |
|---|---|---|
| getWageRate 단/복수 혼동 삭제 | 낮음 | FR-2 단수 정밀 재확인 + 복수형 유지 명시 |
| recalc/getStats mock 누락 | 낮음 | findAll/count/countByCategory/sumAmountByCategory + ledger stub |

---

## 8. 승인 요청

본 기획서에 대한 codex 검토 및 사용자 최종승인을 요청합니다.
