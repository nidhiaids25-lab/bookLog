package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static String url = System.getenv().getOrDefault("DB_URL", "jdbc:mysql://localhost:3306/library_db");
    private static String user = System.getenv().getOrDefault("DB_USER", "root");
    private static String password = System.getenv().getOrDefault("DB_PASSWORD", "password"); // Aapna MySQL password daalein

    public static void setCredentials(String newUrl, String newUser, String newPassword) {
        url = newUrl;
        user = newUser;
        password = newPassword;
    }

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ignored) {
            // Newer JDBC drivers auto-load via ServiceLoader, but class check is kept for older setups
        }
        return DriverManager.getConnection(url, user, password);
    }

    /**
     * Check whether MySQL connection can be established without throwing fatal errors.
     */
    public static boolean isDatabaseAvailable() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (Exception e) {
            return false;
        }
    }

    public static String getUrl() { return url; }
    public static String getUser() { return user; }
}
