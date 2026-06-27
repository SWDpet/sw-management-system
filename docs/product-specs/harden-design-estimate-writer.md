# [기획서] DesignEstimateWriter 견고화 + 단위테스트 확보

- **작성팀**: 기획팀
- **작성일**: 2026-06-27
- **트랙**: 거대클래스 #4(DesignEstimateWriter 분리, `6822f8a`) 후속. 그 dual-review §8 백로그 교정 + beyond-A 커버리지.
- **상태**: ⏳ 기획 — codex 실질검증 완료(라벨 "미구현=FAIL" 7번째 오판 [[codex-plan-review-false-fail]]; 권고5개가 기획과 일치 + 질문 확정: 8-3 상한+warning(shiftRows 회피)·8-1/8-2 명시적 예외 fail-fast·8-5 포함(create-if-absent)·8-4/8-6 제거), 사용자 최종승인 대기.

---

## 1. 배경 / 목표

### 1-1 동기
`DesignEstimateWriter`(분리 직후)는 **골든 안전망이 약하다**(운영DB design_estimate 0건 → 통합테스트는 TYPE_A 셀 2개·TYPE_B/C/D smoke 뿐). dual-review §8 백로그(8-1~8-6)도 미해소. **주 목표 = POI 단위테스트로 현재 동작을 고정(회귀 안전망 + 커버리지↑)**, 그 위에서 저위험 결함을 교정.

### 1-2 순서 원칙 (중요)
**테스트 선확보 → 교정.** 각 결함을 고치기 전에 현재 동작을 단위테스트로 잠근 뒤, 동작 변경분만 테스트와 함께 교정. (골든 약한 상태의 맹목 수정 금지.)

### 1-3 ROI 정직성
§8 백로그 중 **8-3(행침범)만 실사용 가능성** 있는 실버그(HW/SW 항목 3개↑). 8-1/8-2/8-5 는 템플릿 고정 리소스라 방어적(저ROI). 따라서 **본 스프린트의 핵심 가치는 단위테스트 확보**(커버리지·회귀안전망), 결함 교정은 저비용 동반.

---

## 2. 변경 설계

