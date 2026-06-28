package database;
import java.sql.*;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:dental.db";
    private static Connection connection;

    // ── Connection ────────────────────────────────────────────────────────────

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                // Explicitly load the driver (required for some JVM versions)
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException e) {
                throw new SQLException(
                        "SQLite JDBC driver not found. " +
                        "Make sure sqlite-jdbc is in pom.xml dependencies.", e);
            }
            connection = DriverManager.getConnection(DB_URL);
            // Enable foreign key enforcement (SQLite turns this off by default)
            try (Statement st = connection.createStatement()) {
                st.execute("PRAGMA foreign_keys = ON");
            }
            System.out.println("[DB] Connected → dental.db");
        }
        return connection; //This connection is the 'bridge' between Java code and SQL, SQL statements can be sent to SQL via this connection.
    }

    // ── Schema initialisation ─────────────────────────────────────────────────

    /** Create Tables */
    public static void initializeDatabase() {
        try (Statement stmt = getConnection().createStatement()) {

            // ── patients ─────────────────────────────────────────────────────
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS patients (
                    id        INTEGER PRIMARY KEY AUTOINCREMENT,
                    name      TEXT    NOT NULL,
                    username  TEXT    NOT NULL UNIQUE,
                    password  TEXT    NOT NULL,
                    email     TEXT    DEFAULT '',
                    address   TEXT    DEFAULT '',
                    telephone TEXT    DEFAULT ''
                )
            """);

            // ── employees ────────────────────────────────────────────────────
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS employees (
                    id        INTEGER PRIMARY KEY AUTOINCREMENT,
                    name      TEXT    NOT NULL,
                    username  TEXT    NOT NULL UNIQUE,
                    password  TEXT    NOT NULL,
                    role      TEXT    NOT NULL CHECK(role IN ('dentist','staff'))
                )
            """);

            // ── appointments ─────────────────────────────────────────────────
            // ON DELETE CASCADE: deleting a patient also removes their appointments
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS appointments (
                    id         INTEGER PRIMARY KEY AUTOINCREMENT,
                    patient_id INTEGER NOT NULL,
                    date_time  TEXT    NOT NULL UNIQUE,
                    FOREIGN KEY (patient_id) REFERENCES patients(id)
                        ON DELETE CASCADE
                )
            """);

            // Seed default employees only on very first run
            seedEmployees(stmt);
            seedPatients(stmt);

            System.out.println("[DB] Schema ready.");

        } catch (SQLException e) {
            System.err.println("[DB] initializeDatabase error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /** Inserts two default dentist accounts (if employees DB is empty)*/
    private static void seedEmployees(Statement stmt) throws SQLException {
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS n FROM employees");
        if (rs.next() && rs.getInt("n") == 0) {
            String sql = "INSERT INTO employees (name, username, password, role) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
                // Passwords are pre-hashed with BCrypt (cost=10); plain-text never stored in source.
                // djohn→"12345", Sami→"54321"
                String[][] data = {
                    {"John Dow",  "djohn", "$2a$10$AGK5gLJOIAa1BnvvPmRjJOWpbGZ8zRM1t5jfr9GyGILO/76jE1TVy", "dentist"},
                    {"Sam Alton", "Sami",  "$2a$10$aL.UO7OaQYmzdSt25tfcFeZ03R5.CuR9Oe2TE/kA3fkQjs8pWH3ga", "dentist"}
                };
                for (String[] row : data) {
                    ps.setString(1, row[0]);
                    ps.setString(2, row[1]);
                    ps.setString(3, row[2]);
                    ps.setString(4, row[3]);
                    ps.executeUpdate();
                }
            }
            System.out.println("[DB] Default employees seeded.");
        }
    }

    /** Inserts three default patient accounts (if patients DB is empty)*/
    private static void seedPatients(Statement stmt) throws SQLException {
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS n FROM patients");
        if (rs.next() && rs.getInt("n") == 0) {
            String sql = "INSERT INTO patients (name, username, password, email, address, telephone) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
                // Passwords are pre-hashed with BCrypt (cost=10); plain-text never stored in source.
                // lxx2002→"020712Xiao!", hofmann→"ichbinHofmann##DE", leean1991→"LeeAN1991***"
                String[][] data = {
                    {"Xiaoxiao Lin",    "lxx2002",   "$2a$10$AeUwVVt7RkXNiI4qxb7caO5MxEqOnul/uol3BiRoeOi7WXk3g4Itu", "lxx@gmail.com",         "Prague",    "+420 778913400"},
                    {"Florian Hofmann", "hofmann",   "$2a$10$rWhYXFjzB68kJXzO9uBVDuzBlPBZMN8Y15K/evvUAa5/O8vDKU1.K", "hofmann@hotmail.com",   "Frankfurt", "+49 582736400"},
                    {"An Lee",          "leean1991", "$2a$10$o9McIk0TYvAUksVWOFr4W.LdVVUvMrcdlwiGRXLXuP0nZTQRa0wB6", "leean1991@outlook.com", "Taipei",    "+886 139267893"}
                };
                for (String[] row : data) {
                    ps.setString(1, row[0]);
                    ps.setString(2, row[1]);
                    ps.setString(3, row[2]);
                    ps.setString(4, row[3]);
                    ps.setString(5, row[4]);
                    ps.setString(6, row[5]);
                    ps.executeUpdate();
                }
            }
            System.out.println("[DB] Default patients seeded.");
        }
    }

    // ── Testing support ───────────────────────────────────────────────────────

    /** Replaces the active connection. Used by tests to inject an in-memory SQLite DB. */
    public static void setConnection(Connection conn) {
        connection = conn;
    }

    // ── Cleanup ───────────────────────────────────────────────────────────────

    /** Closes the connection. Called automatically via shutdown hook in Main. */
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[DB] Connection closed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
