# 개발계획서 — InspectReportController 예외경로·데이터분기 커버리지 보강 (B)

- **상태**: ✅ 구현 완료(2026-06-28). +9테스트 green, InspectReportController 84.7→98.4% LINE. 구현 1차 codex NEEDS-FIX(assertFailMessage 약함)→error 메시지+뷰명 단언 보강→PASS + dual-review 합의3 반영. `mvnw -o clean verify` 1433 green. ⚠logService.log 오버로드 모호성→`any(String.class),any(AccessActionType.class),any(String.class)` 명시.
- **작성일**: 2026-06-28
- **기획서**: `docs/product-specs/coverage-inspect-report-controller.md` (codex APPROVE-WITH-FIX)
- **안전망**: 현 green 스위트 + JaCoCo ratchet/PIT 게이트. **production 변경 0 (test-only)**.
- **원칙**: 기존 `InspectReportControllerTest`(29) 확장. 7-mock 생성자주입·login 헬퍼 재사용. 커밋 1개.

---

## Step 0 — baseline 고정 + 사실확인(완료)

- 전역 LINE 78.11%·INSTR 63.84%(floor 0.75/0.61). 대상 InspectReportController LINE 84.7%(miss 29).
- **확정 사실**:
  - `ApiResult(Boolean success, Object data, Object error)` record — `body.success()` Boolean. failMessage → success=false.
  - `InfraServer` 기본 `softwares = new ArrayList<>()`(비-null) → `InfraServerRow.from` 의 `getSoftwares().stream()` 안전. `Infra` 기본 `servers = new ArrayList<>()` → `infra.getServers().add(server)`.
  - `InfraServerRow(serverId, serverType, hostName, ipAddr, osNm, ...note, softwares)`. `InspectReportDTO` @Getter/@Setter(setVisits/setCheckResults/setPjtId/setInspectMonth/setDocTitle). `InspectVisitLogDTO.setVisitMonth(String)`. `SwProject` distNm/sysNmEn.
  - 조회 4종(list/find/previousVisits/template) **무가드 try/catch**. delete/reset **isAdmin 가드**. getInfraServers **getAuth NONE 차단**. downloadInspectPdf **EDIT 가드**.

**검증:** 없음(준비).

## Step 1 — C1~C4: 무가드 조회 API catch(예외경로)

각 1 테스트: `when(inspectReportService.<m>(...)).thenThrow(new RuntimeException("boom"))` → 엔드포인트 호출 → status 200 + `((ApiResult)res.getBody()).success()` isFalse.
- C1 `listInspectReports_serviceThrows_failMessage`: `findByProject(7L)` throw → `controller.listInspectReports(7L)`.
- C2 `findInspectReport_serviceThrows_failMessage`: `findByProjectAndMonth(7L,"2026-05")` throw.
- C3 `getPreviousVisits_serviceThrows_failMessage`: `getPreviousVisits(7L,"2026-05")` throw.
- C4 `getInspectTemplate_serviceThrows_failMessage`: `getTemplateItems("KRAS")` throw.

**검증:** `./mvnw -o -Dtest=InspectReportControllerTest test` green(부분).

## Step 2 — C5/C6: 관리자 가드통과 후 예외

- C5 `deleteInspectReport_adminServiceThrows_failMessage`: `loginAdmin()` + `doThrow(new RuntimeException()).when(inspectReportService).delete(9L)` → 200 + ApiResult success false. (가드 통과 확인 — 403 아님.)
- C6 `resetAllInspect_adminServiceThrows_errorMap`: `loginAdmin()` + `when(resetAllInspectData()).thenThrow(...)` → 200 + `(Map)res.getBody()` success=false·containsKey("error"). `verify(logService, never()).log(...)`.

## Step 3 — C7: getInfraServers 데이터 경로

- `getInfraServers_withData_mapsRows`: `loginView()` + `infraRepository.findByDistNmAndSysNmEn("양양군","UPIS")` → Infra(서버 1건: `new InfraServer()` serverId=10/hostName="h1"/ipAddr="10.0.0.1", softwares 기본 빈리스트) → `controller.getInfraServers("양양군","UPIS")` → body `List<InfraServerRow>` size 1 + `serverId`=10·`hostName`="h1"·`ipAddr`="10.0.0.1" 단언.

## Step 4 — C8: downloadInspectPdf visits 월suffix

- `downloadInspectPdf_withVisits_monthSuffixFromVisit`: `loginEdit()` + `inspectPdfService.generatePdf(5L)` → byte[]{1,2,3} + `inspectReportService.findById(5L)` → InspectReportDTO(visits=[InspectVisitLogDTO(visitMonth="5")], docTitle="점검내역서") → Content-Disposition 헤더에 **URL 인코딩된 `_5%EC%9B%94`**(=`_5월`) 포함 단언(codex 보완).

## Step 5 — C9: inspectDetail project-infra 분기

- `inspectDetail_withProject_setsInfraList`: `inspectReportService.findById(3L)` → report(pjtId=70L, checkResults=null) + `swProjectRepository.findById(70L)` → SwProject(distNm="양양군", sysNmEn="UPIS") + `infraRepository.findByDistNmAndSysNmEn("양양군","UPIS")` → [Infra] → `controller.inspectDetail(3L, model())` → model `project` non-null·`infraList` size 1 단언. (checkResults null → 집계 NPE 회피.)

**검증:** `./mvnw -o -Dtest=InspectReportControllerTest test` 전체 green.

## Step 6 — 전체 검증 + floor 검토

- `./mvnw -o clean verify` BUILD SUCCESS(1424 → +9).
- InspectReportController LINE 84.7 → **~96%** 확인(JaCoCo html; 잔여=getCurrentUser/isAdmin 방어 catch 3줄).
- 전역 재측정 → 게인 작으면 floor 유지. ratchet·PIT 불변.

## Step 7 — codex 검증 + dual-review + 커밋 + 듀얼푸시

- codex 검증(분기·brittle·가드).
- dual-review(codex+Opus) → 수렴.
- 커밋: `test(coverage): InspectReportController 예외경로·데이터분기 보강 (beyond-A)`.
- `git push origin master` (듀얼).

---

## 검증 매트릭스 (FR/NFR → Step)

| 항목 | 검증 Step |
|---|---|
| FR C1~C4 무가드 조회 catch | Step 1 |
| FR C5/C6 가드통과 후 예외 | Step 2 |
| FR C7 getInfraServers 데이터 | Step 3 |
| FR C8 downloadPdf visits suffix | Step 4 |
| FR C9 inspectDetail project-infra | Step 5 |
| NFR verify·84.7→~96% | Step 6 |
| NFR floor/ratchet/PIT 불변 | Step 6 |
| NFR production 0·듀얼푸시 | Step 7 |

## 롤백

- 단일 테스트 파일 케이스 추가 → 문제 시 제거. production 무영향.
