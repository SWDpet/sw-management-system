package com.swmanager.system.dto.inspection;

import com.swmanager.system.domain.InfraServer;

import java.util.List;

/**
 * /api/infra-servers (인프라 서버정보 조회) 응답 행 dto.
 *
 * 기존 InspectReportController 의 컨트롤러-로컬 LinkedHashMap 응답조립을 record 로 대체한다
 * (§6-4 inspect-report-rows-dto). 키셋·값 동치로 무손실. camelCase 키=컴포넌트명, null 포함.
 *
 * <p><b>민감정보 제외(감사 P2 1-4)</b>: MAC 주소·계정 ID/PW 등은 현행과 동일하게 응답에서 제외한다.
 * 아래 16키 화이트리스트만 매핑하며, 민감필드를 신규로 노출하지 않는다.
 */
public record InfraServerRow(Long serverId, String serverType, String hostName, String ipAddr,
                             String osNm, String serverModel, String serialNo, String cpuSpec,
                             String memorySpec, String diskSpec, String networkSpec, String powerSpec,
                             String osDetail, String rackLocation, String note,
                             List<SoftwareRow> softwares) {

    public static InfraServerRow from(InfraServer s) {
        List<SoftwareRow> softwares = s.getSoftwares().stream().map(SoftwareRow::from).toList();
        return new InfraServerRow(
                s.getServerId(), s.getServerType(), s.getHostName(), s.getIpAddr(),
                s.getOsNm(), s.getServerModel(), s.getSerialNo(), s.getCpuSpec(),
                s.getMemorySpec(), s.getDiskSpec(), s.getNetworkSpec(), s.getPowerSpec(),
                s.getOsDetail(), s.getRackLocation(), s.getNote(), softwares);
    }
}
