# codex 3차 검토 — inspect-agent-download 개발계획서 v2

- 검토일: 2026-06-02 (회사 PC) · codex v0.136.0 (gpt-5.5, read-only)
- 대상: `docs/exec-plans/inspect-agent-download.md` v2

**최종판정 = ⚠수정필요** (직전 5건 대부분 해소. 단 신규 VERSION 로딩 리스크가 빌드 성공을 막을 수 있음 — 이것만 고치면 승인 가능)

## 직전 ⚠ 5건 판정
| # | 항목 | 판정 |
|---|---|---|
| 1 | sidecar 배치(ClassPathResource 메타) | **해소** — VERSION/release-manifest/.sha256 을 zip+sidecar 동시 생성, 컨트롤러 로딩 전제와 일치 |
| 2 | assembly 문법/위치 | **해소** — fileSet.directory·plugin outputDirectory·appendAssemblyId=false·finalName 정정. fileSet outputDirectory 생략 시 zip 루트 배치(의도와 일치) |
| 3 | prepare-package 실행순서 | **부분해소(조건부)** — properties→manifest→assembly→sha256 순서 맞음. **단 "POM 에 그 선언순서대로 넣는다"는 구현 조건부**. 현재 pom 에 해당 plugin 들 미존재 |
| 4 | self-hash 제외 | **해소** |
| 5 | exclude 보강 | **해소** (추가로 `**/*.tmp`·`**/*.trace.log` 재귀패턴 권장) |

## 신규 리스크
1. **★ VERSION 로딩 (빌드 차단 가능)** — `properties-maven-plugin read-project-properties` 는 `key=value` 파일을 읽음. `agent-windows/VERSION` 이 단일 라인 `0.2.0` 이면 `${agent.version}` 생성 불가 → assembly finalName 깨짐. **→ VERSION 을 `agent.version=0.2.0` properties 형식으로** 하거나 gmaven/antrun 으로 단일 라인 읽어 property 주입.
2. `site.example.json` 현재 미존재 — Step 1-2 에 생성 작업 있음. **구현 시 assembly include 검증 필수**.

## 결론
#3 만 구현 선언순서 조건부, 나머지 해소. **VERSION→Maven property 방식만 구체 정정하면 승인 가능 수준.**
