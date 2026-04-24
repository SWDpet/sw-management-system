import java.io.*;
import java.sql.*;

/**
 * FR-0 사전검증 러너 — tb_process_master / tb_service_purpose 중복 제거 전 강제 체크.
 * (1) 중복 분포 재확인 (2) 각 중복 그룹 MIN PK (3) NULL 데이터 0건 (4) 외부 FK 0건 (5) purpose_text 최대 길이
 */
public class ProcessMasterPrecheck {
    public static void main(String[] args) throws Exception {
        String url = System.getenv().getOrDefault("DB_URL", "jdbc:postgresql://211.104.137.55:5881/SW_Dept");
        String pw = System.getenv("DB_PASSWORD");
        Class.forName("org.postgresql.Driver");
        boolean halt = false;
        StringBuilder out = new StringBuilder("# FR-0 사전검증 결과 (process-master-dedup)\n\n- 실행일: "
            + new java.util.Date() + "\n\n");

        try (Connection c = DriverManager.getConnection(url, "postgres", pw)) {
            try (Statement s = c.createStatement()) { s.execute("SET SESSION statement_timeout = '10s'"); }
            c.setReadOnly(true);

            // (1) 중복 분포
            out.append("## 1. 중복 분포\n\n");
            out.append("| 테이블 | total | distinct |\n|---|---|---|\n");
            long[] pm = {count(c, "SELECT COUNT(*) FROM tb_process_master"),
                        count(c, "SELECT COUNT(DISTINCT (sys_nm_en || '|' || process_name)) FROM tb_process_master")};
            long[] sp = {count(c, "SELECT COUNT(*) FROM tb_service_purpose"),
                        count(c, "SELECT COUNT(DISTINCT (sys_nm_en || '|' || purpose_type || '|' || md5(purpose_text))) FROM tb_service_purpose")};
            out.append("| tb_process_master | ").append(pm[0]).append(" | ").append(pm[1]).append(" |\n");
            out.append("| tb_service_purpose | ").append(sp[0]).append(" | ").append(sp[1]).append(" |\n\n");
            if (pm[1] != 5 || sp[1] != 5) { out.append("❌ HALT — DISTINCT != 5\n\n"); halt = true; }
            else out.append("✅ DISTINCT = 5 확인\n\n");

            // (2) 중복 그룹별 MIN PK
            out.append("## 2. 중복 그룹 MIN PK (보존 대상)\n\n### tb_process_master\n\n");
            renderTable(c, "SELECT sys_nm_en, process_name, MIN(process_id) AS keep_id, COUNT(*) AS dup_cnt " +
                "FROM tb_process_master GROUP BY sys_nm_en, process_name ORDER BY keep_id", out);
            out.append("\n### tb_service_purpose\n\n");
            renderTable(c, "SELECT sys_nm_en, purpose_type, MIN(purpose_id) AS keep_id, COUNT(*) AS dup_cnt " +
                "FROM tb_service_purpose GROUP BY sys_nm_en, purpose_type, purpose_text ORDER BY keep_id", out);

            // (3) NULL 체크
            out.append("## 3. 키 컬럼 NULL 존재\n\n");
            String nullSql =
                "SELECT 'tb_process_master.sys_nm_en' AS col, COUNT(*) FROM tb_process_master WHERE sys_nm_en IS NULL " +
                "UNION ALL SELECT 'tb_process_master.process_name', COUNT(*) FROM tb_process_master WHERE process_name IS NULL " +
                "UNION ALL SELECT 'tb_service_purpose.sys_nm_en', COUNT(*) FROM tb_service_purpose WHERE sys_nm_en IS NULL " +
                "UNION ALL SELECT 'tb_service_purpose.purpose_type', COUNT(*) FROM tb_service_purpose WHERE purpose_type IS NULL " +
                "UNION ALL SELECT 'tb_service_purpose.purpose_text', COUNT(*) FROM tb_service_purpose WHERE purpose_text IS NULL";
            int nullRows = renderTable(c, nullSql, out);
            long totalNull = countSum(c,
                "SELECT (SELECT COUNT(*) FROM tb_process_master WHERE sys_nm_en IS NULL OR process_name IS NULL) + " +
                "(SELECT COUNT(*) FROM tb_service_purpose WHERE sys_nm_en IS NULL OR purpose_type IS NULL OR purpose_text IS NULL)");
            if (totalNull > 0) { out.append("❌ HALT — NULL ").append(totalNull).append("건\n\n"); halt = true; }
            else out.append("✅ NULL 없음\n\n");

            // (4) 외부 FK
            out.append("## 4. 외부 FK\n\n");
            String fkSql =
                "SELECT tc.table_name AS referencing, tc.constraint_name, " +
                "       kcu.column_name, ccu.table_name AS target " +
                "  FROM information_schema.table_constraints tc " +
                "  JOIN information_schema.key_column_usage kcu " +
                "    ON tc.constraint_name = kcu.constraint_name AND tc.table_schema = kcu.table_schema " +
                "  JOIN information_schema.constraint_column_usage ccu " +
                "    ON tc.constraint_name = ccu.constraint_name AND tc.table_schema = ccu.table_schema " +
                " WHERE tc.constraint_type = 'FOREIGN KEY' AND tc.table_schema = 'public' " +
                "   AND ccu.table_name IN ('tb_process_master', 'tb_service_purpose') " +
                "   AND tc.table_name NOT IN ('tb_process_master', 'tb_service_purpose')";
            int fkCount = renderTable(c, fkSql, out);
            if (fkCount > 0) { out.append("❌ HALT — 외부 FK ").append(fkCount).append("건\n\n"); halt = true; }
            else out.append("✅ 외부 FK 없음\n\n");

            // (5) purpose_text 길이
            out.append("## 5. purpose_text 최대 길이\n\n");
            renderTable(c, "SELECT MAX(length(purpose_text)) AS max_len, MAX(octet_length(purpose_text)) AS max_bytes FROM tb_service_purpose", out);

            out.append("---\n\n## 최종 판정\n\n");
            out.append(halt ? "❌ **HALT**\n" : "✅ **PASS**\n");
        }

        try (FileWriter fw = new FileWriter("docs/exec-plans/process-master-precheck-result.md")) {
            fw.write(out.toString());
        }
        System.out.println(halt ? "FAIL (HALT)" : "PASS");
        if (halt) System.exit(1);
    }

    private static long count(Connection c, String sql) throws SQLException {
        try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery(sql)) { rs.next(); return rs.getLong(1); }
    }
    private static long countSum(Connection c, String sql) throws SQLException {
        try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery(sql)) { rs.next(); return rs.getLong(1); }
    }
    private static int renderTable(Connection c, String sql, StringBuilder out) throws SQLException {
        try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery(sql)) {
            ResultSetMetaData md = rs.getMetaData();
            int cols = md.getColumnCount();
            StringBuilder h = new StringBuilder("|");
            for (int i = 1; i <= cols; i++) h.append(' ').append(md.getColumnLabel(i)).append(" |");
            out.append(h).append("\n|");
            for (int i = 1; i <= cols; i++) out.append("---|");
            out.append("\n");
            int n = 0;
            while (rs.next()) {
                StringBuilder r = new StringBuilder("|");
                for (int i = 1; i <= cols; i++) {
                    String v = rs.getString(i);
                    r.append(' ').append(v == null ? "(null)" : v).append(" |");
                }
                out.append(r).append("\n"); n++;
            }
            out.append("\n총 ").append(n).append("행\n\n");
            return n;
        }
    }
}
