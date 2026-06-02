# 🎨 디자인팀 자문 — 수집모듈 다운로드 페이지 (inspect-agent-download)

- 자문일: 2026-06-02 (회사 PC) · 대상 기획서: `docs/product-specs/inspect-agent-download.md` v0.2
- 정책 근거: AGENTS.md §3-1 A+D(UI 키워드 → 디자인팀 자문 필수), §7 디자인 체크
- **판정: 조건부 승인** (아래 토큰·대비·specificity 조건 준수 시 구현 진행 가능)

---

## 1. 레이아웃 (형제 페이지 `ops-doc/list` 패턴 계승)

```
[fragments/top-nav]
┌ .page-header (카드) ────────────────────────────────────┐
│ 📦 점검 수집모듈 다운로드                 [← 목록으로]    │
└─────────────────────────────────────────────────────────┘
┌ .agent-card (카드) ─────────────────────────────────────┐
│ inspect-agent   [ v0.2.0 ] ← 버전 배지                   │
│ 한 줄 설명 (지자체 서버 점검 수집 PowerShell 모듈)        │
│ 빌드시각 · 파일 N개                                       │
│ SHA-256  <mono 텍스트>            [복사]                  │
│                         [ ⬇ 다운로드 (.zip) ] ← primary  │
└─────────────────────────────────────────────────────────┘
┌ .agent-guide (카드) ────────────────────────────────────┐
│ 설치/실행 요약 ① 압축해제 ② setup.ps1 ③ …                │
│                         [상세 가이드 →] (텍스트 링크)     │
└─────────────────────────────────────────────────────────┘
```
- `.container{max-width:900px}` (목록 1500 보다 좁게 — 단일 컬럼 콘텐츠).
- 카드 = `ops-doc/list` 의 `.list-section`/`.page-header` 와 동일 radius·shadow 계열.

## 2. 토큰 매핑 (raw hex 금지 — SoT = design-system.css)

| 요소 | 토큰 |
|---|---|
| 카드 배경 | **`var(--surface)`** (형제의 하드코딩 `white` 대신 — 다크 대비 무료 확보) |
| body 배경 | `var(--bg)` |
| 제목 | `var(--primary)` |
| 본문/보조 | `var(--text)` / **`var(--text2)`** |
| 버전 배지·다운로드 CTA | 배경 `var(--primary)` + 글씨 `#fff` |
| 카드 radius / shadow | `var(--r-card)` / `var(--shadow-card)` |
| 체크섬·버전 값 | `var(--font-mono)`, 배경 `var(--surface-2)` |
| 포커스 링 | `var(--ring)` |

→ **신규 토큰 불필요** — 기존 토큰으로 전부 커버. 따라서 페이지에서 `:root` 재정의 **금지**(self-reference 버그 원천 차단). 색이 더 필요하면 design-system.css `:root` 에 리터럴 hex로만 추가.

## 3. 접근성 (WCAG AA — 계산 결과)

| 조합 | 대비 | 판정 |
|---|---|---|
| CTA: `#fff` on `--primary` | 5.47:1 | ✅ |
| 제목/링크: `--primary` on surface | 5.47:1 | ✅ |
| 보조: `--text2` on surface | 7.63:1 | ✅ |
| 체크섬 mono: `--text` on `--surface-2` | 16.03:1 | ✅ |
| **힌트: `--muted` on surface** | **2.52:1** | ❌ **FAIL** |

- ⚠ **`--muted`(#A8A29E)는 의미 있는 텍스트에 사용 금지** (2.52:1, large 기준도 미달). 라벨·보조정보는 **`--text2`**(7.63:1) 사용. `--muted` 는 비필수 장식/구분선 한정.
- 다운로드 버튼: 아이콘+텍스트 라벨 동시 제공(아이콘 단독 금지). 키보드 포커스 시 `var(--ring)` 가시화.
- "복사" 버튼: `aria-label` + 복사 완료 토스트/텍스트 피드백.

## 4. 다크모드 일관성

- 형제 `ops-doc/list` 는 카드에 `background:white` 하드코딩 → 다크 미대응. **본 페이지는 처음부터 `var(--surface)`/`var(--text)` 등 시맨틱 토큰만 사용** → 라이트 동일 외관 + 다크 자동 대응.
- 단, NFR-4(기존 다크 대응 범위 내) 정책상 **이 페이지 전용 `[data-theme="dark"]` 오버라이드 블록은 추가하지 않는다** — 토큰이 해결. (전면 다크 리팩터는 별도 sprint)

## 5. specificity / 구조 안전

- 모든 스타일은 **페이지 컨테이너 클래스 하위 단일 클래스 selector** 로 한정 (`.agent-card`, `.agent-guide`). bare 요소 selector(`table`,`th` 등) 전역 누출 금지 — 형제 list.html 의 bare `table{}` 전역 정의가 이미 있으므로 본 페이지는 클래스 스코프 유지.
- `!important` 미사용. specificity 매트릭스 단일 레벨 유지(2026-06-01 main-dashboard 충돌 사례 학습).

## 6. 아이콘 / 폰트 (Font Awesome 6.4 기존)

- 페이지 제목 `fa-box` / `fa-cube`, 다운로드 `fa-download`, 체크섬·무결성 `fa-shield-halved`, 복사 `fa-copy`, 가이드 링크 `fa-arrow-right`.
- 폰트 = `var(--font)` (Inter+Noto Sans KR), 코드값 `var(--font-mono)`.

## 7. 자문 결론 / 기획서 반영 요청

- **조건부 승인** — 위 §2 토큰·§3 `--muted` 금지·§4 다크 방침·§5 specificity 준수 시 진행.
- 기획서 NFR-3 에 "**`--muted` 의미텍스트 금지 / `--text2` 사용**", "**페이지 :root 재정의 금지**", "**시맨틱 토큰으로 다크 자동대응(전용 dark 블록 미추가)**" 명시 요청.
- 신규 공용 컴포넌트 추출은 불필요(페이지 1개 한정). 카드/배지/버튼은 기존 패턴 인라인 재사용.
