---
tags: [audit, dashboard]
---

# 📊 감사 조치 대시보드

> 감사 2026-04-18 기준 P1~P3 20건 전체 조치 현황을 Obsidian + Dataview 로 가시화.
> Dataview 플러그인이 없으면 쿼리 블록은 원문 그대로 보입니다 (Settings → Community plugins → Dataview 설치).

---

## 🎯 한눈에 보기

```dataviewjs
const plans = dv.pages('"plans"').where(p => p.file.name.startsWith("audit-fix"));
const devPlans = dv.pages('"dev-plans"').where(p => p.file.name.startsWith("audit-fix"));
dv.paragraph(`- 기획서: **${plans.length}건**`);
dv.paragraph(`- 개발계획서: **${devPlans.length}건**`);
dv.paragraph(`- 최종 커밋: \`a27032c\` (스프린트 3 완료)`);
```

---

## 📋 스프린트별 완료 목록

| 스프린트 | 등급 | 건수 | 주제 | 커밋 |
|----------|------|------|------|------|
| 1 | P1 | 4 | DB 자격증명·문서 API 권한·민감정보·inspect_report 스키마 | `2d0a9c1` |
| 2a | P2 | 6 | 스키마·ERD·plans 정합성 | `b93e8bc` |
| 2b | P2 | 3 | Dead code 삭제 | `914fa5c` |
| 2c | P2 | 4 | 보안·로깅 | `d73dd12` |
| 3 | P3 | 3 | 경미 정리 | `a27032c` |
| **합계** | — | **20** | | **5 커밋** |

---

## 📁 기획서 목록 (Dataview)

```dataview
TABLE 
    file.ctime AS "생성일",
    file.mtime AS "최종수정",
    file.size AS "크기"
FROM "plans"
WHERE startswith(file.name, "audit-fix")
SORT file.name ASC
```

---

## 🛠 개발계획서 목록

```dataview
TABLE 
    file.mtime AS "최종수정",
    file.size AS "크기"
FROM "dev-plans"
WHERE startswith(file.name, "audit-fix")
SORT file.name ASC
```

---

## 🔗 관련 문서 빠른 이동

- [[2026-04-18-system-audit|📋 감사 원본 보고서]]
- [[../ERD|📐 ERD 문서]]
- [[../plans/system-graph-infra-perf|🖼 A 탭 인프라 구성도 (v3)]]

---

## 🔜 향후 작업 후보 (감사 이후 제안)

> 감사 20건 완료 후, 대화 과정에서 도출된 추가 개선 후보.

### DB 보안 강화 (우선순위 ⚠ 높음)
- [ ] `211.104.137.55:5881` 외부 개방 차단 — `pg_hba.conf` IP whitelist
- [ ] DB superuser 대신 앱 전용 유저(`swmanager_app`) 분리
- [ ] DB 비밀번호 회전 (감사 1-1 에서 보류)
- [ ] VPN 도입 (OpenVPN / WireGuard)
- [ ] `log_connections` / `log_statement='ddl'` 감사 로그 활성

### 인프라 / 운영
- [ ] Shodan / Censys 에 211.104.137.55 노출 여부 확인
- [ ] 팀 공용 비밀번호 저장소 (1Password / Bitwarden) 도입

### 코드 품질 (P2/P3 이후 여력 생길 때)
- [ ] phase1 DDL 정식 정리 (감사 2-2 후속, 스프린트 2a 주석에 가이드만)
- [ ] Validation 메시지 `rejected value` 포함 여부 전역 감사 (스프린트 2c 5-3 후속)

---

## 📝 사용 가이드 (Obsidian)

- **그래프 뷰** (`Ctrl + G`): 20개 문서의 연결망 시각화
- **백링크** (오른쪽 패널): 감사 보고서를 참조하는 모든 스프린트 문서 자동 목록
- **퀵 스위처** (`Ctrl + O`): 파일명으로 즉시 이동
- 체크박스 `- [ ]` 는 클릭으로 토글 + git 에 변경 이력 남김
