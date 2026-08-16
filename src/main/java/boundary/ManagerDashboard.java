package boundary;

import com.toedter.calendar.JDateChooser;
import control.ManagerController;
import entity.InventoryItem;
import entity.Supplier;
import net.sf.jasperreports.view.JasperViewer;
import net.sf.jasperreports.engine.JasperPrint;
import utils.DesignUtils;
import utils.GradientPanel;
import utils.UIFactory;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Pattern;
import java.util.concurrent.Callable;

public class ManagerDashboard extends JFrame {
    private ManagerController managerController;
    private DefaultTableModel inventoryModel;
    private JTable inventoryTable;

    public ManagerDashboard(String managerId) {
        this.managerController = new ManagerController();
        setTitle("Manager Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1120, 760);
        setMinimumSize(new Dimension(980, 650));
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();
        UIFactory.styleTabs(tabbedPane);

        GradientPanel treatmentPlanTab = new GradientPanel();
        treatmentPlanTab.setLayout(new BorderLayout());
        treatmentPlanTab.add(new ManageTreatmentPlansTab(managerController, managerId), BorderLayout.CENTER);
        tabbedPane.addTab("Treatment Plans", treatmentPlanTab);

        GradientPanel reportsTab = new GradientPanel();
        reportsTab.setLayout(new BorderLayout(24, 24));
        reportsTab.setBorder(BorderFactory.createEmptyBorder(42, 48, 48, 48));
        JButton revenueReportBtn = UIFactory.createButton("Revenue Report");
        JButton inventoryUsageReportBtn = UIFactory.createButton("Inventory Usage");
        JButton treatmentProgressBtn = UIFactory.createButton("Treatment Progress");

        JPanel reportHeading = new JPanel();
        reportHeading.setOpaque(false);
        reportHeading.setLayout(new BoxLayout(reportHeading, BoxLayout.Y_AXIS));
        JLabel reportTitle = UIFactory.createLabel("Reports");
        reportTitle.setFont(DesignUtils.TITLE_FONT);
        JLabel reportSubtitle = UIFactory.createLabel(
            "Generate clinic insights using the latest database records");
        reportSubtitle.setForeground(new Color(215, 227, 233));
        reportHeading.add(reportTitle);
        reportHeading.add(Box.createVerticalStrut(6));
        reportHeading.add(reportSubtitle);

        JPanel reportCards = new JPanel(new GridLayout(1, 3, 18, 0));
        reportCards.setOpaque(false);
        reportCards.add(createReportCard("Monthly revenue",
            "Summarize paid appointment revenue by month and year.", revenueReportBtn));
        reportCards.add(createReportCard("Inventory usage",
            "Review inventory activity for a selected date range.", inventoryUsageReportBtn));
        reportCards.add(createReportCard("Treatment progress",
            "View active treatment-plan progress by clinician.", treatmentProgressBtn));

        reportsTab.add(reportHeading, BorderLayout.NORTH);
        reportsTab.add(reportCards, BorderLayout.CENTER);

        revenueReportBtn.addActionListener(e -> showRevenueReportDialog());
        treatmentProgressBtn.addActionListener(e -> {
            runReport(() -> managerController.generateTreatmentProgressReport(managerId));
        });
        inventoryUsageReportBtn.addActionListener(e -> showInventoryUsageDialog());

        tabbedPane.addTab("Reports", reportsTab);

        GradientPanel manageStaffTab = new GradientPanel();
        manageStaffTab.setLayout(new BorderLayout());
        manageStaffTab.add(new ManageStaffForm(), BorderLayout.CENTER);
        tabbedPane.addTab("Manage Staff", manageStaffTab);

        GradientPanel inventoryTab = new GradientPanel();
        inventoryTab.setLayout(new BorderLayout());

        JLabel inventoryTitle = UIFactory.createLabel("Inventory Tracking");
        inventoryTitle.setFont(DesignUtils.TITLE_FONT);
        inventoryTitle.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setOpaque(false);
        searchPanel.setBorder(BorderFactory.createEmptyBorder(4, 0, 10, 0));

        JComboBox<String> searchCriteria = new JComboBox<>(new String[]{"Item Name", "Serial Number"});
        JTextField searchField = new JTextField(20);
        JButton searchBtn = UIFactory.createButton("Search");

        JLabel searchLabel = UIFactory.createLabel("Search by");
        searchPanel.add(searchLabel);
        searchPanel.add(searchCriteria);
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);

        inventoryModel = new DefaultTableModel(new Object[]{
                "Item ID", "Item Name", "Description", "Quantity",
                "Supplier Info", "Expiration Date", "Serial Number", "Low Stock Threshold"
        }, 0) {
            public boolean isCellEditable(int row, int col) {
                return col == 3 || col == 7;
            }
        };

        inventoryTable = new JTable(inventoryModel) {
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component comp = super.prepareRenderer(renderer, row, column);
                int modelRow = convertRowIndexToModel(row);
                try {
                    int quantity = Integer.parseInt(inventoryModel.getValueAt(modelRow, 3).toString());
                    int threshold = Integer.parseInt(inventoryModel.getValueAt(modelRow, 7).toString());
                    if (isRowSelected(row)) {
                        comp.setBackground(new Color(205, 230, 246));
                    } else if (quantity <= threshold) {
                        comp.setBackground(new Color(255, 225, 226));
                    } else {
                        comp.setBackground(row % 2 == 0 ? Color.WHITE : new Color(246, 249, 250));
                    }
                } catch (NumberFormatException e) {
                    comp.setBackground(Color.WHITE);
                }
                return comp;
            }
        };
        UIFactory.styleTable(inventoryTable);

