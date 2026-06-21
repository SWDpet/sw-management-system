package com.swmanager.system.dto.workplan;

import com.swmanager.system.domain.SigunguCode;

/**
 * /api/sgg (시도 → 시군구 목록, 캐스케이드) 응답 행 dto.
 *
 * 기존 WorkPlanController 의 컨트롤러-로컬 HashMap 응답조립을 record 로 대체한다
 * (§6-4 doclookup-workplan-rows-dto). 키셋·값 동치로 무손실. camelCase 키=컴포넌트명.
 * {@code isUnit} = 본청/도청 여부(sgg=sido self-행).
 */
public record SggOptionRow(String admSectC, String sggNm, boolean isUnit) {

    public static SggOptionRow from(SigunguCode s) {
        boolean isUnit = s.getSggNm() != null && s.getSggNm().equals(s.getSidoNm());
        return new SggOptionRow(s.getAdmSectC(), s.getSggNm(), isUnit);
    }
}
