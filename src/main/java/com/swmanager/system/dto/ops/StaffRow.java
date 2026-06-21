package com.swmanager.system.dto.ops;

import com.swmanager.system.domain.ops.Staff;

/**
 * /ops-doc/api/staff/search (요청자 직원 검색, tb_staff 재직자) 응답 행 dto.
 *
 * 기존 컨트롤러-로컬 응답조립(HashMap)을 타입 record 로 대체한다(§6-4 opsdoc-search-rows-dto).
 * 키셋·값 동치로 무손실(HashMap 키순서 비결정). camelCase 키=컴포넌트명, null 포함.
 *
 * <p>org(소속 조직명)는 컨트롤러가 orgUnitId 로 조회해 resolve 한 값을 전달받는다(없으면 null).
 */
public record StaffRow(Long id, String name, String org, String pos) {

    public static StaffRow of(Staff s, String org) {
        return new StaffRow(s.getStaffId(), s.getName(), org, s.getPosition());
    }
}
