package entity;

public class User {
    private String role;
    private String username;
    private int linkedID;

    public User(String role, String username, int linkedID) {
        this.role = role;
        this.username = username;
        this.linkedID = linkedID;
    }

    public String getRole() { return role; }
    public String getUsername() { return username; }
    public int getLinkedID() { return linkedID; }
}
