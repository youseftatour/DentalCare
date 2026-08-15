package boundary;

import control.PatientController;
import entity.Patient;
import entity.Treatment;
import entity.User;
import utils.GradientPanel;
import utils.UIFactory;
import utils.DesignUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class PatientDashboard extends JFrame {
    private final Patient patient;
    private JTable appointmentTable;
    private int[] appointmentIds;
    private final PatientController controller = new PatientController();

    public PatientDashboard(User user) {
        setTitle("Patient Dashboard");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        GradientPanel mainPanel = new GradientPanel();
        mainPanel.setLayout(new BorderLayout());
        setContentPane(mainPanel);

        this.patient = controller.getPatientByID(user.getLinkedID());

        JLabel title = UIFactory.createLabel("Welcome, " + patient.getName());
        title.setFont(DesignUtils.TITLE_FONT);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(title, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Profile", createProfileTab());
        tabs.addTab("Treatment Plan", createTreatmentTab());
        tabs.addTab("Appointments", createAppointmentsTab());

        mainPanel.add(tabs, BorderLayout.CENTER);
    }

    private JPanel createProfileTab() {
        JPanel backgroundPanel = new GradientPanel();
        backgroundPanel.setLayout(new GridBagLayout());

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(255, 255, 255, 30));
        card.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));
        card.setMaximumSize(new Dimension(400, 300));

        card.add(createLabeledLine("Full Name: ", patient.getName()));
        card.add(Box.createVerticalStrut(15));
        card.add(createLabeledLine("Email: ", patient.getEmail()));
        card.add(Box.createVerticalStrut(15));
        card.add(createLabeledLine("Phone: ", patient.getPhone()));
        card.add(Box.createVerticalStrut(15));
        card.add(createLabeledLine("Age: ", String.valueOf(patient.getAge())));
        card.add(Box.createVerticalStrut(15));
        card.add(createLabeledLine("Insurance: ", patient.getInsuranceProvider() + " - " + patient.getPolicyNumber()));

        backgroundPanel.add(card);
        return backgroundPanel;
    }

    private JPanel createLabeledLine(String label, String value) {
        JPanel line = new JPanel(new BorderLayout());
        line.setOpaque(false);

        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(new Font("Segoe UI", Font.BOLD, 16));
        labelComponent.setForeground(Color.WHITE);

        JLabel valueComponent = new JLabel(value);
        valueComponent.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        valueComponent.setForeground(Color.LIGHT_GRAY);

        line.add(labelComponent, BorderLayout.WEST);
        line.add(valueComponent, BorderLayout.CENTER);
        return line;
    }

    private JPanel createTreatmentTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        ArrayList<Treatment> treatments = controller.getActiveTreatmentsForPatient(patient.getId());

        String[] columnNames = {"Treatment", "Cost", "Status"};
        Object[][] data = new Object[treatments.size()][3];
        for (int i = 0; i < treatments.size(); i++) {
            Treatment t = treatments.get(i);
            data[i][0] = t.getName();
            data[i][1] = t.getCost();
            data[i][2] = t.getStatus();
        }

        JTable table = new JTable(data, columnNames);
        table.setEnabled(false);
        table.setRowHeight(25);
        table.setFont(DesignUtils.LABEL_FONT);
        table.getTableHeader().setFont(DesignUtils.BUTTON_FONT);

        JScrollPane scrollPane = new JScrollPane(table);
        JLabel title = UIFactory.createLabel("Active Treatment Plan");
        title.setFont(DesignUtils.SUBTITLE_FONT);
        title.setHorizontalAlignment(SwingConstants.CENTER);

        panel.add(title, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createAppointmentsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        JLabel title = UIFactory.createLabel("Upcoming Appointments");
        title.setFont(DesignUtils.SUBTITLE_FONT);
        title.setHorizontalAlignment(SwingConstants.CENTER);

        // Table and data
        String[] columnNames = {"Date", "Time", "Reason", "Treatment", "Status"};
        ArrayList<Object[]> appointments = controller.getUpcomingAppointmentsForPatientWithIDs(patient.getId());
        Object[][] data = new Object[appointments.size()][5];
        appointmentIds = new int[appointments.size()];
        for (int i = 0; i < appointments.size(); i++) {
            Object[] row = appointments.get(i);
            data[i] = new Object[] { row[0], row[1], row[2], row[3], row[4] };
            appointmentIds[i] = (int) row[5];
        }

        appointmentTable = new JTable(new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
        appointmentTable.setRowHeight(25);
        appointmentTable.setFont(DesignUtils.LABEL_FONT);
        appointmentTable.getTableHeader().setFont(DesignUtils.BUTTON_FONT);

        JScrollPane scrollPane = new JScrollPane(appointmentTable);

        // Buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        JButton rescheduleBtn = UIFactory.createButton("Reschedule");
        JButton suspendBtn = UIFactory.createButton("Suspend");
        JButton cancelBtn = UIFactory.createButton("Cancel");
        JButton bookBtn = UIFactory.createButton("Book Appointment");
        buttonPanel.add(bookBtn);
        buttonPanel.add(rescheduleBtn);
        buttonPanel.add(suspendBtn);
        buttonPanel.add(cancelBtn);

        // Action Listeners
        bookBtn.addActionListener(e -> {
            ArrayList<Treatment> allTreatments = controller.getAllTreatments();
            AppointmentBookingPopup popup = new AppointmentBookingPopup(
                this,
                patient.getId(),
                allTreatments,
                () -> refreshAppointmentsTable()
            );
            popup.setVisible(true);
        });

        rescheduleBtn.addActionListener(e -> {
            int row = appointmentTable.getSelectedRow();
            if (row >= 0) {
                String newDate = JOptionPane.showInputDialog(this, "Enter new date (yyyy-MM-dd):");
                String newTime = JOptionPane.showInputDialog(this, "Enter new time (HH:mm):");
                if (newDate != null && newTime != null) {
                    controller.rescheduleAppointment(appointmentIds[row], newDate, newTime);
                    refreshAppointmentsTable();
                }
            }
        });
        suspendBtn.addActionListener(e -> {
            int row = appointmentTable.getSelectedRow();
            if (row >= 0) {
                String date = (String) appointmentTable.getValueAt(row, 0);
                String time = (String) appointmentTable.getValueAt(row, 1);
                

                int confirm = JOptionPane.showConfirmDialog(this, "Do you want to suspend this appointment?", "Confirm Suspend", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    int id = appointmentIds[row];
                    if (controller.updateAppointmentStatus(id, "Suspended")) {
                    	appointmentTable.setValueAt("Suspended", row, 4);
                    }
                }
            }
        });

        cancelBtn.addActionListener(e -> {
            int row = appointmentTable.getSelectedRow();
            if (row >= 0) {
                controller.updateAppointmentStatus(appointmentIds[row], "Cancelled");
                refreshAppointmentsTable();
            }
        });

        panel.add(title, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        return panel;
    }

    public void refreshAppointmentsTable() {
        ((DefaultTableModel) appointmentTable.getModel()).setRowCount(0);
        ArrayList<Object[]> updatedAppointments = controller.getUpcomingAppointmentsForPatientWithIDs(patient.getId());
        appointmentIds = new int[updatedAppointments.size()];

        for (int i = 0; i < updatedAppointments.size(); i++) {
            Object[] row = updatedAppointments.get(i);
            ((DefaultTableModel) appointmentTable.getModel()).addRow(new Object[] { row[0], row[1], row[2], row[3], row[4] });
            appointmentIds[i] = (int) row[5];
        }
    }
}
