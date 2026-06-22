package com.swmanager.system.repository;

import com.swmanager.system.constants.MenuName;
import com.swmanager.system.domain.AccessLog;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 통합검증: 대시보드/로그관리 네이티브 통계쿼리가 실 Postgres 스키마에서 무오류 실행되고,
 * MainController.buildDashboardModel 이 의존하는 투영(projection) 계약을 만족하는지 고정한다.
 *
 * log-management-improvement P4 잔여(회사 PC 통합검증): @DataJpaTest 네이티브 통계쿼리(실 Postgres 필요).
 * 단위테스트(LogControllerTest/DashboardDtoTest)는 mock 기반이라 SQL·스키마 정합을 못 잡음 → 본 테스트가 그 갭을 닫는다.
 *
 * 읽기전용(SELECT) 전용 → 운영DB에 안전. RUN_DB_TESTS=true 환경에서만 실행.
 */
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "RUN_DB_TESTS", matches = "true",
        disabledReason = "Live DB required; set RUN_DB_TESTS=true to run. 기본 CI에서는 스킵.")
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=none",
        "logging.level.org.hibernate.SQL=OFF"
})
class AccessLogStatsQueryIntegrationTest {

    @Autowired
    private AccessLogRepository accessLogRepository;

    /**
     * 일자별 추이(#2): 무오류 실행 + 투영 [date, Number act, Number visitors] +
     * 컨트롤러 소비계약(r[1]/r[2] 가 Number) + 날짜 오름차순.
     */
    @Test
    void dailyTrend30d_executesAndMatchesProjectionContract() {
        List<Object[]> rows = accessLogRepository.findDailyTrend30d();
        assertThat(rows).isNotNull();

        java.sql.Date prev = null;
        for (Object[] r : rows) {
            assertThat(r).hasSize(3);
            // r[0] = CAST(access_time AS date) → java.sql.Date (null 아님: GROUP BY 키)
            assertThat(r[0]).isInstanceOf(java.sql.Date.class);
            // MainController: ((Number) r[1]).longValue()
            assertThat(r[1]).isInstanceOf(Number.class);
            assertThat(((Number) r[1]).longValue()).isGreaterThanOrEqualTo(0L);
            // visitors 는 null 가능(로그인 외 일자) → 컨트롤러도 null 허용
            if (r[2] != null) {
                assertThat(r[2]).isInstanceOf(Number.class);
            }
            // ORDER BY d ASC 불변식
            java.sql.Date d = (java.sql.Date) r[0];
            if (prev != null) {
                assertThat(d).isAfterOrEqualTo(prev);
            }
            prev = d;
        }
    }

    /**
     * 메뉴 TOP(#3): 무오류 실행 + 최대 6건 + [menu_nm, Number] + count 내림차순.
     */
    @Test
    void menuTop30d_executesAndIsBoundedAndDescending() {
        List<Object[]> rows = accessLogRepository.findMenuTop30d();
        assertThat(rows).isNotNull();
        assertThat(rows.size()).isLessThanOrEqualTo(6);

        long prev = Long.MAX_VALUE;
        for (Object[] r : rows) {
            assertThat(r).hasSize(2);
            assertThat(r[0]).isInstanceOf(String.class);   // menu_nm IS NOT NULL 필터
            assertThat(r[1]).isInstanceOf(Number.class);
            long c = ((Number) r[1]).longValue();
            assertThat(c).isGreaterThanOrEqualTo(0L);
            assertThat(c).isLessThanOrEqualTo(prev);        // ORDER BY c DESC
            prev = c;
        }
    }

    /**
     * 액션별 건수(#4): 무오류 실행 + [action_type, Number] + count 내림차순.
     */
    @Test
    void actionCounts30d_executesAndIsDescending() {
        List<Object[]> rows = accessLogRepository.findActionCounts30d();
        assertThat(rows).isNotNull();

        long prev = Long.MAX_VALUE;
        for (Object[] r : rows) {
            assertThat(r).hasSize(2);
            assertThat(r[0]).isInstanceOf(String.class);   // action_type IS NOT NULL 필터
            assertThat(r[1]).isInstanceOf(Number.class);
            long c = ((Number) r[1]).longValue();
            assertThat(c).isLessThanOrEqualTo(prev);
            prev = c;
        }
    }

    /**
     * 최근 사업 변경이력(#1): 파생 쿼리 무오류 실행 + 최대 6건 + 시각 내림차순 +
     * 필터(menu_nm='사업관리' AND action_type ∈ {등록,수정,삭제}) 준수.
     */
    @Test
    void recentProjectLogs_executesBoundedAndFiltered() {
        List<AccessLog> rows = accessLogRepository
                .findTop6ByMenuNmAndActionTypeInOrderByAccessTimeDesc(
                        MenuName.PROJECT, List.of("등록", "수정", "삭제"));
        assertThat(rows).isNotNull();
        assertThat(rows.size()).isLessThanOrEqualTo(6);

        java.time.LocalDateTime prev = null;
        for (AccessLog l : rows) {
            assertThat(l.getMenuNm()).isEqualTo(MenuName.PROJECT);
            assertThat(l.getActionType()).isIn("등록", "수정", "삭제");
            if (prev != null && l.getAccessTime() != null) {
                assertThat(l.getAccessTime()).isBeforeOrEqualTo(prev);   // 내림차순
            }
            if (l.getAccessTime() != null) prev = l.getAccessTime();
        }
    }
}
