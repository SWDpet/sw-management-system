---
tags: [dev-plan, docs, restructure]
sprint: "docs-renewal-01"
status: draft
created: "2026-04-24"
---

# [개발계획서] 문서 구조 전면 리뉴얼 (docs-renewal-01)

- **작성팀**: 개발팀
- **작성일**: 2026-04-24
- **근거 기획서**: [[doc-structure-renewal]] v2 (codex 승인 + 사용자 최종승인 2026-04-24)
- **상태**: v2 (codex v1 ⚠수정필요 7건 반영)
- **v1→v2 변경점**:
  1. Step 1-3 선별 본문 조건을 `"사용자 최종승인 완료"` 정확 매칭으로 수정 (과선별 방지)
  2. **Phase 재배치**: P3↔P4 순서 바꿈 — **P3: rename 먼저 → P4: 링크 수정 나중** (중간 커밋 의도적 깨짐 해소)
  3. 롤백 표에 **Phase 의존성 매트릭스** 추가 (독립 revert 가능/불가 명시)
  4. `verify-links.py` 세부 명세 추가 (anchor slug GitHub 규칙 / 외부링크 제외 / 이미지 src 처리)
  5. T# 확장: **T17 독립 revert 드라이런** + **T18 application.properties 화이트리스트 유지** 검증
  6. CLAUDE.md 도구 우선순위 준수 — 스크립트 내에서는 rg/find 불가피하지만 Claude 가 수동 확인 시 Grep/Glob/Read/Edit 사용 명시
  7. Step 번호 재정렬 (P 순서 반영)

---

## 0. 전제 / 환경

### 0-1. 실측 고정값 (기준일 2026-04-24)
- `docs/product-specs/` 33개 / `docs/exec-plans/` 37개 / `docs/generated/audit/` 16개 / docs 총 96 / repo 총 106 / root .md 5
- `*-decision.md` 5개 (plans 2, dev-plans 3) — dev-plans 3개는 **exec-plans/ 잔류**

### 0-2. 작업 브랜치
- 현재 브랜치: `docs-renewal-v2` (master fast-forward 가능)
- 최종 PR·merge 는 사용자 지시

### 0-3. 롤백 기준 SHA — Step 0 에서 기록

---

## 1. 작업 순서 — Phase P1~P6 원자 commit

### Step 0 — 기준 SHA + 툴 준비

```bash
BASE_SHA=$(git rev-parse HEAD)
```

**링크 검증 스크립트 파일 2개 작성**:
- `docs/exec-plans/docs-renewal-tools/scan-inventory.sh` — Before/After 인벤토리 생성
- `docs/exec-plans/docs-renewal-tools/verify-links.py` — Markdown/HTML 링크 + 파일 존재 + 앵커 검증

### Step 1 — Precheck (인벤토리 + 참조 스캔)

**1-1. Before 인벤토리 생성**:
```bash
bash docs/exec-plans/docs-renewal-tools/scan-inventory.sh before \
  > docs/exec-plans/docs-renewal-inventory-before.md
```

**1-2. 코드 참조 전수 스캔**:
```bash
rg -n 'docs/plans|docs/dev-plans|docs/audit|docs/ERD|docs/HOME|docs/DESIGN_SYSTEM|docs/AI_SEARCH|docs/TEAM_WORKFLOW|docs/OBSIDIAN|docs/SETUP|견적서_VAT|DEPLOYMENT_GUIDE|DEVELOPMENT_GUIDELINES' \
  src/ swdept/ *.md \
  --glob '!**/.git/**' --glob '!**/target/**' \
  > docs/exec-plans/docs-renewal-refs-before.txt
```

**1-3. `*-decision.md` 속성 기반 선별 (design-docs 대상 확정)**:
```bash
# 파일명 suffix
find docs/plans -name '*-decision.md' > /tmp/s_suffix.txt
# frontmatter status: approved|final
rg -l 'status:\s*(approved|final)' docs/product-specs/ > /tmp/s_status.txt
# 본문 "사용자 최종승인 완료" 정확 매칭 + "구현 완료" (v2: 기획서 §2-4-A 정밀 일치)
rg -l '사용자 최종승인 완료' docs/product-specs/ | xargs rg -l '구현 완료' > /tmp/s_body.txt
# 로드맵
echo 'docs/design-docs/data-architecture-roadmap.md' > /tmp/s_roadmap.txt
# 통합 (중복 제거)
cat /tmp/s_suffix.txt /tmp/s_status.txt /tmp/s_body.txt /tmp/s_roadmap.txt | sort -u \
  > docs/exec-plans/docs-renewal-design-docs-list.txt
```

