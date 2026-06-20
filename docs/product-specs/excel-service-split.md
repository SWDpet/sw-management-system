# 기획서 — ExcelExportService 분리 (excel-service-split, §6-5)

- **상태**: v0.2 (codex 검토 ⚠수정필요 반영 — 사용자 최종승인 대기)
- **작성일**: 2026-06-20 (v0.1) / v0.2 동일자 (codex 반영: facade 유지·rounddown static 존치·StyleSupport static화·DocumentService/MessageResolver 주입·골든 해시 안전망)
- **스프린트명**: `excel-service-split`
- **요청자**: 박욱진 (사용자) — "전 항목 A / beyond-A" 품질 로드맵, §6-5 거대클래스 분리 잔여
- **선행**: giant-class-split(S4) 완료로 `DocumentController` 2183→1303줄(임계 1500 탈출). 본 건은 **거대클래스 래칫 baseline 의 마지막 잔존 항목** 처리.
- **성격**: **동작 보존 리팩토링** (기능·생성물 바이트·DB·화면·API 시그니처 변경 0). UI 키워드 없음 → 디자인팀 자문 대상 아님.

---

## 0. 한 줄 요약

거대클래스 래칫 baseline 에 **유일하게 남은** `ExcelExportService`(2,455줄 > 서비스 임계 2,000)를, 응집된 문서타입별 생성기로 쪼갠다. 공용 셀 스타일·원시 헬퍼를 신규 `ExcelStyleSupport`(@Component)로 추출하고, **설계견적서** family(~1,280줄)와 **중간/기성보고서** family(~770줄)를 각각 신규 서비스로 이동한다. **생성 엑셀 바이트·모든 호출 시그니처는 100% 그대로.**

## 1. 배경 / 목적

- 거대클래스 래칫(`GiantClassRatchetTest`, 서비스 임계 2,000줄)의 현재 baseline = `ExcelExportService.java=2455` **단 한 줄**. 이 한 건만 분리하면 거대클래스 부채가 완전 해소된다.
- `ExcelExportService` 는 서로 무관한 4개 문서 생성(성과보고서·설계견적서·중간/기성보고서·문서목록)이 한 클래스에 응축. 문서타입 하나를 고칠 때 2,455줄 전체가 변경 영향권 → 인지부하·회귀 위험.
- 책임을 문서타입별로 가르면 변경 영향 범위가 좁아진다. 단, **출력 엑셀과 외부 API 를 바꾸지 않는 순수 구조 개선**이어야 한다.

## 2. 범위 (In / Out)

**In**
- **A. `ExcelStyleSupport` 추출 (신규, static 유틸 클래스).** 클러스터 간 공유되는 무상태 헬퍼를 모은다. 후보(현 라인): `createTitleStyle`(2280)·`createHeaderStyle`(2292)·`createBodyStyle`(2307)·`createTotalStyle`(2318)·`setBorders`(2333)·`setCellValue`(2340)·`createDataCell`(2370)·`toDouble`(2346)·`toLong`(2354)·`str`(2362)·`safe`(2380)·`toInt`(2384) + 직접쓰기 헬퍼 `setStringDirect`(1186)·`setNumericDirect`(1199)·`applyFontSize`(1819). **개발계획에서 grep 실측으로 "2개 이상 클러스터가 쓰는 것만" 공용 확정**(단일 클러스터 전용은 해당 서비스로 동행).
  - ⚠ **@Component/@Autowired 가 아니라 `static` 메서드 유틸**로 한다(codex). 헬퍼는 무상태(Workbook/Sheet in → Style/값 out)라 static 이 자연스럽고, `new ExcelExportService()`(PerfStyleTest)·신규 서비스 모두 주입 없이 `ExcelStyleSupport.createHeaderStyle(wb)` 로 호출 → **주입 깨짐(NPE)·@Autowired 필드 증식 원천 차단.**
