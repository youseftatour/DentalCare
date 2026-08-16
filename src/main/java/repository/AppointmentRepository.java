package repository;

import utils.DatabaseManager;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;

public class AppointmentRepository {
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