**Exit Gate 1**: 
- Before 인벤토리 수치 = 기획서 §1-1 실측 (±0 허용)
- design-docs 후보 리스트 검토 (사용자 승인 게이트 — 선별 결과 전달)

### Step 2 — Phase P1: 신규 파일 스캐폴드 커밋

**추가 파일 (9개)**:
- `AGENTS.md` (루트, 100±20줄)
- `ARCHITECTURE.md` (루트)
- `docs/FRONTEND.md`
- `docs/PLANS.md`
- `docs/PRODUCT_SENSE.md`
- `docs/QUALITY_SCORE.md`
- `docs/RELIABILITY.md`
- `docs/SECURITY.md`
- `docs/references/README.md` (용도 설명)

**각 파일 규칙**:
- "자동 생성 초안 — 검증 필요" 배너 최상단
- 최소 섹션 3개 + 200자 이상 (NFR-5)
- 생성 근거 (예: `pom.xml`, `templates/`, `audit/dashboard.md` 등) 명시

**커밋**:
```bash
git add AGENTS.md ARCHITECTURE.md docs/FRONTEND.md docs/PLANS.md \
  docs/PRODUCT_SENSE.md docs/QUALITY_SCORE.md docs/RELIABILITY.md \
  docs/SECURITY.md docs/references/README.md
git commit -m "docs(P1): 신규 진입점·SSoT 문서 9종 스캐폴드"
```

### Step 3 — Phase P2: 폴더 git mv

```bash
# 3-1. dev-plans → exec-plans
git mv docs/dev-plans docs/exec-plans
mkdir -p docs/exec-plans/archive

# 3-2. audit → generated/audit
mkdir -p docs/generated
git mv docs/audit docs/generated/audit

# 3-3. plans → product-specs (임시)
git mv docs/plans docs/product-specs

# 3-4. design-docs 선별 이관 (Step 1-3 리스트 기반)
mkdir -p docs/design-docs
while IFS= read -r f; do
  # docs/product-specs/X.md 를 docs/product-specs/X.md → docs/design-docs/X.md 로 재이동
  new_base=$(basename "$f")
  [ -f "docs/product-specs/$new_base" ] && git mv "docs/product-specs/$new_base" "docs/design-docs/$new_base"
done < docs/exec-plans/docs-renewal-design-docs-list.txt
# 주: 실행 중 Step 1-3 리스트가 docs/exec-plans/ 에 있었으므로 exec-plans/ 로 이동됨
```

**커밋**:
```bash
git commit -m "docs(P2): 폴더 재배치 — dev-plans/audit/plans → exec-plans/generated/audit/product-specs + design-docs 선별"
```

**주의**: 이 커밋 직후 링크 **깨짐** 상태. P3 즉시 진행 필수.

### Step 4 — Phase P3: 파일 rename + CLAUDE.md 축약 (v2: 순서 스왑 — rename 먼저)

**4-1. Rename**:
```bash
git mv docs/DESIGN.md docs/DESIGN.md
# 아래는 P2 단계에서 이미 product-specs/ 로 이동된 상태 전제
git mv docs/product-specs/견적서_VAT_표시기준.md docs/product-specs/quotation-vat-rules.md 2>/dev/null || true
git mv docs/product-specs/AI_SEARCH_PLAN.md docs/product-specs/ai-search.md 2>/dev/null || true
git mv docs/product-specs/OBSIDIAN_SETUP.md docs/references/obsidian-setup.md 2>/dev/null || true
git mv docs/product-specs/SETUP_GUIDE.md docs/references/setup-guide.md 2>/dev/null || true
git mv docs/product-specs/ERD.md docs/generated/erd.md 2>/dev/null || true
git mv docs/exec-plans/archive/quotation-deploy.md docs/exec-plans/archive/quotation-deploy.md
```

주: P2 시점 폴더 이동과 P3 rename 은 순서 의존성 있음. P3 의 rename 은 **P2 이후 최종 경로에 맞춰 단일 원자 commit**.

**4-2. CLAUDE.md 축약** (~50줄):
- Claude Code 자동 로드 목적 프로젝트 컨텍스트만 유지
- 팀 워크플로우 / 작업완료 커밋 / 영구 패스 리스트는 `AGENTS.md` 로 이관 (P1 에서 이미 신규 생성)
- `@AGENTS.md` 참조 링크 추가

