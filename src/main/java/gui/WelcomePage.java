package gui;

import database.AppointmentDAO;
import database.EmployeeDAO;
import database.PatientDAO;
import model.Employee;
import model.Patient;

import javax.swing.*;
import java.awt.*;

/**
 * Login screen + patient registration form.
 * This is the first window the user sees.
 */
public class WelcomePage {

    private static final PatientDAO  patientDAO  = new PatientDAO();
    private static final EmployeeDAO employeeDAO = new EmployeeDAO();

    // ── LOGIN ─────────────────────────────────────────────────────────────────

    public static void login() {
        JFrame frame = new JFrame("Teethos – Dental Reservation");
        frame.setSize(420, 480);                          // 窗口稍窄一点，高度够用
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel(null);
        frame.add(panel);

        // Title — 居中在窗口顶部
        JLabel title = new JLabel("=== USER LOGIN ===", JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 15));
        title.setBounds(85, 15, 240, 30);               // 水平居中
        panel.add(title);

        // Image — 原图 1024x512，按比例缩到宽300，高150
        ImageIcon logo = new ImageIcon("images/WelcomeImage.png");
        Image scaledLogo = logo.getImage().getScaledInstance(300, 150, Image.SCALE_SMOOTH);
        JLabel logoLabel = new JLabel(new ImageIcon(scaledLogo));
        logoLabel.setBounds(60, 55, 300, 150);           // 宽300 保持 2:1 比例→高150
        panel.add(logoLabel);

        // Username row
        JLabel lblUser = new JLabel("Username:");
        lblUser.setBounds(60, 230, 90, 25);
        panel.add(lblUser);

        JTextField txtUser = new JTextField();
        txtUser.setBounds(155, 230, 190, 28);
        panel.add(txtUser);

        // Password row
        JLabel lblPass = new JLabel("Password:");
        lblPass.setBounds(60, 275, 90, 25);
        panel.add(lblPass);

        JPasswordField txtPass = new JPasswordField();
        txtPass.setBounds(155, 275, 190, 28);
        panel.add(txtPass);

        // Divider
        JSeparator sep = new JSeparator();
        sep.setBounds(60, 320, 290, 5);
        panel.add(sep);

        // Buttons
        JButton btnLogin    = new JButton("Login");
        JButton btnRegister = new JButton("Create Account");
        btnLogin.setBounds(70, 335, 120, 32);
        btnRegister.setBounds(210, 335, 130, 32);
        panel.add(btnLogin);
        panel.add(btnRegister);

        // ── Login logic ───────────────────────────────────────────────────────
        btnLogin.addActionListener(e -> {
            String username = txtUser.getText().trim();
            String password = new String(txtPass.getPassword());

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(frame,
                        "Please enter username and password.",
                        "Input Required", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 1. Check employees
            Employee emp = employeeDAO.getEmployeeByUsername(username);
            if (emp != null) {
                if (emp.getPassword().equals(password)) {
                    frame.dispose();
                    new EmployeeMenu(emp);
                } else {
                    JOptionPane.showMessageDialog(frame,
                            "Incorrect password.", "Login Error",
                            JOptionPane.ERROR_MESSAGE);
                }
                return;
            }

            // 2. Check patients
            Patient pat = patientDAO.getPatientByUsername(username);
            if (pat != null) {
                if (pat.getPassword().equals(password)) {
                    frame.dispose();
                    new PatientMenu(pat);
                } else {
                    JOptionPane.showMessageDialog(frame,
                            "Incorrect password.", "Login Error",
                            JOptionPane.ERROR_MESSAGE);
                }
                return;
            }

            // 3. Not found
            JOptionPane.showMessageDialog(frame,
                    "Username not found.\nPlease register first.",
                    "Login Error", JOptionPane.ERROR_MESSAGE);
        });

        // ── Register button ───────────────────────────────────────────────────
        btnRegister.addActionListener(e -> {
            frame.dispose();
            registration(true);
        });

        frame.setVisible(true);
    }

    // ── REGISTRATION ──────────────────────────────────────────────────────────

    /**
     * Opens the patient registration form.
     *
     * @param fromLogin true  → "Go Back" button returns to login screen
     *                  false → called from EmployeeMenu (back goes to employee menu)
     */
    public static void registration(boolean fromLogin) {
        JFrame frame = new JFrame("Register New Patient");
        frame.setSize(420, 360);                         // 紧凑一些
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel(null);
        frame.add(panel);

        String[] labelTexts = {"Name", "Username", "Password", "Email", "Address", "Mobile Number"};
        JTextField[] fields = new JTextField[6];

        for (int i = 0; i < labelTexts.length; i++) {
            JLabel lbl = new JLabel(labelTexts[i]);
            lbl.setBounds(40, 20 + i * 40, 110, 25);    // 行间距40，左边距40
            panel.add(lbl);

            // Password field for index 2
            fields[i] = (i == 2) ? new JPasswordField() : new JTextField();
            fields[i].setBounds(155, 20 + i * 40, 220, 26);
            panel.add(fields[i]);
        }

        JButton btnBack = new JButton("Go Back");
        btnBack.setBounds(70, 268, 120, 32);
        panel.add(btnBack);

        JButton btnReg = new JButton("Register");
        btnReg.setBounds(230, 268, 120, 32);
        panel.add(btnReg);

        // ── Go Back ───────────────────────────────────────────────────────────
        btnBack.addActionListener(e -> {
            frame.dispose();
            if (fromLogin) login();
            // if !fromLogin the EmployeeMenu that opened us will reopen itself
        });

        // ── Register ──────────────────────────────────────────────────────────
        btnReg.addActionListener(e -> {
            String name  = fields[0].getText().trim();
            String uname = fields[1].getText().trim();
            String pass  = fields[2].getText().trim();   // JPasswordField.getText() is fine here

            if (name.isEmpty() || uname.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(frame,
                        "Name, Username and Password are required.",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (patientDAO.getPatientByUsername(uname) != null) {
                JOptionPane.showMessageDialog(frame,
                        "Username \"" + uname + "\" is already taken.",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Patient p = new Patient(name, uname, pass,
                    fields[3].getText().trim(),
                    fields[4].getText().trim(),
                    fields[5].getText().trim());

            int newId = patientDAO.addPatient(p);
            if (newId > 0) {
                JOptionPane.showMessageDialog(frame,
                        "Account created! You can now log in.",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                frame.dispose();
                if (fromLogin) {
                    login();
                } else {
                    // Employee registered a new patient → go straight to booking
                    new CalendarView(p, new AppointmentDAO(), true);
                }
            } else {
                JOptionPane.showMessageDialog(frame,
                        "Registration failed. Please try a different username.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        frame.setVisible(true);
    }
}