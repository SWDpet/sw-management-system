package com.swmanager.system.controller;

import com.swmanager.system.domain.User;
import com.swmanager.system.config.CustomUserDetails;
import com.swmanager.system.repository.UserRepository;
import com.swmanager.system.service.LogService;
import com.swmanager.system.exception.InsufficientPermissionException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 관리자 - 회원 관리 Controller
 * ROLE_ADMIN 권한 필수
 */
@Slf4j
@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    @Autowired private UserRepository userRepository;
    @Autowired private LogService logService;
    
    // 페이지당 표시할 사용자 수
    private static final int PAGE_SIZE = 10;

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
     * 관리자 권한 체크
     */
    private void checkAdminAuth() {
        CustomUserDetails currentUser = getCurrentUser();
        
        if (currentUser == null) {
            log.warn("미로그인 사용자의 관리자 페이지 접근 시도");
            throw new InsufficientPermissionException("관리자 권한 필요 - 로그인 필요");
        }
        
        String userRole = currentUser.getUser().getUserRole();
        
        if (!"ROLE_ADMIN".equals(userRole)) {
            log.warn("권한 없는 사용자의 관리자 페이지 접근 시도 - 사용자: {}, 권한: {}", 
                     currentUser.getUsername(), userRole);
            throw new InsufficientPermissionException("관리자 권한 필요");
        }
        
        log.debug("관리자 권한 확인 - 사용자: {}", currentUser.getUsername());
    }

    /**
     * 회원 관리 페이지 (검색 + 페이징)
     * 
     * @param page 페이지 번호 (0부터 시작)
     * @param searchType 검색 타입 (userid, username, orgNm, deptNm, teamNm, tel, email)
     * @param keyword 검색 키워드
     */
    @GetMapping
    public String userManagement(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String searchType,
            @RequestParam(required = false) String keyword,
            Model model) {
        
        log.info("=== 회원 관리 페이지 접근 ===");
        log.info("Page: {}, 검색 타입: {}, 키워드: {}", page, searchType, keyword);
        
        // ✅ 관리자 권한 체크
        checkAdminAuth();
        
        // 승인 대기 사용자 (페이징 없음)
        List<User> pendingUsers = userRepository.findByEnabledFalse();
        
        // 페이징 설정 (10개씩, 등록일 최신순)
        Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("regDt").descending());
        
        Page<User> activeUserPage;
        
        // 검색 처리 (페이징 적용)
        if (keyword != null && !keyword.trim().isEmpty() && searchType != null) {
            log.info("검색 수행 - 타입: {}, 키워드: {}", searchType, keyword);
            
            switch (searchType) {
                case "userid":
                    activeUserPage = userRepository.searchByUserid(keyword, pageable);
                    break;
                case "username":
                    activeUserPage = userRepository.searchByUsername(keyword, pageable);
                    break;
                case "orgNm":
                    activeUserPage = userRepository.searchByOrgNm(keyword, pageable);
                    break;
                case "deptNm":
                    activeUserPage = userRepository.searchByDeptNm(keyword, pageable);
                    break;
                case "teamNm":
                    activeUserPage = userRepository.searchByTeamNm(keyword, pageable);
                    break;
                case "tel":
                    activeUserPage = userRepository.searchByTel(keyword, pageable);
                    break;
                case "email":
                    activeUserPage = userRepository.searchByEmail(keyword, pageable);
                    break;
                default:
                    activeUserPage = userRepository.findByEnabledTrue(pageable);
                    break;
            }
            
            model.addAttribute("searchType", searchType);
            model.addAttribute("keyword", keyword);
            model.addAttribute("searchInfo", getSearchTypeName(searchType) + " 검색: " + keyword);
            
            log.info("검색 결과: {}명 (전체 {}명)", activeUserPage.getContent().size(), activeUserPage.getTotalElements());
            
        } else {
            // 검색이 없으면 전체 조회 (페이징)
            activeUserPage = userRepository.findByEnabledTrue(pageable);
        }

        model.addAttribute("pendingUsers", pendingUsers);
        model.addAttribute("activeUsers", activeUserPage.getContent());
        model.addAttribute("activeUserPage", activeUserPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", activeUserPage.getTotalPages());
        model.addAttribute("totalElements", activeUserPage.getTotalElements());
        
        log.info("회원 목록 조회 - 승인 대기: {}명, 활성: {}명 (전체 {}명, 페이지 {}/{})", 
                 pendingUsers.size(), activeUserPage.getContent().size(), 
                 activeUserPage.getTotalElements(), page + 1, activeUserPage.getTotalPages());
        
        return "admin-user-list"; 
    }
    
    /**
     * 검색 타입 한글명 반환
     */
    private String getSearchTypeName(String searchType) {
        switch (searchType) {
            case "userid": return "아이디";
            case "username": return "성명";
            case "orgNm": return "소속기관";
            case "deptNm": return "부서";
            case "teamNm": return "팀";
            case "tel": return "연락처";
            case "email": return "이메일";
            default: return "전체";
        }
    }

    /**
     * 회원 승인
     */
    @PostMapping("/approve")
    public String approveUser(
            @RequestParam("userSeq") Long userSeq,
            @RequestParam("authDashboard") String authDashboard,
            @RequestParam("authProject") String authProject,
            @RequestParam("authPerson") String authPerson,
            @RequestParam("authInfra") String authInfra,
            @RequestParam(value = "authLicense", required = false, defaultValue = "NONE") String authLicense,
            @RequestParam(value = "authQuotation", required = false, defaultValue = "NONE") String authQuotation,
            @RequestParam(value = "authWorkPlan", required = false, defaultValue = "NONE") String authWorkPlan,
            @RequestParam(value = "authDocument", required = false, defaultValue = "NONE") String authDocument,
            @RequestParam(value = "authContract", required = false, defaultValue = "NONE") String authContract,
            @RequestParam(value = "authPerformance", required = false, defaultValue = "NONE") String authPerformance) {

        log.info("회원 승인 요청 - userSeq: {}", userSeq);

        // ✅ 관리자 권한 체크
        checkAdminAuth();

        User user = userRepository.findById(userSeq)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        user.setAuthDashboard(authDashboard);
        user.setAuthProject(authProject);
        user.setAuthPerson(authPerson);
        user.setAuthInfra(authInfra);
        user.setAuthLicense(authLicense);
        user.setAuthQuotation(authQuotation);
        user.setAuthWorkPlan(authWorkPlan);
        user.setAuthDocument(authDocument);
        user.setAuthContract(authContract);
        user.setAuthPerformance(authPerformance);
        user.setEnabled(true);

        userRepository.save(user);

        log.info("회원 승인 완료 - userid: {}, 권한: D:{}/P:{}/Per:{}/I:{}/L:{}/Q:{}/WP:{}/Doc:{}/Con:{}/Perf:{}",
                 user.getUserid(), authDashboard, authProject, authPerson, authInfra, authLicense, authQuotation,
                 authWorkPlan, authDocument, authContract, authPerformance);
        
        logService.log("회원관리", "승인", user.getUserid() + " 가입 승인 완료");
        
        return "redirect:/admin/users";
    }

    /**
     * 회원 정보 수정
     */
    @PostMapping("/update")
    public String updateUser(
            @RequestParam("userSeq") Long userSeq,
            @RequestParam("deptNm") String deptNm,
            @RequestParam("teamNm") String teamNm,
            @RequestParam("tel") String tel,
            @RequestParam(value = "mobile", required = false) String mobile,
            @RequestParam("email") String email,
            @RequestParam(value = "positionTitle", required = false) String positionTitle,
            @RequestParam(value = "address", required = false) String address,
            @RequestParam(value = "ssn", required = false) String ssn,
            @RequestParam(value = "certificate", required = false) String certificate,
            @RequestParam(value = "techGrade", required = false) String techGrade,
            @RequestParam(value = "tasks", required = false) String tasks,
            @RequestParam("authDashboard") String authDashboard,
            @RequestParam("authProject") String authProject,
            @RequestParam("authPerson") String authPerson,
            @RequestParam("authInfra") String authInfra,
            @RequestParam(value = "authLicense", required = false, defaultValue = "NONE") String authLicense,
            @RequestParam(value = "authQuotation", required = false, defaultValue = "NONE") String authQuotation,
            @RequestParam(value = "authWorkPlan", required = false, defaultValue = "NONE") String authWorkPlan,
            @RequestParam(value = "authDocument", required = false, defaultValue = "NONE") String authDocument,
            @RequestParam(value = "authContract", required = false, defaultValue = "NONE") String authContract,
            @RequestParam(value = "authPerformance", required = false, defaultValue = "NONE") String authPerformance) {

        log.info("회원 정보 수정 요청 - userSeq: {}", userSeq);

        // ✅ 관리자 권한 체크
        checkAdminAuth();

        User user = userRepository.findById(userSeq)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음 ID: " + userSeq));

        user.setDeptNm(deptNm);
        user.setTeamNm(teamNm);
        user.setTel(tel);
        user.setMobile(mobile);
        user.setEmail(email);
        user.setPositionTitle(positionTitle);
        user.setAddress(address);
        user.setSsn(ssn);
        user.setCertificate(certificate);
        user.setTechGrade(techGrade);
        user.setTasks(tasks);
        user.setAuthDashboard(authDashboard);
        user.setAuthProject(authProject);
        user.setAuthPerson(authPerson);
        user.setAuthInfra(authInfra);
        user.setAuthLicense(authLicense);
        user.setAuthQuotation(authQuotation);
        user.setAuthWorkPlan(authWorkPlan);
        user.setAuthDocument(authDocument);
        user.setAuthContract(authContract);
        user.setAuthPerformance(authPerformance);

        userRepository.save(user);

        log.info("회원 정보 수정 완료 - userid: {}", user.getUserid());
        
        logService.log("회원관리", "수정", "관리자가 사용자 정보 수정: " + user.getUserid());

        return "redirect:/admin/users";
    }

    /**
     * 회원 삭제
     */
    @PostMapping("/delete")
    public String deleteUser(@RequestParam("userSeq") Long userSeq) {
        
        log.warn("회원 삭제 요청 - userSeq: {}", userSeq);
        
        // ✅ 관리자 권한 체크
        checkAdminAuth();
        
        User user = userRepository.findById(userSeq).orElse(null);
        String userInfo = (user != null) ? user.getUserid() + "(" + user.getUsername() + ")" : "Unknown";

        userRepository.deleteById(userSeq);
        
        log.info("회원 삭제 완료 - {}", userInfo);
        
        logService.log("회원관리", "삭제", "사용자 삭제 완료: " + userInfo);

        return "redirect:/admin/users";
    }
}
