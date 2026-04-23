---
tags: [plan, docs, restructure]
sprint: "docs-renewal-01"
status: draft
created: "2026-04-24"
---

# [기획서] 문서 구조 전면 리뉴얼 (AGENTS/ARCHITECTURE 중심 SSoT 체계)

- **작성팀**: 기획팀
- **작성일**: 2026-04-24
- **선행**: `HEAD` (현재 브랜치 기준)
- **상태**: 초안 (codex 검토 대기)

---

## 1. 배경 / 목표

### 배경
- 현재 프로젝트 문서는 **90+개 .md 파일**이 루트/`docs/`/`docs/plans/`/`docs/dev-plans/`/`docs/audit/` 등에 분산되어 있어 **에이전트(Claude/codex)가 맥락을 찾기 어렵다**.
- 루트에 `CLAUDE.md`, `DEPLOYMENT_GUIDE.md`, `DEVELOPMENT_GUIDELINES.md`, `HELP.md`, `README.md`가 섞여 있고, **단일 진입점(Entry)이 없어** 어느 문서부터 읽어야 하는지 불명확.
- 에이전트 지향 레이아웃(AGENTS.md 진입점 + ARCHITECTURE.md 최상위 맵 + `docs/` SSoT)으로 전환하면 **맥락 탐색 비용이 감소**하고, **자동 생성 문서(generated/)와 수동 문서(design-docs/product-specs)가 분리**되어 유지보수성이 향상된다.

### 재검증 결과 (2026-04-24)
- `ls docs/` → 17개 (.md 8개 + 폴더 4개 + 기타)
- `ls docs/plans/` → 27개 (기획서)
- `ls docs/dev-plans/` → 31개 (개발계획서)
- `ls docs/audit/` → 16개 (감사 결과)
- 루트 .md → 5개 (`CLAUDE.md`, `DEPLOYMENT_GUIDE.md`, `DEVELOPMENT_GUIDELINES.md`, `HELP.md`, `README.md`)

### 목표
- **G1**: 에이전트가 `AGENTS.md`만 읽으면 전체 문서 맵을 이해할 수 있다 (진입점 단일화).
- **G2**: 모든 문서가 목표 구조(스크린샷 기준)에 정확히 매핑되어 "이 문서는 어디에 있는가" 질문이 1분 내에 해결된다.
- **G3**: 내부 참조 링크(`[text](path)`, `href`, `rel=`) 깨짐 **0건**.
- **G4**: git 히스토리 보존 (모든 이동은 `git mv` 사용).
- **G5**: 신규 문서 6종(`ARCHITECTURE`, `FRONTEND`, `PRODUCT_SENSE`, `QUALITY_SCORE`, `RELIABILITY`, `SECURITY`)은 **코드 스캔 기반 자동 채움** 초안으로 생성된다.

---

## 2. 기능 요건 (FR)

### 2-1. 최상위 구조 (루트)
| ID | 내용 |
|----|------|
| FR-1 | 루트에 `AGENTS.md` 신규 생성. `DEVELOPMENT_GUIDELINES.md` 내용 + `CLAUDE.md` 워크플로우 요약 통합. ~100줄 목차/진입점 역할. |
| FR-2 | 루트에 `ARCHITECTURE.md` 신규 생성. `pom.xml`, `src/main/java/com/swmanager/` 패키지 트리, `ERD.md` 요약 기반 자동 생성. |
| FR-3 | 기존 `CLAUDE.md`는 유지하되 **AGENTS.md를 참조하도록 축약** (2차 소스). Claude Code의 `@CLAUDE.md` 관행 유지. |
| FR-4 | `README.md`, `HELP.md`는 현재 위치 유지 (Spring Initializr 기본). |
| FR-5 | `DEVELOPMENT_GUIDELINES.md`는 AGENTS.md로 통합 후 **파일 삭제**. |
| FR-6 | `DEPLOYMENT_GUIDE.md`는 **견적서 모듈 배포 가이드**(1회성)이므로 `docs/exec-plans/archive/quotation-deploy.md`로 이관 후 원본 삭제. 일반 배포 가이드가 필요해지면 별도 작성. |