**커밋**:
```bash
git commit -m "docs(P3): 파일 rename 완료 + CLAUDE.md 축약 (AGENTS.md 참조)"
```

**주의**: 이 시점에도 링크 **깨짐** 상태 (파일은 최종 경로, 링크는 아직 구 경로). P4 즉시 진행.

### Step 5 — Phase P4: 내부 링크 일괄 수정 (v2: rename 후 실행)

**5-1. Markdown 링크 치환 스크립트** (`docs/exec-plans/docs-renewal-tools/fix-links.py`):

치환 규칙 (sed 대신 Python — CLAUDE.md 도구 우선순위 준수):

| From | To |
|------|----|
| `docs/product-specs/` | `docs/product-specs/` |
| `docs/exec-plans/` | `docs/exec-plans/` |
| `docs/generated/audit/` | `docs/generated/audit/` |
| `docs/generated/erd.md` | `docs/generated/erd.md` |
| `docs/PRODUCT_SENSE.md` | `docs/PRODUCT_SENSE.md` |
| `docs/DESIGN.md` | `docs/DESIGN.md` |
| `AGENTS.md` | `AGENTS.md` |
| `docs/product-specs/ai-search.md` | `docs/product-specs/ai-search.md` |
| `docs/references/obsidian-setup.md` | `docs/references/obsidian-setup.md` |
| `docs/references/setup-guide.md` | `docs/references/setup-guide.md` |
| `docs/product-specs/quotation-vat-rules.md` | `docs/product-specs/quotation-vat-rules.md` |
| `docs/exec-plans/archive/quotation-deploy.md` | `docs/exec-plans/archive/quotation-deploy.md` |
| `AGENTS.md` | `AGENTS.md` |
| design-docs 선별 파일 경로 | `docs/design-docs/<name>` |

**대상**:
- `**/*.md` 내 Markdown 링크 `[text](path)` / `[text][ref]` / `[ref]: path`
- `**/*.html` 내 `href="..."`, `src="..."`
- `src/main/java/**/*.java` 내 `"docs/..."` 문자열
- `src/main/resources/**/*.sql`, `*.properties`, `*.yml` 주석 내 경로
- `src/main/resources/static/js/**/*.js` 내 경로

**5-2. application.properties 화이트리스트 적용**:
- `app.erd.mmd-dir=docs` **유지** (mmd 파일 디렉터리)
- `app.erd.descriptions-file=docs/erd-descriptions.yml` **유지**
- 기타 `@Value("docs/...")` 는 Step 1-2 결과 기반 개별 판정

**커밋**:
```bash
git commit -m "docs(P4): 내부 링크 일괄 수정 (Markdown/HTML/Java/JS/SQL)"
```

이 commit 직후 링크 검증 스크립트 1차 실행 권장 (깨진 링크 0 확인).

### Step 6 — Phase P5: 레거시 파일 삭제

**삭제 대상** (통합·이관 완료 후):
- `AGENTS.md` (AGENTS.md 통합)
- `docs/PRODUCT_SENSE.md` (PRODUCT_SENSE 통합)
- `AGENTS.md` (AGENTS.md 통합)
- `docs/exec-plans/archive/quotation-deploy.md` 원본 (P4 에서 이관했으나 원본 제거 확인)

```bash
git rm AGENTS.md docs/PRODUCT_SENSE.md AGENTS.md
# docs/exec-plans/archive/quotation-deploy.md 는 P4 의 git mv 로 이미 삭제됨
git commit -m "docs(P5): 레거시 파일 삭제 (DEVELOPMENT_GUIDELINES/HOME/TEAM_WORKFLOW)"
```

### Step 7 — Phase P6: redirect README + 인벤토리 산출

**7-1. Redirect README 배치** (2주 유지 후 자동 삭제):
```bash
# 삭제된 폴더는 git mv 로 이미 사라졌으므로, 혼란 방지용 안내는 README 로 남김
mkdir -p docs/plans docs/dev-plans docs/audit
cat > docs/product-specs/README.md <<'EOF'
⚠ 이 폴더는 **2026-04-24 문서 리뉴얼** 로 이동했습니다.
→ `docs/product-specs/` (대부분) 또는 `docs/design-docs/` (결정 완료 문서)
이 안내는 2026-05-08 이후 자동 삭제됩니다.
EOF
# 유사하게 dev-plans → exec-plans, audit → generated/audit 안내
```

**7-2. After 인벤토리 + Before/After 비교 산출**:
```bash
bash docs/exec-plans/docs-renewal-tools/scan-inventory.sh after \
  > docs/generated/docs-renewal-inventory.md
# Before/After diff 추가
```

