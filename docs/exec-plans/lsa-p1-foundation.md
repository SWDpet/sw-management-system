# [개발계획] LSA P1 — DB + 권한 배선 + 메뉴 재구성 + 골격

- **기획서**: `docs/product-specs/lsa-license-ledger.md` (승인 2026-06-29)
- **단계**: P1/4 (토대). **상태**: 개발계획(codex 검토 대기)
- **머신**: 회사 PC(S1NRKD000353050 확인) — 운영DB 작업 가능

---

## P1 범위 (shippable: 메뉴·권한·빈 LSA 페이지 동작)

### 1. DB 마이그레이션
**`db_init_phase1.sql`** (신규 환경 — users CREATE, L140 auth_license 옆):
```sql
auth_lsa character varying(10) DEFAULT 'NONE'::character varying,
```
**`db_init_phase2.sql`** (DbInitRunner 가 기동 시 실행 → 운영DB 자동 반영):
```sql
-- LSA: 권한 컬럼 + 라이선스 발급 대장
ALTER TABLE users ADD COLUMN IF NOT EXISTS auth_lsa VARCHAR(10) DEFAULT 'NONE';
UPDATE users SET auth_lsa='NONE' WHERE auth_lsa IS NULL;
CREATE SEQUENCE IF NOT EXISTS lsa_license_id_seq;
CREATE TABLE IF NOT EXISTS lsa_license (
    id          bigint PRIMARY KEY DEFAULT nextval('lsa_license_id_seq'),
    city_nm     varchar(50),
    dist_nm     varchar(200),
    dept_nm     varchar(100),
    team_nm     varchar(100),
    user_nm     varchar(50),
    tel         varchar(20),
    email       varchar(100),
    version     varchar(50),
    issuer      varchar(50),
    ps_info_id  bigint REFERENCES ps_info(id),
    created_by  varchar(50),
    created_at  timestamp DEFAULT now(),
    updated_by  varchar(50),
    updated_at  timestamp
);
```
→ fresh-init-smoke(빈 postgres replay)가 자동 검증. 운영DB 는 앱 재기동 시 DbInitRunner 가 ALTER/CREATE 적용.

### 2. 권한 배선 (auth_lsa)
- **`User.java`**: `@Column(name="auth_lsa") private String authLsa;` + @PrePersist `if(this.authLsa==null) this.authLsa="NONE";` (authLicense 패턴).
- **`AuthSummary.java`**: FIELDS 에 `{"authLsa","엘싸"}` 추가(10→11) + readAuth switch `case "authLsa": return u.getAuthLsa();` + 주석 "10개"→"11개".
- **`AdminUserController.java`(codex)**: `approveUser`(L208)·`updateUser`(L255) **둘 다** authLsa @RequestParam + setAuthLsa.
- **`admin-user-list.html`**: authLicense select 2곳(L157 승인폼·L423 수정폼) 뒤에 authLsa select(NONE/VIEW/EDIT, 라벨 "LSA: X/조회/편집") 추가.
- (UserDTO 에 auth 필드 없음 확인됨 → 변경 불요. 변경 시 재확인.)

### 3. 메뉴 재구성 (`top-nav.html` 라이선스 그룹)
```html
<th:block th:if="${admin OR authLicense!=NONE OR authLsa!=NONE}" ...>
  <span class="sb-group-label">라이선스</span>   <!-- 헤더, 클릭 미이동 -->
  <a th:if="${admin OR authLicense!=NONE}" href="/license/registry/list" ...>License4J</a>
  <a th:if="${admin OR authLicense!=NONE}" href="/geonuris/license/list" ...>GeoNURIS</a>
  <a th:if="${admin OR authLsa!=NONE}"     href="/lsa/list" ...>LSA</a>
</th:block>
```
→ codex 권고: **실제 템플릿 문법 사용** — 그룹 th:block 조건 = `#authorization.expression('hasRole(''ROLE_ADMIN'')') or authLicense!=NONE or authLsa!=NONE`, sub 링크는 각각 `#authentication.principal.user?.authLicense`(License4J/GeoNURIS)·`?.authLsa`(LSA) th:if. **클래스는 sb-parent/sb-link/sb-sub 계열**(sb-group-label 신규 금지) 재사용 — **디자인팀 자문(다크모드·토큰)**.

### 4. LSA 골격 (placeholder)
- **`LsaController`**(`/lsa`): `getCurrentUser`+`checkViewAuth`(authLsa VIEW이상 또는 admin, QuotationController throw→403 패턴). `/lsa/list` → 빈 목록 뷰 `lsa/lsa-list`(P2 에서 검색·데이터 채움).
- **`lsa-list.html`**: 최소 레이아웃(제목·작성버튼 자리·"P2 구현 예정" 또는 빈 테이블). 디자인시스템 재사용.

## 5. 검증 (S 품질)
- **`AuthSummaryTest`**: 10→11 박제 갱신(11필드 전부 badge·라벨·순서) + **trim 케이스(codex)**: `summarize(u,10)` 에 11권한 → 10 표시 + `moreCount==1`. PIT 92% 유지 확인(`-Ppit` AuthSummary 측정).
- **`AdminUserControllerTest`**: approveUser·updateUser authLsa 반영 검증(존재 시).
- **`LsaControllerMvcTest`**: `/lsa/list` 미인증/NONE→403, VIEW→200 `lsa/lsa-list`(standalone MockMvc 확립 패턴).
- **DB 게이트(codex)**: phase2 수정 → `DbInitRunnerBaselineTest`/baseline fixture(해시) 갱신 필요 여부 확인. 신규 `/lsa/list` endpoint → **endpoint inventory/golden 테스트** 실패 시 golden 갱신.
- `mvnw -o clean verify` green + 전 게이트(JaCoCo·Map부채·거대클래스·ArchUnit) 통과. CI 3 job green.
- 회사 PC 앱 재기동 → 운영DB auth_lsa/lsa_license 생성 확인 + 브라우저 메뉴 렌더(다크모드).

## 6. 리스크
- AuthSummary PIT: 11필드로 늘며 summarize(max) 트림 로직 영향 — 테스트 max=11 로 조정.
- 메뉴 th:if 조건 복잡도 — admin 헬퍼 표현식 기존 패턴 재사용.
- lsa_license FK(ps_info_id) → ps_info 선행 존재(phase1). P1 은 데이터 미입력이라 FK 무부하.