### 2-2. `docs/` 루트 파일
| ID | 내용 |
|----|------|
| FR-7 | `docs/DESIGN_SYSTEM.md` → `docs/DESIGN.md` rename (`git mv`). |
| FR-8 | `docs/FRONTEND.md` 신규. `src/main/resources/templates/` 구조 + Thymeleaf 컴포넌트 규칙 + 인라인 스타일 현황 기반 자동 생성. |
| FR-9 | `docs/PLANS.md` 신규. 로드맵 요약(기존 `docs/plans/data-architecture-roadmap.md` + 미완료 기획서 목록 집계). |
| FR-10 | `docs/PRODUCT_SENSE.md` 신규. 팀 구성(CLAUDE.md 가상팀) + 제품 비전(README/HOME) + 의사결정 히스토리(CLAUDE.md §🚫 영구패스 등) 통합. |
| FR-11 | `docs/QUALITY_SCORE.md` 신규. 패키지별(admin/quotation/inspect/process 등) 품질 등급 테이블. 기존 `docs/audit/` 결과 요약. |
| FR-12 | `docs/RELIABILITY.md` 신규. `server-restart.sh` 타임아웃/재시도 로직, Spring Boot `server.servlet.session.timeout`, `SecurityLoginProperties` 잠금 정책 기반 자동 생성. |
| FR-13 | `docs/SECURITY.md` 신규. `SecurityConfig.java` + `application.properties` 보안 설정 + 감사 보안 조치(P1-1 등) 기반 자동 생성. |
| FR-14 | `docs/ERD.md`, `docs/AI_SEARCH_PLAN.md`, `docs/HOME.md`, `docs/OBSIDIAN_SETUP.md`, `docs/SETUP_GUIDE.md`, `docs/TEAM_WORKFLOW.md`, `docs/견적서_VAT_표시기준.md` 는 **적절한 하위 폴더로 재배치** (§2-4 매핑표 참조). |

### 2-3. `docs/` 하위 폴더
| ID | 내용 |
|----|------|
| FR-15 | `docs/design-docs/` 신규 생성. **검증 완료된 아키텍처 결정 문서** 카탈로그. 기존 `docs/plans/` 중 `*-decision.md` 및 `data-architecture-roadmap.md` 이관. |
| FR-16 | `docs/exec-plans/` 신규 생성. 기존 `docs/dev-plans/` **전체 이관** (31개). `git mv docs/dev-plans docs/exec-plans`. |
| FR-17 | `docs/exec-plans/archive/` 생성. `DEPLOYMENT_GUIDE.md` 이관. |
| FR-18 | `docs/product-specs/` 신규 생성. 기존 `docs/plans/` **전체 이관** (27개, §FR-15에서 design-docs로 옮긴 것 제외). `git mv docs/plans docs/product-specs`. |
| FR-19 | `docs/generated/` 신규 생성. |
| FR-20 | `docs/generated/audit/` 신규 생성. 기존 `docs/audit/` **전체 이관** (16개). `git mv docs/audit docs/generated/audit`. |
| FR-21 | `docs/references/` 신규 생성. 당장 이관 대상 없음 (빈 폴더 + README.md로 용도 설명). 향후 Spring Boot / Thymeleaf / PostgreSQL 외부 레퍼런스 재포맷 시 사용. |
| FR-22 | `docs/templates/` 는 **그대로 유지** (sprint 템플릿들). 목표 구조 외지만 워크플로우에 필수. |

### 2-4. 개별 파일 매핑표 (현재 → 목표)

