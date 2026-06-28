# [기획서] ExcelExportService dead 스타일헬퍼 제거 + facade 위임 커버리지 (beyond-A)

- **작성팀**: 기획팀 / **작성일**: 2026-06-28
- **트랙**: beyond-A 커버리지 증분. §6-5 split 잔재 dead code 제거(무손실 헌장) + facade 위임 2메서드 박제. DesignEstimate/InterimReport Excel dead-removal 자매 스프린트.
- **상태**: ✅ 구현 완료(2026-06-28). Part A(dead 4종 삭제, 분모 227→186)+Part B(facade 위임 2테스트). **ExcelExportService LINE 81.1→100%(186/186, miss 43→0)·INSTR 99.6%, 전역 LINE 80.24→80.53%·INSTR 65.61→65.71%.** floor 0.77/0.63 유지(게인 작아 버퍼 흡수). `mvnw -o clean verify` 1476 green. codex 기획·구현검증 PASS. dual-review(codex1/Opus5) 합의2 전건 오탐(diff-blindness: "삭제 컴파일깸"·"필드명 불일치"→build green+grep callers0 가 반증), 분쟁4 codex 정확 refute(private삭제 안전·setBorders live·JUnit per-method 인스턴스·@Autowired non-final). 실결함 0.

---

## 1. 배경 / 목표

`ExcelExportService`(LINE 184/227 = 81.1%, miss **43**)의 미커버는 외부의존이 아니라 **§6-5 split(설계/기성 Excel 을 DesignEstimateExcelService·InterimReportExcelService 로 분리) 후 남은 미사용 private 스타일 헬퍼 4종**이다. split 이후 `generatePerformanceReport`/`generateDocumentList` 는 각자 **인라인 스타일**을 만들고, `generateDesignEstimate`/`generateInterimReport` 는 분리 서비스로 **facade 위임**만 한다 → 4개 헬퍼는 호출처 0.

JaCoCo 미커버 정체(`ExcelExportService.html`):

| 메서드 | LINE miss | 정체 |
|---|---|---|
| `createHeaderStyle(XSSFWorkbook)` | 12 | **호출처 0 dead**(인라인 스타일로 대체됨) |
| `createTotalStyle(XSSFWorkbook)` | 12 | **호출처 0 dead** |
| `createTitleStyle(XSSFWorkbook,int)` | 9 | **호출처 0 dead** |
| `createBodyStyle(XSSFWorkbook)` | 8 | **호출처 0 dead** |
| `generateDesignEstimate(Integer)` | 1 | facade 위임(미커버) |
| `generateInterimReport(Integer)` | 1 | facade 위임(미커버) |

> 호출처 검증(grep): `createTitleStyle`/`createHeaderStyle`/`createBodyStyle`/`createTotalStyle` = **calls 0**. `setBorders` 는 calls 6(그 중 4개가 dead 헬퍼, **2개는 live `generatePerformanceReport`** line 106/125) → **setBorders 는 live, 유지.**

목표: dead 헬퍼 4종 삭제(분모 ~41줄 축소·무손실), facade 위임 2메서드는 mock 위임 박제로 커버.

## 2. 범위

### Part A — dead 스타일헬퍼 제거 (production, 무손실)
- `createTitleStyle`/`createHeaderStyle`/`createBodyStyle`/`createTotalStyle` 4 private 메서드 삭제(호출처 0). **`setBorders` 는 유지**(generatePerformanceReport 가 2회 호출).
- 삭제로 unused 가 되는 import 가 있으면 함께 정리(없으면 무변경 — Font/HorizontalAlignment/VerticalAlignment/IndexedColors/FillPatternType 는 generatePerformanceReport/generateDocumentList 가 계속 사용). live 경로(성과/목록/setBorders) 무변경 — 빌드 컴파일 + 기존 빌더 테스트 green 으로 회귀 확인.

### Part B — facade 위임 커버리지 (테스트만)
- `ExcelExportServiceTest` 에 facade 위임 2테스트 추가. `new ExcelExportService()` + `designEstimateExcelService`/`interimReportExcelService` mock 을 `ReflectionTestUtils` 주입.
- `generateDesignEstimate(docId)` → `designEstimateExcelService.generateDesignEstimate(docId)` 호출 + 반환 passthrough(동일 byte[]) + 인자 eq verify.
- `generateInterimReport(docId)` → `interimReportExcelService.generateInterimReport(docId)` 동일.

## 3. 요건
- **FR-1**(Part A): dead 4종 삭제, setBorders·live 경로 무변경.
- **FR-2**(Part B): facade 위임 2메서드 박제(delegate 호출·인자·반환 passthrough). 잘못된 위임/인자 회귀 방어.
- **NFR**: `mvnw -o clean verify` SUCCESS, ExcelExportService LINE 81.1%→**~99%**(분모 축소+위임 커버), 전역 LINE/INSTR 유지/소폭 상향, JaCoCo floor 유지(분모 축소라 비율 ↑), 구현 후 codex PASS + dual-review → 듀얼푸시.

## 4. 영향 / 리스크
- 변경: `service/ExcelExportService.java`(dead 4 삭제), `service/ExcelExportServiceTest.java`(+2 위임 테스트).
- **R1 setBorders 오삭제**: setBorders 는 live(generatePerformanceReport line106/125) → 삭제 금지. dead 4종만 제거.
- **R2 import 회귀**: dead 헬퍼만 쓰던 import 가 있으면 unused → 정리. live 가 공유하면 유지. 컴파일로 검출.
- **R3 facade 위임 가치**: 위임은 분기 없으나 잘못된 delegate/인자(설계↔기성 swap 등) 회귀를 verify 로 방어. 기존 테스트 주석("facade 는 분리 서비스서 검증")은 **분리 서비스 내부 로직** 얘기 — facade **배선** 자체는 미박제였음.
- production 동작 회귀 0(Part A 미사용 삭제, Part B 테스트 추가).
