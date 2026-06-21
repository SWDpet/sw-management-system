package com.swmanager.system.dto.workplan;

import com.swmanager.system.domain.SigunguCode;

/**
 * /api/region-sigungus (시도 → 시군구 목록) 응답 행 dto.
 *
 * 기존 DocumentLookupController 의 컨트롤러-로컬 LinkedHashMap 응답조립을 record 로 대체한다
 * (§6-4 doclookup-workplan-rows-dto). 키셋·값 동치로 무손실. camelCase 키=컴포넌트명, null 포함.
 */
public record RegionSigunguRow(String admSectC, String sggNm, String sidoNm) {

    public static RegionSigunguRow from(SigunguCode s) {
        return new RegionSigunguRow(s.getAdmSectC(), s.getSggNm(), s.getSidoNm());
    }
}
