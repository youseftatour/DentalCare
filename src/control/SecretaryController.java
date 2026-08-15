package control;

import entity.Appointment;
import entity.InventoryItem;
import entity.Patient;
import entity.StaffMember;
import utils.DatabaseManager;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.util.*;

public class SecretaryController {
	public boolean addInventoryItem(InventoryItem item) {
	    String sql = """
	        INSERT INTO TblInventoryItems 
	        ([Item Name], Description, Quantity, SupplierInformation, ExpirationDate, SerialNumber, LowStockAlertThreshold)
	        VALUES (?, ?, ?, ?, ?, ?, ?)
	    """;

	    try (Connection conn = DatabaseManager.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(sql)) {

	        stmt.setString(1, item.getItemName());
	        stmt.setString(2, item.getDescription());
	        stmt.setInt(3, item.getQuantity());
	        stmt.setString(4, item.getSupplierInformation());

	        if (item.getExpiryDate() != null) {
	            stmt.setDate(5, Date.valueOf(item.getExpiryDate()));
	        } else {
	            stmt.setNull(5, Types.DATE);
	        }

	        stmt.setString(6, item.getSerialNumber());
	        stmt.setInt(7, item.getLowStockThreshold());

	        return stmt.executeUpdate() > 0;

	    } catch (Exception e) {
	        e.printStackTrace();
	        return false;
	    }
	}

	public boolean deleteInventoryItem(int itemId) {
	    String sql = "DELETE FROM TblInventoryItems WHERE ItemID = ?";
	    try (Connection conn = DatabaseManager.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(sql)) {

	        stmt.setInt(1, itemId);
	        return stmt.executeUpdate() > 0;

	    } catch (Exception e) {
	        e.printStackTrace();
	        return false;
	    }
	}

	public boolean addNewPatient(String id, String firstName, String lastName, String phone, String email,
            java.sql.Date dob, String identifier, String insurance, String policy) {

String sqlPersons = """
INSERT INTO TblPersons (PersonId, FirstName, LastName, PhoneNumber, Email, DateOfBirth)
VALUES (?, ?, ?, ?, ?, ?)
""";

String sqlPatients = """
INSERT INTO TblPatients (PatientId, Identifier, InsuranceProviderName, PolicyNumber)
VALUES (?, ?, ?, ?)
""";

try (Connection conn = DatabaseManager.getConnection()) {
try (PreparedStatement ps1 = conn.prepareStatement(sqlPersons);
PreparedStatement ps2 = conn.prepareStatement(sqlPatients)) {

ps1.setString(1, id);
ps1.setString(2, firstName);
ps1.setString(3, lastName);
ps1.setString(4, phone);
ps1.setString(5, email);
ps1.setDate(6, dob);
ps1.executeUpdate();

ps2.setString(1, id);
ps2.setString(2, identifier);
ps2.setString(3, insurance);
ps2.setString(4, policy);
ps2.executeUpdate();

return true;
}
} catch (Exception e) {
e.printStackTrace();
return false;
}
}


	
	public boolean patientIdExists(String id) {
	    String sql = "SELECT 1 FROM TblPatients WHERE PatientId = ?";
	    try (Connection conn = DatabaseManager.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(sql)) {
	        stmt.setString(1, id);
	        return stmt.executeQuery().next();
	    } catch (Exception e) {
	        e.printStackTrace();
	        return false;
	    }
	}

	public boolean identifierExists(String identifier) {
	    String sql = "SELECT 1 FROM TblPatients WHERE Identifier = ?";
	    try (Connection conn = DatabaseManager.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(sql)) {
	        stmt.setString(1, identifier);
	        return stmt.executeQuery().next();
	    } catch (Exception e) {
	        e.printStackTrace();
	        return false;
	    }
	}

	public boolean policyNumberExists(String policy) {
	    String sql = "SELECT 1 FROM TblPatients WHERE PolicyNumber = ?";
	    try (Connection conn = DatabaseManager.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(sql)) {
	        stmt.setString(1, policy);
	        return stmt.executeQuery().next();
	    } catch (Exception e) {
	        e.printStackTrace();
	        return false;
	    }
	}

