package com.swmanager.system.service;

import com.swmanager.system.domain.Infra;
import com.swmanager.system.domain.User;
import com.swmanager.system.domain.workplan.WorkPlan;
import com.swmanager.system.dto.WorkPlanDTO;
import com.swmanager.system.repository.InfraRepository;
import com.swmanager.system.repository.UserRepository;
import com.swmanager.system.repository.workplan.WorkPlanRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class WorkPlanService {

    @Autowired private WorkPlanRepository workPlanRepository;
    @Autowired private InfraRepository infraRepository;
    @Autowired private UserRepository userRepository;

    /**
     * 캘린더 뷰: 기간 내 업무 조회 (FullCalendar용)
     */
    @Transactional(readOnly = true)
    public List<WorkPlanDTO> getWorkPlansByDateRange(LocalDate startDate, LocalDate endDate) {
        List<WorkPlan> plans = workPlanRepository.findByDateRange(startDate, endDate);
        return plans.stream()
                .map(WorkPlanDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 담당자별 캘린더 조회
     */
    @Transactional(readOnly = true)
    public List<WorkPlanDTO> getWorkPlansByAssigneeAndDateRange(Long assigneeId, LocalDate startDate, LocalDate endDate) {
        List<WorkPlan> plans = workPlanRepository.findByAssigneeAndDateRange(assigneeId, startDate, endDate);
        return plans.stream()
                .map(WorkPlanDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 인프라별 업무 목록
     */
    @Transactional(readOnly = true)
    public List<WorkPlanDTO> getWorkPlansByInfra(Long infraId) {
        List<WorkPlan> plans = workPlanRepository.findByInfra_InfraIdOrderByStartDateDesc(infraId);
        return plans.stream()
                .map(WorkPlanDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 프로세스 단계별 업무 조회 (칸반 보드용)
     */
    @Transactional(readOnly = true)
    public List<WorkPlanDTO> getWorkPlansByProcessStep(Integer processStep, List<String> excludeStatuses) {
        List<WorkPlan> plans = workPlanRepository.findByProcessStepAndStatusNotInOrderByStartDateAsc(processStep, excludeStatuses);
        return plans.stream()
                .map(WorkPlanDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 복합 검색 (목록 페이지용)
     */
    @Transactional(readOnly = true)
    public Page<WorkPlanDTO> searchWorkPlans(String type, String status, Long assigneeId, Long infraId, Pageable pageable) {
        if (type != null && type.trim().isEmpty()) type = null;
        if (status != null && status.trim().isEmpty()) status = null;

        Page<WorkPlan> page = workPlanRepository.searchWorkPlans(type, status, assigneeId, infraId, pageable);
        return page.map(WorkPlanDTO::fromEntity);
    }

    /**
     * 업무 상세 조회
     */
    @Transactional(readOnly = true)
    public WorkPlan getWorkPlanById(Integer planId) {
        return workPlanRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("업무계획 정보가 없습니다. ID: " + planId));
    }

    /**
     * 업무 상세 조회 (DTO)
     */
    @Transactional(readOnly = true)
    public WorkPlanDTO getWorkPlanDTOById(Integer planId) {
        return WorkPlanDTO.fromEntity(getWorkPlanById(planId));
    }

    /**
     * 업무계획 저장 (등록/수정)
     */
    public WorkPlan saveWorkPlan(WorkPlanDTO dto, User currentUser) {
        WorkPlan plan;

        if (dto.getPlanId() != null) {
            plan = workPlanRepository.findById(dto.getPlanId())
                    .orElseThrow(() -> new IllegalArgumentException("업무계획 정보가 없습니다."));
        } else {
            plan = new WorkPlan();
            plan.setCreatedBy(currentUser);
        }

        // 인프라 연동
        if (dto.getInfraId() != null) {
            Infra infra = infraRepository.findById(dto.getInfraId()).orElse(null);
            plan.setInfra(infra);
        } else {
            plan.setInfra(null);
        }

        // 담당자 연동
        if (dto.getAssigneeId() != null) {
            User assignee = userRepository.findById(dto.getAssigneeId()).orElse(null);
            plan.setAssignee(assignee);
        } else {
            plan.setAssignee(null);
        }

        // 부모 계획 연동
        if (dto.getParentPlanId() != null) {
            WorkPlan parent = workPlanRepository.findById(dto.getParentPlanId()).orElse(null);
            plan.setParentPlan(parent);
        } else {
            plan.setParentPlan(null);
        }

        // 기본 정보 매핑
        plan.setPlanType(dto.getPlanType());
        plan.setProcessStep(dto.getProcessStep());
        plan.setTitle(dto.getTitle());
        plan.setDescription(dto.getDescription());
        plan.setStartDate(parseDate(dto.getStartDate()));
        plan.setEndDate(parseDate(dto.getEndDate()));
        plan.setLocation(dto.getLocation());
        plan.setRepeatType(dto.getRepeatType() != null ? dto.getRepeatType() : "NONE");
        plan.setStatus(dto.getStatus() != null ? dto.getStatus() : "PLANNED");
        plan.setStatusReason(dto.getStatusReason());

        return workPlanRepository.save(plan);
    }

    /**
     * 업무계획 삭제
     */
    public void deleteWorkPlan(Integer planId) {
        workPlanRepository.deleteById(planId);
    }

    /**
     * 업무 상태 변경
     */
    public WorkPlan updateStatus(Integer planId, String newStatus, String reason) {
        WorkPlan plan = getWorkPlanById(planId);
        plan.setStatus(newStatus);
        plan.setStatusReason(reason);
        return workPlanRepository.save(plan);
    }

    /**
     * 사전연락 일정 조회 (알림용)
     */
    @Transactional(readOnly = true)
    public List<WorkPlan> getPreContactsByDate(LocalDate targetDate) {
        return workPlanRepository.findPreContactsByDate(targetDate);
    }

    /**
     * 자식 업무 조회
     */
    @Transactional(readOnly = true)
    public List<WorkPlanDTO> getChildPlans(Integer parentPlanId) {
        List<WorkPlan> children = workPlanRepository.findByParentPlan_PlanId(parentPlanId);
        return children.stream()
                .map(WorkPlanDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // === Utility ===

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        try {
            return LocalDate.parse(dateStr.trim());
        } catch (Exception e) {
            return null;
        }
    }
}
