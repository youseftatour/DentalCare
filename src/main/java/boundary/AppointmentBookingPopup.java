package boundary;

import com.toedter.calendar.JDateChooser;
import control.PatientController;
import entity.Treatment;

import javax.swing.*;
import java.awt.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;

public class AppointmentBookingPopup extends JDialog {

    public AppointmentBookingPopup(JFrame parent, int patientId, ArrayList<Treatment> allTreatments, Runnable onSuccess) {
        super(parent, "Book New Appointment", true);
        setSize(400, 350);
        setLocationRelativeTo(parent);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Date chooser
        gbc.gridx = 0; gbc.gridy = 0;
        add(new JLabel("Select Date:"), gbc);
        gbc.gridx = 1;
        JDateChooser dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("dd-MM-yyyy");
        add(dateChooser, gbc);

        // Time dropdown
        gbc.gridx = 0; gbc.gridy++;
        add(new JLabel("Select Time:"), gbc);
        gbc.gridx = 1;
        JComboBox<String> timeCombo = new JComboBox<>();
        for (int hour = 9; hour <= 20; hour++) {
            timeCombo.addItem(String.format("%02d:00", hour));
        }
        add(timeCombo, gbc);

        // Reason
        gbc.gridx = 0; gbc.gridy++;
        add(new JLabel("Reason:"), gbc);
        gbc.gridx = 1;
        JTextField reasonField = new JTextField();
        add(reasonField, gbc);

        // Treatment dropdown
        gbc.gridx = 0; gbc.gridy++;
        add(new JLabel("Treatment:"), gbc);
        gbc.gridx = 1;
        JComboBox<String> treatmentCombo = new JComboBox<>();
        for (Treatment t : allTreatments) {
            treatmentCombo.addItem(t.getName());
        }
        add(treatmentCombo, gbc);

        // Submit button
        gbc.gridx = 0; gbc.gridy++;
        gbc.gridwidth = 2;
        JButton bookButton = new JButton("Book Appointment");
        add(bookButton, gbc);

        bookButton.addActionListener(e -> {
            java.util.Date selectedDate = dateChooser.getDate();
            if (selectedDate == null) {
                JOptionPane.showMessageDialog(this, "Please select a valid date.");
                return;
            }

            LocalDate today = LocalDate.now();
            LocalDate chosen = new java.sql.Date(selectedDate.getTime()).toLocalDate();
            if (chosen.isBefore(today)) {
                JOptionPane.showMessageDialog(this, "Appointment date can't be in the past.");
                return;
            }

            String time = (String) timeCombo.getSelectedItem();
            String reason = reasonField.getText().trim();
            String treatment = (String) treatmentCombo.getSelectedItem();

            if (reason.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please provide a reason.");
                return;
            }

            // Book it
            PatientController controller = new PatientController();
            boolean success = controller.bookAppointment(
                    patientId,
                    new Date(selectedDate.getTime()),
                    time,
                    reason,
                    treatment
            );

            if (success) {
                JOptionPane.showMessageDialog(this, "Appointment booked successfully!");
                dispose();
                onSuccess.run(); // Refresh table in parent
            } else {
                JOptionPane.showMessageDialog(this, "Failed to book appointment.");
            }
        });
    }
}
