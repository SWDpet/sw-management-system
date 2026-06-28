# [개발계획] WorkPlanService getPreContactsByDate dead 체인 제거

- **작성팀**: 개발팀 / **작성일**: 2026-06-28 / **기획**: `docs/product-specs/deadcode-workplan-precontacts.md`
- **상태**: ✅ 구현 완료(2026-06-28). D1/D2/D3 삭제, BUILD SUCCESS(컴파일=호출처 0 증명), WorkPlanService 100% 유지. codex 구현검증 PASS·dual-review 전건 diff-blindness 오탐(build green 반증). 상세는 기획서 상태줄.

---

## 0. 사전검증 (완료)
- grep `src/` 전수: `getPreContactsByDate` 호출 = 테스트 1곳뿐. `findPreContactsByDate` 호출 = 위 dead 서비스 메서드 + 테스트뿐. production 0.
- PRE_CONTACT 는 enum/도메인/DTO/template 에서 일반 planType 으로 사용 → 유지.
- `WorkPlan`(엔티티)·`LocalDate` import 는 두 파일 모두 다른 곳에서 다수 사용 → 잔존 예상.

## 1. 삭제 (3곳)
- **D1** `WorkPlanService.java` L283-290: javadoc `사전연락 일정 조회(알림용)` + `@Transactional(readOnly=true) public List<WorkPlan> getPreContactsByDate(LocalDate)` 블록.
- **D2** `WorkPlanRepository.java` L42-47: 주석 `// 사전연락 일정 조회 (알림용)` + `@Query(...) List<WorkPlan> findPreContactsByDate(@Param ...)`.
- **D3** `WorkPlanServiceTest.java` L373-384: `@Test void getPreContactsByDate_returnsRawList()`.

## 2. 검증 절차
1. `./mvnw -o clean verify` → BUILD SUCCESS(미사용 import/잔여 호출처 0 → 컴파일 통과), 잔여 WorkPlanService 테스트 green.
2. `jacoco.csv` → WorkPlanService 100% 유지(분모 축소), 전역 delta(미미), floor 유지.
3. 구현 후 codex 검증 → dual-review → 듀얼푸시.

## 3. 리스크/완화
- **R1**: 삭제 후 컴파일 에러=잔존 호출처 신호 → 즉시 검출. grep 으로 사전 0 확인.
- **R2 import**: 미사용화 시 정리(예상 잔존). 컴파일로 확인.
- production 회귀 0.
