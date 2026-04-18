# [개발계획서] codex 기반 시스템 감사 (Phase 1)

- **작성팀**: 개발팀
- **작성일**: 2026-04-18
- **기획서**: [docs/plans/system-audit.md](../plans/system-audit.md) (v2 승인됨)
- **상태**: v2 (codex 재검토 대기) — 1차 5건 반영

### 개정 이력
| 버전 | 변경 |
|------|------|
| v1 | 초안 |
| v2 | codex 반영: ①V3 grep regex 수정(\| → \|), JWT 패턴 추가 ②C1 범위 확장 (src/main/java + yml + env + docs 전체) ③작업 순서에 NFR-3 재시도 1회 + NFR-5 status 갱신 명시 ④예상 소요 30분 이내로 재조정 (호출당 3~4분) |

---

## 1. 영향 범위

| 계층 | 파일 | 유형 |
|------|------|------|
| Docs (신규) | `docs/audit/{YYYY-MM-DD}-system-audit.md` | 최종 감사 보고서 (마스킹 적용본) |
| Code | **없음** | 읽기 전용 감사 |
| DB | **없음** | — |

**신규 문서 1개. 코드·DB 변경 없음.**

---

## 2. 실행 흐름

```
Step 1: 실행일(YYYY-MM-DD) 확정 + 보고서 파일 경로 결정
   ↓
Step 2: C1 ~ C5 카테고리 순차 codex review 호출 (5회)
   ↓ (각 호출 후)
Step 3: Claude 가 응답에 민감 패턴 정규식 재검사 + 마스킹 강화
   ↓
Step 4: 카테고리별 섹션을 통합 보고서 마크다운에 누적
   ↓ (5개 카테고리 완료 후)
Step 5: 보고서 저장 직전 전체 파일에 민감 패턴 재-grep (최종 방어선)
   ↓
Step 6: 요약 섹션 작성 (P1/P2/P3 건수 집계)
   ↓
Step 7: codex 최종 검토 — 보고서가 범위 C1~C5 를 모두 다뤘는지 확인
   ↓
Step 8: 사용자 최종확인 → "작업완료" 발화 → git commit + push
```

---

## 3. 카테고리별 상세 (codex 프롬프트 구체화)

### 3-1. C1 — 보안

**대상 경로** (기획서 v2 C1 범위 전체):
- `src/main/java/com/swmanager/system/` 전체 (controller/service/config 포함)
- `src/main/resources/application*.properties`, `src/main/resources/**/*.yml`, `src/main/resources/**/*.yaml`
- 프로젝트 루트의 `.env*` 파일 (있으면)
- `docs/` 전체 (특히 past plans 에 인증 정보가 복사됐는지)

**codex 프롬프트**:
```
codex review "우리 codebase 를 **보안** 관점에서 감사해줘.

대상:
- src/main/java/com/swmanager/system/ 전체 (controller/service/config 등 Java 전반)
- src/main/resources/application.properties, application-local.properties, 모든 *.yml/*.yaml
- 프로젝트 루트의 .env 파일(있으면)
- docs/ 디렉토리 전체 (세션 기록·과거 계획서에 비밀번호/키 복사됐을 수 있음)

핵심 질문:
1. 하드코딩된 비밀번호/API 키/토큰이 있는가 (spring.datasource.password, sk-, Bearer 등)
2. @PreAuthorize / @Secured / URL 패턴 기반 접근 제어 없이 민감 데이터를 반환하는 엔드포인트가 있는가
3. @ResponseBody 로 반환되는 DTO 에 password/email/acc_pw/sw_acc_pw/mac_addr 같은 민감 필드가 포함되는가
4. URL 경로 파라미터·쿼리 파라미터가 검증 없이 SQL/Shell/File path 로 전달되는가 (injection 위험)
5. CSRF 비활성화 / CORS 광범위 허용 등 설정상 위험

결과 형식:
[심각도별 발견]
- P1 (즉시 조치 필요): 실제 노출·침해 위험
- P2 (우선 조치 권장): 잠재 위험
- P3 (선택적 개선): 베스트 프랙티스 위반

각 발견에는 `파일:라인` + 권장 조치 1-2줄.

민감값 마스킹 (필수): 실제 비밀번호·키·토큰은 `***` 로 마스킹. 값 그대로 복사 금지.
범위 제외: 성능/의존성 CVE/테스트 커버리지/스타일은 이번 감사 범위 밖."
```

