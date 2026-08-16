package boundary;

import control.DentistController;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.view.JasperViewer;
import utils.UIFactory;
import utils.DesignUtils;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.Callable;

public class DentistDashboard extends JFrame {
    private String dentistId;
    private DentistController controller;

    public DentistDashboard(String dentistId) {
        this.dentistId = dentistId;
        this.controller = new DentistController();

        setTitle("Dentist Dashboard");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(940, 570);
        setMinimumSize(new Dimension(800, 520));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(20, 20));
        setContentPane(new utils.GradientPanel());

        JLabel title = UIFactory.createLabel("Dentist Dashboard");
        title.setFont(DesignUtils.TITLE_FONT);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setBorder(BorderFactory.createEmptyBorder(34, 20, 6, 20));

        JButton createPlanBtn = UIFactory.createButton("Create Treatment Plan");
        JButton managePlansBtn = UIFactory.createButton("Manage Treatment Plans");
        JButton reportBtn = UIFactory.createButton("Treatment Progress Report");

        createPlanBtn.addActionListener(e -> new CreateTreatmentPlanForm(dentistId));
        managePlansBtn.addActionListener(e -> new ManageTreatmentPlansForm(dentistId));
        reportBtn.addActionListener(e -> generateTreatmentProgressReport());

        JPanel btnPanel = new JPanel(new GridLayout(1, 3, 18, 0));
        btnPanel.setOpaque(false);
        btnPanel.setBorder(BorderFactory.createEmptyBorder(24, 42, 48, 42));

        btnPanel.add(UIFactory.createActionCard("Create plan",
            "Start a treatment plan for a selected patient.", createPlanBtn));
        btnPanel.add(UIFactory.createActionCard("Manage plans",
            "Review your treatment plans and their current status.", managePlansBtn));
        btnPanel.add(UIFactory.createActionCard("Progress report",
            "Generate the latest treatment-progress report.", reportBtn));

        add(title, BorderLayout.NORTH);
        add(btnPanel, BorderLayout.CENTER);

        setVisible(true);
    }

    private void generateTreatmentProgressReport() {
        runReport(() -> controller.generateTreatmentProgressReport(dentistId));
    }

    private void runReport(Callable<JasperPrint> reportTask) {
        new SwingWorker<JasperPrint, Void>() {
            protected JasperPrint doInBackground() throws Exception { return reportTask.call(); }
            protected void done() {
                try {
                    JasperViewer.viewReport(get(), false);
                } catch (Exception exception) {
                    utils.AppLogger.error(DentistDashboard.class,
                        "Treatment report generation failed", exception);
                    JOptionPane.showMessageDialog(DentistDashboard.this,
                        "The report could not be generated. Please try again.",
                        "Report Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }
}



