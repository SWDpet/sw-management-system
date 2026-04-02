package com.swmanager.system.controller;

import com.swmanager.system.domain.PersonInfo;
import com.swmanager.system.domain.SigunguCode;
import com.swmanager.system.config.CustomUserDetails;
import com.swmanager.system.repository.PersonInfoRepository;
import com.swmanager.system.repository.SigunguCodeRepository;
import com.swmanager.system.repository.SysMstRepository;
import com.swmanager.system.service.LogService;
import com.swmanager.system.exception.InsufficientPermissionException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 담당자 관리 Controller
 * authPerson 권한 체크 추가
 */
@Slf4j
@Controller
@RequestMapping("/person")
public class PersonController {

    @Autowired private PersonInfoRepository personInfoRepository;
    @Autowired private LogService logService;
    
    @Autowired private SigunguCodeRepository sigunguRepository;
    @Autowired private SysMstRepository sysMstRepository;

    /**
     * 현재 로그인한 사용자 정보 가져오기
     */
    private CustomUserDetails getCurrentUser() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            
            if (auth == null || !auth.isAuthenticated()) {
                return null;
            }
            
            Object principal = auth.getPrincipal();
            
            if (principal instanceof CustomUserDetails) {
                return (CustomUserDetails) principal;
            }
            
            return null;
            
        } catch (Exception e) {
            log.error("Error getting current user", e);
            return null;
        }
    }

    /**
     * 현재 사용자의 담당자 권한 조회
     */
    private String getAuth() {
        CustomUserDetails currentUser = getCurrentUser();
        
        if (currentUser == null) {
            log.warn("Current user is null, returning NONE");
            return "NONE";
        }
        
        String auth = currentUser.getUser().getAuthPerson();
        log.debug("User: {}, AuthPerson: {}", currentUser.getUsername(), auth);
        
        return (auth != null) ? auth : "NONE";
    }

    /**
     * 담당자 목록 조회
     */
    @GetMapping("/list")
    public String list(Model model, 
                       @PageableDefault(size = 10, sort = {"cityNm", "distNm", "orgNm", "sysNmEn"}, direction = Sort.Direction.ASC) Pageable pageable,
                       @RequestParam(value = "kw", required = false, defaultValue = "") String kw) {
        
        log.info("=== 담당자 목록 조회 시작 ===");
        
        // ✅ 권한 체크
        String auth = getAuth();
        if ("NONE".equals(auth)) {
            log.warn("권한 없는 사용자의 담당자 목록 접근 시도");
            throw new InsufficientPermissionException("담당자 조회");
        }
        
        Page<PersonInfo> paging = personInfoRepository.findAllByKeyword(kw, pageable);
        
        model.addAttribute("paging", paging);
        model.addAttribute("kw", kw);
        model.addAttribute("userAuth", auth);  // VIEW 또는 EDIT
        
        log.info("담당자 목록 조회 성공 - 총 {}건", paging.getTotalElements());
        
        return "person-list";
    }

    /**
     * 담당자 상세 조회
     */
    @GetMapping("/detail/{id}")
    public String detail(@PathVariable("id") Long id, Model model) {
        
        log.info("담당자 상세 조회 - ID: {}", id);
        
        // ✅ 권한 체크
        String auth = getAuth();
        if ("NONE".equals(auth)) {
            log.warn("권한 없는 사용자의 담당자 상세 접근 시도");
            throw new InsufficientPermissionException("담당자 조회");
        }
        
        PersonInfo person = personInfoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 담당자 정보가 없습니다. ID: " + id));
        
        model.addAttribute("person", person);
        model.addAttribute("isDetail", true);
        model.addAttribute("userAuth", auth);
        addCommonAttributes(model);
        
        logService.log("담당자관리", "조회", "담당자 상세 조회: " + person.getUserNm());
        
        return "person-form"; 
    }

    /**
     * 담당자 등록 폼
     */
    @GetMapping("/new")
    public String createForm(Model model) {
        
        log.info("담당자 신규 등록 폼 진입");
        
        // ✅ 권한 체크 - EDIT 권한 필요
        String auth = getAuth();
        if (!"EDIT".equals(auth)) {
            log.warn("권한 없는 사용자의 담당자 등록 시도");
            throw new InsufficientPermissionException("담당자 등록");
        }
        
        model.addAttribute("person", new PersonInfo());
        model.addAttribute("isDetail", false);
        model.addAttribute("userAuth", auth);
        addCommonAttributes(model);
        
        return "person-form";
    }

    /**
     * 담당자 정보 저장 (등록/수정)
     */
    @PostMapping("/save")
    @ResponseBody
    public ResponseEntity<String> save(@ModelAttribute PersonInfo person) {
        
        log.info("담당자 정보 저장 요청 - 이름: {}", person.getUserNm());
        
        // ✅ 권한 체크 - EDIT 권한 필요
        String auth = getAuth();
        if (!"EDIT".equals(auth)) {
            log.warn("권한 없는 사용자의 담당자 저장 시도");
            return ResponseEntity.status(403).body("권한이 없습니다.");
        }
        
        try {
            boolean isNew = (person.getId() == null);
            personInfoRepository.save(person);
            
            String action = isNew ? "등록" : "수정";
            log.info("담당자 정보 {} 성공 - ID: {}", action, person.getId());
            
            logService.log("담당자관리", action, "담당자 정보 저장: " + person.getUserNm());
            
            return ResponseEntity.ok("저장되었습니다.");
        } catch (Exception e) {
            log.error("담당자 정보 저장 실패", e);
            return ResponseEntity.badRequest().body("저장 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 담당자 삭제
     */
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable("id") Long id) {
        
        log.warn("담당자 삭제 요청 - ID: {}", id);
        
        // ✅ 권한 체크 - EDIT 권한 필요
        String auth = getAuth();
        if (!"EDIT".equals(auth)) {
            log.warn("권한 없는 사용자의 담당자 삭제 시도");
            throw new InsufficientPermissionException("담당자 삭제");
        }
        
        PersonInfo person = personInfoRepository.findById(id).orElse(null);
        if (person != null) {
            log.info("담당자 삭제 - 이름: {}", person.getUserNm());
            logService.log("담당자관리", "삭제", "담당자 삭제: " + person.getUserNm());
            personInfoRepository.deleteById(id);
        }
        
        return "redirect:/person/list";
    }

    /**
     * 시군구 목록 조회 API
     */
    @GetMapping("/api/sigungu")
    @ResponseBody
    public List<SigunguCode> getSigunguList(@RequestParam("sido") String sido) {
        return sigunguRepository.findBySidoNmOrderBySggNm(sido);
    }

    /**
     * 공통 속성 추가
     */
    private void addCommonAttributes(Model model) {
        model.addAttribute("sidoList", sigunguRepository.findDistinctSidoNm());
        model.addAttribute("sysMstList", sysMstRepository.findAll(Sort.by(Sort.Direction.ASC, "nm")));
    }
}
