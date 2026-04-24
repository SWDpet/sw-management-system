import java.io.*;
import java.sql.*;

/**
 * 배치 A (업무계획 + 점검내역서) 실데이터 분포 스캔.
 * 기획서 §7-5 안전통제 준수 (SELECT만, LIMIT 적용, PII 없음).
 */
public class BatchAScan {
    public static void main(String[] args) throws Exception {
        String url = System.getenv().getOrDefault("DB_URL", "jdbc:postgresql://211.104.137.55:5881/SW_Dept");
        String pw = System.getenv("DB_PASSWORD");
        Class.forName("org.postgresql.Driver");
        try (Connection c = DriverManager.getConnection(url, "postgres", pw);
             PrintWriter out = new PrintWriter(new FileWriter("docs/audit/batch-a-scan-result.md"))) {
            try (Statement s = c.createStatement()) {
                s.execute("SET SESSION statement_timeout = '30s'");
                s.execute("SET SESSION lock_timeout = '5s'");
            }
            c.setReadOnly(true);

            out.println("# 배치 A 실데이터 분포 (Phase 2)");
            out.println();

            String[][] queries = {
                // tb_work_plan 분포
                {"tb_work_plan.plan_type",   "SELECT plan_type, COUNT(*) FROM tb_work_plan GROUP BY plan_type ORDER BY 2 DESC LIMIT 100"},
                {"tb_work_plan.status",      "SELECT status, COUNT(*) FROM tb_work_plan GROUP BY status ORDER BY 2 DESC LIMIT 100"},
                {"tb_work_plan.repeat_type", "SELECT repeat_type, COUNT(*) FROM tb_work_plan GROUP BY repeat_type ORDER BY 2 DESC LIMIT 100"},
                {"tb_work_plan.process_step","SELECT process_step, COUNT(*) FROM tb_work_plan GROUP BY process_step ORDER BY 2 DESC LIMIT 100"},
                // tb_work_plan.plan_type vs maint_tp_mst
                {"plan_type vs maint_tp_mst", "SELECT w.plan_type AS value, COUNT(*) AS cnt, " +
                    "CASE WHEN m.cd IS NULL THEN '❌' ELSE '✅' END AS match " +
                    "FROM tb_work_plan w LEFT JOIN maint_tp_mst m ON w.plan_type = m.cd " +
                    "GROUP BY w.plan_type, m.cd ORDER BY cnt DESC LIMIT 100"},
                // tb_work_plan.status vs cont_stat_mst
                {"tb_work_plan.status vs cont_stat_mst", "SELECT w.status AS value, COUNT(*) AS cnt, " +
                    "CASE WHEN m.cd IS NULL THEN '❌' ELSE '✅' END AS match " +
                    "FROM tb_work_plan w LEFT JOIN cont_stat_mst m ON w.status = m.cd " +
                    "GROUP BY w.status, m.cd ORDER BY cnt DESC LIMIT 100"},

                // ProcessMaster / ServicePurpose
                {"tb_process_master rows", "SELECT sys_nm_en, process_name, sort_order FROM tb_process_master ORDER BY sys_nm_en, sort_order LIMIT 100"},
                {"tb_service_purpose rows", "SELECT sys_nm_en, purpose_type, LEFT(purpose_text,50) AS purpose_head, sort_order FROM tb_service_purpose ORDER BY sys_nm_en, sort_order LIMIT 100"},

                // 점검내역서
                {"inspect_report.sys_type",     "SELECT sys_type, COUNT(*) FROM inspect_report GROUP BY sys_type ORDER BY 2 DESC LIMIT 100"},
                {"inspect_check_result.section","SELECT section, COUNT(*) FROM inspect_check_result GROUP BY section ORDER BY 2 DESC LIMIT 100"},
                {"inspect_check_result.result_distinct","SELECT result, COUNT(*) FROM inspect_check_result GROUP BY result ORDER BY 2 DESC LIMIT 50"},
                {"inspect_check_result.category","SELECT category, COUNT(*) FROM inspect_check_result GROUP BY category ORDER BY 2 DESC LIMIT 100"},
                {"inspect_template.section",    "SELECT section, COUNT(*) FROM inspect_template GROUP BY section ORDER BY 2 DESC LIMIT 100"},
                {"inspect_template.template_type","SELECT template_type, COUNT(*) FROM inspect_template GROUP BY template_type ORDER BY 2 DESC LIMIT 100"},
                {"inspect_visit_log count", "SELECT COUNT(*) FROM inspect_visit_log"}
            };
            for (String[] q : queries) {
                out.println("## " + q[0]);
                out.println();
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
                        out.println(r);
                        n++;
                    }
                    out.println("\n총 " + n + "행\n");
                } catch (SQLException e) {
                    out.println("❌ " + e.getMessage() + "\n");
                }
            }
        }
        System.out.println("OK: docs/audit/batch-a-scan-result.md");
    }
}
