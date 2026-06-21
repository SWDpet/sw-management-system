# 개발계획서 — OrgUnit 조직 인원 조회 응답 List<Map>→record (orgunit-members-dto)

- **상태**: v0.1 (경량 — 사용자 최종승인 대기)
- **작성일**: 2026-06-21
- **맥락**: §6-4 13번째. OrgUnit 조직 인원 조회(`/api/org-units/{unitId}/members`)의 응답조립 `List<Map>` 을 record 로 치환. **service 계층(OrgUnitService.getMembers) + controller** 동시. OrgUnitService 의 다른 Map(toDto/getTree 중첩 트리·create/update)은 toDto 가 가변 중첩 트리와 얽혀 있어 **본 sprint 대상 외**(별도 후속).
- **무손실 핵심**: getMembers 는 **`LinkedHashMap`**(키순서 결정적) → tree 동치 + **string 동치(키순서)** 검증. 전역 jackson non_null 미설정 → null 포함 기본. 소비자(org-unit-management.html JS)는 `m.staff_id/username/position/active` 키로 접근.
- **디자인팀**: 비해당 — 백엔드 전용 sprint(서버 응답 JSON 무손실 보존, 클라이언트 측 파일 무변경). **DB**: 변경 없음.

## 목표 (FR/NFR)

- **FR**: `OrgUnitService.getMembers`(반환타입·`result`·`m` = 3줄) + `OrgUnitController.getMembers`(반환타입 1줄)의 `List<Map<String,Object>>` → `OrgMemberRow`. **`Map<String,Object>` 선언 319→315 (−4)**.
- **NFR**: wire 무손실(키셋·값·타입·키순서·active boolean), 회귀 0, ratchet 315 tighten.

## record 설계 (com.swmanager.system.dto)

```java
public record OrgMemberRow(
        @JsonProperty("staff_id") Long staffId,
        String username,
        String position,
        boolean active) {
    public static OrgMemberRow from(Staff s) {
        return new OrgMemberRow(
            s.getStaffId(), s.getName(), s.getPosition(),
            Boolean.TRUE.equals(s.getActive()));   // 현행 식: null→false, primitive boolean
    }
}
```

- 키: 현행 put 순(staff_id, username, position, active). **snake_case `staff_id` 만 @JsonProperty**, 나머지 camelCase=컴포넌트명.
- `active` = primitive boolean(`Boolean.TRUE.equals(getActive())` 결과, null 불가). 컴포넌트명 `active`(is-접두 아님)라 키 "active" 보존.
- 타입: staffId=Long, username(name)·position=String. `@JsonInclude` 미부착(null 포함).
- 치환: `OrgUnitService.getMembers` for-루프 → `result.add(OrgMemberRow.from(s))`, 반환타입 `List<OrgMemberRow>`. `OrgUnitController.getMembers` 반환타입 `List<OrgMemberRow>` (service 위임 그대로).

## 검증 (골든 = 레거시 LinkedHashMap 복제본과 tree·string 동치)

1. `OrgMemberRowTest`(신규): 채운 Staff → `from()` 직렬화가 현행 `LinkedHashMap` 복제본과 **tree·string 동치**(키순서). `staff_id` snake_case 키, `active` 키·boolean(active=true/false/null→false), null 필드(position null) 포함, 키셋=4 확인.
2. `GOLDEN_RECORD=1 … MapDebtRatchetTest` → 319→315.
3. `./mvnw test` 전체 green. DB 포함(RUN_DB_TESTS=true) 최종 1회.
4. codex 검토(키·snake_case·active·service+controller 흐름).

## 롤백
원자 커밋 → `git revert`. record 1개 신규 + service/controller 각 1메서드 국소 치환.

## 커밋(승인 후)
`refactor(api): OrgUnit 조직 인원 조회 응답 List<Map>→OrgMemberRow record + ratchet 319→315 (§6-4)`
