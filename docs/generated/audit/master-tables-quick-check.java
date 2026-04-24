import java.sql.*;

public class MasterTablesQuickCheck {
    public static void main(String[] args) throws Exception {
        String url = System.getenv().getOrDefault("DB_URL", "jdbc:postgresql://211.104.137.55:5881/SW_Dept");
        String user = System.getenv().getOrDefault("DB_USERNAME", "postgres");
        String pw = System.getenv("DB_PASSWORD");
        Class.forName("org.postgresql.Driver");
        try (Connection c = DriverManager.getConnection(url, user, pw)) {
            String[] masters = {
                "prj_types", "sys_mst", "cont_frm_mst", "maint_tp_mst",
                "cont_stat_mst", "sigungu_code", "sw_pjt", "ps_info", "users"
            };
            for (String t : masters) {
                System.out.println("\n### " + t);
                try (Statement st = c.createStatement()) {
                    // 존재·컬럼 확인
                    try (ResultSet rs = st.executeQuery(
                            "SELECT column_name, data_type, character_maximum_length " +
                            "FROM information_schema.columns WHERE table_name = '" + t + "' " +
                            "ORDER BY ordinal_position")) {
                        System.out.println("| column | type | len |");
                        boolean found = false;
                        while (rs.next()) {
                            found = true;
                            System.out.println("| " + rs.getString(1) + " | " + rs.getString(2)
                                + " | " + (rs.getString(3) == null ? "-" : rs.getString(3)) + " |");
                        }
                        if (!found) { System.out.println("❌ 테이블 없음"); continue; }
                    }
                    // row count
                    try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + t)) {
                        rs.next();
                        System.out.println("rows: " + rs.getLong(1));
                    }
                }
            }
        }
    }
}
