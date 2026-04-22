package com.swmanager.system.constant.enums;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * S8 qt-quotation-domain-normalize — QtCategory 단위 테스트.
 */
class QtCategoryTest {

    @Test
    void enum_has_exactly_3_values() {
        assertThat(QtCategory.values()).hasSize(3);
    }

    @Test
    void label_is_frozen_korean() {
        assertThat(QtCategory.MAINTENANCE.getLabel()).isEqualTo("유지보수");
        assertThat(QtCategory.SERVICE.getLabel()).isEqualTo("용역");
        assertThat(QtCategory.PRODUCT.getLabel()).isEqualTo("제품");
    }

    @Test
    void fromKoLabel_exact_match() {
        assertThat(QtCategory.fromKoLabel("유지보수")).isEqualTo(QtCategory.MAINTENANCE);
        assertThat(QtCategory.fromKoLabel("용역")).isEqualTo(QtCategory.SERVICE);
        assertThat(QtCategory.fromKoLabel("제품")).isEqualTo(QtCategory.PRODUCT);
    }

    @Test
    void fromKoLabel_trim() {
        assertThat(QtCategory.fromKoLabel("  유지보수  ")).isEqualTo(QtCategory.MAINTENANCE);
    }

    @Test
    void fromKoLabel_unknown_returns_null() {
        assertThat(QtCategory.fromKoLabel("없는값")).isNull();
        assertThat(QtCategory.fromKoLabel(null)).isNull();
    }

    @Test
    void fromJson_by_label_and_name_with_trim() throws Exception {
        ObjectMapper m = new ObjectMapper();
        assertThat(m.readValue("\"유지보수\"", QtCategory.class)).isEqualTo(QtCategory.MAINTENANCE);
        assertThat(m.readValue("\" SERVICE \"", QtCategory.class)).isEqualTo(QtCategory.SERVICE);
    }

    @Test
    void jsonValue_is_label() throws Exception {
        ObjectMapper m = new ObjectMapper();
        assertThat(m.writeValueAsString(QtCategory.PRODUCT)).isEqualTo("\"제품\"");
    }
}
