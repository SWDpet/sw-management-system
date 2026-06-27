# [기획서] DocumentController 분리 — S4 Phase 3 (사업수행계획서 plan)

- **작성팀**: 기획팀
- **작성일**: 2026-06-27
- **트랙**: beyond-A 로드맵 S4 거대클래스 분리. Phase 1(다운로드 `e332d5f`)·Phase 2(batch `3dd2892`) 후속.
- **상태**: ✅ **완료(2026-06-27)**. DocumentPlanController 신설, DocumentController 799→661(원본 1373 대비 -712, ~52%↓), pjt* 필드 3개 제거. 커버리지 LINE 71.65% 상승(success 테스트). ratchet 297→298(swProject +1). dual-review codex 0/Opus 6(합의 3=pre-existing 보존, 분쟁 3 refute). 듀얼푸시.

---

## 1. 배경 / 목표

Phase 1·2 로 DocumentController 1373→799. 잔존 그룹 중 **사업수행계획서(plan)** 는 **전용 repository 3개(pjtTarget/pjtManpowerPlan/pjtSchedule)** 를 동반해 추출 시 필드까지 제거되어 응집도 개선이 가장 크다.

**목표**: plan 2 엔드포인트 + 헬퍼를 신규 `DocumentPlanController` 로 이동하고, plan 전용 repo 3개 필드를 DocumentController 에서 제거. **동작 100% 동일(순수 이동)**, baseline 감소-only. 인증은 `DocumentAccessSupport` 재사용.

비목표: 서명/첨부/signed-scan·조회 API(user/project)·core CRUD 는 후속/잔존.

---

## 2. 변경 설계

### 2-A 신규 `DocumentPlanController` (@Controller, @RequestMapping("/document"))
- 이동 엔드포인트(매핑·시그니처·본문 불변):
  - `GET /api/plan/{projId}` (getPlanData)
  - `POST /api/plan/{projId}` (savePlanData) — **메서드 @Transactional 유지**(TransactionAspectSupport rollback-only 포함).
- 이동 private 헬퍼: `asInt`, `asBool` (plan 전용, savePlanData 만 사용).
- 주입: SwProjectRepository, PjtTargetRepository, PjtManpowerPlanRepository, PjtScheduleRepository, DocumentAccessSupport.
- 본문 `getAuth()` → `access.getAuth()`(private wrapper 불필요, 직접 호출).
- import(codex): PlanData·PlanTargetRow·PlanManpowerRow·PlanScheduleRow·repo 4·DocumentAccessSupport·ApiResult·MVC/HTTP/@Transactional·Map/List·@Slf4j. **PjtTarget/PjtManpowerPlan/PjtSchedule 는 현행 FQN 생성 유지(import 불필요)**.

### 2-B DocumentController 정리
- 위 2 엔드포인트 + asInt/asBool 제거.
- **pjtTargetRepository·pjtManpowerPlanRepository·pjtScheduleRepository 필드 제거**(plan 전용, 12 사용 전부 plan — grep 재확인). ⚠swProjectRepository 는 core(list/detail/save 등)에서 사용 → 잔존.
- 미사용 import 정리: PlanData·PlanTargetRow·PlanManpowerRow·PlanScheduleRow(이동으로 미사용). PjtTarget/PjtManpowerPlan/PjtSchedule 는 FQN 사용이라 import 없음.
- 결과: DocumentController 799 → 약 670줄.

> `/document` 공유 컨트롤러 추가(현재 Controller/Download/Batch/Lookup/Participant + Plan). full path 비충돌(`/api/plan/**` 은 현재 DocumentController 단독 소유 → 이동 후 원본 제거만 정확하면 안전). @SpringBootTest 부팅이 ambiguous mapping 자동검출.

---

## 3. 기능 요건 (FR)

