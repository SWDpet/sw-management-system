package com.swmanager.system.dto.ops;

import com.swmanager.system.domain.User;

/**
 * /ops-doc/api/engineers (담당 엔지니어 풀) 응답 행 dto.
 *
 * 기존 컨트롤러-로컬 응답조립(HashMap)을 타입 record 로 대체한다(§6-4 opsdoc-search-rows-dto).
 * 클라이언트는 응답 JSON 키로 접근. 현행 HashMap 은 키순서 비결정이라 키셋·값 동치로 무손실 보존.
 * camelCase 키=컴포넌트명(무어노테이션), {@code @JsonInclude} 미부착(null 포함, 전역 non_null 미설정).
 */
public record EngineerRow(Long id, String name, String position) {

    /** {@link User} → 엔지니어 행. position 은 현행과 동일하게 position 우선, 없으면 positionTitle 폴백. */
    public static EngineerRow from(User u) {
        return new EngineerRow(
                u.getUserSeq(),
                u.getUsername(),
                u.getPosition() != null ? u.getPosition() : u.getPositionTitle());
    }
}
