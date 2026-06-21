# 개발계획서 — DocumentParticipantController 과업참여자 조회 응답 List<Map>→record (participant-rows-dto)

- **상태**: v0.1 (경량 — 사용자 최종승인 대기)
- **작성일**: 2026-06-21
- **맥락**: §6-4 12번째. 조회 API 의 컨트롤러-로컬 `List<Map>` 응답조립을 record 로 치환. DocumentParticipantController 의 과업참여자 조회 2종(비민감 `/api/contract-participants/{projId}`·민감 `/secure`).
- **무손실 핵심**: 둘 다 **`HashMap` 기반**(키순서 비결정) → tree 동치(키셋·값)만으로 무손실. 전역 jackson non_null 미설정 → null 포함 기본.
- **⚠ 민감 필드 분리(보안, 감사 P1-3)**: 비민감 응답은 9키(ssn/certificate **미포함**), 민감 응답은 11키(9 + ssn + certificate, EDIT 권한 게이트). **별도 record 2종**으로 분리하고 골든에서 비민감=9키·ssn/certificate 부재 / 민감=11키 를 단언해 누출 차단.
- **디자인팀**: 비해당 — 백엔드 전용 sprint(서버 응답 JSON 무손실 보존, 클라이언트 측 파일 무변경). **DB**: 변경 없음.

## 목표 (FR/NFR)

- **FR**: `getContractParticipants`(반환타입·`result`·`m` = 3줄) + `getContractParticipantsSecure`(`result`·`m` = 2줄)의 `List<Map<String,Object>>` → record 2종. **`Map<String,Object>` 선언 324→319 (−5)**.
- **NFR**: wire 무손실(키셋·값·타입·null 포함·폴백), 민감 분리 보존, 회귀 0, ratchet 319 tighten.

## record 설계 (com.swmanager.system.dto.workplan)

```java
// 비민감 (9키)
public record ContractParticipantRow(
    Integer participantId, Long userId, String userName, String position,
    String roleType, String techGrade, String taskDesc,
    @JsonProperty("isSiteRep") Boolean isSiteRep, String tasks) {
    public static ContractParticipantRow from(ContractParticipant cp) {
        User u = cp.getUser();
        return new ContractParticipantRow(
            cp.getParticipantId(),
            u != null ? u.getUserSeq() : null,
            u != null ? u.getUsername() : "",
            u != null ? u.getPositionTitle() : "",
            cp.getRoleType(), cp.getTechGrade(), cp.getTaskDesc(),
            cp.getIsSiteRep(),
            u != null ? u.getTasks() : "");
    }
}

// 민감 (11키 = 9 + ssn + certificate) — EDIT 게이트 엔드포인트 전용
public record SecureContractParticipantRow(
    Integer participantId, Long userId, String userName, String position,
    String roleType, String techGrade, String taskDesc,
    @JsonProperty("isSiteRep") Boolean isSiteRep, String tasks,
    String ssn, String certificate) {
    public static SecureContractParticipantRow from(ContractParticipant cp) {
        User u = cp.getUser();
        return new SecureContractParticipantRow(
            cp.getParticipantId(),
            u != null ? u.getUserSeq() : null,
            u != null ? u.getUsername() : "",
            u != null ? u.getPositionTitle() : "",
            cp.getRoleType(), cp.getTechGrade(), cp.getTaskDesc(),
            cp.getIsSiteRep(),
            u != null ? u.getTasks() : "",
            u != null ? u.getSsn() : "",
            u != null ? u.getCertificate() : "");
    }
}
```

- 타입: participantId=**Integer**, userId=Long(userSeq), isSiteRep=**Boolean**(@JsonProperty 로 is-접두 깎임 차단), 나머지 String.
- 폴백 보존: user null 시 userName/position/tasks/ssn/certificate → `""`(빈문자), userId → null.
- `@JsonInclude` 미부착(null 포함). camelCase 키=컴포넌트명(isSiteRep 제외 무어노테이션).
- 두 endpoint 의 for-루프를 `result.add(Row.from(cp))` 로 치환. `getContractParticipantsSecure` 의 403 forbidden 응답 Map(L101)·`saveContractParticipants`(요청/응답 envelope)는 **대상 외(보존)**.

## 검증 (골든 = 레거시 HashMap 복제본과 tree 동치)

1. `ParticipantRowsTest`(신규): 두 record 각각 채운 ContractParticipant → `from()` 직렬화가 현행 `HashMap` 복제본과 **JsonNode tree 동치**. ★비민감 record **키셋=9·ssn/certificate 부재** 단언(누출 차단), 민감 record **키셋=11·ssn/certificate 존재**. isSiteRep 키·boolean, user null 폴백(""/null), null 포함.
2. `GOLDEN_RECORD=1 … MapDebtRatchetTest` → 324→319.
3. `./mvnw test` 전체 green. DB 포함(RUN_DB_TESTS=true) 최종 1회.
4. codex 검토(키·타입·민감분리·폴백·isSiteRep).

## 롤백
원자 커밋 → `git revert`. record 2개 신규 + 1 컨트롤러 2메서드 국소 치환.

## 커밋(승인 후)
`refactor(api): DocumentParticipantController 과업참여자 조회 응답 List<Map>→record 2종(민감분리 보존) + ratchet 324→319 (§6-4)`
