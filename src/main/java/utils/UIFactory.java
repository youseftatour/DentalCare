package utils;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

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

    public static void styleTable(JTable table) {
        table.setFont(DesignUtils.LABEL_FONT);
        table.setRowHeight(32);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(230, 236, 239));
        table.setSelectionBackground(new Color(205, 230, 246));
        table.setSelectionForeground(DesignUtils.TEXT_COLOR);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(DesignUtils.BUTTON_FONT);
        header.setBackground(new Color(231, 240, 245));
        header.setForeground(DesignUtils.TEXT_COLOR);
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 36));
        header.setReorderingAllowed(false);
    }

    public static void styleTabs(JTabbedPane tabs) {
        tabs.setFont(DesignUtils.BUTTON_FONT);
        tabs.setBackground(new Color(231, 240, 245));
        tabs.setForeground(DesignUtils.TEXT_COLOR);
        tabs.setBorder(BorderFactory.createEmptyBorder());
    }

    public static JPanel createActionCard(String title, String description, JButton action) {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(DesignUtils.SURFACE_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(DesignUtils.BORDER_COLOR),
            BorderFactory.createEmptyBorder(22, 22, 22, 22)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;

        JLabel heading = new JLabel(title);
        heading.setFont(DesignUtils.SUBTITLE_FONT);
        heading.setForeground(DesignUtils.TEXT_COLOR);
        card.add(heading, gbc);

        gbc.gridy = 1;
        gbc.weighty = 1;
        gbc.insets = new Insets(10, 0, 20, 0);
        JLabel details = new JLabel("<html><body style='width:190px'>" + description + "</body></html>");
        details.setFont(DesignUtils.LABEL_FONT);
        details.setForeground(DesignUtils.MUTED_TEXT_COLOR);
        card.add(details, gbc);

        gbc.gridy = 2;
        gbc.weighty = 0;
        gbc.insets = new Insets(0, 0, 0, 0);
        card.add(action, gbc);
        return card;
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
        btn.setBorder(BorderFactory.createEmptyBorder(9, 18, 9, 18));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        return btn;
    }
}
