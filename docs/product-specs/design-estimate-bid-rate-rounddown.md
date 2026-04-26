---
tags: [plan, document, design-estimate, excel-export]
sprint: "design-estimate-dynamic-formula-01"
status: draft-v6
created: "2026-04-24"
revised: "2026-04-24 (v6 — codex v5 잔존 문서 정합성 4건 반영)"
---

# [기획서] 설계내역서 엑셀 — 낙찰율 비고 동적화 + 절사 단위 UI 선택 (v6)

- **작성팀**: 기획팀
- **작성일**: 2026-04-24
- **선행**: `756813b` (docs-renewal-01 완료)
- **상태**: v6 (codex v5 잔존 정합성 4건 반영 / 최종 승인 재검토 대기)

## v5→v6 변경 요약
codex v5 검토 잔존 이슈 4건 (단어 교체 수준) 반영:
1. **§8 버전 표기 갱신** — "본 기획서(v4)" / "codex v4 최종 검토 중점 항목" → **v6** 으로 일괄 갱신.
2. **FR-6''' 제목 오기 교정 (본문)** — "Invalid rounddownUnit 로드 fallback 경고" → **"Invalid rounddownUnit — 엑셀 생성 시 fallback 경고"** 로 수정. 본문이 이미 `ExcelExportService.generateDesignEstimate()` 서버 경로였는데 제목에 "로드"가 남아 FR-6 과 혼동. 제목+본문에 "서버 엑셀 생성 경로 전용", "FR-6(JS 로드)와 경계 명확화" 명시.
3. **v4→v5 변경 요약의 FR-6''' 오기 중복 교정** — "FR-6''' 로드 시 fallback 정책" → "FR-6''' 엑셀 생성 시 fallback 정책".
4. **§8 재검토 중점 항목** — v5 지적 4건(위 항목들) 을 reviewer guide 로 반영.

## v4→v5 변경 요약 (유지)
codex v4 검토 잔존 이슈 3건 (숫자/참조 오타 수준) 반영:
1. **태그 개수 불일치 해소** — 본 요약/§7-0 정의표/§8 승인요청 모두 **"9 태그 체계"** 로 통일. §7-0 표에 `[자동:codex]` 태그(9행째) 추가.
2. **FR 번호 매핑 오타** — §7-9 마지막 Acceptance 항목이 가리키던 `(FR-6''')` → `(FR-6)` 수정. FR-6''' 는 엑셀 생성 시 fallback 이고, 해당 Acceptance 항목은 **브라우저 로드 시 fallback** 이므로 FR-6 가 정합.
3. **§4-9 line 209 잔문장** — "3단 방어에서 걸러지지 않은 케이스" → **"4개 진입점 방어에서 걸러지지 않은 케이스"** 표기 통일.

## v3→v4 변경 요약 (유지)
codex v3 검토 잔존 이슈 4건 (모두 문서 정합성) 반영:
1. **§5 영향 범위 모순** — FR-5' 가 `DocumentController.saveDocument()` 수정 요구하는데 "변경 없음"으로 표기돼 있던 것을 **"수정: `saveDocument()` 내 `design_estimate` 섹션 저장 직전 `rounddownUnit` allow-list 정규화"** 로 정정.
2. **FR-10 정규화 순서 명시** — `"raw 값 추출 → allow-list 정규화 → `int` 변환"` 순서 강제. `toDouble(...)` 호출이 정규화보다 앞서지 않도록 명세. `"abc"` 같은 비수치 입력도 예외 없이 fallback 되게 보장.
3. **Acceptance 태그 재분해** — `[자동:POI]` 과적재 해소. 9 태그 체계로 정제 (§7-0 표 참조).
4. **§4-9 "3단 방어" → "4개 진입점"** — 실제 서술은 4 진입점(클라 저장 / 서버 저장 / 엑셀 생성 / 로드)이므로 표현 일치.

## v2→v3 변경 요약
codex v2 검토에서 새로 지적된 4건 반영 (v1 9건은 이미 v2 에서 해결됨):
1. **TYPE_D override 엣지케이스** — "사용자가 수동으로 1000 재선택" 과 "초기 default 1000" 를 구분하는 **`rounddownUnitTouched` 플래그** 도입 (FR-6'', §4-6 전면 재작성). 사용자가 드롭다운을 **한 번이라도 변경하면 touched=true**, 이후 estimateType 변경에도 자동 덮어쓰기 금지.
2. **invalid `rounddownUnit` 처리 정책** — JSONB 자유 스키마 특성상 오염 데이터 방어. **FR-5' 저장단 allow-list 검증** + **FR-6''' 엑셀 생성 시 fallback 정책**(허용값 외 → `1000` 으로 대체 + WARN 로그) 추가. §4-9 신설.
3. **Acceptance fixture 식별 기준** — "기존 DB 레코드 3건" 을 **재현 가능한 기준**으로 구체화: (a) `design_estimate.estimateType` 값이 TYPE_A/B/D 각 1건 (b) `rounddownUnit` 필드가 section_data JSON 에 존재하지 않는 레코드 (구 스키마) (c) 본 스프린트 개발계획서에서 `docId` 를 고정하고 fixture SQL 로 재현 가능하게 조성. 조건 명시 (§7 Acceptance 도입부).
4. **Acceptance 자동/수동 분리** — 각 수락 기준 항목 앞에 `[자동:POI]`, `[자동:JUnit]`, `[자동:브라우저E2E]`, `[수동:브라우저]`, `[수동:Excel]` 태그 부착. NFR-3 은 엑셀 셀 검증 전용으로 스코프 명시.

## v1→v2 변경 요약 (유지)
codex 1차 검토 지적 9건 전수 반영 (v2 시점 완료):
1. **FR-11/FR-12 부호 충돌 (치명)** — `toExcelRoundDigitsArg` 계약을 "0/-1/-2/-3/-4 직접 반환"으로 고정, FR-12는 `","+digits+")"` 로 단순화
2. **`previewEstimate()` 누락** — FR-3' 추가, Acceptance에 반영
3. **TYPE_D 기본값 override 충돌** — "신규 문서에서 estimateType→TYPE_D 전환 + `rounddownUnit` 미저장" 조건에서만 `10` 제안으로 명문화 (§4-6) *(v3 에서 touched 플래그로 재정의)*
4. **§4-4 사실 오류** — `line 720` 주장 제거 (해당 라인은 이미 `낙찰율`임)
5. **미존재 파일 참조** — `design-estimate-typec-formula.md` 언급 제거, TYPE_C 후속 과제는 평문 각주로만 언급
6. **NFR-3 바이너리 비교 → 핵심 셀 비교** — XLSX 바이너리 비교는 zip 엔트리 순서/timestamp 변동으로 flaky 하므로 `H11/I11/H8/I8` 등 검증 셀 목록 고정 + 수식 문자열/값 직접 비교
7. **FR-16 라벨 공백 불일치** — `quotation-vat-rules.md §43-51` 와 글자단위 일치(공백 포함). 기존 코드 `"백단위절사"`도 이 과정에서 공백 포함 + 새 명칭으로 교체
8. **수락 기준 모호성** — 100% bidRate 시 I셀 값을 정확히 `""` (empty) 로 고정, `previewEstimate` 반영/저장-재로드 복원/TYPE_D 기존 레코드 override 방지 시나리오 추가
9. **TYPE_C 리스크 직접 고지** — 리스크 표에 "엑셀/미리보기 의도적 불일치" 행 추가, UI 안내 툴팁 FR-4'' 로 승격

