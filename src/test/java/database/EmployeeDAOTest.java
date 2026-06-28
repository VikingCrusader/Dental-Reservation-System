package database;

import model.Employee;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeDAOTest {

    private Connection conn;
    private EmployeeDAO employeeDAO;

    @BeforeEach
    void setUp() throws SQLException {
        conn = DriverManager.getConnection("jdbc:sqlite::memory:");
        DatabaseManager.setConnection(conn);

        try (Statement stmt = conn.createStatement()) {
            stmt.execute("""
                CREATE TABLE employees (
                    id        INTEGER PRIMARY KEY AUTOINCREMENT,
                    name      TEXT NOT NULL,
                    username  TEXT NOT NULL UNIQUE,
                    password  TEXT NOT NULL,
                    role      TEXT NOT NULL CHECK(role IN ('dentist','staff'))
                )""");
        }

        employeeDAO = new EmployeeDAO();
    }

    @AfterEach
    void tearDown() throws SQLException {
        conn.close();
    }

    @Test
    void addedEmployeeCanBeRetrievedByUsername() {
        Employee emp = new Employee("Dr. Smith", "drsmith", "hashed_pw", "dentist");

        int id = employeeDAO.addEmployee(emp);

        assertTrue(id > 0);
        Employee retrieved = employeeDAO.getEmployeeByUsername("drsmith");
        assertNotNull(retrieved);
        assertEquals("Dr. Smith", retrieved.getName());
        assertEquals("dentist", retrieved.getRole());
    }
}
