# [기획서] InspectReportService 테스트 강화 — 뮤테이션 78%→게이트 (beyond-A)

- **작성팀**: 기획팀 / **작성일**: 2026-06-29
- **트랙**: beyond-A (PIT 게이트 확장, QuotationService 와 동일 방법론). 점검보고서 핵심 서비스 강화.
- **상태**: ✅ 구현 완료(2026-06-29).

---

## 1. 배경 / 목표
[[beyond-a-quotation-service-mutation]] 과 동일 패턴 — InspectReportService(점검보고서 save/findById/delete/조회) 뮤테이션 **78%(생존 16·NO_COV 1)** → 강화로 회귀방어 + PIT 게이트 편입. 후보 측정에서 WorkPlanService(70%)보다 achievable(NO_COV 이미 1).

## 2. 생존 분석 → 강화 (PIT 실측)
| 클러스터 | 생존 | kill |
|---|---|---|
| save visitLog/checkResult setId(null)·sortOrder 조건(L113·114·128) | VoidMethodCall·NegateConditionals | 사전 id 세팅+비0 sortOrder 보존+캡처 id null 단언 |
| save 수정경로 기존부재 orElseThrow(L62~63) | NullReturnVals NO_COV | findById empty → throw + 메시지 단언 |
| findById visits/previousVisits/checkResults(L160·163·169·177) | VoidMethodCall·NegateConditionals | 비빈 데이터 stub + dto 컬렉션 단언 + pjtId 분기 양쪽 |
| getPreviousVisits(L191) | EmptyObjectReturnVals | 비빈 데이터 반환 단언 |
| getTemplateItems lambda(L282·284) | VoidMethodCall | category·itemMethod 단언 |
| delete setBatchId(null)·month 길이경계·ifPresent delete(L226·232·235·240) | VoidMethodCall·ConditionalsBoundary | 사전 batchId+findByDocNo present→delete 검증+month len 4/3 경계 |

## 3. 결과
- InspectReportService 뮤테이션 **78→98.7%**(생존 16→1·NO_COV 1→0; 잔존 1=equivalent month 경계). 테스트 20→26.
- PIT 게이트 **12→13클래스** 편입, threshold 94 유지(전체 406/419=96.9%).
- 정규 verify green(26테스트·coverage met), PIT 97% BUILD SUCCESS. dual-review(합의2 low·분쟁3 refute). production 0.
- ⚠ batchId 는 String(Long 아님) — 첫 작성 시 컴파일 에러 후 정정.
