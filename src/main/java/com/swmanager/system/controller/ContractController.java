package com.swmanager.system.controller;

import com.swmanager.system.domain.Infra;
import com.swmanager.system.domain.User;
import com.swmanager.system.domain.workplan.Contract;
import com.swmanager.system.domain.workplan.InspectCycle;
import com.swmanager.system.config.CustomUserDetails;
import com.swmanager.system.dto.ContractDTO;
import com.swmanager.system.repository.InfraRepository;
import com.swmanager.system.repository.UserRepository;
import com.swmanager.system.service.ContractService;
import com.swmanager.system.service.LogService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 사업현황(계약) Controller
 */
@Slf4j
@Controller
@RequestMapping("/contract")
public class ContractController {

    @Autowired private ContractService contractService;
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
        String auth = currentUser.getUser().getAuthContract();
        return (auth != null) ? auth : "NONE";
    }

    // === B-01: 사업현황 목록 ===

    @GetMapping("/list")
    public String list(Model model,
                       @PageableDefault(page = 0, size = 15, sort = "contractId", direction = Sort.Direction.DESC) Pageable pageable,
                       @RequestParam(name = "year", required = false) Integer year,
                       @RequestParam(name = "status", required = false) String status,
                       @RequestParam(name = "keyword", required = false) String keyword,
                       RedirectAttributes rttr) {

        String auth = getAuth();
        if ("NONE".equals(auth)) {
            rttr.addFlashAttribute("errorMessage", "접근 권한이 없습니다.");
            return "redirect:/";
        }

        Page<Contract> contracts = contractService.searchContracts(year, status, keyword, pageable);
        List<Integer> years = contractService.getDistinctYears();

        model.addAttribute("contracts", contracts);
        model.addAttribute("years", years);
        model.addAttribute("currentYear", year);
        model.addAttribute("currentStatus", status);
        model.addAttribute("keyword", keyword);
        model.addAttribute("userAuth", auth);

        logService.log("사업현황", "조회", "사업현황 목록 조회");
        return "contract/contract-list";
    }

    // === B-05: 사업 진행 현황 대시보드 ===

    @GetMapping("/dashboard")
    public String dashboard(Model model,
                            @RequestParam(name = "year", required = false) Integer year,
                            RedirectAttributes rttr) {
        String auth = getAuth();
        if ("NONE".equals(auth)) {
            rttr.addFlashAttribute("errorMessage", "접근 권한이 없습니다.");
            return "redirect:/";
        }

        List<Contract> allContracts;
        if (year != null) {
            allContracts = contractService.getContractsByYear(year);
        } else {
            allContracts = contractService.getAllContracts();
        }

        // 상태별 카운트
        Map<String, Long> statusCounts = new HashMap<>();
        String[] statuses = {"BUDGET", "CONTRACTING", "COMMENCED", "IN_PROGRESS", "INTERIM", "COMPLETED", "SETTLED"};
        for (String s : statuses) { statusCounts.put(s, 0L); }
        for (Contract c : allContracts) {
            statusCounts.merge(c.getProgressStatus(), 1L, Long::sum);
        }

        model.addAttribute("contracts", allContracts);
        model.addAttribute("statusCounts", statusCounts);
        model.addAttribute("totalCount", allContracts.size());
        model.addAttribute("years", contractService.getDistinctYears());
        model.addAttribute("currentYear", year);
        model.addAttribute("userAuth", auth);

        logService.log("사업현황", "조회", "사업 진행 현황 대시보드 조회");
        return "contract/contract-dashboard";
    }

    // === B-02: 사업 등록 ===

    @GetMapping("/new")
    public String createForm(Model model, RedirectAttributes rttr) {
        if (!"EDIT".equals(getAuth())) {
            rttr.addFlashAttribute("errorMessage", "등록 권한이 없습니다.");
            return "redirect:/contract/list";
        }

        model.addAttribute("contract", new ContractDTO());
        addFormAttributes(model);
        return "contract/contract-form";
    }

    // === B-02: 사업 수정 ===

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Integer id, Model model, RedirectAttributes rttr) {
        if (!"EDIT".equals(getAuth())) {
            rttr.addFlashAttribute("errorMessage", "수정 권한이 없습니다.");
            return "redirect:/contract/detail/" + id;
        }

        Contract entity = contractService.getContractById(id);
        model.addAttribute("contract", ContractDTO.fromEntity(entity));
        addFormAttributes(model);
        return "contract/contract-form";
    }

    // === B-02: 사업 저장 ===

    @PostMapping("/save")
    public String save(@ModelAttribute ContractDTO dto, RedirectAttributes rttr) {
        if (!"EDIT".equals(getAuth())) {
            rttr.addFlashAttribute("errorMessage", "저장 권한이 없습니다.");
            return "redirect:/contract/list";
        }

        boolean isNew = (dto.getContractId() == null);
        Contract saved = contractService.saveContract(dto);

        String action = isNew ? "등록" : "수정";
        logService.log("사업현황", action, "사업현황 " + action + " (ID: " + saved.getContractId() + ", " + saved.getContractName() + ")");

        return "redirect:/contract/detail/" + saved.getContractId();
    }

    // === B-03: 사업 상세 ===

    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Integer id, Model model, RedirectAttributes rttr) {
        String auth = getAuth();
        if ("NONE".equals(auth)) {
            rttr.addFlashAttribute("errorMessage", "접근 권한이 없습니다.");
            return "redirect:/";
        }

        Contract entity = contractService.getContractById(id);
        ContractDTO dto = ContractDTO.fromEntity(entity);

        model.addAttribute("contract", dto);
        model.addAttribute("userAuth", auth);

        logService.log("사업현황", "조회", "사업현황 상세 조회 (ID: " + id + ", " + entity.getContractName() + ")");
        return "contract/contract-detail";
    }

    // === 사업 삭제 ===

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Integer id, RedirectAttributes rttr) {
        if (!"EDIT".equals(getAuth())) {
            rttr.addFlashAttribute("errorMessage", "삭제 권한이 없습니다.");
            return "redirect:/contract/list";
        }

        Contract target = contractService.getContractById(id);
        String name = target.getContractName();
        contractService.deleteContract(id);

        logService.log("사업현황", "삭제", "사업현황 삭제 (ID: " + id + ", " + name + ")");
        return "redirect:/contract/list";
    }

    // === B-03: 과업참여자 저장 (Ajax) ===

    @PostMapping("/participants/save/{contractId}")
    public String saveParticipants(@PathVariable Integer contractId,
                                   @RequestParam(name = "userId", required = false) List<Long> userIds,
                                   @RequestParam(name = "roleType", required = false) List<String> roleTypes,
                                   @RequestParam(name = "techGrade", required = false) List<String> techGrades,
                                   @RequestParam(name = "taskDesc", required = false) List<String> taskDescs,
                                   @RequestParam(name = "isSiteRep", required = false) List<String> siteReps,
                                   RedirectAttributes rttr) {
        if (!"EDIT".equals(getAuth())) {
            rttr.addFlashAttribute("errorMessage", "수정 권한이 없습니다.");
            return "redirect:/contract/detail/" + contractId;
        }

        List<ContractDTO.ParticipantDTO> list = new ArrayList<>();
        if (userIds != null) {
            for (int i = 0; i < userIds.size(); i++) {
                ContractDTO.ParticipantDTO p = new ContractDTO.ParticipantDTO();
                p.setUserId(userIds.get(i));
                p.setRoleType(roleTypes != null && i < roleTypes.size() ? roleTypes.get(i) : "PARTICIPANT");
                p.setTechGrade(techGrades != null && i < techGrades.size() ? techGrades.get(i) : null);
                p.setTaskDesc(taskDescs != null && i < taskDescs.size() ? taskDescs.get(i) : null);
                p.setIsSiteRep(siteReps != null && siteReps.contains(String.valueOf(i)));
                list.add(p);
            }
        }

        contractService.saveParticipants(contractId, list);
        logService.log("사업현황", "수정", "과업참여자 저장 (계약 ID: " + contractId + ")");
        rttr.addFlashAttribute("successMessage", "과업참여자가 저장되었습니다.");
        return "redirect:/contract/detail/" + contractId + "#participants";
    }

    // === B-04: 유지보수 대상 저장 ===

    @PostMapping("/targets/save/{contractId}")
    public String saveTargets(@PathVariable Integer contractId,
                              @RequestParam(name = "targetCategory", required = false) List<String> categories,
                              @RequestParam(name = "productName", required = false) List<String> names,
                              @RequestParam(name = "productDetail", required = false) List<String> details,
                              @RequestParam(name = "quantity", required = false) List<Integer> quantities,
                              RedirectAttributes rttr) {
        if (!"EDIT".equals(getAuth())) {
            rttr.addFlashAttribute("errorMessage", "수정 권한이 없습니다.");
            return "redirect:/contract/detail/" + contractId;
        }

        List<ContractDTO.TargetDTO> list = new ArrayList<>();
        if (names != null) {
            for (int i = 0; i < names.size(); i++) {
                if (names.get(i) == null || names.get(i).trim().isEmpty()) continue;
                ContractDTO.TargetDTO t = new ContractDTO.TargetDTO();
                t.setTargetCategory(categories != null && i < categories.size() ? categories.get(i) : "S/W");
                t.setProductName(names.get(i));
                t.setProductDetail(details != null && i < details.size() ? details.get(i) : null);
                t.setQuantity(quantities != null && i < quantities.size() ? quantities.get(i) : 1);
                list.add(t);
            }
        }

        contractService.saveTargets(contractId, list);
        logService.log("사업현황", "수정", "유지보수 대상 저장 (계약 ID: " + contractId + ")");
        rttr.addFlashAttribute("successMessage", "유지보수 대상이 저장되었습니다.");
        return "redirect:/contract/detail/" + contractId + "#targets";
    }

    // === B-04: 점검주기 관리 ===

    @GetMapping("/inspect-cycle")
    public String inspectCycleList(Model model, RedirectAttributes rttr) {
        String auth = getAuth();
        if ("NONE".equals(auth)) {
            rttr.addFlashAttribute("errorMessage", "접근 권한이 없습니다.");
            return "redirect:/";
        }

        List<InspectCycle> cycles = contractService.getAllInspectCycles();
        List<Infra> infraList = infraRepository.findAll(Sort.by(Sort.Direction.ASC, "cityNm", "distNm"));
        List<User> users = userRepository.findByEnabledTrue();

        model.addAttribute("cycles", cycles);
        model.addAttribute("infraList", infraList);
        model.addAttribute("users", users);
        model.addAttribute("userAuth", auth);

        logService.log("사업현황", "조회", "점검주기 관리 목록 조회");
        return "contract/inspect-cycle";
    }

    @PostMapping("/inspect-cycle/save")
    public String saveInspectCycle(@RequestParam(name = "cycleId", required = false) Integer cycleId,
                                   @RequestParam("infraId") Long infraId,
                                   @RequestParam("cycleType") String cycleType,
                                   @RequestParam(name = "assigneeId", required = false) Long assigneeId,
                                   @RequestParam(name = "contactName", required = false) String contactName,
                                   @RequestParam(name = "contactPhone", required = false) String contactPhone,
                                   @RequestParam(name = "contactEmail", required = false) String contactEmail,
                                   @RequestParam(name = "preContactDays", defaultValue = "7") Integer preContactDays,
                                   RedirectAttributes rttr) {
        if (!"EDIT".equals(getAuth())) {
            rttr.addFlashAttribute("errorMessage", "저장 권한이 없습니다.");
            return "redirect:/contract/inspect-cycle";
        }

        InspectCycle cycle;
        if (cycleId != null) {
            cycle = contractService.getInspectCycleById(cycleId);
        } else {
            cycle = new InspectCycle();
        }

        Infra infra = infraRepository.findById(infraId).orElse(null);
        cycle.setInfra(infra);
        cycle.setCycleType(cycleType);
        cycle.setContactName(contactName);
        cycle.setContactPhone(contactPhone);
        cycle.setContactEmail(contactEmail);
        cycle.setPreContactDays(preContactDays);

        if (assigneeId != null) {
            User assignee = userRepository.findById(assigneeId).orElse(null);
            cycle.setAssignee(assignee);
        }

        contractService.saveInspectCycle(cycle);
        logService.log("사업현황", "저장", "점검주기 저장 (인프라: " + (infra != null ? infra.getDistNm() : "") + ")");
        return "redirect:/contract/inspect-cycle";
    }

    @PostMapping("/inspect-cycle/delete/{id}")
    public String deleteInspectCycle(@PathVariable Integer id, RedirectAttributes rttr) {
        if (!"EDIT".equals(getAuth())) {
            rttr.addFlashAttribute("errorMessage", "삭제 권한이 없습니다.");
            return "redirect:/contract/inspect-cycle";
        }

        contractService.deleteInspectCycle(id);
        logService.log("사업현황", "삭제", "점검주기 삭제 (ID: " + id + ")");
        return "redirect:/contract/inspect-cycle";
    }

    // === 공통 ===

    private void addFormAttributes(Model model) {
        List<Infra> infraList = infraRepository.findAll(Sort.by(Sort.Direction.ASC, "cityNm", "distNm"));
        model.addAttribute("infraList", infraList);
    }

    /**
     * API: 사용자 목록 (과업참여자 선택용)
     */
    @ResponseBody
    @GetMapping("/api/users")
    public List<User> getUsers() {
        return userRepository.findByEnabledTrue();
    }
}
