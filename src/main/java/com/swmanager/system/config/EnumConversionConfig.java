package com.swmanager.system.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.format.FormatterRegistry;
import org.springframework.lang.NonNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC 경로 변수·쿼리 파라미터 → Enum 역직렬화 정책.
 *
 * 기획서 §5-7-1 / §5-7-2: ConverterFactory는 ALIASES 맵을 조회하지 않는다.
 *   trim() + toUpperCase() 후 Enum.valueOf 만 시도. 실패 시 IllegalArgumentException
 *   이 Spring 의 {@link org.springframework.web.method.annotation.MethodArgumentTypeMismatchException} 로 래핑되어
 *   GlobalExceptionHandler 의 Enum 응답 핸들러가 처리한다.
 */
@Configuration
public class EnumConversionConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(@NonNull FormatterRegistry registry) {
        registry.addConverterFactory(new StringToEnumConverterFactory());
    }

    /** Generic String→Enum 변환기. 모든 Enum 타입에 일괄 적용. */
    private static final class StringToEnumConverterFactory
            implements ConverterFactory<String, Enum<?>> {

        @Override
        @NonNull
        public <T extends Enum<?>> Converter<String, T> getConverter(@NonNull Class<T> targetType) {
            return new StringToEnum<>(targetType);
        }

        private static final class StringToEnum<T extends Enum<?>> implements Converter<String, T> {
            private final Class<T> enumType;

            StringToEnum(Class<T> enumType) { this.enumType = enumType; }

            @Override
            @SuppressWarnings({"unchecked", "rawtypes"})
            public T convert(@NonNull String source) {
                if (source.isBlank()) return null;
                String norm = source.trim().toUpperCase();
                return (T) Enum.valueOf((Class) enumType, norm);
            }
        }
    }
}
