# 개발계획서 — 로그관리 개선 + 대시보드 로그 통계

- 문서상태: **DRAFT v0.3** (2026-06-16) — codex R1(⚠)·R2(⚠) 반영
- 기획서: `docs/product-specs/log-management-improvement.md` (v0.2, 사용자 승인 / codex R2 ⭕)
- 워크플로 단계: 개발계획 → codex 검토 → **사용자 최종승인** → 구현 → codex 검증 → 작업완료
- 원칙: 단계(P0~P4) 독립·순차. 각 단계 컴파일·테스트 통과 후 다음. 모든 신규 화면/엔드포인트 권한 가드 포함.

## 변경 이력
- v0.1 → v0.2 (codex R1 반영): P3 기간필터(from/to) 추가, 로그아웃 방식 A안 확정+근거 정정, 통계 SQL 전체 명시, 보조 인덱스 채택 확정, 로그아웃 테스트 clearContext 강제, P0 Javadoc "13"→"15" 명시, P3/P4 테스트 필수화, 권한 가드 테스트 추가.
- v0.2 → v0.3 (codex R2 반영): P3 날짜경계를 LocalDateTime(fromStart/toExclusive)로 명확화, P4 "최근 변경이력(#1)"은 기간무관 최신 6건으로 명시(30일은 집계 #2~4 한정), P4 대시보드 인증 가드 테스트 추가.

## 확인된 코드 사실 (구현 기준점)
- `LogService.logInternal` 은 `SecurityContextHolder`(userid/username) + `RequestContextHolder`(IP)로 식별자 도출. 실패 try/catch 격리 + orphan guard.
- 로그인 성공 핸들러 `CustomAuthenticationSuccessHandler.onAuthenticationSuccess` 시점 SecurityContext 채워짐.
- 로그아웃: `LogoutFilter` 가 현재 `Authentication` 을 캡처해 `LogoutHandler.logout(req,res,authentication)` 로 전달 → **핸들러 실행 순서/Context clear 와 무관하게 파라미터로 사용자 식별 가능**(핵심 근거).
- `SecurityConfig` logout: `logoutUrl=/logout`, `logoutSuccessUrl=/login?logout=true`, `invalidateHttpSession(true)`, 쿠키삭제. 별도 핸들러 없음. 로그인 `failureHandler(authFailureHandler)` 존재.
- `AccessActionType` Javadoc "유효값 개수: 정확히 13", `AccessActionTypeTest.java:18 hasSize(13)`.
- 레거시 `LogAction`(LOGIN/LOGOUT 문자열) 미사용 잔존.
- 사업 저장: `SwController` 폼/API. `DocumentController:1668` 직접 save 는 문서맥락 4컬럼(본 범위 외).

---

## P0. Enum / 상수 기반 (FR-2,3 / NFR-3)
**변경 파일**
1. `constant/enums/AccessActionType.java` — `LOGIN("로그인")`, `LOGOUT("로그아웃")` 추가(→15). **Javadoc 의 "유효값 개수: 정확히 13 (FR-1)" → "15"** 로 갱신. 필요 시 SYNONYMS 에 레거시 "로그인"/"로그아웃" 흡수.
2. `constants/MenuName.java` — `public static final String ACCESS = "접속";`.
3. `constants/LogAction.java` — `@Deprecated` + Javadoc("AccessActionType 으로 대체"). 제거는 별도(미사용 확인되나 안전상 보류).
4. `test/.../AccessActionTypeTest.java` — `hasSize(13)`→`hasSize(15)`, LOGIN/LOGOUT 라벨·역직렬화 단언 추가.

**테스트**: `mvnw -q test -Dtest=AccessActionTypeTest`. 전체 grep `hasSize(13)`/"정확히 13"/숫자 가정 없는지 재확인.
**롤백**: 상수·Javadoc·테스트 수치 원복.

## P1. 로그인 / 로그아웃 적재 (FR-2,3 / NFR-2)
**로그아웃 방식(A안 확정)**: `.addLogoutHandler(accessLogLogoutHandler)`. 근거 = **LogoutFilter 가 캡처한 authentication 파라미터**(핸들러 순서·Context clear 무관). 기획서 R2 의 "LogoutSuccessHandler 교체" 표현은 동일 목적(정확한 userid 적재)을 만족하는 A안으로 확정 — success url(`/login?logout=true`)은 그대로 두고 부가 핸들러만 추가.

**변경 파일**
1. `service/LogService.java` — 오버로드 `log(String menuNm, AccessActionType action, String detail, String useridOverride, String usernameOverride)`. `logInternal` 리팩터: override 있으면 context 대신 사용, **없으면 기존 동작 그대로**. IP 는 RequestContextHolder(요청). **try/catch·orphan guard·RequestContextHolder 예외 격리 유지**(NFR-2). 기존 시그니처는 override=null 위임.
2. `config/CustomAuthenticationSuccessHandler.java` — 생성자 주입에 `LogService` 추가. `loginAttemptService.loginSucceeded` **이후** `logService.log(MenuName.ACCESS, LOGIN, "로그인", user.getUserid(), user.getUser().getUsername())`. 로그 적재 실패가 로그인 성공 흐름을 막지 않음(LogService try/catch 의존).
3. `config/AccessLogLogoutHandler.java` (신규) — `implements LogoutHandler`. `auth!=null && principal instanceof CustomUserDetails` 면 `logService.log(MenuName.ACCESS, LOGOUT, "로그아웃", userid, username)`, 아니면 skip.
4. `config/SecurityConfig.java` — logout 빌더에 `.addLogoutHandler(accessLogLogoutHandler)` 추가(주입).

**테스트**(codex 필수):
- `AccessLogLogoutHandlerTest`: `SecurityContextHolder.clearContext()` 한 상태에서 **mock Authentication(principal=CustomUserDetails) 파라미터만으로** LogService.log 가 LOGOUT+정확 userid 로 1회 호출. anonymous/null 이면 미호출.
- 성공 핸들러: LOGIN 1회 호출.
- (권장) MockMvc `/logout` 200/redirect 스모크.
**롤백**: 핸들러 등록·신규 파일·오버로드 제거.

## P2. 사업관리 CRUD 로깅 (FR-4, FR-10)
**변경 파일**
1. `controller/SwController.java` — `LogService` 주입. 헬퍼 `projLogDetail(p)` = `"[" + projId + "] " + projNm + " (" + 지역 + " · " + 시스템 + ")"`(null 안전).
   - `/save`: `isNew = projId==null` → 저장 후 `log(PROJECT, isNew?CREATE:UPDATE, detail)`.
   - `/delete/{id}`: 삭제 **전** `getProject(id)` 스냅샷 → 삭제 후 `log(PROJECT, DELETE, detail)`.
   - `POST /api`(CREATE)/`PUT /api/{id}`(UPDATE)/`DELETE /api/{id}`(DELETE: 삭제 전 스냅샷).
**중복 적재 감사**(codex): 폼·API 상호 배타 → **서비스 계층 로깅 금지, 컨트롤러 1회만**.
**테스트**: 저장/삭제 시 `logService.log` 1회·action 정확(Mockito). 권한 가드(EDIT/ADMIN) 기존 유지 확인.
**롤백**: 로깅 호출 제거.

## P3. 로그관리 화면 탭 분리 (FR-1, FR-9)
**탭 필터**: 접속자 로그=`menu_nm='접속'`, 메뉴·행위 로그=`menu_nm<>'접속'`.
**기간 필터(codex 필수)**: 각 탭 `from`,`to`(`LocalDate`, 옵션) + 키워드.
**변경 파일**
1. `repository/AccessLogRepository.java` — `@Query` 2종(기간·키워드 nullable):
   - 날짜 경계는 **컨트롤러에서 LocalDateTime 으로 변환해 전달**: `fromStart = from.atStartOfDay()`, `toExclusive = to.plusDays(1).atStartOfDay()`(둘 다 nullable).
   - `findAccessTab(Pageable p, @Param kw, @Param fromStart LocalDateTime, @Param toExclusive LocalDateTime)`:
     `WHERE l.menuNm='접속' AND (:kw IS NULL OR l.userid LIKE %:kw% OR l.username LIKE %:kw% OR l.actionDetail LIKE %:kw%) AND (:fromStart IS NULL OR l.accessTime >= :fromStart) AND (:toExclusive IS NULL OR l.accessTime < :toExclusive) ORDER BY l.accessTime DESC`
   - `findMenuTab(...)`: 동일 패턴 + `l.menuNm<>'접속'`.
2. `controller/LogController.java` — `@RequestParam tab=menu|access`(기본 menu), `from`,`to`,`kw`,`page`. 탭별 페이징·검색. ADMIN 가드 유지.
3. `templates/admin-logs.html` — 탭 UI(접속자/메뉴·행위) + 기간(date)·검색 폼. 탭별 컬럼:
   - 접속자: 시각·아이디·이름·IP·구분(로그인/로그아웃)
   - 메뉴·행위: 시각·아이디·이름·메뉴·액션·상세
**테스트(필수)**: 리포지토리 슬라이스 — 탭 필터·기간 경계(to 포함성)·키워드. `/admin/logs` **ADMIN 가드** 테스트(비-ADMIN 403).
**롤백**: 단일 테이블 뷰·단일 쿼리로 원복.

## P4. 대시보드 통계 4종 (FR-5,6,7,8 / NFR-1,5)
**쿼리(집계 통계 #2~4 = 최근 30일, 일자=`CAST(access_time AS date)`, DB 로컬 타임존 / 변경이력 #1 = 기간무관 최신 6건)**:
1. 최근 사업 변경이력: 파생 `findTop6ByMenuNmAndActionTypeInOrderByAccessTimeDesc("사업관리", List.of("등록","수정","삭제"))` (actionType=문자열 라벨 컬렉션). **기간 조건 없음**(최신 6건 — 변경이 드물어도 표시). 30일 기준은 집계 통계에만 적용.
2. 일자별 추이: `SELECT CAST(access_time AS date) d, COUNT(*) act, COUNT(DISTINCT CASE WHEN action_type='로그인' THEN userid END) visitors FROM access_logs WHERE access_time >= now()-interval '30 days' GROUP BY d ORDER BY d`.
3. 메뉴 TOP: `SELECT menu_nm, COUNT(*) c FROM access_logs WHERE access_time >= now()-interval '30 days' GROUP BY menu_nm ORDER BY c DESC LIMIT 6`.
4. 액션별: `SELECT action_type, COUNT(*) c FROM access_logs WHERE access_time >= now()-interval '30 days' GROUP BY action_type ORDER BY c DESC`.
**변경 파일**
1. `repository/AccessLogRepository.java` — 파생 1 + native @Query 3(Object[]/projection).
2. `controller/MainController.java` — `AccessLogRepository` 주입. 모델 `recentProjectLogs/logTrend/menuTop/actionCounts`. 기존 `recentProjects` 제거.
3. `templates/main-dashboard.html` — "최근 등록 사업"→"최근 사업 변경이력"(신규 초록/수정 파랑/삭제 빨강 뱃지, 사업명·사용자·시각, **삭제 건 상세 링크 비활성**). 통계 카드 3개.
4. 대시보드 CSS — 뱃지·카드(기존 chip 토큰 재사용, 다크모드 일관, 디자인 자문 반영).
5. `resources/db_init_phase2.sql` — **채택 확정 2개**(멱등):
   `CREATE INDEX IF NOT EXISTS ix_access_logs_time ON access_logs(access_time);`
   `CREATE INDEX IF NOT EXISTS ix_access_logs_menu_action ON access_logs(menu_nm, action_type);`
   DbInitRunner 가 기동 시 적용.
**테스트(필수)**: 통계 native projection 형/기간(30일) 결과 슬라이스 또는 통합. 대시보드 200·빈 데이터(소급 없음) graceful 렌더. **대시보드 인증 가드**(비인증 접근 → 로그인 redirect/302).
**롤백**: 모델/템플릿 원복. 인덱스는 **앱 롤백엔 불필요**, DB 정리 시 `DROP INDEX IF EXISTS ix_access_logs_time, ix_access_logs_menu_action`.

---

## 회귀/리스크 체크리스트 (구현 필수)
- [ ] AccessActionType 15개 — "13" 가정/switch 누락 grep.
- [ ] 로그아웃 LOGOUT 이 **실제 userid**(anonymous 아님) — clearContext 상태 핸들러 테스트로 보장.
- [ ] 폼/API 사업 저장 **중복 로깅 없음**.
- [ ] 집계 통계 3쿼리(#2~4) 30일 WHERE·일자버킷·타임존 일관. 변경이력(#1)은 기간무관 최신 6건.
- [ ] 접속자 탭=menu='접속' 필터·기간 경계 정확.
- [ ] 권한 가드: 대시보드=인증, /admin/logs=ADMIN(테스트 포함).
- [ ] 빈 데이터에서 대시보드·탭 graceful.

## 구현 순서
P0 → P1 → P2 → P3 → P4.

## 커밋 전략
- 단계별/기능 묶음 커밋. 최종 `작업완료` 시 codex 구현검증 후 dual push(SWDpet/ukjin914).

## codex 검토 이력
- R1 (2026-06-16): ⚠ — P3 기간필터 누락, 로그아웃 방식 기획 불일치, 통계 SQL 미명시, 보조 인덱스 미확정, 로그아웃 테스트 강화, Javadoc 13 갱신, 테스트 필수화 지적 → v0.2 반영.
- R2 (2026-06-16): ⚠(잔여 문서정밀화 3건) — P3 날짜경계 LocalDateTime화, 변경이력#1 기간무관 명확화(집계만 30일), 대시보드 인증 가드 테스트 → v0.3 반영. (로그아웃 A안·보조 인덱스·Javadoc 13→15 는 ⭕ 확정.)
- 구현검증 R (2026-06-16): ⚠ — 핵심 경로(LogService 오버로드/로그아웃 authentication/중복적재/경계) ⭕. 반영: 통계쿼리 menu_nm·action_type NULL 제외, LOGIN 성공 핸들러 테스트, SwController CRUD 5경로 로깅 테스트(총 48 단위테스트 통과). ~~**회사 PC 통합검증 잔여**: @DataJpaTest 네이티브 통계쿼리·로그관리 ADMIN 가드 MockMvc·대시보드 렌더 smoke(실 Postgres 필요).~~
- **회사 PC 통합검증 완료 (2026-06-23, 운영DB 192.168.10.194:5880)**: 잔여 3종 모두 RUN_DB_TESTS 게이트 통합테스트로 작성·실행(9 통과).
  - `AccessLogStatsQueryIntegrationTest`(4) — 네이티브 통계쿼리(findDailyTrend30d/findMenuTop30d/findActionCounts30d/findTop6…) 실 스키마 무오류 실행 + MainController 소비계약(투영 타입·정렬 불변식) 검증. 읽기전용.
  - `DashboardAndAdminLogsSecurityIntegrationTest`(5) — `/admin/logs` ADMIN 가드(익명 302/비-ADMIN 403/ADMIN 200) + 대시보드 Thymeleaf 실제 렌더 smoke(백지 회귀 가드). 저장소 첫 MockMvc+Security 통합 하네스. ADMIN 렌더는 실 CustomUserDetails principal 필요(top-nav `principal.user.authProject`).
  - RUN_DB_TESTS 미설정 시 전부 skip(집/CI 안전).
