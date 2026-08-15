package boundary;

import utils.GradientPanel;
import utils.UIFactory;

import javax.swing.*;
import java.awt.*;

public class MainLoginPage extends JFrame {
    public MainLoginPage() {
        setTitle("DentalCare Login");
        setSize(400, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        GradientPanel panel = new GradientPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(40, 80, 40, 80));
        setContentPane(panel);

        JLabel title = UIFactory.createLabel("Login as:");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton patientBtn = UIFactory.createButton("Patient");
        JButton staffBtn = UIFactory.createButton("Staff");

        patientBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        staffBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        patientBtn.addActionListener(e -> {
            dispose();
            new PatientLoginFrame().setVisible(true);
        });

        staffBtn.addActionListener(e -> {
            dispose();
            new StaffLoginFrame().setVisible(true);
        });

        panel.add(title);
        panel.add(Box.createVerticalStrut(30));
        panel.add(patientBtn);
        panel.add(Box.createVerticalStrut(20));
        panel.add(staffBtn);
    }
}
