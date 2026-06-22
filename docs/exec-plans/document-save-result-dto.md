# 개발계획서 — DocumentController 문서저장/상태변경 응답 Map→record/ResponseEntity<?> (document-save-result-dto)

- **상태**: v0.1 (경량 — codex 검토 + 사용자 최종승인 대기)
- **작성일**: 2026-06-22
- **맥락**: §6-4 in-place 후속. DocumentController save/status 응답 엔벨로프 치환(읽기 projection 소진 후 마지막 깨끗한 응답 조각). 요청바디·모델·jsonb 는 보존.
- **무손실 핵심**: 고정형 응답 Map → record/ResponseEntity<?>(Map.of 바디 보존). JsonNode tree 동치.
- **디자인팀** 비해당. **DB** 무관.

## 대상 — 치환 2 / 보존(요청·모델·jsonb)

| 메서드 | 처리 |
|---|---|
| `saveDocument` POST /api/save | **성공 결과만 치환**: `{success:true, docId, docNo}`(L428-432) → `DocumentSaveResult(boolean success, Integer docId, String docNo)` record(docNo null→"" fallback 보존). 반환타입 `ResponseEntity<Map>`→`ResponseEntity<?>`. **나머지 모든 응답 경로 Map.of 그대로 보존(codex P2)**: 403 `{error:str}`(L329)·400 `{success:false,error:{code:INVALID_INPUT,message}}`(L341,357)·400 `{success:false,error:{code:DUPLICATE,message,existingDocId}}`(L388)·500 `{error:str}`(L435). **요청바디 `@RequestBody Map<String,Object> requestData`·sections/sectionData 파싱 보존**(lenient·jsonb). −1(428) |
| `changeStatus` POST /api/status/{id} | 반환타입 `ResponseEntity<Map>`→`ResponseEntity<?>`. 성공 `Map.of(success,status)`·403 `Map.of(error)` **바디 보존**(Map.of=ratchet 비대상). −1(443) |
| `create`(GET) 의 `existingData`(L244)·`sectionsMap`(L251) | **보존**: 폼 모델 속성(Thymeleaf 결합) + `Map<String,Map<String,Object>> sectionsMap`(jsonb 섹션데이터). 타입화는 템플릿 결합/jsonb 동적 → 부적합 |

- 신규 record 1종: `DocumentSaveResult(boolean success, Integer docId, String docNo)` (com.swmanager.system.dto.workplan). 타입: docId=Integer, docNo=String. success/docId primitive·객체 직렬화 동치.
- 응답 wire 100% 보존: 성공 {success:true,docId,docNo}, 500 {error:str}, changeStatus 성공 {success:true,status}·403 {error:str}.

## 목표 (FR/NFR)
- **FR**: saveDocument 결과(428)·changeStatus 반환(443) `Map<String,Object>` 2선언 제거 → record 1 + ResponseEntity<?>. **선언 224→222 (−2)**. 요청바디·모델·jsonb 보존.
- **NFR**: 응답 wire·status(200/403/500) 무손실, 회귀 0, ratchet 222 tighten. URL 무변.

## 검증
1. `DocumentSaveResultTest`(신규): DocumentSaveResult → 현행 HashMap(success/docId/docNo) 복제본과 JsonNode tree 동치. docNo ""·일반, docId null 보존, 3키.
2. `GOLDEN_RECORD=1 … MapDebtRatchetTest` → 224→222.
3. `./mvnw test` 전체 green(EndpointInventory 불변).
4. codex 검토(응답 wire·Map.of 바디·요청/모델/jsonb 보존·status).

## 롤백
원자 1 커밋 → `git revert`. record 1 신규 + 2 메서드 응답 국소 치환. 클라이언트 무변.

## 커밋 메시지(승인 후)
`refactor(api): DocumentController 문서저장/상태변경 응답 Map→DocumentSaveResult record/ResponseEntity<?>(요청바디·모델·jsonb 보존) + ratchet 224→222 (§6-4)`
