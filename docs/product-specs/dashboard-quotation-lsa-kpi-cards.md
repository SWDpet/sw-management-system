# 기획서 — 대시보드 견적서·LSA 발급 KPI 카드 추가

- **스프린트**: `dashboard-quotation-lsa-kpi-cards`
- **작성**: 기획팀 (Claude) · 2026-07-02
- **상태**: 초안 (codex 검토 → 사용자 최종승인 대기)
- **요청 원문**: "대시보드에 견적서 발급 이력도 볼 수 있으면 좋겠다 — 최고 상단의 라이선스 대장처럼. 추가한 김에 LSA도."
- **형태 확정(사용자 선택)**: 최근 N건 리스트가 아니라 **KPI 카드형** (기존 '라이선스 대장' 카드와 동일 패턴). 2026-07-02.
- **최종승인(사용자, 2026-07-02)**: 옵션 **A**(견적서 숫자 = 전기간 누적, 견적서 백엔드 원 통계 로직 유지). 추가 요구 = **"전체/월 업데이트 현황"** → 각 카드에 **누적 전체 건수(큰 숫자) + 이번 달 발급(신규) 건수(서브텍스트)** 를 함께 표시. 이에 따라 서브텍스트를 '분류별'/'최근 발급일'에서 **'이번 달 발급 N건'** 으로 변경(아래 FR-1/FR-2 반영).

---

## 0. 배경·목적

메인 대시보드(`/`) 상단 KPI 그리드에는 이미 **라이선스 대장 / GeoNURIS** KPI 카드가 있고, 총 발급 건수를 AJAX 로 채우고 클릭 시 목록으로 이동한다. 사용자는 **견적서 발급**과 **LSA 발급 대장**도 같은 자리에서 한눈에 보고 싶어 한다.

- 견적서: 발급(발행)될 때마다 `qt_quotation` + `qt_quotation_ledger`(발급 대장)에 이력이 쌓이나, 대시보드에서는 확인 불가.
- LSA: `lsa_license` 발급 대장이 있으나 대시보드 진입점이 없음(사이드바 메뉴만 존재).

**목적**: 두 도메인의 발급 현황을 대시보드 상단 KPI 카드로 노출하여 접근성을 높인다. 라이선스 대장 카드와 **시각·상호작용·권한 규칙을 동일**하게 맞춘다.

---

## 1. 참조(모델) 구현 — 라이선스 대장 KPI 카드

| 구성 | 근거 위치 |
|------|-----------|
| KPI 카드 마크업 | `templates/main-dashboard.html:127` (`<div class="kc click" onclick=…>` + `id` span + `.skeleton`) |
| KPI 그리드 | `main-dashboard.html:123-129` (`.kpis`, 현재 5카드) |
| AJAX 채움 스크립트 | `main-dashboard.html:296-299` (`fetch('/license/registry/dashboard-stats')` → textContent + unskel, catch→'0건') |
| 데이터 API | `LicenseRegistryController.getDashboardStats` `license/…Controller.java:406` (`@ResponseBody`, 권한 없으면 graceful `{totalCount:0, error}`) |
| 클릭 이동 | `/license/registry/list` |

→ 신규 카드는 이 패턴을 **그대로 복제**한다(신규 컴포넌트/토큰 도입 없음).

---

## 2. 기능 요구사항 (FR)

### FR-1 — 견적서 발급 KPI 카드  ("전체/월 업데이트 현황")
- 대시보드 `.kpis` 그리드에 카드 1장 추가.
- 큰 숫자(**전체**) = **총 견적서 발급 건수(전기간 누적)** (`quotationRepository.count()`, 옵션 A).
- 서브텍스트(**월 업데이트 현황**) = **`이번 달 발급 N건`** — `created_at`(발급 시각)이 이번 달(현재 연·월)에 속하는 견적서 건수.
- 아이콘 = `fa-file-invoice-dollar` (사이드바 견적서 메뉴와 동일, `top-nav.html:125`).
- 클릭 시 **`/quotation/ledger`**(견적번호 관리대장 = 발급 이력 페이지)로 이동.
- 데이터 출처 = **기존** `GET /api/quotation/stats`(`QuotationController.getStats:670` → `QuotationService.getStats:385`) 재사용 + **월 카운트 키 1개 additive 추가**.
  - 기존 반환 키 유지: `total`, `용역_count`, `제품_count`, `유지보수_count`, `*_amount`.
  - **신규 additive 키**: `monthCount` = 이번 달 `created_at` 기준 발급 건수. `QuotationRepository` 에 월 범위 카운트 파생/`@Query` 1개 추가(읽기 전용, 하위호환 — 기존 키 불변). 카드는 `total`(숫자)·`monthCount`(서브)만 사용.

