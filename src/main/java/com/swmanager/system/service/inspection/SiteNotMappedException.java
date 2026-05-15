package com.swmanager.system.service.inspection;

import lombok.Getter;

/**
 * payload.site → sw_pjt.site_code 매핑 실패. HTTP 422.
 * 운영자가 사업관리 화면에서 site_code 컬럼을 보정해야 한다.
 */
@Getter
public class SiteNotMappedException extends RuntimeException {

    private final String siteCode;

    public SiteNotMappedException(String siteCode) {
        super("site not mapped: " + siteCode);
        this.siteCode = siteCode;
    }
}
