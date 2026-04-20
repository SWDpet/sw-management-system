import java.sql.*;

public class SysTypeMasterCheck {
    public static void main(String[] args) throws Exception {
        String url = System.getenv().getOrDefault("DB_URL", "jdbc:postgresql://211.104.137.55:5881/SW_Dept");
        String user = System.getenv().getOrDefault("DB_USERNAME", "postgres");
        String pw = System.getenv("DB_PASSWORD");
        Class.forName("org.postgresql.Driver");
        try (Connection c = DriverManager.getConnection(url, user, pw)) {

            System.out.println("\n=== 1. sys_mst 마스터 테이블 전체 레코드 ===");
            runSelect(c, "SELECT cd, nm FROM sys_mst ORDER BY cd");

            System.out.println("\n=== 2. sw_pjt.sys_nm_en vs sys_mst.cd 매칭 여부 ===");
            runSelect(c,
                "SELECT p.sys_nm_en, COUNT(*) AS pjt_count, " +
                "CASE WHEN m.cd IS NULL THEN '❌ NOT IN sys_mst' ELSE '✅ matched' END AS status, " +
                "m.nm AS master_nm " +
                "FROM sw_pjt p LEFT JOIN sys_mst m ON p.sys_nm_en = m.cd " +
                "GROUP BY p.sys_nm_en, m.cd, m.nm ORDER BY pjt_count DESC");

            System.out.println("\n=== 3. sys_mst에만 있는데 sw_pjt에 없는 코드 ===");
            runSelect(c,
                "SELECT m.cd, m.nm FROM sys_mst m " +
                "WHERE NOT EXISTS (SELECT 1 FROM sw_pjt p WHERE p.sys_nm_en = m.cd) ORDER BY m.cd");

            System.out.println("\n=== 4. tb_document.sys_type vs sys_mst 매칭 ===");
            runSelect(c,
                "SELECT d.sys_type, COUNT(*) AS doc_count, " +
                "CASE WHEN m.cd IS NULL THEN '❌ NOT IN sys_mst' ELSE '✅ matched' END AS status " +
                "FROM tb_document d LEFT JOIN sys_mst m ON d.sys_type = m.cd " +
                "WHERE d.sys_type IS NOT NULL GROUP BY d.sys_type, m.cd ORDER BY doc_count DESC");

            System.out.println("\n=== 5. inspect_report.sys_type vs sys_mst 매칭 ===");
            runSelect(c,
                "SELECT r.sys_type, COUNT(*) AS rep_count, " +
                "CASE WHEN m.cd IS NULL THEN '❌ NOT IN sys_mst' ELSE '✅ matched' END AS status " +
                "FROM inspect_report r LEFT JOIN sys_mst m ON r.sys_type = m.cd " +
                "WHERE r.sys_type IS NOT NULL GROUP BY r.sys_type, m.cd ORDER BY rep_count DESC");

            System.out.println("\n=== 6. tb_infra_master.sys_nm_en vs sys_mst 매칭 ===");
            runSelect(c,
                "SELECT i.sys_nm_en, COUNT(*) AS infra_count, " +
                "CASE WHEN m.cd IS NULL THEN '❌ NOT IN sys_mst' ELSE '✅ matched' END AS status " +
                "FROM tb_infra_master i LEFT JOIN sys_mst m ON i.sys_nm_en = m.cd " +
                "WHERE i.sys_nm_en IS NOT NULL GROUP BY i.sys_nm_en, m.cd ORDER BY infra_count DESC");

            System.out.println("\n=== 7. '112' 코드의 위치 확인 ===");
            runSelect(c,
                "SELECT 'sys_mst' AS loc, COUNT(*) AS cnt FROM sys_mst WHERE cd = '112' " +
                "UNION ALL SELECT 'sw_pjt', COUNT(*) FROM sw_pjt WHERE sys_nm_en = '112' " +
                "UNION ALL SELECT 'tb_document', COUNT(*) FROM tb_document WHERE sys_type = '112' " +
                "UNION ALL SELECT 'inspect_report', COUNT(*) FROM inspect_report WHERE sys_type = '112' " +
                "UNION ALL SELECT 'tb_infra_master', COUNT(*) FROM tb_infra_master WHERE sys_nm_en = '112'");
        }
    }

    private static void runSelect(Connection c, String sql) throws SQLException {
        try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            ResultSetMetaData md = rs.getMetaData();
            int cols = md.getColumnCount();
            StringBuilder header = new StringBuilder("| ");
            for (int i = 1; i <= cols; i++) header.append(md.getColumnLabel(i)).append(" | ");
            System.out.println(header);
            while (rs.next()) {
                StringBuilder row = new StringBuilder("| ");
                for (int i = 1; i <= cols; i++) {
                    String v = rs.getString(i);
                    row.append(v == null ? "(null)" : v).append(" | ");
                }
                System.out.println(row);
            }
        }
    }
}
