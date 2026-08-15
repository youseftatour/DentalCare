package boundary;

import control.PatientController;
import utils.UIFactory;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import java.awt.*;
import java.util.Date;
import java.util.function.Consumer;

public class PatientBookAppointmentForm extends JDialog {

    public PatientBookAppointmentForm(int patientId, Runnable refreshCallback) {
        setTitle("Book Appointment");
        setModal(true);
        setSize(400, 350);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(6, 1, 10, 10));

        JLabel reasonLabel = UIFactory.createLabel("Reason:");
        JTextField reasonField = UIFactory.createTextField();

        JLabel dateLabel = UIFactory.createLabel("Date:");
        JDateChooser dateChooser = new JDateChooser();
        dateChooser.setDate(new Date());

        JLabel timeLabel = UIFactory.createLabel("Time (HH:mm):");
        JTextField timeField = UIFactory.createTextField();

        JLabel treatmentLabel = UIFactory.createLabel("Treatment Name:");
        JTextField treatmentField = UIFactory.createTextField();

        JButton bookBtn = UIFactory.createButton("Book");
        bookBtn.addActionListener(e -> {
            String reason = reasonField.getText().trim();
            Date date = dateChooser.getDate();
            java.sql.Date sqlDate = new java.sql.Date(date.getTime());

            String time = timeField.getText().trim();
            String treatment = treatmentField.getText().trim();

            if (reason.isEmpty() || time.isEmpty() || treatment.isEmpty() || date == null) {
                JOptionPane.showMessageDialog(this, "Please fill all fields.");
                return;
            }

            boolean success = new PatientController().bookAppointment(patientId, sqlDate, time, reason, treatment);
            if (success) {
                JOptionPane.showMessageDialog(this, "Appointment booked successfully.");
                refreshCallback.run();
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to book appointment.");
            }
        });

        add(reasonLabel);
        add(reasonField);
        add(dateLabel);
        add(dateChooser);
        add(timeLabel);
        add(timeField);
        add(treatmentLabel);
        add(treatmentField);
        add(bookBtn);
    }
}
