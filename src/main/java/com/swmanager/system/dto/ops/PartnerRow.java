package com.swmanager.system.dto.ops;

import com.swmanager.system.domain.ops.Partner;

/**
 * /ops-doc/api/partner/search (협력업체 검색, tb_partner) 응답 행 dto.
 *
 * 기존 컨트롤러-로컬 응답조립(HashMap)을 타입 record 로 대체한다(§6-4 opsdoc-search-rows-dto).
 * 키셋·값 동치로 무손실(HashMap 키순서 비결정). camelCase 키=컴포넌트명, null 포함.
 */
public record PartnerRow(Long id, String name, String type) {

    public static PartnerRow from(Partner p) {
        return new PartnerRow(p.getPartnerId(), p.getName(), p.getPartnerType());
    }
}
