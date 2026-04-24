package com.swmanager.system.constant.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Collections;
import java.util.Map;

/**
 * 문서 상태 Enum — tb_document.status, inspect_report.status 공용.
 *
 * 기획서: docs/product-specs/hardcoding-improvement.md (v6)
 * 매핑 정책 (§5-6-1): @Enumerated(EnumType.STRING) 기본 선택.
 *   Pre-flight 결과 공백·별칭 이슈 0건이라 AttributeConverter 불필요.
 * 별칭 정책 (§5-7-2): JSON body 전용. ALIASES 맵은 Enum 내부 단일 소스.
 *   ConverterFactory(query/path)는 ALIASES 조회 금지 (대소문자 무시만).
 *   v6 기준 별칭 없음 — 빈 Map으로 시작.
 */
public enum DocumentStatus {
    DRAFT("작성중", "#858796"),
    COMPLETED("작성완료", "#1cc88a");

    private final String label;
    private final String color;

    /** 별칭 단일 소스 (JSON body 전용). 레거시·한글값은 여기 넣지 않는다 (데이터 정제 대상). */
    private static final Map<String, DocumentStatus> ALIASES = Collections.emptyMap();

    DocumentStatus(String label, String color) {
        this.label = label;
        this.color = color;
    }

    public String label() { return label; }

    public String color() { return color; }

    /**
     * JSON body 역직렬화 전용. (기획서 §5-7 구현 예시)
     * null/빈값 → null, trim+upper 후 valueOf, 실패 시 ALIASES 조회, 전부 실패 시 IllegalArgumentException.
     */
    @JsonCreator
    public static DocumentStatus fromString(String v) {
        if (v == null || v.isBlank()) return null;
        String norm = v.trim().toUpperCase();
        try {
            return DocumentStatus.valueOf(norm);
        } catch (IllegalArgumentException ignore) {
            DocumentStatus aliased = ALIASES.get(v.trim());
            if (aliased != null) return aliased;
            throw new IllegalArgumentException("ENUM_VALUE_NOT_ALLOWED: " + v);
        }
    }
}
