# 개발계획서 — DocumentLookup·WorkPlan 잔여 조회 응답 Map→record (doclookup-workplan-rows-dto)

- **상태**: v0.1 (경량 — codex 검토 + 사용자 최종승인 대기)
- **작성일**: 2026-06-22
- **맥락**: §6-4 후속. `doclookup-rows-dto`(process/service-purpose) 와 동일 패턴. 두 컨트롤러에 남은 조회/상태변경 API 의 컨트롤러-로컬 `Map<String,Object>` 응답조립을 record 로 치환. **남은 8 엔드포인트 전수.**
- **무손실 핵심**: 모두 컨트롤러-로컬 `HashMap`/`LinkedHashMap` 응답조립 → JsonNode tree 동치(키셋·값·null·타입)로 무손실 검증. 클라이언트는 응답 JSON 키로 접근(필드 순서 무의존). 전역 jackson non_null 미설정 → null 포함 기본(단 InfraFindResult 만 NON_NULL, 사유 아래).
- **디자인팀**: 비해당 — 백엔드 전용 sprint(서버 응답 JSON 무손실 보존, 클라이언트 파일 무변경). **DB**: 변경 없음.

## 목표 (FR/NFR)

- **FR**: 아래 8 메서드의 `Map<String,Object>` 응답조립 → record 8종. **`Map<String,Object>` 선언 303→281 (−22)**.
- **NFR**: wire 무손실(키셋·값·타입·null 포함), 회귀 0, ratchet 281 tighten. URL·HTTP status·403 body 100% 보존.

## 대상 8 메서드 + record 설계 (com.swmanager.system.dto.workplan)

### A. DocumentLookupController (−11 → 303→292)

| 메서드 | 키(현행) | record |
|---|---|---|
| `getRegionSigungus` `/api/region-sigungus` | admSectC, sggNm, sidoNm (LinkedHashMap) | `RegionSigunguRow(String admSectC, String sggNm, String sidoNm)` ← `from(SigunguCode)` |
| `getSystemsAll` `/api/systems-all` | cd, nm (LinkedHashMap) | `SystemOptionRow(String cd, String nm)` ← `from(SysMst)` |
| `findInfraByRegion` `/api/infra-find` | 미발견 `{found:false}`(1키) / 발견 `{found, infraId, cityNm, distNm, sysNm, sysNmEn}`(6키, 값 null 가능) | **2 타입 분리**(키셋 상이): 발견 `InfraFindResult(boolean found, Long infraId, String cityNm, String distNm, String sysNm, String sysNmEn)` (**@JsonInclude 미부착** — 6키 항상·null 포함) ← `of(Infra)` / 미발견 `InfraNotFound(boolean found)` → `{found:false}`. 메서드 반환타입 `ResponseEntity<?>` |
| `getProjectsFiltered` `/api/projects` | projId, year, projNm, sysNm, sysNmEn, contAmt, cityNm, distNm | `ProjectFilterRow(Long projId, Integer year, String projNm, String sysNm, String sysNmEn, Long contAmt, String cityNm, String distNm)` ← `from(SwProject)` |

- **InfraFindResult: 발견/미발견 2 타입 분리(codex P2 수정)**: 현행 HashMap 은 미발견=`{found:false}`(1키), 발견=6키(`infra.getSysNm()` 등 **null 이어도 put → 키 포함**). 키셋이 달라 단일 NON_NULL record 로는 무손실 불가(NON_NULL 은 발견 시 null 값 키까지 누락). → 발견 record 는 **@JsonInclude 미부착**(6키 항상 직렬화, null 포함)=현행 발견과 동치, 미발견은 별도 `InfraNotFound(boolean found)` record=`{found:false}` 단일키. `found` 는 둘 다 primitive boolean(true/false 포함). InfraFindResult 에는 NON_NULL 을 절대 부착하지 않음.

### B. WorkPlanController (−11 → 292→281)

