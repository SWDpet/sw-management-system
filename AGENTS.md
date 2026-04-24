# AGENTS.md — 에이전트 진입점 (Claude / codex / 기타 AI 협업 가이드)

> **이 문서부터 읽어주세요.** 본 리포지토리에서 작업하는 모든 에이전트(Claude Code, codex CLI 등) 공통 진입점입니다.

---

## 1. 프로젝트 요약

- **이름**: SW Manager (sw-manager)
- **목적**: SW Project Management System — 사업·점검·견적서·업무계획·문서 통합 관리
- **스택**: Spring Boot 3.2.1 / Java 17 / PostgreSQL / Thymeleaf
- **서버 포트**: 9090
- **DB**: PostgreSQL @ 211.104.137.55:5881/SW_Dept
- **빌드**: `./mvnw spring-boot:run` 또는 `bash server-restart.sh`

---

## 2. 문서 맵 (어디부터 읽나?)

| 역할 | 경로 | 용도 |
|------|------|------|
| **진입점** (본 문서) | `AGENTS.md` | 모든 에이전트 첫 참조 |
| Claude Code 자동 로드 | `CLAUDE.md` | Claude Code 플랫폼 전용 컨텍스트 |
| 시스템 아키텍처 | `ARCHITECTURE.md` | 패키지 트리 + ERD 요약 |
| 기능 설계 스펙 | `docs/product-specs/` | 기획서 (확정 전/후 전반) |
| 결정 완료 문서 | `docs/design-docs/` | 로드맵 + 실행 완료된 결정 |
| 실행 계획 | `docs/exec-plans/` | 개발계획서 |
| 자동 생성 | `docs/generated/` | ERD, audit, 인벤토리 |
| 문서 템플릿 | `docs/templates/` | 스프린트 템플릿 |
| 영역별 | `docs/DESIGN.md`, `FRONTEND.md`, `SECURITY.md`, `RELIABILITY.md`, `QUALITY_SCORE.md`, `PRODUCT_SENSE.md`, `PLANS.md` | 영역별 SSoT |

---

## 3. 가상 팀 구조 (협업 규칙)

본 프로젝트는 4개 가상 팀 + 1 검증자(codex) 체계로 운영됩니다.

| 팀 | 역할 | 산출물 |
|----|------|--------|
| 🧭 기획팀 | 요건 수렴 + 1차 기획서 | `docs/product-specs/` |
| 🗄️ 데이터베이스팀 | 스키마·인덱스·마이그레이션 자문 | 기획 단계 삽입 |
| 🛠️ 개발팀 | 개발계획 작성 + 구현 | `docs/exec-plans/` + 코드 |
| 🧪 테스트·검증팀 (codex) | 검증 | codex CLI 결과 |
| 🤖 codex | 모든 산출물 검증 게이트 | 검토 결과 사용자에게 전달 |

### 워크플로우
```
요청 → [기획팀] 기획서 → [codex] 검토 → 사용자 최종승인
     → [개발팀] 개발계획 → [codex] 검토 → 사용자 최종승인
     → 구현 → [codex] 검증 → 사용자 "작업완료" → 자동 commit+push
```

**핵심 규칙**:
- "**최종승인**" 이전에는 실제 파일을 수정하지 않는다.
- 기획/개발 산출물은 반드시 **codex CLI** 로 검토 후 사용자에게 전달.
- 테스트는 Claude 직접 돌리지 않고 codex 에 위임.
- 단순 텍스트 변경은 절차 생략 가능 (사용자 판단).

---

## 4. codex 연결

```bash
codex review "파일 docs/product-specs/feature-xxx.md 검토. 평가: 1)요건 2)설계 3)DB 4)리스크 5)권고"
codex review "파일 docs/exec-plans/feature-xxx.md 검토. 평가: 1)FR 반영 2)순서 3)회귀 4)롤백"
```

**검증 원칙** (⚠ 필수):
1. 프롬프트에 **기획서 + 개발계획서 경로 반드시 명시**
2. codex 는 FR-n / NFR-n / T-n 항목별 `충족/미충족/부분충족` 판정
3. 일부 미충족이면 `⚠ 수정필요` / `❌ 반려`
4. 주관 의견은 본 검증 후 별도 섹션

---

## 5. 자동화 규칙

### 서버 재시작 — 승인 없이 즉시
```bash
bash server-restart.sh
```

### "작업완료" 발화 → 자동 commit+push
1. `git add <명시 파일>` (절대 `-A` / `.` 금지)
2. `git commit -m "의도 중심 메시지"` (Co-Authored-By Claude 포함)
3. `git push`

**금지**: `.env`, credential, 대용량 바이너리, 민감 파일 커밋.

### 도구 우선순위 (Claude Code)
- 검색 → `Grep` (NOT `grep`/`rg`)
- 파일 찾기 → `Glob` (NOT `find`/`ls`)
- 읽기 → `Read` (NOT `cat`)
- 수정 → `Edit` (NOT `sed`/`awk`)

---

## 6. 영구 패스 (사용자 결정, 추천 금지)

라이선스 데이터 정제/Enum 도입 스프린트는 **수동 한글화 정책**상 제외:
- **S4** `license-country-cleanup-and-csv-fix`
- **S11** `license-registry-type-enum`
- **S12** `geonuris-license-type-enum`
- **S14** `qr-license-decision`

→ 자세한 내용은 `docs/design-docs/data-architecture-roadmap.md` §🚫 영구 패스.

---

## 7. 디자인 작업 시 필수 체크

디자인·UI·스타일 관련 이야기가 나오면 먼저:

> **Phase 4 대기 중**: 완전 다크모드는 전체 CSS 변수 리팩터 선행 필요.
> `docs/DESIGN.md` 참조. 사용자가 명시 요청 시에만 진행.

---

## 8. 참조 문서

- `CLAUDE.md` — Claude Code 전용 (본 문서 요약)
- `ARCHITECTURE.md` — 시스템 구조
- `docs/design-docs/data-architecture-roadmap.md` — 데이터 아키텍처 로드맵
- `docs/generated/erd.md` — ERD
- `README.md`, `HELP.md` — Spring Initializr 기본

---

*Last updated: 2026-04-24 · docs-renewal-01 sprint P1*
