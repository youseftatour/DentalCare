package boundary;

import control.DentistController;
import utils.DesignUtils;
import utils.UIFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ManageTreatmentPlansForm extends JFrame {
    private DentistController controller;
    private JTable table;
    private DefaultTableModel model;
    private String dentistId;

    public ManageTreatmentPlansForm(String dentistId) {
        this.dentistId = dentistId;
        this.controller = new DentistController();

        setTitle("My Treatment Plans");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 500);
        setLocationRelativeTo(null);
        setContentPane(new utils.GradientPanel());
        setLayout(new BorderLayout());

        JLabel title = UIFactory.createLabel("My Treatment Plans");
        title.setFont(DesignUtils.TITLE_FONT);
        title.setHorizontalAlignment(SwingConstants.CENTER);

        model = new DefaultTableModel(new Object[]{
                "Plan ID", "Patient", "Start", "Estimated Completion", "Status"
        }, 0);

        table = new JTable(model);
        UIFactory.styleTable(table);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(DesignUtils.BORDER_COLOR));

        title.setBorder(BorderFactory.createEmptyBorder(20, 20, 14, 20));

        add(title, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);

        loadPlans();
        setVisible(true);
    }

    private void loadPlans() {
        model.setRowCount(0);
        List<Object[]> data = controller.getPlansByDentist(dentistId);
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (Object[] row : data) {
            model.addRow(row);
        }
    }
}
