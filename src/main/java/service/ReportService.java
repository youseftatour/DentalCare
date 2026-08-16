package service;

import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import utils.DatabaseManager;

import java.io.InputStream;
import java.sql.Connection;
import java.util.Map;

public class ReportService {
    public JasperPrint generate(String resourcePath, Map<String, Object> parameters) throws Exception {
        try (InputStream report = ReportService.class.getResourceAsStream(resourcePath)) {
            if (report == null) {
                throw new IllegalStateException("Report resource not found: " + resourcePath);
            }
            try (Connection connection = DatabaseManager.getConnection()) {
                return JasperFillManager.fillReport(report, parameters, connection);
            }
        }
    }
}
