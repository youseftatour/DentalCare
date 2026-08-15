package boundary;

import utils.DatabaseManager;
import utils.DesignUtils;
import utils.UIFactory;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.*;

import entity.InventoryItem;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.regex.Pattern;

public class InventoryTrackingForm extends JFrame {
	private final control.SecretaryController secretaryController = new control.SecretaryController();

    private JTable table;
    private DefaultTableModel model;
    private TableRowSorter<TableModel> sorter;

    public InventoryTrackingForm() {
        setTitle("Track Inventory");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setContentPane(new utils.GradientPanel());
        setLayout(new BorderLayout());

        JLabel title = UIFactory.createLabel("Inventory Tracking");
        title.setFont(DesignUtils.TITLE_FONT);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        add(title, BorderLayout.NORTH);

        model = new DefaultTableModel(new Object[]{
                "Item ID", "Item Name", "Description", "Quantity",
                "Supplier Info", "Expiration Date", "SerialNumber", "Low Stock Threshold"
        }, 0) {
            public boolean isCellEditable(int row, int column) {
                return column == 3 || column == 7; // Quantity and Threshold are editable
            }
        };
       


        table = new JTable(model) {
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component comp = super.prepareRenderer(renderer, row, column);
                int modelRow = convertRowIndexToModel(row);
                try {
                    int quantity = Integer.parseInt(model.getValueAt(modelRow, 3).toString());
                    int threshold = Integer.parseInt(model.getValueAt(modelRow, 7).toString());
                    if (quantity <= threshold) {
                        comp.setBackground(new Color(255, 102, 102)); // red for low stock
                    } else {
                        comp.setBackground(Color.WHITE);
                    }
                } catch (NumberFormatException e) {
                    comp.setBackground(Color.WHITE);
                }
                return comp;
            }
        };
        table.getModel().addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                int col = e.getColumn();
                int itemId = Integer.parseInt(model.getValueAt(row, 0).toString());

