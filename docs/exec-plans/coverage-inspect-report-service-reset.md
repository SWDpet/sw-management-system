# [개발계획] InspectReportService 잔여 경로 보강

- **작성팀**: 개발팀 / **작성일**: 2026-06-28 / **기획**: `docs/product-specs/coverage-inspect-report-service-reset.md`
- **상태**: ✅ 구현 완료(2026-06-28). A1~A5 green. InspectReportService 100%(miss 21→0, resetAllInspectData 포함), 전역 LINE 80.95%, floor 0.78/0.64 래칫. codex 구현검증 PASS·dual-review 합의 4 반영(3-arg 토큰·연도 정규식·hasSize·주석). 상세는 기획서 상태줄.

---

## 0. 사전검증 (완료)
- 생성자주입 9(7 repo+OpsDocLinkService+MessageResolver) 전부 기존 테스트가 mock 보유. `new InspectReportService(...)`.
- resetAllInspectData: `opsDocumentRepository.findByDocTypeOrderByCreatedAtDesc(OpsDocType.INSPECT)`→List, 각 `*Repository.count()`(long), `deleteAllInBatch()`(void)×5, `opsDocumentRepository.deleteAll(list)`(void). count map LinkedHashMap 순서=checkResult/visitLog/qrBatch/metricSnapshot/opsDocInspect/report.
- findById 체인: reportRepository.findById(id)(deletedAt=null)→fromEntity + visitLog/checkResult repo(mock 기본 empty) + pjtId&month present 시 findPreviousVisitsByProject(mock empty).
- save L81: update 모드(report.id!=null) + dto.checkResults 중 resultCode 비어있지 않은 항목 → incomingWithCode.add(section).
- currentUser: `SecurityContextHolder.getContext().getAuthentication().getName()`.

## 1. 테스트 매트릭스 (`InspectReportServiceTest` 확장)

| # | 테스트 | 픽스처 | 단언 |
|---|---|---|---|
| A1 | reset_capturesCountsThenDeletes | findByDocType(INSPECT)→[doc,doc](2건), count(): cr=10·vl=5·qr=3·ms=7·rep=2 | counts {checkResult=10,visitLog=5,qrBatch=3,metricSnapshot=7,opsDocInspect=2,report=2} + verify deleteAllInBatch×5 + deleteAll(list) + **InOrder(cr): count()→deleteAllInBatch()** |
| A2a | findByProjectAndMonth_found | repo.findByPjtIdAndInspectMonthAndDeletedAtIsNull(1,"2026-05")→Optional.of(report id=9, deletedAt=null), findById(9) 체인 mock | 반환 DTO.id==9 + verify findById 경유(reportRepository.findById(9)) |
| A2b | findByProjectAndMonth_notFound | repo...→Optional.empty() | null |
| A3 | save_update_incomingResultCode_notProtected | update report(id=5), 기존 checkResult(section "AP",resultCode "OK"=QR적재), **incoming checkResult(section "AP",resultCode "NEW")** | incomingWithCode 에 "AP" → toProtect 비어 protectedSections empty → checkResultRepository.deleteByReportId(5) 호출(보호 안 함) verify |
| A4 | save_new_usesAuthUsername | SecurityContextHolder 인증 "alice" 세팅, save(new report) | reportRepository.save 캡처 report.createdBy=="alice"·updatedBy=="alice"; finally clearContext |
| A5a | delete_notFound_throws | reportRepository.findById(7)→empty | IllegalArgumentException |
| A5b | delete_nullMonth_yearFallback | report(id=3, inspectMonth=null) | delete 성공 + opsDocumentRepository.findByDocNo("INSP-"+현재연도+"-3") 조회 verify(연도 now() fallback 경로) |

- A1: `when(checkResultRepository.count()).thenReturn(10L)` 등. `when(opsDocumentRepository.findByDocTypeOrderByCreatedAtDesc(OpsDocType.INSPECT)).thenReturn(List.of(new OpsDocument(), new OpsDocument()))`. InOrder = `inOrder(checkResultRepository); io.verify(...).count(); io.verify(...).deleteAllInBatch();`.
- A2a: report 에 setId(9)·setPjtId(1L)·setInspectMonth("2026-05")·deletedAt=null. `reportRepository.findById(9L)` 도 같은 report 반환(findById 체인). visitLog/checkResult mock 기본 empty.
- A3: 기존 save_update_qrProtection 패턴 차용하되 incoming 에 resultCode 부여. 보호 로직 결과로 분기 구분.
- A4: `UsernamePasswordAuthenticationToken("alice","x")` → `SecurityContextHolder.getContext().setAuthentication(...)`. ArgumentCaptor<InspectReport>. `try { ... } finally { SecurityContextHolder.clearContext(); }`.
- A5b: report.inspectMonth=null → `yyyy = String.valueOf(LocalDate.now().getYear())`. docNo 조회 verify(연도는 동적이므로 `startsWith("INSP-")` 또는 현재연도 계산값 사용).

## 2. 검증 절차
1. `./mvnw -o clean verify` → BUILD SUCCESS, 기존 + 신규(~7) green.
2. `jacoco.csv` 합산 → InspectReportService LINE 85.8%→~99%+(`.java.html` 재확인). 잔여=findById softDelete filter 등 방어 위주.
3. 전역 delta, floor 유지.
4. 구현 후 codex 검증 → dual-review → 듀얼푸시.

## 3. 리스크/완화
- **R1 reset reversal**: mock 이라 실삭제 0 — InOrder/verify 로 위험연산 박제(회귀 방어). 클래스 주석 "제외" → "mock 커버"로 갱신.
- **R2 A4 SecurityContext 누수**: finally clearContext 필수(다른 테스트 오염 방지).
- **R3 A5b 연도 동적**: LocalDate.now().getYear() 사용 → 테스트도 동일 계산으로 docNo 구성(하드코딩 금지).
- production 회귀 0.
