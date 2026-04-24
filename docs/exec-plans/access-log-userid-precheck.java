import java.sql.*;
import java.util.*;

/**
 * S5 access-log-userid-fix · Step 1 사전검증 러너 (READ ONLY)
 *
 * 실행:
 *   javac -encoding UTF-8 docs/dev-plans/access-log-userid-precheck.java
 *   java -cp ".;<postgresql-jar>" -Dfile.encoding=UTF-8 access-log-userid-precheck > result.txt
 *
 * 출력: 마크다운 표 형식 → docs/dev-plans/access-log-userid-precheck-result.md 로 저장
 *
 * 안전통제:
 *  - SET TRANSACTION READ ONLY (UPDATE/DELETE 차단)
 *  - statement_timeout 10s
 *  - 3 쿼리만, 결과만 STDOUT
 */
public class access_log_userid_precheck {

    public static void main(String[] args) throws Exception {
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
                s.execute("SET statement_timeout = 10000"); // 10s
            }

            System.out.println("# access-log-userid-fix Step 1 — 사전검증 결과");
            System.out.println();
            System.out.println("- 실행시각: " + new java.util.Date());
            System.out.println("- DB: " + url);
            System.out.println("- 안전통제: READ ONLY, statement_timeout=10s");
            System.out.println();

            // ─────────────────────────────────
            // (a) 오염 분포 + status
            // ─────────────────────────────────
            System.out.println("## (a) userid 분포 + status");
            System.out.println();
            System.out.println("| userid | cnt | status |");
            System.out.println("|--------|----:|--------|");
            String qa =
                "SELECT userid, COUNT(*) AS cnt, " +
                "  CASE WHEN EXISTS (SELECT 1 FROM users u WHERE u.userid = access_logs.userid) THEN 'valid' " +
                "       WHEN userid = 'anonymousUser' THEN 'anonymous' " +
                "       WHEN userid = 'anonymous' THEN 'legacy-anonymous' " +
                "       ELSE 'orphan' END AS status " +
                "FROM access_logs GROUP BY userid ORDER BY cnt DESC";
            try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery(qa)) {
                while (rs.next()) {
                    System.out.printf("| %s | %d | %s |%n",
                            esc(rs.getString("userid")),
                            rs.getInt("cnt"),
                            rs.getString("status"));
                }
            }
            System.out.println();

            // ─────────────────────────────────
            // (b) 오염값별 username 1:1 매칭 후보
            // ─────────────────────────────────
            System.out.println("## (b) 오염값별 username 1:1 매칭 후보");
            System.out.println();
            System.out.println("| dirty_userid | candidate_userid | username | log_cnt | match_kind |");
            System.out.println("|--------------|------------------|----------|--------:|------------|");
            String qb =
                "SELECT al.userid AS dirty, u.userid AS candidate, u.username AS uname, COUNT(al.log_id) AS cnt " +
                "FROM access_logs al " +
                "LEFT JOIN users u ON u.username = al.userid " +
                "WHERE al.userid NOT IN (SELECT userid FROM users) " +
                "  AND al.userid <> 'anonymousUser' " +
                "GROUP BY al.userid, u.userid, u.username " +
                "ORDER BY al.userid";
            // 매칭 갯수 측정용 보조 맵
            Map<String, Integer> matchCount = new HashMap<>();
            List<String[]> rowsB = new ArrayList<>();
            try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery(qb)) {
                while (rs.next()) {
                    String dirty = rs.getString("dirty");
                    String cand  = rs.getString("candidate");
                    String uname = rs.getString("uname");
                    int    cnt   = rs.getInt("cnt");
                    rowsB.add(new String[]{dirty, cand, uname, String.valueOf(cnt)});
                    if (cand != null) matchCount.merge(dirty, 1, Integer::sum);
                    else matchCount.putIfAbsent(dirty, 0);
                }
            }
            for (String[] r : rowsB) {
                int m = matchCount.getOrDefault(r[0], 0);
                String kind = (r[1] == null) ? "0:N(no-user)"
                            : (m == 1) ? "1:1 (auto-update OK)"
                            : "1:N (manual decision)";
                System.out.printf("| %s | %s | %s | %s | %s |%n",
                        esc(r[0]), esc(r[1]), esc(r[2]), r[3], kind);
            }
            System.out.println();

            // ─────────────────────────────────
            // (c) 오염 레코드 총량 (UPDATE 기대값)
            // ─────────────────────────────────
            System.out.println("## (c) 오염 레코드 총량 (UPDATE 기대값)");
            System.out.println();
            String qc =
                "SELECT COUNT(*) AS total FROM access_logs " +
                "WHERE userid NOT IN (SELECT userid FROM users) " +
                "  AND userid NOT IN ('anonymousUser', 'anonymous')";
            try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery(qc)) {
                if (rs.next()) {
                    System.out.println("**총 오염 레코드: " + rs.getInt("total") + "건**");
                }
            }
            System.out.println();

            // 보너스: 'anonymous' (legacy) 는 별도 처리 안내용
            String qd = "SELECT COUNT(*) AS legacy FROM access_logs WHERE userid = 'anonymous'";
            try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery(qd)) {
                if (rs.next()) {
                    System.out.println("- 참고: legacy `anonymous` (= `anonymousUser` 통일 대상) 건수: " + rs.getInt("legacy"));
                }
            }

            System.out.println();
            System.out.println("---");
            System.out.println("- 다음 단계: 위 (b) 결과로 매핑표 확정 → V019 SQL 작성 → 사용자 승인 후 Step 2 진행");
        }
    }

    private static String esc(String s) {
        return s == null ? "(null)" : s.replace("|", "\\|");
    }
}
