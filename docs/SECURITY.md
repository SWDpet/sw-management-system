# SECURITY.md — 보안 정책·구성

> ✅ **2026-06-19 코드 대조 검증 (S2 문서 drift 정정)** — §3 CSRF를 실제 `SecurityConfig` 설정(JSON API 면제 + SameSite=lax)과 일치하도록 갱신.
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

## 3. CSRF — 정식화 완료 (2026-06-29, `csrf-formalization`)

- **전 경로 CSRF 보호 활성.** 과거 JSON API 면제(`ignoringRequestMatchers` 6경로)는 **전면 제거**됨.
- **폼 요청**: Spring Security + Thymeleaf 가 `<form th:action ...>` 에 hidden `_csrf` 자동주입(로그인·견적 등 17개 폼).
- **fetch/AJAX 요청**: 공통 fragment `fragments/csrf.html` 의 **전역 `window.fetch` 인터셉터**가 동일출처 상태변경(POST/PUT/DELETE/PATCH)에 `X-CSRF-TOKEN` 헤더 자동주입.
  - 토큰은 `data-attribute`(`_csrf_meta`)로 노출(Thymeleaf escaping). 중복래핑 가드·robust URL 파싱·Request 객체 처리·기존 헤더 미덮어씀.
  - `top-nav`(54화면) + **standalone 페이지(`document-preview` 등)** 가 `~{fragments/csrf :: interceptor}` 공통 include.
  - ⚠ **신규 standalone 페이지(top-nav 미포함)에 상태변경 fetch 추가 시 이 fragment 를 직접 include 필수**(미포함 시 403).
  - ⚠ **fetch 전용 가정**: 코드베이스에 XHR/jQuery/axios 없음(검증). 비-fetch AJAX 도입 시 인터셉터 확장 필요.
- **다층 방어**: 서버측 권한 가드(`requireDocEdit`/`requireDocEditOrAdmin` 등) + 세션 쿠키 `SameSite=lax` 는 그대로 유지(CSRF 토큰과 병행).
- `actuator` 체인(Chain1)은 STATELESS+httpBasic 으로 `csrf.disable()` 유지(별개).

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

## 9. 의존성 취약점 스캔 — OWASP dependency-check (beyond-A, 2026-06-29)

의존성(라이브러리) 알려진 CVE 스캔. codex 자문대로 **PR 게이트가 아닌 주1회 scheduled + 수동** 워크플로로 분리(NVD 동기화 느림·외부 의존).

- **워크플로**: `.github/workflows/dependency-scan.yml` — `schedule`(매주 월 18:00 UTC) + `workflow_dispatch`(수동). PR/push 미차단. HTML/JSON 리포트 artifact 업로드(30일).
- **로컬 실행**: `./mvnw -Psecurity-scan org.owasp:dependency-check-maven:check` (pom `security-scan` 프로파일, 라이프사이클 미바인딩 → 기본 빌드 무영향).
- **실패 정책**: `failBuildOnCVSS=9` — critical(CVSS≥9) 발견 시 실패(보안 신호). NVD 동기화 실패와는 로그로 구분(취약점-발견 ≠ 인프라-실패).

### NVD API 키 설정 (필수 권장)
2023+ NVD 는 API 키 권장 — **미설정 시 첫 동기화가 심하게 rate-limit(수십분~수시간/실패 가능)**.
1. https://nvd.nist.gov/developers/request-an-api-key 에서 무료 키 발급.
2. GitHub 저장소 → Settings → Secrets and variables → Actions → New repository secret: 이름 `NVD_API_KEY`.
3. (로컬) 환경변수 `NVD_API_KEY` 설정 후 위 명령 실행.
- NVD DB 는 워크플로에서 `actions/cache`(`.dependency-check-data`)로 캐시 → 첫 동기화 후 재사용.

### 오탐 억제
- `dependency-check-suppressions.xml` — 첫 리포트 triage 후 false positive 추가(예: 내부 `GeoNURIS_License.jar` 오탐).
- ⚠ **검증 한계**: 본 스캔은 NVD 외부 의존 → PIT/CI/CSRF 와 달리 **첫 실제 실행 검증은 `NVD_API_KEY` secret 추가 후** 가능(스캐폴드까지 구축됨).

---

*Last updated: 2026-06-29 · §3 CSRF 정식화 + §9 의존성 스캔 추가 (beyond-A)*
