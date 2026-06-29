package com.swmanager.system.lsa.dto;

import com.swmanager.system.domain.PersonInfo;

/**
 * LSA prefill 담당자 후보 행 — ps_info 에서 부서/팀/이름/전화/이메일만 노출(엔티티 직접반환 금지, codex).
 */
public record PersonRow(String deptNm, String teamNm, String userNm, String tel, String email) {
    public static PersonRow fromEntity(PersonInfo p) {
        return new PersonRow(p.getDeptNm(), p.getTeamNm(), p.getUserNm(), p.getTel(), p.getEmail());
    }
}
