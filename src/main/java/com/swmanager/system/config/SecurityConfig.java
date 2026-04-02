package com.swmanager.system.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
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

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
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
