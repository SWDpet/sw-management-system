// 일회성: 문서관리 + inspect_report 계열 데이터 클리어
// 실행: jshell --class-path <pg-jdbc.jar> clear-documents.jsh

String pwd = System.getenv("DB_PASSWORD");
if (pwd == null || pwd.isBlank()) { System.out.println("ERR: DB_PASSWORD not set"); System.exit(1); }

var c = java.sql.DriverManager.getConnection(
    "jdbc:postgresql://211.104.137.55:5881/SW_Dept", "postgres", pwd);
c.setAutoCommit(false);
var s = c.createStatement();

System.out.println("=== BEFORE ===");
String[] tables = {
    "tb_document", "tb_document_detail", "tb_document_history",
    "tb_document_attachment", "tb_document_signature",
    "tb_inspect_checklist", "tb_inspect_issue",
    "inspect_report", "inspect_visit_log", "inspect_check_result"
};
for (String t : tables) {
    try (var rs = s.executeQuery("SELECT COUNT(*) FROM " + t)) {
        rs.next();
        System.out.printf("  %-30s = %d%n", t, rs.getLong(1));
    } catch (Exception e) {
        System.out.printf("  %-30s = (does not exist)%n", t);
    }
}

System.out.println("\n=== ATTACHMENT file_path SAMPLES ===");
try (var rs = s.executeQuery("SELECT file_path FROM tb_document_attachment LIMIT 10")) {
    int n = 0;
    while (rs.next()) { System.out.println("  " + rs.getString(1)); n++; }
    if (n == 0) System.out.println("  (none)");
}

System.out.println("\n=== TRUNCATE ===");
s.execute("TRUNCATE tb_document RESTART IDENTITY CASCADE");
System.out.println("  TRUNCATE tb_document RESTART IDENTITY CASCADE -- OK");
try {
    s.execute("TRUNCATE inspect_report RESTART IDENTITY CASCADE");
    System.out.println("  TRUNCATE inspect_report RESTART IDENTITY CASCADE -- OK");
} catch (Exception e) {
    System.out.println("  TRUNCATE inspect_report skipped: " + e.getMessage());
}
c.commit();
System.out.println("  COMMIT -- OK");

System.out.println("\n=== AFTER ===");
for (String t : tables) {
    try (var rs = s.executeQuery("SELECT COUNT(*) FROM " + t)) {
        rs.next();
        System.out.printf("  %-30s = %d%n", t, rs.getLong(1));
    } catch (Exception e) {}
}

c.close();
System.out.println("\nDONE");
/exit
