package com.swmanager.system.lsa.dto;

import com.swmanager.system.lsa.domain.Lsa;

import java.time.LocalDateTime;

/**
 * LSA 목록 행 DTO (MapDebt 금지 — 타입화).
 */
public record LsaDTO(
        Long id,
        String cityNm,
        String distNm,
        String deptNm,
        String teamNm,
        String userNm,
        String tel,
        String email,
        String version,
        String issuer,
        LocalDateTime createdAt,
        String createdBy,
        LocalDateTime updatedAt,
        String updatedBy
) {
    public static LsaDTO fromEntity(Lsa l) {
        return new LsaDTO(
                l.getId(), l.getCityNm(), l.getDistNm(), l.getDeptNm(), l.getTeamNm(),
                l.getUserNm(), l.getTel(), l.getEmail(), l.getVersion(), l.getIssuer(),
                l.getCreatedAt(), l.getCreatedBy(), l.getUpdatedAt(), l.getUpdatedBy());
    }
}
