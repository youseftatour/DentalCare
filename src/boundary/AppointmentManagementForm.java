package boundary;

import com.toedter.calendar.JCalendar;
import control.SecretaryController;
import entity.Appointment;
import utils.DesignUtils;
import utils.UIFactory;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.event.TableModelEvent;

public class AppointmentManagementForm extends JFrame {
    private SecretaryController controller;
    private JTable table;
    private DefaultTableModel model;
    private JButton rescheduleBtn;
    private JComboBox<String> filterBox;

    public AppointmentManagementForm() {
        controller = new SecretaryController();

        setTitle("Manage Appointments");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setContentPane(new utils.GradientPanel());
        setLayout(new BorderLayout(10, 10));

        JLabel title = UIFactory.createLabel("Appointment Management");
        title.setFont(DesignUtils.TITLE_FONT);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        add(title, BorderLayout.NORTH);

        model = new DefaultTableModel(new Object[]{
            "ID", "Patient", "Date", "Time", "Treatment", "Cost", "Status", "Assigned Staff", "Reason", "Reminder Sent", "Paid", "Sterilized"
        }, 0) {
            public boolean isCellEditable(int row, int column) {
                return column == 6 || column == 10; // Status, Paid
            }
        };

        table = new JTable(model);
        table.setFont(DesignUtils.LABEL_FONT);
        table.setRowHeight(25);
        table.getTableHeader().setFont(DesignUtils.BUTTON_FONT);

        // Set ComboBox editor for Status and Paid columns
        TableColumn statusCol = table.getColumnModel().getColumn(6);
        statusCol.setCellEditor(new DefaultCellEditor(new JComboBox<>(new String[]{"Active", "Cancelled", "Suspended", "Completed"})));

        TableColumn paidCol = table.getColumnModel().getColumn(10);
        paidCol.setCellEditor(new DefaultCellEditor(new JComboBox<>(new String[]{"Yes", "No"})));

        // Listen for cell edit commits
        table.getModel().addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                int column = e.getColumn();
                int appointmentId = Integer.parseInt(model.getValueAt(row, 0).toString());

                if (column == 6) {
                    String newStatus = model.getValueAt(row, column).toString();
                    controller.updateStatus(appointmentId, newStatus);
                } else if (column == 10) {
                    boolean isPaid = model.getValueAt(row, column).toString().equals("Yes");
                    controller.updatePaidStatus(appointmentId, isPaid);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);

        // Filter Dropdown
        filterBox = new JComboBox<>(new String[]{"Upcoming", "Past"});
        filterBox.addActionListener(e -> loadAppointments(filterBox.getSelectedItem().toString()));
        bottomPanel.add(filterBox, BorderLayout.WEST);

        // Reschedule Button
        rescheduleBtn = UIFactory.createButton("Reschedule");
        rescheduleBtn.addActionListener(this::rescheduleAction);
        bottomPanel.add(rescheduleBtn, BorderLayout.EAST);

        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        loadAppointments("Upcoming");
        setVisible(true);
    }

    private void loadAppointments(String filter) {
        model.setRowCount(0);
        ArrayList<Object[]> data = controller.getDetailedAppointments(filter);
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (Object[] row : data) {
            model.addRow(row);
        }
    }

    private void rescheduleAction(ActionEvent e) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an appointment to reschedule.");
            return;
        }

        int appointmentId = Integer.parseInt(model.getValueAt(selectedRow, 0).toString());

        JDialog dialog = new JDialog(this, "Select New Date", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        JCalendar calendar = new JCalendar();
        JButton confirm = new JButton("Reschedule");

        confirm.addActionListener(ae -> {
            Date selected = calendar.getDate();
            java.sql.Date sqlDate = new java.sql.Date(selected.getTime());

            if (controller.rescheduleDateOnly(appointmentId, sqlDate.toLocalDate())) {
                model.setValueAt(sqlDate.toLocalDate().toString(), selectedRow, 2);
                JOptionPane.showMessageDialog(this, "Date rescheduled.");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to reschedule.");
            }

            dialog.dispose();
        });

        dialog.setLayout(new BorderLayout());
        dialog.add(calendar, BorderLayout.CENTER);
        dialog.add(confirm, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
}
