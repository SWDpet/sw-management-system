package com.swmanager.system.controller;

import com.swmanager.system.domain.Infra;
import com.swmanager.system.domain.SigunguCode;
import com.swmanager.system.config.CustomUserDetails;
import com.swmanager.system.repository.*;
import com.swmanager.system.service.InfraService;
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

import java.util.List;

/**
 * 인프라/서버 관리 Controller
 * SecurityContext 직접 사용 버전
 */
@Slf4j
@Controller
@RequestMapping("/infra")
public class InfraController {

    @Autowired private InfraService infraService;
    @Autowired private SysMstRepository sysMstRepository;
    @Autowired private SigunguCodeRepository sigunguRepository;
    @Autowired private ContFrmMstRepository contFrmRepository;
    @Autowired private PrjTypesRepository prjTypesRepository;
    @Autowired private MaintTpMstRepository maintTpRepository;
    @Autowired private ContStatMstRepository contStatRepository;

    @Autowired private LogService logService;

    /**
     * 현재 로그인한 사용자 정보 가져오기
     */
    private CustomUserDetails getCurrentUser() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            
            if (auth == null || !auth.isAuthenticated()) {
                log.warn("Authentication is null or not authenticated");
                return null;
            }
            
            Object principal = auth.getPrincipal();
            
            if (principal instanceof CustomUserDetails) {
                return (CustomUserDetails) principal;
            }
            
            if ("anonymousUser".equals(principal)) {
                log.warn("Anonymous user detected");
                return null;
            }
            
