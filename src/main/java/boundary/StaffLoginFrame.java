package boundary;

import control.AuthController;
import entity.User;
import utils.GradientPanel;
import utils.UIFactory;

import javax.swing.*;
import java.awt.*;

public class StaffLoginFrame extends JFrame {

    private JTextField idField;
    private JPasswordField passwordField;

    public StaffLoginFrame() {
        setTitle("Staff Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);

        GradientPanel panel = new GradientPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(40, 80, 40, 80));
        setContentPane(panel);

        JLabel title = UIFactory.createLabel("Enter Staff ID:");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        idField = UIFactory.createTextField();
        passwordField = new JPasswordField();

        JButton loginBtn = UIFactory.createButton("Login");
        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginBtn.addActionListener(e -> attemptLogin());

        panel.add(title);
        panel.add(Box.createVerticalStrut(20));
        panel.add(idField);
        panel.add(Box.createVerticalStrut(10));
        panel.add(UIFactory.createLabel("Password:"));
        panel.add(passwordField);
        panel.add(Box.createVerticalStrut(20));
        panel.add(loginBtn);
    }

    private void attemptLogin() {
        String staffId = idField.getText().trim();

        char[] password = passwordField.getPassword();
        User user;
        try {
            user = AuthController.getInstance().authenticateStaff(staffId, password);
        } finally {
            java.util.Arrays.fill(password, '\0');
        }
        if (user != null) {
            dispose();
            switch (user.getRole()) {
                case "Dentist" -> new DentistDashboard(String.valueOf(user.getLinkedID())).setVisible(true);
                case "Secretary" -> new SecretaryDashboard(user).setVisible(true);
                case "Manager" -> new ManagerDashboard(String.valueOf(user.getLinkedID())).setVisible(true);
                default -> JOptionPane.showMessageDialog(this, "Unknown role.");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Invalid login credentials.", "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}
