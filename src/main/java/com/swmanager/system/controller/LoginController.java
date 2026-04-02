package com.swmanager.system.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            @RequestParam(value = "locked", required = false) String locked,
                            @RequestParam(value = "minutes", required = false) String minutes,
                            @RequestParam(value = "expired", required = false) String expired,
                            Model model) {
        if (error != null) {
            if ("true".equals(locked)) {
                String min = (minutes != null) ? minutes : "15";
                model.addAttribute("loginError",
                        "로그인 시도 횟수 초과로 계정이 잠겼습니다. 약 " + min + "분 후 다시 시도해주세요.");
            } else {
                model.addAttribute("loginError", "아이디 또는 비밀번호가 틀렸거나 승인 대기 중입니다.");
            }
        }
        if (logout != null) {
            model.addAttribute("logoutMsg", "로그아웃 되었습니다.");
        }
        if ("true".equals(expired)) {
            model.addAttribute("loginError", "다른 곳에서 로그인하여 현재 세션이 만료되었습니다. 다시 로그인해주세요.");
        }
        return "login";
    }

    @GetMapping("/login/type/{mode}")
    public String loginModePage(@PathVariable("mode") String mode, Model model) {
        model.addAttribute("mode", mode);
        return "login";
    }
}