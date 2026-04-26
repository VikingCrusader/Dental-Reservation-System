package model;

/**
 * Represents a dentist or staff member.
 * Maps to the "employees" table in dental.db.
 */
public class Employee {

    private int    id;
    private String name;
    private String username;
    private String password;
    private String role;      // e.g. "dentist", "staff"

    // ── Constructors ──────────────────────────────────────────────────────────

    public Employee() {}

    public Employee(String name, String username, String password, String role) {
        this.name     = name;
        this.username = username;
        this.password = password;
        this.role     = role;
    }

    public Employee(int id, String name, String username, String password, String role) {
        this(name, username, password, role);
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

    public String getRole()              { return role; }
    public void   setRole(String role)   { this.role = role; }

    @Override
    public String toString() {
        return name + " [" + role + "]";
    }
}
