# 개발계획서 — DocumentController 일괄작성 조회 응답 Map→record (document-batch-rows-dto)

- **상태**: v0.1 (경량 — codex 검토 + 사용자 최종승인 대기)
- **작성일**: 2026-06-22
- **맥락**: §6-4 in-place 치환 후속(document-plan 다음). DocumentController batch 그룹의 **읽기 projection 2종**. batchGenerate/buildBatchLetterData(대형 요청바디·내부 문서조립)는 보존.
- **무손실 핵심**: 컨트롤러-로컬 `HashMap` projection → JsonNode tree 동치. 키셋 고정 → record.
- **디자인팀** 비해당. **DB** 무관.

## 대상 — 치환 2 / 보존 1

| 메서드 | 키셋 | record (com.swmanager.system.dto.workplan) |
|---|---|---|
| `getAllSystemsForYear` GET /api/project-systems-all | 고정 2키 {sysNmEn, sysNm} | `SystemAllRow(String sysNmEn, String sysNm)` (LinkedHashMap entry 에서 조립, sysNmEn 정렬 유지) |
| `getBatchTargets` GET /api/batch/targets | 고정 11키 {projId,projNm,sysNmEn,cityNm,distNm,orgNm,contAmt,contDt,startDt,endDt,client} | `BatchTargetRow(Long projId, String projNm, String sysNmEn, String cityNm, String distNm, String orgNm, Long contAmt, String contDt, String startDt, String endDt, String client)` ← `from(SwProject)` (contDt/startDt/endDt = LocalDate `getX()!=null?toString():null`) |
| `batchGenerate` POST /api/batch/generate + `buildBatchLetterData` | 요청바디·내부 문서조립 Map | **보존**(대형 lenient 요청바디 + 문서생성용 내부 Map 구조 — 타입화 부적합/과설계) |

- 타입: SystemAllRow 둘 다 String; BatchTargetRow projId=Long·contAmt=Long·contDt/startDt/endDt=LocalDate(→String)·나머지 String.
- 정렬 보존: getAllSystemsForYear 는 sysNmEn 오름차순 정렬 → record 리스트도 동일 정렬(컨트롤러에서 정렬 후 수집).
- badRequest(빈 List.of()) 경로·null 포함 보존(@JsonInclude 미부착).

## 목표 (FR/NFR)
- **FR**: getAllSystemsForYear(return·List·m)·getBatchTargets(return·List·m) `Map<String,Object>` 6선언 제거 → record 2. **선언 230→224 (−6)**. batchGenerate/buildBatchLetterData 보존.
- **NFR**: 응답 wire(키셋·값·null·LocalDate toString·정렬)·status(200/400) 무손실, 회귀 0, ratchet 224 tighten. URL 무변.

## 검증
1. `BatchRowsTest`(신규): SystemAllRow·BatchTargetRow from → 현행 HashMap 복제본과 JsonNode tree 동치. 경계: BatchTargetRow 11키·날짜 null→null·toString, null 필드 키 보존, SystemAllRow 2키.
2. `GOLDEN_RECORD=1 … MapDebtRatchetTest` → 230→224.
3. `./mvnw test` 전체 green(EndpointInventory 불변).
4. codex 검토(키셋·LocalDate toString·정렬·badRequest·batchGenerate 보존).

## 롤백
원자 1 커밋 → `git revert`. record 2 신규 + 2 메서드 국소 치환. 클라이언트 무변.

## 커밋 메시지(승인 후)
`refactor(api): DocumentController 일괄작성 조회(시스템/타겟) 응답 Map→SystemAllRow/BatchTargetRow record(batchGenerate 보존) + ratchet 230→224 (§6-4)`
