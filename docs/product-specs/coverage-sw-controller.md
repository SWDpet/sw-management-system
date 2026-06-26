# [기획서] SwController 커버리지 상향 (beyond-A)

- **작성팀**: 기획팀
- **작성일**: 2026-06-26
- **트랙**: beyond-A 커버리지 스프린트 — 컨트롤러 통합영역([[coverage-controller-integration]]) 잔여분. 사업(SW 프로젝트) 관리 컨트롤러.
- **상태**: ✅ **완료(2026-06-26)**. 정식 워크플로 전체 이행: 기획 codex APPROVE-WITH-FIX→보완→사용자승인 / 개발계획 동일 / 구현(SwControllerTest 39) / dual-review 합의3 반영 / 듀얼푸시. SwController INSTR 36.6→92.1%·LINE 43.5→96.6%, 전역 LINE 69.00→69.88%·INSTR 56.32→56.97%. floor LINE 0.66→0.67·INSTR 0.53→0.54.

---

## 1. 배경 / 목표

`SwController`(394줄, `/projects`)는 사업 마스터(sw_pjt) CRUD 의 중심 컨트롤러인데 현재 INSTR **약 36.6% / LINE 약 43.5%** 로 부분 커버. 기존 `SwControllerLoggingTest` 는 `projLogDetail`(로그 detail 포맷)만 검증하고, **웹 화면 엔드포인트·REST API·InitBinder 날짜파서·시군구 API 는 미커버**다.

**목표**: 권한 가드(미로그인 redirect / NONE throw / EDIT·ADMIN)·CRUD 분기·REST 401/403/404·InitBinder 날짜형식 파싱을 직접호출 단위테스트로 커버. **프로덕션 코드 변경 0(순수 테스트만)**.

---

## 2. 대상 엔드포인트

| 그룹 | 메서드 | 가드 |
|---|---|---|
| 시군구 API | getDistricts, getDistOptions | 없음 |
| 목록 | projectList(/status) | 미로그인 redirect:/login · authProject NONE→throw |
| 상세 | detail | 미로그인·NONE throw · 미존재 ResourceNotFoundException |
| 폼 | newProject(/new), form(/form) | EDIT 아니면 throw · form 은 id 유무 분기 |
| 저장 | saveProject(/save) | EDIT 가드 · 신규/수정 분기 |
| 삭제 | deleteProject(/delete) | EDIT 가드 |
| REST | getProjectApi/createProjectApi/updateProjectApi/deleteProjectApi | 미로그인 401 · 비권한 403 · 미존재 404 · ADMIN 우회 |
| 바인더 | InitBinder LocalDate PropertyEditor | 다중 날짜형식 파싱 + 빈값 null + 형식오류 throw |

---

### 2-1. 권한 매트릭스 (codex 보완 — 웹 vs REST 차이 고정)

| 엔드포인트군 | 미로그인 | 권한 규칙 | ADMIN 우회 |
|---|---|---|---|
| 웹 조회(projectList/detail) | redirect:/login | authProject **NONE 이면 throw**(VIEW/EDIT 허용) | ❌ 없음(role 무관) |
| 웹 편집(newProject/form/save/delete) | redirect:/login | **authProject=="EDIT" 아니면 throw** | ❌ **없음** (ADMIN 이어도 authProject≠EDIT 면 throw) |
| REST 조회(getProjectApi) | 401 | ROLE_ADMIN **또는** authProject≠NONE, 아니면 403. 미존재 시 **404** | ✅ ROLE_ADMIN 우회 |
| REST 편집(create/update/deleteApi) | 401 | ROLE_ADMIN **또는** authProject=="EDIT", 아니면 403 | ✅ ROLE_ADMIN 우회 |

