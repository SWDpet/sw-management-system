package com.swmanager.system.constant.enums;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * S1 inspect-comprehensive-redesign — InspectResultCode 단위 테스트.
 */
class InspectResultCodeTest {

    @Test
    void enum_has_exactly_4_values() {
        assertThat(InspectResultCode.values()).hasSize(4);
    }

    @Test
    void label_is_frozen_korean() {
        assertThat(InspectResultCode.NORMAL.getLabel()).isEqualTo("정상");
        assertThat(InspectResultCode.PARTIAL.getLabel()).isEqualTo("부분정상");
        assertThat(InspectResultCode.ABNORMAL.getLabel()).isEqualTo("비정상");
        assertThat(InspectResultCode.NOT_APPLICABLE.getLabel()).isEqualTo("해당없음");
    }

    @Test
    void fromKoLabel_exact_match() {
        assertThat(InspectResultCode.fromKoLabel("정상")).isEqualTo(InspectResultCode.NORMAL);
        assertThat(InspectResultCode.fromKoLabel("비정상")).isEqualTo(InspectResultCode.ABNORMAL);
    }

    @Test
    void fromKoLabel_trim() {
        assertThat(InspectResultCode.fromKoLabel("  정상  ")).isEqualTo(InspectResultCode.NORMAL);
    }

    @Test
    void fromKoLabel_unknown_returns_null() {
        assertThat(InspectResultCode.fromKoLabel("없는값")).isNull();
        assertThat(InspectResultCode.fromKoLabel(null)).isNull();
    }

    @Test
    void fromJson_by_label_and_name_with_trim() throws Exception {
        ObjectMapper m = new ObjectMapper();
        assertThat(m.readValue("\"정상\"", InspectResultCode.class)).isEqualTo(InspectResultCode.NORMAL);
        assertThat(m.readValue("\" NORMAL \"", InspectResultCode.class)).isEqualTo(InspectResultCode.NORMAL);
    }

    @Test
    void jsonValue_is_label() throws Exception {
        ObjectMapper m = new ObjectMapper();
        assertThat(m.writeValueAsString(InspectResultCode.ABNORMAL)).isEqualTo("\"비정상\"");
    }
}
