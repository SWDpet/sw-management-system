package com.swmanager.system.dto.ops;

import com.swmanager.system.domain.SysMst;

/**
 * /ops-doc/api/systems (시스템 마스터 sys_mst 전체) 응답 행 dto.
 *
 * 기존 컨트롤러-로컬 응답조립(HashMap)을 타입 record 로 대체한다(§6-4 opsdoc-cascade-rows-dto).
 * 키셋·값 동치로 무손실(HashMap 키순서 비결정). camelCase 키=컴포넌트명, null 포함.
 */
public record CascadeSystemRow(String cd, String nm) {

    public static CascadeSystemRow from(SysMst s) {
        return new CascadeSystemRow(s.getCd(), s.getNm());
    }
}