            log.warn("Principal is not CustomUserDetails: {}", principal.getClass().getName());
            return null;
            
        } catch (Exception e) {
            log.error("Error getting current user", e);
            return null;
        }
    }

    /**
     * ADMIN 역할 여부 확인
     */
    private boolean isAdmin() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null) return false;
            return auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 현재 사용자의 인프라 권한 조회
     * ADMIN → "EDIT" 반환 (모든 기능 허용)
     */
    private String getAuth() {
        if (isAdmin()) return "EDIT";

        CustomUserDetails currentUser = getCurrentUser();
        
        if (currentUser == null) {
            log.warn("Current user is null, returning NONE");
            return "NONE";
        }
        
        String auth = currentUser.getUser().getAuthInfra();
        log.debug("User: {}, Auth: {}", currentUser.getUsername(), auth);
        
        return (auth != null) ? auth : "NONE";
    }

    private void addCommonAttributes(Model model) {
        model.addAttribute("sysMstList", sysMstRepository.findAll());
        model.addAttribute("sidoList", sigunguRepository.findDistinctSidoNm());
        model.addAttribute("contFrmList", contFrmRepository.findAll()); 
        model.addAttribute("prjTypesList", prjTypesRepository.findAll());
        model.addAttribute("maintTpList", maintTpRepository.findAll());
        model.addAttribute("contStatList", contStatRepository.findAll());
    }

    /**
     * 1. 목록 페이지
     */
    @GetMapping("/list")
    public String list(Model model, 
                       @PageableDefault(page = 0, size = 10, sort = "infraId", direction = Sort.Direction.DESC) Pageable pageable,
                       @RequestParam(name = "type", defaultValue = "PROD") String type,
                       @RequestParam(name = "keyword", required = false) String keyword,
                       RedirectAttributes rttr) {
        
        log.info("=== 인프라 목록 조회 시작 ===");
        
        String auth = getAuth();
        if ("NONE".equals(auth)) {
            log.warn("접근 권한 없음");
            rttr.addFlashAttribute("errorMessage", "접근 권한이 없습니다.");
            return "redirect:/"; 
        }

        Page<Infra> infraList = infraService.getInfraList(type, keyword, pageable);
        
        model.addAttribute("infraList", infraList);
        model.addAttribute("currentType", type);
        model.addAttribute("keyword", keyword);
        model.addAttribute("userAuth", auth); 
        
        // 목록 조회 로그 기록
        logService.log("서버관리", "조회", "서버관리 현황 목록 조회 (유형: " + type + ")");
        
        log.info("인프라 목록 조회 성공 - 총 {}건", infraList.getTotalElements());
        return "infra-list";
    }

    /**
     * 2. 등록 페이지
     */
    @GetMapping("/new")
    public String createForm(Model model, RedirectAttributes rttr) {
        
        log.info("신규 인프라 등록 폼 진입");
        
        if (!"EDIT".equals(getAuth())) {
            log.warn("등록 권한 없음");
            rttr.addFlashAttribute("errorMessage", "등록 권한이 없습니다.");
            return "redirect:/infra/list";
        }

        model.addAttribute("infra", new Infra());
        addCommonAttributes(model);
        return "infra-form";
    }

    /**
     * 3. 상세 페이지
     */
    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Long id, Model model, RedirectAttributes rttr) {
        
        log.info("인프라 상세 조회 - ID: {}", id);
        
        if ("NONE".equals(getAuth())) {
            log.warn("조회 권한 없음");
            rttr.addFlashAttribute("errorMessage", "접근 권한이 없습니다.");
            return "redirect:/";
        }

        Infra infra = infraService.getInfraById(id);
        model.addAttribute("infra", infra);
        model.addAttribute("userAuth", getAuth()); 
        
        // 상세 조회 로그 기록
        String sysNm = (infra != null) ? infra.getSysNm() : "Unknown";
        logService.log("서버관리", "조회", "인프라 상세 조회 (ID: " + id + ", 시스템: " + sysNm + ")");

        return "infra-detail";
    }

    /**
     * 4. 수정 페이지
     */
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes rttr) {
        
        log.info("인프라 수정 폼 진입 - ID: {}", id);
        
        if (!"EDIT".equals(getAuth())) {
            log.warn("수정 권한 없음");
            rttr.addFlashAttribute("errorMessage", "수정 권한이 없습니다.");
            return "redirect:/infra/detail/" + id;
        }

        Infra infra = infraService.getInfraById(id);
        model.addAttribute("infra", infra);
        addCommonAttributes(model);
        return "infra-form";
    }

    /**
     * 5. 저장
     */
    @PostMapping("/save")
    public String save(@ModelAttribute Infra infra, RedirectAttributes rttr) {
        
        log.info("인프라 저장 요청 - 시스템: {}", infra.getSysNm());
        
        if (!"EDIT".equals(getAuth())) {
            log.warn("저장 권한 없음");
            rttr.addFlashAttribute("errorMessage", "저장 권한이 없습니다.");
            return "redirect:/infra/list";
        }

        boolean isNew = (infra.getInfraId() == null);
        infraService.saveInfra(infra);
        
        String action = isNew ? "등록" : "수정";
        String msg = String.format("인프라 자산 %s (ID: %d, 시스템: %s)", action, infra.getInfraId(), infra.getSysNm());
        logService.log("서버관리", action, msg);
        
        log.info("인프라 저장 성공 - ID: {}", infra.getInfraId());

        String type = (infra.getInfraType() != null) ? infra.getInfraType() : "PROD";
        return "redirect:/infra/list?type=" + type;
    }

    /**
     * 6. 삭제
     */
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes rttr) {
        
        log.warn("인프라 삭제 요청 - ID: {}", id);
        
        if (!"EDIT".equals(getAuth())) {
            log.warn("삭제 권한 없음");
            rttr.addFlashAttribute("errorMessage", "삭제 권한이 없습니다.");
            return "redirect:/infra/list";
        }

        Infra target = infraService.getInfraById(id);
        String sysNm = (target != null) ? target.getSysNm() : "Unknown";

        infraService.deleteInfra(id);
        
        String msg = String.format("인프라 자산 삭제 (ID: %d, 시스템: %s)", id, sysNm);
        logService.log("서버관리", "삭제", msg);
        
        log.info("인프라 삭제 완료 - ID: {}", id);

        return "redirect:/infra/list";
    }

    /**
     * API: 시군구 조회
     */
    @ResponseBody
    @GetMapping("/api/districts")
    public List<SigunguCode> getDistricts(@RequestParam(name="sido") String sido) {
        return sigunguRepository.findBySidoNm(sido);
    }
}