                try (Connection conn = DatabaseManager.getConnection()) {
                    if (col == 3) { // Quantity updated
                        int newQty = Integer.parseInt(model.getValueAt(row, 3).toString());
                        PreparedStatement stmt = conn.prepareStatement(
                            "UPDATE TblInventoryItems SET Quantity = ? WHERE ItemID = ?"
                        );
                        stmt.setInt(1, newQty);
                        stmt.setInt(2, itemId);
                        stmt.executeUpdate();
                    } else if (col == 7) { // Threshold updated
                        int newThreshold = Integer.parseInt(model.getValueAt(row, 7).toString());
                        PreparedStatement stmt = conn.prepareStatement(
                            "UPDATE TblInventoryItems SET LowStockAlertThreshold = ? WHERE ItemID = ?"
                        );
                        stmt.setInt(1, newThreshold);
                        stmt.setInt(2, itemId);
                        stmt.executeUpdate();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Failed to update database.");
                }

                loadInventoryItems(); // refresh to update row color
            }
        });
        table.setFont(DesignUtils.LABEL_FONT);
        table.setRowHeight(25);
        table.getTableHeader().setFont(DesignUtils.BUTTON_FONT);

        sorter = new TableRowSorter<>(model);
        sorter.setComparator(0, Comparator.comparingInt(s -> Integer.parseInt(s.toString())));
        sorter.setComparator(3, Comparator.comparingInt(s -> Integer.parseInt(s.toString())));
        table.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(0, 0)); // ensures full expansion

        // 🔍 Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setOpaque(false);
        JComboBox<String> searchCriteria = new JComboBox<>(new String[]{"Item Name", "SerialNumber"});
        JTextField searchField = new JTextField(20);
        JButton searchBtn = UIFactory.createButton("Search");

        searchBtn.addActionListener(e -> {
            String text = searchField.getText().trim();
            int colIndex = searchCriteria.getSelectedIndex() == 0 ? 1 : 6;

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

        // Table model listener for quantity updates
        table.getModel().addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE && e.getColumn() == 3) {
                int row = e.getFirstRow();
                int itemId = Integer.parseInt(model.getValueAt(row, 0).toString());
                String newQty = model.getValueAt(row, 3).toString();

                try (Connection conn = DatabaseManager.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(
                             "UPDATE TblInventoryItems SET Quantity = ? WHERE ItemID = ?")) {
                    stmt.setString(1, newQty);
                    stmt.setInt(2, itemId);
                    stmt.executeUpdate();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Failed to update quantity.");
                }
            }
        });
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setOpaque(false);
        JButton addBtn = UIFactory.createButton("Add Item");
        JButton deleteBtn = UIFactory.createButton("Delete Selected");

        addBtn.addActionListener(e -> openAddItemDialog());
        deleteBtn.addActionListener(e -> deleteSelectedItem());

        buttonPanel.add(addBtn);
        buttonPanel.add(deleteBtn);
        add(buttonPanel, BorderLayout.SOUTH);

        // Layout placement
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.add(title, BorderLayout.NORTH);
        topPanel.add(searchPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        loadInventoryItems();
        setVisible(true);
    }
    private void openAddItemDialog() {
        JTextField nameField = new JTextField(15);
        JTextField descField = new JTextField(15);
        JTextField quantityField = new JTextField(5);
        JTextField supplierField = new JTextField(15);
        JTextField expirationField = new JTextField(10); // yyyy-MM-dd
        JTextField serialField = new JTextField(10);
        JTextField thresholdField = new JTextField(5);

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.add(new JLabel("Item Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Description:"));
        panel.add(descField);
        panel.add(new JLabel("Quantity:"));
        panel.add(quantityField);
        panel.add(new JLabel("Supplier Info:"));
        panel.add(supplierField);
        panel.add(new JLabel("Expiration Date (yyyy-MM-dd):"));
        panel.add(expirationField);
        panel.add(new JLabel("Serial Number:"));
        panel.add(serialField);
        panel.add(new JLabel("Low Stock Threshold:"));
        panel.add(thresholdField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add Inventory Item",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                InventoryItem newItem = new InventoryItem(
                        0,
                        nameField.getText().trim(),
                        descField.getText().trim(),
                        Integer.parseInt(quantityField.getText().trim()),
                        supplierField.getText().trim(),
                        expirationField.getText().isEmpty() ? null : LocalDate.parse(expirationField.getText().trim()),
                        serialField.getText().trim(),
                        Integer.parseInt(thresholdField.getText().trim())
                );

                boolean success = secretaryController.addInventoryItem(newItem);
                if (success) {
                    loadInventoryItems();
                    JOptionPane.showMessageDialog(this, "Item added successfully.");
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to add item.");
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid input: " + ex.getMessage());
            }
        }
    }


    private void loadInventoryItems() {
        model.setRowCount(0);
        String sql = """
            SELECT ItemID, [Item Name], Description, Quantity, SupplierInformation,
                   ExpirationDate, SerialNumber, LowStockAlertThreshold
            FROM TblInventoryItems
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("ItemID"),
                        rs.getString("Item Name"),
                        rs.getString("Description"),
                        rs.getString("Quantity"),
                        rs.getString("SupplierInformation"),
                        rs.getDate("ExpirationDate") != null ? rs.getDate("ExpirationDate").toString() : "",
                        rs.getString("SerialNumber"),
                        rs.getString("LowStockAlertThreshold")
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void deleteSelectedItem() {
        int selected = table.getSelectedRow();
        if (selected >= 0) {
            int modelRow = table.convertRowIndexToModel(selected);
            int itemId = Integer.parseInt(model.getValueAt(modelRow, 0).toString());

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete item ID " + itemId + "?",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                boolean success = secretaryController.deleteInventoryItem(itemId);
                if (success) {
                    loadInventoryItems();
                    JOptionPane.showMessageDialog(this, "Item deleted.");
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete item.");
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select an item to delete.");
        }
    }

}
