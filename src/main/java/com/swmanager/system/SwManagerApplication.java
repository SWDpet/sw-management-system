package com.swmanager.system; // ※ 패키지명은 기존 파일에 적힌 그대로 유지하세요.

import com.swmanager.system.config.SecurityLoginProperties;
import com.swmanager.system.config.TeamMonitorProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.env.Environment;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.IOException;
import java.util.Arrays;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({SecurityLoginProperties.class, TeamMonitorProperties.class})
@ComponentScan(
        basePackages = "com.swmanager.system",
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.CUSTOM,
                classes = SwManagerApplication.TeamMonitorProdExclusionFilter.class
        )
)
public class SwManagerApplication extends SpringBootServletInitializer {

    // [WAR 배포 핵심 설정]
    // 외부 톰캣에서 실행될 때 이 메서드를 통해 애플리케이션을 시작합니다.
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(SwManagerApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(SwManagerApplication.class, args);
    }

    /**
     * prod profile 활성 시 team-monitor 관련 컴포넌트를 ComponentScan 에서 제외.
     * team-monitor 는 개발자 전용 도구로 운영 WAR 에 불필요 (sprint team-monitor-dashboard).
     * dev/local profile (default) 에서는 모두 정상 스캔.
     * 활성화: 톰캣 setenv 에 `-Dspring.profiles.active=prod` 또는 `SPRING_PROFILES_ACTIVE=prod`.
     * TeamMonitorProperties 는 @EnableConfigurationProperties 로 명시 등록되어 본 필터 영향 X.
     */
    public static class TeamMonitorProdExclusionFilter implements TypeFilter, EnvironmentAware {

        private Environment environment;

        @Override
        public void setEnvironment(Environment environment) {
            this.environment = environment;
        }

        @Override
        public boolean match(MetadataReader reader, MetadataReaderFactory factory) throws IOException {
            if (environment == null) return false;
            boolean prodActive = Arrays.asList(environment.getActiveProfiles()).contains("prod");
            if (!prodActive) return false;
            String name = reader.getClassMetadata().getClassName();
            return name.startsWith("com.swmanager.system.service.teammonitor.")
                    || name.equals("com.swmanager.system.config.TeamMonitorFilterConfig")
                    || name.equals("com.swmanager.system.config.TeamMonitorListenerConfig")
                    || name.equals("com.swmanager.system.config.TeamMonitorSecurityHeadersFilter")
                    || name.equals("com.swmanager.system.controller.TeamMonitorController")
                    || name.equals("com.swmanager.system.controller.TeamMonitorAdvice")
                    || name.equals("com.swmanager.system.actuator.TeamMonitorInfoContributor");
        }
    }
}
