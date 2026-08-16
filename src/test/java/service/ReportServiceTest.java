package service;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ReportServiceTest {
    @Test
    void missingReportProducesControlledError() {
        assertThrows(IllegalStateException.class,
            () -> new ReportService().generate("/missing-report.jasper", Map.of()));
    }
}
