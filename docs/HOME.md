---
tags: [index, home]
---

# 🏠 SW Management System — 문서 허브

> 이 문서는 Obsidian Vault 의 홈 노트입니다. 주요 문서로 바로 이동할 수 있는 허브 역할.
> Obsidian 에서 이 파일을 `Settings → Files & Links → Default file for new tabs` 로 지정하면 실행 시 자동으로 열립니다.

---

## 📚 핵심 문서

### 설계 / 스펙
- [[ERD|📐 ERD 문서]]
- [[erd-descriptions.yml|ERD 한글 설명 YAML]]
- [[AI_SEARCH_PLAN|AI 검색 기획서]]

### 운영 / 배포
- [[../DEPLOYMENT_GUIDE|🚀 배포 가이드]]
- [[../DEVELOPMENT_GUIDELINES|📖 개발 가이드라인]]
- [[../CLAUDE|🤖 Claude 협업 가이드]]
- [[OBSIDIAN_SETUP|🗂 Obsidian 셋업 가이드 (신규 PC / 다기기 동기화)]]

### 감사 / 조치
- [[audit/dashboard|📊 감사 조치 대시보드]] ⭐
- [[audit/2026-04-18-system-audit|📋 감사 원본 보고서]]

### 기획서 (plans/)
- [[plans/audit-fix-p1|스프린트 1 — P1 4건]]
- [[plans/audit-fix-p2-schema-docs|스프린트 2a — 스키마·문서 6건]]
- [[plans/audit-fix-p2-deadcode|스프린트 2b — Dead code 3건]]
- [[plans/audit-fix-p2-security-logging|스프린트 2c — 보안·로깅 4건]]
- [[plans/audit-fix-p3-minor|스프린트 3 — P3 3건]]
- [[plans/system-graph-infra-perf|A 탭 인프라 구성도 v3]]
- [[plans/system-audit|시스템 감사 기획서 v2]]

### 템플릿
- [[templates/sprint-plan-template|📝 스프린트 기획서 템플릿]]
- [[templates/sprint-devplan-template|🛠 스프린트 개발계획서 템플릿]]

---

## 🔥 현재 관심사 / TODO

### ⚠ 우선 조치 대기
- [ ] DB 외부 개방 차단 (`211.104.137.55:5881` — [[audit/dashboard#향후-작업-후보-감사-이후-제안|대시보드 참조]])
- [ ] DB 비밀번호 회전 (감사 1-1 보류분)

### 검토 예정
- [ ] phase1 DDL 정식 정리

---

## 🧭 Obsidian 빠른 사용법

| 단축키 | 기능 |
|--------|------|
| `Ctrl + O` | 퀵 스위처 (파일명 검색) |
| `Ctrl + G` | 그래프 뷰 |
| `Ctrl + Shift + F` | 전체 Vault 검색 |
| `Ctrl + E` | 편집/미리보기 전환 |
| `Ctrl + P` | 커맨드 팔레트 |
| `Alt + ←` / `Alt + →` | 뒤로/앞으로 이동 |

### 필수 플러그인 (Settings → Community plugins)
1. **Dataview** — `dashboard.md` 의 동적 쿼리 활성화
2. **Templater** — 템플릿에서 `{{date}}` 등 변수 자동 치환
3. **Tag Wrangler** — `#audit/p1`, `#sprint/2a` 태그 관리

### 권장 첫 단계
1. 이 파일(HOME) 을 star / pin
2. [[audit/dashboard]] 열어서 Dataview 쿼리 렌더링 확인
3. 그래프 뷰 켜서 20개 문서 연결망 확인
