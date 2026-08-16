package control;

import entity.User;
import utils.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;

public class AuthController {
    private static AuthController instance = null;
    private HashMap<String, User> users;

    private AuthController() {
     //   users = new HashMap<>();
        // Hardcoded users (Username, Password, Role, LinkedID)
      //  users.put("dentist1", new User("dentist1", "1234", "Dentist", 112233445));
      //  users.put("secretary1", new User("secretary1", "1234", "Secretary", null));
    //    users.put("manager1", new User("manager1", "1234", "Manager", 456456789));
    //    users.put("patient1", new User("patient1", "1234", "Patient", 123987456));
    }

    public static AuthController getInstance() {
        if (instance == null) instance = new AuthController();
        return instance;
    }

    
    
    public User authenticatePatient(String identifier) {
        String sql = """
            SELECT P.PersonId, P.FirstName, P.LastName
            FROM TblPatients PT
            JOIN TblPersons P ON PT.PatientId = P.PersonId
            WHERE PT.Identifier = ?
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, identifier);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("PersonId");
                String name = rs.getString("FirstName") + " " + rs.getString("LastName");
                return new User("Patient", name, id);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public User authenticateStaff(String staffId) {
        String sql = """
            SELECT S.Role, P.FirstName, P.LastName
            FROM TblStaff S
            JOIN TblPersons P ON S.StaffId = P.PersonId
            WHERE S.StaffId = ?
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, staffId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String role = rs.getString("Role");
                String name = rs.getString("FirstName") + " " + rs.getString("LastName");
                return new User(role, name, Integer.parseInt(staffId));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    
    
}
