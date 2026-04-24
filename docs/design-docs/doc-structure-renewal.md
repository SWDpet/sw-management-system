---
tags: [plan, docs, restructure]
sprint: "docs-renewal-01"
status: draft
created: "2026-04-24"
updated: "2026-04-24"
---

# [기획서] 문서 구조 전면 리뉴얼 (AGENTS/ARCHITECTURE 중심 SSoT 체계)

- **작성팀**: 기획팀
- **작성일**: 2026-04-24
- **상태**: v2 (codex v1 ❌ 반려 12건 반영)
- **v1→v2 변경점**:
  1. 실측 인벤토리 전면 갱신 (33 / 37 / 16 / 96 / 106)
  2. "전수" 범위 명문화 (root + docs, 제외 디렉터리 명시)
  3. `*-decision.md` 실측 5개 재산정 (plans 2, dev-plans 3)
  4. design-docs 선별을 속성 기반 규칙표로 전환
  5. 링크 수정 범위를 Java/resources/JS/SQL 주석까지 확장 + 실측 근거
  6. `application.properties`/`@Value` 경로 화이트리스트 표 추가
  7. 커밋 전략 단일화: **단일 브랜치 + Phase별 원자 commit** (NFR-7 문구 재작성)
  8. 링크 검증 강화: Markdown + HTML `href/src` + 파일 존재 + 앵커
  9. acceptance 에서 "codex 승인" 을 보조지표로 강등, 객관 스크립트 게이트를 주로 승격
  10. redirect README 유지 기간/삭제 시점 acceptance 포함
  11. Before/After 인벤토리 표 필수 산출물 추가

---

## 1. 배경 / 목표

### 1-1. 배경 (2026-04-24 실측)

| 항목 | 수량 |
|------|-----:|
| `docs/product-specs/*.md` | **33** |
| `docs/exec-plans/*.md` | **37** |
| `docs/generated/audit/*.md` | **16** |
| `docs/**/*.md` (전체) | **96** |
| 리포지토리 전체 `.md` | **106** |
| 루트 `.md` | **5** (`CLAUDE.md`, `docs/exec-plans/archive/quotation-deploy.md`, `AGENTS.md`, `HELP.md`, `README.md`) |
| `*-decision.md` (docs/ 전체) | **5** — plans 2 (pjt-equip / tb-work-plan), dev-plans 3 (pjt-equip / s8-hwpx-literal / tb-work-plan) |

### 1-2. 범위 정의 ("전수"의 명문화)

| 구분 | 포함 여부 |
|------|----------|
| `/` (repo root .md) | ✅ 포함 |
| `docs/**` | ✅ 포함 |
| `src/**`, `swdept/**`, `server-restart.sh` 등 비-.md | ❌ 제외 (링크 수정 대상만 별도 §2-5) |
| `.git`, `target`, `node_modules`, `.idea`, `.vscode`, `docs/.obsidian` | ❌ 제외 (무시) |
| `docs/templates/` | ✅ 포함 (유지 대상) |

### 1-3. 목표

- **G1**: `AGENTS.md` 단일 진입점 → 전체 문서 맵 이해 가능
- **G2**: 모든 문서가 목표 구조에 정확히 매핑 + Before/After 표 제공
- **G3**: 내부 참조 링크(Markdown inline/reference + HTML `href/src` + 앵커) 깨짐 **0건** (자동 검증)
- **G4**: git 히스토리 보존 (`git mv` 강제)
- **G5**: 신규 문서 6종 (`ARCHITECTURE`, `FRONTEND`, `PRODUCT_SENSE`, `QUALITY_SCORE`, `RELIABILITY`, `SECURITY`) 은 코드 스캔 + 기존 문서 통합으로 초안 생성. "자동 생성 초안 — 검증 필요" 배너 포함

---

## 2. 기능 요건 (FR)

### 2-1. 최상위 구조 (루트)

