import java.io.*;
import java.sql.*;

/**
 * 멱등성 5체크 러너 — 서버 재시작마다 실행.
 * (a) COUNT(*) = 5
 * (b) COUNT(DISTINCT key) = 5
 * (c) 중복 그룹 = 0
 * (d) UNIQUE 제약/인덱스 존재
 * (e) 수동 INSERT ON CONFLICT 테스트 (row 증가 0)
 */
public class ProcessMasterIdempotencyCheck {
    public static void main(String[] args) throws Exception {
        String url = System.getenv().getOrDefault("DB_URL", "jdbc:postgresql://211.104.137.55:5881/SW_Dept");
        String pw = System.getenv("DB_PASSWORD");
        Class.forName("org.postgresql.Driver");
        String round = args.length > 0 ? args[0] : "?";
        boolean fail = false;
        StringBuilder out = new StringBuilder("# 재시작 #" + round + " 체크\n\n- 실행: " + new java.util.Date() + "\n\n");

        try (Connection c = DriverManager.getConnection(url, "postgres", pw)) {
            // (a) COUNT
            long cp = count(c, "SELECT COUNT(*) FROM tb_process_master");
            long cs = count(c, "SELECT COUNT(*) FROM tb_service_purpose");
            out.append("- (a) tb_process_master COUNT=").append(cp).append(cp == 5 ? " ✅" : " ❌").append("\n");
            out.append("- (a) tb_service_purpose COUNT=").append(cs).append(cs == 5 ? " ✅" : " ❌").append("\n");
            if (cp != 5 || cs != 5) fail = true;

            // (b) DISTINCT key
            long dp = count(c, "SELECT COUNT(DISTINCT (sys_nm_en || '|' || process_name)) FROM tb_process_master");
            long ds = count(c, "SELECT COUNT(DISTINCT (sys_nm_en || '|' || purpose_type || '|' || md5(purpose_text))) FROM tb_service_purpose");
            out.append("- (b) tb_process_master DISTINCT=").append(dp).append(dp == 5 ? " ✅" : " ❌").append("\n");
            out.append("- (b) tb_service_purpose DISTINCT=").append(ds).append(ds == 5 ? " ✅" : " ❌").append("\n");
            if (dp != 5 || ds != 5) fail = true;

            // (c) 중복 그룹
            long gp = count(c, "SELECT COUNT(*) FROM (SELECT 1 FROM tb_process_master GROUP BY sys_nm_en, process_name HAVING COUNT(*) > 1) x");
            long gs = count(c, "SELECT COUNT(*) FROM (SELECT 1 FROM tb_service_purpose GROUP BY sys_nm_en, purpose_type, purpose_text HAVING COUNT(*) > 1) x");
            out.append("- (c) tb_process_master dup groups=").append(gp).append(gp == 0 ? " ✅" : " ❌").append("\n");
            out.append("- (c) tb_service_purpose dup groups=").append(gs).append(gs == 0 ? " ✅" : " ❌").append("\n");
            if (gp != 0 || gs != 0) fail = true;

            // (d) UNIQUE 존재
            long u1 = count(c, "SELECT COUNT(*) FROM pg_constraint WHERE conname = 'uq_process_master_sys_name'");
            long u2 = count(c, "SELECT COUNT(*) FROM pg_indexes WHERE indexname = 'uq_service_purpose_sys_type_md5'");
            out.append("- (d) UNIQUE constraint(process)=").append(u1).append(u1 == 1 ? " ✅" : " ❌").append("\n");
            out.append("- (d) UNIQUE index(purpose)=").append(u2).append(u2 == 1 ? " ✅" : " ❌").append("\n");
            if (u1 != 1 || u2 != 1) fail = true;

            // (e) 수동 INSERT 차단 테스트
            try (Statement s = c.createStatement()) {
                s.execute("BEGIN");
                int r1 = s.executeUpdate(
                    "INSERT INTO tb_process_master (sys_nm_en, process_name, sort_order) " +
                    "VALUES ('UPIS', '도시계획정보체계용 GIS SW 유지관리', 1) " +
                    "ON CONFLICT (sys_nm_en, process_name) DO NOTHING");
                int r2 = s.executeUpdate(
                    "INSERT INTO tb_service_purpose (sys_nm_en, purpose_type, purpose_text, sort_order) " +
                    "VALUES ('UPIS', 'PURPOSE', '도시계획정보체계(UPIS)의 최신 버전 유지와 원활한 서비스를 제공', 1) " +
                    "ON CONFLICT (sys_nm_en, purpose_type, md5(purpose_text)) DO NOTHING");
                s.execute("ROLLBACK");
                out.append("- (e) 수동 INSERT process r1=").append(r1).append(r1 == 0 ? " ✅" : " ❌").append("\n");
                out.append("- (e) 수동 INSERT purpose r2=").append(r2).append(r2 == 0 ? " ✅" : " ❌").append("\n");
                if (r1 != 0 || r2 != 0) fail = true;
            }

            out.append("\n## 결과\n\n").append(fail ? "❌ FAIL" : "✅ PASS").append("\n");
        }

        try (FileWriter fw = new FileWriter("docs/exec-plans/process-master-idempotency-result.md", true)) {
            fw.write(out.toString() + "\n---\n\n");
        }
        System.out.println("Round " + round + ": " + (fail ? "FAIL" : "PASS"));
        if (fail) System.exit(1);
    }

    private static long count(Connection c, String sql) throws SQLException {
        try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery(sql)) { rs.next(); return rs.getLong(1); }
    }
}
