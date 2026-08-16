package repository;

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
import java.time.LocalTime;
import java.time.Period;
import java.util.ArrayList;

public class PatientRepository {
    public Patient findById(int id) throws SQLException {
        String sql = """
            SELECT P.FirstName, P.LastName, P.PhoneNumber, P.Email, P.DateOfBirth,
                   T.Identifier, T.InsuranceProviderName, T.PolicyNumber
            FROM TblPersons P
            INNER JOIN TblPatients T ON P.PersonId = T.PatientId
            WHERE P.PersonId = ?
            """;
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                Date dateOfBirth = resultSet.getDate("DateOfBirth");
                int age = dateOfBirth == null ? 0
                    : Period.between(dateOfBirth.toLocalDate(), LocalDate.now()).getYears();
                return new Patient(
                    id,
                    resultSet.getString("FirstName") + " " + resultSet.getString("LastName"),
                    resultSet.getString("PhoneNumber"),
                    resultSet.getString("Email"),
                    age,
                    resultSet.getString("InsuranceProviderName"),
                    resultSet.getString("PolicyNumber")
                );
            }
        }
    }

    public ArrayList<Treatment> findActiveTreatments(int patientId) throws SQLException {
        String sql = """
            SELECT A.TreatmentName, A.Cost, A.Status
            FROM TblAppointments A
            INNER JOIN TblTreatmentPlans P ON A.TreatmentPlanID = P.TreatmentPlanID
            WHERE P.PatientID = ? AND P.Status = 'Active'
            ORDER BY A.AppointmentDate, A.AppointmentTime
            """;
        ArrayList<Treatment> treatments = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, patientId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    treatments.add(new Treatment(resultSet.getString("TreatmentName"),
                        resultSet.getDouble("Cost"), resultSet.getString("Status")));
                }
            }
        }
        return treatments;
    }

    public ArrayList<Object[]> findUpcomingAppointments(int patientId, LocalDate fromDate) throws SQLException {
        String sql = """
            SELECT AppointmentID, AppointmentDate, AppointmentTime,
                   ReasonForVisit, Status, TreatmentName
            FROM TblAppointments
            WHERE PatientID = ? AND AppointmentDate >= ?
              AND (Status IS NULL OR (Status <> 'Cancelled' AND Status <> 'Canceled'))
            ORDER BY AppointmentDate, AppointmentTime
            """;
        ArrayList<Object[]> appointments = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, patientId);
            statement.setDate(2, Date.valueOf(fromDate));
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Date date = resultSet.getDate("AppointmentDate");
                    Time time = resultSet.getTime("AppointmentTime");
                    if (date != null && time != null) {
                        appointments.add(new Object[]{date.toLocalDate().toString(),
                            time.toLocalTime().withSecond(0).withNano(0).toString(),
                            resultSet.getString("ReasonForVisit"), resultSet.getString("TreatmentName"),
                            resultSet.getString("Status"), resultSet.getInt("AppointmentID")});
                    }
                }
            }
        }
        return appointments;
    }

    public ArrayList<Treatment> findAllTreatments() throws SQLException {
        ArrayList<Treatment> treatments = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                 "SELECT TreatmentName FROM TblTreatments ORDER BY TreatmentName");
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                treatments.add(new Treatment(resultSet.getString("TreatmentName"), 0.0, "Available"));
            }
        }
        return treatments;
    }

    public boolean updateAppointmentStatus(int appointmentId, String status) throws SQLException {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                 "UPDATE TblAppointments SET Status = ? WHERE AppointmentID = ?")) {
            statement.setString(1, status);
            statement.setInt(2, appointmentId);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean rescheduleAppointment(int appointmentId, LocalDate date, LocalTime time) throws SQLException {
        String sql = """
            UPDATE TblAppointments
            SET AppointmentDate = ?, AppointmentTime = ?, Status = 'Scheduled'
            WHERE AppointmentID = ?
            """;
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDate(1, Date.valueOf(date));
            statement.setTime(2, Time.valueOf(time));
            statement.setInt(3, appointmentId);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean insertAppointment(int patientId, Date date, LocalTime time,
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