### FR-2 — LSA 발급 KPI 카드  ("전체/월 업데이트 현황")
- 같은 그리드에 카드 1장 추가.
- 큰 숫자(**전체**) = **누적 LSA 발급 건수(전기간)** (`lsaRepository.count()`).
- 서브텍스트(**월 업데이트 현황**) = **`이번 달 발급 N건`** — `created_at` 이 이번 달(현재 연·월)에 속하는 LSA 발급 건수.
- 아이콘 = `fa-certificate` (사이드바 LSA 메뉴와 동일, `top-nav.html:121`).
- 클릭 시 **`/lsa/list`**(LSA 발급 대장 목록)로 이동.
- 데이터 출처 = **신규** `GET /lsa/dashboard-stats`(`@ResponseBody`) 1개 추가.

### FR-3 — 동일 패턴 준수 + fetch 게이팅
- 두 카드 모두 기존 `.kc click` 컴포넌트 + `.skeleton` 로딩 + `DOMContentLoaded` fetch 블록(`main-dashboard.html:291-300`)과 동일한 구조로 구현. catch 시 `'0건'` 표시.
- **⚠ 필수(codex 지적 반영)**: 카드를 `th:if` 로 숨기는 것만으로는 부족하다. 신규 **fetch 블록도 반드시 게이트**해야 한다 — 그렇지 않으면 권한 없는 사용자의 브라우저에서도 `/api/quotation/stats`(403) 호출이 나가 콘솔 에러가 발생한다. 두 겹으로 방어:
  1. **서버 게이트(1차)**: 견적서 fetch 블록은 견적서 권한 표현식(FR-5)을 가진 `<th:block th:if="…authQuotation…">…</th:block>`, LSA fetch 블록은 `authLsa` 표현식 안에 배치 → 권한 없는 사용자에겐 해당 fetch JS 자체가 방출되지 않는다.
  2. **DOM 가드(2차, 방어적)**: fetch 실행 전 `var el=document.getElementById('quotationCount'); if(el){ fetch(...) }` 처럼 대상 span 존재를 확인. 카드가 `th:if` 로 없으면 span 도 없어 fetch 미실행.

### FR-4 — 신규 서버 엔드포인트 (LSA 한정)
- `LsaController` 에 `@GetMapping("/dashboard-stats") @ResponseBody` 추가 → 최종 경로 `/lsa/dashboard-stats`.
- 반환 JSON: `{ "total": <long>, "monthCount": <long> }` (전체 누적 / 이번 달 발급).
- `LsaService` 에 읽기 전용 집계 메서드 추가(`getDashboardStats()` → `total = count()`, `monthCount = 이번 달 created_at 카운트`), `LsaRepository` 에 월 범위 카운트 쿼리 1개 추가(읽기 전용).

### FR-5 — 권한 게이팅 (카드 노출)
- **견적서 카드**: `ROLE_ADMIN` 또는 `authQuotation != NONE` 일 때만 렌더.
- **LSA 카드**: `ROLE_ADMIN` 또는 `authLsa != NONE` 일 때만 렌더.
- 규칙은 **사이드바 메뉴 노출 규칙과 동일**(`top-nav.html:117,121,124`). 권한 없는 사용자에겐 카드 자체를 표시하지 않는다.
- 게이팅 방식: **템플릿 표현식 직접 사용**(컨트롤러 변경 불필요). top-nav 와 동일하게 `#authentication.principal.user?.authQuotation` / `authLsa` + `#authorization.expression('hasRole(''ROLE_ADMIN'')')` 를 `th:if` 로 사용. 카드 마크업과 fetch 스크립트 블록을 **동일 `th:if` 블록 안에 함께** 배치하여 카드+fetch 양쪽을 한 번에 게이팅(FR-3 참조).
  - 견적서: `#authorization.expression('hasRole(''ROLE_ADMIN'')') or (#authentication.principal.user?.authQuotation != null and #authentication.principal.user.authQuotation != 'NONE')`
  - LSA: 동일 표현식의 `authLsa` 버전.

---

## 3. 비기능 요구사항 (NFR)

