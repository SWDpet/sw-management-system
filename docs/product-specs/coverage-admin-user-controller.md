# [기획서] AdminUserController 커버리지 보강 (B)

- **작성팀**: 기획팀
- **작성일**: 2026-06-27
- **트랙**: beyond-A 커버리지 증분 B. 컨트롤러 단위테스트(DB·MockMvc 불요, Mockito + SecurityContext).
- **상태**: ✅ **구현 완료(2026-06-27)** — AdminUserController 1.2→100% LINE. 상세는 개발계획서 참조.
- **이전**: v2 (codex NEEDS-FIX 4건 반영 — 사용자 최종승인)
- **codex 반영(2026-06-27)**: ① "6 엔드포인트"→"5 핸들러+2헬퍼" 정정 ② actor=unknown 은 getPrincipal 순차반환 인위 branch 케이스로 명시 ③ Page mock→PageImpl + ArgumentCaptor<Pageable> ④ expandCsv exact assert→set 내용+non-blank 완화

---

## 1. 배경 / 목표

`AdminUserController`(363줄, **LINE 1.2% / miss 163 / 기존 테스트 0**)는 관리자 회원 관리(목록·검색·승인·수정·민감정보 조회·삭제) 컨트롤러다. 의존성이 전부 mock 가능(`UserRepository`·`LogService`·`MessageResolver`)하고 인증은 `SecurityContextHolder` 직접 설정으로 재현 가능 → **순수 단위테스트로 1.2%→90%+** 달성이 가능하다. 163줄은 비-영구패스 클래스 중 **단일 최대 커버리지 구멍**이라 전역 LINE/INSTRUCTION floor에 기여가 크다.

- **동작 변경 0 (test-only)**. production 코드는 건드리지 않는다(가드 완화 금지 — [[feedback_all_pages_require_permission]]).
- 검증된 패턴 재사용: `InspectReportControllerTest`(순수 단위 + `SecurityContextHolder` + `ExtendedModelMap` + `@AfterEach` clearContext). 단 본 컨트롤러는 `@Autowired` **필드 주입**이라 `ReflectionTestUtils.setField`로 mock 주입.

### 미커버 표면(핸들러 5 + 헬퍼 2 + getSearchTypeName)

| # | 시그니처 | 매핑 |
|---|---|---|
| 1 | `userManagement(page,searchType,keyword,expand,Model)` | `GET /admin/users` |
| 2 | `approveUser(userSeq, auth* 10종)` | `POST /admin/users/approve` |
| 3 | `updateUser(userSeq, 필드 11 + auth 10 + expand,page)` | `POST /admin/users/update` |
| 4 | `getSensitiveField(userSeq, field)` | `GET /admin/users/api/{userSeq}/sensitive` (@ResponseBody) |
| 5 | `deleteUser(userSeq)` | `POST /admin/users/delete` |
| H1 | `getCurrentUser()` / `checkAdminAuth()` | private 헬퍼 |
| H2 | `getSearchTypeName(searchType)` | private (8 case) |

---

## 2. 변경 설계 — AdminUserControllerTest 신설

`src/test/java/com/swmanager/system/controller/AdminUserControllerTest.java`. `@BeforeEach`에서 mock 3종(`UserRepository`·`LogService`·`MessageResolver`) 생성 후 `ReflectionTestUtils.setField`로 주입. 인증은 헬퍼로 `SecurityContextHolder`에 `UsernamePasswordAuthenticationToken(CustomUserDetails, ...)` 설정. **`@AfterEach SecurityContextHolder.clearContext()` 필수**(static 상태 누수 차단).

### A. 권한 가드 (checkAdminAuth / getCurrentUser) — 분기 정밀
- **미로그인**: `SecurityContext` 비움 → `userManagement` 호출 시 `InsufficientPermissionException`("로그인 필요" 메시지). (auth==null 분기)
- **principal 비-CustomUserDetails**: principal=`"anonymous"` 문자열 → getCurrentUser null → 예외. (`instanceof` false 분기)
- **비-ADMIN**: `CustomUserDetails`의 `userRole`="ROLE_USER" → 예외("관리자 권한 필요"). (role 불일치 분기)
- **getPrincipal 예외**: mock auth.getPrincipal()이 throw → getCurrentUser catch→null→예외. (try/catch 분기)
- **ADMIN 통과**: role="ROLE_ADMIN" → 가드 통과(이하 모든 정상경로의 전제).

> ⚠ 본 가드는 production 동작이므로 테스트는 **현 동작을 박제**할 뿐 가드를 약화시키지 않는다.

### B. userManagement (검색 switch 8 + expand 파싱 + model)
- **무검색 기본 조회**: keyword=null → `findByEnabledTrue(pageable)` 경로. 반환은 `new PageImpl<>(List.of(user), pageable, total)` 사용(mock Page 회피 — codex). view=="admin-user-list" 단언 + model attr(`pendingUsers`/`activeUsers`/`activeUserPage`/`currentPage`/`totalPages`/`totalElements`/`expandIds`/`expandCsv`) 존재 단언. `Pageable`은 `ArgumentCaptor<Pageable>`로 page=0/size=10/sort=regDt desc 확인.
- **검색 8 case**: searchType ∈ {userid, username, orgNm, deptNm, teamNm, tel, email, (default=알수없는값)} 각각 → 해당 `searchBy*` 호출 `verify`(default는 `findByEnabledTrue` 폴백) + `searchInfo`/`searchType`/`keyword` model attr. 반환은 `PageImpl`.
- **getSearchTypeName 8 case**: 위 검색 케이스의 `searchInfo` 접두(아이디/성명/소속기관/부서/팀/연락처/이메일/전체)로 간접 검증.
- **expand 파싱**: (a) "1,2,3" → `expandIds`가 {1,2,3} **set 내용** 포함·`expandCsv` non-blank (b) "" / null → 빈 set·`expandCsv`="" (c) "1,x,3" → NumberFormat 무시되어 {1,3}. ⚠`expandCsv`는 `HashSet` 순서 비계약 → **exact string 단언 금지**, set 내용+non-blank로만 검증(codex).

