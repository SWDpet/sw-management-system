package com.swmanager.system.controller;

import com.swmanager.system.config.CustomUserDetails;  // ✅ config 패키지!
import com.swmanager.system.domain.User;
import com.swmanager.system.repository.UserRepository;
import com.swmanager.system.service.LogService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 마이페이지 Controller
 * SecurityContext 직접 사용 버전
 */
@Slf4j
@Controller
@RequestMapping("/mypage")
public class MyPageController {

    @Autowired private UserRepository userRepository;
    @Autowired private LogService logService;
    @Autowired private PasswordEncoder passwordEncoder;

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
     * 마이페이지 조회
     */
    @GetMapping
    public String myPage(Model model) {
        
        log.info("=== 마이페이지 접근 ===");
        
        CustomUserDetails currentUser = getCurrentUser();
        
        if (currentUser == null) {
            log.warn("미로그인 사용자의 마이페이지 접근 시도");
            return "redirect:/login";
        }
        
        User user = userRepository.findById(currentUser.getUser().getUserSeq())
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보가 없습니다."));
        
        model.addAttribute("user", user);
        
        log.info("마이페이지 조회 - 사용자: {}", user.getUserid());
        
        return "mypage";
    }

    /**
     * 개인정보 수정
     */
    @PostMapping("/update")
    public String updateMyInfo(
            @RequestParam("deptNm") String deptNm,
            @RequestParam("teamNm") String teamNm,
            @RequestParam("tel") String tel,
            @RequestParam("email") String email,
            RedirectAttributes rttr) {

        log.info("개인정보 수정 요청");

        CustomUserDetails currentUser = getCurrentUser();

        if (currentUser == null) {
            log.warn("미로그인 사용자의 개인정보 수정 시도");
            return "redirect:/login";
        }

        User user = userRepository.findById(currentUser.getUser().getUserSeq())
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보가 없습니다."));

        user.setDeptNm(deptNm);
        user.setTeamNm(teamNm);
        user.setTel(tel);
        user.setEmail(email);
        // 권한(authDashboard, authProject, authPerson)은 관리자만 변경 가능 — 사용자 입력 무시

        // 관리자는 즉시 수정, 일반 사용자는 재승인 요청(비활성화)
        if ("ROLE_ADMIN".equals(user.getUserRole())) {
            user.setEnabled(true);
            userRepository.save(user);

            log.info("관리자 정보 수정 완료 - userid: {}", user.getUserid());
            logService.log("마이페이지", "정보수정", "관리자(" + user.getUserid() + ") 정보 수정 완료");

            rttr.addFlashAttribute("successMessage", "정보가 수정되었습니다.");
            return "redirect:/";
        } else {
            user.setEnabled(false);
            userRepository.save(user);

            log.info("일반 사용자 정보 수정 및 재승인 요청 - userid: {}", user.getUserid());
            logService.log("마이페이지", "승인요청", user.getUserid() + " 정보 수정 및 권한 요청");

            rttr.addFlashAttribute("infoMessage", "정보가 수정되었습니다. 관리자 승인 후 다시 로그인해주세요.");
            return "redirect:/logout";
        }
    }

    /**
     * 비밀번호 변경
     */
    @PostMapping("/password")
    public String updatePassword(
            @RequestParam("currentPw") String currentPw,
            @RequestParam("newPw") String newPw,
            @RequestParam("confirmPw") String confirmPw,
            RedirectAttributes rttr) {

        log.info("비밀번호 변경 요청");
        
        CustomUserDetails currentUser = getCurrentUser();
        
        if (currentUser == null) {
            log.warn("미로그인 사용자의 비밀번호 변경 시도");
            return "redirect:/login";
        }

        User user = userRepository.findById(currentUser.getUser().getUserSeq())
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보가 없습니다."));

        // 1. 현재 비밀번호 일치 확인
        if (!passwordEncoder.matches(currentPw, user.getPassword())) {
            log.warn("현재 비밀번호 불일치 - userid: {}", user.getUserid());
            rttr.addFlashAttribute("errorMessage", "현재 비밀번호가 일치하지 않습니다.");
            return "redirect:/mypage";
        }

        // 2. 새 비밀번호 확인 일치 여부
        if (!newPw.equals(confirmPw)) {
            log.warn("새 비밀번호 확인 불일치 - userid: {}", user.getUserid());
            rttr.addFlashAttribute("errorMessage", "새 비밀번호와 확인 비밀번호가 일치하지 않습니다.");
            return "redirect:/mypage";
        }

        // 3. 비밀번호 변경 (암호화)
        user.setPassword(passwordEncoder.encode(newPw));
        userRepository.save(user);

        log.info("비밀번호 변경 성공 - userid: {}", user.getUserid());
        logService.log("마이페이지", "비번변경", "사용자(" + user.getUserid() + ") 비밀번호 변경 완료");
        
        rttr.addFlashAttribute("successMessage", "비밀번호가 성공적으로 변경되었습니다.");

        return "redirect:/mypage";
    }
}
