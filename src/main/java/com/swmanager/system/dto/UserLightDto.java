package com.swmanager.system.dto;

import com.swmanager.system.domain.User;

/**
 * /api/users/all-with-disabled · /api/users/active (경량 사용자 조회) 응답 행 dto.
 *
 * 기존 컨트롤러-로컬 응답조립(LinkedHashMap)을 타입 record 로 대체한다(§6-4 userapi-light-dto).
 * 두 엔드포인트가 공유하던 {@code toLightDto} 헬퍼를 본 record 로 일원화한다.
 *
 * <p><b>마스킹 정책 보존</b>: 본 dto 는 {@code userId/userid/username/deptNm/positionTitle/enabled}
 * 6필드만 노출하고 마스킹 대상(tel/mobile/email)은 일절 포함하지 않는다(분리 원칙).
 *
 * <p>키순서는 현행 LinkedHashMap put 순(userId…enabled) = 컴포넌트 선언순으로 보존(무어노테이션 →
 * Jackson 재정렬 없음). {@code @JsonInclude} 미부착(null 포함). {@code enabled} 는 현행
 * {@code User.isEnabled()}(null 불가 primitive) 결과를 그대로 담는다.
 */
public record UserLightDto(
        Long userId,
        String userid,
        String username,
        String deptNm,
        String positionTitle,
        boolean enabled) {

    public static UserLightDto from(User u) {
        return new UserLightDto(
                u.getUserSeq(),     // FK 컬럼명 user_id 와 매핑
                u.getUserid(),
                u.getUsername(),
                u.getDeptNm(),
                u.getPositionTitle(),
                u.isEnabled());
    }
}
