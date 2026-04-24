import java.nio.file.*;
import java.sql.*;

/**
 * S5 access-log-userid-fix · Step 5 V019 실행기
 *
 * 사용:
 *   java -cp ".;<jar>" -Dfile.encoding=UTF-8 access_log_userid_apply <sql_file> <run_id>
 *
 * 동작:
 *  1. SQL 파일 읽기
 *  2. :run_id placeholder 치환
 *  3. 단일 Statement.execute() 로 전체 트랜잭션 실행
 *     - V019 는 BEGIN; ... COMMIT; 자체 포함
 *     - 트랜잭션 내 EXCEPTION 시 자동 ROLLBACK (PostgreSQL 보장)
 */
public class access_log_userid_apply {

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("[ERR] usage: access_log_userid_apply <sql_file> <run_id>");
            System.exit(2);
        }
        String sqlFile = args[0];
        String runId   = args[1];

        String url  = "jdbc:postgresql://211.104.137.55:5881/SW_Dept";
        String user = "postgres";
        String pass = System.getenv("DB_PASSWORD");
        if (pass == null || pass.isEmpty()) {
            System.err.println("[ERR] DB_PASSWORD 환경변수 없음");
            System.exit(2);
        }

        // SQL 로드 + placeholder 치환
        String script = new String(Files.readAllBytes(Paths.get(sqlFile)),
                java.nio.charset.StandardCharsets.UTF_8);
        script = script.replace(":run_id", runId);

        System.out.println("[V019] run_id=" + runId);
        System.out.println("[V019] SQL bytes: " + script.length());
        System.out.println("[V019] connecting...");

        try (Connection c = DriverManager.getConnection(url, user, pass)) {
            // 자동커밋 끔 — V019 가 자체 BEGIN/COMMIT 관리
            c.setAutoCommit(true); // 단일 execute 로 BEGIN/COMMIT 포함된 스크립트 통째 실행

            // PostgreSQL NOTICE 캡처
            try (Statement notice = c.createStatement()) {
                // Get NOTICE warnings
                Connection conn = c;
                conn.setClientInfo("ApplicationName", "V019_runner");
            }

            try (Statement s = c.createStatement()) {
                System.out.println("[V019] EXECUTING...");
                s.execute(script);

                // SQLWarning (NOTICE) 출력
                SQLWarning w = s.getWarnings();
                while (w != null) {
                    System.out.println("[NOTICE] " + w.getMessage());
                    w = w.getNextWarning();
                }
            }

            System.out.println("[V019] ✅ COMMITTED successfully");

            // 사후 검증: orphan 잔존 0
            try (Statement s = c.createStatement();
                 ResultSet rs = s.executeQuery(
                    "SELECT COUNT(*) AS n FROM access_logs " +
                    "WHERE userid IN ('박욱진','관리자')")) {
                rs.next();
                int remaining = rs.getInt("n");
                System.out.println("[V019] orphan 잔존: " + remaining + (remaining == 0 ? " ✅" : " ❌"));
            }

            // 백업 row count
            String backupName = "access_logs_cleanup_backup_" + runId;
            try (Statement s = c.createStatement();
                 ResultSet rs = s.executeQuery("SELECT COUNT(*) AS n FROM " + backupName)) {
                rs.next();
                System.out.println("[V019] 백업 테이블: " + backupName + " (rows=" + rs.getInt("n") + ")");
            }
        } catch (SQLException e) {
            System.err.println("[V019] ❌ FAILED: " + e.getMessage());
            System.err.println("[V019] SQLState: " + e.getSQLState());
            // PostgreSQL 트랜잭션 내 EXCEPTION 시 자동 ROLLBACK 됨
            System.exit(1);
        }
    }
}
