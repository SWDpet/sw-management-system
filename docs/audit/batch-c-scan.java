import java.io.*;
import java.sql.*;

public class BatchCScan {
    public static void main(String[] args) throws Exception {
        Class.forName("org.postgresql.Driver");
        try (Connection c = DriverManager.getConnection(
                "jdbc:postgresql://211.104.137.55:5881/SW_Dept", "postgres", System.getenv("DB_PASSWORD"));
             PrintWriter out = new PrintWriter(new FileWriter("docs/audit/batch-c-scan-result.md"))) {
            c.setReadOnly(true);
            try (Statement s = c.createStatement()) {
                s.execute("SET SESSION statement_timeout = '30s'");
            }
            out.println("# Batch C 실데이터 분포\n");
            String[][] qs = {
                // === QR 코드 ===
                {"qr_license 레코드 수",
                 "SELECT COUNT(*) AS total FROM qr_license"},
                {"qr_license.license_type distinct",
                 "SELECT license_type, COUNT(*) AS cnt FROM qr_license GROUP BY license_type ORDER BY cnt DESC LIMIT 20"},
                {"qr_license.license_type_ko distinct",
                 "SELECT license_type_ko, COUNT(*) AS cnt FROM qr_license GROUP BY license_type_ko ORDER BY cnt DESC LIMIT 20"},
                {"qr_license.issued_by distinct (내부직원 users 참조?)",
                 "SELECT issued_by, COUNT(*) AS cnt FROM qr_license GROUP BY issued_by ORDER BY cnt DESC LIMIT 20"},

                // === Access Logs ===
                {"access_logs 레코드 수",
                 "SELECT COUNT(*) AS total FROM access_logs"},
                {"access_logs.menu_nm distinct (MenuName 상수 참조?)",
                 "SELECT menu_nm, COUNT(*) AS cnt FROM access_logs GROUP BY menu_nm ORDER BY cnt DESC LIMIT 30"},
                {"access_logs.action_type distinct",
                 "SELECT action_type, COUNT(*) AS cnt FROM access_logs GROUP BY action_type ORDER BY cnt DESC LIMIT 20"},
                {"access_logs.userid vs users.userid 매칭",
                 "SELECT a.userid, " +
                 "CASE WHEN u.user_id IS NULL THEN '❌ NOT IN users' ELSE '✅' END AS match, " +
                 "COUNT(*) AS cnt " +
                 "FROM access_logs a LEFT JOIN users u ON a.userid = u.userid " +
                 "GROUP BY a.userid, u.user_id ORDER BY cnt DESC LIMIT 20"},

                // === System Graph / ERD (주로 동적 뷰, 별도 테이블 없을 수도) ===
                {"테이블명 패턴 system/graph/menu",
                 "SELECT table_name FROM information_schema.tables " +
                 "WHERE table_schema = current_schema() AND table_type = 'BASE TABLE' " +
                 "AND (table_name LIKE '%system%' OR table_name LIKE '%graph%' OR table_name LIKE '%menu%' " +
                 "OR table_name LIKE '%notif%' OR table_name LIKE '%batch%' OR table_name LIKE '%_cd' OR table_name LIKE '%_code' " +
                 "OR table_name LIKE '%mst' OR table_name LIKE '%_tp' OR table_name LIKE '%setting%' OR table_name LIKE '%config%') " +
                 "ORDER BY table_name"},

                // === 기타 테이블 전수 (감사 외 영역 식별용) ===
                {"감사 대상 후보 테이블 — 전수 목록 (마스터/기초 4개 기능 제외 후보)",
                 "SELECT table_name FROM information_schema.tables " +
                 "WHERE table_schema = current_schema() AND table_type = 'BASE TABLE' " +
                 "AND table_name NOT IN ('prj_types','sys_mst','cont_frm_mst','maint_tp_mst','cont_stat_mst','sigungu_code'," +
                 "'sw_pjt','ps_info','users','org_unit'," +
                 "'tb_infra_master','tb_infra_server','tb_infra_software','tb_infra_memo'," +
                 "'tb_infra_link_upis','tb_infra_link_api'," +
                 "'tb_contract_participant') " +
                 "ORDER BY table_name"},

                // === tb_document 관련 (Batch A에서 이미 일부 확인, 누락 컬럼 검증) ===
                {"tb_document 상세 속성 (region_code, status 등 Batch A에서 건드리지 않은 컬럼)",
                 "SELECT column_name, data_type FROM information_schema.columns WHERE table_name = 'tb_document' ORDER BY ordinal_position"}
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
