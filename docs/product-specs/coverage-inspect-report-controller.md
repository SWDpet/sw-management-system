# [기획서] InspectReportController 예외경로·데이터분기 커버리지 보강 (B)

- **작성팀**: 기획팀 / **작성일**: 2026-06-28
- **트랙**: beyond-A 커버리지 증분 B. 점검내역서 컨트롤러. 기존 `InspectReportControllerTest` 확장형.
- **상태**: ✅ 구현 완료(2026-06-28). InspectReportControllerTest +9(31→40) → InspectReportController **84.7→98.4% LINE**(186/189). 전역 LINE 78.11→78.34%·INSTR 63.84→63.98%, floor 유지. `mvnw -o clean verify` 1433 green. codex 기획/개발계획 APPROVE-WITH-FIX·구현 **1차 NEEDS-FIX(assertFailMessage 약함)→error 메시지 단언+뷰명 단언 보강→PASS**. dual-review(codex2/Opus5) 합의3 반영(explicit setServers·body null체크·instanceof 후 cast), 분쟁4 미반영(읽기전용 visits·UTF-8 인코딩 계약). 잔여 3줄=getCurrentUser/isAdmin 방어 catch 의도적 제외.
- **codex 보완**: ① C8 Content-Disposition filename 은 URL 인코딩(`filename*=UTF-8''…`) → `_5월` 리터럴 대신 **인코딩값 `_5%EC%9B%94`** 단언(또는 디코드 후). ② C7 `InfraServerRow.from(s)` 가 `s.getSoftwares().stream()` 호출 → **InfraServer.softwares 비-null 필수**(실 `new InfraServer()` 기본 `new ArrayList<>()` 사용, mock 시 `getSoftwares()`→`List.of()`).

---

## 1. 배경 / 목표

`InspectReportController`(437줄, **LINE 84.7% / miss 29**)는 세션12(`878420a`)가 저장/조회/가드/스냅샷 happy-path 를 덮었으나, **조회 API 의 `catch(Exception)→failMessage` 예외경로**와 일부 데이터/뷰 분기가 미커버다. 예외경로는 서비스 mock 을 throw 로 스텁해 박제하고, 데이터 분기는 실데이터 1건으로 커버한다.

JaCoCo 미커버 라인(`InspectReportController.java.html` nc): 61-62, 69, 117-119, 133-135, 145-147, 161-163, 182-185, 197-199, 222-225, 307, 379-381.

## 2. 대상 분기

| # | 영역(라인) | 내용 |
|---|---|---|
| C1 | listInspectReports catch(117-119) | `findByProject` throw → `ApiResult.failMessage` (200, success=false) |
| C2 | findInspectReport catch(133-135) | `findByProjectAndMonth` throw → failMessage |
| C3 | getPreviousVisits catch(145-147) | `getPreviousVisits` throw → failMessage |
| C4 | getInspectTemplate catch(197-199) | `getTemplateItems` throw → failMessage |
| C5 | deleteInspectReport catch(161-163) | admin + `delete` throw → failMessage (가드 통과 후 예외) |
| C6 | resetAllInspect catch(182-185) | admin + `resetAllInspectData` throw → result{success:false, error} map |
| C7 | getInfraServers 데이터(222-225) | VIEW + infraList 비어있지 않음 → `InfraServerRow.from` 매핑 응답 |
| C8 | downloadInspectPdf visits(307) | EDIT + report.visits 비어있지 않음 → monthSuffix=`_{visitMonth}월` (기존은 inspectMonth 분기만) |
| C9 | inspectDetail project-infra(379-381) | project != null → infraRepository 조회 + model `infraList` 세팅 |

**의도적 제외**: `getCurrentUser` 비-CustomUserDetails return null(61)·catch(62)·`isAdmin` catch(69) — 방어적/niche(throwing Authentication 주입 필요), ROI 낮아 잔여 허용(정직성 명시).

## 3. 변경 — `InspectReportControllerTest` 케이스 추가 (테스트만)
- 기존 7-mock 생성자주입 setUp·login 헬퍼(loginEdit/View/Admin)·model() 재사용. 신규 ~9 테스트.
- **C1~C4**(무가드 조회): `when(service.xxx(...)).thenThrow(new RuntimeException("boom"))` → 호출 → status 200 + body `ApiResult` success=false(failMessage). (가드 없는 try/catch 엔드포인트.)
- **C5**: loginAdmin + `doThrow(...).when(inspectReportService).delete(id)` → 200 failMessage. **(가드 통과 후 예외경로 — 403 아님 확인.)**
- **C6**: loginAdmin + `resetAllInspectData` throw → 200 + result map success=false·error 키. logService.log 미호출 verify.
- **C7**: loginView + `infraRepository.findByDistNmAndSysNmEn` → Infra(서버 1건) → 응답 List<InfraServerRow> size 1 + 화이트리스트 필드(serverId/hostName/ipAddr 등) 단언. (NONE 403·빈 리스트는 기존 커버.)
- **C8**: loginEdit + `inspectPdfService.generatePdf` → bytes + `findById` → report(visits 1건, visitMonth="5") → Content-Disposition filename 에 `_5월` 포함 단언.
- **C9**: inspectDetail — `findById`→report(pjtId) + `swProjectRepository.findById`→project(distNm/sysNmEn) + `infraRepository.findByDistNmAndSysNmEn`→infraList → model `project`·`infraList` 세팅 단언. (project=null 분기는 기존 커버 가정.)

## 4. 요건
- **FR**: C1~C9 분기 박제(예외경로 failMessage·데이터 매핑·가드통과후 예외). 에러 핸들링·응답 매핑 회귀 방어.
- **NFR**: production 변경 0, `mvnw -o clean verify` SUCCESS, InspectReportController 84.7→~96% LINE, JaCoCo floor 유지, 구현 후 codex PASS → 듀얼푸시.

## 5. 영향/리스크
- `controller/InspectReportControllerTest.java` 케이스 추가. production 0.
- 리스크: C8 visits DTO(`InspectVisitDTO.getVisitMonth`)·C7 Infra/Server 엔티티·InfraServerRow 필드명 구현 시 소스 대조. C9 inspectDetail 의 추가 model 의존(checkResults 집계 등) NPE 회피 위해 report 필드 충분히 채움.
- getCurrentUser/isAdmin 방어 catch 3줄 잔여(의도적).
