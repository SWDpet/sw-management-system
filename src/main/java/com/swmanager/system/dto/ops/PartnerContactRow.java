package com.swmanager.system.dto.ops;

import com.swmanager.system.domain.ops.PartnerContact;

/**
 * /ops-doc/api/partner-contact/search (요청자 업체담당자 검색, tb_partner_contact) 응답 행 dto.
 *
 * 기존 컨트롤러-로컬 응답조립(HashMap)을 타입 record 로 대체한다(§6-4 opsdoc-search-rows-dto).
 * 키셋·값 동치로 무손실(HashMap 키순서 비결정). camelCase 키=컴포넌트명, null 포함.
 */
public record PartnerContactRow(Long id, String name, String org, String pos, String tel) {

    /** {@link PartnerContact} → 행. org 는 현행과 동일하게 소속 업체명(없으면 null). */
    public static PartnerContactRow from(PartnerContact c) {
        return new PartnerContactRow(
                c.getContactId(),
                c.getName(),
                c.getPartner() != null ? c.getPartner().getName() : null,
                c.getPosition(),
                c.getTel());
    }
}
