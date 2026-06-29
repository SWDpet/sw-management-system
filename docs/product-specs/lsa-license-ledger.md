# [기획서] 라이선스 대장 개선 — 메뉴 재구성 + LSA 신설

- **작성팀**: 기획팀 / **작성일**: 2026-06-29
- **출처**: 사용자 요청(`라이선스.txt`) + 3대 결정(AskUserQuestion).
- **상태**: 초안(구현 전, 승인 대기). **S 품질 목표** — 정식 워크플로(기획→codex→승인→개발계획→codex→승인→구현→검증)+게이트.

---

## 0. 정책 메모
⚠ 기존 license 모듈(LicenseRegistry/Geonuris/QrLicense)은 "영구 패스"(품질 유보)였으나, **본 LSA 는 사용자 명시 요청 + S 품질 목표**라 **정식 품질 대상**(테스트·게이트·MockMvc net 적용). License4J(=기존 대장)·GeoNURIS 는 리라벨/메뉴만 손대고 로직 불변.

## 1. 요구사항 (라이선스.txt + 결정)
1. **메뉴 재구성**: 사이드바 "라이선스 대장" → **"라이선스"(그룹 헤더, 클릭해도 대장 미이동)**. 하위:
   - **License4J** = 기존 라이선스 대장(`/license/registry`) 역할 리라벨.
   - **GeoNURIS** = 변경 없음(`/geonuris/license`).
   - **LSA** = 신설.
2. **LSA**: 클릭 → ① 목록(검색 + 작성 버튼). 작성 → 입력 페이지.
   - 입력 필드: 지자체(**시도·시군구 드롭다운 캐스케이드**)·부서·팀·이름·전화번호·이메일·**버전**·**발급자**.
   - **ps_info 대조**: 시도/시군구/부서/팀/이름/전화/이메일을 ps_info 에서 불러오되 **수정 가능**(시도/시군구/부서/팀 동일하나 담당자 다를 수 있음).
   - **ps_info 동시저장(결정①)**: 입력 시 새 담당자(이름/전화/이메일)면 **ps_info 에 새 행 INSERT**(기존 담당자 보존).
   - **발급자**: 로그인 사용자 이름 자동. **관리자는 발급자 변경 가능**.
3. **승인(결정②)**: 별도 결재 흐름 **없음** — 작성=즉시 확정. 관리자="승인 권한"=전권(수정/삭제).
4. **권한(결정③)**: LSA 는 별도 **조회/편집/관리자** 권한. **User 에 신규 `auth_lsa` 컬럼**(NONE/VIEW/EDIT + ROLE_ADMIN=전권). 다른 메뉴 동일 패턴.

## 2. 범위
### D1 — DB (codex: 마이그레이션 경로 명확화)
- **신규 테이블 `lsa_license`**: id(PK seq)·city_nm·dist_nm·dept_nm·team_nm·user_nm·tel·email·version·issuer·ps_info_id(FK nullable)·created_by·created_at·updated_by·updated_at.
- **User `auth_lsa` 컬럼**(varchar, default 'NONE'). **마이그레이션 경로(drift 방지)**:
  - `db_init_phase1.sql` users CREATE 에 `auth_lsa` 컬럼 추가(신규 환경).
  - `db_init_phase2.sql`(DbInitRunner 가 기동 시 실행)에 `ALTER TABLE users ADD COLUMN IF NOT EXISTS auth_lsa varchar DEFAULT 'NONE'` + `lsa_license CREATE IF NOT EXISTS`(기존 환경 자동 반영).
  - 기존 사용자 백필: `UPDATE users SET auth_lsa='NONE' WHERE auth_lsa IS NULL`.
  - 운영DB(회사 PC) 동일 ALTER 적용(swdept 패치 도구 또는 수동). fresh-init-smoke 가 lsa_license/auth_lsa 자동 검증.

### D2 — 백엔드 (S 품질: 테스트 포함)
- `Lsa`(엔티티)·`LsaRepository`·`LsaService`·`LsaController`(`/lsa`)·`LsaDTO`.
- 목록(검색: 지자체/이름/버전/발급자), 작성 폼, 저장(LSA insert + ps_info upsert), 상세/수정/삭제.
- ps_info 조회 API(시도/시군구/부서/팀 → 담당자 후보 prefill) + 시군구 캐스케이드 재사용(`/api/districts` 등).
- 발급자 자동(로그인 이름)·관리자 변경.
- **ps_info upsert 판정 규칙(codex)**: `normalize(userNm)+normalize(tel)+lower(email)` 기준 finder(`PersonInfoRepository` 신규 메서드). 일치 행 있으면 재사용, 없으면 INSERT. ⚠**tel·email 둘 다 빈값이면 자동 중복판정 금지**(동명이인 오매칭 방지) → 항상 신규 INSERT. tel 하이픈/공백 정규화. 저장 후 `lsa_license.ps_info_id` 는 **반드시** 신규/기존 `PersonInfo.id` 연결. PersonInfo id 는 수동 `@Id`(seq 아님) → `ps_info_id_seq` nextval 명시 할당.

