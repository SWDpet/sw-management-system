package com.swmanager.system.dto.workplan;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swmanager.system.domain.workplan.ProcessMaster;
import com.swmanager.system.domain.workplan.ServicePurpose;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ProcessMasterRow / ServicePurposeRow 직렬화 골든 테스트 (doclookup-rows-dto §6-4).
 *
 * 기존 DocumentLookupController 의 컨트롤러-로컬 HashMap 응답조립을 record 로 바꾸면서,
 * /api/process-master · /api/service-purpose 응답 JSON 이 무손실(키셋·값·null 포함)인지 고정한다.
 * 현행이 HashMap 이라 키순서는 비결정 → JsonNode tree 동치만 검증.
 */
class DocLookupRowsTest {

    private final ObjectMapper om = new ObjectMapper();

    private void assertTreeEqual(Object record, Map<String, Object> legacy) {
        JsonNode a = om.valueToTree(record);
        JsonNode b = om.valueToTree(legacy);
        assertThat(a).isEqualTo(b);
    }

    @Test
    void processMasterRow_matchesLegacy() {
        ProcessMaster pm = new ProcessMaster();
        pm.setProcessId(7);
        pm.setProcessName("착수 단계");
        Map<String, Object> m = new HashMap<>();
        m.put("processId", pm.getProcessId());
        m.put("processName", pm.getProcessName());
        assertTreeEqual(ProcessMasterRow.from(pm), m);

        ProcessMaster empty = new ProcessMaster();
        empty.setProcessId(8);                  // processName null
        JsonNode j = om.valueToTree(ProcessMasterRow.from(empty));
        assertThat(j.size()).isEqualTo(2);
        assertThat(j.has("processName")).isTrue();
        assertThat(j.get("processName").isNull()).isTrue();
    }

    @Test
    void servicePurposeRow_matchesLegacy() {
        ServicePurpose sp = new ServicePurpose();
        sp.setPurposeId(11);
        sp.setPurposeType("PURPOSE");
        sp.setPurposeText("UPIS 최신 버전 유지");
        Map<String, Object> m = new HashMap<>();
        m.put("purposeId", sp.getPurposeId());
        m.put("purposeType", sp.getPurposeType());
        m.put("purposeText", sp.getPurposeText());
        assertTreeEqual(ServicePurposeRow.from(sp), m);

        ServicePurpose empty = new ServicePurpose();
        empty.setPurposeId(12);                 // type/text null
        JsonNode j = om.valueToTree(ServicePurposeRow.from(empty));
        assertThat(j.size()).isEqualTo(3);
        assertThat(j.has("purposeText")).isTrue();
        assertThat(j.get("purposeText").isNull()).isTrue();
    }
}
