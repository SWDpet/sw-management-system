import java.io.*;
import java.sql.*;
import java.util.*;

/**
 * 데이터 아키텍처 감사 SQL 러너 (1회용).
 *
 * 근거:
 *  - 기획서: docs/plans/data-architecture-audit.md v2
 *  - 개발계획서: docs/dev-plans/data-architecture-audit.md v2 §1 Step 2-1
 *
 * 안전통제 (기획서 §7-5 5개 하위통제):
 *  1) 권한: SELECT 전용. 본 파일 내 UPDATE/DELETE/INSERT/DROP/ALTER/TRUNCATE 키워드 0개.
 *  2) 런타임: SET TRANSACTION READ ONLY + statement_timeout 30s + lock_timeout 5s
 *  3) Allowlist: 모든 SQL을 static final String[] QUERIES 로 고정. 애드혹 금지.
 *  4) PII 보호: users/ps_info 샘플은 LEFT + *** 마스킹
 *  5) 실행환경: 업무시간 외 권장 (시작 시각을 산출물 상단에 기록)
 *
 * 실행:
 *   JAR=~/.m2/repository/org/postgresql/postgresql/42.7.4/postgresql-42.7.4.jar
 *   DB_PASSWORD=XXX java -cp "$JAR" docs/audit/data-architecture-scan.java <output_dir>
 */
public class DataArchitectureScan {

    // ============================================================
    // SQL Allowlist (§7-5-3)
    // ============================================================

    /** Phase 1-1: 전수 스키마 덤프 */
    private static final String Q_SCHEMA =
        "SELECT table_name, column_name, data_type, character_maximum_length, " +
        "       is_nullable, column_default " +
        "  FROM information_schema.columns " +
        " WHERE table_schema = current_schema() " +
        " ORDER BY table_name, ordinal_position";

    /** Phase 1-2: 테이블별 레코드 수 */
    private static final String Q_TABLE_ROWCOUNTS =
        "SELECT table_name FROM information_schema.tables " +
        " WHERE table_schema = current_schema() AND table_type = 'BASE TABLE' " +
        " ORDER BY table_name";

    /** 권한 확인 */
    private static final String Q_PRIVS =
        "SELECT DISTINCT privilege_type FROM information_schema.table_privileges " +
        " WHERE grantee = CURRENT_USER AND table_schema = current_schema() LIMIT 20";

    // 9개 마스터 각각 전체 덤프 (PII 컬럼은 아래 Q_USERS_SAMPLE / Q_PSINFO_SAMPLE 별도 처리)
    private static final String[][] Q_MASTERS = {
        {"prj_types", "SELECT cd, nm FROM prj_types ORDER BY cd LIMIT 1000"},
        {"sys_mst", "SELECT cd, nm FROM sys_mst ORDER BY cd LIMIT 1000"},
        {"cont_frm_mst", "SELECT cd, nm FROM cont_frm_mst ORDER BY cd LIMIT 1000"},
        {"maint_tp_mst", "SELECT cd, nm FROM maint_tp_mst ORDER BY cd LIMIT 1000"},
        {"cont_stat_mst", "SELECT cd, nm FROM cont_stat_mst ORDER BY cd LIMIT 1000"},
        {"sigungu_code_sample",
            "SELECT instt_c, adm_sect_c, full_name, sido_name, sgg_name FROM sigungu_code ORDER BY adm_sect_c LIMIT 20"},
        {"sw_pjt_sample",
            "SELECT proj_id, year, sys_nm_en, biz_cat_en, cont_type, stat, maint_type, " +
            "       LEFT(proj_nm, 30) AS proj_nm_head FROM sw_pjt ORDER BY proj_id DESC LIMIT 10"}
    };

    /** PII 마스킹 — users 샘플 */
    private static final String Q_USERS_SAMPLE =
        "SELECT user_id, " +
        "       LEFT(COALESCE(userid, ''), 2) || '***' AS userid_masked, " +
        "       LEFT(COALESCE(username, ''), 1) || '***' AS name_masked, " +
        "       LEFT(COALESCE(email, ''), 2) || '***' AS email_masked, " +
        "       user_role, enabled " +
        "  FROM users ORDER BY user_id LIMIT 3";

