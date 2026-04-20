# SW Manager — 디자인 시스템 (2025)

> 전체 사이트 UI를 2019년 어드민 템플릿 톤에서
> **2025 엔터프라이즈 SaaS (Linear · Stripe · Notion · Perplexity 급)** 수준으로 개편한 기록.

---

## 🎨 디자인 토큰 (Source of Truth)

### 색상 — Deep Teal (Cool-Warm Hybrid)

| 역할 | 토큰 | HEX | 용도 |
|------|------|-----|------|
| Primary | `--primary` | `#0F766E` | 브랜드 액센트, Primary CTA |
| Primary Dark | `--primary2` | `#115E59` | hover, 강조 텍스트 |
| Primary Light | `--primary-lt` | `#F0FDFA` | hover 배경, selected 배경 |
| BG | `--bg` | `#FAFAF9` | body 배경 (웜 오프화이트) |
| Surface | `--surface` | `#FFFFFF` | 카드/패널 배경 |
| Surface-2 | `--surface-2` | `#F5F5F4` | hover 배경 (stone-100) |
| Border | `--border` | `#E7E5E4` | 일반 테두리 (stone-200) |
| Border-2 | `--border2` | `#D6D3D1` | 강조 테두리 (stone-300) |
| Text | `--text` | `#1C1917` | 본문 (stone-900) |
| Text-2 | `--text2` | `#57534E` | 보조 (stone-600) |
| Muted | `--muted` | `#A8A29E` | 힌트/라벨 (stone-400) |
| Success | `--success` | `#059669` | 완료 상태 (emerald-600) |
| Warning | `--warning` | `#D97706` | 진행중/주의 (amber-600) |
| Danger | `--danger` | `#DC2626` | 삭제/에러 (red-600) |
| Info | `--info` | `#0891B2` | 정보 (cyan-600) |

### 타이포그래피
- 본문: `'Inter', 'Noto Sans KR', sans-serif`
- 모노: `'JetBrains Mono', monospace` (코드 스니펫)
- 기본 본문: 13.5px
- h2 페이지 제목: 1.35rem / 700
- 카드 숫자(KPI): 1.55rem / 700 / tabular-nums
- 메뉴: 0.82rem / 600
- 테이블 헤더: 0.72rem / 600 / uppercase / letter-spacing 0.06em

### 공통 패턴
- 버튼 라운드: `8px`
- 카드 라운드: `12~14px`
- 카드 hover: `translateY(-2px)` + soft shadow
- 테이블 행 hover: `inset 3px 0 0 0 var(--primary)` (좌측 teal 바)
- Input focus: teal 보더 + `0 0 0 3px rgba(15,118,110,0.12)` 링
- 버튼 press: `scale(0.97)`
- Transition 기본: `0.15s ~ 0.18s ease`

---

## 🧩 공용 GNB (Global Navigation Bar)

### 위치
- 프래그먼트: `src/main/resources/templates/fragments/top-nav.html`
- 인라인: `main-dashboard.html` (동일 구조 복사)

### 구조
```
[SW Manager 칩] · · · [서버관리][사업관리][담당자관리][라이선스 관리 ▾][견적서 ▾][업무계획][문서관리 ▾][성과통계][관리자 ▾] [🌙][마이페이지][로그아웃]
```

### 키 포인트
- **로고**: 단일 teal 칩 `SW Manager` (이전: [S] + SW Manager 분리 → 통합)
- **드롭다운**: 라이선스 관리 / 견적서 / 문서관리 / 관리자 (클릭 toggle)
- **아이콘**: Font Awesome SVG 통일 (이모지 제거)
  - 🖥️ → `fa-server`, 📋 → `fa-briefcase`, 👥 → `fa-users`
  - 📋 → `fa-key`, 📝 → `fa-file-invoice-dollar`, 📅 → `fa-calendar-days`
  - 📄 → `fa-file-lines`, 📈 → `fa-chart-line`, ⚙️ → `fa-gear`
  - 👤 → `fa-circle-user`, 🚪 → `fa-arrow-right-from-bracket`
- **마이페이지** 위치: 로그아웃 **바로 앞**
- **로그아웃**: Ghost 스타일 (투명 배경 + 연한 회색 텍스트 + hover 시 코랄)
- **다크모드 토글**: 🌙/☀️ 버튼 (마이페이지 앞)

---

## 📐 페이지 레이아웃 패턴

### 표준 페이지 구조
```html
<body>
  <!-- 공용 GNB -->
  <div th:replace="~{fragments/top-nav :: nav}"></div>

  <div class="container">      <!-- max-width 1700px · padding 28px 32px 56px -->
    <div class="page-header">  <!-- h2 + sub + action buttons -->
      ...
    </div>
    <!-- 콘텐츠 -->
  </div>
</body>
```

