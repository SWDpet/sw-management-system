import java.sql.*;
public class Rc {
    public static void main(String[] args) throws Exception {
        Class.forName("org.postgresql.Driver");
        String url = System.getenv().getOrDefault("DB_URL", "jdbc:postgresql://211.104.137.55:5881/SW_Dept");
        try (var c = DriverManager.getConnection(url, "postgres", System.getenv("DB_PASSWORD")); var s = c.createStatement()) {
            for (String t : new String[]{"ps_info", "users"}) {
                try (var r = s.executeQuery("SELECT COUNT(*) FROM " + t)) { r.next(); System.out.println(t + ": " + r.getLong(1)); }
            }
        }
    }
}