---

## 1. 배경 / 목표

### 배경
설계내역서(착공계 §⑧) **엑셀 출력물의 총괄표 비고란**이 사용자가 입력한 낙찰율과 어긋난 값으로 표시되는 회귀가 확인됐다. 예: UI에서 낙찰율 **91%** 를 입력하고 엑셀을 내리면, 수식은 `=ROUNDDOWN((H4+H8)*0.91,-3)` 로 올바르게 갱신되지만 비고 셀(I11)에는 템플릿의 하드코딩된 **"낙찰율적용 97%"** 가 그대로 남는다.

또한 설계내역서는 **절사 단위(rounddown_unit)가 UI에서 선택 불가**능하며, 내부적으로 `-3`(천원 단위 절사)가 고정되어 있다. 견적서(`qt_quotation`)는 2026-03-13부터 `rounddown_unit` 컬럼이 도입되어 1/10/100/1000/10000 단위를 지원하고 UI에 노출되지만, 설계내역서는 같은 정책이 적용되지 않아 **모듈 간 일관성 부재**가 있다.

### 재검증 결과 (2026-04-24)
- `src/main/java/com/swmanager/system/service/ExcelExportService.java:373-384` — TYPE_A `fillSummarySheet()`에서 **H11 수식만 갱신, I11 비고 셀 업데이트 코드 부재**. TYPE_B(984-991)/TYPE_D(1085-1091)는 I 셀 갱신 구현됨.
- `src/main/java/com/swmanager/system/service/ExcelExportService.java:987` — `i11.setCellValue("낙찰률 적용(... %)\n백단위절사")` — **"낙찰률"(률) 표기 + "백단위절사"(공백 없음)** 사용. 나머지 TYPE_A(373 주석)/TYPE_D(1088)/`line 720`/`line 542`/`line 1088` 은 이미 **"낙찰율"(율)** 사용.
- `src/main/resources/templates/document/doc-commence.html:246-299` — 설계내역서 입력 폼. **절사 단위 입력 필드 없음**. 낙찰율 기본값 `97` 하드코딩 (line 264).
- `doc-commence.html:1084-1129` `calcEstPreview()` — 자동 산출 미리보기. `Math.floor(subTotal * bidRate / 1000) * 1000` 으로 **천원 단위 절사 하드코딩** (line 1123 등).
- `doc-commence.html:1814+` `previewEstimate()` — 확대 미리보기 팝업. **동일하게 `/1000` 하드코딩 잔존**.
- `doc-commence.html:1443-1450` — `collectSections()` 저장 JS. 현재 sectionData 에 `rounddownUnit` 필드 미포함.
- `doc-commence.html:~738` — 편집 진입 시 sectionData 복원 JS. 현재 `rounddownUnit` 로드 코드 없음.
- `docs/product-specs/quotation-vat-rules.md:43-55` — 견적서의 절사 단위 라벨 체계 명문화됨. 라벨 표기는 **"십원단위 절사 / 백원단위 절사 / 천단위 절사 / 만단위 절사"** (공백 포함). 설계내역서는 해당 규칙 미적용.
- `swdept/sql/V003_add_rounddown_unit.sql`, `V015_fix_rounddown_units.sql` — 견적서용 마이그레이션. 설계내역서(document_detail.section_data JSONB)는 스키마 변경 불필요.
- `Quotation.java:67-68` — `rounddown_unit` 컬럼, 기본값 `1`. `QuotationController.java:38-41` 에 `roundDown(long, int)` 유틸 존재.

### 목표
- **G1**: 사용자가 UI에서 입력한 낙찰율(%)이 **엑셀의 수식 + 비고 라벨 양쪽에 모두** 반영된다 (TYPE_A/B/D 공통).
- **G2**: 설계내역서에도 **절사 단위를 UI에서 선택**할 수 있고, 선택값이 엑셀 수식(ROUNDDOWN 자릿수)과 비고 문구, 화면 미리보기 2종(`calcEstPreview` + `previewEstimate`) 모두에 일관되게 반영된다.
- **G3**: 기존 데이터(절사 단위 필드가 없는 레코드)는 **기본값 `1000` (천원 단위 절사)** 로 렌더링되어 **기존 출력물과 동일**. 회귀 없음. 단, TYPE_D 는 §4-6 단서 참조.
- **G4**: 견적서의 절사 단위 라벨 체계(`quotation-vat-rules.md §43-51`, 공백 포함)를 **설계내역서에도 동일하게 적용**해 용어 일관성 확보. 기존 코드 `"백단위절사"`(공백 없음, 구 명칭)는 **"천단위 절사"**(공백 포함, 신 명칭)로 교체. 용어 drift 종료.

### 비목표 (Out of scope)
- **TYPE_C 엑셀 수식 동적화 제외**. TYPE_C(김포시 템플릿)는 ROUNDDOWN 수식이 Java 코드가 아니라 **템플릿 `.xlsx` 파일 14개 시트 내부 셀 수식**에 내장되어 있어 수정 범위가 크다. 이번 스프린트에서는 TYPE_C 선택 시 **엑셀 출력은 템플릿 기본 수식 유지**하고, **UI에서는 "TYPE_C 는 엑셀 수식에 절사 단위 반영 안 됨" 툴팁 노출** (FR-4''). 완전한 동적화는 이후 별도 스프린트로 검토한다(본 기획서에 파일 경로로 참조하지 않음).
- 견적서(`qt_quotation`) 측 로직 변경 없음. `Quotation.rounddownUnit` 은 현재 값/동작 그대로 유지.
- 엑셀 템플릿 파일(`templates/excel/design_estimate_*.xlsx`) **바이너리 수정 없음**. 모든 동적화는 Java 코드에서 셀 값/수식 덮어쓰기로 처리.

---

## 2. 기능 요건 (FR)

