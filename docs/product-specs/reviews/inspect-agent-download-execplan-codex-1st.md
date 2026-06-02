# codex 검토 — inspect-agent-download 개발계획서 v1

- 검토일: 2026-06-02 (회사 PC) · codex v0.136.0 (gpt-5.5, read-only)
- 대상: `docs/exec-plans/inspect-agent-download.md` v1 (기획서 v0.4 근거)

**최종판정 = ⚠수정필요** (방향은 승인 근접. v1 그대로 구현 시 빌드 산출물 조회·assembly 설정에서 실패 가능)

## 주요 지적 (codex 실파일 확인)

1. **페이지 메타 로딩 경로 ↔ 패키징 불일치** — 컨트롤러(Step 3-2)는 classpath `agent/VERSION`·`agent/release-manifest.json`·`.sha256` 파싱 전제인데, Step 2/6 은 VERSION·release-manifest 를 **zip 내부에만** 포함 → `ClassPathResource("agent/VERSION")` 미존재로 페이지 메타 깨짐. **→ VERSION·release-manifest·.sha256 을 `target/classes/agent/` 에 sidecar 로도 배치**(또는 컨트롤러가 zip entry 직접 read).

2. **assembly descriptor 문법 혼동** — `<baseDirectory>` 는 "zip 내부 최상위 디렉터리명"이지 소스 폴더 지정이 아님. 소스는 `<fileSet><directory>${project.basedir}/inspection-poc/agent-windows</directory>`. `<outputDirectory>` 는 descriptor 루트가 아니라 **plugin configuration** 파라미터. **`appendAssemblyId=false`** 미설정 시 파일명이 `inspect-agent-<ver>-<assemblyId>.zip` 로 어긋남.

3. **zip checksum 생성 순서 불명확** — zip 자체 SHA-256 은 assembly 가 zip 을 **만든 후**에만 계산 가능. → release-manifest = assembly **전**, `.zip.sha256` = assembly **후 + WAR 생성 전**. 둘 다 prepare-package 에 두되 execution 선언 순서 명확화.

4. **release-manifest 자기 자신 해시 순환** — 파일 walk 에 `release-manifest.json` 포함 시 자기 SHA-256 을 담아야 하는 순환. **→ manifest 는 파일해시 대상에서 제외** 명시.

5. **exclude ↔ 실제 폴더 불일치** — 계획은 `output/**` 만 제외하나 `.gitignore` 엔 `inspection-poc/agent-windows/out/`·`snapshots/` 존재. **→ `out/**`·`snapshots/**`·`*.trace.log`·`*.tmp` exclude 추가**.

## 커버리지 / 타당성
- FR-1~6/NFR-1~5 대부분 Step 1~6 매핑. 단 #1 로 FR-2/FR-6 페이지 표시가 현 계획만으론 미성립, #2~4 로 FR-1/FR-6/NFR-5 구현 세부 보강 필요.
- **방향 타당**: prepare-package assembly → target/classes/agent zip, `ClassPathResource` 서빙 + `authDocument != NONE` 이중 게이트 모두 현 구조상 OK. Maven 설정 위치·sidecar 배치만 정정하면 됨.

## 롤백
- DB 무변경, 신규 중심이라 방향 타당. 단 pom 변경은 ROOT.war 빌드 전체 영향 → **assembly/checksum execution 제거 후 `clean package` 검증**을 롤백 절차에 포함 권장.

## → 최종승인 조건
위 5건(특히 #1 sidecar 배치, #2 descriptor 문법, #3 순서, #4 self-exclude, #5 exclude 보강) 반영 시 승인 가능.
