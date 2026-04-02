package com.swmanager.system.controller;

import com.swmanager.system.domain.User;
import com.swmanager.system.repository.UserRepository;
import com.swmanager.system.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class SignupController {
    @Autowired private UserRepository userRepository;
    @Autowired private BCryptPasswordEncoder passwordEncoder;
    @Autowired private LogService logService;

    @GetMapping("/signup")
    public String signupForm() { return "signup"; }

    @PostMapping("/signup")
    public String register(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEnabled(false);
        user.setUserRole("ROLE_USER");
        user.setAuthDashboard("NONE");
        user.setAuthProject("NONE");
        user.setAuthPerson("NONE");
        
        userRepository.save(user);

        logService.log("회원가입", "신청", user.getUserid() + "(" + user.getUsername() + ") 가입신청");
        
        return "redirect:/login?msg=pending";
    }
}