package com.swmanager.system.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** ExceptionMessages.safe 단위 테스트 (harden-error-message-nullsafe-sweep). */
class ExceptionMessagesTest {

    @Test
    void safe_withMessage_returnsMessage() {
        assertThat(ExceptionMessages.safe(new RuntimeException("boom"))).isEqualTo("boom");
    }

    @Test
    void safe_nullMessage_returnsSimpleName() {
        assertThat(ExceptionMessages.safe(new NullPointerException())).isEqualTo("NullPointerException");
    }

    @Test
    void safe_nullThrowable_returnsEmpty() {
        assertThat(ExceptionMessages.safe(null)).isEqualTo("");
    }
}
