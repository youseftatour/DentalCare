package service;

import repository.AppointmentRepository.AppointmentSlot;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class AppointmentSchedulingService {
    public static final int DEFAULT_DURATION_MINUTES = 30;

    public boolean isValidRequest(String patientId, String treatmentName, String staffId,
                                  LocalDate date, LocalTime time, double cost, int durationMinutes) {
        return patientId != null && !patientId.isBlank()
            && treatmentName != null && !treatmentName.isBlank()
            && staffId != null && !staffId.isBlank()
            && date != null && time != null && cost >= 0 && durationMinutes > 0;
    }

    public boolean isInPast(LocalDate date, LocalTime time) {
        return isInPast(date, time, Clock.systemDefaultZone());
    }

    boolean isInPast(LocalDate date, LocalTime time, Clock clock) {
        return LocalDateTime.of(date, time).isBefore(LocalDateTime.now(clock));
    }

    public boolean hasConflict(LocalTime newStart, int durationMinutes,
                               List<AppointmentSlot> existing, Integer excludedAppointmentId) {
        LocalTime newEnd = newStart.plusMinutes(durationMinutes);
        for (AppointmentSlot appointment : existing) {
            if (excludedAppointmentId != null && appointment.appointmentId() == excludedAppointmentId) {
                continue;
            }
            if (isCancelled(appointment.status())) {
                continue;
            }
            LocalTime existingEnd = appointment.start().plusMinutes(appointment.durationMinutes());
            if (overlaps(newStart, newEnd, appointment.start(), existingEnd)) {
                return true;
            }
        }
        return false;
    }

    public static boolean overlaps(LocalTime firstStart, LocalTime firstEnd,
                                   LocalTime secondStart, LocalTime secondEnd) {
        return firstStart.isBefore(secondEnd) && secondStart.isBefore(firstEnd);
    }

    private boolean isCancelled(String status) {
        return status != null
            && (status.equalsIgnoreCase("Cancelled") || status.equalsIgnoreCase("Canceled"));
    }
}
