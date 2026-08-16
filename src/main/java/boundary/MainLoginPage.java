package boundary;

import utils.GradientPanel;
import utils.UIFactory;
import utils.DesignUtils;

import javax.swing.*;
import java.awt.*;

public class MainLoginPage extends JFrame {
    public MainLoginPage() {
        setTitle("DentalCare Login");
        setSize(620, 430);
        setMinimumSize(new Dimension(560, 400));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        GradientPanel panel = new GradientPanel();
        panel.setLayout(new BorderLayout(20, 28));
        panel.setBorder(BorderFactory.createEmptyBorder(42, 50, 48, 50));
        setContentPane(panel);

        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        JLabel title = UIFactory.createLabel("Welcome to DentalCare");
        title.setFont(DesignUtils.TITLE_FONT);
        JLabel subtitle = UIFactory.createLabel("Choose how you want to sign in");
        subtitle.setForeground(new Color(215, 227, 233));
        heading.add(title);
        heading.add(Box.createVerticalStrut(6));
        heading.add(subtitle);

        JButton patientBtn = UIFactory.createButton("Patient");
        JButton staffBtn = UIFactory.createButton("Staff");

        patientBtn.addActionListener(e -> {
            dispose();
            new PatientLoginFrame().setVisible(true);
        });

        staffBtn.addActionListener(e -> {
            dispose();
            new StaffLoginFrame().setVisible(true);
        });

        JPanel choices = new JPanel(new GridLayout(1, 2, 18, 0));
        choices.setOpaque(false);
        choices.add(UIFactory.createActionCard("Patient",
            "Review appointments, treatments, and manage your upcoming visits.", patientBtn));
        choices.add(UIFactory.createActionCard("Staff",
            "Open the dentist, secretary, or manager workspace.", staffBtn));

        panel.add(heading, BorderLayout.NORTH);
        panel.add(choices, BorderLayout.CENTER);
    }
}
