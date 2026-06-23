# FRONTEND.md — 프론트엔드 구조

> ✅ **2026-06-23 코드 대조 검증** — 정적 리소스 구조·fragments·hwpx·다크모드 현황을 실제 `templates/`·`static/` 트리와 대조해 정정.

---

## 1. 기술 스택

- **템플릿 엔진**: Thymeleaf 3 + thymeleaf-extras-springsecurity6
- **정적 리소스**: `src/main/resources/static/`
  - `css/` — `design-system.css`(전역 토큰·사이드바·반응형) + `ops-doc.css`(장애·지원 화면). (그 외 `preview-*.html` 은 디자인 검토 하네스)
  - `js/` — 페이지별 JS 8종 (org-unit-selector, document-project-selector, document-region-selector, ops-doc-relations, ops-doc-cascade, admin-user 등)
  - `images/`, `favicon.ico/png` (별도 `fonts/` 디렉터리 없음 — 웹폰트는 CDN)
- **CSS 프레임워크**: 자체 CSS 변수(디자인 토큰) 시스템 (`docs/DESIGN.md` 참조). 외부 프레임워크 미사용
- **아이콘/폰트**: FontAwesome + Google Fonts (CDN, `fragments/top-nav.html` 로드)

---

## 2. 템플릿 디렉터리

```
src/main/resources/templates/
├── document/            # 문서관리 (inspect-*, doc-*, document-*)
├── quotation/           # 견적서 (quotation-*, wage-*, pattern-*)
├── infra*.html          # 인프라 관리
├── project-*.html       # 사업관리
├── workplan-*.html      # 업무계획
├── user/, admin*        # 사용자·관리자
├── login.html, signup.html, mypage.html
├── fragments/           # 공통 조각 2종: top-nav.html(사이드바 GNB), ops-cascade.html(지역 cascade)
├── pdf/                 # PDF 출력용 HTML (OpenHTMLToPDF)
└── hwpx/                # HWPX 바이너리 원본(.hwpx ZIP) — HwpxExportService 가 ZIP+XML 직접 치환(Apache POI 미사용). HTML 템플릿 아님
```

> 네비게이션은 수평 GNB→**전역 사이드바**(`global-sidebar-responsive`)로 전환됨. 공통 조각은 `top-nav.html`(이름과 달리 사이드바) 하나로 통합, 별도 footer/header/modal 조각 없음.

---

## 3. 인라인 스타일 현황

Thymeleaf 템플릿의 `style="..."` 인라인 스타일·페이지내 `<style>` 블록이 여전히 다수(예: `main-dashboard.html` 의 `<style>` 블록에 raw hex `#0F766E` 등 토큰과 혼재). 전면 토큰화는 미완(점진 이관 대상).

검사 명령:
```bash
rg -c 'style="' src/main/resources/templates/ | head
```

대응 방향: 신규/변경 UI 는 디자인 토큰(`var(--...)`)만 사용(디자인 자문 정책), 기존 raw hex 는 점진 이관.

### 다크모드 현황
`design-system.css` 에 `[data-theme="dark"]` 토큰 정의는 존재하나 **사용자 토글 버튼·`localStorage` 활성화 메커니즘은 미구현**(현재 라이트 모드만 실사용). 신규 UI 는 라이트 토큰 기준(사용자 결정 "다크모드 비대상").

---

## 4. JS 규칙

- **모듈 시스템 없음** — 각 페이지별 `<script>` 블록 or `static/js/*.js` 직접 포함
- **공통 JS**: `org-unit-selector.js`, `document-project-selector.js`, `document-region-selector.js`, `ops-doc-relations.js`, `ops-doc-cascade.js`, `admin-user.js` 등
- **CSRF 헤더**: `<meta name="_csrf">` 태그 + `fetch` 요청 헤더 수동 부착

---

## 5. 참조

- 디자인 시스템: `docs/DESIGN.md`
- 보안 (CSRF 등): `docs/SECURITY.md`
- 주요 스프린트: `docs/product-specs/`, `docs/exec-plans/`

---

*Last updated: 2026-06-23 · 코드 대조 검증(문서 A 승급): 정적 구조·fragments·hwpx·사이드바·다크모드 현황 정정*
