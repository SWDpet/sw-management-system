# 기획서 — 커버리지 상향 (핵심 비즈니스 서비스 단위테스트)

> **상태**: DRAFT v1 · 작성 🧭기획팀 · 2026-06-23
> **워크플로우**: 기획서(본 문서) → 🔍codex 검토 → ✅사용자 승인 → 개발계획 → 🔍codex → 구현 → 🔍codex 검증 → 커밋
> **차원**: 품질 로드맵 **beyond-A**(테스트 차원 강화). 코드품질 A는 헌장상 plateau로 판정 → 실질 품질 향상 레버로 피벗(사용자 결정 2026-06-23).

---

## 1. 배경 · 문제

`com/swmanager` 라인 커버리지 **약 21.9%**(게이트 floor 18%). 핵심 비즈니스 로직 서비스가 거의 미검증:

| 서비스 | 미커버 라인 | 커버율 | 성격 |
|---|---|---|---|
| `QuotationService` | 205 | **0%** | 견적 산출·도메인 로직 |
| `DocumentService` | 127 | 2% | 문서 저장/조회 |
| `InspectReportService` | 122 | 18% | 점검내역서 |

→ 회귀 시 게이트가 못 잡음. 커버리지=실제 버그 포착 + 회귀 봉인.

**제외(타겟 아님)**: Excel/HWPX 생성 서비스(바이너리 출력 — 골든테스트 `ExcelSplitGoldenIT`로 별도 보호), license/geonuris(영구 패스 정책), 컨트롤러(통합테스트 영역, 비즈니스 로직은 서비스에 위치).

## 2. 목표 · 비목표

- **목표**: 핵심 비즈니스 서비스에 **mock 기반 단위테스트**(운영DB 무관) 추가 → 커버리지 상향 + **JaCoCo 라인 floor ratchet 상향**으로 회귀 봉인.
- **비목표**: 통합테스트(DB)·Excel 골든·license. 100% 커버(가치 낮은 getter/분기 제외).

## 3. 범위 (1차 증분)

- **P1 `QuotationService`**(0%, 205라인): 생성자 주입(@Mock repo/RemarksRenderer). `generateQuoteNumber`·총액 산출·ledger 동기화·패턴/wage 분기 + 경계(null/빈/잘못된 입력). **최적 타겟**(codex ⭕).
- **P2 `DocumentService`**(2%, 127라인): 저장/조회/상태 로직 mock 테스트. ⚠필드 주입이라 `@InjectMocks`/리플렉션 필요. **제외**: `enrichInspectMonth` 의 `JdbcTemplate` 백업테이블 폴백(SQL/환경 결합 — codex).
- **P3 `InspectReportService`**(18%→상향): 미커버 메서드 보강. `currentUser()` 는 `SecurityContextHolder` set/clear 로 테스트. **제외**: `resetAllInspectData()`(파괴적 일괄삭제 — mock이어도 커버 타겟 금지, codex).
- **게이트**: 1차 증분 후 실측 라인 커버율로 JaCoCo `minimum`(현 LINE 0.18 / INSTRUCTION 0.14)을 **달성치 −1~2%p 여유** 로 상향(ratchet 철학 — 감소 불가). ⚠게이트는 `com/swmanager/**` **번들 전역**이라 3클래스 추가는 전역을 **소폭만** 올림(보수적 상향, codex).

## 4. 비기능 · 원칙

- **NFR-1 운영DB 무접촉**: 전부 mock(@Mock repository). `RUN_DB_TESTS` 불필요 → 기본 `mvn test`에서 실행·CI 안전.
- **NFR-2 의미있는 단언**: 단순 라인 통과가 아니라 동작·경계·예외 검증(PIT 친화).
- **NFR-3 회귀 0**: 기존 테스트·동작 불변. 신규 테스트만 추가.

## 5. 검증 계획

- 신규 테스트 그린 + 전체 단위 스위트 그린.
- JaCoCo 라인 커버율 상승 확인(`target/site/jacoco/jacoco.csv` 필터).
- 상향된 floor로 `mvn verify` 통과.

## 6. 리스크 · 롤백

- R1: 서비스 메서드가 mock 곤란한 정적/외부 의존 → 해당 메서드는 범위 제외(런타임 의존은 통합테스트 영역).
- R2: floor 과다 상향 → 향후 정당한 신규 미커버 코드가 막힘 → **달성치 −1~2%p 여유** 두고 상향.
- 롤백: 신규 테스트·floor 변경 revert. 프로덕션 코드 무변경.
