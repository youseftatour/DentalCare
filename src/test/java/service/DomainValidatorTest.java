package service;

import entity.InventoryItem;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DomainValidatorTest {
    @Test
    public void acceptsValidPatient() {
        assertNull(DomainValidator.validatePatient("123", "Ada", "Lovelace",
            "+972 50-123-4567", "ada@example.com", Date.valueOf("1990-01-01"),
            "A1234", "Provider", "POL-1"));
    }

    @Test
    public void rejectsBlankNameInvalidEmailPhoneAndFutureBirthDate() {
        assertNotNull(DomainValidator.validatePerson("123", "", "Lovelace",
            "1234567", "ada@example.com", Date.valueOf("1990-01-01")));
        assertNotNull(DomainValidator.validatePerson("123", "Ada", "Lovelace",
            "1234567", "invalid", Date.valueOf("1990-01-01")));
        assertNotNull(DomainValidator.validatePerson("123", "Ada", "Lovelace",
            "bad", "ada@example.com", Date.valueOf("1990-01-01")));
        assertNotNull(DomainValidator.validatePerson("123", "Ada", "Lovelace",
            "1234567", "ada@example.com", Date.valueOf(LocalDate.now().plusDays(1))));
    }

    @Test
    public void rejectsInvalidIdentifiersAndMissingPolicy() {
        assertFalse(DomainValidator.isValidId("0"));
        assertFalse(DomainValidator.isValidId("1234567890"));
        assertNotNull(DomainValidator.validatePatient("123", "Ada", "Lovelace",
            "1234567", "ada@example.com", Date.valueOf("1990-01-01"),
            "123", "Provider", ""));
    }

    @Test
    public void validatesTreatmentPlanDateOrder() {
        assertTrue(DomainValidator.isValidTreatmentPlan(
            LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 1)));
        assertFalse(DomainValidator.isValidTreatmentPlan(
            LocalDate.of(2026, 1, 2), LocalDate.of(2026, 1, 1)));
    }

    @Test
    public void rejectsInvalidInventoryValues() {
        InventoryItem item = new InventoryItem(1, "Gloves", "", -1, "Supplier",
            null, "SER-1", 0);
        assertFalse(DomainValidator.isValidInventoryItem(item));
    }
}
