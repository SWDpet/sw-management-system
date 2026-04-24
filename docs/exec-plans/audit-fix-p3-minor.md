# [개발계획서] 감사 후속조치 스프린트 3 — P3 경미 정리 3건

- **작성팀**: 개발팀
- **작성일**: 2026-04-19
- **근거 기획서**: [docs/product-specs/audit-fix-p3-minor.md](../plans/audit-fix-p3-minor.md) (v1, 승인)
- **상태**: 초안 (codex 검토 대기)

---

## 1. 작업 순서 (Stepwise)

### Step 1 — 사전 스캔
1. `rg -n "/login/type" src/ src/main/resources/` → `LoginController.java:37` 외 0건.
2. `rg -n "SwRepository" src/` → `SwRepository.java` 1건 외 0건.
3. `rg -n "log\.(info|debug|warn|error).*keyword" src/main/java/com/swmanager/system/controller/AdminUserController.java` → 2 hit (line 101, 116) 확인.
4. `rg -n "/login/type" src/main/resources/` (템플릿·SecurityConfig) → 0건.
5. `rg -n "login/type|antMatchers.*login" src/main/java/com/swmanager/system/config/` → SecurityConfig 특수 처리 0건 확인.

### Step 2 — 4-4 `LoginController.loginModePage` 제거
2-1. `LoginController.java` 에서 `loginModePage` 메서드 전체(37~41) 삭제.
2-2. 남는 쓰임 없는 import 제거 (`PathVariable`).
2-3. `mvn compile` → BUILD SUCCESS.

### Step 3 — 4-5 `SwRepository` 파일 삭제
3-1. `rm src/main/java/com/swmanager/system/repository/SwRepository.java`.
3-2. `mvn compile` → BUILD SUCCESS.

### Step 4 — 5-4 `AdminUserController` 키워드 로그 원문 제거
4-1. line 101:
```java
// Before
log.info("Page: {}, 검색 타입: {}, 키워드: {}", page, searchType, keyword);
// After
log.info("Page: {}, 검색 타입: {}, 키워드 길이: {}", page, searchType,
        keyword != null ? keyword.length() : 0);
```
4-2. line 116:
```java
// Before
log.info("검색 수행 - 타입: {}, 키워드: {}", searchType, keyword);
// After
log.debug("검색 수행 - 타입: {}, 키워드 길이: {}", searchType,
        keyword != null ? keyword.length() : 0);
```
4-3. `rg -n "log\.[a-z]+\([^)]*keyword" src/main/java/com/swmanager/system/controller/AdminUserController.java` → 0 hits 확인.

### Step 5 — 감사 보고서 체크박스
- `docs/generated/audit/2026-04-18-system-audit.md` 의 4-4, 4-5, 5-4 에 ☑ 조치함 + 요약 한 줄씩.

### Step 6 — 빌드 / 재기동 / 런타임 검증
6-1. `./mvnw -q compile` → BUILD SUCCESS.
6-2. `bash server-restart.sh` → 성공 + `Started SwmanagerApplication`.
6-3. 런타임 확인:
- `curl -s -o /dev/null -w "%{http_code}" http://localhost:9090/login/type/user` → **404** (제거 확인).
- 로그인 후 `/admin/user?searchType=username&keyword=AUDIT_TEST_987xyz` 1회 호출 (또는 브라우저에서 직접). 302 인증 리다이렉트 → 실 계정 로그인은 사용자가 확인하거나 curl cookie 사용.
- `grep -c 'AUDIT_TEST_987xyz' server.log` → **0** (NFR-3 통과).
- `grep -c '키워드:' server.log` → 길이 기록만 남고 원문 없음 확인 (육안).

### Step 7 — codex Specs 검증

---

## 2. 테스트 (T#)

| ID | 내용 | 검증 방법 | Pass 기준 |
|----|------|----------|-----------|
| T1 | `loginModePage` 제거 | `rg -n "loginModePage|/login/type" src/main/java/` (rg 는 `|` 를 바로 OR 로 해석) | 0 hits |
| T2 | `PathVariable` import 제거 | `rg -n "import.*PathVariable" src/main/java/com/swmanager/system/controller/LoginController.java` | 0 hits |
| T3 | `SwRepository.java` 삭제 | `test ! -f src/main/java/com/swmanager/system/repository/SwRepository.java` | exit 0 |
| T4 | `SwRepository` 참조 0 | `rg -n "SwRepository" src/` | 0 hits |
| T5 | AdminUserController 키워드 원문 로그 제거 | `rg -n 'log\.[a-z]+\([^)]*keyword' src/main/java/com/swmanager/system/controller/AdminUserController.java` | 0 hits |
| T6 | AdminUserController 길이 기록 전환 확인 | `rg -n '키워드 길이' src/main/java/com/swmanager/system/controller/AdminUserController.java` | ≥ 2 hits |
| T7 | 제거된 URL 엔드포인트 부재 확정 | (a) 정적: `rg -n "/login/type" src/main/java/` 0 hit. (b) 동적: `curl -w "%{http_code}" http://localhost:9090/login/type/user` 응답이 존재하지 않는 임의 경로 응답과 **동일** (Spring Security 가 미인증 요청을 /login 으로 선리다이렉트 → 둘 다 302). **기준 변경 사유**: codex 검증 중 원 기준 "404" 는 Security filter chain 구조와 충돌함을 확인 (2026-04-19). | (a) 0 hit, (b) 302 == 302 |
| T8 | 런타임 키워드 미노출 (NFR-3) | 유니크 문자열로 검색 1회 수행 후 `grep -c 'AUDIT_TEST_987xyz' server.log` | 0 |
| T9 | Maven compile | `./mvnw -q compile` | BUILD SUCCESS |
| T10 | 서버 재기동 | `bash server-restart.sh` | `Started SwmanagerApplication` + ERROR 0 |
| T11 | 감사 체크박스 | 4-4/4-5/5-4 3건 ☑ 조치함 갱신 | 3 hits |

---

## 3. 롤백 전략

| 상황 | 조치 |
|------|------|
| Step 2/3 compile 실패 | Edit·rm 되돌림 후 재검토 (import/의존 재확인) |
| T7 이 404 아닌 500 반환 | 남은 `@PathVariable` import 또는 Security 매칭이 영향 — `LoginController`/`SecurityConfig` 재확인 |
| T8 런타임 검증이 환경 제약(로그인 불가)으로 어려움 | **T8 은 필수** — 대체 절차: (a) 기존 세션 쿠키를 curl `--cookie` 로 재사용해 GET `/admin/user?searchType=username&keyword=AUDIT_TEST_987xyz` 호출, (b) 또는 브라우저에서 실제 로그인 후 검색창에 해당 유니크 문자열 입력. 둘 다 불가할 경우 스프린트 완료 판정을 **'미검증/보류'** 로 남기고 증빙 확보 후 다시 결로 처리. 정적 T5 만으로 완료 판정 **금지**. |
| 배포 후 회귀 발견 | `git revert <sprint-3-commit>` → 재배포 |

---

## 4. 리스크·완화 재확인

| 리스크 | 수준 | 완화책 |
|--------|------|--------|
| keyword 길이 기록도 충분한 디버깅이 안 될 가능성 | 낮음 | 검색 실패는 사용자 재현/DevTools 로 확인. 로그는 보안이 우선 |
| 테스트 환경에서 로그인 후 검색 수행이 번거로움 | 낮음 | T8 은 1회 수동 검증. 추후 자동화 e2e 시 스크립트화 가능 |

---

## 5. 승인 요청

본 개발계획서에 대한 codex 검토 및 사용자 최종승인을 요청합니다.
