package com.swmanager.system.constant.enums;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * S8-C qt-quotation-template-type-enum — QuoteTemplateType 단위 테스트.
 */
class QuoteTemplateTypeTest {

    private ListAppender<ILoggingEvent> appender;
    private Logger enumLogger;

    @BeforeEach
    void setUp() {
        enumLogger = (Logger) LoggerFactory.getLogger(QuoteTemplateType.class);
        appender = new ListAppender<>();
        appender.start();
        enumLogger.addAppender(appender);
    }

    @AfterEach
    void tearDown() {
        enumLogger.detachAppender(appender);
    }

    @Test
    void enum_has_exactly_2_values() {
        assertThat(QuoteTemplateType.values()).hasSize(2);
    }

    @Test
    void code_mapping_basic_1_and_labor_2() {
        assertThat(QuoteTemplateType.BASIC.getCode()).isEqualTo(1);
        assertThat(QuoteTemplateType.LABOR_COST_INTEGRATED.getCode()).isEqualTo(2);
    }

    @Test
    void label_is_frozen() {
        assertThat(QuoteTemplateType.BASIC.getLabel()).isEqualTo("기본양식");
        assertThat(QuoteTemplateType.LABOR_COST_INTEGRATED.getLabel()).isEqualTo("인건비 통합양식");
    }

    @Test
    void fromCode_null_returns_basic_without_warn() {
        assertThat(QuoteTemplateType.fromCode(null)).isEqualTo(QuoteTemplateType.BASIC);
        // null 은 정상 기본값 경로 — WARN 발생하지 않아야 함
        boolean warned = appender.list.stream().anyMatch(e -> e.getLevel() == Level.WARN);
        assertThat(warned).isFalse();
    }

    @Test
    void fromCode_known_values() {
        assertThat(QuoteTemplateType.fromCode(1)).isEqualTo(QuoteTemplateType.BASIC);
        assertThat(QuoteTemplateType.fromCode(2)).isEqualTo(QuoteTemplateType.LABOR_COST_INTEGRATED);
    }

    @Test
    void fromCode_unknown_returns_basic_with_warn() {
        QuoteTemplateType v = QuoteTemplateType.fromCode(99);
        assertThat(v).isEqualTo(QuoteTemplateType.BASIC);
        // WARN 로그 발생 확인 (이상값 은닉 방지)
        boolean warned = appender.list.stream()
                .anyMatch(e -> e.getLevel() == Level.WARN
                        && e.getFormattedMessage().contains("QT_TEMPLATE_TYPE_UNKNOWN")
                        && e.getFormattedMessage().contains("99"));
        assertThat(warned).isTrue();
    }

    @Test
    void jsonValue_is_int_code() throws Exception {
        ObjectMapper m = new ObjectMapper();
        assertThat(m.writeValueAsString(QuoteTemplateType.BASIC)).isEqualTo("1");
        assertThat(m.writeValueAsString(QuoteTemplateType.LABOR_COST_INTEGRATED)).isEqualTo("2");
    }
}