### D3 — 권한 (codex: 영향 범위 전체 명시)
- `User.authLsa` 필드 + `@PrePersist` default "NONE" + `UserDTO` 반영.
- `AuthSummary`(util): **FIELDS 10→11** + `readAuth` switch 11분기 + **`AuthSummaryTest` 10→11 박제 갱신**(⚠ PIT 게이트 92% 유지 검증).
- `AdminUserController.updateAuth`: authLsa @RequestParam 추가 + **`admin-user-list.html` 승인/수정 폼 2곳** select 추가 + `AdminUserControllerTest` 갱신.
- `LsaController` checkViewAuth/checkEditAuth(QuotationController 패턴)·관리자 전권.

### D4 — 프론트/디자인 (⚠ 디자인팀 가상 자문 필수 — AGENTS.md A+D)
- **사이드바 그룹 재구성**: 라이선스 헤더(클릭 미이동) + License4J/GeoNURIS/LSA 3 sub.
  - **메뉴 권한 조건(codex)**: 그룹 표시 = `ROLE_ADMIN or authLicense != NONE or authLsa != NONE`. 하위 = License4J/GeoNURIS 는 `authLicense`, **LSA 는 `authLsa`** 분리(authLsa=VIEW·authLicense=NONE 사용자가 LSA 메뉴 보이도록 — 회귀 방지).
  - 디자인 토큰·다크모드 일관성.
- LSA 목록(테이블+검색), 입력 폼(캐스케이드+prefill). 기존 디자인시스템 재사용.

### D5 — 검증 (S 품질)
- `LsaServiceTest`(ps_info upsert·발급자·검색, mock 기반)·`LsaControllerMvcTest`(standalone MockMvc, 라우팅/권한/redirect/JSON — 확립 패턴).
- 회사 PC 브라우저 QA(메뉴·캐스케이드·prefill·저장·ps_info 동시저장·권한). JaCoCo/Map부채 게이트 통과.

## 3. 요건
- **FR**: 메뉴 재구성·LSA 목록/검색/작성/수정/삭제·ps_info 대조 prefill + 동시저장(새 담당자 INSERT)·발급자 자동/관리자변경·auth_lsa 권한분리.
- **NFR(S 품질)**: 권한 가드 기본 포함([[feedback_all_pages_require_permission]])·`mvnw -o clean verify` green·신규 Map 금지(MapDebt ratchet)·MockMvc net·CI 전 게이트 통과·codex 사전검토+dual-review+듀얼푸시. ⚠SSN/PII 더미값.

## 4. 영향 / 리스크
- **R1 (高) AuthSummary PIT 게이트**: authLsa 추가 → AuthSummary FIELDS 11개 + AuthSummaryTest(10→11) 갱신 필요. PIT 92% 유지 검증.
- **R2 DB 마이그레이션**: 신규 테이블 + auth_lsa 컬럼. 운영DB(회사PC) ALTER + phase SQL + fresh-init. 기존 사용자 auth_lsa=NONE 백필.
- **R3 ps_info 동시저장**: 새 행 INSERT 시 id 시퀀스·중복 판정(이름+전화+이메일 동일성) 로직. 운영DB 쓰기라 회사 PC 만.
- **R4 디자인**: 사이드바 그룹 구조 변경 → 디자인 자문 + 다크모드/토큰.
- **R5 라이선스 정책 전환**: LSA 는 영구패스 예외(S 품질 대상). License4J/GeoNURIS 는 불변.

## 5. 단계 (phased — 각 단계 검증·커밋)
- **P1**: DB(lsa_license + auth_lsa) + 메뉴 재구성(License4J 리라벨 + LSA placeholder) + authLsa 권한 배선(AuthSummary/AdminUser).
- **P2**: LSA 목록 + 검색 + 권한 가드.
- **P3**: LSA 입력 폼(캐스케이드 + ps_info prefill) + 저장(LSA insert + ps_info upsert) + 발급자.
- **P4**: 상세/수정/삭제 + MockMvc net + 브라우저 QA.
