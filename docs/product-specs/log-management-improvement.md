# 기획서 — 로그관리 개선 + 대시보드 로그 통계

- 문서상태: **DRAFT v0.2** (2026-06-16) — codex 1차 검토(⚠) 반영 + 사용자 결정 확정
- 유형: 기능 개선 (로그관리 + 대시보드)
- 관련 SSoT: `AccessLog`(access_logs), `AccessActionType`, `LogAction`(레거시), `MenuName`, `LogController`, `MainController`, `CustomAuthenticationSuccessHandler`, `SecurityConfig`
- 워크플로 단계: 기획서 → (DB·디자인 자문) → codex 검토 → **사용자 최종승인** → 개발계획

## 변경 이력
- v0.1 → v0.2: codex 검토(⚠) 반영 — 집계식 정의, AccessActionTypeTest(hasSize 13) 갱신 명시, LogAction SSoT 정리, 로그아웃 범위 한정, detail 표준 포맷·삭제 링크 정책, 인덱스 세분화, 폼/API 중복적재 점검. 사용자 결정 4건 확정(아래 §8).

---

## 1. 배경 / 문제
- 로그관리(`/admin/logs`)는 **단일 평면 테이블** 1개. 접속자/메뉴 관점 구분 없음, **통계 없음**.
- **로그인/로그아웃 미기록.** `AccessActionType`(enum, 13개 freeze)에 LOGIN/LOGOUT 없음. 단, 레거시 `LogAction`에 `로그인`/`로그아웃` 문자열 상수는 존재(미사용).
- **사업관리 CRUD 미로깅.** `MenuName.PROJECT` 상수만 있고 `SwController` 가 `logService.log` 미호출.
- 대시보드에 운영 통계 카드 없음.

## 2. 목적
1. 접속자 로그 / 메뉴·행위 로그 **탭 분리**로 가독성·감사성 향상.
2. **로그인/로그아웃(명시적)** 이벤트 적재로 접속 이력 추적.
3. **사업 변경이력**(신규/수정/삭제)을 로그 기반으로 대시보드 노출.
4. 대시보드 **통계 4종** 제공.

## 3. 범위

### 3-A. 로그관리 화면 — **탭 분리** (`/admin/logs`, ADMIN 전용)
- [접속자 로그] 탭: 로그인/로그아웃 이벤트(아이디·이름·IP·시각·구분).
- [메뉴·행위 로그] 탭: 메뉴/액션 로그(조회·등록·수정·삭제 등, 접속 제외).
- 각 탭: 키워드 검색 + 기간 필터 + 페이지네이션.

### 3-B. 로그인/로그아웃 적재
- 로그인 **성공**: `CustomAuthenticationSuccessHandler` 에서 `logService.log("접속", LOGIN, "로그인 (IP: …)")` 적재.
- **로그아웃**: `SecurityConfig` 에 `LogoutSuccessHandler`(또는 logout success url 핸들러) 추가 — 세션 무효화 **전** 사용자 식별이 살아있는 시점에 적재. **명시적 로그아웃만** 대상(세션 만료·중복 로그인 만료·브라우저 종료는 범위 외).
- 로그인 **실패는 미적재**(결정 D1). menu_nm = **"접속"**(결정 D4, 신규 `MenuName.ACCESS="접속"`).

### 3-C. 사업관리 CRUD 로깅 계측
- `SwController` 폼(`/save`, `/delete/{id}`) + API(`POST /api`, `PUT /api/{id}`, `DELETE /api/{id}`)에 로깅 추가.
- 신규 vs 수정 = `projId == null` 여부. 삭제는 삭제 전 엔티티를 조회해 스냅샷 detail 구성.
- **중복 적재 방지**: 한 사용자 액션이 폼 또는 API 중 한 경로만 타므로 경로별 1회만 호출(서비스 계층 중복 호출 금지).
- **detail 표준 포맷(FR-10)**: `"[projId] 사업명 (시도 시군구 · 시스템)"`. 대시보드는 이 포맷에서 사업명 표시, **삭제 건은 상세 링크 비활성**(행이 없으므로). 신규/수정은 projId 링크 허용.