    /** PII 마스킹 — ps_info 샘플 (컬럼은 실행 시 조정) */
    private static final String Q_PSINFO_SAMPLE =
        "SELECT id, sys_nm_en, " +
        "       LEFT(COALESCE(org_nm, ''), 3) || '***' AS org_nm_masked " +
        "  FROM ps_info ORDER BY id LIMIT 3";

    /** 마스터 활용 — 각 비즈니스 테이블의 코드 컬럼이 마스터와 매칭되는지 */
    private static final String[][] Q_MATCH_ANALYSIS = {
        // 감사 대상: tb_document
        {"tb_document_status_vs_cont_stat_mst",
            "SELECT d.status AS value, COUNT(*) AS cnt, " +
            "       CASE WHEN m.cd IS NULL THEN '❌ NOT IN cont_stat_mst' ELSE '✅' END AS match " +
            "  FROM tb_document d LEFT JOIN cont_stat_mst m ON d.status = m.cd " +
            " WHERE d.status IS NOT NULL " +
            " GROUP BY d.status, m.cd ORDER BY cnt DESC LIMIT 100"},
        {"tb_document_doc_type_vs_maint_tp_mst",
            "SELECT d.doc_type AS value, COUNT(*) AS cnt, " +
            "       CASE WHEN m.cd IS NULL THEN '❌ NOT IN maint_tp_mst' ELSE '✅' END AS match " +
            "  FROM tb_document d LEFT JOIN maint_tp_mst m ON d.doc_type = m.cd " +
            " WHERE d.doc_type IS NOT NULL " +
            " GROUP BY d.doc_type, m.cd ORDER BY cnt DESC LIMIT 100"},
        {"tb_document_sys_type_vs_sys_mst",
            "SELECT d.sys_type AS value, COUNT(*) AS cnt, " +
            "       CASE WHEN m.cd IS NULL THEN '❌ NOT IN sys_mst' ELSE '✅' END AS match " +
            "  FROM tb_document d LEFT JOIN sys_mst m ON d.sys_type = m.cd " +
            " WHERE d.sys_type IS NOT NULL " +
            " GROUP BY d.sys_type, m.cd ORDER BY cnt DESC LIMIT 100"},
        {"inspect_report_status_vs_cont_stat_mst",
            "SELECT r.status AS value, COUNT(*) AS cnt, " +
            "       CASE WHEN m.cd IS NULL THEN '❌ NOT IN cont_stat_mst' ELSE '✅' END AS match " +
            "  FROM inspect_report r LEFT JOIN cont_stat_mst m ON r.status = m.cd " +
            " WHERE r.status IS NOT NULL " +
            " GROUP BY r.status, m.cd ORDER BY cnt DESC LIMIT 100"},
        {"inspect_report_sys_type_vs_sys_mst",
            "SELECT r.sys_type AS value, COUNT(*) AS cnt, " +
            "       CASE WHEN m.cd IS NULL THEN '❌ NOT IN sys_mst' ELSE '✅' END AS match " +
            "  FROM inspect_report r LEFT JOIN sys_mst m ON r.sys_type = m.cd " +
            " WHERE r.sys_type IS NOT NULL " +
            " GROUP BY r.sys_type, m.cd ORDER BY cnt DESC LIMIT 100"},
        {"sw_pjt_cont_type_vs_cont_frm_mst",
            "SELECT p.cont_type AS value, COUNT(*) AS cnt, " +
            "       CASE WHEN m.cd IS NULL THEN '❌ NOT IN cont_frm_mst' ELSE '✅' END AS match " +
            "  FROM sw_pjt p LEFT JOIN cont_frm_mst m ON p.cont_type = m.cd " +
            " WHERE p.cont_type IS NOT NULL " +
            " GROUP BY p.cont_type, m.cd ORDER BY cnt DESC LIMIT 100"},
        {"sw_pjt_stat_vs_cont_stat_mst",
            "SELECT p.stat AS value, COUNT(*) AS cnt, " +
            "       CASE WHEN m.cd IS NULL THEN '❌ NOT IN cont_stat_mst' ELSE '✅' END AS match " +
            "  FROM sw_pjt p LEFT JOIN cont_stat_mst m ON p.stat = m.cd " +
            " WHERE p.stat IS NOT NULL " +
            " GROUP BY p.stat, m.cd ORDER BY cnt DESC LIMIT 100"},
        {"sw_pjt_maint_type_vs_maint_tp_mst",
            "SELECT p.maint_type AS value, COUNT(*) AS cnt, " +
            "       CASE WHEN m.cd IS NULL THEN '❌ NOT IN maint_tp_mst' ELSE '✅' END AS match " +
            "  FROM sw_pjt p LEFT JOIN maint_tp_mst m ON p.maint_type = m.cd " +
            " WHERE p.maint_type IS NOT NULL " +
            " GROUP BY p.maint_type, m.cd ORDER BY cnt DESC LIMIT 100"},
        {"sw_pjt_biz_cat_en_distinct",
            "SELECT biz_cat_en AS value, COUNT(*) AS cnt FROM sw_pjt " +
            " WHERE biz_cat_en IS NOT NULL GROUP BY biz_cat_en ORDER BY cnt DESC LIMIT 100"},
        // 인프라
        {"tb_infra_master_sys_nm_en_vs_sys_mst",
            "SELECT i.sys_nm_en AS value, COUNT(*) AS cnt, " +
            "       CASE WHEN m.cd IS NULL THEN '❌' ELSE '✅' END AS match " +
            "  FROM tb_infra_master i LEFT JOIN sys_mst m ON i.sys_nm_en = m.cd " +
            " WHERE i.sys_nm_en IS NOT NULL " +
            " GROUP BY i.sys_nm_en, m.cd ORDER BY cnt DESC LIMIT 100"}
    };

