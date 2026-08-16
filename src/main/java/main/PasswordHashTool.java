package main;

import repository.AuthRepository;
import service.PasswordService;

import java.io.Console;
import java.util.Arrays;

public final class PasswordHashTool {
    private PasswordHashTool() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Usage: PasswordHashTool <person-id>");
            System.exit(2);
        }
        Console console = System.console();
        if (console == null) {
            throw new IllegalStateException("Run this tool from an interactive terminal");
        }
        char[] password = console.readPassword("New password: ");
        char[] confirmation = console.readPassword("Confirm password: ");
        try {
            if (!Arrays.equals(password, confirmation)) {
                throw new IllegalArgumentException("Passwords do not match");
            }
            String hash = new PasswordService().hash(password);
            if (!new AuthRepository().updatePasswordHash(args[0], hash)) {
                throw new IllegalArgumentException("Unknown person ID");
            }
            System.out.println("Password updated.");
        } finally {
            Arrays.fill(password, '\0');
            Arrays.fill(confirmation, '\0');
        }
    }
}
