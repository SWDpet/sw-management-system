package com.swmanager.system.dto.ops;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.swmanager.system.domain.ops.Partner;

import java.util.List;

/**
 * /api/list (협력업체 목록) 응답 행 dto.
 *
 * 기존 PartnerController 의 컨트롤러-로컬 LinkedHashMap 응답조립을 record 로 대체한다
 * (§6-4 partner-list-rows-dto). 키셋·값 동치로 무손실. 현행 응답 키가 snake_case 라
 * {@code @JsonNaming(SnakeCaseStrategy)} 로 컴포넌트(camelCase)→snake 매핑
 * (partnerId→partner_id, partnerType→partner_type, mainTel→main_tel). null 포함.
 *
 * <p>※ ops-doc 검색용 {@link PartnerRow}(id/name/type)와는 응답 형태가 달라 별도 타입.
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record PartnerListRow(Long partnerId, String name, String partnerType, String mainTel,
                             String note, List<PartnerListContactRow> contacts) {

    public static PartnerListRow from(Partner p, List<PartnerListContactRow> contacts) {
        return new PartnerListRow(p.getPartnerId(), p.getName(), p.getPartnerType(),
                p.getMainTel(), p.getNote(), contacts);
    }
}
