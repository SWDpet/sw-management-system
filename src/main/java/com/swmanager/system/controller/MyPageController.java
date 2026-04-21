package com.swmanager.system.controller;

import com.swmanager.system.config.CustomUserDetails;  // ✅ config 패키지!
import com.swmanager.system.constant.enums.AccessActionType;
import com.swmanager.system.constants.MenuName;
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
    @Autowired private com.swmanager.system.i18n.MessageResolver messages;
    @Autowired private com.swmanager.system.util.MaskingDetector maskingDetector;  // S3-B

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
                .orElseThrow(() -> new IllegalArgumentException(messages.get("error.user.not_found", currentUser.getUser().getUserSeq())));
        
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
            @RequestParam(value = "mobile", required = false) String mobile,
            @RequestParam("email") String email,
            @RequestParam(value = "positionTitle", required = false) String positionTitle,
            @RequestParam(value = "address", required = false) String address,
            @RequestParam(value = "ssn", required = false) String ssn,
            @RequestParam(value = "certificate", required = false) String certificate,
            @RequestParam(value = "techGrade", required = false) String techGrade,
            @RequestParam(value = "tasks", required = false) String tasks,
            RedirectAttributes rttr) {

        log.info("개인정보 수정 요청");

        CustomUserDetails currentUser = getCurrentUser();

        if (currentUser == null) {
            log.warn("미로그인 사용자의 개인정보 수정 시도");
            return "redirect:/login";
        }

        User user = userRepository.findById(currentUser.getUser().getUserSeq())
                .orElseThrow(() -> new IllegalArgumentException(messages.get("error.user.not_found", currentUser.getUser().getUserSeq())));

        user.setDeptNm(deptNm);
        user.setTeamNm(teamNm);
        user.setPositionTitle(positionTitle);
        user.setCertificate(certificate);
        user.setTechGrade(techGrade);
        user.setTasks(tasks);

        // S3-B: 마스킹 회귀 가드 — 입력값에 마스킹 패턴이 감지되면 DB 기존값 유지
        java.util.List<String> blockedFields = new java.util.ArrayList<>();
        if (maskingDetector.isMaskedTel(tel, user.getTel())) {
            blockedFields.add("tel");
        } else {
            user.setTel(tel);
        }
        if (maskingDetector.isMaskedTel(mobile, user.getMobile())) {
            blockedFields.add("mobile");
        } else {
            user.setMobile(mobile);
        }
        if (maskingDetector.isMaskedEmail(email, user.getEmail())) {
            blockedFields.add("email");
        } else {
            user.setEmail(email);
        }
        if (maskingDetector.isMaskedSsn(ssn, user.getSsn())) {
            blockedFields.add("ssn");
        } else {
            user.setSsn(ssn);
        }
        if (maskingDetector.isMaskedAddress(address, user.getAddress())) {
            blockedFields.add("address");
        } else {
            user.setAddress(address);
        }

        if (!blockedFields.isEmpty()) {
            // 값 절대 미포함, userid + 필드명만 기록 (S3-B 정책)
            log.warn("MASKING_GUARD_BLOCKED: userid={} fields={}", user.getUserid(), blockedFields);
            rttr.addFlashAttribute("warningMessage",
                    "다음 필드는 마스킹된 값이 감지되어 변경되지 않았습니다: " + String.join(", ", blockedFields));
        }
        // 권한(authDashboard, authProject, authPerson)은 관리자만 변경 가능 — 사용자 입력 무시

        // 관리자는 즉시 수정, 일반 사용자는 재승인 요청(비활성화)
        if ("ROLE_ADMIN".equals(user.getUserRole())) {
            user.setEnabled(true);
            userRepository.save(user);

            log.info("관리자 정보 수정 완료 - userid: {}", user.getUserid());
            logService.log(MenuName.MYPAGE, AccessActionType.UPDATE, "관리자(" + user.getUserid() + ") 정보 수정 완료");

            rttr.addFlashAttribute("successMessage", "정보가 수정되었습니다.");
            return "redirect:/";
        } else {
            user.setEnabled(false);
            userRepository.save(user);

            log.info("일반 사용자 정보 수정 및 재승인 요청 - userid: {}", user.getUserid());
            logService.log(MenuName.MYPAGE, AccessActionType.APPROVE, user.getUserid() + " 정보 수정 및 권한 요청");

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
                .orElseThrow(() -> new IllegalArgumentException(messages.get("error.user.not_found", currentUser.getUser().getUserSeq())));

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
        logService.log(MenuName.MYPAGE, AccessActionType.UPDATE, "사용자(" + user.getUserid() + ") 비밀번호 변경 완료");
        
        rttr.addFlashAttribute("successMessage", "비밀번호가 성공적으로 변경되었습니다.");

        return "redirect:/mypage";
    }
}
