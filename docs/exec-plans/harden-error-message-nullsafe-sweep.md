# [개발계획서] Map.of("error", e.getMessage()) null-안전 sweep

- **작성팀**: 개발팀
- **작성일**: 2026-06-27
- **기획서**: `docs/product-specs/harden-error-message-nullsafe-sweep.md` (codex APPROVE-WITH-FIX 보완 + 사용자 최종승인).
- **상태**: ✅ **완료(2026-06-27)**. 19사이트 치환(잔여 perl -0 재검증 0)·import 4파일(DocumentFile은 직전 커밋 보유)·ExceptionMessagesTest 3. clean verify green, ratchet 불변, 커버리지 LINE 72.11%.

---

## 1. 작업 개요

4 컨트롤러 19 사이트의 `Map.of("error", e.getMessage())` → `ExceptionMessages.safe(e)`. import 추가. ExceptionMessages 단위테스트 신규. 기존 컨트롤러 테스트로 회귀 방어.

---

## 2. 구현 순서 (S-n)

### S-1 사전 확인
- 대상 4파일에 `put("error", e.getMessage())`·`ApiResult...(e.getMessage())` 등 **Map.of 아닌 `"error", e.getMessage()` 동일 substring** 존재 여부 grep(치환 오염 방지). 있으면 Map.of 만 선별 수동.
- catch 변수명 e 확인(non-e 있으면 개별 처리).

### S-2 치환 (파일별)
- 멀티라인 포함이라 perl -0(slurp) 사용: `s/"error",\s*e\.getMessage\(\)/"error", ExceptionMessages.safe(e)/g` (대상 4파일). ⚠S-1 에서 put/ApiResult 충돌 없음 확인 후에만 일괄, 충돌 시 Map.of 라인만 Edit.
- 각 파일 `import com.swmanager.system.util.ExceptionMessages;` 추가.

### S-3 단위테스트
- `src/test/java/com/swmanager/system/util/ExceptionMessagesTest.java`:
  - safe_withMessage_returnsMessage: `safe(new RuntimeException("boom"))` == "boom".
  - safe_nullMessage_returnsSimpleName: `safe(new NullPointerException())` == "NullPointerException".
  - safe_nullThrowable_empty: `safe(null)` == "".

### S-4 검증
- **잔여 재검증(NFR-3b, codex: 멀티라인-aware 필수)**: 줄단위 grep 은 멀티라인 1건 놓침 → **perl -0 slurp** 사용: `perl -0ne 'print "$ARGV\n" if /"error",\s*e\.getMessage\(\)/' 대상4파일` → 출력 0(잔여 0건). + import 는 "없으면 추가".
- `./mvnw -o clean verify` green. ratchet 불변·커버리지 비감소.

### S-5 (작업완료)
- dual-review → 합의 반영 → 커밋 + 듀얼푸시.

---

## 3. 검증 기준 (NFR 매핑)

| NFR | 검증 |
|----|------|
| NFR-1 | S-4 compile + clean verify |
| NFR-2 | S-3 ExceptionMessagesTest green + 기존 컨트롤러 테스트 불변 |
| NFR-3 | S-4 커버리지 비감소·ratchet 불변 |
| NFR-3b | S-4 grep 재검증 0건(멀티라인 포함) |
| NFR-4 | S-5 dual-review + 듀얼푸시 |

---

## 4. 리스크 / 롤백

| 리스크 | 완화 |
|---|---|
| perl 치환이 put/ApiResult 등 오염 | S-1 사전 grep 확인 후에만 일괄, 충돌 시 수동 Edit |
| 멀티라인 사이트 누락 | perl -0 slurp + S-4 grep 재검증 |
| 비-e 변수 | S-1 확인 |

롤백: 단일 커밋 `git revert`. 에러 경로 null-안전화라 원복 시 원래 결함.

---

## 5. 커밋

- `fix(document): Map.of error 메시지 null-안전 sweep — ExceptionMessages.safe (harden-error-msg-sweep)`.
- 듀얼푸시. 기획/개발계획 ✅ 완료 갱신 동봉.

---

## 6. 승인 요청

본 개발계획서에 대한 codex 검토 및 사용자 최종승인을 요청합니다.
