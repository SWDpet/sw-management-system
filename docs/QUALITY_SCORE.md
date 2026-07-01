# QUALITY_SCORE.md — 품질 등급 (차원별 + 패키지별)

> ✅ **2026-06-23 코드 대조 재채점** — §6 품질 헌장 작업(Map→DTO·거대클래스 분리·게이트 도입·PIT) 반영. 등급은 "스냅샷 선언"이 아니라 **게이트가 강제하는 불변식**으로 관리(`QUALITY_CHARTER.md` §0).

---

## 0. 활성 게이트 (등급을 강제하는 불변식)

| 게이트 | 강제 대상 | 현재 |
|---|---|---|
| JaCoCo ratchet | `com/swmanager` **LINE≥78%/INSTRUCTION≥64%** floor (실측 ~81%/66%, `mvn verify`) | ✅ |
| Map 부채 ratchet (`MapDebtRatchetTest`) | 무타입 `Map<String,Object>` 총량 감소만 (baseline **188**) | ✅ |
| 거대클래스 ratchet (`GiantClassRatchetTest`) | 컨트롤러>1500·서비스>2000줄 신규 0 (baseline **비움=부채 0**) | ✅ |
| PIT 뮤테이션 게이트 (`-Ppit`) | 고신호 **15종** KILLED/TOTAL≥**94%** (현 96.4%) | ✅ |
| 아키텍처 불변식 (`LayeredArchitectureTest`) | 순환의존 0·도메인 순수성·상향의존 금지·Repo=인터페이스·명명 (위반 0) | ✅ |
| 컨트롤러→Repo ratchet (`ControllerRepositoryRatchetTest`) | 직접접근 신규 0 (baseline **303**, license4j 연동분 반영) | ✅ |
| Enum/Master sync (arch test) | 마스터 drift 0 | ✅ |
| **CI 자동 강제 (GitHub Actions `ci.yml`)** | **위 전 게이트를 매 push/PR 자동 강제** (gates=verify·fresh-init-smoke·mutation 3 job) | ✅ |
| fresh-init smoke (CI) | 빈 postgres:16 에 부트스트랩 DDL replay + Testcontainers `BootstrapSchemaContainerTest` | ✅ |
| 의존성 CVE 스캔 (OWASP, scheduled) | 주1회 dependency-check. **첫 완주 완료(2026-07-01, NVD 정식키) → CVSS≥9 critical 0**(억제 근거 = derby read-only). OSS Index 분석기는 익명 401 로 비활성(NVD 정본) | ✅ |

## 1. 평가 기준

| 등급 | 정의 |
|------|------|
| 🟢 **S** (beyond-A) | A 충족 + **게이트가 CI 에서 자동 강제** + 뮤테이션/통합 등 심화 검증(일반 A 초과) |
| 🟢 A | 감사 지적 0, 테스트 커버리지 있음, Enum/Master 활용, 게이트로 회귀 봉인 |
| 🟡 B | 감사 지적 1~2건 또는 매직넘버·리터럴 일부 잔존 |
| 🔴 C | 감사 지적 3+ 또는 주요 하드코딩 다수 |

---

## 2. 차원별 등급 (S-tier 로드맵)

> **2026-06-30 codex×Claude 재평가** (owner-edit-guard 전도메인 확대 + PIT deny-path 보강 후). 두 평가자 **독립 채점 → 종합 A+ 수렴**. S=beyond-A(게이트 자동강제로 일반 A 초과). 단일 병목=코드품질 B+(MapDebt 188 plateau). 이전 2026-06-29 표기 대비: SQL/운영 S−→**A+ 보정**(codex 재검토: V*.sql 전체 replay 부재 + DB enum/master sync 테스트가 기본 CI에서 skip이라 S/S− 근거 약함).

