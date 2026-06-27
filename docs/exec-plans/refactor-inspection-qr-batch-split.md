# [개발계획서] InspectionQrBatchService 순수 변환 로직 분리 — InspectQrMetricSupport

- **작성팀**: 개발팀
- **작성일**: 2026-06-27
- **기획서**: `docs/product-specs/refactor-inspection-qr-batch-split.md` (codex 1차 NEEDS-FIX 반영 + 사용자 최종승인 2026-06-27).
- **상태**: ✅ **구현 완료(2026-06-27)**. InspectQrMetricSupport 신설(A 포맷+B 매핑+C 숫자), InspectionQrBatchService **705→474(-231)**. manifestSort(Integer 반환)로 int[] 비노출. ⚠**ResultText 이동으로 서비스의 record 필드 직접접근(.text/.truncated)→accessor(.text()/.truncated()) 전환 필요**(같은 클래스 nested 였을 때만 필드접근 가능했음). 2차 truncation(L299-301) 잔류(동작 유지, §8-1 백로그). FormatTest import 갱신 + manifestSort 보강 1. `mvnw -o clean verify` BUILD SUCCESS(1313 green/52 skip, ratchet·JaCoCo·arch 게이트 통과). codex 2차 PASS. dual-review→듀얼푸시.

---

## 1. 작업 개요

순수 변환 함수군(A 값 포맷팅 / B 섹션·정렬 매핑 / C 숫자 추출)을 신규 `InspectQrMetricSupport`(package-private static, `service.inspection` 패키지)로 이동. InspectionQrBatchService 내부 호출 치환. **순수 이동(동작 100% 동일)**. hash 군·orchestration 잔류. FormatTest import 갱신으로 직접테스트 보존. InspectionQrBatchService 705 → 약 450~460줄.

---

## 2. 구현 순서 (S-n)

### S-1 InspectQrMetricSupport 신설
- `service/inspection/InspectQrMetricSupport.java` (final, private ctor).
- 이동(본문 바이트 동일):
  - **A**: `formatValue`·`formatValueWithContext`·`buildRemarks` (static) + `UNIT_LABELS`(private static) + `RESULT_TEXT_MAX`(package-private static) + `ResultText` record.
  - **B**: `MANIFEST_SORT`(**private static** 유지) + static 초기화 블록 + `resolveSection`(static).
  - **C**: `extractPercent`·`parseNumeric` (static).
- **신규 접근 메서드**(기획 5-5): `static Integer manifestSort(String key)` — `return key == null ? null : (MANIFEST_SORT.get(key) == null ? null : MANIFEST_SORT.get(key)[0]);`. `int[]` 원시구조 비노출(mutable 변조 차단). 매핑 없으면 null 반환(호출부 null 분기 유지).
- 접근제어: 메서드는 `static`(package-private) — 동일 패키지(`com.swmanager.system.service.inspection`)라 Service·FormatTest 접근 가능. `UNIT_LABELS`/`MANIFEST_SORT`는 support 내부 private.
- import: support 가 사용하는 타입(InspectResultCode, Map/List/Set/Locale 등) 이관.

### S-2 InspectionQrBatchService 정리
- A·B·C 정의 + `UNIT_LABELS`·`MANIFEST_SORT`·`RESULT_TEXT_MAX`·`ResultText` 제거.
- 내부 호출 치환:
  - `saveCheckResults` L279 `resolveSection(key)` → `InspectQrMetricSupport.resolveSection(key)`.
  - `saveCheckResults` L280 `int[] mapped = MANIFEST_SORT.get(key); … mapped[0]` → `Integer mapped = InspectQrMetricSupport.manifestSort(key); … mapped`(if(mapped != null) sortOrder = mapped; else fallback — int[] → Integer 로 타입만 변경, 분기 동일).
  - `saveCheckResults` L297 `formatValueWithContext(...)` → `InspectQrMetricSupport.formatValueWithContext(...)`, L299 `RESULT_TEXT_MAX` → `InspectQrMetricSupport.RESULT_TEXT_MAX`, L303 `buildRemarks(...)` → `InspectQrMetricSupport.buildRemarks(...)`. `resultText.truncated()` 접근 동일(`ResultText` import 갱신).
  - `saveMetricSnapshot` L407 `extractPercent(value)` → `InspectQrMetricSupport.extractPercent(value)`.
