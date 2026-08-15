package entity;

public class Treatment {
    private String name;
    private double cost;
    private String status;

    public Treatment(String name, double cost, String status) {
        this.name = name;
        this.cost = cost;
        this.status = status;
    }

    public String getName() { return name; }
    public double getCost() { return cost; }
    public String getStatus() { return status; }
}
