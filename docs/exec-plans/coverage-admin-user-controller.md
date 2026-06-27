# 개발계획서 — AdminUserController 커버리지 보강 (B)

- **상태**: ✅ **구현 완료(2026-06-27)**. AdminUserControllerTest 19개 신설 → AdminUserController **1.2→100% LINE**(165/165)·BRANCH 90.3%. 전역 LINE 73.29→**74.65%**/INSTRUCTION 59.67→**60.84%**. floor 상향 LINE 0.71→0.73·INSTR 0.57→0.59. `mvnw -o clean verify` BUILD SUCCESS(1353 tests). 기획서 codex NEEDS-FIX 4건+개발계획 PASS+구현 PASS. 듀얼푸시 대기(사용자 "작업완료").
- **작성일**: 2026-06-27
- **기획서**: `docs/product-specs/coverage-admin-user-controller.md` (v2, codex NEEDS-FIX 4건 반영·사용자 승인)
- **안전망**: 현 green 스위트(`./mvnw -o clean verify`) + JaCoCo ratchet/PIT 게이트. **production 변경 0 (test-only)** → 회귀 위험 원천 없음.
- **원칙**: 단일 신규 파일(`AdminUserControllerTest.java`). 검증된 `InspectReportControllerTest` 패턴 재사용. 커밋 1개(원자).

---

## Step 0 — baseline 고정

**0-1.** 현 커버리지 실측: `./mvnw -o clean verify` BUILD SUCCESS 확인 + `target/site/jacoco/com.swmanager.system.controller/AdminUserController.html` 의 LINE% 기록(기대 ~1.2%). 전역 LINE/INSTRUCTION 도 기록(pom floor 0.71/0.57 대비 버퍼).
**0-2.** 소스 재확인: 5 핸들러 시그니처 + `getCurrentUser`/`checkAdminAuth`/`getSearchTypeName` 분기 + `UserRepository` 검색 8종(`(keyword,Pageable)→Page<User>`, default 폴백=`findByEnabledTrue`) + `AdminSensitiveFieldRow(field,value)` 레코드.

**검증:** 없음(준비).

---

## Step 1 — AdminUserControllerTest 골격 + 권한 가드 (그룹 A)

**1-1.** 신규 `src/test/java/com/swmanager/system/controller/AdminUserControllerTest.java`:
```java
class AdminUserControllerTest {
    private AdminUserController controller;
    private UserRepository userRepository;
    private LogService logService;
    private MessageResolver messages;

    @BeforeEach void setUp() {
        controller = new AdminUserController();
        userRepository = mock(UserRepository.class);
        logService = mock(LogService.class);
        messages = mock(MessageResolver.class);
        ReflectionTestUtils.setField(controller, "userRepository", userRepository);
        ReflectionTestUtils.setField(controller, "logService", logService);
        ReflectionTestUtils.setField(controller, "messages", messages);
    }
    @AfterEach void tearDown() { SecurityContextHolder.clearContext(); }
    // 헬퍼: authenticateAs(role) → SecurityContext 에 CustomUserDetails principal 설정
}
```
- 인증 헬퍼: `User`(userRole, userid, username) → `CustomUserDetails` → `UsernamePasswordAuthenticationToken` → `SecurityContextHolder.getContext().setAuthentication(...)`.

**1-2. 그룹 A (가드 4분기):**
- 미로그인(context 비움) → `userManagement(...)` 호출 시 `InsufficientPermissionException` (`assertThatThrownBy`).
- principal=비-CustomUserDetails(문자열) → 예외.
- role="ROLE_USER" → 예외.
- `Authentication` mock 의 `getPrincipal()` throw → getCurrentUser catch→null→예외.
- role="ROLE_ADMIN" → 가드 통과(이하 정상경로 전제, 별도 단언은 그룹 B 에서).

**검증:** `./mvnw -o -Dtest=AdminUserControllerTest test` green(부분).

## Step 2 — userManagement (그룹 B)

