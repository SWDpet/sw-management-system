package com.swmanager.system.dto.ops;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.swmanager.system.domain.ops.OpsDocPartner;
import com.swmanager.system.domain.ops.Partner;

/**
 * /ops-doc/api/{docId}/partners (문서 협력업체 목록, 수정 폼 프리필) 응답 행 dto.
 *
 * 기존 컨트롤러-로컬 응답조립(HashMap)을 타입 record 로 대체한다(§6-4 opsdoc-search-rows-dto).
 * 키셋·값 동치로 무손실(HashMap 키순서 비결정). snake_case 키({@code partner_id}/{@code role_label})는
 * {@link JsonProperty} 매핑, {@code @JsonInclude} 미부착(null 포함).
 */
public record DocPartnerRow(
        @JsonProperty("partner_id") Long partnerId,
        String name,
        @JsonProperty("role_label") String roleLabel) {

    /** {@link OpsDocPartner} + 조회된 {@link Partner} → 행. name 은 현행과 동일하게 업체명, 없으면 {@code "#"+id}. */
    public static DocPartnerRow from(OpsDocPartner dp, Partner p) {
        return new DocPartnerRow(
                dp.getPartnerId(),
                p != null ? p.getName() : ("#" + dp.getPartnerId()),
                dp.getRoleLabel());
    }
}
