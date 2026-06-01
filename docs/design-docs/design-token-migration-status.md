# 디자인 토큰 마이그레이션 — 진행 상태 & 다음 작업

> 작성 2026-05-31 (집 세션). **내일 사무실에서 이어서 작업하기 위한 진입점.**
> SoT: `docs/DESIGN.md` · 구현: `src/main/resources/static/css/design-system.css` · 매핑: `docs/design-docs/color-token-map.md`

---

## ✅ 완료 (커밋·푸시됨, master 동기화)

- **파운데이션**: `design-system.css`(중앙 `:root` 토큰 + `ds-*` 공용 컴포넌트), `color-token-map.md`, before/after 시안(`docs/design-docs/migrate-preview-*.html`)
- **공유 GNB(`fragments/top-nav.html`)** — 메뉴 hover 무지개 → 브랜드 teal 통일. **프래그먼트에 토큰 `:root` 내장** → design-system.css 미링크 페이지에서도 전역 토큰 자급
- **앱 UI 47개 페이지** 색 토큰화 (commit 범위 `30a9003` ~ `627984c`):
  - 문서: document-list/detail, ops-doc/list, doc-batch/commence/interim/completion, document-preview, inspect-detail/preview, doc-inspect
  - 성과: personal/dept-dashboard, performance-report
  - 업무: workplan-calendar/form, process-status
  - 관리자: admin-system-graph, admin-user-list, org-unit-management
  - 인프라: infra-list/detail/form
  - 프로젝트: project-list/detail/form
  - 인증/계정: signup, mypage (login·geonuris-list/form·registry-list/upload 는 이미 토큰 기반)
  - person-list/form, geonuris-detail/edit, license/registry-detail, 견적(quotation list/detail/ledger/form/preview/preview2 + pattern/remarks/wage), QR(detail/issue/ledger), main-dashboard
- 부수 버그 정정: 견적 미리보기/상세/대장 hover·포커스 보라색(#5a2fb*) → teal
- **검증**: grep 기계 검증 통과 — 비-pdf 템플릿의 잔존 hex는 100% 의도적 카테고리 팔레트. 깨진 `var()`·누락 토큰 없음. (codex 불필요 = 순수 CSS·로직 0)

---

## ⏭ 다음 작업 (우선순위 순)

### ✅ 1. 라이브 반영 (서버 재기동) — 2026-06-01 사무실 세션 완료
- 사무실 세션 (회사 PC = `IU` 호스트, ASUS TUF Gaming A16) 에서 서버 재기동 + 시크릿 모드로 GNB·메인대시보드·점검내역서 육안 확인 완료
- **부수 fix** (commit `a0f1868`): 메인 대시보드 `:root` 의 `--primary/--primary2/--primary-lt` 가 `var(--primary)` self-reference 라 invalid 였던 잠재 버그 → design-system.css SoT 와 동일한 실제 hex 값으로 정의
- **추가 적용** (commit `a0f1868`): 시스템별 사업 현황 표 thead/tfoot teal 강조 (디자인팀 자문 통과, WCAG AAA/AA)

### ✅ 2. 보류한 공유 색 시스템 — 2026-06-01 완료
- **유지보수 스코프 3색** (commit `1861d5d`): design-system.css 에 `--scope-gis` (#0F766E teal-700) / `--scope-std` (#B45309 amber-700) / `--scope-all` (#1E3A5F navy) 토큰 신설. `ops-doc/list.html` `.mtag` 배지 raw hex → var() 교체. 기존 WCAG AA 미달이던 teal 3.5:1 / amber 3.0:1 → 모두 4.6:1 통과. `--warning` 과 의미 충돌도 한 톤 진한 amber-700 으로 해소. PDF (`templates/pdf/*`) 는 우선순위 3번 정책 따라 별도.
- **칸반 스텝 7컬럼** (commit `1b38781`): design-system.css 에 `--step-1` ~ `--step-7` gradient 토큰 신설 (teal-50 → teal-800 단일 hue progression, 단계 진행 = 색 진해짐). `process-status.html` `.step-N .col-header` raw hex 14개 → var(--step-N) 교체. 기존 step-7 단독 글씨 색 `#333` 일관성 깨짐 해소. SoT: `WorkPlanController.stepLabels`.

### 3. PDF 납품 산출물 (요청 시에만)
- `templates/pdf/*` 7종 — 고객 제출 PDF 본문. 공식 양식이라 보존 중. 통일 원하면 별도 진행

### 4. 선택 (완료에 근접)
- `ds-*` 공용 컴포넌트 클래스 점진 채택 (현재는 토큰 var() 까지만)
- `color-token-map.md` "적용순서 4번"(레거시 hex grep 0) — 비-pdf 앱 UI 기준 사실상 달성

---

## 의도적 보존 (재설계 전엔 손대지 않음)
vis-network 그래프 팔레트(admin-system-graph) · 캘린더 이벤트 10종(workplan-calendar/form/process-status) · 업무유형 카테고리색(성과 대시보드 차트) · 메달색(dept-dashboard) · 다운로드 파일종류색(HWP/ZIP) · 서명 잉크/인쇄 흑백 · 다크모드 블록(main-dashboard)

## 참고
- `docs/exec-plans/dist_cd_fill_backup_2026-05-31.csv` 는 데이터 백업 → `.gitignore` 처리(로컬 보관)
