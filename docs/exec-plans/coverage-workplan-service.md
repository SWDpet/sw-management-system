# 개발계획서 — WorkPlanService 캘린더/칸반 조회 위임 커버리지 보강 (B)

- **상태**: ✅ 구현 완료(2026-06-28). +4테스트 green, WorkPlanService 100% LINE. 구현 1차 codex NEEDS-FIX(same/isSameAs 과도)→eq/containsExactly 완화→PASS + dual-review(합의=production 백로그·분쟁3 refute). `mvnw -o clean verify` 1457 green.
- **작성일**: 2026-06-28
- **기획서**: `docs/product-specs/coverage-workplan-service.md` (codex APPROVE)
- **안전망**: 현 green 스위트 + JaCoCo ratchet/PIT 게이트. **production 변경 0 (test-only)**.
- **원칙**: 기존 `WorkPlanServiceTest` 확장. field-mock setUp 재사용. 커밋 1개.
- **codex hygiene**: start/end 는 **서로 다른 값**(인자순서 오류 관찰) · C3 `excludeStatuses` 는 named list 로 동일 인스턴스 verify.

---

## Step 0 — baseline 고정(완료)

- 전역 LINE 78.72%·INSTR 64.23%(floor 0.75/0.61). 대상 WorkPlanService miss 13.
- **확정**: `findByDateRange(LocalDate,LocalDate)`·`findByAssigneeAndDateRange(Long,LocalDate,LocalDate)`·`findByProcessStepAndStatusNotInOrderByStartDateAsc(Integer,List<String>)`·`findPreContactsByDate(LocalDate)`. C1~C3 은 `.map(WorkPlanDTO::fromEntity)`, C4 raw.

**검증:** 없음(준비).

## Step 1 — C1~C4 위임 테스트

`workPlan(id,title)` 로컬 헬퍼: `new WorkPlan()` + setPlanId/setTitle.
- **C1** `getWorkPlansByDateRange_delegatesAndMaps`: start=2026-05-01, end=2026-05-31(상이) → `when(findByDateRange(start,end)).thenReturn(List.of(workPlan(1,"A")))` → 반환 size 1·`get(0).getPlanId()`=1·title="A". `verify(findByDateRange(eq(start),eq(end)))`.
- **C2** `getWorkPlansByAssigneeAndDateRange_delegatesAndMaps`: assigneeId=9, start≠end → `findByAssigneeAndDateRange(9L,start,end)` → 매핑 + `verify(eq(9L),eq(start),eq(end))`.
- **C3** `getWorkPlansByProcessStep_delegatesAndMaps`: step=3, `List<String> exclude = List.of("DONE","CANCELED")` → `findByProcessStepAndStatusNotInOrderByStartDateAsc(3,exclude)` → 매핑 + `verify(eq(3),eq(exclude))`(동일 인스턴스).
- **C4** `getPreContactsByDate_returnsRawList`: date=2026-05-10 → `findPreContactsByDate(date)` → `List<WorkPlan> raw = List.of(wp)` → 반환 `isSameAs(raw)` 또는 동일 원소 + `verify(eq(date))`. (DTO 변환 없음.)

**검증:** `./mvnw -o -Dtest=WorkPlanServiceTest test` 전체 green.

## Step 2 — 전체 검증 + floor 검토

- `./mvnw -o clean verify` BUILD SUCCESS(1453 → +4).
- WorkPlanService miss 13 → 0(~100%) 확인(JaCoCo html).
- 전역 재측정 → 게인 작으면 floor 유지. ratchet·PIT 불변.

## Step 3 — codex 검증 + dual-review + 커밋 + 듀얼푸시

- codex 검증(위임·인자 verify).
- dual-review(codex+Opus) → 수렴.
- 커밋: `test(coverage): WorkPlanService 캘린더/칸반/알림 조회 위임 보강 (beyond-A)`.
- `git push origin master` (듀얼).

---

## 검증 매트릭스 (FR/NFR → Step)

| 항목 | 검증 Step |
|---|---|
| FR C1~C4 위임(인자/매핑) | Step 1 |
| NFR verify·miss 13→0 | Step 2 |
| NFR floor/ratchet/PIT 불변 | Step 2 |
| NFR production 0·듀얼푸시 | Step 3 |

## 롤백

- 단일 테스트 파일 케이스 추가 → 문제 시 제거. production 무영향.
