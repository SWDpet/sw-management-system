package com.swmanager.system.constant.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * S8 qt-quotation-domain-normalize — 견적서 분류.
 *
 * 정책:
 *  - DB 저장: 한글 label (유지보수/용역/제품)
 *  - qt_category_mst FK 제약 + 본 Enum 상수 동시 관리
 *  - label Freeze (기획서 NFR-4/NFR-5)
 *  - 정확히 3 values (FR-1)
 */
public enum QtCategory {
    MAINTENANCE("유지보수"),
    SERVICE("용역"),
    PRODUCT("제품");

    private final String label;

    QtCategory(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    /** JSON 역직렬화: label 우선, Enum name 차순위, 둘 다 trim */
    @JsonCreator
    public static QtCategory fromJson(String raw) {
        if (raw == null) return null;
        String norm = raw.trim();
        QtCategory v = fromKoLabel(norm);
        if (v != null) return v;
        try {
            return QtCategory.valueOf(norm);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /** 한글 label 매칭 (trim 포함) */
    public static QtCategory fromKoLabel(String raw) {
        if (raw == null) return null;
        String norm = raw.trim();
        for (QtCategory v : values()) {
            if (v.label.equals(norm)) return v;
        }
        return null;
    }
}
