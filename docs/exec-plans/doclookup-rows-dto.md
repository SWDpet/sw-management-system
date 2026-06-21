# 개발계획서 — DocumentLookupController 공정/용역목적 조회 응답 List<Map>→record (doclookup-rows-dto)

- **상태**: v0.1 (경량 — 사용자 최종승인 대기)
- **작성일**: 2026-06-21
- **맥락**: §6-4 11번째. `opskb-todto-record` 계열과 동일 패턴 — 조회 API 의 컨트롤러-로컬 `List<Map>` 응답조립을 record 로 치환. DocumentLookupController 의 공정명(`/api/process-master`)·용역목적(`/api/service-purpose`) 2종.
- **무손실 핵심**: 둘 다 **`HashMap` 기반**(키순서 비결정) → tree 동치(키셋·값)만으로 무손실. 클라이언트는 응답 JSON 키로 접근. 전역 jackson non_null 미설정 → null 포함 기본.
- **디자인팀**: 비해당 — 백엔드 전용 sprint(서버 응답 JSON 무손실 보존, 클라이언트 측 파일 무변경). **DB**: 변경 없음.

## 목표 (FR/NFR)

- **FR**: `getProcessMasterList`·`getServicePurposeList` 의 `List<Map<String,Object>>`(반환타입·`result`·`m` = 각 3줄) → record 2종. **`Map<String,Object>` 선언 330→324 (−6)**.
- **NFR**: wire 무손실(키셋·값·타입·null 포함), 회귀 0, ratchet 324 tighten.

## 대상 2 메서드 + record 설계 (com.swmanager.system.dto.workplan)

| 메서드 | 키(현행) | record |
|---|---|---|
| `getProcessMasterList` | processId, processName | `ProcessMasterRow(Integer processId, String processName)` ← `from(ProcessMaster)` |
| `getServicePurposeList` | purposeId, purposeType, purposeText | `ServicePurposeRow(Integer purposeId, String purposeType, String purposeText)` ← `from(ServicePurpose)` |

- camelCase 키=컴포넌트명(무어노테이션). 타입: processId/purposeId = **Integer**(엔티티 게터), 나머지 String.
- `@JsonInclude` 미부착(null 포함). 빌드 로직 단순 직매핑.
- 두 메서드의 for-루프를 `.stream().map(Row::from).collect(toList())` 또는 루프 내 `result.add(Row.from(x))` 로 치환(행위 동일).

## 검증 (골든 = 레거시 HashMap 복제본과 tree 동치)

1. `DocLookupRowsTest`(신규): 두 record 각각 채운 엔티티 → `from()` 직렬화가 현행 `HashMap` 복제본과 **JsonNode tree 동치**(키셋·값). null 필드(processName/purposeText null) 포함, 키셋 크기(2/3) 확인.
2. `GOLDEN_RECORD=1 … MapDebtRatchetTest` → 330→324.
3. `./mvnw test` 전체 green. DB 포함(RUN_DB_TESTS=true) 최종 1회.
4. codex 검토(키·타입·tree 동치).

## 롤백
원자 커밋 → `git revert`. record 2개 신규 + 1 컨트롤러 2메서드 국소 치환.

## 커밋(승인 후)
`refactor(api): DocumentLookupController 공정/용역목적 조회 응답 List<Map>→record 2종 + ratchet 330→324 (§6-4)`
