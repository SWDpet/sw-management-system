package com.swmanager.system.dto.orgunit;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

/**
 * /admin/api/staff 직원 등록 성공(200) 응답 dto — `{success:true, staff_id:n}`.
 *
 * 기존 OrgUnitController.createStaff 의 컨트롤러-로컬 LinkedHashMap 응답조립을 record 로 대체한다
 * (§6-4). 키셋·값 동치로 무손실. 현행 키가 snake_case 라 {@code @JsonNaming(SnakeCaseStrategy)} 로
 * staffId→staff_id 매핑(success 는 단일어 동일). success 는 primitive 라 항상 직렬화.
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record StaffCreateResult(boolean success, Long staffId) {
}
