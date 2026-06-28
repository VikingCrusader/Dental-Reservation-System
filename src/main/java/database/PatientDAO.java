package database;

import model.Patient;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
public class PatientDAO {

    // ── CREATE ────────────────────────────────────────────────────────────────

    /**
     * Inserts a new patient row.
     * Sets patient.id to the auto-generated primary key.
     * return the new id, or -1 on failure (e.g. duplicate username)
     */
    public int addPatient(Patient patient) {
        String sql = """
            INSERT INTO patients (name, username, password, email, address, telephone)
            VALUES (?, ?, ?, ?, ?, ?)
        """; //"?" are place holders

        try (PreparedStatement ps = DatabaseManager.getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) { //.prepareStatement(): The SQL statement is sent to the database for compilation in advance, ready but not yet executed.

            // DB received SQL, check syntax, compiled, waiting for parameters.
            ps.setString(1, patient.getName());
            ps.setString(2, patient.getUsername());
            ps.setString(3, patient.getPassword());
            ps.setString(4, patient.getEmail());
            ps.setString(5, patient.getAddress());
            ps.setString(6, patient.getTelephone()); // fill in the placeholders
            ps.executeUpdate(); // now execute

            ResultSet keys = ps.getGeneratedKeys(); //the "keys" here's datatype is "ResultSet", its like a container, not int, we need to convert it to int to return.
            if (keys.next()) { // open the container, move to the first line using .next(), default point to the place before the first line.
                int newId = keys.getInt(1);  // get the integer type value of the first column(id generated).
                patient.setId(newId);   // update the object too
                return newId;
            }

        } catch (SQLException e) {
            System.err.println("[PatientDAO] addPatient: " + e.getMessage());
        }
        return -1;
    }

    // ── READ ──────────────────────────────────────────────────────────────────

    /** Returns all patients sorted alphabetically by name. */
    public List<Patient> getAllPatients() {
        List<Patient> list = new ArrayList<>();
        String sql = "SELECT * FROM patients ORDER BY name";

        try (Statement stmt = DatabaseManager.getConnection().createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));

        } catch (SQLException e) {
            System.err.println("[PatientDAO] getAllPatients: " + e.getMessage());
        }
        return list;
    }

    /**
     * Finds a patient by username — used for login authentication.
     * return Patient object, or null if not found.
     */
    public Patient getPatientByUsername(String username) {
        String sql = "SELECT * FROM patients WHERE username = ?";

        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) { //if there's a variable/placeholder "?" in SQL, use prepareStatement() instead of executeQuery()
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);

        } catch (SQLException e) {
            System.err.println("[PatientDAO] getPatientByUsername: " + e.getMessage());
        }
        return null;
    }

    /** Finds a patient by primary key — used when loading appointments. */
    public Patient getPatientById(int id) {
        String sql = "SELECT * FROM patients WHERE id = ?";

        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);

        } catch (SQLException e) {
            System.err.println("[PatientDAO] getPatientById: " + e.getMessage());
        }
        return null;
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────

    /** Updates all attributes of a patient */
    public boolean updatePatient(Patient patient) {
        String sql = """
            UPDATE patients
               SET name = ?, password = ?, email = ?, address = ?, telephone = ?
             WHERE id = ?
        """;

        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, patient.getName());
            ps.setString(2, patient.getPassword());
            ps.setString(3, patient.getEmail());
            ps.setString(4, patient.getAddress());
            ps.setString(5, patient.getTelephone());
            ps.setInt(6, patient.getId());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[PatientDAO] updatePatient: " + e.getMessage());
        }
        return false;
    }

    // ── DELETE ────────────────────────────────────────────────────────────────

    /**
     * Deletes a patient by id.
     */
    public boolean deletePatient(int patientId) {
        String sql = "DELETE FROM patients WHERE id = ?";

        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, patientId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[PatientDAO] deletePatient: " + e.getMessage());
        }
        return false;
    }

    // ── Converter ────────────────────────────────────────────────────────────────

    /** Converts one ResultSet row → Patient object. */
    private Patient mapRow(ResultSet rs) throws SQLException {
        return new Patient(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("username"),
                rs.getString("password"),
                rs.getString("email"),
                rs.getString("address"),
                rs.getString("telephone")
        );
    }
}
