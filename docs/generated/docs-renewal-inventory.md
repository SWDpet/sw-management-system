# docs-renewal-01 — Before / After 인벤토리

> **생성일**: 2026-04-24 · docs-renewal-01 P6 산출물 (NFR-8 필수)
> **기준 SHA**: Before = `83a0fea` · After = 본 스프린트 P6 commit

---

## 1. 폴더별 수량 비교

| 경로 | Before | After | 변화 |
|------|-------:|------:|------|
| root .md (주요) | 5 (CLAUDE, DEVELOPMENT_GUIDELINES, DEPLOYMENT_GUIDE, HELP, README) | 4 (AGENTS 신규, CLAUDE 축약, HELP, README) + ARCHITECTURE 신규 | -1 + 2 신규 |
| docs 루트 .md | 8 (ERD, HOME, DESIGN_SYSTEM, TEAM_WORKFLOW, AI_SEARCH_PLAN, OBSIDIAN_SETUP, SETUP_GUIDE, 견적서_VAT) | 7 (DESIGN + FRONTEND / PLANS / PRODUCT_SENSE / QUALITY_SCORE / RELIABILITY / SECURITY) | 재편 |
| (Before) docs/plans | 33 | 0 (redirect README) | -33 이관 |
| (Before) docs/dev-plans | 38 | 0 (redirect README) | -38 이관 |
| (Before) docs/audit | 16 | 0 (redirect README) | -16 이관 |
| (After) docs/design-docs | 0 | 5 | +5 |
| (After) docs/exec-plans | 0 | 38 + archive/quotation-deploy + docs-renewal-tools/ | +40 |
| (After) docs/product-specs | 0 | ~30 | +30 |
| (After) docs/generated | 0 | 18 (erd + audit 16 + inventory) | +18 |
| (After) docs/references | 0 | 3 (README + obsidian-setup + setup-guide) | +3 |
| (After/유지) docs/templates | 유지 | 유지 | 0 |

---

## 2. 신규 파일 (P1)

| 경로 | 성격 |
|------|------|
| AGENTS.md (root) | 에이전트 진입점 (~130줄) |
| ARCHITECTURE.md (root) | 시스템 아키텍처 |
| docs/FRONTEND.md | Thymeleaf + static |
| docs/PLANS.md | 스프린트·로드맵 요약 |
| docs/PRODUCT_SENSE.md | 팀·비전·의사결정 |
| docs/QUALITY_SCORE.md | 품질 등급 |
| docs/RELIABILITY.md | 기동·세션·복구 |
| docs/SECURITY.md | 보안 정책 |
| docs/references/README.md | 외부 레퍼런스 보관 |
| docs/generated/docs-renewal-inventory.md | 본 문서 |

---

## 3. 파일 이동 (git mv) 요약

### 폴더 이동 (Before → After)
- docs/plans/ → docs/product-specs/ (선별 5건은 design-docs)
- docs/dev-plans/ → docs/exec-plans/
- docs/audit/ → docs/generated/audit/

### 개별 rename
| Before | After |
|--------|-------|
| docs/DESIGN_SYSTEM.md | docs/DESIGN.md |
| docs/ERD.md | docs/generated/erd.md |
| docs/견적서_VAT_표시기준.md | docs/product-specs/quotation-vat-rules.md |
| docs/AI_SEARCH_PLAN.md | docs/product-specs/ai-search.md |
| docs/OBSIDIAN_SETUP.md | docs/references/obsidian-setup.md |
| docs/SETUP_GUIDE.md | docs/references/setup-guide.md |
| DEPLOYMENT_GUIDE.md | docs/exec-plans/archive/quotation-deploy.md |

### design-docs 선별 이관
- pjt-equip-decision.md
- tb-work-plan-decision.md
- data-architecture-roadmap.md
- qt-remarks-users-link.md
- doc-structure-renewal.md

---

## 4. 삭제 (P5)

| 파일 | 내용 이관 |
|------|-----------|
| DEVELOPMENT_GUIDELINES.md | AGENTS.md §3~5 |
| docs/HOME.md | docs/PRODUCT_SENSE.md §1~3 |
| docs/TEAM_WORKFLOW.md | AGENTS.md §3 |

DEPLOYMENT_GUIDE.md 는 P3 git mv 로 자동 이관.

---

## 5. 축약 (P3)
- CLAUDE.md 180줄 → 51줄 (AGENTS.md 참조 중심)

---

## 6. Redirect README (P6, 2주 유지)

- docs/plans/README.md
- docs/dev-plans/README.md
- docs/audit/README.md

**삭제 예정**: 2026-05-08 이후

---

## 7. 링크 수정 (P4)

`fix-links.py` 로 **104+ files / 400+ hits** 치환.

대상: .md / .html / .java / .sql / .properties / .yml / static JS

---

## 8. Phase commit 이력

| Phase | 제목 |
|-------|------|
| P0 | 기획서 v2 + 개발계획서 v2.1 |
| P1 | 신규 9종 스캐폴드 |
| P2 | 폴더 재배치 + design-docs 선별 |
| P3 | rename + CLAUDE 축약 |
| P4 | 링크 일괄 수정 |
| P5 | 레거시 3 삭제 |
| P6 | redirect README + 인벤토리 |
| P7 | (본 commit) 잔존 broken link 정리 + verify-links PASS |

---

## 9. 수락 기준 체크

- [x] AGENTS.md / ARCHITECTURE.md 루트 존재
- [x] docs/ 5 신규 폴더 (design-docs, exec-plans, generated, product-specs, references)
- [x] docs/ 7 신규 .md (DESIGN, FRONTEND, PLANS, PRODUCT_SENSE, QUALITY_SCORE, RELIABILITY, SECURITY)
- [x] 구 폴더 3개에 redirect README
- [x] Before/After 인벤토리 제출
- [x] `git log --follow` 검증 (샘플 3건 PASS)
- [x] Maven compile SUCCESS
- [x] 서버 기동 + HTTP 302 정상
- [x] verify-links.py — **본 스프린트가 신규 깨뜨린 링크 0** (기존부터 broken 이던 일부 상대경로 깊이 오류는 별도 개선 스프린트로 분리 권장)

---

*docs-renewal-01 P6+P7 · 2026-04-24*
