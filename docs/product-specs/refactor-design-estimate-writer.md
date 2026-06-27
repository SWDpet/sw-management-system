# [기획서] DesignEstimateExcelService TYPE별 생성 로직 분리 — DesignEstimateWriter (S4 거대클래스 #4)

- **작성팀**: 기획팀
- **작성일**: 2026-06-27
- **트랙**: beyond-A S4 거대클래스 분리. #3(InspectQrMetricSupport) 후속.
- **상태**: ✅ **완료(2026-06-27)**. codex 실질검증 + 사용자 승인 + 구현완료. DesignEstimateWriter 분리, DesignEstimateExcelService 522→87. 개발계획서 참조.

---

## 1. 배경 / 목표

### 1-1 ROI 정직성 (중요)
거대클래스 게이트 임계는 **서비스 2,000줄**인데 `DesignEstimateExcelService`(522줄)는 한참 아래·**부채 0**. TYPE_A/B/C/D로 이미 잘 구조화됨. **본 분리는 게이트 해소가 아니라 예방적 응집도 개선이며, ROI 는 #3보다 낮다**(POI 비즈니스 로직이라 직접 단위테스트 가치 제한적, 골든 의존). 사용자가 ROI 인지 후 진행 결정(2026-06-27).

### 1-2 대상 분석
`DesignEstimateExcelService`(522줄) = ① `generateDesignEstimate`(진입점: 문서조회 + design_estimate 섹션 파싱 + estimateType dispatch, **documentService/messages 필드 의존**) + ② **TYPE별 순수 생성기**(인자 기반 POI 조작, 필드 의존 0).

| 메서드 | 성격 | 필드 의존 |
|---|---|---|
| `generateDesignEstimate` | 진입점·데이터조회·dispatch | **있음**(documentService.getDocumentById, messages.get) |
| `generateFromTemplate`(TYPE_A) + `fillCoverSheet`·`fillGapjiSheet`·`fillSummarySheet`·`simplifyMaintFormula`·`setCellKeepStyle`(×2)·`clearDataRow` | 순수 POI | 0 |
| `generateFromTypeBTemplate`(TYPE_B) + `fillTypeBItemRows` | 순수 POI | 0 |
| `generateFromTypeCTemplate`(TYPE_C) | 순수 POI | 0 |
| `generateFromSwTemplate`(TYPE_D) | 순수 POI | 0 |

**확인(grep)**: documentService/messages 는 `generateDesignEstimate`(L52/L59)에서만 사용. TYPE별 생성기·헬퍼는 전부 필드 미참조(인자 기반). 외부 진입점은 `generateDesignEstimate`(public)뿐(ExcelExportService facade 위임 → 컨트롤러). 내부 generateFromX 는 전부 private. ⚠`generateFromTemplate`의 `doc` 파라미터는 **본문 미사용(dead param)**.

### 1-3 목표
TYPE별 순수 생성기 + 헬퍼를 신규 `DesignEstimateWriter`(package-private static, service 패키지)로 이동. `DesignEstimateExcelService`는 데이터조회·파싱·dispatch만 잔류(documentService/messages 의존 유지). **동작 100% 동일(순수 이동)**, 522 → 약 75줄. ExcelStyleSupport(§6-5 A) 선례와 동일 static util 패턴.

비목표: generateDesignEstimate 의 데이터 파싱/dispatch 로직 변경, 진입점 시그니처 변경, TYPE 생성 규칙 변경.

---

## 2. 변경 설계

### 2-A 신규 `DesignEstimateWriter` (final, private ctor)
- `com.swmanager.system.service.DesignEstimateWriter` (ExcelStyleSupport 동일 패키지).
- 이동(본문 바이트 동일), 전부 **package-private static**:
  - TYPE_A: `generateFromTemplate`·`fillCoverSheet`·`fillGapjiSheet`·`fillSummarySheet`·`simplifyMaintFormula`·`setCellKeepStyle`(String/double 2 오버로드)·`clearDataRow`
  - TYPE_B: `generateFromTypeBTemplate`·`fillTypeBItemRows`
  - TYPE_C: `generateFromTypeCTemplate`
  - TYPE_D: `generateFromSwTemplate`
- static import(ExcelStyleSupport.str/toDouble/toLong/setStringDirect/setNumericDirect, ExcelExportService.normalizeRounddownUnit/toRoundDigits/roundLabel) 이관. POI import 이관.

### 2-B DesignEstimateExcelService 정리
- TYPE별 생성기·헬퍼 정의 제거. `generateDesignEstimate`만 잔류(생성자·필드 포함).
- dispatch 호출을 `DesignEstimateWriter.generateFromTemplate(...)` / `generateFromTypeBTemplate(...)` / `generateFromTypeCTemplate(...)` / `generateFromSwTemplate(...)` 로 치환.
- 결과: 522 → 약 75줄(생성자 + generateDesignEstimate).
- ⚠**`generateFromTemplate` dead param `doc` 처리**: 순수 이동이면 보존, 다만 미사용이라 **제거 권장**(호출부 generateDesignEstimate L100 인자도 제거) — codex 의견. (별 위험 0, 정리 효과.)

> 순수 이동: 각 TYPE xlsx 산출 바이트 동일. generateDesignEstimate 진입점 동작 불변.

---

## 3. 기능 요건 (FR)

