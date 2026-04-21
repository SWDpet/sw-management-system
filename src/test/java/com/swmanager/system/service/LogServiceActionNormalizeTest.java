package com.swmanager.system.service;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.swmanager.system.constant.enums.AccessActionType;
import com.swmanager.system.constants.MenuName;
import com.swmanager.system.domain.AccessLog;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

/**
 * S9 access-log-action-and-menu-sync — LogService action_type 정규화·fail-soft 테스트.
 *
 * T7 — Deprecated 오버로드 동의어 매핑 → 정규 label 저장
 * T8 — Deprecated 오버로드 unknown → WARN + 원본 저장
 * T9 — Enum 오버로드 → label 그대로 저장
 */
@ExtendWith(MockitoExtension.class)
class LogServiceActionNormalizeTest {

    @Mock com.swmanager.system.repository.AccessLogRepository accessLogRepository;
    @Mock com.swmanager.system.repository.UserRepository userRepository;

    @InjectMocks LogService logService;

    private ListAppender<ILoggingEvent> appender;
    private Logger logServiceLogger;

    @BeforeEach
    void setUp() {
        logServiceLogger = (Logger) LoggerFactory.getLogger(LogService.class);
        appender = new ListAppender<>();
        appender.start();
        logServiceLogger.addAppender(appender);
        RequestContextHolder.setRequestAttributes(
                new ServletRequestAttributes(new MockHttpServletRequest()));
    }

    @AfterEach
    void tearDown() {
        logServiceLogger.detachAppender(appender);
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void deprecated_overload_maps_synonym_to_label() {
        logService.log(MenuName.QUOTATION, "목록조회", "샘플");

        ArgumentCaptor<AccessLog> cap = ArgumentCaptor.forClass(AccessLog.class);
        verify(accessLogRepository).save(cap.capture());
        // 동의어 → 정규 label("조회")
        assertThat(cap.getValue().getActionType()).isEqualTo("조회");
    }

    @Test
    void deprecated_overload_unknown_logs_warn_and_saves_raw() {
        String unknown = "완전없는값_xyz";
        logService.log(MenuName.QUOTATION, unknown, "샘플");

        // fail-soft: 원본 그대로 저장
        ArgumentCaptor<AccessLog> cap = ArgumentCaptor.forClass(AccessLog.class);
        verify(accessLogRepository).save(cap.capture());
        assertThat(cap.getValue().getActionType()).isEqualTo(unknown);

        // WARN 1건
        boolean warned = appender.list.stream()
                .anyMatch(e -> e.getLevel() == Level.WARN
                        && e.getFormattedMessage().contains("ACCESS_LOG_ACTION_UNKNOWN")
                        && e.getFormattedMessage().contains(unknown));
        assertThat(warned).isTrue();
    }

    @Test
    void enum_overload_saves_label() {
        logService.log(MenuName.DOCUMENT, AccessActionType.SIGN, "서명 저장");

        ArgumentCaptor<AccessLog> cap = ArgumentCaptor.forClass(AccessLog.class);
        verify(accessLogRepository).save(cap.capture());
        assertThat(cap.getValue().getActionType()).isEqualTo("서명");
        assertThat(cap.getValue().getMenuNm()).isEqualTo("문서관리");
    }
}
