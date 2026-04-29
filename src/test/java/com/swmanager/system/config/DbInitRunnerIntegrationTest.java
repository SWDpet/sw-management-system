package com.swmanager.system.config;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * NFR-3(d) 게이트 — Java splitter 출력 → JDBC 순차 실행 검증 (T-C).
 *
 * <p>환경: ephemeral PG 클러스터 (localhost:25880). 클러스터 미기동 시 skip.
 * <p>본 테스트는 sw_test2 DB 를 매 회 DROP/CREATE 후 phase1 → sigungu → 'phase2 DDL only' →
 * shim → phase2 전체 멱등 재실행 으로 검증 (exec plan v3.2 §7-3 D-3 패턴).
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DbInitRunnerIntegrationTest {

    private static final String JDBC_URL = "jdbc:postgresql://localhost:25880/sw_test2";
    private static final String JDBC_ADMIN_URL = "jdbc:postgresql://localhost:25880/postgres";
    private static final String DB_USER = "postgres";
    private static final String DB_PASS = System.getenv().getOrDefault("PGPASSWORD", "ephemeral");

    private static JdbcTemplate jdbc;
    private static JdbcTemplate adminJdbc;

    @BeforeAll
    static void setup() throws Exception {
        adminJdbc = newJdbc(JDBC_ADMIN_URL);
        try {
            adminJdbc.queryForObject("SELECT 1", Integer.class);
        } catch (Exception e) {
            Assumptions.abort("ephemeral PG (localhost:25880) 미기동 — 통합테스트 skip");
        }
        adminJdbc.execute("DROP DATABASE IF EXISTS sw_test2");
        adminJdbc.execute("CREATE DATABASE sw_test2");
        jdbc = newJdbc(JDBC_URL);

        execSqlFile(jdbc, "db_init_phase1.sql");
        execSqlFile(jdbc, "db_init_phase1_sigungu.sql");

        String phase2 = loadResource("db_init_phase2.sql");
        List<String> phase2Stmts = DbInitRunner.splitSqlStatements(phase2);
        int firstInsertIdx = phase2Stmts.size();
        for (int i = 0; i < phase2Stmts.size(); i++) {
            if (phase2Stmts.get(i).toUpperCase().startsWith("INSERT INTO")) {
                firstInsertIdx = i;
                break;
            }
        }
        for (int i = 0; i < firstInsertIdx; i++) {
            jdbc.execute(phase2Stmts.get(i));
        }
        execSqlFile(jdbc, "ephemeral/V018_constraints_only.sql");
    }

    @AfterAll
    static void teardown() {
        if (adminJdbc != null) {
            try { adminJdbc.execute("DROP DATABASE IF EXISTS sw_test2"); } catch (Exception ignored) {}
        }
    }

    @Test
    @Order(1)
    void tC_phase2FullReplayHasZeroErrors() throws Exception {
        String phase2 = loadResource("db_init_phase2.sql");
        List<String> stmts = DbInitRunner.splitSqlStatements(phase2);
        int errors = 0;
        StringBuilder firstErr = new StringBuilder();
        for (String stmt : stmts) {
            try {
                jdbc.execute(stmt);
            } catch (Exception e) {
                errors++;
                if (firstErr.length() == 0) {
                    String preview = stmt.length() > 120 ? stmt.substring(0, 120) + "..." : stmt;
                    firstErr.append("first error stmt=[").append(preview)
                            .append("] msg=").append(e.getMessage());
                }
            }
        }
        assertEquals(0, errors,
                "phase2 멱등 재실행 errors=0 기대 (D-3: 첫 INSERT 직전까지 DDL + shim setup 후). "
                        + firstErr);
    }

    @Test
    @Order(2)
    void tC_fourConstraintsAppliedAfterRun() {
        Integer cnt = jdbc.queryForObject(
                "SELECT COUNT(*) FROM pg_constraint WHERE conname IN "
                        + "('fk_tb_document_org_unit','ck_tb_document_support_target_type',"
                        + "'ck_tb_document_environment','fk_tb_document_region_code')",
                Integer.class);
        assertEquals(4, cnt,
                "4개 제약 모두 적용 — DO 블록이 splitter 에서 단일 statement 로 보존됨");
    }

    private static JdbcTemplate newJdbc(String url) {
        DriverManagerDataSource ds = new DriverManagerDataSource(url, DB_USER, DB_PASS);
        ds.setDriverClassName("org.postgresql.Driver");
        return new JdbcTemplate(ds);
    }

    private static void execSqlFile(JdbcTemplate j, String name) throws Exception {
        String sql = loadResource(name);
        for (String stmt : DbInitRunner.splitSqlStatements(sql)) {
            j.execute(stmt);
        }
    }

    private static String loadResource(String name) throws Exception {
        ClassPathResource r = new ClassPathResource(name);
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(r.getInputStream(), StandardCharsets.UTF_8))) {
            return br.lines().collect(Collectors.joining("\n"));
        }
    }
}
