package gui;

import database.AppointmentDAO;
import model.Employee;
import model.Patient;

import javax.swing.*;
import java.awt.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.MonthDay;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Set;

/**
 * Appointment booking screen.
 */
public class CalendarView {

    /** Available appointment hours – skip noon break (12). */
    private static final int[] HOURS = {9, 10, 11, 13, 14, 15, 16};

    /**
     * Czech public holidays (fixed dates only).
     * Easter Monday is the only movable holiday and is omitted here.
     */
    private static final Set<MonthDay> CZ_HOLIDAYS = Set.of(
            MonthDay.of( 1,  1),  // New Year's Day / Restoration of Czech Independence
            MonthDay.of( 5,  1),  // Labour Day
            MonthDay.of( 5,  8),  // Victory in Europe Day
            MonthDay.of( 7,  5),  // Sts Cyril & Methodius Day
            MonthDay.of( 7,  6),  // Jan Hus Day
            MonthDay.of( 9, 28),  // Czech Statehood Day
            MonthDay.of(10, 28),  // Czechoslovak Independence Day
            MonthDay.of(11, 17),  // Struggle for Freedom and Democracy Day
            MonthDay.of(12, 24),  // Christmas Eve
            MonthDay.of(12, 25),  // Christmas Day
            MonthDay.of(12, 26)   // St Stephen's Day
    );

    // ── Colour palette ────────────────────────────────────────────────────────
    private static final Color COL_SELECTED    = new Color( 70, 130, 180); // steel blue
    private static final Color COL_TODAY       = new Color(255, 223, 128); // amber
    private static final Color COL_FREE        = new Color(220, 245, 220); // light green
    private static final Color COL_PARTIAL     = new Color(255, 220, 150); // orange
    private static final Color COL_FULL        = new Color(255, 180, 180); // light red
    private static final Color COL_UNAVAILABLE = new Color(255, 180, 180); // light red (weekend/holiday)
    private static final Color COL_PAST        = new Color(220, 220, 220); // grey

    // ── Fields ────────────────────────────────────────────────────────────────
    private final Patient        patient;
    private final AppointmentDAO apptDAO;
    private final boolean        isEmployee;
    private final Employee       employee;   // non-null when opened by an employee

    private JFrame    frame;
    private JPanel    calendarPanel;
    private JPanel    slotsPanel;
    private JLabel    monthLabel;

    private LocalDate currentMonth;  // first day of the displayed month
    private LocalDate selectedDate;  // the day the user clicked

    // ── Constructor ───────────────────────────────────────────────────────────

    public CalendarView(Patient patient, AppointmentDAO apptDAO, boolean isEmployee, Employee employee) {
        this.patient      = patient;
        this.apptDAO      = apptDAO;
        this.isEmployee   = isEmployee;
        this.employee     = employee;
        this.currentMonth = LocalDate.now().withDayOfMonth(1);
        this.selectedDate = LocalDate.now();
        buildUI();
    }

    // ── Main layout ───────────────────────────────────────────────────────────

    private void buildUI() {
        frame = new JFrame("Book Appointment – " + patient.getName());
        frame.setSize(660, 380);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        frame.add(main);

        // ── Left: calendar ────────────────────────────────────────────────────
        JPanel leftWrapper = new JPanel(new BorderLayout(4, 4));
        leftWrapper.setBorder(BorderFactory.createTitledBorder("Select Date"));

        // Month nav bar
        JPanel navBar = new JPanel(new BorderLayout());
        JButton btnPrev = new JButton("◀");
        JButton btnNext = new JButton("▶");
        monthLabel = new JLabel("", JLabel.CENTER);
        monthLabel.setFont(new Font("Arial", Font.BOLD, 13));
        navBar.add(btnPrev,    BorderLayout.WEST);
        navBar.add(monthLabel, BorderLayout.CENTER);
        navBar.add(btnNext,    BorderLayout.EAST);
        leftWrapper.add(navBar, BorderLayout.NORTH);

        // Calendar grid placeholder
        calendarPanel = new JPanel();
        leftWrapper.add(calendarPanel, BorderLayout.CENTER);

        // Legend
        leftWrapper.add(buildLegend(), BorderLayout.SOUTH);

        btnPrev.addActionListener(e -> { currentMonth = currentMonth.minusMonths(1); rebuildCalendar(); });
        btnNext.addActionListener(e -> { currentMonth = currentMonth.plusMonths(1);  rebuildCalendar(); });

        main.add(leftWrapper, BorderLayout.WEST);

        // ── Right: time slots ─────────────────────────────────────────────────
        slotsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        slotsPanel.setBorder(BorderFactory.createTitledBorder("Time slots"));
        main.add(new JScrollPane(slotsPanel), BorderLayout.CENTER);

        // ── Bottom: Go Back ───────────────────────────────────────────────────
        JButton btnBack = new JButton("Go Back");
        JPanel botPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        botPanel.add(btnBack);
        main.add(botPanel, BorderLayout.SOUTH);

        btnBack.addActionListener(e -> goBack());

        rebuildCalendar();
        refreshSlots();
        frame.setVisible(true);
    }

    /** Colour legend strip shown below the calendar grid. */
    private JPanel buildLegend() {
        JPanel p = new JPanel(new GridLayout(0, 2, 4, 1));
        p.setBorder(BorderFactory.createEmptyBorder(6, 2, 2, 2));
        addLegendItem(p, COL_FREE,        "Available");
        addLegendItem(p, COL_PARTIAL,     "Partly booked");
        addLegendItem(p, COL_FULL,        "Fully booked");
        addLegendItem(p, COL_UNAVAILABLE, "Weekend / Holiday");
        return p;
    }

