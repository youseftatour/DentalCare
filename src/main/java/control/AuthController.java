package control;

import entity.User;
import repository.AuthRepository;
import repository.AuthRepository.Account;
import service.PasswordService;

import java.sql.SQLException;

public class AuthController {
    private static AuthController instance;
    private final AuthRepository authRepository;
    private final PasswordService passwordService;

    private AuthController() {
        authRepository = new AuthRepository();
        passwordService = new PasswordService();
    }

    public static AuthController getInstance() {
        if (instance == null) {
            instance = new AuthController();
        }
        return instance;
    }

    public User authenticatePatient(String identifier, char[] password) {
        try {
            Account account = authRepository.findPatientByIdentifier(identifier);
            return authenticatedUser(account, password);
        } catch (SQLException exception) {
            exception.printStackTrace();
            return null;
        }
    }

    public User authenticateStaff(String staffId, char[] password) {
        try {
            Account account = authRepository.findStaffById(staffId);
            return authenticatedUser(account, password);
        } catch (SQLException | NumberFormatException exception) {
            exception.printStackTrace();
            return null;
        }
    }

    private User authenticatedUser(Account account, char[] password) {
        return account != null && passwordService.matches(password, account.passwordHash())
            ? account.user() : null;
    }
}
