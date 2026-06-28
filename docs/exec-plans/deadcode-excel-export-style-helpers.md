# [개발계획] ExcelExportService dead 스타일헬퍼 제거 + facade 위임 커버리지

- **작성팀**: 개발팀 / **작성일**: 2026-06-28 / **기획**: `docs/product-specs/deadcode-excel-export-style-helpers.md`
- **상태**: ✅ 구현 완료(2026-06-28). dead 4 삭제+facade F1/F2 green. ExcelExportService 100%(miss 43→0), 전역 LINE 80.53%. codex 구현검증 PASS·dual-review 실결함0. 상세는 기획서 상태줄.

---

## 0. 사전검증 (완료)
- 호출처 grep: `createTitleStyle`/`createHeaderStyle`/`createBodyStyle`/`createTotalStyle` calls=0(dead). `setBorders` calls=6(dead 4 + **live generatePerformanceReport line106·125**) → setBorders 유지.
- facade 필드: `@Autowired DesignEstimateExcelService designEstimateExcelService`(32)·`InterimReportExcelService interimReportExcelService`(33). 위임 메서드 generateDesignEstimate(217)/generateInterimReport(225).
- 기존 ExcelExportServiceTest setUp = `new ExcelExportService()`(delegate 미주입) → facade 테스트는 ReflectionTestUtils 로 mock 주입 추가.

## 1. Part A — dead 제거 (production)
- `ExcelExportService.java` 의 `createTitleStyle(XSSFWorkbook,int)`·`createHeaderStyle(XSSFWorkbook)`·`createBodyStyle(XSSFWorkbook)`·`createTotalStyle(XSSFWorkbook)` 4 메서드(line 230~281) 삭제. `setBorders`(283~288) 및 주석 헤더 정리.
- 삭제 후 `mvnw compile` 로 unused import 검출 → 있으면 제거(없으면 무변경). 순수 dead 제거·live 무변경.

## 2. Part B — facade 위임 테스트 (테스트만)
`ExcelExportServiceTest` 에 2 테스트 추가(Mockito):
- import 추가: `org.mockito.*`(mock/when/verify), `org.springframework.test.util.ReflectionTestUtils`, 분리 서비스 2종.
- 헬퍼: mock 2종 생성 + `ReflectionTestUtils.setField(service, "designEstimateExcelService", mockDe)`·`"interimReportExcelService", mockIr)`(별도 헬퍼 or 각 테스트 내).

| # | 테스트 | 검증 |
|---|---|---|
| F1 | generateDesignEstimate 위임 | `when(mockDe.generateDesignEstimate(7)).thenReturn(bytes)` → `service.generateDesignEstimate(7)` 가 동일 byte[] 반환(isSameAs) + `verify(mockDe).generateDesignEstimate(7)` + `verifyNoInteractions(mockIr)`(설계↔기성 swap 회귀 방어) |
| F2 | generateInterimReport 위임 | `when(mockIr.generateInterimReport(9)).thenReturn(bytes)` → 동일 passthrough + `verify(mockIr).generateInterimReport(9)` + `verifyNoInteractions(mockDe)` |

- byte[] 픽스처 = `new byte[]{1,2,3}`. checked IOException 선언(위임 메서드 throws IOException) → 테스트 `throws IOException` 또는 `throws Exception`.

## 3. 검증 절차
1. `./mvnw -o clean verify` → BUILD SUCCESS, 기존 빌더 테스트(성과/목록/절사) green 유지(setBorders·live 무변경 확인), 신규 F1/F2 green.
2. `jacoco.csv` 점표기 합산 → ExcelExportService LINE 81.1%→~99%(분모 227→~186, miss 43→~0), 전역 delta 측정.
3. floor 유지(분모 축소로 비율 ↑, 게인 작아 버퍼 흡수 예상).
4. 구현 후 codex 검증 → dual-review → 듀얼푸시.

## 4. 리스크/완화
- **R1**: setBorders live 호출(generatePerformanceReport) 보존 — dead 4종만 삭제. 성과 리포트 테스트가 회귀 검출.
- **R2**: facade 위임은 분기 없음 → verify(인자 eq)+verifyNoInteractions(상대 delegate)로 swap/오배선 회귀 방어가 핵심 가치.
- production 회귀 0.
