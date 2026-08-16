package boundary;

import com.toedter.calendar.JDateChooser;
import control.SecretaryController;
import utils.DesignUtils;
import utils.UIFactory;
import service.DomainValidator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Date;
import java.util.List;

public class AddPatientForm extends JFrame {

    private final SecretaryController controller;
    private JTextField idField, firstNameField, lastNameField, phoneField, emailField, identifierField, policyField;
    private JComboBox<String> insuranceProviderDropdown;
    private JDateChooser dobChooser;

    public AddPatientForm() {
        controller = new SecretaryController();

        setTitle("Add New Patient");
        setSize(450, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setContentPane(new utils.GradientPanel());
        setLayout(new GridLayout(10, 2, 10, 10));

        idField = new JTextField();
        firstNameField = new JTextField();
        lastNameField = new JTextField();
        phoneField = new JTextField();
        emailField = new JTextField();
        identifierField = new JTextField();
        dobChooser = new JDateChooser();
        policyField = new JTextField();
        insuranceProviderDropdown = new JComboBox<>();

        loadInsuranceProviders();

        add(UIFactory.createLabel("ID (max 9 digits):")); add(idField);
        add(UIFactory.createLabel("First Name:")); add(firstNameField);
        add(UIFactory.createLabel("Last Name:")); add(lastNameField);
        add(UIFactory.createLabel("Phone:")); add(phoneField);
        add(UIFactory.createLabel("Email:")); add(emailField);
        add(UIFactory.createLabel("Date of Birth:")); add(dobChooser);
        add(UIFactory.createLabel("Identifier (5 chars):")); add(identifierField);
        add(UIFactory.createLabel("Insurance Provider:")); add(insuranceProviderDropdown);
        add(UIFactory.createLabel("Policy Number:")); add(policyField);

        JButton submit = UIFactory.createButton("Add Patient");
        submit.addActionListener(this::handleSubmit);

        add(new JLabel());
        add(submit);

        setVisible(true);
    }

    private void loadInsuranceProviders() {
        List<String> providers = controller.getAllInsuranceProviders();
        for (String provider : providers) {
            insuranceProviderDropdown.addItem(provider);
        }
    }

    private void handleSubmit(ActionEvent e) {
        String id = idField.getText().trim();
        String first = firstNameField.getText().trim();
        String last = lastNameField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();
        String identifier = identifierField.getText().trim();
        String insurance = (String) insuranceProviderDropdown.getSelectedItem();
        String policy = policyField.getText().trim();
        Date dob = dobChooser.getDate();

        java.sql.Date sqlDob = dob == null ? null : new java.sql.Date(dob.getTime());
        String validationError = DomainValidator.validatePatient(id, first, last, phone, email,
            sqlDob, identifier, insurance, policy);
        if (validationError != null) {
            JOptionPane.showMessageDialog(this, validationError);
            return;
        }

        if (controller.patientIdExists(id)) {
            JOptionPane.showMessageDialog(this, "A patient with this ID already exists.");
            return;
        }

        if (controller.identifierExists(identifier)) {
            JOptionPane.showMessageDialog(this, "This identifier is already used.");
            return;
        }

        if (controller.policyNumberExists(policy)) {
            JOptionPane.showMessageDialog(this, "This policy number already exists.");
            return;
        }

        boolean success = controller.addNewPatient(id, first, last, phone, email,
                sqlDob, identifier, insurance, policy);

        if (success) {
            JOptionPane.showMessageDialog(this, "Patient added successfully.");
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to add patient.");
        }
    }
}
