import java.io.*;
import java.sql.*;

public class UpisSwCheck {
    public static void main(String[] args) throws Exception {
        String pw = System.getenv("DB_PASSWORD");
        Class.forName("org.postgresql.Driver");
        try (Connection c = DriverManager.getConnection(
                "jdbc:postgresql://211.104.137.55:5881/SW_Dept", "postgres", pw);
             PrintWriter out = new PrintWriter(new FileWriter("docs/generated/audit/upis-sw-check-result.md"))) {
            try (Statement s = c.createStatement()) {
                s.execute("SET SESSION statement_timeout = '30s'");
            }
            c.setReadOnly(true);
            out.println("# UPIS_SW 사용처 실데이터 분석\n");

            String[][] qs = {
                {"inspect_template.UPIS_SW 20건 상세",
                 "SELECT id, template_type, section, category, item_name FROM inspect_template " +
                 "WHERE template_type = 'UPIS_SW' ORDER BY section, sort_order LIMIT 100"},
                {"inspect_template.UPIS 상위 항목 (비교)",
                 "SELECT id, template_type, section, category, item_name FROM inspect_template " +
                 "WHERE template_type = 'UPIS' ORDER BY section, sort_order LIMIT 30"},
                {"inspect_report.UPIS_SW 1건",
                 "SELECT id, pjt_id, sys_type, inspect_month, insp_company, insp_name, doc_title " +
                 "FROM inspect_report WHERE sys_type = 'UPIS_SW'"},
                {"tb_document.UPIS_SW 1건",
                 "SELECT doc_id, doc_type, sys_type, LEFT(title,50) AS title_head, status " +
                 "FROM tb_document WHERE sys_type = 'UPIS_SW'"},
                {"inspect_report.UPIS_SW 레코드가 참조하는 project",
                 "SELECT r.id, r.sys_type, p.sys_nm_en, p.sys_nm, LEFT(p.proj_nm, 60) AS proj_nm " +
                 "FROM inspect_report r LEFT JOIN sw_pjt p ON r.pjt_id = p.proj_id " +
                 "WHERE r.sys_type = 'UPIS_SW'"}
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
