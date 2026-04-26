package com.swmanager.system.service.teammonitor;

import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;

/**
 * 세션 만료 시 해당 Principal 의 모든 SSE emitter 종료.
 * (sprint team-monitor-dashboard, 개발계획 Step 3-6 — codex v2 보완 S3)
 *
 * 등록은 TeamMonitorListenerConfig 의 ServletListenerRegistrationBean.
 */
public class TeamMonitorSessionListener implements HttpSessionListener {

    private static final Logger log = LoggerFactory.getLogger(TeamMonitorSessionListener.class);
    private final TeamMonitorService service;

    public TeamMonitorSessionListener(TeamMonitorService service) {
        this.service = service;
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        try {
            Object ctx = se.getSession().getAttribute("SPRING_SECURITY_CONTEXT");
            if (ctx instanceof SecurityContext sc) {
                Authentication auth = sc.getAuthentication();
                if (auth != null) {
                    service.removeEmittersByPrincipal(auth.getName());
                }
            }
        } catch (Exception e) {
            log.debug("세션 만료 처리 중 예외 (무시): {}", e.getMessage());
        }
    }
}