**7-3. 자동 삭제 예약**: GitHub Actions or 단순 TODO — `docs/exec-plans/archive/docs-renewal-cleanup.md` 에 2026-05-08 삭제 예정 기재.

**커밋**:
```bash
git commit -m "docs(P6): redirect README 배치 + Before/After 인벤토리 산출 (2주 유지)"
```

### Step 8 — 검증 (링크 + 컴파일 + 기동)

**8-1. 링크 검증 스크립트 실행**:
```bash
python docs/exec-plans/docs-renewal-tools/verify-links.py
```

**verify-links.py 세부 명세** (v2 보강):

| 영역 | 처리 |
|------|------|
| Markdown inline | `[text](path)` — path 부분 파일 존재 확인 |
| Markdown reference | `[text][ref]` + `[ref]: path` 양방향 연결 확인 |
| HTML `href`, `src` | 태그 속성 파싱. `<img src>` 도 포함 |
| 외부 링크 | `http://`, `https://`, `mailto:` 는 **검증 제외** (네트워크 의존) |
| 앵커 (`#slug`) | **GitHub slug 규칙**: 소문자 + 하이픈 + 한글 유지. 대상 파일 내 해당 heading 존재 확인 |
| 이미지 바이너리 | `.png/.jpg/.gif/.svg` 는 존재만 확인 (내용 검증 X) |
| 상대 vs 절대 | 문서 기준 상대경로 → 절대 경로 normalize 후 파일시스템 조회 |
| 에러 출력 | `<file>:<line> <reason> <link>` 형식. exit code 0(PASS) / 1(FAIL) |

기대: 모든 링크 PASS → exit 0

**8-2. Maven 컴파일**:
```bash
./mvnw -q clean compile
```

**8-3. 서버 기동 + ERD 탭 스모크**:
```bash
bash server-restart.sh
curl -s -o /dev/null -w "HTTP %{http_code}\n" http://localhost:9090/system-graph
```

**8-4. git log --follow 샘플 3건**:
```bash
git log --follow docs/exec-plans/audit-fix-p1.md | head -5
git log --follow docs/generated/audit/dashboard.md | head -5
git log --follow docs/product-specs/inspect-comprehensive-redesign.md | head -5
```

**Exit Gate 2**: 모두 PASS 아니면 중단 + Phase 단위 revert.

### Step 9 — 사용자 `"작업완료"` 발화 후 PR/merge

---

## 2. 테스트 / 검증 (T#)

| T# | 항목 | 검증 | Pass 기준 |
|----|------|------|-----------|
| T1 | Before 인벤토리 | scan-inventory.sh | 실측 일치 |
| T2 | design-docs 선별 | Step 1-3 | 사용자 승인 |
| T3 | P1 신규 파일 9개 | ls | 존재 |
| T4 | NFR-5: 200자+섹션 3 | 문자수 검사 | 9개 모두 PASS |
| T5 | NFR-6: AGENTS 100±20 | wc -l | OK |
| T6 | P2 폴더 이동 | ls | dev-plans/audit/plans 비어있음 |
| T7 | P3 rename | git log --follow | 히스토리 추적 |
| T8 | P4 링크 수정 | verify-links.py | 깨진 링크 0 |
| T9 | P5 레거시 삭제 | ls | 3개 파일 없음 |
| T10 | P6 redirect README | 3곳 README | 안내문 존재 |
| T11 | Before/After 인벤토리 | `docs/generated/docs-renewal-inventory.md` | 제출 |
| T12 | Maven compile | mvnw | SUCCESS |
| T13 | /system-graph | curl | HTTP 200 |
| T14 | git log --follow | Step 8-4 | 3건 모두 히스토리 보임 |
| T15 | CLAUDE.md 링크 5개 | 검사 | 갱신됨 |
| T16 | 한글 파일명 인용 없음 | `rg '견적서_VAT|HOME|TEAM_WORKFLOW' .` | 주석 외 0 |
| T17 (v2) | **독립 revert 드라이런** | 각 Phase commit 순차 `git revert --no-commit <sha> && git revert --abort` 시뮬 | 의존 있는 Phase 쌍은 묶음 revert 문서화 (§3 매트릭스) |
| T18 (v2) | `application.properties` 화이트리스트 유지 | `app.erd.mmd-dir=docs`, `app.erd.descriptions-file=docs/erd-descriptions.yml` 값 유지 검증 | 변경 없음 |

---

