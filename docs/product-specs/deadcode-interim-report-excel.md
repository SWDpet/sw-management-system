# [기획서] InterimReportExcelService Dead code 제거 (beyond-A)

- **작성팀**: 기획팀
- **작성일**: 2026-06-25
- **선행**: `deadcode-design-estimate-excel`(commit `91b24d9`) — 자매 클래스 동일 패턴 후속.
- **트랙**: beyond-A 커버리지 스프린트. excel-service-split(§6-5) 잔재 dead code 삭제(무손실).
- **상태**: ✅ 완료 (2026-06-25). codex 기획/개발계획/구현 전단계 APPROVE/PASS. 순수 402줄 삭제(메서드 4 + import 2), live 무변경.

---

## 1. 배경 / 목표

`InterimReportExcelService.java`(826줄)도 `DesignEstimateExcelService` 와 똑같이 excel-service-split 시 템플릿 방식(`generateInterimReport` → `populateHwInterimTemplate`)으로 전환되며 **구(POI 직접생성) 양식 메서드 4개가 호출처를 잃은 채 잔류**. 이 클래스 LINE 43.8%(약 314 미커버)의 주원인이다.

**현행 live 경로**: `generateInterimReport(Integer)`(공개 진입) → `populateHwInterimTemplate(...)` + 헬퍼(`applyFontSize/setSumFormula/currentMonthLabel/trimPct/monthsFromPeriodText/buildPeriodText`).

아래 4개는 구 직접생성 잔재로 어디서도 호출되지 않는다(전부 private, 라이브/테스트 참조 0건).

**목표**: 런타임·산출 바이트 무영향으로 dead code 4개(~399줄) 삭제 → 부채 제거 + 커버리지 분모 정상화.

---

## 2. 삭제 대상 (재검증 완료, 현 라인 기준)

전체 `src/`(main+test) 스캔 시 4개 메서드명 각 정의부 1건뿐(호출처 0).

| # | 메서드 | 라인 | 비고 |
|---|---|---|---|
| 1 | `createInterimCoverSheet` | 427~449 | 구 표지 직접생성 |
| 2 | `createInterimDetailSheet` | 450~626 | 구 상세 직접생성 |
| 3 | `createInterimSummarySheet` | 627~739 | 구 총괄 직접생성 |
| 4 | `createInterimItemDetailSheet` | 740~825 | 구 항목상세 직접생성 |

4개 연속(426행 `// --- 기성 Sheet 1: 표지 ---` 주석부터 825행까지), 826행은 클래스 닫는 `}`.

### 2-1. unused 전락 import (실측)

| import | 처리 | 근거 |
|---|---|---|
| `org.apache.poi.ss.util.CellRangeAddress` | **제거** | live 영역(1~424) 사용 0, 전 32회 모두 dead 블록. |
| `static …ExcelStyleSupport.setCellValue` | **제거** | live 영역 사용 0(멤버 `Cell.setCellValue` 포함 0), dead 전용. |
| `static …ExcelStyleSupport.FONT` | **유지** | DesignEstimate 와 달리 **live 영역에서 1회 사용** → 유지. |

기타(`str/toDouble/toLong/setStringDirect/setNumericDirect`, wildcard `usermodel.*`, `XSSFWorkbook`, `ClassPathResource`, `SwProject` 등) 전부 live 사용 → 유지.

---

## 3. 기능 요건 (FR)

| ID | 내용 |
|----|------|
| FR-1 | §2 의 4개 private 메서드 + 직전 구분 주석 삭제(live 헬퍼·진입점 무변경). |
| FR-2 | 삭제 직전 4개 메서드명 `src/` 전역 재스캔 → 각 정의부 1건만 확인(예외 발견 시 중단). |
| FR-3 | §2-1 대로 import 2개(`CellRangeAddress`, static `setCellValue`) 제거. `FONT` 는 유지. 삭제 후 잔여 import body 사용 0 인 것 추가 정리. |
| FR-4 | live 경로(`generateInterimReport`/`populateHwInterimTemplate`/헬퍼 6종) 동작·시그니처 불변. |

---

## 4. 비기능 요건 (NFR)

| ID | 내용 |
|----|------|
| NFR-1 | 삭제 후 `compile` 성공. |
| NFR-2 | 기존 `InterimReportExcelServiceTest` 전건 green(산출 바이트 회귀 0 대리지표). |
| NFR-3 | 전체 `./mvnw test`(순수 단위) green. |
| NFR-4 | 비가역 — git history 복구. |
| NFR-5 | JaCoCo 게이트 통과 + 분모 축소로 커버리지 상승. |

---

## 5. 의사결정

- **5-1 간접호출 가능성** ✅ 배제: 전부 private, 리플렉션/문자열 호출 grep 0.
- **5-2 레퍼런스 보존** ✅ 삭제 우위: 구 직접생성은 템플릿 방식으로 완전 대체, git history 보존.
- **5-3 FONT 차이** ✅ 자매 클래스라도 import 처리는 파일별 실측(InterimReport 는 FONT 유지)이 정답.

---

## 6. 영향 범위 / 리스크

| 계층 | 파일 | 유형 |
|------|------|------|
| Service(수정) | `…/service/InterimReportExcelService.java` | 메서드 4개(~399줄) + import 2 제거 |
| Docs | 본 기획서 + 개발계획서 | 신규 |

DB/API/UI/템플릿 리소스 변경 0. UI 키워드 0 → 디자인팀 skip.

| 리스크 | 수준 | 완화 |
|---|---|---|
| 미사용 오판 | 낮음 | FR-2 grep + compile/test 게이트 |
| import 과잉제거 | 중간 | 메서드삭제→compile→import정리→compile |
| 산출 바이트 회귀 | 낮음 | dead 한정, NFR-2 |

---

## 7. 승인 요청

본 기획서에 대한 codex 검토 및 사용자 최종승인을 요청합니다.
