package com.swmanager.system.arch;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * S8 qt-quotation-domain-normalize — V024 사후 스키마 검증.
 *
 * T3: qt_category_mst = 3 rows
 * T4: FK fk_qt_category 존재 (대상 테이블 한정)
 * T8: 마스터 외 값 insert → FK 위반
 * T9: 기존 61건 값 유지
 */
@SpringBootTest
class QtCategoryMstSchemaTest {

    @Autowired JdbcTemplate jdbc;

    @Test
    void master_has_exactly_3_rows() {
        Integer cnt = jdbc.queryForObject("SELECT COUNT(*) FROM qt_category_mst", Integer.class);
        assertThat(cnt).isEqualTo(3);
    }

    @Test
    void fk_exists_on_qt_quotation() {
        Integer cnt = jdbc.queryForObject(
            "SELECT COUNT(*) FROM pg_constraint " +
            " WHERE conname='fk_qt_category' AND conrelid='qt_quotation'::regclass",
            Integer.class);
        assertThat(cnt).isEqualTo(1);
    }

    @Test
    void master_contains_3_expected_values() {
        Integer cnt = jdbc.queryForObject(
            "SELECT COUNT(*) FROM qt_category_mst WHERE category_code IN ('유지보수','용역','제품')",
            Integer.class);
        assertThat(cnt).isEqualTo(3);
    }

    @Test
    void existing_61_rows_preserved() {
        // V-opt1 (한글 값 유지) 검증 — 모든 qt_quotation.category 가 마스터 3종 내
        Integer out_of_master = jdbc.queryForObject(
            "SELECT COUNT(*) FROM qt_quotation WHERE category NOT IN (SELECT category_code FROM qt_category_mst)",
            Integer.class);
        assertThat(out_of_master).isZero();
    }

    @Test
    void unknown_category_insert_is_rejected() {
        // qt_quotation 에 마스터 외 category 값 insert 시도 → FK 위반
        assertThatThrownBy(() -> jdbc.update(
            "INSERT INTO qt_quotation (quote_number, category, quote_date) " +
            "VALUES (?, ?, CURRENT_DATE)",
            "__S8_TEST__", "__INVALID_CAT__"))
            .isInstanceOf(DataAccessException.class);
    }
}
