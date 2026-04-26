package com.swmanager.system.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;

/**
 * Spring Security 설정
 *
 * 최종 안정화 버전:
 * - usernameParameter("userid") 적용
 * - sessionManagement: 세션 고정 공격 방어(migrateSession) 적용
 * - logout GET/POST 모두 지원
 * - 로그인 시도 제한 핸들러 적용
 * - /admin/** ADMIN 전용, @EnableMethodSecurity 활성화
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomAuthenticationFailureHandler authFailureHandler;
    private final CustomAuthenticationSuccessHandler authSuccessHandler;

    /**
     * /actuator/** 전용 시큐리티 체인 (sprint team-monitor-wildcard-watcher — R-12 / N10 / N13 / SEC-01).
     *
     * <p>main chain (filterChain) 의 anyRequest().authenticated() + formLogin 이 actuator path 도
     * 잡으면서 302 redirect 가 발생하는 충돌 → 별도 chain @Order(HIGHEST_PRECEDENCE) + main chain 에서
     * actuator path 명시 제외 (permitAll) 로 책임 분리.
     *
     * <p>적용 결과:
     * <ul>
     *   <li>/actuator/health: permitAll (모니터링 도구 익명 접근)
     *   <li>/actuator/info: ROLE_ADMIN — 익명 = 401 (httpBasic + formLogin disable, 302 redirect 차단)
     *   <li>비ADMIN = 403
     * </ul>
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain actuatorChain(HttpSecurity http) throws Exception {
        // Custom entry point — 응답 즉시 commit (main chain 의 후속 filter 영향 차단)
        org.springframework.security.web.AuthenticationEntryPoint actuatorEntryPoint =
                (request, response, authException) -> {
                    response.setHeader("WWW-Authenticate", "Basic realm=\"actuator\"");
                    response.setStatus(401);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"status\":401,\"error\":\"Unauthorized\"}");
                    response.getWriter().flush();
                };

        http
            .securityMatcher("/actuator/**")
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                .requestMatchers("/actuator/info").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .httpBasic(b -> b.authenticationEntryPoint(actuatorEntryPoint))
            .exceptionHandling(ex -> ex
                    .authenticationEntryPoint(actuatorEntryPoint)
                    .accessDeniedHandler((request, response, ex2) -> {
                        // 비ADMIN — 403 명시 (302 차단)
                        response.setStatus(403);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write("{\"status\":403,\"error\":\"Forbidden\"}");
                        response.getWriter().flush();
                    }))
            .formLogin(form -> form.disable())
            .logout(logout -> logout.disable())
            .csrf(csrf -> csrf.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // [스프린트 5 v2] JSON API 경로는 CSRF 면제 (세션 쿠키 기반 인증).
            // 폼 로그인·로그아웃은 기본 CSRF 보호 유지.
            .csrf(csrf -> csrf
                .ignoringRequestMatchers(
                    "/api/**",
                    "/admin/api/**",
                    "/document/api/**",
                    "/admin/api/org-units/**"
                )
            )
            .authorizeHttpRequests(auth -> auth
                // 인증 없이 접근 가능한 경로
                .requestMatchers("/login", "/signup", "/logout").permitAll()
                .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico", "/favicon.png").permitAll()

                // 관리자 전용 경로
                .requestMatchers("/admin/**").hasRole("ADMIN")

                // 모든 주요 경로는 인증 필요
                .requestMatchers("/").authenticated()
                .requestMatchers("/dashboard/**").authenticated()
                .requestMatchers("/projects/**").authenticated()
                .requestMatchers("/infra/**").authenticated()
                .requestMatchers("/person/**").authenticated()
                .requestMatchers("/mypage/**").authenticated()
                .requestMatchers("/logs/**").authenticated()

                // 업무계획 및 성과 전산화 모듈
                .requestMatchers("/contract/**").authenticated()
                .requestMatchers("/workplan/**").authenticated()
                .requestMatchers("/document/**").authenticated()
                .requestMatchers("/performance/**").authenticated()

                // 그 외 모든 요청은 인증 필요
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("userid")
                .passwordParameter("password")
                .successHandler(authSuccessHandler)
                .failureHandler(authFailureHandler)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID", "SWMANAGER_SESSION")
                .permitAll()
            )
            .sessionManagement(session -> session
                // 로그인 시 세션 ID를 새로 발급하여 세션 고정 공격 방어
                .sessionFixation().migrateSession()
                // 동시 세션 제한: 1계정 1세션 (중복 로그인 차단)
                .maximumSessions(1)
                .expiredUrl("/login?expired=true")
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
