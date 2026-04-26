package com.swmanager.system.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * team-monitor 페이지/스트림 전용 보안 헤더 적용 필터.
 * (sprint team-monitor-dashboard, 개발계획 Step 4-3 — 기획 §4-6)
 *
 * 등록은 TeamMonitorFilterConfig 의 FilterRegistrationBean (URL 패턴 2개 한정).
 * SecurityFilterChain 직접 삽입 금지 (codex v1 보완 #2).
 */
public class TeamMonitorSecurityHeadersFilter extends OncePerRequestFilter {

    private static final String DEFAULT_CSP =
            "default-src 'self'; script-src 'self'; style-src 'self'; "
            + "font-src 'self' data:; connect-src 'self'; img-src 'self' data:; "
            + "frame-ancestors 'none'; base-uri 'self'; form-action 'self'";

    private final TeamMonitorProperties props;
    @Autowired(required = false) private Environment env;

    public TeamMonitorSecurityHeadersFilter(TeamMonitorProperties props) {
        this.props = props;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                     HttpServletResponse resp,
                                     FilterChain chain) throws ServletException, IOException {
        String csp = props.getSecurity().getCsp();
        if (csp == null || csp.isBlank()) csp = DEFAULT_CSP;
        resp.setHeader("Content-Security-Policy", csp);
        resp.setHeader("X-Content-Type-Options", "nosniff");
        resp.setHeader("X-Frame-Options", "DENY");
        resp.setHeader("Referrer-Policy", "same-origin");
        resp.setHeader("Permissions-Policy", "microphone=(), camera=(), geolocation=()");
        resp.setHeader("Cross-Origin-Opener-Policy", "same-origin");
        resp.setHeader("Cross-Origin-Resource-Policy", "same-origin");

        // HSTS — prod 프로파일 한정 (기획 §4-6 단서)
        if (env != null && env.acceptsProfiles(Profiles.of("prod"))) {
            resp.setHeader("Strict-Transport-Security",
                    "max-age=31536000; includeSubDomains");
        }

        chain.doFilter(req, resp);
    }
}
