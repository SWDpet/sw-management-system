package com.swmanager.system.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

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
     * /actuator/** 전용 시큐리티 체인 (sprint team-monitor-wildcard-watcher — R-12 / N10 / N13).
     * - /actuator/health: permitAll (모니터링 도구 익명 접근)
     * - /actuator/info: ROLE_ADMIN (teamMetadataFallback 등 내부 상태 노출)
     * - httpBasic 인증 + formLogin disable → 익명 = 401, 비ADMIN = 403 (302 리다이렉트 X)
     * - SEC-01 / R-12-A: 401/403 보장.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain actuatorChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/actuator/**")
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                .requestMatchers("/actuator/info").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .httpBasic(Customizer.withDefaults())
            .formLogin(form -> form.disable())
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
