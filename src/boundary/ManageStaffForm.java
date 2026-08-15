package boundary;

import control.ManagerController;
import utils.DesignUtils;
import utils.UIFactory;

import javax.swing.*;
import javax.swing.table.*;
import com.toedter.calendar.JDateChooser;
import java.awt.*;
import java.awt.event.*;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.regex.Pattern;

public class ManageStaffForm extends JPanel {
    private final ManagerController managerController = new ManagerController();
    private final DefaultTableModel model;
    private final JTable table;
    private final TableRowSorter<TableModel> sorter;

    public ManageStaffForm() {
        setLayout(new BorderLayout());
        setOpaque(false);

        JLabel title = UIFactory.createLabel("Manage Staff");
        title.setFont(DesignUtils.TITLE_FONT);
        title.setHorizontalAlignment(SwingConstants.CENTER);

        // Table model
        model = new DefaultTableModel(new Object[]{
                "Staff ID", "First Name", "Last Name", "Phone", "Email",
                "Date of Birth", "Qualifications", "Specialization", "Role"
        }, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.setRowHeight(25);
        table.setFont(DesignUtils.LABEL_FONT);
        table.getTableHeader().setFont(DesignUtils.BUTTON_FONT);

        sorter = new TableRowSorter<>(model);
        sorter.setComparator(0, Comparator.comparing(Object::toString));
        table.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(table);

        // Search Panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setOpaque(false);
        JComboBox<String> searchCriteria = new JComboBox<>(new String[]{"First Name", "Role"});
        JTextField searchField = new JTextField(20);
        JButton searchBtn = UIFactory.createButton("Search");

        searchBtn.addActionListener(e -> {
            String text = searchField.getText().trim();
            int colIndex = searchCriteria.getSelectedIndex() == 0 ? 1 : 8;

            if (text.isEmpty()) {
                sorter.setRowFilter(null);
            } else {
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(text), colIndex));
            }
        });

        searchPanel.add(new JLabel("Search by:"));
        searchPanel.add(searchCriteria);
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setOpaque(false);
        JButton addBtn = UIFactory.createButton("Add Staff");
        JButton editBtn = UIFactory.createButton("Edit Staff");
        JButton deleteBtn = UIFactory.createButton("Delete Staff");

        addBtn.addActionListener(e -> showStaffDialog(false, null));
        editBtn.addActionListener(e -> {
            int selected = table.getSelectedRow();
            if (selected >= 0) {
                Object[] data = new Object[model.getColumnCount()];
                for (int i = 0; i < model.getColumnCount(); i++) {
                    data[i] = model.getValueAt(table.convertRowIndexToModel(selected), i);
                }
                showStaffDialog(true, data);
            } else {
                JOptionPane.showMessageDialog(this, "Please select a staff member to edit.");
            }
        });
        deleteBtn.addActionListener(e -> deleteSelectedStaff());

        buttonPanel.add(addBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(deleteBtn);

        // Top layout
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.add(title, BorderLayout.NORTH);
        topPanel.add(searchPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        loadStaffMembers();
    }

    private void loadStaffMembers() {
        model.setRowCount(0);
        ArrayList<Object[]> staff = managerController.getAllStaff();
        for (Object[] row : staff) {
            model.addRow(row);
        }
    }

    private void showStaffDialog(boolean isEdit, Object[] existingData) {
        JTextField idField = new JTextField(15);
        JTextField firstNameField = new JTextField(15);
        JTextField lastNameField = new JTextField(15);
        JTextField phoneField = new JTextField(15);
        JTextField emailField = new JTextField(15);
        JDateChooser dobChooser = new JDateChooser();
        JTextField qualificationsField = new JTextField(15);
        JTextField specializationField = new JTextField(15);
        JComboBox<String> roleCombo = new JComboBox<>(new String[]{"Dentist", "Specialist", "Secretary", "Manager"});

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.add(new JLabel("Staff ID:"));
        panel.add(idField);
        panel.add(new JLabel("First Name:"));
        panel.add(firstNameField);
        panel.add(new JLabel("Last Name:"));
        panel.add(lastNameField);
        panel.add(new JLabel("Phone:"));
        panel.add(phoneField);
        panel.add(new JLabel("Email:"));
        panel.add(emailField);
        panel.add(new JLabel("Date of Birth:"));
        panel.add(dobChooser);
        panel.add(new JLabel("Qualifications:"));
        panel.add(qualificationsField);
        panel.add(new JLabel("Specialization:"));
        panel.add(specializationField);
        panel.add(new JLabel("Role:"));
        panel.add(roleCombo);

        if (isEdit && existingData != null) {
            idField.setText(existingData[0].toString());
            idField.setEditable(false);
            firstNameField.setText(existingData[1].toString());
            lastNameField.setText(existingData[2].toString());
            phoneField.setText(existingData[3].toString());
            emailField.setText(existingData[4].toString());
            try {
                dobChooser.setDate(Date.valueOf(existingData[5].toString()));
            } catch (Exception ignored) {}
            qualificationsField.setText(existingData[6].toString());
            specializationField.setText(existingData[7].toString());
            roleCombo.setSelectedItem(existingData[8].toString());
        }

        int result = JOptionPane.showConfirmDialog(this, panel, (isEdit ? "Edit Staff" : "Add Staff"),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String id = idField.getText().trim();
            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            String phone = phoneField.getText().trim();
            String email = emailField.getText().trim();
            java.util.Date selectedDate = dobChooser.getDate();
            String qualifications = qualificationsField.getText().trim();
            String specialization = specializationField.getText().trim();
            String role = roleCombo.getSelectedItem().toString();

            if (id.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || selectedDate == null) {
                JOptionPane.showMessageDialog(this, "Please fill in all required fields.");
                return;
            }

            if (!isEdit && managerController.personIdExists(id)) {
                JOptionPane.showMessageDialog(this, "This ID already exists.");
                return;
            }

            Date dob = new Date(selectedDate.getTime());
            boolean success = isEdit
                    ? managerController.editStaffMember(id, firstName, lastName, phone, email, dob, qualifications, specialization, role)
                    : managerController.addStaffMember(id, firstName, lastName, phone, email, dob, qualifications, specialization, role);

            if (success) {
                loadStaffMembers();
                JOptionPane.showMessageDialog(this, (isEdit ? "Staff updated." : "Staff added."));
            } else {
                JOptionPane.showMessageDialog(this, "Operation failed.");
            }
        }
    }

    private void deleteSelectedStaff() {
        int selected = table.getSelectedRow();
        if (selected >= 0) {
            int modelRow = table.convertRowIndexToModel(selected);
            String staffId = model.getValueAt(modelRow, 0).toString();
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete Staff ID: " + staffId + "?",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                boolean success = managerController.deleteStaffMember(staffId);
                if (success) {
                    loadStaffMembers();
                    JOptionPane.showMessageDialog(this, "Staff deleted.");
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete staff.");
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a staff member to delete.");
        }
    }
}
