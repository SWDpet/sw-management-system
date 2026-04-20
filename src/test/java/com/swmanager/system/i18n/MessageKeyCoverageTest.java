package com.swmanager.system.i18n;

import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * messages.properties 필수 키 커버리지 테스트.
 * 개발계획서 v2 §1 Step 5 / 개발계획서 §0-3 14개 키.
 *
 * 런타임 "{missing:key}" 노출을 방지하기 위해, 코드에서 사용하는 14개 키가
 * messages.properties에 반드시 존재하는지 컴파일 후 정적으로 검증.
 */
class MessageKeyCoverageTest {

    private static final List<String> REQUIRED_KEYS = List.of(
            "error.document.not_found",
            "error.document.type_empty",
            "error.document.type_unsupported",
            "error.attachment.not_found",
            "error.inspect_report.not_found",
            "error.project.not_found",
            "error.person.not_found",
            "error.user.not_found",
            "error.auth.account_locked",
            "error.export.design_data_empty",
            "error.export.performance_data_empty",
            "error.export.hwpx_generation",
            "error.export.pdf_conversion",
            "error.template.file_not_found"
    );

    @Test
    void allRequiredKeysPresent_ko() {
        MessageSource src = messageSource();
        for (String key : REQUIRED_KEYS) {
            assertThatCode(() -> src.getMessage(key, new Object[]{"X"}, Locale.KOREAN))
                    .as("필수 키 누락: %s", key)
                    .doesNotThrowAnyException();
        }
    }

    @Test
    void allRequiredKeysPresent_en() {
        MessageSource src = messageSource();
        for (String key : REQUIRED_KEYS) {
            String msg = src.getMessage(key, new Object[]{"X"}, Locale.ENGLISH);
            assertThat(msg).as("영문 번들 키 누락 또는 빈 값: %s", key).isNotEmpty();
        }
    }

    @Test
    void requiredKeyCount_is14() {
        assertThat(REQUIRED_KEYS).hasSize(14);
    }

    private MessageSource messageSource() {
        ReloadableResourceBundleMessageSource src = new ReloadableResourceBundleMessageSource();
        src.setBasename("classpath:messages");
        src.setDefaultEncoding("UTF-8");
        src.setFallbackToSystemLocale(false);
        return src;
    }
}
