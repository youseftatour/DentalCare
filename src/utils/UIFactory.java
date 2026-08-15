package utils;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import java.awt.*;

public class UIFactory {
	public static JPanel createFormPanel(String[] labels, JComponent[] inputs) {
	    JPanel panel = new JPanel(new GridBagLayout());
	    GridBagConstraints gbc = new GridBagConstraints();
	    gbc.insets = new Insets(5, 5, 5, 5);
	    gbc.anchor = GridBagConstraints.WEST;

	    for (int i = 0; i < labels.length; i++) {
	        gbc.gridx = 0;
	        gbc.gridy = i;
	        panel.add(new JLabel(labels[i]), gbc);

	        gbc.gridx = 1;
	        panel.add(inputs[i], gbc);
	    }

	    return panel;
	}

	public static JTextField createSearchField() {
	    JTextField searchField = new JTextField(20);
	    searchField.setToolTipText("Search...");
	    return searchField;
	}

	 public static JTable createStripedTable(DefaultTableModel model) {
	        JTable table = new JTable(model);
	        table.setFillsViewportHeight(true);
	        table.setAutoCreateRowSorter(true); // Enables column sorting
	        table.setRowHeight(25);
	        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

	        // Optional striped rows
	        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
	            @Override
	            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
	                Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
	                if (!isSelected) {
	                    c.setBackground(row % 2 == 0 ? new Color(245, 245, 245) : Color.WHITE);
	                }
	                return c;
	            }
	        });

	        return table;
	    }
    public static JLabel createTitleLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 24));
        label.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return label;
    }
    public static JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(DesignUtils.LABEL_FONT);
        label.setForeground(DesignUtils.FOREGROUND_COLOR);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

    public static JTextField createTextField() {
        JTextField tf = new JTextField();
        tf.setMaximumSize(DesignUtils.TEXTFIELD_SIZE);
        tf.setBackground(DesignUtils.FIELD_BG);
        tf.setFont(DesignUtils.LABEL_FONT);
        tf.setAlignmentX(Component.CENTER_ALIGNMENT);
        return tf;
    }

    public static JPasswordField createPasswordField() {
        JPasswordField pf = new JPasswordField();
        pf.setMaximumSize(DesignUtils.TEXTFIELD_SIZE);
        pf.setBackground(DesignUtils.FIELD_BG);
        pf.setFont(DesignUtils.LABEL_FONT);
        pf.setAlignmentX(Component.CENTER_ALIGNMENT);
        return pf;
    }

    public static JButton createButton(String text) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(DesignUtils.BUTTON_SIZE);
        btn.setFont(DesignUtils.BUTTON_FONT);
        btn.setBackground(DesignUtils.PRIMARY_COLOR);
        btn.setForeground(Color.WHITE);
        btn.setCursor(DesignUtils.HAND_CURSOR);
        btn.setFocusPainted(false);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        return btn;
    }
}
