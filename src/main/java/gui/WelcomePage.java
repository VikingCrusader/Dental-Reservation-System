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

    // DAO instances shared across all methods in this class, created once at class load
    private static final PatientDAO  patientDAO  = new PatientDAO();
    private static final EmployeeDAO employeeDAO = new EmployeeDAO();

    // "fromLogin" : Flag indicating the caller of registration():
    // fromLogin  → opened from the Login page   (Go Back / success → return to login)
    // !fromLogin → opened from EmployeeMenu     (Go Back → employee menu reopens itself; success → open CalendarView)
    private static boolean fromLogin;

    // ── LOGIN ─────────────────────────────────────────────────────────────────

    public static void login() {

        // Main Page
        JFrame frame = new JFrame("Project 3 – Dental Reservation");
        frame.setSize(420, 480);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null); // center the page

        // Add panel to the main page
        JPanel panel = new JPanel(null);
        frame.add(panel);

        // Title
        JLabel title = new JLabel("USER LOGIN", JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 15));
        title.setBounds(85, 15, 240, 30);
        panel.add(title);

        // Image
        ImageIcon logo = new ImageIcon("images/WelcomeImage.png");
        Image scaledLogo = logo.getImage().getScaledInstance(300, 150, Image.SCALE_SMOOTH);
        JLabel logoLabel = new JLabel(new ImageIcon(scaledLogo));
        logoLabel.setBounds(60, 55, 300, 150);
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

        // Authors Button (Added at 2026-04-30)
        JButton btnAuthors = new JButton("See Creators");
        btnAuthors.setBounds( 130, 400, 160, 20);
        btnAuthors.addActionListener(e -> {

            ImageIcon Authors = new ImageIcon("images/Authors.png");
            Image scaledAuthors = Authors.getImage().getScaledInstance(500, 272, Image.SCALE_SMOOTH);
            JLabel AuthorsPhoto = new JLabel(new ImageIcon(scaledAuthors));
            AuthorsPhoto.setHorizontalAlignment(JLabel.CENTER);
            AuthorsPhoto.setAlignmentX(Component.CENTER_ALIGNMENT);

            JTextArea textArea = new JTextArea(
                    "Authors:\n\n" +
                            "• Sohrab Dokmechin\n" +
                            "• Yiwen Zhang\n" +
                            "• Jingsen Huang\n" +
                            "• Haochen Gao\n" +
                            "From Technische Universität Berlin Summer School\n" +
                            "2025-08-14, in Berlin, Germany\n"
            );
            textArea.setFont(new Font("SansSerif", Font.BOLD, 20));
            textArea.setEditable(false);
            textArea.setBackground(new Color(240, 240, 240));
            textArea.setAlignmentX(Component.CENTER_ALIGNMENT);

            JPanel contentPanel = new JPanel();
            contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
            contentPanel.setBackground(new Color(240, 240, 240));
            contentPanel.add(AuthorsPhoto);
            contentPanel.add(Box.createVerticalStrut(10));
            contentPanel.add(textArea);

            JOptionPane.showMessageDialog(frame, contentPanel, "About the Authors", JOptionPane.PLAIN_MESSAGE);
        });
        panel.add(btnAuthors);


        // ── Login logic ───────────────────────────────────────────────────────
        btnLogin.addActionListener(e -> {
            // get username and password from TextField and PasswordField from the main page GUI
            String username = txtUser.getText().trim();
            String password = new String(txtPass.getPassword());

            // non-empty verification
            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(frame,
                        "Please enter username and password.",
                        "Input Required", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 1. Check employees
            // Access the DB, using the method "getEmployeeByUsername()" to iterate the Employee's Username DB.
            Employee emp = employeeDAO.getEmployeeByUsername(username);
            if (emp != null) {
                if (emp.getPassword().equals(password)) {
                    frame.dispose(); //if match, then close the window
                    new EmployeeMenu(emp); // and then open employee menu
                } else {
                    JOptionPane.showMessageDialog(frame,
                            "Incorrect password.", "Login Error",
                            JOptionPane.ERROR_MESSAGE);
                }
                return;
            }

            // 2. Check patients, same logic as employees
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

        // After all components added done, set the panel visible
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
        WelcomePage.fromLogin = fromLogin;
        JFrame frame = new JFrame("Register New Patient");
        frame.setSize(420, 360);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel(null);
        frame.add(panel);

        String[] labelTexts = {"Name", "Username", "Password", "Email", "Address", "Mobile Number"};
        JTextField[] fields = new JTextField[6];

        for (int i = 0; i < labelTexts.length; i++) {
            JLabel lbl = new JLabel(labelTexts[i]);
            lbl.setBounds(40, 20 + i * 40, 110, 25); // make sure equal longitudinal spacing
            panel.add(lbl);

            // Password field for index 2
            fields[i] = (i == 2) ? new JPasswordField() : new JTextField(); // if it's pwd field, apply pwd format
            fields[i].setBounds(155, 20 + i * 40, 220, 26);
            panel.add(fields[i]);
        }

        // define back button
        JButton btnBack = new JButton("Go Back");
        btnBack.setBounds(70, 268, 120, 32);
        panel.add(btnBack);

        // define register button
        JButton btnReg = new JButton("Register");
        btnReg.setBounds(230, 268, 120, 32);
        panel.add(btnReg);

        // add back listener
        btnBack.addActionListener(e -> {
            frame.dispose();
            if (fromLogin) login();
            // Because there are two origins to open the registration menu, so we need a branch statement.
            // if fromLogin == true, reopen Login() panel.
            // if !fromLogin the EmployeeMenu that opened us will reopen itself.
        });

        // ── Register ──────────────────────────────────────────────────────────
        btnReg.addActionListener(e -> {
            String name  = fields[0].getText().trim();
            String uname = fields[1].getText().trim();
            String pass  = fields[2].getText().trim();   // JPasswordField.getText() is fine here

            // check non-empty key values.
            if (name.isEmpty() || uname.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(frame,
                        "Name, Username and Password are required.",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // check repeat
            if (patientDAO.getPatientByUsername(uname) != null) {
                JOptionPane.showMessageDialog(frame,
                        "Username \"" + uname + "\" is already taken.",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Generate new patient object using registered attributes
            Patient p = new Patient(name, uname, pass,
                    fields[3].getText().trim(),
                    fields[4].getText().trim(),
                    fields[5].getText().trim());

            int newId = patientDAO.addPatient(p);
            // if the new patient is successfully created, the return patientDAO.addPatient() will be the new ID of the patient, which is positive, otherwise it's -1.
            if (newId > 0) {
                JOptionPane.showMessageDialog(frame,
                        "Account created! You can now log in.",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                frame.dispose();
                if (fromLogin) {
                    // Patients registered him/herself via main menu → login page
                    login();
                } else {
                    // Employee registered a new patient → go straight to booking in the calender page
                    new CalendarView(p, new AppointmentDAO(), true);
                }
            } else {
                JOptionPane.showMessageDialog(frame,
                        "Registration failed. Please try a different username.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // After all components added done, set the panel visible
        frame.setVisible(true);
    }
}