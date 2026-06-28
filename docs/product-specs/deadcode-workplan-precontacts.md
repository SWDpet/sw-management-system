# [기획서] WorkPlanService getPreContactsByDate dead 체인 제거 (무손실)

- **작성팀**: 기획팀 / **작성일**: 2026-06-28
- **트랙**: production 무손실 정리(beyond-A 헌장). 미사용 "사전연락 알림" 조회 체인 제거.
- **상태**: ✅ 구현 완료(2026-06-28). D1(서비스 메서드)·D2(repo @Query)·D3(테스트) 삭제. `mvnw -o clean verify` BUILD SUCCESS(컴파일=잔여 호출처/orphan import 0 증명), WorkPlanServiceTest 25→24 green, WorkPlanService LINE 100% 유지(분모 축소), 전역 LINE 81.18%·floor 0.78/0.64 유지. PRE_CONTACT enum/도메인/DTO/template 무변경. codex 기획 APPROVE-WITH-FIX(구 coverage-workplan-service.md C4 흔적 정리 반영)·구현검증 PASS(코드삭제 안전). dual-review(codex3/Opus3) 합의2·분쟁4 **전건 diff-blindness 오탐**("diff 밖 호출처 깨짐")→build green+grep 0+codex 2회 repo-wide 확인으로 정의적 반증. 실결함 0.

---

## 1. 배경 / 목표

`WorkPlanService.getPreContactsByDate(LocalDate)`(L287)는 "사전연락 일정 조회(알림용)" 주석을 달고 `WorkPlanRepository.findPreContactsByDate`(L47, `@Query` PRE_CONTACT+PLANNED+startDate) 를 위임한다. 그러나 **production 호출처가 0개**다 — 컨트롤러/스케줄러/알림 컴포넌트/템플릿 어디에서도 호출하지 않는다(grep `src/` 전수: 유일 호출처는 테스트 `WorkPlanServiceTest.getPreContactsByDate_returnsRawList` 뿐). 알림 기능이 끝내 와이어링되지 않은 **미완성 스텁**이다.

[[project_swmanager_coverage_sprint]] 백로그는 이를 "raw 엔티티 반환(영속성 누출) → DTO 일관화 리팩터(호환성 영향)"로 기재했으나, **호출처 0 이라 DTO 일관화 대상 자체가 없다**(아무도 안 쓰니 "일관성"·"영속성 누출" 모두 무의미). 실체는 **dead code 체인**이며, [[feedback_swmanager_s_tier_quality]] 무손실 헌장의 "dead code부터 의심·삭제"가 정답이다.

## 2. 범위 (production 무손실 삭제)
- **D1** `WorkPlanService.getPreContactsByDate` 메서드 + javadoc(L283-290) 삭제.
- **D2** `WorkPlanRepository.findPreContactsByDate` `@Query` 메서드 + 주석(L42-47) 삭제.
- **D3** 테스트 `WorkPlanServiceTest.getPreContactsByDate_returnsRawList`(L374-384) 삭제(대상 사라짐).

**유지(삭제 금지)**: `WorkPlanType.PRE_CONTACT` enum·`WorkPlan.planType`·`WorkPlanDTO.planType`·template `.type-PRE_CONTACT` CSS — PRE_CONTACT 는 사용자가 생성하는 **일반 업무유형**으로 캘린더/칸반 전반에서 유효(삭제 대상은 "PRE_CONTACT 를 알림용으로 전수 조회"하는 미사용 쿼리뿐).

## 3. 요건
- **FR-1**: dead 3종(서비스 메서드·repo 쿼리·테스트) 삭제. live 경로 무변경.
- **NFR**: `mvnw -o clean verify` SUCCESS(컴파일=미사용 import/잔여 호출처 0 확인), WorkPlanService 커버리지 유지(분모 축소, 100% 유지), 전역 유지, floor 유지, 구현 후 codex PASS + dual-review → 듀얼푸시.

## 4. 영향 / 리스크
- 변경: `service/WorkPlanService.java`·`repository/workplan/WorkPlanRepository.java`·`service/WorkPlanServiceTest.java`.
- **R1 호출처 잔존**: grep 전수로 0 확인(production·test 통틀어 getPreContactsByDate/findPreContactsByDate 의 호출은 본 체인 내부+삭제할 테스트뿐). 컴파일로 재확인.
- **R2 import 정리**: 삭제로 `WorkPlan`/`LocalDate` import 가 미사용이 되면 정리(둘 다 서비스 전반 사용이라 잔존 예상). 컴파일로 검출.
- **R3 PRE_CONTACT 일반유형 보존**: enum/도메인/DTO/템플릿 무변경(미사용 알림쿼리만 제거).
- production 동작 회귀 0(미사용 코드 제거).
