package com.swmanager.system.dto.ops;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * OpsKbForm / RejectForm 요청 바디 바인딩 골든 테스트 (opskb-request-dto §6-4).
 *
 * 기존 {@code @RequestBody} 비타입 Map 수신을 타입 DTO 로 바꾸면서, 프론트가 보내던 JSON 키를
 * 무손실 바인딩하는지 고정한다. 전역 PropertyNamingStrategy 미설정이라 snake_case 키
 * ({@code sys_type})는 {@code @JsonProperty} 로, 단일어 키는 컴포넌트명으로 바인딩됨을 검증.
 */
class OpsKbFormBindingTest {

    private final ObjectMapper om = new ObjectMapper();

    @Test
    void opsKbForm_bindsAllFormKeys_snakeCaseSysType() throws Exception {
        // kb-form.html:76 이 보내는 본문 키 그대로
        String json = "{\"sys_type\":\"UPIS\",\"gubun\":\"장애\",\"symptom\":\"증상x\","
                + "\"cause\":\"원인x\",\"action\":\"조치x\",\"prevention\":\"예방x\",\"keywords\":\"k1,k2\"}";

        OpsKbForm f = om.readValue(json, OpsKbForm.class);

        assertThat(f.sysType()).isEqualTo("UPIS");   // sys_type → sysType (@JsonProperty)
        assertThat(f.gubun()).isEqualTo("장애");
        assertThat(f.symptom()).isEqualTo("증상x");
        assertThat(f.cause()).isEqualTo("원인x");
        assertThat(f.action()).isEqualTo("조치x");
        assertThat(f.prevention()).isEqualTo("예방x");
        assertThat(f.keywords()).isEqualTo("k1,k2");
        assertThat(f.category()).isNull();           // 폼 미전송 → null (apply 가 set 건너뜀)
    }

    @Test
    void opsKbForm_missingKeys_bindNull() throws Exception {
        OpsKbForm f = om.readValue("{\"gubun\":\"업무\"}", OpsKbForm.class);
        assertThat(f.gubun()).isEqualTo("업무");
        assertThat(f.sysType()).isNull();
        assertThat(f.symptom()).isNull();
    }

    @Test
    void opsKbForm_camelCaseSysType_doesNotBind() throws Exception {
        // 전역 snake_case 미설정 근거 고정: camelCase 키는 sys_type 슬롯에 안 들어감
        OpsKbForm f = om.readValue("{\"sysType\":\"X\"}", OpsKbForm.class);
        assertThat(f.sysType()).isNull();
    }

    @Test
    void rejectForm_bindsReason() throws Exception {
        // kb-list.html:124 이 보내는 본문
        RejectForm f = om.readValue("{\"reason\":\"중복 항목\"}", RejectForm.class);
        assertThat(f.reason()).isEqualTo("중복 항목");
    }
}
