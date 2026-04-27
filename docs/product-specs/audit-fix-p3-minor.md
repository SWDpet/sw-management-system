# [기획서] 감사 후속조치 스프린트 3 — P3 경미 정리 3건

- **작성팀**: 기획팀
- **작성일**: 2026-04-19
- **선행**: `d73dd12` (스프린트 2c P2 보안·로깅)
- **상태**: ✅ 완료 (commit `a27032c`, 2026-04-19) — P3 경미 3건 모두 ☑ 조치함.

---

## 1. 배경 / 목표

감사 P3 (선택적 개선) 3건 처리:

| ID | 내용 | 권고 |
|----|------|------|
| 4-4 | `LoginController` 의 `/login/type/{mode}` 진입점 없는 핸들러 | 미사용이면 제거 |
| 4-5 | `SwRepository` 인터페이스 및 `findAllByOrderByYearDescCityNmAscDistNmAsc` 호출자 0 | 파일 삭제 |
| 5-4 | `AdminUserController` 검색 `keyword` 를 INFO 로 원문 기록 | 마스킹 또는 DEBUG 전환 |

**재검증 결과(2026-04-19)**:
- 4-4: `rg -n "/login/type" .` → 정의 1줄 + 감사 문서 참조 1건. 링크·fetch·form action 에서 호출 0건.
- 4-5: `rg -n "SwRepository" src/` → 파일 자체 1건. `@Autowired SwRepository` 또는 `SwRepository` 주입 0건 (이전 스프린트 2b 에서 `SwService` 정리 후 남은 고아).
- 5-4: `AdminUserController.java:101, 116` 에서 `log.info("검색 수행 - 타입: {}, 키워드: {}", searchType, keyword)` + 접근 로그도 keyword 포함.

**목표**: 유지보수 부담·입력 데이터 장기 보관 리스크를 P3 수준에서 최종 정리.

---

## 2. 기능 요건 (FR)

### 4-4. `/login/type/{mode}` 핸들러 제거

| ID | 내용 |
|----|------|
| FR-4-4-A | `LoginController.loginModePage(...)` 메서드 및 `@GetMapping("/login/type/{mode}")` 어노테이션 제거. |
| FR-4-4-B | 관련 import 중 남는 쓰임이 없는 것만 제거: `PathVariable` (다른 메서드 미사용). `Model` 은 `loginPage` 가 유지하므로 남김. |
| FR-4-4-C | Spring Security / `SecurityConfig` 의 URL 매칭 패턴에 `/login/type/**` 특수 허용이 있는지 확인 후 있으면 함께 제거. (없으면 변경 없음) |

### 4-5. `SwRepository` 인터페이스 삭제

| ID | 내용 |
|----|------|
| FR-4-5-A | `src/main/java/com/swmanager/system/repository/SwRepository.java` 파일 삭제. |
| FR-4-5-B | 삭제 전 최종 스캔: `rg -n "SwRepository" src/` 결과 해당 파일 1건 외 0건이어야 함. |

### 5-4. `AdminUserController` 검색 keyword 로그 안전화

| ID | 내용 |
|----|------|
| FR-5-4-A | line 101: `log.info("Page: {}, 검색 타입: {}, 키워드: {}", page, searchType, keyword)` → 키워드 원문 제거. 대체: `log.info("Page: {}, 검색 타입: {}, 키워드 길이: {}", page, searchType, keyword != null ? keyword.length() : 0)`. |
| FR-5-4-B | line 116: `log.info("검색 수행 - 타입: {}, 키워드: {}", searchType, keyword)` → **레벨 강등(DEBUG) + 원문 제거**. 대체: `log.debug("검색 수행 - 타입: {}, 키워드 길이: {}", searchType, keyword != null ? keyword.length() : 0)`. (codex 지적 반영: DEBUG 라도 외부 로그 수집기가 가져갈 수 있으므로 원문 로그는 레벨 불문 금지) |
| FR-5-4-C | keyword 자체가 민감 PII 일 가능성 낮지만(주로 사용자명·조직명 검색) 장기 보관 시 회원정보 재식별 리스크. 본 FR 로 해결. |
| FR-5-4-D | `AdminUserController` 전체에서 `keyword` 변수가 로그 포맷 파라미터로 전달되는 지점 0건이어야 함 (`rg -n 'log\.[a-z]+\([^)]*keyword' src/main/java/com/swmanager/system/controller/AdminUserController.java` → 0). |

---

