package service;

import entity.InventoryItem;

import java.sql.Date;
import java.time.LocalDate;
import java.util.regex.Pattern;

public final class DomainValidator {
    private static final Pattern ID = Pattern.compile("[1-9]\\d{0,8}");
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern PHONE = Pattern.compile("^[+()0-9][+()0-9 .-]{6,19}$");

    private DomainValidator() {
    }

    public static String validatePerson(String id, String firstName, String lastName,
                                        String phone, String email, Date dateOfBirth) {
        if (!isValidId(id)) return "ID must contain 1–9 digits and cannot start with zero.";
        if (isBlank(firstName) || isBlank(lastName)) return "First and last names are required.";
        if (!isValidPhone(phone)) return "Enter a valid phone number.";
        if (!isValidEmail(email)) return "Enter a valid email address.";
        if (dateOfBirth == null || dateOfBirth.toLocalDate().isAfter(LocalDate.now())) {
            return "Date of birth cannot be empty or in the future.";
        }
        return null;
    }

    public static String validatePatient(String id, String firstName, String lastName,
                                         String phone, String email, Date dateOfBirth,
                                         String identifier, String insurance, String policy) {
        String personError = validatePerson(id, firstName, lastName, phone, email, dateOfBirth);
        if (personError != null) return personError;
        if (identifier == null || identifier.trim().length() != 5) {
            return "Patient identifier must contain exactly 5 characters.";
        }
        if (isBlank(insurance)) return "Insurance provider is required.";
        if (isBlank(policy)) return "Policy number is required.";
        return null;
    }

    public static String validateStaff(String id, String firstName, String lastName,
                                       String phone, String email, Date dateOfBirth,
                                       String qualifications, String specialization, String role) {
        String personError = validatePerson(id, firstName, lastName, phone, email, dateOfBirth);
        if (personError != null) return personError;
        if (isBlank(qualifications) || isBlank(specialization) || isBlank(role)) {
            return "Qualifications, specialization, and role are required.";
        }
        return null;
    }

    public static boolean isValidInventoryItem(InventoryItem item) {
        return item != null && !isBlank(item.getItemName()) && !isBlank(item.getSerialNumber())
            && item.getQuantity() >= 0 && item.getLowStockThreshold() >= 0;
    }

    public static boolean isValidTreatmentPlan(LocalDate startDate, LocalDate endDate) {
        return startDate != null && (endDate == null || !endDate.isBefore(startDate));
    }

    public static boolean isValidId(String value) {
        return value != null && ID.matcher(value.trim()).matches();
    }

    public static boolean isValidEmail(String value) {
        return value != null && EMAIL.matcher(value.trim()).matches();
    }

    public static boolean isValidPhone(String value) {
        return value != null && PHONE.matcher(value.trim()).matches();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
