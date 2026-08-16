package control;

import entity.Appointment;
import entity.InventoryItem;
import entity.Patient;
import entity.Supplier;
import entity.TreatmentPlan;
import repository.InventoryRepository;
import repository.PatientRepository;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.view.JasperViewer;
import utils.DatabaseManager;
import utils.InventoryParser;
import utils.TransactionUtils;
import service.DomainValidator;

import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ManagerController {
    private final InventoryRepository inventoryRepository = new InventoryRepository();
    private final PatientRepository patientRepository = new PatientRepository();

    public boolean personIdExists(String id) {
        String sql = "SELECT 1 FROM TblPersons WHERE PersonId = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }

        } catch (Exception e) {
            utils.AppLogger.error(ManagerController.class, "Manager database or report operation failed", e);
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
                Date start = rs.getDate("StartDate");
                Date estimatedEnd = rs.getDate("EstimatedCompletionDate");

                plans.add(new TreatmentPlan(
                    rs.getInt("TreatmentPlanID"),
                    start != null ? start.toLocalDate() : null,
                    estimatedEnd != null ? estimatedEnd.toLocalDate() : null,
                    rs.getString("PatientID"),
                    rs.getString("Status"),
                    rs.getString("CreatedByDentist")
                ));
            }

        } catch (SQLException e) {
            utils.AppLogger.error(ManagerController.class, "Manager database or report operation failed", e);
        }

        return plans;
    }

    public boolean addTreatmentPlan(int patientId, String status, LocalDate startDate,
                                    LocalDate estimatedEndDate, String createdByDentist) {

        if (!DomainValidator.isValidTreatmentPlan(startDate, estimatedEndDate)
                || status == null || status.isBlank()) {
            return false;
        }

        String sql = """
            INSERT INTO TblTreatmentPlans
            (PatientId, Status, StartDate, EstimatedCompletionDate, CreatedByDentist)
            VALUES (?, ?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, patientId);
            stmt.setString(2, status);
            stmt.setDate(3, Date.valueOf(startDate));

            if (estimatedEndDate != null) {
                stmt.setDate(4, Date.valueOf(estimatedEndDate));
            } else {
                stmt.setNull(4, Types.DATE);
            }

            stmt.setString(5, createdByDentist);
            return stmt.executeUpdate() > 0;

        } catch (Exception e) {
            utils.AppLogger.error(ManagerController.class, "Manager database or report operation failed", e);
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
            utils.AppLogger.error(ManagerController.class, "Manager database or report operation failed", e);
            return false;
        }
    }

    public boolean deleteTreatmentPlan(int planId) {
        String sql = "DELETE FROM TblTreatmentPlans WHERE TreatmentPlanId = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, planId);
            return stmt.executeUpdate() > 0;

        } catch (Exception e) {
            utils.AppLogger.error(ManagerController.class, "Manager database or report operation failed", e);
            return false;
        }
    }

    public boolean deleteStaffMember(String id) {
        String deleteStaff = "DELETE FROM TblStaff WHERE StaffId = ?";
        String deletePerson = "DELETE FROM TblPersons WHERE PersonId = ?";

        Connection conn = null;

        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement ps1 = conn.prepareStatement(deleteStaff);
                 PreparedStatement ps2 = conn.prepareStatement(deletePerson)) {

                ps1.setString(1, id);
                int deletedStaff = ps1.executeUpdate();

                ps2.setString(1, id);
                int deletedPerson = ps2.executeUpdate();

                if (deletedStaff != 1 || deletedPerson != 1) {
                    conn.rollback();
                    return false;
                }

                conn.commit();
                return true;
            }

        } catch (Exception e) {
            rollbackQuietly(conn);
            utils.AppLogger.error(ManagerController.class, "Manager database or report operation failed", e);
            return false;
        } finally {
            closeQuietly(conn);
        }
    }

    public boolean editStaffMember(String id, String firstName, String lastName, String phone, String email,
                                   Date dob, String qualifications, String specialization, String role) {

        if (DomainValidator.validateStaff(id, firstName, lastName, phone, email, dob,
                qualifications, specialization, role) != null) {
            return false;
        }

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

        Connection conn = null;

        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement ps1 = conn.prepareStatement(updatePerson);
                 PreparedStatement ps2 = conn.prepareStatement(updateStaff)) {

                ps1.setString(1, firstName);
                ps1.setString(2, lastName);
                ps1.setString(3, phone);
                ps1.setString(4, email);
                ps1.setDate(5, dob);
                ps1.setString(6, id);

                ps2.setString(1, qualifications);
                ps2.setString(2, specialization);
                ps2.setString(3, role);
                ps2.setString(4, id);

                if (ps1.executeUpdate() != 1 || ps2.executeUpdate() != 1) {
                    conn.rollback();
                    return false;
                }

                conn.commit();
                return true;
            }

        } catch (Exception e) {
            rollbackQuietly(conn);
            utils.AppLogger.error(ManagerController.class, "Manager database or report operation failed", e);
            return false;
        } finally {
            closeQuietly(conn);
        }
    }

    public boolean addStaffMember(String id, String firstName, String lastName, String phone, String email,
                                  Date dob, String qualifications, String specialization, String role) {

        if (DomainValidator.validateStaff(id, firstName, lastName, phone, email, dob,
                qualifications, specialization, role) != null || personIdExists(id)) {
            return false;
        }

        String sqlPerson = """
            INSERT INTO TblPersons (PersonId, FirstName, LastName, PhoneNumber, Email, DateOfBirth)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        String sqlStaff = """
            INSERT INTO TblStaff (StaffId, Qualifications, Specialization, Role)
            VALUES (?, ?, ?, ?)
            """;

        Connection conn = null;

        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement ps1 = conn.prepareStatement(sqlPerson);
                 PreparedStatement ps2 = conn.prepareStatement(sqlStaff)) {

                ps1.setString(1, id);
                ps1.setString(2, firstName);
                ps1.setString(3, lastName);
                ps1.setString(4, phone);
                ps1.setString(5, email);
                ps1.setDate(6, dob);

                ps2.setString(1, id);
                ps2.setString(2, qualifications);
                ps2.setString(3, specialization);
                ps2.setString(4, role);

                if (ps1.executeUpdate() != 1 || ps2.executeUpdate() != 1) {
                    conn.rollback();
                    return false;
                }

                conn.commit();
                return true;
            }

        } catch (Exception e) {
            rollbackQuietly(conn);
            utils.AppLogger.error(ManagerController.class, "Manager database or report operation failed", e);
            return false;
        } finally {
            closeQuietly(conn);
        }
    }

    public ArrayList<Object[]> getAllStaff() {
        ArrayList<Object[]> staffList = new ArrayList<>();

        String sql = """
            SELECT S.StaffId, P.FirstName, P.LastName, P.PhoneNumber, P.Email, P.DateOfBirth,
                   S.Qualifications, S.Specialization, S.Role
            FROM TblStaff S
            INNER JOIN TblPersons P ON P.PersonId = S.StaffId
            ORDER BY P.LastName, P.FirstName
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Date dob = rs.getDate("DateOfBirth");

                staffList.add(new Object[]{
                    rs.getString("StaffId"),
                    rs.getString("FirstName"),
                    rs.getString("LastName"),
                    rs.getString("PhoneNumber"),
                    rs.getString("Email"),
                    dob != null ? dob.toString() : "",
                    rs.getString("Qualifications"),
                    rs.getString("Specialization"),
                    rs.getString("Role")
                });
            }

        } catch (Exception e) {
            utils.AppLogger.error(ManagerController.class, "Manager database or report operation failed", e);
        }

        return staffList;
    }

    public void updateInventoryQuantity(int itemId, String newQty) {
        int quantity;

        try {
            quantity = Integer.parseInt(newQty);
        } catch (NumberFormatException e) {
            utils.AppLogger.warn(ManagerController.class, "Invalid inventory quantity: {}", newQty);
            return;
        }

        if (quantity < 0) {
            utils.AppLogger.warn(ManagerController.class, "Inventory quantity cannot be negative: {}", quantity);
            return;
        }

        try {
            inventoryRepository.updateQuantity(itemId, quantity);
        } catch (SQLException e) {
            utils.AppLogger.error(ManagerController.class, "Manager database or report operation failed", e);
        }
    }

    public void updateLowStockThreshold(int itemId, int newThreshold) {
        if (newThreshold < 0) {
            return;
        }

        try {
            inventoryRepository.updateLowStockThreshold(itemId, newThreshold);
        } catch (SQLException e) {
            utils.AppLogger.error(ManagerController.class, "Manager database or report operation failed", e);
        }
    }

    public ArrayList<InventoryItem> getAllInventoryItems() {
        try {
            return inventoryRepository.findAll();
        } catch (SQLException e) {
            utils.AppLogger.error(ManagerController.class, "Manager database or report operation failed", e);
            return new ArrayList<>();
        }
    }

    public boolean addInventoryItem(InventoryItem item) {
        if (!DomainValidator.isValidInventoryItem(item)) {
            return false;
        }

        try {
            return inventoryRepository.insert(item);
        } catch (SQLException e) {
            utils.AppLogger.error(ManagerController.class, "Manager database or report operation failed", e);
            return false;
        }
    }

    public boolean deleteInventoryItem(int itemId) {
        try {
            return inventoryRepository.deleteById(itemId);
        } catch (SQLException e) {
            utils.AppLogger.error(ManagerController.class, "Manager database or report operation failed", e);
            return false;
        }
    }

    public double getMonthlyRevenue(int month, int year) {
        String sql = """
            SELECT SUM(Cost) AS Total
            FROM TblAppointments
            WHERE MONTH(AppointmentDate) = ?
              AND YEAR(AppointmentDate) = ?
              AND Status = 'Completed'
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, month);
            stmt.setInt(2, year);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("Total");
                }
            }

        } catch (Exception e) {
            utils.AppLogger.error(ManagerController.class, "Manager database or report operation failed", e);
        }

        return 0;
    }

    public ArrayList<Appointment> getCompletedAppointmentsForMonth(int month, int year) {
        ArrayList<Appointment> list = new ArrayList<>();

        String sql = """
            SELECT AppointmentID, PatientID, TreatmentPlanID, TreatmentName, Cost, Status,
                   AppointmentDate, AppointmentTime, IsPaid, IsSterilized
            FROM TblAppointments
            WHERE MONTH(AppointmentDate) = ?
              AND YEAR(AppointmentDate) = ?
              AND Status = 'Completed'
            ORDER BY AppointmentDate, AppointmentTime
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, month);
            stmt.setInt(2, year);

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
            utils.AppLogger.error(ManagerController.class, "Manager database or report operation failed", e);
        }

        return list;
    }

    public ArrayList<Patient> getAllPatients() {
        try {
            return patientRepository.findAll();
        } catch (SQLException e) {
            utils.AppLogger.error(ManagerController.class, "Manager database or report operation failed", e);
            return new ArrayList<>();
        }
    }

    public boolean addTreatmentPlan(String patientId, LocalDate startDate,
                                    LocalDate estimatedEndDate, String createdBy) {

        if (!DomainValidator.isValidId(patientId)
                || !DomainValidator.isValidTreatmentPlan(startDate, estimatedEndDate)) {
            return false;
        }

        String sql = """
            INSERT INTO TblTreatmentPlans
            (PatientID, StartDate, EstimatedCompletionDate, Status, CreatedByDentist)
            VALUES (?, ?, ?, 'Active', ?)
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, patientId);
            stmt.setDate(2, Date.valueOf(startDate));

            if (estimatedEndDate != null) {
                stmt.setDate(3, Date.valueOf(estimatedEndDate));
            } else {
                stmt.setNull(3, Types.DATE);
            }

            stmt.setString(4, createdBy);
            return stmt.executeUpdate() > 0;

        } catch (Exception e) {
            utils.AppLogger.error(ManagerController.class, "Manager database or report operation failed", e);
            return false;
        }
    }

    public boolean generateRevenueReport(String month, String year) {
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("reportMonth", month);
        parameters.put("reportYear", year);

        try (InputStream reportStream =
                     getClass().getResourceAsStream("/boundary/MonthlyRevenueReport.jasper");
             Connection conn = DatabaseManager.getConnection()) {

            if (reportStream == null) {
                throw new IllegalStateException(
                    "Report file not found: /boundary/MonthlyRevenueReport.jasper"
                );
            }

            JasperPrint jasperPrint = JasperFillManager.fillReport(reportStream, parameters, conn);
            JasperViewer.viewReport(jasperPrint, false);
            return true;

        } catch (Exception e) {
            utils.AppLogger.error(ManagerController.class, "Manager database or report operation failed", e);
            return false;
        }
    }

    public boolean generateTreatmentProgressReport(String managerId) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("DentistID", managerId);

        try (InputStream reportStream =
                     getClass().getResourceAsStream("/boundary/TreatmentProgressReport.jasper");
             Connection conn = DatabaseManager.getConnection()) {

            if (reportStream == null) {
                throw new IllegalStateException(
                    "Report file not found: /boundary/TreatmentProgressReport.jasper"
                );
            }

            JasperPrint print = JasperFillManager.fillReport(reportStream, params, conn);
            JasperViewer.viewReport(print, false);
            return true;

        } catch (Exception e) {
            utils.AppLogger.error(ManagerController.class, "Manager database or report operation failed", e);
            return false;
        }
    }

    public static boolean generateInventoryUsageReport(java.util.Date startDate, java.util.Date endDate) {
        if (startDate == null || endDate == null || endDate.before(startDate)) {
            return false;
        }

        HashMap<String, Object> params = new HashMap<>();
        params.put("StartDate", new Date(startDate.getTime()));
        params.put("EndDate", new Date(endDate.getTime()));

        try (InputStream reportStream =
                     ManagerController.class.getResourceAsStream("/boundary/InventoryUsageReport.jasper");
             Connection conn = DatabaseManager.getConnection()) {

            if (reportStream == null) {
                throw new IllegalStateException(
                    "Report file not found: /boundary/InventoryUsageReport.jasper"
                );
            }

            JasperPrint print = JasperFillManager.fillReport(reportStream, params, conn);

            JasperViewer viewer = new JasperViewer(print, false);
            viewer.setTitle("Inventory Usage Report");
            viewer.setVisible(true);
            return true;

        } catch (Exception e) {
            utils.AppLogger.error(ManagerController.class, "Manager database or report operation failed", e);
            return false;
        }
    }

    public boolean importInventoryFromXML(File xmlFile) {
        Map<String, Supplier> suppliers = InventoryParser.parseSuppliersWithItems(xmlFile);

        if (suppliers.isEmpty()) {
            return false;
        }

        Connection conn = null;

        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);

            for (Supplier supplier : suppliers.values()) {
                insertSupplierIfNotExists(
                    conn,
                    supplier.getName(),
                    supplier.getEmail(),
                    supplier.getPhone(),
                    supplier.getAddress()
                );

                for (InventoryItem item : supplier.getItems()) {
                    insertInventoryItem(conn, item);
                }
            }

            conn.commit();
            return true;

        } catch (Exception e) {
            rollbackQuietly(conn);
            utils.AppLogger.error(ManagerController.class, "Manager database or report operation failed", e);
            return false;
        } finally {
            closeQuietly(conn);
        }
    }

    private void insertSupplierIfNotExists(Connection conn, String name, String email,
                                           String phone, String address) throws SQLException {

        String checkSql = "SELECT 1 FROM TblProviders WHERE SupplierName = ?";
        String insertSql = """
            INSERT INTO TblProviders (SupplierName, Email, Phone, Address)
            VALUES (?, ?, ?, ?)
            """;

        try (PreparedStatement check = conn.prepareStatement(checkSql)) {
            check.setString(1, name);

            try (ResultSet rs = check.executeQuery()) {
                if (rs.next()) {
                    return;
                }
            }
        }

        try (PreparedStatement insert = conn.prepareStatement(insertSql)) {
            insert.setString(1, name);
            insert.setString(2, email);
            insert.setString(3, phone);
            insert.setString(4, address);
            insert.executeUpdate();
        }
    }

    private void insertInventoryItem(Connection conn, InventoryItem item) throws SQLException {
        String sql = """
            INSERT INTO TblInventoryItems
            ([Item Name], Description, Quantity, SupplierInformation,
             ExpirationDate, SerialNumber, LowStockAlertThreshold)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
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
            stmt.executeUpdate();
        }
    }

    public Map<String, Supplier> parseSuppliersWithItems(File file) {
        return InventoryParser.parseSuppliersWithItems(file);
    }

    private static void rollbackQuietly(Connection conn) {
        TransactionUtils.rollbackQuietly(conn);
    }

    private static void closeQuietly(Connection conn) {
        TransactionUtils.restoreAutoCommitAndClose(conn);
    }
}



