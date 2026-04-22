package com.swmanager.system.constant.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * S16 tb-work-plan-decision — 업무계획 유형.
 *
 * 정책:
 *  - DB 저장: 영문 코드 (CONTRACT/INSTALL/...) — work_plan_type_mst.type_code FK
 *  - API 직렬화: 한글 label (@JsonValue) — UI·리포트 호환
 *  - label Freeze (기획서 NFR-8 / Phase 4 seed 정합 게이트)
 *  - 정확히 10 values
 */
public enum WorkPlanType {
    CONTRACT("계약", "#1565c0"),
    INSTALL("설치", "#2e7d32"),
    PATCH("패치", "#00897b"),
    INSPECT("점검", "#ff9800"),
    PRE_CONTACT("사전연락", "#9e9e9e"),
    FAULT("장애처리", "#e74a3b"),
    SUPPORT("업무지원", "#6a1b9a"),
    SETTLE("기성/준공", "#5c6bc0"),
    COMPLETE("준공", "#37474f"),
    ETC("기타", "#795548");

    private final String label;
    private final String color;

    WorkPlanType(String label, String color) {
        this.label = label;
        this.color = color;
    }

    @JsonValue
    public String getLabel() { return label; }

    public String getColor() { return color; }

    @JsonCreator
    public static WorkPlanType fromJson(String raw) {
        if (raw == null) return null;
        String norm = raw.trim();
        WorkPlanType v = fromKoLabel(norm);
        if (v != null) return v;
        try { return WorkPlanType.valueOf(norm); }
        catch (IllegalArgumentException e) { return null; }
    }

    public static WorkPlanType fromKoLabel(String raw) {
        if (raw == null) return null;
        String norm = raw.trim();
        for (WorkPlanType v : values()) if (v.label.equals(norm)) return v;
        return null;
    }

    /** DTO 위임용: code → color (null-safe) */
    public static String colorOf(String code) {
        if (code == null) return "#858796";
        try { return valueOf(code).color; }
        catch (IllegalArgumentException e) { return "#858796"; }
    }

    /** DTO 위임용: code → label (null-safe) */
    public static String labelOf(String code) {
        if (code == null) return "";
        try { return valueOf(code).label; }
        catch (IllegalArgumentException e) { return code; }
    }
}
