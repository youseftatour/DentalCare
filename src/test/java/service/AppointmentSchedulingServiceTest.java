package service;

import org.junit.Test;
import repository.AppointmentRepository.AppointmentSlot;

import java.time.LocalTime;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AppointmentSchedulingServiceTest {
    private final AppointmentSchedulingService service = new AppointmentSchedulingService();

    @Test
    public void overlappingIntervalsConflict() {
        assertTrue(hasConflict("10:30", slot(1, "10:00", "Active"), null));
    }

    @Test
    public void touchingIntervalAfterIsAllowed() {
        assertFalse(hasConflict("11:00", slot(1, "10:00", "Active"), null));
    }

    @Test
    public void touchingIntervalBeforeIsAllowed() {
        assertFalse(service.hasConflict(LocalTime.of(9, 0), 60,
            List.of(slot(1, "10:00", "Active")), null));
    }

    @Test
    public void sameIntervalConflicts() {
        assertTrue(hasConflict("10:00", slot(1, "10:00", "Active"), null));
    }

    @Test
    public void cancelledAppointmentDoesNotConflict() {
        assertFalse(hasConflict("10:00", slot(1, "10:00", "Cancelled"), null));
    }

    @Test
    public void reschedulingExcludesSameAppointment() {
        assertFalse(hasConflict("10:00", slot(42, "10:00", "Active"), 42));
    }

    private boolean hasConflict(String start, AppointmentSlot existing, Integer excludedId) {
        return service.hasConflict(LocalTime.parse(start), 60, List.of(existing), excludedId);
    }

    private AppointmentSlot slot(int id, String start, String status) {
        return new AppointmentSlot(id, LocalTime.parse(start), 60, status);
    }
}
