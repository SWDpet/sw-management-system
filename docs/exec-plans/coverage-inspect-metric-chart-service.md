# [개발계획] InspectMetricChartService emptyChart 경로 보강

- **작성팀**: 개발팀 / **작성일**: 2026-06-28 / **기획**: `docs/product-specs/coverage-inspect-metric-chart-service.md`
- **상태**: ✅ 구현 완료(2026-06-28). M1/M2 green. InspectMetricChartService 92.92%(miss 24→8), 전역 LINE 81.08%(81% 돌파). codex 구현검증 PASS·dual-review 합의2 반영(고정 instant·양 role verify). 상세는 기획서 상태줄.

---

## 0. 사전검증 (완료)
- `@RequiredArgsConstructor`(single final `InspectMetricSnapshotRepository repository`) → `new InspectMetricChartService(mock)`.
- renderChart: AP/DB 각 `repository.findHostsByPjtRoleRange(pjtId,role,since,until)`→hosts, host 별 `findRangeByPjtRoleHost(...)`→rows. dataset 0 → `emptyChart(...)`(L128-130).
- emptyChart(L197-216): BufferedImage(TYPE_INT_RGB)+Graphics2D drawString+ImageIO.write("png")→bytes. 외부의존 0(headless-OK).
- PNG 매직: `0x89 0x50 0x4E 0x47`(\x89PNG).

## 1. 테스트 매트릭스 (신규 `InspectMetricChartServiceTest`)

| # | 테스트 | 픽스처 | 단언 |
|---|---|---|---|
| M1 | renderChart_noHosts_returnsEmptyChartPng | findHostsByPjtRoleRange(any,any,any,any)→List.of() | renderChart 반환 byte[] 비어있지 않음 + PNG 매직 4바이트 |
| M2 | renderChart_hostsButNoRows_returnsEmptyChartPng | findHostsByPjtRoleRange→["host1"], findRangeByPjtRoleHost(any…)→List.of() | PNG 매직 + verify(repository, atLeastOnce()).findRangeByPjtRoleHost(...) (루프 진입·empty continue 경로) |

- mock matcher: OffsetDateTime 인자는 `any()` 또는 `any(OffsetDateTime.class)`. `eq(pjtId)`/`anyString()`(role) 혼용 시 전부 matcher.
- since/until 은 `OffsetDateTime.now().minusMonths(12)` / `now()` 임의값(분기 무관).
- PNG 매직 헬퍼: `assertThat(png).startsWith((byte)0x89,(byte)0x50,(byte)0x4E,(byte)0x47)` (AssertJ byte[] startsWith) 또는 인덱스 비교.

## 2. 검증 절차
1. `./mvnw -o clean verify` → BUILD SUCCESS, 신규 2 green.
2. `jacoco.csv` 합산 → InspectMetricChartService LINE ~78%→~94%(`.java.html` 재확인, 잔여=catch L145-147/L213-215 + 도달불가 L187).
3. 전역 delta, floor 유지(게인 작아 버퍼 흡수).
4. 구현 후 codex 검증 → dual-review → 듀얼푸시.

## 3. 리스크/완화
- **R1 headless**: surefire JVM 에서 offscreen BufferedImage 통상 동작. 실패 시 `<argLine>-Djava.awt.headless=true` 확인(이미 export 서비스 테스트가 동일 패턴으로 green 이므로 환경 OK 추정).
- **R2 제외**: catch(ImageIO 실패)·L187(도달불가 방어) 제외 명시.
- production 회귀 0.
