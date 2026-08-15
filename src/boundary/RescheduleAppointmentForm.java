package boundary;

import control.SecretaryController;
import entity.Appointment;
import utils.DesignUtils;
import utils.UIFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;

public class RescheduleAppointmentForm extends JFrame {
    private SecretaryController controller;
    private Appointment appointment;
    private AppointmentManagementForm parentForm;

    private JComboBox<LocalDate> dateCombo;
    private JComboBox<LocalTime> timeCombo;
    private JButton rescheduleBtn;

    public RescheduleAppointmentForm(Appointment appointment, AppointmentManagementForm parentForm) {
        this.controller = new SecretaryController();
        this.appointment = appointment;
        this.parentForm = parentForm;

        setTitle("Reschedule Appointment");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);
        setContentPane(new utils.GradientPanel());
        setLayout(new BorderLayout(10, 10));

        JLabel title = UIFactory.createLabel("Reschedule Appointment");
        title.setFont(DesignUtils.TITLE_FONT);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        add(title, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        formPanel.setOpaque(false);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        formPanel.add(UIFactory.createLabel("Select New Date:"));
        ArrayList<LocalDate> dates = controller.getUpcomingDateOptions();
        dateCombo = new JComboBox<>(dates.toArray(new LocalDate[0]));
      //.addActionListener(e -> loadAvailableTimes());
        formPanel.add(dateCombo);

        formPanel.add(UIFactory.createLabel("Select New Time:"));
        timeCombo = new JComboBox<>();
        formPanel.add(timeCombo);

        rescheduleBtn = UIFactory.createButton("Confirm Reschedule");
      //  rescheduleBtn.addActionListener(this::handleReschedule);

        add(formPanel, BorderLayout.CENTER);
        add(rescheduleBtn, BorderLayout.SOUTH);

    //    loadAvailableTimes();
        setVisible(true);
    }
    /*
    private void loadAvailableTimes() {
        LocalDate selectedDate = (LocalDate) dateCombo.getSelectedItem();
        if (selectedDate != null) {
            ArrayList<LocalTime> times = controller.getAvailableTimeSlots(selectedDate, false, 30);
            timeCombo.removeAllItems();
            for (LocalTime time : times) {
                timeCombo.addItem(time);
            }
        }
    }

    private void handleReschedule(ActionEvent e) {
        LocalDate newDate = (LocalDate) dateCombo.getSelectedItem();
        LocalTime newTime = (LocalTime) timeCombo.getSelectedItem();

        if (newDate == null || newTime == null) {
            JOptionPane.showMessageDialog(this, "Please select both date and time.");
            return;
        }

        boolean success = controller.rescheduleAppointment(appointment.getAppointmentId(), newDate, newTime);
        if (success) {
            JOptionPane.showMessageDialog(this, "Appointment rescheduled successfully.");
            parentForm.refreshData(); // ✅ Refresh parent table
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to reschedule appointment.");
        }
    }
    */
}
