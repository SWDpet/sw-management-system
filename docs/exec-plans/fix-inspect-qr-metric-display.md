# [개발계획서] InspectQrMetricSupport 표시/파싱 결함 교정 (§8 버그픽스)

- **작성팀**: 개발팀
- **작성일**: 2026-06-27
- **기획서**: `docs/product-specs/fix-inspect-qr-metric-display.md` (codex 2차 NEEDS-FIX 반영 + 사용자 최종승인 2026-06-27).
- **상태**: ✅ **구현 완료(2026-06-27)** — **§8-3 제외(철회)**. dual-review(codex2/Opus4) 합의로 parseNumeric 첫토큰 추출이 의미회귀(날짜/범위→숫자, malformed 마스킹)로 판정 → 원복. **§8-1·8-2·8-4만 커밋.** `mvnw -o clean verify` BUILD SUCCESS(1315 green/52 skip). 신규 테스트: switch arm null→`-`(4종+보존2)·context-경로 truncation remarks. parseNumeric 테스트는 `1.2.3`/`2026-05-01`→null 회귀잠금으로 갱신.

---

## 1. 작업 개요

`InspectQrMetricSupport`(8-2/8-3/8-4)와 `InspectionQrBatchService.saveCheckResults`(8-1)의 표시/파싱 결함 4종 교정. 정상 케이스 회귀 0. 동작표를 테스트로 잠금. 8-5 제외.

---

## 2. 구현 순서 (S-n)

### S-1 [8-3] parseNumeric 첫 유효숫자 매칭
- `InspectQrMetricSupport.parseNumeric` 재작성:
  ```java
  private static final java.util.regex.Pattern NUMERIC = java.util.regex.Pattern.compile("-?(?:\\d+(?:\\.\\d+)?|\\.\\d+)");
  static Double parseNumeric(String s) {
      if (s == null) return null;
      String cleaned = s.replace(",", "");          // 천단위 콤마 선제거
      java.util.regex.Matcher m = NUMERIC.matcher(cleaned);
      if (!m.find()) return null;
      try { return Double.parseDouble(m.group()); }
      catch (Exception e) { return null; }
  }
  ```
- 동작표 전수(11 케이스) 만족: `1,234`→1234·`52%`→52·`-3.5C`→-3.5·`abc`→null·`""`→null·`1.2.3`→1.2·`1-2`→1.0·`.5`→0.5·`1.`→1.0·`-`→null·`1,2,3`→123·`12.34.56`→12.34·`3.5.5%`→3.5·`--5`→-5.0.
  - ⚠`1.` → `m.group()`="1"(정규식이 `1.`의 `.`을 소수부 없이 끊음, `\.\d+` 미충족) → 1.0 ✓.
  - ⚠`-` → find() 매칭 없음 → null ✓.
  - ⚠`--5` → pos0 `-?`뒤 숫자없어 실패, pos1 `-5` 매칭 → -5.0 ✓.

### S-2 [8-4] switch arm null 가드
- `formatValueWithContext` switch arm 중 **키 부재 시 `null<단위>` 출력하는 arm**을 `Object v=m.get(key); yield v!=null ? v+"단위" : "-"` 형태로 가드. 대상(codex 확장 포함):
  - `db.os.netstat_perf`(total_errs+건), `db.os.net_link`(established+건), `db.os.net_collisions`(total_coll+건), `db.os.lsvg_rootvg`(stale_count+건), `db.oracle.datafile_status`(total+개), `gis.gws.store_size`(total_gb+GB), `gis.gws.stdout_log_size`(total_mb+MB), `ap.log.system_err`/`ap.log.security_err`(error_count+건), `ap.net.routes`(routes+개), `ap.security.users`(enabled+계정), `gis.gss.log_purge`/`gis.gws.log_purge`(purged_count+파일).
  - `ap.hw.adapter`(up/total → `null개 UP / null개`): `Object u=m.get("up"),t=m.get("total"); yield (u!=null&&t!=null)? u+"개 UP / "+t+"개" : "-"`.
  - `gis.gws.http`(http_status): 현재 `+""` → null 이면 "null". → `Object v=m.get("http_status"); yield v!=null? String.valueOf(v):"-"`.
