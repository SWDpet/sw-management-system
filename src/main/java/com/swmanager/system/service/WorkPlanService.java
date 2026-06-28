package com.swmanager.system.service;

import com.swmanager.system.constant.enums.WorkPlanStatus;
import com.swmanager.system.domain.Infra;
import com.swmanager.system.domain.SigunguCode;
import com.swmanager.system.domain.SwProject;
import com.swmanager.system.domain.User;
import com.swmanager.system.domain.workplan.WorkPlan;
import com.swmanager.system.dto.WorkPlanDTO;
import com.swmanager.system.repository.InfraRepository;
import com.swmanager.system.repository.SigunguCodeRepository;
import com.swmanager.system.repository.SwProjectRepository;
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
    @Autowired private SigunguCodeRepository sigunguCodeRepository;
    @Autowired private SwProjectRepository swProjectRepository;

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

        // 대상(인프라/지역) 연동 — workplan-target-infra-cascade (FR-9/9b/9c)
        applyTarget(plan, dto);

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
        plan.setStatus(dto.getStatus() != null ? dto.getStatus() : WorkPlanStatus.PLANNED.name());
        plan.setStatusReason(dto.getStatusReason());

        return workPlanRepository.save(plan);
    }

    // ===== [workplan-target-infra-cascade] 대상(인프라/지역) 해석 =====

    private static final java.util.Set<String> REGION_UNIT_LABELS = java.util.Set.of("도청", "본청");

    /**
     * 대상 연동: ① 계약(infra_id) → infra 연결 + region 4필드 서버 재계산(클라이언트 region 무시, FR-9b)
     *           ② 미계약 직접입력 → SUPPORT 한정 게이트(FR-9c-①) + region_code 검증(FR-11) + 시스템명 정제
     *           ③ 대상 없음 → infra null + region null(FR-9c-②)
     */
    private void applyTarget(WorkPlan plan, WorkPlanDTO dto) {
        // ① 계약 대상 = 사업(sw_pjt). region 4필드 서버 재계산(클라이언트 region 무시, FR-9b)
        if (dto.getProjId() != null) {
            SwProject proj = swProjectRepository.findById(dto.getProjId()).orElse(null);
            plan.setProject(proj);
            plan.setInfra(null);
            if (proj != null) {
                plan.setRegionCityNm(proj.getCityNm());
                plan.setRegionDistNm(proj.getDistNm());
                plan.setTargetSysNm(proj.getSysNm());
                plan.setRegionCode(resolveRegionCode(proj.getCityNm(), proj.getDistNm()));
            } else {
                clearRegion(plan);
            }
            return;
        }

        // ② 미계약 직접입력 (SUPPORT 한정)
        boolean hasRegionInput = isNotBlank(dto.getRegionCode()) || isNotBlank(dto.getTargetSysNm());
        if (hasRegionInput) {
            if (!"SUPPORT".equals(dto.getPlanType())) {
                throw new IllegalArgumentException("미계약 직접입력 대상은 업무지원(SUPPORT) 업무유형에서만 지정할 수 있습니다.");
            }
            SigunguCode sgg = isNotBlank(dto.getRegionCode())
                    ? sigunguCodeRepository.findById(dto.getRegionCode()).orElse(null) : null;
            if (sgg == null) {
                throw new IllegalArgumentException("올바른 시군구를 선택하세요.");
            }
            String sysNm = sanitizeSysNm(dto.getTargetSysNm());
            if (sysNm == null) {
                throw new IllegalArgumentException("시스템명을 입력하세요(1~100자).");
            }
            plan.setProject(null);
            plan.setInfra(null);
            plan.setRegionCode(sgg.getAdmSectC());
            plan.setRegionCityNm(sgg.getSidoNm());   // 서버 출처(클라이언트 이름 불신)
            plan.setRegionDistNm(sgg.getSggNm());
            plan.setTargetSysNm(sysNm);
            return;
        }

        // ③ 대상 없음
        plan.setProject(null);
        plan.setInfra(null);
        clearRegion(plan);
    }

    /** (시도명,시군구명) → adm_sect_c. 도청/본청→시도 self-행, 동명 접미사 제거, 군위군 행정개편 alias(§5-4). */
    private String resolveRegionCode(String cityNm, String distNm) {
        if (isBlank(cityNm)) return null;
        // 도청/본청 → 시도 self-행(sgg_name = sido_name)
        if (distNm != null && REGION_UNIT_LABELS.contains(distNm.trim())) {
            return firstAdmSectC(cityNm, cityNm);
        }
        String dist = stripSuffix(distNm);
        String city = cityNm;
        // 군위군: 2023 경북→대구 편입
        if ("군위군".equals(dist) && cityNm.startsWith("경상북도")) {
            city = "대구광역시";
        }
        String code = firstAdmSectC(city, dist);
        if (code != null) return code;
        // fallback: 시도 self-행
        return firstAdmSectC(cityNm, cityNm);
    }

    private String firstAdmSectC(String sido, String sgg) {
        if (isBlank(sido) || isBlank(sgg)) return null;
        List<SigunguCode> list = sigunguCodeRepository.findBySidoNmAndSggNm(sido, sgg);
        return list.isEmpty() ? null : list.get(0).getAdmSectC();
    }

    /** 괄호 접미사 제거: 고성군(강원도)→고성군, 당진시(가상화)→당진시 */
    static String stripSuffix(String s) {
        if (s == null) return null;
        return s.replaceAll("\\(.*?\\)", "").trim();
    }

    /** 시스템명 정제: 제어문자·HTML 특수문자 제거 + 공백압축 + trim + 1~100자. 빈값 null. */
    static String sanitizeSysNm(String raw) {
        if (raw == null) return null;
        String s = raw.replaceAll("[\\x00-\\x1F]", "")
                      .replaceAll("[<>\"'&]", "")
                      .replaceAll("\\s+", " ")
                      .trim();
        if (s.isEmpty()) return null;
        if (s.length() > 100) s = s.substring(0, 100).trim();
        return s;
    }

    private void clearRegion(WorkPlan plan) {
        plan.setRegionCode(null);
        plan.setRegionCityNm(null);
        plan.setRegionDistNm(null);
        plan.setTargetSysNm(null);
    }

    private static boolean isBlank(String s) { return s == null || s.isBlank(); }
    private static boolean isNotBlank(String s) { return !isBlank(s); }

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
