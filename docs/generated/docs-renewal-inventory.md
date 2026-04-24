# docs-renewal-01 — Before / After 인벤토리

> **생성일**: 2026-04-24 · docs-renewal-01 P6 산출물 (NFR-8 필수)
> **기준 SHA**: Before = `83a0fea2845db73dd464c7d5836a9c086e30308c`, After = P6 commit

---

## 1. 폴더별 수량 비교

| 경로 | Before (2026-04-24 BASE) | After (P6) | 변화 |
|------|-------------------------:|-----------:|------|
| root `*.md` | 5 | 4 (AGENTS 신규 + CLAUDE 축약 + HELP + README, DEVELOPMENT_GUIDELINES/DEPLOYMENT_GUIDE 제거) | -1 |
| root `ARCHITECTURE.md` | 0 | 1 | +1 신규 |
| `docs/*.md` 직속 | 8 (ERD, HOME, DESIGN_SYSTEM, TEAM_WORKFLOW, AI_SEARCH_PLAN, OBSIDIAN_SETUP, SETUP_GUIDE, 견적서_VAT) | 7 (DESIGN 외 6종 신규) | 재편 |
| `docs/plans/` | 33 | 0 (redirect README 만) | -33 이관 |
| `docs/dev-plans/` | 37 | 0 (redirect README 만, P0 commit 추가로 38 → 이관 시) | -38 이관 |
| `docs/audit/` | 16 | 0 (redirect README 만) | -16 이관 |
| `docs/design-docs/` | 0 | 5 (pjt-equip-decision, tb-work-plan-decision, data-architecture-roadmap, qt-remarks-users-link, doc-structure-renewal) | +5 |
| `docs/exec-plans/` | 0 | 38 + archive/quotation-deploy + docs-renewal-tools/ | +40 |
| `docs/product-specs/` | 0 | 33 - 5 design-docs 선별 + 4 리네임(quotation-vat-rules, ai-search) = ~30 | +30 |
| `docs/generated/` | 0 | 18 (erd.md + audit 16 + docs-renewal-inventory.md) | +18 |
| `docs/references/` | 0 | 3 (README, obsidian-setup, setup-guide) | +3 |
| `docs/templates/` | 유지 | 유지 | 0 |

---

## 2. 신규 파일 (P1)

| 경로 | 성격 |
|------|------|
| `AGENTS.md` | 루트 에이전트 진입점 (~130줄, DEVELOPMENT_GUIDELINES + CLAUDE 워크플로우 통합) |
| `ARCHITECTURE.md` | 루트 시스템 아키텍처 (pom.xml + 패키지 트리 + ERD 요약) |
| `docs/FRONTEND.md` | Thymeleaf + static 구조 |
| `docs/PLANS.md` | 스프린트·로드맵 요약 |
| `docs/PRODUCT_SENSE.md` | 팀·비전·의사결정 히스토리 (HOME 통합) |
| `docs/QUALITY_SCORE.md` | 패키지별 품질 등급 |
| `docs/RELIABILITY.md` | 기동·세션·복구 정책 |
| `docs/SECURITY.md` | 보안 정책·감사 P1 |
| `docs/references/README.md` | 외부 레퍼런스 보관 용도 설명 |
| `docs/generated/docs-renewal-inventory.md` | 본 문서 (Before/After 인벤토리, NFR-8) |

---

## 3. 파일 이동 (git mv)

### 폴더 이동
- `docs/plans/` → `docs/product-specs/` (33) → design-docs 선별 후 잔여 ~28
- `docs/dev-plans/` → `docs/exec-plans/` (38, P0 커밋으로 +1)
- `docs/audit/` → `docs/generated/audit/` (16)

