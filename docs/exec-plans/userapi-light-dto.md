# 개발계획서 — UserApiController 경량 조회 응답 List<Map>→record (userapi-light-dto)

- **상태**: v0.1 (경량 — 사용자 최종승인 대기)
- **작성일**: 2026-06-21
- **맥락**: §6-4 10번째. `opskb-todto-record` 계열과 동일 패턴 — 조회 API 의 컨트롤러-로컬 `List<Map>` 응답조립을 record 로 치환. UserApiController 의 공유 헬퍼 `toLightDto` 1개를 두 엔드포인트(`/api/users/all-with-disabled`·`/active`)가 공유 → **record 1종으로 둘 다 처리**.
- **무손실 핵심**: `toLightDto` 는 **`LinkedHashMap`**(키순서 결정적) → record 치환 시 tree 동치 + **string 동치(키순서)** 모두 검증. 전역 jackson non_null 미설정 → null 포함 기본.
- **⚠ 마스킹 정책 보존(민감)**: 본 API 는 의도적으로 `userId/userid/username/deptNm/positionTitle/enabled` **6필드만 반환**하고 마스킹 대상(tel/mobile/email)은 **미반환**(클래스 Javadoc 명시·users-masking-regression-fix 계열). record 필드를 **정확히 동일 6개**로 고정하고 골든에서 키셋·size=6 을 단언해 마스킹 회귀를 차단한다.
- **디자인팀**: 비해당 — 백엔드 전용 sprint(서버 응답 JSON 무손실 보존, 클라이언트 측 파일 무변경). **DB**: 변경 없음.

## 목표 (FR/NFR)

- **FR**: `toLightDto`(반환타입·지역 `m`)와 이를 흘리는 `getAllWithDisabled`·`getActive` 반환타입 = 4줄 → `UserLightDto`. **`Map<String,Object>` 선언 334→330 (−4)**.
- **NFR**: wire 무손실(키셋·값·타입·키순서·null 포함·마스킹 6필드), 회귀 0, ratchet 330 tighten.

## 대상 4줄 (UserApiController)

| 줄 | 현행 | 후 |
|---|---|---|
| L33 | `public List<Map<String,Object>> getAllWithDisabled()` | `public List<UserLightDto> getAllWithDisabled()` |
| L43 | `public List<Map<String,Object>> getActive()` | `public List<UserLightDto> getActive()` |
| L49 | `private static Map<String,Object> toLightDto(User u) {` | (제거 — `UserLightDto::from` 직접 사용) |
| L50 | `Map<String,Object> m = new LinkedHashMap<>(); …` | (record 로 이동) |

- 두 메서드의 `.map(UserApiController::toLightDto)` → `.map(UserLightDto::from)`. `toLightDto` 헬퍼 제거.

## DTO 설계 (com.swmanager.system.dto)

```java
public record UserLightDto(
        Long userId, String userid, String username,
        String deptNm, String positionTitle, boolean enabled) {
    public static UserLightDto from(User u) {
        return new UserLightDto(
            u.getUserSeq(), u.getUserid(), u.getUsername(),
            u.getDeptNm(), u.getPositionTitle(), u.isEnabled());
    }
}
```

- 컴포넌트 선언순 = 현행 put 순(userId…enabled). **@JsonProperty 없음**(camelCase=컴포넌트명) → Jackson 재정렬 없음.
- `enabled` = primitive boolean: 현행 `u.isEnabled()`(수동 메서드 `Boolean.TRUE.equals(enabled)`, **null 불가 primitive**) 그대로 호출 → true/false 동치. 컴포넌트명 `enabled`(is-접두 아님)라 키 "enabled" 보존.
- `userId` = Long(userSeq, FK user_id 매핑), 나머지 String. **tel/mobile/email 필드 없음**(마스킹 보존).

## 검증

1. `UserLightDtoTest`(신규): 채운 User → `from()` 직렬화가 현행 `LinkedHashMap` 복제본과 **tree·string 동치**(키순서). `enabled` 키·boolean(true/false) 확인, **키셋=6·마스킹 필드(tel/mobile/email) 부재** 단언, null 필드(deptNm 등) 포함.
2. `GOLDEN_RECORD=1 … MapDebtRatchetTest` → 334→330.
3. `./mvnw test` 전체 green. DB 포함(RUN_DB_TESTS=true) 최종 1회.
4. codex 검토(키·타입·enabled·마스킹 6필드).

## 롤백
원자 커밋 → `git revert`. record 1개 신규 + 1 컨트롤러 국소 치환(헬퍼 제거).

## 커밋(승인 후)
`refactor(api): UserApiController 경량 조회 응답 List<Map>→UserLightDto record + ratchet 334→330 (§6-4)`
