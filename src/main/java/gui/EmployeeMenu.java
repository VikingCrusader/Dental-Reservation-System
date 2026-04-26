package gui;

import database.AppointmentDAO;
import database.PatientDAO;
import model.Appointment;
import model.Employee;
import model.Patient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Dashboard shown after an employee (dentist / staff) logs in.
 */
public class EmployeeMenu {

    private final Employee       employee;   // may be null when reopening menu
    private final PatientDAO     patientDAO = new PatientDAO();
    private final AppointmentDAO apptDAO    = new AppointmentDAO();

    public EmployeeMenu(Employee employee) {
        this.employee = employee;
        buildUI();
    }

    private void buildUI() {
        String name = (employee != null) ? employee.getName() : "Staff";

        JFrame frame = new JFrame("Staff Portal – " + name);
        frame.setSize(300, 280);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel(null);
        frame.add(panel);

        JLabel welcome = new JLabel("Welcome, " + name + "!", JLabel.CENTER);
        welcome.setFont(new Font("Arial", Font.BOLD, 13));
        welcome.setBounds(20, 12, 260, 25);
        panel.add(welcome);

        String[] labels = {
                "Patient List",
                "Appointment List",
                "New Appointment",
                "Cancel Appointment",
                "Logout"
        };

        for (int i = 0; i < labels.length; i++) {
            JButton btn = new JButton(labels[i]);
            btn.setBounds(75, 48 + i * 38, 150, 30);
            int idx = i;
            btn.addActionListener(e -> handle(idx, frame));
            panel.add(btn);
        }

        frame.setVisible(true);
    }

    private void handle(int action, JFrame frame) {
        switch (action) {
            case 0 -> showPatientList(frame);
            case 1 -> showAppointmentList(frame);
            case 2 -> findPatientForBooking(frame);
            case 3 -> showCancelScreen(frame);
            case 4 -> { frame.dispose(); WelcomePage.login(); }
        }
    }

    // ── Patient List ──────────────────────────────────────────────────────────

    private void showPatientList(JFrame parent) {
        List<Patient> patients = patientDAO.getAllPatients();

        JDialog dialog = new JDialog(parent, "Patient List", true);
        dialog.setSize(400, 320);
        dialog.setLocationRelativeTo(parent);

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        dialog.add(panel);

        if (patients.isEmpty()) {
            panel.add(new JLabel("No patients registered yet.", JLabel.CENTER),
                    BorderLayout.CENTER);
        } else {
            DefaultListModel<String> model = new DefaultListModel<>();
            for (Patient p : patients)
                model.addElement(p.getName() + "  (@" + p.getUsername() + ")");

            JList<String> jList = new JList<>(model);
            jList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            // Double-click → patient profile popup
            jList.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        int sel = jList.getSelectedIndex();
                        if (sel >= 0) showPatientProfile(dialog, patients.get(sel));
                    }
                }
            });

            panel.add(new JLabel("Double-click a name to view full profile",
                    JLabel.CENTER), BorderLayout.NORTH);
            panel.add(new JScrollPane(jList), BorderLayout.CENTER);
        }

        JButton close = new JButton("Close");
        close.addActionListener(e -> dialog.dispose());
        JPanel bot = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bot.add(close);
        panel.add(bot, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void showPatientProfile(JDialog parent, Patient p) {
        String html = "<html>" +
                "<b>Name:</b> "      + p.getName()      + "<br>" +
                "<b>Username:</b> "  + p.getUsername()  + "<br>" +
                "<b>Email:</b> "     + p.getEmail()     + "<br>" +
                "<b>Address:</b> "   + p.getAddress()   + "<br>" +
                "<b>Telephone:</b> " + p.getTelephone() +
                "</html>";
        JOptionPane.showMessageDialog(parent, html,
                "Profile – " + p.getName(), JOptionPane.INFORMATION_MESSAGE);
    }

    // ── Appointment List ──────────────────────────────────────────────────────

    private void showAppointmentList(JFrame parent) {
        List<Appointment> list = apptDAO.getAllAppointments();

        JDialog dialog = new JDialog(parent, "All Appointments", true);
        dialog.setSize(450, 320);
        dialog.setLocationRelativeTo(parent);

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        dialog.add(panel);

        if (list.isEmpty()) {
            panel.add(new JLabel("No appointments booked yet.", JLabel.CENTER),
                    BorderLayout.CENTER);
        } else {
            DefaultListModel<String> model = new DefaultListModel<>();
            for (Appointment a : list) model.addElement(a.toString());
            panel.add(new JScrollPane(new JList<>(model)), BorderLayout.CENTER);
        }

        JButton close = new JButton("Close");
        close.addActionListener(e -> dialog.dispose());
        JPanel bot = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bot.add(close);
        panel.add(bot, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    // ── New Appointment ───────────────────────────────────────────────────────

    private void findPatientForBooking(JFrame parent) {
        String input = JOptionPane.showInputDialog(parent,
                "Enter patient username\n(leave blank to register a new patient):",
                "Find Patient", JOptionPane.QUESTION_MESSAGE);

        if (input == null) return;   // cancelled

        if (input.trim().isEmpty()) {
            parent.dispose();
            WelcomePage.registration(false);
            return;
        }

        Patient p = patientDAO.getPatientByUsername(input.trim());
        if (p == null) {
            int choice = JOptionPane.showConfirmDialog(parent,
                    "Patient \"" + input + "\" not found.\nRegister as a new patient?",
                    "Not Found", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                parent.dispose();
                WelcomePage.registration(false);
            }
        } else {
            parent.dispose();
            new CalendarView(p, apptDAO, true);
        }
    }

    // ── Cancel Appointment ────────────────────────────────────────────────────

    private void showCancelScreen(JFrame parent) {
        List<Appointment> list = apptDAO.getAllAppointments();

        JDialog dialog = new JDialog(parent, "Cancel Appointment", true);
        dialog.setSize(460, 320);
        dialog.setLocationRelativeTo(parent);

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        dialog.add(panel);

        if (list.isEmpty()) {
            panel.add(new JLabel("No appointments to cancel.", JLabel.CENTER),
                    BorderLayout.CENTER);
        } else {
            JPanel btnPanel = new JPanel();
            btnPanel.setLayout(new BoxLayout(btnPanel, BoxLayout.Y_AXIS));
            for (Appointment a : list) {
                JButton btn = new JButton(a.toString());
                btn.setAlignmentX(Component.CENTER_ALIGNMENT);
                btn.addActionListener(e -> {
                    int ok = JOptionPane.showConfirmDialog(dialog,
                            "Cancel: " + a + "?",
                            "Confirm", JOptionPane.YES_NO_OPTION);
                    if (ok == JOptionPane.YES_OPTION) {
                        apptDAO.removeAppointmentById(a.getId());
                        dialog.dispose();
                    }
                });
                btnPanel.add(btn);
                btnPanel.add(Box.createVerticalStrut(6));
            }
            panel.add(new JScrollPane(btnPanel), BorderLayout.CENTER);
        }

        JButton back = new JButton("Go Back");
        back.addActionListener(e -> dialog.dispose());
        JPanel bot = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bot.add(back);
        panel.add(bot, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }
}
