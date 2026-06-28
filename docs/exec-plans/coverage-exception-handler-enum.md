# 개발계획서 — GlobalExceptionHandler enum 바인딩 핸들러 커버리지 보강 (B)

- **상태**: ✅ 구현 완료(2026-06-28). +4테스트 green, GlobalExceptionHandler 79.3→100% LINE. 구현 1차 codex NEEDS-FIX(로그 미단언)→ListAppender+MDC 단언 보강→PASS + dual-review 합의7 반영. `mvnw -o clean verify` 1424 green.
- **작성일**: 2026-06-28
- **기획서**: `docs/product-specs/coverage-exception-handler-enum.md` (codex APPROVE-WITH-FIX)
- **안전망**: 현 green 스위트 + JaCoCo ratchet/PIT 게이트. **production 변경 0 (test-only)**.
- **원칙**: 기존 `GlobalExceptionHandlerTest`(11) 확장. 예외 구성은 `EnumErrorResponseFactoryTest`의 검증된 패턴 차용. 커밋 1개.

---

## Step 0 — baseline 고정(완료)

- 전역 LINE 77.91%·INSTR 63.72%(floor 0.75/0.61). 대상 GlobalExceptionHandler LINE 79.3%(miss 25, nc 74-75,86-94,99-114,177).
- **확정 패턴**(EnumErrorResponseFactoryTest 대조): `MethodArgumentTypeMismatchException("FOO", DocumentStatus.class, "status", null, new RuntimeException())`(DocumentStatus=DRAFT/COMPLETED) · `InvalidFormatException.from(null,"bad","UNKNOWN_TYPE",DocumentType.class)`+`prependPath("root","docType")`(DocumentType=COMMENCE/INTERIM/COMPLETION) · `HttpMessageNotReadableException(msg, cause, new MockHttpInputMessage(new byte[0]))`.
- 핸들러 반환은 `EnumErrorResponseFactory` 의 **`ResponseEntity<Map<String,Object>>`**(기존 ApiResponse 아님) → `bodyOf` 아닌 `(ResponseEntity<?>)result).getBody()` Map 직접 단언.

**검증:** 없음(준비).

## Step 1 — C1: handleEnumTypeMismatch + logUnknownEnum(principal 분기)

**1-1.** `enumTypeMismatch_returnsBadRequestWithAllowed`:
```java
MockHttpServletRequest req = new MockHttpServletRequest("GET", "/document/api/status/1");
req.setUserPrincipal(() -> "alice");  // logUnknownEnum userid 분기(principal present)
MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException(
        "FOO", DocumentStatus.class, "status", null, new RuntimeException());
Object res = handler.handleEnumTypeMismatch(ex, req);
```
- 단언: `(ResponseEntity)res` status 400, body `code`=ENUM_VALUE_NOT_ALLOWED·`enumType`=DocumentStatus.class.getName()·`allowed` containsExactlyInAnyOrder(DRAFT,COMPLETED)·`field`="status"·`path`="/document/api/status/1". (logUnknownEnum: enumType non-null + principal 분기 라인 도달.)

**검증:** `./mvnw -o -Dtest=GlobalExceptionHandlerTest test` green(부분).

## Step 2 — C2: handleJsonEnumMismatch (InvalidFormatException 분기)

**2-1.** `jsonEnumMismatch_withInvalidFormat_returnsBadRequest`:
```java
InvalidFormatException ife = InvalidFormatException.from(null, "bad", "UNKNOWN_TYPE", DocumentType.class);
ife.prependPath("root", "docType");
HttpMessageNotReadableException hmnr = new HttpMessageNotReadableException(
        "bad enum", ife, new MockHttpInputMessage(new byte[0]));
MockHttpServletRequest req = new MockHttpServletRequest("POST", "/document/api/save");
Object res = handler.handleJsonEnumMismatch(hmnr, req);
```
- 단언: status 400, body `enumType`=DocumentType.class.getName()·`allowed` containsExactlyInAnyOrder(COMMENCE,INTERIM,COMPLETION)·`field`="docType". (핸들러 line 89-91 ife 분기 + logUnknownEnum(targetType non-null).)

## Step 3 — C3: handleJsonEnumMismatch non-IFE + logUnknownEnum(enumType null/anonymous)

**3-1.** `jsonEnumMismatch_withoutInvalidFormat_returnsUnknown`:
```java
HttpMessageNotReadableException hmnr = new HttpMessageNotReadableException(
        "bad", new RuntimeException("x"), new MockHttpInputMessage(new byte[0]));
MockHttpServletRequest req = new MockHttpServletRequest("POST", "/x");  // principal 없음 → anonymous
Object res = handler.handleJsonEnumMismatch(hmnr, req);
```
- 단언: status 400, body `enumType`="UNKNOWN"·`allowed` 키 없음. (핸들러 targetType=null → logUnknownEnum(null,...) enumType-null 분기 + req principal 없음 → anonymous 분기.)

## Step 4 — C5: handleValidationException MethodArgumentNotValid 분기(line 177)

**4-1.** `validation_methodArgumentNotValid_apiAndWeb`:
```java
BeanPropertyBindingResult br = new BeanPropertyBindingResult(new Form(), "form");
br.rejectValue("name", "NotBlank", "이름은 필수입니다.");
MethodArgumentNotValidException ex = new MethodArgumentNotValidException(mock(MethodParameter.class), br);
```
- api → status 400 + message "이름은 필수입니다."(기존 BindException 테스트 동일 단언). web → null + resp 400. (line 177 `e instanceof MethodArgumentNotValidException` true 분기.)

**검증:** `./mvnw -o -Dtest=GlobalExceptionHandlerTest test` 전체 green.

## Step 5 — 전체 검증 + floor 검토

- `./mvnw -o clean verify` BUILD SUCCESS(1420 → +5).
- GlobalExceptionHandler LINE 79.3 → **high-90s** 확인(JaCoCo html).
- 전역 LINE/INSTR 재측정. 게인 작으면(25줄) **floor 유지**. ratchet·PIT 게이트 불변.

## Step 6 — codex 검증 + dual-review + 커밋 + 듀얼푸시

- codex 검증(분기·brittle·가드).
- dual-review(codex+Opus) → 수렴.
- 커밋: `test(coverage): GlobalExceptionHandler enum 바인딩 핸들러·logUnknownEnum 분기 보강 (beyond-A)`.
- `git push origin master` (듀얼).

---

## 검증 매트릭스 (FR/NFR → Step)

| 항목 | 검증 Step |
|---|---|
| FR C1 enumTypeMismatch + logUnknownEnum principal | Step 1 |
| FR C2 jsonEnumMismatch IFE | Step 2 |
| FR C3 jsonEnumMismatch non-IFE + enumType-null/anonymous | Step 3 |
| FR C5 validation MethodArgumentNotValid(177) | Step 4 |
| NFR verify SUCCESS·79.3→high-90s | Step 5 |
| NFR floor/ratchet/PIT 불변 | Step 5 |
| NFR production 0·듀얼푸시 | Step 6 |

## 롤백

- 단일 테스트 파일 케이스 추가 → 문제 시 제거. production 무영향.
