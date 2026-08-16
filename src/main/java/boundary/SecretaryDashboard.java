package boundary;

import entity.User;
import utils.GradientPanel;
import utils.UIFactory;
import utils.DesignUtils;

import javax.swing.*;
import java.awt.*;

public class SecretaryDashboard extends JFrame {

    private final User user;

    public SecretaryDashboard(User user) {
        this.user = user;
        setTitle("Secretary Dashboard");
        setSize(920, 650);
        setMinimumSize(new Dimension(780, 580));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        GradientPanel mainPanel = new GradientPanel();
        mainPanel.setLayout(new BorderLayout());
        setContentPane(mainPanel);

        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        heading.setBorder(BorderFactory.createEmptyBorder(32, 44, 12, 44));
        JLabel title = UIFactory.createLabel("Welcome, " + user.getUsername());
        title.setFont(DesignUtils.TITLE_FONT);
        JLabel subtitle = UIFactory.createLabel("Secretary workspace");
        subtitle.setForeground(new Color(215, 227, 233));
        heading.add(title);
        heading.add(Box.createVerticalStrut(6));
        heading.add(subtitle);
        mainPanel.add(heading, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(2, 2, 18, 18));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 44, 48, 44));
        JButton addPatientBtn = UIFactory.createButton("Add Patient");

        JButton bookBtn = UIFactory.createButton("Book Appointment");
        JButton inventoryBtn = UIFactory.createButton("Track Inventory");
        JButton btnManageAppointments = UIFactory.createButton("Manage Appointments");

        bookBtn.addActionListener(e -> openBookAppointmentForm());
        inventoryBtn.addActionListener(e -> openInventoryViewer());
        btnManageAppointments.addActionListener(e -> openAppointmentManagementForm());
        addPatientBtn.addActionListener(e -> openAddPatientForm());

        centerPanel.add(UIFactory.createActionCard("Patients",
            "Register a new patient and capture insurance details.", addPatientBtn));
        centerPanel.add(UIFactory.createActionCard("Appointments",
            "Book a duration-aware appointment with available staff.", bookBtn));
        centerPanel.add(UIFactory.createActionCard("Inventory",
            "Review stock levels and maintain clinic supplies.", inventoryBtn));
        centerPanel.add(UIFactory.createActionCard("Schedule management",
            "Update, reschedule, and manage existing appointments.", btnManageAppointments));


        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
    }
    private void openAddPatientForm() {
        new AddPatientForm();
    }


    private void openBookAppointmentForm() {
         new BookAppointmentForm().setVisible(true);
    }

 

    private void openInventoryViewer() {
         new InventoryTrackingForm().setVisible(true);
    }
    
    private void openAppointmentManagementForm() {
        new AppointmentManagementForm();
    }

    
    
}
