package com.swmanager.system.dto.ops;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.swmanager.system.domain.ops.PartnerContact;

/**
 * /api/list (협력업체 목록) 응답의 업체별 담당자 행 dto (PartnerListRow.contacts 중첩).
 *
 * 기존 PartnerController 의 컨트롤러-로컬 LinkedHashMap 응답조립을 record 로 대체한다
 * (§6-4 partner-list-rows-dto). 키셋·값 동치로 무손실. 현행 응답 키가 snake_case 라
 * {@code @JsonNaming(SnakeCaseStrategy)} 로 컴포넌트(camelCase)→snake 매핑(contactId→contact_id).
 *
 * <p>※ ops-doc 검색용 {@link PartnerContactRow}(id/name/org/pos/tel)와는 응답 형태가 달라 별도 타입.
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record PartnerListContactRow(Long contactId, String name, String position, String tel, String email) {

    public static PartnerListContactRow from(PartnerContact c) {
        return new PartnerListContactRow(c.getContactId(), c.getName(), c.getPosition(), c.getTel(), c.getEmail());
    }
}