- **A2. rounddown static 3종 존치 (codex).** `normalizeRounddownUnit`(41)·`toRoundDigits`(56)·`roundLabel`(66) 은 package-private static 이고 `ExcelExportServiceRounddownTest` 가 `ExcelExportService.xxx` 로 직접 참조 + 설계/기성 양쪽이 사용(232·425·441·1055·1071). → **이번 스프린트는 `ExcelExportService` 에 그대로 둔다**(이동·삭제 안 함). 신규 서비스는 같은 패키지(`com.swmanager.system.service`)라 `ExcelExportService.toRoundDigits(...)` 로 package-private static 호출 가능. **테스트 무변경.**
- **B. `DesignEstimateExcelService` (@Service, 신규).** `generateDesignEstimate`(218) + 설계견적서 전용 빌더 일체: `generateFromTemplate`·`fillCoverSheet`·`fillGapjiSheet`·`fillSummarySheet`·`simplifyMaintFormula`·`setCellKeepStyle`(2종)·`clearDataRow`·`createCoverSheet`·`createSummaryCoverSheet`·`createSummaryTableSheet`·`createGradeSheet`·`generateFromTypeCTemplate`·`generateFromTypeBTemplate`·`fillTypeBItemRows`·`generateFromSwTemplate`·`replacePlaceholder`·`createTypeDCoverSheet_OLD`·`createTypeDGapjiSheet`·`createTypeDSummarySheet` (현 218~1503, ~1,280줄). 의존성: `DocumentService`·`MessageResolver` **생성자 주입**(raw Repository 불필요 — 데이터는 `documentService.getDocumentById(docId)` 유지).
- **C. `InterimReportExcelService` (@Service, 신규).** `generateInterimReport`(1504) + 중간/기성 전용 빌더: `populateHwInterimTemplate`·`setSumFormula`·`currentMonthLabel`·`trimPct`·`monthsFromPeriodText`·`buildPeriodText`·`createInterimCoverSheet`·`createInterimDetailSheet`·`createInterimSummarySheet`·`createInterimItemDetailSheet` (현 1504~2279, ~770줄). 의존성: `DocumentService`·`MessageResolver` 생성자 주입.
- **D. `ExcelExportService` 잔존 + facade (codex 핵심).** `generatePerformanceReport`(78, 인자만으로 동작·골든 보유)·`generateDocumentList`(2394) 는 로직 그대로 유지. **`generateDesignEstimate(Integer)`·`generateInterimReport(Integer)` 는 public 시그니처를 유지한 채 신규 서비스로 위임하는 얇은 wrapper 로 남긴다**(public API 보존 = FR-2). 분리 후 ~400줄(임계 1500/2000 모두 탈출). 실제 대형 로직이 빠지므로 거대클래스 목적 달성 + god-facade 아님.
- **E. 호출처 갱신 (선택·낮은 우선순위).** facade 유지로 `DocumentController`·`PerformanceController` 는 **무수정으로 동작**. 컨트롤러를 신규 서비스 직접 호출로 바꾸는 것은 결합 명료화를 위한 **선택사항**(이번 미적용 또는 별도 후속). **API 보존이 우선.**

**Out (이번 미포함)**
- `Map<String,Object>` 동적 조립 → DTO 전환(엑셀 빌더 다수가 `List<Map<String,Object>>` 입력): §6-4 Map 부채 스레드 소관, 본 건은 **건드리지 않음**.
- 엑셀 출력물의 서식·시트구성·계산식 변경 일절 없음. 신규 문서타입 추가 없음.
- `_OLD` 접미사 죽은코드(`createTypeDCoverSheet_OLD`) 제거: 동작보존 우선이라 **이번엔 그대로 이동만**(별도 데드코드 정리 건).

## 3. 개발자 스토리

> 유지보수자로서, 설계견적서 엑셀 양식을 고칠 때 중간보고서·성과보고서 코드가 섞인 2,455줄이 아니라 설계견적 전용 서비스만 보고, 변경이 다른 문서타입에 영향 없음을 확신하고 싶다.

## 4. 기능 요구사항 (FR)

| ID | 요구사항 |
|---|---|
| FR-1 (불변식·핵심) | 분리 후 4개 생성 메서드가 만드는 **엑셀 바이트(셀 값·서식·시트구성·계산식)가 100% 동일**. **검증(codex): 회사 PC 운영DB로 설계견적·기성 대표 docId 1건씩 리팩토링 前 생성→SHA256 기록, 각 Step 後 재생성→해시 동일 확인**(POI 비결정 출력 우려 시 workbook 셀값/스타일 구조 비교로 대체). 성과·문서목록은 기존 골든 테스트로 커버. |
| FR-2 (불변식·codex 핵심) | **`ExcelExportService` 의 기존 public 메서드 4종(generatePerformanceReport/generateDesignEstimate/generateInterimReport/generateDocumentList) 시그니처·반환형·예외(throws IOException) 모두 유지.** 설계/기성은 facade wrapper 로 유지(제거 금지). package-private static rounddown 3종도 `ExcelExportService` 에 유지. 컨트롤러의 URL·요청/응답·다운로드 파일명 그대로. |
| FR-3 | 공용 헬퍼는 정확히 "2개 이상 클러스터 사용"만 `ExcelStyleSupport` 로. 단일 클러스터 전용 헬퍼는 해당 서비스에 동행(잘못된 공용화로 인한 불필요 결합 방지). |
| FR-4 | 신규 서비스의 의존성(Repository/Service) 주입 정상. Spring 빈 등록·기동 정상. |
| FR-5 | `ExcelExportService` 잔존분(성과·문서목록)의 기존 골든 테스트(`ExcelExportServicePerfStyleTest`)·`ExcelExportServiceRounddownTest` **그대로 통과**. |

