# [기획서] InspectMetricChartService emptyChart 경로 보강 (beyond-A)

- **작성팀**: 기획팀 / **작성일**: 2026-06-28
- **트랙**: beyond-A 커버리지 증분. 점검 메트릭 추이 차트 서비스. 신규 단위테스트(테스트만).
- **상태**: ✅ 구현 완료(2026-06-28). 신규 `InspectMetricChartServiceTest` +2(M1 no-hosts·M2 hosts-but-no-rows → emptyChart PNG). **InspectMetricChartService LINE ~78→92.92%(miss 24→8)·INSTR 92.68%, 전역 LINE 80.95→81.08%(81% 돌파)·INSTR 66.11→66.22%.** floor 0.78/0.64 유지(직전 래칫·게인 작아 흡수). `mvnw -o clean verify` green. 잔여 8줄=정적 ImageIO catch(L145-147·L213-215)+L187 도달불가 방어 else 의도 제외. codex 기획 APPROVE-WITH-FIX(ImageIO.read decode 검증 반영)·구현검증 PASS. dual-review(codex0/Opus4) 합의2 반영(고정 instant·AP/DB 양 role verify), 분쟁2 codex refute(ImageIO Java17 표준·wildcard import 관례). production 변경 0.

---

## 1. 배경 / 목표

`InspectMetricChartService`(LINE ~78%, miss **24**)는 데이터 있는 차트 렌더(JFreeChart→PNG)는 덮였으나, **데이터 0건 시 "수집 대기" 안내 PNG 를 만드는 `emptyChart()` 경로가 전혀 미커버**다. emptyChart 는 외부의존 없는 순수 AWT(BufferedImage+Graphics2D+ImageIO PNG, headless-OK)라 단위테스트로 도달 가능하다.

JaCoCo 미커버(`InspectMetricChartService.java.html`, nc):

| 영역 | 라인 | 정체 |
|---|---|---|
| renderChart no-data | L128-130 | dataset 0 시리즈 → `emptyChart("수집 대기…")` |
| emptyChart 본문 | L199-212 (14) | BufferedImage+Graphics2D 메시지 그리기 + ImageIO PNG → bytes |
| **제외** | L145-147 / L213-215 | PNG 직렬화 catch·emptyChart catch — 정적 ImageIO 예외라 단위 도달불가 |
| **제외** | L187 | applyLineColors 의 `else c=COLOR_DISK` — **모든 시리즈가 " CPU"/" MEM"/" DISK" 로 끝나 도달불가**(직전 `else if DISK` 와 동일값 중복 방어) |

목표: renderChart 가 데이터 0건일 때 emptyChart 가 **유효 PNG(메시지 안내)** 를 반환하는 경로 박제(~16줄). 위장통과 차단 위해 PNG 매직바이트 단언.

## 2. 범위 (테스트만, production 무변경)
신규 `InspectMetricChartServiceTest`. `@RequiredArgsConstructor` → `new InspectMetricChartService(mock repository)`.

- **M1 no-data emptyChart**: repository.findHostsByPjtRoleRange(any,…)→빈 List(AP/DB 둘 다) → dataset 0 → `renderChart(pjtId, since, until)` 가 **emptyChart PNG** 반환. 단언: 반환 byte[] 비어있지 않음 + **PNG 매직(0x89 'P' 'N' 'G')**.
- **M2 hosts-but-no-rows → still empty**: findHostsByPjtRoleRange 가 host 1개 반환하나 findRangeByPjtRoleHost→빈 List → `rows.isEmpty() continue` → dataset 여전히 0 → emptyChart. (호스트 루프 진입 + empty-rows continue 경로 + emptyChart 재확인.)

## 3. 검증 방식 (위장통과 차단)
- 단순 "byte[] 반환" 이 아니라 **PNG 매직바이트**로 실 이미지 생성 확인. M2 는 host 조회 verify 로 루프 진입 입증.

## 4. 요건
- **FR-1**: renderChart 데이터 0건 → emptyChart 유효 PNG 반환 박제(no-hosts / hosts-but-no-rows 2경로).
- **NFR**: production 변경 0, `mvnw -o clean verify` SUCCESS, InspectMetricChartService LINE ~78%→**~94%**(잔여=정적 ImageIO catch + 도달불가 else, 의도 제외), 전역 유지/소폭 상향, floor 유지, 구현 후 codex PASS + dual-review → 듀얼푸시.

## 5. 영향 / 리스크
- 변경: 신규 `service/InspectMetricChartServiceTest.java`만. production 0.
- **R1 headless**: emptyChart 는 offscreen BufferedImage(TYPE_INT_RGB)+ImageIO → headless 환경에서도 동작(서버 GUI 불요). surefire 기본 JVM 에서 통과 예상; 실패 시 `java.awt.headless=true` 확인.
- **R2 제외 타당성**: L145-147/L213-215 catch 는 ImageIO.write 실패를 강제해야 도달 → 정적 메서드라 mock 없이 단위 불가. L187 은 시리즈 키 명명 규칙상 도달불가(방어). → 의도 제외.
- production 회귀 0.
