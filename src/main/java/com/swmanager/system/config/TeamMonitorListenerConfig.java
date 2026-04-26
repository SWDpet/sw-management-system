package com.swmanager.system.config;

import com.swmanager.system.service.teammonitor.TeamMonitorService;
import com.swmanager.system.service.teammonitor.TeamMonitorSessionListener;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * HttpSessionListener 등록 (sprint team-monitor-dashboard, codex v2 보완 S3).
 *
 * @WebMvcConfigurer 가 아닌 ServletListenerRegistrationBean 으로 등록해야
 * Spring Boot 가 listener 를 servlet container 에 정상 등록.
 */
@Configuration
public class TeamMonitorListenerConfig {

    @Bean
    public ServletListenerRegistrationBean<TeamMonitorSessionListener> teamMonitorSessionListener(
            TeamMonitorService service) {
        return new ServletListenerRegistrationBean<>(new TeamMonitorSessionListener(service));
    }
}
