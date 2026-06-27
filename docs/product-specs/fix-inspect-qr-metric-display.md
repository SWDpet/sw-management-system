# [기획서] InspectQrMetricSupport 표시/파싱 결함 교정 (§8 버그픽스)

- **작성팀**: 기획팀
- **작성일**: 2026-06-27
- **트랙**: S4 거대클래스 #3 분리(`348769b`) 후속. 그 dual-review(codex0/Opus5) 가 **전부 pre-existing** 으로 합의한 결함 5건을 별도 버그픽스 스프린트로 교정.
- **상태**: ✅ **완료(2026-06-27)** — 단 **§8-3(parseNumeric) 철회**. 구현 후 dual-review(codex2/Opus4) 합의가 §8-3 "첫토큰 추출"을 **의미회귀**(날짜 `2026-05-01`→2026·범위 `5-3`→5·malformed `1.2.3`→1.2 마스킹)로 지적 → 사용자 판정으로 **parseNumeric 원복**(기존 다중구분자→null 이 더 안전, "의도된 방어"로 재분류). **§8-1(truncation 플래그)·§8-2(db.os.disk dead 제거)·§8-4(switch null 가드)만 적용**. 분쟁 3건은 전부 한쪽 refute 로 기각(L158/L157=pre-existing·L230 instanceof=shape guard 유효).

---

## 1. 배경 / 목표

`InspectQrMetricSupport`(점검 QR batch metric → 표시텍스트/숫자 변환)와 `InspectionQrBatchService.saveCheckResults` 에 **사용자 화면 표시 결함 + 데이터 추출 누락**이 존재. 분리 스프린트는 "동작 100% 동일" 원칙이라 보존했고(기획 §8-1~8-5 백로그), 본 스프린트에서 **동작을 의도적으로 교정**한다.

⚠**핵심 제약**: 점검내역서(inspect_check_result.result_text/remarks)는 공무원 제출 문서에 반영되는 **사용자 가시 텍스트**. 교정은 표시 품질을 올리되, 정상 케이스 출력은 회귀 없이 보존해야 한다. 각 항목을 동작표로 고정하고 테스트로 잠근다.

---

## 2. 결함별 현재→수정후 동작

### 2-A [8-4] switch arm `null<단위>` 출력 (실표시버그, 우선순위 1)
`formatValueWithContext` 의 switch arm 다수가 `m.get(key) + "단위"` 직접 concat → 키 부재 시 **`null건`/`null개`/`nullGB`** 등을 화면에 표시.

| arm | key 부재 시 현재 | 수정후 |
|---|---|---|
| db.os.netstat_perf | `null건` | `-` |
| db.os.net_link | `null건` | `-` |
| db.os.net_collisions | `null건` | `-` |
| db.os.lsvg_rootvg | `null건` | `-` |
| db.oracle.datafile_status | `null개` | `-` |
| gis.gws.store_size | `nullGB` | `-` |
| gis.gws.stdout_log_size | `nullMB` | `-` |
| ap.hw.adapter | `null개 UP / null개` | `-` |
| ap.net.routes | `null개` | `-` |
| ap.security.users | `null계정` | `-` |
| ap.log.system_err/security_err | `null건` | `-` |
| gis.gss.log_purge/gws.log_purge | `null파일` | `-` |

**수정**: 각 arm 을 `Object v=m.get(key); yield v!=null ? v+"단위" : "-"` 가드. 값 존재 시 출력 **불변**(정상 케이스 회귀 0). fallback 통일값 = `-`(타 fallback `"-"` 과 일관).

### 2-B [8-3] `parseNumeric` 다중 구분자 — ❌ **철회**(구현 후 dual-review 합의)
> ⚠ **결론: 변경하지 않음.** 아래는 당초 계획이나, 구현 후 dual-review(codex+Opus)가 "첫토큰 추출"이 날짜/범위/malformed 문자열을 숫자로 오인·마스킹하는 **의미회귀**라고 합의 → parseNumeric **원복**. 기존 동작(다중구분자 `1.2.3`·날짜 `2026-05-01` → null)이 malformed 데이터를 노출해 더 안전하므로 "버그"가 아니라 **의도된 방어**로 재분류. ("값+단위" `52%`/`1,234`/`-3.5C` 정상처리는 기존 코드도 이미 동일.) 아래 원안은 기록 보존용.

