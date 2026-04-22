package com.swmanager.system.arch;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * S10 inspect-check-result-category-master — V023 사후 스키마 검증.
 *
 * T3: check_category_mst = 16 rows
 * T4: 공백 X 2쌍 잔존 = 0 (양 테이블)
 * T5: FK 2개 존재 (fk_it_category, fk_icr_category)
 * T6: inspect_template.category NOT NULL
 */
@SpringBootTest
class CheckCategoryMstArchTest {

    @Autowired JdbcTemplate jdbc;

    @Test
    void master_has_exactly_16_rows() {
        Integer cnt = jdbc.queryForObject("SELECT COUNT(*) FROM check_category_mst", Integer.class);
        assertThat(cnt).isEqualTo(16);
    }

    @Test
    void no_whitespace_variant_in_inspect_template() {
        Integer cnt = jdbc.queryForObject(
            "SELECT COUNT(*) FROM inspect_template WHERE category IN (" +
            "  'GeoNURIS GeoWeb Server(GWS)','GeoNURIS Spatial Server(GSS)')", Integer.class);
        assertThat(cnt).isZero();
    }

    @Test
    void no_whitespace_variant_in_inspect_check_result() {
        Integer cnt = jdbc.queryForObject(
            "SELECT COUNT(*) FROM inspect_check_result WHERE category IN (" +
            "  'GeoNURIS GeoWeb Server(GWS)','GeoNURIS Spatial Server(GSS)')", Integer.class);
        assertThat(cnt).isZero();
    }

    @Test
    void fk_it_category_exists() {
        Integer cnt = jdbc.queryForObject(
            "SELECT COUNT(*) FROM pg_constraint WHERE conname = 'fk_it_category'", Integer.class);
        assertThat(cnt).isEqualTo(1);
    }

    @Test
    void fk_icr_category_exists() {
        Integer cnt = jdbc.queryForObject(
            "SELECT COUNT(*) FROM pg_constraint WHERE conname = 'fk_icr_category'", Integer.class);
        assertThat(cnt).isEqualTo(1);
    }

    @Test
    void inspect_template_category_is_not_null() {
        String isNullable = jdbc.queryForObject(
            "SELECT is_nullable FROM information_schema.columns " +
            " WHERE table_schema='public' AND table_name='inspect_template' AND column_name='category'",
            String.class);
        assertThat(isNullable).isEqualTo("NO");
    }

    @Test
    void section_group_distribution_is_valid() {
        // CORE vs DETAIL 분포 확인 — V022 에서 설정된 section_group 값
        // (DETAIL section 은 category 가 현재 0개 — USAGE/ETC 접미사 섹션은 category 시드 아직 없음)
        // 본 테스트는 단순 smoke: 마스터 rows 는 CORE 섹션만 참조
        Integer nonCoreRefs = jdbc.queryForObject(
            "SELECT COUNT(*) FROM check_category_mst ccm " +
            " JOIN check_section_mst csm ON ccm.section_code = csm.section_code " +
            " WHERE csm.section_group <> 'CORE'", Integer.class);
        assertThat(nonCoreRefs).isZero();
    }
}
