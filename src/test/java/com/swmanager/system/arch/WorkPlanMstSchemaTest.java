package com.swmanager.system.arch;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * S16 tb-work-plan-decision — V026 사후 스키마 검증.
 */
@SpringBootTest
class WorkPlanMstSchemaTest {

    @Autowired JdbcTemplate jdbc;

    @Test
    void type_mst_has_exactly_10_rows() {
        Integer cnt = jdbc.queryForObject("SELECT COUNT(*) FROM work_plan_type_mst", Integer.class);
        assertThat(cnt).isEqualTo(10);
    }

    @Test
    void status_mst_has_exactly_7_rows() {
        Integer cnt = jdbc.queryForObject("SELECT COUNT(*) FROM work_plan_status_mst", Integer.class);
        assertThat(cnt).isEqualTo(7);
    }

    @Test
    void fk_twp_type_exists() {
        Integer cnt = jdbc.queryForObject(
            "SELECT COUNT(*) FROM pg_constraint " +
            " WHERE conname='fk_twp_type' AND conrelid='tb_work_plan'::regclass",
            Integer.class);
        assertThat(cnt).isEqualTo(1);
    }

    @Test
    void fk_twp_status_exists() {
        Integer cnt = jdbc.queryForObject(
            "SELECT COUNT(*) FROM pg_constraint " +
            " WHERE conname='fk_twp_status' AND conrelid='tb_work_plan'::regclass",
            Integer.class);
        assertThat(cnt).isEqualTo(1);
    }

    @Test
    void existing_row_is_preserved() {
        // 기존 1 row (PATCH/CONFIRMED) 값 유지 검증 (NFR-5)
        Integer cnt = jdbc.queryForObject(
            "SELECT COUNT(*) FROM tb_work_plan WHERE plan_type='PATCH' AND status='CONFIRMED'",
            Integer.class);
        assertThat(cnt).isGreaterThanOrEqualTo(1);
    }
}
