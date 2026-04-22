package com.swmanager.system.constant.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * S16 tb-work-plan-decision — 업무계획 상태.
 *
 * 정책:
 *  - DB 저장: 영문 코드 — work_plan_status_mst.status_code FK
 *  - API 직렬화: 한글 label (@JsonValue)
 *  - 정확히 7 values + label Freeze
 */
public enum WorkPlanStatus {
    PLANNED("예정"),
    CONTACTED("연락완료"),
    CONFIRMED("확정"),
    IN_PROGRESS("진행중"),
    COMPLETED("완료"),
    POSTPONED("연기"),
    CANCELLED("취소");

    private final String label;

    WorkPlanStatus(String label) { this.label = label; }

    @JsonValue
    public String getLabel() { return label; }

    @JsonCreator
    public static WorkPlanStatus fromJson(String raw) {
        if (raw == null) return null;
        String norm = raw.trim();
        WorkPlanStatus v = fromKoLabel(norm);
        if (v != null) return v;
        try { return WorkPlanStatus.valueOf(norm); }
        catch (IllegalArgumentException e) { return null; }
    }

    public static WorkPlanStatus fromKoLabel(String raw) {
        if (raw == null) return null;
        String norm = raw.trim();
        for (WorkPlanStatus v : values()) if (v.label.equals(norm)) return v;
        return null;
    }

    /** DTO 위임용: code → label (null-safe) */
    public static String labelOf(String code) {
        if (code == null) return "";
        try { return valueOf(code).label; }
        catch (IllegalArgumentException e) { return code; }
    }
}