### 페이지 헤더 스타일 (라이트 버전)
```css
.page-header {
    padding: 0 0 22px; margin-bottom: 22px;
    border-bottom: 1px solid var(--border);
    display: flex; justify-content: space-between; align-items: center;
}
.page-header h2 {
    font-size: 1.35rem; font-weight: 700; color: var(--text);
    display: flex; align-items: center; gap: 10px;
}
.page-header h2 i { color: var(--primary); font-size: 1.1rem; }
```

→ 이전의 **진한 네이비(#1a237e) 블록 헤더**는 전 페이지에서 제거됨.

### 버튼 스타일 가이드
- **Ghost (기본)**: 배경 투명 + stone 보더 + stone 텍스트
- **Primary (강조)**: teal 배경 + 흰 텍스트 (페이지당 1~2개 권장)
- **Subtle (라이트)**: teal-50 배경 + teal 텍스트 + teal-200 보더
- **Action (상세/수정/삭제)**: outline 고스트 + 기능별 색 (teal/amber/red)

---

## 🃏 UI 패턴 라이브러리

### 요약 카드 (Summary Cards)
```
┌────────────────────┐
│ [icon] 라벨        │
│                    │
│ 142 건             │   ← tabular-nums, 숫자 강조, 단위 작게
│ ● 부가 메타정보    │   ← 작은 dot + 메타
└────────────────────┘
```
- 5개 카드 중 **1개만** `is-primary` (teal 그라디언트 + accent)
- 나머지는 모노크롬
- Hover: `translateY(-2px)` + 큰 soft shadow
- 클릭 가능 카드는 hover 시 `→` 화살표 등장

### 테이블 (Data Tables)
- 헤더: surface 배경 + muted 텍스트 + uppercase + 0.06em letter-spacing
- 행 높이: 14px vertical padding
- 행 구분선: `#F5F5F4` (매우 subtle)
- 행 hover: `#FAFAF9` 배경 + **inset 3px 0 0 0 teal** (좌측 teal 바)
- 합계/강조 컬럼: `#F0FDFA` (teal-50 배경)

### 스켈레톤 로딩 (비동기 카드)
```css
.skeleton { 
    background: linear-gradient(90deg, #F5F5F4 0%, #EDEDEC 50%, #F5F5F4 100%);
    animation: skeleton-shimmer 1.4s linear infinite;
    border-radius: 4px;
    color: transparent !important;
}
```
- fetch 완료 시 JS에서 `.skeleton` 클래스 제거

### 엠프티 스테이트 (Empty State)
```
        ┌──────────┐
        │  [icon]  │   ← 72x72 stone 배경 박스
        └──────────┘
   제목: 검색 결과가 없습니다
   안내: 다른 조건으로 검색하거나 신규 등록해보세요
```

### 상태 뱃지
- Success(완료): `#F0FDF4` 배경 + `#166534` 텍스트 + `#BBF7D0` 보더
- Warning(진행): `#FFFBEB` + `#92400E` + `#FDE68A`
- Danger(만료): `#FEF2F2` + `#B91C1C` + `#FECACA`

---

## ✨ 마이크로 인터랙션 (Phase 3)

```css
html { scroll-behavior: smooth; }

@keyframes fadeUp {
    from { opacity: 0; transform: translateY(6px); }
    to   { opacity: 1; transform: none; }
}
.page-title, .summary-container, .toolbar, .table-section {
    animation: fadeUp 0.45s cubic-bezier(0.2, 0.6, 0.2, 1) both;
}
/* 순차 지연: 0.04s, 0.08s, 0.12s */

button:active:not(:disabled) { transform: scale(0.97); transition: transform 0.08s; }
```

- 페이지 진입: 요소별 순차 fade-up
- 버튼 press: 미세 scale 반응
- 카드 hover: lift 2px
- 테이블 행 hover: 좌측 teal 바 등장
- 클릭 카드 hover: `→` 화살표 등장

---

## 🌙 다크모드 (실험적 · Phase 3)

### 현재 상태 (2026-04 기준)
- **메인 대시보드 전용** 실험 버전
- GNB의 🌙/☀️ 버튼으로 토글
- LocalStorage `sw_theme` 키에 선호 지속

### 스왑되는 토큰
```css
[data-theme="dark"] {
    --bg: #0C0A09;          /* stone-950 */
    --surface: #1C1917;     /* stone-900 */
    --surface-2: #292524;   /* stone-800 */
    --border: #44403C;      /* stone-700 */
    --text: #FAFAF9;
    --text2: #D6D3D1;
    --muted: #A8A29E;
}
```

### ⚠️ 알려진 제한사항
- **하드코딩된 인라인 style**이 많은 페이지는 전환 안 됨
  (예: `style="color:#5a5c69"` 같은 직접 지정)
- 메인 대시보드에서만 깔끔하게 동작
- 다른 페이지는 부분적으로만 전환

### 🚧 Phase 4 (예정 · 아직 착수 안 함)

> **사용자 요청**: "완전 다크모드는 전체 CSS 변수 리팩터 필요 (Phase 4 감) — 나중에 추가적으로 진행하게,
> 이후에 디자인 관련 이야기할 때 이 부분을 상기시켜줘"

### Phase 4 착수 시 해야 할 것
1. **50+ 템플릿의 인라인 style → CSS 변수 전환**
   - `color: #5a5c69` → `color: var(--text2)`
   - `background: #f8f9fa` → `background: var(--surface-2)`
   - `border: 1px solid #e3e6f0` → `border: 1px solid var(--border)`
2. **인라인 onmouseover/onmouseout 이벤트 → CSS `:hover`로 이전**
3. **각 페이지에 `[data-theme="dark"]` 오버라이드 확실히 적용**
4. **테스트**: 라이선스·견적서·문서·성과통계 전 페이지 다크모드 검증
5. **시스템 설정 연동**: `prefers-color-scheme: dark` 미디어 쿼리 감지 (선택)

---

## 🔒 마스킹 정책 (S3-B, 2026-04-20)

민감정보(tel/mobile/email/ssn/address) 의 표시 정책을 단일 기준으로 명시.

**확정 정책 (한 문장)**:
> DB는 항상 unmasked 원본을 저장하고, 관리자 사용자 목록 등 일반 화면·로그·디버그 출력에서는 SensitiveMask 출력 형태로 마스킹 표시한다. 단, 마이페이지(본인 전용) 및 문서관리·견적서(PDF 포함)에서는 항상 unmasked 원본을 노출한다.

| 영역 | 표시 정책 |
|------|----------|
| **DB 저장** | **항상 unmasked 원본** (정정 대상) |
| **마이페이지** | **unmasked (본인 전용)** — 본인이 자신의 정보를 확인·수정하므로 마스킹하지 않음 |
| **관리자 사용자 목록 등 일반 화면** | 마스킹 표시 (호출 측에서 명시적 적용) |
| **로그/디버그 출력** | **마스킹 표시** (값 절대 미포함, **userid + 필드명만** 기록) |
| **문서관리(document)·견적서(quotation)·견적서 PDF·실문서** | **항상 unmasked** |

### 회귀 방지 메커니즘 (S3-B)

마스킹 표시된 값이 폼 submit 으로 DB 에 다시 저장되는 회귀를 차단:

1. **`SensitiveMask`** — 화면 표시 시점에 마스킹 적용 (스프린트 6 도입)
2. **`MaskingDetector`** — 폼 입력값에 마스킹 패턴 감지 (S3-B 도입)
   - 1차: `SensitiveMask(currentDb) == input` 동등 비교 (100% 회귀 확정)
   - 2차: 컬럼별 정규식 (tel: `^\d{2,4}-\*{4}-\d{4}$` 등)
3. **MyPageController 가드** — 감지 시 DB 기존값 유지 + 사용자 토스트 + WARN 로그(값 미포함)

**관련 문서**: [`docs/plans/users-masking-regression-fix.md`](plans/users-masking-regression-fix.md), [`docs/dev-plans/users-masking-regression-fix.md`](dev-plans/users-masking-regression-fix.md)

---

## 🔐 로그인 페이지 — Minimal Gradient Mesh

### 디자인 컨셉
- 웜 오프화이트 베이스 (`#FAFAF9`)
- 5개 색상 블러 그라디언트 메시 (teal · 민트 · 코랄 · 크림 · cyan)
- 미세 SVG 그레인 텍스처 (`mix-blend-mode: multiply`, opacity 0.06)
- 장식 요소 0개 (깔끔함 극대화)
- Stripe / Linear / Notion 로그인 스타일 지향

### 이전에 고려한 대안들
- Topographic Map (등고선 + lat/lng 그리드) — GIS 도메인 정체성 강조
- Organic Blobs / Architectural Grid / Pure Whitespace

---

## 📋 개편 여정 (2026-04)

### Phase 0 — 초기 상태
- 보라 (#6B3EF0) 기반 2019년 어드민 템플릿 톤
- 페이지마다 다른 헤더 (메인=흰 GNB, 사업현황=네이비 블록, 마이페이지=독립)
- 5색 카드 (purple/green/orange/blue) 혼재
- 이모지 아이콘 (OS별 렌더 다름)
- 진한 코랄 filled 로그아웃 버튼

### Phase 1 — 디자인 시스템 통일
1. ✅ 보라 `#6B3EF0` → Deep Teal `#0F766E` (전 50+ 템플릿)
2. ✅ 공용 GNB 프래그먼트 (`fragments/top-nav.html`) 생성
3. ✅ 페이지 헤더 라이트화 (네이비 블록 → 1px border-bottom)
4. ✅ 로고 단일 teal 칩으로 통합
5. ✅ 로그아웃 Ghost 스타일로 전환
6. ✅ 요약 카드 모노크롬 + 단일 accent로 개편
7. ✅ 테이블 현대화 (14px 여백, hover 좌측 teal 바, sticky 헤더, uppercase 헤더)
8. ✅ 폼 focus ring (teal 보더 + 반투명 teal 링)

### Phase 2 — SVG 아이콘 / 스켈레톤 / 엠프티
1. ✅ GNB 이모지 → FontAwesome SVG 전환
2. ✅ 스켈레톤 로딩 (라이선스/GeoNURIS 카드 비동기)
3. ✅ 엠프티 스테이트 업그레이드 (아이콘 박스 + 친절 안내)
4. ✅ 로그인 페이지 재디자인 (Minimal Gradient Mesh)

### Phase 3 — 마이크로 인터랙션 / 다크모드 (실험)
1. ✅ 페이지 진입 fade-up 애니메이션
2. ✅ 버튼 press 피드백 (scale 0.97)
3. ✅ Smooth scroll
4. ✅ 다크모드 토글 (실험 · 메인 대시보드)
5. ✅ 라이선스 4페이지 전체 리디자인 (대장·업로드·GeoNURIS 대장·GeoNURIS 발급)
6. ✅ 업무계획·문서관리·성과통계 19개 파일 색상 일괄 스웁

### Phase 4 — 🚧 미착수
- 전체 CSS 변수 리팩터 (인라인 style 제거)
- 완전한 다크모드 구현

---

## 📁 주요 파일 목록

### 디자인 시스템 정의
- `src/main/resources/templates/fragments/top-nav.html` — 공용 GNB
- `src/main/resources/templates/main-dashboard.html` — 메인 대시보드 (참조 템플릿)
- `src/main/resources/templates/login.html` — 로그인 (Minimal Gradient Mesh)
- `src/main/resources/templates/mypage.html` — 마이페이지 (2x2 그리드)

### 관리 페이지 (통일 완료)
- project-list, project-form, project-detail
- person-list, person-form, person-detail
- license/registry-list, license/registry-upload, license/registry-detail
- geonuris/geonuris-list, geonuris/geonuris-form, geonuris/geonuris-edit, geonuris/geonuris-detail
- quotation/quotation-list, quotation-form, quotation-detail, quotation-ledger, pattern-list, remarks-pattern-list, wage-rate-list
- workplan/workplan-calendar, workplan-form, process-status
- document/document-list, document-detail, doc-* (8개 문서 타입), inspect-*
- performance/personal-dashboard, dept-dashboard, performance-report
- qrcode/qr-ledger, qr-issue, qr-detail
- admin-user-list, admin-logs, admin-system-graph, admin/org-unit-management
- infra-list, infra-form, infra-detail

---

## 🎯 레퍼런스 벤치마크

- **Linear** — Cool Minimal, 모노크롬 + 단일 액센트
- **Stripe** — 그라디언트 메시, 타이포 균형
- **Notion** — 조용한 데이터 테이블, 여백
- **Perplexity** — Deep Teal 톤 레퍼런스
- **Mapbox** — GIS 도메인 유사 제품
- **Arc Browser** — 웜 뉴트럴 베이스
- **Vercel** — 대담한 모노톤
- **Cal.com** — 깔끔한 관리자 UI

---

## 📝 변경 이력

| 일자 | 변경 내용 |
|------|----------|
| 2026-04-19 | Phase 0~3 대규모 디자인 개편 (보라 → Teal) |
| 2026-04-19 | 로그인 페이지 Minimal Gradient Mesh 적용 |
| 2026-04-19 | 라이선스 4페이지 전면 리디자인 |
| 2026-04-19 | workplan/document/performance 19개 파일 컬러 스웁 |
| TBD | **Phase 4**: CSS 변수 리팩터 + 완전 다크모드 |
