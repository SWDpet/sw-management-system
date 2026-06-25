# [개발계획서] GlobalExceptionHandler·ErrorCode 커버리지 상향 (beyond-A)

- **작성팀**: 개발팀
- **작성일**: 2026-06-25
- **기획서**: `docs/product-specs/coverage-exception-handler.md` (codex APPROVE-WITH-FIX → 보완 반영, 사용자 최종승인 완료)
- **상태**: ✅ 완료 (2026-06-25). codex 기획/개발계획 APPROVE-WITH-FIX(보완 반영) + 구현검증. 신규 12테스트(GlobalExceptionHandler 11/ErrorCode 1). GlobalExceptionHandler 1.7%→79.3%(잔여=enum-mismatch 2핸들러 제외), ErrorCode 0%→100%. 전역 LINE 51.56%→52.67%. floor 0.49→0.50/0.41→0.42.

---

## 1. 작업 개요

`exception` 패키지에 순수 단위테스트 추가. **프로덕션 코드 변경 0**, 신규 테스트 2파일. MockHttpServletRequest/Response(spring-test)로 핸들러 직접 호출.

---

## 2. 신규 테스트 (T-n)

### `ErrorCodeTest`
| ID | 검증 |
|----|------|
| T-E1 | 대표 상수 getStatus/getCode/getMessage: INVALID_INPUT_VALUE(400,G001)·ACCESS_DENIED(403,A003)·RESOURCE_NOT_FOUND(404,R001)·INTERNAL_SERVER_ERROR(500,S001) |

### `GlobalExceptionHandlerTest`
공통 헬퍼: `apiReq()`=MockHttpServletRequest + addHeader("Accept","application/json"); `webReq()`=일반 URI; `resp()`=new MockHttpServletResponse(). API 분기는 ResponseEntity 캐스팅 후 status + body(ApiResponse) success/errorCode/message 단언, web 분기는 resp.getStatus()+getContentAsString() 단언.

| ID | 테스트 | 검증 |
|----|--------|------|
| T-H1 | `businessException_api` | handleBusinessException(new BusinessException(ErrorCode.DUPLICATE_PROJECT), apiReq, resp) → ResponseEntity 409 + body errorCode "D003" |
| T-H2 | `businessException_web_writesHtml` | webReq → resp.status=409, contentType text/html, content 에 코드/메시지 포함, 반환 null |
| T-H3 | `resourceNotFound_apiAndWeb` | ResourceNotFoundException → 404 API(body code) / web(404 HTML) |
| T-H4 | `accessDenied_apiAndWeb` | Spring Security AccessDeniedException → 403 API(A003) / web(403 HTML) |
| T-H5 | `validation_bindException_apiAndWeb` | BindException(BeanPropertyBindingResult target,name + `rejectValue("field","code","msg")` → FieldError 생성) → 400, 메시지=defaultMessage, getFieldErrors 로깅 람다 커버, API/web 양분기 |
| T-H6 | `constraintViolation_emptySet_apiAndWeb` | ConstraintViolationException(empty) → 400, "입력값이 올바르지 않습니다.", API/web 양분기 |
| T-H7 | `constraintViolation_withMockViolation_apiAndWeb` | mock ConstraintViolation(propertyPath/descriptor.annotation.annotationType/message) 1개 → 매핑 람다 커버 + 메시지 단언, API/web 양분기 |
| T-H8 | `noHandlerFound_apiAndWeb` | NoHandlerFoundException("GET","/x",new HttpHeaders()) → 404 API/web |
| T-H9 | `noResourceFound_noThrow` | handleNoResourceFound(new NoResourceFoundException(HttpMethod.GET,"/favicon.ico")) → 예외 없이 통과(void) |
| T-H10 | `genericException_apiAndWeb` | handleException(new RuntimeException("x")) → 500 API(S001) / web(500 HTML) |
| T-H11 | `isApiRequest_viaApiPath` | Accept 헤더 없이 setRequestURI("/api/x") → API 분기(JSON) 선택됨(예: generic 으로 확인) |

- enum-mismatch 2핸들러(handleEnumTypeMismatch/handleJsonEnumMismatch)는 best-effort 제외(생성 비용·EnumErrorResponseFactory 기존 테스트).
- getErrorHtml(private)·isApiRequest(private)는 위 핸들러 경유 간접 커버.

---

## 3. 구현 순서 (S)

| ID | 단계 |
|----|------|
| S-1 | `ErrorCodeTest`·`GlobalExceptionHandlerTest` 작성. |
| S-2 | `./mvnw test "-Dtest=ErrorCodeTest,GlobalExceptionHandlerTest"` → green(PowerShell 쉼표 인자 따옴표 필수). |
| S-3 | `./mvnw verify`(전체 + JaCoCo 게이트) green. |
| S-4 | JaCoCo csv 로 GlobalExceptionHandler·ErrorCode·전역 상승 확인 → floor 상향 판단(실측−2~2.5pp). |

---

## 4. 검증 기준 (NFR 매핑)

| NFR | 검증 |
|----|------|
| NFR-1 | S-2 신규 테스트 green |
| NFR-2 | S-3 전체 test green |
| NFR-3 | S-3 게이트 + S-4 상승 |

---

## 5. 롤백

신규 테스트 파일만 → 회귀 위험 극소. 문제 시 파일 제거. 프로덕션 영향 0.

---

## 6. 커밋 (작업완료 후)

- 메시지: `test(coverage): GlobalExceptionHandler 핸들러 분기 + ErrorCode 단위테스트 추가 (beyond-A)` (+ floor 상향 시 동봉)
- 듀얼푸시(SWDpet + ukjin914), author `ukjin_park@jungdouit.com`. 기획서·개발계획서 ✅ 완료 갱신 동봉.

---

## 7. 승인 요청

본 개발계획서에 대한 codex 검토 및 사용자 최종승인을 요청합니다.
