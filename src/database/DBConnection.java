package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Central place that opens JDBC connections to the MySQL database.
 *
 * IMPORTANT (setup):
 * 1. Create the database and tables first by running database/schema.sql
 *    in MySQL Workbench (or `mysql -u root -p < schema.sql`).
 * 2. Update DB_USER / DB_PASSWORD below to match your local MySQL account.
 * 3. Put the MySQL Connector/J jar (mysql-connector-j-x.x.x.jar) on your
 *    classpath - it is NOT bundled with the JDK. Download it from
 *    https://dev.mysql.com/downloads/connector/j/ and add it as a
 *    "Referenced Library" in VS Code, or via -cp on the command line.
 *
 * Every DAO calls DBConnection.getConnection() to get a fresh connection
 * and closes it in a try-with-resources block - this class does not keep
 * a shared connection open.
 */
public class DBConnection {

    private static final String DB_URL =
        setting("HOSTEL_DB_URL", "jdbc:mysql://localhost:3306/hostel_maintenance?useSSL=false&serverTimezone=UTC");
    private static final String DB_USER = setting("HOSTEL_DB_USER", "root");
    private static final String DB_PASSWORD = setting("HOSTEL_DB_PASSWORD", "");

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println(
                "MySQL JDBC driver not found on classpath. " +
                "Download mysql-connector-j and add it as a library. " + e.getMessage());
        }
    }

    private DBConnection() { } // utility class, no instances

    private static String setting(String name, String defaultValue) {
        String property = System.getProperty(name);
        if (property != null && !property.isBlank()) return property;
        String environment = System.getenv(name);
        return environment != null && !environment.isBlank() ? environment : defaultValue;
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
}
