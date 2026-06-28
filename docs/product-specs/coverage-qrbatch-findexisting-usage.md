# [기획서] InspectionQrBatchService 소형 잔여 보강 (findExisting / usageText) (beyond-A)

- **작성팀**: 기획팀 / **작성일**: 2026-06-28
- **트랙**: beyond-A 소형 마무리. 기존 `InspectionQrBatchServiceTest`(26테스트) 확장(테스트만). 충돌·파괴 없는 깔끔한 잔여 분기만.
- **상태**: ✅ 구현 완료(2026-06-28). `InspectionQrBatchServiceTest` +2(Q1 findExisting·Q2 usageText else). **InspectionQrBatchService LINE 93.6→94.34%(miss 17→15), 전역 LINE 81.08→81.18%·INSTR 66.22→66.47%.** floor 0.78/0.64 유지. `mvnw -o clean verify` 24 green. 남은 하드 catch(snapshot/JSON/hash verify·RuntimeException) 제외 유지. codex 기획 검토 실질승인(글리치)·구현검증 PASS. dual-review(codex0/Opus4) 합의3 반영(2요소 manual 주석·하드코딩 라인번호 L248 제거·Q2 anySatisfy→singleElement 중복행 차단), 분쟁1 codex refute(findBySiteCode는 pjtId 유일소스). production 변경 0.

---

## 1. 배경 / 목표
`InspectionQrBatchService`(LINE 93.6%, miss 17)는 기존 26테스트로 철저히 덮였고, 남은 미커버 대부분은 **정적예외 catch(JsonProcessing/NoSuchAlgorithm/Exception)·hash-match("ok")** 등 단위 도달 비용이 큰 하드 분기다. 본 스프린트는 그중 **충돌·하드함 없는 2개 깔끔한 분기만** 박제한다(나머지 하드 catch 는 제외 유지).

| # | 라인 | 미커버 정체 |
|---|---|---|
| Q1 | L81 | **public `findExisting(payloadId)`** — `batchRepository.findByPayloadId(id).map(toIdempotentResponse)`. upload() 내부 멱등경로와 별개라 직접 미커버. |
| Q2 | L248 | usage 행 생성 중 mount 값(mval)이 **Map 아닐 때**(`String.valueOf(mval)+"%"`). 기존 test-16 은 Map 값만 사용. |

> 제외(하드, 유지): L135-137(snapshot 적재 실패 catch)·L339-340(JSON serialize error)·L391/396-401(hash "ok"/verify catch)·L411-412(catch RuntimeException) — 정적예외/해시매칭 강제라 단위 비용 과다.

## 2. 범위 (테스트만, production 무변경)
`InspectionQrBatchServiceTest` 확장. `@ExtendWith(MockitoExtension.class)` strict + `@InjectMocks` + `pjt`/`sampleRequest`/`stubReportSaveWithId` 재사용.

- **Q1 findExisting_found**: batch(payloadId/reportId/siteCode/payloadJson tiers) + `batchRepository.findByPayloadId(id)→Optional.of(batch)` + `swProjectRepository.findBySiteCode("dyg")→pjt` → `service.findExisting(id)` 가 **present + idempotent + pjtId 17 + reportId + tier/item 카운트** 반환. (strict: findBySiteCode present 라 alias 미스텁.)
- **Q2 usageText_nonMapMount**: test-16 변형 — `db.os.disk = {"/": 75}`(75=Integer, non-Map) → DB_USAGE 행 resultText `"75%"`(L248 else). ArgumentCaptor 로 단언.

## 3. 검증 방식
- Q1: 반환 DTO 필드값 단언(라인커버 아닌 멱등 응답 계약). Q2: 저장된 InspectCheckResult resultText 정확값("75%").

## 4. 요건
- **FR-1**: findExisting found→idempotent 응답 박제.
- **FR-2**: usage mount 값 non-Map → plain "%" 분기 박제.
- **NFR**: production 변경 0, `mvnw -o clean verify` SUCCESS, InspectionQrBatchService 소폭 상향(잔여 하드 catch 제외 유지), 전역 유지, floor 유지, 구현 후 codex PASS + dual-review → 듀얼푸시.

## 5. 영향 / 리스크
- 변경: `service/inspection/InspectionQrBatchServiceTest.java`(+2)만. production 0. 기존 26테스트 무수정.
- **R1 strict stubbing**: Q1 은 findBySiteCode present → alias 미호출 → 미스텁(strict 준수). Q2 는 perf 메트릭 없어 metricSnapshotRepository 미사용 → 미스텁.
- production 회귀 0.
