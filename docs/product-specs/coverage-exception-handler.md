# [기획서] GlobalExceptionHandler·ErrorCode 커버리지 상향 (beyond-A)

- **작성팀**: 기획팀
- **작성일**: 2026-06-25
- **트랙**: beyond-A 커버리지 스프린트. 선행 `coverage-dtos`(`09b018b`, 전역 50% 돌파).
- **상태**: ✅ 완료 (2026-06-25). codex APPROVE-WITH-FIX(보완 반영) + 구현검증. 신규 12테스트. GlobalExceptionHandler 1.7%→79.3%, ErrorCode 0%→100%. 전역 LINE 51.56%→52.67%.

---

## 1. 배경 / 목표

`exception` 패키지가 미커버 대량으로 남음:
| 클래스 | 현재 LINE | 미커버 |
|---|---|---|
| GlobalExceptionHandler | **1.7%** | 119 |
| ErrorCode (enum) | **0%** | 29 |

GlobalExceptionHandler 는 `@RestControllerAdvice` 의 핸들러 ~9종으로, 각 **API 요청(JSON)** vs **웹 요청(HTML)** 분기(`isApiRequest`)를 가진다. MockHttpServletRequest/Response 로 핸들러를 직접 호출해 단위 테스트 가능(MockMvc·컨텍스트 불요).

**목표**: 예외 핸들러 분기 + ErrorCode enum 에 순수 단위테스트 추가 → 커버리지 대폭 상향. 프로덕션 코드 변경 0.

---

## 2. 테스트 대상

### 2-1. ErrorCode
- 대표 상수(INVALID_INPUT_VALUE/RESOURCE_NOT_FOUND/INTERNAL_SERVER_ERROR 등) getStatus/getCode/getMessage 값 고정.

### 2-2. GlobalExceptionHandler (핸들러별 API/web 양분기)
- `isApiRequest`: `/api/**` 경로 또는 `Accept: application/json` → API(JSON), 그 외 → web(HTML). 두 경로 모두.
- `handleBusinessException`: API → ResponseEntity(errorCode.status, ApiResponse.error); web → response.status + HTML write(getErrorHtml).
- `handleResourceNotFound`: 404 API/web.
- `handleAccessDenied`(Spring Security AccessDeniedException): 403 API/web.
- `handleValidationException`(BindException 경유): 400, 메시지 = 첫 에러 defaultMessage, API/web.
- `handleConstraintViolation`: (a) 빈 violation set → 400, "입력값이 올바르지 않습니다."; (b) **mock ConstraintViolation 1개**(propertyPath/descriptor.annotationType/message) → 매핑 람다 커버 + 메시지 단언. API/web.
- `handleNotFound`(NoHandlerFoundException): 404 API/web.
- `handleNoResourceFound`(NoResourceFoundException): void(로그만) — 호출 시 예외 없이 통과.
- `handleException`(generic Exception): 500 API/web.
- `getErrorHtml`(private): web 분기 테스트로 간접 커버(HTML 에 status/code/message 포함).

### 2-3. 범위 제외(best-effort)
- `handleEnumTypeMismatch`(MethodArgumentTypeMismatchException)·`handleJsonEnumMismatch`(HttpMessageNotReadableException): 인스턴스 생성이 까다롭고(MethodParameter/InvalidFormatException 래핑) 이미 `EnumErrorResponseFactory`(테스트 보유)에 위임 → 본 스프린트 best-effort, 미달 시 후속.

---

## 3. 기능 요건 (FR)

| ID | 내용 |
|----|------|
| FR-1 | 신규 `GlobalExceptionHandlerTest`·`ErrorCodeTest` 추가(순수, MockHttpServletRequest/Response 사용). |
| FR-2 | §2-2 핸들러(`handleNoResourceFound` 제외 — void·request/response 미수신이라 "예외 없이 통과" 단일 테스트) 각각 API 경로(Accept json 또는 /api/) + web 경로 두 분기 단언(status + ApiResponse body success/errorCode/message / HTML 내용). |
| FR-3 | 프로덕션 코드(GlobalExceptionHandler/ErrorCode 등) 무변경. 테스트만. |

---

## 4. 비기능 요건 (NFR)

| ID | 내용 |
|----|------|
| NFR-1 | 신규 테스트 전건 green. |
| NFR-2 | 전체 `./mvnw test` green. |
| NFR-3 | JaCoCo 게이트 통과 + exception 패키지·전역 커버리지 상승. floor 상향은 구현 후 실측 판단. |

---

## 5. 의사결정

- **5-1 MockHttpServletRequest/Response** ✅: spring-test 제공. API 분기는 `addHeader("Accept","application/json")` 또는 `setRequestURI("/api/x")`, web 분기는 일반 URI. response.getContentAsString()/getStatus() 로 HTML 분기 단언.
- **5-2 enum-mismatch 핸들러 제외** ✅: 생성 비용 대비 가치 낮고 위임 대상이 이미 테스트됨.
- **5-3 BindException 으로 validation 커버** ✅: MethodArgumentNotValidException 보다 생성 용이(BeanPropertyBindingResult + reject).

---

## 6. 영향 범위 / 리스크

| 계층 | 파일 | 유형 |
|------|------|------|
| Test(신규) | `exception/GlobalExceptionHandlerTest`·`exception/ErrorCodeTest` | 신규 |
| Docs | 본 기획서 + 개발계획서 | 신규 |

프로덕션/DB/API/UI 변경 0. UI 키워드 0(`getErrorHtml` 의 인라인 CSS 는 테스트 대상 코드 내부일 뿐 UI 변경 아님) → 디자인팀 skip.

| 리스크 | 수준 | 완화 |
|---|---|---|
| 일부 예외 생성자 시그니처 차이 | 낮음 | 컴파일/실행 게이트, 생성 어려운 2핸들러는 제외 |
| ApiResponse 본문 게터 접근 | 낮음 | status code 위주 + 접근 가능한 필드만 단언 |

---

## 7. 승인 요청

본 기획서에 대한 codex 검토 및 사용자 최종승인을 요청합니다.
