package com.swmanager.system.service;

import com.swmanager.system.domain.AccessLog;
import com.swmanager.system.config.CustomUserDetails;
import com.swmanager.system.repository.AccessLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Service
public class LogService {

    @Autowired
    private AccessLogRepository accessLogRepository;

    public void log(String menuNm, String actionType, String detail) {
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            String ip = request.getRemoteAddr();
            
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String userid = "anonymous";
            String username = "Unknown";

            if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
                CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
                userid = user.getUsername(); // CustomUserDetails에서 getUsername()은 userid 반환
                username = user.getUser().getUsername(); // 실명
            } else if (auth != null) {
                userid = auth.getName();
            }

            AccessLog log = new AccessLog();
            log.setUserid(userid);
            log.setUsername(username);
            log.setIpAddr(ip);
            log.setMenuNm(menuNm);
            log.setActionType(actionType);
            log.setActionDetail(detail);
            
            accessLogRepository.save(log);

        } catch (Exception e) {
            log.error("로그 저장 실패: {}", e.getMessage());
        }
    }
}