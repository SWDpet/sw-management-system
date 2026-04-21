package com.swmanager.system.controller;

import com.swmanager.system.constant.enums.AccessActionType;
import com.swmanager.system.constants.MenuName;
import com.swmanager.system.domain.User;
import com.swmanager.system.config.CustomUserDetails;
import com.swmanager.system.repository.UserRepository;
import com.swmanager.system.service.LogService;
import com.swmanager.system.exception.InsufficientPermissionException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
    @Autowired private com.swmanager.system.i18n.MessageResolver messages;
    
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
            @RequestParam(value = "expand", required = false) String expand,
            Model model) {
        
        log.info("=== 회원 관리 페이지 접근 ===");
        // [감사 P3 5-4] 키워드 원문 노출 방지 — 길이만 기록
        log.info("Page: {}, 검색 타입: {}, 키워드 길이: {}", page, searchType,
                keyword != null ? keyword.length() : 0);
        
        // ✅ 관리자 권한 체크
        checkAdminAuth();
        
        // 승인 대기 사용자 (페이징 없음)
        List<User> pendingUsers = userRepository.findByEnabledFalse();
        
        // 페이징 설정 (10개씩, 등록일 최신순)
        Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("regDt").descending());
        
        Page<User> activeUserPage;
        
        // 검색 처리 (페이징 적용)
        if (keyword != null && !keyword.trim().isEmpty() && searchType != null) {
            // [감사 P3 5-4] 키워드 원문 로그 금지 — DEBUG 레벨 + 길이만 기록
            log.debug("검색 수행 - 타입: {}, 키워드 길이: {}", searchType,
                    keyword != null ? keyword.length() : 0);
            
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

        // [스프린트 6] expand 쉼표 리스트 파싱 → Set<Long> 으로 Thymeleaf 전달
        java.util.Set<Long> expandIds = new java.util.HashSet<>();
        if (expand != null && !expand.isBlank()) {
            for (String s : expand.split(",")) {
                try { expandIds.add(Long.parseLong(s.trim())); }
                catch (NumberFormatException ignored) {}
            }
        }
        model.addAttribute("expandIds", expandIds);
        model.addAttribute("expandCsv", expandIds.isEmpty() ? "" :
                expandIds.stream().map(String::valueOf).reduce((a, b) -> a + "," + b).orElse(""));

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
                .orElseThrow(() -> new IllegalArgumentException(messages.get("error.user.not_found", userSeq)));

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
        
        logService.log(MenuName.USER, AccessActionType.APPROVE, user.getUserid() + " 가입 승인 완료");
        
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
            @RequestParam(value = "authPerformance", required = false, defaultValue = "NONE") String authPerformance,
            @RequestParam(value = "expand", required = false) String expand,
            @RequestParam(value = "page", required = false) Integer page) {

        log.info("회원 정보 수정 요청 - userSeq: {}", userSeq);

        // ✅ 관리자 권한 체크
        checkAdminAuth();

        User user = userRepository.findById(userSeq)
                .orElseThrow(() -> new IllegalArgumentException(messages.get("error.user.not_found", userSeq)));

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

        logService.log(MenuName.USER, AccessActionType.UPDATE, "관리자가 사용자 정보 수정: " + user.getUserid());

        // [스프린트 6] 펼침 상태·페이지 유지 — 안전한 쿼리 조립
        java.util.List<String> parts = new java.util.ArrayList<>();
        if (page != null) parts.add("page=" + page);
        if (expand != null && !expand.isBlank()) {
            java.util.List<String> ids = new java.util.ArrayList<>();
            for (String s : expand.split(",")) {
                try { ids.add(Long.valueOf(s.trim()).toString()); }
                catch (NumberFormatException ignored) {}
            }
            if (!ids.isEmpty()) parts.add("expand=" + String.join(",", ids));
        }
        String target = "redirect:/admin/users" + (parts.isEmpty() ? "" : "?" + String.join("&", parts));
        return target;
    }

    /**
     * [스프린트 6 FR-3-C] 민감정보 필드별 평문 조회 API (ADMIN 전용, 감사 로그 기록).
     * 필드 1개만 반환하여 일괄 수집을 차단.
     */
    @ResponseBody
    @GetMapping("/api/{userSeq}/sensitive")
    public ResponseEntity<?> getSensitiveField(
            @PathVariable Long userSeq,
            @RequestParam String field) {
        checkAdminAuth();
        User u = userRepository.findById(userSeq).orElse(null);
        if (u == null) {
            return ResponseEntity.status(404).body(java.util.Map.of(
                    "success", false,
                    "error", java.util.Map.of("code", "NOT_FOUND", "message", "사용자 없음")));
        }
        String value;
        switch (field) {
            case "ssn":     value = u.getSsn(); break;
            case "tel":     value = u.getTel(); break;
            case "mobile":  value = u.getMobile(); break;
            case "email":   value = u.getEmail(); break;
            case "address": value = u.getAddress(); break;
            default:
                return ResponseEntity.badRequest().body(java.util.Map.of(
                        "success", false,
                        "error", java.util.Map.of("code", "INVALID_FIELD", "message", "허용되지 않는 필드")));
        }
        CustomUserDetails cu = getCurrentUser();
        String actor = (cu != null && cu.getUser() != null) ? cu.getUser().getUsername() : "unknown";
        logService.log(MenuName.USER, AccessActionType.SENSITIVE_VIEW,
                "관리자 " + actor + " → userSeq=" + userSeq + " field=" + field);
        java.util.Map<String, Object> body = new java.util.LinkedHashMap<>();
        body.put("field", field);
        body.put("value", value != null ? value : "");
        return ResponseEntity.ok(body);
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
        
        logService.log(MenuName.USER, AccessActionType.DELETE, "사용자 삭제 완료: " + userInfo);

        return "redirect:/admin/users";
    }
}
