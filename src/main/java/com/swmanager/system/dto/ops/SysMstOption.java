package com.swmanager.system.dto.ops;

import com.swmanager.system.domain.SysMst;

/**
 * 시스템 마스터(sys_mst) 드롭다운 옵션 dto — {@code {cd, nm}}.
 *
 * 기존 컨트롤러-로컬 모델조립(LinkedHashMap)을 타입 record 로 대체한다(§6-4 opskb-sysoption-record).
 * Thymeleaf {@code ${s.cd}}/{@code ${s.nm}} 는 record accessor 로 동치 — 무손실.
 * (응답 행 dto {@link CascadeSystemRow} 와 셰이프는 같으나 용도가 달라 별도 명명.)
 */
public record SysMstOption(String cd, String nm) {

    public static SysMstOption from(SysMst s) {
        return new SysMstOption(s.getCd(), s.getNm());
    }
}