## 3. 롤백 전략

### 3-1. Phase 의존성 매트릭스 (v2 신규)

각 Phase 커밋은 원자적이나 **일부 Phase 쌍은 의존 관계**로 단독 revert 시 링크 깨짐 발생:

| 시나리오 | 독립 revert 가능? | 대응 |
|---------|------------------|------|
| **P1 신규 스캐폴드** 만 revert | ✅ 가능 | 신규 파일만 삭제됨 |
| **P6 redirect README** 만 revert | ✅ 가능 | README 만 삭제 |
| **P5 레거시 삭제** 만 revert | ✅ 가능 | 레거시 파일 복원 |
| **P4 링크 수정** 만 revert | ⚠ 주의 | 링크가 구 경로로 돌아가지만 파일은 P2/P3 결과 (rename 된) 위치 → **링크 깨짐**. P2+P3+P4 함께 revert 필요 |
| **P3 rename** 만 revert | ⚠ 주의 | rename 되돌려지나 P4 링크는 신규 경로 가정 → **링크 깨짐**. P3+P4 함께 revert 필요 |
| **P2 폴더 이동** 만 revert | ⚠ 주의 | 원래 폴더 복원되나 P3/P4 파일 이동·링크가 신규 경로 가정 → **전면 깨짐**. P2+P3+P4 함께 revert 필요 |
| **전체 롤백** | ✅ | `git revert BASE_SHA..HEAD --no-commit && git commit` |

**권장 롤백 패턴**:
- 배포 후 운영 이슈 발생 시: 전체 롤백 (BASE_SHA..HEAD)
- 특정 Phase 만 수정 필요 시: 해당 Phase 묶음 revert + 패치 commit 재진행

### 3-2. 긴급 복구 체크리스트

1. `git revert` 시 **충돌 발생 가능** (특히 Phase 4 링크 치환이 많은 파일 건드림) — `git revert --abort` 로 중단 가능
2. revert commit 은 이력에 남김 (force push 금지)
3. redirect README 는 P6 revert 해도 무해 (단순 삭제)

**롤백 기준 SHA**: __________ (Step 0 기록)

---

## 4. 파일 변경 요약

### 신규 (루트)
- `AGENTS.md` (진입점)
- `ARCHITECTURE.md`

### 신규 (docs/)
- `docs/FRONTEND.md` / `PLANS.md` / `PRODUCT_SENSE.md` / `QUALITY_SCORE.md` / `RELIABILITY.md` / `SECURITY.md`
- `docs/references/README.md`
- `docs/exec-plans/archive/` (폴더)
- `docs/design-docs/` (폴더)
- `docs/generated/` (폴더)
- `docs/generated/docs-renewal-inventory.md`

### 이동 (git mv)
- `docs/product-specs/` → `docs/product-specs/` + 선별 `docs/design-docs/`
- `docs/exec-plans/` → `docs/exec-plans/`
- `docs/generated/audit/` → `docs/generated/audit/`
- `docs/generated/erd.md` → `docs/generated/erd.md`
- `docs/product-specs/quotation-vat-rules.md` → `docs/product-specs/quotation-vat-rules.md` 등

### 축약
- `CLAUDE.md` (~50줄)

### 삭제
- `AGENTS.md` / `docs/exec-plans/archive/quotation-deploy.md` (원본 제거, 내용 이관)
- `docs/PRODUCT_SENSE.md` / `AGENTS.md`

### 링크 수정 대상 파일 (Step 1-2 결과 기반)
- `src/main/resources/application.properties` (L26 외)
- `src/main/java/com/swmanager/system/service/ErdGraphService.java` (L33, 34)
- `src/main/resources/db_init_phase2.sql` (L431)
- `src/main/resources/static/js/admin-user.js` (L4)
- 기타 Step 1-2 스캔 결과 추가

### 유지
- `README.md`, `HELP.md`, `docs/templates/`, `docs/application-*.properties*`

---

## 5. 승인 요청

### 승인 확인 사항
- [ ] §0 전제 (base SHA 기록 절차)
- [ ] §1 Step 0~9 Phase P1~P6 원자 commit
- [ ] §1 Step 1-3 design-docs 선별 규칙 (Exit Gate 1)
- [ ] §1 Step 4 링크 치환 규칙표
- [ ] §2 T1~T18 검증 체크리스트 (v2: T17 독립 revert 드라이런 / T18 화이트리스트 유지 포함)
- [ ] §3 Phase별 독립 revert 롤백
- [ ] §4 파일 변경 범위
