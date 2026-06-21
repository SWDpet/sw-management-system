package com.swmanager.system.dto.workplan;

import com.swmanager.system.domain.SwProject;

/**
 * /api/projects (연도+지자체+시스템 → 사업 목록) 응답 행 dto.
 *
 * 기존 DocumentLookupController 의 컨트롤러-로컬 HashMap 응답조립을 record 로 대체한다
 * (§6-4 doclookup-workplan-rows-dto). 키셋·값 동치로 무손실. camelCase 키=컴포넌트명, null 포함.
 */
public record ProjectFilterRow(Long projId, Integer year, String projNm, String sysNm,
                               String sysNmEn, Long contAmt, String cityNm, String distNm) {

    public static ProjectFilterRow from(SwProject p) {
        return new ProjectFilterRow(p.getProjId(), p.getYear(), p.getProjNm(), p.getSysNm(),
                p.getSysNmEn(), p.getContAmt(), p.getCityNm(), p.getDistNm());
    }
}
