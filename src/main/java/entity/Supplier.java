package entity;

import java.util.ArrayList;

public class Supplier {
    private String name;
    private String email;
    private String phone;
    private String address;
    private ArrayList<InventoryItem> items;

    public Supplier(String name, String email, String phone, String address) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.items = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public ArrayList<InventoryItem> getItems() {
        return items;
    }

    public void addItem(InventoryItem item) {
        items.add(item);
    }

    @Override
    public String toString() {
        return name + " (" + email + ")";
    }
}
