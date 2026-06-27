# [기획서] Map.of("error", e.getMessage()) null-안전 sweep (백로그 #3 · correctness)

- **작성팀**: 기획팀
- **작성일**: 2026-06-27
- **트랙**: nullsafe 하드닝(`2644743`)에서 만든 `ExceptionMessages.safe()` 를 동일 NPE 위험 전 사이트로 확장. 누적 백로그.
- **상태**: ✅ **완료(2026-06-27)**. 4컨트롤러 19사이트 `Map.of("error", e.getMessage())`→`ExceptionMessages.safe(e)`(멀티라인 포함, perl 잔여 0). ExceptionMessagesTest 3. dual-review codex1/Opus6 — "DocumentFileController import 누락"(high)은 오탐(직전 커밋서 추가·build green), 분쟁4 refute(safe non-null·message 보존). 듀얼푸시.

---

## 1. 배경 / 목표

`java.util.Map.of` 는 null 값에 NPE. `Map.of("error", e.getMessage())` 는 예외의 getMessage()==null(예: NPE) 이면 **catch 안에서 다시 NPE → 원래 에러 응답이 깨지고 상위로 전파**(500 또는 더 나쁜 경로). batch/upload 는 직전 스프린트에서 수정했고, 동일 패턴이 **4 컨트롤러 19 사이트** 잔존(codex 재집계 — QuotationController 멀티라인 1개 포함):

| 파일 | 사이트 수 |
|---|---|
| controller/DocumentController | 1 (saveDocument catch) |
| controller/DocumentFileController | 4 (saveSignature·uploadAttachment·deleteSignedScan catch; uploadSignedScan 2개는 직전 스프린트서 이미 safe) |
| qrcode/controller/QrLicenseController | 4 |
| quotation/controller/QuotationController | 10 (멀티라인 1개 포함) |

**목표**: 18 사이트의 `e.getMessage()` → `ExceptionMessages.safe(e)`(message null 이면 클래스 단순명). **동작: message 있으면 동일, null 일 때만 NPE 대신 안전 문자열**. 로직·상태코드·응답 키 불변.

---

## 2. 변경 설계

- 각 사이트 `Map.of("error", e.getMessage())` → `Map.of("error", ExceptionMessages.safe(e))`. (e 가 catch 변수.)
- import `com.swmanager.system.util.ExceptionMessages` 추가(파일별 1회).
- **범위 한정**: `Map.of(...)` 안의 error=e.getMessage() 만. `put("error", e.getMessage())`(HashMap, null 허용 → NPE 없음)·`ApiResult.failMessage(e.getMessage())`(record, null 허용)·로그(log.error(e.getMessage()))는 NPE 무관 → **미대상**.
- ⚠license 영구패스 정책은 coverage 추천금지지 bug fix 아님 → QrLicenseController 포함(정당).

---

## 3. 기능 요건 (FR)

| ID | 내용 |
|----|------|
| FR-1 | 18 사이트 모두 getMessage()==null 예외에서 NPE 없이 에러 응답 정상(상태코드·키 불변). |
| FR-2 | message 비-null 예외는 기존과 동일 문자열 반환(동작 불변). |
| FR-3 | Map.of 외 패턴(put/ApiResult/log)·다른 로직 불변. |

---

## 4. 비기능 요건 (NFR)

| ID | 내용 |
|----|------|
| NFR-1 | compile + `./mvnw -o clean verify` green. |
| NFR-2 | `ExceptionMessages` 단위테스트 신규(message 있음→그대로, null→클래스명, throwable null→""). 기존 컨트롤러 테스트(에러 경로) 불변 green. |
| NFR-3 | 커버리지 비감소·floor 유지·ratchet 불변. |
| NFR-3b | **구현 후 grep 재검증(codex)**: 4 대상 파일에 `Map.of(..."error"..., e.getMessage())` 0건(멀티라인 포함). 멀티라인 사이트도 누락 없이 치환. |
| NFR-4 | dual-review → 듀얼푸시. |

---

## 5. 의사결정

- **5-1 sweep 일괄** ✅: 동일 결함군을 util 로 일괄 정리(부분 수정 시 패턴 표류). 18 사이트 기계적·저위험(에러 경로만).
- **5-2 Map.of 한정** ✅: put/ApiResult/log 는 null-안전 또는 NPE 무관 → 불필요한 변경 회피.
- **5-3 ExceptionMessages 단위테스트로 로직 검증** ✅: 사이트별 테스트 대신 util 1 테스트 + 기존 컨트롤러 테스트로 회귀 방어(과잉 테스트 회피).

---

## 6. 영향 범위 / 리스크

| 계층 | 파일 | 유형 |
|------|------|------|
| Controller | DocumentController·DocumentFileController·QrLicenseController·QuotationController | error 값 null-안전(기계적) |
| Test(신규) | `util/ExceptionMessagesTest.java` | util 단위테스트 |

UI/DB 변경 0. 동작 변경 = null-message 시 NPE→안전문자열(에러 경로만).

| 리스크 | 수준 | 완화 |
|---|---|---|
| 잘못된 사이트 치환(비-catch e) | 낮음 | catch 블록 내 `Map.of("error", e.getMessage())` 만, 컴파일+기존 테스트 |
| QuotationController 9 사이트 누락/오치환 | 낮음 | grep 전후 카운트 대조(18→0) |
| 동작 회귀 | 낮음 | message 비-null 경로 동일, 기존 테스트 green |

---

## 7. 승인 요청

본 기획서(error-message null-안전 sweep)에 대한 codex 검토 및 사용자 최종승인을 요청합니다.
