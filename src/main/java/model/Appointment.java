package model;

import java.time.LocalDateTime;

/**
 * Represents a booked appointment.
 * Maps to the "appointments" table in dental.db.
 *
 * date_time is stored as ISO-8601 text in SQLite (e.g. "2025-07-25T11:00")
 * so it sorts correctly and is human-readable in DataGrip.
 */
public class Appointment {

    private int           id;
    private Patient       patient;
    private LocalDateTime dateTime;

    // ── Constructors ──────────────────────────────────────────────────────────

    public Appointment() {}

    public Appointment(Patient patient, LocalDateTime dateTime) {
        this.patient  = patient;
        this.dateTime = dateTime;
    }

    public Appointment(int id, Patient patient, LocalDateTime dateTime) {
        this(patient, dateTime);
        this.id = id;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public int  getId()       { return id; }
    public void setId(int id) { this.id = id; }

    public Patient getPatient()               { return patient; }
    public void    setPatient(Patient p)      { this.patient = p; }

    public LocalDateTime getDateTime()                    { return dateTime; }
    public void          setDateTime(LocalDateTime dt)    { this.dateTime = dt; }

    @Override
    public String toString() {
        return dateTime.toLocalDate() + "  "
                + String.format("%02d:00", dateTime.getHour())
                + "  —  " + patient.getName();
    }
}
