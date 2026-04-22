package com.swmanager.system.constant.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * S8-C qt-quotation-template-type-enum — 견적서 출력 양식.
 *
 * 정책:
 *  - DB 저장: Integer code (1=BASIC, 2=LABOR_COST_INTEGRATED)
 *  - API 직렬화: @JsonValue = int code (기존 JSON 계약 호환)
 *  - fromCode(null | unknown) → BASIC fallback + WARN 로그 (이상값 은닉 방지)
 *  - 정확히 2 values
 */
public enum QuoteTemplateType {
    BASIC(1, "기본양식"),
    LABOR_COST_INTEGRATED(2, "인건비 통합양식");

    private static final Logger log = LoggerFactory.getLogger(QuoteTemplateType.class);

    private final int code;
    private final String label;

    QuoteTemplateType(int code, String label) {
        this.code = code;
        this.label = label;
    }

    @JsonValue
    public int getCode() { return code; }

    public String getLabel() { return label; }

    /** Integer code → Enum (null / unknown → BASIC fallback + WARN) */
    public static QuoteTemplateType fromCode(Integer code) {
        if (code == null) return BASIC;
        for (QuoteTemplateType t : values()) {
            if (t.code == code) return t;
        }
        log.warn("QT_TEMPLATE_TYPE_UNKNOWN: code={} → BASIC 로 fallback", code);
        return BASIC;
    }
}
