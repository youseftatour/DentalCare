package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {

    private static final String DB_PATH = "jdbc:ucanaccess://DentalCare_Nimbus2000s.accdb";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_PATH);
    }
}