- ⚠**값 존재 시 출력 불변**(기존 concat 그대로). null 일 때만 `-`.
- 이미 null-safe 한 arm(예 `gis.gws.running` getOrDefault, `db.oracle.export_last`/`standby_lag` 삼항, `db.os.iostat`/`net_ping` 삼항)은 **무변경**.

### S-3 [8-1] saveCheckResults truncation 플래그
- `InspectionQrBatchService.saveCheckResults`:
  ```java
  InspectQrMetricSupport.ResultText resultText = InspectQrMetricSupport.formatValueWithContext(key, value);
  String rtText = resultText.text();
  boolean actuallyTruncated = resultText.truncated();
  if (rtText != null && rtText.length() > InspectQrMetricSupport.RESULT_TEXT_MAX) {
      rtText = rtText.substring(0, InspectQrMetricSupport.RESULT_TEXT_MAX);
      actuallyTruncated = true;
  }
  row.setResultText(rtText);
  row.setRemarks(InspectQrMetricSupport.buildRemarks(status, code, actuallyTruncated));
  ```
- formatValue 경로(이미 truncated=true)는 OR 로 멱등 — 충돌 없음.

### S-4 [8-2] db.os.disk dead 캐스트 제거
- `formatValueWithContext` db.os.disk 분기:
  ```java
  if (key.equals("db.os.disk")) {
      Object rootMount = m.get("/");
      if (rootMount instanceof Map) {
          return new ResultText("worst " + m.size() + "개 마운트", false);
      }
      ...
  }
  ```
  - `rm` 캐스트 줄 제거(분기 조건 `instanceof Map` 유지). 출력 `worst N개 마운트` 불변.

### S-5 테스트
- `InspectionQrBatchFormatTest`:
  - `parseNumeric_*`: 동작표 11+보존 5 케이스 보강(기존 케이스 유지 + 신규 추가).
  - `formatValueWithContext` arm null→`-` 대표 4종(netstat_perf·hw.adapter·store_size·stdout_log_size) + 값 존재 보존 1.
  - db.os.disk `worst 2개 마운트` 단언 불변 확인.
- `InspectionQrBatchServiceTest`:
  - [8-1] `db.oracle.export_last` last_export 600자(또는 truncated=false 600자 반환 arm) → result_text 500 + remarks `(truncated)`. 기존 `k.long` 단언 불변.

### S-6 검증
- `./mvnw -o clean verify` BUILD SUCCESS. 기존 1313 green 유지(+신규 케이스).
- ratchet·JaCoCo·PIT 게이트 불변. 커버리지 비감소.

### S-7 (작업완료)
- dual-review → 합의 반영 → 커밋 `fix(inspection)` + 듀얼푸시.

---

## 3. 검증 기준 (NFR 매핑)

| NFR | 검증 |
|----|------|
| NFR-1 | S-6 clean verify green |
| NFR-2 | S-5 FormatTest 동작표 전수 + arm null |
| NFR-2b | S-5 ServiceTest truncation remarks |
| NFR-3 | S-6 ratchet·JaCoCo·PIT 불변 |
| NFR-4 | S-7 dual-review + 듀얼푸시 |

---

## 4. 리스크 / 롤백

| 리스크 | 완화 |
|---|---|
| parseNumeric 콤마/소수점 회귀 | 보존 케이스(`1,234`·`.5`) 단언 + 동작표 전수 테스트 |
| switch 값 존재 케이스 회귀 | null 분기만 추가, 값 존재 시 기존 concat 유지 + 보존 단언 |
| truncated 과표기 | 실제 절단 발생 시에만 OR 결합, formatValue 경로 멱등 |
| 표시값 변경 누락 arm | S-2 대상 목록 전수(codex 확장 포함) |

롤백: 단일 커밋 `git revert`. 동작 교정이라 §8 백로그도 함께 해소 표기.

---

## 5. 커밋

- `fix(inspection): QR metric 표시/파싱 결함 4종 교정 — null 가드·parseNumeric 첫숫자·truncation 플래그·dead 제거 (§8)`.
- 듀얼푸시. 기획/개발계획 ✅ 완료 갱신 + 기획 §8-2~8-4 해소 표기(8-5 잔존).

---

## 6. 승인 요청

본 개발계획서에 대한 codex 검토 및 사용자 최종승인을 요청합니다.
