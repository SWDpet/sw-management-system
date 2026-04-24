import java.io.*;
import java.sql.*;

public class BatchBExtra {
    public static void main(String[] args) throws Exception {
        Class.forName("org.postgresql.Driver");
        try (Connection c = DriverManager.getConnection(
                "jdbc:postgresql://211.104.137.55:5881/SW_Dept", "postgres", System.getenv("DB_PASSWORD"));
             PrintWriter out = new PrintWriter(new FileWriter("docs/audit/batch-b-extra-result.md"))) {
            c.setReadOnly(true);
            out.println("# 배치 B 추가 검증\n");
            String[][] qs = {
                {"qt_wage_rate 컬럼",
                 "SELECT column_name, data_type FROM information_schema.columns " +
                 "WHERE table_name = 'qt_wage_rate' ORDER BY ordinal_position"},
                {"qt_wage_rate 샘플 5건",
                 "SELECT * FROM qt_wage_rate LIMIT 5"},
                {"qt_product_pattern 샘플",
                 "SELECT pattern_id, category, pattern_name, LEFT(COALESCE(pattern_data,''),40) AS data_head FROM qt_product_pattern LIMIT 10"},
                {"qt_remarks_pattern 샘플",
                 "SELECT * FROM qt_remarks_pattern LIMIT 10"},
                // qt_product_pattern 컬럼
                {"qt_product_pattern 컬럼",
                 "SELECT column_name, data_type FROM information_schema.columns " +
                 "WHERE table_name = 'qt_product_pattern' ORDER BY ordinal_position"},
                // qt_remarks_pattern 컬럼
                {"qt_remarks_pattern 컬럼",
                 "SELECT column_name, data_type FROM information_schema.columns " +
                 "WHERE table_name = 'qt_remarks_pattern' ORDER BY ordinal_position"},
                // license_registry.country 오염 데이터 (사람 이름 포함)
                {"license_registry.country 오염 추정 값 — 사람 이름/이상치",
                 "SELECT country, full_name, company, COUNT(*) AS cnt FROM license_registry " +
                 "WHERE country IN ('서현규','kim hanjun','seo hyeon gyu','김한준','Park Uk Jin') " +
                 "GROUP BY country, full_name, company LIMIT 20"}
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
