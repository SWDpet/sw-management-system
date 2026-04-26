---
tags: [dev-plan, document, design-estimate, excel-export]
sprint: "design-estimate-dynamic-formula-01"
status: draft-v6
created: "2026-04-24"
revised: "2026-04-24 (v6 — codex v5 잔존 1건 반영)"
---

# [개발계획서] 설계내역서 엑셀 — 낙찰율 비고 동적화 + 절사 단위 UI 선택 (v6)

- **작성팀**: 개발팀
- **작성일**: 2026-04-24
- **근거 기획서**: [design-estimate-bid-rate-rounddown.md (v6)](../product-specs/design-estimate-bid-rate-rounddown.md)
- **상태**: v6 (codex v5 잔존 1건 반영 / 최종 승인 재검토 대기)
- **선행 커밋**: `756813b`

## v5→v6 변경 요약
codex v5 검토 잔존 1건 반영 (새 결함 0, 문서 자기모순 해소):
1. **§3 롤백 전략 line 514 `git revert <merge-commit>` → `git revert -m 1 <merge-commit>`** — merge commit 되돌림엔 `-m 1` 옵션 필수. 같은 문서 line 516 과 일관성 확보.

## v4→v5 변경 요약
codex v4 검토 잔존 1건 반영:
1. **§3 롤백 전략 `git revert -m 1` 오용 교정** — `-m 1` 은 merge commit 전용이라 일반 커밋 집합 롤백에 부적합. "스프린트 전체 중단" 시나리오 명령을 **`git log master..HEAD --oneline` → 각 SHA 를 `git revert <sha>` 로 개별 되돌림**으로 교체. `-m 1` 은 **master 머지 완료 후 merge commit 되돌림 전용 용례**로만 남김.

## v3→v4 변경 요약
codex v3 검토 잔존 2건 반영:
1. **§6 "확정된 Fixture" 섹션 신설** — Step 15/15.5/FR-7 이 참조하는 SSoT 섹션. docId 3개 기록 테이블 + fixture SQL 파일 기록 테이블 + 갱신 절차 명시. 개발 시작 시 Step 15 결과로 채움.
2. **`V028` 하드코딩 잔존 2곳 제거** — T21 pass 기준 `"빈 출력 (또는 fixture V028 하나)"` → `"빈 출력, 또는 §6 에 기록된 파일명만 출력"`. §3 롤백 전략의 `V028_design_estimate_fixture.sql` → `V<NEXT>_design_estimate_fixture.sql` + §6 기록 참조. (Step 15 본문의 "예: V028" 예시 표현은 유지 — 역사적 문서 예시이므로 고정 아님).

## v2→v3 변경 요약
codex v2 검토 잔존 3건 (번호 정합성) 반영:
1. **Step 제목 T 번호 갱신** — Step 10~16 본문 제목의 `(T1/T2)`, `(T3~T5)`, `(T6)`, `(T7~T10)`, `(T11)`, `(NFR-9, T12)` 등을 재구성된 T 체계(T1~T23, T28, T29)로 교정.
2. **교차표 미매핑 해소** — 신규 **T28** (NFR-2 페이지 로드 + 콘솔 에러 0) + **T29** (§7-9 마지막 브라우저 로드 fallback, FR-6 분기) 추가. 교차표를 "Acceptance 섹션 × 항목 수 × T" 단위로 재작성해 1:1 매핑 완결.
3. **fixture fallback 파일명 가변화** — `V028` 하드코딩 제거. `V<NEXT>_design_estimate_fixture.sql` 템플릿 + "직전 최고 번호 +1 확인 후 §6 에 기록" 절차 명시. FR-7 체크도 §6 기록 파일명 기반으로 동적화.

## v1→v2 변경 요약
codex v1 검토 지적 5건 전수 반영:
1. **Step 15 fixture 쿼리** — `DISTINCT ON` 으로 estimateType 당 1건 보장 + 결과 부족 시 `V028_design_estimate_fixture.sql` fallback 경로 명시.
2. **T 매핑 1:1 재구성** — T 를 23개(핵심) + 4개(보조) 로 재분해, §2-2 에 Acceptance→T 교차표 추가. T9 의 "5/5" 오타 → T14 "6/6" 으로 교정.
3. **롤백 전략** — `git reset --hard` / `git checkout --` / `git clean -f` 제거. 파일 단위 역패치 + `git revert` 중심으로 재작성. 맨 윗줄에 "CLAUDE.md Git Safety Protocol 준수" 명시.
4. **JS `bidRate` 단위 고정** — `bidRatePct` (퍼센트, 91) + `bidRatio` (비율, 0.91) 변수명을 Step 8-3/8-4/8-8 전체에서 일관되게 사용. 공통 함수 `applyBidAndRound(v, bidRatio, unit)` 는 **비율 형식** 받음. `docId` 공급원도 `document.body.dataset.docId` 로 명시 + 구현 직전 `rg` 로 컨벤션 확인 절차 추가.
5. **누락 FR/NFR 전용 Step/T** — Step 15.5 신설 (FR-7, FR-15, NFR-4, NFR-7 검증). T19/T20/T21/T22 로 개별 등록.

---

## 0. 기획서 v6 수락 후 잔존 문서 정합성 2건

codex 가 기획서 v6 최종 검토에서 지적한 2건(구현 무관, 문서 정합성):
- (a) 기획서 `§4-6 touched 상태 전이 규칙` 의 `(FR-6''')` → `(FR-6)` 오타
- (b) 기획서 `§5 영향 범위` 하단 `수정 파일: 2` → 실제 **3** (DocumentController/ExcelExportService/doc-commence.html)

**처리 방침**: 개발 완료 직전 Step (N-1) "문서 정합성 정리" 에서 함께 교정. 별도 스프린트 분리하지 않음.

---

## 1. 작업 순서 (Stepwise)

### Step 1 — 사전 스캔 (baseline 기록)
구현 전 현재 상태를 문자열 매치로 기록, Step N 회귀 검증 기준점으로 사용.

