package control;

import entity.Patient;
import entity.Treatment;
import utils.DatabaseManager;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

public class PatientController {

    public PatientController() {
        // Connections are deliberately opened per operation.
        // This avoids keeping a database connection alive for the lifetime of the UI.
    }

    public Patient getPatientByID(int id) {
        String sql = """
            SELECT P.FirstName, P.LastName, P.PhoneNumber, P.Email, P.DateOfBirth,
                   T.Identifier, T.InsuranceProviderName, T.PolicyNumber
            FROM TblPersons P
            INNER JOIN TblPatients T ON P.PersonId = T.PatientId
            WHERE P.PersonId = ?
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String fullName = rs.getString("FirstName") + " " + rs.getString("LastName");

                    return new Patient(
                        id,
                        fullName,
                        rs.getString("PhoneNumber"),
                        rs.getString("Email"),
                        calculateAge(rs.getDate("DateOfBirth")),
                        rs.getString("InsuranceProviderName"),
                        rs.getString("PolicyNumber")
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public ArrayList<Treatment> getActiveTreatmentsForPatient(int patientId) {
        ArrayList<Treatment> treatments = new ArrayList<>();

        String sql = """
            SELECT A.TreatmentName, A.Cost, A.Status
            FROM TblAppointments A
            INNER JOIN TblTreatmentPlans P ON A.TreatmentPlanID = P.TreatmentPlanID
            WHERE P.PatientID = ? AND P.Status = 'Active'
            ORDER BY A.AppointmentDate, A.AppointmentTime
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, patientId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    treatments.add(new Treatment(
                        rs.getString("TreatmentName"),
                        rs.getDouble("Cost"),
                        rs.getString("Status")
                    ));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return treatments;
    }

    public ArrayList<Object[]> getUpcomingAppointmentsForPatientWithIDs(int patientId) {
        ArrayList<Object[]> appointments = new ArrayList<>();

        String sql = """
            SELECT AppointmentID, AppointmentDate, AppointmentTime,
                   ReasonForVisit, Status, TreatmentName
            FROM TblAppointments
            WHERE PatientID = ?
              AND AppointmentDate >= ?
              AND (Status IS NULL OR (Status <> 'Cancelled' AND Status <> 'Canceled'))
            ORDER BY AppointmentDate, AppointmentTime
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, patientId);
            stmt.setDate(2, Date.valueOf(LocalDate.now()));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Date sqlDate = rs.getDate("AppointmentDate");
                    Time sqlTime = rs.getTime("AppointmentTime");

                    if (sqlDate == null || sqlTime == null) {
                        continue;
                    }

                    appointments.add(new Object[]{
                        sqlDate.toLocalDate().toString(),
                        sqlTime.toLocalTime().withSecond(0).withNano(0).toString(),
                        rs.getString("ReasonForVisit"),
                        rs.getString("TreatmentName"),
                        rs.getString("Status"),
                        rs.getInt("AppointmentID")
                    });
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return appointments;
    }

    public boolean updateAppointmentStatus(int appointmentId, String newStatus) {
        if (newStatus == null || newStatus.isBlank()) {
            return false;
        }

        String sql = "UPDATE TblAppointments SET Status = ? WHERE AppointmentID = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newStatus);
            stmt.setInt(2, appointmentId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean rescheduleAppointment(int appointmentId, String newDate, String newTime) {
        try {
            LocalDate date = LocalDate.parse(newDate);
            LocalTime time = parseTime(newTime);

            if (LocalDateTime.of(date, time).isBefore(LocalDateTime.now())) {
                return false;
            }

            String sql = """
                UPDATE TblAppointments
                SET AppointmentDate = ?, AppointmentTime = ?, Status = 'Scheduled'
                WHERE AppointmentID = ?
                """;

            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setDate(1, Date.valueOf(date));
                stmt.setTime(2, Time.valueOf(time));
                stmt.setInt(3, appointmentId);
                return stmt.executeUpdate() > 0;
            }

        } catch (DateTimeParseException | SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int calculateAge(Date dob) {
        if (dob == null) {
            return 0;
        }

        return Period.between(dob.toLocalDate(), LocalDate.now()).getYears();
    }

    public ArrayList<Treatment> getAllTreatments() {
        ArrayList<Treatment> treatments = new ArrayList<>();
        String sql = "SELECT TreatmentName FROM TblTreatments ORDER BY TreatmentName";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                treatments.add(new Treatment(
                    rs.getString("TreatmentName"),
                    0.0,
                    "Available"
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return treatments;
    }

    public boolean bookAppointment(int patientId, Date date, String time,
                                   String reason, String treatmentName) {

        if (date == null || treatmentName == null || treatmentName.isBlank()) {
            return false;
        }

        try {
            LocalTime appointmentTime = parseTime(time);
            LocalDate appointmentDate = date.toLocalDate();

            if (LocalDateTime.of(appointmentDate, appointmentTime).isBefore(LocalDateTime.now())) {
                return false;
            }

            String sql = """
                INSERT INTO TblAppointments
                (PatientID, AppointmentDate, AppointmentTime, ReasonForVisit, TreatmentName, Status)
                VALUES (?, ?, ?, ?, ?, 'Scheduled')
                """;

            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, patientId);
                stmt.setDate(2, date);
                stmt.setTime(3, Time.valueOf(appointmentTime));
                stmt.setString(4, reason);
                stmt.setString(5, treatmentName);
                return stmt.executeUpdate() > 0;
            }

        } catch (DateTimeParseException | SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private LocalTime parseTime(String value) {
        if (value == null || value.isBlank()) {
            throw new DateTimeParseException("Time is empty", "", 0);
        }

        String trimmed = value.trim();

        if (trimmed.length() == 5) {
            return LocalTime.parse(trimmed);
        }

        return Time.valueOf(trimmed).toLocalTime();
    }
}
