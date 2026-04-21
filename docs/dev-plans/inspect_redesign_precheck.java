import java.sql.*;
import java.util.*;

/**
 * S1 inspect-comprehensive-redesign · Step 1 사전검증 러너 (READ ONLY)
 *
 * 실행:
 *   javac -encoding UTF-8 docs/dev-plans/inspect-redesign-precheck.java
 *   DB_PASSWORD=... java -cp ".;<postgresql-jar>" -Dfile.encoding=UTF-8 \
 *     inspect_redesign_precheck > docs/dev-plans/inspect-redesign-precheck-result.md
 *
 * 안전통제: READ ONLY + statement_timeout 10s
 *
 * 검증 Exit Gate 1:
 *  (a) section 분포 (9종 외 or NULL/공백 → HALT)
 *  (b) UPIS_SW 건수
 *  (c) APP 섹션 건수 (A3 영향)
 *  (d) 대상 테이블 row 수 (TRUNCATE 전 스냅샷)
 *  (e) 외부 FK 의존성 (DROP/TRUNCATE CASCADE 위험)
 *  (f) section NULL/공백/변형
 */
public class inspect_redesign_precheck {

    public static void main(String[] args) throws Exception {
        String url  = "jdbc:postgresql://211.104.137.55:5881/SW_Dept";
        String user = "postgres";
        String pass = System.getenv("DB_PASSWORD");
        if (pass == null || pass.isEmpty()) {
            System.err.println("[ERR] DB_PASSWORD 환경변수 없음");
            System.exit(2);
        }
        Class.forName("org.postgresql.Driver");
        boolean halt = false;
        StringBuilder haltReasons = new StringBuilder();

        try (Connection c = DriverManager.getConnection(url, user, pass)) {
            try (Statement s = c.createStatement()) {
                s.execute("SET TRANSACTION READ ONLY");
                s.execute("SET statement_timeout = 10000");
            }

            System.out.println("# inspect-comprehensive-redesign Step 1 — 사전검증 결과");
            System.out.println();
            System.out.println("- 실행시각: " + new java.util.Date());
            System.out.println("- DB: " + url);
            System.out.println("- 안전통제: READ ONLY, statement_timeout=10s");
            System.out.println();

            // ─────────────────────────────────
            // (a) section 분포
            // ─────────────────────────────────
            System.out.println("## (a) section 분포");
            System.out.println();
            System.out.println("### inspect_check_result.section");
            System.out.println("| section | cnt |");
            System.out.println("|---------|----:|");
            runTable(c, "SELECT COALESCE(section,'(NULL)') AS section, COUNT(*) FROM inspect_check_result GROUP BY section ORDER BY section");
            System.out.println();
            System.out.println("### inspect_template.section");
            System.out.println("| section | cnt |");
            System.out.println("|---------|----:|");
            runTable(c, "SELECT COALESCE(section,'(NULL)') AS section, COUNT(*) FROM inspect_template GROUP BY section ORDER BY section");
            System.out.println();

            // ─────────────────────────────────
            // (b) UPIS_SW 건수
            // ─────────────────────────────────
            System.out.println("## (b) UPIS_SW 건수");
            System.out.println();
            System.out.println("| 테이블 | count |");
            System.out.println("|--------|------:|");
            long upisSw1 = countOne(c, "SELECT COUNT(*) FROM inspect_template WHERE template_type='UPIS_SW'");
            long upisSw2 = countOne(c, "SELECT COUNT(*) FROM inspect_report WHERE sys_type='UPIS_SW'");
            long upisSw3 = countOne(c, "SELECT COUNT(*) FROM tb_document    WHERE sys_type='UPIS_SW'");
            System.out.println("| inspect_template.template_type='UPIS_SW' | " + upisSw1 + " |");
            System.out.println("| inspect_report.sys_type='UPIS_SW'        | " + upisSw2 + " |");
            System.out.println("| tb_document.sys_type='UPIS_SW'           | " + upisSw3 + " |");
            long totalSw = upisSw1 + upisSw2 + upisSw3;
            System.out.println();
            System.out.println("- 합계: **" + totalSw + "** (로드맵 기록 22건 기준 ±5 허용)");
            if (Math.abs(totalSw - 22) > 5) {
                halt = true;
                haltReasons.append("- UPIS_SW 총건수 ").append(totalSw).append(" (기준 22±5 초과)\n");
            }
            System.out.println();

            // ─────────────────────────────────
            // (c) APP 섹션 건수
            // ─────────────────────────────────
            System.out.println("## (c) APP 섹션 건수");
            System.out.println();
            System.out.println("| 테이블 | count |");
            System.out.println("|--------|------:|");
            long appT = countOne(c, "SELECT COUNT(*) FROM inspect_template WHERE section='APP'");
            long appR = countOne(c, "SELECT COUNT(*) FROM inspect_check_result WHERE section='APP'");
            System.out.println("| inspect_template.section='APP'   | " + appT + " |");
            System.out.println("| inspect_check_result.section='APP' | " + appR + " |");
            System.out.println();

            // ─────────────────────────────────
            // (d) 대상 테이블 row 수
            // ─────────────────────────────────
            System.out.println("## (d) 대상 테이블 row 수 (TRUNCATE 전 스냅샷)");
            System.out.println();
            System.out.println("| 테이블 | rows |");
            System.out.println("|--------|-----:|");
            String[] tables = {"inspect_report","inspect_check_result","inspect_visit_log","inspect_template",
                               "tb_inspect_checklist","tb_inspect_issue","tb_document_signature"};
            for (String t : tables) {
                try {
                    long cnt = countOne(c, "SELECT COUNT(*) FROM " + t);
                    System.out.println("| " + t + " | " + cnt + " |");
                } catch (SQLException ex) {
                    System.out.println("| " + t + " | (테이블 없음) |");
                }
            }
            System.out.println();

            // ─────────────────────────────────
            // (e) 외부 FK 의존성
            // ─────────────────────────────────
            System.out.println("## (e) 외부 FK 의존성 (DROP + TRUNCATE CASCADE 위험)");
            System.out.println();
            System.out.println("### e-1. Checklist/Issue DROP 영향");
            System.out.println("| referencing | column | target | constraint |");
            System.out.println("|-------------|--------|--------|------------|");
            long fk1 = runTable(c,
                "SELECT tc.table_name, kcu.column_name, ccu.table_name, tc.constraint_name " +
                " FROM information_schema.table_constraints tc " +
                " JOIN information_schema.key_column_usage kcu USING (constraint_name, table_schema) " +
                " JOIN information_schema.constraint_column_usage ccu USING (constraint_name, table_schema) " +
                " WHERE tc.constraint_type='FOREIGN KEY' AND tc.table_schema='public' " +
                "   AND ccu.table_name IN ('tb_inspect_checklist','tb_inspect_issue') " +
                "   AND tc.table_name NOT IN ('tb_inspect_checklist','tb_inspect_issue') " +
                " ORDER BY tc.table_name");
            if (fk1 > 0) {
                halt = true;
                haltReasons.append("- tb_inspect_checklist/issue 외부 FK ").append(fk1).append("건 — DROP 전 정리 필요\n");
            }
            System.out.println();

            System.out.println("### e-2. inspect_* TRUNCATE CASCADE 위험");
            System.out.println("| referencing | column | target | constraint |");
            System.out.println("|-------------|--------|--------|------------|");
            long fk2 = runTable(c,
                "SELECT tc.table_name, kcu.column_name, ccu.table_name, tc.constraint_name " +
                " FROM information_schema.table_constraints tc " +
                " JOIN information_schema.key_column_usage kcu USING (constraint_name, table_schema) " +
                " JOIN information_schema.constraint_column_usage ccu USING (constraint_name, table_schema) " +
                " WHERE tc.constraint_type='FOREIGN KEY' AND tc.table_schema='public' " +
                "   AND ccu.table_name IN ('inspect_report','inspect_check_result','inspect_visit_log','inspect_template') " +
                "   AND tc.table_name NOT IN ('inspect_report','inspect_check_result','inspect_visit_log','inspect_template') " +
                " ORDER BY tc.table_name");
            if (fk2 > 0) {
                // NOT HALT — 경고만. Phase 6 CASCADE 회피 설계로 대응 가능
                System.out.println();
                System.out.println("⚠ **경고**: inspect_* 를 참조하는 외부 FK " + fk2 + "건 발견. Phase 6 TRUNCATE 전 CASCADE 설계 재검토 필요");
            }
            System.out.println();

            // ─────────────────────────────────
            // (f) section NULL/공백/9종외
            // ─────────────────────────────────
            System.out.println("## (f) section NULL/공백/9종외 (Phase 5 FK ADD 전 필수)");
            System.out.println();
            System.out.println("### inspect_check_result 이상 section");
            System.out.println("| section | cnt |");
            System.out.println("|---------|----:|");
            long bad1 = runTable(c,
                "SELECT COALESCE(section,'(NULL)') AS section, COUNT(*) FROM inspect_check_result " +
                " WHERE section IS NULL OR section = '' OR section <> TRIM(section) " +
                "    OR section NOT IN ('DB','AP','DBMS','GIS','APP','DB_USAGE','AP_USAGE','DBMS_ETC','APP_ETC') " +
                " GROUP BY section ORDER BY section");
            System.out.println();
            System.out.println("### inspect_template 이상 section");
            System.out.println("| section | cnt |");
            System.out.println("|---------|----:|");
            long bad2 = runTable(c,
                "SELECT COALESCE(section,'(NULL)') AS section, COUNT(*) FROM inspect_template " +
                " WHERE section IS NULL OR section = '' OR section <> TRIM(section) " +
                "    OR section NOT IN ('DB','AP','DBMS','GIS','APP','DB_USAGE','AP_USAGE','DBMS_ETC','APP_ETC') " +
                " GROUP BY section ORDER BY section");
            System.out.println();
            if (bad1 > 0 || bad2 > 0) {
                // NOT HALT by itself — V022 Phase 3/4 에서 정리하면 됨. 다만 알림
                System.out.println("ℹ **알림**: 9종 외 section 값 발견 → V022 에서 DELETE or UPDATE 정리 대상. Phase 5 FK ADD 전 반드시 해소");
                System.out.println();
            }

            // ─────────────────────────────────
            // 최종 판정
            // ─────────────────────────────────
            System.out.println("---");
            System.out.println();
            System.out.println("## Exit Gate 1 판정");
            System.out.println();
            if (halt) {
                System.out.println("❌ **HALT** — 다음 사유로 작업 중단·재계획 권장:");
                System.out.println();
                System.out.println(haltReasons.toString());
            } else {
                System.out.println("✅ **PASS** — Step 2 V022 마이그 착수 가능");
                System.out.println();
                if (fk2 > 0) {
                    System.out.println("ℹ 단, inspect_* 외부 FK " + fk2 + "건 있어 Phase 6 설계 유의");
                }
            }
        }
    }

    private static long countOne(Connection c, String sql) throws SQLException {
        try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery(sql)) {
            rs.next();
            return rs.getLong(1);
        }
    }

    /** 쿼리 실행 + 마크다운 표 행 출력 + 총 행수 리턴 */
    private static long runTable(Connection c, String sql) throws SQLException {
        long n = 0;
        try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery(sql)) {
            ResultSetMetaData md = rs.getMetaData();
            int cols = md.getColumnCount();
            while (rs.next()) {
                StringBuilder row = new StringBuilder("|");
                for (int i = 1; i <= cols; i++) {
                    String v = rs.getString(i);
                    row.append(' ').append(v == null ? "(null)" : v).append(" |");
                }
                System.out.println(row);
                n++;
            }
        }
        if (n == 0) {
            System.out.println("| _(없음)_ | |");
        }
        return n;
    }
}
