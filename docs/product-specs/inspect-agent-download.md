# 점검 수집모듈 다운로드 제공 (inspect-agent-download)

> **Status**: `draft v0.4` (2026-06-02, 회사 PC) — codex 1차+2차 + 디자인팀 자문 **모두 반영**. codex 2차 "최종승인 조건" 4건 정정 완료. 다음 = **사용자 최종승인** → 개발계획.
> **디자인 자문 원문**: `docs/design-docs/components/inspect-agent-download-page.md`
> **codex 2차 원문**: `docs/product-specs/reviews/inspect-agent-download-codex-2nd.md`
> **Sprint**: `inspect-agent-download`
> **워크플로우**: 기획서 → **디자인팀 자문(UI 신규 페이지)** → codex → 사용자 최종승인 → 개발계획 → codex → 구현 → codex 검증
> **codex 1차 원문**: `docs/product-specs/reviews/inspect-agent-download-and-https-codex-1st.md`
> **관련**: [[inspection-agent-v2-setup]] (수집모듈 설치 가이드), [[inspection-qr-batch]] (업로드 API)

---

## 1. 목적

지자체 서버 담당자가 점검 **수집모듈**(`inspection-poc/agent-windows/`, PowerShell 기반)을 **웹에서 직접 다운로드**하고, 설치/실행 가이드를 볼 수 있는 **전용 페이지**를 제공한다. 빌드 시 모듈을 zip으로 자동·결정적으로 패키징한다.

## 2. 배경 / Why

- 현재 수집모듈은 저장소 폴더를 **수동 복사**해야만 배포 가능 (다운로드 경로 없음).
- ⚠ `inspection-poc/` 는 `src/main/resources` **밖**이라 ROOT.war 에 포함되지 않음 → 웹 제공하려면 **빌드 시 zip 패키징 단계가 선행**돼야 함.
- zip만으론 불충분 — `setup.ps1` wizard·권한·원격점검 설정 등 **실행 가이드 동반** 필요 → 전용 페이지가 적합.

## 3. 사용자 / 이해관계자

- **지자체 점검 담당자** — 다운로드 + 가이드 확인 → 현장 서버 실행
- **운영팀** — 버전 관리, 배포물 무결성(체크섬·manifest) 확인, 다운로드 추적
- (간접) 점검 자동화 흐름 — 최신 모듈 사용률 향상

## 4. FR (Functional Requirements)

| ID | 요건 |
|---|---|
| **FR-1** | **빌드 시 zip 자동 패키징** — **`maven-assembly-plugin`** descriptor 로 `inspection-poc/agent-windows/` 를 `inspect-agent-<version>.zip` 으로 압축, 산출물을 **classpath 리소스 경로**(`target/classes/agent/inspect-agent-<version>.zip`)에 배치 → ROOT.war 의 `WEB-INF/classes/agent/` 로 포함. <br>⚠ **phase = `prepare-package`** (codex 2차) — WAR 생성(`war:war`)도 `package` phase 라, zip 이 `target/classes` 에 **WAR 생성 전** 들어가도록 `prepare-package` 에 바인딩(선후관계 보장). <br>**stale 정리**: `clean` phase 가 `target/` 제거로 보장(수동 zip 금지). <br>**제외**(§7-4): `output/`·임시파일·로그·현장 secret. **실 site config 제외 — `config/site.gj.json`·`site.dyg.json` 등 실제 존재**(codex 확인) → exclude. 배포엔 **`config/site.example.json` 샘플만 포함**(구현 전 샘플 생성 필요 — 현재 미존재). |
| **FR-2** | **전용 다운로드 페이지** — `GET /ops-doc/inspect-agent` (codex §7-1 확정). 표시: 모듈명·**버전**·설명·**설치/실행 가이드 요약**([[inspection-agent-v2-setup]] 링크)·**SHA-256 체크섬**·**manifest 요약**(빌드시각·파일수)·다운로드 버튼. |
| **FR-3** | **다운로드 엔드포인트** — `GET /ops-doc/inspect-agent/download`. `ResponseEntity<byte[]>` (DocumentController 패턴 재사용) + 헤더: `Content-Type: application/zip`, `Content-Length`, `Content-Disposition: attachment; filename*=UTF-8''inspect-agent-<ver>.zip`. **리소스 미존재 시 명확한 404** (5xx 와 구분), 권한 미달 시 403. |
| **FR-4** | **운영문서목록 진입점** — `/ops-doc/list` 헤더 액션 영역에 "수집모듈 다운로드" 링크 → FR-2 페이지로 이동. |
| **FR-5** | **버전 표기 + 배포버전 전용 파일** (codex 2차 — 역할 분리) — 배포 버전 SoT = **신규 `agent-windows/VERSION` 파일**. ⚠ 기존 `agent-windows/manifest.json`(version=1)은 **수집모듈 런타임 실행순서 manifest** 이므로 배포 버전으로 전용 금지(혼선). zip 파일명·페이지·(가능 시) 응답 헤더에 `VERSION` 값 노출. (pom version 비채택 — 앱/모듈 버전 분리) |
| **FR-6** | **무결성 — 배포 manifest + 체크섬** (codex 2차 — 런타임 manifest 와 분리) — zip 내부에 **신규 `release-manifest.json`** 생성(버전·빌드시각·파일목록 SHA-256). 기존 런타임 `manifest.json` 재사용 금지. 페이지에 zip 의 SHA-256 + release-manifest 요약 표시 → 변조 검증 경로 제공. (Authenticode 서명은 §8 후속) |

## 5. NFR (Non-Functional Requirements)

