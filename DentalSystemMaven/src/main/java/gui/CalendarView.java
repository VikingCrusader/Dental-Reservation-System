package gui;

import database.AppointmentDAO;
import model.Patient;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * Appointment booking screen.
 *
 * Shows a date picker (JSpinner) and a row of time-slot buttons.
 * Booked slots are greyed out (queried live from the DB).
 * Clicking a free slot asks for confirmation then saves to DB.
 */
public class CalendarView {

    /** Available appointment hours (24h). Extend this list freely. */
    private static final int[] HOURS = {9, 10, 11, 13, 14, 15, 16};

    private final Patient        patient;
    private final AppointmentDAO apptDAO;
    private final boolean        isEmployee;   // affects "Go Back" destination

    private JFrame   frame;
    private JSpinner datePicker;
    private JPanel   slotsPanel;

    public CalendarView(Patient patient, AppointmentDAO apptDAO, boolean isEmployee) {
        this.patient    = patient;
        this.apptDAO    = apptDAO;
        this.isEmployee = isEmployee;
        buildUI();
    }

    private void buildUI() {
        frame = new JFrame("Book Appointment – " + patient.getName());
        frame.setSize(460, 290);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        JPanel main = new JPanel(new BorderLayout(8, 8));
        main.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        frame.add(main);

        // ── Top: date picker + Load button ───────────────────────────────────
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        topPanel.add(new JLabel("Date:"));

        SpinnerDateModel model = new SpinnerDateModel();
        datePicker = new JSpinner(model);
        datePicker.setEditor(new JSpinner.DateEditor(datePicker, "yyyy-MM-dd"));
        datePicker.setPreferredSize(new Dimension(130, 28));
        topPanel.add(datePicker);

        JButton btnLoad = new JButton("Show Slots");
        topPanel.add(btnLoad);
        main.add(topPanel, BorderLayout.NORTH);

        // ── Centre: time-slot buttons ─────────────────────────────────────────
        slotsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        slotsPanel.setBorder(BorderFactory.createTitledBorder(
                "Click a green slot to book"));
        main.add(new JScrollPane(slotsPanel), BorderLayout.CENTER);

        // ── Bottom: Go Back ───────────────────────────────────────────────────
        JButton btnBack = new JButton("Go Back");
        JPanel botPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        botPanel.add(btnBack);
        main.add(botPanel, BorderLayout.SOUTH);

        // ── Listeners ─────────────────────────────────────────────────────────
        btnLoad.addActionListener(e -> refreshSlots());
        btnBack.addActionListener(e -> goBack());

        // Render slots for today immediately
        refreshSlots();
        frame.setVisible(true);
    }

    // ── Slot rendering ────────────────────────────────────────────────────────

    private void refreshSlots() {
        // Convert spinner value (java.util.Date) → LocalDate
        Date spinnerDate = (Date) datePicker.getValue();
        LocalDate chosen = spinnerDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        slotsPanel.removeAll();

        for (int hour : HOURS) {
            LocalDateTime slot = chosen.atTime(hour, 0);
            boolean taken = apptDAO.isSlotTaken(slot);

            JButton btn = new JButton(String.format("%02d:00", hour));
            btn.setPreferredSize(new Dimension(80, 38));
            btn.setFocusPainted(false);

            if (taken) {
                btn.setEnabled(false);
                btn.setBackground(new Color(220, 220, 220));
                btn.setToolTipText("Already booked");
            } else {
                btn.setBackground(new Color(144, 238, 144));   // light green
                btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                btn.addActionListener(e -> confirmBooking(slot));
            }
            slotsPanel.add(btn);
        }

        slotsPanel.revalidate();
        slotsPanel.repaint();
    }

    // ── Booking confirmation ──────────────────────────────────────────────────

    private void confirmBooking(LocalDateTime slot) {
        String when = slot.toLocalDate() + "  "
                + String.format("%02d:00", slot.getHour());

        int answer = JOptionPane.showConfirmDialog(frame,
                "Book appointment for " + patient.getName() + "\n" + when + "?",
                "Confirm Booking", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (answer != JOptionPane.YES_OPTION) return;

        boolean ok = apptDAO.addAppointment(patient, slot);
        if (ok) {
            JOptionPane.showMessageDialog(frame,
                    "✓ Appointment booked!\n" + when,
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            frame.dispose();
            goBack();
        } else {
            JOptionPane.showMessageDialog(frame,
                    "That slot was just taken. Please choose another.",
                    "Booking Failed", JOptionPane.ERROR_MESSAGE);
            refreshSlots();
        }
    }

    private void goBack() {
        frame.dispose();
        if (isEmployee) new EmployeeMenu(null);
        else            new PatientMenu(patient);
    }
}