| 현재 경로 | 목표 경로 | 방식 |
|-----------|-----------|------|
| `CLAUDE.md` | `CLAUDE.md` (축약) + `AGENTS.md` (신규) | 축약 + 신규 |
| `DEVELOPMENT_GUIDELINES.md` | `AGENTS.md` (통합) | 통합 후 삭제 |
| `DEPLOYMENT_GUIDE.md` | `docs/exec-plans/archive/quotation-deploy.md` | git mv + rename |
| `README.md` | `README.md` | 유지 |
| `HELP.md` | `HELP.md` | 유지 |
| `docs/AI_SEARCH_PLAN.md` | `docs/product-specs/ai-search.md` | git mv + rename |
| `docs/DESIGN_SYSTEM.md` | `docs/DESIGN.md` | git mv (rename) |
| `docs/ERD.md` | `docs/generated/erd.md` | git mv |
| `docs/HOME.md` | `docs/PRODUCT_SENSE.md`에 통합 + 삭제 | 통합 |
| `docs/OBSIDIAN_SETUP.md` | `docs/references/obsidian-setup.md` | git mv |
| `docs/SETUP_GUIDE.md` | `docs/references/setup-guide.md` | git mv |
| `docs/TEAM_WORKFLOW.md` | `AGENTS.md`에 통합 + 삭제 | 통합 |
| `docs/견적서_VAT_표시기준.md` | `docs/product-specs/quotation-vat-rules.md` | git mv + rename (한글→영문) |
| `docs/audit/*.md` (16개) | `docs/generated/audit/*.md` | git mv (폴더 이동) |
| `docs/plans/*-decision.md` (5개) + `data-architecture-roadmap.md` | `docs/design-docs/` | git mv (선별) |
| `docs/plans/*.md` (나머지 22개) | `docs/product-specs/*.md` | git mv (일괄) |
| `docs/dev-plans/*.md` (31개) | `docs/exec-plans/*.md` | git mv (폴더 이동) |
| `docs/templates/` | `docs/templates/` | 유지 |

**선별 이관 대상 (design-docs)**:
- `docs/plans/pjt-equip-decision.md`
- `docs/plans/tb-work-plan-decision.md`
- `docs/plans/s8-hwpx-literal-decision.md` — dev-plans에만 있음, 확인 필요
- `docs/plans/data-architecture-roadmap.md`
- (codex 검토 단계에서 `*-decision.md` 정규식 기반 재확정)

### 2-5. 내부 링크 수정
| ID | 내용 |
|----|------|
| FR-23 | 모든 .md 파일 내부의 상대경로 링크(`(./xxx.md)`, `(../xxx.md)`)를 신규 경로로 일괄 수정. |
| FR-24 | Java 소스의 문서 참조 문자열 (`"docs/plans/..."` 같은 하드코딩)도 검색/수정. 예: `application.properties` 의 `app.erd.mmd-dir=docs`, `app.erd.descriptions-file=docs/erd-descriptions.yml` 등은 **유지**(경로 변경 없음). |
| FR-25 | `CLAUDE.md` §5 "주요 문서" 섹션의 링크 5개 (`DEPLOYMENT_GUIDE`, `DEVELOPMENT_GUIDELINES`, `docs/ERD.md`, `docs/AI_SEARCH_PLAN.md`) 신규 경로로 갱신. |
| FR-26 | `.mmd` 파일 로드 경로 (`app.erd.mmd-dir=docs`)는 ERD가 `docs/generated/erd.md` 이동 후에도 `.mmd` 파일은 별도라 영향 없음을 확인. |

---

## 3. 비기능 요건 (NFR)

| ID | 내용 |
|----|------|
| NFR-1 | Maven compile 성공 (문서 변경이 코드 빌드에 영향 없음 확인). |
| NFR-2 | 서버 재기동 후 ERD 탭(`/system-graph` 등) 정상 동작. 경로 하드코딩 검증. |
| NFR-3 | `git log --follow docs/exec-plans/audit-fix-p1.md` 수행 시 이관 전 `docs/dev-plans/audit-fix-p1.md` 히스토리 추적 가능 (git mv 사용 확인). |
| NFR-4 | 내부 링크 깨짐 0건 — `find . -name "*.md" -exec grep -l "\]([\./].*\.md)" {} \;` 후 모든 링크 검증 스크립트 통과. |
| NFR-5 | 신규 문서 6종 각 **최소 섹션 3개 + 200자 이상** 콘텐츠 채움 (빈 파일 금지). |
| NFR-6 | AGENTS.md 100줄±20 (진입점 목적에 맞는 간결성). |
| NFR-7 | 모든 이관 작업은 **단일 브랜치** 내 commit으로 묶어 rollback 시 revert 1회로 처리 가능. |

---

## 4. 의사결정 / 우려사항

