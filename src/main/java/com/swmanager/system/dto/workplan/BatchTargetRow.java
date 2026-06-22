package com.swmanager.system.dto.workplan;

import com.swmanager.system.domain.SwProject;

/**
 * /document/api/batch/targets (일괄작성 대상 사업 목록) 응답 행 dto.
 *
 * 기존 DocumentController.getBatchTargets 의 컨트롤러-로컬 HashMap 응답조립을 record 로 대체한다
 * (§6-4 document-batch-rows-dto). 키셋·값 동치로 무손실. contDt/startDt/endDt 는 현행과 동일하게
 * LocalDate → toString()(null 보존). null 포함.
 */
public record BatchTargetRow(Long projId, String projNm, String sysNmEn, String cityNm, String distNm,
                             String orgNm, Long contAmt, String contDt, String startDt, String endDt,
                             String client) {

    public static BatchTargetRow from(SwProject p) {
        return new BatchTargetRow(
                p.getProjId(), p.getProjNm(), p.getSysNmEn(), p.getCityNm(), p.getDistNm(),
                p.getOrgNm(), p.getContAmt(),
                p.getContDt() != null ? p.getContDt().toString() : null,
                p.getStartDt() != null ? p.getStartDt().toString() : null,
                p.getEndDt() != null ? p.getEndDt().toString() : null,
                p.getClient());
    }
}
