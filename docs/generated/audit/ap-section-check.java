import java.io.*;
import java.sql.*;

public class ApSectionCheck {
    public static void main(String[] args) throws Exception {
        Class.forName("org.postgresql.Driver");
        try (Connection c = DriverManager.getConnection(
                "jdbc:postgresql://211.104.137.55:5881/SW_Dept", "postgres", System.getenv("DB_PASSWORD"));
             PrintWriter out = new PrintWriter(new FileWriter("docs/generated/audit/ap-section-check-result.md"))) {
            c.setReadOnly(true);
            out.println("# AP / APP 섹션 상세 확인\n");
            String[][] qs = {
                {"inspect_template.section별 template_type 분포",
                 "SELECT section, template_type, COUNT(*) AS cnt FROM inspect_template " +
                 "GROUP BY section, template_type ORDER BY section, template_type"},
                {"inspect_template.AP 섹션 14건 상세 (서버관리 연관?)",
                 "SELECT id, template_type, section, category, item_name FROM inspect_template " +
                 "WHERE section = 'AP' ORDER BY sort_order LIMIT 30"},
                {"inspect_template.APP 섹션 28건 상세 (미사용 확인)",
                 "SELECT id, template_type, section, category, item_name FROM inspect_template " +
                 "WHERE section = 'APP' ORDER BY template_type, sort_order LIMIT 50"},
                {"실제 사용 중인 섹션 (inspect_check_result 실저장 데이터)",
                 "SELECT section, COUNT(*) AS used_cnt FROM inspect_check_result " +
                 "GROUP BY section ORDER BY used_cnt DESC"}
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
