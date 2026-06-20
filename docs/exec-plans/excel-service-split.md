# 개발계획서 — ExcelExportService 분리 (excel-service-split, §6-5)

- **상태**: v0.2 (codex 진행가능 + 보완 4건 반영 — 사용자 최종승인 대기)
- **작성일**: 2026-06-20 (v0.1) / v0.2 동일자 (codex: 헬퍼 분류 명문화·StyleSupport package-private·골든 SHA256 "가능하면"+구조비교 fallback DoD·NPE 저위험 확인)
- **기획서**: `docs/product-specs/excel-service-split.md` (v0.2, codex ⚠수정필요 5건 반영·사용자 승인)
- **안전망**: 현 green 스위트(`./mvnw test` 410 tests, 42 skip) + JaCoCo/GiantClass/MapDebt 래칫 + **회사 PC 운영DB SHA256 골든 해시**(설계/기성). 각 Step 후 재실행.
- **원칙**: **동작 보존**. 엑셀 바이트·public API·URL·DB·화면 변경 0. "바이트 동일 코드 이동"(로직 1자 수정 금지). 커밋 Step 단위 원자.

---

## Step 0 — 인벤토리 확정 + 골든 해시 캡처 (구현 전 필수)

### 0-1. 클러스터 경계 (현 라인, 확정)
| 클러스터 | 라인 범위 | 진입점 | 분리 후 위치 |
|---|---|---|---|
| 성과보고서 | 78~217 | `generatePerformanceReport` | **ExcelExportService 잔존** |
| 설계견적서 | 218~1503 | `generateDesignEstimate` | **DesignEstimateExcelService** |
| 중간/기성 | 1504~2279 | `generateInterimReport` | **InterimReportExcelService** |
| (스타일/유틸 헬퍼) | 2280~2393 | — | 분류는 0-2 |
| 문서목록 | 2394~ | `generateDocumentList` | **ExcelExportService 잔존** |
| rounddown static | 41~76 | `normalizeRounddownUnit`/`toRoundDigits`/`roundLabel` | **ExcelExportService 잔존**(이동 금지) |

### 0-2. 헬퍼 배치 결정 규칙 (FR-3, codex 명문화)
각 private 헬퍼의 호출 라인을 grep → 위 클러스터로 매핑 후 **4분기**:
- **설계+기성(추출 서비스 간) 공유, 또는 facade 잔존분과 추출 서비스가 함께 사용** → `ExcelStyleSupport` 의 **package-private static**.
- **설계 전용** → `DesignEstimateExcelService` private.
- **기성 전용** → `InterimReportExcelService` private.
- **성과/문서목록(잔존)에서만 사용** → `ExcelExportService` private 존치.
> 확정: `createDataCell/safe/toInt` = 성과(160~189)+문서목록(2435~2444) 만 → **잔존**(codex). `toDouble/toLong/str`·`setStringDirect/setNumericDirect`·style factory 계열 = 설계/기성 공유 → **Support 후보**.

**grep 명령(구현 시 실측):**
```
grep -nE '\b<helper>\s*\(' ExcelExportService.java   # 각 헬퍼별, 정의 라인 제외하고 호출 라인의 범위 판정
```

**예비 분류(Step 0 grep 으로 최종 확정 — 아래는 현 세션 실측 기반 잠정):**
| 헬퍼 | 관측 사용처 | 잠정 배치 |
|---|---|---|
| `setStringDirect`(1186)·`setNumericDirect`(1199) | 설계(946~1151)+기성(1586~1814) | **ExcelStyleSupport** (static) |
| `setCellKeepStyle`×2(462/475)·`simplifyMaintFormula`(450)·`clearDataRow`(488)·`replacePlaceholder`(1211) | 설계 전용(318~413 등) | DesignEstimateExcelService |
| `applyFontSize`(1819) | 기성 전용(1765) | InterimReportExcelService |
| `createDataCell`(2370)·`safe`(2380)·`toInt`(2384) | 성과(160~189)+문서목록(2435~2444) | **ExcelExportService 잔존** |
| `setCellValue`(2340) | 설계(557~929) (+ 타 클러스터 여부 grep 확인) | grep 후 결정(설계전용→Design / 공용→Support) |
| `str`(2362)·`toDouble`(2346)·`toLong`(2354) | 설계+기성+성과 다수 | **ExcelStyleSupport** (static) |
| `createTitleStyle`/`createHeaderStyle`/`createBodyStyle`/`createTotalStyle`(2280~2329)·`setBorders`(2333) | 호출처 grep 미확정(로컬 style var 경유 추정). 성과(106~125 setBorders 직접)+설계/기성 style | **grep 으로 확정** — 경계 넘으면 Support, 아니면 잔존 |

