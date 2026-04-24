import java.io.*;
import java.sql.*;

public class BatchAExtra {
    public static void main(String[] args) throws Exception {
        String url = System.getenv().getOrDefault("DB_URL", "jdbc:postgresql://211.104.137.55:5881/SW_Dept");
        String pw = System.getenv("DB_PASSWORD");
        Class.forName("org.postgresql.Driver");
        try (Connection c = DriverManager.getConnection(url, "postgres", pw);
             PrintWriter out = new PrintWriter(new FileWriter("docs/generated/audit/batch-a-extra-result.md"))) {
            try (Statement s = c.createStatement()) {
                s.execute("SET SESSION statement_timeout = '30s'");
            }
            c.setReadOnly(true);
            out.println("# 배치 A 추가 검증\n");

            String[][] queries = {
                {"tb_process_master 총 레코드/DISTINCT",
                    "SELECT COUNT(*) AS total, COUNT(DISTINCT (sys_nm_en || '|' || process_name)) AS distinct_cnt " +
                    "FROM tb_process_master"},
                {"tb_process_master 시스템별 중복 정도",
                    "SELECT sys_nm_en, COUNT(*) AS total, COUNT(DISTINCT process_name) AS uniq_names " +
                    "FROM tb_process_master GROUP BY sys_nm_en ORDER BY total DESC LIMIT 20"},
                {"tb_service_purpose 총 레코드/DISTINCT",
                    "SELECT COUNT(*) AS total, COUNT(DISTINCT (sys_nm_en || '|' || purpose_type || '|' || LEFT(purpose_text,50))) AS distinct_cnt " +
                    "FROM tb_service_purpose"},
                {"inspect_check_result.category 공백 정규화 후",
                    "SELECT TRIM(REGEXP_REPLACE(category, '\\s+', ' ', 'g')) AS cat_normalized, COUNT(*) AS cnt, " +
                    "COUNT(DISTINCT category) AS raw_variants " +
                    "FROM inspect_check_result GROUP BY cat_normalized ORDER BY cnt DESC LIMIT 50"},
                {"inspect_check_result.item_name 샘플",
                    "SELECT LEFT(item_name, 60) AS item_head, COUNT(*) AS cnt FROM inspect_check_result " +
                    "GROUP BY item_head ORDER BY cnt DESC LIMIT 20"},
                {"inspect_template vs sys_mst 비교",
                    "SELECT t.template_type AS value, COUNT(*) AS cnt, " +
                    "CASE WHEN m.cd IS NULL THEN '❌ NOT IN sys_mst' ELSE '✅' END AS match " +
                    "FROM inspect_template t LEFT JOIN sys_mst m ON t.template_type = m.cd " +
                    "GROUP BY t.template_type, m.cd ORDER BY cnt DESC LIMIT 50"},
                {"tb_work_plan 전체 레코드 상세",
                    "SELECT plan_id, plan_type, status, process_step, repeat_type, LEFT(title,40) AS title_head, infra_id " +
                    "FROM tb_work_plan ORDER BY plan_id LIMIT 100"},
                // inspect_check_result.result이 %/GB/자유텍스트 분리 가능한지
                {"inspect_check_result.result 패턴 분류",
                    "SELECT " +
                    "  CASE " +
                    "    WHEN result IS NULL OR result = '' THEN 'EMPTY' " +
                    "    WHEN result = '정상' THEN 'NORMAL_KOR' " +
                    "    WHEN result = '점검' THEN 'INSPECT_KOR' " +
                    "    WHEN result ~ '^[0-9.]+%$' THEN 'PERCENT' " +
                    "    WHEN result ~ 'GB' THEN 'DISK_USAGE' " +
                    "    ELSE 'OTHER' " +
                    "  END AS pattern, COUNT(*) AS cnt " +
                    "FROM inspect_check_result GROUP BY pattern ORDER BY cnt DESC"}
            };
            for (String[] q : queries) {
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