### 3-2. C2 — 엔티티-DB 스키마 정합성

**대상 경로**:
- `src/main/java/com/swmanager/system/domain/` 전체 (엔티티)
- `src/main/resources/db_init_phase2.sql`

**핵심 질문**:
1. 모든 `@Table(name="X")` 의 X 가 db_init SQL 의 CREATE TABLE 에 존재하는가
2. 모든 `@Column(name="col")` 이 해당 테이블의 컬럼에 존재하는가 (이름·타입 일치)
3. `@ManyToOne @JoinColumn(name="fk")` 의 fk 가 테이블에 존재하고, 참조 대상 테이블도 실존하는가
4. 엔티티에는 있지만 init SQL 에 없는 테이블/컬럼이 있는가 (DB drift 가능성)
5. **알려진 사례**: tb_contract 는 엔티티/init SQL 모두 없으나 ERD 문서에 있음 — 유사 패턴 탐지

### 3-3. C3 — 문서-구현 일치

**대상 경로**:
- `docs/ERD.md`, `docs/erd-*.mmd`
- `docs/plans/*.md`, `docs/dev-plans/*.md`
- 비교 대상: 실제 `domain/`, `controller/`, `service/`, `repository/`

**핵심 질문**:
1. ERD mermaid 에 정의된 테이블 중 엔티티 없는 것
2. plans/dev-plans 에 정의된 API 엔드포인트가 실제 `@GetMapping`/`@PostMapping` 에 존재하는가
3. 계획서가 명시한 DTO 필드가 실제 DTO 에 모두 있는가
4. 계획서의 FR/NFR 중 명백히 구현 안 된 것
5. CLAUDE.md 의 워크플로우·팀 구성이 현재 코드와 일치하는가

### 3-4. C4 — Dead code

**대상 경로**:
- `src/main/java/com/swmanager/system/` 전체
- `src/main/resources/templates/` 전체

**핵심 질문**:
1. `@Controller` 메서드 중 어느 템플릿에서도 참조 안 되는 페이지 핸들러
2. `@Service` public 메서드 중 호출 없는 것
3. `@Repository` 커스텀 메서드 중 호출 없는 것 (기본 CRUD 제외)
4. Thymeleaf 템플릿 중 `@Controller` 가 return 하지 않는 것
5. `.team-workspace` 외의 사용 안 되는 `.js`/`.css`/`.html`

### 3-5. C5 — 민감 로그

**대상 경로**:
- `src/main/java/com/swmanager/system/` 전체 중 `log.info`/`log.debug`/`log.warn`/`System.out.println`

**핵심 질문**:
1. 로그 문자열에 password, token, cookie, session, Authorization 등 민감 용어가 포맷 인자로 들어가는 케이스
2. 요청 바디 / 응답 바디 전체를 `toString()` 등으로 찍는 로그
3. SQL 로그(`spring.jpa.show-sql=true`) 가 운영 환경에서도 활성화돼 있는가 (application.properties)
4. `e.printStackTrace()` 직접 호출 (로거 사용 권장)

---

## 4. 마스킹 파이프라인 (FR-5 구현)

### 4-1. 호출 → 응답 → 파일 저장 흐름

```
codex 응답 문자열 → maskText(text) → 보고서 섹션 추가 → 최종 저장 → finalGrep 검증
```

### 4-2. 마스킹 함수 (Bash + grep/sed 활용)