    private void addLegendItem(JPanel parent, Color colour, String text) {
        JLabel dot = new JLabel("■");
        dot.setForeground(colour);
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Arial", Font.PLAIN, 10));
        parent.add(dot);
        parent.add(lbl);
    }

    // ── Calendar grid ─────────────────────────────────────────────────────────

    private void rebuildCalendar() {
        monthLabel.setText(
                currentMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH)
                        + " " + currentMonth.getYear());

        calendarPanel.removeAll();
        calendarPanel.setLayout(new GridLayout(0, 7, 2, 2));

        // Day-of-week headers
        for (String name : new String[]{"Mo", "Tu", "We", "Th", "Fr", "Sa", "Su"}) {
            JLabel lbl = new JLabel(name, JLabel.CENTER);
            lbl.setFont(new Font("Arial", Font.BOLD, 11));
            lbl.setForeground(new Color(80, 80, 80));
            calendarPanel.add(lbl);
        }

        // Leading blank cells before the 1st
        int offset = currentMonth.getDayOfWeek().getValue() - 1; // Mon=0
        for (int i = 0; i < offset; i++) calendarPanel.add(new JLabel(""));

        LocalDate today = LocalDate.now();

        for (int day = 1; day <= currentMonth.lengthOfMonth(); day++) {
            LocalDate date = currentMonth.withDayOfMonth(day);
            JButton btn = new JButton(String.valueOf(day));
            btn.setFont(new Font("Arial", Font.PLAIN, 12));
            btn.setFocusPainted(false);
            btn.setMargin(new Insets(2, 2, 2, 2));

            boolean isSelected = date.equals(selectedDate);
            boolean isPast     = date.isBefore(today);
            boolean isWeekend  = isWeekend(date);
            boolean isHoliday  = CZ_HOLIDAYS.contains(MonthDay.from(date));
            boolean isToday    = date.equals(today);

            if (isSelected) {
                // Selected takes priority over everything for visual feedback
                btn.setBackground(COL_SELECTED);
                btn.setForeground(Color.WHITE);
                btn.setFont(new Font("Arial", Font.BOLD, 12));
                btn.addActionListener(e -> onDateClicked(date));

            } else if (isPast) {
                btn.setEnabled(false);
                btn.setBackground(COL_PAST);
                btn.setToolTipText("Past date");

            } else if (isWeekend || isHoliday) {
                btn.setEnabled(false);
                btn.setBackground(COL_UNAVAILABLE);
                btn.setToolTipText(isHoliday ? "Public holiday" : "Weekend");

            } else {
                // Bookable weekday – check occupancy
                int taken = countTakenSlots(date);
                boolean full = taken >= HOURS.length;

                if (full) {
                    btn.setEnabled(false);
                    btn.setBackground(COL_FULL);
                    btn.setToolTipText("Fully booked");
                } else {
                    // Amber for today, green for future; orange overlay if partially taken
                    if (taken > 0) {
                        btn.setBackground(COL_PARTIAL);
                    } else {
                        btn.setBackground(isToday ? COL_TODAY : COL_FREE);
                    }
                    btn.setToolTipText(taken == 0
                            ? "All slots free"
                            : taken + "/" + HOURS.length + " slots taken");
                    btn.addActionListener(e -> onDateClicked(date));
                }
            }

            calendarPanel.add(btn);
        }

        calendarPanel.revalidate();
        calendarPanel.repaint();
    }

    private void onDateClicked(LocalDate date) {
        selectedDate = date;
        rebuildCalendar();
        refreshSlots();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private boolean isWeekend(LocalDate date) {
        DayOfWeek dow = date.getDayOfWeek();
        return dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY;
    }

    /** Counts how many HOURS slots are already taken on the given date. */
    private int countTakenSlots(LocalDate date) {
        int count = 0;
        for (int hour : HOURS) {
            if (apptDAO.isSlotTaken(date.atTime(hour, 0))) count++;
        }
        return count;
    }

    // ── Time-slot rendering ───────────────────────────────────────────────────

    private void refreshSlots() {
        slotsPanel.removeAll();
        slotsPanel.setBorder(BorderFactory.createTitledBorder(
                "Slots for " + selectedDate + "  –  click a green slot to book"));

        for (int hour : HOURS) {
            LocalDateTime slot  = selectedDate.atTime(hour, 0);
            boolean       taken = apptDAO.isSlotTaken(slot);

            JButton btn = new JButton(String.format("%02d:00", hour));
            btn.setPreferredSize(new Dimension(80, 38));
            btn.setFocusPainted(false);

            if (taken) {
                btn.setEnabled(false);
                btn.setBackground(new Color(220, 220, 220));
                btn.setToolTipText("Already booked");
            } else {
                btn.setBackground(new Color(144, 238, 144)); // light green
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
            goBack();
        } else {
            JOptionPane.showMessageDialog(frame,
                    "That slot was just taken. Please choose another.",
                    "Booking Failed", JOptionPane.ERROR_MESSAGE);
            refreshSlots();
        }
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    private void goBack() {
        frame.dispose();
        if (isEmployee) new EmployeeMenu(employee);
        else            new PatientMenu(patient);
    }
}