| 메서드 | 키(현행) | record |
|---|---|---|
| `getSggBySido` `/api/sgg` | admSectC, sggNm, isUnit | `SggOptionRow(String admSectC, String sggNm, boolean isUnit)` ← `from(SigunguCode)` |
| `getPjtByRegion` `/api/pjt-by-region` | projId, label | `PjtOptionRow(Long projId, String label)` (라벨 조립 로직은 컨트롤러 유지, record 는 값 보유만) |
| `updateStatus` `/api/status/{id}` | 200 `{success, planId, status, statusLabel}` / 403 `{error:...}` | `WorkPlanStatusResult(boolean success, Integer planId, String status, String statusLabel)` — **200 만 record**, 403 `Map.of("error",...)` 유지(메서드 반환타입 `ResponseEntity<?>`, Map.of 는 ratchet 비대상) |
| `getCalendarEvents` `/api/events` | `{id, title, start, end, color, extendedProps:{planType, planTypeLabel, status, statusLabel, assigneeName, infraName, processStep, stepLabel}}` | `CalendarEvent(Integer id, String title, String start, String end, String color, ExtendedProps extendedProps)` + nested `ExtendedProps(String planType, String planTypeLabel, String status, String statusLabel, String assigneeName, String infraName, Integer processStep, String stepLabel)` ← `from(WorkPlanDTO)` |

- **CalendarEvent 무손실 주의**: 현행 `extendedProps` 는 `Map.of(...)`(null 금지) → 각 키 `""`/`0` fallback 적용 중. record `from()` 에 **동일 fallback** 복제(planType/status/assigneeName→`""`, processStep→`0`, infraName=`getTargetLabel()`). FullCalendar wire(top-level id/title/start/end/color + extendedProps) 보존.
- 타입 근거(엔티티/DTO 게터): SysMst cd/nm=String, SigunguCode admSectC/sggNm/sidoNm=String, Infra infraId=Long·나머지 String, SwProject projId=Long·year=Integer·contAmt=Long·나머지 String, WorkPlan planId=Integer, WorkPlanDTO planId=Integer·startDate/endDate/color/status/planType/assigneeName=String·processStep=Integer.
- `@JsonInclude` 미부착(InfraFindResult 제외) → null 포함. camelCase 키=컴포넌트명(무어노테이션).

## 커밋 분할 (원자 3 커밋, golden 선행)

1. **C1** DocumentLookupController 4종 → record 4 (303→292) + `DocLookupRows2Test` golden.
2. **C2** WorkPlanController 평면 3종(sgg/pjt/updateStatus) → record 3 (292→284) + `WorkPlanRowsTest` golden.
3. **C3** WorkPlanController `getCalendarEvents` → CalendarEvent+ExtendedProps (284→281) + `CalendarEventTest` golden(nested tree 동치).

각 커밋: 해당 엔드포인트 net 무변(레거시 HashMap 복제본 ↔ record 직렬화 tree 동치) → record 신규 → 컨트롤러 국소 치환 → `GOLDEN_RECORD=1 MapDebtRatchetTest` tighten → 커밋.

## 검증 (골든 = 레거시 Map 복제본과 tree 동치)

1. 각 golden 테스트: record 채운 엔티티/DTO → `from()` 직렬화가 현행 `HashMap`/`Map.of` 복제본과 **JsonNode tree 동치**(키셋·값·타입·null). 경계: InfraFindResult 발견 시 6키 모두 포함(sysNm 등 null 인 Infra 로도 키 6개·null 값 보존 확인) + InfraNotFound→`{found:false}` 단일키, CalendarEvent nested extendedProps 키셋(8)·`""`/`0` fallback, updateStatus 200 키셋(4).
2. `GOLDEN_RECORD=1 … MapDebtRatchetTest` → 303→281 (단계적 292→284→281).
3. `./mvnw test` 전체 green. DB 포함(RUN_DB_TESTS=true) 최종 1회.
4. codex 검토(키·타입·tree 동치·403 보존·FullCalendar wire).

## 롤백
원자 3 커밋 → 커밋별 `git revert`. record 9 신규(infra-find 2 타입 분리 반영) + 컨트롤러 2개 국소 치환(메서드 8). 클라이언트 무변.

## 커밋 메시지(승인 후)
- C1 `refactor(api): DocumentLookupController 지역/시스템/인프라/사업 조회 응답 Map→record 4종 + ratchet 303→292 (§6-4)`
- C2 `refactor(api): WorkPlanController 시군구/사업/상태변경 응답 Map→record 3종(403 보존) + ratchet 292→284 (§6-4)`
- C3 `refactor(api): WorkPlanController 캘린더이벤트 응답 Map→CalendarEvent+ExtendedProps record + ratchet 284→281 (§6-4)`