### 2-1. UI (착공계 §⑧ 설계내역서 폼)
| ID | 내용 |
|----|------|
| FR-1 | `doc-commence.html` 의 설계내역서 섹션(부가세 별도 체크박스 다음 행)에 **절사 단위 드롭다운** 추가. 옵션 값/표시 문자열(정확한 공백 포함, `quotation-vat-rules.md §43-51` 인용):<br>• `value="1"` → `(표시 안함)` (실제 `<option>` 표시 텍스트는 "절사 없음")<br>• `value="10"` → `"십원단위 절사"`<br>• `value="100"` → `"백원단위 절사"`<br>• `value="1000"` → `"천단위 절사"` (**기본 선택**)<br>• `value="10000"` → `"만단위 절사"` |
| FR-2 | 드롭다운 element id: `est_rounddownUnit`. name 속성 없음(JS가 직접 읽음). 기존 UI 행 스타일과 일치. |
| FR-3 | `calcEstPreview()` (line 1084-1129) 수정: 하드코딩 `/1000) * 1000` 을 `Math.floor(v / unit) * unit` 로 동적화. `unit === 1` 이면 절사 미적용. |
| **FR-3'** | **`previewEstimate()` (line ~1814, 확대 미리보기 팝업) 수정**: `calcEstPreview` 와 동일한 절사 공식 적용. 두 미리보기 결과는 동일한 값을 표시. (v1 누락 지적 #2 반영) |
| FR-4 | "자동 산출 미리보기" 패널 끝부분에 비고 라벨 한 줄 추가: `(낙찰율 91% 적용, 천단위 절사)`. `unit === 1` 일 때 절사 문구 **생략** (quotation-vat-rules §45: "절사 없음 = 표시 안함"). `bidRate >= 100` 또는 `bidRate <= 0` 일 때 낙찰율 문구 생략. 둘 다 생략이면 비고 라벨 자체 비노출. |
| **FR-4'** | **드롭다운 `onchange` 이벤트 + estimateType `onchange` 이벤트**: UI 조작으로 값이 바뀌면 즉시 `calcEstPreview()` 재호출. 드롭다운 변경 시 preview 가 지연 없이 갱신되어야 함. |
| **FR-4''** | **TYPE_C 선택 시 UI 안내 툴팁/고지 노출**: estimateType 이 `TYPE_C` 일 때 절사 단위 드롭다운 옆에 `ℹ "TYPE_C 는 엑셀 수식이 템플릿 고정이라 절사 단위가 엑셀에 반영되지 않습니다 (화면 미리보기만 반영)"` 아이콘+tooltip. `TYPE_A/B/D` 일 때는 숨김. |

