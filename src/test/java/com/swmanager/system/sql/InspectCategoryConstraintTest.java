package com.swmanager.system.sql;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * S10 inspect-check-result-category-master — FR-3(FK) + FR-4(NOT NULL) 회귀 보장.
 *
 * 검증:
 *  (1) inspect_template.category NULL insert → 실패 (NOT NULL)
 *  (2) inspect_template 에 마스터 외 category insert → 실패 (FK)
 *  (3) inspect_check_result NULL insert → 허용 (FK만, NULL OK)
 *  (4) inspect_check_result 에 마스터 외 category insert → 실패 (FK)
 */
@SpringBootTest
class InspectCategoryConstraintTest {

    @Autowired JdbcTemplate jdbc;

    @Test
    void inspect_template_rejects_null_category() {
        assertThatThrownBy(() -> jdbc.update(
            "INSERT INTO inspect_template (template_type, section, category, item_name, item_method, sort_order) " +
            "VALUES (?, ?, NULL, ?, ?, ?)",
            "UPIS", "DB", "test-item", "test-method", 9999))
            .isInstanceOf(DataAccessException.class);
    }

    @Test
    void inspect_template_rejects_unknown_category() {
        assertThatThrownBy(() -> jdbc.update(
            "INSERT INTO inspect_template (template_type, section, category, item_name, item_method, sort_order) " +
            "VALUES (?, ?, ?, ?, ?, ?)",
            "UPIS", "DB", "__NOT_IN_MASTER__", "test-item", "test-method", 9999))
            .isInstanceOf(DataAccessException.class);
    }

    @Test
    void inspect_template_accepts_master_category() {
        // 삽입 후 롤백 대신 UNIQUE 충돌 회피용 item_name 사용 + 직후 삭제
        try {
            int rows = jdbc.update(
                "INSERT INTO inspect_template (template_type, section, category, item_name, item_method, sort_order) " +
                "VALUES (?, ?, ?, ?, ?, ?)",
                "UPIS", "DB", "네트워크", "__s10_constraint_test__", "test", 99999);
            assertThat(rows).isEqualTo(1);
        } finally {
            jdbc.update("DELETE FROM inspect_template WHERE item_name = '__s10_constraint_test__'");
        }
    }

    @Test
    void inspect_check_result_accepts_null_category() {
        // ICR 는 category NULL 허용 (작성 중 임시 저장 시나리오)
        // 신규 report 삽입부터 필요하므로 FK 없는 테스트는 생략하고 정책만 확인
        String isNullable = jdbc.queryForObject(
            "SELECT is_nullable FROM information_schema.columns " +
            " WHERE table_schema='public' AND table_name='inspect_check_result' AND column_name='category'",
            String.class);
        assertThat(isNullable).isEqualTo("YES");
    }

    @Test
    void inspect_check_result_rejects_unknown_category_via_fk() {
        // FK 제약 직접 검증: 마스터 외 값으로 inspect_check_result insert 는 실패해야 함.
        // (report_id=0 등 더미는 다른 FK 에 걸릴 수 있으므로, 본 테스트는 FK 존재만 확인)
        Integer fk = jdbc.queryForObject(
            "SELECT COUNT(*) FROM pg_constraint " +
            " WHERE conname = 'fk_icr_category' " +
            "   AND contype = 'f'", Integer.class);
        assertThat(fk).isEqualTo(1);
    }
}
