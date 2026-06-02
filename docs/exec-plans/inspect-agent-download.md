---
tags: [dev-plan, sprint, feature, ops-doc, inspection]
sprint: "inspect-agent-download"
status: draft
created: "2026-06-02"
---

# [개발계획서] 점검 수집모듈 다운로드 제공 (inspect-agent-download)

- **작성팀**: 개발팀
- **작성일**: 2026-06-02 (회사 PC)
- **근거 기획서**: [[inspect-agent-download]] **v0.4 (사용자 최종승인 2026-06-02)** — codex 1·2차 + 디자인팀 자문 반영
- **상태**: draft v1 — codex 검토 대기
- **다음 단계**: codex 검토 → 사용자 승인 → 구현

---

## 0. 전제 / 환경

### 0-1. 패키징 제약 (핵심)
- 수집모듈 `inspection-poc/agent-windows/` 는 **리포 루트(= `src/main/resources` 밖)** → 빌드 산출물에 자동 포함 안 됨. **`maven-assembly-plugin` 으로 zip 생성 후 classpath 주입** 필요.
- ⚠ WAR 생성(`war:war`)이 `package` phase → assembly 는 **`prepare-package`** 에 바인딩해 zip 이 `target/classes/agent/` 에 **WAR 패키징 전** 들어가게 함(순서 보장, codex 2차).
- 결과 경로: `WEB-INF/classes/agent/inspect-agent-<version>.zip` → 컨트롤러가 `ClassPathResource("agent/...")` 로 서빙.

### 0-2. 기존 자산 재사용 (조사 결과)
| 자산 | 현황 | 활용 |
|---|---|---|
| `DocumentController.getAuth()` (L99) | `isAdmin()?"EDIT":authDocument?:"NONE"` | 다운로드 권한 게이트 동일 패턴 복제 (NFR-1) |
| `DocumentController` 파일 다운로드 (PDF/Excel L492~) | `ResponseEntity<byte[]>`+`Content-Disposition` | 다운로드 엔드포인트 패턴 재사용 (FR-3) |
| `OpsDocController` (`/ops-doc/list`) | `isAdmin()`(L175)·`CustomUserDetails` 보유 | 신규 컨트롤러 동일 ops 패키지 + 헤더 진입점 추가 (FR-4) |
| `ops-doc/list.html` | `.page-header`·`.list-section` 카드 패턴 | 다운로드 페이지 레이아웃 계승 (디자인 자문) |
| `design-system.css` 토큰 | `--primary/--surface/--text2/--font-mono/--shadow-card` 등 | 신규 토큰 0, 토큰 전용 (NFR-3) |
| `agent-windows/manifest.json` | **런타임 실행순서 manifest (version=1)** | **배포용으로 쓰지 않음** — 별도 VERSION/release-manifest 신설 |

---

## 1. 작업 순서

### Step 1 — 수집모듈 측 산출물 (agent-windows)
**1-1.** `inspection-poc/agent-windows/VERSION` 신규 — 배포 버전 SoT(예: `0.2.0`). 단일 라인.
**1-2.** `inspection-poc/agent-windows/config/site.example.json` 신규 — 실 site config(`site.gj.json`/`site.dyg.json`) 구조의 **마스킹 샘플**(시크릿·실주소 제거). 배포 zip 엔 이것만 포함.
- 기존 `manifest.json`(런타임) 미변경.

### Step 2 — 빌드 패키징 (pom.xml + assembly descriptor)
**2-1.** `pom.xml` `<properties>`: `<project.build.outputTimestamp>` 고정값(재현성) + agent 버전 property(= VERSION 파일값, `properties-maven-plugin` 으로 로드 또는 수동 동기).
**2-2.** `src/assembly/inspect-agent.xml` descriptor:
- `<baseDirectory>` = `inspection-poc/agent-windows`, format `zip`.
- **exclude**: `config/site.gj.json`, `config/site.dyg.json`, `output/**`, `**/*.log`, temp.
- **include**: 그 외 전체 + `config/site.example.json` + `VERSION` + `release-manifest.json`.
- `<outputDirectory>` = `${project.build.outputDirectory}/agent` (= target/classes/agent).
**2-3.** `maven-assembly-plugin` **버전 고정** + execution `phase=prepare-package`, `finalName=inspect-agent-${agent.version}`, reproducible(outputTimestamp 적용, entry 정렬).
**2-4.** **release-manifest.json 생성** — `gmavenplus-plugin`(groovy) 등으로 `generate-resources`~`prepare-package`(assembly 직전)에 agent-windows 파일 walk → `{version, buildTimestamp(=outputTimestamp, 결정적), files:[{path, sha256}]}` 생성 → assembly include. (buildTimestamp 는 비결정 `now()` 금지 — 재현성 위해 outputTimestamp 사용)
**2-5.** zip 자체 SHA-256 사이드카: `inspect-agent-<ver>.zip.sha256` 생성(페이지 표기용) — checksum plugin 또는 동일 groovy 단계.

