package service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PasswordServiceTest {
    private final PasswordService service = new PasswordService();

    @Test
    public void verifiesCorrectPassword() {
        char[] password = "correct horse battery staple".toCharArray();
        assertTrue(service.matches(password, service.hash(password)));
    }

    @Test
    public void rejectsWrongPassword() {
        String hash = service.hash("correct password".toCharArray());
        assertFalse(service.matches("wrong password".toCharArray(), hash));
    }

    @Test
    public void rejectsMissingOrMalformedHash() {
        assertFalse(service.matches("password".toCharArray(), null));
        assertFalse(service.matches("password".toCharArray(), "not-a-bcrypt-hash"));
    }
}
