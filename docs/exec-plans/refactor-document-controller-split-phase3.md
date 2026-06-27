# [개발계획서] DocumentController 분리 — S4 Phase 3 (plan)

- **작성팀**: 개발팀
- **작성일**: 2026-06-27
- **기획서**: `docs/product-specs/refactor-document-controller-split-phase3.md` (codex APPROVE-WITH-FIX 보완 + 사용자 최종승인).
- **상태**: ✅ **완료(2026-06-27)**. S-1~S-5 수행. DocumentController 799→661·DocumentPlanController(plan 2+asInt/asBool, pjt* repo 3 동반). 3 이관+success 신규. ratchet 297→298(GOLDEN_RECORD). clean verify green.

## 후속 백로그 (dual-review — pre-existing, 순수이동서 미변경)
- savePlanData rollback 예외경로 = MockMvc/통합 테스트 필요(직접호출 단위테스트 불가)
- getPlanData 무가드(원본부터) — plan 데이터 읽기 권한 정책 결정 필요(viewer-guard 연계)
- savePlanData unchecked cast(malformed JSON)→200 success:false (400 검증 부재)

---

## 1. 작업 개요

plan 2 엔드포인트 + asInt/asBool 을 신규 `DocumentPlanController` 로 이동, pjt* 전용 repo 3개 필드 제거. 순수 이동 + codex 보완 success 테스트 1건. `DocumentAccessSupport` 재사용.

---

## 2. 구현 순서 (S-n)

### S-1 DocumentPlanController 신설
- `controller/DocumentPlanController.java` (@Controller @RequestMapping("/document"), @Slf4j, 필드주입).
- 주입: SwProjectRepository, PjtTargetRepository, PjtManpowerPlanRepository, PjtScheduleRepository, DocumentAccessSupport access.
- **이동**(본문 불변): getPlanData(GET /api/plan/{projId}), savePlanData(POST, **@Transactional 유지**) + private asInt/asBool.
- 본문 `getAuth()` → `access.getAuth()`. PjtTarget/PjtManpowerPlan/PjtSchedule 는 FQN 유지(import 없음).
- import: PlanData·PlanTargetRow·PlanManpowerRow·PlanScheduleRow·repo 4·DocumentAccessSupport·ApiResult·@Transactional·TransactionAspectSupport(FQN)·MVC/HTTP·java.util·@Slf4j.

### S-2 DocumentController 정리
- getPlanData/savePlanData/asInt/asBool 제거.
- **pjtTargetRepository·pjtManpowerPlanRepository·pjtScheduleRepository 필드 제거**(grep 12사용 전부 plan 재확인).
- import 제거: PlanData·PlanTargetRow·PlanManpowerRow·PlanScheduleRow(이동으로 미사용 — grep 확인). swProjectRepository 잔존.

### S-3 테스트 이관
- `DocumentPlanControllerTest.java` 신설: getPlanData_notFound/getPlanData_found/savePlanData_nonEdit_forbidden (3) 이관 + injectPjtRepos 로직 반영. 셋업=필드주입 reflection + login 헬퍼 + `new DocumentAccessSupport()`.
- **codex: savePlanData_success 신규** — loginEdit + swProjectRepository.findById→SwProject + body(projPurpose + targets/manpowerPlans/schedules 각 1행) → 응답 ApiResult(success=true) + pjtTargetRepository/pjtManpowerPlanRepository/pjtScheduleRepository 각 deleteByProjId·save 호출 verify + swProjectRepository.save verify. ⚠happy path(catch 미도달)이라 @Transactional 직접호출 무관.
- DocumentControllerTest: plan 3 케이스 + injectPjtRepos 헬퍼 + 미사용 pjt* mock/필드/import 제거. 잔존 불변.

### S-4 검증
- `./mvnw -o clean verify` — compile + 전체 테스트 + @SpringBootTest 부팅(ambiguous mapping 검출) + JaCoCo 게이트 green.
- ControllerRepositoryRatchet 초과 시 GOLDEN_RECORD=1 갱신(pjt* relocate net0 + swProject +1 예상).
- DocumentController LOC 확인(약 670 목표).

### S-5 (작업완료)
- dual-review → 합의 반영 → 커밋 + 듀얼푸시.

---

## 3. 검증 기준 (NFR 매핑)

| NFR | 검증 |
|----|------|
| NFR-1 | S-4 compile + clean verify |
| NFR-2 | S-3 이관 3 동일 단언 green |
| NFR-2c | S-3 savePlanData_success 신규 green |
| NFR-2b | S-4 @SpringBootTest 부팅(매핑 비충돌) |
| NFR-3 | S-4 JaCoCo 게이트(코드 이동→비감소) |
| NFR-4 | S-4 DocumentController LOC + 필드 3 감소 |
| NFR-4b | S-4 ratchet GOLDEN_RECORD(필요 시) |
| NFR-5 | S-5 dual-review + 듀얼푸시 |

---

## 4. 리스크 / 롤백

| 리스크 | 완화 |
|---|---|
| 2 매핑 잔존 → ambiguous | S-2 제거 후 clean verify(startup 실패) |
| @Transactional 누락 → 부분커밋 회귀 | 메서드 애노테이션 + rollback-only 동반 이동, 바이트 동일 |
| pjt* 필드 오제거 | grep 12사용 전부 plan 확인 |
| import 누락/잔존 | 컴파일 + grep |

롤백: 단일 커밋 `git revert`. 순수 구조 이동(+테스트 1건)이라 프로덕션 의미 변화 0.

---

## 5. 커밋

- `refactor(document): DocumentController 사업수행계획서 plan 분리 — S4 Phase 3`.
- 듀얼푸시(SWDpet + ukjin914), author `ukjin_park@jungdouit.com`. 기획/개발계획 ✅ 완료 갱신 동봉.

---

## 6. 승인 요청

본 개발계획서에 대한 codex 검토 및 사용자 최종승인을 요청합니다.
