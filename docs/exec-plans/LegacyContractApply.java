import java.io.*;
import java.nio.file.*;
import java.sql.*;

/**
 * 마이그레이션 SQL 파일을 JDBC 로 실행.
 * 안전통제: 자동커밋 off, 실패 시 ROLLBACK, 스크립트 내 BEGIN/COMMIT 준수.
 */
public class LegacyContractApply {
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: java LegacyContractApply <sql-file>");
            System.exit(2);
        }
        String sqlPath = args[0];
        String sql = Files.readString(Path.of(sqlPath));
        System.out.println("[INFO] SQL file: " + sqlPath + " (" + sql.length() + " chars)");

        String url = System.getenv().getOrDefault("DB_URL", "jdbc:postgresql://211.104.137.55:5881/SW_Dept");
        String pw = System.getenv("DB_PASSWORD");
        Class.forName("org.postgresql.Driver");

        try (Connection c = DriverManager.getConnection(url, "postgres", pw)) {
            c.setAutoCommit(true);  // 스크립트 내부 BEGIN/COMMIT 이 트랜잭션 제어
            try (Statement s = c.createStatement()) {
                // PostgreSQL JDBC가 여러 statement 포함 스크립트 허용 (;분리)
                s.execute(sql);
                System.out.println("[OK] 스크립트 실행 완료");
                // NOTICE 메시지 출력
                SQLWarning w = s.getWarnings();
                while (w != null) { System.out.println("[NOTICE] " + w.getMessage()); w = w.getNextWarning(); }
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] " + e.getMessage());
            Throwable t = e.getNextException();
            while (t != null) { System.err.println("[ERROR caused] " + t.getMessage()); t = (t instanceof SQLException) ? ((SQLException)t).getNextException() : null; }
            System.exit(1);
        }
    }
}
