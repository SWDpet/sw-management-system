# 개발계획서 — DocumentController 사용자 정보 조회 응답 Map→record (document-userinfo-rows-dto)

- **상태**: v0.1 (경량 — codex 검토 + 사용자 최종승인 대기)
- **작성일**: 2026-06-22
- **맥락**: §6-4 in-place 치환 후속(document-file 다음 그룹). DocumentController user/project 조회 그룹(L903-1012). 읽기 projection.
- **무손실 핵심**: 컨트롤러-로컬 `HashMap` 응답조립 → JsonNode tree 동치. 키셋 **고정** 메서드만 record 치환, **가변 키셋**은 보존.
- **디자인팀** 비해당. **DB** 무관.

## 대상 분석 — 치환 2 / 보존 1(가변)

| 메서드 | 키셋 | 처리 |
|---|---|---|
| `getUserInfo` GET /api/user/{userSeq} | **고정 13키**(userSeq,username,positionTitle,position,techGrade,mobile,tel,address,tasks,deptNm,teamNm,careerYears,fieldRole) | `UserInfoRow` record(13). **민감(ssn/cert/email) 미포함** = 화이트리스트 강제 |
| `getUserInfoSecure` GET /api/user/{userSeq}/secure | **고정 16키**(위 13 + ssn,certificate,email) | `UserInfoSecureRow` record(16). 403 `{error:{code,message}}`(success 없음) → `Map.of`(보존) |
| `getProjectInfo` GET /api/project/{projId} | **가변**(person* 7키가 `personId!=null` 일 때만 put) | **보존**(가변 키셋 → flat record 무손실 불가, InfraFindResult P2 동류). Map 유지 |

- **신규 record 2종**(com.swmanager.system.dto.workplan): `UserInfoRow(Long userSeq, String username, …13 전부 String 1개 Long)` ← `from(User)`, `UserInfoSecureRow(…16…)` ← `from(User)`. 타입: userSeq=Long, 나머지 전부 String(careerYears 포함).
- 비민감/민감 **별도 2 record**: getUserInfo 는 13키만(민감 절대 미노출), secure 는 16키. 키셋 상이 → 단일 record 불가(NON_NULL 충돌). 분리로 화이트리스트 보존.
- 404(notFound) 경로·403 wire·null 필드 키 보존(HashMap.put-null = record null 직렬화 동치, @JsonInclude 미부착).

## 목표 (FR/NFR)
- **FR**: getUserInfo(904·906)·getUserInfoSecure(931·933·938) `Map<String,Object>` 5선언 제거 → record 2 + Map.of. **선언 243→238 (−5)**. getProjectInfo(964·966) 보존.
- **NFR**: 응답 wire(키셋·값·null·민감 화이트리스트)·status(200/403/404) 무손실, 회귀 0, ratchet 238 tighten. URL 무변.

## 검증
1. `UserInfoRowsTest`(신규): UserInfoRow(13키)·UserInfoSecureRow(16키) from(User) → 현행 HashMap 복제본과 JsonNode tree 동치(키셋·값·null). 경계: UserInfoRow 에 ssn/certificate/email 키 부재 단정(민감 비노출), null 필드 키 보존.
2. `GOLDEN_RECORD=1 … MapDebtRatchetTest` → 243→238.
3. `./mvnw test` 전체 green(EndpointInventory 불변).
4. codex 검토(키셋·민감 화이트리스트·null·getProjectInfo 보존 타당성).

## 롤백
원자 1 커밋 → `git revert`. record 2 신규 + 컨트롤러 2 메서드 응답 국소 치환. 클라이언트 무변.

## 커밋 메시지(승인 후)
`refactor(api): DocumentController 사용자정보 조회 응답 Map→UserInfoRow/UserInfoSecureRow record(민감 화이트리스트·getProjectInfo 보존) + ratchet 243→238 (§6-4)`