#### (원안, 미적용) parseNumeric 다중 구분자 silently null
`replaceAll("[^\\d.\\-]","")` 후 `Double.parseDouble` → `1.2.3`·`1-2` 등이 비파싱 문자열이 되어 catch 로 null. metric snapshot(extractPercent) 의 cpu/mem/disk 누락 유발 가능.

⚠**제약**: 기존 `parseNumeric("1,234")==1234.0`(콤마=천단위 제거) 단언 **보존 필수**.

| 입력 | 현재 | 수정후 | 비고 |
|---|---|---|---|
| `"52%"` | 52.0 | 52.0 | 불변 |
| `"1,234"` | 1234.0 | **1234.0** | 콤마 선제거 보존 |
| `"-3.5C"` | -3.5 | -3.5 | 불변 |
| `"abc"` | null | null | 불변 |
| `""` | null | null | 불변 |
| `"1.2.3"` | null | **1.2** | 첫 유효숫자 |
| `"1-2"` | null | **1.0** | 첫 유효숫자 |
| `".5"` | 0.5 | **0.5** | 선행 소수점 보존(회귀 방지) |
| `"1."` | 1.0 | 1.0 | 불변 |
| `"-"` | null | null | 불변 |
| `"1,2,3"` | 123.0 | 123.0 | 콤마 전부 제거 |
| `"12.34.56"` | null | **12.34** | 첫 유효숫자(둘째 dot 이후 버림) |
| `"3.5.5%"` | null | **3.5** | 첫 유효숫자 |
| `"--5"` | null | **-5.0** | find()가 2번째 위치 `-5` 매칭(비정상 부호 입력, -5.0 고정) |

**수정**: ① 콤마(`,`) 선제거(천단위) → ② 정규식 **`-?(?:\d+(?:\.\d+)?|\.\d+)`** 첫 매칭(`Pattern.compile(...).matcher(cleaned).find()` → `group()`)→ `Double.parseDouble`, 매칭 없으면 null. 콤마 선제거로 `1,234`→`1234`→`1234`. ⚠**선행 소수점 분기(`|\.\d+`)** 필수 — 누락 시 `.5`→`5.0` 회귀(현재 0.5). `replaceAll` blanket 제거 폐기. 다중구분자는 silently null 대신 첫 유효숫자 채택(데이터 보존).

### 2-C [8-1] 2차 truncation 플래그 미반영 (remarks 누락, 우선순위 3)
`saveCheckResults`: `formatValueWithContext` 가 `truncated=false` 로 500 초과 텍스트를 반환할 수 있는데, 서비스가 `rtText` 를 재절단하면서 `buildRemarks(..., resultText.truncated())` 에 **원래 플래그(false)** 를 넘김 → 실제 잘렸는데 remarks `(truncated)` 누락.

**수정**: 재절단 발생 여부를 반영. `boolean actuallyTruncated = resultText.truncated() || (rtText 가 MAX 초과로 잘림); buildRemarks(status, code, actuallyTruncated)`. result_text 절단값 자체는 불변(500자), remarks 만 정합.

### 2-D [8-2] `db.os.disk` 미사용 캐스트 (dead 정리, 우선순위 4)
`rm = (Map)rootMount` 캐스트 후 미사용, 반환 `"worst N개 마운트"` 고정. **동작 무관 dead 변수**.

**수정**: `rm` 캐스트 제거(분기 조건은 `rootMount instanceof Map` 유지). 출력 **불변** → 기존 단언 `"worst 2개 마운트"` 통과. (worst 텍스트 강화는 표시 스펙 변경이라 본 스프린트 제외 — dead 제거만.)

### 2-E [8-5] unchecked cast 키타입 미검증 (방어, **본 스프린트 제외**)
`(Map<String,Object>)value` 가 키 String 가정. Jackson 파싱 맵은 통상 String 키라 실위험 low(즉시 사용자 표시버그 아님). **본 스프린트 명시적 제외** — 별도 cleanup 티켓으로 분리(codex 권고). raw `value instanceof Map` + unchecked cast 가 넓게 분포해 일괄 정리가 적합. → 기획 §8-5 백로그 유지.

---

## 3. 기능 요건 (FR)