## 5. 설계

### 5-1. 분리 후 구조
```
ExcelStyleSupport (신규, static 유틸 — 주입 없음)   // 무상태 스타일·원시 헬퍼 (공용)
  ├─ static createTitleStyle/Header/Body/Total, setBorders, setCellValue, createDataCell
  └─ static toDouble/toLong/str/safe/toInt (+ 2클러스터+ 공유 확인된 것만)

DesignEstimateExcelService (@Service, 신규)   // generateDesignEstimate + 템플릿 family
  → 생성자 주입: DocumentService, MessageResolver
  → 호출: ExcelStyleSupport.xxx() (static), ExcelExportService.toRoundDigits() 등(같은 패키지 static)

InterimReportExcelService (@Service, 신규)    // generateInterimReport + 중간/기성 family
  → 생성자 주입: DocumentService, MessageResolver
  → 호출: ExcelStyleSupport.xxx() (static), ExcelExportService rounddown static

ExcelExportService (@Service, 잔존 + facade)  // 로직: generatePerformanceReport + generateDocumentList
  → @Autowired DocumentService/MessageResolver 유지(generateDocumentList 등 사용분)
  → static rounddown 3종 유지(테스트·신규서비스 참조)
  → generateDesignEstimate/Interim = 신규 서비스로 위임하는 public wrapper
     (이를 위해 두 신규 서비스를 ExcelExportService 에도 주입; perf/docList 경로는 미사용이라
      new ExcelExportService() 단위테스트는 해당 wrapper 만 호출 안 하면 NPE 없음 — PerfStyleTest 무변경)
```
> ⚠ facade wrapper 주입 주의(R-7): `ExcelExportService` 가 두 신규 서비스를 필드로 가지면 `new ExcelExportService()`(PerfStyleTest/RounddownTest) 에서 그 필드는 null. 단 두 테스트는 `generatePerformanceReport`/static rounddown 만 호출하고 wrapper(`generateDesignEstimate/Interim`)는 호출 안 하므로 NPE 없음. 개발계획에서 이 무호출 가정을 명시 검증.

### 5-2. 이동 원칙 — "순수 코드 이동"
- 설계견적·중간 생성기는 **DB(docId)·Repository 의존**이라 인자만으로 골든 캡처가 비싸다(DB 필요). 따라서 **메서드 본문을 바이트 단위 그대로 잘라 이동**(로직 1자 변경 없음) → 동작 보존을 *구성적으로* 보장(S4-a `InspectReportController` 이동과 동일 전략).
- `ExcelStyleSupport` 로 헬퍼를 옮길 때만 **호출 형태가 `this.foo()` → `styleSupport.foo()` 로 변경**된다. 이 부분이 유일한 코드 변형 → 헬퍼는 무상태(필드 미참조)임을 grep 으로 선검증해야 안전(R-2).

### 5-3. 호출처 (facade 채택 — codex 권고)
- **채택: facade 유지** → `DocumentController`·`PerformanceController` **무수정**. `excelExportService.generateDesignEstimate(id)`/`generateInterimReport(id)` 호출이 그대로 wrapper 를 타고 신규 서비스로 위임. public API 보존(FR-2).
- (선택·후속) 컨트롤러를 신규 서비스 직접 호출로 바꾸는 결합 명료화는 별도 건. 이번 미적용.

## 6. 비기능 요구사항 (NFR)

| ID | 요구사항 |
|---|---|
| NFR-1 (안전망) | 작업 전/후 `./mvnw test`(현 410 green, 42 skip) **동일 통과**. 회귀 0. |
| NFR-2 | `GiantClassRatchetTest` baseline 에서 `ExcelExportService` **제거**(2455→~400, 임계 미달). 신규 3클래스 모두 임계(2000) 이하. baseline 갱신(감소-only). |
| NFR-3 | `MapDebtRatchetTest`(397) **불변 또는 감소만**. 이동 과정에서 Map 사용 라인 증가 금지. |
| NFR-4 | JaCoCo 게이트(LINE≥18%/INSTRUCTION≥14%, com/swmanager) 유지 통과. |
| NFR-5 | commit 원자 단위(A→B→C→D 순, 각 커밋 후 빌드 green). |

