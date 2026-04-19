---
tags: [dev-plan, sprint]
sprint: "{{숫자-코드}}"
status: draft
created: "{{date:YYYY-MM-DD}}"
---

# [개발계획서] {{주제}} — 스프린트 {{번호}}

- **작성팀**: 개발팀
- **작성일**: {{date:YYYY-MM-DD}}
- **근거 기획서**: [[{{기획서-파일명}}]]
- **상태**: 초안 (codex 검토 대기)

---

## 1. 작업 순서 (Stepwise)

### Step 1 — 사전 스캔
1. `rg -n "..." src/` → {{기대}}

### Step 2 — {{작업명}}
2-1. {{구체 지시}}
2-2. `mvn compile` → BUILD SUCCESS.

### Step 3 — {{작업명}}

### Step N — 감사 보고서 갱신
- `docs/audit/2026-04-18-system-audit.md` {{항목}} ☑ 조치함 + 요약 한 줄.

### Step (마지막) — 빌드 / 재기동 / 스모크
- `./mvnw -q compile` → BUILD SUCCESS
- `bash server-restart.sh` → `Started SwmanagerApplication` + ERROR 0
- 런타임 검증: {{정량 확인 커맨드}}

---

## 2. 테스트 (T#)

| ID | 내용 | 검증 방법 | Pass 기준 |
|----|------|----------|-----------|
| T1 | {{항목}} | `rg -n "..." src/` | 0 hits |
| T2 | {{항목}} | {{커맨드}} | {{기준}} |
| T3 | Maven compile | `./mvnw -q compile` | BUILD SUCCESS |
| T4 | 서버 재기동 | `bash server-restart.sh` | 성공 + ERROR 0 |
| T5 | 감사 체크박스 | 해당 항목 ☑ 확인 | hits |

---

## 3. 롤백 전략

| 상황 | 조치 |
|------|------|
| compile 실패 | Edit 되돌림 후 import/의존 재확인 |
| 배포 후 회귀 | `git revert <commit>` → 재배포 |

---

## 4. 리스크·완화 재확인

| 리스크 | 수준 | 완화책 |
|--------|------|--------|
| {{항목}} | 낮음 | {{완화}} |

---

## 5. 승인 요청

본 개발계획서에 대한 codex 검토 및 사용자 최종승인을 요청합니다.
