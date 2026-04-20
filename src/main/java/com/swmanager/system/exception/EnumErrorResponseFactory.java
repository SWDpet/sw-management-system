package com.swmanager.system.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Enum 바인딩 실패 예외 → 표준 400 응답 생성 유틸.
 * 기획서 §5-7-3 / §5-7-4.
 *
 * 응답 포맷:
 * {
 *   "timestamp": "...",
 *   "status": 400,
 *   "error": "BadRequest",
 *   "code": "ENUM_VALUE_NOT_ALLOWED",
 *   "field": "status",
 *   "enumType": "com.swmanager.system.constant.enums.DocumentStatus",
 *   "message": "허용되지 않는 값입니다.",
 *   "allowed": ["DRAFT","COMPLETED"],
 *   "path": "/document/api/inspect-report"
 * }
 */
public final class EnumErrorResponseFactory {

    private EnumErrorResponseFactory() {}

    public static ResponseEntity<Map<String, Object>> from(
            MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        Class<?> required = ex.getRequiredType();
        String field = ex.getName();
        return build(required, field, req);
    }

    public static ResponseEntity<Map<String, Object>> from(
            HttpMessageNotReadableException ex, HttpServletRequest req) {
        Class<?> required = null;
        String field = null;
        Throwable cause = ex.getCause();
        if (cause instanceof InvalidFormatException ife) {
            required = ife.getTargetType();
            if (!ife.getPath().isEmpty()) {
                field = ife.getPath().get(ife.getPath().size() - 1).getFieldName();
            }
        }
        return build(required, field, req);
    }

    private static ResponseEntity<Map<String, Object>> build(
            Class<?> required, String field, HttpServletRequest req) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "BadRequest");
        body.put("code", "ENUM_VALUE_NOT_ALLOWED");
        body.put("field", field != null ? field : "unknown");
        if (required != null && required.isEnum()) {
            body.put("enumType", required.getName());
            Object[] values = required.getEnumConstants();
            body.put("allowed", Arrays.stream(values).map(Object::toString).toList());
        } else {
            body.put("enumType", "UNKNOWN");
            // allowed 생략 (§5-7-4)
        }
        body.put("message", "허용되지 않는 값입니다.");
        body.put("path", req != null ? req.getRequestURI() : "");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
}
