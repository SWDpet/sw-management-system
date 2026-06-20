package com.swmanager.system.dto.ops;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * OpsDocForm 요청 바디 바인딩 골든 테스트 (opsdoc-createupdate-dto §6-4).
 *
 * 핵심 문서 CRUD(create/update) 의 비타입 Map 수신을 타입 DTO 로 바꾸면서, 프론트(doc-fault.html:187 +
 * ops-doc-relations.js)가 보내던 키를 무손실 바인딩하는지 고정한다. 특히 중첩 section_data(동적 jsonb Map 유지)와
 * partners 리스트(PartnerRef), id 류 숫자 바인딩을 검증한다.
 */
class OpsDocCreateFormBindingTest {

    private final ObjectMapper om = new ObjectMapper();

    @Test
    void opsDocForm_faultBody_bindsScalarsAndNestedSection() throws Exception {
        // doc-fault.html:187 본문 (environment/support_target_type 미전송)
        String json = "{"
                + "\"title\":\"장애보고\",\"sys_type\":\"UPIS\",\"region_code\":\"46810\","
                + "\"engineer_id\":12,\"requester_kind\":\"PERSON\",\"requester_id\":7,"
                + "\"partners\":[{\"partner_id\":3,\"role_label\":\"유지보수\"},{\"partner_id\":4,\"role_label\":\"\"}],"
                + "\"section_data\":{\"fault_date\":\"2026-06-21\",\"severity\":\"HIGH\",\"downtime_minutes\":30,\"symptom\":\"증상\"}"
                + "}";

        OpsDocForm f = om.readValue(json, OpsDocForm.class);

        assertThat(f.title()).isEqualTo("장애보고");
        assertThat(f.sysType()).isEqualTo("UPIS");          // sys_type
        assertThat(f.regionCode()).isEqualTo("46810");      // region_code
        assertThat(f.environment()).isNull();               // 미전송
        assertThat(f.supportTargetType()).isNull();
        assertThat(f.engineerId()).isEqualTo(12L);          // 숫자 → Long
        assertThat(f.requesterKind()).isEqualTo("PERSON");
        assertThat(f.requesterId()).isEqualTo(7L);
        // 중첩 section_data 는 Map 으로 보존 (동적 jsonb)
        assertThat(f.sectionData()).containsEntry("fault_date", "2026-06-21")
                .containsEntry("severity", "HIGH").containsEntry("downtime_minutes", 30)
                .containsEntry("symptom", "증상");
        // partners 리스트 → PartnerRef
        assertThat(f.partners()).hasSize(2);
        assertThat(f.partners().get(0).partnerId()).isEqualTo(3L);
        assertThat(f.partners().get(0).roleLabel()).isEqualTo("유지보수");
        assertThat(f.partners().get(1).roleLabel()).isEqualTo("");
    }

    @Test
    void opsDocForm_supportBody_bindsEnvironmentAndTarget() throws Exception {
        String json = "{\"title\":\"지원\",\"environment\":\"운영\",\"support_target_type\":\"SERVER\"}";
        OpsDocForm f = om.readValue(json, OpsDocForm.class);
        assertThat(f.environment()).isEqualTo("운영");
        assertThat(f.supportTargetType()).isEqualTo("SERVER");
    }

    @Test
    void opsDocForm_emptyBody_allNull() throws Exception {
        OpsDocForm f = om.readValue("{}", OpsDocForm.class);
        assertThat(f.title()).isNull();
        assertThat(f.sectionData()).isNull();
        assertThat(f.partners()).isNull();
        assertThat(f.engineerId()).isNull();
    }

    @Test
    void opsDocForm_unknownKeys_ignored() throws Exception {
        OpsDocForm f = om.readValue("{\"title\":\"x\",\"legacy_field\":99}", OpsDocForm.class);
        assertThat(f.title()).isEqualTo("x");
    }
}
