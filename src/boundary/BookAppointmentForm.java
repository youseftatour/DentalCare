package boundary;

import com.toedter.calendar.JDateChooser;
import control.SecretaryController;
import entity.Patient;
import entity.StaffMember;
import utils.UIFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.List;

public class BookAppointmentForm extends JFrame {
    private JComboBox<Patient> patientDropdown;
    private JComboBox<String> treatmentDropdown;
    private JComboBox<String> staffDropdown;
    private JDateChooser dateChooser;
    private JComboBox<String> timeDropdown;
    private JButton submitBtn;
    private JTextField costField;
    private SecretaryController controller;
    private final Map<String, Integer> staffMap = new HashMap<>();

    public BookAppointmentForm() {
        controller = new SecretaryController();

        setTitle("Book Appointment");
        setSize(500, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setContentPane(new utils.GradientPanel());
        setLayout(new GridBagLayout());

        // Initialize components
        patientDropdown = new JComboBox<>();
        treatmentDropdown = new JComboBox<>();
        staffDropdown = new JComboBox<>();
        dateChooser = new JDateChooser();
        timeDropdown = new JComboBox<>();
        costField = new JTextField();
        submitBtn = UIFactory.createButton("Book Appointment");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // Row 1: Patient
        gbc.gridx = 0; gbc.gridy = row;
        add(UIFactory.createLabel("Select Patient:"), gbc);
        gbc.gridx = 1;
        add(patientDropdown, gbc);

        // Row 2: Treatment
        gbc.gridx = 0; gbc.gridy = ++row;
        add(UIFactory.createLabel("Treatment:"), gbc);
        gbc.gridx = 1;
        add(treatmentDropdown, gbc);

        // Row 3: Staff
        gbc.gridx = 0; gbc.gridy = ++row;
        add(UIFactory.createLabel("Assign Staff:"), gbc);
        gbc.gridx = 1;
        add(staffDropdown, gbc);

        // Row 4: Appointment Date
        gbc.gridx = 0; gbc.gridy = ++row;
        add(UIFactory.createLabel("Appointment Date:"), gbc);
        gbc.gridx = 1;
        add(dateChooser, gbc);

        // Row 5: Time
        gbc.gridx = 0; gbc.gridy = ++row;
        add(UIFactory.createLabel("Time:"), gbc);
        gbc.gridx = 1;
        add(timeDropdown, gbc);

        // Row 6: Cost
        gbc.gridx = 0; gbc.gridy = ++row;
        add(UIFactory.createLabel("Cost:"), gbc);
        gbc.gridx = 1;
        add(costField, gbc);

        // Row 7: Submit Button
        gbc.gridx = 0; gbc.gridy = ++row;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(submitBtn, gbc);

        loadDropdowns();

        dateChooser.addPropertyChangeListener(e -> {
            if ("date".equals(e.getPropertyName())) {
                Date selectedDate = dateChooser.getDate();
                if (selectedDate != null) {
                    LocalDate localDate = selectedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    loadAvailableTimes(localDate);
                }
            }
        });

        submitBtn.addActionListener(this::bookAppointment);

        setVisible(true);
    }

    private void loadDropdowns() {
        // --- Load Patients ---
        List<Patient> allPatients = controller.getAllPatients();
        Map<String, List<Integer>> nameToIds = new HashMap<>();

        for (Patient p : allPatients) {
            String name = p.getName();
            nameToIds.putIfAbsent(name, new ArrayList<>());
            nameToIds.get(name).add(p.getId());
        }

        for (Patient p : allPatients) {
            String name = p.getName();
            String displayText;

            if (nameToIds.get(name).size() > 1) {
                displayText = name + " (ID: " + p.getId() + ")";
            } else {
                displayText = name;
            }

            // Add minimal Patient object for dropdown with display name and ID
            patientDropdown.addItem(new Patient(p.getId(), displayText));
        }

        // --- Load Treatments ---
        for (String treatment : controller.getAllTreatmentNames()) {
            treatmentDropdown.addItem(treatment);
        }

        // --- Load Staff Members ---
        List<StaffMember> staffList = controller.getAllStaffMembers();
        Map<String, List<Integer>> nameToStaffIds = new HashMap<>();

        for (StaffMember sm : staffList) {
            String fullName = sm.getFirstName() + " " + sm.getLastName();
            nameToStaffIds.putIfAbsent(fullName, new ArrayList<>());
            nameToStaffIds.get(fullName).add(sm.getId());
        }

        for (StaffMember sm : staffList) {
            String fullName = sm.getFirstName() + " " + sm.getLastName();
            String displayText;

            if (nameToStaffIds.get(fullName).size() > 1) {
                displayText = fullName + " (" + sm.getRole() + ", ID: " + sm.getId() + ")";
            } else {
                displayText = fullName + " (" + sm.getRole() + ")";
            }

            staffDropdown.addItem(displayText);
            staffMap.put(displayText, sm.getId());
        }
    }



    private void loadAvailableTimes(LocalDate date) {
        timeDropdown.removeAllItems();
        ArrayList<LocalTime> available = controller.getAvailableTimeSlots(date, false, 30);
        for (LocalTime t : available) {
            timeDropdown.addItem(t.toString());
        }
    }

    private void bookAppointment(ActionEvent e) {
        Patient selectedPatient = (Patient) patientDropdown.getSelectedItem();
        String treatment = (String) treatmentDropdown.getSelectedItem();
        String staffDisplay = (String) staffDropdown.getSelectedItem();
        Date date = dateChooser.getDate();
        String timeStr = (String) timeDropdown.getSelectedItem();
        String costText = costField.getText().trim();

        if (selectedPatient == null || treatment == null || staffDisplay == null || date == null || timeStr == null) {
            JOptionPane.showMessageDialog(this, "Please complete all fields.");
            return;
        }

        double cost;
        try {
            cost = Double.parseDouble(costText);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid cost value.");
            return;
        }
        
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalTime localTime = LocalTime.parse(timeStr);

        int staffId = staffMap.getOrDefault(staffDisplay, -1);
        if (staffId == -1) {
            JOptionPane.showMessageDialog(this, "Unable to determine selected staff member.");
            return;
        }

        boolean success = controller.bookAppointment(
           String.valueOf(selectedPatient.getId()) , treatment,String.valueOf(staffId), localDate, localTime,cost
        );

        if (success) {
            JOptionPane.showMessageDialog(this, "Appointment booked.");
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Booking failed.");
        }
    }
}