- **NFR-1 (읽기 전용)**: 신규/재사용 서버 로직은 모두 조회(count/월 범위 count). 운영DB 쓰기·스키마 변경 **없음**. 견적서 `getStats` 변경은 **additive**(기존 키·소비처 불변, `monthCount` 키만 추가) — 기존 견적서 통계 화면 회귀 없음.
- **NFR-2 (graceful API)**: `/lsa/dashboard-stats` 는 권한 없거나 데이터 없을 때 예외 throw 대신 `{total:0, monthCount:0}` 반환(라이선스 카드 `dashboard-stats` graceful 패턴과 일치). 대시보드 fetch 콘솔 에러 금지.
- **NFR-3 (디자인 무증분)**: 신규 색상/토큰/컴포넌트 도입 0. 기존 `.kc` 재사용 → 다크모드·반응형 자동 상속.
- **NFR-4 (레이아웃 안정)**: KPI 그리드 5→최대 7개 카드. 기존 반응형 그리드가 자동 줄바꿈 유지, 깨짐 없음(디자인팀 확인).
- **NFR-5 (회귀 없음)**: 기존 라이선스·GeoNURIS 카드 및 대시보드 차트·피드·연도 필터 동작 불변.

---

## 4. 🎨 디자인팀 자문 (A+D 정책)

**A 판정**: 기획서 본문에 UI 키워드(`카드`, `아이콘`, `레이아웃`, `UI`, `다크모드`) 다수 → **디자인팀 자문 필수**. 자문 결과:

- **컴포넌트 재사용**: 신규 카드는 기존 `.kc` / `.kc.click`(hover·클릭 커서 포함)을 그대로 사용. 신규 CSS 규칙·토큰 없음 → 디자인 시스템 무증분, 다크모드 자동 정합.
- **아이콘 일관성**: 견적서 `fa-file-invoice-dollar`, LSA `fa-certificate` — **각 사이드바 메뉴 아이콘과 동일**하게 맞춰 인지 일관성 확보.
- **로딩 상태**: 기존 `.skeleton` shimmer 재사용(`main-dashboard.html:99`).
- **그리드 밀도**: 카드가 5→7개로 늘어 한 행 밀도가 올라감. 기존 `.kpis` 반응형 규칙으로 자동 줄바꿈되는지 **브라우저 실측 확인 필요**(구현 후 QA 항목 T-6).
- **서브텍스트 톤**: 라이선스 카드 `신규 N · 기존 N` 와 동일한 `라벨 N · 라벨 N` 구두점 스타일 유지(견적서=분류별, LSA=최근 발급일).
- **⚠ 별건 참고**: 채움버튼 대비비 이슈(디자인시스템 전역 특성)는 본 스프린트 범위 아님 — KPI 카드는 채움버튼 미사용.

> 근거: `docs/DESIGN.md` 토큰 상속, `feedback_ui_change_always_design_consult`(UI 변경은 항상 디자인 자문).

---

## 5. 🗄️ DB팀 자문

- **스키마 변경 없음**. 신규 테이블/컬럼/마이그레이션 0.
- **쿼리 부하**:
  - 견적서: `count()` + `countByCategory×3`(기존 getStats, 이미 운영 중) — 변화 없음.
  - LSA: `count()` + 최신 1건(`findTop1ByOrderByCreatedAtDesc`). `lsa_license`·`qt_quotation` 모두 소규모(수십~수백 행) → 풀스캔 무해.
- **인덱스**: 현 데이터 규모에서 불필요. 향후 대량화 시 `lsa_license.created_at` 정렬용 인덱스 고려(현 시점 과설계 → 보류).
- **트랜잭션**: 모두 `readOnly=true` (기존 `LsaService` 클래스 기본값 준수).

---

## 6. 권한 요약

| 대상 | 조회 노출 조건 | 근거 |
|------|----------------|------|
| 견적서 카드/데이터 | `ROLE_ADMIN` or `authQuotation != NONE` | `QuotationController.checkViewAuth`, `top-nav.html:125` |
| LSA 카드/데이터 | `ROLE_ADMIN` or `authLsa != NONE` | `LsaController.checkViewAuth`, `top-nav.html:117,121` |

- 견적서 `/api/quotation/stats` 는 `checkViewAuth` 로 이미 보호됨(권한 없으면 403). 카드를 서버 게이팅하므로 정상 경로에서 권한자만 호출.
- LSA 신규 엔드포인트는 동일 조건으로 게이트하되 **graceful 반환**(NFR-2).

---

## 7. 리스크·대안

