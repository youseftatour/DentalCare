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
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        GradientPanel mainPanel = new GradientPanel();
        mainPanel.setLayout(new BorderLayout());
        setContentPane(mainPanel);

        JLabel title = UIFactory.createLabel("Welcome, Secretary " + user.getUsername());
        title.setFont(DesignUtils.TITLE_FONT);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(title, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(30, 200, 30, 200));
        JButton addPatientBtn = UIFactory.createButton("Add Patient");

        JButton bookBtn = UIFactory.createButton("Book Appointment");
        JButton inventoryBtn = UIFactory.createButton("Track Inventory");
        JButton btnManageAppointments = UIFactory.createButton("Manage Appointments");

        bookBtn.addActionListener(e -> openBookAppointmentForm());
        inventoryBtn.addActionListener(e -> openInventoryViewer());
        btnManageAppointments.addActionListener(e -> openAppointmentManagementForm());
        addPatientBtn.addActionListener(e -> openAddPatientForm());

        centerPanel.add(Box.createVerticalStrut(20));
        centerPanel.add(addPatientBtn);
        centerPanel.add(Box.createVerticalStrut(20));
        centerPanel.add(bookBtn);
        centerPanel.add(Box.createVerticalStrut(20));
        centerPanel.add(inventoryBtn);
        centerPanel.add(Box.createVerticalStrut(20));
        centerPanel.add(btnManageAppointments);


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
