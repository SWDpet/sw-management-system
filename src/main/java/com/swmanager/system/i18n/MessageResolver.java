package com.swmanager.system.i18n;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

/**
 * 메시지 리소스 조회 컴포넌트 (하드코딩 개선 스프린트 #1-B).
 *
 * 기획서 §5-8 키 네이밍 규칙: `{계층}.{도메인}.{상황}` (예: `error.document.not_found`).
 * FR-B3 Fallback 정책: 키 누락 시 `"{missing:key}"` 반환 + WARN 로그 1회.
 *
 * 사용 패턴:
 * <pre>{@code
 * @Service
 * @RequiredArgsConstructor
 * public class DocumentService {
 *     private final MessageResolver messages;
 *     public Document get(Long id) {
 *         return repo.findById(id)
 *             .orElseThrow(() -> new BusinessException(
 *                 ErrorCode.NOT_FOUND,
 *                 messages.get("error.document.not_found", id)));
 *     }
 * }
 * }</pre>
 */
@Component
@RequiredArgsConstructor
public class MessageResolver {

    private static final Logger log = LoggerFactory.getLogger("messages");

    private final MessageSource messageSource;

    /**
     * 메시지 조회. 키 누락 시 `"{missing:key}"` 반환 + WARN 로그 1회 (FR-B3).
     * 매개변수 치환은 Java {@link java.text.MessageFormat} 규칙 ({0}, {1} ...).
     */
    public String get(String key, Object... args) {
        try {
            return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
        } catch (NoSuchMessageException e) {
            log.warn("Missing message key: {}", key);
            return "{missing:" + key + "}";
        }
    }
}
