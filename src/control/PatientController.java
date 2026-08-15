package control;

import entity.Patient;
import entity.Treatment;
import utils.DatabaseManager;

import java.time.LocalDate;
import java.time.Period;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class PatientController {

    private Connection conn;

    public PatientController() {
        try {
            conn = DatabaseManager.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Patient getPatientByID(int id) {
        String sql = """
            SELECT P.FirstName, P.LastName, P.PhoneNumber, P.Email, P.DateOfBirth,
                   T.Identifier, T.InsuranceProviderName, T.PolicyNumber
            FROM TblPersons P
            JOIN TblPatients T ON P.PersonId = T.PatientId
            WHERE P.PersonId = ?
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
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
            JOIN TblTreatmentPlans P ON A.TreatmentPlanID = P.TreatmentPlanID
            WHERE P.PatientID = ? AND P.Status = 'Active'
        """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                treatments.add(new Treatment(
                        rs.getString("TreatmentName"),
                        parseCost(rs.getString("Cost")),
                        rs.getString("Status")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return treatments;
    }

    private double parseCost(String costStr) {
        try {
            return Double.parseDouble(costStr.replaceAll("[^\\d.]", ""));
        } catch (Exception e) {
            return 0.0;
        }
    }

    public ArrayList<Object[]> getUpcomingAppointmentsForPatientWithIDs(int patientId) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        ArrayList<Object[]> appointments = new ArrayList<>();

        String sql = """
            SELECT AppointmentId, AppointmentDate, AppointmentTime, ReasonForVisit, Status, TreatmentName
            FROM TblAppointments
            WHERE PatientID = ? AND AppointmentDate >= DATE()
            ORDER BY AppointmentDate, AppointmentTime
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Object[] row = new Object[6];
                java.sql.Date sqlDate = rs.getDate("AppointmentDate");
                java.sql.Time sqlTime = rs.getTime("AppointmentTime");
                row[0] = dateFormat.format(sqlDate);
                row[1] = timeFormat.format(sqlTime);
                row[2] = rs.getString("ReasonForVisit");
                row[3] = rs.getString("TreatmentName");
                row[4] = rs.getString("Status");
                row[5] = rs.getInt("AppointmentID");
                appointments.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return appointments;
    }

    public boolean updateAppointmentStatus(int appointmentId, String newStatus) {
        String sql = "UPDATE TblAppointments SET Status = ? WHERE AppointmentID = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newStatus);
            stmt.setInt(2, appointmentId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean rescheduleAppointment(int appointmentId, String newDate, String newTime) {
        String sql = """
            UPDATE TblAppointments
            SET AppointmentDate = ?, AppointmentTime = ?, Status = 'Scheduled'
            WHERE AppointmentID = ?
            """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            // Convert to java.sql.Date
            java.sql.Date sqlDate = java.sql.Date.valueOf(newDate); // newDate should be in yyyy-MM-dd format
            stmt.setDate(1, sqlDate);

            // Convert to java.sql.Time
            java.sql.Time sqlTime = java.sql.Time.valueOf(newTime + ":00"); // newTime should be in HH:mm format
            stmt.setTime(2, sqlTime);

            stmt.setInt(3, appointmentId);

            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public int calculateAge(Date dob) {
        if (dob == null) return 0;
        LocalDate birthDate = dob.toLocalDate();
        LocalDate today = LocalDate.now();
        return Period.between(birthDate, today).getYears();
    }

    public ArrayList<Treatment> getAllTreatments() {
        ArrayList<Treatment> treatments = new ArrayList<>();
        String sql = "SELECT TreatmentName FROM TblTreatments";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
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


   
  
    
    public boolean bookAppointment(int patientId, Date date, String time, String reason, String treatmentName) {
        String sql = """
            INSERT INTO TblAppointments (PatientID, AppointmentDate, AppointmentTime, ReasonForVisit, TreatmentName, Status)
            VALUES (?, ?, ?, ?, ?, 'Scheduled')
        """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            stmt.setDate(2, new java.sql.Date(date.getTime()));
            java.sql.Time sqlTime = java.sql.Time.valueOf(time + ":00"); 
            stmt.setTime(3, sqlTime);   
            stmt.setString(4, reason);
            stmt.setString(5, treatmentName);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    
}