```bash
rg -n "0.9696" src/main/java/com/swmanager/system/service/ExcelExportService.java       # 기대: 2 hits (line 374, 376)
rg -n "백단위절사" src/main/java/com/swmanager/system/service/ExcelExportService.java    # 기대: 1 hit (line 987)
rg -n "낙찰률" src/main/java/com/swmanager/system/service/ExcelExportService.java         # 기대: 1 hit (line 987)
rg -n "est_rounddownUnit" src/main/resources/templates/document/doc-commence.html        # 기대: 0 hits
rg -n "rounddownUnit" src/main/java/com/swmanager/system/controller/DocumentController.java  # 기대: 0 hits
```

### Step 2 — ExcelExportService 공통 유틸 3종 신설 (FR-10/11/16)

**2-1. `toExcelRoundDigitsArg(int unit)` 추가** — `ExcelExportService` 클래스 최하단 private static 영역.
- 계약: `1→0, 10→-1, 100→-2, 1000→-3, 10000→-4`. 그 외 → `IllegalArgumentException("rounddownUnit must be one of {1,10,100,1000,10000}, got: " + unit)`.
- **반환값이 이미 음수 포함**. 호출부는 `"," + toExcelRoundDigitsArg(unit) + ")"` 로 `-` 추가 없이 삽입.

**2-2. `rounddownLabel(int unit)` 추가** — 동일 영역. `quotation-vat-rules.md §43-51` 공백 포함 표기 그대로:
```java
private static String rounddownLabel(int unit) {
    switch (unit) {
        case 1: return "";
        case 10: return "십원단위 절사";
        case 100: return "백원단위 절사";
        case 1000: return "천단위 절사";
        case 10000: return "만단위 절사";
        default: throw new IllegalArgumentException("rounddownUnit must be one of {1,10,100,1000,10000}, got: " + unit);
    }
}
```

**2-3. `normalizeRounddownUnit(Object raw)` 추가** — 허용값 외(null, 비수치, 허용값 외 숫자) → `1000` fallback + WARN 로그.
```java
private int normalizeRounddownUnit(Object raw, Integer docIdForLog) {
    Set<Integer> ALLOWED = Set.of(1, 10, 100, 1000, 10000);
    try {
        if (raw == null) return 1000;
        int v;
        if (raw instanceof Number) v = ((Number) raw).intValue();
        else v = Integer.parseInt(String.valueOf(raw).trim());
        if (ALLOWED.contains(v)) return v;
        log.warn("design_estimate.rounddownUnit invalid value '{}' → using default 1000 (docId={})", raw, docIdForLog);
        return 1000;
    } catch (NumberFormatException ex) {
        log.warn("design_estimate.rounddownUnit invalid value '{}' → using default 1000 (docId={})", raw, docIdForLog);
        return 1000;
    }
}
```
- Logger: 기존 클래스에 `private static final Logger log = LoggerFactory.getLogger(ExcelExportService.class);` 없으면 추가.

**2-4. 검증**: `./mvnw -q compile` → BUILD SUCCESS.

### Step 3 — `generateDesignEstimate(docId)` 진입점 수정 (FR-10)

- `estimateType` 추출 아래에 다음 줄 추가 (line ~193):
  ```java
  int rounddownUnit = normalizeRounddownUnit(estData.get("rounddownUnit"), docId);
  ```
- TYPE_B/TYPE_D/TYPE_A 호출 시그니처에 `rounddownUnit` 파라미터 전달 (`generateFromTypeBTemplate(..., rounddownUnit)`, `generateFromSwTemplate(..., rounddownUnit)`, `generateFromTemplate(..., rounddownUnit)`).
- `generateFromTemplate()` 내부에서 `fillSummarySheet(wb, hwItems, swItems, bidRate, rounddownUnit)` 로 전달.

### Step 4 — TYPE_A `fillSummarySheet` 수정 (FR-8, FR-9, FR-12)

Line 326-385 구간. 핵심 변경:
- 시그니처: `double bidRate` → `double bidRate, int rounddownUnit`.
- 기존 `if (bidRate > 0 && bidRate != 0.9696)` → **`if (bidRate > 0)`** (FR-8, §4-5).
- 수식: `"ROUNDDOWN((H4+H8)*" + bidRate + ",-3)"` → `"ROUNDDOWN((H4+H8)*" + bidRate + "," + toExcelRoundDigitsArg(rounddownUnit) + ")"`.
- **신규 I11 셀 업데이트 로직 추가** (FR-9 규칙 적용). 헬퍼 추출 권장:
  ```java
  private String buildRemarkCellValue(double bidRate, int unit) {
      boolean bidActive = bidRate > 0 && bidRate < 1.0;
      String label = rounddownLabel(unit); // "" or "십원단위 절사" 등
      if (bidActive && !label.isEmpty()) return "낙찰율 적용(" + Math.round(bidRate * 100) + "%)\n" + label;
      if (bidActive) return "낙찰율 적용(" + Math.round(bidRate * 100) + "%)";
      if (!label.isEmpty()) return label;
      return "";
  }
  ```
- `fillSummarySheet` 말미에서 I11(row 10, col 8) 에 `buildRemarkCellValue(bidRate, rounddownUnit)` 설정.
- `bidRate == 0` 이어도 rounddownUnit 변경 시 수식/라벨 갱신되도록 블록을 분리(수식은 bidRate>0 조건 유지, I11 는 항상 갱신).

### Step 5 — TYPE_B `generateFromTypeBTemplate` 수정 (FR-13)

Line 941-997 구간. 시그니처에 `int rounddownUnit` 추가.
- H11 수식 (line 979): `"-3"` → `"," + toExcelRoundDigitsArg(rounddownUnit) + ")"`.
- else 분기 (line 981): 동일.
- I11 라벨 (line 987): **`"낙찰률 적용(... %)\n백단위절사"` 하드코딩 완전 제거**, `buildRemarkCellValue(bidRate, rounddownUnit)` 호출로 교체. 낙찰"률"→낙찰"율" 통일 자동 반영 (FR-18).

### Step 6 — TYPE_D `generateFromSwTemplate` 수정 (FR-14)

Line 1036-1098 구간. 시그니처에 `int rounddownUnit` 추가.
- H8 수식 (line 1079, 기존 `-1`): `"ROUNDDOWN((H6+H7)*" + bidRate + "," + toExcelRoundDigitsArg(rounddownUnit) + ")"`.
- else 분기 (line 1081): `"ROUNDDOWN(H6+H7," + toExcelRoundDigitsArg(rounddownUnit) + ")"`.
- I8 라벨 (line 1088, 기존 `"낙찰율 적용(...%)"` 만): `buildRemarkCellValue(bidRate, rounddownUnit)` 호출로 교체. 절사 단위 문구 포함.

