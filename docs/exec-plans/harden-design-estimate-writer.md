# [개발계획서] DesignEstimateWriter 견고화 + 단위테스트 확보

- **작성팀**: 개발팀
- **작성일**: 2026-06-27
- **기획서**: `docs/product-specs/harden-design-estimate-writer.md` (codex 실질검증 + 사용자 최종승인 2026-06-27).
- **상태**: ✅ **구현 완료(2026-06-27)**. DesignEstimateWriterTest 7개 신설(TYPE_A/B/C/D + 8-3 overflow + 경계). §8-1~8-6 교정: 8-3 상한+warning(TYPE_A·B)·8-1/8-2 requireSheet/requireRow fail-fast·8-5 setNumericDirect create-if-absent·8-4 vatSeparate/type 제거·8-6 orgNm 제거. `mvnw -o clean verify` BUILD SUCCESS(1322 green/52 skip). codex 8번째 오판 실질채택. dual-review→듀얼푸시.

---

## 1. 작업 개요

DesignEstimateWriter 에 POI 단위테스트를 확보(현재 동작 고정·커버리지↑)하고 §8 백로그 교정: 8-3 행침범 상한+로그, 8-1/8-2 명시적 예외 fail-fast, 8-5 create-if-absent, 8-4/8-6 미사용 제거. **순서: 시그니처 확정(동작 불변) → 테스트 고정 → 동작변경 교정.**

---

## 2. 구현 순서 (S-n)

### S-1 미사용 param·변수 제거 (8-4/8-6, 동작 불변 → 시그니처 확정)
- `DesignEstimateWriter.generateFromTemplate`: `boolean vatSeparate` param 제거.
- `DesignEstimateWriter.fillTypeBItemRows`: `String type` param 제거(호출부 2곳 `fillTypeBItemRows(summary, hwItems, 3)` / `(summary, swItems, 6)`).
- `DesignEstimateExcelService.generateDesignEstimate`: 미사용 지역변수 `orgNm` 제거 + TYPE_A 호출의 `vatSeparate` 인자 제거. (vatSeparate 파싱 라인도 제거 — 다른 사용처 없음 확인.)
- 컴파일 확인(동작 불변).

### S-2 DesignEstimateWriterTest 신설 (현재 동작 고정 — 정상 케이스)
- `service/DesignEstimateWriterTest.java`(동일 패키지). Writer static 직접 호출(mock 불요).
- 헬퍼: `item(type,name,rate,unitPrice)` Map 생성, `open(byte[])` → XSSFWorkbook.
- **정상 케이스(현재=교정후 동일)** 고정:
  - TYPE_A(`generateFromTemplate`, 항목 2/2): 표지 A5 사업명·A4 연도·F20 지자체, 총괄표 HW B5 품명/D5 율/E5 가·SW B9 품명, H11 총계 수식(절사·낙찰율 분기 2~3 케이스), 빈 행 클리어(항목 1개 시 2번째 행 blank).
  - TYPE_B(`generateFromTypeBTemplate`, 2/2): 표지·설계갑지 핵심 셀 + 총괄표 항목 행 B/D/E.
  - TYPE_C(`generateFromTypeCTemplate`): 표지/갑지 + HW등급측정 B/E + 조건부(GIS엔진→J8, DBMS→G6).
  - TYPE_D(`generateFromSwTemplate`, SW 1): 표지·설계갑지 + 총괄표 B5/D5/E5 + H8 수식.
  - 항목 0개/1개 경계(빈 행·단건).
- 좌표·값은 실제 템플릿 리소스 결합(주석 명시, 통합테스트 패턴 계승).

