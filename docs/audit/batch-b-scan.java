import java.io.*;
import java.sql.*;

public class BatchBScan {
    public static void main(String[] args) throws Exception {
        Class.forName("org.postgresql.Driver");
        try (Connection c = DriverManager.getConnection(
                "jdbc:postgresql://211.104.137.55:5881/SW_Dept", "postgres", System.getenv("DB_PASSWORD"));
             PrintWriter out = new PrintWriter(new FileWriter("docs/audit/batch-b-scan-result.md"))) {
            c.setReadOnly(true);
            try (Statement s = c.createStatement()) {
                s.execute("SET SESSION statement_timeout = '30s'");
                s.execute("SET SESSION lock_timeout = '5s'");
            }

            out.println("# 배치 B 실데이터 분포 — 견적서·라이선스\n");

            String[][] qs = {
                // === 견적서 ===
                {"qt_quotation 레코드 수 + 상태·분류 분포",
                 "SELECT status, category, COUNT(*) AS cnt FROM qt_quotation GROUP BY status, category ORDER BY cnt DESC LIMIT 30"},
                {"qt_quotation.category distinct",
                 "SELECT category, COUNT(*) AS cnt FROM qt_quotation GROUP BY category ORDER BY cnt DESC"},
                {"qt_quotation.status distinct",
                 "SELECT status, COUNT(*) AS cnt FROM qt_quotation GROUP BY status ORDER BY cnt DESC"},
                {"qt_quotation.template_type distinct",
                 "SELECT template_type, COUNT(*) AS cnt FROM qt_quotation GROUP BY template_type ORDER BY cnt DESC"},
                {"qt_quotation_item 레코드 수 + 단위 distinct",
                 "SELECT unit, COUNT(*) AS cnt FROM qt_quotation_item GROUP BY unit ORDER BY cnt DESC LIMIT 30"},
                {"qt_product_pattern 레코드 수",
                 "SELECT COUNT(*) AS total FROM qt_product_pattern"},
                {"qt_product_pattern.category distinct",
                 "SELECT category, COUNT(*) AS cnt FROM qt_product_pattern GROUP BY category ORDER BY cnt DESC LIMIT 20"},
                {"qt_wage_rate 레코드 수 + 등급 distinct",
                 "SELECT tech_grade, COUNT(*) AS cnt FROM qt_wage_rate GROUP BY tech_grade ORDER BY cnt DESC LIMIT 20"},
                {"qt_remarks_pattern 레코드 수",
                 "SELECT COUNT(*) AS total FROM qt_remarks_pattern"},
                {"qt_quotation_ledger 레코드 수",
                 "SELECT COUNT(*) AS total FROM qt_quotation_ledger"},
                {"qt_quote_number_seq",
                 "SELECT * FROM qt_quote_number_seq LIMIT 10"},
                // qt_quotation과 sw_pjt/프로젝트 연관 여부
                {"qt_quotation 컬럼 조회",
                 "SELECT column_name, data_type FROM information_schema.columns " +
                 "WHERE table_name = 'qt_quotation' ORDER BY ordinal_position"},

                // === 라이선스 ===
                {"license_registry 레코드 수",
                 "SELECT COUNT(*) AS total FROM license_registry"},
                {"license_registry.license_type distinct",
                 "SELECT license_type, COUNT(*) AS cnt FROM license_registry GROUP BY license_type ORDER BY cnt DESC LIMIT 20"},
                {"license_registry.generation_source distinct",
                 "SELECT generation_source, COUNT(*) AS cnt FROM license_registry GROUP BY generation_source ORDER BY cnt DESC LIMIT 20"},
                {"license_registry.country distinct",
                 "SELECT country, COUNT(*) AS cnt FROM license_registry GROUP BY country ORDER BY cnt DESC LIMIT 20"},
                {"license_registry.valid_product_edition distinct",
                 "SELECT valid_product_edition, COUNT(*) AS cnt FROM license_registry GROUP BY valid_product_edition ORDER BY cnt DESC LIMIT 20"},
                {"license_registry.product_id distinct",
                 "SELECT product_id, COUNT(*) AS cnt FROM license_registry GROUP BY product_id ORDER BY cnt DESC LIMIT 20"},
                {"license_upload_history 레코드 수",
                 "SELECT COUNT(*) AS total FROM license_upload_history"},

                // === GeoNURIS ===
                {"geonuris_license 레코드 수",
                 "SELECT COUNT(*) AS total FROM geonuris_license"},
                {"geonuris_license.license_type distinct",
                 "SELECT license_type, COUNT(*) AS cnt FROM geonuris_license GROUP BY license_type ORDER BY cnt DESC LIMIT 20"},
                {"geonuris_license 컬럼",
                 "SELECT column_name, data_type FROM information_schema.columns " +
                 "WHERE table_name = 'geonuris_license' ORDER BY ordinal_position"},

                // === 마스터 매칭 가능성 ===
                {"qt_quotation.category vs prj_types 매칭",
                 "SELECT q.category AS value, COUNT(*) AS cnt, " +
                 "CASE WHEN pt.cd IS NULL THEN '❌' ELSE '✅' END AS match " +
                 "FROM qt_quotation q LEFT JOIN prj_types pt ON q.category = pt.cd OR q.category = pt.nm " +
                 "GROUP BY q.category, pt.cd ORDER BY cnt DESC LIMIT 30"},
                {"qt_wage_rate.tech_grade 샘플",
                 "SELECT tech_grade, wage_amount FROM qt_wage_rate ORDER BY tech_grade LIMIT 20"}
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