| ID | 내용 |
|----|------|
| FR-1 | 루트에 `AGENTS.md` 신규. `AGENTS.md` 내용 + `CLAUDE.md` 워크플로우 요약 통합 |
| FR-2 | 루트에 `ARCHITECTURE.md` 신규. pom.xml + 패키지 트리 + ERD 요약 자동 |
| FR-3 | `CLAUDE.md` 유지하되 AGENTS.md 참조로 축약 (Claude Code 자동 로드 기능 보존) |
| FR-4 | `README.md`, `HELP.md` 유지 |
| FR-5 | `AGENTS.md` → AGENTS.md 통합 후 삭제 |
| FR-6 | `docs/exec-plans/archive/quotation-deploy.md` → `docs/exec-plans/archive/quotation-deploy.md` 이관 (git mv + rename) |

### 2-2. `docs/` 루트 파일

| ID | 내용 |
|----|------|
| FR-7 | `docs/DESIGN.md` → `docs/DESIGN.md` rename |
| FR-8 | `docs/FRONTEND.md` 신규 (templates 트리 + static + 인라인 스타일 통계) |
| FR-9 | `docs/PLANS.md` 신규 (로드맵 요약) |
| FR-10 | `docs/PRODUCT_SENSE.md` 신규 (팀 구성 + 비전 + 의사결정 히스토리) |
| FR-11 | `docs/QUALITY_SCORE.md` 신규 (audit 요약 + 패키지별 등급) |
| FR-12 | `docs/RELIABILITY.md` 신규 (기동/세션/락 정책) |
| FR-13 | `docs/SECURITY.md` 신규 (SecurityConfig + 감사 P1 조치) |
| FR-14 | 나머지 `docs/*.md` 재배치 (§2-4 매핑표) |

### 2-3. `docs/` 하위 폴더 (§2-4 매핑표 참조)

| ID | 내용 |
|----|------|
| FR-15 | `docs/design-docs/` 신규 — 속성 기반 선별 이관 (§2-4-A 규칙) |
| FR-16 | `docs/exec-plans/` 신규 — `docs/exec-plans/` 37개 전체 이관 (git mv) |
| FR-17 | `docs/exec-plans/archive/` 신규 — `docs/exec-plans/archive/quotation-deploy.md` 이관 |
| FR-18 | `docs/product-specs/` 신규 — `docs/product-specs/` 나머지(=31개) 이관 |
| FR-19 | `docs/generated/` 신규 |
| FR-20 | `docs/generated/audit/` 신규 — `docs/generated/audit/` 16개 전체 이관 |
| FR-21 | `docs/references/` 신규 + README 설명 (빈 폴더 방지) |
| FR-22 | `docs/templates/` 그대로 유지 |

### 2-4-A. design-docs 선별 규칙 (속성 기반)

**이관 대상 조건** (AND/OR):

| 속성 | 판정 |
|------|------|
| 파일명 suffix `-decision.md` | **✅ 자동 대상** (plans 2개: `pjt-equip-decision`, `tb-work-plan-decision`) |
| frontmatter `status: approved` or `status: final` | ✅ 대상 |
| 본문에 "사용자 최종승인 완료" + "구현 완료" 동시 기재 | ✅ 대상 |
| 로드맵 성격 (`data-architecture-roadmap.md`) | ✅ 대상 (아키텍처 결정 집약) |
| `dev-plans/` 내 `-decision.md` (3개) | **❌ 제외** — 개발계획은 실행 문서이므로 `exec-plans/` 잔류 (선별 제외 규칙) |

**예상 이관 목록** (개발계획 단계 Step 1 Precheck 에서 실측 재확정):
1. `docs/design-docs/pjt-equip-decision.md`
2. `docs/design-docs/tb-work-plan-decision.md`
3. `docs/design-docs/data-architecture-roadmap.md`
4. 기타 status/본문 조건으로 자동 감지 (개발계획에서 grep 스크립트로 확정)

### 2-4-B. 개별 파일 매핑표 (Before → After)