### 3-D. 대시보드 통계 (기본 최근 30일 · 목록 6건 — 결정 D2)
1. **최근 사업 변경이력** — access_logs(menu="사업관리", action∈{등록,수정,삭제}) 최신 6건, 신규/수정/삭제 뱃지.
2. **일자별 접속/활동 추이(30일)** — 일자별 `접속자수 = COUNT(DISTINCT userid) (action=로그인 기준)`, `활동건수 = COUNT(*)`.
3. **메뉴별 사용 빈도 TOP** — 최근 30일 menu_nm 별 COUNT 상위 6.
4. **액션별 건수** — 최근 30일 action_type 별 COUNT(조회/등록/수정/삭제/…).

### 범위 외(Out)
- 로그 보존기간/아카이빙, CSV export, 실시간 모니터링, 로그인 실패·세션만료 기록(후속).

## 4. 기능 요건 (FR)

| ID | 요건 |
|---|---|
| FR-1 | `/admin/logs` 를 [접속자 로그]/[메뉴·행위 로그] 탭으로 분리. ADMIN 전용. 각 탭 검색·기간·페이지네이션. |
| FR-2 | 로그인 성공 시 (menu="접속", action="로그인", userid·username·IP·시각) 1건 적재. |
| FR-3 | **명시적 로그아웃** 시 (menu="접속", action="로그아웃") 1건 적재. 세션만료/중복로그인 만료는 제외. |
| FR-4 | 사업관리 등록/수정/삭제 시 access_logs 에 등록/수정/삭제 1건 적재. |
| FR-5 | 대시보드 "최근등록사업" → "최근 사업 변경이력"(뱃지 + 사업명·사용자·시각, 6건). |
| FR-6 | 대시보드 일자별 접속/활동 추이(30일): 접속자수=COUNT(DISTINCT userid|action=로그인), 활동건수=COUNT(*). |
| FR-7 | 대시보드 메뉴별 사용 빈도 TOP(30일, 6건). |
| FR-8 | 대시보드 액션별 건수(30일). |
| FR-9 | 권한 가드 준수(대시보드=로그인 사용자, 로그관리=ROLE_ADMIN). |
| FR-10 | 사업 변경이력 detail 표준 포맷 `[projId] 사업명 (지역·시스템)`. 삭제 건은 상세 링크 비활성. |

## 5. 비기능 요건 (NFR)

| ID | 요건 |
|---|---|
| NFR-1 | 통계는 GROUP BY 집계 + **기간(30일) 조건 필수**로 풀스캔 억제. §6 인덱스 적용. |
| NFR-2 | 로그 적재 실패가 로그인/로그아웃/사업저장 본 흐름을 막지 않도록 `LogService` try/catch 격리 유지. |
| NFR-3 | `AccessActionType` 13→15(LOGIN/LOGOUT) 확장 → **`AccessActionTypeTest`(hasSize(13)) 반드시 15로 갱신**, "정확히 13" 주석/문서 갱신. 레거시 `LogAction` 은 enum 으로 통합(deprecated 표기 또는 제거). |
| NFR-4 | 신규 화면/엔드포인트 권한 가드 포함(모든 페이지 권한 필요 정책). |
| NFR-5 | 과거 소급 불가(계측 이후분부터). 대시보드/화면에 안내 또는 수용. |

## 6. 데이터 모델 / DB팀 자문 결과(반영)