### C. approveUser
- **정상**: user found(mock) → auth 10필드 + `enabled=true` set 확인 + `save` verify + `logService.log(USER, APPROVE, ...)` verify + return "redirect:/admin/users".
- **not found**: `findById` empty → `IllegalArgumentException`(`messages.get("error.user.not_found", ...)` mock 스텁).
- auth 선택 파라미터 기본값(NONE)은 컨트롤러 시그니처 책임이라 단위호출에선 명시 전달.

### D. updateUser (redirect 쿼리 조립 분기)
- **정상**: 필드 set + `save` + `log(USER, UPDATE, ...)` verify.
- **redirect 조립 4분기**: (page=null,expand=null)→`/admin/users`, (page=2,expand=null)→`?page=2`, (page=null,expand="3,4")→`?expand=3,4`, (page=2,expand="3,x,4")→`?page=2&expand=3,4`(NumberFormat 무시).
- **not found**: 예외.

### E. getSensitiveField (@ResponseBody ResponseEntity)
- **필드 5종**(ssn/tel/mobile/email/address): user found → 200 + body=`AdminSensitiveFieldRow(field, value)` + `log(USER, SENSITIVE_VIEW, ...)` verify. value==null이면 "" 치환 분기 1건 포함.
- **user 없음**: `findById` empty → 404 + Map(success=false, NOT_FOUND).
- **허용 외 필드**: field="password" → 400 + Map(INVALID_FIELD). (단, 권한 가드는 통과 후이므로 ADMIN 컨텍스트)
- **actor=unknown 분기 (인위적 branch-coverage 케이스)**: `getSensitiveField`는 `checkAdminAuth()`를 먼저 통과해야 하므로 일반 SecurityContext 조작만으론 `getCurrentUser()==null`에 도달 못 한다. → mock `Authentication.getPrincipal()`을 **순차 반환**으로 구성(1차=ADMIN `CustomUserDetails`로 가드 통과, 2차=null/비정상 principal)해 actor="unknown" 로그 분기 도달. 본 케이스는 정상 인증 흐름이 아닌 **분기 커버 전용 인위 케이스**임을 테스트 주석에 명시(codex).

### F. deleteUser
- **user found**: userInfo="userid(username)" → `deleteById` verify + `log(USER, DELETE, ...)`.
- **user null**: userInfo="Unknown" 분기 → `deleteById` 여전히 호출 + log.

---

## 3. 기능 / 비기능 요건

| ID | 내용 |
|----|------|
| FR-1 | 5 핸들러 + 2 헬퍼 + getSearchTypeName의 정상/예외/분기를 단위테스트로 박제. |
| FR-2 | 권한 가드 4분기(미로그인·비CustomUserDetails·비ADMIN·예외) 동작 확인 — 가드 **약화 0**. |
| NFR-1 | `./mvnw -o clean verify` BUILD SUCCESS (DB 불필요). |
| NFR-2 | AdminUserControllerTest 신설, AdminUserController **1.2 → 90%+ LINE**. |
| NFR-3 | floor 실측 후 상향(전역 영향 작으면 생략). Map/거대클래스 ratchet·PIT 게이트 불변. |
| NFR-4 | production 코드 변경 0. dual-review → 듀얼푸시. |

---

## 4. 영향 / 리스크

| 파일 | 유형 |
|------|------|
| `controller/AdminUserControllerTest.java` | 신규 단위테스트 |

production 변경 0.

| 리스크 | 수준 | 완화 |
|---|---|---|
| `SecurityContextHolder` static 상태 누수 → 타 테스트 오염 | 중 | `@AfterEach clearContext()` 필수. |
| 필드 `@Autowired` 주입 → 생성자 없음 | 낮음 | `ReflectionTestUtils.setField`로 3 mock 주입(검증된 패턴). |
| `Page`/`Pageable` mock brittle | 낮음 | mock 대신 `PageImpl` 사용 + `ArgumentCaptor<Pageable>`로 page/size/sort 단언(codex). `expandCsv` exact string 비단언. |
| 가드 테스트가 실수로 약화 검증 | 낮음 | 가드는 "현 동작 박제"만; production 무변경. |

---

## 5. 승인 요청

본 기획서(AdminUserController 커버리지)에 대한 codex 검토 및 사용자 최종승인을 요청합니다. 특히
1. **테스트 전략**(순수 단위 Mockito vs MockMvc+Security) — 순수 단위가 floor 기여/속도/안정성에서 적합한가,
2. **getSensitiveField actor=unknown 분기** 도달 가능성(가드 통과 후 getCurrentUser가 null 될 수 있는가 — SecurityContext 조작으로 재현 가능한가),
3. **분기 누락** 여부(검토 의견 요청).
