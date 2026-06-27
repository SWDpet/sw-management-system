# [기획서] CustomAuthenticationFailureHandler 커버리지 보강 (B)

- **작성팀**: 기획팀 / **작성일**: 2026-06-28
- **트랙**: beyond-A 커버리지 증분 B. 보안 컴포넌트(로그인 실패 핸들러) 순수 단위테스트. 신규 테스트 클래스.
- **상태**: ✅ 구현 완료(2026-06-28). CustomAuthenticationFailureHandlerTest 6개 신설 → 클래스 **10→100% LINE**(20/20)·INSTR 100%·BRANCH 10/10. 전역 LINE 77.09→77.24%·INSTR 63.07→63.16%, floor 유지. `mvnw -o clean verify` 1400 green. codex 기획 APPROVE·개발계획 APPROVE-WITH-FIX·구현 PASS. dual-review(codex0/Opus5) 합의2 중 verifyNoMoreInteractions 반영(정확 URL 단언은 S-tier 특성화로 유지), 분쟁3 refute.

---

## 1. 배경 / 목표

`CustomAuthenticationFailureHandler`(63줄, **LINE 10% / miss 18**, 테스트 0)는 Spring Security 로그인 실패 시 호출되어 **계정 잠금 정책에 따른 리다이렉트 분기**를 결정하는 보안 컴포넌트다. `SimpleUrlAuthenticationFailureHandler`를 상속하고 `LoginAttemptService` 1개만 의존하며, `onAuthenticationFailure(req, res, ex)` 메서드의 모든 분기가 미커버 상태다.

보안 회귀(잠금 우회·잘못된 리다이렉트)를 방어하기 위해, 분기별 리다이렉트 URL을 박제하는 순수 단위테스트를 추가한다. 운영 DB·MockMvc 불필요(POJO + `MockHttpServletRequest/Response`).

## 2. 대상 분기 (onAuthenticationFailure)

| # | 조건 | 기대 결과 |
|---|---|---|
| B1 | `userid` 파라미터 null | `/login?error=true` (서비스 미호출) |
| B2 | `userid` blank(공백) | `/login?error=true` (서비스 미호출) |
| B3 | userid 존재, `findUser` empty | `/login?error=true` (isLocked 미호출) |
| B4 | user 존재, `isLocked`=true | `/login?error=true&locked=true&minutes={remaining}` + return (loginFailed 미호출) |
| B5 | user 존재, isLocked=false, `loginFailed`=true(신규 잠금) | `/login?error=true&locked=true&minutes={remaining}` + return |
| B6 | user 존재, isLocked=false, loginFailed=false | `/login?error=true` (실패카운트만 증가) |

## 3. 변경 — 신규 `CustomAuthenticationFailureHandlerTest` (테스트만)

- `new CustomAuthenticationFailureHandler(mockLoginAttemptService)` 직접 생성(생성자 주입, reflection 불요).
- `MockHttpServletRequest`(`setParameter("userid", …)`) + `MockHttpServletResponse`. `AuthenticationException`은 `mock` 또는 `BadCredentialsException` 인스턴스.
- 상속한 `getRedirectStrategy()`의 기본 `DefaultRedirectStrategy`가 `response.sendRedirect()` 호출 → **`response.getRedirectedUrl()` 단언**(setRedirectStrategy 불요).
- 6 테스트(B1~B6): 각 리다이렉트 URL exact 단언 + 서비스 호출/미호출 `verify`(B1/B2 findUser never, B3 isLocked never, B4 loginFailed never).
- 잠금 분기(B4/B5)는 `getRemainingLockMinutes` stub 값(예: 15)이 URL `minutes=15`로 전파되는지 단언.

## 4. 요건
- **FR**: 위 6 분기 박제(리다이렉트 URL + 서비스 호출 검증). 잠금 우회 회귀 방어.
- **NFR**: production 변경 0, `mvnw -o clean verify` SUCCESS, CustomAuthenticationFailureHandler 10→~95% LINE(잔여=상속 boilerplate 한정), JaCoCo floor 유지(게인 작아 버퍼 흡수 예상), 구현 후 codex PASS → 듀얼푸시.

## 5. 영향/리스크
- 신규 `config/CustomAuthenticationFailureHandlerTest.java` 1파일. production 0.
- 리스크: `DefaultRedirectStrategy`가 context-relative redirect를 쓰면 URL 앞에 contextPath가 붙을 수 있으나, `MockHttpServletRequest` 기본 contextPath="" 라 `/login?...` 그대로. 만약 차이 시 `getRedirectedUrl()` 대신 `startsWith`/`contains` 단언으로 조정.