### Step 7 — `DocumentController.saveDocument` 서버측 검증 (FR-5')

`saveDocument()` 메서드 내 `sections` 루프(line 391-402) 안에서 `sectionKey.equals("design_estimate")` 인 경우에만 정규화:
```java
if ("design_estimate".equals(sectionKey) && sectionData != null) {
    Object raw = sectionData.get("rounddownUnit");
    Set<Integer> ALLOWED = Set.of(1, 10, 100, 1000, 10000);
    Integer v = null;
    try {
        if (raw instanceof Number) v = ((Number) raw).intValue();
        else if (raw != null) v = Integer.parseInt(String.valueOf(raw).trim());
    } catch (NumberFormatException ignored) { /* v stays null */ }
    if (v == null || !ALLOWED.contains(v)) {
        log.warn("design_estimate.rounddownUnit invalid value '{}' → normalized to 1000 (docId={})", raw, doc.getDocId());
        sectionData.put("rounddownUnit", 1000);
    }
}
documentService.saveSection(doc.getDocId(), sectionKey, sectionData, i);
```
- Logger 없으면 상단에 `private static final Logger log = LoggerFactory.getLogger(DocumentController.class);` 추가.
- 다른 섹션 플로우에는 영향 없음.

### Step 8 — `doc-commence.html` UI + JS 전반 수정

**8-1. HTML: 절사 단위 드롭다운 + TYPE_C 툴팁** (FR-1, FR-4'')
- 부가세 별도 체크박스 행 직후에 한 행 추가. `select#est_rounddownUnit` 에 option 5개(`1/10/100/1000/10000`, 공백 포함 라벨, default `value="1000"`).
- 드롭다운 옆에 TYPE_C 안내 아이콘 + `<span id="estTypeCHint" hidden>…</span>` — 초기 hidden. estimateType 이 TYPE_C 일 때만 `hidden=false`.

**8-2. JS: `rounddownLabel(unit)` 함수** (FR-17) — Java FR-16 과 글자단위 일치:
```javascript
function rounddownLabel(unit) {
    switch (Number(unit)) {
        case 1: return '';
        case 10: return '십원단위 절사';
        case 100: return '백원단위 절사';
        case 1000: return '천단위 절사';
        case 10000: return '만단위 절사';
        default: return '';
    }
}
```

**JS 단위 계약 고정** (이하 Step 8-3~8-8 공통):
- 변수명 `bidRatePct` = 사용자 입력 퍼센트 값 (예: `91`). 인풋 `#est_bidRate.value` 로부터 `parseFloat`.
- 변수명 `bidRatio` = 비율 형식 (예: `0.91`). `bidRatePct / 100.0` 로 명시적 변환.
- **모든 내부 계산/판정은 `bidRatio` 기준** (예: `bidRatio > 0 && bidRatio < 1.0`). 표시/메시지만 `bidRatePct` 사용.
- 기존 `var bidRate = bidRateInput / 100.0;` (line 1819) 는 `var bidRatio = bidRatePct / 100.0;` 로 이름 명시.

**8-3. JS: `calcEstPreview()` 수정** (FR-3, FR-4)
- 기존 `/ 1000) * 1000` 하드코딩 모두 제거. 다음 불변 변수 셋업:
  ```javascript
  var bidRatePct = parseFloat(document.getElementById('est_bidRate').value) || 0;   // 91
  var bidRatio   = bidRatePct / 100.0;                                               // 0.91
  var unit       = parseInt(document.getElementById('est_rounddownUnit').value, 10) || 1000;
  ```
- 금액 계산은 §8-4 의 공통 함수 `applyBidAndRound(v, bidRatio, unit)` 로 교체.
- 미리보기 패널 끝부분 비고 라벨:
  ```javascript
  var parts = [];
  if (bidRatio > 0 && bidRatio < 1.0) parts.push('낙찰율 ' + Math.round(bidRatePct) + '% 적용');
  if (unit > 1) parts.push(rounddownLabel(unit));
  if (parts.length) previewHtml += '<div class="text-muted">(' + parts.join(', ') + ')</div>';
  ```

