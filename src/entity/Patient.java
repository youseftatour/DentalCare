package entity;

public class Patient {
    private int id;
    private String name, phone, email;
    private int age;
    private String insuranceProvider, policyNumber;

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

    public int getId() { return id; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public int getAge() { return age; }
    public String getInsuranceProvider() { return insuranceProvider; }
    public String getPolicyNumber() { return policyNumber; }

	@Override
	public String toString() {
		return name;
	}
    
    

}
