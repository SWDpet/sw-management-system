# [기획서] GlobalExceptionHandler enum 바인딩 핸들러 커버리지 보강 (B)

- **작성팀**: 기획팀 / **작성일**: 2026-06-28
- **트랙**: beyond-A 커버리지 증분 B. 예외 처리 컴포넌트. 기존 `GlobalExceptionHandlerTest` 확장형.
- **상태**: ✅ 구현 완료(2026-06-28). GlobalExceptionHandlerTest +4(11→15) → GlobalExceptionHandler **79.3→100% LINE**(121/121)·BRANCH 35/40. 전역 LINE 77.91→78.11%·INSTR 63.72→63.84%, floor 유지. `mvnw -o clean verify` 1424 green. codex 기획/개발계획 APPROVE-WITH-FIX·구현 **1차 NEEDS-FIX(로그 미단언 위장통과)→logback ListAppender+MDC 단언 보강→PASS**. dual-review(codex2/Opus7) 합의7 반영(appender stop·로거레벨 고정·MDC.clear·실 MethodParameter·Javadoc 라인번호 제거·List 일관성), 분쟁2 미반영(enum 리터럴=API 계약).
- **codex 보완**: 예외 구성은 기존 `EnumErrorResponseFactoryTest`의 **검증된 패턴 그대로** — `new MethodArgumentTypeMismatchException("FOO", DocumentStatus.class, "status", null, new RuntimeException())`(MethodParameter=null, mock 불요) · `InvalidFormatException.from(null,"bad","UNKNOWN_TYPE",DocumentType.class)`+`ife.prependPath("root","docType")` · `new HttpMessageNotReadableException(msg, ife, new MockHttpInputMessage(new byte[0]))` · C3 비-IFE는 `new HttpMessageNotReadableException("bad", new RuntimeException("x"), input)` · C5 `MethodArgumentNotValidException(mock(MethodParameter.class), br)`. 커버리지 목표는 **"high-90s"**(미커버 25줄 타깃 시, 정확 ~98% 단정 회피).

---

## 1. 배경 / 목표

`GlobalExceptionHandler`(331줄, **LINE 79.3% / miss 25**)는 세션7(`d3a52d6`)에서 7핸들러를 79.3%까지 덮었으나, **enum 바인딩 실패 2핸들러(`handleEnumTypeMismatch`/`handleJsonEnumMismatch`) + `logUnknownEnum` + `handleValidationException` 의 MethodArgumentNotValid 분기**를 "EnumErrorResponseFactory 위임이라 best-effort 제외"로 남겼다. 이들은 잘못된 enum 입력 시 400 응답·모니터링 로그(MDC `UNKNOWN_ENUM_VALUE`)를 결정하는 의미 있는 분기이며, 순수 단위(MockHttpServletRequest + 예외 직접 구성)로 박제 가능하다.

JaCoCo 미커버 라인(`GlobalExceptionHandler.java.html` nc): 74-75, 86-94, 99-114, 177.

## 2. 대상 분기

| # | 영역(라인) | 내용 |
|---|---|---|
| C1 | handleEnumTypeMismatch(74-75) | `MethodArgumentTypeMismatchException`(requiredType=enum) → `logUnknownEnum` + `EnumErrorResponseFactory.from` → 400 body(code=ENUM_VALUE_NOT_ALLOWED, enumType, allowed) |
| C2 | handleJsonEnumMismatch + IFE 분기(86-94) | cause=`InvalidFormatException`(targetType=enum, path→field) → targetType/inputValue 추출 + factory → 400 |
| C3 | handleJsonEnumMismatch non-IFE 분기(86-88,93-94) | cause=비-IFE → targetType=null → factory enumType="UNKNOWN" + `logUnknownEnum(null,...)` |
| C4 | logUnknownEnum(99-114) | MDC put/finally remove 전체. enumType null/non-null·req userPrincipal present/anonymous 분기(C1=principal, C3=anonymous) |
| C5 | handleValidationException MethodArgumentNotValid(177) | 기존은 BindException만 커버 → `MethodArgumentNotValidException` 분기 |

## 3. 변경 — `GlobalExceptionHandlerTest` 케이스 추가 (테스트만)
- 기존 테스트(11) 스타일 재사용(`MockHttpServletRequest`/`Response`, api vs web). 신규 ~5 테스트.
- **C1**: `new MethodArgumentTypeMismatchException(value, SomeEnum.class, "field", mockMethodParameter, cause)` + req(userPrincipal set) → 반환 ResponseEntity status 400 + body `code`=ENUM_VALUE_NOT_ALLOWED·`enumType`=enum.getName()·`allowed` 포함·`field`·`path` 단언.
- **C2**: `InvalidFormatException.from(null, msg, value, SomeEnum.class)`(path 포함되도록 구성 or path 비어있으면 field="unknown") → `HttpMessageNotReadableException(msg, ife, mockHttpInputMessage)` → 400 + enumType=enum.getName().
- **C3**: cause=`new RuntimeException()` 래핑한 HttpMessageNotReadableException → targetType null → body enumType="UNKNOWN"·allowed 없음. req userPrincipal 없음(anonymous 분기).
- **C4**: C1/C3 가 logUnknownEnum 전 분기 커버(enumType null/non-null·principal/anonymous). MDC 부작용은 finally 로 정리됨(별도 단언 불필요, 라인 도달이 목표).
- **C5**: `new MethodArgumentNotValidException(mockMethodParameter, bindingResult)` → handleValidationException api/web → 400(api)·400 html(web). 기존 BindException 테스트와 동일 단언 패턴.

## 4. 요건
- **FR**: C1~C5 분기 박제(enum 400 응답 계약 + 모니터링 로그 경로 + validation MethodArgumentNotValid 분기).
- **NFR**: production 변경 0, `mvnw -o clean verify` SUCCESS, GlobalExceptionHandler 79.3→high-90s LINE(미커버 25줄 해소 시), JaCoCo floor 유지(전역 게인 작아 버퍼 흡수), 구현 후 codex PASS → 듀얼푸시.

## 5. 영향/리스크
- `exception/GlobalExceptionHandlerTest.java` 케이스 추가. production 0.
- 리스크: 예외 객체 구성 — `MethodArgumentTypeMismatchException`/`InvalidFormatException`/`MethodArgumentNotValidException` 생성자에 `MethodParameter`/`HttpInputMessage` mock 필요(구현 시 정확 시그니처 확인). `InvalidFormatException.from(null parser,...)` 가 NPE 시 대안(직접 생성자 or path 빈 케이스로 단순화).
- MDC 정적 상태는 finally 로 정리(테스트 간 누수 없음).
