# [기획서] 의존성 취약점 스캔 — OWASP dependency-check scheduled workflow (beyond-A)

- **작성팀**: 기획팀 / **작성일**: 2026-06-29
- **트랙**: beyond-A track 4 (codex 자문 보안 순위). 의존성 CVE 스캔을 주1회 자동 + 수동.
- **상태**: ✅ 스캐폴드 구축 완료(2026-06-29). pom `security-scan` 프로파일(dependency-check-maven 12.1.0, failBuildOnCVSS=9) + `.github/workflows/dependency-scan.yml`(schedule 주1회+수동, NVD_API_KEY secret, 주차 캐시키, 리포트 artifact) + `dependency-check-suppressions.xml` scaffold + SECURITY.md §9 + .gitignore(.dependency-check-data). **로컬 실측 검증**: `mvnw -Psecurity-scan ...check` 가 dependency-check:12.1.0 실행→NVD 361,526 records 다운로드 시작(config 유효, 키 없어 "VERY long time" 경고=degrade 동작 확인). pom validate(프로파일 on/off) green. codex 사전검토(미구현글리치+failBuildOnCVSS=9 정제 채택). dual-review(합의6 중 캐시키 주차화·cancel false·pom newline 채택, NVD literal은 워크플로 env+실측 무영향, 분쟁2 refute). ⚠**첫 실제 완주 검증은 사용자가 `NVD_API_KEY` secret 추가 후**(NVD 외부의존 — PIT/CI/CSRF 와 다른 한계).

---

## 1. 배경 / codex 권고
의존성(라이브러리)의 알려진 CVE 를 탐지. codex 자문: *"NVD API/캐시/느린 실행 때문에 메인 게이트(PR 차단)로 넣기엔 부담 → **주1회 scheduled workflow 또는 수동 workflow**가 적절."* → PR 게이트가 아닌 **별도 scheduled job** 으로 분리.

**핵심 제약 (정직)**:
- OWASP dependency-check 는 NVD 데이터 필요. **2023+ NVD 는 API 키 권장**(무료, nvd.nist.gov) — 키 없으면 첫 동기화가 심하게 rate-limit(수십분~수시간/실패 위험).
- → 워크플로는 `NVD_API_KEY` GitHub secret 사용 + NVD DB 캐시. **키 미설정 시 rate-limited(느림)로 degrade**.
- ⚠ **PIT/CI/CSRF 와 달리 본 트랙은 NVD 외부 의존**: 로컬에서 Actions+NVD 전체 동기화 검증 불가 → **첫 실제 실행 검증은 사용자가 NVD 키를 secret 에 추가한 뒤** 가능. 본 스프린트는 **스캐폴드(워크플로+플러그인+억제파일+문서) 구축**까지.

## 2. 범위
### D1 — pom `security-scan` 프로파일
- `dependency-check-maven` 플러그인(라이프사이클 미바인딩, PIT 처럼 프로파일 명시 실행). `mvn -Psecurity-scan org.owasp:dependency-check-maven:check`.
- 설정: 억제파일(`dependency-check-suppressions.xml`), 포맷 HTML+JSON, `nvdApiKey` = `${env.NVD_API_KEY}`, `failBuildOnCVSS` (scheduled 는 보고 위주 → 임계 높게/혹은 미차단).
- license 모듈(GeoNURIS jar) 등 오탐은 억제파일로.

### D2 — `.github/workflows/dependency-scan.yml`
- 트리거: `schedule`(주1회 cron) + `workflow_dispatch`(수동). **PR/push 미차단**(별도 job).
- NVD DB 캐시(actions/cache, ~/.m2 의 dependency-check-data), `NVD_API_KEY` secret 주입.
- 리포트 artifact 업로드(HTML/JSON). 타임아웃 넉넉히(첫 동기화 대비).

### D3 — 문서
- `docs/SECURITY.md`: 의존성 스캔 절차 + NVD 키 설정법(secret 추가) + 결과 확인법.

## 3. 요건
- **FR-1**: scheduled+수동 워크플로가 dependency-check 실행, 리포트 artifact 생성.
- **FR-2**: 로컬 `mvn -Psecurity-scan` 으로 동일 실행 가능(키 있으면 빠름).
- **NFR**: 메인 CI(PR 게이트) 무영향(별도 워크플로), 플러그인 버전 로컬 resolve 확인, YAML 정합, codex + dual-review. **첫 NVD 실행 검증은 키 추가 후(사용자)** — 한계 명시.

## 4. 영향 / 리스크
- 변경: pom 프로파일 추가 + 신규 워크플로 + 억제파일 + 문서. production 코드 0. 메인 빌드/게이트 무영향(프로파일/별도 워크플로).
- **R1 (핵심) NVD 키 의존**: 키 없으면 첫 실행 느림/실패 가능 → secret 문서화, 캐시로 완화. **본 트랙만 사후검증 불가**(나머지 3트랙은 CI green 실측).
- **R2 플러그인 버전**: Java17/Maven 호환 버전 선택 → 로컬 plugin resolve 로 사전확인.
- **R3 오탐**: 억제파일 scaffold 로 대응(초기 빈 파일, 첫 리포트 후 triage).