- **재사용**: 기존 `access_logs` 그대로. **신규 테이블 없음.**
- **AccessActionType 확장**: `LOGIN("로그인")`, `LOGOUT("로그아웃")` 2개 추가(→15). 라벨은 레거시 `LogAction` 과 동일하므로 기존 데이터(있다면)와 호환. action_type 은 VARCHAR 라 DB 마이그레이션 불필요(코드/테스트만 갱신).
- **SSoT 정리**: `LogAction`(문자열 상수) 과 `AccessActionType`(enum) 이중 SSoT → enum 단일화. 신규 코드는 enum 사용, `LogAction` 은 @Deprecated.
- **menu_nm**: 신규 `MenuName.ACCESS="접속"`(로그인/로그아웃 전용).
- **인덱스(통계별)**:
  - 변경이력/액션·메뉴 집계: `access_logs(access_time)` 범위 인덱스 우선(모든 통계가 30일 조건 공유).
  - 보조: `(menu_nm, action_type)` 또는 `(action_type)` — 데이터량 보고 개발계획에서 확정.
  - 로그관리 검색 `LIKE %kw%` 는 btree 효과 제한 → 인덱스 대상 아님(현행 유지).
  - 인덱스 추가 위치: `db_init_phase2.sql` vs 별도 SQL → 개발계획에서 결정.
- **소급 불가**: access_logs 에 기존 사업관리/접속 행위 없음 → 계측 후 누적.

## 7. 디자인팀 자문 결과(반영)
- 로그관리: **탭 분리** 확정. 접속자 로그 컬럼(아이디·이름·IP·시각·구분[로그인/로그아웃]), 메뉴·행위 로그 컬럼(시각·사용자·메뉴·액션·상세).
- 대시보드: 통계 4종 카드 그리드. 추이=막대/스파크라인, 메뉴 TOP·액션별=가로막대/숫자카드. 기존 디자인 토큰·다크모드 일관성.
- 사업 변경이력 뱃지: 신규(초록)/수정(파랑)/삭제(빨강) — 기존 chip 토큰 재사용.

## 8. 사용자 결정 (확정)
- **D1 로그인 실패**: 미적재(후속 과제).
- **D2 통계 기본**: 최근 **30일**, 목록 표시 **6건**.
- **D3 로그관리 화면**: **탭 분리**.
- **D4 menu_nm 라벨**: **"접속"**.
- (잔여 기술결정은 개발계획에서: 인덱스 추가 위치, LogAction 제거 vs deprecated, 추이 차트 형태 세부.)

## 9. 리스크
- enum 확장 → `AccessActionTypeTest`·"13" 불변 가정 회귀(개발계획에서 수정).
- 로그량 증가(로그인/로그아웃·사업CRUD) → 보존정책 부재(후속 아카이빙).
- 통계 풀스캔 → 인덱스 미비 시 대시보드 지연(NFR-1 로 완화).

## 10. codex 검토 이력
- R1 (2026-06-16): ⚠ — 집계식 정의/테스트 갱신/SSoT/로그아웃 범위/detail 포맷·삭제 링크/인덱스 세분화/중복적재 지적 → v0.2 전면 반영.
- R2 (2026-06-16): **⭕** — R1 지적 6건 전부 해소 확인. 개발계획 진행 가능. 개발계획에 다음 체크리스트 필수 반영:
  1. 로그아웃 핸들러: `SecurityConfig` 의 `logoutSuccessUrl` → `LogoutSuccessHandler` 로 교체/위임, **세션 무효화 전 Authentication/principal 읽는 테스트** 포함.
  2. 통계 쿼리 SQL/JPQL 고정: 일자 date bucket/timezone, 활동건수에 로그인/로그아웃 포함 여부, 접속자 탭 필터 기준(menu="접속" vs action∈{로그인,로그아웃}).
  3. 탭 필터 정의.
  4. 폼/API 실제 호출 경로 **중복 적재 감사**.
  - 확인된 코드 사실: `AccessActionTypeTest.java:18 hasSize(13)` 존재, `SecurityConfig` 는 `logoutSuccessUrl` 만 보유.

---
*다음 단계: 본 v0.2 사용자 최종승인 → 개발계획서 작성 → codex 검토 → 구현.*