> ⚠ **REST 404 는 getProjectApi 한정**. create/update/**deleteApi 는 target null 이어도 save/delete 호출 후 200** 반환(존재성 404 없음) — 404 기대 테스트 금지.

## 3. 테스트 컨벤션

[[coverage-controller-integration]] §3 동일. SwController 는 필드주입(@Autowired 10) + 권한을 `getCurrentUser()`(SecurityContextHolder) + `cu.getUser().getAuthProject()` 에서 읽음 → mock reflection 주입 + SecurityContext 직접세팅 후 메서드 직접호출. InitBinder 는 `WebDataBinder` 에 등록된 PropertyEditor 를 꺼내 직접 `setAsText/getAsText` 호출로 검증. 실 Postgres 무접촉.

---

## 4. 기능 요건 (FR)

| ID | 내용 |
|----|------|
| FR-1 | 웹 엔드포인트 가드·분기·렌더 커버. 세부분기 포함: projectList(page<0→0·city blank/nonblank districtOptions), form(id=null 신규 / id 존재 / id 미존재 ResourceNotFound), save(신규 projId null / 수정). **웹 편집은 EDIT 전용, ADMIN 우회 없음(§2-1)** — ADMIN+authProject≠EDIT throw 케이스 포함. |
| FR-2 | REST API 4종 401(미로그인)/403(비권한)/정상 + **getProjectApi 404(한정)** + **ROLE_ADMIN 우회**(§2-1). create/update/delete 는 미존재 404 없음(테스트 금지). |
| FR-3 | InitBinder LocalDate PropertyEditor: 8개 허용형식 중 대표 파싱 성공 + 빈값→null + 미허용형식→IllegalArgumentException. |
| FR-4 | 시군구 API(getDistricts/getDistOptions) 위임·빈 city 분기 커버. |
| FR-5 | 가드 위반 시 서비스 미호출 verify(부수효과 0). 프로덕션 코드 변경 0. |

---

## 5. 비기능 요건 (NFR)

| ID | 내용 |
|----|------|
| NFR-1 | compile 성공. |
| NFR-2 | `SwControllerTest`(신규) 전건 green. 기존 `SwControllerLoggingTest` 불변 green. |
| NFR-3 | 전체 `./mvnw -o clean verify`(전체 + JaCoCo 게이트) green. ⚠`-o`(offline)는 로컬 .m2 캐시 의존 — 캐시 미스 시 `-o` 제거. |
| NFR-4 | SwController·전역 커버리지 상승. floor 상향은 실측−~3pp, 게인 작으면 유지. |
| NFR-5 | (작업완료 트리거 이후 절차) 구현 후 dual-review(codex+Opus4.8) → 사용자 확인 → 듀얼푸시. 검증 요건과 분리. |

---

## 6. 의사결정

- **6-1 InitBinder 직접 PropertyEditor 검증** ✅: 풀 MVC 바인딩 없이 등록된 editor 를 꺼내 setAsText 호출 → 날짜 다형식 파서 결정적 커버.
- **6-2 SwProjectDTO.fromEntity 는 검증된 null-safe** ✅(coverage-dtos 선행): 상세/REST 응답에서 bare/부분 SwProject 사용 가능.
- **6-3 web saveProject 는 @ModelAttribute SwProject, REST 는 @RequestBody SwProject** ✅: 둘 다 mock swService.save 로 분기만 커버(신규=projId null, 수정=projId set).

---

## 7. 영향 범위 / 리스크

| 계층 | 파일 | 유형 |
|------|------|------|
| Test(신규) | `controller/SwControllerTest.java` | 신규 테스트 |
| Build(수정) | `pom.xml` | floor ratchet(상승 시) |

프로덕션 변경 0. DB/API/UI 변경 0 → 디자인팀 skip.

| 리스크 | 수준 | 완화 |
|---|---|---|
| InitBinder editor 추출 방식 취약 | 낮음 | WebDataBinder.findCustomEditor(LocalDate.class) 표준 API |
| REST/web 권한 매트릭스 누락 | 낮음 | FR-2 401/403/404/ADMIN 우회 명시 |

---

## 8. 승인 요청

본 기획서에 대한 codex 검토 및 사용자 최종승인을 요청합니다.
