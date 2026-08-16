package service;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordService {
    private static final int LOG_ROUNDS = 12;

    public String hash(char[] password) {
        if (password == null || password.length == 0) {
            throw new IllegalArgumentException("Password must not be empty");
        }
        return BCrypt.hashpw(new String(password), BCrypt.gensalt(LOG_ROUNDS));
    }

    public boolean matches(char[] password, String passwordHash) {
        if (password == null || password.length == 0
                || passwordHash == null || passwordHash.isBlank()) {
            return false;
        }
        try {
            return BCrypt.checkpw(new String(password), passwordHash);
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }
}
