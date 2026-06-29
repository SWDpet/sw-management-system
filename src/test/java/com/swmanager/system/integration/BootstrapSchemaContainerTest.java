package com.swmanager.system.integration;

import com.swmanager.system.domain.SigunguCode;
import com.swmanager.system.repository.SigunguCodeRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * [beyond-A testcontainers] 운영DB 비접촉 — 신선 postgres:16 에 부트스트랩 스키마(phase1+sigungu+phase2)를
 * 적용하고 JPA 리포지토리 CRUD·파생쿼리가 동작함을 검증. fresh-init-smoke(SQL 적용만)보다 한 단계 깊은
 * 검증(엔티티↔스키마 매핑·쿼리 실행).
 *
 * <p><b>Docker 게이팅</b>: {@code @Testcontainers(disabledWithoutDocker = true)} → Docker 미가용 환경
 * (로컬 회사PC/집/출장)에서는 컨테이너 start 전에 클래스 전체 skip → {@code mvnw verify} 무회귀.
 * Docker 가용(CI ubuntu-latest)에서만 실행. ⚠ 로컬 Docker 부재로 본 테스트 검증은 CI(GitHub Actions)에서 수행.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=none")   // Hibernate 스키마 검증/생성 안 함(부트스트랩 SQL 이 스키마 소유)
@Testcontainers(disabledWithoutDocker = true)
class BootstrapSchemaContainerTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16").withDatabaseName("sw_dept_tc");

    /** 운영 부트스트랩 순서 (psql -f 동등). */
    private static final String[] BOOTSTRAP_SQL = {
            "db_init_phase1.sql", "db_init_phase1_sigungu.sql", "db_init_phase2.sql"
    };

    @BeforeAll
    static void applyBootstrapSchema() throws Exception {
        try (Connection c = DriverManager.getConnection(
                POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())) {
            for (String file : BOOTSTRAP_SQL) {
                String sql;
                try (InputStream in = new ClassPathResource(file).getInputStream()) {   // 스트림 누수 방지
                    sql = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                }
                try (Statement st = c.createStatement()) {
                    // pg JDBC 는 멀티스테이트먼트 + $$ DO 블록을 서버측 처리 → @Sql/ScriptUtils 의 $$ 분리결함 회피.
                    st.execute(sql);
                } catch (Exception e) {
                    // fail-fast: DbInitRunner 처럼 swallow 하지 않음(스키마 결함을 테스트가 명확히 노출).
                    throw new IllegalStateException("부트스트랩 SQL 적용 실패: " + file + " — " + e.getMessage(), e);
                }
            }
        }
    }

    @Autowired
    private SigunguCodeRepository sigunguCodeRepository;

    @Test
    void freshBootstrapSchema_supportsRepositoryCrudAndDerivedQuery() {
        // 부트스트랩(db_init_phase1_sigungu.sql)이 sigungu_code 에 279행 시드 → baseline > 0.
        // (테이블/매핑 부재면 count() 가 예외 → 스키마·매핑 결함 검출. + 시드 적재도 함께 박제.)
        long baseline = sigunguCodeRepository.count();
        assertThat(baseline).isPositive();

        SigunguCode code = new SigunguCode();
        code.setAdmSectC("99999");          // PK — 실제 행정코드(예 51150)와 비충돌 테스트값
        code.setSidoNm("테스트도");          // 실제 시드에 없는 유일 시도명
        code.setSggNm("테스트군");
        code.setInsttC("TC001");
        sigunguCodeRepository.save(code);

        // 1) findById 라운드트립 — 쓰기·읽기·@Column 매핑 검증
        assertThat(sigunguCodeRepository.findById("99999"))
                .isPresent()
                .get()
                .satisfies(found -> {
                    assertThat(found.getSidoNm()).isEqualTo("테스트도");
                    assertThat(found.getSggNm()).isEqualTo("테스트군");
                    assertThat(found.getInsttC()).isEqualTo("TC001");
                });

        // 2) 파생쿼리(findBySidoNm) — Spring Data 쿼리 파생이 실제 스키마에서 동작(유일 시도명이라 1건)
        assertThat(sigunguCodeRepository.findBySidoNm("테스트도"))
                .extracting(SigunguCode::getAdmSectC)
                .containsExactly("99999");

        assertThat(sigunguCodeRepository.count()).isEqualTo(baseline + 1);
    }
}