실행 시 다음 패턴을 `***` 로 치환:
```
비밀번호      : password[=:]\s*["']?[^"'\s,;]+
OpenAI 키     : sk-[A-Za-z0-9_-]{20,}
Bearer 토큰   : Bearer\s+[A-Za-z0-9._-]+
JWT           : eyJ[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+
PRIVATE KEY   : -----BEGIN [A-Z ]*KEY-----[\s\S]+?-----END [A-Z ]*KEY-----
```

### 4-3. 최종 grep 검증 (V3)

`grep -E` 의 alternation 은 **escape 하지 않은 `|`** 사용. JWT 패턴 포함. 보고서 저장 후:

```bash
# 실제 값(마스킹 안 된) 이 남아있는지 최종 확인
grep -iEn "(password[=:] *[\"']?[^*\"'\s]{4,}|sk-[A-Za-z0-9_-]{20,}|Bearer +[A-Za-z0-9._-]{20,}|eyJ[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+|-----BEGIN [A-Z ]*KEY-----)" docs/audit/*-system-audit.md
```

- `***` 로 마스킹된 형태는 매치 안 됨 (`[^*\"'\s]{4,}` 가 `*` 제외)
- JWT 는 `eyJ...` 3-segment 구조로 정확 매칭
- 매치 있으면: **커밋 보류** + 사용자 검토 요청 + 수동 마스킹 후 재검증
- 매치 0 이어야만 다음 단계 진행

---

## 5. 보고서 템플릿

```markdown
# 시스템 감사 보고서 (Phase 1)

- **실행일**: {YYYY-MM-DD}
- **감사 도구**: codex (프롬프트 기반)
- **범위**: C1 보안 / C2 엔티티-DB / C3 문서-구현 / C4 Dead code / C5 민감 로그

## 📊 요약
| 카테고리 | P1 | P2 | P3 | 수행 상태 |
|---------|----|----|----|----------|
| C1 보안 | x | y | z | ✅ |
| ... | | | | |
| **합계** | **X** | **Y** | **Z** | — |

## 🔐 C1. 보안

### P1 (즉시 조치 필요)
- **[파일:라인]** 발견 내용 (민감값 `***` 마스킹)
  - 권장 조치: ...
  - 사용자 검토: ☐ 조치함 / ☐ 보류 / ☐ 불필요

### P2 (우선 조치 권장)
...

### P3 (선택적 개선)
...

## 🗂️ C2. 엔티티-DB 정합성
...

## 📚 C3. 문서 ↔ 구현 일치
...

## 🧹 C4. Dead code
...

## 📋 C5. 민감 로그
...

---

## 🔍 감사 메타데이터
- codex 호출 5회, 마스킹 적용, 최종 grep 검증 통과
- 실행 실패 카테고리: (있으면 명시)

## 🎯 후속 조치 제안
1. P1 항목 우선 수정 (별도 스프린트)
2. P2 항목은 다음 분기 기술 부채 정리 때
3. P3 항목은 해당 파일 재작업 시 처리
```

---

## 6. 작업 순서

각 카테고리 Step 시작 시 `set-status.sh codex 진행중 ... "Cn 감사 중"` (NFR-5), 종료 시 진행률 갱신. codex 호출 실패 시 1회 재시도 후 그래도 실패하면 해당 섹션에 "수행 실패" 기록 + 다음 카테고리 진행 (NFR-3).

