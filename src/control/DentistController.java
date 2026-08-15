package control;

import entity.Appointment;
import entity.Patient;
import utils.DatabaseManager;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;

public class DentistController {
	 public ArrayList<Patient> getAllPatients() {
	        ArrayList<Patient> patients = new ArrayList<>();
	        String sql = """
	            SELECT Pa.PatientID, Per.FirstName, Per.LastName
	            FROM TblPatients Pa
	            JOIN TblPersons Per ON Pa.PatientID = Per.PersonID
	        """;

	        try (Connection conn = DatabaseManager.getConnection();
	             PreparedStatement stmt = conn.prepareStatement(sql);
	             ResultSet rs = stmt.executeQuery()) {

	            while (rs.next()) {
	                int id = rs.getInt("PatientID");
	                String name = rs.getString("FirstName") + " " + rs.getString("LastName");
	                patients.add(new Patient(id, name));
	            }

	        } catch (Exception e) {
	            e.printStackTrace();
	        }

	        return patients;
	    }

	    public boolean createTreatmentPlan(int patientId, Date startDate, Date endDate, String dentistId) {
	        String sql = """
	            INSERT INTO TblTreatmentPlans (StartDate, EstimatedCompletionDate, PatientId, Status, CreatedByDentist)
	            VALUES (?, ?, ?, 'Active', ?)
	        """;

	        try (Connection conn = DatabaseManager.getConnection();
	             PreparedStatement stmt = conn.prepareStatement(sql)) {

	            stmt.setDate(1, new java.sql.Date(startDate.getTime()));
	            stmt.setDate(2, new java.sql.Date(endDate.getTime()));
	            stmt.setString(3, String.valueOf(patientId));
	            stmt.setString(4, dentistId);

	            stmt.executeUpdate();
	            return true;

	        } catch (Exception e) {
	            e.printStackTrace();
	            return false;
	        }
	    }

	    public ArrayList<Object[]> getPlansByDentist(String dentistId) {
	        ArrayList<Object[]> plans = new ArrayList<>();
	        String sql = """
	            SELECT TP.TreatmentPlanId, Per.FirstName || ' ' || Per.LastName AS PatientName,
	                   TP.StartDate, TP.EstimatedCompletionDate, TP.Status
	            FROM TblTreatmentPlans TP
	            JOIN TblPatients Pa ON TP.PatientId = Pa.PatientId
	            JOIN TblPersons Per ON Pa.PatientId = Per.PersonID
	            WHERE TP.CreatedByDentist = ?
	        """;

	        try (Connection conn = DatabaseManager.getConnection();
	             PreparedStatement stmt = conn.prepareStatement(sql)) {

	            stmt.setString(1, dentistId);
	            ResultSet rs = stmt.executeQuery();

	            while (rs.next()) {
	                plans.add(new Object[]{
	                        rs.getInt("TreatmentPlanId"),
	                        rs.getString("PatientName"),
	                        rs.getDate("StartDate").toString(),
	                        rs.getDate("EstimatedCompletionDate").toString(),
	                        rs.getString("Status")
	                });
	            }

	        } catch (Exception e) {
	            e.printStackTrace();
	        }

	        return plans;
	    }
	
	

    public ArrayList<Appointment> getTodaysAppointments(int dentistId) {
        ArrayList<Appointment> list = new ArrayList<>();
        String sql = """
            SELECT AppointmentID, PatientID, TreatmentPlanID, TreatmentName, Cost, Status,
                   AppointmentDate, AppointmentTime, IsPaid, IsSterilized
            FROM TblAppointments
            WHERE AppointmentDate = Date()
            AND TreatmentPlanID IN (
                SELECT TreatmentPlanID FROM TblTreatmentPlans
                WHERE DentistID = ?
            )
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, dentistId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new Appointment(
                        rs.getInt("AppointmentID"),
                        rs.getInt("PatientID"),
                        rs.getInt("TreatmentPlanID"),
                        rs.getString("TreatmentName"),
                        rs.getDouble("Cost"),
                        rs.getString("Status"),
                        rs.getDate("AppointmentDate").toLocalDate(),
                        rs.getTime("AppointmentTime").toLocalTime(),
                        rs.getBoolean("IsPaid"),
                        rs.getBoolean("IsSterilized")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public boolean completeAppointment(int appointmentId) {
        String sql = "UPDATE TblAppointments SET Status = 'Completed' WHERE AppointmentID = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, appointmentId);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
