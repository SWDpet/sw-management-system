package com.swmanager.system.dto;

import com.swmanager.system.domain.workplan.WorkPlan;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 업무계획 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkPlanDTO {

    private Integer planId;
    private Long infraId;

    // 인프라 정보 (표시용)
    private String cityNm;
    private String distNm;
    private String sysNm;

    // 업무 정보
    private String planType;       // CONTRACT, INSTALL, PATCH, INSPECT, PRE_CONTACT, FAULT, SUPPORT, SETTLE, COMPLETE
    private Integer processStep;   // 1~7
    private String title;
    private String description;
    private Long assigneeId;
    private String assigneeName;   // 표시용
    private String startDate;      // yyyy-MM-dd
    private String endDate;
    private String location;
    private String repeatType;     // NONE, MONTHLY, QUARTERLY, HALF_YEARLY
    private Integer parentPlanId;
    private String status;         // PLANNED, CONTACTED, CONFIRMED, IN_PROGRESS, COMPLETED, POSTPONED, CANCELLED
    private String statusReason;

    // FullCalendar용 필드
    private String color;          // 유형별 색상

    /**
     * Entity → DTO 변환
     */
    public static WorkPlanDTO fromEntity(WorkPlan entity) {
        if (entity == null) return null;

        return WorkPlanDTO.builder()
                .planId(entity.getPlanId())
                .infraId(entity.getInfra() != null ? entity.getInfra().getInfraId() : null)
                .cityNm(entity.getInfra() != null ? entity.getInfra().getCityNm() : null)
                .distNm(entity.getInfra() != null ? entity.getInfra().getDistNm() : null)
                .sysNm(entity.getInfra() != null ? entity.getInfra().getSysNm() : null)
                .planType(entity.getPlanType())
                .processStep(entity.getProcessStep())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .assigneeId(entity.getAssignee() != null ? entity.getAssignee().getUserSeq() : null)
                .assigneeName(entity.getAssignee() != null ? entity.getAssignee().getUsername() : null)
                .startDate(entity.getStartDate() != null ? entity.getStartDate().toString() : null)
                .endDate(entity.getEndDate() != null ? entity.getEndDate().toString() : null)
                .location(entity.getLocation())
                .repeatType(entity.getRepeatType())
                .parentPlanId(entity.getParentPlan() != null ? entity.getParentPlan().getPlanId() : null)
                .status(entity.getStatus())
                .statusReason(entity.getStatusReason())
                .color(getTypeColor(entity.getPlanType()))
                .build();
    }

    /**
     * 업무유형별 캘린더 색상
     */
    public static String getTypeColor(String planType) {
        if (planType == null) return "#858796";
        switch (planType) {
            case "CONTRACT": return "#1565c0";    // 파랑
            case "INSTALL": return "#2e7d32";      // 초록
            case "PATCH": return "#00897b";         // 틸
            case "INSPECT": return "#ff9800";       // 주황
            case "PRE_CONTACT": return "#9e9e9e";   // 회색
            case "FAULT": return "#e74a3b";         // 빨강
            case "SUPPORT": return "#6a1b9a";       // 보라
            case "SETTLE": return "#5c6bc0";        // 인디고
            case "COMPLETE": return "#37474f";       // 다크
            case "ETC": return "#795548";            // 브라운
            default: return "#858796";
        }
    }

    /**
     * 업무유형 한글명
     */
    public static String getTypeLabel(String planType) {
        if (planType == null) return "";
        switch (planType) {
            case "CONTRACT": return "계약";
            case "INSTALL": return "설치";
            case "PATCH": return "패치";
            case "INSPECT": return "점검";
            case "PRE_CONTACT": return "사전연락";
            case "FAULT": return "장애처리";
            case "SUPPORT": return "업무지원";
            case "SETTLE": return "기성/준공";
            case "COMPLETE": return "준공";
            case "ETC": return "기타";
            default: return planType;
        }
    }

    /**
     * 상태 한글명
     */
    public static String getStatusLabel(String status) {
        if (status == null) return "";
        switch (status) {
            case "PLANNED": return "예정";
            case "CONTACTED": return "연락완료";
            case "CONFIRMED": return "확정";
            case "IN_PROGRESS": return "진행중";
            case "COMPLETED": return "완료";
            case "POSTPONED": return "연기";
            case "CANCELLED": return "취소";
            default: return status;
        }
    }

    /**
     * 프로세스 단계 한글명
     */
    public static String getStepLabel(Integer step) {
        if (step == null) return "";
        switch (step) {
            case 1: return "STEP1. 대상선정/예산제안";
            case 2: return "STEP2. 현장방문/설계전달";
            case 3: return "STEP3. 계약체결";
            case 4: return "STEP4. 착수계 제출";
            case 5: return "STEP5. 사업수행";
            case 6: return "STEP6. 기성/준공 제출";
            case 7: return "STEP7. 대금신청";
            default: return "STEP" + step;
        }
    }
}
