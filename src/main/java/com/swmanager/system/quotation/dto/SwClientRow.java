package com.swmanager.system.quotation.dto;

import com.swmanager.system.domain.SwProject;

/**
 * /api/quotation/sw-projects/clients (특정 시스템의 클라이언트 목록) 응답 행 dto.
 *
 * 기존 비타입 응답조립(LinkedHashMap)을 타입 record 로 대체한다(§6-4 quotation-list-dto).
 * 소비자(quotation-form.html JS)는 {@code c.client/distNm/year/projId}·{@code item.swAmt} 로 접근한다
 * ({@code projNm} 은 현재 미사용이나 응답 보존). 키·값·null 포함·키순서를 무손실 보존한다.
 *
 * <p>camelCase 키 = 컴포넌트명(무어노테이션), {@code @JsonInclude} 미부착(null 포함),
 * 컴포넌트 선언순 = 현행 put 순(projId…distNm). 타입은 {@link SwProject} 게터 1:1.
 */
public record SwClientRow(
        Long projId,
        String client,
        Long swAmt,
        String projNm,
        Integer year,
        String distNm) {

    /** {@link SwProject} → 클라이언트 응답 행. 현행 응답조립과 1:1 동치. */
    public static SwClientRow from(SwProject p) {
        return new SwClientRow(
                p.getProjId(),
                p.getClient(),
                p.getSwAmt(),
                p.getProjNm(),
                p.getYear(),
                p.getDistNm());
    }
}
