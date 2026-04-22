package com.swmanager.system.arch;

import com.swmanager.system.constant.enums.WorkPlanStatus;
import com.swmanager.system.constant.enums.WorkPlanType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * S16 tb-work-plan-decision — NFR-8: Enum ↔ 마스터 시드 drift 게이트.
 *
 * Enum values 와 DB 마스터 row 를 name/label/color 까지 전수 비교.
 * drift 발생 시 본 테스트 실패로 빌드 중단.
 */
@SpringBootTest
class WorkPlanMstEnumSyncTest {

    @Autowired JdbcTemplate jdbc;

    @Test
    void work_plan_type_enum_and_mst_are_in_sync() {
        List<Map<String, Object>> rows = jdbc.queryForList(
            "SELECT type_code, type_label, color FROM work_plan_type_mst ORDER BY type_code");
        assertThat(rows).hasSameSizeAs(WorkPlanType.values());

        for (WorkPlanType t : WorkPlanType.values()) {
            Map<String, Object> match = rows.stream()
                .filter(r -> t.name().equals(r.get("type_code")))
                .findFirst().orElseThrow(() ->
                    new AssertionError("Enum " + t.name() + " 가 마스터에 없음"));
            assertThat(match.get("type_label")).as("label drift: " + t.name())
                .isEqualTo(t.getLabel());
            assertThat(match.get("color")).as("color drift: " + t.name())
                .isEqualTo(t.getColor());
        }
    }

    @Test
    void work_plan_status_enum_and_mst_are_in_sync() {
        List<Map<String, Object>> rows = jdbc.queryForList(
            "SELECT status_code, status_label FROM work_plan_status_mst ORDER BY status_code");
        assertThat(rows).hasSameSizeAs(WorkPlanStatus.values());

        for (WorkPlanStatus s : WorkPlanStatus.values()) {
            Map<String, Object> match = rows.stream()
                .filter(r -> s.name().equals(r.get("status_code")))
                .findFirst().orElseThrow(() ->
                    new AssertionError("Enum " + s.name() + " 가 마스터에 없음"));
            assertThat(match.get("status_label")).as("label drift: " + s.name())
                .isEqualTo(s.getLabel());
        }
    }

    @Test
    void no_orphan_mst_rows_not_in_enum() {
        // 마스터에 있지만 Enum 에 없는 코드는 없어야 함 (역방향 drift)
        List<String> typeCodes = jdbc.queryForList("SELECT type_code FROM work_plan_type_mst", String.class);
        List<String> enumNames = Arrays.stream(WorkPlanType.values()).map(Enum::name).toList();
        assertThat(typeCodes).allMatch(enumNames::contains);

        List<String> statusCodes = jdbc.queryForList("SELECT status_code FROM work_plan_status_mst", String.class);
        List<String> enumStatusNames = Arrays.stream(WorkPlanStatus.values()).map(Enum::name).toList();
        assertThat(statusCodes).allMatch(enumStatusNames::contains);
    }
}
