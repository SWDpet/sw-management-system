import java.io.*;
import java.sql.*;

/**
 * FR-0 사전검증 러너 — tb_contract/tb_contract_target DROP 전 강제 체크.
 * - 외부 FK 0건 (information_schema)
 * - 레코드 수 0건
 * - 비-FK 의존성 0건 (pg_depend)
 * - 테이블 존재 여부 (to_regclass)
 * 안전통제: SET TRANSACTION READ ONLY + statement_timeout 10s
 */
public class LegacyContractPrecheck {
    public static void main(String[] args) throws Exception {
        String url = System.getenv().getOrDefault("DB_URL", "jdbc:postgresql://211.104.137.55:5881/SW_Dept");
        String pw = System.getenv("DB_PASSWORD");
        Class.forName("org.postgresql.Driver");
        boolean halt = false;
        StringBuilder result = new StringBuilder("# FR-0 사전검증 결과\n\n- 실행일: "
            + new java.util.Date() + "\n\n");

        try (Connection c = DriverManager.getConnection(url, "postgres", pw)) {
            try (Statement s = c.createStatement()) {
                s.execute("SET SESSION statement_timeout = '10s'");
            }
            c.setReadOnly(true);

            // (a) 테이블 존재 확인
            result.append("## 1. 테이블 존재 확인 (to_regclass)\n\n");
            boolean hasContract = exists(c, "SELECT to_regclass('public.tb_contract') IS NOT NULL");
            boolean hasTarget   = exists(c, "SELECT to_regclass('public.tb_contract_target') IS NOT NULL");
            result.append("- tb_contract 존재: ").append(hasContract).append("\n");
            result.append("- tb_contract_target 존재: ").append(hasTarget).append("\n\n");
            if (!hasContract && !hasTarget) {
                result.append("✅ 두 테이블 모두 이미 없음 — DROP 작업 **SKIP 가능**\n\n");
            }

            // (b) 레코드 수
            result.append("## 2. 레코드 수\n\n");
            long cntContract = hasContract ? count(c, "SELECT COUNT(*) FROM tb_contract") : 0L;
            long cntTarget   = hasTarget   ? count(c, "SELECT COUNT(*) FROM tb_contract_target") : 0L;
            result.append("- tb_contract: ").append(cntContract)
                  .append(cntContract == 0 ? " ✅" : " ❌ HALT").append("\n");
            result.append("- tb_contract_target: ").append(cntTarget)
                  .append(cntTarget == 0 ? " ✅" : " ❌ HALT").append("\n\n");
            if (cntContract > 0 || cntTarget > 0) halt = true;

            // (c) 외부 FK
            result.append("## 3. 외부 FK (대상 테이블을 참조하는 다른 테이블의 FK)\n\n");
            String sqlFk =
                "SELECT tc.table_name AS referencing, tc.constraint_name, " +
                "       kcu.column_name AS fk_column, ccu.table_name AS target " +
                "  FROM information_schema.table_constraints tc " +
                "  JOIN information_schema.key_column_usage kcu " +
                "    ON tc.constraint_name = kcu.constraint_name AND tc.table_schema = kcu.table_schema " +
                "  JOIN information_schema.constraint_column_usage ccu " +
                "    ON tc.constraint_name = ccu.constraint_name AND tc.table_schema = ccu.table_schema " +
                " WHERE tc.constraint_type = 'FOREIGN KEY' " +
                "   AND tc.table_schema = 'public' " +
                "   AND ccu.table_name IN ('tb_contract', 'tb_contract_target') " +
                "   AND tc.table_name NOT IN ('tb_contract', 'tb_contract_target')";
            int fkCount = renderTable(c, sqlFk, result);
            if (fkCount == 0) result.append("✅ 외부 FK 없음\n\n");
            else { result.append("❌ HALT — 외부 FK ").append(fkCount).append("건 존재\n\n"); halt = true; }

            // (d) pg_depend 비-FK 의존성
            result.append("## 4. 비-FK 의존성 (pg_depend — view/rule/function 등)\n\n");
            String sqlDep =
                "SELECT c.relname AS target_table, d.deptype, " +
                "       (SELECT relname FROM pg_class WHERE oid = d.objid) AS dependent_obj, " +
                "       d.objid, d.classid " +
                "  FROM pg_depend d " +
                "  JOIN pg_class c ON d.refobjid = c.oid " +
                "  JOIN pg_namespace n ON c.relnamespace = n.oid " +
                " WHERE n.nspname = 'public' " +
                "   AND c.relname IN ('tb_contract', 'tb_contract_target') " +
                "   AND d.deptype NOT IN ('a', 'i')";
            int depCount = renderTable(c, sqlDep, result);
            if (depCount == 0) result.append("✅ 비-FK 의존성 없음\n\n");
            else { result.append("❌ HALT — 비-FK 의존성 ").append(depCount).append("건 존재\n\n"); halt = true; }

            // 최종 판정
            result.append("---\n\n## 최종 판정\n\n");
            if (halt) {
                result.append("❌ **HALT — DROP 진행 불가**. 위 조건 해소 후 재실행.\n");
            } else {
                result.append("✅ **PASS — DROP 진행 가능**.\n");
            }
        }

        try (FileWriter fw = new FileWriter("docs/dev-plans/legacy-contract-precheck-result.md")) {
            fw.write(result.toString());
        }
        System.out.println(halt ? "FAIL (HALT)" : "PASS");
        System.out.println("결과: docs/dev-plans/legacy-contract-precheck-result.md");
        if (halt) System.exit(1);
    }

    private static boolean exists(Connection c, String sql) throws SQLException {
        try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery(sql)) {
            rs.next(); return rs.getBoolean(1);
        }
    }
    private static long count(Connection c, String sql) throws SQLException {
        try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery(sql)) {
            rs.next(); return rs.getLong(1);
        }
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
