package boundary;

import javax.swing.*;
import java.awt.*;

public class TrackInventoryForm extends JPanel {

    public TrackInventoryForm() {
        setOpaque(false);
        setLayout(new BorderLayout(10, 10));
        JLabel label = new JLabel("Inventory Tracking (To be implemented)");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 18));
        add(label, BorderLayout.CENTER);
    }
}