| 현재 경로 | 목표 경로 | 방식 |
|-----------|-----------|------|
| `CLAUDE.md` | `CLAUDE.md` (축약) + `AGENTS.md` 신규 | 축약 + 신규 |
| `AGENTS.md` | `AGENTS.md` 통합 | 삭제 (통합 후) |
| `docs/exec-plans/archive/quotation-deploy.md` | `docs/exec-plans/archive/quotation-deploy.md` | git mv + rename |
| `README.md`, `HELP.md` | 유지 | 유지 |
| `docs/product-specs/ai-search.md` | `docs/product-specs/ai-search.md` | git mv + rename |
| `docs/DESIGN.md` | `docs/DESIGN.md` | git mv |
| `docs/generated/erd.md` | `docs/generated/erd.md` | git mv |
| `docs/PRODUCT_SENSE.md` | `docs/PRODUCT_SENSE.md` 통합 | 삭제 |
| `docs/references/obsidian-setup.md` | `docs/references/obsidian-setup.md` | git mv |
| `docs/references/setup-guide.md` | `docs/references/setup-guide.md` | git mv |
| `AGENTS.md` | `AGENTS.md` 통합 | 삭제 |
| `docs/product-specs/quotation-vat-rules.md` | `docs/product-specs/quotation-vat-rules.md` | git mv + rename (한→영) |
| `docs/generated/audit/*.md` (16개) | `docs/generated/audit/*.md` | git mv (폴더 이동) |
| `docs/product-specs/*` 중 design-docs 선별 대상 (≥3개) | `docs/design-docs/` | git mv (선별) |
| `docs/product-specs/*` 나머지 (~30개) | `docs/product-specs/*.md` | git mv (일괄) |
| `docs/exec-plans/*.md` (37개) | `docs/exec-plans/*.md` | git mv (폴더 이동) |
| `docs/templates/` | 유지 | — |

### 2-5. 링크·참조 수정 범위 (v2 확장)

**코드 내 `docs/` 참조 실측 (2026-04-24 codex 스캔)**:

| 파일 | 라인 | 참조 | 조치 |
|------|-----:|------|------|
| `src/main/resources/application.properties` | 26 | `app.erd.mmd-dir=docs` | ⚠ 키별 화이트리스트 표 §2-5-B 참조 |
| `src/main/java/com/swmanager/system/service/ErdGraphService.java` | 33, 34 | `docs/generated/erd.md` 유사 경로 | ✅ 수정 필요 → `docs/generated/erd.md` |
| `src/main/resources/db_init_phase2.sql` | 431 | docs 경로 주석 참조 | ✅ 주석 업데이트 |
| `src/main/resources/static/js/admin-user.js` | 4 | docs 참조 | ✅ 수정 or 주석 제거 |

| ID | 내용 |
|----|------|
| FR-23 | 모든 `.md` 파일 내 Markdown 링크(`[text](./path)`, `[text](path)`, reference style) 신규 경로로 일괄 수정 |
| FR-24 | `src/main/java`, `src/main/resources`, `src/main/resources/static`, `swdept/sql` 내 `docs/` 문자열 참조 전수 스캔·수정 |
| FR-25 | `CLAUDE.md` §5 "주요 문서" 링크 5개 갱신 |
| FR-26 | `.mmd` 로드 경로 영향 확인 (`app.erd.mmd-dir` 은 mmd 파일용이라 ERD.md 이관과 별개) |

### 2-5-B. application.properties / @Value 경로 화이트리스트

| Key | 현재 값 | 조치 | 사유 |
|-----|---------|------|------|
| `app.erd.mmd-dir` | `docs` | **유지** | `.mmd` 파일 디렉터리이지 ERD.md 경로 아님 |
| `app.erd.descriptions-file` | `docs/erd-descriptions.yml` | **유지** | yml 파일 경로, 이관 대상 아님 |
| (기타 `@Value("docs/...")`) | Step 1 precheck 에서 `rg '@Value\("\$\{?docs' src/main/java` 전수 | 필요 시 갱신 or 유지 판단 | 개별 판정 |

---

## 3. 비기능 요건 (NFR)

