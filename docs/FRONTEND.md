# FRONTEND.md — 프론트엔드 구조

> ⚠ **자동 생성 초안 — 검증 필요**
> 근거: `src/main/resources/templates/` 트리 + `static/` 자산 + 인라인 스타일 통계

---

## 1. 기술 스택

- **템플릿 엔진**: Thymeleaf 3 + thymeleaf-extras-springsecurity6
- **정적 리소스**: `src/main/resources/static/`
  - `css/` — 전역/페이지별 CSS
  - `js/` — 페이지별 JS (org-unit-selector, document-project-selector 등)
  - `img/`, `fonts/`
- **CSS 프레임워크**: 자체 CSS 변수 시스템 (`docs/DESIGN.md` 참조). 외부 프레임워크 미사용
- **아이콘**: FontAwesome (CDN)

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
├── fragments/           # 공통 레이아웃 조각
├── pdf/                 # PDF 출력용 (OpenHTMLToPDF)
└── hwpx/                # HWPX 출력 템플릿 (Apache POI 미사용, ZIP+XML 직접)
```

---

## 3. 인라인 스타일 현황

Thymeleaf 템플릿의 `style="..."` 사용량은 아직 높음. Phase 4 대기 중(`docs/DESIGN.md` 참조).

검사 명령:
```bash
rg -c 'style="' src/main/resources/templates/ | head
```

대응 계획: CSS 변수(`var(--...)`)로 점진적 이관.

---

## 4. JS 규칙

- **모듈 시스템 없음** — 각 페이지별 `<script>` 블록 or `static/js/*.js` 직접 포함
- **공통 JS**: `org-unit-selector.js`, `document-project-selector.js`, `document-region-selector.js`, `admin-user.js` 등
- **CSRF 헤더**: `<meta name="_csrf">` 태그 + `fetch` 요청 헤더 수동 부착

---

## 5. 참조

- 디자인 시스템: `docs/DESIGN.md`
- 보안 (CSRF 등): `docs/SECURITY.md`
- 주요 스프린트: `docs/product-specs/`, `docs/exec-plans/`

---

*Last updated: 2026-04-24 · docs-renewal-01 P1 자동 생성 초안*