| ID | 내용 |
|----|------|
| FR-1 | getPlanData/savePlanData 의 URL·메서드·요청/응답·EDIT 가드·@Transactional(rollback-only) 동작이 이전과 완전 동일. |
| FR-2 | savePlanData 의 sw_pjt 4컬럼 갱신 + targets/manpowerPlans/schedules delete&insert·실패 시 setRollbackOnly·응답형태(200 + ApiResult.ok/failMessage) 불변. |
| FR-3 | DocumentController 잔존(core·preview·서명/첨부·조회)·기타 분리 컨트롤러 동작 불변. |
| FR-4 | 신규 기능·로직 변경·UI 변경 0(순수 이동). |

---

## 4. 비기능 요건 (NFR)

| ID | 내용 |
|----|------|
| NFR-1 | compile + `./mvnw -o clean verify` green. |
| NFR-2 | 기존 DocumentControllerTest 의 getPlanData_notFound/getPlanData_found/savePlanData_nonEdit_forbidden (3) + injectPjtRepos 헬퍼 → DocumentPlanControllerTest 로 이관, 동일 단언. 잔존 테스트 불변. |
| NFR-2c | **codex: savePlanData 성공 저장 테스트 1건 신규** — loginEdit + swProjectRepository.findById→사업 + body(projPurpose + targets/manpowerPlans/schedules) → ApiResult.ok + 각 pjt* deleteByProjId·save 호출 verify. ⚠happy path 라 catch 의 setRollbackOnly 미도달(직접호출 트랜잭션 무관). **rollback 예외경로는 직접호출 단위테스트 금지**(TransactionAspectSupport NoTransaction)→MockMvc 통합은 후속 백로그. |
| NFR-2b | @SpringBootTest 부팅 성공(매핑 비충돌). |
| NFR-3 | 전역 커버리지 비감소(코드 이동). floor 유지. |
| NFR-4 | DocumentController LOC + **필드 3개 감소**(baseline 개선). |
| NFR-4b | ControllerRepositoryRatchet: pjt* 3 edge 는 relocate(net 0), swProjectRepository +1 예상 → 정당한 이동 증가면 GOLDEN_RECORD 갱신. |
| NFR-5 | dual-review → 듀얼푸시. |

---

## 5. 의사결정

- **5-1 Phase 3 = plan** ✅: 전용 repo 3개 동반 제거로 응집도 개선 최대. asInt/asBool 도 plan 전용이라 함께 이동(잔존부 미사용 확인).
- **5-2 @Transactional 보존** ✅: savePlanData 메서드 애노테이션 + TransactionAspectSupport rollback-only 그대로 이동(프록시 동작은 @Controller 빈 공통). ⚠단위테스트 직접호출은 트랜잭션 미동작 — 기존 테스트는 예외경로 미검증(403/notFound/found 만)이라 영향 없음.
- **5-3 swProjectRepository 잔존** ✅: core 다수 사용 → 제거 불가(plan 전용 아님).

---

## 6. 영향 범위 / 리스크

| 계층 | 파일 | 유형 |
|------|------|------|
| Controller(신규) | `controller/DocumentPlanController.java` | 신규 |
| Controller(수정) | `controller/DocumentController.java` | plan 2+헬퍼+필드3 제거 |
| Test(신규/이관) | `controller/DocumentPlanControllerTest.java` | 이관 |
| Test(수정) | `controller/DocumentControllerTest.java` | plan 케이스/injectPjtRepos/미사용 import 제거 |
| Build | `golden/controller-repo-arch-baseline.txt` | 이동에 따른 ratchet 갱신(필요 시) |

UI/DB/API 계약 변경 0 → 디자인·DB팀 skip.

| 리스크 | 수준 | 완화 |
|---|---|---|
| 2 매핑 잔존 → ambiguous | 중 | 제거 후 clean verify(startup 실패로 즉시 검출) |
| @Transactional 누락 이동 → 부분커밋 회귀 | 높음 | 메서드 애노테이션 + rollback-only 동반 이동, 본문 바이트 동일 |
| pjt* 필드 잘못 제거(잔존부 사용) | 중 | grep 12사용 전부 plan 확인 후 제거 |
| 미사용/누락 import | 낮음 | 컴파일 + grep |

---

## 7. 승인 요청

본 기획서(S4 Phase 3)에 대한 codex 검토 및 사용자 최종승인을 요청합니다.
