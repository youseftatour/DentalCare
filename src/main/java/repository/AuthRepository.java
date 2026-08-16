package repository;

import entity.User;
import utils.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthRepository {
    public record Account(User user, String passwordHash) { }

    public Account findPatientByIdentifier(String identifier) throws SQLException {
        String sql = """
            SELECT P.PersonId, P.FirstName, P.LastName, P.PasswordHash
            FROM TblPatients PT
            JOIN TblPersons P ON PT.PatientId = P.PersonId
            WHERE PT.Identifier = ?
            """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, identifier);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                int id = resultSet.getInt("PersonId");
                String name = resultSet.getString("FirstName") + " " + resultSet.getString("LastName");
                return new Account(new User("Patient", name, id),
                    resultSet.getString("PasswordHash"));
            }
        }
    }

    public Account findStaffById(String staffId) throws SQLException {
        String sql = """
            SELECT S.Role, P.FirstName, P.LastName, P.PasswordHash
            FROM TblStaff S
            JOIN TblPersons P ON S.StaffId = P.PersonId
            WHERE S.StaffId = ?
            """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, staffId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                String role = resultSet.getString("Role");
                String name = resultSet.getString("FirstName") + " " + resultSet.getString("LastName");
                return new Account(new User(role, name, Integer.parseInt(staffId)),
                    resultSet.getString("PasswordHash"));
            }
        }
    }

    public boolean updatePasswordHash(String personId, String passwordHash) throws SQLException {
        String sql = "UPDATE TblPersons SET PasswordHash = ? WHERE PersonId = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, passwordHash);
            statement.setString(2, personId);
            return statement.executeUpdate() == 1;
        }
    }
}