> ⚠ 규칙: 의심스러우면 **Support(static)로** 올린다(같은 패키지+무상태라 어디서든 호출 가능, 동작 무영향). 단 잔존 전용으로 확실하면 불필요 이동 안 함.

### 0-3. 신규 서비스 의존성 (실측: `@Autowired DocumentService documentService`(32)·`MessageResolver messages`(33), 데이터 `documentService.getDocumentById(docId)`(219/1505))
- `DesignEstimateExcelService`·`InterimReportExcelService` 둘 다 **생성자 주입**: `DocumentService`, `MessageResolver`. raw Repository 불필요(실측 0).
- 각 서비스 본문 grep 으로 `messages.` / `documentService.` 외 인스턴스 의존 0 확인.

### 0-4. 테스트 무호출 가정 확정 (R-7)
- `ExcelExportServicePerfStyleTest`(23): `new ExcelExportService()` → `generatePerformanceReport` 만 호출. wrapper(`generateDesignEstimate/Interim`) **미호출** ✓ (실측).
- `ExcelExportServiceRounddownTest`: `ExcelExportService.normalizeRounddownUnit/toRoundDigits/roundLabel` static 만 참조 ✓.
- → facade 가 신규 서비스를 null 필드로 가져도 두 테스트는 NPE 없음.

### 0-5. 골든 캡처 (FR-1 안전망, 회사 PC DB — codex 순서 명문화)
- 운영DB(192.168.10.194:5880)에서 설계견적·기성 문서가 있는 대표 `docId` 각 1건 선정(없으면 목록 조회로 확보).
- **검증 방식 결정 순서**:
  1. **결정성 판정**: 현 HEAD 로 동일 docId 를 **2회** 생성 → raw SHA256 동일한가?
  2. **안정적(동일)** → SHA256 골든 사용. 리팩토링 前 해시 기록(`excel-split-golden-hash.txt`, 세션 참조용) → 각 Step 後 재생성 해시 대조.
  3. **불안정**(zip metadata/workbook property/formula cache 로 흔들림) → **구조 비교 fallback**: 시트명 · row/cell 값 · formula · 핵심 스타일(폰트/채움/보더) · merged region · column width 덤프 비교.
- 임시 통합테스트(RUN_DB_TESTS 게이트) 또는 일회성 스크립트로 수행. 골든 산출물은 커밋 불필요(세션 검증용).

**검증:** 없음(준비). 단 0-5 결정성 판정 결과를 기록.

---

## Step A — ExcelStyleSupport (static 유틸) 추출

**A-1.** 신규 `src/main/java/com/swmanager/system/service/ExcelStyleSupport.java` — **package-private 클래스 + package-private static 메서드**(public 노출 0, codex):
```java
final class ExcelStyleSupport {           // 무상태 static 유틸 (주입 없음, 같은 패키지 전용)
    private ExcelStyleSupport() {}
    // 0-2 에서 "공용" 확정된 헬퍼를 package-private static 으로 이동 (본문 동일, this. 참조 0 선확인)
    static String str(Object o) { ... }
    static double toDouble(Object o) { ... }
    static void setStringDirect(Sheet s, int r, int c, String v) { ... }
    // ...
}
```
- 이동 전 각 헬퍼 본문 grep: `this.`·인스턴스 필드 참조 0 확인(R-2). 있으면 Support 제외.
- ExcelExportService(잔존)·이후 신규 서비스에서 호출부를 `ExcelStyleSupport.xxx(...)` 로 치환.

**A-2.** ExcelExportService 의 해당 헬퍼 정의 삭제 + 호출부 치환. 잔존 전용 헬퍼(createDataCell/safe/toInt 등)는 그대로.

**검증:** 컴파일 + `./mvnw test` 410 green + **PerfStyleTest green**(perf 가 Support static 써도 `new ExcelExportService()` 무영향) + MapDebt 불변.

## Step B — DesignEstimateExcelService 분리