| ID | 내용 |
|----|------|
| NFR-1 | Maven `./mvnw clean compile` 성공 |
| NFR-2 | 서버 기동 후 ERD 탭/`/system-graph` 정상 동작 |
| NFR-3 | `git log --follow docs/exec-plans/audit-fix-p1.md` 등으로 이관 전 히스토리 추적 가능 |
| NFR-4 | **링크 검증 스크립트 PASS** — Markdown inline/reference + HTML `href/src` + 파일 존재 + `#anchor` 검증 |
| NFR-5 | 신규 문서 6종 각 **최소 섹션 3개 + 200자 이상** (빈 파일 금지) |
| NFR-6 | `AGENTS.md` 100줄±20 (진입점 간결성) |
| NFR-7 | **단일 브랜치 + Phase별 원자 commit** — 각 commit 이 독립적으로 revert 가능. Phase 분리 기준: ①신규 스캐폴드 ②폴더 mv ③파일 rename ④링크 수정 ⑤레거시 삭제 (~5 commits). "revert 1회" 표현 삭제 |
| NFR-8 | Before/After 인벤토리 표 최종 산출물로 제출 (파일수 + 경로 + 이동/삭제/신규 분류) |

---

## 4. 의사결정

### 4-1. `CLAUDE.md` 루트 유지 ✅
- Claude Code 자동 로드 기능 보존. `AGENTS.md` 는 모든 에이전트 공통 진입점.

### 4-2. 한글 파일명 → 영문 ✅
- `견적서_VAT_표시기준.md` → `quotation-vat-rules.md`.

### 4-3. `docs/references/` 빈 폴더 방지 ✅
- `docs/references/README.md` 로 용도 명시.

### 4-4. 이관 순서 (커밋 전략 단일화, v2 변경) ✅
**Phase별 원자 commit** — 단일 브랜치, 각 Phase 가 독립 revert 가능:

| Phase | 커밋 단위 | 혼란 최소화 전략 |
|-------|-----------|------------------|
| P1 | 신규 파일 스캐폴드 (AGENTS/ARCHITECTURE/docs 신규 6종 + references/README) | 레거시 유지, 신규만 추가 → revert 안전 |
| P2 | 폴더 git mv (audit/dev-plans/plans 이동) | 이 시점엔 링크 깨짐 발생. **단, P3 와 하루 이내 순차** |
| P3 | 내부 링크 수정 (Markdown + HTML + 코드 문자열) | 링크 검증 스크립트 PASS 조건 |
| P4 | 파일 rename (DESIGN_SYSTEM, 한글→영문 등) + CLAUDE.md 축약 | rename 영향 범위 작음 |
| P5 | 레거시 삭제 (DEVELOPMENT_GUIDELINES, HOME, TEAM_WORKFLOW, DEPLOYMENT_GUIDE 원본) | 통합 완료 확인 후 |
| P6 | redirect README 배치 (`docs/product-specs/README.md` = "→ product-specs/") **+ 유지 기간 명시 (2주)** | acceptance NFR-8 |

### 4-5. redirect README 정책 (v2 신규)
- `docs/product-specs/README.md`, `docs/exec-plans/README.md`, `docs/generated/audit/README.md` 에 "이 폴더는 X로 이동했습니다" 안내
- **유지 기간**: 리뉴얼 commit 이후 **2주** (2026-05-08)
- 이후 **자동 삭제 예약** → acceptance 에 포함

---

## 5. 영향 범위 (Before/After 인벤토리 요약)

| 분류 | Before | After | 변화 |
|------|-------:|------:|------|
| 루트 .md | 5 | 4 (AGENTS 신규, DEVELOPMENT_GUIDELINES/DEPLOYMENT_GUIDE 제거) + ARCHITECTURE 신규 | +1 신규 / -2 |
| docs/ 직속 .md | 8 | 8 (DESIGN rename, FRONTEND/PLANS/PRODUCT_SENSE/QUALITY_SCORE/RELIABILITY/SECURITY 신규, ERD/HOME/AI_SEARCH/OBSIDIAN/SETUP/TEAM_WORKFLOW/견적서_VAT 이관) | 순 증감 0, 대거 재편 |
| `docs/product-specs/` | 33 | 0 (전부 이관) | -33 |
| `docs/exec-plans/` | 37 | 0 (전부 이관) | -37 |
| `docs/generated/audit/` | 16 | 0 (전부 이관) | -16 |
| `docs/design-docs/` | (없음) | ≥3 (선별 이관) | +3~ |
| `docs/exec-plans/` | (없음) | 37 + 1(archive) | +38 |
| `docs/product-specs/` | (없음) | ~30 (plans 잔여 + quotation-vat-rules + ai-search) | +30~ |
| `docs/generated/` | (없음) | 1 (erd.md) + 16 (audit/) | +17 |
| `docs/references/` | (없음) | 3 (README + obsidian-setup + setup-guide) | +3 |
| 링크 수정 | — | ~100+ 링크 일괄 갱신 | (스크립트 처리) |

