package com.swmanager.system.sql;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * S15 pjt-equip-decision — T3.
 * V025 실행 후 pjt_equip 테이블 미존재 검증.
 */
@SpringBootTest
class PjtEquipSchemaTest {

    @Autowired JdbcTemplate jdbc;

    @Test
    void pjt_equip_table_is_dropped() {
        Integer cnt = jdbc.queryForObject(
            "SELECT COUNT(*) FROM information_schema.tables " +
            " WHERE table_schema='public' AND table_name='pjt_equip'",
            Integer.class);
        assertThat(cnt).isZero();
    }
}
