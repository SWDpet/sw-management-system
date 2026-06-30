package com.swmanager.system.dto;

import com.swmanager.system.constant.enums.WorkPlanStatus;
import com.swmanager.system.constant.enums.WorkPlanType;
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
    private Long projId;     // 대상 사업(sw_pjt) — 계약 대상 식별
    private String projNm;   // 사업명(표시용)
    private Long infraId;    // 레거시(기존 행)

    // 인프라/사업 정보 (표시용)
    private String cityNm;
    private String distNm;
    private String sysNm;

    // [workplan-target-infra-cascade] 대상 지역+시스템 (계약=Infra 복사 / 미계약=직접입력)
    private String regionCode;
    private String regionCityNm;
    private String regionDistNm;
    private String targetSysNm;

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

    private Long createdById;       // [owner-edit-guard] 작성자 userSeq (소유권 UI 판정용, assignee 와 별개)

    /**
     * Entity → DTO 변환
     */
    public static WorkPlanDTO fromEntity(WorkPlan entity) {
        if (entity == null) return null;

        return WorkPlanDTO.builder()
                .planId(entity.getPlanId())
                .projId(entity.getProject() != null ? entity.getProject().getProjId() : null)
                .projNm(entity.getProject() != null ? entity.getProject().getProjNm() : null)
                .infraId(entity.getInfra() != null ? entity.getInfra().getInfraId() : null)
                .cityNm(entity.getInfra() != null ? entity.getInfra().getCityNm() : null)
                .distNm(entity.getInfra() != null ? entity.getInfra().getDistNm() : null)
                .sysNm(entity.getInfra() != null ? entity.getInfra().getSysNm() : null)
                .regionCode(entity.getRegionCode())
                .regionCityNm(entity.getRegionCityNm())
                .regionDistNm(entity.getRegionDistNm())
                .targetSysNm(entity.getTargetSysNm())
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
                .createdById(entity.getCreatedBy() != null ? entity.getCreatedBy().getUserSeq() : null)
                .build();
    }

    /**
     * [workplan-target-infra-cascade FR-10] 대상 표시 라벨 (시군구 - 시스템).
     *  계약 대상은 infra 의 cityNm/distNm/sysNm, 미계약은 region 필드/target_sys_nm.
     *  region 값이 채워졌으면 우선(저장 시 계약도 채움), 없으면 infra 값으로 fallback (기존 행 호환).
     *  목록/캘린더/상세 모두 본 메서드 단일 사용.
     */
    public String getTargetLabel() {
        String city = firstNonBlank(regionCityNm, cityNm);
        String dist = firstNonBlank(regionDistNm, distNm);
        String sys  = firstNonBlank(targetSysNm, sysNm);
        String area = (dist != null) ? dist : city; // 시군구 우선, 없으면 시도
        if (area == null && sys == null) return "";
        if (area == null) return sys;
        if (sys == null) return area;
        return area + " - " + sys;
    }

    private static String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) return a;
        if (b != null && !b.isBlank()) return b;
        return null;
    }

    /**
     * S16 tb-work-plan-decision (2026-04-22): switch 제거 + Enum 위임.
     *  - 업무유형별 캘린더 색상 → WorkPlanType.colorOf(code)
     */
    public static String getTypeColor(String planType) {
        return WorkPlanType.colorOf(planType);
    }

    /**
     * 업무유형 한글명 → WorkPlanType.labelOf(code)
     */
    public static String getTypeLabel(String planType) {
        return WorkPlanType.labelOf(planType);
    }

    /**
     * 상태 한글명 → WorkPlanStatus.labelOf(code)
     */
    public static String getStatusLabel(String status) {
        return WorkPlanStatus.labelOf(status);
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
