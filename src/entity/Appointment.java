package entity;

import java.time.LocalDate;
import java.time.LocalTime;

public class Appointment {
    private int appointmentId;
    private int patientId;
    private int treatmentPlanId;
    private String treatmentName;
    private double cost;
    private String status;
    private LocalDate date;
    private LocalTime time;
    private boolean isPaid;
    private boolean isSterilized;


    public int getAppointmentId() {
		return appointmentId;
	}



	public void setAppointmentId(int appointmentId) {
		this.appointmentId = appointmentId;
	}



	public int getPatientId() {
		return patientId;
	}



	public void setPatientId(int patientId) {
		this.patientId = patientId;
	}



	public int getTreatmentPlanId() {
		return treatmentPlanId;
	}



	public void setTreatmentPlanId(int treatmentPlanId) {
		this.treatmentPlanId = treatmentPlanId;
	}



	public String getTreatmentName() {
		return treatmentName;
	}



	public void setTreatmentName(String treatmentName) {
		this.treatmentName = treatmentName;
	}



	public double getCost() {
		return cost;
	}



	public void setCost(double cost) {
		this.cost = cost;
	}



	public String getStatus() {
		return status;
	}



	public void setStatus(String status) {
		this.status = status;
	}



	public LocalDate getDate() {
		return date;
	}



	public void setDate(LocalDate date) {
		this.date = date;
	}



	public LocalTime getTime() {
		return time;
	}



	public void setTime(LocalTime time) {
		this.time = time;
	}

	public boolean isPaid() { return isPaid; }
	public boolean isSterilized() { return isSterilized; }


	public Appointment(int appointmentId, int patientId, int treatmentPlanId, String treatmentName,
            double cost, String status, LocalDate date, LocalTime time,
            boolean isPaid, boolean isSterilized) {
this.appointmentId = appointmentId;
this.patientId = patientId;
this.treatmentPlanId = treatmentPlanId;
this.treatmentName = treatmentName;
this.cost = cost;
this.status = status;
this.date = date;
this.time = time;
this.isPaid = isPaid;
this.isSterilized = isSterilized;
}


  

    @Override
    public String toString() {
        return date.toString() + " " + time.toString() + " - " + treatmentName + " (" + status + ")";
    }

}