### S-3 8-3 행침범 교정 + 경계 테스트
- `DesignEstimateWriter` 에 `@Slf4j`(lombok) 추가(static 메서드에서 `log` 사용).
- `fillSummarySheet`: HW/SW 루프를 `Math.min(size, slots=2)` 상한. 초과 시 `log.warn("설계내역서 총괄표 HW 항목 {}개 중 템플릿 슬롯 {} 초과분 누락(행침범 방지)", size, slots)` (SW 동일).
- `fillTypeBItemRows`: 기존 `templateSlots=2` 유지 + 초과 경고 로그 추가(`items.size() > templateSlots` 시).
- **테스트(교정 후 동작)**: HW 3개 입력 → 총괄표 row6/7(SW헤더)·row10(총계) 영역 **불변**(침범 없음) + 첫 2개만 기록. (교정 전엔 침범했음 — 본 커밋에서 동작 변경.)

### S-4 8-1/8-2 필수 시트/행 명시적 예외 (fail-fast)
- private static 헬퍼: `requireSheet(Workbook wb, String name)` → null 시 `IllegalStateException("설계내역서 템플릿 시트 누락: " + name)`; `requireRow(Sheet s, int idx)` → null 시 예외.
- `generateFromTypeBTemplate`/`generateFromTypeCTemplate`/`generateFromSwTemplate`: `wb.getSheet("표지"/"설계갑지"/"총괄표" 등)` → `requireSheet(...)`. ⚠TYPE_C optional 시트(hwGrade/swDesign/calc)는 **기존 null-가드 유지**(선택적).
- `generateFromSwTemplate`: `summary.getRow(4)/getRow(7)` → `requireRow(summary, 4/7)`.
- **테스트**: 정상 경로 불변(기존 케이스 통과). (시트 누락 케이스는 템플릿 변조 불가라 단위테스트 생략 — 헬퍼 자체는 정상 경로로 커버.)

### S-5 8-5 fillTypeBItemRows create-if-absent
- A/D/E `if (row.getCell(x) != null) ...setCellValue` → `setNumericDirect(summary, r, x, value)`(create-if-absent, B 품명의 setStringDirect 와 일관). 번호(i+1)·rate·price.
- **테스트**: TYPE_B 항목의 번호/율/가가 셀에 기록됨 확인(현재 템플릿엔 셀 존재 → 값 동일, 일관성 확보).

### S-6 검증
- `./mvnw -o clean verify` BUILD SUCCESS. DesignEstimateWriterTest + 기존 통합테스트 green.
- **커버리지 상승** 확인(DesignEstimateWriter LINE). JaCoCo floor·ratchet·PIT 불변.

### S-7 (작업완료)
- dual-review → 합의 반영 → 커밋 `fix(excel)` + 듀얼푸시.

---

## 3. 검증 기준 (NFR 매핑)

| NFR | 검증 |
|----|------|
| NFR-1 | S-6 clean verify green |
| NFR-2 | S-2~S-5 DesignEstimateWriterTest(TYPE 4종+경계+8-3 교정) + 통합테스트 |
| NFR-3 | S-6 커버리지 상승·floor/ratchet/PIT 불변 |
| NFR-4 | S-7 dual-review + 듀얼푸시 |

---

## 4. 리스크 / 롤백

| 리스크 | 완화 |
|---|---|
| 8-3 상한이 정상(≤2) 회귀 | 항목 1/2 케이스 테스트 + min 상한은 ≤2 무영향 |
| 테스트 좌표 brittle | 실제 템플릿으로 생성→검증, 불일치 즉시 실패. 통합테스트 좌표 계승 |
| requireSheet 예외가 정상경로 변경 | null 일 때만 throw, 정상 시트 불변 |
| setNumericDirect 전환 부작용 | 템플릿 셀 존재 → 값 동일, create-if-absent 는 부재 시만 추가 |
| @Slf4j 도입 | lombok 기존 사용 중, static 메서드 log 접근 정상 |

롤백: 단일 커밋 `git revert`.

---

## 5. 커밋

- `fix(excel): DesignEstimateWriter 견고화 — 행침범 상한·필수시트 예외·셀 일관·단위테스트 (§8)`.
- 듀얼푸시. 기획/개발계획 ✅ 완료 + §8-1~8-6 해소 표기.

---

## 6. 승인 요청

본 개발계획서에 대한 codex 검토 및 사용자 최종승인을 요청합니다.
