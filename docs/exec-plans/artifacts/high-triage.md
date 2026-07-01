# High/잔존 CVE Triage — dependency-cve-upgrade (P4b)

> 2026-07-01 · 상향(Boot 3.5.16 + poi/jfreechart/pdfbox) + derby 억제 후 재스캔 결과
> **CVSS≥9 critical = 0 달성** (before 12개 라이브러리 critical → 0)

## 1. before/after 요약

| 구분 | before(3.2.1) | after(3.5.16+명시상향+억제) |
|---|---|---|
| CVSS≥9 critical | **12개 라이브러리**(tomcat 11·spring·postgresql·thymeleaf·derby·jfreechart 등) | **0** ✅ |
| HIGH(7.0~8.9) | (다수) | **5건**(log4j-api 4 + angus-activation 1) |
| medium 이하 | 다수 | 7건 |

## 2. HIGH(7.0~8.9) triage — 5건

| artifact | CVE | CVSS | 상향 가능 | runtime 영향 | 조치 |
|---|---|---|---|---|---|
| log4j-api 2.24.3 | CVE-2026-34478 | 7.5 | ❌ 이미 최신(Boot 3.5.16 BOM 관리) | **낮음** — 앱은 **logback** 사용, **log4j-core 부재**(deptree 확인). log4j-api 는 transitive API 표면만, 로깅 백엔드 미동작 | **백로그**(패치 릴리스 시 BOM 상향으로 자동 해소). 실행경로 없어 실위험 낮음 |
| log4j-api 2.24.3 | CVE-2026-34479 | 7.5 | ❌ 동일 | 동일 | 동일 |
| log4j-api 2.24.3 | CVE-2026-34480 | 7.5 | ❌ 동일 | 동일 | 동일 |
| log4j-api 2.24.3 | CVE-2026-34481 | 7.5 | ❌ 동일 | 동일 | 동일 |
| angus-activation 2.0.3 | CVE-2025-7962 | 7.5 | ❌ 이미 최신(BOM 관리) | 낮음 — Jakarta Activation impl(transitive), 파일 첨부/메일 경로 간접 | **백로그**(패치 릴리스 시 BOM 상향) |

**공통**: 5건 모두 **이미 최신 관리버전**이라 현재 상향으로 해소 불가(패치 미출시). 억제하지 않고 **백로그**로 명시 추적 — 게이트(CVSS≥9)는 미차단, 향후 Boot 패치 상향 시 자동 해소. log4j 4건은 log4j-core 부재로 실행경로가 없어 실위험 낮음.

## 3. jfreechart 개별 판정 (codex 요청)

| CVE | before(1.5.4) | after(1.5.6) |
|---|---|---|
| CVE-2024-22949 (9.1, critical) | 검출 | **해소** ✅ (1.5.6 상향, 잔존 0) |
| CVE-2023-52070 | 검출 | **해소** ✅ |
| CVE-2024-23076 (disputed) | 검출 | **해소** ✅ (1.5.6 에서 CPE 미매칭) |
| CVE-2024-23077 (disputed) | 검출 | **해소** ✅ |

→ jfreechart 는 **1.5.6 상향으로 전건 해소**, 억제 불필요(disputed 억제도 불필요).

## 4. medium 이하(<7) — 7건 (게이트 무관, 참고)

| artifact | CVE | CVSS |
|---|---|---|
| log4j-api 2.24.3 | CVE-2026-34477 | 5.9 |
| jackson-databind 2.21.4 | CVE-2026-54515 | 5.3 |
| hibernate-validator 8.0.3 | CVE-2025-15104 | 5.3 |
| commons-lang3 3.17.0 | CVE-2025-48924 | 5.3 |
| log4j-api 2.24.3 | CVE-2025-68161 | 4.8 |
| pdfbox 2.0.36 | CVE-2026-33929 | 4.3 |
| xmpbox 2.0.36 | CVE-2026-33929 | 4.3 |

전부 최신 관리/명시버전. 패치 미출시분은 백로그, 향후 상향 시 해소.

## 5. 억제 현황

| 억제 | CVE | 사유 |
|---|---|---|
| derby 10.14.2.0 | CVE-2022-46337(9.8) | 임베디드 read-only, network/LDAP 미사용(grep 실증), 10.10 호환 게이트로 버전 고정 → 적용불가 |

- **오탐 CPE 억제(P3b) 불필요**: critical 0 이 상향+derby 억제만으로 달성됨(오탐 광역억제 없이). `web_project:web` 등은 잔존 critical 목록에 없어 억제 대상 아님.

## 6. 결론

- **CVSS≥9 critical: 12개 라이브러리 → 0** (게이트 통과, BUILD SUCCESS).
- 잔존 HIGH 5건 = 이미 최신버전, 패치 미출시 → 백로그(log4j 는 실행경로 없음).
- 억제 = derby 1건(근거 실증), 오탐 광역억제 0.
- **후속**: Boot 정기 패치 상향 시 log4j-api/angus/jackson 등 HIGH·medium 자동 해소 예상.
