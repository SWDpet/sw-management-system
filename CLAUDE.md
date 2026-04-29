# CLAUDE.md — Claude Code 프로젝트 컨텍스트

> ⚠ Claude Code 플랫폼 전용 자동 로드 문서. **모든 에이전트 공통 진입점은 `@AGENTS.md`** 를 읽으세요.

---

## 프로젝트 요약

- **SW Manager** — Spring Boot 3.2.1 / Java 17 / PostgreSQL / Thymeleaf
- **서버 포트**: 8080
- **DB**: PostgreSQL @ `${DB_HOST}:${DB_PORT}/${DB_NAME}` (실제 좌표는 환경변수 또는 1Password)
- **빌드**: `./mvnw spring-boot:run` 또는 `bash server-restart.sh`

---

## 핵심 워크플로우

본 프로젝트는 **가상 팀 체계** (기획/DB/디자인/개발/테스트 + codex 검증) 를 따릅니다.

```
요청 → 기획서 → (UI 변경 시) DB팀 + 디자인팀 병렬 자문 → codex 검토 → 사용자 최종승인
     → 개발계획 → codex 검토 → 사용자 최종승인
     → 구현 → codex 검증 → "작업완료" → 자동 commit+push
```

디자인팀(🎨) 은 A+D 정책 (UI 키워드 있을 때만 자문). 자세한 룰은 `@AGENTS.md` §3-1.

**자세한 규칙은 `@AGENTS.md` 참조**:
- §3 팀 구조·워크플로우
- §4 codex 연결 방법
- §5 자동화 규칙 (서버 재시작, 작업완료 커밋)
- §6 영구 패스 (라이선스 스프린트 추천 금지)
- §7 디자인 작업 체크

---

## 도구 우선순위 (Claude Code 전용)

- 검색 → **Grep** (NOT `grep`/`rg`)
- 파일 찾기 → **Glob** (NOT `find`/`ls`)
- 읽기 → **Read** (NOT `cat`/`head`/`tail`)
- 수정 → **Edit** (NOT `sed`/`awk`)
- 쓰기 → **Write** (신규만. 기존 파일은 Edit)

---

## 주요 문서 링크

- `@AGENTS.md` — 에이전트 진입점 (필독)
- `@ARCHITECTURE.md` — 시스템 아키텍처
- `@docs/design-docs/data-architecture-roadmap.md` — 데이터 아키텍처 로드맵
- `@docs/generated/erd.md` — ERD
- `@docs/product-specs/ai-search.md` — AI 검색 기획서
- `@docs/DESIGN.md` — 디자인 시스템
- `@docs/SECURITY.md` / `@docs/RELIABILITY.md` — 보안·안정성
- `@docs/exec-plans/archive/quotation-deploy.md` — 견적서 배포 가이드

---

*Last updated: 2026-04-24 · docs-renewal-01 P3 축약 (기존 180줄 → ~50줄)*