**8-4. JS: `previewEstimate()` + 공통 함수** (FR-3')
- 공통 함수 추출 — **`bidRatio` 비율 형식을 받음** (퍼센트 아님):
  ```javascript
  function applyBidAndRound(v, bidRatio, unit) {
      var afterBid = (bidRatio > 0 && bidRatio < 1.0) ? v * bidRatio : v;
      return unit === 1 ? afterBid : Math.floor(afterBid / unit) * unit;
  }
  ```
- `calcEstPreview()` / `previewEstimate()` 모두 위 공통 함수 호출. `previewEstimate` 내부에서도 `bidRatio = bidRatePct / 100.0` 로 먼저 변환.

**8-5. JS: `collectSections()` 수정 — 저장 시 필드 포함 + allow-list** (FR-5, FR-5' 클라이언트)
```javascript
var unitRaw = parseInt(document.getElementById('est_rounddownUnit').value, 10);
var ALLOWED = [1, 10, 100, 1000, 10000];
var unitFinal = ALLOWED.indexOf(unitRaw) >= 0 ? unitRaw : 1000;
```
`sectionData` 에 `rounddownUnit: unitFinal` 포함.

**8-6. JS: 편집 페이지 로드 시 복원 + touched 초기화** (FR-6, FR-6'')
- 기존 design_estimate 섹션 로드 코드(line ~738) 에 추가:
  ```javascript
  var loadedUnit = data.rounddownUnit;
  var ALLOWED = [1, 10, 100, 1000, 10000];
  var finalUnit = ALLOWED.indexOf(Number(loadedUnit)) >= 0 ? Number(loadedUnit) : 1000;
  document.getElementById('est_rounddownUnit').value = String(finalUnit);
  window._rounddownUnitTouched = (finalUnit !== 1000); // 기획서 §4-6 규칙
  ```

**8-7. JS: 드롭다운 `onchange` 핸들러** (FR-4', FR-6'')
- `document.getElementById('est_rounddownUnit').addEventListener('change', function(ev) { if (!ev.isTrusted) return; window._rounddownUnitTouched = true; calcEstPreview(); });`
- **`ev.isTrusted === false`** 는 프로그램 주도 change (FR-6' 자동 제안) → touched 상태 변경 금지.

**8-8. JS: `estimateType` `onchange` 핸들러 — TYPE_D 자동 제안** (FR-6')

**`docId` 공급원 확정**: 기존 템플릿 `doc-commence.html` 은 편집 모드일 때 전역 변수 `window.DOC_ID` 또는 `<body data-doc-id="...">` 중 하나로 컨벤션이 잡혀 있을 가능성이 높다. 구현 직전 다음으로 확인:
```bash
rg -n "docId|DOC_ID|data-doc-id" src/main/resources/templates/document/doc-commence.html
```
- 결과가 있으면 그 관행을 따름.
- 결과가 없으면 본 스프린트에서 **`<body data-doc-id="[[${docId} ?: '']]">` 를 Thymeleaf 에 추가** + Step 13 체크리스트에 포함.

```javascript
document.getElementById('estimateType').addEventListener('change', function() {
    var raw = document.body.dataset.docId;              // 위 컨벤션 확정 후 사용
    var docId = (raw && raw !== '') ? raw : null;        // 신규 문서 → null
    var dd = document.getElementById('est_rounddownUnit');
    // TYPE_C 툴팁 표시/숨김
    document.getElementById('estTypeCHint').hidden = (this.value !== 'TYPE_C');
    // TYPE_D 자동 제안
    if (docId === null && window._rounddownUnitTouched === false && this.value === 'TYPE_D') {
        // 프로그램 주도 change — touched 세팅 금지 (isTrusted=false 로 발생)
        dd.value = '10';
    }
    calcEstPreview();  // 자동/수동 경로 공통 미리보기 재계산
});
```

**8-9. 초기화 시 estTypeCHint 상태 동기화**: 로드 직후 `estimateType` 초기값 기준으로 `estTypeCHint.hidden` 세팅.

### Step 9 — 빌드 & 기동 확인
```bash
./mvnw -q clean compile    # BUILD SUCCESS
bash server-restart.sh     # Started SwManagerApplication + ERROR 0
```

### Step 10 — 유틸 단위테스트 (T1~T4, FR-11/16) `[자동:JUnit]`

`src/test/java/com/swmanager/system/service/ExcelExportServiceUtilTest.java` 신설 (또는 기존 테스트 클래스 존재 시 그곳에 추가):
- 5개 허용값 + 1개 invalid 입력 → 기획서 Acceptance §7-8 과 동일 8 케이스.
- 유틸이 package-private 이면 테스트 접근 가능, private 이면 테스트 전용 public 접근자 없으면 리플렉션 또는 서비스 메서드 경유.

### Step 11 — POI 기반 Excel 셀 검증 테스트 (T5~T7, FR-8/9/12/13/14) `[자동:POI]`

`src/test/java/com/swmanager/system/service/ExcelExportDesignEstimateTest.java` 신설. Spring Boot `@SpringBootTest` 또는 서비스만 mock.
- TYPE_A 6 케이스 (§7-5): 91%/천단위, 85%/만단위, 91%/절사없음, 100%/천단위, 100%/절사없음, 추가 1 엣지.
- TYPE_B 1 케이스 (§7-6).
- TYPE_D 1 케이스 (§7-7).
- 각 테스트에서 `byte[]` → `XSSFWorkbook` → 특정 셀 `getCellFormula()` / `getStringCellValue()` 문자열 assertEquals.

### Step 12 — 통합/API 테스트 — Invalid value 방어 (T8~T10, FR-5'/FR-6''', §4-9) `[자동:통합/API + 자동:POI + 자동:로그검증]`

`src/test/java/com/swmanager/system/controller/DocumentSaveRounddownTest.java` 신설. `MockMvc` 사용.
- 케이스 A: `POST /document/api/save` 에 `rounddownUnit=500` → HTTP 200 + DB 조회 시 정규화값 `1000` + logback appender에 WARN 1건.
- 케이스 B: 동일하되 `"abc"` 문자열.
- 케이스 C: SQL UPDATE 로 오염된 레코드 `GET /document/api/excel/{docId}` → HTTP 200 + POI 로 H11 수식 `",-3)"` 확인 + WARN.

### Step 13 — 수동 브라우저 검증 (T11~T15 + T28, §7-2/7-3/7-4/7-9/7-11) `[수동:브라우저]`
**체크리스트** (QA/개발자가 브라우저에서 수행):

**UI 노출** (§7-3)
- [ ] 착공계 §⑧ 의 절사 단위 드롭다운이 부가세 별도 다음 행에 노출, 옵션 라벨 공백 포함 일치.
- [ ] TYPE_C 선택 → 툴팁 표시. 다른 TYPE → 툴팁 숨김.

**touched 엣지케이스 4종** (§7-3, §4-6)
- [ ] (a) 신규 문서 / default 1000 → TYPE_D 선택 → 드롭다운 `10` 자동 세팅.
- [ ] (b) (a) 상태 + 사용자가 `100` 수동 변경 → TYPE_A → TYPE_D → `100` 유지.
- [ ] (c) (b) 상태 + 사용자가 `1000` 수동 재선택 → TYPE_D → `1000` 유지 (핵심).
- [ ] (d) 기존 TYPE_D(저장값 `10`) 레코드 로드 → 드롭다운 `10` + TYPE_A → TYPE_D → `10` 유지.

**미리보기 2종** (§7-4)
- [ ] 91% + 천단위 → `(낙찰율 91% 적용, 천단위 절사)`
- [ ] 91% + 절사없음 → `(낙찰율 91% 적용)`
- [ ] 100% + 천단위 → `(천단위 절사)`
- [ ] 100% + 절사없음 → 비고 행 비노출
- [ ] 확대 미리보기 값이 자동 산출 미리보기와 동일
- [ ] 드롭다운 변경 100ms 이내 반영

**저장 후 재진입 복원** (§7-11)
- [ ] 5개 값(1/10/100/1000/10000) 각각 선택 → 저장 → 재진입 → 드롭다운 값 유지.

### Step 14 — 수동 Excel 데스크톱 확인 (T18, NFR-6) `[수동:Excel]`
- [ ] Step 11 의 테스트 생성 파일 3종(TYPE_A/B/D) 을 Microsoft Excel 에서 열어 `#NAME?`/`#NUM!`/`#REF!` 등 오류 없음.
- [ ] ROUNDDOWN(x, 0) 형태도 Excel에서 정상 계산.

### Step 15 — NFR-3 회귀 검증 (T16) `[자동:POI]`

**Fixture 선정 — TYPE_A/B/D 각 1건 SQL 수준 보장** (§7-1):
```sql
-- DISTINCT ON 으로 estimateType 당 1건 씩만 반환. 결과는 정확히 3행 이하.
SELECT DISTINCT ON (dd.section_data->>'estimateType')
       d.doc_id,
       dd.section_data->>'estimateType' AS estimate_type
FROM tb_document d
JOIN tb_document_detail dd ON dd.doc_id = d.doc_id
WHERE d.doc_type = 'COMMENCE'
  AND dd.section_key = 'design_estimate'
  AND NOT (dd.section_data ? 'rounddownUnit')
  AND dd.section_data->>'estimateType' IN ('TYPE_A','TYPE_B','TYPE_D')
ORDER BY dd.section_data->>'estimateType', d.doc_id DESC;
```

**Fixture docId 확정 절차** (개발 시작 시점에 1회 실행):
1. 위 쿼리 실행 → 결과 저장.
2. 결과가 **3행이면** 해당 `doc_id` 3개를 **테스트 클래스 상단 상수**로 고정:
   ```java
   private static final int FIXTURE_DOC_ID_TYPE_A = <값>;
   private static final int FIXTURE_DOC_ID_TYPE_B = <값>;
   private static final int FIXTURE_DOC_ID_TYPE_D = <값>;
   ```
   → 상수값을 본 개발계획서 §6 "확정된 Fixture" 섹션에 추가 commit.
3. 결과가 **3행 미만이면** fixture SQL 생성. **파일명은 고정 아님**:
   - Step 1 사전 스캔 결과 중 `ls swdept/sql/V*.sql | sort -V | tail -1` 로 **직전 최고 번호 확인** → +1.
   - 예: 직전이 `V027` 이면 `swdept/sql/V028_design_estimate_fixture.sql`. 직전이 `V030` 이면 `V031_*`.
   - **파일명 템플릿**: `swdept/sql/V<NEXT>_design_estimate_fixture.sql` (대괄호/꺽쇠 제거한 실제 번호 대입).
   - 내용:
     - TYPE_A/B/D 각 1건씩 `tb_document` + `tb_document_detail` 삽입
     - JSONB section_data 에 `rounddownUnit` key 없음 (구 스키마 재현)
     - bidRate 예: 0.97 (가장 흔한 현업 값)
     - SQL 헤더 주석에 docId 3개 명시 + `-- NOTE: 본 SQL 은 design-estimate-bid-rate-rounddown 스프린트의 NFR-3 회귀 fixture 용입니다. 전용 태그(예: `tb_document.doc_type = 'COMMENCE_FIXTURE_DROP_ME'`) 후 운영 이관 시 일괄 정리 가능.`
4. fixture SQL 을 생성한 경우: **확정된 실제 파일명**(예: `V028_design_estimate_fixture.sql`)을 본 개발계획서 §6 "확정된 Fixture" 섹션에 추가 commit. 테스트 `@BeforeAll`/`@Sql` 도 동일 경로로 설정.
5. **FR-7 검증(Step 15.5)** 은 확정된 파일명 기준으로 동적으로 수행: "본 스프린트 기간 신규 `swdept/sql/V*.sql` 이 §6 에 기록된 fixture 파일(들) 외 존재하지 않아야 함".

**검증**:
- 각 docId 다운로드 → POI 로 H11/I11/H8/I8 수식/표시값 확인.
- 수식: 현재 릴리스 결과와 동일(bidRate 적용 + ROUNDDOWN `-3` 또는 `-1`).
- I 셀: TYPE_B 의 `"백단위절사"` → `"천단위 절사"` 만 허용 변경.

### Step 15.5 — 누락 FR/NFR 전용 검증 (T19~T22, FR-7, FR-15, NFR-4, NFR-7)

**FR-7 (DB 스키마 변경 없음)** `[자동:grep]`
```bash
# 본 스프린트 기간 동안 swdept/sql/V*.sql 에 신규 마이그레이션이 없거나, 있다면 §6 에 기록된 fixture 파일(들) 뿐인지 확인.
git diff --name-only master..HEAD -- 'swdept/sql/V*.sql'
```
- 기대 1: **빈 출력** — schema 변경/fixture 없음 (기존 레코드로 §7-1 충족한 경우).
- 기대 2: **본 개발계획서 §6 "확정된 Fixture" 섹션에 기록된 fixture 파일명**(예: `V028_design_estimate_fixture.sql`) 만 출력 — fixture fallback 경로 채택 시.
- 기대 위반: §6 에 기록되지 않은 다른 `V*.sql` 파일이 출력되면 기획서 FR-7(DB 스키마 변경 없음) 위배 — 즉시 롤백 검토.

**FR-15 (TYPE_C Java 미수정 보장)** `[자동:grep]`
```bash
# generateFromTypeCTemplate 메서드의 시그니처/본문이 본 스프린트에서 rounddownUnit 관련 변경을 받지 않음 확인
git diff master..HEAD -- src/main/java/com/swmanager/system/service/ExcelExportService.java | grep -E "generateFromTypeCTemplate|TYPE_C"
# 기대: 출력 중 rounddownUnit / toExcelRoundDigitsArg / rounddownLabel 관련 변경 0건
```
- 추가 확인: `rg -n "rounddownUnit" src/main/java/com/swmanager/system/service/ExcelExportService.java` 결과에 TYPE_C 메서드 영역(대략 line 864-934) 포함 여부 — 포함되면 위반.

**NFR-4 (신규 저장 → 다운로드 왕복 검증)** `[자동:통합/API + 자동:POI]`

`src/test/java/com/swmanager/system/controller/DesignEstimateRoundTripTest.java` 신설:
- **시나리오 1**: 신규 `docId` 를 `POST /document/api/save` 로 생성, `design_estimate` sectionData 에 `{estimateType:"TYPE_A", bidRate:91, rounddownUnit:1000, vatSeparate:false, items:[...]}`.
- 저장 성공 응답 → `GET /document/api/excel/{newDocId}` 로 다운로드.
- 응답 byte[] 를 POI 로 로드 → H11 수식 `"ROUNDDOWN((H4+H8)*0.91,-3)"` + I11 `"낙찰율 적용(91%)\n천단위 절사"` 정확 일치.
- **시나리오 2**: TYPE_D + rounddownUnit=10 + bidRate=95 로 반복. H8/I8 검증.
- **시나리오 3**: TYPE_A + bidRate=100 + rounddownUnit=1 → I11=`""`.
→ T19 로 등록.

**NFR-7 (견적서 비영향 검증)** `[자동:빌드 + 자동:grep]`
- `./mvnw -q test -Dtest='Quotation*Test'` → 전체 PASS (기존 견적서 테스트 회귀 없음).
- `git diff master..HEAD -- src/main/java/com/swmanager/system/quotation/` 결과가 **빈 출력** (견적서 도메인 파일 변경 0 건).
- `git diff master..HEAD -- 'swdept/sql/V*rounddown*.sql'` 도 빈 출력 (견적서의 `rounddown_unit` 관련 migration 변경 없음).
→ T20 으로 등록.

### Step 16 — `낙찰률` grep 0건 확인 (T17, NFR-9) `[자동:grep]`
```bash
rg -n "낙찰률" src/   # 기대: 0 hits
```

### Step 17 — 기획서 잔존 문서 정합성 2건 교정 (Step 0 참조)
- `docs/product-specs/design-estimate-bid-rate-rounddown.md` §4-6 내 `(FR-6''')` → `(FR-6)` 1곳.
- 동 문서 §5 하단 `수정 파일: 2` → `수정 파일: 3`.

### Step 18 — codex 구현 검증 (CLAUDE.md §3)
- `codex review` 호출 — 기획서 FR-1~FR-21, NFR-1~NFR-9 항목별 "충족/미충족/부분충족" 판정 요청.
- 응답의 **FR 미충족 0건** 이 최종 승인 조건(§7-12).

### Step 19 — 최종 빌드 / 재기동 / 스모크
- `./mvnw -q clean compile` → BUILD SUCCESS
- `./mvnw -q test` → Step 10~12/15 의 신규 테스트 모두 PASS
- `bash server-restart.sh` → `Started SwmanagerApplication` + ERROR 0
- `curl -s http://localhost:9090/` → HTTP 200

---

## 2. 테스트 (T#)

### 2-1. 핵심 테스트 (기획서 §7 Acceptance 1:1 매핑)

| ID | 내용 | 검증 방법 | Pass 기준 | 태그 |
|----|------|----------|-----------|------|
| T1 | 유틸 `toExcelRoundDigitsArg` — 5 허용값 | JUnit | 5/5 일치 | [자동:JUnit] |
| T2 | 유틸 `toExcelRoundDigitsArg` — invalid 입력 1건 | JUnit | `IllegalArgumentException` | [자동:JUnit] |
| T3 | 유틸 `rounddownLabel` — 5 허용값 | JUnit | 5/5 일치 | [자동:JUnit] |
| T4 | 유틸 `rounddownLabel` — invalid 입력 1건 | JUnit | `IllegalArgumentException` | [자동:JUnit] |
| T5 | TYPE_A 6 케이스 H11/I11 셀 문자열 정확 일치 | POI | 6/6 일치 | [자동:POI] |
| T6 | TYPE_B 1 케이스 H11/I11 + 낙찰률 문자 미포함 | POI | 통과 | [자동:POI] |
| T7 | TYPE_D 1 케이스 H8/I8 | POI | 통과 | [자동:POI] |
| T8 | Invalid value 방어 — `rounddownUnit=500` SQL 오염 + 엑셀 다운로드 | MockMvc + POI + logback list-appender | HTTP 200 + 수식 `",-3)"` + WARN 1건 | [자동:통합/API, 자동:POI, 자동:로그검증] |
| T9 | Invalid value 방어 — `rounddownUnit="abc"` + 엑셀 | MockMvc + POI + 로그 | HTTP 200 + 정규화 + WARN | [자동:통합/API, 자동:POI, 자동:로그검증] |
| T10 | Invalid value 방어 — `POST /save` 에 `rounddownUnit=500` | MockMvc + 로그 | HTTP 200 + DB 저장값 `1000` + WARN | [자동:통합/API, 자동:로그검증] |
| T11 | UI 노출 — 드롭다운, 옵션 5개 라벨 공백 포함 | 브라우저 | 5/5 일치 | [수동:브라우저] |
| T12 | UI 노출 — TYPE_C 툴팁 토글 | 브라우저 | 선택/해제 각 상태 일치 | [수동:브라우저] |
| T13 | touched 엣지케이스 4종 (a,b,c,d) | 브라우저 | 4/4 통과 | [수동:브라우저] |
| T14 | 미리보기 6 케이스 (§7-4 5 비고 + 1 응답시간 100ms) | 브라우저 | 6/6 통과 | [수동:브라우저] |
| T15 | 저장 후 재진입 복원 (5 값) | 브라우저 | 5/5 통과 | [수동:브라우저] |
| T16 | NFR-3 회귀 fixture 3건 (TYPE_A/B/D) | POI | 수식 동일, I11 "백단위절사"→"천단위 절사" 만 허용 | [자동:POI] |
| T17 | `grep "낙찰률"` 0 hits | shell | 0 hits | [자동:grep] |
| T18 | 생성 xlsx 3종 Excel 오픈 | Excel | `#NAME?`/`#NUM!` 0건 | [수동:Excel] |
| T19 | **NFR-4** 신규 저장→다운로드 왕복 3 시나리오 | MockMvc + POI | 3/3 통과 | [자동:통합/API, 자동:POI] |
| T20 | **NFR-7** 견적서 비영향 | `./mvnw test -Dtest='Quotation*Test'` + git diff | ALL PASS + 변경 파일 0 | [자동:빌드, 자동:grep] |
| T21 | **FR-7** DB 스키마 변경 없음 | `git diff --name-only master..HEAD -- 'swdept/sql/V*.sql'` | 빈 출력, 또는 §6 "확정된 Fixture" 에 기록된 파일명만 출력 | [자동:grep] |
| T22 | **FR-15** TYPE_C Java 미수정 | `git diff ExcelExportService.java` + grep | TYPE_C 영역 rounddownUnit 등장 0 | [자동:grep] |
| T23 | codex 구현 검증 | codex review | FR 미충족 0 | [자동:codex] |
| T28 | **NFR-2** 편집 페이지 정상 로드 + 브라우저 콘솔 에러 0 | 브라우저 DevTools Console | 에러/경고 0건 | [수동:브라우저] |
| T29 | **§7-9 마지막 항목 (FR-6 로드 fallback 분기)** — sectionData 의 `rounddownUnit` 이 허용값 외(`500` 등)로 저장된 레코드 편집 페이지 열기 → 드롭다운 `천단위 절사(1000)` 표시 + `rounddownUnitTouched === false` | 브라우저 + DevTools 콘솔에서 `window._rounddownUnitTouched` 조회 | 드롭다운값 `1000`, touched=false | [수동:브라우저] |

### 2-2. Acceptance → T 교차표 (1:1 매핑)

| Acceptance 섹션 (§7) | 항목 수 | 커버하는 T | 비고 |
|---|---|---|---|
| §7-2 빌드/기본 — Maven compile | 1 | T24 | |
| §7-2 빌드/기본 — 페이지 로드 + 콘솔 에러 0 | 1 | **T28 (신설)** | |
| §7-3 UI 노출 (드롭다운) | 1 | T11 | |
| §7-3 UI 노출 (TYPE_C 툴팁) | 1 | T12 | |
| §7-3 touched 엣지케이스 (4종) | 1 (내부 4) | T13 | 4/4 모두 통과 |
| §7-4 미리보기 (6 항목) | 6 | T14 | 확대 미리보기 동일값 포함 |
| §7-5 엑셀 TYPE_A (6 케이스) | 6 | T5 | |
| §7-6 엑셀 TYPE_B (2 항목) | 2 | T6 | |
| §7-7 엑셀 TYPE_D | 1 | T7 | |
| §7-8 유틸 `toExcelRoundDigitsArg` 허용값 | 1 | T1 | |
| §7-8 유틸 `toExcelRoundDigitsArg` invalid | 1 | T2 | |
| §7-8 유틸 `rounddownLabel` 허용값 | 1 | T3 | |
| §7-8 유틸 `rounddownLabel` invalid | 1 | T4 | |
| §7-9 Invalid `=500` + 엑셀 | 1 | T8 | |
| §7-9 Invalid `="abc"` + 엑셀 | 1 | T9 | |
| §7-9 Invalid `POST /save` | 1 | T10 | |
| §7-9 브라우저 로드 fallback (FR-6 분기) | 1 | **T29 (신설)** | |
| §7-10 회귀 fixture 3건 | 1 | T16 | |
| §7-10 Excel 데스크톱 오류 | 1 | T18 | |
| §7-10 `낙찰률` grep 0 | 1 | T17 | |
| §7-11 저장/로드 복원 | 1 | T15 | |
| §7-12 codex 최종 | 1 | T23 | |

**교차표 의미**: Acceptance §7 의 모든 item 이 위 표에 최소 1개 T 로 매핑됨. 매핑되지 않은 Acceptance bullet 이 남으면 승인 불가 (NFR/FR 미충족).

**보조 테스트** (Acceptance 직접 대응 없음, 품질 게이트 역할):

| ID | 내용 | 태그 |
|----|------|------|
| T24 | `./mvnw -q clean compile` BUILD SUCCESS | [자동:빌드] |
| T25 | `./mvnw -q test` 전 테스트 PASS (T1~T10, T16, T19, T20 포함) | [자동:빌드] |
| T26 | `bash server-restart.sh` 성공 + ERROR 0 | [수동:브라우저] (사전 조건) |
| T27 | 기획서 §0 문서 정합성 정리 (§4-6 FR 참조 + §5 파일 수) | Read/Edit 수동 확인 | [자동:grep, 수동] |

---

## 3. 롤백 전략

**기본 원칙** (CLAUDE.md Git Safety Protocol 준수):
- `git reset --hard`, `git checkout --`, `git clean -f`, `git push --force` 사용 금지.
- 미푸시 커밋은 **파일 단위 역패치** (`Edit` 으로 해당 변경만 되돌림) 또는 **신규 되돌림 커밋** 생성.
- 이미 푸시된 커밋은 반드시 **`git revert <sha>`** 로 되돌림 커밋 생성.
- 브랜치/워크트리 자체를 날리는 destructive 명령 금지.

| 상황 | 조치 |
|------|------|
| compile 실패 (Step 2~8 중, 미커밋 상태) | 해당 파일 `Read` 로 변경부분 확인 → `Edit` 로 역패치 (해당 Step 변경만 되돌림). Step 단위를 더 작게 쪼개 재시도 |
| compile 실패 (해당 Step 이 이미 커밋됨) | `git revert HEAD` 로 되돌림 커밋 생성. 해당 Step 을 더 작게 쪼개 재시도 |
| TYPE_A/B/D 특정 케이스 테스트 실패 | 해당 TYPE 수정만 식별 → 파일 단위 역패치 (해당 메서드만). 실패 케이스 디버그 후 재진행. 나머지 TYPE 진행 가능 |
| invalid value 방어 logback 검증 실패 | logback-spring.xml 의 `com.swmanager.system.controller` / `com.swmanager.system.service` 로거 레벨 WARN 이상 확인. appender 주입이 안되면 TestConfiguration 으로 교체 (코드 revert 아님) |
| fixture docId 부족 (Step 15) | Step 15 의 `V<NEXT>_design_estimate_fixture.sql` fallback 경로로 이동 (실제 번호는 `ls swdept/sql/V*.sql \| sort -V \| tail -1` 결과 +1). 확정된 파일명은 §6 "확정된 Fixture" 에 기록. SQL 의 실행 실패 시 테스트 `@Sql` 대신 `@BeforeEach` 에서 JPA 로 동등 데이터 삽입 |
| 머지 후 운영 회귀 | `git revert -m 1 <merge-commit>` (merge commit 되돌림 전용 옵션) → 재배포. DB 변경 없으므로 schema rollback 불필요. fixture SQL 이 머지됐다면 별도 revert (fixture 는 테스트 전용이라 운영 DB 영향 없음) |
| JS 수정 후 touched 엣지케이스 오작동 | 콘솔에서 `window._rounddownUnitTouched` 출력 + 이벤트 시퀀스 재현. `ev.isTrusted` 가드 확인 우선. 수정이 필요하면 JS 파일 단위 역패치 + 재시도 |
| 스프린트 전체 중단 결정 | `git log master..HEAD --oneline` 로 본 브랜치의 커밋 SHA 목록 확인 → **각 SHA 를 최신순부터 `git revert <sha>` 로 개별 되돌림 커밋 생성** (여러 개 생성 가능, `--no-edit` 옵션으로 반복). master 에 머지된 이후면 그 merge commit 을 `git revert -m 1 <merge-sha>` 로 되돌림 (이 옵션은 merge commit 전용). 로컬 작업 중이면 PR 닫은 뒤 원격 브랜치 삭제 (`git push origin --delete <branch>`). 로컬 `git branch -D` 는 revert 커밋이 merge 되었거나 원격 삭제를 확인한 뒤에만 수행 |

---

## 4. 리스크·완화 재확인 (기획서 §6 대비 구현 시 추가 발견)

| 리스크 | 수준 | 완화책 |
|--------|------|--------|
| 테스트가 Spring context 로딩에 의존해 느림 | 낮음 | 유틸(T1/T2)은 Spring 없이 순수 JUnit. POI 테스트(T3~T5)만 Spring 필요 시 `@ContextConfiguration` 최소 구성 |
| `ev.isTrusted` 브라우저 지원 미흡 | 매우 낮음 | 지원 범위: IE 10+ 포함 모든 현행. 내부 프로젝트라 환경 한정 |
| Python 없이 fixture 쿼리 결과 JSON 파싱 | 낮음 | psql `-A -F','` 또는 node -e 활용. 개발계획서 Step 15 의 쿼리 그대로 제공 |
| MockMvc 에 로그 appender 주입 실패 | 낮음 | Slf4jBridgeHandler / `ch.qos.logback.classic.Logger` 직접 접근, 또는 memory-appender 라이브러리 사용 |
| TYPE_C 사용자 혼란 — 실제 사용자 반응 | 중간 | QA 수동 스모크 (Step 13 UI 체크리스트) 에서 툴팁 가독성 확인. 부족 시 아이콘 클릭 시 alert 추가 고려 (Step 8-1 보완) |
| `invalidValue` 로그 패턴 문자열 정확 매치 필요 | 낮음 | Step 7 / Step 2-3 의 로그 포맷을 두 곳에서 **동일 상수** 로 공유 (옵션: `DesignEstimateLogConstants`) |

---

## 5. 승인 요청

본 개발계획서에 대한 codex 검토 및 사용자 최종승인을 요청합니다.

**codex 검토 중점 항목**:
1. 기획서 FR-1~FR-21, NFR-1~NFR-9 을 Step 1~19 가 **모두 커버** 하는지 매트릭스 확인.
2. Step 내 구체 코드 스케치(유틸 3종, Controller 정규화 블록, JS 8-5~8-8)가 기획서 명세와 **문자적으로 일치** 하는지.
3. 테스트(T1~T29) 태그 분류가 기획서 §7-0 9 태그 체계와 어긋남 없는지.
4. Fixture 쿼리(Step 15)가 §7-1 선정 조건과 1:1 매핑되는지, 결과 부족 시 fallback 경로(§6 에 기록) 가 명확한지.
5. Step 17 이 기획서 §0 이월 문서 정합성 2건을 개발 완료 전에 확실히 닫는지.
6. 롤백 전략(§3)이 부분 실패 시에도 안전한지.

---

## 6. 확정된 Fixture (Step 15 완료 후 기록)

본 섹션은 Step 15 의 fixture 선정 절차 결과를 문서화하는 **동적 기록 영역**입니다. 개발 시작 시점에 비어 있고, 실제 docId/파일명을 확정한 후 commit 으로 채워집니다.

### 6-1. 회귀 검증 fixture docId (Step 15 §2)

| estimateType | fixture docId | 출처 | 확정 시각 |
|--------------|---------------|------|-----------|
| TYPE_A | _미정 (Step 15 쿼리 결과 또는 fixture SQL)_ | _기존 DB / V< NEXT > SQL_ | _YYYY-MM-DD HH:MM_ |
| TYPE_B | _미정_ | _기존 DB / V< NEXT > SQL_ | _YYYY-MM-DD HH:MM_ |
| TYPE_D | _미정_ | _기존 DB / V< NEXT > SQL_ | _YYYY-MM-DD HH:MM_ |

### 6-2. Fixture SQL 파일 (해당 시만)

Step 15 쿼리 결과가 3행 미만이어서 fixture SQL 을 생성한 경우, 아래 표에 실제 생성 파일명을 기록.

| 파일 경로 | 용도 | 생성 커밋 |
|-----------|------|-----------|
| _예: `swdept/sql/V028_design_estimate_fixture.sql`_ | _NFR-3 회귀 검증 fixture_ | _SHA_ |

**위 표에 기록되지 않은 새 `swdept/sql/V*.sql` 은 FR-7 (DB 스키마 변경 없음) 위반**. T21 pass 기준은 이 표를 SSoT 로 사용한다.

### 6-3. 기록 갱신 절차

- Step 15 의 DISTINCT ON 쿼리 실행 직후:
  - 3행 반환 → §6-1 에 docId 채우기, §6-2 는 비움.
  - 3행 미만 → `V<NEXT>` 파일 생성 + §6-1 의 fixture docId 및 §6-2 의 파일 경로 채우기.
- 본 개발계획서 파일에 직접 commit (같은 브랜치에서).
- 이 commit 이 push 된 이후에 Step 15 이후의 POI 테스트(T16, T19 등) 를 실행.