### Step 3 — 다운로드 컨트롤러 (신규 `InspectAgentController`, `controller/ops` 패키지)
**3-1.** 권한 헬퍼 — `DocumentController.getAuth()` 동등(`isAdmin()?EDIT:authDocument?:NONE`). `NONE` 차단(NFR-1).
**3-2.** `@GetMapping("/ops-doc/inspect-agent")` (페이지) — `getAuth()=="NONE"` 시 403/로그인. model: `version`·`sha256`·`fileCount`·`buildTimestamp` (classpath 의 `agent/VERSION`·`release-manifest.json`·`.sha256` 파싱). → `templates/ops-doc/inspect-agent.html`.
**3-3.** `@GetMapping("/ops-doc/inspect-agent/download")` — `ClassPathResource("agent/inspect-agent-"+version+".zip")`. 200 + `Content-Type: application/zip` + `Content-Length` + `Content-Disposition: attachment; filename*=UTF-8''inspect-agent-<ver>.zip`. **리소스 미존재 404** / 권한 미달 403 (FR-3).
**3-4.** 버전·메타는 기동 시 1회 로드 캐시(매 요청 재파싱 회피).

### Step 4 — 다운로드 페이지 템플릿 (`templates/ops-doc/inspect-agent.html`) — 디자인 자문 준수
- `fragments/top-nav` + `.container{max-width:900px}`.
- `.page-header`(제목 `fa-box` + "← 목록으로") / `.agent-card`(모듈명·버전배지·설명·빌드시각·파일수·`fa-shield-halved` SHA-256 mono+복사·`fa-download` CTA) / `.agent-guide`(설치요약 + 상세 가이드 링크 [[inspection-agent-v2-setup]]).
- **토큰 전용**(raw hex 금지), 라벨=`var(--text2)`(`--muted` 금지), CTA=`var(--primary)`+#fff, 체크섬=`var(--font-mono)`/`var(--surface-2)`, radius/shadow=`var(--r-card)`/`var(--shadow-card)`.
- 클래스 스코프 selector·`!important` 미사용·페이지 `:root` 재정의 금지·전용 dark 블록 미추가(토큰 자동 대응).
- 복사 버튼 `aria-label` + 완료 피드백, CTA 포커스 `var(--ring)`.

### Step 5 — 진입점 (`ops-doc/list.html`)
- `.header-actions` 에 `<a href="/ops-doc/inspect-agent" class="btn-primary"><i class="fas fa-box"></i> 수집모듈 다운로드</a>` 추가(드롭다운 옆). 기존 동작 무영향.

### Step 6 — 테스트 / 검증
- **빌드**: `clean package` 후 `ROOT.war` 내 `WEB-INF/classes/agent/inspect-agent-<ver>.zip` + `.sha256` + (zip 내부) `release-manifest.json`·`VERSION`·`site.example.json` 존재 / `site.gj.json`·`site.dyg.json` **미포함** (T-4).
- **재현성(NFR-5)**: `clean package` 2회 → 두 zip **바이트 동일**(sha 비교) (T-6).
- **다운로드(T-1~T-3b,T-8)**: 페이지 200(버전·체크섬·가이드) / 다운로드 200 `application/zip`+`Content-Length` / 미인증 redirect / `authDocument==NONE` 403 / 산출물 누락 시 404.
- **무결성(T-5)**: 페이지 SHA-256 == 실제 zip == `.sha256`. release-manifest 파일해시 일치.
- **회귀/진입점(T-7, NFR-4)**: `/ops-doc/list` 링크 이동·기존 목록 정상.
- 검증은 codex 위임(테스트), Claude 는 빌드 산출물·다운로드 스모크만.

---

## 2. FR / NFR / T 커버리지 매핑
| 기획서 | 구현 Step |
|---|---|
| FR-1 (빌드 zip·prepare-package·exclude) | Step 2-1~2-3 |
| FR-2 (전용 페이지) | Step 3-2, Step 4 |
| FR-3 (다운로드 엔드포인트·헤더·404) | Step 3-3 |
| FR-4 (목록 진입점) | Step 5 |
| FR-5 (VERSION 전용 버전) | Step 1-1, 2-1, 3-4 |
| FR-6 (release-manifest·체크섬) | Step 2-4/2-5, 3-2 |
| NFR-1 (권한 이중) | Step 3-1 |
| NFR-2 (WAR 크기) | Step 6 실측 |
| NFR-3 (디자인 토큰·대비·다크·specificity) | Step 4 |
| NFR-4 (회귀) | Step 5, 6 |
| NFR-5 (재현성) | Step 2-1/2-3/2-4, T-6 |
| T-1~T-8 | Step 6 |

## 3. 롤백
- 순수 추가 sprint — 커밋 단위 revert 안전. 제거 대상: pom assembly/groovy execution + `src/assembly/inspect-agent.xml`, `InspectAgentController`, `ops-doc/inspect-agent.html`, list.html 링크 1줄, `agent-windows/VERSION`·`site.example.json`·(빌드생성)release-manifest.
- DB 변경 없음.

## 4. 미해결/구현 시 확정
- release-manifest 생성 플러그인 최종 선택(gmavenplus vs antrun+script) — 구현 시 PoC.
- `site.example.json` 마스킹 항목 — 실 site config 구조 확인 후 시크릿/실주소 제거 범위 확정.
- agent 버전 property 와 VERSION 파일 동기 방식(properties-maven-plugin vs 수동).

## 5. 변경 이력
- **2026-06-02 v1** (회사 PC) — 기획서 v0.4 최종승인 기반 초안. prepare-package 패키징·VERSION/release-manifest 분리·재현성·권한 이중·디자인 자문 반영. 다음 = codex 검토.
