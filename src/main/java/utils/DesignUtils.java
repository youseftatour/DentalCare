package utils;

import java.awt.*;

import javax.swing.BorderFactory;
import javax.swing.JLabel;

public class DesignUtils {

    // Fonts
    public static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font SUBTITLE_FONT = new Font("Segoe UI", Font.BOLD, 18);


    // Colors
    public static final Color PRIMARY_COLOR = new Color(70, 130, 180);      // Steel Blue
    public static final Color BACKGROUND_COLOR = new Color(45, 45, 45);     // Dark Gray
    public static final Color FOREGROUND_COLOR = Color.WHITE;
    public static final Color FIELD_BG = new Color(245, 245, 245);

    // Sizes
    public static final Dimension BUTTON_SIZE = new Dimension(200, 30);
    public static final Dimension TEXTFIELD_SIZE = new Dimension(300, 25);

    // Cursors
    public static final Cursor HAND_CURSOR = new Cursor(Cursor.HAND_CURSOR);

    public static JLabel createTitleLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 24));
        label.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return label;
    }
}
