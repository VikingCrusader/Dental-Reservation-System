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
        return connection;
    }

    // ── Schema initialisation ─────────────────────────────────────────────────

    /**
     * Creates all tables (if they don't exist yet) and seeds default employees.
     * Safe to call every time the app starts.
     */
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

            System.out.println("[DB] Schema ready.");

        } catch (SQLException e) {
            System.err.println("[DB] initializeDatabase error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /** Inserts two default dentist accounts if the employees table is empty. */
    private static void seedEmployees(Statement stmt) throws SQLException {
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS n FROM employees");
        if (rs.next() && rs.getInt("n") == 0) {
            stmt.execute("""
                INSERT INTO employees (name, username, password, role) VALUES
                    ('John Dow',  'djohn', '12345', 'dentist'),
                    ('Sam Alton', 'Sami',  '54321', 'dentist')
            """);
            System.out.println("[DB] Default employees seeded.");
        }
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
