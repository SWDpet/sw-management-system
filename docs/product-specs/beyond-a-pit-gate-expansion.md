# [기획서] PIT 뮤테이션 게이트 확장 7→11 + threshold 90→94 (beyond-A)

- **작성팀**: 기획팀 / **작성일**: 2026-06-28
- **트랙**: beyond-A track 1 (전부 진행). PIT 게이트 스코프 확장 + ratchet. [[project_swmanager_all_A_roadmap]] PIT 백로그.
- **상태**: ✅ 구현 완료(2026-06-28). PIT 게이트 7→11클래스(+ExceptionMessages·QrBatchPayloadAdapter·RuleKbMatcher·AuthSummary[강화 72→92%]), threshold 90→**93** ratchet. **PIT 180/188=95.7%≥93 BUILD SUCCESS**(test strength 97%), 정규 `mvnw -o clean verify` green(AuthSummaryTest 5). HwpxXmlSupport(50%) 제외. codex 검증 NEEDS-FIX(통계주석만, 코드 sound)→reconcile 주석+테스트주석 완화 반영. dual-review(codex0/Opus4) 합의1(주석 cross-commit, 비이슈)·분쟁3 중 threshold 브리틀 채택(94→93, floor정책 실측−2.5pp 일관), 나머지 refute. production 0.

---

## 1. 배경 / 목표
PIT 게이트는 고신호(NO_COV≤2) 완전커버 7클래스를 KILLED/TOTAL≥90 으로 동결 중(실측 97%). beyond-A 로 **고신호 클래스를 추가 편입**하고 threshold 를 ratchet 한다. 후보를 PIT 로 측정해 ≥92% killer 만 편입(약한 클래스는 제외).

## 2. 측정 결과 (PIT 실측, 후보)
| 클래스 | KILLED/TOTAL | 판정 |
|---|---|---|
| ExceptionMessages | 3/3 (100%) | ✅ 편입 |
| QrBatchPayloadAdapter | 18/19 (94%) | ✅ 편입 |
| RuleKbMatcher | 31/33 (93%) | ✅ 편입 |
| AuthSummary | 18/25 (72%)→**23/25(92%)** | ✅ **테스트 강화 후 편입** |
| HwpxXmlSupport | 53/105 (50%) | ❌ 제외(XML 정규식, 게이트 부적합) |

## 3. 범위
- **D1** `AuthSummaryTest` +1: `summarize_allTenFields_eachBadgeMappedInOrder` — 10 권한 전부 badge(EDIT/VIEW 교차) → `readAuth` 디스패치 각 필드 반환 mutation(EmptyObjectReturnVals L105-109) kill. 72→92%.
- **D2** pom PIT profile targetClasses/targetTests 에 ExceptionMessages·QrBatchPayloadAdapter·RuleKbMatcher·AuthSummary(+테스트) 추가. **게이트 7→11클래스.**
- **D3** threshold 90→**94** ratchet(실측 180/188=96%, 2pt 헤드룸).
- 잔존 noise: NO_COV 3(MaskingDetector 1·AuthSummary L110 default 도달불가 1·equivalent L90 1)+저가치 생존 5.

## 4. 요건
- **FR-1**: 고신호 4클래스 편입, AuthSummary 강화, threshold 94 ratchet.
- **NFR**: `mvnw -Ppit ...mutationCoverage` BUILD SUCCESS(180/188≥94), 정규 `mvnw -o clean verify` green(AuthSummary 신규테스트 포함), 구현 후 codex PASS + dual-review → 듀얼푸시.

## 5. 영향 / 리스크
- 변경: `pom.xml`(PIT profile)·`util/AuthSummaryTest.java`. production 코드 0.
- **R1 헤드룸 2pt**: 향후 생존 +3 시 game(178/188=94.7≥94). 신규 약한 클래스 편입 금지(NO_COV≤2 유지).
- **R2 PIT 미바인딩**: 기본 lifecycle 불변(profile 명시 goal 로만 실행) → 정규 CI 무영향.
- production 회귀 0(테스트 추가 + 게이트 강화).