### 개별 rename
| From | To |
|------|----|
| `docs/DESIGN_SYSTEM.md` | `docs/DESIGN.md` |
| `docs/ERD.md` | `docs/generated/erd.md` |
| `docs/견적서_VAT_표시기준.md` | `docs/product-specs/quotation-vat-rules.md` |
| `docs/AI_SEARCH_PLAN.md` | `docs/product-specs/ai-search.md` |
| `docs/OBSIDIAN_SETUP.md` | `docs/references/obsidian-setup.md` |
| `docs/SETUP_GUIDE.md` | `docs/references/setup-guide.md` |
| `DEPLOYMENT_GUIDE.md` | `docs/exec-plans/archive/quotation-deploy.md` |

### design-docs 선별 이관
- `docs/product-specs/pjt-equip-decision.md` → `docs/design-docs/pjt-equip-decision.md`
- `docs/product-specs/tb-work-plan-decision.md` → `docs/design-docs/tb-work-plan-decision.md`
- `docs/product-specs/data-architecture-roadmap.md` → `docs/design-docs/data-architecture-roadmap.md`
- `docs/product-specs/qt-remarks-users-link.md` → `docs/design-docs/qt-remarks-users-link.md`
- `docs/product-specs/doc-structure-renewal.md` → `docs/design-docs/doc-structure-renewal.md`

---

## 4. 삭제 (P5)

| 파일 | 내용 이관 |
|------|-----------|
| `DEVELOPMENT_GUIDELINES.md` | AGENTS.md §3~5 |
| `docs/HOME.md` | docs/PRODUCT_SENSE.md §1~3 |
| `docs/TEAM_WORKFLOW.md` | AGENTS.md §3 |

(DEPLOYMENT_GUIDE.md 는 P3 git mv 로 이관 + 원본 자동 삭제)

---

## 5. 축약 (P3)

- `CLAUDE.md` 180줄 → 51줄 (AGENTS.md 참조 중심, Claude Code 자동 로드 목적)

---

## 6. Redirect README (P6, 2주 유지)

- `docs/plans/README.md` — 이동 안내
- `docs/dev-plans/README.md` — 이동 안내
- `docs/audit/README.md` — 이동 안내

**삭제 예정**: 2026-05-08 이후

---

## 7. 링크 수정 (P4)

`fix-links.py` 로 일괄 처리:
- 1차 실행: **82 files / 365 hits**
- 2차 실행: **22 files / 34 hits** (docs/exec-plans + docs/generated/audit 내 .java + docs/erd-descriptions.yml)
- 누적: **104 files / 399 hits**

대상 확장자: `.md / .html / .java / .sql / .properties / .yml / .js`

### 잔존 (의도된)
- `docs/design-docs/doc-structure-renewal.md` §4-4, §5 — 본 기획서 내부 설명 텍스트 (링크 아닌 역사 기록)

---

## 8. Phase commit 이력

| Phase | SHA | 제목 |
|-------|-----|------|
| P0 | `46e08aa` | 기획서 v2 + 개발계획서 v2.1 |
| P1 | `b02a932` | 신규 9종 스캐폴드 |
| P2 | `2b579ae` | 폴더 재배치 + design-docs 선별 |
| P3 | `c3fb6d0` | rename + CLAUDE 축약 |
| P4 | `3841cef` | 링크 일괄 수정 |
| P5 | `97e88a8` | 레거시 3 삭제 |
| P6 | (본 커밋) | redirect README + 인벤토리 |

---

## 9. 수락 기준 체크

- [x] AGENTS.md / ARCHITECTURE.md 루트 존재
- [x] docs/ 하위 5 신규 폴더 존재 (design-docs, exec-plans, generated, product-specs, references)
- [x] docs/ 루트 7 신규 .md 존재 (DESIGN, FRONTEND, PLANS, PRODUCT_SENSE, QUALITY_SCORE, RELIABILITY, SECURITY)
- [x] docs/plans / docs/dev-plans / docs/audit 폴더에 redirect README 만
- [x] Before/After 인벤토리 제출 (본 문서)
- [ ] `git log --follow` 검증 (Step 8)
- [ ] `verify-links.py` 검증 (Step 8)
- [ ] Maven compile (Step 8)
- [ ] 서버 기동 + /system-graph (Step 8)

---

*docs-renewal-01 P6 · 2026-04-24*
