package entity;

import java.time.LocalDate;

public class TreatmentPlan {
    private int treatmentPlanId;
    private LocalDate startDate;
    private LocalDate estimatedCompletionDate;
    private String patientId;
    private String status;
    private String createdByDentist;

    public TreatmentPlan(int treatmentPlanId, LocalDate startDate, LocalDate estimatedCompletionDate,
                         String patientId, String status, String createdByDentist) {
        this.treatmentPlanId = treatmentPlanId;
        this.startDate = startDate;
        this.estimatedCompletionDate = estimatedCompletionDate;
        this.patientId = patientId;
        this.status = status;
        this.createdByDentist = createdByDentist;
    }

    public int getTreatmentPlanId() {
        return treatmentPlanId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEstimatedCompletionDate() {
        return estimatedCompletionDate;
    }

    public String getPatientId() {
        return patientId;
    }

    public String getStatus() {
        return status;
    }

    public String getCreatedByDentist() {
        return createdByDentist;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public void setEstimatedCompletionDate(LocalDate estimatedCompletionDate) {
        this.estimatedCompletionDate = estimatedCompletionDate;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCreatedByDentist(String createdByDentist) {
        this.createdByDentist = createdByDentist;
    }
}
