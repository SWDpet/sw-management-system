package com.swmanager.system.config;

import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * TeamMonitorSecurityHeadersFilter 등록.
 * (sprint team-monitor-dashboard, 개발계획 Step 4-3 — codex v1 #2 / v3 E2)
 *
 * URL 패턴은 정확히 2개만 매칭 — 다른 admin 페이지에 헤더 누출 방지.
 * order 는 SecurityFilterChain 직후 보장.
 */
@Configuration
public class TeamMonitorFilterConfig {

    @Bean
    public FilterRegistrationBean<TeamMonitorSecurityHeadersFilter> teamMonitorSecurityHeadersFilter(
            TeamMonitorProperties props) {
        FilterRegistrationBean<TeamMonitorSecurityHeadersFilter> reg = new FilterRegistrationBean<>();
        reg.setFilter(new TeamMonitorSecurityHeadersFilter(props));
        reg.addUrlPatterns(
                "/admin/team-monitor",
                "/admin/team-monitor/stream"
        );
        // E2: SecurityFilterChain 직후 보장
        reg.setOrder(SecurityProperties.DEFAULT_FILTER_ORDER + 1);
        reg.setName("teamMonitorSecurityHeadersFilter");
        return reg;
    }
}
