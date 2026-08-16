package boundary;

import control.DentistController;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.view.JasperViewer;
import utils.DatabaseManager;
import utils.UIFactory;

import javax.swing.*;
import java.awt.*;
import java.io.InputStream;
import java.util.HashMap;

public class DentistDashboard extends JFrame {
    private String dentistId;
    private DentistController controller;

    public DentistDashboard(String dentistId) {
        this.dentistId = dentistId;
        this.controller = new DentistController();

        setTitle("Dentist Dashboard");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        setContentPane(new utils.GradientPanel());

        JLabel title = UIFactory.createLabel("Dentist Dashboard");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setHorizontalAlignment(SwingConstants.CENTER);

        JButton createPlanBtn = UIFactory.createButton("Create Treatment Plan");
        JButton managePlansBtn = UIFactory.createButton("Manage Treatment Plans");
        JButton reportBtn = UIFactory.createButton("Treatment Progress Report");

        createPlanBtn.addActionListener(e -> new CreateTreatmentPlanForm(dentistId));
        managePlansBtn.addActionListener(e -> new ManageTreatmentPlansForm(dentistId));
        reportBtn.addActionListener(e -> generateTreatmentProgressReport());

        JPanel btnPanel = new JPanel(new GridLayout(3, 1, 20, 20));
        btnPanel.setOpaque(false);
        btnPanel.setBorder(BorderFactory.createEmptyBorder(30, 60, 30, 60));

        btnPanel.add(createPlanBtn);
        btnPanel.add(managePlansBtn);
        btnPanel.add(reportBtn);

        add(title, BorderLayout.NORTH);
        add(btnPanel, BorderLayout.CENTER);

        setVisible(true);
    }

    private void generateTreatmentProgressReport() {
        try {
            HashMap<String, Object> params = new HashMap<>();
            params.put("DentistID", dentistId);
            InputStream is = getClass().getResourceAsStream("/boundary/TreatmentProgressReport.jasper");
            JasperPrint print = JasperFillManager.fillReport(
                    getClass().getResourceAsStream("TreatmentProgressReport.jasper"),
                    params,
                    DatabaseManager.getConnection()
            );
            JasperViewer.viewReport(print, false);
        } catch (Exception e) {
            utils.AppLogger.error(DentistDashboard.class, "Treatment report generation failed", e);
            JOptionPane.showMessageDialog(this,
                "The report could not be generated. Please try again.",
                "Report Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}



