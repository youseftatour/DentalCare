package control;

import entity.Patient;
import entity.Treatment;
import repository.AppointmentRepository;
import repository.PatientRepository;
import service.AppointmentSchedulingService;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

public class PatientController {
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final AppointmentSchedulingService schedulingService;

    public PatientController() {
        patientRepository = new PatientRepository();
        appointmentRepository = new AppointmentRepository();
        schedulingService = new AppointmentSchedulingService();
    }

    public Patient getPatientByID(int id) {
        try {
            return patientRepository.findById(id);
        } catch (SQLException exception) {
            utils.AppLogger.error(PatientController.class, "Patient database operation failed", exception);
            return null;
        }
    }

    public ArrayList<Treatment> getActiveTreatmentsForPatient(int patientId) {
        try {
            return patientRepository.findActiveTreatments(patientId);
        } catch (SQLException exception) {
            utils.AppLogger.error(PatientController.class, "Patient database operation failed", exception);
            return new ArrayList<>();
        }
    }

    public ArrayList<Object[]> getUpcomingAppointmentsForPatientWithIDs(int patientId) {
        try {
            return patientRepository.findUpcomingAppointments(patientId, LocalDate.now());
        } catch (SQLException exception) {
            utils.AppLogger.error(PatientController.class, "Patient database operation failed", exception);
            return new ArrayList<>();
        }
    }

    public boolean updateAppointmentStatus(int appointmentId, String newStatus) {
        if (newStatus == null || newStatus.isBlank()) {
            return false;
        }
        try {
            return appointmentRepository.updateStatus(appointmentId, newStatus);
        } catch (SQLException exception) {
            utils.AppLogger.error(PatientController.class, "Patient database operation failed", exception);
            return false;
        }
    }

    public boolean rescheduleAppointment(int appointmentId, String newDate, String newTime) {
        try {
            LocalDate date = LocalDate.parse(newDate);
            LocalTime time = parseTime(newTime);
            if (schedulingService.isInPast(date, time)) {
                return false;
            }
            String staffId = appointmentRepository.findAssignedStaff(appointmentId);
            int durationMinutes = appointmentRepository.findDurationMinutes(appointmentId);
            if (staffId != null && !staffId.isBlank()
                    && schedulingService.hasConflict(time,
                        durationMinutes,
                        appointmentRepository.findStaffAppointments(staffId, date), appointmentId)) {
                return false;
            }
            return appointmentRepository.reschedule(appointmentId, date, time);
        } catch (DateTimeParseException | SQLException exception) {
            utils.AppLogger.error(PatientController.class, "Patient database operation failed", exception);
            return false;
        }
    }

    public int calculateAge(Date dob) {
        if (dob == null) {
            return 0;
        }
        return Period.between(dob.toLocalDate(), LocalDate.now()).getYears();
    }

    public ArrayList<Treatment> getAllTreatments() {
        try {
            return patientRepository.findAllTreatments();
        } catch (SQLException exception) {
            utils.AppLogger.error(PatientController.class, "Patient database operation failed", exception);
            return new ArrayList<>();
        }
    }

    public boolean bookAppointment(int patientId, Date date, String time,
                                   String reason, String treatmentName) {
        if (date == null || treatmentName == null || treatmentName.isBlank()) {
            return false;
        }
        try {
            LocalTime appointmentTime = parseTime(time);
            int durationMinutes = patientRepository.findTreatmentDuration(treatmentName);
            if (durationMinutes <= 0) {
                return false;
            }
            if (schedulingService.isInPast(date.toLocalDate(), appointmentTime)) {
                return false;
            }
            return appointmentRepository.insert(patientId, date, appointmentTime, reason, treatmentName);
        } catch (DateTimeParseException | SQLException exception) {
            utils.AppLogger.error(PatientController.class, "Patient database operation failed", exception);
            return false;
        }
    }

    private LocalTime parseTime(String value) {
        if (value == null || value.isBlank()) {
            throw new DateTimeParseException("Time is empty", "", 0);
        }
        String trimmed = value.trim();
        return trimmed.length() == 5 ? LocalTime.parse(trimmed) : Time.valueOf(trimmed).toLocalTime();
    }
}


