package gui;

import database.AppointmentDAO;
import model.Appointment;
import model.Patient;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Dashboard shown after a patient logs in.
 */
public class PatientMenu {

    private final Patient        patient;
    private final AppointmentDAO apptDAO = new AppointmentDAO();

    public PatientMenu(Patient patient) {
        this.patient = patient;
        buildUI();
    }

    private void buildUI() {
        JFrame frame = new JFrame("Patient Portal – " + patient.getName());
        frame.setSize(300, 250);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel(null);
        frame.add(panel);

        JLabel welcome = new JLabel("Welcome, " + patient.getName() + "!", JLabel.CENTER);
        welcome.setFont(new Font("Arial", Font.BOLD, 13));
        welcome.setBounds(20, 12, 260, 25);
        panel.add(welcome);

        String[] labels = {
                "My Appointments",
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
            case 0 -> showMyAppointments(frame);
            case 1 -> { frame.dispose(); new CalendarView(patient, apptDAO, false); }
            case 2 -> showCancelScreen(frame);
            case 3 -> { frame.dispose(); WelcomePage.login(); }
        }
    }

    // ── My Appointments ───────────────────────────────────────────────────────

    private void showMyAppointments(JFrame parent) {
        List<Appointment> list = apptDAO.getAppointmentsByPatient(patient);

        JDialog dialog = new JDialog(parent, "My Appointments", true);
        dialog.setSize(360, 260);
        dialog.setLocationRelativeTo(parent);

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        dialog.add(panel);

        if (list.isEmpty()) {
            panel.add(new JLabel("You have no upcoming appointments.", JLabel.CENTER),
                    BorderLayout.CENTER);
        } else {
            DefaultListModel<String> model = new DefaultListModel<>();
            for (Appointment a : list) {
                model.addElement("  " + a.getDateTime().toLocalDate()
                        + "  at " + String.format("%02d:00", a.getDateTime().getHour()));
            }
            panel.add(new JScrollPane(new JList<>(model)), BorderLayout.CENTER);
        }

        JButton ok = new JButton("OK");
        ok.addActionListener(e -> dialog.dispose());
        JPanel bot = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bot.add(ok);
        panel.add(bot, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    // ── Cancel Appointment ────────────────────────────────────────────────────

    private void showCancelScreen(JFrame parent) {
        List<Appointment> list = apptDAO.getAppointmentsByPatient(patient);

        JDialog dialog = new JDialog(parent, "Cancel Appointment", true);
        dialog.setSize(380, 290);
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
                String label = a.getDateTime().toLocalDate()
                        + "  at " + String.format("%02d:00", a.getDateTime().getHour());
                JButton btn = new JButton(label);
                btn.setAlignmentX(Component.CENTER_ALIGNMENT);
                btn.addActionListener(e -> {
                    int ok = JOptionPane.showConfirmDialog(dialog,
                            "Cancel appointment on " + label + "?",
                            "Confirm Cancellation", JOptionPane.YES_NO_OPTION);
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
