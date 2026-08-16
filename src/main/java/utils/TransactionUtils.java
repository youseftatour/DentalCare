package utils;

import java.sql.Connection;
import java.sql.SQLException;

public final class TransactionUtils {
    private TransactionUtils() {
    }

    public static void rollbackQuietly(Connection connection) {
        if (connection == null) return;
        try {
            connection.rollback();
        } catch (SQLException ignored) {
            // Preserve the original database failure.
        }
    }

    public static void restoreAutoCommitAndClose(Connection connection) {
        if (connection == null) return;
        try {
            connection.setAutoCommit(true);
        } catch (SQLException ignored) {
            // Still attempt to close the connection.
        }
        try {
            connection.close();
        } catch (SQLException ignored) {
            // Cleanup must not mask the workflow result.
        }
    }
}