    public static void main(String[] args) throws Exception {
        String outDir = (args.length > 0 ? args[0] : "docs/audit");
        new File(outDir).mkdirs();

        String url = System.getenv().getOrDefault("DB_URL", "jdbc:postgresql://211.104.137.55:5881/SW_Dept");
        String user = System.getenv().getOrDefault("DB_USERNAME", "postgres");
        String pw = System.getenv("DB_PASSWORD");
        if (pw == null || pw.isBlank()) { System.err.println("ERROR: DB_PASSWORD env required"); System.exit(2); }
        Class.forName("org.postgresql.Driver");

        long tsStart = System.currentTimeMillis();
        String startedAt = new java.util.Date().toString();

        try (Connection c = DriverManager.getConnection(url, user, pw)) {
            // §7-5-2 런타임 통제
            try (Statement s = c.createStatement()) {
                s.execute("SET SESSION statement_timeout = '30s'");
                s.execute("SET SESSION lock_timeout = '5s'");
            }
            c.setReadOnly(true);

            // 권한 확인
            List<String> privs = listOne(c, Q_PRIVS);
            System.out.println("[INFO] Current user privileges: " + privs);

            // 1) 전수 스키마 덤프
            writeSchemaFull(c, outDir + "/db-schema-full.md", startedAt);

            // 2) 마스터 인벤토리
            writeMasterInventory(c, outDir + "/data-architecture-master-inventory.md", startedAt);
        }

        long elapsed = (System.currentTimeMillis() - tsStart) / 1000;
        System.out.println("\n[DONE] Phase 1 scan complete in " + elapsed + "s");
        System.out.println("       docs/audit/db-schema-full.md");
        System.out.println("       docs/audit/data-architecture-master-inventory.md");
    }

