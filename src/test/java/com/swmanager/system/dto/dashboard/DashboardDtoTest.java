package com.swmanager.system.dto.dashboard;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 대시보드 DTO getter 테스트 (dashboard-typed-model §6-4).
 *
 * Thymeleaf 는 {@code ${r.h}/${m.w}/${m.dday}} 처럼 getter(getH/getW/getDday)를 통해 속성접근하므로,
 * Lombok @Getter 가 템플릿이 기대하는 정확한 getter 명·값을 생성하는지 고정한다(백지 렌더 회귀 가드).
 */
class DashboardDtoTest {

    @Test
    void yearBar_getters() {
        DashYearBar b = new DashYearBar(2026, 5L, 80);
        assertThat(b.getY()).isEqualTo(2026);
        assertThat(b.getC()).isEqualTo(5L);
        assertThat(b.getH()).isEqualTo(80);
    }

    @Test
    void logBar_getters() {
        DashLogBar b = new DashLogBar("2026-06-22", 12L, 3L, 55);
        assertThat(b.getDate()).isEqualTo("2026-06-22");
        assertThat(b.getAct()).isEqualTo(12L);
        assertThat(b.getVisitors()).isEqualTo(3L);
        assertThat(b.getH()).isEqualTo(55);
    }

    @Test
    void menuBar_actionBar_getters() {
        DashMenuBar m = new DashMenuBar("사업관리", 30L, 100);
        assertThat(m.getMenu()).isEqualTo("사업관리");
        assertThat(m.getCnt()).isEqualTo(30L);
        assertThat(m.getW()).isEqualTo(100);

        DashActionBar a = new DashActionBar("조회", 42L, 70);
        assertThat(a.getAction()).isEqualTo("조회");
        assertThat(a.getCnt()).isEqualTo(42L);
        assertThat(a.getW()).isEqualTo(70);
    }

    @Test
    void upcoming_expiring_getters() {
        DashUpcoming u = new DashUpcoming("UPIS 점검", LocalDate.of(2026, 6, 25), 3L);
        assertThat(u.getTitle()).isEqualTo("UPIS 점검");
        assertThat(u.getDate()).isEqualTo(LocalDate.of(2026, 6, 25));
        assertThat(u.getDday()).isEqualTo(3L);

        DashExpiring e = new DashExpiring("강릉시청", "GSS", 7L);
        assertThat(e.getName()).isEqualTo("강릉시청");
        assertThat(e.getType()).isEqualTo("GSS");
        assertThat(e.getDays()).isEqualTo(7L);
    }
}