	public List<String> getAllInsuranceProviders() {
	    List<String> providers = new ArrayList<>();
	    String sql = "SELECT ProviderName FROM TblInsurances";
	    try (Connection conn = DatabaseManager.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(sql);
	         ResultSet rs = stmt.executeQuery()) {
	        while (rs.next()) {
	            providers.add(rs.getString("ProviderName"));
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return providers;
	}
	
	public void updateStatus(int appointmentId, String newStatus) {
	    String sql = "UPDATE TblAppointments SET Status = ? WHERE AppointmentID = ?";
	    try (Connection conn = DatabaseManager.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(sql)) {
	        stmt.setString(1, newStatus);
	        stmt.setInt(2, appointmentId);
	        stmt.executeUpdate();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}

	public void updatePaidStatus(int appointmentId, boolean isPaid) {
	    String sql = "UPDATE TblAppointments SET IsPaid = ? WHERE AppointmentID = ?";
	    try (Connection conn = DatabaseManager.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(sql)) {
	        stmt.setBoolean(1, isPaid);
	        stmt.setInt(2, appointmentId);
	        stmt.executeUpdate();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}

	public boolean rescheduleDateOnly(int appointmentId, LocalDate newDate) {
	    String sql = "UPDATE TblAppointments SET AppointmentDate = ?, Status = 'Rescheduled' WHERE AppointmentID = ?";
	    try (Connection conn = DatabaseManager.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(sql)) {
	        stmt.setDate(1, Date.valueOf(newDate));
	        stmt.setInt(2, appointmentId);
	        stmt.executeUpdate();
	        return true;
	    } catch (Exception e) {
	        e.printStackTrace();
	        return false;
	    }
	}

	public ArrayList<Object[]> getDetailedAppointments(String filter) {
	    ArrayList<Object[]> list = new ArrayList<>();
	    String comparison = filter.equalsIgnoreCase("Upcoming") ? ">=" : "<";

	    String sql = """
	        SELECT A.AppointmentID, P.FirstName || ' ' || P.LastName AS PatientName,
	               A.AppointmentDate, A.AppointmentTime, A.TreatmentName, A.Cost,
	               A.Status, 
	               PS.FirstName || ' ' || PS.LastName AS StaffName,
	               A.ReasonForVisit, A.IsReminderSent, A.IsPaid, A.IsSterilized
	        FROM TblAppointments A
	        JOIN TblPatients Pa ON A.PatientID = Pa.PatientID
	        JOIN TblPersons P ON Pa.PatientID = P.PersonID
	        LEFT JOIN TblStaff S ON A.AssignedMedicalStaff = S.StaffId
	        LEFT JOIN TblPersons PS ON S.StaffId = PS.PersonId
	        WHERE A.AppointmentDate """ + comparison + " ?";

	    try (Connection conn = DatabaseManager.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(sql)) {

	        stmt.setDate(1, Date.valueOf(LocalDate.now()));
	        ResultSet rs = stmt.executeQuery();

	        while (rs.next()) {
	            list.add(new Object[]{
	                rs.getInt("AppointmentID"),
	                rs.getString("PatientName"),
	                rs.getDate("AppointmentDate").toLocalDate().toString(),
	                rs.getTime("AppointmentTime").toLocalTime().toString(),
	                rs.getString("TreatmentName"),
	                rs.getString("Cost"),
	                rs.getString("Status"),
	                rs.getString("StaffName"), 
	                rs.getString("ReasonForVisit"),
	                rs.getBoolean("IsReminderSent") ? "Yes" : "No",
	                rs.getBoolean("IsPaid") ? "Yes" : "No",
	                rs.getBoolean("IsSterilized") ? "Yes" : "No"
	            });
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	    }

	    return list;
	}


	
	
	
	public ArrayList<Object[]> getAppointmentsWithPatientNames(String filter) {
	    ArrayList<Object[]> rows = new ArrayList<>();

	    String baseSQL = """
	        SELECT A.AppointmentID, A.PatientID,
	               P.FirstName, P.LastName,
	               A.AppointmentDate, A.AppointmentTime,
	               A.TreatmentName, A.Status,
	               A.IsPaid, A.IsSterilized
	        FROM TblAppointments A
	        JOIN TblPersons P ON A.PatientID = P.PersonID
	        WHERE 1=1
	        """;

	    if ("Upcoming".equalsIgnoreCase(filter)) {
	        baseSQL += " AND A.AppointmentDate >= ?";
	    } else if ("Past".equalsIgnoreCase(filter)) {
	        baseSQL += " AND A.AppointmentDate < ?";
	    }

	    baseSQL += " ORDER BY A.AppointmentDate DESC, A.AppointmentTime DESC";

	    try (Connection conn = DatabaseManager.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(baseSQL)) {

	        if (!"All".equalsIgnoreCase(filter)) {
	            stmt.setDate(1, Date.valueOf(LocalDate.now()));
	        }

	        ResultSet rs = stmt.executeQuery();
	        while (rs.next()) {
	            int id = rs.getInt("AppointmentID");
	            String patientName = rs.getString("FirstName") + " " + rs.getString("LastName");
	            LocalDate date = rs.getDate("AppointmentDate").toLocalDate();
	            LocalTime time = rs.getTime("AppointmentTime").toLocalTime();
	            String treatment = rs.getString("TreatmentName");
	            String status = rs.getString("Status");
	            boolean isPaid = rs.getBoolean("IsPaid");
	            boolean isSterilized = rs.getBoolean("IsSterilized");

	            rows.add(new Object[]{
	                id,
	                patientName,
	                date.toString(),
	                time.toString(),
	                treatment,
	                status,
	                isPaid ? "Yes" : "No",
	                isSterilized ? "Yes" : "No"
	            });
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	    }

	    return rows;
	}
	public void updateInventoryQuantity(int itemId, String newQuantity) {
	    String sql = "UPDATE TblInventoryItems SET Quantity = ? WHERE ItemID = ?";
	    try (Connection conn = DatabaseManager.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(sql)) {
	        stmt.setString(1, newQuantity);
	        stmt.setInt(2, itemId);
	        stmt.executeUpdate();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}


	public ArrayList<InventoryItem> getAllInventoryItems() {
		ArrayList<InventoryItem> list = new ArrayList<>();
	    String sql = "SELECT ItemID, [Item Name], Description, Quantity, SupplierInformation, ExpirationDate, SerialNumber, LowStockAlertThreshold FROM TblInventoryItems";
	    
	    try (Connection conn = DatabaseManager.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(sql);
	         ResultSet rs = stmt.executeQuery()) {

	    	while (rs.next()) {
	    	    list.add(new InventoryItem(
	    	        rs.getInt("ItemID"),
	    	        rs.getString("Item Name"),
	    	        rs.getString("Description"),
	    	        rs.getInt("Quantity"),
	    	        rs.getString("SupplierInformation"),
	    	        rs.getDate("ExpirationDate") != null ? rs.getDate("ExpirationDate").toLocalDate() : null,
	    	        rs.getString("SerialNumber"),
	    	        rs.getInt("LowStockAlertThreshold")
	    	    ));
	    	}

	    } catch (Exception e) {
	        e.printStackTrace();
	    }

	    return list;
	}

	
	
	public ArrayList<Appointment> getUpcomingAppointments() {
	    ArrayList<Appointment> list = new ArrayList<>();

	    String sql = """
	        SELECT AppointmentID, PatientID, TreatmentPlanID, TreatmentName, Cost, Status,
	               AppointmentDate, AppointmentTime, IsPaid, IsSterilized
	        FROM TblAppointments
	        WHERE AppointmentDate >= ?
	        ORDER BY AppointmentDate, AppointmentTime
	    """;

        try (Connection conn = DatabaseManager.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(sql)) {

	        stmt.setDate(1, Date.valueOf(LocalDate.now()));
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

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }

	    return list;
	}
	
	
	 public ArrayList<Patient> getAllPatients() {
	        ArrayList<Patient> list = new ArrayList<>();
	        String sql = """
	            SELECT P.PersonId, P.FirstName, P.LastName, P.PhoneNumber, P.Email, P.DateOfBirth,
	                   Pt.InsuranceProviderName, Pt.PolicyNumber
	            FROM TblPersons P
	            JOIN TblPatients Pt ON Pt.PatientId = P.PersonId
	            """;

	        try (Connection conn = DatabaseManager.getConnection();
	             PreparedStatement stmt = conn.prepareStatement(sql);
	             ResultSet rs = stmt.executeQuery()) {

	            while (rs.next()) {
	                int id = rs.getInt("PersonId");
	                String name = rs.getString("FirstName") + " " + rs.getString("LastName");
	                String phone = rs.getString("PhoneNumber");
	                String email = rs.getString("Email");
	                int age = Period.between(rs.getDate("DateOfBirth").toLocalDate(), LocalDate.now()).getYears();
	                String insurance = rs.getString("InsuranceProviderName");
	                String policy = rs.getString("PolicyNumber");

	                list.add(new Patient(id, name, phone, email, age, insurance, policy));
	            }

	        } catch (Exception e) {
	            e.printStackTrace();
	        }

	        return list;
	    }

	    public ArrayList<String> getAllTreatmentNames() {
	        ArrayList<String> list = new ArrayList<>();
	        String sql = "SELECT TreatmentName FROM TblTreatments";

	        try (Connection conn = DatabaseManager.getConnection();
	             PreparedStatement stmt = conn.prepareStatement(sql);
	             ResultSet rs = stmt.executeQuery()) {

	            while (rs.next()) {
	                list.add(rs.getString("TreatmentName"));
	            }

	        } catch (Exception e) {
	            e.printStackTrace();
	        }

	        return list;
	    }

	    public List<StaffMember> getAllStaffMembers() {
	        List<StaffMember> allStaff = new ArrayList<>();

	        String sql = """
	            SELECT S.StaffId, P.FirstName, P.LastName, S.Role
	            FROM TblStaff S
	            JOIN TblPersons P ON P.PersonId = S.StaffId
	        """;

	        try (Connection conn = DatabaseManager.getConnection();
	             PreparedStatement stmt = conn.prepareStatement(sql);
	             ResultSet rs = stmt.executeQuery()) {

	            while (rs.next()) {
	                String id = rs.getString("StaffId");
	                String firstName = rs.getString("FirstName") ;
	                String lastName = rs.getString("LastName");
	                String role = rs.getString("Role");

	                allStaff.add(new StaffMember(id, firstName,lastName, role));
	            }

	        } catch (Exception e) {
	            e.printStackTrace();
	        }

	        return allStaff;
	    }



	    public boolean bookAppointment(String patientId, String treatmentName, String staffId, LocalDate date, LocalTime time, double cost) {
	        String sql = """
	            INSERT INTO TblAppointments 
	            (PatientId, TreatmentName, AssignedMedicalStaff, AppointmentDate, AppointmentTime, Status, Cost, IsPaid, IsReminderSent, IsSterilized)
	            VALUES (?, ?, ?, ?, ?, 'Active', '?', false, false, false)
	            """;

	        try (Connection conn = DatabaseManager.getConnection();
	             PreparedStatement stmt = conn.prepareStatement(sql)) {

	            stmt.setString(1, patientId);
	            stmt.setString(2, treatmentName);
	            stmt.setString(3, staffId);
	            stmt.setDate(4, Date.valueOf(date));
	            stmt.setTime(5, Time.valueOf(time));
	            stmt.setDouble(6, cost); 

	            return stmt.executeUpdate() > 0;

	        } catch (Exception e) {
	            e.printStackTrace();
	            return false;
	        }
	    }
	
    public HashMap<Patient, ArrayList<Appointment>> getActivePlansWithTreatments() {
        HashMap<Patient, ArrayList<Appointment>> map = new HashMap<>();

        String sql = """
        	    SELECT P.PersonId, P.FirstName, P.LastName, P.PhoneNumber, P.Email, P.DateOfBirth,
       PT.InsuranceProviderName, PT.PolicyNumber,
       TP.TreatmentPlanID,
       A.AppointmentID, A.TreatmentName, A.Cost, A.Status,
       A.AppointmentDate, A.AppointmentTime, A.IsPaid, A.IsSterilized
FROM TblTreatmentPlans TP
JOIN TblPatients PT ON TP.PatientID = PT.PatientId
JOIN TblPersons P ON PT.PatientId = P.PersonId
JOIN TblAppointments A ON A.TreatmentPlanID = TP.TreatmentPlanID
WHERE TP.Status = 'Active'

        	    """;


        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("PersonId");
                String name = rs.getString("FirstName") + " " + rs.getString("LastName");
                String phone = rs.getString("PhoneNumber");
                String email = rs.getString("Email");
                LocalDate dob = rs.getDate("DateOfBirth").toLocalDate();
                int age = Period.between(dob, LocalDate.now()).getYears();
                String insuranceProvider = rs.getString("InsuranceProviderName");
                String policyNumber = rs.getString("PolicyNumber");

                Patient patient = new Patient(id, name, phone, email, age, insuranceProvider, policyNumber);

                int appointmentId = rs.getInt("AppointmentID");
                int treatmentPlanId = rs.getInt("TreatmentPlanID");
                String treatmentName = rs.getString("TreatmentName");
                double cost = rs.getDouble("Cost");
                String status = rs.getString("Status");
                LocalDate date = rs.getDate("AppointmentDate").toLocalDate();
                LocalTime time = rs.getTime("AppointmentTime").toLocalTime();
                boolean paid = rs.getBoolean("IsPaid");
                boolean sterilized = rs.getBoolean("IsSterilized");

                Appointment appointment = new Appointment(appointmentId, id, treatmentPlanId, treatmentName, cost, status, date, time, paid, sterilized);
                map.computeIfAbsent(patient, k -> new ArrayList<>()).add(appointment);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return map;
    }

   
    public ArrayList<LocalDate> getUpcomingDateOptions() {
        ArrayList<LocalDate> dates = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 14; i++) { // show 2 weeks of options
            dates.add(today.plusDays(i));
        }
        return dates;
    }

    public ArrayList<LocalTime> getAvailableTimeSlots(LocalDate date, boolean isUrgent, int durationMinutes) {
        ArrayList<LocalTime> availableSlots = new ArrayList<>();

        if (isUrgent) {
            // For urgent cases, return the current time slot immediately
            availableSlots.add(LocalTime.now().withSecond(0).withNano(0));
            return availableSlots;
        }

        // Define working hours: 8 AM to 5 PM
        LocalTime opening = LocalTime.of(8, 0);
        LocalTime closing = LocalTime.of(17, 0);

        ArrayList<LocalTime> occupied = new ArrayList<>();

        String sql = "SELECT AppointmentTime FROM TblAppointments WHERE AppointmentDate = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(date));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                occupied.add(rs.getTime("AppointmentTime").toLocalTime());
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Generate available slots in 30-minute increments, for example
        for (LocalTime time = opening; 
             !time.plusMinutes(durationMinutes).isAfter(closing); 
             time = time.plusMinutes(30)) {

            boolean isOccupied = false;
            for (LocalTime busy : occupied) {
                if (time.equals(busy)) {
                    isOccupied = true;
                    break;
                }
            }

            if (!isOccupied) {
                availableSlots.add(time);
            }
        }

        return availableSlots;
    }


    public void markAsPaid(int appointmentId) {
        String sql = "UPDATE TblAppointments SET IsPaid = true WHERE AppointmentID = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, appointmentId);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void markAsSterilized(int appointmentId) {
        String sql = "UPDATE TblAppointments SET IsSterilized = true WHERE AppointmentID = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, appointmentId);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void cancelAppointment(int appointmentId) {
        String sql = "UPDATE TblAppointments SET Status = 'Cancelled' WHERE AppointmentID = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, appointmentId);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean rescheduleAppointment(int appointmentId, LocalDate newDate, LocalTime newTime) {
        String sql = "UPDATE TblAppointments SET AppointmentDate = ?, AppointmentTime = ?, Status = 'Rescheduled' WHERE AppointmentID = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(newDate));
            stmt.setTime(2, Time.valueOf(newTime));
            stmt.setInt(3, appointmentId);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public Appointment getAppointmentById(int id) {
        String sql = """
            SELECT AppointmentID, PatientID, TreatmentPlanID, TreatmentName, Cost, Status,
                   AppointmentDate, AppointmentTime, IsPaid, IsSterilized
            FROM TblAppointments
            WHERE AppointmentID = ?
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Appointment(
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
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