### 4-1. `CLAUDE.md` 위치 — ✅ 루트 유지 + AGENTS.md와 공존
- **근거**: Claude Code 플랫폼은 `CLAUDE.md`를 자동 탐색하여 프로젝트 컨텍스트로 로드함 (Anthropic 공식 관행). `AGENTS.md`는 모든 에이전트 공통 진입점, `CLAUDE.md`는 Claude Code 고유 동작 규칙(hooks, slash commands 포함 가능)을 담아 역할 분리.
- **대안 기각**: `CLAUDE.md` 내용을 모두 AGENTS.md로 이관하면 Claude Code가 프로젝트 지침을 자동 인식 못 할 수 있음.

### 4-2. `docs/plans/` 내 *-decision.md 선별 이관 — ⚠ codex 검토 필요
- 현재 5개 후보 식별하였으나, 파일명이 `-decision`이 아니어도 "검증 완료 아키텍처 결정" 성격인 문서가 있을 수 있음. (예: `legacy-contract-tables-drop.md`, `process-master-dedup.md`)
- **codex 검토 시 각 파일을 열어 성격 판정 요청**.

### 4-3. 신규 문서 자동 생성 범위 — ✅ 코드 스캔 + 기존 문서 통합
- **ARCHITECTURE.md**: `pom.xml` dependencies + `src/main/java/com/swmanager/` 패키지 트리 + `docs/ERD.md` 요약 링크.
- **FRONTEND.md**: `src/main/resources/templates/` 트리 + `static/` 자산 구조 + 인라인 스타일 통계 (`grep -rn 'style="' templates/ | wc -l`).
- **PRODUCT_SENSE.md**: `CLAUDE.md` §가상팀 + `README.md` + `docs/HOME.md` + `docs/plans/data-architecture-roadmap.md` §🚫 영구패스 의사결정 통합.
- **QUALITY_SCORE.md**: `docs/audit/dashboard.md` 결과 요약 + 패키지별 테이블.
- **RELIABILITY.md**: `server-restart.sh` 기동 타임아웃, `application.properties` session/login lock 설정, JPA DDL 정책(`ddl-auto=none`) + `DEPLOYMENT_GUIDE.md`의 DB 점검 체크리스트 발췌.
- **SECURITY.md**: `SecurityConfig.java` filter chain + `@PreAuthorize` 사용 현황 + CLAUDE.md §감사 P1-1 조치.

### 4-4. 한글 파일명 — ✅ 영문으로 통일
- `docs/견적서_VAT_표시기준.md` → `docs/product-specs/quotation-vat-rules.md`.
- **근거**: 일부 CI 도구(Windows Git, URL 인코딩) 한글 파일명 호환 이슈. 에이전트 탐색도 영문이 일관됨.

### 4-5. `docs/references/` 현재 비어있는 폴더 생성 여부 — ✅ README.md만 두고 생성
- **근거**: 향후 LLM 소비용 외부 레퍼런스 재포맷 시 사용. 빈 폴더는 git이 추적하지 않으므로 `docs/references/README.md`에 용도 설명 1~2줄 추가.

### 4-6. 이관 순서 — ⚠ 링크 수정 타이밍이 핵심
- 파일 이동 전에 링크 수정하면 깨지고, 이동 후 수정 지연 시 intermediate commit 상태에서 링크 깨진 채로 머뭇.
- **결정**: **하나의 commit에서 git mv + 링크 수정 동시 처리**. 개발계획서에서 세부 순서 명시.

---

## 5. 영향 범위

| 계층 | 파일 | 유형 |
|------|------|------|
| Docs (신규) | `AGENTS.md`, `ARCHITECTURE.md`, `docs/FRONTEND.md`, `docs/PLANS.md`, `docs/PRODUCT_SENSE.md`, `docs/QUALITY_SCORE.md`, `docs/RELIABILITY.md`, `docs/SECURITY.md`, `docs/references/README.md` | 신규 9개 |
| Docs (이동) | `docs/plans/**` (27) + `docs/dev-plans/**` (31) + `docs/audit/**` (16) + 기타 .md 7개 | git mv 81개 |
| Docs (삭제) | `DEVELOPMENT_GUIDELINES.md`, `DEPLOYMENT_GUIDE.md`, `docs/HOME.md`, `docs/TEAM_WORKFLOW.md` | 삭제 4개 (내용은 통합) |
| Docs (rename) | `docs/DESIGN_SYSTEM.md` → `docs/DESIGN.md` | 1개 |
| Docs (축약) | `CLAUDE.md` | 1개 |
| 링크 수정 | 모든 .md 파일 내 상대경로 링크 | 추정 100+ 링크 |
| Java 소스 | `application.properties`, `SystemGraphController.java` 등 경로 참조 | 검색 후 결정 (영향 없을 가능성 높음) |

