# 개발계획서 — 커버리지 상향 (핵심 서비스 단위테스트)

- **기획서**: `docs/product-specs/coverage-core-services.md` (codex PASS)
- **상태**: 1차 증분 구현 중 · 2026-06-23
- **원칙**: mock 기반(운영DB 무관), 의미있는 단언, 프로덕션 코드 무변경.

---

## 1. P1 — QuotationServiceTest (1차 증분)
생성자 주입 7 deps 전부 @Mock 으로 서비스 직접 생성. 대상 메서드·케이스:

| 메서드 | 케이스 |
|---|---|
| `generateQuoteNumber` | 카테고리 코드맵(용역→SQ/제품→PQ/유지보수→MQ/미상→EQ) · seq 신규(1)/기존(+1) · 포맷 `UIT - %s - %d - %03d` · seqRepository.save 1회 |
| `previewNextNumber` | 신규→1 / 기존→+1 · **save 미호출** |
| `createQuotation` | **calcGrandTotal**: VAT미포함(+10%)·VAT포함(=원금)·낙찰율(floor)·절사(roundDown) 4종 · 견적번호 set · 대장 registerLedger 1회 |
| `updateQuotation` | 품목 교체 재계산 + 대장 동기화 · 미존재 → RuntimeException |
| `getQuotation` | 미존재 → RuntimeException |
| `copyPatterns` | 필드 복사 + null src skip + count |
| `saveWageRate` | 기존 존재(다른 id)→update / 신규→insert |
| `deleteQuotation` | 대장 삭제 + 견적 삭제 호출 |

## 2. 후속 증분 (별도)
- P2 DocumentService(필드주입 @InjectMocks, JdbcTemplate 폴백 제외) / P3 InspectReportService(SecurityContextHolder set/clear, resetAllInspectData 제외).

## 3. 게이트
- 1차 후 `jacoco.csv` 실측 → `pom.xml` LINE/INSTRUCTION `minimum` 을 달성치 −여유 로 상향(별 커밋). 번들 전역이라 소폭.

## 4. 검증
- 신규 테스트 그린 + 전체 스위트 그린(회귀 0) + codex 구현검증.

## 5. 롤백
- 신규 테스트·게이트 수치 revert. 프로덕션 무변경.
