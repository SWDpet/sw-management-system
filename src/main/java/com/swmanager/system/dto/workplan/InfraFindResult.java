package com.swmanager.system.dto.workplan;

import com.swmanager.system.domain.Infra;

/**
 * /api/infra-find 발견 응답 dto — `{found:true, infraId, cityNm, distNm, sysNm, sysNmEn}`(6키).
 *
 * 기존 DocumentLookupController 의 컨트롤러-로컬 HashMap 응답조립을 record 로 대체한다
 * (§6-4 doclookup-workplan-rows-dto). 현행은 발견 시 6키를 항상 put(값이 null 이어도 키 유지) →
 * <b>@JsonInclude 를 부착하지 않아</b> 6키 모두 직렬화(null 포함)하여 무손실 보존한다.
 * 미발견(`{found:false}` 단일키)은 키셋이 달라 별도 {@link InfraNotFound} 로 표현한다.
 */
public record InfraFindResult(boolean found, Long infraId, String cityNm,
                              String distNm, String sysNm, String sysNmEn) {

    public static InfraFindResult of(Infra infra) {
        return new InfraFindResult(true, infra.getInfraId(), infra.getCityNm(),
                infra.getDistNm(), infra.getSysNm(), infra.getSysNmEn());
    }
}
