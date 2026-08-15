package boundary;

import control.ManagerController;
import entity.Patient;
import entity.TreatmentPlan;
import utils.DesignUtils;
import utils.UIFactory;

import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class ManageTreatmentPlansTab extends JPanel {
	private String managerId;
    private ManagerController controller;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> searchColumnCombo;
    private TableRowSorter<DefaultTableModel> sorter;

    public ManageTreatmentPlansTab(ManagerController controller,String managerId) {
    	this.managerId=managerId;
        this.controller = controller;
        setLayout(new BorderLayout());

        JLabel titleLabel = UIFactory.createTitleLabel("Manage Treatment Plans");
        add(titleLabel, BorderLayout.NORTH);

        // Table Setup
        String[] columnNames = {"Plan ID", "Start Date", "Estimated End Date", "Patient ID", "Status", "Created By"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            public boolean isCellEditable(int row, int column) {
                return column == 2 || column == 4; // Estimated End Date or Status
            }
        };
        table = UIFactory.createStripedTable(tableModel);
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        table.getModel().addTableModelListener(new TableModelListener() {
            public void tableChanged(TableModelEvent e) {
                if (e.getType() == TableModelEvent.UPDATE) {
                    int row = e.getFirstRow();
                    int planId = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
                    String status = (String) tableModel.getValueAt(row, 4);
                    String estimatedEnd = (String) tableModel.getValueAt(row, 2);
                    controller.updateTreatmentPlan(planId, status, estimatedEnd);
                    loadTreatmentPlans();
                }
            }
        });

        add(new JScrollPane(table), BorderLayout.CENTER);

        // Search Panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(new Color(44, 62, 80));
        searchColumnCombo = new JComboBox<>(columnNames);
        searchField = UIFactory.createSearchField();
        JButton searchBtn = new JButton("Search");
        searchBtn.addActionListener(e -> filterTable());
        searchPanel.add(new JLabel("Search by:"));
        searchPanel.add(searchColumnCombo);
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);
        add(searchPanel, BorderLayout.NORTH);

        // Button Panel
        JPanel buttonPanel = new JPanel();
        JButton addBtn = new JButton("Add Plan");
        JButton deleteBtn = new JButton("Delete Selected");

        addBtn.addActionListener(e -> openAddPlanDialog());
        deleteBtn.addActionListener(e -> deleteSelectedPlan());

        buttonPanel.add(addBtn);
        buttonPanel.add(deleteBtn);
        add(buttonPanel, BorderLayout.SOUTH);

        loadTreatmentPlans();
    }

    private void loadTreatmentPlans() {
        tableModel.setRowCount(0);
        ArrayList<TreatmentPlan> plans = controller.getAllTreatmentPlans();
        for (TreatmentPlan plan : plans) {
            tableModel.addRow(new Object[]{
                    plan.getTreatmentPlanId(),                                 
                    plan.getStartDate().toString(),
                    plan.getEstimatedCompletionDate().toString(),
                    plan.getPatientId(),                               
                    plan.getStatus(),
                    plan.getCreatedByDentist()
            });
        }
    }


    private void filterTable() {
        String keyword = searchField.getText();
        int col = searchColumnCombo.getSelectedIndex();
        if (keyword.trim().length() == 0) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + keyword, col));
        }
    }

    private void deleteSelectedPlan() {
        int row = table.getSelectedRow();
        if (row == -1) return;

        int modelRow = table.convertRowIndexToModel(row);
        int planId = Integer.parseInt(tableModel.getValueAt(modelRow, 0).toString());
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure?", "Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (controller.deleteTreatmentPlan(planId)) {
                loadTreatmentPlans();
            }
        }
    }

private void openAddPlanDialog() {
    // --- Patient dropdown ---
    JComboBox<String> patientDropdown = new JComboBox<>();
    Map<String, String> patientMap = new HashMap<>();

    List<Patient> patients = controller.getAllPatients(); // now returns List<Patient>
    Map<String, List<String>> nameToIds = new HashMap<>();

    for (Patient p : patients) {
        String id = String.valueOf(p.getId());
        String fullName = p.getName();  // already formatted as "FirstName LastName"

        nameToIds.putIfAbsent(fullName, new ArrayList<>());
        nameToIds.get(fullName).add(id);
    }

    for (Patient p : patients) {
        String id = String.valueOf(p.getId());
        String fullName = p.getName();
        String displayText = nameToIds.get(fullName).size() > 1
            ? fullName + " (ID: " + id + ")"
            : fullName;

        patientDropdown.addItem(displayText);
        patientMap.put(displayText, id);
    }

    // --- Date choosers ---
    JDateChooser startDateChooser = new JDateChooser();
    JDateChooser estimatedEndDateChooser = new JDateChooser();

    // --- Form panel ---
    JPanel panel = UIFactory.createFormPanel(
        new String[]{"Patient", "Start Date", "Estimated End Date"},
        new JComponent[]{patientDropdown, startDateChooser, estimatedEndDateChooser}
    );

    int result = JOptionPane.showConfirmDialog(this, panel, "Add Treatment Plan", JOptionPane.OK_CANCEL_OPTION);
    if (result == JOptionPane.OK_OPTION) {
        try {
            String selectedKey = (String) patientDropdown.getSelectedItem();
            String patientId = patientMap.get(selectedKey);

            LocalDate startDate = startDateChooser.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate estimatedEndDate = estimatedEndDateChooser.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            if (controller.addTreatmentPlan(patientId, startDate, estimatedEndDate, managerId)) {
                loadTreatmentPlans();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add treatment plan.");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid input. Please ensure all fields are filled correctly.");
        }
    }
}



}
