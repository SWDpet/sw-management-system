package com.swmanager.system.dto.ops;

import com.swmanager.system.domain.PersonInfo;

/**
 * /ops-doc/api/requester/search (요청자 공무원 검색, ps_info) 응답 행 dto.
 *
 * 기존 컨트롤러-로컬 응답조립(HashMap)을 타입 record 로 대체한다(§6-4 opsdoc-search-rows-dto).
 * 키셋·값 동치로 무손실(HashMap 키순서 비결정). camelCase 키=컴포넌트명, null 포함.
 */
public record RequesterRow(Long id, String name, String org, String dept, String pos, String tel) {

    public static RequesterRow from(PersonInfo p) {
        return new RequesterRow(
                p.getId(), p.getUserNm(), p.getOrgNm(), p.getDeptNm(), p.getPos(), p.getTel());
    }
}