## 3. 비기능 요건 (NFR)

| ID | 내용 |
|----|------|
| NFR-1 | Maven compile 성공. |
| NFR-2 | 서버 재기동 후 `/login` 페이지 접근 정상, `/admin/user` (또는 동등 경로) 검색 동작 정상. |
| NFR-3 | 키워드 원문 로그 미노출 — **강한 기준** (codex 지적 반영): (a) 정적 검증: FR-5-4-D grep 0건. (b) 런타임 검증: 재기동 후 알려진 유니크 문자열(예: `AUDIT_TEST_987xyz`)로 실제 검색 1회 수행 → `grep -c 'AUDIT_TEST_987xyz' server.log` = 0. INFO/DEBUG 레벨 무관. |
| NFR-4 | `/login/type/{mode}` 경로 제거 확정. **판정**: Spring Security filter chain 구조상 미인증 요청은 라우팅 전에 `/login` 으로 302 리다이렉트되므로 "404 반환" 은 기준 부적절. 실제 기준: (a) 정적 검증 `rg -n "/login/type" src/main/java/` 0 hit, (b) 동일 302 응답이 존재하지 않는 임의 경로(`/definitely-not-exists`)와 동일한지 확인 — 동일하면 엔드포인트 제거 확정. (codex verify 결과 반영, 2026-04-19) |

---

## 4. 의사결정 / 우려사항

### 4-1. `loginModePage` 제거 vs 유지 — ✅ 제거 확정
- 현재 사용처 0건, template `login.html` 에서도 `mode` 분기 없음(코드 전수 확인 가능). 제거해도 회귀 없음.

### 4-2. `SwRepository` 삭제 vs `SwProjectRepository` 로 통합 — ✅ 삭제 확정
- `SwProjectRepository` 가 이미 모든 정렬·검색을 Specification 기반으로 제공. 통합 불필요.

### 4-3. 키워드 로그 처리 — ✅ 원문 전면 제거 (codex 지적 반영)
- 당초 안: line 101 은 길이만, line 116 은 DEBUG 강등.
- codex 반영: DEBUG 도 외부 로그 수집기가 수집할 수 있으므로 **레벨 불문 원문 로그 금지**. 두 라인 모두 키워드 원문 제거, 길이·타입만 기록. line 116 은 DEBUG 레벨 유지(디버깅 빈도 낮음).
- 검색 실패 디버깅은 브라우저 DevTools Network 탭 또는 해당 유저의 실시간 재현으로 대체 가능.

### 4-4. 감사 P2 5-1 조치와의 관계 — ✅ 충돌 없음
- 스프린트 2c 에서 logback `org.hibernate.SQL` 등을 WARN 으로 낮춤. 본 건은 `AdminUserController` 자체 로그 레벨 조정이므로 무관.

---

## 5. 영향 범위

| 계층 | 파일 | 유형 |
|------|------|------|
| Controller (수정) | `src/main/java/com/swmanager/system/controller/LoginController.java` | `loginModePage` 메서드 + import 제거 |
| Repository (삭제) | `src/main/java/com/swmanager/system/repository/SwRepository.java` | 파일 전체 삭제 |
| Controller (수정) | `src/main/java/com/swmanager/system/controller/AdminUserController.java` | log.info 2줄 수정 |
| Docs (수정) | `docs/generated/audit/2026-04-18-system-audit.md` | 4-4, 4-5, 5-4 체크박스 ☑ 조치함 |

**수정 3 + 삭제 1 + 문서 수정 1. 신규 0. DB/API 계약 변경 0.**

---

## 6. 리스크 평가

| 리스크 | 수준 | 완화책 |
|--------|------|--------|
| `/login/type/{mode}` 가 외부 북마크·타사 시스템에 남아있을 가능성 | 낮음 | 인증 플로우 초기 화면이고 사용처 0건 확인. 404 나와도 `/login` 으로 바로 이동 가능 |
| `SecurityConfig` 가 `/login/type/**` 를 permitAll 로 특수 처리 | 낮음 | 구현 전 `SecurityConfig` grep 재확인 (FR-4-4-C) |
| keyword 길이만 남기는 방식으로 검색 실패 디버깅 어려움 | 낮음 | 동일 정보는 DEBUG 레벨(`log.debug("검색 수행 - 타입: {}, 키워드: {}", ...)`) 에서 확인 가능 |

---

## 7. 승인 요청

본 기획서에 대한 codex 검토 및 사용자 최종승인을 요청합니다.
