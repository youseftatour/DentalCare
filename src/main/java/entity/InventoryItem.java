package entity;

import java.time.LocalDate;

public class InventoryItem {
    private int itemId;
    private String itemName;
    private String description;
    private int quantity;
    private String supplierInformation;
    private LocalDate expiryDate;
    private String serialNumber;
    private int lowStockThreshold;

    public InventoryItem(int itemId, String itemName, String description, int quantity,
                         String supplierInformation, LocalDate expiryDate,
                         String serialNumber, int lowStockThreshold) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.description = description;
        this.quantity = quantity;
        this.supplierInformation = supplierInformation;
        this.expiryDate = expiryDate;
        this.serialNumber = serialNumber;
        this.lowStockThreshold = lowStockThreshold;
    }

    public int getItemId() {
        return itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public String getDescription() {
        return description;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getSupplierInformation() {
        return supplierInformation;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public int getLowStockThreshold() {
        return lowStockThreshold;
    }
}
