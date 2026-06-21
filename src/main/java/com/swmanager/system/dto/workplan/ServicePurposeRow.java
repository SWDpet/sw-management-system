package com.swmanager.system.dto.workplan;

import com.swmanager.system.domain.workplan.ServicePurpose;

/**
 * /api/service-purpose (시스템별 용역목적/과업내용) 응답 행 dto.
 *
 * 기존 컨트롤러-로컬 응답조립(HashMap)을 타입 record 로 대체한다(§6-4 doclookup-rows-dto).
 * 키셋·값 동치로 무손실(HashMap 키순서 비결정). camelCase 키=컴포넌트명, null 포함.
 */
public record ServicePurposeRow(Integer purposeId, String purposeType, String purposeText) {

    public static ServicePurposeRow from(ServicePurpose sp) {
        return new ServicePurposeRow(sp.getPurposeId(), sp.getPurposeType(), sp.getPurposeText());
    }
}
