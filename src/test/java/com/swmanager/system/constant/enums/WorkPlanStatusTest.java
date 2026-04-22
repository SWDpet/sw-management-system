package com.swmanager.system.constant.enums;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WorkPlanStatusTest {

    @Test
    void enum_has_exactly_7_values() {
        assertThat(WorkPlanStatus.values()).hasSize(7);
    }

    @Test
    void label_is_frozen() {
        assertThat(WorkPlanStatus.PLANNED.getLabel()).isEqualTo("예정");
        assertThat(WorkPlanStatus.CONTACTED.getLabel()).isEqualTo("연락완료");
        assertThat(WorkPlanStatus.CONFIRMED.getLabel()).isEqualTo("확정");
        assertThat(WorkPlanStatus.IN_PROGRESS.getLabel()).isEqualTo("진행중");
        assertThat(WorkPlanStatus.COMPLETED.getLabel()).isEqualTo("완료");
        assertThat(WorkPlanStatus.POSTPONED.getLabel()).isEqualTo("연기");
        assertThat(WorkPlanStatus.CANCELLED.getLabel()).isEqualTo("취소");
    }

    @Test
    void fromKoLabel_exact_and_trim() {
        assertThat(WorkPlanStatus.fromKoLabel("확정")).isEqualTo(WorkPlanStatus.CONFIRMED);
        assertThat(WorkPlanStatus.fromKoLabel("  진행중  ")).isEqualTo(WorkPlanStatus.IN_PROGRESS);
        assertThat(WorkPlanStatus.fromKoLabel("없는값")).isNull();
    }

    @Test
    void fromJson_by_label_and_name() throws Exception {
        ObjectMapper m = new ObjectMapper();
        assertThat(m.readValue("\"확정\"", WorkPlanStatus.class)).isEqualTo(WorkPlanStatus.CONFIRMED);
        assertThat(m.readValue("\" CONFIRMED \"", WorkPlanStatus.class)).isEqualTo(WorkPlanStatus.CONFIRMED);
    }

    @Test
    void jsonValue_is_label() throws Exception {
        ObjectMapper m = new ObjectMapper();
        assertThat(m.writeValueAsString(WorkPlanStatus.CANCELLED)).isEqualTo("\"취소\"");
    }

    @Test
    void labelOf_null_safe() {
        assertThat(WorkPlanStatus.labelOf(null)).isEqualTo("");
        assertThat(WorkPlanStatus.labelOf("PLANNED")).isEqualTo("예정");
        assertThat(WorkPlanStatus.labelOf("UNKNOWN")).isEqualTo("UNKNOWN");
    }
}
