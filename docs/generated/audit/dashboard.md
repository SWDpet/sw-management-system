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

### ⏸ 보류 결정 — DB 보안 강화 (스프린트 4 연기, 2026-04-19)

**결정 사유**: 현재 시스템은 일부 사용자만 쓰는 상태이며 개발 속도가 우선. 네트워크·계정·웹 보안 조치는 개발 일정 안정화 이후 재논의.

**수용된 잔존 리스크**:
- `211.104.137.55:5881` 인터넷 공개 유지 → 브루트포스·스캔 노출 지속
- `postgres` superuser 로 앱 접속 (추정) → RCE 시 blast radius 큼
- Spring Boot 3.2.1 구버전 → 공개 CVE 노출
- 파일 업로드 확장자 화이트리스트 없음 (DocumentAttachmentService.java:36-55)
- `/signup` 공개 → 익명 계정 생성 가능
- 앱·DB 같은 Windows 11 서버 → RCE 시 localhost 로 DB 직결

**재논의 트리거 조건** (하나라도 해당 시 즉시 재개):
- 외부 사용자 범위 확대 (사용자 수 증가 / 공개 도메인 배포)
- 보안 사고 의심 징후 (이상 로그, 불명 IP 지속 시도, 브루트포스 성공 의심)
- DB 에 더 민감한 데이터 추가 (PII 대량, 결제정보 등)
- 감사 기관·고객사 보안 요구
- 분기 1회 정기 재검토 (최소)

**최소 비용 권장 조치** (언제 해도 OK):
- [ ] 강력한 비번 1회 설정 (32자 random) — 10분, 워크플로우 무영향
- [ ] DB 감사 로그 활성 (`log_connections=on`) — 10분, 사후 추적용
- [ ] Shodan 에서 `211.104.137.55` 현황 1회 확인 — 5분, 이미 노출 중인지 파악

**재개 시 시작 지점**:
- `docs/product-specs/security-hardening-v2-draft.md` — codex 검토까지 완료된 초안
- codex 추천 TOP 3: 5881 외부 차단 / `postgres`→`swmanager_app` / Tailscale+SSL

### 인프라 / 운영
- [ ] Shodan / Censys 에 211.104.137.55 노출 여부 확인
- [ ] 팀 공용 비밀번호 저장소 (1Password / Bitwarden) 도입

### 코드 품질 (P2/P3 이후 여력 생길 때)
- [ ] phase1 DDL 정식 정리 (감사 2-2 후속, 스프린트 2a 주석에 가이드만)
- [ ] Validation 메시지 `rejected value` 포함 여부 전역 감사 (스프린트 2c 5-3 후속)

### Windows 11 DB 서버 운영 (별도 TODO)
- [ ] Windows 11 은 원칙적으로 데스크탑 OS — 서버 라이선스 회색지대
- [ ] 자동 업데이트 재부팅 정책 점검
- [ ] PostgreSQL 서비스 복구(재시작) 설정 점검
- [ ] `pg_dump` 자동 백업 스케줄 점검
- [ ] Windows 관리자 계정 / BitLocker 복구 키 소재 확인

---

## 📝 사용 가이드 (Obsidian)

- **그래프 뷰** (`Ctrl + G`): 20개 문서의 연결망 시각화
- **백링크** (오른쪽 패널): 감사 보고서를 참조하는 모든 스프린트 문서 자동 목록
- **퀵 스위처** (`Ctrl + O`): 파일명으로 즉시 이동
- 체크박스 `- [ ]` 는 클릭으로 토글 + git 에 변경 이력 남김
