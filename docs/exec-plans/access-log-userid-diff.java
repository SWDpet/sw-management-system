import java.sql.*;

/**
 * S5 access-log-userid-fix · Step 2-2 사후 Diff 리포트 (READ ONLY)
 *
 * V019 실행 후 호출. 백업 테이블 vs 현재 access_logs 의 변경사항 집계.
 *
 * 실행:
 *   javac -encoding UTF-8 access_log_userid_diff.java
 *   java -cp ".;<jar>" -Dfile.encoding=UTF-8 access_log_userid_diff <run_id>
 *
 * 출력: docs/exec-plans/access-log-userid-diff-result.md (수동 저장)
 */
public class access_log_userid_diff {

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("[ERR] usage: access_log_userid_diff <run_id>");
            System.exit(2);
        }
        String runId = args[0];
        String backupTable = "access_logs_cleanup_backup_" + runId;

        String url  = "jdbc:postgresql://211.104.137.55:5881/SW_Dept";
        String user = "postgres";
        String pass = System.getenv("DB_PASSWORD");
        if (pass == null || pass.isEmpty()) {
            System.err.println("[ERR] DB_PASSWORD 환경변수 없음");
            System.exit(2);
        }

        try (Connection c = DriverManager.getConnection(url, user, pass)) {
            try (Statement s = c.createStatement()) {
                s.execute("SET TRANSACTION READ ONLY");
                s.execute("SET statement_timeout = 10000");
            }

            System.out.println("# V019 사후 Diff 리포트");
            System.out.println();
            System.out.println("- run_id: `" + runId + "`");
            System.out.println("- 백업 테이블: `" + backupTable + "`");
            System.out.println("- 실행시각: " + new java.util.Date());
            System.out.println();

            // (1) 백업 테이블 존재 확인
            String qExists = "SELECT to_regclass('public." + backupTable + "') AS reg";
            try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery(qExists)) {
                if (rs.next() && rs.getString("reg") == null) {
                    System.out.println("**ERROR: 백업 테이블이 존재하지 않습니다.**");
                    System.exit(1);
                }
            }

            // (2) 백업 테이블 분포
            System.out.println("## 백업 (변경 전) 분포");
            System.out.println();
            System.out.println("| userid | cnt |");
            System.out.println("|--------|----:|");
            try (Statement s = c.createStatement();
                 ResultSet rs = s.executeQuery(
                    "SELECT userid, COUNT(*) AS cnt FROM " + backupTable +
                    " GROUP BY userid ORDER BY cnt DESC")) {
                while (rs.next()) {
                    System.out.printf("| %s | %d |%n", rs.getString("userid"), rs.getInt("cnt"));
                }
            }
            System.out.println();

            // (3) 현재 access_logs 분포 (orphan 잔존 + 매핑된 userid 증가량)
            System.out.println("## 현재 access_logs 의 매핑 결과 검증");
            System.out.println();
            System.out.println("| 검증 항목 | 결과 |");
            System.out.println("|----------|-----:|");

            // 3-1. orphan 잔존
            try (Statement s = c.createStatement();
                 ResultSet rs = s.executeQuery(
                    "SELECT COUNT(*) AS n FROM access_logs WHERE userid IN ('박욱진','관리자')")) {
                if (rs.next()) {
                    int n = rs.getInt("n");
                    System.out.printf("| orphan 잔존 (`박욱진`/`관리자`) | %d %s |%n",
                            n, n == 0 ? "✅" : "❌");
                }
            }

            // 3-2. admin / ukjin914 의 백업 분량 통합 확인
            int backupKwanli = countBackup(c, backupTable, "관리자");
            int backupParkUkjin = countBackup(c, backupTable, "박욱진");

            try (Statement s = c.createStatement();
                 ResultSet rs = s.executeQuery(
                    "SELECT COUNT(*) AS n FROM access_logs WHERE userid='admin'")) {
                if (rs.next()) {
                    System.out.printf("| `admin` 현재 건수 (이전 + 관리자 %d 흡수) | %d |%n",
                            backupKwanli, rs.getInt("n"));
                }
            }
            try (Statement s = c.createStatement();
                 ResultSet rs = s.executeQuery(
                    "SELECT COUNT(*) AS n FROM access_logs WHERE userid='ukjin914'")) {
                if (rs.next()) {
                    System.out.printf("| `ukjin914` 현재 건수 (이전 + 박욱진 %d 흡수) | %d |%n",
                            backupParkUkjin, rs.getInt("n"));
                }
            }

            // 3-3. 백업 row 합계 vs 매핑 건수
            int backupTotal = backupKwanli + backupParkUkjin;
            System.out.printf("| 백업 총 row | **%d** |%n", backupTotal);

            System.out.println();
            System.out.println("---");
            System.out.println("- 정상: orphan 0건 + admin/ukjin914 가 각각 11/16 만큼 증가");
            System.out.println("- 백업 테이블은 운영 안정 확인 후 별도 DROP (보존 기간 내부 정책 따름)");
        }
    }

    private static int countBackup(Connection c, String table, String userid) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT COUNT(*) FROM " + table + " WHERE userid = ?")) {
            ps.setString(1, userid);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }
}
