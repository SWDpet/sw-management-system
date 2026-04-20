import java.sql.*;
import java.io.*;
import java.util.*;

/**
 * Pre-flight 진단 SQL 실행기 (1회용, 스프린트 #1 착수 전 사용).
 * 실행: java -cp <postgresql-jdbc.jar> hardcoding-preflight-runner.java
 */
public class HardcodingPreflight {
    public static void main(String[] args) throws Exception {
        String url = System.getenv().getOrDefault("DB_URL", "jdbc:postgresql://211.104.137.55:5881/SW_Dept");
        String user = System.getenv().getOrDefault("DB_USERNAME", "postgres");
        String pw = System.getenv("DB_PASSWORD");
        if (pw == null || pw.isBlank()) {
            System.err.println("ERROR: DB_PASSWORD env required");
            System.exit(2);
        }
        Class.forName("org.postgresql.Driver");
        try (Connection c = DriverManager.getConnection(url, user, pw);
             PrintWriter out = new PrintWriter(new FileWriter("docs/dev-plans/hardcoding-preflight-result.md"))) {

            out.println("# Pre-flight SQL 실행 결과 (스프린트 #1 착수 전)");
            out.println();
            out.println("- 실행일: " + new java.util.Date());
            out.println("- DB: " + url);
            out.println();

            String[][] queries = {
                {"1-1. tb_document.status 분포",       "SELECT status, COUNT(*) FROM tb_document GROUP BY status ORDER BY COUNT(*) DESC"},
                {"1-2. tb_document.doc_type 분포",     "SELECT doc_type, COUNT(*) FROM tb_document GROUP BY doc_type ORDER BY COUNT(*) DESC"},
                {"1-3. sw_pjt.sys_nm_en 분포",         "SELECT sys_nm_en, COUNT(*) FROM sw_pjt GROUP BY sys_nm_en ORDER BY COUNT(*) DESC"},
                {"1-4. inspect_check_result.section 분포", "SELECT section, COUNT(*) FROM inspect_check_result GROUP BY section ORDER BY COUNT(*) DESC"},
                {"1-5. inspect_check_result.result 분포",  "SELECT result, COUNT(*) FROM inspect_check_result GROUP BY result ORDER BY COUNT(*) DESC"},
                {"1-6. inspect_report.status 분포",    "SELECT status, COUNT(*) FROM inspect_report GROUP BY status ORDER BY COUNT(*) DESC"},
                {"2-1. sw_pjt.sys_nm_en 공백/대소문자 진단",
                    "SELECT sys_nm_en, TRIM(sys_nm_en) AS trimmed, LOWER(sys_nm_en) AS lowered, COUNT(*) " +
                    "FROM sw_pjt GROUP BY sys_nm_en, TRIM(sys_nm_en), LOWER(sys_nm_en) " +
                    "ORDER BY COUNT(*) DESC"},
                {"2-2. sw_pjt.sys_nm_en NULL/빈값",     "SELECT COUNT(*) AS null_or_empty FROM sw_pjt WHERE sys_nm_en IS NULL OR sys_nm_en = ''"},
                {"2-3. tb_document.status NULL/빈값",  "SELECT COUNT(*) AS null_or_empty FROM tb_document WHERE status IS NULL OR status = ''"},
                {"2-4. tb_document.doc_type NULL/빈값","SELECT COUNT(*) AS null_or_empty FROM tb_document WHERE doc_type IS NULL OR doc_type = ''"},
            };

            for (String[] q : queries) {
                out.println("## " + q[0]);
                out.println();
                out.println("```sql");
                out.println(q[1]);
                out.println("```");
                out.println();
                try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(q[1])) {
                    ResultSetMetaData md = rs.getMetaData();
                    int cols = md.getColumnCount();
                    // header
                    out.print("|");
                    for (int i = 1; i <= cols; i++) out.print(" " + md.getColumnLabel(i) + " |");
                    out.println();
                    out.print("|");
                    for (int i = 1; i <= cols; i++) out.print("---|");
                    out.println();
                    int rowCount = 0;
                    while (rs.next()) {
                        out.print("|");
                        for (int i = 1; i <= cols; i++) {
                            String v = rs.getString(i);
                            if (v == null) v = "(null)";
                            // escape pipe and show whitespace markers
                            String display = v.replace("|", "\\|");
                            // highlight leading/trailing whitespace
                            if (!v.equals(v.trim())) display = "⟨WS⟩" + display + "⟨/WS⟩";
                            out.print(" " + display + " |");
                        }
                        out.println();
                        rowCount++;
                    }
                    out.println();
                    out.println("총 " + rowCount + "행");
                } catch (SQLException e) {
                    out.println("❌ 에러: " + e.getMessage());
                }
                out.println();
            }

            out.println("---");
            out.println();
            out.println("**평가 기준**:");
            out.println("- 값이 Enum 후보와 1:1 정렬되면 `@Enumerated(STRING)` 가능");
            out.println("- 대소문자/공백 불일치만 있으면 `AttributeConverter` 정규화로 해결");
            out.println("- 별칭/레거시 값(한글 등)이 있으면 **Pre-flight 데이터 정제 스크립트** 선행 필요");
            System.out.println("OK: docs/dev-plans/hardcoding-preflight-result.md 생성 완료");
        }
    }
}
