# [개발계획] InspectionQrBatchService 소형 잔여 보강

- **작성팀**: 개발팀 / **작성일**: 2026-06-28 / **기획**: `docs/product-specs/coverage-qrbatch-findexisting-usage.md`
- **상태**: ✅ 구현 완료(2026-06-28). Q1/Q2 green. InspectionQrBatchService 94.34%(miss 17→15), 전역 LINE 81.18%. codex 구현검증 PASS·dual-review 합의3 반영. 상세는 기획서 상태줄.

---

## 0. 사전검증 (완료)
- `findExisting(L80-81)`: `batchRepository.findByPayloadId(id).map(this::toIdempotentResponse)`. toIdempotentResponse(L422-466) 의존=`swProjectRepository.findBySiteCode(siteCode).or(findFirstBySiteCodeAlias)` + payloadJson 파싱(repo 무). present 시 alias 미호출.
- L248: db.os.disk mounts 루프 — `mval instanceof Map` else → `String.valueOf(mval)+"%"`.
- 기존 test-16(usageRows) 은 `Map.of("/", Map.of("p",52))`(Map 값) → L246. non-Map 값으로 L248.

## 1. 테스트 (`InspectionQrBatchServiceTest` +2)

| # | 테스트 | 픽스처 | 단언 |
|---|---|---|---|
| Q1 | findExisting_found_returnsIdempotent | InspectQrBatch(id7/payloadId "dyg-2026-05"/reportId 101/siteCode "dyg"/payloadJson {tiers:{ap:{i:[["a","ok",1],["b","M"]]}}}), findByPayloadId→Optional.of(batch), findBySiteCode("dyg")→pjt | `findExisting("dyg-2026-05")` present·idempotent true·pjtId 17·reportId 101·reportUrl "/document/inspect/101"·tierCount 1·itemCount 2·manualItems 1 |
| Q2 | usageRows_nonMapMount_plainPercent | test-16 변형: db.os.disk=`Map.of("/", 75)`, ap.os.disk_summary 생략 가능, findByPayloadId(any)→empty, findBySiteCode→pjt, stubReportSaveWithId(1) | captor InspectCheckResult: DB_USAGE/"/"/resultText "75%" anySatisfy |

- Q1: `service.findExisting(...)` 직접 호출(upload 아님). 반환 `Optional<InspectionQrBatchResponse>`.
- Q2: 기존 test-16 구조 차용, mount 값만 `75`(Integer). strict: metricSnapshotRepository 미스텁(perf 없음).

## 2. 검증 절차
1. `./mvnw -o clean verify` → BUILD SUCCESS, 기존 26 + 신규 2 green.
2. `jacoco.csv` → InspectionQrBatchService L81·L248 커버 확인. 전역 delta, floor 유지.
3. 구현 후 codex 검증 → dual-review → 듀얼푸시.

## 3. 리스크/완화
- **R1 strict stubbing**: Q1 alias 미스텁·Q2 snapshot 미스텁(미사용 stub 금지).
- **R2 payloadJson 카운트**: toIdempotentResponse 가 tiers→items 파싱 — 픽스처 json 구조 정확히(test-17 패턴).
- production 회귀 0.
