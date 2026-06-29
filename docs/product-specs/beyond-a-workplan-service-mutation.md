# [기획서] WorkPlanService 테스트 강화 — 뮤테이션 70%→게이트 (beyond-A)

- **작성팀**: 기획팀 / **작성일**: 2026-06-29
- **트랙**: beyond-A (PIT 게이트 확장, Quotation/InspectReport 동일 방법론). 업무계획 캐스케이드 서비스.
- **상태**: ✅ 구현 완료(2026-06-29).

---

## 1. 배경
WorkPlanService(업무계획 저장 + 대상 인프라/지역 캐스케이드) 뮤테이션 **70%(생존 25·NO_COV 5)**.

## 2. 핵심 발견 + 강화
- ⚠ **분리된 테스트**: 캐스케이드 해피패스는 `WorkPlanTargetTest`(별도)가 커버하는데 PIT targetTests 에 `WorkPlanServiceTest` 만 있어 놓침 → **WorkPlanTargetTest 도 targetTests 에 추가**(NO_COV 5→2).
- 나머지 강화(`WorkPlanServiceTest` +6):
  - saveWorkPlan 기본필드(planType·processStep·title·description·start/endDate·location·statusReason) 전 단언(VoidMethodCall kill).
  - applyTarget 3분기에서 **기존 plan 에 prior project/infra/region 세팅** 후 set-null 검출(L171·198·199·208·209·257~260) — 신규 plan 은 이미 null 이라 mutant 미검출이었음.
  - resolveRegionCode 분기: cityNm 공백→null(L215 EmptyObjectReturnVals), distNm 도청→시도 self-행(L217 NegateConditionals, `never()` 로 비-도청 조회 부재 검증).

## 3. 결과
- WorkPlanService 뮤테이션 **70→97.1%**(생존 25→2·NO_COV 5→1; 잔존=equivalent firstAdmSectC blank·sysNm 길이경계). 테스트 24→30(+WorkPlanTargetTest 9 동원).
- PIT 게이트 **13→14클래스**, threshold 94 유지(전체 505/521=96.9%).
- 정규 verify green(30테스트+coverage), PIT 97% BUILD SUCCESS. dual-review(전 low). production 0.