    private static List<String> listOne(Connection c, String sql) throws SQLException {
        List<String> out = new ArrayList<>();
        try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) out.add(rs.getString(1));
        }
        return out;
    }

    private static void writeSchemaFull(Connection c, String path, String startedAt) throws Exception {
        try (PrintWriter out = new PrintWriter(new FileWriter(path))) {
            out.println("# DB 전수 스키마 덤프");
            out.println();
            out.println("- 실행일: " + startedAt);
            out.println("- DB: SW_Dept @ PostgreSQL");
            out.println("- 감사 스프린트: `data-architecture-audit` (Phase 1)");
            out.println();
            out.println("## 테이블·컬럼 목록 (information_schema)");
            out.println();

            // 스키마 덤프
            Map<String, List<String[]>> perTable = new LinkedHashMap<>();
            try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(Q_SCHEMA)) {
                while (rs.next()) {
                    String t = rs.getString("table_name");
                    perTable.computeIfAbsent(t, k -> new ArrayList<>()).add(new String[]{
                        rs.getString("column_name"),
                        rs.getString("data_type"),
                        String.valueOf(rs.getObject("character_maximum_length")),
                        rs.getString("is_nullable"),
                        String.valueOf(rs.getObject("column_default"))
                    });
                }
            }

            // 레코드 수
            Map<String, Long> rowCounts = new HashMap<>();
            List<String> tables = listOne(c, Q_TABLE_ROWCOUNTS);
            for (String t : tables) {
                try (Statement st = c.createStatement();
                     ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + t)) {
                    if (rs.next()) rowCounts.put(t, rs.getLong(1));
                } catch (SQLException e) { rowCounts.put(t, -1L); }
            }

            out.println("총 " + perTable.size() + "개 테이블.");
            out.println();

            for (Map.Entry<String, List<String[]>> e : perTable.entrySet()) {
                String t = e.getKey();
                out.println("### " + t + "  (rows: " + rowCounts.getOrDefault(t, -1L) + ")");
                out.println();
                out.println("| column | type | len | null | default |");
                out.println("|---|---|---|---|---|");
                for (String[] col : e.getValue()) {
                    out.println("| " + col[0] + " | " + col[1] + " | "
                        + ("null".equals(col[2]) ? "-" : col[2]) + " | " + col[3]
                        + " | " + ("null".equals(col[4]) ? "-" : col[4]) + " |");
                }
                out.println();
            }
        }
    }

    private static void writeMasterInventory(Connection c, String path, String startedAt) throws Exception {
        try (PrintWriter out = new PrintWriter(new FileWriter(path))) {
            out.println("# 기초 마스터 인벤토리 (Phase 1 산출)");
            out.println();
            out.println("- 실행일: " + startedAt);
            out.println("- 근거: 사용자 확정 9개 마스터 (2026-04-20)");
            out.println();
            out.println("## A. 공통 마스터 레이어");
            out.println();

            for (String[] q : Q_MASTERS) {
                String label = q[0];
                String sql = q[1];
                out.println("### " + label);
                out.println();
                runAndPrintTable(c, sql, out);
                out.println();
            }

            // PII 마스킹 — users / ps_info (§7-5-4)
            out.println("## B. 기초 기능 소유 마스터 (PII 마스킹 샘플)");
            out.println();
            out.println("### users (샘플 3건, PII 마스킹)");
            out.println();
            runAndPrintTable(c, Q_USERS_SAMPLE, out);
            out.println();

            out.println("### ps_info (샘플 3건, PII 마스킹)");
            out.println();
            runAndPrintTable(c, Q_PSINFO_SAMPLE, out);
            out.println();

            // 마스터 매칭 분석
            out.println("## C. 마스터 매칭 분석 (Phase 2 입력)");
            out.println();
            for (String[] q : Q_MATCH_ANALYSIS) {
                out.println("### " + q[0]);
                out.println();
                try {
                    runAndPrintTable(c, q[1], out);
                } catch (SQLException ex) {
                    out.println("❌ 쿼리 실패 (테이블/컬럼 부재 가능): " + ex.getMessage());
                }
                out.println();
            }
        }
    }

    private static void runAndPrintTable(Connection c, String sql, PrintWriter out) throws SQLException {
        try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
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
                    if (v == null) v = "(null)";
                    v = v.replace("|", "\\|").replace("\n", " ");
                    r.append(' ').append(v).append(" |");
                }
                out.println(r);
                n++;
            }
            out.println();
            out.println("총 " + n + "행");
        }
    }
}
