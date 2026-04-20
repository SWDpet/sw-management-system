package com.swmanager.system.i18n;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MessageResolver 정상 경로 단위 테스트.
 * 개발계획서 v2 §1 Step 5.
 */
class MessageResolverTest {

    private MessageResolver messages;

    @BeforeEach
    void setUp() {
        ReloadableResourceBundleMessageSource src = new ReloadableResourceBundleMessageSource();
        src.setBasename("classpath:messages");
        src.setDefaultEncoding("UTF-8");
        messages = new MessageResolver(src);
    }

    @Test
    void get_knownKey_returnsKoreanMessage() {
        String msg = messages.get("error.document.not_found", 123L);
        assertThat(msg).isEqualTo("문서를 찾을 수 없습니다. ID: 123");
    }

    @Test
    void get_messageWithNoArgs_returnsKoreanMessage() {
        String msg = messages.get("error.attachment.not_found");
        assertThat(msg).isEqualTo("첨부파일을 찾을 수 없습니다.");
    }

    @Test
    void get_messageWithMultipleArgs_formatsCorrectly() {
        String msg = messages.get("error.auth.account_locked", 10);
        assertThat(msg).contains("계정이 잠겼습니다")
                       .contains("10분 후");
    }

    @Test
    void get_hwpxGenerationError_formatsException() {
        String msg = messages.get("error.export.hwpx_generation", "NullPointerException");
        assertThat(msg).isEqualTo("HWPX 생성 중 오류: NullPointerException");
    }
}
