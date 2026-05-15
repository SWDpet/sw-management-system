package com.swmanager.system.constant.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * S1 inspect-comprehensive-redesign — 점검 결과 코드.
 *
 * 정책:
 *  - DB 저장: Enum name (예: "NORMAL")
 *  - UI 표시: label ("정상")
 *  - label Freeze (기획서 NFR-7)
 *  - 정확히 4 values (FR-1)
 */
public enum InspectResultCode {
    NORMAL("정상"),
    PARTIAL("부분정상"),
    ABNORMAL("비정상"),
    NOT_APPLICABLE("해당없음");

    private final String label;

    InspectResultCode(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    /** JSON 역직렬화 — label 우선, 그 다음 Enum name, 둘 다 trim. */
    @JsonCreator
    public static InspectResultCode fromJson(String raw) {
        if (raw == null) return null;
        String norm = raw.trim();
        InspectResultCode v = fromKoLabel(norm);
        if (v != null) return v;
        try {
            return InspectResultCode.valueOf(norm);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /** 한글 label 매칭. */
    public static InspectResultCode fromKoLabel(String raw) {
        if (raw == null) return null;
        String norm = raw.trim();
        for (InspectResultCode v : values()) {
            if (v.label.equals(norm)) return v;
        }
        return null;
    }

    /**
     * inspection-qr-batch sprint: PoC payload status → result code.
     *   ok   → NORMAL
     *   warn → PARTIAL
     *   err  → ABNORMAL
     *   M    → NOT_APPLICABLE  (육안 점검 항목 — 자동수집 불가)
     *   그 외 → NORMAL (fallback, service 에서 remarks 에 표지)
     */
    public static InspectResultCode fromPoCStatus(String status) {
        if (status == null) return NORMAL;
        String norm = status.trim();
        return switch (norm.toLowerCase()) {
            case "ok" -> NORMAL;
            case "warn" -> PARTIAL;
            case "err", "error" -> ABNORMAL;
            case "m" -> NOT_APPLICABLE;
            default -> NORMAL;
        };
    }
}
