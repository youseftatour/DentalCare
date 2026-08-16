package control;

import entity.Patient;
import entity.Treatment;
import repository.AppointmentRepository;
import repository.PatientRepository;

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

    public PatientController() {
        patientRepository = new PatientRepository();
        appointmentRepository = new AppointmentRepository();
    }

    public Patient getPatientByID(int id) {
        try {
            return patientRepository.findById(id);
        } catch (SQLException exception) {
            exception.printStackTrace();
            return null;
        }
    }

    public ArrayList<Treatment> getActiveTreatmentsForPatient(int patientId) {
        try {
            return patientRepository.findActiveTreatments(patientId);
        } catch (SQLException exception) {
            exception.printStackTrace();
            return new ArrayList<>();
        }
    }

    public ArrayList<Object[]> getUpcomingAppointmentsForPatientWithIDs(int patientId) {
        try {
            return patientRepository.findUpcomingAppointments(patientId, LocalDate.now());
        } catch (SQLException exception) {
            exception.printStackTrace();
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
            exception.printStackTrace();
            return false;
        }
    }

    public boolean rescheduleAppointment(int appointmentId, String newDate, String newTime) {
        try {
            LocalDate date = LocalDate.parse(newDate);
            LocalTime time = parseTime(newTime);
            if (LocalDateTime.of(date, time).isBefore(LocalDateTime.now())) {
                return false;
            }
            return appointmentRepository.reschedule(appointmentId, date, time);
        } catch (DateTimeParseException | SQLException exception) {
            exception.printStackTrace();
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
            exception.printStackTrace();
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
            if (LocalDateTime.of(date.toLocalDate(), appointmentTime).isBefore(LocalDateTime.now())) {
                return false;
            }
            return appointmentRepository.insert(patientId, date, appointmentTime, reason, treatmentName);
        } catch (DateTimeParseException | SQLException exception) {
            exception.printStackTrace();
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
