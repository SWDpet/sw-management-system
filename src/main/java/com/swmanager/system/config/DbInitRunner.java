package com.swmanager.system.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * 서버 시작 시 필요한 DB 테이블 자동 생성
 * CREATE TABLE IF NOT EXISTS 사용으로 멱등성 보장
 */
@Slf4j
@Component
public class DbInitRunner implements ApplicationRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        try {
            ClassPathResource resource = new ClassPathResource("db_init_phase2.sql");
            if (!resource.exists()) {
                log.info("db_init_phase2.sql 파일 없음 - 스킵");
                return;
            }

            String sql;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                sql = reader.lines().collect(Collectors.joining("\n"));
            }

            // 주석 제거 후 세미콜론으로 분리하여 개별 실행
            // 한줄 주석(--) 제거
            StringBuilder cleanSql = new StringBuilder();
            for (String line : sql.split("\n")) {
                String trimLine = line.trim();
                if (trimLine.startsWith("--")) continue;
                cleanSql.append(line).append("\n");
            }
            String[] statements = cleanSql.toString().split(";");
            int executed = 0;
            for (String stmt : statements) {
                String trimmed = stmt.trim();
                if (trimmed.isEmpty()) continue;
                try {
                    jdbcTemplate.execute(trimmed);
                    executed++;
                } catch (Exception e) {
                    log.debug("SQL 실행 스킵 (이미 존재하거나 에러): {}", e.getMessage());
                }
            }
            log.info("DB 초기화 완료: {}개 SQL 실행", executed);
        } catch (Exception e) {
            log.warn("DB 초기화 실패 (무시): {}", e.getMessage());
        }
    }
}
