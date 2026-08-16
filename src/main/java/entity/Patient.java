package entity;

import java.util.Objects;

public class Patient {
    private int id;
    private String name;
    private String phone;
    private String email;
    private int age;
    private String insuranceProvider;
    private String policyNumber;

    public Patient(int id, String name, String phone, String email, int age,
                   String insuranceProvider, String policyNumber) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.age = age;
        this.insuranceProvider = insuranceProvider;
        this.policyNumber = policyNumber;
    }

    public Patient(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public int getAge() {
        return age;
    }

    public String getInsuranceProvider() {
        return insuranceProvider;
    }

    public String getPolicyNumber() {
        return policyNumber;
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * Patients represent the same logical person when they have the same database ID.
     * This is important when Patient objects are used as keys in maps.
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof Patient)) {
            return false;
        }

        Patient patient = (Patient) other;
        return id == patient.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
