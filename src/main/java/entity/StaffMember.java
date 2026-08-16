package entity;

import java.util.Objects;

public class StaffMember {
    private String id;
    private String firstName;
    private String lastName;
    private String role;

    public StaffMember(String id, String firstName, String lastName, String role) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
    }

    public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public int getId() {
        return Integer.parseInt(id) ;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getRole() {
        return role;
    }

    @Override
    public String toString() {
        return getFullName(); 
    }

    @Override
    public boolean equals(Object other) {
        return this == other || other instanceof StaffMember staff && Objects.equals(id, staff.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