| 리스크 | 대응 |
|--------|------|
| **카드는 `th:if` 로 숨겼지만 전역 JS 가 API 를 호출하는 회귀** (codex 지적) — fetch 블록을 게이트 밖 전역에 두면 권한 없는 사용자 브라우저에서도 `/api/quotation/stats`(403) 호출 발생 → 콘솔 에러 | FR-3 대로 fetch 블록을 카드와 **동일 `th:if` 서버 게이트 안**에 배치(JS 미방출) + DOM 존재 가드. 구현 후 비권한 계정으로 네트워크 탭 실측(T-3 확장). |
| `/api/quotation/stats` 가 권한 없을 때 403 throw | 위 게이팅으로 정상 경로에서 권한자만 호출. 만일의 catch 시 '0건' 폴백. |
| KPI 그리드 7카드 시 줄바꿈/밀도 | 기존 반응형 재사용, 구현 후 브라우저 실측(T-6). 필요 시 그리드 컬럼 규칙만 미세 조정(디자인팀 재확인). |
| 카드 숫자(견적서)의 집계 범위 모호 | **확정(사용자)**: 큰 숫자 = 전기간 누적(옵션 A, 라이선스 카드와 일관), 서브텍스트 = 이번 달 발급 건수(월 업데이트). '전체/월' 두 지표를 한 카드에 병기. |
| `이번 달 발급` 기준 컬럼 혼동(견적일자 vs 발급 시각) | **`created_at`(발급 시각) 기준**으로 통일(발급 이력 취지). `quote_date`(사용자 지정 견적일자) 아님. 견적서·LSA 동일 규칙. |

---

## 8. 범위 밖 (Out of scope)

- 최근 발급 **N건 리스트** 위젯(사용자가 KPI 카드형 선택 → 제외).
- 견적서 PDF 다운로드, 발급 감사로그 신규 테이블.
- 라이선스/GeoNURIS 카드 개편.
- 연도 스코프 통계(견적서 '올해' 집계) — 필요 시 후속.

---

## 9. 수용 기준 (테스트 T-n)

- **T-1**: 견적서·LSA 권한을 **모두 보유한 사용자(또는 `ROLE_ADMIN`)** 가 `/` 접속 시 KPI 그리드에 견적서·LSA 카드 2장 표시, 숫자(전체 누적)·서브텍스트(`이번 달 발급 N건`)가 스켈레톤 → 실데이터로 채워짐. (한쪽 권한만 있으면 해당 카드만 표시.)
- **T-2**: 견적서 카드 클릭 → `/quotation/ledger`, LSA 카드 클릭 → `/lsa/list` 이동.
- **T-3**: `authQuotation=NONE` 비관리자에게 견적서 카드 **미표시** + **해당 fetch 미발생**(네트워크 탭에 `/api/quotation/stats` 호출 없음); `authLsa=NONE` 비관리자에게 LSA 카드 **미표시** + `/lsa/dashboard-stats` **미호출**; `ROLE_ADMIN` 은 둘 다 표시·정상 호출.
- **T-4**: `GET /lsa/dashboard-stats` — 권한 有: `{total, monthCount}` 정상 JSON(`monthCount` = 이번 달 발급); 권한 無: `{total:0, monthCount:0}` graceful(예외 미발생). 견적서 `getStats` 는 `monthCount` 키 추가 후에도 기존 키(`total`·`*_count`·`*_amount`) 불변.
- **T-5**: 기존 라이선스·GeoNURIS 카드, 대시보드 차트·피드·연도 필터 회귀 없음.
- **T-6**: (디자인 QA) KPI 그리드 7카드 반응형 — 데스크톱/태블릿 폭에서 줄바꿈·정렬 깨짐 없음(브라우저 실측).

---

## 10. 변경 파일 예상 (개발계획에서 확정)

- `templates/main-dashboard.html` — 카드 2장 + fetch 블록 2개(각각 top-nav 표현식 `th:if` 게이팅). **컨트롤러 변경 불필요**.
- `lsa/controller/LsaController.java` — `/dashboard-stats` 엔드포인트(graceful).
- `lsa/service/LsaService.java` — `getDashboardStats()` (total + monthCount).
- `lsa/repository/LsaRepository.java` — 월 범위 count 쿼리.
- `quotation/service/QuotationService.java` — `getStats()` 에 `monthCount` 키 additive 추가.
- `quotation/repository/QuotationRepository.java` — 이번 달 `created_at` count 쿼리.
- 테스트: `LsaControllerMvcTest`(dashboard-stats 권한/형태·monthCount), `QuotationServiceTest`(getStats additive monthCount·기존 키 불변), `MainController` 슬라이스(플래그) — 개발계획에서 확정.
