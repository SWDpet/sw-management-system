package com.swmanager.system.i18n;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MessageResolver Fallback 경로 단위 테스트.
 * 개발계획서 v2 §1 Step 5 / FR-B3 강화:
 *   (1) 키 누락 시 WARN 로그 정확히 1회 발생
 *   (2) 로그 메시지에 누락 키 이름 포함
 *   (3) 반환값은 "{missing:key}" 포맷
 */
class MessageResolverFallbackTest {

    private MessageResolver messages;
    private ListAppender<ILoggingEvent> listAppender;
    private Logger messagesLogger;

    @BeforeEach
    void setUp() {
        ReloadableResourceBundleMessageSource src = new ReloadableResourceBundleMessageSource();
        src.setBasename("classpath:messages");
        src.setDefaultEncoding("UTF-8");
        messages = new MessageResolver(src);

        messagesLogger = (Logger) LoggerFactory.getLogger("messages");
        listAppender = new ListAppender<>();
        listAppender.start();
        messagesLogger.addAppender(listAppender);
    }

    @AfterEach
    void tearDown() {
        messagesLogger.detachAppender(listAppender);
    }

    @Test
    void get_missingKey_returnsMissingPlaceholder() {
        String result = messages.get("__nonexistent_key__");
        assertThat(result).isEqualTo("{missing:__nonexistent_key__}");
    }

    @Test
    void get_missingKey_logsWarnExactlyOnce() {
        messages.get("__another_missing_key__");

        long warnCount = listAppender.list.stream()
                .filter(e -> e.getLevel() == Level.WARN)
                .count();
        assertThat(warnCount).isEqualTo(1L);
    }

    @Test
    void get_missingKey_logMessageContainsKeyName() {
        messages.get("error.deliberately_missing");

        ILoggingEvent warnEvent = listAppender.list.stream()
                .filter(e -> e.getLevel() == Level.WARN)
                .findFirst()
                .orElseThrow();
        assertThat(warnEvent.getFormattedMessage()).contains("error.deliberately_missing");
    }
}