| ID | 내용 |
|----|------|
| FR-1 | [8-4] 키 부재 arm 출력이 `null<단위>` → `-`. 값 존재 시 출력 불변. |
| FR-2 | [8-3] `1.2.3`/`1-2` 첫 유효숫자 추출. `1,234`/`52%`/`-3.5C`/`abc`/`""` 기존 동작 보존. |
| FR-3 | [8-1] 2차 절단 시 remarks `(truncated)` 표기. 정상/formatValue 경로 불변. |
| FR-4 | [8-2] dead `rm` 제거, db.os.disk 출력 불변. |
| FR-5 | 그 외 모든 변환 동작 불변(정상 케이스 회귀 0). |

---

## 4. 비기능 요건 (NFR)

| ID | 내용 |
|----|------|
| NFR-1 | `./mvnw -o clean verify` BUILD SUCCESS. 기존 1313 green 유지(변경 단언 갱신분 제외). |
| NFR-2 | **FormatTest 케이스 추가**: [8-3] parseNumeric 동작표 전수 — `1.2.3`→1.2·`1-2`→1.0·`.5`→0.5·`1.`→1.0·`-`→null·`1,2,3`→123·`12.34.56`→12.34·`3.5.5%`→3.5·`--5`→-5.0 + 보존 `1,234`→1234·`52%`→52·`-3.5C`→-3.5·`abc`→null·`""`→null. [8-4] arm null→`-` 대표 4종(netstat_perf·hw.adapter·store_size·stdout_log_size) + 값 존재 시 보존 1. [8-2] db.os.disk `worst N개 마운트` 불변. |
| NFR-2b | **ServiceTest 필수 보강**: [8-1] `formatValueWithContext`가 truncated=false 로 500 초과 반환하는 케이스(예 `db.oracle.export_last` last_export 600자) → saveCheckResults 가 result_text 500자 절단 + remarks `(truncated)` 표기 검증. 기존 `k.long`(formatValue 경로) 단언은 불변. |
| NFR-3 | 커버리지 비감소. ratchet·JaCoCo·PIT 게이트 불변. |
| NFR-4 | dual-review → 듀얼푸시. |

---

## 5. 의사결정

- **5-1 범위 = 8-1·8-2·8-3·8-4** ✅: 표시버그·데이터누락·dead. **8-5(unchecked)는 제외 확정**(codex 권고: low + 광범위 → 별도 cleanup 티켓).
- **5-2 fallback 표기 `-`** ✅: 기존 fallback(`"-"`/`"N/A"`/`"없음"`)과 일관. 빈 문자열보다 "데이터 없음"이 명확.
- **5-3 parseNumeric 콤마 선제거 + 첫매칭** ✅: 천단위 보존 + 다중구분자 데이터 보존 양립.
- **5-4 db.os.disk worst 텍스트 강화 제외** ✅: 표시 스펙 변경은 별건(디자인/현업 확인 필요). 본 스프린트는 dead 제거만.

---

## 6. 영향 범위 / 리스크

| 계층 | 파일 | 유형 |
|------|------|------|
| Service(수정) | `service/inspection/InspectQrMetricSupport.java` | switch 가드·parseNumeric·dead 제거 |
| Service(수정) | `service/inspection/InspectionQrBatchService.java` | saveCheckResults truncation 플래그 |
| Test(수정) | `service/inspection/InspectionQrBatchFormatTest.java` | 케이스 추가 |
| Test(수정·필수) | `service/inspection/InspectionQrBatchServiceTest.java` | 8-1 2차 truncation remarks 케이스 필수(FormatTest 만으론 saveCheckResults 절단경로 미커버, codex) |

UI/DB/API 스키마 변경 0(표시 텍스트 값만). 디자인·DB팀 skip(단 result_text 는 표시값이라 §2 동작표가 곧 표시 스펙).

| 리스크 | 수준 | 완화 |
|---|---|---|
| 정상 케이스 회귀(값 존재 arm) | 낮음 | 가드는 null 일 때만 분기, 값 존재 시 기존 concat 유지 + FormatTest 보존 단언 |
| parseNumeric 콤마 회귀 | 중 | `1,234` 보존 단언 유지 + 콤마 선제거 순서 고정 |
| 첫매칭 정규식 부작용 | 낮음 | 동작표의 7케이스 전수 테스트 |
| truncated 플래그 과표기 | 낮음 | 실제 절단 발생 시에만 OR 결합 |

---

## 7. 승인 요청

본 기획서(§8 버그픽스)에 대한 codex 검토 및 사용자 최종승인을 요청합니다. 특히 **5-1(8-5 포함 여부)**, **5-3(parseNumeric 첫매칭 vs null 유지)**, **5-2(fallback `-` 적절성)** 의견 요청.
