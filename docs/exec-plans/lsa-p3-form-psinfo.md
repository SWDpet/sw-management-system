# [개발계획] LSA P3 — 입력 폼 + ps_info 연동 + 발급자 (핵심)

- **기획서**: `docs/product-specs/lsa-license-ledger.md` / **선행**: P1(cba27a0)·P2(ff01a99)
- **단계**: P3/4 (핵심). **상태**: 개발계획(codex 검토 대기). 머신=회사 PC(운영DB 쓰기 가능).

---

## P3 범위 (작성 폼 + ps_info 대조/동시저장 + 발급자)

### 1. 캐스케이드/조회 API (LsaController, auth 일관)
- 폼 진입 시 model 에 `sidoList`(SigunguCodeRepository.findDistinctSidoNm()).
- **`GET /lsa/api/districts?sido=`** → 시군구 list(`findBySidoNmOrderBySggNm`, @ResponseBody, checkViewAuth). 시군구 캐스케이드.
- **`GET /lsa/api/persons?city=&dist=&dept=&team=`** → 매칭 ps_info 담당자 후보(userNm/tel/email) JSON. prefill 용(checkViewAuth).

### 2. PersonInfoRepository 신규 메서드
- `findByCityNmAndDistNmAndDeptNmAndTeamNm(city,dist,dept,team)` → prefill 후보.
- dedup 은 서비스에서 후보 조회 후 Java 정규화 매칭(아래).

### 3. 입력 폼 (lsa-form.html — ⚠ 구현 전 디자인 자문 필수)
- 필드: 시도(드롭다운)·시군구(캐스케이드 드롭다운)·부서·팀·이름·전화·이메일·버전·발급자.
- **ps_info 불러오기**: 시도/시군구/부서/팀 입력 후 "담당자 불러오기" → /lsa/api/persons 호출 → 후보 있으면 이름/전화/이메일 prefill(복수면 선택). **전부 수정 가능**.
- **발급자**: 로그인 사용자 실명 자동(readonly). **관리자는 편집 가능**(isAdmin 시 input 활성).
- 디자인: 기존 폼 패턴(project-form/quotation-form) + 디자인시스템 토큰·.btn·.form 클래스. 다크모드 라이트 토큰.

### 4. 저장 (POST /lsa/save — checkEditAuth)
- **`LsaController.save(@ModelAttribute LsaForm form)`**: checkEditAuth(EDIT|admin throw→403). 발급자=비관리자면 강제 로그인 실명(폼값 무시), 관리자면 폼값. → `lsaService.create(form, issuer, createdBy)` → redirect:/lsa/list.
- **`LsaService.create`(@Transactional 쓰기)**:
  1. ps_info upsert: `findByCityNmAndDistNmAndDeptNmAndTeamNm` 후보 중 **정규화(userNm)+정규화(tel)+lower(email)** 일치 검색.
     - 일치 → 기존 PersonInfo.id 재사용.
     - 불일치 → 새 PersonInfo INSERT(city/dist/dept/team/userNm/tel/email 세팅, save → IDENTITY id).
     - ⚠**tel·email 둘 다 빈값이면 dedup 금지** → 항상 새 INSERT(동명이인 오매칭 방지). tel 하이픈/공백 정규화.
  2. Lsa 엔티티 생성(폼 필드 + issuer + createdBy + psInfoId=위 id) → save.
- **LsaForm**(DTO): city/dist/dept/team/userNm/tel/email/version/issuer 바인딩(@ModelAttribute, record 불가→@Getter/@Setter 클래스).

### 5. 검증 (S 품질)
- **`LsaServiceTest` 확장**: create — ① 기존 담당자 매칭 시 재사용(insert 0) ② 새 담당자 INSERT(psInfoId 연결) ③ tel·email 둘다 빈값→항상 INSERT ④ 정규화(공백/하이픈/대소문자) 매칭 ⑤ Lsa 필드 매핑(ArgumentCaptor). mock PersonInfoRepository/LsaRepository.
- **`LsaControllerMvcTest` 확장**: form GET(VIEW 200·sidoList model), save POST(EDIT→3xx /lsa/list·service 호출, VIEW→403), districts/persons API(JSON·checkViewAuth), 발급자 비관리자 강제(폼 위조값 무시 검증).
- `mvnw -o clean verify` green(MapDebt·도메인순수성·ControllerRepoRatchet) + **endpoint golden 갱신**(신규 /lsa/api/districts·/lsa/api/persons·/lsa/save·/lsa/new GET → GOLDEN_RECORD). PIT(LsaService 후보 시).
- 회사 PC: 앱 재기동 → 브라우저 QA(캐스케이드·prefill·저장·**운영DB ps_info 신규행 확인**·발급자 자동/관리자변경).

## 5-1. codex 반영(APPROVE-WITH-FIX 필수 5)
1. **finder null 정규화**: `@Query("... p.cityNm=:city AND p.distNm=:dist AND (:dept IS NULL OR p.deptNm=:dept) AND (:team IS NULL OR p.teamNm=:team)")` — dept/team blank→null trim 후 조회(미입력 prefill 보호).
2. **트랜잭션**: `LsaService.create` 에 **메서드레벨 `@Transactional`**(클래스 readOnly=true 오버라이드) + `PersonInfoRepository` 생성자 주입 명시.
3. **CSRF**: lsa-form.html 은 Thymeleaf `th:action`(자동 `_csrf` hidden). MockMvc save POST 는 standalone(CSRF 미강제)이라 토큰 불요, 단 실폼은 토큰 포함 검증.
4. **발급자**: 강제값 = `cu.getUser().getUsername()`(실명) 고정. 비관리자 폼값 무시.
5. **persons API**: `PersonInfo` 엔티티 직접 반환 금지(orgCd/distCd/sysNmEn 노출) → 전용 `PersonRow` record(userNm/tel/email)만.

## 6. 리스크
- **ps_info 운영DB 쓰기**(회사 PC만). 새 담당자 INSERT 시 IDENTITY id 자동. 트랜잭션 원자성(LSA+ps_info).
- 발급자 위조 방지: 비관리자 폼값 무시 = 서버 강제.
- prefill 복수 후보 UX(P3 단순화: 첫 후보 또는 select). 캐스케이드 JS = fetch + CSRF(전역 인터셉터).
- 정규화 매칭 오판(과매칭/과소매칭) — tel/email 둘다 빈값 가드로 동명이인 보호. endpoint golden 갱신 필수.
