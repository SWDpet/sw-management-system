# 개발계획서 — 전사 사이드바 + 반응형 + 대시보드 승격 (global-sidebar-responsive)

- **상태**: v1.1 (codex 검토 반영 — 사용자 승인 대기)
- **작성일**: 2026-06-11 (v1) / v1.1 동일자 (codex: 셸/예외클래스 정합·화면수 52 검증·권한active 매핑표·Phase2 공유메서드·Phase별 롤백)
- **스프린트명**: `global-sidebar-responsive`
- **기획서**: `docs/product-specs/global-sidebar-responsive.md` (v0.2, commit fab5b2d)

---

## 0. 핵심 전략 (per-page 편집 최소화)
1. **공통 프래그먼트 재사용**: `fragments/top-nav.html :: nav` 의 **이름(`nav`)을 유지**하고 내용만 사이드바로 교체 → 51개 `th:replace` 화면이 **편집 없이** 사이드바를 받음.
2. **셸 + 전역 오프셋 (FR-2 정합)**: 사이드바 `<nav class="app-sidebar" position:fixed>`. **기본 오프셋**은 `body:has(.app-sidebar){ padding-left:var(--sb-w) }`(사이드바 있는 화면만 → 로그인/에러 자동 제외, per-page 무편집). **예외 클래스**(FR-2): `body.no-sidebar`(오프셋 해제) / `.fullscreen`(팀모니터) / `@media print`(인쇄·PDF). 모달·fixed alert 는 `position:fixed` 라 오프셋 영향 없음(점검만).
3. **반응형 토큰** `--sb-w`: 데스크탑 230px / 태블릿 64px / 모바일 0(드로어).
4. **화면 수(검증)**: `th:replace="~{fragments/top-nav :: nav}"` = **51개** + main-dashboard 인라인 GNB **1개** = **총 52개**. (Phase 3·QA 범위 고정)

## 1. Phase 1 — 토큰 + 사이드바 프래그먼트 + 셸/반응형 CSS

