import java.io.*;
import java.sql.*;

public class BatchBWageCheck {
    public static void main(String[] args) throws Exception {
        Class.forName("org.postgresql.Driver");
        try (Connection c = DriverManager.getConnection(
                "jdbc:postgresql://211.104.137.55:5881/SW_Dept", "postgres", System.getenv("DB_PASSWORD"));
             PrintWriter out = new PrintWriter(new FileWriter("docs/audit/batch-b-wage-check-result.md"))) {
            c.setReadOnly(true);
            out.println("# qt_wage_rate ↔ users.tech_grade 연관 확인\n");
            String[][] qs = {
                {"users.tech_grade 분포 (PII 제외)",
                 "SELECT tech_grade, COUNT(*) AS cnt FROM users GROUP BY tech_grade ORDER BY cnt DESC"},
                {"qt_wage_rate.grade_name distinct",
                 "SELECT DISTINCT grade_name, description FROM qt_wage_rate ORDER BY grade_name LIMIT 30"},
                {"grade_name vs tech_grade 교차 매칭",
                 "SELECT w.grade_name AS wage_grade, u.tech_grade AS user_grade " +
                 "FROM qt_wage_rate w FULL OUTER JOIN users u ON w.grade_name = u.tech_grade " +
                 "GROUP BY w.grade_name, u.tech_grade ORDER BY 1 NULLS LAST LIMIT 30"},
                // Person 에도 등급 있는지
                {"ps_info 컬럼",
                 "SELECT column_name FROM information_schema.columns WHERE table_name = 'ps_info' ORDER BY ordinal_position"}
            };
            for (String[] q : qs) {
                out.println("## " + q[0] + "\n");
                try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(q[1])) {
                    ResultSetMetaData md = rs.getMetaData();
                    int cols = md.getColumnCount();
                    StringBuilder h = new StringBuilder("|");
                    for (int i = 1; i <= cols; i++) h.append(' ').append(md.getColumnLabel(i)).append(" |");
                    out.println(h);
                    StringBuilder d = new StringBuilder("|");
                    for (int i = 1; i <= cols; i++) d.append("---|");
                    out.println(d);
                    int n = 0;
                    while (rs.next()) {
                        StringBuilder r = new StringBuilder("|");
                        for (int i = 1; i <= cols; i++) {
                            String v = rs.getString(i);
                            r.append(' ').append(v == null ? "(null)" : v.replace("|","\\|").replace("\n"," ")).append(" |");
                        }
                        out.println(r); n++;
                    }
                    out.println("\n총 " + n + "행\n");
                } catch (SQLException e) { out.println("❌ " + e.getMessage() + "\n"); }
            }
        }
        System.out.println("OK");
    }
}