| 차원 | 등급 | 근거 |
|------|------|------|
| SQL/데이터접근 | 🟢 **A+** | 전건 파라미터 바인딩 + fresh-init smoke(CI) + Testcontainers 부트스트랩 검증. ⚠ V*.sql 전체 replay 미검증 + DB enum/master sync 테스트가 `RUN_DB_TESTS` 게이트(기본 CI skip) → S 보류 |
| 보안 | 🟢 **A+ (→S 근접)** | 인증/권한 가드 + **CSRF 정식화** + 마스킹 + **owner-edit-guard 전도메인** + **dependency-check 첫 완주(NVD 정식키) + CVSS≥9 critical 12→0 대응(Boot 3.5.16 상향+명시버전+derby 억제, 2026-07-01)**. S=dependency-check push/PR 차단(현 scheduled) + owner-guard 상시 PIT 게이트화 + 잔여 HIGH(log4j-api 등, 실행경로 없음) 후속 |
| 테스트 | 🟢 **S** | **beyond-A** — JaCoCo floor(실측 ~81%/66%)·**PIT 15종 게이트(실측 96%, threshold 93)**·골든·Enum + **CI 매 push 강제(3 job)** + **MockMvc net** + Testcontainers + **owner-guard deny 테스트 +9**(소유권 가드 라인 42 뮤테이션 1회성 측정 전건 KILLED) |
| 문서 | 🟢 **A** | 라이브 레퍼런스 코드 대조 + QUALITY_SCORE/SECURITY/RELIABILITY 현행화. ⚠ 게이트 수치 stale 보정(JaCoCo floor 18/14→**78/64 실측**, controller→repo 295→**303**) — 본 재평가에서 수정 |
| 코드품질 | 🟡 **B+** | DTO/record 전환 진행 + **plateau**(MapDebt baseline 188 = Excel/HWPX/JSONB 동적데이터·legacy envelope·@RequestBody Map 보존군). A는 응답계약 변경(프론트 동반) 필요 — codex·Claude "0으로 만들지 말 것" |
| 아키텍처 | 🟢 **A** | 거대클래스 부채 0 + **레이어 불변식 5종 게이트(CI 강제)** + controller→repo ratchet(303). ⚠ 소유권 판정이 정책/서비스 계층으로 수렴 안 하고 **컨트롤러별 헬퍼로 분산**(codex 지적) → 향후 정책계층 통합 후보 |
| 운영/CI 재현성 | 🟢 **A+** | push/PR CI 3 job(verify·fresh-init·PIT)·fresh-init postgres service·의존성스캔 scheduled·timeout/concurrency. NVD 외부의존·Docker/Testcontainers 의존·V*.sql replay 백로그로 S 보류 |

## 2-1. 패키지별 평가

| 패키지 | 등급 | 근거 |
|--------|------|------|
| `constant/enums/`, `constants/` | 🟢 A | Enum 체계화(+OpsDocType), MenuName |
| `service/` (core), `response/` | 🟢 A | LogService fail-soft, ApiResult 표준 envelope |
| `controller/` | 🟡 B+ | 거대클래스 분리 완료·ApiResult 부분 이관 + **standalone MockMvc net 3종(Sw·Quotation·Document)으로 HTTP 표면 박제**. 잔여 도메인키 Map(응답계약) |
| `dto/` | 🟡 B | record/DTO 화 진행(§6-4), 요청바디·jsonb 보존군 잔존 |
| `quotation/` | 🟡 B | S3/S8/S8-C 완료, S8-B 보류 |
| `domain/workplan/`, `domain/ops/` | 🟢 A | S1/S10/S16 + ops-fault-support 정규화 |
| `geonuris/`, `license/`, `qrcode/` | 🟡 B | 영구 패스 정책 → 평가 유보 |
| `util/` (SensitiveMask) | 🟢 A | 마스킹 정책 확정 (S3B), PIT 94%+ |

---

## 3. 감사 로드맵 완료 현황

전체 12 스프린트 완료 (Wave 1~4 + S8-C). 세부 로드맵: `docs/design-docs/data-architecture-roadmap.md`.

| Wave | 스프린트 수 | 상태 |
|------|------------|------|
| Wave 1 (P1) | 5 | ✅ |
| Wave 2 (대형) | 1 | ✅ |
| Wave 3 (P2) | 3 | ✅ |
| Wave 4 (P3) | 3 (S15/S16/S8-C) | ✅ |

---

## 4. 지속 관리 포인트

- **리터럴 하드코딩**: Enum 도입 이후 `rg` 기반 회귀 검증 (NFR-4 패턴)
- **마스터 drift**: `WorkPlanMstEnumSyncTest` 등 CI 게이트 패턴 확산 권장
- **문서 링크**: `docs/generated/` 산출물 정기 갱신

---

*Last updated: 2026-07-01 · dependency-cve-upgrade 스프린트: OWASP dependency-check 첫 완주(NVD 정식키) → **CVSS≥9 critical 12→0**. Spring Boot 3.2.1→3.5.16(BOM 일괄: tomcat/spring/postgresql/thymeleaf/jackson 등)+poi 5.5.1·jfreechart 1.5.6·pdfbox/fontbox/xmpbox 2.0.36+derby 억제(read-only 근거). PIT plugin 1.15→1.25.5(JUnit 5.12 호환)·logback rolling policy 마이그레이션. 전 게이트 green·1638 테스트·스모크 통과. 보안 A+→S 근접.*
*2026-06-30 · codex×Claude 재평가(owner-guard+PIT deny): 종합 A+ 수렴. 게이트 stale 수치 보정(JaCoCo floor 78/64·ctrl→repo 303). 등급=게이트 강제 불변식*
