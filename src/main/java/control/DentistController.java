package control;

import entity.Appointment;
import entity.Patient;
import repository.AppointmentRepository;
import repository.PatientRepository;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import utils.DatabaseManager;
import service.DomainValidator;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

public class DentistController {
    private final PatientRepository patientRepository = new PatientRepository();
    private final AppointmentRepository appointmentRepository = new AppointmentRepository();

    public ArrayList<Patient> getAllPatients() {
        try {
            return patientRepository.findAll();
        } catch (SQLException e) {
            utils.AppLogger.error(DentistController.class, "Dentist database operation failed", e);
            return new ArrayList<>();
        }
    }

    public boolean createTreatmentPlan(int patientId, Date startDate, Date endDate, String dentistId) {
        if (startDate == null || endDate == null || !DomainValidator.isValidId(dentistId)
                || !DomainValidator.isValidTreatmentPlan(
                    startDate.toLocalDate(), endDate.toLocalDate())) {
            return false;
        }

        if (endDate.before(startDate)) {
            return false;
        }

        String sql = """
            INSERT INTO TblTreatmentPlans
            (StartDate, EstimatedCompletionDate, PatientId, Status, CreatedByDentist)
            VALUES (?, ?, ?, 'Active', ?)
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, startDate);
            stmt.setDate(2, endDate);
            stmt.setInt(3, patientId);
            stmt.setString(4, dentistId);

            return stmt.executeUpdate() > 0;

        } catch (Exception e) {
            utils.AppLogger.error(DentistController.class, "Dentist database operation failed", e);
            return false;
        }
    }

    public ArrayList<Object[]> getPlansByDentist(String dentistId) {
        ArrayList<Object[]> plans = new ArrayList<>();

        String sql = """
            SELECT TP.TreatmentPlanId,
                   Per.FirstName & ' ' & Per.LastName AS PatientName,
                   TP.StartDate, TP.EstimatedCompletionDate, TP.Status
            FROM (TblTreatmentPlans TP
            INNER JOIN TblPatients Pa ON TP.PatientId = Pa.PatientId)
            INNER JOIN TblPersons Per ON Pa.PatientId = Per.PersonID
            WHERE TP.CreatedByDentist = ?
            ORDER BY TP.StartDate DESC
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, dentistId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Date start = rs.getDate("StartDate");
                    Date end = rs.getDate("EstimatedCompletionDate");

                    plans.add(new Object[]{
                        rs.getInt("TreatmentPlanId"),
                        rs.getString("PatientName"),
                        start != null ? start.toString() : "",
                        end != null ? end.toString() : "",
                        rs.getString("Status")
                    });
                }
            }

        } catch (Exception e) {
            utils.AppLogger.error(DentistController.class, "Dentist database operation failed", e);
        }

        return plans;
    }

    public ArrayList<Appointment> getTodaysAppointments(int dentistId) {
        ArrayList<Appointment> list = new ArrayList<>();

        String sql = """
            SELECT AppointmentID, PatientID, TreatmentPlanID, TreatmentName, Cost, Status,
                   AppointmentDate, AppointmentTime, IsPaid, IsSterilized
            FROM TblAppointments
            WHERE AppointmentDate = ?
              AND AssignedMedicalStaff = ?
              AND (Status IS NULL OR (Status <> 'Cancelled' AND Status <> 'Canceled'))
            ORDER BY AppointmentTime
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(LocalDate.now()));
            stmt.setString(2, String.valueOf(dentistId));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Date appointmentDate = rs.getDate("AppointmentDate");
                    java.sql.Time appointmentTime = rs.getTime("AppointmentTime");

                    if (appointmentDate == null || appointmentTime == null) {
                        continue;
                    }

                    list.add(new Appointment(
                        rs.getInt("AppointmentID"),
                        rs.getInt("PatientID"),
                        rs.getInt("TreatmentPlanID"),
                        rs.getString("TreatmentName"),
                        rs.getDouble("Cost"),
                        rs.getString("Status"),
                        appointmentDate.toLocalDate(),
                        appointmentTime.toLocalTime(),
                        rs.getBoolean("IsPaid"),
                        rs.getBoolean("IsSterilized")
                    ));
                }
            }

        } catch (Exception e) {
            utils.AppLogger.error(DentistController.class, "Dentist database operation failed", e);
        }

        return list;
    }

    public boolean completeAppointment(int appointmentId) {
        try {
            return appointmentRepository.updateStatus(appointmentId, "Completed");
        } catch (SQLException e) {
            utils.AppLogger.error(DentistController.class, "Dentist database operation failed", e);
            return false;
        }
    }

    /**
     * Keeps database/report filling out of the Swing boundary class.
     * The dashboard is still responsible for displaying the returned JasperPrint.
     */
    public JasperPrint generateTreatmentProgressReport(String dentistId) throws Exception {
        HashMap<String, Object> params = new HashMap<>();
        params.put("DentistID", dentistId);

        try (InputStream reportStream =
                     DentistController.class.getResourceAsStream("/boundary/TreatmentProgressReport.jasper");
             Connection conn = DatabaseManager.getConnection()) {

            if (reportStream == null) {
                throw new IllegalStateException(
                    "Report file not found: /boundary/TreatmentProgressReport.jasper"
                );
            }

            return JasperFillManager.fillReport(reportStream, params, conn);
        }
    }
}


