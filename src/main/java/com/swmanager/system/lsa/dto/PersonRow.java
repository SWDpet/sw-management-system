package com.swmanager.system.lsa.dto;

import com.swmanager.system.domain.PersonInfo;

/**
 * LSA prefill 담당자 후보 행 — ps_info 에서 userNm/tel/email 만 노출(엔티티 직접반환 금지, codex).
 */
public record PersonRow(String userNm, String tel, String email) {
    public static PersonRow fromEntity(PersonInfo p) {
        return new PersonRow(p.getUserNm(), p.getTel(), p.getEmail());
    }
}