| Step | 작업 | 검증 / 상태 |
|------|------|------|
| 1 | 보고서 파일 헤더 작성 + 실행일 확정. status=codex 진행중 10% | 파일 존재 |
| 2 | **C1** codex 호출(**1회 재시도**) → 응답 마스킹 → 섹션 추가. status 진행률 갱신 | 섹션 있음 or "수행 실패" 기록 |
| 3 | **C2** codex 호출(**1회 재시도**) → 마스킹 → 섹션 추가. status 갱신 | 섹션 있음 or 실패 기록 |
| 4 | **C3** 동일 (재시도 + 마스킹 + status) | 섹션 있음 or 실패 기록 |
| 5 | **C4** 동일 | 섹션 있음 or 실패 기록 |
| 6 | **C5** 동일 | 섹션 있음 or 실패 기록 |
| 7 | 요약 섹션 집계 (P1/P2/P3 건수, 실패 카테고리 표시) | 표 완성 |
| 8 | **최종 grep 검증** (섹션 4-3 명령어 실행) | 매치 **0 필수** — 매치 있으면 커밋 보류 |
| 9 | codex 최종 검토 — 범위 커버리지. status=codex 완료 100% | ✅ |
| 10 | 사용자 "작업완료" 후 commit + push | master 반영 |

---

## 7. 회귀 테스트

감사는 코드/DB 변경 없음 → 전통적 회귀 테스트 불필요. 대신 **검증 기준**:

| # | 검증 항목 | Pass 기준 |
|---|---------|-----------|
| V1 | 보고서 파일 생성 | `docs/audit/{YYYY-MM-DD}-system-audit.md` 존재 |
| V2 | 5 카테고리 모두 섹션 존재 | `grep -c "^## " = 7+ ` (요약 + C1~5 + 메타) |
| V3 | 마스킹 검증 — **4-3 의 명령어 정확히 실행** → **0 hits** (단, 보고서 자체에 인용된 패턴 예시는 `***` 마스킹 돼 있으므로 문제없음). JWT/Bearer/PRIVATE KEY/password 값/OpenAI 키 전부 확인. | 매치 0 |
| V4 | 요약 표 집계 일치 | 합계 행의 숫자가 각 카테고리 합과 일치 |
| V5 | 후속 조치 섹션 존재 | 마지막 섹션에 우선순위 제안 |
| V6 | 코드/DB 변경 없음 확인 | `git status` — 변경 파일은 보고서·status 뿐 |

---

## 8. 롤백 전략

감사는 **추가만 하는 작업** (보고서 1개 신규). 롤백:
```bash
git revert <commit-sha>
git push
```
→ 보고서 파일만 삭제됨. 다른 영향 없음. 서버 재시작 **불필요**.

---

## 9. 빌드 · 재시작

- **빌드 불필요** (Java 변경 없음)
- **서버 재시작 불필요** (템플릿 변경 없음)

---

## 10. 예상 소요

| 작업 | 시간 |
|------|------|
| 보고서 헤더 | 1 분 |
| C1 ~ C5 codex 호출·마스킹·섹션 추가 (각 3~4분) | 15~20 분 |
| 요약 집계 + grep 검증 | 2 분 |
| codex 최종 검토 | 3 분 |
| **합계** | **~25~26 분** (NFR-1 상한 30분 이내) |

> codex 호출이 느려질 경우: C1 재시도 1회(NFR-3) 로 최대 +3분 허용. 여전히 30분 초과 우려 시 프롬프트 간소화로 조정.

---

## 11. 체크리스트

- [ ] `docs/audit/` 디렉토리 존재 확인
- [ ] 보고서 파일 경로 확정 (실행일 반영)
- [ ] C1 codex 호출 + 마스킹 + 섹션 추가
- [ ] C2 codex 호출 + 마스킹 + 섹션 추가
- [ ] C3 codex 호출 + 마스킹 + 섹션 추가
- [ ] C4 codex 호출 + 마스킹 + 섹션 추가
- [ ] C5 codex 호출 + 마스킹 + 섹션 추가
- [ ] 요약 표 집계
- [ ] 최종 grep 민감 패턴 검증 (V3)
- [ ] codex 최종 범위 커버리지 검토
- [ ] 사용자 "작업완료" 후 commit + push

---

## 12. 승인 요청

본 개발계획서에 대한 codex 검토 및 사용자 최종승인을 요청합니다.
