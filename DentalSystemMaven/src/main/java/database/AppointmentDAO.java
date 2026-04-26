package database;

import model.Appointment;
import model.Patient;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object — all SQL for the "appointments" table.
 *
 * date_time is stored as ISO-8601 text: "2025-07-25T11:00"
 * SQLite has no native datetime type; TEXT works perfectly and
 * sorts chronologically because ISO-8601 is lexicographically ordered.
 *
 * The UNIQUE constraint on date_time prevents double-booking at the DB level.
 * isSlotTaken() lets us check before attempting the insert so we can show
 * a friendly message instead of a raw SQL exception.
 */
public class AppointmentDAO {

    private final PatientDAO patientDAO = new PatientDAO();

    // ── CREATE ────────────────────────────────────────────────────────────────

    /**
     * Books a new appointment.
     * @return true on success, false if the slot is already taken.
     */
    public boolean addAppointment(Patient patient, LocalDateTime dateTime) {
        if (isSlotTaken(dateTime)) {
            System.out.println("[AppointmentDAO] Slot taken: " + dateTime);
            return false;
        }

        String sql = "INSERT INTO appointments (patient_id, date_time) VALUES (?, ?)";

        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, patient.getId());
            ps.setString(2, dateTime.toString());   // → "2025-07-25T11:00"
            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("[AppointmentDAO] addAppointment: " + e.getMessage());
        }
        return false;
    }

    // ── READ ──────────────────────────────────────────────────────────────────

    /** Returns all appointments, sorted by date/time ascending. */
    public List<Appointment> getAllAppointments() {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT * FROM appointments ORDER BY date_time";

        try (Statement stmt = DatabaseManager.getConnection().createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Appointment a = mapRow(rs);
                if (a != null) list.add(a);
            }

        } catch (SQLException e) {
            System.err.println("[AppointmentDAO] getAllAppointments: " + e.getMessage());
        }
        return list;
    }

    /** Returns only the appointments for a specific patient. */
    public List<Appointment> getAppointmentsByPatient(Patient patient) {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT * FROM appointments WHERE patient_id = ? ORDER BY date_time";

        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, patient.getId());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Appointment a = mapRow(rs);
                if (a != null) list.add(a);
            }

        } catch (SQLException e) {
            System.err.println("[AppointmentDAO] getAppointmentsByPatient: " + e.getMessage());
        }
        return list;
    }

    /**
     * Checks if a time slot is already booked.
     * Called before addAppointment AND by CalendarView to grey out taken slots.
     */
    public boolean isSlotTaken(LocalDateTime dateTime) {
        String sql = "SELECT COUNT(*) FROM appointments WHERE date_time = ?";

        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, dateTime.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;

        } catch (SQLException e) {
            System.err.println("[AppointmentDAO] isSlotTaken: " + e.getMessage());
        }
        return false;
    }

    // ── DELETE ────────────────────────────────────────────────────────────────

    /** Cancels an appointment by its primary key. */
    public boolean removeAppointmentById(int appointmentId) {
        String sql = "DELETE FROM appointments WHERE id = ?";

        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, appointmentId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[AppointmentDAO] removeAppointmentById: " + e.getMessage());
        }
        return false;
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────

    /** Reschedules an appointment to a new slot. */
    public boolean reschedule(int appointmentId, LocalDateTime newDateTime) {
        if (isSlotTaken(newDateTime)) return false;

        String sql = "UPDATE appointments SET date_time = ? WHERE id = ?";

        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, newDateTime.toString());
            ps.setInt(2, appointmentId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[AppointmentDAO] reschedule: " + e.getMessage());
        }
        return false;
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    /** Maps one ResultSet row → Appointment (also fetches the linked Patient). */
    private Appointment mapRow(ResultSet rs) throws SQLException {
        int patientId = rs.getInt("patient_id");
        Patient patient = patientDAO.getPatientById(patientId);
        if (patient == null) return null;   // orphaned row — skip

        LocalDateTime dt = LocalDateTime.parse(rs.getString("date_time"));
        return new Appointment(rs.getInt("id"), patient, dt);
    }
}