**수정 ~100 파일. 신규 9. 삭제 4. DB/API 계약 변경 없음.**

---

## 6. 리스크 평가

| 리스크 | 수준 | 완화책 |
|--------|------|--------|
| 내부 링크 대량 깨짐 → 에이전트 혼란 | 높음 | 개발계획서에서 링크 정규식 목록 고정, 스크립트로 일괄 치환 후 검증 스크립트 (모든 `.md` 링크 targets 존재 확인) 통과 필수 |
| git 히스토리 유실 (git mv 대신 rm+add) | 중간 | 반드시 `git mv` 사용. `git log --follow` 검증 NFR-3. |
| Java 소스 하드코딩 경로 (`app.erd.mmd-dir`) 누락 | 중간 | `grep -rn "docs/" src/main/java/ src/main/resources/` 사전 스캔 + 필요시 config 갱신 |
| Claude Code의 `CLAUDE.md` 자동 로드 기능 손상 | 낮음 | CLAUDE.md를 루트에 유지 (§4-1). |
| 팀원이 기존 경로로 문서 찾음 → 일시적 혼란 | 중간 | 이관 후 README.md에 "문서 구조 리뉴얼 공지" 추가 + 주요 폴더에 한시적 redirect README (e.g., `docs/plans/README.md` = "이 폴더는 `product-specs/`로 이동했습니다") |
| 신규 문서 자동생성 시 부정확한 정보 | 중간 | **모든 신규 문서에 "자동 생성 초안 — 검증 필요" 배너** + 생성 근거 명시. codex 검토 단계에서 사실 검증. |
| 커밋이 비대해져 리뷰/revert 어려움 | 중간 | **개발계획서에서 Phase 단위 commit 분리**: ① 신규파일 스캐폴드 ② 폴더 이동 ③ 파일 rename ④ 링크 수정 ⑤ 레거시 삭제. |
| 한글 파일명 변환 시 인용 누락 | 낮음 | `grep -rn "견적서_VAT" .` 사전 스캔 |

---

## 7. 수락 기준 (Acceptance)

스프린트 종료 시 다음이 모두 참이어야 한다:

- [ ] `AGENTS.md`, `ARCHITECTURE.md` 루트에 존재, 100±20줄/적절 길이.
- [ ] `docs/` 하위에 목표 구조의 5개 폴더(`design-docs`, `exec-plans`, `generated`, `product-specs`, `references`) 존재.
- [ ] `docs/` 루트에 목표 구조의 7개 .md(`DESIGN`, `FRONTEND`, `PLANS`, `PRODUCT_SENSE`, `QUALITY_SCORE`, `RELIABILITY`, `SECURITY`) 존재.
- [ ] 모든 이관 파일은 `git log --follow` 로 이전 히스토리 추적 가능.
- [ ] `find . -name "*.md" -print0 | xargs -0 grep -E "\]\([./]"` 결과의 모든 링크가 유효한 파일 대상 (자동 검증 스크립트 통과).
- [ ] Maven `./mvnw clean compile` 성공.
- [ ] 서버 재기동 후 ERD 탭/`/system-graph` 정상.
- [ ] `CLAUDE.md` 링크 5개 갱신 확인.
- [ ] codex 최종 검증에서 "✅ 승인" 평가.

---

## 8. 승인 요청

본 기획서에 대한 codex 검토 및 사용자 최종승인을 요청합니다.

**검토 중점 항목** (codex에게 전달):
1. 현재 매핑표에 누락된 파일 없는지 (전체 90+개 vs 매핑 커버리지)
2. `*-decision.md` 선별 이관 기준이 타당한지 / 추가 후보 있는지
3. 신규 문서 6종 자동 생성 방식이 충분한 정보량을 담을 수 있는지
4. 리스크 평가에 누락 시나리오 있는지
5. 수락 기준이 완결적인지 (false positive/negative 방지)