### T-1 (토큰) `static/css/design-system.css`
- `:root` 에 `--cat-1`~`--cat-6`(차트/막대 카테고리색: #0F766E,#2563eb,#D97706,#0891B2,#7c3aed,#059669), `--sb-w`(반응형) 추가.

### T-2 (사이드바 프래그먼트) `fragments/top-nav.html`
- `th:fragment="nav"` 유지. 내용을 **사이드바**로: 로고 + 그룹(개요/사업/자산/계정) + 하단 사용자·마이페이지·로그아웃.
- **기존 GNB 의 모든 메뉴·드롭다운 항목·`sec:authorize`/`th:if` 권한 분기 1:1 이전**(FR-1). 드롭다운 메뉴(라이선스/견적/문서/관리자)는 사이드바에선 **그룹 헤더 + 하위 링크**로 펼침.
- `aria-label`, 활성 `aria-current=page`(prefix 매칭, FR-3).
- 모바일 햄버거 버튼(`top-nav.html` 상단에 모바일 전용 바) + 드로어 토글 JS + 백드롭 + Esc/포커스 트랩.

#### T-2 매핑표 — 기존 GNB → 사이드바 (권한·링크·active prefix 1:1 이전, FR-1/FR-3)
| 사이드바 항목 | 링크 | 권한 게이트 | active prefix |
|---|---|---|---|
| 서버관리 | /infra/list | `authInfra != NONE` | /infra/* |
| 사업관리 | /projects/status | `authProject != NONE` | /projects/* |
| 담당자관리 | /person/list | `authPerson != NONE` | /person/* |
| 라이선스(그룹) 대장목록/CSV업로드/GeoNURIS목록/GeoNURIS발급 | /license/registry/list · /license/registry/upload · /geonuris/license/list · /geonuris/license/new | 그룹=`authLicense != NONE or ROLE_ADMIN`, 업로드·발급=`authLicense==EDIT or ROLE_ADMIN` | /license/* , /geonuris/* |
| 견적서(그룹) 목록/작성/견적대장/패턴/비고패턴/노임단가 | /quotation/list · /quotation/new · /quotation/ledger · /quotation/patterns · /quotation/remarks-patterns · /quotation/wage-rates | 그룹=`authQuotation != NONE or ROLE_ADMIN`, 작성=`EDIT or ADMIN` | /quotation/* |
| 업무계획 | /workplan/calendar | `authWorkPlan != NONE or ROLE_ADMIN` | /workplan/* |
| 문서관리(그룹) 사업문서목록/운영문서목록 + (EDIT)착수·기성·준공·점검·장애·지원·설치·패치 | /document/list · /ops-doc/list · /document/create(docType) · /ops-doc/{fault,support,install,patch}/form | 그룹=`authDocument != NONE or ROLE_ADMIN`, 작성=`EDIT or ADMIN` | /document/* , /ops-doc/* |
| 성과통계 | /performance/personal | `authPerformance != NONE or ROLE_ADMIN` | /performance/* |
| 관리자(그룹) 사용자승인/조직도/로그/시스템관계도/팀모니터 | /admin/users · /admin/org-units · /admin/logs · /admin/system-graph · /admin/team-monitor | `ROLE_ADMIN` | /admin/* |
| (하단) 마이페이지 / 로그아웃 | /mypage · /logout | 로그인 | /mypage |
- 그룹(라이선스/견적/문서/관리자)은 사이드바에서 **그룹 헤더 + 하위 링크**(아코디언 또는 상시 펼침 — §7).

### T-3 (셸/반응형 CSS) `design-system.css`
- `.app-sidebar{position:fixed;left:0;top:0;height:100vh;width:var(--sb-w);...}`
- `body:has(.app-sidebar){padding-left:var(--sb-w)}`
- 브레이크포인트(FR-4): `@media(max-width:1199px){:root{--sb-w:64px}}` (아이콘 축소) / `@media(max-width:767px){:root{--sb-w:0} .app-sidebar{transform:translateX(-100%)} .app-sidebar.open{transform:none}}` (드로어).
- `@media print{ body{padding-left:0} .app-sidebar,.mobile-bar{display:none} }`
- `.no-sidebar`(셸 적용 화면이지만 오프셋 해제), `.fullscreen`(팀모니터) 예외.
- 본문 폭: `@media(min-width:1920px){ .container,.admin-container{max-width:1760px} }` (와이드 중앙정렬).

## 2. Phase 2 — 대시보드 승격 (파일럿)

### T-4 (컨트롤러) `MainController`
- 현 `dashboardPreview` 로직을 **공유 private 메서드 `buildDashboardModel(queryYear, model)`** 로 추출 → `/`(mainDashboard)와 공용. `/` 가 증감·연도추이·금액Top·최근등록·임박일정·만료라이선스 전부 모델에 담음.
- `GET /dashboard-preview` → **`return "redirect:/"`** (302). 기존 `dashboardPreview` 본문 로직은 공유 메서드로 이동(중복 제거).
- 회귀 주의: 기존 `/` 가 쓰던 모델 속성(totalProjects 등)은 공유 메서드가 그대로 채우므로 유지.

### T-5 (템플릿) `main-dashboard.html`
- 본문을 dashboard-preview 구조로 교체(차트행+피드행+증감 테이블). **인라인 GNB 제거 → 공통 사이드바 프래그먼트 사용**.
- raw hex → `--cat-*` 토큰 치환(FR-7). `dashboard-preview.html` 의 인라인 색·하드코딩 `/dashboard-preview` 링크 정리(또는 preview 템플릿 제거).
- 라이선스/GeoNURIS 비동기 KPI JS 유지.

### T-6 (정리) `dashboard-preview.html`
- 승격 후 템플릿 제거(라우트는 `/` 리다이렉트). 또는 잠정 유지 시 링크만 `/` 로.

## 3. Phase 3 — 51개 화면 적용
- T-2 프래그먼트 교체로 **대부분 자동 반영**(편집 불필요). 
- 화면별 점검: 자체 고정폭/인라인 style 로 깨지는 곳만 컨테이너 보정. 특수화면에 예외 클래스 부여:
  - 인쇄/PDF: `document/*`, `quotation/*` PDF·preview → `@media print` 로 처리(대부분 자동), 필요 시 `.no-sidebar`.
  - 팀모니터(`admin/team-monitor`) 풀스크린 → `.fullscreen`.
- **대상 51화면(그룹)**: admin(5), infra(3), document(8: list/detail/batch/commence/interim/completion/inspect-detail/doc-inspect), geonuris(4), license(3), person(2), project(3), ops-doc(6), performance(3), qrcode(3), quotation(7: list/form/detail/ledger/pattern/remarks/wage), workplan(2: form/process-status, calendar), mypage(1). (+main-dashboard 인라인 1)

## 4. Phase 4 — QA (기획 §7-1)
- 뷰포트 `375/768/1199/1920`(+필요시 2560) × 계정 3종(관리자/EDIT/제한) × 51+1 화면.
- 특수: 문서·견적 **PDF/인쇄**, 팀모니터 풀스크린, 넓은 금액 테이블 가로스크롤, 모달, fixed alert/토스트.
- 합격: 레이아웃 깨짐·메뉴 오노출·가로 overflow 0.

## 5. 구현 순서
Phase 1(T-1~T-3) → Phase 2(T-4~T-6, 대시보드 검증) → Phase 3(전파+특수화면) → Phase 4(QA) → codex 구현검토.

## 6. 회귀 / 롤백 (Phase별 파일 세트)
- **Phase 1 롤백** = `fragments/top-nav.html` + `static/css/design-system.css` revert → 전 화면 GNB 원복.
- **Phase 2 롤백** = `MainController.java` + `main-dashboard.html` + `dashboard-preview.html` revert → 대시보드·라우트 원복.
- **Phase 3 롤백** = 특수화면 예외 클래스 부여분(개별 파일) revert.
- **Phase별 커밋** 필수 → 문제 시 해당 Phase 단위 롤백.
- **회귀 표면 최대**: 52개 화면(§7-1 QA 매트릭스로 검증).
- **`:has()` fallback**: 미지원 환경 대비 — 기본 `body{padding-left:var(--sb-w)}` + 로그인/에러에 `body.no-sidebar` 부여 방식 대안 준비(사내 최신 크롬 기준이면 `:has()` 우선).

## 7. 미결
- `:has()` 의존 vs body 클래스 방식 최종 택1(브라우저 지원 확인 후) — 기본은 `:has()`.
- 드롭다운 메뉴(라이선스/견적/문서/관리자)의 사이드바 표현: 항상 펼침 vs 아코디언.
