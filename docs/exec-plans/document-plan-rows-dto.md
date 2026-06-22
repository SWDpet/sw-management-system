# 개발계획서 — DocumentController 사업수행계획서 조회 응답 Map→record (document-plan-rows-dto)

- **상태**: v0.1 (경량 — codex 검토 + 사용자 최종승인 대기)
- **작성일**: 2026-06-22
- **맥락**: §6-4 in-place 치환 후속(document-userinfo 다음). DocumentController plan 그룹(L1248-…). 읽기 projection.
- **무손실 핵심**: 컨트롤러-로컬 `HashMap` 래퍼 + 3 `List<Map>` projection → JsonNode tree 동치. 키셋 **고정** → record. 요청바디(savePlanData)는 보존.
- **디자인팀** 비해당. **DB** 무관.

## 대상 — 치환 1(getPlanData) / 보존 1(savePlanData)

**`getPlanData` GET /api/plan/{projId}** — 응답 래퍼 7키 고정:
`{projPurpose, supportType, scopeText, inspectMethod, targets[], manpowerPlans[], schedules[]}`

| record (com.swmanager.system.dto.workplan) | 키 | from |
|---|---|---|
| `PlanData(String projPurpose, String supportType, String scopeText, String inspectMethod, List<PlanTargetRow> targets, List<PlanManpowerRow> manpowerPlans, List<PlanScheduleRow> schedules)` | 7키 래퍼 | 컨트롤러 조립 |
| `PlanTargetRow(Long id, String productName, Integer qty)` | id,productName,qty | `from(PjtTarget)` |
| `PlanManpowerRow(Long id, String stepName, String startDt, String endDt, Integer gradeSpecial, Integer gradeHigh, Integer gradeMid, Integer gradeLow, Integer funcHigh, Integer funcMid, Integer funcLow, String remark)` | 12키 | `from(PjtManpowerPlan)` — startDt/endDt = `getX()!=null?toString():null`(LocalDate) |
| `PlanScheduleRow(Long id, String processName, Boolean m01,…,Boolean m12, String remark)` | 15키 | `from(PjtSchedule)` |

- 타입(엔티티 게터): PjtTarget id=Long·productName=String·qty=Integer; PjtManpowerPlan id=Long·stepName/remark=String·startDt/endDt=LocalDate(→String)·grade*/func*=Integer; PjtSchedule id=Long·processName/remark=String·m01-m12=Boolean.
- 전 키 무조건 put(고정 키셋) → null 포함(@JsonInclude 미부착) 동치. 404(notFound) 보존.

**`savePlanData` POST /api/plan/{projId}** — **보존**: `@RequestBody Map<String,Object> body` + targets/manpowerPlans/schedules 배열 lenient 파싱(`body.containsKey`·`(String)`캐스트·`m.get`). 타입화는 무손실 불가(participant 결론 동일) → Map 유지.

## 목표 (FR/NFR)
- **FR**: getPlanData `Map<String,Object>` 8선언(return·result·targets/mps/schs 각 List+m) 제거 → record 4. **선언 238→230 (−8)**. savePlanData 보존.
- **NFR**: 응답 wire(7키 래퍼·중첩 행 키셋·값·null·LocalDate toString·Boolean)·status(200/404) 무손실, 회귀 0, ratchet 230 tighten. URL 무변.

## 검증
1. `PlanDataRowsTest`(신규): 4 record from → 현행 HashMap 복제본과 JsonNode tree 동치. 경계: PlanData 7키, PlanManpowerRow startDt null→null·값→toString, PlanScheduleRow m01-m12 Boolean(15키), 빈 리스트, null 필드 키 보존.
2. `GOLDEN_RECORD=1 … MapDebtRatchetTest` → 238→230.
3. `./mvnw test` 전체 green(EndpointInventory 불변).
4. codex 검토(키셋·LocalDate toString·Boolean·null·savePlanData 보존).

## 롤백
원자 1 커밋 → `git revert`. record 4 신규 + getPlanData 1 메서드 국소 치환. 클라이언트 무변.

## 커밋 메시지(승인 후)
`refactor(api): DocumentController 사업수행계획서 조회 응답 Map→PlanData+행 record 4종(savePlanData 요청바디 보존) + ratchet 238→230 (§6-4)`
