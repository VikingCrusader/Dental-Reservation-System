package database;

import model.Patient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

class PatientDAOTest {

    private Connection conn;
    private PatientDAO patientDAO;

    @BeforeEach
    void setUp() throws SQLException {
        conn = DriverManager.getConnection("jdbc:sqlite::memory:");
        DatabaseManager.setConnection(conn);

        try (Statement stmt = conn.createStatement()) {
            stmt.execute("""
                CREATE TABLE patients (
                    id        INTEGER PRIMARY KEY AUTOINCREMENT,
                    name      TEXT NOT NULL,
                    username  TEXT NOT NULL UNIQUE,
                    password  TEXT NOT NULL,
                    email     TEXT DEFAULT '',
                    address   TEXT DEFAULT '',
                    telephone TEXT DEFAULT ''
                )""");
        }

        patientDAO = new PatientDAO();
    }

    @AfterEach
    void tearDown() throws SQLException {
        conn.close();
    }

    @Test
    void registeredPatientCanBeRetrievedByUsername() {
        Patient p = new Patient("Alice", "alice99", "hashed_pw", "alice@example.com", "Berlin", "+49 123456");

        int id = patientDAO.addPatient(p);

        assertTrue(id > 0);
        Patient retrieved = patientDAO.getPatientByUsername("alice99");
        assertNotNull(retrieved);
        assertEquals("Alice", retrieved.getName());
        assertEquals("alice@example.com", retrieved.getEmail());
    }

    @Test
    void duplicateUsernameIsRejected() {
        patientDAO.addPatient(new Patient("Alice", "alice99", "hashed_pw", "", "", ""));

        int result = patientDAO.addPatient(new Patient("Other Alice", "alice99", "other_pw", "", "", ""));

        assertEquals(-1, result);
    }

    @Test
    void storedPasswordHashMatchesPlainTextUsingBCrypt() {
        String plainText = "SecurePass123!";
        String hashed = BCrypt.hashpw(plainText, BCrypt.gensalt());
        patientDAO.addPatient(new Patient("Bob", "bob42", hashed, "", "", ""));

        Patient retrieved = patientDAO.getPatientByUsername("bob42");

        assertNotNull(retrieved);
        assertTrue(BCrypt.checkpw(plainText, retrieved.getPassword()));
    }
}
