package boundary;

import control.AuthController;
import entity.User;
import utils.GradientPanel;
import utils.UIFactory;

import javax.swing.*;
import java.awt.*;

public class PatientLoginFrame extends JFrame {

    private JTextField identifierField;

    public PatientLoginFrame() {
        setTitle("Patient Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);

        GradientPanel panel = new GradientPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(40, 80, 40, 80));
        setContentPane(panel);

        JLabel title = UIFactory.createLabel("Enter Identifier:");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        identifierField = UIFactory.createTextField();

        JButton loginBtn = UIFactory.createButton("Login");
        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginBtn.addActionListener(e -> attemptLogin());

        panel.add(title);
        panel.add(Box.createVerticalStrut(20));
        panel.add(identifierField);
        panel.add(Box.createVerticalStrut(20));
        panel.add(loginBtn);
    }

    private void attemptLogin() {
        String identifier = identifierField.getText().trim();

        User user = AuthController.getInstance().authenticatePatient(identifier);
        if (user != null) {
            dispose();
            new PatientDashboard(user).setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Invalid identifier.", "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}
