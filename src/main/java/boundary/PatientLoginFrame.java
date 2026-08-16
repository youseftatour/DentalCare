package boundary;

import control.AuthController;
import entity.User;
import utils.GradientPanel;
import utils.UIFactory;
import utils.DesignUtils;

import javax.swing.*;
import java.awt.*;

public class PatientLoginFrame extends JFrame {

    private JTextField identifierField;
    private JPasswordField passwordField;

    public PatientLoginFrame() {
        setTitle("Patient Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(480, 420);
        setMinimumSize(new Dimension(440, 390));
        setLocationRelativeTo(null);

        GradientPanel background = new GradientPanel();
        background.setLayout(new GridBagLayout());
        background.setBorder(BorderFactory.createEmptyBorder(28, 34, 28, 34));
        setContentPane(background);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createEmptyBorder(28, 38, 30, 38));
        JLabel title = new JLabel("Patient sign in");
        title.setFont(DesignUtils.TITLE_FONT);
        title.setForeground(DesignUtils.TEXT_COLOR);
        JLabel subtitle = new JLabel("Use your patient identifier and password");
        subtitle.setFont(DesignUtils.LABEL_FONT);
        subtitle.setForeground(DesignUtils.MUTED_TEXT_COLOR);
        JLabel identifierLabel = new JLabel("Patient identifier");
        JLabel passwordLabel = new JLabel("Password");
        identifierLabel.setFont(DesignUtils.BUTTON_FONT);
        passwordLabel.setFont(DesignUtils.BUTTON_FONT);
        identifierLabel.setForeground(DesignUtils.TEXT_COLOR);
        passwordLabel.setForeground(DesignUtils.TEXT_COLOR);
        identifierField = UIFactory.createTextField();
        passwordField = UIFactory.createPasswordField();

        JCheckBox showPassword = new JCheckBox("Show password");
        showPassword.setOpaque(false);
        showPassword.setForeground(DesignUtils.MUTED_TEXT_COLOR);
        showPassword.addActionListener(e -> passwordField.setEchoChar(
            showPassword.isSelected() ? '\0' : '\u2022'));

        JButton loginBtn = UIFactory.createButton("Login");
        loginBtn.addActionListener(e -> attemptLogin());
        getRootPane().setDefaultButton(loginBtn);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 6, 0);
        card.add(title, gbc);
        gbc.gridy = 1; gbc.insets = new Insets(0, 0, 24, 0); card.add(subtitle, gbc);
        gbc.gridy = 2; gbc.insets = new Insets(0, 0, 6, 0); card.add(identifierLabel, gbc);
        gbc.gridy = 3; gbc.insets = new Insets(0, 0, 16, 0); card.add(identifierField, gbc);
        gbc.gridy = 4; gbc.insets = new Insets(0, 0, 6, 0); card.add(passwordLabel, gbc);
        gbc.gridy = 5; gbc.insets = new Insets(0, 0, 4, 0); card.add(passwordField, gbc);
        gbc.gridy = 6; gbc.insets = new Insets(0, 0, 20, 0); card.add(showPassword, gbc);
        gbc.gridy = 7; gbc.insets = new Insets(0, 0, 0, 0); card.add(loginBtn, gbc);
        background.add(card);
    }

    private void attemptLogin() {
        String identifier = identifierField.getText().trim();

        char[] password = passwordField.getPassword();
        User user;
        try {
            user = AuthController.getInstance().authenticatePatient(identifier, password);
        } finally {
            java.util.Arrays.fill(password, '\0');
        }
        if (user != null) {
            dispose();
            new PatientDashboard(user).setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Invalid login credentials.", "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}
