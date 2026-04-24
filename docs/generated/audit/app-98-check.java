import java.io.*;
import java.sql.*;

public class App98Check {
    public static void main(String[] args) throws Exception {
        Class.forName("org.postgresql.Driver");
        try (Connection c = DriverManager.getConnection(
                "jdbc:postgresql://211.104.137.55:5881/SW_Dept", "postgres", System.getenv("DB_PASSWORD"));
             PrintWriter out = new PrintWriter(new FileWriter("docs/audit/app-98-check-result.md"))) {
            c.setReadOnly(true);
            out.println("# inspect_check_result.APP 98건 실저장 경로 조사\n");

            String[][] qs = {
                // 1. APP 섹션이 어느 점검내역서에 들어있나?
                {"APP 섹션을 사용하는 inspect_report 목록",
                 "SELECT icr.report_id, ir.sys_type, ir.inspect_month, ir.doc_title, " +
                 "       COUNT(*) AS app_rows " +
                 "FROM inspect_check_result icr " +
                 "LEFT JOIN inspect_report ir ON icr.report_id = ir.id " +
                 "WHERE icr.section = 'APP' " +
                 "GROUP BY icr.report_id, ir.sys_type, ir.inspect_month, ir.doc_title " +
                 "ORDER BY app_rows DESC LIMIT 30"},

                // 2. APP 저장 데이터 샘플 (section='APP' 98건 내용)
                {"APP 섹션 실저장 샘플 15건",
                 "SELECT report_id, section, category, item_name, " +
                 "       LEFT(COALESCE(result,''),20) AS result_head, " +
                 "       LEFT(COALESCE(remarks,''),30) AS remarks_head " +
                 "FROM inspect_check_result " +
                 "WHERE section = 'APP' " +
                 "ORDER BY report_id, sort_order LIMIT 15"},

                // 3. inspect_template.APP과 inspect_check_result.APP 의 category 비교
                {"inspect_template.APP category (미사용 주장)",
                 "SELECT DISTINCT category FROM inspect_template WHERE section = 'APP' ORDER BY category"},
                {"inspect_check_result.APP category (실저장)",
                 "SELECT category, COUNT(*) AS cnt FROM inspect_check_result " +
                 "WHERE section = 'APP' GROUP BY category ORDER BY cnt DESC LIMIT 30"},

                // 4. sys_type 별 APP 사용 여부
                {"sys_type별 APP 사용 건수",
                 "SELECT ir.sys_type, COUNT(*) AS app_rows " +
                 "FROM inspect_check_result icr " +
                 "LEFT JOIN inspect_report ir ON icr.report_id = ir.id " +
                 "WHERE icr.section = 'APP' " +
                 "GROUP BY ir.sys_type ORDER BY app_rows DESC"},

                // 5. APP_ETC 1건 (혹시 별도 섹션인지)
                {"APP_ETC 섹션 1건 정체",
                 "SELECT report_id, section, category, item_name, " +
                 "       LEFT(COALESCE(result,''),40) AS result_head " +
                 "FROM inspect_check_result WHERE section = 'APP_ETC'"}
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
