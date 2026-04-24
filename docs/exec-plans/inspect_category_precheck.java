import java.sql.*;

/**
 * S10 inspect-category-master · Step 1 사전검증 러너 (READ ONLY)
 *
 * 실행:
 *   javac -encoding UTF-8 docs/exec-plans/inspect_category_precheck.java
 *   DB_PASSWORD=... java -cp ".;<postgresql-jar>" -Dfile.encoding=UTF-8 \
 *     inspect_category_precheck > docs/exec-plans/inspect-category-precheck-result.md
 *
 * 검증:
 *  (a) inspect_template.category distinct (특히 AP 4종 정확 label)
 *  (b) 공백 변형 2쌍 검출
 *  (c) NULL 카운트
 *  (d) inspect_check_result 불일치
 */
public class inspect_category_precheck {

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

        try (Connection c = DriverManager.getConnection(url, user, pass)) {
            try (Statement s = c.createStatement()) {
                s.execute("SET TRANSACTION READ ONLY");
                s.execute("SET statement_timeout = 10000");
            }

            System.out.println("# S10 inspect-category-master Step 1 — 사전검증");
            System.out.println();
            System.out.println("- 실행시각: " + new java.util.Date());
            System.out.println();

            // (a) distinct category
            System.out.println("## (a) inspect_template.(section, category) 분포");
            System.out.println("| section | category | cnt |");
            System.out.println("|---------|----------|----:|");
            runTable(c, "SELECT section, category, COUNT(*) FROM inspect_template " +
                        " GROUP BY section, category ORDER BY section, category");
            System.out.println();

            // (b) 공백 변형
            System.out.println("## (b) 공백 변형 2쌍 검출");
            System.out.println("| table | category | cnt |");
            System.out.println("|-------|----------|----:|");
            long w1 = runTable(c,
                "SELECT 'inspect_template', category, COUNT(*) FROM inspect_template " +
                " WHERE category IN ('GeoNURIS GeoWeb Server(GWS)','GeoNURIS Spatial Server(GSS)') " +
                " GROUP BY category");
            long w2 = runTable(c,
                "SELECT 'inspect_check_result', category, COUNT(*) FROM inspect_check_result " +
                " WHERE category IN ('GeoNURIS GeoWeb Server(GWS)','GeoNURIS Spatial Server(GSS)') " +
                " GROUP BY category");
            System.out.println();

            // (c) NULL
            System.out.println("## (c) NULL 카운트");
            long nullIt  = countOne(c, "SELECT COUNT(*) FROM inspect_template WHERE category IS NULL");
            long nullIcr = countOne(c, "SELECT COUNT(*) FROM inspect_check_result WHERE category IS NULL");
            System.out.println("| table | null_cnt |");
            System.out.println("|-------|---------:|");
            System.out.println("| inspect_template | " + nullIt + " |");
            System.out.println("| inspect_check_result | " + nullIcr + " |");
            if (nullIt > 0) { halt = true; }
            System.out.println();

            // (d) inspect_check_result category 분포
            System.out.println("## (d) inspect_check_result.(section, category) 분포");
            System.out.println("| section | category | cnt |");
            System.out.println("|---------|----------|----:|");
            runTable(c, "SELECT section, COALESCE(category,'(NULL)'), COUNT(*) FROM inspect_check_result " +
                        " GROUP BY section, category ORDER BY section, category");
            System.out.println();

            // Final
            System.out.println("---");
            System.out.println();
            System.out.println("## Exit Gate 1 판정");
            System.out.println();
            if (halt) {
                System.out.println("❌ HALT — inspect_template.category NULL = " + nullIt);
            } else {
                System.out.println("✅ PASS — Step 2 V023 착수 가능");
                System.out.println();
                System.out.println("- inspect_template.category NULL = 0");
                System.out.println("- 공백 변형 inspect_template 총 = " + w1 + "행");
                System.out.println("- 공백 변형 inspect_check_result 총 = " + w2 + "행");
            }
        }
    }

    private static long countOne(Connection c, String sql) throws SQLException {
        try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery(sql)) {
            rs.next();
            return rs.getLong(1);
        }
    }

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
        if (n == 0) System.out.println("| _(없음)_ | | |");
        return n;
    }
}
