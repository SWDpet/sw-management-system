# 점검 수집모듈 다운로드 제공 (inspect-agent-download)

> **Status**: `draft v0.1` (2026-06-02, 회사 PC / ASUS TUF A16) — 초안. codex 검토·디자인팀 자문 전.
> **Sprint**: `inspect-agent-download`
> **워크플로우**: 기획서 → **디자인팀 자문(UI 신규 페이지)** → codex → 사용자 승인 → 개발계획 → codex → 구현 → codex 검증
> **관련**: [[inspection-agent-v2-setup]] (수집모듈 설치 가이드), [[inspection-qr-batch]] (업로드 API)

---

## 1. 목적

지자체 서버 담당자가 점검 **수집모듈**(`inspection-poc/agent-windows/`, PowerShell 기반)을 **웹에서 직접 다운로드**하고, 함께 설치/실행 가이드를 볼 수 있는 **전용 페이지**를 제공한다. 빌드 시 모듈을 zip으로 자동 패키징한다.

## 2. 배경 / Why

- 현재 수집모듈은 저장소 `inspection-poc/agent-windows/` 폴더를 **수동 복사**해야만 배포 가능 (다운로드 경로 없음).
- ⚠ `inspection-poc/` 는 `src/main/resources` **밖**이라 ROOT.war 에 포함되지 않음 → 웹 제공하려면 **빌드 시 zip 패키징 단계가 선행**돼야 함.
- 수집모듈은 zip만으로 불충분 — `setup.ps1` wizard·권한·원격점검 설정 등 **실행 가이드 동반**이 필요 → 단순 버튼보다 전용 페이지가 적합 (사용자 결정 2026-06-02).

## 3. 사용자 / 이해관계자

- **지자체 점검 담당자** — 모듈 다운로드 + 설치 가이드 확인 → 현장 서버에서 실행
- **운영팀** — 버전 관리, 배포 모듈 무결성(체크섬) 확인
- (간접) 점검 자동화 흐름 — 최신 모듈 사용률 향상

## 4. FR (Functional Requirements)

| ID | 요건 |
|---|---|
| **FR-1** | **빌드 시 zip 자동 패키징** — maven 빌드(`package`)에서 `inspection-poc/agent-windows/` 를 `inspect-agent-<version>.zip` 으로 압축해 **classpath 리소스**(예: `target/classes/agent/`)에 포함. 수동 zip 작성 금지(누락·stale 방지). |
| **FR-2** | **전용 다운로드 페이지** — `GET /ops-doc/inspect-agent` (또는 확정 경로). 표시: 모듈명·**버전**·간단 설명·**설치/실행 가이드 요약**(상세는 [[inspection-agent-v2-setup]] 링크)·**SHA-256 체크섬**·다운로드 버튼. |
| **FR-3** | **다운로드 엔드포인트** — `GET /ops-doc/inspect-agent/download`. `ResponseEntity<byte[]>` + `Content-Disposition: attachment; filename*=UTF-8''inspect-agent-<ver>.zip` (DocumentController PDF/Excel 다운로드 기존 패턴 재사용). |
| **FR-4** | **운영문서목록 진입점** — `/ops-doc/list` 헤더 액션 영역에 "수집모듈 다운로드" 링크/버튼 추가 → FR-2 페이지로 이동. |
| **FR-5** | **버전 표기** — zip 파일명·페이지·(가능하면) 다운로드 응답 헤더에 버전 노출. 버전 소스 = §7 결정사항. |
| **FR-6** | **무결성** — 페이지에 SHA-256 체크섬 표기 → 사용자가 다운로드 후 검증 가능. (스크립트 배포물이므로 변조 확인 경로 제공) |

## 5. NFR (Non-Functional Requirements)

| ID | 요건 |
|---|---|
| **NFR-1** | **권한** — 다운로드/페이지는 인증 사용자(최소 VIEW 이상). 공개 무인증 노출 여부는 §7 결정 (수집모듈은 사내 점검용 → 인증 권장). |
| **NFR-2** | **WAR 크기 영향 최소** — agent-windows 는 스크립트라 zip <~ 수 MB 예상. war(현 ~100M)에 미미. 측정·기록. |
| **NFR-3** | **디자인 일관성** — 신규 페이지는 design-system.css 토큰 사용, 다크모드 일관, specificity·:root self-reference 점검 (디자인팀 자문 필수 — [[feedback_ui_change_always_design_consult]] 정책). |
| **NFR-4** | **회귀 0건** — /ops-doc/list 기존 동작 무영향. 빌드 산출물(ROOT.war) 정상 기동. |
| **NFR-5** | **빌드 재현성** — zip 내용·파일명이 빌드마다 결정적(타임스탬프 등 비결정 요소 배제 권장). |

## 6. T (Test scenarios)

| ID | 시나리오 | 기대 |
|---|---|---|
| **T-1** | 인증 사용자가 다운로드 페이지 접근 | 200, 버전·체크섬·가이드·버튼 표시 |
| **T-2** | 다운로드 버튼 클릭 | `inspect-agent-<ver>.zip` 첨부 다운로드 |
| **T-3** | 미인증/권한부족 접근 | 차단(로그인 리다이렉트 또는 403) |
| **T-4** | 다운로드 zip 압축해제 | agent-windows 전체 구조 정상, setup.ps1 실행 가능 |
| **T-5** | 페이지 체크섬 vs 실제 파일 SHA-256 | 일치 |
| **T-6** | 빌드 2회 반복 | 동일 zip 내용(결정성) |
| **T-7** | /ops-doc/list 진입점 링크 | 다운로드 페이지로 이동 |

## 7. 결정 필요 사항 (자문/승인 전 확정)

1. **다운로드 페이지 경로** — `/ops-doc/inspect-agent` vs 별도 네임스페이스(`/download/...`).
2. **권한 수준** — 인증 VIEW 이상 vs 특정 역할(운영자) 한정 vs 무인증 공개.
3. **버전 소스** — (a) agent-windows 내 `VERSION`/README 값, (b) pom version, (c) 고정 property. README 상 v0.2.0+ 단서 있음.
4. **zip 포함 범위** — agent-windows 전체 vs 가이드 문서 동봉 여부.
5. **패키징 플러그인** — `maven-antrun`(zip task) vs `maven-assembly`(descriptor). antrun이 경량.

## 8. 비범위 (Out of scope)

- 수집모듈 자체 기능 변경(점검 로직·QR 생성) — 본 sprint 무관.
- 자동 업데이트/버전 체크 클라이언트 — 후속 후보.
- 업로드 API(`/api/inspection/qr-batch`) 변경 없음.

## 9. 변경 이력
- **2026-06-02 v0.1** (회사 PC) — 초안. 전용 페이지 + 빌드시 zip 방향 확정(사용자). 다음 = 디자인팀 자문 + codex 검토.