**2-1.** ADMIN 컨텍스트 전제. `userRepository.findByEnabledFalse()` → `List.of()`, 검색/기본 조회는 **`new PageImpl<>(List.of(user), PageRequest.of(0,10,Sort.by("regDt").descending()), 1)`** 반환(mock Page 회피).
**2-2.**
- 무검색: keyword=null → `findByEnabledTrue(pageable)` verify + view=="admin-user-list" + model attr 8종 존재.
- `ArgumentCaptor<Pageable>` 로 page=0/size=10/sort=regDt DESC 단언.
- 검색 8 case(userid…email + default): 각 `searchBy*` verify(default→`findByEnabledTrue`) + `searchInfo` 접두로 `getSearchTypeName` 간접 검증 + `searchType`/`keyword` attr.
- expand: "1,2,3"→`expandIds` {1,2,3} 포함·`expandCsv` non-blank / ""·null→빈 set·`expandCsv`="" / "1,x,3"→{1,3}. **exact string 단언 금지**(HashSet 순서 비계약).

**검증:** 부분 green.

## Step 3 — approve / update / delete / getSensitiveField (그룹 C·D·E·F)

**3-1. approve(C):** found → auth 10필드+`enabled=true` set + `save` verify + `logService.log(USER, APPROVE, …)` verify + return "redirect:/admin/users". / not found → `messages.get("error.user.not_found", …)` 스텁 + `IllegalArgumentException`.
**3-2. update(D):** found → 필드 set + `save` + `log(USER, UPDATE, …)`. redirect 4분기: (null,null)→`/admin/users` / (2,null)→`?page=2` / (null,"3,4")→`?expand=3,4` / (2,"3,x,4")→`?page=2&expand=3,4`. / not found → 예외.
**3-3. delete(F):** found → userInfo="userid(username)" + `deleteById` verify + `log(USER, DELETE, …)` / null → userInfo="Unknown" 분기 + `deleteById` 여전히 호출.
**3-4. getSensitiveField(E):** 필드 5종(ssn/tel/mobile/email/address) found → 200 + body=`AdminSensitiveFieldRow` + `log(USER, SENSITIVE_VIEW, …)`(value==null→"" 분기 1건 포함). / user 없음 → 404 Map(NOT_FOUND). / field="password" → 400 Map(INVALID_FIELD). / **actor=unknown**: `Authentication.getPrincipal()` 순차반환(1차 ADMIN→2차 null)로 가드 통과 후 actor="unknown" 분기 도달 — **인위적 branch-coverage 케이스**임을 주석 명시.

**검증:** `./mvnw -o -Dtest=AdminUserControllerTest test` 전체 green.

## Step 4 — 전체 검증 + floor 상향 검토

- `./mvnw -o clean verify` BUILD SUCCESS(전체 스위트 green, 1334+ → +N).
- `AdminUserController` LINE 1.2 → **90%+** 확인(JaCoCo html).
- 전역 LINE/INSTRUCTION 재측정 → 버퍼 충분하면 floor 상향(NFR-3, 전역 영향 작으면 생략). Map/거대클래스 ratchet·PIT 게이트 불변 확인.

## Step 5 — codex 검증 + dual-review + 커밋 + 듀얼푸시

- codex 검증(분기 누락·brittle·가드 약화 0·골든 의미).
- dual-review(codex+Opus) → 수렴.
- 커밋: `test(coverage): AdminUserController 단위테스트 신설 — 가드/검색/CRUD/민감조회 (beyond-A)`.
- `git push origin master` (듀얼).

---

## 검증 매트릭스 (FR/NFR → Step)

| 항목 | 검증 Step |
|---|---|
| FR-1 5핸들러+2헬퍼+getSearchTypeName 박제 | Step 1·2·3 |
| FR-2 가드 4분기·약화 0 | Step 1 (+production 무변경) |
| NFR-1 verify SUCCESS | Step 4 |
| NFR-2 1.2→90%+ | Step 4 |
| NFR-3 floor/ratchet/PIT | Step 4 |
| NFR-4 변경 0·듀얼푸시 | Step 5 |

## 롤백

- 단일 테스트 파일 신규 → 문제 시 파일 삭제 또는 `git revert`. production 무영향.
