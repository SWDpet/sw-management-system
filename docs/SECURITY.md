# SECURITY.md — 보안 정책·구성

> ⚠ **자동 생성 초안 — 검증 필요**
> 근거: `SecurityConfig.java` + `application.properties` + 감사 P1-1 조치 (2026-04-03)

---

## 1. 인증 (Authentication)

- **프레임워크**: Spring Security 6 (spring-boot-starter-security)
- **UserDetails**: `CustomUserDetails` (`getUsername()` = userid, `getUser().getUsername()` = 실명)
- **비밀번호**: BCrypt (`BCryptPasswordEncoder`)
- **로그인 폼**: `login.html` + `/login` POST
- **로그인 잠금**: `SecurityLoginProperties` (실패 임계치)

---

## 2. 권한 (Authorization)

### AuthLevel 체계
- `NONE` — 권한 없음
- `VIEW` — 조회만
- `EDIT` — 조회·수정·등록·삭제

### 주요 체크 포인트
- `CustomUserDetails.getUser().getAuthProject()` — 사업 권한
- `CustomUserDetails.getUser().getAuthPerson()` — 담당자 권한
- `CustomUserDetails.getUser().getAuthDashboard()` — 대시보드 권한
- `CustomUserDetails.getUser().getUserRole()` — `ROLE_ADMIN` / `ROLE_USER`

### 민감 정보 접근
- `AdminUserController.getSensitiveField()` — 관리자만 민감정보(SSN/전화 등) 조회. `ACTION_TYPE=SENSITIVE_VIEW` 로그 남김 (S9)

---

## 3. CSRF

- Spring Security 기본 CSRF 토큰 활성
- Thymeleaf: `<meta name="_csrf" content="...">` + `<meta name="_csrf_header" content="...">`
- fetch 요청 시 헤더 수동 부착 패턴 (`admin-user.js`, `project-form.html` 등)

---

## 4. 민감 데이터 마스킹

- `SensitiveMask.java` + `MaskingDetector.java` (S3B 완료)
- 대상: 주민번호(ssn), 전화번호(tel), 이메일 (마이페이지 제외 — 본인 조회)
- `MyPageController` Guard 테스트: `MyPageControllerGuardTest` 커버

자세한 정책: `docs/DESIGN.md` §마스킹 / `docs/exec-plans/users-masking-regression-fix.md`.

---

## 5. 감사 대응 이력 (P1)

### 2026-04-03 감사 조치 (P1 4건)
1. **DB 비밀번호** 소스코드 하드코딩 제거 → 환경변수 `DB_PASSWORD`
2. **권한 검사** 누락 엔드포인트 보강
3. **민감 필드** 응답에서 마스킹 기본값
4. **inspect_report 스키마 정합성** 보정

---

## 6. 접근 로그 (Audit Log)

- 테이블: `access_logs` (S5 정제 완료, S9 Enum 리팩터 완료)
- 기록: `LogService.log(MenuName.XXX, AccessActionType.YYY, detail)`
- Orphan Guard: 유효하지 않은 userid → WARN + `anonymousUser` fallback
- 시스템 화이트리스트: `anonymousUser`, `system`, `scheduler`

---

## 7. 쿼리 보안

- **Prepared Statement**: JPA 기본 바인딩 (SQL 인젝션 방지)
- **동적 쿼리**: `@Query` + `@Param` 명시
- **마이그 SQL**: 트랜잭션 + Exit Gate + 백업 테이블 패턴 (S5/S1/S10 확립)

---

## 8. 정적 파일 보안

- `static/` 리소스는 Spring 기본 서빙 — 인증 없이 접근 가능 (css/js/img)
- 비밀 파일(예: `.env`, credential) 커밋 금지 원칙 (`AGENTS.md §5`)

---

*Last updated: 2026-04-24 · docs-renewal-01 P1*
