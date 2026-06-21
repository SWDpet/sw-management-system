package com.swmanager.system.dto.inspection;

import com.swmanager.system.domain.InfraSoftware;

/**
 * /api/infra-servers 응답의 서버별 소프트웨어 행 dto (InfraServerRow.softwares 중첩).
 *
 * 기존 InspectReportController 의 컨트롤러-로컬 LinkedHashMap 응답조립을 record 로 대체한다
 * (§6-4 inspect-report-rows-dto). 키셋·값 동치로 무손실. swVer 는 현행 fallback(null→"")을 보존한다.
 */
public record SoftwareRow(String swCategory, String swNm, String swVer) {

    public static SoftwareRow from(InfraSoftware sw) {
        return new SoftwareRow(sw.getSwCategory(), sw.getSwNm(),
                sw.getSwVer() != null ? sw.getSwVer() : "");
    }
}
