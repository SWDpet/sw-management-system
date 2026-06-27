# 개발계획서 — CustomAuthenticationFailureHandler 커버리지 보강 (B)

- **상태**: ✅ 구현 완료(2026-06-28). 6테스트 green, 클래스 10→100% LINE/BRANCH. 구현 후 codex PASS + dual-review 합의(verifyNoMoreInteractions+findUser/isLocked 명시 verify) 반영. 정확 URL 단언은 하드코딩 문자열 특성화로 유지(분쟁/합의-1 미적용 근거). `mvnw -o clean verify` 1400 green.
- **작성일**: 2026-06-28
- **기획서**: `docs/product-specs/coverage-auth-failure-handler.md` (codex APPROVE)
- **codex 보완(개발계획)**: B5/B6 `loginFailed(user)` 호출 verify 추가 · B3 `never().loginFailed(any())` 추가 · B5 `isLocked(user)` 호출 verify · 테스트 격리(@BeforeEach 매 테스트 fresh mock — JUnit 인스턴스/메서드별 재생성으로 충족).
- **안전망**: 현 green 스위트(`./mvnw -o clean verify`) + JaCoCo ratchet/PIT 게이트. **production 변경 0 (test-only)** → 회귀 위험 원천 없음.
- **원칙**: 단일 신규 파일(`CustomAuthenticationFailureHandlerTest.java`). 생성자 주입이라 reflection 불요(`new` 직접). 커밋 1개(원자).

---

## Step 0 — baseline 고정

**0-1.** 현 커버리지: 직전 측정 전역 LINE 77.09%·INSTR 63.07%(pom floor LINE 0.74/INSTR 0.60 대비 버퍼 ~2.5/~3.0pp). 대상 `CustomAuthenticationFailureHandler` LINE 10%(miss 18).
**0-2.** 소스 재확인: `onAuthenticationFailure` 6 분기(기획서 B1~B6) + 의존 `LoginAttemptService` 4 메서드(findUser/isLocked/getRemainingLockMinutes/loginFailed). 리다이렉트는 상속 `getRedirectStrategy()`(기본 DefaultRedirectStrategy → `response.sendRedirect`).

**검증:** 없음(준비).

---

## Step 1 — 테스트 골격 + B1~B3 (서비스 미진입/미존재)

**1-1.** 신규 `src/test/java/com/swmanager/system/config/CustomAuthenticationFailureHandlerTest.java`:
```java
class CustomAuthenticationFailureHandlerTest {
    private LoginAttemptService loginAttemptService;
    private CustomAuthenticationFailureHandler handler;
    private final AuthenticationException ex = new BadCredentialsException("bad");

    @BeforeEach void setUp() {
        loginAttemptService = mock(LoginAttemptService.class);
        handler = new CustomAuthenticationFailureHandler(loginAttemptService);
    }

    // 헬퍼: 주어진 userid 파라미터로 onAuthenticationFailure 실행 후 redirectedUrl 반환
    private String invoke(String userid) throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        if (userid != null) req.setParameter("userid", userid);
        MockHttpServletResponse res = new MockHttpServletResponse();
        handler.onAuthenticationFailure(req, res, ex);
        return res.getRedirectedUrl();
    }
}
```

**1-2.**
- **B1** `useridNull_redirectsPlainError`: `invoke(null)` → `"/login?error=true"`. `verify(loginAttemptService, never()).findUser(any())`.
- **B2** `useridBlank_redirectsPlainError`: `invoke("   ")` → `"/login?error=true"`. `verify … never().findUser(any())`.
- **B3** `userNotFound_redirectsPlainError`: `when(findUser("alice")).thenReturn(Optional.empty())` → `invoke("alice")` == `"/login?error=true"`. `verify … never().isLocked(any())` + `never().loginFailed(any())`(codex 보완).

**검증:** `./mvnw -o -Dtest=CustomAuthenticationFailureHandlerTest test` green(부분).

## Step 2 — B4~B6 (잠금/실패 분기)

공통: `User user = new User()` (또는 mock) + `when(findUser("alice")).thenReturn(Optional.of(user))`.

- **B4** `alreadyLocked_redirectsLockedWithMinutes`: `when(isLocked(user)).thenReturn(true)`; `when(getRemainingLockMinutes(user)).thenReturn(15L)` → `invoke("alice")` == `"/login?error=true&locked=true&minutes=15"`. `verify(loginAttemptService, never()).loginFailed(any())`.
- **B5** `failureCausesLock_redirectsLocked`: `isLocked=false`, `when(loginFailed(user)).thenReturn(true)`, `getRemainingLockMinutes=15L` → == `"/login?error=true&locked=true&minutes=15"`. verify: `isLocked(user)` + `loginFailed(user)` + `getRemainingLockMinutes(user)` 모두 호출(codex 보완 — 사전잠금 vs 실패후잠금 경로 구별).
- **B6** `failureNoLock_redirectsPlainError`: `isLocked=false`, `loginFailed=false` → == `"/login?error=true"`. verify: `loginFailed(user)` 호출(codex 보완) + `never().getRemainingLockMinutes(any())`(잠금 아님 → 분 계산 미수행).

**검증:** `./mvnw -o -Dtest=CustomAuthenticationFailureHandlerTest test` 전체 green(6).

## Step 3 — 전체 검증 + floor 검토

- `./mvnw -o clean verify` BUILD SUCCESS(전체 스위트 green, 1394 → +6).
- `CustomAuthenticationFailureHandler` LINE 10 → **~95%** 확인(JaCoCo html, 잔여=상속 boilerplate 한정).
- 전역 LINE/INSTR 재측정. 게인 작아(18줄) 버퍼 흡수 예상 → **floor 유지**. Map/거대클래스 ratchet·PIT 게이트 불변 확인.

## Step 4 — codex 검증 + dual-review + 커밋 + 듀얼푸시

- codex 검증(분기 누락·brittle·redirect 단언 유효성).
- dual-review(codex+Opus) → 수렴.
- 커밋: `test(coverage): CustomAuthenticationFailureHandler 단위테스트 신설 — 로그인 실패/계정잠금 리다이렉트 6분기 (beyond-A)`.
- `git push origin master` (듀얼).

---

## 검증 매트릭스 (FR/NFR → Step)

| 항목 | 검증 Step |
|---|---|
| FR 6분기(B1~B6) 박제 | Step 1·2 |
| FR 잠금 우회 회귀 방어(verify never) | Step 1·2 |
| NFR verify SUCCESS | Step 3 |
| NFR 10→~95% | Step 3 |
| NFR floor/ratchet/PIT 불변 | Step 3 |
| NFR production 0·듀얼푸시 | Step 4 |

## 롤백

- 단일 테스트 파일 신규 → 문제 시 파일 삭제 또는 `git revert`. production 무영향.