- ⚠**잔류 보존**: `saveCheckResults` L299-301 **2차 truncation 로직은 그대로 유지**(기획 §8-1, 동작 100% 동일). 본 분리에서 교정하지 않음.
- `ResultText` 참조: `import com.swmanager.system.service.inspection.InspectQrMetricSupport.ResultText;` 또는 정규화 참조.
- 미사용 import 정리(ObjectMapper 등은 HASH_MAPPER 잔류로 유지).
- 정의 제거 후 미해결 호출은 컴파일 에러로 즉시 검출 → 전수 치환 보장.

### S-3 테스트 갱신
- `InspectionQrBatchFormatTest`: static import 6종(`buildRemarks`·`extractPercent`·`formatValue`·`formatValueWithContext`·`parseNumeric`·`resolveSection`) + `ResultText` import 를 `InspectionQrBatchService` → `InspectQrMetricSupport`로 경로 변경. **테스트 로직·단언 불변**(호출 대상만 변경).
- **신규 최소 1 케이스 보강**: `manifestSort(key)` 접근 메서드 — 매핑 존재(예: `ap.led.system`→0, `db.os.users`→23) + 미매핑(`unknown.x`→null) 검증. (기존 19 케이스가 A·C·resolveSection 직접 커버하므로 B 의 신규 메서드만 보강.)
- `InspectionQrBatchServiceTest`(DB, RUN_DB_TESTS): 순수함수 직접호출 0 → import 영향 없음. L318 500자 절단·L528 섹션 검증이 회귀 방어.

### S-4 검증
- `./mvnw -o clean test` — compile + 순수 테스트 green(390+). FormatTest(19+1) green.
- 회사 PC `RUN_DB_TESTS=true DB_URL=jdbc:postgresql://192.168.10.194:5880/SW_Dept DB_USERNAME=SW_Dept ./mvnw -o test` — InspectionQrBatchServiceTest 통합 green(섹션·정렬·resultText·remarks·metric snapshot 회귀).
- JaCoCo 게이트(floor)·PIT 게이트 green. ratchet(controller-repo) 불변.
- InspectionQrBatchService LOC 확인(약 450~460), GiantClassRatchet green(신규 support < 2000줄).

### S-5 (작업완료)
- dual-review(codex+Opus 독립검수) → 합의 반영 → 커밋 + 듀얼푸시.

---

## 3. 검증 기준 (NFR 매핑)

| NFR | 검증 |
|----|------|
| NFR-1 | S-4 compile + clean test green + RUN_DB_TESTS 통합 green |
| NFR-2 | S-3 FormatTest import 갱신 green(19) + manifestSort 보강 1 |
| NFR-3 | S-4 커버리지 비감소(순수 직접테스트 유지)·JaCoCo/PIT/ratchet 불변 |
| NFR-4 | S-5 dual-review + 듀얼푸시 |

---

## 4. 리스크 / 롤백

| 리스크 | 완화 |
|---|---|
| 내부 호출 치환 누락 | 정의 제거 후 컴파일 에러로 전수 검출 |
| `int[]`→`Integer` 변환 시 null 분기 오류 | manifestSort 가 미매핑 null 반환(기존 `mapped == null`과 동일 의미), 보강 테스트로 확인 |
| `ResultText` 가시성/import 깨짐 | 동일 패키지(inspection) 유지 → package-private 접근 보존 |
| 상호 호출 깨짐(formatValueWithContext→formatValue/UNIT_LABELS, extractPercent→parseNumeric) | A·B·C 함께 이동(동일 클래스 내 상호 호출 유지) |
| 2차 truncation 동작 변경 | L299-301 그대로 잔류(교정 안 함) + ServiceTest L318 회귀 방어 |

롤백: 단일 커밋 `git revert`. 순수 구조 이동.

---

## 5. 커밋

- `refactor(inspection): InspectionQrBatchService 순수 변환 로직 → InspectQrMetricSupport 분리 (S4 거대클래스 #3)`.
- 듀얼푸시. 기획/개발계획 ✅ 완료 갱신 동봉.

---

## 6. 승인 요청

본 개발계획서에 대한 codex 검토 및 사용자 최종승인을 요청합니다.