| ID | 요건 |
|---|---|
| **NFR-1** | **권한 — 인증 + 업무권한 이중** (codex 지적) — `/ops-doc/**` 는 SecurityConfig상 인증만 → 다운로드 페이지·엔드포인트는 **컨트롤러에서 `authDocument != NONE` 추가 검사** (가능하면 `EDIT` 이상). 무인증 공개 금지. |
| **NFR-2** | WAR 크기 영향 최소 — agent zip(<~수 MB) war(~100M) 대비 미미. 빌드 후 실측·기록. |
| **NFR-3** | 디자인 일관성 (디자인팀 자문 반영) — ① **시맨틱 토큰만 사용**(raw hex 금지, `var(--surface)`/`var(--text)` 등), 신규 토큰 불필요 → **페이지 `:root` 재정의 금지**. ② **`--muted`(2.52:1) 의미텍스트 금지 → `--text2`(7.63:1) 사용**. ③ 시맨틱 토큰으로 **다크 자동대응**(페이지 전용 `[data-theme=dark]` 블록 미추가, NFR-4 범위). ④ 클래스 스코프 selector·`!important` 미사용(specificity 단일레벨). ⑤ CTA·버전배지 `--primary`+#fff(5.47:1), 체크섬 `--font-mono`. 형제 `ops-doc/list` 카드 패턴 계승. |
| **NFR-4** | 회귀 0건 — `/ops-doc/list` 기존 동작 무영향, ROOT.war 정상 기동. |
| **NFR-5** | **결정적 zip** (codex 2차 — 구체화) — **동일 입력 → 동일 zip 바이트**. 검증 기준: ① `maven-assembly-plugin` **버전 고정**, ② `project.build.outputTimestamp`(또는 assembly outputTimestamp) 적용, ③ zip **entry order 정렬 보장**. ⚠ Windows 환경의 timestamp·줄끝(CRLF)·권한비트·entry order 변동 주의(개발계획에서 실측 검증). |

## 6. T (Test scenarios)

| ID | 시나리오 | 기대 |
|---|---|---|
| **T-1** | 인증+권한 충족 사용자 페이지 접근 | 200, 버전·체크섬·manifest·가이드·버튼 |
| **T-2** | 다운로드 버튼 클릭 | `inspect-agent-<ver>.zip` 첨부(application/zip, Content-Length) |
| **T-3** | 미인증 접근 | 로그인 리다이렉트 |
| **T-3b** | 인증되나 `authDocument == NONE` | 403 |
| **T-4** | zip 압축해제 | agent-windows 정상 구조, setup.ps1 실행 가능, 실 site config 미포함 |
| **T-5** | 페이지 SHA-256 vs 실제 파일 | 일치. manifest 내부 파일해시도 일치 |
| **T-6** | 빌드 2회 반복 | **동일 zip 바이트**(reproducible 기준) |
| **T-7** | /ops-doc/list 진입점 | 다운로드 페이지 이동 |
| **T-8** | 빌드 산출물 누락 상태로 다운로드 호출 | 404(5xx 아님) |

## 7. 결정 사항 (확정 — codex 권고 채택 2026-06-02)

1. **경로**: `/ops-doc/inspect-agent` ✅
2. **권한**: `authDocument != NONE` (EDIT 이상 권장), 무인증 공개 금지 ✅
3. **버전 소스**: **신규 `agent-windows/VERSION` 파일** (런타임 manifest.json 과 분리 — codex 2차) ✅
4. **zip 범위**: agent-windows 전체 − (output/·temp·log·실 site config `site.gj.json`/`site.dyg.json`). **`site.example.json` 샘플 신규 생성 후 그것만 포함** ✅
5. **플러그인**: `maven-assembly-plugin` (버전 고정 + reproducible) ✅
6. **배포 manifest**: zip 내부 신규 `release-manifest.json` (런타임 manifest.json 재사용 금지 — codex 2차) ✅

## 8. 비범위 / 후속 후보

- 수집모듈 자체 기능 변경 — 무관.
- **Authenticode 코드서명** — 보안 강화 후속(권장).
- **다운로드 로그**(누가·언제·어떤 버전) — 운영 추적용 후속 후보(codex 권고).
- 자동 업데이트/버전체크 클라이언트 — 후속.

## 9. 변경 이력
- **2026-06-02 v0.4** (회사 PC) — codex 2차 "최종승인 조건" 4건 반영: FR-1 phase `package`→**`prepare-package`**(WAR 순서 보장)·실 site config(gj/dyg) exclude; FR-5 배포버전 **VERSION 전용 파일**(런타임 manifest 분리); FR-6 **release-manifest.json** 신규(런타임 manifest 재사용 금지); NFR-5 reproducible 구체화(plugin 버전고정·outputTimestamp·entry 정렬). §7-3/4/6 갱신. 다음 = 사용자 최종승인.
- **2026-06-02 v0.3** (회사 PC) — 디자인팀 자문 완료(조건부 승인): NFR-3 에 토큰전용·`--muted` 금지(→`--text2`)·다크 자동대응·specificity·CTA 대비 5.47:1 명시. 자문서 = `docs/design-docs/components/inspect-agent-download-page.md`. 다음 = codex 2차.
- **2026-06-02 v0.2** (회사 PC) — codex 1차 검토 반영: §7 5건 확정, FR-1(assembly·package phase·재현성·제외목록)·FR-3(application/zip·Content-Length·404)·FR-5(버전 단일소스)·FR-6(manifest)·NFR-1(업무권한 이중)·NFR-5(결정적 zip)·T-3b/T-6/T-8 추가. 다음 = 디자인팀 자문 → codex 2차.
- **2026-06-02 v0.1** (회사 PC) — 초안. 전용 페이지 + 빌드시 zip 방향 확정.