        inventoryModel.addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                int col = e.getColumn();
                int itemId = (int) inventoryModel.getValueAt(row, 0);
                try {
                    if (col == 3) {
                        String newQty = inventoryModel.getValueAt(row, 3).toString();
                        managerController.updateInventoryQuantity(itemId, newQty);
                    } else if (col == 7) {
                        int threshold = Integer.parseInt(inventoryModel.getValueAt(row, 7).toString());
                        managerController.updateLowStockThreshold(itemId, threshold);
                    }
                    loadInventoryItems();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Invalid input: " + ex.getMessage());
                }
            }
        });

        TableRowSorter<TableModel> sorter = new TableRowSorter<>(inventoryModel);
        sorter.setComparator(0, Comparator.comparingInt(s -> Integer.parseInt(s.toString())));
        sorter.setComparator(3, Comparator.comparingInt(s -> Integer.parseInt(s.toString())));
        inventoryTable.setRowSorter(sorter);

        searchBtn.addActionListener(e -> {
            String text = searchField.getText().trim();
            int col = searchCriteria.getSelectedIndex() == 0 ? 1 : 6;
            sorter.setRowFilter(text.isEmpty() ? null : RowFilter.regexFilter("(?i)" + Pattern.quote(text), col));
        });

        JScrollPane scrollPane = new JScrollPane(inventoryTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(DesignUtils.BORDER_COLOR));

        GradientPanel inventoryButtons = new GradientPanel();
        inventoryButtons.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton addBtn = UIFactory.createButton("Add Item");
        JButton deleteBtn = UIFactory.createButton("Delete Selected");
        JButton loadXmlBtn = UIFactory.createButton("Load XML");

        addBtn.addActionListener(e -> openAddItemDialog());
        deleteBtn.addActionListener(e -> deleteSelectedItem());
        loadXmlBtn.addActionListener(e -> loadInventoryFromXML());

        inventoryButtons.add(addBtn);
        inventoryButtons.add(deleteBtn);
        inventoryButtons.add(loadXmlBtn);

        JPanel inventoryHeader = new JPanel(new BorderLayout());
        inventoryHeader.setOpaque(false);
        inventoryHeader.setBorder(BorderFactory.createEmptyBorder(18, 18, 0, 18));
        inventoryHeader.add(inventoryTitle, BorderLayout.NORTH);
        inventoryHeader.add(searchPanel, BorderLayout.SOUTH);
        inventoryTab.add(inventoryHeader, BorderLayout.NORTH);
        inventoryTab.add(scrollPane, BorderLayout.CENTER);
        inventoryTab.add(inventoryButtons, BorderLayout.SOUTH);

        tabbedPane.addTab("Inventory", inventoryTab);

        add(tabbedPane);
        loadInventoryItems();
        setVisible(true);
    }

    private void loadInventoryItems() {
        inventoryModel.setRowCount(0);
        for (InventoryItem item : managerController.getAllInventoryItems()) {
            inventoryModel.addRow(new Object[]{
                    item.getItemId(), item.getItemName(), item.getDescription(),
                    item.getQuantity(), item.getSupplierInformation(),
                    item.getExpiryDate() != null ? item.getExpiryDate().toString() : "",
                    item.getSerialNumber(), item.getLowStockThreshold()
            });
        }
    }

    private void openAddItemDialog() {
        JTextField nameField = new JTextField(), descField = new JTextField();
        JTextField quantityField = new JTextField(), supplierField = new JTextField();
        JTextField expirationField = new JTextField(), serialField = new JTextField();
        JTextField thresholdField = new JTextField();

        JPanel panel = new JPanel(new GridLayout(0, 2));
        panel.add(new JLabel("Item Name:")); panel.add(nameField);
        panel.add(new JLabel("Description:")); panel.add(descField);
        panel.add(new JLabel("Quantity:")); panel.add(quantityField);
        panel.add(new JLabel("Supplier Info:")); panel.add(supplierField);
        panel.add(new JLabel("Expiration Date (yyyy-MM-dd):")); panel.add(expirationField);
        panel.add(new JLabel("Serial Number:")); panel.add(serialField);
        panel.add(new JLabel("Low Stock Threshold:")); panel.add(thresholdField);

        if (JOptionPane.showConfirmDialog(this, panel, "Add Item", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                InventoryItem item = new InventoryItem(0,
                        nameField.getText().trim(),
                        descField.getText().trim(),
                        Integer.parseInt(quantityField.getText().trim()),
                        supplierField.getText().trim(),
                        expirationField.getText().isEmpty() ? null : LocalDate.parse(expirationField.getText().trim()),
                        serialField.getText().trim(),
                        Integer.parseInt(thresholdField.getText().trim()));

                if (managerController.addInventoryItem(item)) {
                    loadInventoryItems();
                    JOptionPane.showMessageDialog(this, "Item added successfully.");
                } else JOptionPane.showMessageDialog(this, "Failed to add item.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid input: " + ex.getMessage());
            }
        }
    }

    private void deleteSelectedItem() {
        int selected = inventoryTable.getSelectedRow();
        if (selected >= 0) {
            int modelIndex = inventoryTable.convertRowIndexToModel(selected);
            int itemId = (int) inventoryModel.getValueAt(modelIndex, 0);
            if (JOptionPane.showConfirmDialog(this, "Delete item ID " + itemId + "?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                if (managerController.deleteInventoryItem(itemId)) {
                    loadInventoryItems();
                    JOptionPane.showMessageDialog(this, "Item deleted.");
                } else JOptionPane.showMessageDialog(this, "Delete failed.");
            }
        }
    }

    private void loadInventoryFromXML() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            Map<String, Supplier> supplierMap = managerController.parseSuppliersWithItems(file);
            if (supplierMap == null || supplierMap.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No suppliers found in XML.");
                return;
            }

            JDialog previewDialog = new JDialog(this, "Preview XML Data", true);
            previewDialog.setSize(900, 500);
            previewDialog.setLayout(new BorderLayout());
            previewDialog.setLocationRelativeTo(this);

            DefaultListModel<String> supplierListModel = new DefaultListModel<>();
            for (String supplierName : supplierMap.keySet()) {
                supplierListModel.addElement(supplierName);
            }
            JList<String> supplierList = new JList<>(supplierListModel);
            JScrollPane supplierScroll = new JScrollPane(supplierList);
            supplierScroll.setPreferredSize(new Dimension(250, 400));

            DefaultTableModel previewModel = new DefaultTableModel(new Object[]{
                    "Item Name", "Description", "Quantity", "Supplier Info", "Expiry Date", "Serial", "Low Threshold"
            }, 0);
            JTable itemTable = new JTable(previewModel);
            JScrollPane tableScroll = new JScrollPane(itemTable);

            JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, supplierScroll, tableScroll);
            split.setResizeWeight(0.3);

            supplierList.addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) {
                    String selectedSupplier = supplierList.getSelectedValue();
                    previewModel.setRowCount(0);
                    Supplier supplier = supplierMap.get(selectedSupplier);
                    if (supplier != null) {
                        for (InventoryItem item : supplier.getItems()) {
                            previewModel.addRow(new Object[]{
                                    item.getItemName(), item.getDescription(), item.getQuantity(),
                                    supplier.getName(),
                                    item.getExpiryDate() != null ? item.getExpiryDate().toString() : "",
                                    item.getSerialNumber(), item.getLowStockThreshold()
                            });
                        }
                    }
                }
            });

            JButton confirmBtn = new JButton("Import");
            confirmBtn.addActionListener(e -> {
                ManagerController.InventoryImportResult result =
                    managerController.importInventoryDetailed(file);
                if (result.success()) {
                    loadInventoryItems();
                    JOptionPane.showMessageDialog(this,
                        "Imported: " + result.importedCount() +
                        "\nSkipped: " + result.skippedCount());
                    previewDialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Import failed. No database changes were committed.\n" +
                        String.join("\n", result.errors()));
                }
            });

            GradientPanel bottom = new GradientPanel();
            bottom.add(confirmBtn);

            previewDialog.add(split, BorderLayout.CENTER);
            previewDialog.add(bottom, BorderLayout.SOUTH);
            previewDialog.setVisible(true);
        }
    }

    private void showRevenueReportDialog() {
        JTextField monthField = new JTextField(), yearField = new JTextField();
        JPanel panel = new JPanel(new GridLayout(0, 2));
        panel.add(new JLabel("Month (mm):")); panel.add(monthField);
        panel.add(new JLabel("Year (yyyy):")); panel.add(yearField);

        if (JOptionPane.showConfirmDialog(this, panel, "Select Period", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            runReport(() -> managerController.generateRevenueReport(
                monthField.getText().trim(), yearField.getText().trim()));
        }
    }

    private void showInventoryUsageDialog() {
        JDialog dialog = new JDialog(this, "Generate Inventory Usage Report", true);
        dialog.setSize(400, 200);
        dialog.setLayout(new GridBagLayout());
        dialog.setLocationRelativeTo(null);

        JLabel startLabel = new JLabel("Start Date:"), endLabel = new JLabel("End Date:");
        JDateChooser startChooser = new JDateChooser(), endChooser = new JDateChooser();
        JButton generateBtn = new JButton("Generate");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        gbc.gridx = 0; gbc.gridy = 0; dialog.add(startLabel, gbc);
        gbc.gridx = 1; dialog.add(startChooser, gbc);
        gbc.gridx = 0; gbc.gridy = 1; dialog.add(endLabel, gbc);
        gbc.gridx = 1; dialog.add(endChooser, gbc);
        gbc.gridx = 1; gbc.gridy = 2; dialog.add(generateBtn, gbc);

        generateBtn.addActionListener(e -> {
            Date start = startChooser.getDate();
            Date end = endChooser.getDate();
            if (start == null || end == null) {
                JOptionPane.showMessageDialog(dialog, "Please select both dates.");
                return;
            }
            dialog.dispose();
            runReport(() -> managerController.generateInventoryUsageReport(start, end));
        });

        dialog.setVisible(true);
    }

    private void showReportError() {
        JOptionPane.showMessageDialog(this,
            "The report could not be generated. Please try again.",
            "Report Error", JOptionPane.ERROR_MESSAGE);
    }

    private JPanel createReportCard(String title, String description, JButton action) {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(DesignUtils.SURFACE_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(DesignUtils.BORDER_COLOR),
            BorderFactory.createEmptyBorder(28, 24, 28, 24)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(DesignUtils.SUBTITLE_FONT);
        titleLabel.setForeground(DesignUtils.TEXT_COLOR);
        card.add(titleLabel, gbc);
        gbc.gridy = 1;
        gbc.weighty = 1;
        gbc.insets = new Insets(12, 0, 24, 0);
        JLabel descriptionLabel = new JLabel("<html><body style='width:190px'>" + description + "</body></html>");
        descriptionLabel.setFont(DesignUtils.LABEL_FONT);
        descriptionLabel.setForeground(DesignUtils.MUTED_TEXT_COLOR);
        card.add(descriptionLabel, gbc);
        gbc.gridy = 2;
        gbc.weighty = 0;
        gbc.insets = new Insets(0, 0, 0, 0);
        card.add(action, gbc);
        return card;
    }

    private void runReport(Callable<JasperPrint> reportTask) {
        new SwingWorker<JasperPrint, Void>() {
            protected JasperPrint doInBackground() throws Exception { return reportTask.call(); }
            protected void done() {
                try {
                    JasperViewer.viewReport(get(), false);
                } catch (Exception exception) {
                    utils.AppLogger.error(ManagerDashboard.class, "Report generation failed", exception);
                    showReportError();
                }
            }
        }.execute();
    }
}
