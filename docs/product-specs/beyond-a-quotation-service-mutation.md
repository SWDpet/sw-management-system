# [기획서] QuotationService 테스트 강화 — 뮤테이션 52%→게이트 + PIT 편입 (beyond-A)

- **작성팀**: 기획팀 / **작성일**: 2026-06-29
- **트랙**: beyond-A (PIT 게이트 확장). 핵심 견적 서비스의 약한 테스트(line커버는 되나 mutation 52%)를 강화.
- **상태**: ✅ 구현 완료(2026-06-29). QuotationServiceTest 강화(20→32 테스트, ArgumentCaptor 저장엔티티 전필드단언+조건분기 양쪽+비빈 distinct 데이터+경계+non-null+메시지). **뮤테이션 52%→97.4%**(생존 68→4·NO_COV 4→0; 잔존 4=equivalent roundDown L30·setLastSeq(0)·vatIncluded negate·copyPattern). **PIT 게이트 편입**(11→12클래스, threshold 93→94, 전체 329/341=96.5%). 정규 verify green(32테스트, coverage met). codex APPROVE-WITH-FIX(createWith quoteId 부여·92% 조건부·비빈데이터 전부 반영). dual-review(합의1 라인주석 low·분쟁4 refute). ⚠CI mutation job 으로 자동 검증(push 후).

---

## 1. 배경 / 목표
`QuotationService`(견적 핵심 비즈니스 로직, ext의존0)는 라인커버는 되지만 **PIT 뮤테이션 52%(생존 68·NO_COV 4)** — 테스트가 메서드를 호출만 하고 **결과/효과를 약하게 검증**해 행동변경 버그의 절반을 못 잡음. 강화해 **회귀방어 실효** 확보 + 가능하면 PIT 게이트 편입.

## 2. 생존 분석 (PIT 실측) → 강화 전략
| 클러스터 | 생존 유형 | kill 방법 |
|---|---|---|
| createQuotation/registerLedger setter (L110~122,214~225) | VoidMethodCall 다수 | 저장 엔티티 **ArgumentCaptor 캡처 + 전 필드 단언**(totalAmountText·status·items·ledger 12필드·MathMutator nextNo) |
| updateQuotation (L155~183) | VoidMethodCall+NegateConditionals | setter 결과 단언 + **조건부 기본값 분기 양쪽**(dto null↔set: rounddownUnit·showSeal·templateType·status) + createdBy 분기(L166~167 NO_COV) |
| copyPatterns (L272~281) | VoidMethodCall | 캡처 + **복사 전 필드** 단언(현 테스트는 일부만) |
| saveWageRate (L337~343) | VoidMethodCall+NullReturnVals | 갱신 전 필드(monthly·hourly·desc) 단언 + **반환 non-null** |
| getLedger/getPatterns/getWageRates (L231~322) | EmptyObjectReturnVals+NegateConditionals | **비어있지 않은 distinct 데이터** stub + 분기별 다른 반환 단언(현 테스트는 빈 리스트라 무력) |
| generateQuoteNumber lambda (L69~71) | VoidMethodCall | new-seq 캡처 + year/category/lastSeq 단언 |
| calcGrandTotal (L47) | ConditionalsBoundary | **bidRate=0 경계** 테스트(>0 vs >=0) |
| getQuotation (L137 NO_COV) | — | 성공 경로(findById present) 테스트 |
| roundDown (L30) | ConditionalsBoundary | **equivalent**(unit=1 시 양쪽 동일) — kill 불가, 잔존 허용 |

## 3. 범위
- **D1** `QuotationServiceTest` 강화: 위 전략대로 ArgumentCaptor·전필드단언·분기양쪽·경계·non-null·비빈데이터. 기존 테스트는 유지/보강.
- **D2** PIT 게이트 편입(달성 시): `QuotationService`+`QuotationServiceTest` 를 pom PIT targetClasses/Tests 에 추가. **단 KILLED/TOTAL≥92·NO_COV≤2 달성 시에만**(equivalent 로 미달 시 게이트 미편입, 테스트 강화만으로도 가치).

## 4. 요건
- **FR-1**: QuotationService 뮤테이션 52%→**≥92%**(목표), NO_COV≤2. 미달 시 달성치까지(테스트 강화 자체가 회귀방어 향상).
- **FR-2**: 달성 시 PIT 게이트 편입(threshold 영향 재측정).
- **NFR**: `mvnw -o clean verify` green(테스트 추가), production 코드 0(테스트만), codex + dual-review. PIT 게이트 편입 시 CI mutation job 으로 자동 검증.

## 5. 영향 / 리스크
- 변경: `QuotationServiceTest`(대폭 보강) + (달성 시) pom PIT 목록. production 0.
- **R1 92% 미달 가능**: equivalent mutation(roundDown 등)으로 정확히 92% 못 칠 수 있음 → 그 경우 게이트 미편입, 강화만 커밋(가치 유지). 정직하게 달성치 보고.
- **R2 mock 정합**: ArgumentCaptor/thenAnswer 가 실제 흐름과 일치해야(엔티티 동일성·저장순서). 기존 mock 패턴 재사용.
