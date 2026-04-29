package com.swmanager.system.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(20)
public class InspectDocNoMigrationRunner implements ApplicationRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        int totalUpdated = 0;
        // 1) 월포함(INSP-YYYY-MM-id) → 년만(INSP-YYYY-id) 단순화
        totalUpdated += simplifyMonthFormat();
        // 2) 살아있는 inspect_report 기준 (구포맷 INSP-id → INSP-YYYY-id)
        totalUpdated += migrateToYearFormat("inspect_report");
        // 3) 4/21 백업 테이블 기준 (orphan INSP 행 보완)
        totalUpdated += migrateToYearFormat("inspect_report_backup_20260421_235813");
        if (totalUpdated > 0) {
            log.info("INSP docNo 마이그레이션 완료: 총 {} 건을 INSP-YYYY-id 포맷으로 갱신", totalUpdated);
        } else {
            log.info("INSP docNo 마이그레이션: 갱신 대상 없음 (이미 모두 신포맷)");
        }
    }

    /** INSP-YYYY-MM-id → INSP-YYYY-id 단순화 (월 segment 제거) */
    private int simplifyMonthFormat() {
        try {
            String sql =
                "UPDATE tb_document " +
                "SET doc_no = REGEXP_REPLACE(doc_no, '^INSP-([0-9]{4})-[0-9]{2}-([0-9]+)$', 'INSP-\\1-\\2') " +
                "WHERE doc_type = 'INSPECT' " +
                "  AND doc_no ~ '^INSP-[0-9]{4}-[0-9]{2}-[0-9]+$'";
            int n = jdbcTemplate.update(sql);
            if (n > 0) log.info("[INSP 마이그] 월포함→년만 단순화: {} 건", n);
            return n;
        } catch (Exception e) {
            Throwable cause = e;
            while (cause.getCause() != null && cause.getCause() != cause) cause = cause.getCause();
            log.warn("[INSP 마이그] simplify 실패: {}", cause.getMessage());
            return 0;
        }
    }

    /** 구포맷 INSP-{id} → INSP-{year}-{id} (sourceTable 의 inspect_month 에서 year 추출) */
    private int migrateToYearFormat(String sourceTable) {
        try {
            // sourceTable 은 코드 내 하드코딩 식별자 (화이트리스트). SQL injection 우려 없음.
            String sql =
                "UPDATE tb_document d " +
                "SET doc_no = 'INSP-' || SUBSTRING(ir.inspect_month, 1, 4) || '-' || ir.id::text " +
                "FROM " + sourceTable + " ir " +
                "WHERE d.doc_type = 'INSPECT' " +
                "  AND ir.inspect_month IS NOT NULL " +
                "  AND LENGTH(ir.inspect_month) >= 4 " +
                "  AND d.doc_no = 'INSP-' || ir.id::text";
            int n = jdbcTemplate.update(sql);
            if (n > 0) log.info("[INSP 마이그] {} 에서 INSP-id → INSP-YYYY-id : {} 건", sourceTable, n);
            return n;
        } catch (Exception e) {
            Throwable cause = e;
            while (cause.getCause() != null && cause.getCause() != cause) cause = cause.getCause();
            log.warn("[INSP 마이그] {} 실패 (무시): {}", sourceTable, cause.getMessage());
            return 0;
        }
    }
}
