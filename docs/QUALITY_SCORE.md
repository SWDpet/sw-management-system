# QUALITY_SCORE.md — 품질 등급 (차원별 + 패키지별)

> ✅ **2026-06-23 코드 대조 재채점** — §6 품질 헌장 작업(Map→DTO·거대클래스 분리·게이트 도입·PIT) 반영. 등급은 "스냅샷 선언"이 아니라 **게이트가 강제하는 불변식**으로 관리(`QUALITY_CHARTER.md` §0).

---

## 0. 활성 게이트 (등급을 강제하는 불변식)

| 게이트 | 강제 대상 | 현재 |
|---|---|---|
| JaCoCo ratchet | `com/swmanager` LINE≥18%/INSTRUCTION≥14% floor (`mvn verify`) | ✅ |
| Map 부채 ratchet (`MapDebtRatchetTest`) | 무타입 `Map<String,Object>` 총량 감소만 (baseline **188**) | ✅ |
| 거대클래스 ratchet (`GiantClassRatchetTest`) | 컨트롤러>1500·서비스>2000줄 신규 0 (baseline **비움=부채 0**) | ✅ |
| PIT 뮤테이션 게이트 (`-Ppit`) | 완전커버 7종 KILLED/TOTAL≥90% (현 95%) | ✅ |
| 아키텍처 불변식 (`LayeredArchitectureTest`) | 도메인 순수성·상향의존 금지·Repo=인터페이스·명명 (위반 0) | ✅ |
| 컨트롤러→Repo ratchet (`ControllerRepositoryRatchetTest`) | 직접접근 신규 0 (baseline **295**) | ✅ |
| Enum/Master sync (arch test) | 마스터 drift 0 | ✅ |

## 1. 평가 기준

| 등급 | 정의 |
|------|------|
| 🟢 A | 감사 지적 0, 테스트 커버리지 있음, Enum/Master 활용, 게이트로 회귀 봉인 |
| 🟡 B | 감사 지적 1~2건 또는 매직넘버·리터럴 일부 잔존 |
| 🔴 C | 감사 지적 3+ 또는 주요 하드코딩 다수 |

---

## 2. 차원별 등급 (S-tier 로드맵)

| 차원 | 등급 | 근거 |
|------|------|------|
| SQL/데이터접근 | 🟢 **A** | 전건 파라미터 바인딩 (원천) |
| 보안 | 🟢 **A** | S1 — ops-doc 첨부/삭제 가드(`OpsDocControllerAttachmentGuardTest`) |
| 테스트 | 🟢 **A** (+beyond-A) | S3 JaCoCo·골든·DB게이팅 + PIT 뮤테이션 게이트 |
| 문서 | 🟢 **A** | 2026-06-23 라이브 레퍼런스 문서(ARCHITECTURE/FRONTEND/DESIGN/QUALITY_SCORE/PLANS/PRODUCT_SENSE/SECURITY/RELIABILITY) 코드 대조 검증·미검증 꼬리표 제거 |
| 코드품질 | 🟡 **B** | S4 완료. A는 응답 envelope 전면 이관 필요하나 **plateau**(Map 188, 잔여 `put("success")`은 P6 도메인키 응답=형태변경 없인 이관불가) |
| 아키텍처 | 🟡 **B+** | 거대클래스 부채 0·Excel/문서 분리 + **레이어 불변식 게이트화**(2026-06-23: 도메인순수성·상향의존금지·Repo인터페이스·명명 하드게이트 + controller→repo ratchet). 잔여 1: config↔service 순환(CustomUserDetails/SecurityLoginProperties를 security/로 이동 시 A — 세션영향 사용자 승인 대기) |

## 2-1. 패키지별 평가

| 패키지 | 등급 | 근거 |
|--------|------|------|
| `constant/enums/`, `constants/` | 🟢 A | Enum 체계화(+OpsDocType), MenuName |
| `service/` (core), `response/` | 🟢 A | LogService fail-soft, ApiResult 표준 envelope |
| `controller/` | 🟡 B | 거대클래스 분리 완료(Document/InspectReport/Excel)·ApiResult 부분 이관, 잔여 도메인키 Map |
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

*Last updated: 2026-06-23 · 코드 대조 재채점(문서 A 승급): 활성 게이트·차원별 등급·패키지 현행화. 등급=게이트 강제 불변식*
