# [기획서] WorkPlanService 캘린더/칸반 조회 위임 커버리지 보강 (B)

- **작성팀**: 기획팀 / **작성일**: 2026-06-28
- **트랙**: beyond-A 커버리지 증분 B. 업무계획 서비스. 기존 `WorkPlanServiceTest` 확장형.
- **상태**: ✅ 구현 완료(2026-06-28). WorkPlanServiceTest +4(21→25) → WorkPlanService **miss 13→0 (100% LINE)**(130/130). 전역 LINE 78.72→78.82%·INSTR 64.23→64.28%, floor 유지. `mvnw -o clean verify` 1457 green. codex 기획 APPROVE·개발계획 APPROVE-WITH-FIX(same→다시 eq 권고)·구현 1차 NEEDS-FIX(same/isSameAs 과도)→eq/containsExactly 완화→PASS. dual-review(codex1/Opus4) 합의2(아래 백로그)·분쟁3 refute.
- **production 백로그(범위 밖)**: `getPreContactsByDate` 가 `List<WorkPlan>`(raw 엔티티)를 반환 — 형제 메서드는 DTO. 영속성 계층 누출 소지. **DTO 매핑 일관화는 별도 production 리팩터**(호환성 영향, 본 test-only 증분 범위 밖). 테스트는 현 동작 특성화만.
  - **⛔ 해소(2026-06-28, dead 제거)**: 조사 결과 `getPreContactsByDate`/`findPreContactsByDate` 는 **production 호출처 0**(미완성 알림기능 스텁) → DTO 일관화 무의미. 무손실 헌장상 **dead 체인 삭제**(C4 메서드·repo 쿼리·`getPreContactsByDate_returnsRawList` 테스트). 기획/계획 `docs/{product-specs,exec-plans}/deadcode-workplan-precontacts.md`. ⇒ 본 문서 C4 항목은 현재 WorkPlanServiceTest 에 부재.

---

## 1. 배경 / 목표

`WorkPlanService`(LINE miss 13)는 CRUD·검색·정규화는 덮였으나, **캘린더/칸반/알림용 조회 위임 메서드 4종**이 미커버다. 각각 repo 조회 + `WorkPlanDTO::fromEntity` 매핑(또는 raw 반환)이라, mock repo + 인자/매핑 단언으로 박제한다(잘못된 repo 메서드·인자순서·매핑 누락 회귀 방어).

JaCoCo 미커버(`WorkPlanService.java.html` nc): 41-44, 52-55, 74-77, 288.

## 2. 대상 메서드

| # | 메서드(라인) | 위임 |
|---|---|---|
| C1 | getWorkPlansByDateRange(start,end)(41-44) | `findByDateRange(start,end)` → List<WorkPlanDTO> |
| C2 | getWorkPlansByAssigneeAndDateRange(assigneeId,start,end)(52-55) | `findByAssigneeAndDateRange(assigneeId,start,end)` → List<WorkPlanDTO> |
| C3 | getWorkPlansByProcessStep(step,excludeStatuses)(74-77) | `findByProcessStepAndStatusNotInOrderByStartDateAsc(step,excludeStatuses)` → List<WorkPlanDTO> |
| ~~C4~~ | ~~getPreContactsByDate(targetDate)(288)~~ → **2026-06-28 dead 제거**([deadcode-workplan-precontacts](deadcode-workplan-precontacts.md)) | ~~`findPreContactsByDate` raw List<WorkPlan>~~ — 메서드·쿼리·테스트 삭제(호출처 0) |

## 3. 변경 — `WorkPlanServiceTest` 케이스 추가 (테스트만)
- 기존 field-mock setUp(`workPlanRepository` 등)·`new WorkPlanService()`+ReflectionTestUtils 재사용. 신규 4 테스트.
- **C1~C3**: `when(repo.<find>(인자)).thenReturn(List.of(workPlan))`(planId/title 세팅) → 반환 List size 1 + DTO planId/title 매핑 단언 + **인자 정확 전달 verify**(eq(start)/eq(end)/eq(step)/eq(excludeStatuses) — 잘못된 메서드·인자 회귀 방어).
- ~~**C4**: `findPreContactsByDate(date)` …~~ → **2026-06-28 dead 제거**(메서드·테스트 삭제, deadcode-workplan-precontacts).

## 4. 요건
- **FR**: ~~C1~C4~~ → **C1~C3** 위임 박제(repo 메서드·인자·매핑/passthrough). 조회 회귀 방어. (C4 는 dead 제거됨.)
- **NFR**: production 변경 0, `mvnw -o clean verify` SUCCESS, WorkPlanService miss 13→0(~100%), JaCoCo floor 유지, 구현 후 codex PASS → 듀얼푸시.

## 5. 영향/리스크
- `service/WorkPlanServiceTest.java` 케이스 추가. production 0.
- 리스크: 위임 메서드라 가치는 낮으나(분기 없음) 잘못된 repo 메서드/인자 회귀를 인자 verify 로 방어. WorkPlanDTO.fromEntity 매핑 필드는 기존 테스트와 동일(planId/title) 사용.
