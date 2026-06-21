package com.swmanager.system.dto.orgunit;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.swmanager.system.domain.ops.Staff;

/**
 * /api/org-units/{unitId}/members (조직 단위 소속 인원) 응답 행 dto.
 *
 * 기존 OrgUnitService.getMembers 의 LinkedHashMap 응답조립을 타입 record 로 대체한다(§6-4 orgunit-members-dto).
 * 소비자(org-unit-management.html JS)는 {@code m.staff_id/username/position/active} 키로 접근.
 * 키순서는 현행 LinkedHashMap put 순(staff_id…active)=컴포넌트 선언순으로 보존, null 포함(@JsonInclude 미부착).
 *
 * <p>snake_case 키 {@code staff_id} 만 {@link JsonProperty}, 나머지는 camelCase 컴포넌트명.
 * {@code active} 는 현행 {@code Boolean.TRUE.equals(getActive())}(null 불가 primitive) 결과를 담는다.
 */
public record OrgMemberRow(
        @JsonProperty("staff_id") Long staffId,
        String username,
        String position,
        boolean active) {

    public static OrgMemberRow from(Staff s) {
        return new OrgMemberRow(
                s.getStaffId(),
                s.getName(),
                s.getPosition(),
                Boolean.TRUE.equals(s.getActive()));
    }
}
