package com.swmanager.system.service;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 설계내역서 엑셀 — {@code normalizeRounddownUnit} invalid 값 방어 테스트.
 *
 * 기획서 v6 FR-6''', §4-9 (4개 진입점 방어 중 "엑셀 생성" 진입점) /
 * 개발계획서 v6 §7-9 일부 (통합/API 까지 확장 전 단위 수준).
 */
class ExcelExportServiceNormalizeTest {

    private ExcelExportService service;
    private Logger serviceLogger;
    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    void setUp() {
        service = new ExcelExportService();
        serviceLogger = (Logger) LoggerFactory.getLogger(ExcelExportService.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        serviceLogger.addAppender(listAppender);
    }

    @AfterEach
    void tearDown() {
        serviceLogger.detachAppender(listAppender);
        listAppender.stop();
    }

    @Test
    void normalizeRounddownUnit_allowedValuesPassThrough() {
        assertThat(service.normalizeRounddownUnit(1, 1)).isEqualTo(1);
        assertThat(service.normalizeRounddownUnit(1000, 1)).isEqualTo(1000);
        assertThat(service.normalizeRounddownUnit(10000, 1)).isEqualTo(10000);
        // 허용값 입력이면 WARN 로그 없음
        assertThat(listAppender.list).noneMatch(e -> e.getLevel() == Level.WARN);
    }

    @Test
    void normalizeRounddownUnit_nullFallsBackTo1000_withoutWarn() {
        assertThat(service.normalizeRounddownUnit(null, 1)).isEqualTo(1000);
        // null 은 "필드 미존재 = 구 스키마" → 정상 케이스. WARN 남기지 않음.
        assertThat(listAppender.list).noneMatch(e -> e.getLevel() == Level.WARN);
    }

    @Test
    void normalizeRounddownUnit_invalidNumber500FallsBackAndLogsWarn() {
        assertThat(service.normalizeRounddownUnit(500, 42)).isEqualTo(1000);
        ILoggingEvent warn = listAppender.list.stream()
                .filter(e -> e.getLevel() == Level.WARN)
                .findFirst().orElseThrow();
        String msg = warn.getFormattedMessage();
        assertThat(msg).contains("design_estimate.rounddownUnit");
        assertThat(msg).contains("500");
        assertThat(msg).contains("docId=42");
    }

    @Test
    void normalizeRounddownUnit_nonNumericStringFallsBackAndLogsWarn() {
        assertThat(service.normalizeRounddownUnit("abc", 99)).isEqualTo(1000);
        ILoggingEvent warn = listAppender.list.stream()
                .filter(e -> e.getLevel() == Level.WARN)
                .findFirst().orElseThrow();
        assertThat(warn.getFormattedMessage())
                .contains("abc")
                .contains("docId=99");
    }

    @Test
    void normalizeRounddownUnit_numericStringAllowedValueAccepted() {
        // JSON 에서 문자열로 들어왔으나 숫자로 변환 가능 + 허용값
        assertThat(service.normalizeRounddownUnit("1000", 1)).isEqualTo(1000);
        assertThat(listAppender.list).noneMatch(e -> e.getLevel() == Level.WARN);
    }
}
