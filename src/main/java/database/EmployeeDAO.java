package database;

import model.Employee;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object — all SQL for the "employees" table.
 */
public class EmployeeDAO {

    // ── CREATE ────────────────────────────────────────────────────────────────

    public int addEmployee(Employee employee) {
        String sql = """
            INSERT INTO employees (name, username, password, role)
            VALUES (?, ?, ?, ?)
        """;

        try (PreparedStatement ps = DatabaseManager.getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, employee.getName());
            ps.setString(2, employee.getUsername());
            ps.setString(3, employee.getPassword());
            ps.setString(4, employee.getRole());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                employee.setId(keys.getInt(1));
                return employee.getId();
            }

        } catch (SQLException e) {
            System.err.println("[EmployeeDAO] addEmployee: " + e.getMessage());
        }
        return -1;
    }

    // ── READ ──────────────────────────────────────────────────────────────────

    public List<Employee> getAllEmployees() {
        List<Employee> list = new ArrayList<>();
        String sql = "SELECT * FROM employees ORDER BY name";

        try (Statement stmt = DatabaseManager.getConnection().createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {

            while (rs.next()) list.add(mapRow(rs));

        } catch (SQLException e) {
            System.err.println("[EmployeeDAO] getAllEmployees: " + e.getMessage());
        }
        return list;
    }

    /**
     * Finds an employee by username — used for login.
     * @return Employee object, or null if not found.
     */
    public Employee getEmployeeByUsername(String username) {
        String sql = "SELECT * FROM employees WHERE username = ?";

        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);

        } catch (SQLException e) {
            System.err.println("[EmployeeDAO] getEmployeeByUsername: " + e.getMessage());
        }
        return null;
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private Employee mapRow(ResultSet rs) throws SQLException {
        return new Employee(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("username"),
                rs.getString("password"),
                rs.getString("role")
        );
    }
}
