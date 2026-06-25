# [개발계획서] InspectReportService 커버리지 상향 (beyond-A)

- **작성팀**: 개발팀
- **작성일**: 2026-06-25
- **기획서**: `docs/product-specs/coverage-inspect-report-service.md` (codex APPROVE-WITH-FIX → 보완 반영, 사용자 최종승인 완료)
- **상태**: ✅ 완료 (2026-06-25). codex 기획/개발계획 APPROVE-WITH-FIX(보완 반영) + 구현검증. T-1/T-2 를 update 경로 1테스트로 통합 → 신규 4테스트(기존 9→13). InspectReportService LINE 56.1%→85.8%, 전역 47.89%→48.46%. floor 유지(0.45/0.38, 게인 +0.57pp 버퍼 흡수).

---

## 1. 작업 개요

`InspectReportServiceTest` 에 `save()` 미커버 분기 mock 단위테스트 추가. **프로덕션 코드 변경 0**, 테스트 1파일만 수정.

codex 보완 2건 반영:
- (a) 각 save 테스트에서 `SecurityContextHolder.clearContext()` 호출(currentUser "system" 폴백 고정 + suite 오염 방지).
- (b) QR 보호 경로: `checkResultRepository.findByReportIdOrderBySectionAscSortOrderAsc()` 가 save 내부에서 여러 번 호출됨 → Mockito 기본 stub(매 호출 동일 반환)으로 일관 유지, 단언은 ArgumentCaptor 로.

---

## 2. 추가 테스트 (T-n)

기존 9테스트 유지. 신규 추가:

> 구현 시 T-1/T-2 를 update 경로 1개 테스트로 통합 → **신규 4테스트**(기존 9 → 13). 분기 커버는 동일.

| ID | 테스트 | 검증 |
|----|--------|------|
| T-1+T-2 | `save_update_preservesAudit_deletesVisitLogs_noQrProtection` | id 있는 dto → 기존 createdBy/createdAt 보존, updatedBy="system", `visitLogRepository.deleteByReportId(id)` + 보호 없음 → `deleteByReportId(id)` 호출, `deleteByReportIdAndSectionIn` 미호출 |
| T-3 | `save_update_qrProtection_keepsProtectedAndSkipsIncoming` | 기존 resultCode 섹션(QR_SEC)+plain(PLAIN), incoming 은 QR_SEC(코드없음)+X → `deleteByReportIdAndSectionIn(id, [PLAIN])` 호출, `deleteByReportId` 미호출, incoming QR_SEC 는 `save` skip, X 만 저장 |
| T-4 | `save_savesVisits_withSortOrderAssigned` | visits [sortOrder null, sortOrder 0] → 저장 시 1,2 자동 배정(ArgumentCaptor) |
| T-5 | `save_completed_linksOpsDoc` | status COMPLETED → `opsDocLinkService.linkInspectReport(saved)` 호출 |

- 전 테스트: 최종 `return findById(reportId)` 때문에 `reportRepository.findById`·`visitLogRepository.findByReportIdOrderBySortOrderAsc`·`checkResultRepository.findByReportIdOrderBySectionAscSortOrderAsc` mock 필수.
- 기존 setUp 의 9개 mock 재사용(추가 mock 불요). 신규 import: `InspectCheckResult`/`InspectVisitLog`(domain), `InspectCheckResultDTO`/`InspectVisitLogDTO`(dto), `SecurityContextHolder`, matcher `anyLong`/`anyList`.

**codex plan 보완 2건 반영**:
- (a) T-3 `deleteByReportIdAndSectionIn` 섹션 단언은 `ArgumentCaptor<List<String>>` + `containsExactlyInAnyOrder`(소스가 HashSet+List.copyOf 라 순서 비결정).
- (b) save 테스트에서 최종 `findById` 가 반환하는 report 는 `pjtId=null`(또는 inspectMonth=null)로 두어 `findPreviousVisitsByProject` 호출 경로를 회피(불필요 stub 제거).

---

## 3. 구현 순서 (T)

| ID | 단계 |
|----|------|
| S-1 | `InspectReportServiceTest` 에 import + save 분기 테스트 추가(T-1/T-2 통합 → 신규 4). 각 save 테스트 앞에 `SecurityContextHolder.clearContext()`. |
| S-2 | `./mvnw test -Dtest=InspectReportServiceTest` → 전건 green(기존 9 + 신규 4 = 13). |
| S-3 | `./mvnw verify`(전체 + JaCoCo 게이트) green. |
| S-4 | JaCoCo csv 로 InspectReportService·전역 커버리지 상승 확인 → floor 상향 수치 판단(실측−2~2.5pp). |

---

## 4. 검증 기준 (NFR 매핑)

| NFR | 검증 |
|----|------|
| NFR-1 | S-2 InspectReportServiceTest green |
| NFR-2 | S-3 전체 test green |
| NFR-3 | S-3 게이트 + S-4 상승 |

---

## 5. 롤백

테스트 추가만이라 회귀 위험 극소. 문제 시 해당 테스트 Edit 되돌림. 프로덕션 영향 0.

---

## 6. 커밋 (작업완료 후)

- 메시지: `test(coverage): InspectReportService.save 분기 단위테스트 4 추가 (beyond-A)` (T-1/T-2 통합, floor 유지)
- 듀얼푸시(SWDpet + ukjin914), author `ukjin_park@jungdouit.com`. 기획서·개발계획서 ✅ 완료 갱신 동봉.

---

## 7. 승인 요청

본 개발계획서에 대한 codex 검토 및 사용자 최종승인을 요청합니다.