**최종 산출물**: Before/After 인벤토리 표를 `docs/generated/docs-renewal-inventory.md` 에 저장 (NFR-8 필수).

---

## 6. 리스크 평가

| 리스크 | 수준 | 완화책 |
|--------|------|--------|
| 링크 대량 깨짐 | 높음 | NFR-4 검증 스크립트 통과 필수. Markdown + HTML + 파일 존재 + 앵커 전부 |
| git 히스토리 유실 | 중간 | `git mv` 강제. NFR-3 `git log --follow` 테스트 |
| Java 하드코딩 경로 누락 | 중간 | §2-5-B 화이트리스트 + precheck grep (`rg 'docs/' src/main`) |
| Claude Code 자동 로드 손상 | 낮음 | CLAUDE.md 루트 유지 (§4-1) |
| 사용자 일시적 혼란 | 중간 | redirect README 2주 유지 (§4-5) |
| 신규 문서 자동생성 부정확 | 중간 | "자동 생성 초안 — 검증 필요" 배너 + codex 사실 검증 |
| Phase 분리 commit 리뷰 부담 | 낮음 | 각 Phase 당 ≤ 30 파일. revert 용이 |
| 한글 파일명 인용 누락 | 낮음 | `rg '견적서_VAT|HOME|TEAM_WORKFLOW' .` 사전 스캔 |

---

## 7. 수락 기준 (v2 객관화)

**주 게이트 (스크립트 통과 필수)**:
- [ ] `AGENTS.md`, `ARCHITECTURE.md` 루트 존재, 100±20줄 / 300+ 줄 적정
- [ ] `docs/` 하위 5개 신규 폴더 (`design-docs`, `exec-plans`, `generated`, `product-specs`, `references`) 존재
- [ ] `docs/` 루트 7개 신규 .md (`DESIGN`, `FRONTEND`, `PLANS`, `PRODUCT_SENSE`, `QUALITY_SCORE`, `RELIABILITY`, `SECURITY`) 존재
- [ ] `docs/product-specs/`, `docs/exec-plans/`, `docs/generated/audit/` 폴더 비어 있음 or redirect README 만
- [ ] `git log --follow` 로 이관 파일 히스토리 추적 성공 (샘플 3건)
- [ ] **링크 검증 스크립트 통과**: Markdown `(./path.md)` + `(path.md)` + HTML `href=`, `src=` + `#anchor` 전수 존재 확인
- [ ] Maven `./mvnw clean compile` 성공
- [ ] 서버 기동 후 `/system-graph` 200
- [ ] `CLAUDE.md` 주요 문서 링크 5개 갱신
- [ ] Before/After 인벤토리 `docs/generated/docs-renewal-inventory.md` 제출

**보조 게이트**:
- [ ] codex 최종 검증 ✅ 승인 (주관 판정 — 주 게이트 보조)
- [ ] redirect README 2주 유지 후 삭제 자동화 schedule 등록

---

## 8. 승인 요청

본 기획서 v2 에 대한 codex 재검토 및 사용자 최종승인 요청.

**재검토 중점**:
1. 실측 인벤토리 반영 충실성
2. 커밋 전략 (Phase 분리 vs 단일 brunch) 일관성
3. design-docs 속성 기반 선별 규칙 타당성
4. NFR-4 링크 검증 스크립트 설계 충분성
5. Before/After 인벤토리 산출물 범위