### 2-2. 저장/로드 데이터 구조 (`design_estimate` section JSON)
| ID | 내용 |
|----|------|
| FR-5 | `collectSections()` (line 1443-1450) 수정: `sectionData` 객체에 `rounddownUnit: parseInt(document.getElementById('est_rounddownUnit').value) \|\| 1000` 필드 포함. |
| **FR-5'** | **저장단 허용값 검증** (클라이언트 + 서버 양쪽): 허용값 집합 = `{1, 10, 100, 1000, 10000}`. **클라이언트** `collectSections()` 에서 드롭다운 value 를 parseInt 한 뒤 허용값에 없으면 `1000` 으로 강제 대체 후 저장. **서버** `DocumentController.saveDocument()` 가 `design_estimate` 섹션을 저장하기 직전에 동일 검증을 거쳐 `sectionData.rounddownUnit` 정규화 (허용값 외 → `1000` 치환, WARN 로그 `"design_estimate.rounddownUnit invalid value {v} → normalized to 1000 (docId={id})"`). 예외 throw 하지 않음(사용자 데이터 보존 우선). |
| FR-6 | **로드 시 처리 (line ~738 편집 페이지 진입 JS)**: `sectionData.rounddownUnit` 이 허용값 중 하나면 드롭다운 `value` 로 세팅. **없거나 허용값 외면 `1000` 디폴트**. |
| **FR-6'** | **TYPE_D 자동 제안 조건**: `estimateType` `onchange` 이벤트 핸들러는 **세 조건 모두 참**일 때만 `rounddownUnit` 드롭다운을 `10` 으로 제안:<br>① `docId === null` (신규 문서)<br>② `rounddownUnitTouched === false` (사용자가 드롭다운을 한 번도 수동 변경하지 않음)<br>③ 새로 선택된 `estimateType === "TYPE_D"`. |
| **FR-6''** | **`rounddownUnitTouched` 플래그 관리**: 초기값 `false`. 사용자가 `est_rounddownUnit` 드롭다운에 직접 `change` 이벤트를 발생시키면 (사용자 인터랙션 기반) `true` 로 세팅. 그 후로는 `estimateType` 변경이 있어도 드롭다운 값을 자동으로 덮어쓰지 않는다. **자동 제안(FR-6')에 의한 값 변경은 touched 로 간주하지 않는다** (프로그램 주도의 change 이벤트는 무시). 편집 페이지 **로드 시** 기존 저장값 복원도 touched 로 간주하지 않는다(신규 로드 시 touched=false 로 시작하되, 로드된 값이 1000 이 아니면 `rounddownUnitTouched=true` 로 초기화하는 정책이 더 안전 — **결정: 로드된 값 ≠ 1000 이면 touched=true 로 초기화, 1000 이면 false**. 사유: 기존 TYPE_D(10 저장) 레코드 편집 중 TYPE_A → TYPE_D 왕복 시에도 10 유지 보장). |
| **FR-6'''** | **Invalid rounddownUnit — 엑셀 생성 시 fallback 경고**: `estData.rounddownUnit` 이 허용값 외(예: `500`, `"abc"`, negative)일 때 `ExcelExportService.generateDesignEstimate()` (서버 Java) 는 `1000` 으로 대체 후 `log.warn("design_estimate.rounddownUnit invalid value '{}' → using default 1000 (docId={})", invalidValue, docId)` 출력. 엑셀 생성 자체는 성공(사용자 경험 우선). **경계 명확화**: 클라이언트 편집 페이지 로드 시 fallback 은 FR-6 소관(JS), 본 FR-6''' 는 **서버 엑셀 생성 경로 전용**. §4-9 의 4개 진입점 중 "③ 엑셀 생성" 에 해당. |
| FR-7 | DB 스키마 변경 **없음**. `tb_document_detail.section_data` JSONB 컬럼 내 key 추가만. |

### 2-3. ExcelExportService — TYPE_A 비고 셀 업데이트 (버그 수정)
| ID | 내용 |
|----|------|
| FR-8 | `fillSummarySheet()` (line 326-385) 확장: `bidRate > 0` 이면 **H11 수식 + I11 비고 셀 둘 다** 갱신. v1 의 `bidRate != 0.9696` 조건 제거. |
| FR-9 | I11 비고 셀 값 규칙 (모든 TYPE 공통, FR-13/FR-14 도 동일):<br>• `bidRate > 0 && bidRate < 1.0 && unit > 1` → `"낙찰율 적용(XX%)\n<절사단위라벨>"`<br>• `bidRate > 0 && bidRate < 1.0 && unit == 1` → `"낙찰율 적용(XX%)"`<br>• `bidRate <= 0 OR bidRate >= 1.0 && unit > 1` → `"<절사단위라벨>"`<br>• `bidRate <= 0 OR bidRate >= 1.0 && unit == 1` → `""` (빈 문자열) |

### 2-4. ExcelExportService — 절사 단위 파라미터화 (TYPE_A/B/D)
| ID | 내용 |
|----|------|
| FR-10 | `generateDesignEstimate(docId)` (line 179-229) 진입점에서 `int rounddownUnit` 파라미터 추출. **정규화 순서 강제** (비수치/오염 값 방어):<br>① `Object rawRounddownUnit = estData.get("rounddownUnit");` (null, 문자열, 숫자 모두 Object 로 받음)<br>② `int normalizedUnit = normalizeRounddownUnit(rawRounddownUnit);` — 신규 유틸. 내부 규칙: `null` 또는 허용값 외(`"abc"`, `500`, `-1` 등 포함) → `1000` + WARN 로그. 허용값(`1/10/100/1000/10000`) 은 그대로 반환. `toDouble()` 호출은 이 유틸 **내부**에서 try/catch 로 감싸 예외 발생 금지.<br>③ 이후 모든 TYPE 메서드에 `int rounddownUnit = normalizedUnit` 전달.<br>**핵심**: `toDouble()` / `(int)` 변환이 정규화 전에 일어나지 않는다. `"abc"` 같은 비수치 입력도 파싱 예외 없이 `1000` 으로 fallback. |
| **FR-11 (수정)** | 신규 유틸 `private static int toExcelRoundDigitsArg(int unit)` 의 **반환 계약**:<br>• `1 → 0`<br>• `10 → -1`<br>• `100 → -2`<br>• `1000 → -3`<br>• `10000 → -4`<br>• 그 외 값 → `IllegalArgumentException` (방어적). <br>**계약 핵심: 반환값이 이미 음수(또는 0). 호출부는 음수 부호를 추가로 붙이지 않는다.** |
| **FR-12 (수정)** | 호출부 수식 템플릿 변경 규칙:<br>• 기존 `",-3)"` → 신규 `"," + toExcelRoundDigitsArg(unit) + ")"`<br>예: `unit=1000` → `",-3)"` 동일 결과. `unit=1` → `",0)"`. `unit=10000` → `",-4)"`.<br>**`"-"` 문자를 별도로 붙이지 않는다** (FR-11 반환값에 이미 부호 포함). |
| FR-13 | TYPE_B `generateFromTypeBTemplate()` (line 976-991):<br>• H11 수식: `"ROUNDDOWN((H10+H9)*" + bidRate + "," + toExcelRoundDigitsArg(unit) + ")"` (line 979)<br>• `bidRate` 미적용 분기(`else`): `"ROUNDDOWN(H10+H9," + toExcelRoundDigitsArg(unit) + ")"` (line 981)<br>• I11 라벨: FR-9 규칙 적용. **기존 `"백단위절사"` 문자열 제거, `"낙찰률"` → `"낙찰율"` 통일 (표기법 §4-7)**. |
| FR-14 | TYPE_D `generateFromSwTemplate()` (line 1076-1091):<br>• H8 수식: 기존 `-1` 고정 → `toExcelRoundDigitsArg(unit)` 적용.<br>• `bidRate` 미적용 분기: `"ROUNDDOWN(H6+H7," + toExcelRoundDigitsArg(unit) + ")"`.<br>• I8 라벨: FR-9 규칙 적용. |
| FR-15 | **TYPE_C 엑셀 수식 변경 없음** (비목표). Java 측 `generateFromTypeCTemplate()` 은 이번 스프린트에서 수정하지 않음. UI(FR-4'') 만 별도 처리. |

### 2-5. 절사 단위 라벨 매핑 (공통 유틸)
| ID | 내용 |
|----|------|
| FR-16 | Java 측: `ExcelExportService` 에 `private static String rounddownLabel(int unit)` 추가. 매핑값은 **`quotation-vat-rules.md §43-51` 공백 포함 원문**:<br>• `1 → ""` (빈 문자열)<br>• `10 → "십원단위 절사"`<br>• `100 → "백원단위 절사"`<br>• `1000 → "천단위 절사"`<br>• `10000 → "만단위 절사"`<br>• 그 외 → `IllegalArgumentException`. |
| FR-17 | JS 측: `doc-commence.html` script 블록에 동일 매핑의 `rounddownLabel(unit)` 함수 추가. **Java 의 FR-16 과 글자단위 일치**. |
| FR-18 | 표기 통일: 파일 전체에서 `"낙찰률"` → `"낙찰율"` 로 치환. **치환 대상 한정 파일**: `src/main/java/com/swmanager/system/service/ExcelExportService.java`. 치환 전후 `grep -rn "낙찰률" src/` 로 범위 확인 (재검증 결과: line 987 만 해당). |

### 2-6. 회귀/하위 호환
| ID | 내용 |
|----|------|
| FR-19 | 기존 `design_estimate` 레코드(`rounddownUnit` 미포함)는 Java/JS 모두 `getOrDefault(1000)` 적용. 엑셀/미리보기 결과가 **현재 릴리스와 동일**(천원 단위 절사 + ROUNDDOWN `-3`). 단 I11 비고 셀의 "백단위절사" 문자열은 FR-13/§4-7 에 따라 **"천단위 절사" 로 교체** — 이는 용어 통일 의도이며 의도된 차이. |
| FR-20 | 낙찰율 `>=100%` (bidRate >= 1.0) 또는 `<=0` 입력 시: FR-9 규칙에 따라 수식은 `ROUNDDOWN(...)` 단순 형태, I 비고 셀은 `<절사라벨>` 만 (또는 `unit == 1` 이면 `""`). |
| **FR-21** | **저장 후 재진입 복원 시나리오**: 저장 → 편집 페이지 재진입 → 드롭다운 `value` 가 저장값과 동일. TYPE_D 기존 레코드를 편집 페이지에서 열 때 드롭다운이 `10` 으로 override 되지 않음 (FR-6'). |

---

## 3. 비기능 요건 (NFR)

| ID | 내용 |
|----|------|
| NFR-1 | `./mvnw clean compile` 성공. |
| NFR-2 | 서버 재기동 후 착공계 편집 페이지 정상 (`/document/edit/{docId}`), 설계내역서 섹션 입력/저장/로드 정상. |
| **NFR-3 (수정)** | 회귀 검증 방식 — **XLSX 바이너리 비교 대신 핵심 셀 수식/표시값 비교**. 검증 셀 목록 고정:<br>• TYPE_A: `H11` 수식, `I11` 표시 문자열<br>• TYPE_B: `H11` 수식, `I11` 표시 문자열<br>• TYPE_D: `H8` 수식, `I8` 표시 문자열<br>Apache POI 로 다운로드된 xlsx 를 로드해 각 셀의 `getCellFormula()` / `getStringCellValue()` 를 읽어 **기대값 문자열과 정확 일치**. 기존 레코드 3건(각 TYPE 별 1건) 샘플 다운로드 결과의 H/I 셀 수식값이 현재 릴리스와 동일 (FR-19). 단 I 셀 표시 문자열은 "백단위절사"→"천단위 절사" 변경 허용. |
| NFR-4 | 신규 저장 후 엑셀 다운로드 → 핵심 셀이 입력값과 일치 (예: 91% + 천단위 절사 → H11 = `ROUNDDOWN((H4+H8)*0.91,-3)`, I11 = `"낙찰율 적용(91%)\n천단위 절사"`). |
| NFR-5 | 절사 단위 `1` 선택 + 낙찰율 `91` → I 비고 셀 = `"낙찰율 적용(91%)"` (개행+절사 문구 없음). |
| NFR-6 | 생성된 `.xlsx` 를 Microsoft Excel 에서 열었을 때 `#NAME?` / `#NUM!` 등 수식 오류 없음. `ROUNDDOWN(x, 0)` 포함. |
| NFR-7 | 견적서 측 `Quotation.rounddownUnit` 로직 변경 없음 — `QuotationService/Controller` 영향 없음. |
| NFR-8 | 설계내역서 두 미리보기(`calcEstPreview`, `previewEstimate`) 가 서버 왕복 없이 드롭다운 변경 즉시 반영 (100ms 이내). |
| **NFR-9** | `grep -rn "낙찰률" src/` 실행 결과 **0건** (FR-18). |

---

## 4. 의사결정 / 우려사항

### 4-1. 절사 단위 필드 위치 — ✅ `sectionData` JSON 내부 key (스키마 변경 없음)
- **근거**: `tb_document_detail.section_data` 는 JSONB 자유 스키마. 기존 관행(`estimateType`, `designDate` 등도 모두 JSON key)에 부합. 마이그레이션 SQL 불필요.
- **대안 기각**: `tb_document` 에 전용 컬럼 추가 → 설계내역서 전용 속성이 다른 docType 과 섞이게 되어 관심사 혼재.

### 4-2. 기본값 `1000` vs `1` — ✅ `1000` (천원 단위 절사)
- **근거**: 현재 설계내역서의 실질 동작이 `ROUNDDOWN(x, -3) = 천원 단위 절사`. 기존 데이터/출력과 동일해야 회귀 없음 (G3, FR-19).
- **견적서 기본값(1)과 다름**: 견적서는 `Quotation.java:68` 에서 `1` 기본값인데, V003 SQL 은 `1000` 기본값으로 어긋나 있음. 이 불일치는 본 스프린트 범위 외. 설계내역서만 `1000` 명시.

### 4-3. TYPE_C 제외 — ✅ 이번 스프린트 범위 외 (엑셀 수식)
- **근거**: TYPE_C 템플릿(`design_estimate_c_template.xlsx`) 는 14개 시트 각각의 셀 수식(`F16=ROUNDDOWN(E16*G16*0.01,-1)` 등)에 ROUNDDOWN 이 심어져 있어, Java 코드 한 지점 수정으로 해결 불가.
- **이번 스프린트의 TYPE_C 동작**: 엑셀 수식은 템플릿 기본값 유지. 미리보기(JS) 는 드롭다운 반영. **UI 에 안내 툴팁(FR-4'') 로 불일치 사실을 사용자에게 명시적으로 고지**.
- **후속 작업**: POI 로 모든 시트의 모든 수식 문자열을 치환하는 별도 스프린트. 본 문서에는 파일 경로로 참조하지 않는다.

### 4-4. 낙찰율 표기 (v1 사실 오류 정정) — ✅ "낙찰율" 로 통일
- **재검증(2026-04-24)**: `grep -rn "낙찰률" src/main/java/com/swmanager/system/service/ExcelExportService.java` 결과 **line 987 단 1건**. 나머지(line 373 주석, line 542, line 720, line 1088, line 1408 등)는 모두 이미 `"낙찰율"` 사용.
- **v1 수정**: v1 §4-4 "TYPE_B만 line 987, 720 혼재" 는 사실 오류였음 (line 720 은 이미 "낙찰율"). v2 에서 정정.
- **변경 범위**: `ExcelExportService.java:987` 단일 라인만 `"낙찰률"` → `"낙찰율"`.

### 4-5. `bidRate != 0.9696` 조건 제거 — ✅ 제거
- **근거**: 현 조건은 "템플릿 기본 수식(0.9696)이면 갱신 생략" 의미인데, 사용자가 정확히 96.96%를 입력하면 조건이 참이 되어 갱신되지 않는 빈틈. 의미 없는 특수 케이스.
- **결정**: `if (bidRate > 0)` 로 단순화. 0 이면 미적용(템플릿 기본 유지), 0 초과면 항상 갱신.

### 4-6. TYPE_D 기본값 override 정책 (v3 재작성 — `touched` 플래그 도입)
- **v2 정책의 한계**: "드롭다운 value === 1000" 조건만으로는 **"초기 default 1000"** 과 **"사용자가 수동으로 다시 1000 선택"** 을 구분 불가. 후자도 override 대상이 되어 사용자 의도 훼손.
- **v3 정책**: **`rounddownUnitTouched` 불린 상태 변수** (UI 스크립트 스코프)를 도입. 자동 제안은 **세 조건 모두 참** 일 때만:
  1. `docId === null` (신규 문서 — FR-6')
  2. `rounddownUnitTouched === false` (사용자 미조작 — FR-6'')
  3. 새로 선택된 `estimateType === "TYPE_D"` (FR-6')
- **touched 상태 전이 규칙** (FR-6''):
  - 페이지 로드 시 기존 `sectionData.rounddownUnit` 가 **1000** 이면 → `touched=false`
  - 페이지 로드 시 `sectionData.rounddownUnit` 가 **1000 이 아닌 허용값** 이면 → `touched=true` (기존 레코드 존중)
  - 페이지 로드 시 허용값 외(FR-6 로드 fallback 분기) → `touched=false` (fallback 후 신규처럼 대우)
  - 사용자가 드롭다운을 수동 변경하면 `touched=true` (자동 제안 이외의 모든 change 이벤트)
  - 자동 제안(FR-6')에 의한 `value` 변경은 DOM 조작으로만 적용 + `touched` 상태 건드리지 않음(코드상 "auto apply" 플래그로 내부 가드)
- **엣지케이스 검증** (Acceptance `[수동:브라우저]` 블록 참조):
  - 신규 문서 → TYPE_A 로드(default 1000, touched=false) → TYPE_D 선택 → **자동 10 제안** ✓
  - 위에서 사용자가 100 으로 바꿈(touched=true) → TYPE_A → TYPE_D 왕복 → **100 유지** ✓
  - 사용자가 100 → 다시 1000 으로 직접 선택(touched=true) → TYPE_D → **1000 유지** (v2 정책 대비 개선 포인트) ✓
  - 기존 TYPE_D(rounddownUnit=10) 레코드 로드(touched=true 초기화) → TYPE_A → TYPE_D 왕복 → **10 유지** ✓
- **근거**: 기존 TYPE_D 사용자(김해시 등)는 현재 `-1 = 십원 단위` 출력에 익숙. 기존 레코드 편집 시 자동 override 하면 의도치 않은 출력 변경 발생. 또 사용자가 "일부러 1000 으로 바꾼" 의사도 존중해야 함.

### 4-7. 용어 통일 (공백 + "백단위절사" → "천단위 절사") — ✅ `quotation-vat-rules.md` SSoT 따름
- **배경**: 현재 코드 `line 987` 의 `"백단위절사"` 문자열은 `swdept/docs/절사_규칙_가이드.md` 의 구(舊) 명명 체계("백단위 절사" = 1000 단위 절사 — 백원 자릿수 버림 의미).
- **반면**: `docs/product-specs/quotation-vat-rules.md` (신 SSoT) 은 `"천단위 절사"` = 1000 단위 절사 (공백 포함). 표기 체계가 정면 충돌.
- **결정**: 새 SSoT (`quotation-vat-rules.md`) 를 따른다. `"백단위절사"` → `"천단위 절사"` (공백 포함) 치환. **사용자 영향 고지**: 기존 TYPE_B 출력물의 I11 셀 문구가 `"낙찰률 적용(...)\n백단위절사"` → `"낙찰율 적용(...)\n천단위 절사"` 로 **시각적으로 달라짐**. 숫자 결과는 동일 (ROUNDDOWN -3 유지). 이는 용어 drift 를 끝내기 위한 의도된 변경.
- **장기 과제**: `swdept/docs/절사_규칙_가이드.md` 는 구 관리자 영역 문서이므로 본 스프린트에서 수정하지 않음. 신규 `docs/product-specs/` SSoT 만 정합성 확보.

### 4-8. TYPE_C 엑셀/미리보기 의도적 불일치 — ⚠ 사용자 고지 필수
- **현상**: TYPE_C 선택 시 UI 미리보기는 사용자가 선택한 절사 단위를 반영하지만, 실제 엑셀 파일은 템플릿 내장 수식(`-1` 십원 단위 절사) 그대로.
- **완화책**: FR-4'' 로 UI 툴팁 강제. 사용자가 사전에 인지할 수 있도록 절사 단위 드롭다운 바로 옆에 노출.
- **대안 기각**: (a) TYPE_C 에서 드롭다운 자체를 비활성화 → 데이터 저장 불가로 후속 스프린트에 불편. (b) TYPE_C 미리보기를 엑셀 기준으로 고정(`-1` 하드코딩) → 다른 TYPE 로 전환 후 되돌아올 때 UI 상태 일관성 깨짐.

### 4-9. Invalid `rounddownUnit` 처리 정책 (v3 신설) — ✅ 이중 방어 + 데이터 보존
- **배경**: `tb_document_detail.section_data` 는 JSONB 자유 스키마. 수동 SQL, 외부 마이그레이션, 잘못된 JS 로직 등으로 허용값(1/10/100/1000/10000) 외 값이 들어올 가능성 상존.
- **정책 (4개 진입점에서 이중삼중 방어)**:
  1. **클라이언트 저장 (FR-5')**: 제출 전 JS 에서 드롭다운 value 허용값 검증 — 서버로 오염 데이터 전송 최소화(방어적 중복이며 UX 즉시 피드백 목적 아님).
  2. **서버 저장 (FR-5')**: `DocumentController.saveDocument` 가 `design_estimate` 섹션 저장 직전 `rounddownUnit` 정규화. 허용값 외 → `1000` 치환 + WARN 로그. 예외 throw 하지 않음.
  3. **엑셀 생성 (FR-6''' + FR-10)**: `ExcelExportService.generateDesignEstimate()` 가 `estData` 로딩 직후 `normalizeRounddownUnit` 으로 정규화. 허용값 외(문자열 `"abc"` 포함) → `1000` + WARN 로그. 엑셀 생성은 성공.
  4. **로드 (FR-6)**: 편집 페이지 JS 가 로드 시 드롭다운 세팅 전 허용값 검증. 허용값 외 → `1000` + touched=false.
- **예외 throw 금지**: FR-11 `toExcelRoundDigitsArg(invalid)` 는 내부 유틸이므로 `IllegalArgumentException` 유지하되, **호출부는 항상 사전 정규화된 값만 전달**. 즉 4개 진입점 방어(위 1~4)에서 걸러지지 않은 케이스는 프로그래머 오류로 간주.
- **데이터 보존 우선순위**: "저장 거부" 보다 "정규화 후 저장 + 로그" 선호. 사용자가 입력한 다른 필드(낙찰율, 항목 등)가 `rounddownUnit` 검증 실패로 유실되는 것을 방지.

---

## 5. 영향 범위

| 계층 | 파일 | 유형 |
|------|------|------|
| Controller | `src/main/java/com/swmanager/system/controller/DocumentController.java` | **수정** (FR-5' 서버측 allow-list 정규화): `saveDocument()` 메서드 내에서 `design_estimate` 섹션 저장 루프에 `rounddownUnit` 허용값 검증 + `1000` 치환 + WARN 로그 1건 추가 (~5줄). 기타 섹션 플로우 영향 없음. |
| Service | `src/main/java/com/swmanager/system/service/ExcelExportService.java` | 수정: 진입점(179-229), TYPE_A `fillSummarySheet` (326-385), TYPE_B `generateFromTypeBTemplate` (941-997), TYPE_D `generateFromSwTemplate` (1036-1098), 신규 유틸 `toExcelRoundDigitsArg`, `rounddownLabel`, `normalizeRounddownUnit` 3개 + 낙찰률→낙찰율 1건 치환(line 987) |
| Entity | `Document.java`, `DocumentDetail.java` | 변경 없음 |
| Template (UI) | `src/main/resources/templates/document/doc-commence.html` | 수정: `<select id="est_rounddownUnit">` 추가(부가세 별도 행 다음), TYPE_C 툴팁 요소, JS 함수 추가/수정 (`calcEstPreview`, `previewEstimate`, `collectSections`, `rounddownLabel`, estimateType/드롭다운 onchange 핸들러, 로드 시 복원 로직) |
| Excel 템플릿 | `src/main/resources/templates/excel/design_estimate_*.xlsx` | **변경 없음** |
| DB | `tb_document_detail.section_data` JSONB | 스키마 변경 없음, JSON key 추가만 |
| 문서 | `docs/product-specs/design-estimate-bid-rate-rounddown.md` (본 문서) | 신규 (v2) |
| 개발계획서 | `docs/exec-plans/design-estimate-bid-rate-rounddown.md` | 신규 (다음 단계) |

**수정 파일: 3 (`DocumentController.java` / `ExcelExportService.java` / `doc-commence.html`). 신규 문서: 2. DB/API 계약 변경: 없음. 엑셀 템플릿 바이너리: 변경 없음.**

---

## 6. 리스크 평가

| 리스크 | 수준 | 완화책 |
|--------|------|--------|
| **TYPE_C 엑셀/미리보기 의도적 불일치 → 사용자 혼란** (codex 지적 #9) | 높음 | FR-4'' UI 툴팁 강제. 개발계획서에서 디자인 확인(아이콘 위치, 대체 텍스트). 리뷰어가 UI 사진 캡처로 확인 |
| 기존 TYPE_D 레코드 override 로 출력 변화 | 중간 | §4-6 이중 조건 정책 (신규 문서 + 미저장). FR-21 저장-재로드 복원 검증 |
| 기존 TYPE_B 출력의 I11 문구가 "백단위절사" → "천단위 절사" 로 시각 변경 | 중간 | §4-7 에 의도된 용어 통일임을 명시. 릴리스 노트/커밋 메시지에 고지 |
| 기존 sectionData 에 `rounddownUnit` 없는 경우 | 낮음 | `getOrDefault(1000)` 로 안전 처리. FR-19 |
| Excel `ROUNDDOWN(x, 0)` 호환성 | 낮음 | POI 출력 후 Microsoft Excel 수식 실행 테스트 (NFR-6). `ROUNDDOWN` 2nd arg=0 정상 지원 확인됨 |
| 낙찰율 입력 소수점 (예: 90.5%) | 낮음 | `Math.round(bidRate*100)` 이미 정수화. I 셀은 반올림 정수로 표시(의도) |
| "낙찰률→낙찰율" 치환이 타 파일 영향 | 낮음 | FR-18 범위를 `ExcelExportService.java` 로 한정. NFR-9 검증 |
| JS `rounddownLabel` 과 Java `rounddownLabel` drift | 중간 | 개발계획서에 "양쪽 함수 동시 수정 체크리스트" + Acceptance 라벨 매핑 5종 각각 스냅샷 검증 |
| XLSX 바이너리 비교의 flakiness (v1 NFR-3) | 중간 | NFR-3 을 셀 수식/문자열 비교로 변경 (codex 지적 #6 반영) |
| FR-11/12 부호 혼동 재발 | 낮음 | FR-11 계약을 "이미 음수 반환" 으로 명문화. 유틸 테스트(단위 입력 5종 + 예외 입력 1종)를 개발계획서 checklist 에 |

---

## 7. 수락 기준 (Acceptance)

스프린트 종료 시 다음이 모두 참이어야 한다.

### 7-0. 검증 구분 태그 (9 태그 체계)
각 항목 앞의 태그는 **테스트 소유권 + 도구** 를 명시. 총 **9종**:

| # | 태그 | 의미 | 예시 도구 |
|---|------|------|-----------|
| 1 | `[자동:빌드]` | `./mvnw clean compile` 등 빌드/정적 검사 | Maven |
| 2 | `[자동:POI]` | **xlsx 셀 수식/표시값 직접 비교 전용** (생성된 엑셀을 POI 로 로드해 특정 셀 검증) | Apache POI + JUnit |
| 3 | `[자동:통합/API]` | HTTP 저장/조회 왕복 시나리오 (`POST /document/api/save` + `GET .../excel`) | Spring `MockMvc` 또는 `RestAssured` |
| 4 | `[자동:JUnit]` | 순수 유틸 함수 단위테스트 (외부 I/O 없음) | JUnit |
| 5 | `[자동:로그검증]` | 서버 로그(WARN/INFO)에 특정 패턴 포함 여부 | `@Slf4jMemoryAppender` 또는 logback list-appender |
| 6 | `[자동:grep]` | `grep -rn` 명령 결과 개수 | shell |
| 7 | `[자동:codex]` | 기획서/개발계획서 충족도 codex CLI 판정 (CLAUDE.md §3) | codex CLI |
| 8 | `[수동:브라우저]` | 개발자/QA 가 브라우저에서 UI 조작으로 확인 (툴팁, onchange, touched 엣지케이스) | 브라우저 |
| 9 | `[수동:Excel]` | Microsoft Excel 데스크톱에서 파일 열어 수식 오류 없음 확인 | Excel |

**사용 원칙**: 하나의 Acceptance 항목이 여러 도구를 필요로 하면 **태그를 쉼표로 복수 표기** (예: `[자동:통합/API, 자동:POI]`) 하거나 **항목을 분리**.

### 7-1. Fixture 선정 기준 (회귀 검증용 3건 식별)
NFR-3 회귀 검증에서 "기존 DB 레코드 3건" 의 선정 조건:
1. `tb_document.doc_type === 'COMMENCE'` 이고 `tb_document_detail.section_key === 'design_estimate'` 인 레코드.
2. `section_data->>'estimateType'` 이 `TYPE_A`, `TYPE_B`, `TYPE_D` 각 1건 (TYPE_C 는 이번 스프린트 엑셀 수식 변경 없음으로 회귀 fixture 불필요).
3. `section_data ? 'rounddownUnit'` 결과가 **`false`** — 즉 **구 스키마(rounddownUnit 필드 없음)** 인 레코드. 이는 FR-19 (필드 미포함 → 기본값 `1000` 재현) 의 직접 검증 조건.
4. 대안: 위 조건을 충족하는 레코드가 DB 에 없을 경우, 개발계획서에서 **fixture SQL 스크립트** 를 작성해 재현 데이터를 조성. 이 경우 스크립트와 `docId` 목록을 개발계획서에 고정.
- **개발계획서에서 확정할 것**: 실제 사용할 `docId` 3개 또는 fixture SQL 경로.

### 7-2. 빌드/기본
- [ ] `[자동:빌드]` `./mvnw clean compile` 성공 (NFR-1)
- [ ] `[수동:브라우저]` 서버 재기동 후 착공계 편집 페이지(`/document/edit/{docId}`) 정상 로드 — 콘솔 에러 없음 (NFR-2)

### 7-3. UI 기능
- [ ] `[수동:브라우저]` 착공계 §⑧ 에 `est_rounddownUnit` 드롭다운 노출. 옵션 5개 모두 `quotation-vat-rules.md §43-51` 공백 포함 라벨과 글자단위 일치 (FR-1)
- [ ] `[수동:브라우저]` estimateType `TYPE_C` 선택 시 드롭다운 옆에 안내 툴팁 표시, 다른 TYPE 선택 시 숨김 (FR-4'')
- [ ] `[수동:브라우저]` **TYPE_D touched 엣지케이스 4종** (§4-6 검증 목록):
  - (a) 신규 문서, default 1000, touched=false → TYPE_D 선택 → 드롭다운 자동 `10`
  - (b) (a) 상태에서 사용자가 수동으로 `100` 으로 변경 → TYPE_A → TYPE_D 왕복 → **`100` 유지**
  - (c) (b) 상태에서 사용자가 다시 수동으로 `1000` 선택 → TYPE_D → **`1000` 유지** (v3 touched 정책 핵심 검증)
  - (d) 기존 TYPE_D(저장값 `10`) 레코드 로드 → drop-down `10` + touched=true → TYPE_A → TYPE_D 왕복 → **`10` 유지**

### 7-4. 미리보기 2종 (FR-3, FR-3', FR-4)
- [ ] `[수동:브라우저]` 91% + 천단위 절사 → 자동 산출 미리보기 하단 비고 = `"(낙찰율 91% 적용, 천단위 절사)"`
- [ ] `[수동:브라우저]` 91% + 절사 없음 → 비고 = `"(낙찰율 91% 적용)"`
- [ ] `[수동:브라우저]` 100% + 천단위 절사 → 비고 = `"(천단위 절사)"` (FR-20)
- [ ] `[수동:브라우저]` 100% + 절사 없음 → 비고 행 자체 비노출
- [ ] `[수동:브라우저]` 확대 미리보기(`previewEstimate`) 의 계산/표시값이 자동 산출 미리보기와 동일 (FR-3')
- [ ] `[수동:브라우저]` 드롭다운 변경 100ms 이내 양쪽 미리보기 갱신 (NFR-8)

### 7-5. 엑셀 출력 — TYPE_A (FR-8/9/12) `[자동:POI]`
- [ ] 91% + 천단위 절사 → H11 수식 = `"ROUNDDOWN((H4+H8)*0.91,-3)"` (문자열 정확 일치)
- [ ] 91% + 천단위 절사 → I11 표시값 = `"낙찰율 적용(91%)\n천단위 절사"`
- [ ] 85% + 만단위 절사 → H11 = `"ROUNDDOWN((H4+H8)*0.85,-4)"`, I11 = `"낙찰율 적용(85%)\n만단위 절사"`
- [ ] 91% + 절사 없음 → H11 = `"ROUNDDOWN((H4+H8)*0.91,0)"`, I11 = `"낙찰율 적용(91%)"`
- [ ] 100% + 천단위 절사 → H11 = `"ROUNDDOWN((H4+H8),-3)"`, I11 = `"천단위 절사"`
- [ ] 100% + 절사 없음 → H11 = `"ROUNDDOWN((H4+H8),0)"`, I11 = `""` (빈 문자열) (FR-20)

### 7-6. 엑셀 출력 — TYPE_B (FR-13) `[자동:POI]`
- [ ] 91% + 천단위 절사 → H11 = `"ROUNDDOWN((H10+H9)*0.91,-3)"`, I11 = `"낙찰율 적용(91%)\n천단위 절사"`
- [ ] I11 문자열에 `"낙찰률"` (률) 포함 안 됨 (FR-18 확인)

### 7-7. 엑셀 출력 — TYPE_D (FR-14) `[자동:POI]`
- [ ] 91% + 십원단위 절사(기본값) → H8 = `"ROUNDDOWN((H6+H7)*0.91,-1)"`, I8 = `"낙찰율 적용(91%)\n십원단위 절사"`

### 7-8. 유틸 함수 (FR-11/16) `[자동:JUnit]`
- [ ] `toExcelRoundDigitsArg(1) == 0`, `(10) == -1`, `(100) == -2`, `(1000) == -3`, `(10000) == -4`
- [ ] `toExcelRoundDigitsArg(500)` → `IllegalArgumentException`
- [ ] `rounddownLabel(1) == ""`, `(10) == "십원단위 절사"`, `(100) == "백원단위 절사"`, `(1000) == "천단위 절사"`, `(10000) == "만단위 절사"`
- [ ] `rounddownLabel(500)` → `IllegalArgumentException`

### 7-9. Invalid value 방어 (FR-5', FR-6''', §4-9)
엑셀/저장/로드 각 진입점별로 태그 분리:
- [ ] `[자동:통합/API, 자동:POI, 자동:로그검증]` `sectionData` 에 `rounddownUnit=500` 을 직접 SQL UPDATE 후 `GET /document/api/excel/{docId}` → HTTP 200 + 다운로드 xlsx 의 H11 에 `",-3)"` (1000 fallback 확인) + 서버 로그에 WARN 패턴 `"invalid value '500' → using default 1000"` 1건
- [ ] `[자동:통합/API, 자동:POI, 자동:로그검증]` 위와 동일하되 `rounddownUnit="abc"` 문자열 값 → 동일 fallback 동작, 파싱 예외 없음 (FR-10 정규화 순서)
- [ ] `[자동:통합/API, 자동:로그검증]` `POST /document/api/save` 바디에 `rounddownUnit:500` 전송 → HTTP 성공 + DB 저장 후 `sectionData->>'rounddownUnit'` 조회값 `'1000'` (서버 정규화, FR-5') + WARN 로그 1건
- [ ] `[수동:브라우저]` 편집 페이지 로드 시 invalid 값은 드롭다운 `"천단위 절사"(1000)` 표시 + `rounddownUnitTouched === false` (FR-6 로드 fallback 분기)

### 7-10. 회귀/하위 호환 (NFR-3)
- [ ] `[자동:POI]` §7-1 선정 기준의 3개 fixture docId 각각 다운로드 → 각 TYPE 의 핵심 셀(H11/I11, H8/I8) 수식+표시값이 현재 릴리스 결과와 비교. **수식은 동일, I 셀 문자열은 TYPE_B 의 "백단위절사" → "천단위 절사" 변경만 허용**
- [ ] `[수동:Excel]` 위 3개 파일 Microsoft Excel 에서 열 때 수식 오류(#NAME?/#NUM! 등) 없음 (NFR-6)
- [ ] `[자동:grep]` `grep -rn "낙찰률" src/` 결과 **0건** (NFR-9, FR-18)

### 7-11. 저장/로드 (FR-21)
- [ ] `[수동:브라우저]` 각 TYPE 에서 드롭다운 값(1/10/100/1000/10000) 선택 → 저장 → 페이지 재진입 → 드롭다운 값 유지 (5회 반복)

### 7-12. codex 최종 검증
- [ ] `[자동:codex]` codex 가 FR-1 ~ FR-21, NFR-1 ~ NFR-9 각 항목을 "충족/미충족/부분충족" 으로 판정, **FR 미충족 0건**

---

## 8. 승인 요청

본 기획서(v6)에 대한 codex 최종 검토 및 사용자 최종승인을 요청합니다.

**codex v6 최종 검토 중점 항목**:
1. v5 잔존 4건 **반영 완결성**:
   - §8 버전 표기 `v4` → `v6` 갱신 여부 (표제 + 도입 문구)
   - FR-6''' 제목이 **"엑셀 생성 fallback"** 으로 정정되어 본문 (`ExcelExportService.generateDesignEstimate()` 에서의 fallback) 과 일치하는지. FR-6(클라이언트 로드 fallback) 과의 경계가 분명해졌는지
   - v4→v5 변경 요약의 FR-6''' 오기 교정 여부
2. v1~v5 누적 반영 사항에 회귀/모순 없는지 (특히 FR-11, §4-6 touched 정책, §4-9 4개 진입점 서술)
3. 최종 승인 가능 여부. 미결 이슈가 있다면 **개발계획서 단계로 이월** 가능한지 구체 판단
