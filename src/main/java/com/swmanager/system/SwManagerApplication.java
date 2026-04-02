package com.swmanager.system; // ※ 패키지명은 기존 파일에 적힌 그대로 유지하세요.

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
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

}