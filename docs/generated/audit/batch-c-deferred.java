import java.io.*;
import java.sql.*;

public class BatchCDeferred {
    public static void main(String[] args) throws Exception {
        Class.forName("org.postgresql.Driver");
        try (Connection c = DriverManager.getConnection(
                "jdbc:postgresql://211.104.137.55:5881/SW_Dept", "postgres", System.getenv("DB_PASSWORD"));
             PrintWriter out = new PrintWriter(new FileWriter("docs/generated/audit/batch-c-deferred-result.md"))) {
            c.setReadOnly(true);
            try (Statement s = c.createStatement()) {
                s.execute("SET SESSION statement_timeout = '30s'");
            }
            out.println("# Batch C 추가 조사 — 미확인 테이블\n");

            String[][] qs = {
                // === tb_contract / tb_contract_target ===
                {"tb_contract 컬럼",
                 "SELECT column_name, data_type FROM information_schema.columns WHERE table_name = 'tb_contract' ORDER BY ordinal_position"},
                {"tb_contract 레코드 수",
                 "SELECT COUNT(*) AS total FROM tb_contract"},
                {"tb_contract 샘플",
                 "SELECT * FROM tb_contract LIMIT 5"},
                {"tb_contract_target 컬럼",
                 "SELECT column_name, data_type FROM information_schema.columns WHERE table_name = 'tb_contract_target' ORDER BY ordinal_position"},
                {"tb_contract_target 레코드 수",
                 "SELECT COUNT(*) AS total FROM tb_contract_target"},

                // === tb_inspect_checklist / tb_inspect_issue ===
                {"tb_inspect_checklist 컬럼",
                 "SELECT column_name, data_type FROM information_schema.columns WHERE table_name = 'tb_inspect_checklist' ORDER BY ordinal_position"},
                {"tb_inspect_checklist 레코드 수",
                 "SELECT COUNT(*) AS total FROM tb_inspect_checklist"},
                {"tb_inspect_issue 컬럼",
                 "SELECT column_name, data_type FROM information_schema.columns WHERE table_name = 'tb_inspect_issue' ORDER BY ordinal_position"},
                {"tb_inspect_issue 레코드 수",
                 "SELECT COUNT(*) AS total FROM tb_inspect_issue"},

                // === pjt_equip ===
                {"pjt_equip 컬럼",
                 "SELECT column_name, data_type FROM information_schema.columns WHERE table_name = 'pjt_equip' ORDER BY ordinal_position"},
                {"pjt_equip 레코드 수",
                 "SELECT COUNT(*) AS total FROM pjt_equip"},
                {"pjt_equip 샘플",
                 "SELECT * FROM pjt_equip LIMIT 5"},

                // === tb_document_history / tb_document_signature ===
                {"tb_document_history 레코드 수",
                 "SELECT COUNT(*) AS total FROM tb_document_history"},
                {"tb_document_history.action_type distinct",
                 "SELECT action_type, COUNT(*) AS cnt FROM tb_document_history GROUP BY action_type ORDER BY cnt DESC LIMIT 20"},
                {"tb_document_signature 레코드 수",
                 "SELECT COUNT(*) AS total FROM tb_document_signature"},

                // === tb_performance_summary ===
                {"tb_performance_summary 컬럼",
                 "SELECT column_name, data_type FROM information_schema.columns WHERE table_name = 'tb_performance_summary' ORDER BY ordinal_position"},
                {"tb_performance_summary 레코드 수",
                 "SELECT COUNT(*) AS total FROM tb_performance_summary"},

                // === tb_org_unit ===
                {"tb_org_unit 컬럼",
                 "SELECT column_name, data_type FROM information_schema.columns WHERE table_name = 'tb_org_unit' ORDER BY ordinal_position"},
                {"tb_org_unit 레코드 수",
                 "SELECT COUNT(*) AS total FROM tb_org_unit"}
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
                            if (v != null && v.length() > 60) v = v.substring(0, 60) + "...";
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