### 2-A [주] DesignEstimateWriterTest 신설
- `service/DesignEstimateWriterTest.java`(동일 패키지 → package-private static 직접 호출). documentService mock 불요(Writer 는 인자+템플릿 리소스만).
- 검증(현재 동작 고정 = 골든 역할):
  - **TYPE_A** `generateFromTemplate`: 표지 A5(사업명)·F20(지자체)·A4(연도), 총괄표 HW 품명(B5)/적용율(D5)/도입가(E5)·SW 품명(B9), H11 총계 수식(낙찰율·절사 분기), 빈 행 클리어.
  - **TYPE_B** `generateFromTypeBTemplate`: 표지/설계갑지 핵심 셀 + 총괄표 항목 행.
  - **TYPE_C** `generateFromTypeCTemplate`: 표지/갑지 + HW등급측정 + 조건부 시트(GIS/DBMS 매칭).
  - **TYPE_D** `generateFromSwTemplate`: 표지/설계갑지 + 총괄표 SW 단건 + H8 총용역비 수식.
  - **항목 경계**: 0개(빈)/1개/2개(슬롯 정합)/**3개(8-3 행침범 노출)** 케이스.
- 좌표·값은 템플릿과 1:1 결합(주석 명시, 템플릿 변경 시 동반 갱신 — 기존 통합테스트 패턴 계승).

### 2-B [교정] 8-3 행침범 (실버그)
- `fillSummarySheet`(TYPE_A)·`fillTypeBItemRows`(TYPE_B): 항목이 템플릿 슬롯(2)을 초과하면 rowIdx 가 SW헤더/총계 행을 덮어씀.
- **교정**(codex 동의): 루프를 템플릿 슬롯 상한(2)으로 제한 + **초과 항목 경고 로그**(silent drop 가시화). **TYPE_A·TYPE_B 양쪽** 적용(TYPE_B 는 상한은 있으나 로그 부재 → 추가, codex). 행삽입(shiftRows)은 템플릿 고정행 수식(H4+H8 등) 깨짐 위험 → 비채택.
- ⚠동작 변경: 3개째 항목이 "침범"→"누락+로그". 단위테스트로 전후 고정. (TYPE_D 는 단건 SW 라 무관.)

### 2-C [교정] 저위험 동반
- **8-1/8-2 NPE 가드 → 명시적 예외 fail-fast**(codex 확정): `generateFromSwTemplate` 의 `summary.getRow(4).getCell(...)` 등 + TYPE_B/C/D 의 `wb.getSheet("표지")` 등 **필수 시트/행** null 시 `IllegalStateException("템플릿 시트/행 누락: ...")` throw. (silent 가드 아님 — 템플릿 파손을 조용히 넘기지 않음.) ⚠TYPE_C 의 optional 시트(hwGrade/swDesign/calc)는 기존 null-가드 유지(선택적이므로).
- **8-4 미사용 param 제거**: `generateFromTemplate` 의 `vatSeparate`, `fillTypeBItemRows` 의 `type` 제거(+ 호출부 인자).
- **8-6 orgNm 제거**: DesignEstimateExcelService.generateDesignEstimate 의 미사용 지역변수 `orgNm` 제거.
- **8-5 getCell silently drop → create-if-absent**(codex 확정 포함): `fillTypeBItemRows` 의 A/D/E 를 `setNumericDirect`(create-if-absent)로 통일(품명 B 의 setStringDirect 와 일관). 동작 변경(템플릿 셀 부재 시 누락→기록)이라 단위테스트 선고정.

---

## 3. 기능 요건 (FR)

| ID | 내용 |
|----|------|
| FR-1 | TYPE_A/B/C/D 생성 결과가 단위테스트로 고정된 현재 동작과 일치(8-3 교정분 제외). |
| FR-2 | 8-3: 항목 3개↑ 시 SW헤더/총계 행 침범 없음. 초과 항목은 경고 로그로 가시화. |
| FR-3 | 8-1/8-2: 필수 시트/행 누락 시 NPE 대신 명확한 예외 또는 가드. |
| FR-4 | 8-4/8-6: 미사용 param·변수 제거, 동작 불변. |

---

## 4. 비기능 요건 (NFR)

| ID | 내용 |
|----|------|
| NFR-1 | `./mvnw -o clean verify` BUILD SUCCESS. |
| NFR-2 | DesignEstimateWriterTest 신설(TYPE 4종 + 항목 경계). 기존 DesignEstimateExcelServiceTest(통합) 통과 유지. |
| NFR-3 | **커버리지 상승**(DesignEstimateWriter 직접 단위테스트 — 현재 골든 약점 보완). JaCoCo floor·ratchet·PIT 불변. |
| NFR-4 | dual-review → 듀얼푸시. |

---

## 5. 의사결정

- **5-1 Writer 직접 단위테스트** ✅: package-private static → 동일 패키지 직접 호출. mock 불요·POI 결과 직접 검증. beyond-A 커버리지 직접 기여.
- **5-2 테스트 선확보 → 교정** ✅: 골든 약한 상태 맹목 수정 방지.
- **5-3 8-3 = 상한+경고로그** ✅(codex 동의): 행삽입(수식 깨짐) 대신 상한(TYPE_A·B 양쪽). 누락은 로그 가시화.
- **5-4 범위 = 8-1·8-2·8-3·8-4·8-5·8-6** ✅(codex 확정): 8-1/8-2 명시적 예외 fail-fast, 8-5 create-if-absent 포함. 전부 단위테스트 선고정 후 교정.

---

## 6. 영향 범위 / 리스크

| 계층 | 파일 | 유형 |
|------|------|------|
| Test(신규) | `service/DesignEstimateWriterTest.java` | POI 직접 단위테스트 |
| Service(수정) | `service/DesignEstimateWriter.java` | 8-3 상한·8-1/8-2 가드·8-4 param 제거·8-5 |
| Service(수정) | `service/DesignEstimateExcelService.java` | 8-6 orgNm 제거·8-4 호출부 인자 |

UI/DB/API 변경 0. 디자인·DB팀 skip(단 result xlsx 산출 동작은 §2 단위테스트가 스펙).

| 리스크 | 수준 | 완화 |
|---|---|---|
| 8-3 교정으로 정상(≤2항목) 회귀 | 낮음 | 항목 1/2개 케이스 단위테스트 + 상한은 ≤2 에 무영향 |
| 셀 좌표 단언이 템플릿과 불일치 | 중 | 실제 템플릿 리소스로 생성→검증(통합테스트 좌표 계승), 실패 시 즉시 노출 |
| NPE 가드가 정상 경로 변경 | 낮음 | null 일 때만 분기, 정상 경로 불변 |
| 미사용 param 제거 부작용 | 낮음 | 호출부 1곳, 컴파일 검출 |

---

## 6-2 후속 백로그 (dual-review 2026-06-27, codex2/Opus9 → 합의5/분쟁6)

본 견고화에서 합의 4건 동반교정(TYPE_D D5/E5 setNumericDirect 통일·fillTypeBItemRows requireRow fail-fast·빈행 A열 클리어·미사용 row 제거). 분쟁 6건 전건 refute 기각(vatSeparate/orgNm=dead cleanup·빈행 0덮어쓰기=원본동일·테스트 골든·NPE 단언=의도).

- **잔여: 3개 이상 항목 지원(상한 트레이드오프)**: TYPE_A/B 는 템플릿 슬롯(2) 초과분을 누락+warn 로그(caller 에러 미전파). 실무상 HW/SW 2개 내외라 수용했으나, 3개↑ 사업 발생 시 **동적 행삽입(shiftRows)+수식 재배치** 또는 caller 예외 전파 필요 — 별도 대형 스프린트. 현재는 데이터 누락이 로그로만 가시화됨.

---

## 7. 승인 요청

본 기획서(DesignEstimateWriter 견고화)에 대한 codex 검토 및 사용자 최종승인을 요청합니다. 특히 **5-3(8-3 상한 vs 행삽입)**, **5-4(8-5 포함 여부)**, **8-1/8-2 가드 방식(예외 throw vs silent 가드)** 의견 요청.