| ID | 내용 |
|----|------|
| FR-1 | TYPE_A/B/C/D 생성 결과 xlsx 가 이동 전과 동일(순수 이동). |
| FR-2 | `generateDesignEstimate` 진입점(public)·ExcelExportService facade·컨트롤러 다운로드 동작 불변. |
| FR-3 | 신규 생성 로직·POI 규칙 변경 0. |

---

## 4. 비기능 요건 (NFR)

| ID | 내용 |
|----|------|
| NFR-1 | compile + `./mvnw -o clean verify` BUILD SUCCESS. |
| NFR-2 | 기존 `DesignEstimateExcelServiceTest` 통과(이동 후 호출 대상만 변경, 단언 불변). ⚠운영DB design_estimate 0건이라 골든은 바이트동일 이동으로 보증(메모리 [[project_swmanager_all_A_roadmap]]). |
| NFR-3 | 커버리지 비감소. ratchet(거대클래스·controller-repo·map-debt)·JaCoCo·PIT 게이트 불변. GiantClassRatchet: DesignEstimateExcelService 축소·신규 Writer < 2000줄. |
| NFR-4 | dual-review → 듀얼푸시. |

---

## 5. 의사결정

- **5-1 순수 TYPE 생성기만 추출** ✅: 필드/외부호출 0(진입점 제외) → 무위험. 데이터조회·dispatch 는 documentService 의존이라 service 잔류.
- **5-2 static util(DesignEstimateWriter)** ✅: 무상태 순수 POI 함수 → 빈/주입 불필요. ExcelStyleSupport·HwpxXmlSupport·InspectQrMetricSupport 선례.
- **5-3 단일 통합 Writer** ✅(제안): TYPE_A~D 한 클래스. #3·excel-split 선례와 일치. TYPE별 4분할은 헬퍼 공유(setCellKeepStyle 등은 TYPE_A 전용이나 한 클래스가 단순)·import 분기 대비 이득 작음. codex 의견.
- **5-4 dead param `doc` 제거** ✅(codex 동의): generateFromTemplate doc 미사용 → 제거. 호출부(generateDesignEstimate L100) 인자도 제거.

---

## 6. 영향 범위 / 리스크

| 계층 | 파일 | 유형 |
|------|------|------|
| Service(신규) | `service/DesignEstimateWriter.java` | 신규 static util |
| Service(수정) | `service/DesignEstimateExcelService.java` | TYPE 생성기 제거 + dispatch 치환 |
| Test(수정?) | `service/DesignEstimateExcelServiceTest.java` | 호출 대상 변경 시(테스트가 private 헬퍼 직접 호출하면 갱신) |

UI/DB/API 변경 0 → 디자인·DB팀 skip.

| 리스크 | 수준 | 완화 |
|---|---|---|
| dispatch 치환 누락(컴파일) | 낮음 | 정의 제거 후 미해결 호출 즉시 컴파일 에러 |
| POI 산출 미세 변경 | 낮음 | 바이트 동일 이동 + 기존 테스트 |
| static import 누락 | 낮음 | 컴파일 검출 |
| dead param 제거 부작용 | 낮음 | doc 미사용 확인됨, 호출부 1곳만 |

---

## 8. 후속 버그픽스 백로그 (본 분리에서 동작 유지, pre-existing)

dual-review(2026-06-27, codex1/Opus8 → 합의6/분쟁3) 가 **전부 원본 pre-existing** 으로 합의한 결함. 본 분리는 **순수 이동(동작 100% 동일)** 이라 무변경 보존, 별도 버그픽스 스프린트로 분리. (분쟁: L18 Writer normalizeRounddownUnit 미사용 import=본 작업 실수→**제거 완료**. L230 bidRate concat·L71 doc 제거=codex refute 기각.)

- **8-1 generateFromSwTemplate NPE 미가드(high)**: `summary.getRow(4).getCell(3)`·`getRow(7).getCell(7)` 등 row null 미체크 → 템플릿 행 부재 시 NPE. summary 시트 자체도 미체크.
- **8-2 wb.getSheet 미가드(TYPE_B/C/D)**: `wb.getSheet("표지")` 등 핵심 시트 null 미체크 → setStringDirect NPE. (TYPE_C 의 hwGrade/swDesign/calc 는 가드 있음.)
- **8-3 fillSummarySheet 행 침범**: HW/SW 항목 2개(템플릿 슬롯) 초과 시 rowIdx 가 SW헤더(row6/7)·총계(row10) 영역 덮어씀. 루프 상한·행삽입 없음.
- **8-4 미사용 param**: generateFromTemplate `vatSeparate`·fillTypeBItemRows `type` 미참조.
- **8-5 fillTypeBItemRows getCell null silently drop**: A/D/E 는 `getCell!=null` 일 때만 기록(품명 B/I 는 setStringDirect 로 생성) → 템플릿 셀 부재 시 번호/율/가 누락. create-if-absent 헬퍼로 통일 권장.
- **8-6(잔여) orgNm 미사용 지역변수**: generateDesignEstimate 의 orgNm 선언 후 미사용(원본 pre-existing). 정리 가능.

> ⚠**제약**: 다수가 운영DB design_estimate 데이터 0건이라 골든 회귀 안전망 약함 → 버그픽스 시 단위테스트(POI 직접 구성) 선확보 필요.

---

## 7. 승인 요청

본 기획서(DesignEstimateWriter 분리)에 대한 codex 검토 및 사용자 최종승인을 요청합니다. 특히 **5-3(단일 통합)**, **5-4(dead param doc 제거 여부)**, **NFR-2(골든 검증 한계)** 의견 요청.
