package utils;

import java.awt.*;

import javax.swing.BorderFactory;
import javax.swing.JLabel;

public class DesignUtils {

    // Fonts
    public static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font SUBTITLE_FONT = new Font("Segoe UI", Font.BOLD, 18);


    // Colors
    public static final Color PRIMARY_COLOR = new Color(38, 116, 166);
    public static final Color BACKGROUND_COLOR = new Color(15, 32, 39);
    public static final Color FOREGROUND_COLOR = Color.WHITE;
    public static final Color FIELD_BG = new Color(248, 250, 252);
    public static final Color SURFACE_COLOR = new Color(250, 252, 253);
    public static final Color TEXT_COLOR = new Color(30, 45, 55);
    public static final Color MUTED_TEXT_COLOR = new Color(92, 108, 117);
    public static final Color BORDER_COLOR = new Color(205, 216, 222);

    // Sizes
    public static final Dimension BUTTON_SIZE = new Dimension(200, 38);
    public static final Dimension TEXTFIELD_SIZE = new Dimension(300, 36);

    // Cursors
    public static final Cursor HAND_CURSOR = new Cursor(Cursor.HAND_CURSOR);

    public static JLabel createTitleLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 24));
        label.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return label;
    }
}
