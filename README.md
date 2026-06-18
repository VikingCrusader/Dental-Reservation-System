# Dental Reservation System (IntelliJ IDEA + DataGrip)

Rebuilt from **Prof. Kouatly's Working Group 3**.

**Storage upgrade:** `ArrayList` (in-memory) → **SQLite database** (`dental.db`)

---

## Open in IntelliJ IDEA (3 steps)

### 1) Open the project
- **IntelliJ IDEA →** `File` → `Open`
- Select the **`DentalSystemMaven`** folder

IntelliJ will detect `pom.xml` and prompt:

> “Maven build scripts found. Load?”

Click **Load** (and **Trust Project** if prompted).

### 2) Let Maven download dependencies
- Open the **Maven** tool window (right sidebar) or check the notification at the bottom.
- IntelliJ will download `sqlite-jdbc` automatically from Maven Central.

> **Note:** Internet access is needed the first time. After that, dependencies are cached locally.

### 3) Run the app
- Open: `src/main/java/Main.java`
- Click the green **▶** next to `main()`

On first launch, **`dental.db`** will be created in the **project root**.

---

## Connect DataGrip to `dental.db`

1. Open **DataGrip**
2. Click **+** → **Data Source** → **SQLite**
3. In **File**: click the folder icon → select your project’s **`dental.db`**
4. Click **Test Connection**  
   (DataGrip may offer to download the SQLite driver — accept)
5. Click **OK**

You can now browse, query, and edit the database while the app runs.

### Useful SQL queries

```sql
-- See all patients
SELECT * FROM patients;

-- See all appointments with patient names (JOIN)
SELECT a.id,
       p.name AS patient,
       a.date_time
FROM   appointments a
JOIN   patients p ON p.id = a.patient_id
ORDER  BY a.date_time;

-- Count appointments per patient
SELECT p.name, COUNT(a.id) AS total
FROM   patients p
LEFT   JOIN appointments a ON a.patient_id = p.id
GROUP  BY p.id
ORDER  BY total DESC;

-- Delete a specific appointment
DELETE FROM appointments WHERE id = 3;
```

---

## Default Login Credentials

| Role    | Username | Password |
|---------|----------|----------|
| Dentist | djohn    | 12345    |
| Dentist | Sami     | 54321    |

**Patient accounts** are created via **“Create Account”** on the login screen.

---

## Project Structure

```text
DentalSystemMaven/
├── pom.xml                              ← Maven config (sqlite-jdbc dependency here)
└── src/main/java/
    ├── Main.java                        ← Entry point
    ├── model/
    │   ├── Patient.java                 ← Data class → maps to patients table
    │   ├── Employee.java                ← Data class → maps to employees table
    │   └── Appointment.java             ← Data class → maps to appointments table
    ├── database/
    │   ├── DatabaseManager.java         ← Opens connection, creates tables
    │   ├── PatientDAO.java              ← INSERT / SELECT / UPDATE / DELETE patients
    │   ├── EmployeeDAO.java             ← INSERT / SELECT employees
    │   └── AppointmentDAO.java          ← INSERT / SELECT / DELETE appointments
    └── gui/
        ├── WelcomePage.java             ← Login + Registration
        ├── PatientMenu.java             ← Patient dashboard
        ├── EmployeeMenu.java            ← Staff dashboard
        └── CalendarView.java            ← Time-slot booking screen
```

---

## Database Schema

```sql
CREATE TABLE patients (
    id        INTEGER PRIMARY KEY AUTOINCREMENT,
    name      TEXT NOT NULL,
    username  TEXT NOT NULL UNIQUE,   -- used for login
    password  TEXT NOT NULL,
    email     TEXT DEFAULT '',
    address   TEXT DEFAULT '',
    telephone TEXT DEFAULT ''
);

CREATE TABLE employees (
    id        INTEGER PRIMARY KEY AUTOINCREMENT,
    name      TEXT NOT NULL,
    username  TEXT NOT NULL UNIQUE,
    password  TEXT NOT NULL,
    role      TEXT NOT NULL CHECK(role IN ('dentist','staff'))
);

CREATE TABLE appointments (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    patient_id INTEGER NOT NULL,
    date_time  TEXT NOT NULL UNIQUE,         -- ISO-8601: "2025-07-25T11:00"
    FOREIGN KEY (patient_id)                 -- UNIQUE prevents double-booking
        REFERENCES patients(id)
        ON DELETE CASCADE                    -- deleting a patient removes their appointments
);
```

---

## Differences from the Teacher’s Version

| Feature | Teacher’s version | This version |
|---|---|---|
| Storage | `ArrayList` in RAM | SQLite file (`dental.db`) |
| Persistence | Lost on exit | Survives restarts |
| Double-booking | Not prevented | `UNIQUE` constraint + `isSlotTaken()` |
| Architecture | Logic mixed into GUI | Model / DAO / GUI layers separated |
| IDE setup | Eclipse + manual JAR | IntelliJ + Maven (auto-download) |
| Data inspection | `System.out.println` | DataGrip with live SQL queries |

---

## TO-DOs

### Login
1. Add password format requirements.

### Patient → Book Appointment
1. Time slots: booked appointments should appear **red** from other patients’ point of view.（finished:2026-06-18)
2. Improve calendar UI (use a real calendar date picker).(finished:2026-06-18)
3. Prevent selecting previous dates.(finished:2026-06-18)
4. Add categories of appointments, such as "clinic service","operation" etc

### Data Model
1. Add genders of users.

### Database
1. Use postgreSQL or mySQL instead of SQLite in the future.
---

## Patch Notes
- **2025-08-07:** Finished the initial version, without using Database, using List instead.
- **2026-04-26:** Initial Commit. Rewrite and improve the project.
- **2026-04-30:** Added “See Creators” function to the application.
- **2026-05-02:** Main Page(Welcome and Login) done.
- **2026-05-06:** JDBC done.
- **2026-05-06:** JDBC and database setup done.
- **2026-05-10:** PatientDAO CRUD finished
- **2026-06-18:** Refactor CalendarView: Replace JSpinner with custom Swing calendar grid; Disable weekends and CZ public holidays; Color-code days by booking availability (green/orange/red); Fix stale date bug from JSpinner.
