package com.swmanager.system.dto.ops;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.swmanager.system.domain.SigunguCode;

/**
 * /ops-doc/api/sgg (시도→시군구 cascade) 응답 행 dto.
 *
 * 기존 컨트롤러-로컬 응답조립(HashMap)을 타입 record 로 대체한다(§6-4 opsdoc-cascade-rows-dto).
 * 키셋·값 동치로 무손실(HashMap 키순서 비결정). camelCase 키=컴포넌트명, null 포함.
 *
 * <p>{@code isUnit} 은 boolean 컴포넌트라 Jackson 이 is-접두를 깎아 키를 "unit" 으로 만들 수 있어
 * {@link JsonProperty}{@code ("isUnit")} 로 현행 키를 고정한다.
 */
public record CascadeSggRow(
        String admSectC,
        String sggNm,
        @JsonProperty("isUnit") boolean isUnit) {

    /** {@link SigunguCode} → 행. isUnit 은 현행과 동일하게 시군구명=시도명(본청/도청 self-행)일 때 true. */
    public static CascadeSggRow from(SigunguCode s) {
        return new CascadeSggRow(
                s.getAdmSectC(),
                s.getSggNm(),
                s.getSggNm() != null && s.getSggNm().equals(s.getSidoNm()));
    }
}
