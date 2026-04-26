package model;

/**
 * Represents a patient in the dental system.
 * Maps to the "patients" table in dental.db.
 */
public class Patient {

    private int    id;
    private String name;
    private String username;
    private String password;
    private String email;
    private String address;
    private String telephone;

    // ── Constructors ──────────────────────────────────────────────────────────

    public Patient() {}

    /** Used when creating a new patient (id not yet assigned by DB). */
    public Patient(String name, String username, String password,
                   String email, String address, String telephone) {
        this.name      = name;
        this.username  = username;
        this.password  = password;
        this.email     = email;
        this.address   = address;
        this.telephone = telephone;
    }

    /** Used when reading a patient back from the database. */
    public Patient(int id, String name, String username, String password,
                   String email, String address, String telephone) {
        this(name, username, password, email, address, telephone);
        this.id = id;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public int    getId()        { return id; }
    public void   setId(int id)  { this.id = id; }

    public String getName()              { return name; }
    public void   setName(String name)   { this.name = name; }

    public String getUsername()                  { return username; }
    public void   setUsername(String username)   { this.username = username; }

    public String getPassword()                  { return password; }
    public void   setPassword(String password)   { this.password = password; }

    public String getEmail()               { return email; }
    public void   setEmail(String email)   { this.email = email; }

    public String getAddress()                 { return address; }
    public void   setAddress(String address)   { this.address = address; }

    public String getTelephone()                     { return telephone; }
    public void   setTelephone(String telephone)     { this.telephone = telephone; }

    @Override
    public String toString() {
        return name + " (@" + username + ")";
    }
}
