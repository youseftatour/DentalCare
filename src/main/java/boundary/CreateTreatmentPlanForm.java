package boundary;

import com.toedter.calendar.JDateChooser;
import control.DentistController;
import entity.Patient;
import utils.DatabaseManager;
import utils.DesignUtils;
import utils.UIFactory;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import service.DomainValidator;

public class CreateTreatmentPlanForm extends JFrame {
    private JComboBox<Patient> patientCombo;
    private JDateChooser startDateChooser, completionDateChooser;
    private JButton createBtn;
    private String dentistId;
    private DentistController controller;

    public CreateTreatmentPlanForm(String dentistId) {
        this.dentistId = dentistId;
        this.controller = new DentistController();

        setTitle("Create Treatment Plan");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(400, 350);
        setLocationRelativeTo(null);
        setContentPane(new utils.GradientPanel());
        setLayout(new GridLayout(5, 2, 10, 10));

        JLabel patientLabel = UIFactory.createLabel("Select Patient:");
        patientCombo = new JComboBox<>();
        ArrayList<Patient> patients = controller.getAllPatients();
        for (Patient p : patients) patientCombo.addItem(p);

        JLabel startLabel = UIFactory.createLabel("Start Date:");
        startDateChooser = new JDateChooser();
        startDateChooser.setDateFormatString("yyyy-MM-dd");

        JLabel completeLabel = UIFactory.createLabel("Estimated Completion:");
        completionDateChooser = new JDateChooser();
        completionDateChooser.setDateFormatString("yyyy-MM-dd");

        createBtn = UIFactory.createButton("Create Plan");
        createBtn.addActionListener(e -> createPlan());

        add(patientLabel); add(patientCombo);
        add(startLabel); add(startDateChooser);
        add(completeLabel); add(completionDateChooser);
        add(new JLabel()); add(createBtn);

        setVisible(true);
    }

    private void createPlan() {
        Patient selected = (Patient) patientCombo.getSelectedItem();
        Date utilStart = startDateChooser.getDate();
        Date utilEnd = completionDateChooser.getDate();

        if (selected == null || utilStart == null || utilEnd == null) {
            JOptionPane.showMessageDialog(this, "Please fill all fields.");
            return;
        }

        // Convert java.util.Date to java.sql.Date
        java.sql.Date sqlStart = new java.sql.Date(utilStart.getTime());
        java.sql.Date sqlEnd = new java.sql.Date(utilEnd.getTime());

        if (!DomainValidator.isValidTreatmentPlan(sqlStart.toLocalDate(), sqlEnd.toLocalDate())) {
            JOptionPane.showMessageDialog(this,
                "Estimated completion date cannot be before the start date.");
            return;
        }

        boolean success = controller.createTreatmentPlan(
                selected.getId(),
                sqlStart,
                sqlEnd,
                dentistId
        );

        if (success) {
            JOptionPane.showMessageDialog(this, "Treatment Plan Created.");
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to create plan.");
        }
    }

}
