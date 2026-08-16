package repository;

import utils.DatabaseManager;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class AppointmentRepository {
    public record AppointmentSlot(int appointmentId, LocalTime start, int durationMinutes, String status) { }

    public List<AppointmentSlot> findStaffAppointments(String staffId, LocalDate date) throws SQLException {
        boolean filterByStaff = staffId != null && !staffId.isBlank();
        String sql = """
            SELECT A.AppointmentID, A.AppointmentTime, A.Status, T.DurationMinutes
            FROM TblAppointments A
            INNER JOIN TblTreatments T ON A.TreatmentName = T.TreatmentName
            WHERE A.AppointmentDate = ?
            """;
        if (filterByStaff) {
            sql += " AND A.AssignedMedicalStaff = ?";
        }
        List<AppointmentSlot> appointments = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDate(1, Date.valueOf(date));
            if (filterByStaff) {
                statement.setString(2, staffId);
            }
            try (var resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Time time = resultSet.getTime("AppointmentTime");
                    if (time != null) {
                        appointments.add(new AppointmentSlot(resultSet.getInt("AppointmentID"),
                            time.toLocalTime(), resultSet.getInt("DurationMinutes"),
                            resultSet.getString("Status")));
                    }
                }
            }
        }
        return appointments;
    }

    public String findAssignedStaff(int appointmentId) throws SQLException {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                 "SELECT AssignedMedicalStaff FROM TblAppointments WHERE AppointmentID = ?")) {
            statement.setInt(1, appointmentId);
            try (var resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getString("AssignedMedicalStaff") : null;
            }
        }
    }

    public int findDurationMinutes(int appointmentId) throws SQLException {
        String sql = """
            SELECT T.DurationMinutes
            FROM TblAppointments A
            INNER JOIN TblTreatments T ON A.TreatmentName = T.TreatmentName
            WHERE A.AppointmentID = ?
            """;
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, appointmentId);
            try (var resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getInt("DurationMinutes") : 0;
            }
        }
    }
    public boolean updateStatus(int appointmentId, String status) throws SQLException {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                 "UPDATE TblAppointments SET Status = ? WHERE AppointmentID = ?")) {
            statement.setString(1, status);
            statement.setInt(2, appointmentId);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean reschedule(int appointmentId, LocalDate date, LocalTime time) throws SQLException {
        return reschedule(appointmentId, date, time, "Scheduled");
    }

    public boolean reschedule(int appointmentId, LocalDate date, LocalTime time, String status) throws SQLException {
        String sql = """
            UPDATE TblAppointments
            SET AppointmentDate = ?, AppointmentTime = ?, Status = ?
            WHERE AppointmentID = ?
            """;
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDate(1, Date.valueOf(date));
            statement.setTime(2, Time.valueOf(time));
            statement.setString(3, status);
            statement.setInt(4, appointmentId);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean insert(int patientId, Date date, LocalTime time,
                          String reason, String treatmentName) throws SQLException {
        String sql = """
            INSERT INTO TblAppointments
                (PatientID, AppointmentDate, AppointmentTime, ReasonForVisit, TreatmentName, Status)
            VALUES (?, ?, ?, ?, ?, 'Scheduled')
            """;
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, patientId);
            statement.setDate(2, date);
            statement.setTime(3, Time.valueOf(time));
            statement.setString(4, reason);
            statement.setString(5, treatmentName);
            return statement.executeUpdate() > 0;
        }
    }
}
