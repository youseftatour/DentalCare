package entity;

public class Treatment {
    private String name;
    private double cost;
    private String status;
    private int durationMinutes;

    public Treatment(String name, double cost, String status) {
        this(name, cost, status, 30);
    }

    public Treatment(String name, double cost, String status, int durationMinutes) {
        this.name = name;
        this.cost = cost;
        this.status = status;
        this.durationMinutes = durationMinutes;
    }

    public String getName() { return name; }
    public double getCost() { return cost; }
    public String getStatus() { return status; }
    public int getDurationMinutes() { return durationMinutes; }
}
