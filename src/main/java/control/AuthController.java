package control;

import entity.User;
import repository.AuthRepository;

import java.sql.SQLException;

public class AuthController {
    private static AuthController instance;
    private final AuthRepository authRepository;

    private AuthController() {
        authRepository = new AuthRepository();
    }

    public static AuthController getInstance() {
        if (instance == null) {
            instance = new AuthController();
        }
        return instance;
    }

    public User authenticatePatient(String identifier) {
        try {
            return authRepository.findPatientByIdentifier(identifier);
        } catch (SQLException exception) {
            exception.printStackTrace();
            return null;
        }
    }

    public User authenticateStaff(String staffId) {
        try {
            return authRepository.findStaffById(staffId);
        } catch (SQLException | NumberFormatException exception) {
            exception.printStackTrace();
            return null;
        }
    }
}
