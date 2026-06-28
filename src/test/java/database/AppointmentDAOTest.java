package database;

import model.Patient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AppointmentDAOTest {

    private Connection conn;
    private AppointmentDAO apptDAO;
    private Patient testPatient;

    @BeforeEach
    void setUp() throws SQLException {
        conn = DriverManager.getConnection("jdbc:sqlite::memory:");
        DatabaseManager.setConnection(conn);

        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON");
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
            stmt.execute("""
                CREATE TABLE appointments (
                    id         INTEGER PRIMARY KEY AUTOINCREMENT,
                    patient_id INTEGER NOT NULL,
                    date_time  TEXT NOT NULL UNIQUE,
                    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE
                )""");
        }

        testPatient = new Patient("Test Patient", "testuser", "hashed_pw", "", "", "");
        new PatientDAO().addPatient(testPatient);
        apptDAO = new AppointmentDAO();
    }

    @AfterEach
    void tearDown() throws SQLException {
        conn.close();
    }

    @Test
    void successfulBookingReturnsTrueAndSlotIsMarkedTaken() {
        LocalDateTime slot = LocalDateTime.of(2025, 9, 15, 10, 0);

        assertTrue(apptDAO.addAppointment(testPatient, slot));
        assertTrue(apptDAO.isSlotTaken(slot));
    }

    @Test
    void doubleBookingIsRejected() {
        LocalDateTime slot = LocalDateTime.of(2025, 9, 15, 11, 0);

        assertTrue(apptDAO.addAppointment(testPatient, slot));
        assertFalse(apptDAO.addAppointment(testPatient, slot));
    }
}
