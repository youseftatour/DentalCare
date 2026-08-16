package repository;

import entity.InventoryItem;
import utils.DatabaseManager;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;

public class InventoryRepository {
    public ArrayList<InventoryItem> findAll() throws SQLException {
        String sql = """
            SELECT ItemID, [Item Name], Description, Quantity, SupplierInformation,
                   ExpirationDate, SerialNumber, LowStockAlertThreshold
            FROM TblInventoryItems
            """;
        ArrayList<InventoryItem> items = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                Date expirationDate = resultSet.getDate("ExpirationDate");
                items.add(new InventoryItem(
                    resultSet.getInt("ItemID"),
                    resultSet.getString("Item Name"),
                    resultSet.getString("Description"),
                    resultSet.getInt("Quantity"),
                    resultSet.getString("SupplierInformation"),
                    expirationDate == null ? null : expirationDate.toLocalDate(),
                    resultSet.getString("SerialNumber"),
                    resultSet.getInt("LowStockAlertThreshold")
                ));
            }
        }
        return items;
    }

    public boolean insert(InventoryItem item) throws SQLException {
        String sql = """
            INSERT INTO TblInventoryItems
                ([Item Name], Description, Quantity, SupplierInformation,
                 ExpirationDate, SerialNumber, LowStockAlertThreshold)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, item.getItemName());
            statement.setString(2, item.getDescription());
            statement.setInt(3, item.getQuantity());
            statement.setString(4, item.getSupplierInformation());
            if (item.getExpiryDate() == null) {
                statement.setNull(5, Types.DATE);
            } else {
                statement.setDate(5, Date.valueOf(item.getExpiryDate()));
            }
            statement.setString(6, item.getSerialNumber());
            statement.setInt(7, item.getLowStockThreshold());
            return statement.executeUpdate() > 0;
        }
    }

    public boolean deleteById(int itemId) throws SQLException {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                 "DELETE FROM TblInventoryItems WHERE ItemID = ?")) {
            statement.setInt(1, itemId);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean updateQuantity(int itemId, int quantity) throws SQLException {
        return updateInteger("Quantity", itemId, quantity);
    }

    public boolean updateLowStockThreshold(int itemId, int threshold) throws SQLException {
        return updateInteger("LowStockAlertThreshold", itemId, threshold);
    }

    private boolean updateInteger(String column, int itemId, int value) throws SQLException {
        String sql = "UPDATE TblInventoryItems SET " + column + " = ? WHERE ItemID = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, value);
            statement.setInt(2, itemId);
            return statement.executeUpdate() > 0;
        }
    }
}
