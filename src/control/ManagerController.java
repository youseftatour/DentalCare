package control;

import utils.DatabaseManager;
import utils.InventoryParser;
import entity.Appointment;
import entity.InventoryItem;
import entity.Patient;
import entity.Supplier;
import entity.TreatmentPlan;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.view.JasperViewer;

import java.io.File;
import java.io.InputStream;
import java.sql.*;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ManagerController {
    public boolean personIdExists(String id) {
        String sql = "SELECT 1 FROM TblPersons WHERE PersonId = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            return stmt.executeQuery().next();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public ArrayList<TreatmentPlan> getAllTreatmentPlans() {
        ArrayList<TreatmentPlan> plans = new ArrayList<>();
        String sql = "SELECT * FROM TblTreatmentPlans";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                TreatmentPlan plan = new TreatmentPlan(
                    rs.getInt("TreatmentPlanID"),
                    rs.getDate("StartDate").toLocalDate(),
                    rs.getDate("EstimatedCompletionDate") != null ? rs.getDate("EstimatedCompletionDate").toLocalDate() : null,
                    rs.getString("PatientID"),
                    rs.getString("Status"),
                    rs.getString("CreatedByDentist")
                );
                plans.add(plan);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return plans;
    }

    public boolean addTreatmentPlan(int patientId, String status, LocalDate startDate, LocalDate estimatedEndDate, String createdByDentist) {
        String sql = """
            INSERT INTO TblTreatmentPlans (PatientId, Status, StartDate, EstimatedCompletionDate, CreatedByDentist)
            VALUES (?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, patientId);
            stmt.setString(2, status);
            stmt.setDate(3, Date.valueOf(startDate));
            stmt.setDate(4, Date.valueOf(estimatedEndDate));
            stmt.setString(5, createdByDentist);

            return stmt.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public boolean updateTreatmentPlan(int planId, String status, String notes) {
        String sql = """
            UPDATE TblTreatmentPlans
            SET Status = ?
            WHERE TreatmentPlanID = ?
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setInt(2, planId);

            return stmt.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteTreatmentPlan(int planId) {
        String sql = "DELETE FROM TblTreatmentPlans WHERE TreatmentPlanId = ?";
 
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, planId);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        

    }


    public boolean deleteStaffMember(String id) {
        String deleteStaff = "DELETE FROM TblStaff WHERE StaffId = ?";
        String deletePerson = "DELETE FROM TblPersons WHERE PersonId = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps1 = conn.prepareStatement(deleteStaff);
             PreparedStatement ps2 = conn.prepareStatement(deletePerson)) {

            ps1.setString(1, id);
            ps1.executeUpdate();

            ps2.setString(1, id);
            ps2.executeUpdate();

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean editStaffMember(String id, String firstName, String lastName, String phone, String email,
                                   java.sql.Date dob, String qualifications, String specialization, String role) {

        String updatePerson = """
            UPDATE TblPersons 
            SET FirstName = ?, LastName = ?, PhoneNumber = ?, Email = ?, DateOfBirth = ?
            WHERE PersonId = ?
        """;

        String updateStaff = """
            UPDATE TblStaff 
            SET Qualifications = ?, Specialization = ?, Role = ?
            WHERE StaffId = ?
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps1 = conn.prepareStatement(updatePerson);
             PreparedStatement ps2 = conn.prepareStatement(updateStaff)) {

            ps1.setString(1, firstName);
            ps1.setString(2, lastName);
            ps1.setString(3, phone);
            ps1.setString(4, email);
            ps1.setDate(5, dob);
            ps1.setString(6, id);
            ps1.executeUpdate();

            ps2.setString(1, qualifications);
            ps2.setString(2, specialization);
            ps2.setString(3, role);
            ps2.setString(4, id);
            ps2.executeUpdate();

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean addStaffMember(String id, String firstName, String lastName, String phone, String email,
                                  java.sql.Date dob, String qualifications, String specialization, String role) {

        String sqlPerson = """
            INSERT INTO TblPersons (PersonId, FirstName, LastName, PhoneNumber, Email, DateOfBirth)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        String sqlStaff = """
            INSERT INTO TblStaff (StaffId, Qualifications, Specialization, Role)
            VALUES (?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps1 = conn.prepareStatement(sqlPerson);
             PreparedStatement ps2 = conn.prepareStatement(sqlStaff)) {

            ps1.setString(1, id);
            ps1.setString(2, firstName);
            ps1.setString(3, lastName);
            ps1.setString(4, phone);
            ps1.setString(5, email);
            ps1.setDate(6, dob);
            ps1.executeUpdate();

            ps2.setString(1, id);
            ps2.setString(2, qualifications);
            ps2.setString(3, specialization);
            ps2.setString(4, role);
            ps2.executeUpdate();

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public ArrayList<Object[]> getAllStaff() {
        ArrayList<Object[]> staffList = new ArrayList<>();

        String sql = """
            SELECT S.StaffId, P.FirstName, P.LastName, P.PhoneNumber, P.Email, P.DateOfBirth,
                   S.Qualifications, S.Specialization, S.Role
            FROM TblStaff S
            JOIN TblPersons P ON P.PersonId = S.StaffId
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                staffList.add(new Object[]{
                    rs.getString("StaffId"),
                    rs.getString("FirstName"),
                    rs.getString("LastName"),
                    rs.getString("PhoneNumber"),
                    rs.getString("Email"),
                    rs.getDate("DateOfBirth") != null ? rs.getDate("DateOfBirth").toString() : "",
                    rs.getString("Qualifications"),
                    rs.getString("Specialization"),
                    rs.getString("Role")
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return staffList;
    }

    public void updateInventoryQuantity(int itemId, String newQty) {
        String sql = "UPDATE TblInventoryItems SET Quantity = ? WHERE ItemID = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newQty);
            stmt.setInt(2, itemId);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateLowStockThreshold(int itemId, int newThreshold) {
        String sql = "UPDATE TblInventoryItems SET LowStockAlertThreshold = ? WHERE ItemID = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, newThreshold);
            stmt.setInt(2, itemId);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<InventoryItem> getAllInventoryItems() {
        ArrayList<InventoryItem> list = new ArrayList<>();
        String sql = """
            SELECT ItemID, [Item Name], Description, Quantity, SupplierInformation,
                   ExpirationDate, SerialNumber, LowStockAlertThreshold
            FROM TblInventoryItems
        """;

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

    public double getMonthlyRevenue(int month, int year) {
        String sql = """
            SELECT SUM(Cost) AS Total
            FROM TblAppointments
            WHERE MONTH(AppointmentDate) = ? AND YEAR(AppointmentDate) = ? AND Status = 'Completed'
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, month);
            stmt.setInt(2, year);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("Total");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    public ArrayList<Appointment> getCompletedAppointmentsForMonth(int month, int year) {
        ArrayList<Appointment> list = new ArrayList<>();

        String sql = """
            SELECT AppointmentID, PatientID, TreatmentPlanID, TreatmentName, Cost, Status,
                   AppointmentDate, AppointmentTime, IsPaid, IsSterilized
            FROM TblAppointments
            WHERE MONTH(AppointmentDate) = ? AND YEAR(AppointmentDate) = ? AND Status = 'Completed'
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, month);
            stmt.setInt(2, year);
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
    public ArrayList<Patient> getAllPatients() {
        ArrayList<Patient> list = new ArrayList<>();
        String sql = """
            SELECT 
                p.PersonId,
                p.FirstName,
                p.LastName,
                p.PhoneNumber,
                p.Email,
                p.DateOfBirth,
                pat.InsuranceProviderName,
                pat.PolicyNumber
            FROM TblPersons p
            JOIN TblPatients pat ON p.PersonId = pat.PatientId
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("PersonId");
                String fullName = rs.getString("FirstName") + " " + rs.getString("LastName");
                String phone = rs.getString("PhoneNumber");
                String email = rs.getString("Email");

                LocalDate dob = rs.getDate("DateOfBirth").toLocalDate();
                int age = Period.between(dob, LocalDate.now()).getYears();

                String insuranceProvider = rs.getString("InsuranceProviderName");
                String policyNumber = rs.getString("PolicyNumber");

                list.add(new Patient(id, fullName, phone, email, age, insuranceProvider, policyNumber));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }




    public boolean addTreatmentPlan(String patientId, LocalDate startDate, LocalDate estimatedEndDate, String createdBy) {
        String sql = """
            INSERT INTO TblTreatmentPlans (PatientID, StartDate, EstimatedCompletionDate, Status, CreatedByDentist)
            VALUES (?, ?, ?, 'Active', ?)
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, patientId);
            stmt.setDate(2, Date.valueOf(startDate));
            stmt.setDate(3, Date.valueOf(estimatedEndDate));
            stmt.setString(4, createdBy);

            return stmt.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void generateRevenueReport(String month, String year) {
        try {
            // Path to compiled .jasper file inside src/boundary
            InputStream reportStream = getClass().getResourceAsStream("/boundary/MonthlyRevenueReport.jasper");

            if (reportStream == null) {
                throw new RuntimeException("Report file not found at /boundary/MonthlyRevenueReport.jasper");
            }

            // Set up parameters
            HashMap<String, Object> parameters = new HashMap<>();
            parameters.put("reportMonth", month);
            parameters.put("reportYear", year);

        
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
            Connection conn = utils.DatabaseManager.getConnection();

            // Fill and view the report
            JasperPrint jasperPrint = JasperFillManager.fillReport(reportStream, parameters, conn);
            JasperViewer.viewReport(jasperPrint, false);

            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
            javax.swing.JOptionPane.showMessageDialog(null,
                "Failed to generate report: " + e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    public void generateTreatmentProgressReport(String managerId) {
        try {
            HashMap<String, Object> params = new HashMap<>();
            params.put("DentistID", managerId);

            InputStream is = getClass().getResourceAsStream("/boundary/TreatmentProgressReport.jasper");
            System.out.println("is null? " + (is == null));
            
            JasperPrint print = JasperFillManager.fillReport(
                    is,
                    params,
                    DatabaseManager.getConnection()
            );
            JasperViewer.viewReport(print, false);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to generate report: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void generateInventoryUsageReport(java.util.Date startDate, java.util.Date endDate) {
        try {
            HashMap<String, Object> params = new HashMap<>();
            params.put("StartDate", new java.sql.Date(startDate.getTime()));
            params.put("EndDate", new java.sql.Date(endDate.getTime()));

            InputStream reportStream = ManagerController.class.getResourceAsStream("/boundary/InventoryUsageReport.jasper");

            if (reportStream == null) {
                throw new RuntimeException("Report file not found.");
            }

            JasperPrint print = JasperFillManager.fillReport(
                    reportStream,
                    params,
                    DatabaseManager.getConnection()
            );

            JasperViewer viewer = new JasperViewer(print, false);
            viewer.setTitle("Inventory Usage Report");
            viewer.setVisible(true);

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error generating report: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean importInventoryFromXML(File xmlFile) {
        try (Connection conn = DatabaseManager.getConnection()) {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            NodeList supplierList = doc.getElementsByTagName("Supplier");
            for (int i = 0; i < supplierList.getLength(); i++) {
                Element supplier = (Element) supplierList.item(i);
                String supplierName = supplier.getElementsByTagName("SupplierName").item(0).getTextContent();
                String email = supplier.getElementsByTagName("Email").item(0).getTextContent();
                String phone = supplier.getElementsByTagName("Phone").item(0).getTextContent();
                String address = supplier.getElementsByTagName("Address").item(0).getTextContent();

                // Insert supplier if not exists
                insertSupplierIfNotExists(conn, supplierName, email, phone, address);

                NodeList itemList = supplier.getElementsByTagName("InventoryItem");
                for (int j = 0; j < itemList.getLength(); j++) {
                    Element item = (Element) itemList.item(j);

                    String itemName = item.getElementsByTagName("Name").item(0).getTextContent();
                    String description = item.getElementsByTagName("Description").item(0).getTextContent();
                    int quantity = Integer.parseInt(item.getElementsByTagName("Quantity").item(0).getTextContent());
                    String serial = item.getElementsByTagName("SerialNumber").item(0).getTextContent();
                    int threshold = Integer.parseInt(item.getElementsByTagName("LowStockAlertThreshold").item(0).getTextContent());
                    String expStr = item.getElementsByTagName("ExpirationDate").item(0).getTextContent();
                    LocalDate expiry = LocalDate.parse(expStr);

                    // Insert inventory item
                    PreparedStatement stmt = conn.prepareStatement("""
                    	    INSERT INTO TblInventoryItems ([Item Name], Description, Quantity, SupplierInformation, ExpirationDate, SerialNumber, LowStockAlertThreshold)
                    	    VALUES (?, ?, ?, ?, ?, ?, ?)
                    	""");
                    stmt.setString(1, itemName);
                    stmt.setString(2, description);
                    stmt.setString(3, String.valueOf(quantity));
                    stmt.setString(4, supplierName);
                    stmt.setString(5, expiry.toString());
                    stmt.setString(6, serial);
                    stmt.setString(7, String.valueOf(threshold));
                    int result = stmt.executeUpdate();
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void insertSupplierIfNotExists(Connection conn, String name, String email, String phone, String address) throws Exception {
        PreparedStatement check = conn.prepareStatement(
            "SELECT 1 FROM TblProviders WHERE SupplierName = ?");
        check.setString(1, name);
        ResultSet rs = check.executeQuery();

        if (!rs.next()) {
            PreparedStatement insert = conn.prepareStatement("""
                INSERT INTO TblProviders (SupplierName, Email, Phone, Address) VALUES (?, ?, ?, ?)
            """);
            insert.setString(1, name);
            insert.setString(2, email);
            insert.setString(3, phone);
            insert.setString(4, address);
            insert.executeUpdate();
        }
    }
    public Map<String, Supplier> parseSuppliersWithItems(File file) {
        return InventoryParser.parseSuppliersWithItems(file);
    }


		
	
    
}
