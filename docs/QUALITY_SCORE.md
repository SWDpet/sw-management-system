# QUALITY_SCORE.md — 패키지별 품질 등급

> ⚠ **자동 생성 초안 — 검증 필요**
> 근거: `docs/generated/audit/dashboard.md` 요약 + 패키지 트리

---

## 1. 평가 기준

| 등급 | 정의 |
|------|------|
| 🟢 A | 감사 지적 0, 테스트 커버리지 있음, Enum/Master 활용 |
| 🟡 B | 감사 지적 1~2건 또는 매직넘버·리터럴 일부 잔존 |
| 🔴 C | 감사 지적 3+ 또는 주요 하드코딩 다수 |

---

## 2. 패키지별 평가 (2026-04-24 기준)

| 패키지 | 등급 | 근거 |
|--------|------|------|
| `constant/enums/` | 🟢 A | S9/S10/S16/S8/S8-C 로 Enum 체계화 완료 |
| `constants/` | 🟢 A | MenuName 16개 (S9 완료) |
| `service/` (core) | 🟢 A | S5/S9 완료, LogService fail-soft |
| `controller/` | 🟡 B | 일부 컨트롤러 직접 문자열 참조 — S9 이후 감소 |
| `dto/` | 🟡 B | WorkPlanDTO switch → Enum 위임 (S16) 완료, 나머지 유지 |
| `quotation/` | 🟡 B | S3/S8/S8-C 완료, S8-B 보류 |
| `domain/workplan/` | 🟢 A | S1/S10/S16 리팩터 완료 |
| `geonuris/`, `license/`, `qrcode/` | 🟡 B | 영구 패스 정책 → 평가 유보 |
| `util/` (SensitiveMask) | 🟢 A | 마스킹 정책 확정 (S3B) |

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

*Last updated: 2026-04-24 · docs-renewal-01 P1*