## 7. 리스크 / 함정

| ID | 리스크 | 완화 |
|---|---|---|
| R-1 | 메서드 이동 중 본문 미세 변형 → 엑셀 출력 달라짐 | "바이트 동일 이동" 원칙. 로직 1자 수정 금지. 이동 후 `git diff` 로 본문 동일(들여쓰기·호출만 변동) 확인. |
| R-2 | `ExcelStyleSupport` 로 옮긴 헬퍼가 사실은 인스턴스 필드 참조(무상태 아님) → 컴파일/런타임 깨짐 | 추출 전 각 헬퍼 본문 grep: `this.`·필드 참조 0 확인. 참조 있으면 공용화 제외(해당 서비스 동행). |
| R-3 | 단일 클러스터 전용 헬퍼를 공용화 → 불필요 결합 | FR-3: grep 으로 사용 클러스터 수 실측, 2+ 만 공용. |
| R-4 | 신규 서비스 의존성 누락/과잉 주입 | 이동 메서드 본문 grep 으로 실제 사용 Repository/Service만 주입(S4-a 의 sigungu/sysMst 과잉 제거 교훈). |
| R-5 | facade wrapper 위임 누락/오타 → 다운로드 깨짐 | wrapper 본문은 단순 `return xxxService.generateXxx(docId);` 1줄. 기동 스모크로 설계/기성 다운로드 경로 실호출 확인. |
| R-6 | `_OLD` 죽은코드 함께 이동 시 혼란 | 이번엔 그대로 이동(동작보존). 데드코드 제거는 별도 건으로 분리(Out). |
| R-7 (codex) | facade 가 신규 2서비스를 필드로 가져 `new ExcelExportService()` 단위테스트에서 null → wrapper 호출 시 NPE | PerfStyleTest/RounddownTest 는 perf/static 만 호출(wrapper 미호출) → NPE 없음. 개발계획 Step 0 에서 두 테스트가 wrapper 미호출임을 grep 확정. 향후 wrapper 단위테스트가 필요하면 생성자 주입본으로 명시 생성. |
| R-8 (codex) | rounddown static 을 옮기면 `ExcelExportServiceRounddownTest` 의 `ExcelExportService.xxx` 참조 깨짐 | 이동하지 않음(A2). `ExcelExportService` 에 존치 → 테스트·신규서비스(같은 패키지) 모두 무변경 참조. |

## 8. 단계 (개발계획서에서 상세화)

1. **Step 0**: 헬퍼별 사용 클러스터 grep 실측(공용/전용 확정) + 각 생성기 의존성 인벤토리 + rounddown/테스트 무호출 가정 확정 + **회사 PC DB로 설계견적·기성 대표 docId 생성→SHA256 골든 해시 기록(FR-1 안전망)**.
2. **Step A**: `ExcelStyleSupport`(static 유틸) 생성 → 공용 헬퍼 이동 → ExcelExportService 에서 `ExcelStyleSupport.` static 호출로 치환. 빌드 green + perf 골든 통과.
3. **Step B**: `DesignEstimateExcelService` 생성(DocumentService/MessageResolver 생성자 주입) → 설계견적 family 이동 → ExcelExportService.generateDesignEstimate = 위임 wrapper. 빌드 green + 설계 해시 동일.
4. **Step C**: `InterimReportExcelService` 생성 → 중간/기성 family 이동 → generateInterimReport = 위임 wrapper. 빌드 green + 기성 해시 동일.
5. **Step D**: 래칫 baseline 갱신(ExcelExportService 제거, 신규 3클래스 임계 이하) + 기동 스모크(설계/기성/성과 다운로드 실호출).
6. **Step F**: codex 검증 → 커밋(A~D 분리) → 듀얼 푸시.

## 9. 완료 기준 (DoD)

- `ExcelExportService` 가 거대클래스 baseline 에서 제거(임계 미달), 신규 3클래스 모두 임계 이하.
- `./mvnw test` 410 green 유지 + 기존 엑셀 골든 통과 + JaCoCo/Map 부채 게이트 통과.
- public API 4종 시그니처 보존(facade), 설계/기성 SHA256 해시 리팩토링 前後 동일.
- 기동 스모크 정상(설계/기성/성과 엑셀 다운로드 경로 동작).
- codex 검토 통과 + 사용자 승인.

---

### codex 검토 라인 (workflow)
> 요청 → **기획서(본 문서)** → codex 검토 → 사용자 최종승인 → 개발계획서 → codex 검토 → 승인 → 구현 → codex 검증 → 작업완료.
