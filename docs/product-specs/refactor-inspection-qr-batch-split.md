# [기획서] InspectionQrBatchService 순수 변환 로직 분리 — InspectQrMetricSupport (S4 거대클래스 #3)

- **작성팀**: 기획팀
- **작성일**: 2026-06-27
- **트랙**: beyond-A S4 거대클래스 분리. DocumentController(5 Phase)·HwpxExportService(#2) 완료 후 #3 후보 InspectionQrBatchService(705줄).
- **상태**: ✅ **완료(2026-06-27)**. codex 1차 NEEDS-FIX 반영 + 사용자 승인 + 구현완료. InspectQrMetricSupport 분리, InspectionQrBatchService 705→474. 개발계획서 참조.

> **codex 1차 검토(NEEDS-FIX) 반영(2026-06-27)**: ① 5-3 단일 통합 **확정**(codex 동의). ② 2-B `MANIFEST_SORT` 직접노출 **철회** — `int[]` mutable 외부변조 위험 → 접근 메서드 `manifestSort(String key)` 채택. ③ hash 군 잔류 **확정**(codex 동의). ④ `RESULT_TEXT_MAX`/`ResultText` 가시성·import 정책 명문화. ⑤ `saveCheckResults` L299-301 **2차 truncation 불일치**(`truncated=false`인 긴 문자열을 자르면 remarks `(truncated)` 누락 가능 — 기존 버그) → 본 분리는 **동작 100% 유지**, 불일치는 §8 후속 버그픽스 백로그로 분리.

---

## 1. 배경 / 목표

### 1-1 게이트 현황 (정직성)
거대클래스 ratchet 게이트 임계는 **컨트롤러 1,500 / 서비스 2,000줄**(`GiantClassRatchetTest`)이고 baseline 은 **비어 있음(부채 0)**. `InspectionQrBatchService`(705줄)는 게이트 위반이 **아니다**. 본 작업은 게이트 해소가 아니라 **예방적 응집도 개선(자발 분리)** — HwpxXmlSupport(#2, 1311→1152) 분리와 동일 성격. 사용자 확인(2026-06-27) 후 진행.

### 1-2 대상 분석
`InspectionQrBatchService`(705줄)는 ① QR batch upload **orchestration**(멱등→site매핑→report→check_result→metric_snapshot→hash→batch INSERT, DB 의존) + ② **순수 변환 함수**(payload metric value → 표시텍스트 / 섹션·정렬 매핑 / 숫자 추출)가 혼재. 이 중 **순수 변환 함수군**은 필드/repo/`this` 참조 0·외부 클래스 호출처 0(서비스 내부 + 단위테스트에서만 호출) → 분리 가장 안전.

| 그룹 | 멤버 | 시그니처 | 줄수(대략) | 호출처 |
|---|---|---|---|---|
| **A. 값 포맷팅** | `formatValue` | (Object)→ResultText | ~6 | formatValueWithContext, FormatTest |
| | `formatValueWithContext` | (String,Object)→ResultText | ~120 | saveCheckResults L297, FormatTest |
| | `buildRemarks` | (String,InspectResultCode,boolean)→String | ~16 | saveCheckResults L303, FormatTest |
| | `UNIT_LABELS` | Map 상수 | ~20 | formatValueWithContext(내부) |
| | `RESULT_TEXT_MAX` | int 상수(500) | 1 | formatValue + saveCheckResults L299 |
| | `ResultText` | record(text,truncated) | 1 | A그룹 반환 + saveCheckResults L297/303 |
| **B. 섹션·정렬 매핑** | `MANIFEST_SORT` | Map 상수(80행) | ~80 | saveCheckResults L280 |
| | `resolveSection` | (String)→String | ~7 | saveCheckResults L279, FormatTest |
| **C. 숫자 추출** | `extractPercent` | (Object)→Double | ~16 | saveMetricSnapshot L407, FormatTest |
| | `parseNumeric` | (String)→Double | ~7 | extractPercent(내부), FormatTest |

**확인(grep)**: A·B·C 그룹 전체가 `static`(또는 무상태 변환) + 필드/repo 참조 0 + 상호 호출만. **InspectionQrBatchService 외 production 호출처 없음**. 테스트는 `InspectionQrBatchFormatTest`(static import 직접 호출) — 분리 시 import 경로만 갱신. `InspectionQrBatchServiceTest`는 순수함수 직접 호출 0(영향 없음).

### 1-3 분리 대상 외 (잔류)
- **hash 검증군**: `verifyHash`·`gzip`·`sha1Hex`·`HASH_MAPPER`. ⚠**`HASH_MAPPER`(ObjectMapper 상수)가 `verifyHash`(L503)뿐 아니라 `saveMetricSnapshot`(L429)·`buildBatch`(L490) 3곳에서 공유** → 분리하면 잔류 서비스가 `Support.HASH_MAPPER` 역참조하게 되어 응집도가 오히려 악화. **이번 분리 비목표 — 서비스 잔류**.
- **orchestration**: `upload`·`findExisting`·`saveCheckResults`·`saveMetricSnapshot`·`buildReport`·`buildBatch`·`toIdempotentResponse`·`ItemStats`(DB/repo 의존) — 서비스 본체.

**목표**: A·B·C 순수 변환 함수군을 신규 `InspectQrMetricSupport`(package-private static)로 이동. 서비스 내부 호출은 `InspectQrMetricSupport.xxx(...)`로 치환. **동작 100% 동일(순수 이동)**, InspectionQrBatchService 705 → 약 450~460줄. **부수효과: FormatTest import 한 클래스로 통일 + support 직접 단위테스트 보존.**

---

## 2. 변경 설계

### 2-A 신규 `InspectQrMetricSupport` (final, private ctor)
- 패키지: `com.swmanager.system.service.inspection` (대상 서비스와 동일 패키지 — package-private 가시성 유지, ResultText record 동일 패키지 공유).
- A·B·C 그룹 멤버(메서드 + 상수 + ResultText record)를 **본문 바이트 동일** 이동. **package-private static** 유지(테스트가 동일 패키지 → public 불필요, API 표면 최소). 상호 호출(formatValueWithContext→formatValue/UNIT_LABELS, extractPercent→parseNumeric)은 동일 클래스 내 유지.

> **설계 확정(codex PASS)**: 단일 `InspectQrMetricSupport`로 통합(A+B+C). 선례(HwpxXmlSupport 1클래스)와 일치, churn 최소, 셋 다 "QR metric value 변환" 단일 도메인. 2분할은 클래스 수↑·import 분기↑ 대비 응집 이득 작음 → 기각.

#### 가시성 정책 (codex 반영)
- `formatValue`·`formatValueWithContext`·`buildRemarks`·`resolveSection`·`extractPercent`·`parseNumeric`: **package-private static**(FormatTest 동일 패키지 직접 호출 유지).
- `UNIT_LABELS`·`HASH_MAPPER`(미이동)·`RESULT_TEXT_MAX`: `RESULT_TEXT_MAX`는 **package-private static**으로 support 에 두고 서비스가 `InspectQrMetricSupport.RESULT_TEXT_MAX` 참조(동일 패키지). `UNIT_LABELS`는 support 내부 private.
- `MANIFEST_SORT`: **private static** 유지 + **접근 메서드 `static Integer manifestSort(String key)`** 신설(매핑 없으면 null). `int[]` 원시구조를 외부에 노출하지 않아 mutable 변조 차단(codex). 서비스 호출부 L280 `MANIFEST_SORT.get(key)` → `InspectQrMetricSupport.manifestSort(key)`(반환 Integer, null 분기 동일).
- `ResultText` record: support 로 이동 → `InspectQrMetricSupport.ResultText`. 서비스·FormatTest 가 정규화 참조/import.

### 2-B InspectionQrBatchService 정리
- A·B·C 그룹 정의 제거.
- 내부 호출처 치환:
  - `saveCheckResults`: `resolveSection(key)`→`InspectQrMetricSupport.resolveSection(key)`, `MANIFEST_SORT.get(key)`→`InspectQrMetricSupport.manifestSort(key)`(상수 직접노출 대신 접근 메서드 권장, codex 의견), `formatValueWithContext(...)`, `buildRemarks(...)`, `RESULT_TEXT_MAX`, `ResultText`.
  - `saveMetricSnapshot`: `extractPercent(value)`→`InspectQrMetricSupport.extractPercent(value)`.
- `ResultText`는 `InspectQrMetricSupport.ResultText`로 이동 → 서비스에서 import 또는 정규화 참조.
- import 정리. 결과: 705 → 약 450~460줄.

> 순수 이동: 텍스트/섹션/정렬/숫자 변환 결과 동일. upload 산출(report/check_result/metric_snapshot/batch row)은 불변.

---

## 3. 기능 요건 (FR)

| ID | 내용 |
|----|------|
| FR-1 | A·B·C 그룹의 입출력 동작이 이동 전과 동일(순수 변환). |
| FR-2 | `upload`/`saveCheckResults`/`saveMetricSnapshot` 결과(섹션·정렬·resultText·remarks·metric snapshot 값)가 이전과 동일 — 기존 `InspectionQrBatchServiceTest`(RUN_DB_TESTS) 통과. |
| FR-3 | 신규 기능·변환 규칙 변경 0. hash 검증·orchestration 무변경. |

---

## 4. 비기능 요건 (NFR)

| ID | 내용 |
|----|------|
| NFR-1 | compile + `./mvnw -o clean test` green(순수 테스트). 회사 PC `RUN_DB_TESTS=true` 통합테스트 통과. |
| NFR-2 | `InspectionQrBatchFormatTest`를 신규 `InspectQrMetricSupport`로 import 경로 갱신(테스트 로직 불변, 호출 대상만 변경). 신규 단위테스트 추가 불필요(기존 19 케이스가 이미 직접 커버) — 단 ResultText/manifestSort 접근 메서드 신설 시 최소 1 케이스 보강. |
| NFR-3 | 커버리지 비감소(순수 로직 직접테스트 유지). JaCoCo floor·PIT 게이트 불변. ratchet(controller-repo) 불변(repo 접근 이동 없음). |
| NFR-4 | dual-review(codex+Opus 독립검수) → 듀얼푸시. |

---

## 5. 의사결정

- **5-1 순수 A·B·C만 추출** ✅: 필드/repo/외부호출 0 → 무위험·무churn. hash(HASH_MAPPER 공유)·orchestration(DB)은 잔류.
- **5-2 static util(InspectQrMetricSupport)** ✅: 무상태 순수함수 → 빈/주입 불필요. HwpxXmlSupport·ExcelStyleSupport 선례.
- **5-3 단일 통합** ✅(codex 동의): `InspectQrMetricSupport` 하나로 A+B+C 통합. 2분할 기각.
- **5-4 hash 군 잔류** ✅(codex 동의): HASH_MAPPER 가 metric/batch 와 공유되어 분리 시 역참조 발생 → 이번 비목표.
- **5-5 MANIFEST_SORT 접근 메서드** ✅(codex 요구): 상수 직접노출 대신 `manifestSort(key)` → `int[]` mutable 변조 차단.

---

## 6. 영향 범위 / 리스크

| 계층 | 파일 | 유형 |
|------|------|------|
| Service(신규) | `service/inspection/InspectQrMetricSupport.java` | 신규 util |
| Service(수정) | `service/inspection/InspectionQrBatchService.java` | A·B·C 제거 + 호출 치환 |
| Test(수정) | `service/inspection/InspectionQrBatchFormatTest.java` | import 경로 갱신 |

UI/DB/API 변경 0 → 디자인·DB팀 skip.

| 리스크 | 수준 | 완화 |
|---|---|---|
| 내부 호출 치환 누락(컴파일) | 낮음 | 정의 제거 후 미해결 호출 즉시 컴파일 에러 |
| 변환 동작 미세 변경 | 낮음 | 바이트 동일 이동 + FormatTest 19 케이스 + ServiceTest(DB) 통과로 보증 |
| ResultText 가시성/import 깨짐 | 낮음 | 동일 패키지(inspection) 유지 → package-private 접근 보존 |
| 상호 호출 깨짐 | 낮음 | A·B·C 함께 이동(동일 클래스 내 상호 호출 유지) |

---

## 8. 후속 버그픽스 백로그 (본 분리에서 동작 유지, 차기 별건)

본 분리는 **동작 100% 동일(순수 이동)** 이 원칙이므로 아래는 전부 **무변경 보존** — pre-existing 결함 교정은 별도 버그픽스 스프린트로 분리. (dual-review 2026-06-27 codex0/Opus5 합의=전부 pre-existing.)

- **8-1 2차 truncation 불일치(codex 기획검토 발견)**: `saveCheckResults` `formatValueWithContext` 반환 `ResultText.truncated()`로 remarks `(truncated)`를 붙이는데, 그 뒤 `resultText.text()`가 RESULT_TEXT_MAX 초과 시 한 번 더 자른다. 이 2차 절단은 `truncated=false`인 경우에도 발생 → **실제 잘렸는데 `(truncated)` 미표기** 가능. (정합 안: 2차 절단도 플래그 반영 또는 포맷터로 일원화 — 동작 변경이라 제외.)
- **8-2 `db.os.disk` 미사용 캐스트(dual-review)**: `formatValueWithContext`의 `db.os.disk` 분기에서 `rm = (Map)rootMount` 캐스트 후 미사용, 반환은 `"worst N개 마운트"` 고정. dead 변수(동작 무관). 정리 시 rm 제거 또는 p/t/f로 worst 텍스트 구성.
- **8-3 `parseNumeric` 다중 구분자(dual-review)**: `replaceAll("[^\\d.\\-]","")` 가 `1.2.3`/`1-2` 같은 입력을 비파싱 문자열로 만들어 silently null. 정합 안: `-?\d+(\.\d+)?` 첫 매칭 추출.
- **8-4 switch arm `"null건"` 출력(dual-review, 광범위)**: `db.os.netstat_perf`/`net_link`/`net_collisions`/`lsvg_rootvg`/`datafile_status` + (codex 확장) `gis.gws.store_size`→`nullGB`·`stdout_log_size`→`nullMB`·`ap.hw.adapter`→`null개 UP / null개`·`ap.net.routes`·`ap.security.users` 등이 키 부재 시 `null<단위>` 표기. 정합 안: 각 arm null 가드.
- **8-5 unchecked cast 키타입 미검증(dual-review)**: `(Map<String,Object>)value` 가 키 String 가정. 비-String 키 맵이면 `getKey().toUpperCase()` 런타임 실패 가능(Jackson 파싱 맵은 통상 String 키라 low). 정합 안: 키타입 검증 또는 `Map<?,?>` + 명시 변환.

---

## 9. 승인 요청

본 기획서(InspectQrMetricSupport 분리, codex 1차 NEEDS-FIX 반영본)에 대한 사용자 최종승인을 요청합니다. 승인 시 개발계획서 작성 → codex 검토 → 재승인 → 구현 순으로 진행합니다.
