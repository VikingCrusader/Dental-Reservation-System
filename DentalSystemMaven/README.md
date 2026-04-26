# 🦷 Dental Reservation System — IntelliJ IDEA + DataGrip Edition

Rebuilt from Prof. Kouatly's Working Group 3.
Storage replaced: `ArrayList` in memory → **SQLite database** (`dental.db`).

---

## ✅ Open in IntelliJ IDEA (3 steps)

### Step 1 — Open the project
```
File → Open → select the "DentalSystemMaven" folder
```
IntelliJ detects `pom.xml` automatically and asks:
> "Maven build scripts found. Load?"

Click **Load** (or Trust Project if prompted).

### Step 2 — Let Maven download dependencies
Look for the Maven tool window (right side bar) or the notification at the bottom.
IntelliJ downloads `sqlite-jdbc` automatically from Maven Central.

> ⚠️ You need internet access the first time. After that it's cached locally.

### Step 3 — Run the app
Open `src/main/java/Main.java` → click the green ▶ button next to `main()`.

`dental.db` is created in your **project root** on first launch.

---

## 🗄️ Connect DataGrip to dental.db

1. Open DataGrip
2. **+ → Data Source → SQLite**
3. In the "File" field: click the folder icon → navigate to your project folder → select `dental.db`
4. Click **Test Connection** (DataGrip may offer to download the SQLite driver → accept)
5. Click **OK**

You can now browse, query, and edit data live while the app is running.

### Useful queries to try in DataGrip:
```sql
-- See all patients
SELECT * FROM patients;

-- See all appointments with patient names (JOIN)
SELECT a.id,
       p.name        AS patient,
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

## 🔑 Default Login Credentials

| Role    | Username | Password |
|---------|----------|----------|
| Dentist | djohn    | 12345    |
| Dentist | Sami     | 54321    |

Patient accounts are created via **"Create Account"** on the login screen.

---

## 📁 Project Structure

```
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

## 🗃️ Database Schema

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

## 🔄 Differences from Teacher's Version

| Feature | Teacher's version | This version |
|---|---|---|
| Storage | `ArrayList` in RAM | SQLite file (`dental.db`) |
| Persistence | Lost on exit | Survives restarts |
| Double-booking | Not prevented | `UNIQUE` constraint + `isSlotTaken()` |
| Architecture | Logic mixed into GUI | Model / DAO / GUI layers separated |
| IDE setup | Eclipse + manual JAR | IntelliJ + Maven (auto-download) |
| Data inspection | `System.out.println` | DataGrip with live SQL queries |
