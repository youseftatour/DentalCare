package control;

import entity.Appointment;
import entity.InventoryItem;
import entity.Patient;
import entity.StaffMember;
import repository.InventoryRepository;
import repository.AppointmentRepository;
import repository.PatientRepository;
import service.AppointmentSchedulingService;
import utils.DatabaseManager;
import utils.TransactionUtils;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SecretaryController {

    private static final LocalTime OPENING_TIME = LocalTime.of(8, 0);
    private static final LocalTime CLOSING_TIME = LocalTime.of(17, 0);
    private static final int SLOT_STEP_MINUTES = 30;
    private final InventoryRepository inventoryRepository = new InventoryRepository();
    private final AppointmentRepository appointmentRepository = new AppointmentRepository();
    private final PatientRepository patientRepository = new PatientRepository();
    private final AppointmentSchedulingService schedulingService = new AppointmentSchedulingService();

    public boolean addInventoryItem(InventoryItem item) {
        if (item == null || item.getQuantity() < 0 || item.getLowStockThreshold() < 0) {
            return false;
        }

        try {
            return inventoryRepository.insert(item);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteInventoryItem(int itemId) {
        try {
            return inventoryRepository.deleteById(itemId);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean addNewPatient(String id, String firstName, String lastName, String phone, String email,
                                 Date dob, String identifier, String insurance, String policy) {

        String sqlPersons = """
            INSERT INTO TblPersons (PersonId, FirstName, LastName, PhoneNumber, Email, DateOfBirth)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        String sqlPatients = """
            INSERT INTO TblPatients (PatientId, Identifier, InsuranceProviderName, PolicyNumber)
            VALUES (?, ?, ?, ?)
            """;

        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement ps1 = conn.prepareStatement(sqlPersons);
                 PreparedStatement ps2 = conn.prepareStatement(sqlPatients)) {

                ps1.setString(1, id);
                ps1.setString(2, firstName);
                ps1.setString(3, lastName);
                ps1.setString(4, phone);
                ps1.setString(5, email);
                ps1.setDate(6, dob);

                if (ps1.executeUpdate() != 1) {
                    conn.rollback();
                    return false;
                }

                ps2.setString(1, id);
                ps2.setString(2, identifier);
                ps2.setString(3, insurance);
                ps2.setString(4, policy);

                if (ps2.executeUpdate() != 1) {
                    conn.rollback();
                    return false;
                }

                conn.commit();
                return true;
            }
        } catch (SQLException e) {
            rollbackQuietly(conn);
            e.printStackTrace();
            return false;
        } finally {
            closeQuietly(conn);
        }
    }

    public boolean patientIdExists(String id) {
        return valueExists("SELECT 1 FROM TblPatients WHERE PatientId = ?", id);
    }

    public boolean identifierExists(String identifier) {
        return valueExists("SELECT 1 FROM TblPatients WHERE Identifier = ?", identifier);
    }

    public boolean policyNumberExists(String policy) {
        return valueExists("SELECT 1 FROM TblPatients WHERE PolicyNumber = ?", policy);
    }

    private boolean valueExists(String sql, String value) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, value);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<String> getAllInsuranceProviders() {
        List<String> providers = new ArrayList<>();
        String sql = "SELECT ProviderName FROM TblInsurances ORDER BY ProviderName";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                providers.add(rs.getString("ProviderName"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return providers;
    }

    public void updateStatus(int appointmentId, String newStatus) {
        try {
            appointmentRepository.updateStatus(appointmentId, newStatus);
        } catch (SQLException e) {
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

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean rescheduleDateOnly(int appointmentId, LocalDate newDate) {
        if (newDate == null || newDate.isBefore(LocalDate.now())) {
            return false;
        }

        AppointmentSlot current = getAppointmentSlot(appointmentId);
        if (current == null) {
            return false;
        }

        if (current.staffId != null && !current.staffId.isBlank()
                && !isStaffAvailable(current.staffId, newDate, current.time,
                                     getAppointmentDuration(appointmentId), appointmentId)) {
            return false;
        }

        String sql = """
            UPDATE TblAppointments
            SET AppointmentDate = ?, Status = 'Rescheduled'
            WHERE AppointmentID = ?
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(newDate));
            stmt.setInt(2, appointmentId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public ArrayList<Object[]> getDetailedAppointments(String filter) {
        ArrayList<Object[]> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder("""
            SELECT A.AppointmentID,
                   P.FirstName & ' ' & P.LastName AS PatientName,
                   A.AppointmentDate, A.AppointmentTime, A.TreatmentName, A.Cost,
                   A.Status,
                   PS.FirstName & ' ' & PS.LastName AS StaffName,
                   A.ReasonForVisit, A.IsReminderSent, A.IsPaid, A.IsSterilized
            FROM ((TblAppointments A
            INNER JOIN TblPatients Pa ON A.PatientID = Pa.PatientID)
            INNER JOIN TblPersons P ON Pa.PatientID = P.PersonID)
            LEFT JOIN (TblStaff S
            LEFT JOIN TblPersons PS ON S.StaffId = PS.PersonId)
            ON A.AssignedMedicalStaff = S.StaffId
            """);

        boolean useDateParameter = false;

        if ("Upcoming".equalsIgnoreCase(filter)) {
            sql.append(" WHERE A.AppointmentDate >= ?");
            useDateParameter = true;
        } else if ("Past".equalsIgnoreCase(filter)) {
            sql.append(" WHERE A.AppointmentDate < ?");
            useDateParameter = true;
        }

        sql.append(" ORDER BY A.AppointmentDate DESC, A.AppointmentTime DESC");

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            if (useDateParameter) {
                stmt.setDate(1, Date.valueOf(LocalDate.now()));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Date appointmentDate = rs.getDate("AppointmentDate");
                    Time appointmentTime = rs.getTime("AppointmentTime");

                    list.add(new Object[]{
                        rs.getInt("AppointmentID"),
                        rs.getString("PatientName"),
                        appointmentDate != null ? appointmentDate.toLocalDate().toString() : "",
                        appointmentTime != null ? appointmentTime.toLocalTime().toString() : "",
                        rs.getString("TreatmentName"),
                        rs.getDouble("Cost"),
                        rs.getString("Status"),
                        rs.getString("StaffName"),
                        rs.getString("ReasonForVisit"),
                        rs.getBoolean("IsReminderSent") ? "Yes" : "No",
                        rs.getBoolean("IsPaid") ? "Yes" : "No",
                        rs.getBoolean("IsSterilized") ? "Yes" : "No"
                    });
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public ArrayList<Object[]> getAppointmentsWithPatientNames(String filter) {
        ArrayList<Object[]> rows = new ArrayList<>();

        StringBuilder sql = new StringBuilder("""
            SELECT A.AppointmentID, A.PatientID,
                   P.FirstName, P.LastName,
                   A.AppointmentDate, A.AppointmentTime,
                   A.TreatmentName, A.Status,
                   A.IsPaid, A.IsSterilized
            FROM TblAppointments A
            INNER JOIN TblPersons P ON A.PatientID = P.PersonID
            WHERE 1=1
            """);

        boolean useDateParameter = false;

        if ("Upcoming".equalsIgnoreCase(filter)) {
            sql.append(" AND A.AppointmentDate >= ?");
            useDateParameter = true;
        } else if ("Past".equalsIgnoreCase(filter)) {
            sql.append(" AND A.AppointmentDate < ?");
            useDateParameter = true;
        }

        sql.append(" ORDER BY A.AppointmentDate DESC, A.AppointmentTime DESC");

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            if (useDateParameter) {
                stmt.setDate(1, Date.valueOf(LocalDate.now()));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Date sqlDate = rs.getDate("AppointmentDate");
                    Time sqlTime = rs.getTime("AppointmentTime");

                    rows.add(new Object[]{
                        rs.getInt("AppointmentID"),
                        rs.getString("FirstName") + " " + rs.getString("LastName"),
                        sqlDate != null ? sqlDate.toLocalDate().toString() : "",
                        sqlTime != null ? sqlTime.toLocalTime().toString() : "",
                        rs.getString("TreatmentName"),
                        rs.getString("Status"),
                        rs.getBoolean("IsPaid") ? "Yes" : "No",
                        rs.getBoolean("IsSterilized") ? "Yes" : "No"
                    });
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return rows;
    }

    public void updateInventoryQuantity(int itemId, String newQuantity) {
        int quantity;

        try {
            quantity = Integer.parseInt(newQuantity);
        } catch (NumberFormatException e) {
            System.err.println("Invalid inventory quantity: " + newQuantity);
            return;
        }

        if (quantity < 0) {
            System.err.println("Inventory quantity cannot be negative.");
            return;
        }

        try {
            inventoryRepository.updateQuantity(itemId, quantity);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateInventoryQuantity(int itemId, int newQuantity) {
        updateInventoryQuantity(itemId, Integer.toString(newQuantity));
    }

    public void updateInventoryThreshold(int itemId, int threshold) {
        if (threshold < 0) {
            return;
        }
        try {
            inventoryRepository.updateLowStockThreshold(itemId, threshold);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<InventoryItem> getAllInventoryItems() {
        try {
            return inventoryRepository.findAll();
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public ArrayList<InventoryItem> getInventoryItems() {
        return getAllInventoryItems();
    }

    public ArrayList<Appointment> getUpcomingAppointments() {
        ArrayList<Appointment> list = new ArrayList<>();

        String sql = """
            SELECT AppointmentID, PatientID, TreatmentPlanID, TreatmentName, Cost, Status,
                   AppointmentDate, AppointmentTime, IsPaid, IsSterilized
            FROM TblAppointments
            WHERE AppointmentDate >= ?
              AND (Status IS NULL OR (Status <> 'Cancelled' AND Status <> 'Canceled'))
            ORDER BY AppointmentDate, AppointmentTime
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(LocalDate.now()));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Date sqlDate = rs.getDate("AppointmentDate");
                    Time sqlTime = rs.getTime("AppointmentTime");

                    if (sqlDate == null || sqlTime == null) {
                        continue;
                    }

                    list.add(new Appointment(
                        rs.getInt("AppointmentID"),
                        rs.getInt("PatientID"),
                        rs.getInt("TreatmentPlanID"),
                        rs.getString("TreatmentName"),
                        rs.getDouble("Cost"),
                        rs.getString("Status"),
                        sqlDate.toLocalDate(),
                        sqlTime.toLocalTime(),
                        rs.getBoolean("IsPaid"),
                        rs.getBoolean("IsSterilized")
                    ));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public ArrayList<Patient> getAllPatients() {
        try {
            return patientRepository.findAll();
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public ArrayList<String> getAllTreatmentNames() {
        ArrayList<String> list = new ArrayList<>();
        try {
            patientRepository.findAllTreatments().forEach(treatment -> list.add(treatment.getName()));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public int getTreatmentDuration(String treatmentName) {
        try {
            return patientRepository.findTreatmentDuration(treatmentName);
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public List<StaffMember> getAllStaffMembers() {
        List<StaffMember> allStaff = new ArrayList<>();

        String sql = """
            SELECT S.StaffId, P.FirstName, P.LastName, S.Role
            FROM TblStaff S
            INNER JOIN TblPersons P ON P.PersonId = S.StaffId
            ORDER BY P.LastName, P.FirstName
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                allStaff.add(new StaffMember(
                    rs.getString("StaffId"),
                    rs.getString("FirstName"),
                    rs.getString("LastName"),
                    rs.getString("Role")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return allStaff;
    }

    public boolean bookAppointment(String patientId, String treatmentName, String staffId,
                                   LocalDate date, LocalTime time, double cost) {

        int durationMinutes = getTreatmentDuration(treatmentName);

        if (!schedulingService.isValidRequest(patientId, treatmentName, staffId, date, time,
                cost, durationMinutes)) {
            return false;
        }

        if (schedulingService.isInPast(date, time)) {
            return false;
        }

        if (staffId != null && !staffId.isBlank()
                && !isStaffAvailable(staffId, date, time, durationMinutes, null)) {
            return false;
        }

        String sql = """
            INSERT INTO TblAppointments
            (PatientId, TreatmentName, AssignedMedicalStaff, AppointmentDate, AppointmentTime,
             Status, Cost, IsPaid, IsReminderSent, IsSterilized)
            VALUES (?, ?, ?, ?, ?, 'Active', ?, false, false, false)
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, patientId);
            stmt.setString(2, treatmentName);

            if (staffId == null || staffId.isBlank()) {
                stmt.setNull(3, Types.VARCHAR);
            } else {
                stmt.setString(3, staffId);
            }

            stmt.setDate(4, Date.valueOf(date));
            stmt.setTime(5, Time.valueOf(time));
            stmt.setDouble(6, cost);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public HashMap<Patient, ArrayList<Appointment>> getActivePlansWithTreatments() {
        HashMap<Patient, ArrayList<Appointment>> result = new LinkedHashMap<>();

        String sql = """
            SELECT P.PersonId, P.FirstName, P.LastName, P.PhoneNumber, P.Email, P.DateOfBirth,
                   PT.InsuranceProviderName, PT.PolicyNumber,
                   TP.TreatmentPlanID,
                   A.AppointmentID, A.TreatmentName, A.Cost, A.Status,
                   A.AppointmentDate, A.AppointmentTime, A.IsPaid, A.IsSterilized
            FROM ((TblTreatmentPlans TP
            INNER JOIN TblPatients PT ON TP.PatientID = PT.PatientId)
            INNER JOIN TblPersons P ON PT.PatientId = P.PersonId)
            INNER JOIN TblAppointments A ON A.TreatmentPlanID = TP.TreatmentPlanID
            WHERE TP.Status = 'Active'
            ORDER BY P.LastName, P.FirstName, A.AppointmentDate, A.AppointmentTime
            """;

        Map<Integer, Patient> patientsById = new LinkedHashMap<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("PersonId");

                Patient patient = patientsById.get(id);
                if (patient == null) {
                    Date dob = rs.getDate("DateOfBirth");
                    int age = dob != null
                            ? Period.between(dob.toLocalDate(), LocalDate.now()).getYears()
                            : 0;

                    patient = new Patient(
                        id,
                        rs.getString("FirstName") + " " + rs.getString("LastName"),
                        rs.getString("PhoneNumber"),
                        rs.getString("Email"),
                        age,
                        rs.getString("InsuranceProviderName"),
                        rs.getString("PolicyNumber")
                    );

                    patientsById.put(id, patient);
                    result.put(patient, new ArrayList<>());
                }

                Date appointmentDate = rs.getDate("AppointmentDate");
                Time appointmentTime = rs.getTime("AppointmentTime");

                if (appointmentDate == null || appointmentTime == null) {
                    continue;
                }

                result.get(patient).add(new Appointment(
                    rs.getInt("AppointmentID"),
                    id,
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

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    public ArrayList<LocalDate> getUpcomingDateOptions() {
        ArrayList<LocalDate> dates = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = 0; i < 14; i++) {
            dates.add(today.plusDays(i));
        }

        return dates;
    }

    public ArrayList<LocalTime> getAvailableTimeSlots(LocalDate date, boolean isUrgent, int durationMinutes) {
        return getAvailableTimeSlots(date, null, isUrgent, durationMinutes);
    }

    /**
     * Staff-aware overload. Existing callers can keep using the original method above.
     */
    public ArrayList<LocalTime> getAvailableTimeSlots(LocalDate date, String staffId,
                                                       boolean isUrgent, int durationMinutes) {

        ArrayList<LocalTime> availableSlots = new ArrayList<>();

        if (date == null || date.isBefore(LocalDate.now())) {
            return availableSlots;
        }

        int duration = Math.max(SLOT_STEP_MINUTES, durationMinutes);
        LocalTime firstCandidate = OPENING_TIME;

        if (date.equals(LocalDate.now())) {
            firstCandidate = max(OPENING_TIME, roundUpToSlot(LocalTime.now()));
        }

        for (LocalTime candidate = firstCandidate;
             !candidate.plusMinutes(duration).isAfter(CLOSING_TIME);
             candidate = candidate.plusMinutes(SLOT_STEP_MINUTES)) {

            if (isStaffAvailable(staffId, date, candidate, duration, null)) {
                availableSlots.add(candidate);

                if (isUrgent) {
                    break;
                }
            }
        }

        return availableSlots;
    }

    public void markAsPaid(int appointmentId) {
        updateBooleanField(appointmentId, "IsPaid");
    }

    public void markAsSterilized(int appointmentId) {
        updateBooleanField(appointmentId, "IsSterilized");
    }

    private void updateBooleanField(int appointmentId, String columnName) {
        if (!"IsPaid".equals(columnName) && !"IsSterilized".equals(columnName)) {
            throw new IllegalArgumentException("Unsupported appointment field.");
        }

        String sql = "UPDATE TblAppointments SET " + columnName + " = true WHERE AppointmentID = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, appointmentId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void cancelAppointment(int appointmentId) {
        try {
            appointmentRepository.updateStatus(appointmentId, "Cancelled");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean rescheduleAppointment(int appointmentId, LocalDate newDate, LocalTime newTime) {
        if (newDate == null || newTime == null
                || LocalDateTime.of(newDate, newTime).isBefore(LocalDateTime.now())) {
            return false;
        }

        AppointmentSlot current = getAppointmentSlot(appointmentId);
        if (current == null) {
            return false;
        }

        if (current.staffId != null && !current.staffId.isBlank()
                && !isStaffAvailable(current.staffId, newDate, newTime,
                                     getAppointmentDuration(appointmentId), appointmentId)) {
            return false;
        }

        try {
            return appointmentRepository.reschedule(appointmentId, newDate, newTime, "Rescheduled");
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private int getAppointmentDuration(int appointmentId) {
        try {
            return appointmentRepository.findDurationMinutes(appointmentId);
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
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

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Date appointmentDate = rs.getDate("AppointmentDate");
                    Time appointmentTime = rs.getTime("AppointmentTime");

                    if (appointmentDate == null || appointmentTime == null) {
                        return null;
                    }

                    return new Appointment(
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
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    private boolean isStaffAvailable(String staffId, LocalDate date, LocalTime candidateStart,
                                     int durationMinutes, Integer excludedAppointmentId) {
        try {
            return !schedulingService.hasConflict(candidateStart, durationMinutes,
                appointmentRepository.findStaffAppointments(staffId, date), excludedAppointmentId);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private AppointmentSlot getAppointmentSlot(int appointmentId) {
        String sql = """
            SELECT AssignedMedicalStaff, AppointmentTime
            FROM TblAppointments
            WHERE AppointmentID = ?
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, appointmentId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Time time = rs.getTime("AppointmentTime");
                    if (time == null) {
                        return null;
                    }

                    return new AppointmentSlot(
                        rs.getString("AssignedMedicalStaff"),
                        time.toLocalTime()
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static LocalTime roundUpToSlot(LocalTime time) {
        LocalTime normalized = time.withSecond(0).withNano(0);
        int minute = normalized.getMinute();

        if (minute == 0 || minute == 30) {
            return normalized;
        }

        if (minute < 30) {
            return normalized.withMinute(30);
        }

        return normalized.plusHours(1).withMinute(0);
    }

    private static LocalTime max(LocalTime first, LocalTime second) {
        return first.isAfter(second) ? first : second;
    }

    private static void rollbackQuietly(Connection conn) {
        TransactionUtils.rollbackQuietly(conn);
    }

    private static void closeQuietly(Connection conn) {
        TransactionUtils.restoreAutoCommitAndClose(conn);
    }

    private static final class AppointmentSlot {
        private final String staffId;
        private final LocalTime time;

        private AppointmentSlot(String staffId, LocalTime time) {
            this.staffId = staffId;
            this.time = time;
        }
    }
}
