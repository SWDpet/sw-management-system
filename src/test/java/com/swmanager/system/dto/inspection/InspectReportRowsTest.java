package com.swmanager.system.dto.inspection;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swmanager.system.domain.InfraServer;
import com.swmanager.system.domain.InfraSoftware;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * InfraServerRow / SoftwareRow / SnapshotRow 직렬화 골든 테스트 (inspect-report-rows-dto §6-4).
 *
 * 기존 InspectReportController 의 컨트롤러-로컬 LinkedHashMap 응답조립을 record 로 바꾸면서,
 * /api/infra-servers · /api/inspect-snapshots 응답 JSON 무손실(키셋·값·null·민감필드 제외)을 고정.
 * 현행 LinkedHashMap 이라 키순서 결정적이나 클라이언트 키접근 → JsonNode tree 동치만 검증.
 */
class InspectReportRowsTest {

    private final ObjectMapper om = new ObjectMapper();

    private void assertTreeEqual(Object record, Object legacy) {
        JsonNode a = om.valueToTree(record);
        JsonNode b = om.valueToTree(legacy);
        assertThat(a).isEqualTo(b);
    }

    /** 현행 컨트롤러 로직을 그대로 복제한 레거시 서버 행(검증 기준, 16키 화이트리스트). */
    private Map<String, Object> legacyServer(InfraServer s) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("serverId", s.getServerId());
        m.put("serverType", s.getServerType());
        m.put("hostName", s.getHostName());
        m.put("ipAddr", s.getIpAddr());
        m.put("osNm", s.getOsNm());
        m.put("serverModel", s.getServerModel());
        m.put("serialNo", s.getSerialNo());
        m.put("cpuSpec", s.getCpuSpec());
        m.put("memorySpec", s.getMemorySpec());
        m.put("diskSpec", s.getDiskSpec());
        m.put("networkSpec", s.getNetworkSpec());
        m.put("powerSpec", s.getPowerSpec());
        m.put("osDetail", s.getOsDetail());
        m.put("rackLocation", s.getRackLocation());
        m.put("note", s.getNote());
        List<Map<String, String>> swList = new java.util.ArrayList<>();
        for (var sw : s.getSoftwares()) {
            Map<String, String> swMap = new LinkedHashMap<>();
            swMap.put("swCategory", sw.getSwCategory());
            swMap.put("swNm", sw.getSwNm());
            swMap.put("swVer", sw.getSwVer() != null ? sw.getSwVer() : "");
            swList.add(swMap);
        }
        m.put("softwares", swList);
        return m;
    }

    @Test
    void infraServerRow_full_matchesLegacy() {
        InfraServer s = new InfraServer();
        s.setServerId(11L);
        s.setServerType("WEB");
        s.setHostName("web-01");
        s.setIpAddr("10.0.0.1");
        s.setOsNm("Rocky Linux 8");
        s.setServerModel("PowerEdge R740");
        s.setSerialNo("SN12345");
        s.setCpuSpec("Xeon Gold 6230");
        s.setMemorySpec("128GB");
        s.setDiskSpec("SSD 2TB");
        s.setNetworkSpec("10GbE");
        s.setPowerSpec("750W x2");
        s.setOsDetail("kernel 4.18");
        s.setRackLocation("A-12");
        s.setNote("비고");
        InfraSoftware sw = new InfraSoftware();
        sw.setSwCategory("WAS");
        sw.setSwNm("Tomcat");
        sw.setSwVer("9.0");
        s.getSoftwares().add(sw);

        assertTreeEqual(InfraServerRow.from(s), legacyServer(s));

        // 민감필드(MAC/accId/accPw) 비노출 확인 — 16키만
        JsonNode j = om.valueToTree(InfraServerRow.from(s));
        assertThat(j.size()).isEqualTo(16);
        assertThat(j.has("macAddr")).isFalse();
        assertThat(j.has("accId")).isFalse();
        assertThat(j.has("accPw")).isFalse();
    }

    @Test
    void infraServerRow_nullsAndSwVerFallback() {
        InfraServer s = new InfraServer();
        s.setServerId(12L);   // 나머지 spec null
        InfraSoftware sw = new InfraSoftware();
        sw.setSwCategory("DB");
        sw.setSwNm("PostgreSQL");
        // swVer null → "" fallback
        s.getSoftwares().add(sw);

        assertTreeEqual(InfraServerRow.from(s), legacyServer(s));

        JsonNode j = om.valueToTree(InfraServerRow.from(s));
        assertThat(j.get("cpuSpec").isNull()).isTrue();          // null 키 보존
        assertThat(j.get("softwares").get(0).get("swVer").asText()).isEmpty(); // ""
    }

    @Test
    void infraServerRow_emptySoftwares() {
        InfraServer s = new InfraServer();
        s.setServerId(13L);
        JsonNode j = om.valueToTree(InfraServerRow.from(s));
        assertThat(j.get("softwares").isArray()).isTrue();
        assertThat(j.get("softwares").size()).isZero();
    }

    @Test
    void snapshotRow_matchesLegacy_andNulls() {
        Map<String, Object> legacy = new LinkedHashMap<>();
        legacy.put("serverRole", "WEB");
        legacy.put("hostName", "web-01");
        legacy.put("cpu", "Xeon");
        legacy.put("memory", "128GB");
        legacy.put("disk", "2TB");
        assertTreeEqual(new SnapshotRow("WEB", "web-01", "Xeon", "128GB", "2TB"), legacy);

        // cpu/memory/disk null 보존(현장 미수집 항목)
        Map<String, Object> legacyNull = new LinkedHashMap<>();
        legacyNull.put("serverRole", "DB");
        legacyNull.put("hostName", "db-01");
        legacyNull.put("cpu", null);
        legacyNull.put("memory", null);
        legacyNull.put("disk", null);
        SnapshotRow r = new SnapshotRow("DB", "db-01", null, null, null);
        assertTreeEqual(r, legacyNull);
        JsonNode j = om.valueToTree(r);
        assertThat(j.size()).isEqualTo(5);
        assertThat(j.get("cpu").isNull()).isTrue();
    }
}
