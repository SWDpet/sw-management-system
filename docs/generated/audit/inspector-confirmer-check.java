import java.io.*;
import java.sql.*;

public class InspectorConfirmerCheck {
    public static void main(String[] args) throws Exception {
        Class.forName("org.postgresql.Driver");
        try (Connection c = DriverManager.getConnection(
                "jdbc:postgresql://211.104.137.55:5881/SW_Dept", "postgres", System.getenv("DB_PASSWORD"));
             PrintWriter out = new PrintWriter(new FileWriter("docs/generated/audit/inspector-confirmer-check-result.md"))) {
            c.setReadOnly(true);
            out.println("# 점검자/확인자 저장 방식 확인\n\n- 점검자 = 내부 직원(users)\n- 확인자 = 고객 담당자(ps_info)\n\n");
            String[][] qs = {
                {"inspect_report 점검자/확인자 컬럼 현황",
                 "SELECT column_name, data_type, character_maximum_length FROM information_schema.columns " +
                 "WHERE table_name='inspect_report' AND (column_name LIKE '%insp%' OR column_name LIKE '%conf%') " +
                 "ORDER BY ordinal_position"},
                {"inspect_report 점검자(insp_*) 샘플 — 내부직원 users 참조 필요",
                 "SELECT id, insp_company, insp_name FROM inspect_report ORDER BY id LIMIT 15"},
                {"inspect_report 확인자(conf_*) 샘플 — 고객담당자 ps_info 참조 필요",
                 "SELECT id, conf_org, conf_name FROM inspect_report ORDER BY id LIMIT 15"},
                {"inspect_report.insp_name vs users.username 매칭 (PII 마스킹)",
                 "SELECT r.insp_name, CASE WHEN u.user_id IS NULL THEN '❌ NOT IN users' " +
                 "ELSE '✅ matched user_id='||u.user_id END AS match " +
                 "FROM inspect_report r LEFT JOIN users u ON r.insp_name = u.username " +
                 "WHERE r.insp_name IS NOT NULL GROUP BY r.insp_name, u.user_id ORDER BY r.insp_name LIMIT 30"},
                {"inspect_report.conf_name vs ps_info.user_nm 매칭",
                 "SELECT r.conf_name, r.conf_org, " +
                 "CASE WHEN p.id IS NULL THEN '❌ NOT IN ps_info' ELSE '✅ matched ps_info.id='||p.id END AS match " +
                 "FROM inspect_report r LEFT JOIN ps_info p ON r.conf_name = p.user_nm " +
                 "WHERE r.conf_name IS NOT NULL GROUP BY r.conf_name, r.conf_org, p.id ORDER BY r.conf_name LIMIT 30"}
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
