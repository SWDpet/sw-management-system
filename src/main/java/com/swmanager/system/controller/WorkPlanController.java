package com.swmanager.system.controller;

import com.swmanager.system.constant.enums.AccessActionType;
import com.swmanager.system.constant.enums.WorkPlanStatus;
import com.swmanager.system.constants.MenuName;
import com.swmanager.system.domain.Infra;
import com.swmanager.system.domain.User;
import com.swmanager.system.domain.workplan.WorkPlan;
import com.swmanager.system.config.CustomUserDetails;
import com.swmanager.system.dto.WorkPlanDTO;
import com.swmanager.system.repository.InfraRepository;
import com.swmanager.system.repository.UserRepository;
import com.swmanager.system.service.WorkPlanService;
import com.swmanager.system.service.LogService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 업무계획 Controller
 */
@Slf4j
@Controller
@RequestMapping("/workplan")
public class WorkPlanController {

    @Autowired private WorkPlanService workPlanService;
    @Autowired private InfraRepository infraRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private LogService logService;

    // === 권한 체크 ===

    private CustomUserDetails getCurrentUser() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) return null;
            Object principal = auth.getPrincipal();
            if (principal instanceof CustomUserDetails) return (CustomUserDetails) principal;
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isAdmin() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null) return false;
            return auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        } catch (Exception e) {
            return false;
        }
    }

    private String getAuth() {
        if (isAdmin()) return "EDIT";
        CustomUserDetails currentUser = getCurrentUser();
        if (currentUser == null) return "NONE";
        String auth = currentUser.getUser().getAuthWorkPlan();
        return (auth != null) ? auth : "NONE";
    }

    // === P-01: 업무 캘린더 (메인 화면) ===

    @GetMapping("/calendar")
    public String calendar(Model model, RedirectAttributes rttr) {
        String auth = getAuth();
        if ("NONE".equals(auth)) {
            rttr.addFlashAttribute("errorMessage", "접근 권한이 없습니다.");
            return "redirect:/";
        }

        List<Infra> infraList = infraRepository.findAll(Sort.by(Sort.Direction.ASC, "cityNm", "distNm"));
        List<User> users = userRepository.findByEnabledTrue();

        model.addAttribute("infraList", infraList);
        model.addAttribute("users", users);
        model.addAttribute("userAuth", auth);

        logService.log(MenuName.WORK_PLAN, AccessActionType.VIEW, "업무 캘린더 조회");
        return "workplan/workplan-calendar";
    }

    // === P-03: 프로세스 현황 (칸반 보드) ===

    @GetMapping("/process")
    public String processStatus(Model model, RedirectAttributes rttr) {
        String auth = getAuth();
        if ("NONE".equals(auth)) {
            rttr.addFlashAttribute("errorMessage", "접근 권한이 없습니다.");
            return "redirect:/";
        }

        // 7단계 프로세스별 업무 조회
        Map<Integer, List<WorkPlanDTO>> stepPlans = new HashMap<>();
        Map<Integer, Integer> stepCounts = new HashMap<>();
        Map<Integer, String> stepLabels = new HashMap<>();
        stepLabels.put(1, "대상선정");
        stepLabels.put(2, "현장방문");
        stepLabels.put(3, "계약체결");
        stepLabels.put(4, "착수계");
        stepLabels.put(5, "사업수행");
        stepLabels.put(6, "기성/준공");
        stepLabels.put(7, "대금신청");

        List<String> excludeStatuses = List.of(WorkPlanStatus.CANCELLED.name());
        int totalCount = 0;

        for (int step = 1; step <= 7; step++) {
            List<WorkPlanDTO> plans = workPlanService.getWorkPlansByProcessStep(step, excludeStatuses);
            stepPlans.put(step, plans);
            stepCounts.put(step, plans.size());
            totalCount += plans.size();
        }

        List<User> users = userRepository.findByEnabledTrue();

        model.addAttribute("stepPlans", stepPlans);
        model.addAttribute("stepCounts", stepCounts);
        model.addAttribute("stepLabels", stepLabels);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("users", users);
        model.addAttribute("userAuth", auth);

        logService.log(MenuName.WORK_PLAN, AccessActionType.VIEW, "프로세스 현황 조회");
        return "workplan/process-status";
    }

    // === P-01: FullCalendar JSON API ===

    @ResponseBody
    @GetMapping("/api/events")
    public ResponseEntity<List<Map<String, Object>>> getCalendarEvents(
            @RequestParam("start") String start,
            @RequestParam("end") String end,
            @RequestParam(name = "assigneeId", required = false) Long assigneeId) {

        LocalDate startDate = LocalDate.parse(start.substring(0, 10));
        LocalDate endDate = LocalDate.parse(end.substring(0, 10));

        List<WorkPlanDTO> plans;
        if (assigneeId != null) {
            plans = workPlanService.getWorkPlansByAssigneeAndDateRange(assigneeId, startDate, endDate);
        } else {
            plans = workPlanService.getWorkPlansByDateRange(startDate, endDate);
        }

        // FullCalendar 이벤트 형식으로 변환
        List<Map<String, Object>> events = plans.stream().map(p -> {
            Map<String, Object> event = new HashMap<>();
            event.put("id", p.getPlanId());
            event.put("title", p.getTitle());
            event.put("start", p.getStartDate());
            event.put("end", p.getEndDate());
            event.put("color", p.getColor());
            event.put("extendedProps", Map.of(
                "planType", p.getPlanType() != null ? p.getPlanType() : "",
                "planTypeLabel", WorkPlanDTO.getTypeLabel(p.getPlanType()),
                "status", p.getStatus() != null ? p.getStatus() : "",
                "statusLabel", WorkPlanDTO.getStatusLabel(p.getStatus()),
                "assigneeName", p.getAssigneeName() != null ? p.getAssigneeName() : "",
                "infraName", (p.getCityNm() != null ? p.getCityNm() + " " : "") + (p.getDistNm() != null ? p.getDistNm() : ""),
                "processStep", p.getProcessStep() != null ? p.getProcessStep() : 0,
                "stepLabel", WorkPlanDTO.getStepLabel(p.getProcessStep())
            ));
            return event;
        }).toList();

        return ResponseEntity.ok(events);
    }

    // === P-02: 업무 등록 화면 ===

    @GetMapping("/new")
    public String createForm(Model model,
                             @RequestParam(name = "date", required = false) String date,
                             @RequestParam(name = "infraId", required = false) Long infraId,
                             RedirectAttributes rttr) {
        if (!"EDIT".equals(getAuth())) {
            rttr.addFlashAttribute("errorMessage", "등록 권한이 없습니다.");
            return "redirect:/workplan/calendar";
        }

        WorkPlanDTO dto = new WorkPlanDTO();
        if (date != null) dto.setStartDate(date);
        if (infraId != null) dto.setInfraId(infraId);

        model.addAttribute("workPlan", dto);
        addFormAttributes(model);
        return "workplan/workplan-form";
    }

    // === P-02: 업무 수정 화면 ===

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Integer id, Model model, RedirectAttributes rttr) {
        if (!"EDIT".equals(getAuth())) {
            rttr.addFlashAttribute("errorMessage", "수정 권한이 없습니다.");
            return "redirect:/workplan/calendar";
        }

        WorkPlan entity = workPlanService.getWorkPlanById(id);
        model.addAttribute("workPlan", WorkPlanDTO.fromEntity(entity));
        addFormAttributes(model);
        return "workplan/workplan-form";
    }

    // === 업무 상세 조회 (Ajax) ===

    @ResponseBody
    @GetMapping("/api/detail/{id}")
    public ResponseEntity<WorkPlanDTO> getDetail(@PathVariable Integer id) {
        WorkPlanDTO dto = workPlanService.getWorkPlanDTOById(id);
        return ResponseEntity.ok(dto);
    }

    // === P-02: 업무 저장 ===

    @PostMapping("/save")
    public String save(@ModelAttribute WorkPlanDTO dto, RedirectAttributes rttr) {
        if (!"EDIT".equals(getAuth())) {
            rttr.addFlashAttribute("errorMessage", "저장 권한이 없습니다.");
            return "redirect:/workplan/calendar";
        }

        CustomUserDetails currentUser = getCurrentUser();
        User user = (currentUser != null) ? currentUser.getUser() : null;

        boolean isNew = (dto.getPlanId() == null);
        WorkPlan saved = workPlanService.saveWorkPlan(dto, user);

        AccessActionType action = isNew ? AccessActionType.CREATE : AccessActionType.UPDATE;
        logService.log(MenuName.WORK_PLAN, action, "업무계획 " + action.getLabel() + " (ID: " + saved.getPlanId() + ", " + saved.getTitle() + ")");

        rttr.addFlashAttribute("successMessage", "업무계획이 " + (isNew ? "등록" : "수정") + "되었습니다.");
        return "redirect:/workplan/calendar";
    }

    // === 업무 삭제 ===

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Integer id, RedirectAttributes rttr) {
        if (!"EDIT".equals(getAuth())) {
            rttr.addFlashAttribute("errorMessage", "삭제 권한이 없습니다.");
            return "redirect:/workplan/calendar";
        }

        WorkPlan target = workPlanService.getWorkPlanById(id);
        String title = target.getTitle();
        workPlanService.deleteWorkPlan(id);

        logService.log(MenuName.WORK_PLAN, AccessActionType.DELETE, "업무계획 삭제 (ID: " + id + ", " + title + ")");
        rttr.addFlashAttribute("successMessage", "업무계획이 삭제되었습니다.");
        return "redirect:/workplan/calendar";
    }

    // === 상태 변경 API ===

    @ResponseBody
    @PostMapping("/api/status/{id}")
    public ResponseEntity<Map<String, Object>> updateStatus(
            @PathVariable Integer id,
            @RequestParam("status") String status,
            @RequestParam(name = "reason", required = false) String reason) {

        if (!"EDIT".equals(getAuth())) {
            return ResponseEntity.status(403).body(Map.of("error", "권한이 없습니다."));
        }

        WorkPlan updated = workPlanService.updateStatus(id, status, reason);
        logService.log(MenuName.WORK_PLAN, AccessActionType.UPDATE, "업무계획 상태변경 (ID: " + id + ", " + status + ")");

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("planId", updated.getPlanId());
        result.put("status", updated.getStatus());
        result.put("statusLabel", WorkPlanDTO.getStatusLabel(updated.getStatus()));
        return ResponseEntity.ok(result);
    }

    // === 공통 ===

    private void addFormAttributes(Model model) {
        List<Infra> infraList = infraRepository.findAll(Sort.by(Sort.Direction.ASC, "cityNm", "distNm"));
        List<User> users = userRepository.findByEnabledTrue();
        model.addAttribute("infraList", infraList);
        model.addAttribute("users", users);
    }
}
