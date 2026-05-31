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

### 1. 라이브 반영 (서버 재기동) — 최우선
- 마지막 재기동(36개 페이지 시점) **이후 커밋분 11개+는 아직 라이브 미반영**:
  doc-batch/completion/interim/commence/inspect, top-nav, main-dashboard, qr×3
- **재기동 절차** (이 비대화형 셸에선 `cmd /c server-start.bat` 이 NoDefaultCurrentDirectoryInExePath 로 실패 → PowerShell 직접 수행):
  1. 8080 점유 java 종료: `Get-NetTCPConnection -LocalPort 8080 -State Listen | %{ Stop-Process -Id $_.OwningProcess -Force }`
  2. 기동: `.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local" *> server.log` (background)
  3. 확인: server.log 에 `Started SwManagerApplication` + `/css/design-system.css` HTTP 200
  - (사용자가 탐색기에서 server-restart.bat 더블클릭하면 정상)
- 시크릿 모드로 GNB·메인대시보드·점검내역서 등 육안 확인

### 2. 보류한 공유 색 시스템 — 별도 결정 후 일괄 통일
- **유지보수 스코프 3색** (GIS만=teal `#0d9488` / 표준=amber `#d97706` / 전체=navy `#1e3a5f`):
  점검내역서(doc-inspect)·ops-doc/list(.mtag)·PDF·`InspectMaintProfile.badgeTone` 에 공유. 분산 변경 대신 **앱 전체 한 번에** 통일 필요 (amber 는 이미 --warning 동일)
- **칸반 스텝 7컬럼 그라데이션** (workplan/process-status L45-51): 무지개 Material 그라데이션 → 브랜드 순차 팔레트로 통일하려면 디자인 결정 필요

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
