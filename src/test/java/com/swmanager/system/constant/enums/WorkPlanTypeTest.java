package com.swmanager.system.constant.enums;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WorkPlanTypeTest {

    @Test
    void enum_has_exactly_10_values() {
        assertThat(WorkPlanType.values()).hasSize(10);
    }

    @Test
    void label_and_color_are_frozen() {
        assertThat(WorkPlanType.CONTRACT.getLabel()).isEqualTo("계약");
        assertThat(WorkPlanType.CONTRACT.getColor()).isEqualTo("#1565c0");
        assertThat(WorkPlanType.PATCH.getLabel()).isEqualTo("패치");
        assertThat(WorkPlanType.PATCH.getColor()).isEqualTo("#00897b");
        assertThat(WorkPlanType.ETC.getLabel()).isEqualTo("기타");
    }

    @Test
    void fromKoLabel_exact_match_and_trim() {
        assertThat(WorkPlanType.fromKoLabel("계약")).isEqualTo(WorkPlanType.CONTRACT);
        assertThat(WorkPlanType.fromKoLabel("  패치  ")).isEqualTo(WorkPlanType.PATCH);
        assertThat(WorkPlanType.fromKoLabel("없는값")).isNull();
    }

    @Test
    void fromJson_by_label_and_name() throws Exception {
        ObjectMapper m = new ObjectMapper();
        assertThat(m.readValue("\"패치\"", WorkPlanType.class)).isEqualTo(WorkPlanType.PATCH);
        assertThat(m.readValue("\" CONTRACT \"", WorkPlanType.class)).isEqualTo(WorkPlanType.CONTRACT);
    }

    @Test
    void jsonValue_is_label() throws Exception {
        ObjectMapper m = new ObjectMapper();
        assertThat(m.writeValueAsString(WorkPlanType.INSPECT)).isEqualTo("\"점검\"");
    }

    @Test
    void labelOf_and_colorOf_null_safe() {
        assertThat(WorkPlanType.labelOf(null)).isEqualTo("");
        assertThat(WorkPlanType.colorOf(null)).isEqualTo("#858796");
        assertThat(WorkPlanType.labelOf("PATCH")).isEqualTo("패치");
        assertThat(WorkPlanType.colorOf("PATCH")).isEqualTo("#00897b");
        assertThat(WorkPlanType.labelOf("NON_EXISTENT")).isEqualTo("NON_EXISTENT");
        assertThat(WorkPlanType.colorOf("NON_EXISTENT")).isEqualTo("#858796");
    }
}