**B-1.** 신규 `DesignEstimateExcelService` (@Service, 생성자 주입 DocumentService/MessageResolver):
- `generateDesignEstimate` + 설계 전용 빌더/헬퍼 일체(0-1 의 218~1503 중 잔존·Support 로 안 간 것) **바이트 동일 이동**.
- rounddown 은 `ExcelExportService.toRoundDigits(...)` 등 static 참조(같은 패키지). 공용 헬퍼는 `ExcelStyleSupport.xxx`.

**B-2.** ExcelExportService: `generateDesignEstimate(Integer docId)` 를 **위임 wrapper** 로 교체:
```java
public byte[] generateDesignEstimate(Integer docId) throws IOException {
    return designEstimateExcelService.generateDesignEstimate(docId);
}
```
- 신규 서비스 필드 주입(`@Autowired` 또는 생성자). 이동한 설계 전용 의존성 중 ExcelExportService 에서 더 안 쓰는 것 정리.

**검증:** 컴파일 + 기동 + `./mvnw test` green + **설계 docId 재생성 → SHA256/구조 골든 동일**(FR-1) + `git diff` 로 본문 동일(호출 prefix 만 변동) 확인.

## Step C — InterimReportExcelService 분리

**C-1.** 신규 `InterimReportExcelService` (@Service, 생성자 주입): `generateInterimReport` + 중간/기성 전용 빌더(1504~2279) 바이트 동일 이동. `applyFontSize` 동행. 공용은 Support, rounddown 은 ExcelExportService static.

**C-2.** ExcelExportService: `generateInterimReport(Integer)` 위임 wrapper.

**검증:** 컴파일 + 기동 + `./mvnw test` green + **기성 docId 재생성 골든 동일** + git diff 본문 동일.

## Step D — 래칫 갱신 + 기동 스모크

**D-1.** `GiantClassRatchetTest` 재기록: `GOLDEN_RECORD=1 ./mvnw -Dtest=GiantClassRatchetTest test` → `giant-class-baseline.txt` 에서 ExcelExportService 제거 확인, 신규 3클래스(DesignEstimate≈1300/Interim≈800/StyleSupport) 모두 임계(2000) 이하 → baseline 비게 되거나 ExcelExportService 만 ≈400. 위반 0 일 때만 블레스.
**D-2.** `MapDebtRatchetTest`: 증가 0 확인(이동만이라 불변). 감소면 tighten.
**D-3.** 기동 스모크: `DEV_HTTPS_ENABLED=false bash server-restart.sh` → 설계/기성/성과 엑셀 다운로드 엔드포인트 실호출 200 + 파일 열림.

**검증:** 위 전부 + `./mvnw verify`(JaCoCo 게이트).

## Step F — codex 검증 + 커밋 + 푸시

- codex 검증(바이트 동일·API 보존·테스트 무변경·래칫 감소·헬퍼 누락).
- 커밋 분리: `refactor(excel): ExcelStyleSupport static 유틸 추출 (§6-5 A)` / `refactor(excel): DesignEstimateExcelService 분리 + facade wrapper (§6-5 B)` / `refactor(excel): InterimReportExcelService 분리 (§6-5 C)` / `chore(ratchet): giant-class baseline ExcelExportService 제거 (§6-5 D)`.
- `git push origin master`(듀얼).

---

## 검증 매트릭스 (FR/NFR → Step)
| 항목 | Step |
|---|---|
| FR-1 엑셀 바이트 동일 | Step 0(해시 캡처)·B·C(재생성 대조) |
| FR-2 public API/rounddown 보존 | Step B·C(wrapper) + 0-4(테스트 무변경) |
| FR-3 공용/전용 경계 | Step 0-2(규칙) + A |
| FR-4 빈 주입 정상 | Step B·C(생성자 주입) + D(기동) |
| FR-5 기존 골든 통과 | Step A·B·C(`./mvnw test`) |
| NFR-1 회귀 0 | 각 Step `./mvnw test` 410 green |
| NFR-2 래칫 감소 | Step D |
| NFR-3 Map 부채 불변 | Step A·D |
| NFR-4 JaCoCo | Step D(`verify`) |
| NFR-5 원자 커밋 | Step F |

## 롤백
- 각 Step 원자 커밋 → `git revert <sha>`. A~C 는 순수 이동/위임이라 revert 시 원상복구(동작 동일). D 는 baseline 파일만.
